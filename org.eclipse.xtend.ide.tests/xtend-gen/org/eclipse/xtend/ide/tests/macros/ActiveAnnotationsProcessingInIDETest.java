/**
 * Copyright (c) 2013, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.macros;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.ide.tests.StopwatchRule;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.editor.hover.html.IEObjectHoverDocumentationProvider;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XtendIDEInjectorProvider.class)
@SuppressWarnings("all")
public class ActiveAnnotationsProcessingInIDETest extends AbstractReusableActiveAnnotationTests {
  @Inject
  @Extension
  private IEObjectHoverDocumentationProvider documentationProvider;

  @Inject
  private IResourceValidator validator;

  @Inject
  private WorkbenchTestHelper workbenchTestHelper;

  @Test
  public void testDocumentationProvider() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package myannotation");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.Active");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.RegisterGlobalsContext");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.TransformationParticipant");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Active(ChangeDocProcessor)");
    _builder.newLine();
    _builder.append("annotation ChangeDoc {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class ChangeDocProcessor implements RegisterGlobalsParticipant<MemberDeclaration>, TransformationParticipant<MutableMemberDeclaration> {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doRegisterGlobals(List<? extends MemberDeclaration> annotatedSourceElements, RegisterGlobalsContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("//do nothing");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doTransform(List<? extends MutableMemberDeclaration> annotatedTargetElements,");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("extension TransformationContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("for (it : annotatedTargetElements) {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("docComment = \"Hello World!\"");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, String> _mappedTo = Pair.<String, String>of("myannotation/ChangeDoc.xtend", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package usercode");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import myannotation.ChangeDoc");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("/** ");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*\t Comment");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("@ChangeDoc");
    _builder_1.newLine();
    _builder_1.append("class UserClass {");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("private UserClass object");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    Pair<String, String> _mappedTo_1 = Pair.<String, String>of("usercode/UserCode.xtend", _builder_1.toString());
    final Procedure1<CompilationUnitImpl> _function = (CompilationUnitImpl it) -> {
      final XtendClass xtendClass = IterableExtensions.<XtendClass>head(Iterables.<XtendClass>filter(it.getXtendFile().getXtendTypes(), XtendClass.class));
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("@<a href=\"eclipse-xtext-doc:platform:/resource/macroProject/src/myannotation/ChangeDoc.xtend%23/1\">ChangeDoc</a><br>Comment");
      this.assertDocumentation(_builder_2, xtendClass);
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("@<a href=\"eclipse-xtext-doc:platform:/resource/macroProject/src/myannotation/ChangeDoc.xtend%23/1\">ChangeDoc</a><br>Hello World!");
      final Function1<XtendField, Boolean> _function_1 = (XtendField it_1) -> {
        return Boolean.valueOf(it_1.getName().equals("object"));
      };
      this.assertDocumentation(_builder_3, IterableExtensions.<XtendField>head(IterableExtensions.<XtendField>filter(Iterables.<XtendField>filter(xtendClass.getMembers(), XtendField.class), _function_1)).getType().getType());
    };
    this.assertProcessing(_mappedTo, _mappedTo_1, _function);
  }

  public void assertDocumentation(final CharSequence charSequence, final EObject sourceElement) {
    Assert.assertEquals(charSequence.toString(), this.documentationProvider.getDocumentation(sourceElement));
  }

  @Inject
  private XtextResourceSetProvider resourceSetProvider;

  @Inject
  private Provider<CompilationUnitImpl> compilationUnitProvider;

  @Rule
  public StopwatchRule stopwatch = new StopwatchRule(true);

  private static IJavaProject macroProject;

  private static IJavaProject userProject;

  @BeforeClass
  public static void createProjects() {
    try {
      ActiveAnnotationsProcessingInIDETest.macroProject = JavaCore.create(WorkbenchTestHelper.createPluginProject("macroProject"));
      ActiveAnnotationsProcessingInIDETest.userProject = JavaCore.create(
        WorkbenchTestHelper.createPluginProject("userProject", "com.google.inject", "org.eclipse.xtend.lib",
          "org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit", "macroProject"));
      WorkbenchTestHelper.addExportedPackages(ActiveAnnotationsProcessingInIDETest.macroProject.getProject(), "myannotation");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @AfterClass
  public static void deleteProjects() {
    try {
      IResourcesSetupUtil.cleanWorkspace();
      ActiveAnnotationsProcessingInIDETest.macroProject = null;
      ActiveAnnotationsProcessingInIDETest.userProject = null;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @After
  public void tearDown() throws Exception {
    this.workbenchTestHelper.closeAllEditors(false);
    this.clientFile.delete(true, null);
    this.macroFile.delete(true, null);
    boolean _notEquals = (!Objects.equals("myannotation", this.exportedPackage));
    if (_notEquals) {
      boolean _removeExportedPackages = WorkbenchTestHelper.removeExportedPackages(ActiveAnnotationsProcessingInIDETest.macroProject.getProject(), this.exportedPackage);
      if (_removeExportedPackages) {
        IResourcesSetupUtil.waitForBuild();
      }
    }
  }

  private IFile macroFile;

  private IFile clientFile;

  private String exportedPackage;

  @Override
  public void assertProcessing(final Pair<String, String> macroContent, final Pair<String, String> clientContent, final Procedure1<? super CompilationUnitImpl> expectations) {
    try {
      this.macroFile = this.newSource(ActiveAnnotationsProcessingInIDETest.macroProject, macroContent.getKey(), macroContent.getValue().toString());
      final int lidx = macroContent.getKey().lastIndexOf("/");
      if ((lidx != (-1))) {
        this.exportedPackage = macroContent.getKey().substring(0, lidx).replace("/", ".");
        boolean _addExportedPackages = WorkbenchTestHelper.addExportedPackages(ActiveAnnotationsProcessingInIDETest.macroProject.getProject(), this.exportedPackage);
        if (_addExportedPackages) {
          IResourcesSetupUtil.reallyWaitForAutoBuild();
        }
      }
      this.clientFile = this.newSource(ActiveAnnotationsProcessingInIDETest.userProject, clientContent.getKey(), clientContent.getValue().toString());
      IResourcesSetupUtil.waitForBuild();
      final ResourceSet resourceSet = this.resourceSetProvider.get(ActiveAnnotationsProcessingInIDETest.userProject.getProject());
      final Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(this.clientFile.getFullPath().toString(), true), true);
      EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl);
      this.validator.validate(resource, CheckMode.FAST_ONLY, CancelIndicator.NullImpl);
      final CompilationUnitImpl unit = this.compilationUnitProvider.get();
      unit.setXtendFile(IterableExtensions.<XtendFile>head(Iterables.<XtendFile>filter(resource.getContents(), XtendFile.class)));
      expectations.apply(unit);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  public IFile newSource(final IJavaProject it, final String fileName, final String contents) {
    try {
      final IFile result = it.getProject().getFile(("src/" + fileName));
      this.createIfNotExistent(result.getParent());
      String _name = StandardCharsets.ISO_8859_1.name();
      StringInputStream _stringInputStream = new StringInputStream(contents, _name);
      result.create(_stringInputStream, true, null);
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private void createIfNotExistent(final IContainer container) {
    try {
      boolean _exists = container.exists();
      boolean _not = (!_exists);
      if (_not) {
        this.createIfNotExistent(container.getParent());
        ((IFolder) container).create(true, false, null);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
