/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.builder

import com.google.inject.Inject
import java.util.Collections
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.util.JREContainerProvider
import org.eclipse.xtext.ui.util.PluginProjectFactory
import org.junit.After
import org.junit.Before
import org.junit.Test

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*
import com.google.inject.Provider

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
class SameClassNamesTest extends AbstractXtendUITestCase {
	
	@Inject WorkbenchTestHelper testHelper
	@Inject Provider<PluginProjectFactory> projectFactoryProvider
	
	IProject first
	IProject second
	IProject third
	
	@Before
	def void createProjects() {
		first = createPluginProject('first', WorkbenchTestHelper.DEFAULT_REQ_BUNDLES)
		second = createPluginProject('second', WorkbenchTestHelper.DEFAULT_REQ_BUNDLES + #['first'])
		third = createPluginProject('third', WorkbenchTestHelper.DEFAULT_REQ_BUNDLES)
		reallyWaitForAutoBuild
		testHelper.closeWelcomePage
	}
	 
	@After
	def deleteProjects() {
		WorkbenchTestHelper.deleteProject(first)
		WorkbenchTestHelper.deleteProject(second)
		WorkbenchTestHelper.deleteProject(third)
		reallyWaitForAutoBuild
	}
	
	@Test
	def void testNoDuplicateNames() {
		testHelper.createFileImpl('first/src/com/acme/A.xtend', 'package com.acme class A {}')
		testHelper.createFileImpl('second/src/com/acme/B.xtend', 'package com.acme class B {}')
		testHelper.createFileImpl('third/src/com/acme/C.xtend', 'package com.acme class C {}')
		waitForBuild
		assertNoErrorsInWorkspace
	}
	
	@Test
	def void testDuplicateNames_01() {
		testHelper.createFileImpl('first/src/com/acme/A.xtend', 'package com.acme class A { new(int i) {} }')
		testHelper.createFileImpl('second/src/com/acme/B.xtend', 'package com.acme class B extends A { new() { super(1) } }')
		testHelper.createFileImpl('third/src/com/acme/A.xtend', 'package com.acme class A {}')
		waitForBuild
		assertNoErrorsInWorkspace
	}
	
	@Test
	def void testDuplicateNames_02() {
		testHelper.createFileImpl('first/src/com/acme/A.xtend', 'package com.acme class A { def static void m1() {} }')
		testHelper.createFileImpl('first/src/com/acme/B.xtend', 'package com.acme class B { new() { A.m1 } }')
		testHelper.createFileImpl('second/src/com/acme/B.xtend', 'package com.acme class B {}')
		testHelper.createFileImpl('third/src/com/acme/A.xtend', 'package com.acme class A { def static void m2() {} }')
		testHelper.createFileImpl('third/src/com/acme/B.xtend', 'package com.acme class B { new() { A.m2 } }')
		waitForBuild
		assertNoErrorsInWorkspace
	}
	
	@Test
	def void testDuplicateNames_03() { 
		testHelper.createFileImpl('first/src/com/acme/A.xtend', 'package com.acme class A { new(String s) {} }')
		testHelper.createFileImpl('second/src/com/acme/A.xtend', 'package com.acme class A { new(int i) {} }')
		testHelper.createFileImpl('second/src/com/acme/B.xtend', 'package com.acme class B extends A { new() { super(1) } }')
		testHelper.createFileImpl('third/src/com/acme/A.xtend', 'package com.acme class A {}')
		waitForBuild
		assertNoErrorsInWorkspace
	}
	
	@Test
	def void testDuplicateNames_04() {
		val javaProject = JavaCore.create(second)
		val cp = javaProject.rawClasspath
		val reversed = cp.reverse
		javaProject.setRawClasspath(reversed, null)
		reallyWaitForAutoBuild
		testHelper.createFileImpl('first/src/com/acme/A.xtend', 'package com.acme class A { new(String s) {} }')
		testHelper.createFileImpl('second/src/com/acme/A.xtend', 'package com.acme class A { new(int i) {} }')
		testHelper.createFileImpl('second/src/com/acme/B.xtend', 'package com.acme class B extends A { new() { super(1) } }')
		testHelper.createFileImpl('third/src/com/acme/A.xtend', 'package com.acme class A {}')
		waitForBuild
		assertNoErrorsInWorkspace
	}
	
	def IProject createPluginProject(String name, String... requiredBundles) throws CoreException {
		val projectFactory = projectFactoryProvider.get()
		projectFactory.setProjectName(name);
		projectFactory.setBreeToUse(JREContainerProvider.PREFERRED_BREE);
		projectFactory.addFolders(Collections.singletonList("src"));
		projectFactory.addBuilderIds(
			XtextProjectHelper.BUILDER_ID,
			JavaCore.BUILDER_ID,
			"org.eclipse.pde.ManifestBuilder",
			"org.eclipse.pde.SchemaBuilder");
		projectFactory.addProjectNatures(
				XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature");
		projectFactory.addRequiredBundles(requiredBundles);
		projectFactory.addExportedPackages(#['com.acme'])
		val result = projectFactory.createProject(new NullProgressMonitor(), null);
		val javaProject = JavaCore.create(result);
		JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
		JavaProjectSetupUtil.addJreClasspathEntry(javaProject);
		return result;
	}
	
}