package org.eclipse.xtend.core.tests.macro;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.macro.AnnotationProcessor;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.tests.RuntimeInjectorProvider;
import org.eclipse.xtend.core.tests.macro.AbstractReusableActiveAnnotationTests;
import org.eclipse.xtend.core.tests.macro.DelegatingClassloader;
import org.eclipse.xtend.core.validation.IssueCodes;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration;
import org.eclipse.xtend.lib.macro.file.MutableFileSystemSupport;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.TemporaryFolder;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.file.RuntimeWorkspaceConfigProvider;
import org.eclipse.xtext.xbase.file.SimpleWorkspaceConfig;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(RuntimeInjectorProvider.class)
@SuppressWarnings("all")
public class ActiveAnnotationsRuntimeTest extends AbstractReusableActiveAnnotationTests {
  @Rule
  @Inject
  public TemporaryFolder temporaryFolder;
  
  @Inject
  private CompilationTestHelper compiler;
  
  @Inject
  private Provider<CompilationUnitImpl> compilationUnitProvider;
  
  @Inject
  private ProcessorInstanceForJvmTypeProvider processorProvider;
  
  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;
  
  @Inject
  private RuntimeWorkspaceConfigProvider configProvider;
  
  @Inject
  @Extension
  private MutableFileSystemSupport fileSystemSupport;
  
  @Inject
  private ValidationTestHelper validator;
  
  private final String macroProject = "macroProject";
  
  private final String clientProject = "userProject";
  
  @Before
  public void setUp() {
    this.compiler.setJavaCompilerClassPath(MutableTypeDeclaration.class, IterableExtensions.class, Lists.class);
    this.configureFreshWorkspace();
  }
  
  protected void configureFreshWorkspace() {
    final File tempDir = this.createFreshTempDir();
    String _absolutePath = tempDir.getAbsolutePath();
    SimpleWorkspaceConfig _simpleWorkspaceConfig = new SimpleWorkspaceConfig(_absolutePath);
    final Procedure1<SimpleWorkspaceConfig> _function = new Procedure1<SimpleWorkspaceConfig>() {
      @Override
      public void apply(final SimpleWorkspaceConfig it) {
        ProjectConfig _projectConfig = new ProjectConfig(ActiveAnnotationsRuntimeTest.this.macroProject);
        final Procedure1<ProjectConfig> _function = new Procedure1<ProjectConfig>() {
          @Override
          public void apply(final ProjectConfig it) {
            Map<Path, Path> _sourceFolderMappings = it.getSourceFolderMappings();
            Path _path = new Path((("/" + ActiveAnnotationsRuntimeTest.this.macroProject) + "/src"));
            Path _path_1 = new Path((("/" + ActiveAnnotationsRuntimeTest.this.macroProject) + "/xtend-gen"));
            _sourceFolderMappings.put(_path, _path_1);
          }
        };
        ProjectConfig _doubleArrow = ObjectExtensions.<ProjectConfig>operator_doubleArrow(_projectConfig, _function);
        it.addProjectConfig(_doubleArrow);
        ProjectConfig _projectConfig_1 = new ProjectConfig(ActiveAnnotationsRuntimeTest.this.clientProject);
        final Procedure1<ProjectConfig> _function_1 = new Procedure1<ProjectConfig>() {
          @Override
          public void apply(final ProjectConfig it) {
            Map<Path, Path> _sourceFolderMappings = it.getSourceFolderMappings();
            Path _path = new Path((("/" + ActiveAnnotationsRuntimeTest.this.clientProject) + "/src"));
            Path _path_1 = new Path((("/" + ActiveAnnotationsRuntimeTest.this.clientProject) + "/xtend-gen"));
            _sourceFolderMappings.put(_path, _path_1);
          }
        };
        ProjectConfig _doubleArrow_1 = ObjectExtensions.<ProjectConfig>operator_doubleArrow(_projectConfig_1, _function_1);
        it.addProjectConfig(_doubleArrow_1);
      }
    };
    SimpleWorkspaceConfig _doubleArrow = ObjectExtensions.<SimpleWorkspaceConfig>operator_doubleArrow(_simpleWorkspaceConfig, _function);
    this.configProvider.setWorkspaceConfig(_doubleArrow);
  }
  
  protected File createFreshTempDir() {
    try {
      return this.temporaryFolder.newFolder();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected URI copyToDisk(final String projectName, final Pair<String, String> fileRepresentation) {
    String _key = fileRepresentation.getKey();
    String _plus = ((("/" + projectName) + "/src/") + _key);
    final Path path = new Path(_plus);
    String _value = fileRepresentation.getValue();
    this.fileSystemSupport.setContents(path, _value);
    WorkspaceConfig _workspaceConfig = this.configProvider.getWorkspaceConfig();
    String _absoluteFileSystemPath = _workspaceConfig.getAbsoluteFileSystemPath();
    String _string = path.toString();
    String _plus_1 = (_absoluteFileSystemPath + _string);
    return URI.createFileURI(_plus_1);
  }
  
  @Override
  public void assertProcessing(final Pair<String, String> macroFile, final Pair<String, String> clientFile, final Procedure1<? super CompilationUnitImpl> expectations) {
    try {
      final XtextResourceSet resourceSet = this.compileMacroResourceSet(macroFile, clientFile);
      EList<Resource> _resources = resourceSet.getResources();
      final Resource singleResource = IterableExtensions.<Resource>head(_resources);
      Map<Object, Object> _emptyMap = CollectionLiterals.<Object, Object>emptyMap();
      singleResource.load(_emptyMap);
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        @Override
        public void accept(final CompilationTestHelper.Result it) {
          it.getGeneratedCode();
          final CompilationUnitImpl unit = ActiveAnnotationsRuntimeTest.this.compilationUnitProvider.get();
          EList<EObject> _contents = singleResource.getContents();
          Iterable<XtendFile> _filter = Iterables.<XtendFile>filter(_contents, XtendFile.class);
          final XtendFile xtendFile = IterableExtensions.<XtendFile>head(_filter);
          unit.setXtendFile(xtendFile);
          expectations.apply(unit);
        }
      };
      this.compiler.compile(resourceSet, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void assertIssues(final Pair<String, String> macroFile, final Pair<String, String> clientFile, final Procedure1<? super List<Issue>> expectations) {
    try {
      final XtextResourceSet resourceSet = this.compileMacroResourceSet(macroFile, clientFile);
      EList<Resource> _resources = resourceSet.getResources();
      Resource _head = IterableExtensions.<Resource>head(_resources);
      final XtextResource singleResource = ((XtextResource) _head);
      boolean _isLoaded = singleResource.isLoaded();
      boolean _not = (!_isLoaded);
      if (_not) {
        Map<Object, Object> _loadOptions = resourceSet.getLoadOptions();
        singleResource.load(_loadOptions);
      }
      IResourceServiceProvider _resourceServiceProvider = singleResource.getResourceServiceProvider();
      final IResourceValidator validator = _resourceServiceProvider.getResourceValidator();
      List<Issue> _validate = validator.validate(singleResource, CheckMode.ALL, CancelIndicator.NullImpl);
      expectations.apply(_validate);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public XtextResourceSet compileMacroResourceSet(final Pair<String, String> macroFile, final Pair<String, String> clientFile) {
    XtextResourceSet _xblockexpression = null;
    {
      final URI macroURI = this.copyToDisk(this.macroProject, macroFile);
      final URI clientURI = this.copyToDisk(this.clientProject, clientFile);
      final XtextResourceSet macroResourceSet = this.resourceSetProvider.get();
      Class<? extends ActiveAnnotationsRuntimeTest> _class = this.getClass();
      ClassLoader _classLoader = _class.getClassLoader();
      macroResourceSet.setClasspathURIContext(_classLoader);
      macroResourceSet.createResource(macroURI);
      final XtextResourceSet resourceSet = this.resourceSetProvider.get();
      resourceSet.createResource(clientURI);
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        @Override
        public void accept(final CompilationTestHelper.Result result) {
          Class<? extends ActiveAnnotationsRuntimeTest> _class = ActiveAnnotationsRuntimeTest.this.getClass();
          ClassLoader _classLoader = _class.getClassLoader();
          final DelegatingClassloader classLoader = new DelegatingClassloader(_classLoader, result);
          resourceSet.setClasspathURIContext(classLoader);
          ActiveAnnotationsRuntimeTest.this.processorProvider.setClassLoader(classLoader);
        }
      };
      this.compiler.compile(macroResourceSet, _function);
      _xblockexpression = resourceSet;
    }
    return _xblockexpression;
  }
  
  @Test
  @Override
  public void testSimpleModification() {
    super.testSimpleModification();
  }
  
  @Test
  public void testBug403563() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package myannotation");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.Active");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Active(ThrowExceptionAnnotationProcessor)");
    _builder.newLine();
    _builder.append("annotation ThrowExceptionAnnotation{ }");
    _builder.newLine();
    _builder.append("class ThrowExceptionAnnotationProcessor extends AbstractClassProcessor {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("throw new IllegalStateException");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, String> _mappedTo = Pair.<String, String>of("myannotation/ThrowExceptionAnnotationProcessor.xtend", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package myusercode");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@myannotation.ThrowExceptionAnnotation class MyClass {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    Pair<String, String> _mappedTo_1 = Pair.<String, String>of("myusercode/UserCode.xtend", _builder_1.toString());
    final Procedure1<List<Issue>> _function = new Procedure1<List<Issue>>() {
      @Override
      public void apply(final List<Issue> it) {
        int _size = it.size();
        Assert.assertEquals(1, _size);
        final Issue error = IterableExtensions.<Issue>head(it);
        Assert.assertNotNull(error);
        String _code = error.getCode();
        Assert.assertEquals(IssueCodes.PROCESSING_ERROR, _code);
        String _message = error.getMessage();
        String _name = IllegalStateException.class.getName();
        boolean _contains = _message.contains(_name);
        Assert.assertTrue(_contains);
        String _message_1 = error.getMessage();
        String _name_1 = AnnotationProcessor.class.getName();
        boolean _contains_1 = _message_1.contains(_name_1);
        Assert.assertFalse(_contains_1);
      }
    };
    this.assertIssues(_mappedTo, _mappedTo_1, _function);
  }
  
  @Test
  public void testDetectOrphanedElements() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package myannotation");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.Active");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.RegisterGlobalsContext");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Active(EvilProcessor)");
    _builder.newLine();
    _builder.append("annotation EvilAnnotation {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class EvilProcessor extends AbstractClassProcessor {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doRegisterGlobals(List<? extends ClassDeclaration> classes, extension RegisterGlobalsContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("classes.forEach[");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("registerClass(qualifiedName+\'.Inner\')");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("]");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doTransform(List<? extends MutableClassDeclaration> classes, extension TransformationContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("classes.forEach[");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("addField(\"foo\")[");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("type = object");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("markAsRead");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("]");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("addMethod(\"foo\")[");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("returnType = object");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("body = [\"return null;\"]");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("addParameter(\"x\", Integer.newTypeReference)");
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
    Pair<String, String> _mappedTo = Pair.<String, String>of("myannotation/EvilAnnotation.xtend", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package myusercode");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import myannotation.EvilAnnotation");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@EvilAnnotation");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    Pair<String, String> _mappedTo_1 = Pair.<String, String>of("myusercode/UserCode.xtend", _builder_1.toString());
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
      @Override
      public void apply(final CompilationUnitImpl it) {
        XtendFile _xtendFile = it.getXtendFile();
        ActiveAnnotationsRuntimeTest.this.validator.assertIssue(_xtendFile, XtendPackage.Literals.XTEND_FILE, IssueCodes.ORPHAN_ELMENT, Severity.WARNING, 
          "The generated field \'myusercode.Foo.foo\' is not associated with a source element.");
        XtendFile _xtendFile_1 = it.getXtendFile();
        ActiveAnnotationsRuntimeTest.this.validator.assertIssue(_xtendFile_1, XtendPackage.Literals.XTEND_FILE, IssueCodes.ORPHAN_ELMENT, Severity.WARNING, 
          "The generated method \'myusercode.Foo.foo(Integer)\' is not associated with a source element.");
        XtendFile _xtendFile_2 = it.getXtendFile();
        ActiveAnnotationsRuntimeTest.this.validator.assertIssue(_xtendFile_2, XtendPackage.Literals.XTEND_FILE, IssueCodes.ORPHAN_ELMENT, Severity.WARNING, 
          "The generated type \'myusercode.Foo.Inner\' is not associated with a source element.");
      }
    };
    this.assertProcessing(_mappedTo, _mappedTo_1, _function);
  }
  
  @Test
  public void testDetectOrphanedElements2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package myannotation");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.Active");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
    _builder.newLine();
    _builder.append("import org.eclipse.xtend.lib.macro.RegisterGlobalsContext");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Active(EvilProcessor)");
    _builder.newLine();
    _builder.append("annotation EvilAnnotation {}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class EvilProcessor extends AbstractClassProcessor {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doRegisterGlobals(ClassDeclaration clazz, extension RegisterGlobalsContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("registerClass(clazz.qualifiedName+\'.Inner\')");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("findClass(clazz.qualifiedName+\'.Inner\').primarySourceElement = clazz");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    Pair<String, String> _mappedTo = Pair.<String, String>of("myannotation/EvilAnnotation.xtend", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package myusercode");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import myannotation.EvilAnnotation");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@EvilAnnotation");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("Comparable<String> p = new Comparable<String>() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("override compareTo(String other) { 42 }");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    Pair<String, String> _mappedTo_1 = Pair.<String, String>of("myusercode/UserCode.xtend", _builder_1.toString());
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
      @Override
      public void apply(final CompilationUnitImpl it) {
        XtendFile _xtendFile = it.getXtendFile();
        ActiveAnnotationsRuntimeTest.this.validator.assertNoWarnings(_xtendFile, XtendPackage.Literals.XTEND_FILE, IssueCodes.ORPHAN_ELMENT);
      }
    };
    this.assertProcessing(_mappedTo, _mappedTo_1, _function);
  }
}
