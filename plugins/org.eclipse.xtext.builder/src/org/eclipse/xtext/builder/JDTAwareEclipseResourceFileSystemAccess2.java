/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.builder;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.Multimap;

/**
 * A specialization of the EclipseResourceFileSystemAccess2 for languages that produce
 * Java code. If code is written to a folder that is not yet a source folder, it will be
 * automatically added as a source folder.
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @since 2.1
 */
public class JDTAwareEclipseResourceFileSystemAccess2 extends EclipseResourceFileSystemAccess2 {

	/*
	 * Overridden to convince the API tooling that this method still exists.
	 */
	@Override
	@Deprecated
	protected void createFolder(IFolder folder) throws CoreException {
		super.createFolder(folder);
	}
	
	/**
	 * @since 2.4
	 */
	@Override
	protected void createContainer(IContainer container) throws CoreException {
		super.createContainer(container);
		addToSourceFolders(container);
	}
	
	/**
	 * @since 2.6
	 */
	protected void addToSourceFolders(IContainer container) throws JavaModelException {
		IJavaProject jp = JavaCore.create(container.getProject());
		if (jp.exists() && !jp.isOnClasspath(container)) {
			IClasspathEntry srcFolderClasspathEntry = JavaCore.newSourceEntry(container.getFullPath());
			IClasspathEntry[] classPath = jp.getRawClasspath();
			IClasspathEntry[] newClassPath = new IClasspathEntry[classPath.length + 1];
			System.arraycopy(classPath, 0, newClassPath, 1, classPath.length);
			newClassPath[0] = srcFolderClasspathEntry;
			jp.setRawClasspath(newClassPath, getMonitor());
		}
	}
	
	/**
	 * Since sourceTraces are relative the URI has to be computed with the currentSource as context
	 */
	@Override
	public void flushSourceTraces(String generatorName) throws CoreException {
		Multimap<URI, IPath> sourceTraces = getSourceTraces();
		if (sourceTraces != null) {
			Set<URI> keys = sourceTraces.keySet();
			String source = getCurrentSource();
			IContainer container = Strings.isEmpty(source) ? getProject() : getProject().getFolder(source);
			for (URI uri : keys) {
				if (uri != null && source != null) {
					Collection<IPath> paths = sourceTraces.get(uri);
					IFile sourceFile = container.getFile(new Path(uri.toFileString()));
					if (sourceFile.exists()) {
						IPath[] tracePathArray = paths.toArray(new IPath[paths.size()]);
						getTraceMarkers().installMarker(sourceFile, generatorName, tracePathArray);
					}
				}
			}
		}
		resetSourceTraces();
	}
}
