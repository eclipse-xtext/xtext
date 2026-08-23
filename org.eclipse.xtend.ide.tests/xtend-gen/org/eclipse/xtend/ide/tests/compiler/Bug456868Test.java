/**
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.compiler;

import com.google.inject.Inject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtend.ide.buildpath.XtendLibClasspathAdder;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.builder.nature.ToggleXtextNatureCommand;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sven Efftinge
 * 
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=456868
 */
@SuppressWarnings("all")
public class Bug456868Test extends AbstractXtendUITestCase {
  @Inject
  private XtendLibClasspathAdder libAdder;

  @Inject
  private ToggleXtextNatureCommand natureAdder;

  private IJavaProject project;

  @After
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    this.project.getProject().delete(true, null);
  }

  /**
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=452821
   */
  @Test
  public void testProjectWithWhitespaceInNameWorks() {
    try {
      this.project = JavaProjectSetupUtil.createJavaProject("my project");
      JavaProjectSetupUtil.makeDefaultCompliant(this.project);
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      this.libAdder.addLibsToClasspath(this.project, _nullProgressMonitor);
      this.natureAdder.toggleNature(this.project.getProject());
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class MyClass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      IResourcesSetupUtil.createFile("my project/src/MyClass.xtend", _builder.toString());
      IResourcesSetupUtil.waitForBuild();
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
      final IResource generatedJava = IResourcesSetupUtil.file("my project/xtend-gen/MyClass.java");
      Assert.assertTrue(generatedJava.exists());
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
