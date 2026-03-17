/**
 * Copyright (c) 2019, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.buildpath;

import com.google.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.ide.buildpath.Junit4LibClasspathAdder;
import org.eclipse.xtend.ide.buildpath.Junit6LibClasspathAdder;
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author vivien.jovet - Initial contribution and API
 */
@InjectWith(XtendIDEInjectorProvider.class)
@RunWith(XtextRunner.class)
@SuppressWarnings("all")
public class JunitLibClasspathAdderTest extends AbstractJunitLibClasspathAdderTestCase {
  @Inject
  private Junit4LibClasspathAdder junit4LibAdder;

  @Inject
  private Junit6LibClasspathAdder junit6LibAdder;

  @Test
  public void addJUnit4LibToPluginProjectClasspath() {
    try {
      IJavaProject _create = JavaCore.create(this.workbenchHelper.getProject());
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      this.junit4LibAdder.addLibsToClasspath(_create, _nullProgressMonitor);
      final Consumer<IClasspathEntry> _function = (IClasspathEntry it) -> {
        InputOutput.<IPath>println(it.getPath());
      };
      ((List<IClasspathEntry>)Conversions.doWrapArray(JavaCore.create(this.workbenchHelper.getProject()).getRawClasspath())).forEach(_function);
      this.assertRequireBundles(new String[] { Junit4LibClasspathAdder.BUNDLE_ID });
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void addJUnit4LibToProjectClasspath() {
    this.removePluginNature();
    IJavaProject _create = JavaCore.create(this.workbenchHelper.getProject());
    NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
    this.junit4LibAdder.addLibsToClasspath(_create, _nullProgressMonitor);
    this.assertClasspath(
      "classpath should contain a JUnit 4 container entry", 
      Junit4LibClasspathAdder.JUNIT4_LIBRARY_PATH);
  }

  @Test
  public void addJUnit6LibToPluginProjectClasspath() {
    IJavaProject _create = JavaCore.create(this.workbenchHelper.getProject());
    NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
    this.junit6LibAdder.addLibsToClasspath(_create, _nullProgressMonitor);
    this.assertRequireBundles(Junit6LibClasspathAdder.BUNDLE_IDS);
  }

  @Test
  public void addJUnit6LibToProjectClasspath() {
    this.removePluginNature();
    IJavaProject _create = JavaCore.create(this.workbenchHelper.getProject());
    NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
    this.junit6LibAdder.addLibsToClasspath(_create, _nullProgressMonitor);
    this.assertClasspath(
      "classpath should contain a JUnit 6 container entry",
      Junit6LibClasspathAdder.JUNIT6_LIBRARY_PATH);
  }
}
