/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests;

import org.eclipse.xtend.core.XtendRuntimeModule2;
import org.eclipse.xtend.core.XtendStandaloneSetup;
import org.eclipse.xtext.common.types.access.CachingClasspathTypeProviderFactory;
import org.eclipse.xtext.common.types.access.ClasspathTypeProviderFactory;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider.SingletonPreferenceValuesProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class NewTypeSystemRuntimeTestSetup extends XtendStandaloneSetup {
	
	@Override
	public Injector createInjector() {
		return Guice.createInjector(new XtendRuntimeModule2() {
			@Override
			public ClassLoader bindClassLoaderToInstance() {
				return AbstractXtendTestCase.class.getClassLoader();
			}
			
			@SuppressWarnings("unused")
			public Class<? extends ClasspathTypeProviderFactory> bindClasspathTypeProviderFactory() {
				return CachingClasspathTypeProviderFactory.class;
			}
			
			@SuppressWarnings("unused")
			public Class<? extends IPreferenceValuesProvider> bindIPreferenceValuesProvider() {
				return SingletonPreferenceValuesProvider.class;
			}
		});
	}
}