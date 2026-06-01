/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.model.project;

import java.util.List;
import org.eclipse.xtext.xtext.generator.IGuiceAwareGeneratorComponent;

/**
 * Configuration of the subprojects for one or more Xtext languages. Implemented by {@link XtextProjectConfig}.
 * 
 * @noimplement This interface should not be implemented by clients.
 */
public interface IXtextProjectConfig extends IGuiceAwareGeneratorComponent {
	
	IRuntimeProjectConfig getRuntime();
	
	IBundleProjectConfig getRuntimeTest();
	
	IBundleProjectConfig getGenericIde();
	
	IBundleProjectConfig getEclipsePlugin();
	
	IBundleProjectConfig getEclipsePluginTest();
	
	List<? extends ISubProjectConfig> getEnabledProjects();
	
	List<? extends ISubProjectConfig> getTestProjects();
	
}
