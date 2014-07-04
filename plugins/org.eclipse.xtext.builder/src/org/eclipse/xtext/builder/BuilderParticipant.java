/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.builder;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static org.eclipse.xtext.ui.util.ResourceUtil.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.builder.DerivedResourceMarkers.GeneratorIdProvider;
import org.eclipse.xtext.builder.EclipseResourceFileSystemAccess2.IFileCallback;
import org.eclipse.xtext.builder.preferences.BuilderPreferenceAccess;
import org.eclipse.xtext.generator.IDerivedResourceMarkers;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.generator.OutputConfiguration.SourceMapping;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.clustering.IResourceClusteringPolicy;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.internal.Stopwatches;
import org.eclipse.xtext.util.internal.Stopwatches.StoppedTask;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Michael Clay
 * @author Anton Kosyakov
 * @since 2.1
 */
public class BuilderParticipant implements IXtextBuilderParticipant {

	private final static Logger logger = Logger.getLogger(BuilderParticipant.class);

	@Inject
	private IResourceClusteringPolicy clusteringPolicy;

	@Inject
	private Provider<EclipseResourceFileSystemAccess2> fileSystemAccessProvider;

	@Inject
	private IGenerator generator;

	@Inject
	private IResourceServiceProvider resourceServiceProvider;

	@Inject
	private IStorage2UriMapper storage2UriMapper;

	@Inject
	private IDerivedResourceMarkers derivedResourceMarkers;

	@Inject
	private GeneratorIdProvider generatorIdProvider;

	private EclipseOutputConfigurationProvider outputConfigurationProvider;
	private BuilderPreferenceAccess builderPreferenceAccess;
	
	/**
	 * @since 2.7
	 */
	public IStorage2UriMapper getStorage2UriMapper() {
		return storage2UriMapper;
	}

	/**
	 * @since 2.4
	 */
	protected IDerivedResourceMarkers getDerivedResourceMarkers() {
		return derivedResourceMarkers;
	}

	/**
	 * @since 2.4
	 */
	protected GeneratorIdProvider getGeneratorIdProvider() {
		return generatorIdProvider;
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public BuilderPreferenceAccess getBuilderPreferenceAccess() {
		return builderPreferenceAccess;
	}

	/**
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Inject
	public void setBuilderPreferenceAccess(BuilderPreferenceAccess builderPreferenceAccess) {
		this.builderPreferenceAccess = builderPreferenceAccess;
	}

	public EclipseOutputConfigurationProvider getOutputConfigurationProvider() {
		return outputConfigurationProvider;
	}

	/**
	 * @since 2.2
	 */
	public IGenerator getGenerator() {
		return generator;
	}

	/**
	 * @since 2.2
	 */
	public IResourceServiceProvider getResourceServiceProvider() {
		return resourceServiceProvider;
	}

	@Inject
	public void setOutputConfigurationProvider(EclipseOutputConfigurationProvider outputConfigurationProvider) {
		this.outputConfigurationProvider = outputConfigurationProvider;
	}

	/**
	 * @since 2.4
	 */
	protected Map<OutputConfiguration, Iterable<IMarker>> getGeneratorMarkers(IProject builtProject,
			Collection<OutputConfiguration> outputConfigurations) throws CoreException {
		Map<OutputConfiguration, Iterable<IMarker>> generatorMarkers = newHashMap();
		for (OutputConfiguration config : outputConfigurations) {
			if (config.isCleanUpDerivedResources()) {
				List<IMarker> markers = Lists.newArrayList();
				for (IContainer container : getOutputs(builtProject, config)) {
					Iterables.addAll(
							markers,
							derivedResourceMarkers.findDerivedResourceMarkers(container,
									generatorIdProvider.getGeneratorIdentifier()));
				}
				generatorMarkers.put(config, markers);
			}
		}
		return generatorMarkers;
	}

	public void build(final IBuildContext context, IProgressMonitor monitor) throws CoreException {
		if (!isEnabled(context)) {
			return;
		}

		final List<IResourceDescription.Delta> deltas = getRelevantDeltas(context);
		if (deltas.isEmpty()) {
			return;
		}
		
		StoppedTask task = Stopwatches.forTask("org.eclipse.xtext.builder.BuilderParticipant.build(IBuildContext, IProgressMonitor)");
		try {
			task.start();
			
			// monitor handling
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
	
			EclipseResourceFileSystemAccess2 access = fileSystemAccessProvider.get();
			IProject builtProject = context.getBuiltProject();
			access.setProject(builtProject);
			Map<String, OutputConfiguration> outputConfigurations = getOutputConfigurations(context);
			refreshOutputFolders(context, outputConfigurations, subMonitor.newChild(1));
			access.setOutputConfigurations(outputConfigurations);
			if (context.getBuildType() == BuildType.CLEAN || context.getBuildType() == BuildType.RECOVERY) {
				SubMonitor cleanMonitor = SubMonitor.convert(subMonitor.newChild(1), outputConfigurations.size());
				for (OutputConfiguration config : outputConfigurations.values()) {
					cleanOutput(context, config, access, cleanMonitor.newChild(1));
				}
				if (context.getBuildType() == BuildType.CLEAN)
					return;
			}
			Map<OutputConfiguration, Iterable<IMarker>> generatorMarkers = getGeneratorMarkers(builtProject, outputConfigurations.values());
			doBuild(deltas, outputConfigurations, generatorMarkers, context, access, monitor);

		} finally {
			task.stop();
		}
	}

	/**
	 * @since 2.7
	 */
	protected void doBuild(List<IResourceDescription.Delta> deltas, 
			Map<String, OutputConfiguration> outputConfigurations,
			Map<OutputConfiguration, Iterable<IMarker>> generatorMarkers, IBuildContext context,
			EclipseResourceFileSystemAccess2 access, IProgressMonitor progressMonitor) throws CoreException {
		final int numberOfDeltas = deltas.size();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 2 * numberOfDeltas);
		
		int clusterIndex = 0;
		for (int i = 0; i < numberOfDeltas; i++) {
			IResourceDescription.Delta delta = deltas.get(i);
			
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			subMonitor.subTask("Compiling " + delta.getUri().lastSegment() + " (" + i + " of " + numberOfDeltas + ")");
			access.setMonitor(subMonitor.newChild(1));
			
			if (delta.getNew() != null && !clusteringPolicy.continueProcessing(context.getResourceSet(), delta.getUri(), clusterIndex)) {
				clearResourceSet(context.getResourceSet());
				clusterIndex = 0;
			}

			Set<IFile> derivedResources = getDerivedResources(delta, outputConfigurations, generatorMarkers);
			access.setPostProcessor(getPostProcessor(delta, context, derivedResources));
			
			if (doGenerate(delta, context, access)) {
				clusterIndex++;
				access.flushSourceTraces();
			}
			
			SubMonitor deleteMonitor = SubMonitor.convert(subMonitor.newChild(1), derivedResources.size());
			cleanDerivedResources(delta, derivedResources, context, access, deleteMonitor);
		}
	}

	/**
	 * Clears the content of the resource set without sending notifications.
	 * This avoids unnecessary, explicit unloads.
	 * @since 2.7
	 */
	protected void clearResourceSet(ResourceSet resourceSet) {
		boolean wasDeliver = resourceSet.eDeliver();
		try {
			resourceSet.eSetDeliver(false);
			resourceSet.getResources().clear();
		} finally {
			resourceSet.eSetDeliver(wasDeliver);
		}
	}

	/**
	 * @since 2.7
	 */
	protected void logErrorDuringCompilation(final URI uri, Throwable e) {
		Throwable cause = e;
		if (cause instanceof CoreException) {
			cause = cause.getCause();
		}
		if (uri == null) {
			logger.error("Error during compilation.", e);
		} else {
			logger.error("Error during compilation of '" + uri + "'.", e);
		}
	}

	/**
	 * @since 2.7
	 */
	protected void cleanDerivedResources(Delta delta, Set<IFile> derivedResources, IBuildContext context,
			EclipseResourceFileSystemAccess2 access, IProgressMonitor deleteMonitor) throws CoreException {
		String uri = delta.getUri().toString();
		for (IFile iFile : newLinkedHashSet(derivedResources)) {
			IMarker marker = derivedResourceMarkers.findDerivedResourceMarker(iFile, uri);
			if (marker != null)
				marker.delete();
			if (derivedResourceMarkers.findDerivedResourceMarkers(iFile).length == 0) {
				access.deleteFile(iFile, deleteMonitor);
				context.needRebuild();
			}
		}
	}

	/**
	 * @since 2.7
	 */
	protected IFileCallback getPostProcessor(Delta delta, final IBuildContext context,
			final Set<IFile> derivedResources) {
		final String uri = delta.getUri().toString();
		return new EclipseResourceFileSystemAccess2.IFileCallback() {

			public boolean beforeFileDeletion(IFile file) {
				derivedResources.remove(file);
				context.needRebuild();
				return true;
			}

			public void afterFileUpdate(IFile file) {
				handleFileAccess(file);
			}

			public void afterFileCreation(IFile file) {
				handleFileAccess(file);
			}

			protected void handleFileAccess(IFile file) {
				try {
					derivedResources.remove(file);
					derivedResourceMarkers.installMarker(file, uri);
					context.needRebuild();
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}

		};
	}

	/**
	 * @since 2.7
	 */
	protected Set<IFile> getDerivedResources(Delta delta, 
			final Map<String, OutputConfiguration> outputConfigurations,
			Map<OutputConfiguration, Iterable<IMarker>> generatorMarkers) {
		String uri = delta.getUri().toString();
		Set<IFile> derivedResources = newLinkedHashSet();
		for (OutputConfiguration config : outputConfigurations.values()) {
			if (config.isCleanUpDerivedResources()) {
				Iterable<IMarker> markers = generatorMarkers.get(config);
				for (IMarker marker : markers) {
					String source = derivedResourceMarkers.getSource(marker);
					if (source != null && source.equals(uri))
						derivedResources.add((IFile) marker.getResource());
				}
			}
		}
		return derivedResources;
	}

	protected boolean isEnabled(final IBuildContext context) {
		return builderPreferenceAccess.isAutoBuildEnabled(context.getBuiltProject());
	}

	/**
	 * @since 2.3
	 */
	protected List<IResourceDescription.Delta> getRelevantDeltas(IBuildContext context) {
		List<IResourceDescription.Delta> result = newArrayList();
		for (IResourceDescription.Delta delta : context.getDeltas()) {
			if (resourceServiceProvider.canHandle(delta.getUri()))
				result.add(delta);
		}
		return result;
	}

	protected void refreshOutputFolders(IBuildContext ctx, Map<String, OutputConfiguration> outputConfigurations,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, outputConfigurations.size());
		for (OutputConfiguration config : outputConfigurations.values()) {
			SubMonitor child = subMonitor.newChild(1);
			final IProject project = ctx.getBuiltProject();
			for (IContainer container : getOutputs(project, config)) {
				container.refreshLocal(IResource.DEPTH_INFINITE, child);
			}
		}
	}

	protected void cleanOutput(IBuildContext ctx, OutputConfiguration config, IProgressMonitor monitor)
			throws CoreException {
		cleanOutput(ctx, config, null, monitor);
	}

	/**
	 * @since 2.5
	 */
	protected void cleanOutput(IBuildContext ctx, OutputConfiguration config, EclipseResourceFileSystemAccess2 access,
			IProgressMonitor monitor) throws CoreException {
		final IProject project = ctx.getBuiltProject();
		for (IContainer container : getOutputs(project, config)) {
			if (!container.exists()) {
				return;
			}
			if (config.isCanClearOutputDirectory()) {
				for (IResource resource : container.members()) {
					if (!config.isKeepLocalHistory()) {
						resource.delete(IResource.NONE, monitor);
					} else if (access == null) {
						resource.delete(IResource.KEEP_HISTORY, monitor);
					} else {
						delete(resource, config, access, monitor);
					}
				}
			} else if (config.isCleanUpDerivedResources()) {
				List<IFile> resources = derivedResourceMarkers.findDerivedResources(container, null);
				for (IFile iFile : resources) {
					if (access != null) {
						access.deleteFile(iFile, config.getName(), monitor);
					} else {
						iFile.delete(config.isKeepLocalHistory() ? IResource.KEEP_HISTORY : IResource.NONE, monitor);
					}
				}
			}
		}

	}

	private void delete(IResource resource, OutputConfiguration config, EclipseResourceFileSystemAccess2 access,
			IProgressMonitor monitor) throws CoreException {
		if (resource instanceof IContainer) {
			IContainer container = (IContainer) resource;
			for (IResource child : container.members()) {
				delete(child, config, access, monitor);
			}
			container.delete(IResource.KEEP_HISTORY, monitor);
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			access.deleteFile(file, config.getName(), monitor);
		} else {
			resource.delete(IResource.KEEP_HISTORY, monitor);
		}
	}

	/**
	 * @since 2.7
	 */
	protected boolean doGenerate(IResourceDescription.Delta delta, IBuildContext context, IFileSystemAccess access) {
		if (delta.getNew() != null) {
			try {
				handleChangedContents(delta, context, access);
			} catch (OperationCanceledException e) {
				throw e;
			} catch (Exception e) {
				logErrorDuringCompilation(delta.getUri(), e);
			}
			return true;
		}
		return false;
	}

	/**
	 * @since 2.7
	 */
	protected void handleChangedContents(Delta delta, IBuildContext context, IFileSystemAccess access) throws CoreException {
		handleChangedContents(delta, context, (EclipseResourceFileSystemAccess2) access);
	}

	protected void handleChangedContents(Delta delta, IBuildContext context,
			EclipseResourceFileSystemAccess2 fileSystemAccess) throws CoreException {
		// TODO: we will run out of memory here if the number of deltas is large enough
		Resource resource = context.getResourceSet().getResource(delta.getUri(), true);
		if (shouldGenerate(resource, context)) {
			try {
				registerCurrentSourceFolder(context, delta, fileSystemAccess);
				generator.doGenerate(resource, fileSystemAccess);
			} catch (RuntimeException e) {
				if (e.getCause() instanceof CoreException) {
					throw (CoreException) e.getCause();
				}
				throw e;
			}
		}
	}

	/**
	 * @since 2.6
	 */
	protected void registerCurrentSourceFolder(IBuildContext context, Delta delta, EclipseResourceFileSystemAccess2 fileSystemAccess) {
		String sourceFolder = getCurrentSourceFolder(context, delta);
		if (sourceFolder != null) {
			fileSystemAccess.setCurrentSource(sourceFolder);
		}
	}

	/**
	 * @since 2.7
	 */
	protected String getCurrentSourceFolder(IBuildContext context, Delta delta) {
		Iterable<org.eclipse.xtext.util.Pair<IStorage, IProject>> storages = storage2UriMapper.getStorages(delta.getUri());
		for (org.eclipse.xtext.util.Pair<IStorage, IProject> pair : storages) {
			final IResource resource = (IResource) pair.getFirst();
			IProject project = pair.getSecond();
			for (OutputConfiguration output : getOutputConfigurations(context).values()) {
				for (SourceMapping sourceMapping : output.getSourceMappings()) {
					IContainer folder = ResourceUtil.getContainer(project, sourceMapping.getSourceFolder());
					if (folder.contains(resource)) {
						return sourceMapping.getSourceFolder();
					}
				}
			}
		}
		return null;
	}

	protected boolean shouldGenerate(Resource resource, IBuildContext context) {
		try {
			Iterable<Pair<IStorage, IProject>> storages = storage2UriMapper.getStorages(resource.getURI());
			for (Pair<IStorage, IProject> pair : storages) {
				if (pair.getFirst() instanceof IFile && pair.getSecond().equals(context.getBuiltProject())) {
					IFile file = (IFile) pair.getFirst();
					return file.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE) != IMarker.SEVERITY_ERROR;
				}
			}
			return false;
		} catch (CoreException exc) {
			throw new WrappedException(exc);
		}
	}

	protected Map<String, OutputConfiguration> getOutputConfigurations(IBuildContext context) {
		Set<OutputConfiguration> configurations = outputConfigurationProvider.getOutputConfigurations(context
				.getBuiltProject());
		return uniqueIndex(configurations, new Function<OutputConfiguration, String>() {
			public String apply(OutputConfiguration from) {
				return from.getName();
			}
		});
	}
	
	/**
	 * @since 2.6
	 */
	protected Set<IContainer> getOutputs(IProject project, OutputConfiguration outputConfiguration) {
		Set<IContainer> outputs = Sets.newLinkedHashSet();
		for (String outputPath : outputConfiguration.getOutputDirectories()) {
			IContainer output = getContainer(project, outputPath);
			outputs.add(output);
		}
		return outputs;
	}
}
