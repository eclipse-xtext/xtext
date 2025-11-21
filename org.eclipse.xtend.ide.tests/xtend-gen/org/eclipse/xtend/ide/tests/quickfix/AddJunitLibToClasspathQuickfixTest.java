/**
 * Copyright (c) 2019, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.quickfix;

import com.google.inject.Inject;
import org.eclipse.xtend.ide.buildpath.Junit4LibClasspathAdder;
import org.eclipse.xtend.ide.buildpath.Junit5LibClasspathAdder;
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider;
import org.eclipse.xtend.ide.tests.buildpath.AbstractJunitLibClasspathAdderTestCase;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.diagnostics.Diagnostic;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.xbase.lib.Extension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author vivien.jovet - Initial contribution and API
 */
@InjectWith(XtendIDEInjectorProvider.class)
@RunWith(XtextRunner.class)
@SuppressWarnings("all")
public class AddJunitLibToClasspathQuickfixTest extends AbstractJunitLibClasspathAdderTestCase {
  @Inject
  @Extension
  private QuickfixTestBuilder builder;

  @Before
  @Override
  public void setUpProject() throws Exception {
    this.workbenchHelper.closeAllEditors(false);
    super.setUpProject();
  }

  @After
  public void tearDown() {
    this.workbenchHelper.closeAllEditors(false);
  }

  @Test
  public void addJUnit4LibToPluginProjectClasspath() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.junit|.Test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class FooTest {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Test");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def test() {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String content = _builder.toString();
    this.builder.create("FooTest.xtend", content).assertIssueCodes(Diagnostic.LINKING_DIAGNOSTIC).assertResolutionLabels("Add JUnit 4 lib to classpath").assertModelAfterQuickfix(content.replace("|", ""));
    this.assertRequireBundles(new String[] { Junit4LibClasspathAdder.BUNDLE_ID });
  }

  @Test
  public void addJUnit4LibToProjectClasspath() {
    this.removePluginNature();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.junit|.Test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class FooTest {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Test");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def test() {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String content = _builder.toString();
    this.builder.create("FooTest.xtend", content).assertIssueCodes(Diagnostic.LINKING_DIAGNOSTIC).assertResolutionLabels("Add JUnit 4 lib to classpath").assertModelAfterQuickfix(content.replace("|", ""));
    this.assertClasspath(
      "classpath should contain a JUnit 4 container entry", 
      Junit4LibClasspathAdder.JUNIT4_LIBRARY_PATH);
  }

  @Test
  public void addJUnit5LibToPluginProjectClasspath() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.junit.jupiter.api|.Test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class FooTest2 {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Test");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def test() {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String content = _builder.toString();
    this.builder.create("FooTest2.xtend", content).assertIssueCodes(Diagnostic.LINKING_DIAGNOSTIC).assertResolutionLabels("Add JUnit 5 lib to classpath").assertModelAfterQuickfix(content.replace("|", ""));
    this.assertImportPackages(new String[] { "org.junit.jupiter.api;version=\"[6.0.0,7.0.0)\"" });
  }

  @Test
  public void addJUnit5LibToProjectClasspath() {
    this.removePluginNature();
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.junit.jupiter.api|.Test");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class FooTest {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("@Test");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def test() {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final String content = _builder.toString();
    this.builder.create("FooTest.xtend", content).assertIssueCodes(Diagnostic.LINKING_DIAGNOSTIC).assertResolutionLabels("Add JUnit 5 lib to classpath").assertModelAfterQuickfix(content.replace("|", ""));
    this.assertClasspath(
      "classpath should contain a JUnit 5 container entry", 
      Junit5LibClasspathAdder.JUNIT5_LIBRARY_PATH);
  }
}
