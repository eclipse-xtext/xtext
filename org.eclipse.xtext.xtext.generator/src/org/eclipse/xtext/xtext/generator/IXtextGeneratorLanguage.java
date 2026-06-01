/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator;

import java.util.List;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.StandaloneSetupAccess;

/**
 * Configuration for an Xtext language. Implemented by {@link XtextGeneratorLanguage}.
 * 
 * @noimplement This interface should not be implemented by clients.
 */
public interface IXtextGeneratorLanguage {
	
	Grammar getGrammar();
	
	List<String> getFileExtensions();
	
	StandaloneSetupAccess getRuntimeGenSetup();
	
	GuiceModuleAccess getRuntimeGenModule();
	
	GuiceModuleAccess getIdeGenModule();
	
	GuiceModuleAccess getEclipsePluginGenModule();
	
	ResourceSet getResourceSet();
	
	/**
	 * @deprecated Use {@link CodeConfig#isPreferXtendStubs()} instead
	 */
	@Deprecated
	boolean isGenerateXtendStubs();
	
}
