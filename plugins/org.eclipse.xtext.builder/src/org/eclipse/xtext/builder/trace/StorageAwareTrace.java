/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.builder.trace;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtext.util.Pair;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@NonNullByDefault
public class StorageAwareTrace extends AbstractTrace {

	private IStorage localStorage;

	private String projectName;

	public IStorage getLocalStorage() {
		return localStorage;
	}
	
	@Override
	public URI getLocalURI() {
		IStorage localStorage = getLocalStorage();
		return getURIForStorage(localStorage);
	}

	@Override
	public IProject getLocalProject() {
		return findProject(projectName);
	}
	
	@Override
	protected URI resolvePath(URI path) {
		if (!path.isRelative())
			return path;
		if (localStorage instanceof IFile) {
			IProject project = ((IFile) localStorage).getProject();
			if (project != null) {
				return resolvePath(project, path);
			}
		}
		return path;
	}

	protected URI resolvePath(IProject project, URI path) {
		IResource candidate = project.findMember(path.toString());
		if (candidate != null && candidate.exists())
			return URI.createPlatformResourceURI(project.getFullPath() + "/" + path, true);
		return path;
	}
	
	protected void setLocalStorage(IStorage derivedResource) {
		this.localStorage = derivedResource;
		if (derivedResource instanceof IResource) {
			this.projectName = ((IResource) derivedResource).getProject().getName();
		}
	}
	
	@Override
	protected IStorage findStorage(URI uri, IProject project) {
		Iterable<Pair<IStorage, IProject>> allStorages = getStorage2uriMapper().getStorages(resolvePath(uri));
		for(Pair<IStorage, IProject> storage: allStorages) {
			if (project.equals(storage.getSecond())) {
				return storage.getFirst();
			}
		}
		throw new IllegalStateException("No storage found for given path: " + uri);
	}

	@Override
	protected InputStream getContents(URI uri, IProject project) throws CoreException {
		return findStorage(uri, project).getContents();
	}


}
