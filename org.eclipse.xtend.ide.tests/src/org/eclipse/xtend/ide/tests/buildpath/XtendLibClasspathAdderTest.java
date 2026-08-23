/*******************************************************************************
 * Copyright (c) 2011, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.buildpath;

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.core.validation.IssueCodes;
import org.eclipse.xtend.ide.buildpath.XtendLibClasspathAdder;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.ui.util.JavaProjectFactory;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class XtendLibClasspathAdderTest extends AbstractXtendUITestCase {
	@Inject
	private XtendLibClasspathAdder adder;

	@Inject
	private MarkerAssertions markerAssert;

	@Inject
	private JavaProjectFactory javaProjectFactory;

	@Inject
	private PluginProjectFactory pluginProjectFactory;

	@Inject
	private SyncUtil syncUtil;
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		IResourcesSetupUtil.cleanWorkspace();
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		pluginProjectFactory.setBreeToUse(JREContainerProvider.PREFERRED_BREE);
	}
	
	@Test
	public void testAddToJavaProject() throws Exception {
		javaProjectFactory.setProjectName("test");
		javaProjectFactory.addFolders(Collections.singletonList("src"));
		javaProjectFactory.addBuilderIds(JavaCore.BUILDER_ID, XtextProjectHelper.BUILDER_ID);
		javaProjectFactory.addProjectNatures(JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID);
		IProject project = javaProjectFactory.createProject(null, null);
		IJavaProject javaProject = JavaCore.create(project);
		JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
		IFile file = project.getFile("src/Foo.xtend");
		file.create(new StringInputStream("import org.eclipse.xtend.lib.annotations.Accessors class Foo { @Accessors int bar }"),
				true, null);
		syncUtil.waitForBuild(null);
		markerAssert.assertErrorMarker(file, IssueCodes.XBASE_LIB_NOT_ON_CLASSPATH);
		adder.addLibsToClasspath(javaProject, null);
		waitForAutoBuild();
		syncUtil.waitForBuild(null);
		markerAssert.assertNoErrorMarker(file);
	}

	@Test
	public void testAddToPlugin() throws Exception {
		pluginProjectFactory.setProjectName("test");
		pluginProjectFactory.addFolders(Collections.singletonList("src"));
		pluginProjectFactory.addBuilderIds(JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
				"org.eclipse.pde.SchemaBuilder", XtextProjectHelper.BUILDER_ID);
		pluginProjectFactory.addProjectNatures(JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature",
				XtextProjectHelper.NATURE_ID);
		IProject project = pluginProjectFactory.createProject(null, null);
		IJavaProject javaProject = JavaCore.create(project);
		JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
		IFile file = project.getFile("src/Foo.xtend");
		file.create(new StringInputStream("import org.eclipse.xtend.lib.annotations.Accessors class Foo { @Accessors int bar }"),
				true, null);
		syncUtil.waitForBuild(null);
		markerAssert.assertErrorMarker(file, IssueCodes.XBASE_LIB_NOT_ON_CLASSPATH);
		adder.addLibsToClasspath(javaProject, null);
		waitForAutoBuild();
		markerAssert.assertNoErrorMarker(file);
	}

	@Test
	public void testAddToBrokenPlugin() throws Exception {
		pluginProjectFactory.setProjectName("test");
		pluginProjectFactory.addFolders(Collections.singletonList("src"));
		pluginProjectFactory.addBuilderIds(JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
				"org.eclipse.pde.SchemaBuilder", XtextProjectHelper.BUILDER_ID);
		pluginProjectFactory.addProjectNatures(JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature",
				XtextProjectHelper.NATURE_ID);
		IProject project = pluginProjectFactory.createProject(null, null);
		IJavaProject javaProject = JavaCore.create(project);
		JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
		project.findMember("META-INF").delete(true, null);
		IFile file = project.getFile("src/Foo.xtend");
		file.create(new StringInputStream("import org.eclipse.xtend.lib.annotations.Accessors class Foo { @Accessors int bar }"),
				true, null);
		syncUtil.waitForBuild(null);
		markerAssert.assertErrorMarker(file, IssueCodes.XBASE_LIB_NOT_ON_CLASSPATH);
		adder.addLibsToClasspath(javaProject, null);
		waitForAutoBuild();
		syncUtil.waitForBuild(null);
		markerAssert.assertNoErrorMarker(file);
	}

	/**
	 * Behavior relies on scheduled jobs which are not really builds but still belong to the
	 * {@link org.eclipse.core.resources.ResourcesPlugin#FAMILY_AUTO_BUILD} thus we wait for that one here.
	 */
	protected void waitForAutoBuild() {
		// changing classpath triggers multiple builds, the first one only sets forgetlastBuildState
		// we need to wait for that
		IResourcesSetupUtil.reallyWaitForAutoBuild();
		// now lets do a build
		syncUtil.waitForBuild(null);
	}

}
