/*******************************************************************************
 * Copyright (c) 2016, 2022 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.eclipse.xtext.ide.server.BuildManager.Buildable;
import org.eclipse.xtext.resource.IExternalContentSupport;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ChunkedResourceDescriptions;
import org.eclipse.xtext.resource.impl.ProjectDescription;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.IWorkspaceConfig;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @since 2.11
 */
public class WorkspaceManager {
	
	private static final Logger LOG = Logger.getLogger(WorkspaceManager.class);

	@Inject
	private Provider<ProjectManager> projectManagerProvider;

	@Inject
	private IMultiRootWorkspaceConfigFactory workspaceConfigFactory;

	@Inject
	private IProjectDescriptionFactory projectDescriptionFactory;
	
	@Inject 
	private UriExtensions uriExtensions;

	private BuildManager buildManager;

	private List<WorkspaceFolder> workspaceFolders = Collections.emptyList();

	private Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor;

	private IWorkspaceConfig workspaceConfig;

	private final Map<String, ProjectManager> projectName2ProjectManager = new HashMap<>();
	
	private final List<ILanguageServerAccess.IBuildListener> buildListeners = new CopyOnWriteArrayList<>();

	private final Map<String, ResourceDescriptionsData> fullIndex = new HashMap<>();

	private final Map<URI, Document> openDocuments = createOpenDocuments();

	/**
	 * Add the listener to this workspace.
	 *
	 * @param listener
	 *            the new listener.
	 */
	public void addBuildListener(ILanguageServerAccess.IBuildListener listener) {
		buildListeners.add(listener);
	}

	/**
	 * Removes a build listener if it was previously registered
	 *
	 * @since 2.18
	 */
	public void removeBuildListener(ILanguageServerAccess.IBuildListener listener) {
		buildListeners.remove(listener);
	}

	private final IExternalContentSupport.IExternalContentProvider openedDocumentsContentProvider = new IExternalContentSupport.IExternalContentProvider() {
		@Override
		public IExternalContentSupport.IExternalContentProvider getActualContentProvider() {
			return this;
		}

		@Override
		public String getContent(URI uri) {
			Document document = openDocuments.get(uri);
			if (document != null) {
				return document.getContents();
			}
			return null;
		}

		@Override
		public boolean hasContent(URI uri) {
			return isDocumentOpen(uri);
		}
	};

	/**
	 * Inject the build manager and establish circular dependency.
	 *
	 * @param buildManager
	 *            the build manager.
	 */
	@Inject
	public void setBuildManager(BuildManager buildManager) {
		buildManager.setWorkspaceManager(this);
		this.buildManager = buildManager;
	}

	/**
	 * Initialize a workspace at the given location.
	 *
	 * @param baseDir
	 *            the location
	 * @param issueAcceptor
	 *            the issue acceptor
	 * @param cancelIndicator
	 *            allows to cancel the initialization
	 */
	public void initialize(URI baseDir, Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor,
			CancelIndicator cancelIndicator) {
		WorkspaceFolder workspaceFolder = new WorkspaceFolder(uriExtensions.toUriString(baseDir), "");
		initialize(Collections.singletonList(workspaceFolder), issueAcceptor, cancelIndicator);			
	}

	/**
	 * Initialize a workspace with the given workspace folders.
	 *
	 * @param workspaceFolders
	 *            the list of workspace root folders
	 * @param issueAcceptor
	 *            the issue acceptor
	 * @param cancelIndicator
	 *            allows to cancel the initialization
	 * @since 2.21
	 */
	public void initialize(List<WorkspaceFolder> workspaceFolders, Procedure2<? super URI, ? super Iterable<Issue>> issueAcceptor,
			CancelIndicator cancelIndicator) {
		this.workspaceFolders = new ArrayList<>(workspaceFolders);
		this.issueAcceptor = issueAcceptor;
		refreshWorkspaceConfig(cancelIndicator);
	}

	/**
	 * @return whether this workspace manager supports multiple workspace root folders.
	 * @since 2.21
	 */
	public boolean isSupportsWorkspaceFolders() {
		return true;
	}
	
	protected List<WorkspaceFolder> getWorkspaceFolders() {
		return workspaceFolders;
	}
	
	/**
	 * Creates the open document map and returns it, never {@code null}.
	 * 
	 * @since 2.28
	 */
	protected Map<URI, Document> createOpenDocuments() {
		return new HashMap<>();
	}

	/**
	 * @return the open document map, never {@code null}.
	 * 
	 * @since 2.28
	 */
	protected Map<URI, Document> getOpenDocuments() {
		return openDocuments;
	}

	/**
	 * Updates the workspace folders and refreshes the workspace.
	 * 
	 * @since 2.21
	 */
	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params, CancelIndicator cancelIndicator) {
		WorkspaceFoldersChangeEvent event = params.getEvent();
		Map<String, WorkspaceFolder> uri2workspaceFolder = new HashMap<>();
		workspaceFolders.forEach(it -> uri2workspaceFolder.put(it.getUri(), it));
		event.getRemoved().forEach(it -> uri2workspaceFolder.remove(it.getUri()));
		event.getAdded().forEach(it -> {
			if (!uri2workspaceFolder.containsKey(it.getUri())) 
				uri2workspaceFolder.put(it.getUri(), it);
		});
		this.workspaceFolders = new ArrayList<>(uri2workspaceFolder.values());
		refreshWorkspaceConfig(cancelIndicator);
	}
	
	protected IWorkspaceConfig createWorkspaceConfig() {
		return workspaceConfigFactory.getWorkspaceConfig(workspaceFolders);	
	}
	
	/**
	 * Refresh the workspace.
	 */
	protected void refreshWorkspaceConfig(CancelIndicator cancelIndicator) {
		IWorkspaceConfig newWorkspaceConfig = createWorkspaceConfig();
		setWorkspaceConfig(newWorkspaceConfig);
		List<ProjectDescription> newProjects = new ArrayList<>();
		Set<String> projectNames = projectName2ProjectManager.keySet();
		Set<String> remainingProjectNames = new HashSet<>(projectNames);
		for (IProjectConfig projectConfig : getWorkspaceConfig().getProjects()) {
			if (projectName2ProjectManager.containsKey(projectConfig.getName())) {
				remainingProjectNames.remove(projectConfig.getName());
			} else {
				ProjectManager projectManager = projectManagerProvider.get();
				ProjectDescription projectDescription = projectDescriptionFactory.getProjectDescription(projectConfig);
				projectManager.initialize(projectDescription, projectConfig, issueAcceptor,
						openedDocumentsContentProvider, () -> fullIndex, cancelIndicator);
				projectName2ProjectManager.put(projectDescription.getName(), projectManager);
				newProjects.add(projectDescription);
			}
		}
		for (String deletedProject : remainingProjectNames) {
			ProjectManager projectManager = projectName2ProjectManager.remove(deletedProject);
			projectManager.aboutToRemoveFromWorkspace();
			fullIndex.remove(deletedProject);
			for (ProjectManager remainingProject : projectName2ProjectManager.values()) {
				remainingProject.notifyProjectRemoved(deletedProject);
			}
		}
		afterBuild(buildManager.doInitialBuild(newProjects, cancelIndicator));
	}

	/**
	 * @return the workspace configuration
	 * @throws ResponseErrorException
	 *             if the workspace is not yet initialized
	 */
	protected IWorkspaceConfig getWorkspaceConfig() throws ResponseErrorException {
		if (workspaceConfig == null) {
			ResponseError error = new ResponseError(ResponseErrorCode.ServerNotInitialized,
					"Workspace has not been initialized yet.", null);
			throw new ResponseErrorException(error);
		}
		return workspaceConfig;
	}

	/**
	 * @param workspaceConfig
	 *            the new workspace configuration.
	 */
	protected void setWorkspaceConfig(IWorkspaceConfig workspaceConfig) {
		this.workspaceConfig = workspaceConfig;
	}

	/**
	 * Callback after a build was performend
	 */
	protected void afterBuild(List<IResourceDescription.Delta> deltas) {
		for (ILanguageServerAccess.IBuildListener listener : buildListeners) {
			listener.afterBuild(deltas);
		}
	}

	/**
	 * Announce dirty and deleted files and provide means to start a build.
	 *
	 * @param dirtyFiles
	 *            the dirty files
	 * @param deletedFiles
	 *            the deleted files
	 * @return a build command that can be triggered
	 */
	public Buildable didChangeFiles(List<URI> dirtyFiles, List<URI> deletedFiles) {
		BuildManager.Buildable buildable = buildManager.submit(dirtyFiles, deletedFiles);
		return (cancelIndicator) -> {
			List<IResourceDescription.Delta> deltas = buildable.build(cancelIndicator);
			afterBuild(deltas);
			return deltas;
		};
	}

	/**
	 * Announce dirty and deleted files and perform a build.
	 *
	 * @param dirtyFiles
	 *            the dirty files
	 * @param deletedFiles
	 *            the deleted files
	 * @param cancelIndicator
	 *            cancellation support
	 * @return the delta
	 */
	public List<IResourceDescription.Delta> doBuild(List<URI> dirtyFiles, List<URI> deletedFiles,
			CancelIndicator cancelIndicator) {
		return didChangeFiles(dirtyFiles, deletedFiles).build(cancelIndicator);
	}

	/**
	 * Returns the current index.
	 */
	public IResourceDescriptions getIndex() {
		return new ChunkedResourceDescriptions(fullIndex);
	}

	/**
	 * Returns the project that contains the given URI.
	 *
	 * @param uri
	 *            the contained uri
	 * @return the project base uri.
	 */
	public URI getProjectBaseDir(URI uri) {
		IProjectConfig projectConfig = getProjectConfig(uri);
		if (projectConfig != null) {
			return projectConfig.getPath();
		}
		return null;
	}

	/**
	 * @param uri
	 *            the contained uri
	 * @return the project manager.
	 */
	public ProjectManager getProjectManager(URI uri) {
		IProjectConfig projectConfig = getProjectConfig(uri);
		String name = null;
		if (projectConfig != null) {
			name = projectConfig.getName();
		}
		return getProjectManager(name);
	}

	/**
	 * Find the project that contains the uri.
	 */
	protected IProjectConfig getProjectConfig(URI uri) {
		return getWorkspaceConfig().findProjectContaining(uri);
	}

	/**
	 * Return the project manager for the project with the given name.
	 *
	 * @param projectName
	 *            the project name
	 * @return the project manager
	 */
	public ProjectManager getProjectManager(String projectName) {
		return projectName2ProjectManager.get(projectName);
	}

	/**
	 * Return all project managers.
	 *
	 * @return all project managers.
	 */
	public List<ProjectManager> getProjectManagers() {
		return ImmutableList.copyOf(projectName2ProjectManager.values());
	}

	/**
	 * As opposed to {@link TextEdit}[] the positions in the edits of a {@link DidChangeTextDocumentParams} refer to the
	 * state after applying the preceding edits. See
	 * https://microsoft.github.io/language-server-protocol/specification#textedit-1 and
	 * https://github.com/microsoft/vscode/issues/23173#issuecomment-289378160 for details.
	 *
	 * In particular, this has to be taken into account when undoing the deletion of multiple characters at the end of a
	 * line.
	 *
	 * @since 2.18
	 * @param version
	 *            unused
	 */
	public BuildManager.Buildable didChangeTextDocumentContent(URI uri, Integer version,
			Iterable<TextDocumentContentChangeEvent> changes) {
		Document contents = openDocuments.get(uri);
		if (contents == null) {
			LOG.error("The document " + uri + " has not been opened.");
			return Buildable.NO_BUILD;
		}
		openDocuments.put(uri, contents.applyTextDocumentChanges(changes));
		return didChangeFiles(ImmutableList.of(uri), Collections.emptyList());
	}

	/**
	 * Mark the given document as open and build it.
	 */
	public List<IResourceDescription.Delta> didOpen(URI uri, Integer version, String contents,
			CancelIndicator cancelIndicator) {
		return didOpen(uri, version, contents).build(cancelIndicator);
	}

	/**
	 * Mark the given document as open.
	 */
	public BuildManager.Buildable didOpen(URI uri, Integer version, String contents) {
		openDocuments.put(uri, new Document(version, contents));
		return didChangeFiles(ImmutableList.of(uri), Collections.emptyList());
	}

	/**
	 * Mark the given document as cloded.
	 */
	public BuildManager.Buildable didClose(URI uri) {
		openDocuments.remove(uri);
		if (exists(uri)) {
			return didChangeFiles(ImmutableList.of(uri), Collections.emptyList());
		}
		return didChangeFiles(Collections.emptyList(), ImmutableList.of(uri));
	}

	/**
	 * Return true if the given resource still exists.
	 */
	protected boolean exists(URI uri) {
		ProjectManager projectManager = getProjectManager(uri);
		if (projectManager != null) {
			XtextResourceSet rs = projectManager.getResourceSet();
			if (rs != null) {
				return rs.getURIConverter().exists(uri, null);
			}
		}
		return false;
	}

	/**
	 * Find the resource and the document with the given URI and perform a a read operation.
	 */
	public <T> T doRead(URI uri, Function2<? super Document, ? super XtextResource, ? extends T> work) {
		Pair<? super Document, ? super XtextResource> pair = read(uri);
		if (pair != null) {
			Document doc = (Document) pair.getFirst();
			XtextResource resource = (XtextResource) pair.getSecond();
			return work.apply(doc, resource);
		}
		return work.apply(null, null);
	}

	/**
	 * Find the resource and the document with the given URI.
	 */
	public Pair<? super Document, ? super XtextResource> read(URI uri) {
		URI resourceURI = uri.trimFragment();
		ProjectManager projectMnr = getProjectManager(resourceURI);
		if (projectMnr != null) {
			Resource resource = projectMnr.getResource(resourceURI);
			if (resource instanceof XtextResource) {
				XtextResource xtextResource = (XtextResource) resource; 
				Document doc = getDocument(xtextResource);
				return Tuples.pair(doc, xtextResource);
			}
		}
		return null;
	}
	
	/**
	 * Find the document for the given resource.
	 *
	 * @param resource
	 *            the resource.
	 * @return the document
	 */
	protected Document getDocument(XtextResource resource) {
		Document doc = openDocuments.get(resource.getURI());
		if (doc != null) {
			return doc;
		}
		String text = resource.getParseResult().getRootNode().getText();
		return new Document(1, text);
	}

	/**
	 * Return true if there is a open document with the givne URI.
	 *
	 * @param uri
	 *            the URI
	 */
	public boolean isDocumentOpen(URI uri) {
		return openDocuments.containsKey(uri);
	}

}
