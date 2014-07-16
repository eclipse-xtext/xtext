package org.eclipse.xtend.ide.tests.compiler;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.junit4.internal.StopwatchRule;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@SuppressWarnings("all")
public class RebuildAffectedResourcesTest extends AbstractXtendUITestCase {
  @Inject
  private WorkbenchTestHelper workbenchTestHelper;
  
  @Rule
  public StopwatchRule rule = new StopwatchRule(true);
  
  @After
  public void tearDown() {
    try {
      IProject _project = this.workbenchTestHelper.getProject();
      _project.delete(true, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testRemoveReferencedType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Type1 {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile type1 = this.workbenchTestHelper.createFile("Type1.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Type2 {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("Type1 myRef");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final IFile type2 = this.workbenchTestHelper.createFile("Type2.xtend", _builder_1.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Type3 {");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_2.toString());
      type1.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(type2, "Type1");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAddReferencedType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Type3 {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile type1 = this.workbenchTestHelper.createFile("Type1.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Type2 {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("Type1 myRef");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final IFile type2 = this.workbenchTestHelper.createFile("Type2.xtend", _builder_1.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(type2, "Type1");
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Type1 {");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_2.toString());
      type1.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAddMethod() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Type3 {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile type3 = this.workbenchTestHelper.createFile("Type3.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Type1 {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("def Type3 foo() { return new Type3()}");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      this.workbenchTestHelper.createFile("Type1.xtend", _builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Type2 {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("def void doStuff() {");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("new Type1().foo().bar()");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final IFile type2 = this.workbenchTestHelper.createFile("Type2.xtend", _builder_2.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(type2, "bar");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("class Type3 {");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("def void bar() {}");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_3.toString());
      type3.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testExtendedClassChanges() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Type3 {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile type3 = this.workbenchTestHelper.createFile("Type3.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Type1 extends Type3 {");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      this.workbenchTestHelper.createFile("Type1.xtend", _builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Type2 {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("def void doStuff(Type1 foo) {");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("foo.bar");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final IFile type2 = this.workbenchTestHelper.createFile("Type2.xtend", _builder_2.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(type2, "bar");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("class Type3 {");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("def void bar() {}");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_3.toString());
      type3.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("class Type3 {");
      _builder_4.newLine();
      _builder_4.append("\t");
      _builder_4.append("def void baz() {}");
      _builder_4.newLine();
      _builder_4.append("}");
      _builder_4.newLine();
      StringInputStream _stringInputStream_1 = new StringInputStream(_builder_4.toString());
      type3.setContents(_stringInputStream_1, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(type2, "bar");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  /**
   * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=388828
   */
  @Test
  public void testBug388828() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("abstract class A {}");
      _builder.newLine();
      final IFile typeA = this.workbenchTestHelper.createFile("A.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class B extends A {");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final IFile typeB = this.workbenchTestHelper.createFile("B.xtend", _builder_1.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("abst ract class A {");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_2.toString());
      typeA.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(typeA, "abst");
      this.assertHasErrors(typeB, "A");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("abstract class A {}");
      _builder_3.newLine();
      StringInputStream _stringInputStream_1 = new StringInputStream(_builder_3.toString());
      typeA.setContents(_stringInputStream_1, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testChangeInAnnotationProcessor() {
    try {
      IProject _project = this.workbenchTestHelper.getProject();
      final IJavaProject macroProject = JavaCore.create(_project);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package anno");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.Active");
      _builder.newLine();
      _builder.newLine();
      _builder.append("@Active(AnnoProcessor)");
      _builder.newLine();
      _builder.append("annotation Anno {}");
      _builder.newLine();
      this.workbenchTestHelper.createFile("anno/Anno.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("package anno");
      _builder_1.newLine();
      _builder_1.append("import org.eclipse.xtend.lib.macro.AbstractFieldProcessor");
      _builder_1.newLine();
      _builder_1.append("import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration");
      _builder_1.newLine();
      _builder_1.append("import org.eclipse.xtend.lib.macro.TransformationContext");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("class AnnoProcessor extends AbstractFieldProcessor {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("override doTransform(MutableFieldDeclaration it, extension TransformationContext context) {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("}");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final IFile processorClass = this.workbenchTestHelper.createFile("anno/AnnoProcessor.xtend", _builder_1.toString());
      IProject _project_1 = macroProject.getProject();
      WorkbenchTestHelper.addExportedPackages(_project_1, "anno");
      IProject _createPluginProject = WorkbenchTestHelper.createPluginProject((WorkbenchTestHelper.TESTPROJECT_NAME + "-client"));
      final IJavaProject clientProject = JavaCore.create(_createPluginProject);
      JavaProjectSetupUtil.addProjectReference(clientProject, macroProject);
      IProject _project_2 = clientProject.getProject();
      IPath _fullPath = _project_2.getFullPath();
      String _string = _fullPath.toString();
      String _plus = (_string + "/src/");
      String _plus_1 = (_plus + "Foo.xtend");
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("import anno.Anno");
      _builder_2.newLine();
      _builder_2.append("class Foo {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("@Anno String foo");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("def bar() {");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("foo - 3");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final IFile clientClass = this.workbenchTestHelper.createFileImpl(_plus_1, _builder_2.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(clientClass, "- cannot be resolved");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("package anno");
      _builder_3.newLine();
      _builder_3.append("import org.eclipse.xtend.lib.macro.AbstractFieldProcessor");
      _builder_3.newLine();
      _builder_3.append("import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration");
      _builder_3.newLine();
      _builder_3.append("import org.eclipse.xtend.lib.macro.TransformationContext");
      _builder_3.newLine();
      _builder_3.newLine();
      _builder_3.append("class AnnoProcessor extends AbstractFieldProcessor {");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("override doTransform(MutableFieldDeclaration it, extension TransformationContext context) {");
      _builder_3.newLine();
      _builder_3.append("\t\t");
      _builder_3.append("type = primitiveInt");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("}");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      StringInputStream _stringInputStream = new StringInputStream(_builder_3.toString());
      processorClass.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      IProject _project_3 = clientProject.getProject();
      WorkbenchTestHelper.deleteProject(_project_3);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testChangeInResourceReadFromAnnotationProcessor() {
    try {
      IProject _project = this.workbenchTestHelper.getProject();
      final IJavaProject macroProject = JavaCore.create(_project);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package anno");
      _builder.newLine();
      _builder.append("import com.google.common.base.Splitter");
      _builder.newLine();
      _builder.append("import java.util.regex.Pattern");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.Active");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
      _builder.newLine();
      _builder.newLine();
      _builder.append("@Active(AnnoProcessor)");
      _builder.newLine();
      _builder.append("annotation Anno {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class AnnoProcessor extends AbstractClassProcessor {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("val content = context.getContents(cls.compilationUnit.filePath.parent.append(\"constants.txt\"))");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("val constants = Splitter.on(Pattern.compile(\"\\\\r?\\\\n\")).split(content)");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("constants.forEach [ constant |");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("cls.addField(constant) [");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("static = true");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("final = true");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("initializer = [\'\"\' + constant + \'\"\']");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("type = string");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      this.workbenchTestHelper.createFile("anno/Anno.xtend", _builder.toString());
      IProject _project_1 = macroProject.getProject();
      WorkbenchTestHelper.addExportedPackages(_project_1, "anno");
      IProject _createPluginProject = WorkbenchTestHelper.createPluginProject((WorkbenchTestHelper.TESTPROJECT_NAME + "-client"));
      final IJavaProject clientProject = JavaCore.create(_createPluginProject);
      JavaProjectSetupUtil.addProjectReference(clientProject, macroProject);
      IProject _project_2 = clientProject.getProject();
      IPath _fullPath = _project_2.getFullPath();
      String _string = _fullPath.toString();
      String _plus = (_string + "/src/");
      String _plus_1 = (_plus + "constants.txt");
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("A");
      _builder_1.newLine();
      _builder_1.append("B");
      final IFile constants = this.workbenchTestHelper.createFileImpl(_plus_1, _builder_1.toString());
      IProject _project_3 = clientProject.getProject();
      IPath _fullPath_1 = _project_3.getFullPath();
      String _string_1 = _fullPath_1.toString();
      String _plus_2 = (_string_1 + "/src/");
      String _plus_3 = (_plus_2 + "Foo.xtend");
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("import anno.Anno");
      _builder_2.newLine();
      _builder_2.append("@Anno");
      _builder_2.newLine();
      _builder_2.append("class Foo {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("def test() {");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("C");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final IFile fooClass = this.workbenchTestHelper.createFileImpl(_plus_3, _builder_2.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(fooClass, "C is undefined");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("A");
      _builder_3.newLine();
      _builder_3.append("B");
      _builder_3.newLine();
      _builder_3.append("C");
      StringInputStream _stringInputStream = new StringInputStream(_builder_3.toString());
      constants.setContents(_stringInputStream, true, true, null);
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      IProject _project_4 = clientProject.getProject();
      WorkbenchTestHelper.deleteProject(_project_4);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testChangeJavaClassLookedUpInAnnotationProcessor() {
    try {
      IProject _project = this.workbenchTestHelper.getProject();
      final IJavaProject macroProject = JavaCore.create(_project);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package anno");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.Active");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
      _builder.newLine();
      _builder.newLine();
      _builder.append("@Active(AnnoProcessor)");
      _builder.newLine();
      _builder.append("annotation Anno {}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class AnnoProcessor extends AbstractClassProcessor {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("val theType = context.findTypeGlobally(\"anno.A\")?:context.findTypeGlobally(\"anno.B\")");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("cls.addField(\"foo\") [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = theType.newTypeReference");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      this.workbenchTestHelper.createFile("anno/Anno.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("package anno");
      _builder_1.newLine();
      _builder_1.append("class B {}");
      _builder_1.newLine();
      this.workbenchTestHelper.createFile("anno/B.xtend", _builder_1.toString());
      IProject _project_1 = macroProject.getProject();
      WorkbenchTestHelper.addExportedPackages(_project_1, "anno");
      IProject _createPluginProject = WorkbenchTestHelper.createPluginProject((WorkbenchTestHelper.TESTPROJECT_NAME + "-client"));
      final IJavaProject clientProject = JavaCore.create(_createPluginProject);
      JavaProjectSetupUtil.addProjectReference(clientProject, macroProject);
      IProject _project_2 = clientProject.getProject();
      IPath _fullPath = _project_2.getFullPath();
      String _string = _fullPath.toString();
      String _plus = (_string + "/src/");
      String _plus_1 = (_plus + "Foo.xtend");
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("import anno.Anno");
      _builder_2.newLine();
      _builder_2.append("@Anno");
      _builder_2.newLine();
      _builder_2.append("class Foo {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("def test() {");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("foo.bar");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final IFile fooClass = this.workbenchTestHelper.createFileImpl(_plus_1, _builder_2.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertHasErrors(fooClass, "bar is undefined");
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("package anno");
      _builder_3.newLine();
      _builder_3.append("class A {");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("def bar() {}");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      this.workbenchTestHelper.createFile("anno/A.xtend", _builder_3.toString());
      IResourcesSetupUtil.waitForAutoBuild();
      this.assertNoErrorsInWorkspace();
      IProject _project_3 = clientProject.getProject();
      WorkbenchTestHelper.deleteProject(_project_3);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void assertNoErrorsInWorkspace() {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot _root = _workspace.getRoot();
      final IMarker[] findMarkers = _root.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      for (final IMarker iMarker : findMarkers) {
        String _message = MarkerUtilities.getMessage(iMarker);
        int _severity = MarkerUtilities.getSeverity(iMarker);
        boolean _equals = (_severity == IMarker.SEVERITY_ERROR);
        Assert.assertFalse(_message, _equals);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void assertHasErrors(final IFile file, final String msgPart) {
    try {
      final IMarker[] findMarkers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      for (final IMarker iMarker : findMarkers) {
        boolean _and = false;
        int _severity = MarkerUtilities.getSeverity(iMarker);
        boolean _equals = (_severity == IMarker.SEVERITY_ERROR);
        if (!_equals) {
          _and = false;
        } else {
          String _message = MarkerUtilities.getMessage(iMarker);
          boolean _contains = _message.contains(msgPart);
          _and = _contains;
        }
        if (_and) {
          return;
        }
      }
      IPath _fullPath = file.getFullPath();
      String _plus = ((("Expected an error marker containing \'" + msgPart) + "\' on ") + _fullPath);
      String _plus_1 = (_plus + " but found ");
      final Function1<IMarker, String> _function = new Function1<IMarker, String>() {
        public String apply(final IMarker it) {
          return MarkerUtilities.getMessage(it);
        }
      };
      List<String> _map = ListExtensions.<IMarker, String>map(((List<IMarker>)Conversions.doWrapArray(findMarkers)), _function);
      String _join = IterableExtensions.join(_map, ",");
      String _plus_2 = (_plus_1 + _join);
      Assert.fail(_plus_2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
