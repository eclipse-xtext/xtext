/*******************************************************************************
 * Copyright (c) 2016, 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.build.BuildRequest;
import org.eclipse.xtext.build.IncrementalBuilder;
import org.eclipse.xtext.build.IndexState;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.IExternalContentSupport;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ChunkedResourceDescriptions;
import org.eclipse.xtext.resource.impl.ProjectDescription;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.IFileSystemScanner;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.ISourceFolder;
import org.eclipse.xtext.workspace.ProjectConfigAdapter;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @since 2.11
 */
public class ProjectManager {
	@Inject
	protected IncrementalBuilder incrementalBuilder;

	@Inject
	protected Provider<XtextResourceSet> resourceSetProvider;

	@Inject
	protected IResourceServiceProvider.Registry languagesRegistry;

	@Inject
	protected IFileSystemScanner fileSystemScanner;

	@Inject
	protected IExternalContentSupport externalContentSupport;

	private IndexState indexState = new IndexState();

	private URI baseDir;

	private Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor;

	private Provider<Map<String, ResourceDescriptionsData>> indexProvider;

	private IExternalContentSupport.IExternalContentProvider openedDocumentsContentProvider;

	private XtextResourceSet resourceSet;

	private ProjectDescription projectDescription;

	private IProjectConfig projectConfig;

	public void initialize(ProjectDescription description, IProjectConfig projectConfig,
			Procedure2<? super URI, ? super Iterable<Issue>> acceptor,
			IExternalContentSupport.IExternalContentProvider openedDocumentsContentProvider,
			Provider<Map<String, ResourceDescriptionsData>> indexProvider, CancelIndicator cancelIndicator) {
		this.projectDescription = description;
		this.projectConfig = projectConfig;
		this.baseDir = projectConfig.getPath();
		this.issueAcceptor = acceptor;
		this.openedDocumentsContentProvider = openedDocumentsContentProvider;
		this.indexProvider = indexProvider;
	}

	/**
	 * Initial build of this project.
	 */
	public IncrementalBuilder.Result doInitialBuild(CancelIndicator cancelIndicator) {
		List<URI> allUris = new ArrayList<>();
		for (ISourceFolder srcFolder : projectConfig.getSourceFolders()) {
			allUris.addAll(srcFolder.getAllResources(fileSystemScanner));
		}
		return doBuild(allUris, Collections.emptyList(), Collections.emptyList(), cancelIndicator);
	}

	/**
	 * Build this project.
	 */
	public IncrementalBuilder.Result doBuild(List<URI> dirtyFiles, List<URI> deletedFiles,
			List<IResourceDescription.Delta> externalDeltas, CancelIndicator cancelIndicator) {
		BuildRequest request = newBuildRequest(dirtyFiles, deletedFiles, externalDeltas, cancelIndicator);
		IncrementalBuilder.Result result = incrementalBuilder.build(request,
				languagesRegistry::getResourceServiceProvider);
		indexState = result.getIndexState();
		resourceSet = request.getResourceSet();
		indexProvider.get().put(projectDescription.getName(), indexState.getResourceDescriptions());
		return result;
	}

	/**
	 * Creates a new build request for this project.
	 */
	protected BuildRequest newBuildRequest(List<URI> changedFiles, List<URI> deletedFiles,
			List<IResourceDescription.Delta> externalDeltas, CancelIndicator cancelIndicator) {
		BuildRequest result = new BuildRequest();
		result.setBaseDir(baseDir);
		result.setState(
				new IndexState(indexState.getResourceDescriptions().copy(), indexState.getFileMappings().copy()));
		result.setResourceSet(createFreshResourceSet(result.getState().getResourceDescriptions()));
		result.setDirtyFiles(changedFiles);
		result.setDeletedFiles(deletedFiles);
		result.setExternalDeltas(externalDeltas);
		result.setAfterValidate((URI uri, Iterable<Issue> issues) -> {
			issueAcceptor.apply(uri, issues);
			return true;
		});
		result.setCancelIndicator(cancelIndicator);
		result.setIndexOnly(projectConfig.isIndexOnly());
		return result;
	}

	/**
	 * Create and configure a new resource set for this project.
	 */
	public XtextResourceSet createNewResourceSet(ResourceDescriptionsData newIndex) {
		XtextResourceSet result = resourceSetProvider.get();
		projectDescription.attachToEmfObject(result);
		ProjectConfigAdapter.install(result, projectConfig);
		ChunkedResourceDescriptions index = new ChunkedResourceDescriptions(indexProvider.get(), result);
		index.setContainer(projectDescription.getName(), newIndex);
		externalContentSupport.configureResourceSet(result, openedDocumentsContentProvider);
		return result;
	}

	/**
	 * Create an empty resource set.
	 */
	protected XtextResourceSet createFreshResourceSet(ResourceDescriptionsData newIndex) {
		if (resourceSet == null) {
			resourceSet = createNewResourceSet(newIndex);
		} else {
			ChunkedResourceDescriptions resDescs = ChunkedResourceDescriptions.findInEmfObject(resourceSet);
			for (Map.Entry<String, ResourceDescriptionsData> entry : indexProvider.get().entrySet()) {
				resDescs.setContainer(entry.getKey(), entry.getValue());
			}
			resDescs.setContainer(projectDescription.getName(), newIndex);
		}
		return resourceSet;
	}

	/**
	 * Create and configure a new live resource set for this project.
	 *
	 * @since 2.27
	 */
	public XtextResourceSet createLiveScopeResourceSet() {
		XtextResourceSet resourceSet = createNewResourceSet(getIndexState().getResourceDescriptions());
		resourceSet.getLoadOptions().put(ResourceDescriptionsProvider.LIVE_SCOPE, true);
		return resourceSet;
	}

	/**
	 * Get the resource with the given URI.
	 */
	public Resource getResource(URI uri) {
		Resource resource = resourceSet.getResource(uri, true);
		resource.getContents();
		return resource;
	}

	public void reportProjectIssue(String message, String code, Severity severity) {
		Issue.IssueImpl result = new Issue.IssueImpl();
		result.setMessage(message);
		result.setCode(code);
		result.setSeverity(severity);
		result.setUriToProblem(baseDir);
		issueAcceptor.apply(baseDir, ImmutableList.of(result));
	}

	public IndexState getIndexState() {
		return indexState;
	}

	protected void setIndexState(IndexState indexState) {
		this.indexState = indexState;
	}

	public URI getBaseDir() {
		return baseDir;
	}

	protected Procedure2<? super URI, ? super Iterable<Issue>> getIssueAcceptor() {
		return issueAcceptor;
	}

	protected Provider<Map<String, ResourceDescriptionsData>> getIndexProvider() {
		return indexProvider;
	}

	protected IExternalContentSupport.IExternalContentProvider getOpenedDocumentsContentProvider() {
		return openedDocumentsContentProvider;
	}

	public XtextResourceSet getResourceSet() {
		return resourceSet;
	}

	public ProjectDescription getProjectDescription() {
		return projectDescription;
	}

	public IProjectConfig getProjectConfig() {
		return projectConfig;
	}
	
	/**
	 * @since 2.21
	 */
	public void aboutToRemoveFromWorkspace() {
		for (ISourceFolder srcFolder : projectConfig.getSourceFolders()) {
			for (URI resourceURI: srcFolder.getAllResources(fileSystemScanner)) {
				issueAcceptor.apply(resourceURI, Collections.emptyList());
			}
		}
	}

	/**
	 * Notifies the project manager that the project has been removed from the workspace.
	 *
	 * <p>No-op if this project has no resource set yet. Must be called on the workspace write thread.</p>
	 *
	 * @since 2.45
	 */
	public void notifyProjectRemoved(String projectName) {
		if (resourceSet != null) {
			ChunkedResourceDescriptions index = ChunkedResourceDescriptions.findInEmfObject(resourceSet);
			if (index != null) {
				index.removeContainer(projectName);
			}
		}
	}
}
