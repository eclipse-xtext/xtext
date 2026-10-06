/**
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.builder;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("all")
public class SameClassNamesTest extends AbstractXtendUITestCase {
  @Inject
  private WorkbenchTestHelper testHelper;

  @Inject
  private Provider<PluginProjectFactory> projectFactoryProvider;

  private IProject first;

  private IProject second;

  private IProject third;

  @Before
  public void createProjects() {
    try {
      this.first = this.createPluginProject("first", ((String[])Conversions.unwrapArray(WorkbenchTestHelper.DEFAULT_REQ_BUNDLES, String.class)));
      Iterable<String> _plus = Iterables.<String>concat(WorkbenchTestHelper.DEFAULT_REQ_BUNDLES, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("first")));
      this.second = this.createPluginProject("second", ((String[])Conversions.unwrapArray(_plus, String.class)));
      this.third = this.createPluginProject("third", ((String[])Conversions.unwrapArray(WorkbenchTestHelper.DEFAULT_REQ_BUNDLES, String.class)));
      IResourcesSetupUtil.reallyWaitForAutoBuild();
      this.testHelper.closeWelcomePage();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @After
  public void deleteProjects() {
    try {
      WorkbenchTestHelper.deleteProject(this.first);
      WorkbenchTestHelper.deleteProject(this.second);
      WorkbenchTestHelper.deleteProject(this.third);
      IResourcesSetupUtil.reallyWaitForAutoBuild();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testNoDuplicateNames() {
    try {
      this.testHelper.createFileImpl("first/src/com/acme/A.xtend", "package com.acme class A {}");
      this.testHelper.createFileImpl("second/src/com/acme/B.xtend", "package com.acme class B {}");
      this.testHelper.createFileImpl("third/src/com/acme/C.xtend", "package com.acme class C {}");
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testDuplicateNames_01() {
    try {
      this.testHelper.createFileImpl("first/src/com/acme/A.xtend", "package com.acme class A { new(int i) {} }");
      this.testHelper.createFileImpl("second/src/com/acme/B.xtend", "package com.acme class B extends A { new() { super(1) } }");
      this.testHelper.createFileImpl("third/src/com/acme/A.xtend", "package com.acme class A {}");
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testDuplicateNames_02() {
    try {
      this.testHelper.createFileImpl("first/src/com/acme/A.xtend", "package com.acme class A { def static void m1() {} }");
      this.testHelper.createFileImpl("first/src/com/acme/B.xtend", "package com.acme class B { new() { A.m1 } }");
      this.testHelper.createFileImpl("second/src/com/acme/B.xtend", "package com.acme class B {}");
      this.testHelper.createFileImpl("third/src/com/acme/A.xtend", "package com.acme class A { def static void m2() {} }");
      this.testHelper.createFileImpl("third/src/com/acme/B.xtend", "package com.acme class B { new() { A.m2 } }");
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testDuplicateNames_03() {
    try {
      this.testHelper.createFileImpl("first/src/com/acme/A.xtend", "package com.acme class A { new(String s) {} }");
      this.testHelper.createFileImpl("second/src/com/acme/A.xtend", "package com.acme class A { new(int i) {} }");
      this.testHelper.createFileImpl("second/src/com/acme/B.xtend", "package com.acme class B extends A { new() { super(1) } }");
      this.testHelper.createFileImpl("third/src/com/acme/A.xtend", "package com.acme class A {}");
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testDuplicateNames_04() {
    try {
      final IJavaProject javaProject = JavaCore.create(this.second);
      final IClasspathEntry[] cp = javaProject.getRawClasspath();
      final List<IClasspathEntry> reversed = ListExtensions.<IClasspathEntry>reverse(((List<IClasspathEntry>)Conversions.doWrapArray(cp)));
      javaProject.setRawClasspath(((IClasspathEntry[])Conversions.unwrapArray(reversed, IClasspathEntry.class)), null);
      IResourcesSetupUtil.reallyWaitForAutoBuild();
      this.testHelper.createFileImpl("first/src/com/acme/A.xtend", "package com.acme class A { new(String s) {} }");
      this.testHelper.createFileImpl("second/src/com/acme/A.xtend", "package com.acme class A { new(int i) {} }");
      this.testHelper.createFileImpl("second/src/com/acme/B.xtend", "package com.acme class B extends A { new() { super(1) } }");
      this.testHelper.createFileImpl("third/src/com/acme/A.xtend", "package com.acme class A {}");
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public IProject createPluginProject(final String name, final String... requiredBundles) throws CoreException {
    final PluginProjectFactory projectFactory = this.projectFactoryProvider.get();
    projectFactory.setProjectName(name);
    projectFactory.setBreeToUse(JREContainerProvider.PREFERRED_BREE);
    projectFactory.addFolders(Collections.<String>singletonList("src"));
    projectFactory.addBuilderIds(
      XtextProjectHelper.BUILDER_ID, 
      JavaCore.BUILDER_ID, 
      "org.eclipse.pde.ManifestBuilder", 
      "org.eclipse.pde.SchemaBuilder");
    projectFactory.addProjectNatures(
      XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature");
    projectFactory.addRequiredBundles(((List<String>)Conversions.doWrapArray(requiredBundles)));
    projectFactory.addExportedPackages(Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("com.acme")));
    NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
    final IProject result = projectFactory.createProject(_nullProgressMonitor, null);
    final IJavaProject javaProject = JavaCore.create(result);
    JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
    JavaProjectSetupUtil.addJreClasspathEntry(javaProject);
    return result;
  }
}
