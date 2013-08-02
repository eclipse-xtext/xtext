package org.eclipse.xtend.ide.tests.macros;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.tests.macro.AbstractReusableActiveAnnotationTests;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.internal.StopwatchRule;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.editor.hover.html.IEObjectHoverDocumentationProvider;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.After;
import org.junit.Assert;
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
  
  @Test
  public void testSimpleModification() {
    super.testSimpleModification();
  }
  
  @Test
  public void testDocumentationProvider() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package annotation");
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
    Pair<String,String> _mappedTo = Pair.<String, String>of("annotation/ChangeDoc.xtend", _builder.toString());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package usercode");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import annotation.ChangeDoc");
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
    Pair<String,String> _mappedTo_1 = Pair.<String, String>of("usercode/UserCode.xtend", _builder_1.toString());
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
      public void apply(final CompilationUnitImpl it) {
        XtendFile _xtendFile = it.getXtendFile();
        EList<XtendTypeDeclaration> _xtendTypes = _xtendFile.getXtendTypes();
        Iterable<XtendClass> _filter = Iterables.<XtendClass>filter(_xtendTypes, XtendClass.class);
        final XtendClass xtendClass = IterableExtensions.<XtendClass>head(_filter);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("@<a href=\"eclipse-xtext-doc:platform:/resource/macroProject/src/annotation/ChangeDoc.xtend%23/1\">ChangeDoc</a><br>Comment");
        ActiveAnnotationsProcessingInIDETest.this.assertDocumentation(_builder, xtendClass);
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Hello World!");
        EList<XtendMember> _members = xtendClass.getMembers();
        Iterable<XtendField> _filter_1 = Iterables.<XtendField>filter(_members, XtendField.class);
        final Function1<XtendField,Boolean> _function = new Function1<XtendField,Boolean>() {
          public Boolean apply(final XtendField it) {
            String _name = it.getName();
            boolean _equals = _name.equals("object");
            return Boolean.valueOf(_equals);
          }
        };
        Iterable<XtendField> _filter_2 = IterableExtensions.<XtendField>filter(_filter_1, _function);
        XtendField _head = IterableExtensions.<XtendField>head(_filter_2);
        JvmTypeReference _type = _head.getType();
        JvmType _type_1 = _type.getType();
        ActiveAnnotationsProcessingInIDETest.this.assertDocumentation(_builder_1, _type_1);
      }
    };
    this.assertProcessing(_mappedTo, _mappedTo_1, _function);
  }
  
  public void assertDocumentation(final CharSequence charSequence, final EObject sourceElement) {
    String _string = charSequence.toString();
    String _documentation = this.documentationProvider.getDocumentation(sourceElement);
    Assert.assertEquals(_string, _documentation);
  }
  
  @Inject
  private XtextResourceSetProvider resourceSetProvider;
  
  @Inject
  private Provider<CompilationUnitImpl> compilationUnitProvider;
  
  @Rule
  public StopwatchRule stopwatch = new Function0<StopwatchRule>() {
    public StopwatchRule apply() {
      StopwatchRule _stopwatchRule = new StopwatchRule(true);
      return _stopwatchRule;
    }
  }.apply();
  
  @After
  public void tearDown() throws Exception {
    this.macroProject = null;
    this.userProject = null;
    this.sourceFile = null;
    IResourcesSetupUtil.cleanWorkspace();
  }
  
  private IJavaProject macroProject;
  
  private IJavaProject userProject;
  
  private IFile sourceFile;
  
  public void assertProcessing(final Pair<String,String> macroFile, final Pair<String,String> clientFile, final Procedure1<? super CompilationUnitImpl> expectations) {
    try {
      IProject _createPluginProject = WorkbenchTestHelper.createPluginProject("macroProject");
      IJavaProject _create = JavaCore.create(_createPluginProject);
      this.macroProject = _create;
      String _key = macroFile.getKey();
      String _value = macroFile.getValue();
      String _string = _value.toString();
      this.newSource(this.macroProject, _key, _string);
      String _key_1 = macroFile.getKey();
      final int lidx = _key_1.lastIndexOf("/");
      int _minus = (-1);
      boolean _notEquals = (lidx != _minus);
      if (_notEquals) {
        String _key_2 = macroFile.getKey();
        String _substring = _key_2.substring(0, lidx);
        final String packageName = _substring.replace("/", ".");
        this.addExportedPackage(this.macroProject, packageName);
      }
      IProject _createPluginProject_1 = WorkbenchTestHelper.createPluginProject("userProject", "com.google.inject", "org.eclipse.xtend.lib", 
        "org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit", "macroProject");
      IJavaProject _create_1 = JavaCore.create(_createPluginProject_1);
      this.userProject = _create_1;
      String _key_3 = clientFile.getKey();
      String _value_1 = clientFile.getValue();
      String _string_1 = _value_1.toString();
      IFile _newSource = this.newSource(this.userProject, _key_3, _string_1);
      this.sourceFile = _newSource;
      IResourcesSetupUtil.waitForAutoBuild();
      IProject _project = this.macroProject.getProject();
      int _minus_1 = (-1);
      IMarker[] _findMarkers = _project.findMarkers(IMarker.PROBLEM, true, _minus_1);
      final Function1<IMarker,Boolean> _function = new Function1<IMarker,Boolean>() {
        public Boolean apply(final IMarker it) {
          try {
            Object _attribute = it.getAttribute(IMarker.SEVERITY);
            boolean _equals = Objects.equal(_attribute, Integer.valueOf(IMarker.SEVERITY_ERROR));
            return Boolean.valueOf(_equals);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      final Iterable<IMarker> markers = IterableExtensions.<IMarker>filter(((Iterable<IMarker>)Conversions.doWrapArray(_findMarkers)), _function);
      final Function1<IMarker,String> _function_1 = new Function1<IMarker,String>() {
        public String apply(final IMarker it) {
          try {
            IResource _resource = it.getResource();
            IPath _fullPath = _resource.getFullPath();
            String _lastSegment = _fullPath.lastSegment();
            String _plus = ("file" + _lastSegment);
            String _plus_1 = (_plus + " - ");
            Object _attribute = it.getAttribute(IMarker.MESSAGE);
            String _plus_2 = (_plus_1 + _attribute);
            return _plus_2;
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      Iterable<String> _map = IterableExtensions.<IMarker, String>map(markers, _function_1);
      String _join = IterableExtensions.join(_map, ",");
      int _length = ((Object[])Conversions.unwrapArray(markers, Object.class)).length;
      Assert.assertEquals(_join, 0, _length);
      IProject _project_1 = this.userProject.getProject();
      int _minus_2 = (-1);
      IMarker[] _findMarkers_1 = _project_1.findMarkers(IMarker.PROBLEM, true, _minus_2);
      final Function1<IMarker,Boolean> _function_2 = new Function1<IMarker,Boolean>() {
        public Boolean apply(final IMarker it) {
          try {
            Object _attribute = it.getAttribute(IMarker.SEVERITY);
            boolean _equals = Objects.equal(_attribute, Integer.valueOf(IMarker.SEVERITY_ERROR));
            return Boolean.valueOf(_equals);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      final Iterable<IMarker> markers2 = IterableExtensions.<IMarker>filter(((Iterable<IMarker>)Conversions.doWrapArray(_findMarkers_1)), _function_2);
      StringBuilder _stringBuilder = new StringBuilder();
      final StringBuilder buffer = _stringBuilder;
      final Function1<IMarker,IResource> _function_3 = new Function1<IMarker,IResource>() {
        public IResource apply(final IMarker it) {
          IResource _resource = it.getResource();
          return _resource;
        }
      };
      Iterable<IResource> _map_1 = IterableExtensions.<IMarker, IResource>map(markers2, _function_3);
      Set<IResource> _set = IterableExtensions.<IResource>toSet(_map_1);
      Iterable<IFile> _filter = Iterables.<IFile>filter(_set, IFile.class);
      final Function1<IFile,Long> _function_4 = new Function1<IFile,Long>() {
        public Long apply(final IFile it) {
          try {
            final InputSupplier<InputStreamReader> _function = new InputSupplier<InputStreamReader>() {
              public InputStreamReader getInput() throws IOException {
                try {
                  InputStream _contents = it.getContents();
                  InputStreamReader _inputStreamReader = new InputStreamReader(_contents);
                  return _inputStreamReader;
                } catch (Throwable _e) {
                  throw Exceptions.sneakyThrow(_e);
                }
              }
            };
            long _copy = CharStreams.<InputStreamReader>copy(_function, buffer);
            return Long.valueOf(_copy);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      /* IterableExtensions.<IFile, Long>map(_filter, _function_4); */
      InputOutput.<StringBuilder>println(buffer);
      final Function1<IMarker,String> _function_5 = new Function1<IMarker,String>() {
        public String apply(final IMarker it) {
          try {
            IResource _resource = it.getResource();
            IPath _fullPath = _resource.getFullPath();
            String _lastSegment = _fullPath.lastSegment();
            String _plus = ("file" + _lastSegment);
            String _plus_1 = (_plus + " - ");
            Object _attribute = it.getAttribute(IMarker.MESSAGE);
            String _plus_2 = (_plus_1 + _attribute);
            return _plus_2;
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      Iterable<String> _map_2 = IterableExtensions.<IMarker, String>map(markers2, _function_5);
      String _join_1 = IterableExtensions.join(_map_2, ",");
      int _length_1 = ((Object[])Conversions.unwrapArray(markers2, Object.class)).length;
      Assert.assertEquals(_join_1, 0, _length_1);
      IProject _project_2 = this.userProject.getProject();
      final ResourceSet resourceSet = this.resourceSetProvider.get(_project_2);
      IPath _fullPath = this.sourceFile.getFullPath();
      String _string_2 = _fullPath.toString();
      URI _createPlatformResourceURI = URI.createPlatformResourceURI(_string_2, true);
      final Resource resource = resourceSet.getResource(_createPlatformResourceURI, true);
      final CompilationUnitImpl unit = this.compilationUnitProvider.get();
      EList<EObject> _contents = resource.getContents();
      Iterable<XtendFile> _filter_1 = Iterables.<XtendFile>filter(_contents, XtendFile.class);
      XtendFile _head = IterableExtensions.<XtendFile>head(_filter_1);
      unit.setXtendFile(_head);
      expectations.apply(unit);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public IFile newSource(final IJavaProject it, final String fileName, final String contents) {
    try {
      IProject _project = it.getProject();
      String _plus = ("src/" + fileName);
      final IFile result = _project.getFile(_plus);
      IContainer parent = result.getParent();
      boolean _exists = parent.exists();
      boolean _not = (!_exists);
      boolean _while = _not;
      while (_while) {
        ((IFolder) parent).create(true, false, null);
        boolean _exists_1 = parent.exists();
        boolean _not_1 = (!_exists_1);
        _while = _not_1;
      }
      StringInputStream _stringInputStream = new StringInputStream(contents);
      result.create(_stringInputStream, true, null);
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void addExportedPackage(final IJavaProject pluginProject, final String... exportedPackages) {
    try {
      IProject _project = pluginProject.getProject();
      final IFile manifestFile = _project.getFile("META-INF/MANIFEST.MF");
      InputStream _contents = manifestFile.getContents();
      Manifest _manifest = new Manifest(_contents);
      final Manifest manifest = _manifest;
      final Attributes attrs = manifest.getMainAttributes();
      boolean _containsKey = attrs.containsKey("Export-Package");
      if (_containsKey) {
        Object _get = attrs.get("Export-Package");
        String _plus = (_get + ",");
        String _join = IterableExtensions.join(((Iterable<? extends Object>)Conversions.doWrapArray(exportedPackages)), ",");
        String _plus_1 = (_plus + _join);
        attrs.putValue("Export-Package", _plus_1);
      } else {
        String _join_1 = IterableExtensions.join(((Iterable<? extends Object>)Conversions.doWrapArray(exportedPackages)), ",");
        attrs.putValue("Export-Package", _join_1);
      }
      ByteArrayOutputStream _byteArrayOutputStream = new ByteArrayOutputStream();
      final ByteArrayOutputStream out = _byteArrayOutputStream;
      manifest.write(out);
      byte[] _byteArray = out.toByteArray();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_byteArray);
      final ByteArrayInputStream in = _byteArrayInputStream;
      BufferedInputStream _bufferedInputStream = new BufferedInputStream(in);
      manifestFile.setContents(_bufferedInputStream, true, true, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
