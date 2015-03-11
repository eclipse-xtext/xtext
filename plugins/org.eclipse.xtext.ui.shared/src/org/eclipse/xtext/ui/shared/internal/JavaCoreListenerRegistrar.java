/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ui.shared.internal;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtext.builder.impl.javasupport.ProjectClasspathChangeListener;
import org.eclipse.xtext.ui.resource.Storage2UriMapperJavaImpl;
import org.eclipse.xtext.ui.shared.contribution.IEagerContribution;

import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class JavaCoreListenerRegistrar implements IEagerContribution {

	@Inject
	private ProjectClasspathChangeListener classpathChangeListener;
	
	@Inject
	private Storage2UriMapperJavaImpl storage2UriMapperJavaImpl;
	
	@Override
	public void initialize() {
		JavaCore.addElementChangedListener(classpathChangeListener);
	}

	@Override
	public void discard() {
		JavaCore.removeElementChangedListener(classpathChangeListener);
		JavaCore.removeElementChangedListener(storage2UriMapperJavaImpl);
	}
	
}
