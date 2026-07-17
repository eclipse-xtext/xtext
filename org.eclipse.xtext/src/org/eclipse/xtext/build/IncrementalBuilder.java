/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IContextualOutputConfigurationProvider;
import org.eclipse.xtext.generator.IContextualOutputConfigurationProvider2;
import org.eclipse.xtext.generator.IFilePostProcessor;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IFileSystemAccessExtension3;
import org.eclipse.xtext.generator.IShouldGenerate;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.generator.URIBasedFileSystemAccess;
import org.eclipse.xtext.generator.trace.TraceFileNameProvider;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.IResourceServiceProviderExtension;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.clustering.DisabledClusteringPolicy;
import org.eclipse.xtext.resource.clustering.IResourceClusteringPolicy;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.resource.persistence.IResourceStorageFacade;
import org.eclipse.xtext.resource.persistence.SerializableResourceDescription;
import org.eclipse.xtext.resource.persistence.SourceLevelURIsAdapter;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.RuntimeIOException;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.IProjectConfigProvider;
import org.eclipse.xtext.workspace.ISourceFolder;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @since 2.9
 */
public class IncrementalBuilder {
	/**
	 * The result of the build. Encapsulates the new index state and the list of changes.
	 */
	public static class Result {
		private final IndexState indexState;

		private final List<IResourceDescription.Delta> affectedResources;

		public Result(IndexState indexState, List<IResourceDescription.Delta> affectedResources) {
			this.indexState = indexState;
			this.affectedResources = affectedResources;
		}

		public IndexState getIndexState() {
			return indexState;
		}

		public List<IResourceDescription.Delta> getAffectedResources() {
			return affectedResources;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (indexState == null ? 0 : indexState.hashCode());
			return prime * result + (affectedResources == null ? 0 : affectedResources.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			IncrementalBuilder.Result other = (IncrementalBuilder.Result) obj;
			if (indexState == null) {
				if (other.indexState != null) {
					return false;
				}
			} else if (!indexState.equals(other.indexState)) {
				return false;
			}
			if (affectedResources == null) {
				if (other.affectedResources != null) {
					return false;
				}
			} else if (!affectedResources.equals(other.affectedResources)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("indexState", indexState);
			b.add("affectedResources", affectedResources);
			return b.toString();
		}

	}

	/**
	 * Builder instance that is bound to a single running build.
	 */
	public static class InternalStatefulIncrementalBuilder {

		/**
		 * Creates an {@link IFileSystemAccess file system access} that is backed by a {@link URIConverter}.
		 */
		@Singleton
		public static class URIBasedFileSystemAccessFactory {
			@Inject
			private IContextualOutputConfigurationProvider outputConfigurationProvider;

			@Inject
			private IFilePostProcessor postProcessor;

			@Inject(optional = true)
			private IEncodingProvider encodingProvider;

			@Inject
			private TraceFileNameProvider traceFileNameProvider;

			@Inject
			private TraceRegionSerializer traceRegionSerializer;

			@Inject(optional = true)
			private IProjectConfigProvider projectConfigProvider;

			public URIBasedFileSystemAccess newFileSystemAccess(Resource resource, BuildRequest request) {
				URIBasedFileSystemAccess uriBasedFileSystemAccess = new URIBasedFileSystemAccess();
				uriBasedFileSystemAccess.setOutputConfigurations(IterableExtensions.toMap(
						outputConfigurationProvider.getOutputConfigurations(resource), OutputConfiguration::getName));
				uriBasedFileSystemAccess.setPostProcessor(postProcessor);
				if (encodingProvider != null) {
					uriBasedFileSystemAccess.setEncodingProvider(encodingProvider);
				}
				uriBasedFileSystemAccess.setTraceFileNameProvider(traceFileNameProvider);
				uriBasedFileSystemAccess.setTraceRegionSerializer(traceRegionSerializer);
				uriBasedFileSystemAccess.setGenerateTraces(true);
				uriBasedFileSystemAccess.setBaseDir(request.getBaseDir());
				if (projectConfigProvider != null) {
					IProjectConfig projectConfig = projectConfigProvider.getProjectConfig(resource.getResourceSet());
					if (projectConfig != null) {
						ISourceFolder sourceFolder = projectConfig.findSourceFolderContaining(resource.getURI());
						if (sourceFolder != null) {
							uriBasedFileSystemAccess.setCurrentSource(sourceFolder.getName());
						}
					}
				}
				uriBasedFileSystemAccess.setConverter(resource.getResourceSet().getURIConverter());
				return uriBasedFileSystemAccess;
			}
		}

		private BuildContext context;

		private BuildRequest request;

		@Inject
		private Indexer indexer;

		@Inject
		private OperationCanceledManager operationCanceledManager;

		protected void unloadResource(URI uri) {
			unloadResource(uri, x -> true);
		}

		/**
		 * Unload the resource with the given uri if it satisfies the given condition.
		 * 
		 * @param uri
		 *            the URI, must not be {@code null}
		 * @param condition
		 *            the condition, must not be {@code null}
		 * @since 2.28
		 */
		protected void unloadResource(URI uri, Predicate<Resource> condition) {
			XtextResourceSet resourceSet = request.getResourceSet();
			Resource resource = resourceSet.getResource(uri, false);
			if (resource != null && condition.test(resource)) {
				resourceSet.getResources().remove(resource);
				// proxify
				resource.unload();
			}
		}

		/**
		 * Unload the given uris that are not already in the unloaded set.
		 * 
		 * @param uriStream
		 *            the uris to unload.
		 * @param unloaded
		 *            the set of unloaded uris, to which the given uris to unload will be added.
		 * @since 2.33
		 */
		protected void unloadResources(final Stream<URI> uriStream, final Set<URI> unloaded) {
			uriStream.forEach(uri -> {
				if (unloaded.add(uri)) {
					unloadResource(uri);
				}
			});
		}

		public IncrementalBuilder.Result launch() {
			Set<URI> unloaded = new HashSet<>();
			unloadResources(request.getDeletedFiles().stream(), unloaded);
			unloadResources(request.getDirtyFiles().stream(), unloaded);

			Source2GeneratedMapping newSource2GeneratedMapping = request.getState().getFileMappings();
			deleteGeneratedSources(getRequest().getDeletedFiles(), newSource2GeneratedMapping);
			Indexer.IndexResult result = indexer.computeAndIndexAffected(request, context);
			operationCanceledManager.checkCanceled(request.getCancelIndicator());

			List<IResourceDescription.Delta> resolvedDeltas = new ArrayList<>();
			result.getResourceDeltas().stream().filter(delta -> delta.getNew() == null).forEach(resolvedDeltas::add);
			unloadResources(result.getResourceDeltas().stream().filter(it -> it.getOld() != null).map(Delta::getUri),
					unloaded);
			List<URI> toBeBuilt = result.getResourceDeltas().stream().filter((it) -> it.getNew() != null)
					.map(Delta::getUri).collect(Collectors.toList());

			installSourceLevelURIs(toBeBuilt);
			Iterable<IResourceDescription.Delta> deltas = context.executeClustered(toBeBuilt,
					resource -> buildResource(resource, result.getNewIndex(), newSource2GeneratedMapping));

			Iterables.addAll(resolvedDeltas, deltas);
			return new IncrementalBuilder.Result(request.getState(), resolvedDeltas);
		}

		/**
	 	* Return a new Serializable resource description from a {@link IResourceDescription}.
	 	*
	 	* @param description
	 	*            the serializable description, must not be {@code null}
	 	* @return the new serializable resource description, never {@code null}
	 	* @since 2.26
	 	*/
		protected IResourceDescription getSerializableResourceDescription(IResourceDescription description) {
			return SerializableResourceDescription.createCopy(description);
		}

		/**
		 * @since 2.28
		 */
		protected OperationCanceledManager getOperationCanceledManager() {
			return operationCanceledManager;
		}

		/**
		 * @since 2.28
		 */
		protected Indexer getIndexer() {
			return indexer;
		}

		/**
		 * @since 2.33
		 */
		protected void deleteGeneratedSources(final List<URI> deletedSources,
				final Source2GeneratedMapping newSource2GeneratedMapping) {
			for (URI source : deletedSources) {
				request.getAfterValidate().afterValidate(source, Collections.emptyList());
				Map<URI, String> outputConfigs = newSource2GeneratedMapping.deleteSourceAndGetOutputConfigs(source);
				for (URI generated : outputConfigs.keySet()) {
					IResourceServiceProvider serviceProvider = context.getResourceServiceProvider(source);
					XtextResourceSet resourceSet = request.getResourceSet();
					Set<OutputConfiguration> configs = serviceProvider
							.get(IContextualOutputConfigurationProvider2.class).getOutputConfigurations(resourceSet);
					String configName = outputConfigs.get(generated);
					OutputConfiguration config = FluentIterable.from(configs)
							.firstMatch(it -> it.getName().equals(configName)).orNull();
					if (config != null && config.isCleanUpDerivedResources()) {
						try {
							resourceSet.getURIConverter().delete(generated, Collections.emptyMap());
							request.getAfterDeleteFile().apply(generated);
						} catch (IOException e) {
							throw new RuntimeIOException(e);
						}
					}
				}
			}
		}

		/**
		 * @since 2.33
		 */
		protected IResourceDescription.Delta buildResource(final Resource resource,
				final ResourceDescriptionsData newIndex, final Source2GeneratedMapping newSource2GeneratedMapping) {
			CancelIndicator cancelIndicator = request.getCancelIndicator();
			operationCanceledManager.checkCanceled(cancelIndicator);
			// trigger init
			resource.getContents();
			EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
			operationCanceledManager.checkCanceled(cancelIndicator);
			IResourceServiceProvider serviceProvider = getResourceServiceProvider(resource);
			IResourceDescription.Manager manager = serviceProvider.getResourceDescriptionManager();
			IResourceDescription description = manager.getResourceDescription(resource);
			IResourceDescription copiedDescription = getSerializableResourceDescription(description);
			newIndex.addDescription(resource.getURI(), copiedDescription);
			operationCanceledManager.checkCanceled(cancelIndicator);
			if (!request.isIndexOnly() && validate(resource)
					&& serviceProvider.get(IShouldGenerate.class).shouldGenerate(resource, CancelIndicator.NullImpl)) {
				operationCanceledManager.checkCanceled(cancelIndicator);
				generate(resource, request, newSource2GeneratedMapping);
			}
			IResourceDescription old = context.getOldState().getResourceDescriptions()
					.getResourceDescription(resource.getURI());
			return manager.createDelta(old, copiedDescription);
		}
		  
		/**
		 * @since 2.28
		 */
		protected IResourceServiceProvider getResourceServiceProvider(Resource resource) {
			if (resource instanceof XtextResource) {
				return ((XtextResource) resource).getResourceServiceProvider();
			}
			return context.getResourceServiceProvider(resource.getURI());
		}

		/**
		 * Validate the resource and return true, if the build should proceed for the current state.
		 */
		protected boolean validate(Resource resource) {
			IResourceValidator resourceValidator = getResourceServiceProvider(resource).getResourceValidator();
			if (resourceValidator == null) {
				return true;
			}
			List<Issue> validationResult = resourceValidator.validate(resource, CheckMode.ALL,
					request.getCancelIndicator());
			return request.getAfterValidate().afterValidate(resource.getURI(), validationResult);
		}

		/**
		 * Generate code for the given resource
		 */
		protected void generate(Resource resource, BuildRequest request, Source2GeneratedMapping newMappings) {
			IResourceServiceProvider serviceProvider = getResourceServiceProvider(resource);
			Set<URI> previous = newMappings.deleteSource(resource.getURI());
			URIBasedFileSystemAccess fileSystemAccess = createFileSystemAccess(serviceProvider, resource);
			fileSystemAccess.setBeforeWrite((uri, outputCfgName, contents) -> {
				newMappings.addSource2Generated(resource.getURI(), uri, outputCfgName);
				previous.remove(uri);
				request.getAfterGenerateFile().apply(resource.getURI(), uri);
				return contents;
			});
			fileSystemAccess.setBeforeDelete((uri) -> {
				newMappings.deleteGenerated(uri);
				request.getAfterDeleteFile().apply(uri);
				return true;
			});
			fileSystemAccess.setContext(resource);
			if (request.isWriteStorageResources()) {
				storeBinaryResource(resource, fileSystemAccess);
			}
			GeneratorContext generatorContext = new GeneratorContext();
			generatorContext.setCancelIndicator(request.getCancelIndicator());
			GeneratorDelegate generator = serviceProvider.get(GeneratorDelegate.class);
			generator.generate(resource, fileSystemAccess, generatorContext);
			XtextResourceSet resourceSet = request.getResourceSet();
			for (URI noLongerCreated : previous) {
				try {
					resourceSet.getURIConverter().delete(noLongerCreated, Collections.emptyMap());
					request.getAfterDeleteFile().apply(noLongerCreated);
				} catch (IOException e) {
					throw new RuntimeIOException(e);
				}
			}
		}

		/**
		 * @since 2.28
		 */
		protected boolean isLoadedFromStorage(Resource resource) {
			return resource instanceof StorageAwareResource && ((StorageAwareResource) resource).isLoadedFromStorage();
		}

		/**
		 * @since 2.28
		 */
		protected void installSourceLevelURIs(List<URI> uris) {
			Set<URI> sourceLevelURIs = request.getSourceLevelUris();
			ResourceSet resourceSet = request.getResourceSet();
			for (URI uri : uris) {
				if (isSource(uri)) {
					sourceLevelURIs.add(uri);
					unloadResource(uri, this::isLoadedFromStorage);
				}
			}
			
			SourceLevelURIsAdapter.setSourceLevelUrisWithoutCopy(resourceSet, sourceLevelURIs);
		}

		/**
		 * Return true if the given uri must be loaded from source.
		 * 
		 * @since 2.28
		 */
		protected boolean isSource(URI uri) {
			IResourceServiceProvider provider = context.getResourceServiceProvider(uri);
			return provider instanceof IResourceServiceProviderExtension
					&& ((IResourceServiceProviderExtension) provider).isSource(uri);
		}
		
		/**
		 * Stores resource as a binary.
		 *
		 * @param resource
		 *            resource to store, must not be {@code null}
		 * @param fileSystemAccess
		 *            the file system access, must not be {@code null}
		 * 
		 * @since 2.28
		 */
		protected void storeBinaryResource(final Resource resource, IFileSystemAccessExtension3 fileSystemAccess) {
			if (resource instanceof StorageAwareResource) {
				IResourceStorageFacade resourceStorageFacade = ((StorageAwareResource) resource)
						.getResourceStorageFacade();
				if (resourceStorageFacade != null) {
					resourceStorageFacade.saveResource((StorageAwareResource) resource, fileSystemAccess);
					request.getSourceLevelUris().remove(resource.getURI());
				}
			}
		}

		protected URIBasedFileSystemAccess createFileSystemAccess(IResourceServiceProvider serviceProvider,
				Resource resource) {
			return serviceProvider.get(URIBasedFileSystemAccessFactory.class).newFileSystemAccess(resource, request);
		}

		protected BuildContext getContext() {
			return context;
		}

		protected void setContext(BuildContext context) {
			this.context = context;
		}

		protected BuildRequest getRequest() {
			return request;
		}

		protected void setRequest(BuildRequest request) {
			this.request = request;
		}
	}

	@Inject
	private Provider<IncrementalBuilder.InternalStatefulIncrementalBuilder> provider;

	@Inject
	private OperationCanceledManager operationCanceledManager;

	/**
	 * Run the build without clustering.
	 */
	public IncrementalBuilder.Result build(BuildRequest request,
			Function1<? super URI, ? extends IResourceServiceProvider> languages) {
		return build(request, languages, new DisabledClusteringPolicy());
	}

	/**
	 * Run the build.
	 */
	public IncrementalBuilder.Result build(BuildRequest request,
			Function1<? super URI, ? extends IResourceServiceProvider> languages,
			IResourceClusteringPolicy clusteringPolicy) {
		XtextResourceSet resourceSet = request.getResourceSet();
		IndexState oldState = new IndexState(request.getState().getResourceDescriptions().copy(),
				request.getState().getFileMappings().copy());
		BuildContext context = new BuildContext(languages, resourceSet, oldState, clusteringPolicy,
				request.getCancelIndicator());
		IncrementalBuilder.InternalStatefulIncrementalBuilder builder = provider.get();
		builder.setContext(context);
		builder.setRequest(request);
		try {
			return builder.launch();
		} catch (Throwable t) {
			operationCanceledManager.propagateIfCancelException(t);
			throw t;
		}
	}
}
