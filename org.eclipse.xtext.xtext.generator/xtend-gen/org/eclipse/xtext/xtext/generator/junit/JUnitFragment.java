/**
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Objects;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.xbase.lib.CollectionExtensions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xtext.generator.AbstractStubGeneratingFragment;
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;
import org.eclipse.xtext.xtext.generator.model.JavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;

/**
 * @since 2.14
 */
@SuppressWarnings("all")
public class JUnitFragment extends AbstractStubGeneratingFragment {
  @Inject
  @Extension
  private XtextGeneratorNaming _xtextGeneratorNaming;

  @Inject
  private FileAccessFactory fileAccessFactory;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean skipXbaseTestingPackage = false;

  private JUnitVersion junitVersion = JUnitVersion.JUNIT_4;

  public JUnitFragment() {
    this.setGenerateXtendStub(true);
  }

  public void setJunitVersion(final String version) {
    this.junitVersion = JUnitVersion.fromString(version);
  }

  protected String getTestingPackage() {
    return "org.eclipse.xtext.testing";
  }

  protected String getUiTestingPackage() {
    return "org.eclipse.xtext.ui.testing";
  }

  protected String getXbaseTestingPackage() {
    String _xifexpression = null;
    if (this.skipXbaseTestingPackage) {
      return "";
    } else {
      _xifexpression = "org.eclipse.xtext.xbase.testing";
    }
    return _xifexpression;
  }

  protected String getXbaseUiTestingPackage() {
    if (this.skipXbaseTestingPackage) {
      return "";
    }
    return "org.eclipse.xtext.xbase.ui.testing";
  }

  @Override
  public void generate() {
    ManifestAccess _manifest = this.getProjectConfig().getRuntimeTest().getManifest();
    boolean _tripleNotEquals = (_manifest != null);
    if (_tripleNotEquals) {
      ManifestAccess _manifest_1 = this.getProjectConfig().getRuntimeTest().getManifest();
      final Procedure1<ManifestAccess> _function = (ManifestAccess it) -> {
        String _testingPackage = this.getTestingPackage();
        String _xbaseTestingPackage = this.getXbaseTestingPackage();
        String _xbaseLibVersionLowerBound = this.getProjectConfig().getRuntime().getXbaseLibVersionLowerBound();
        String _plus = ("org.eclipse.xtext.xbase.lib;bundle-version=\"" + _xbaseLibVersionLowerBound);
        String _plus_1 = (_plus + "\"");
        CollectionExtensions.<String>addAll(it.getRequiredBundles(), _testingPackage, _xbaseTestingPackage, _plus_1);
        String _runtimeTestBasePackage = this._xtextGeneratorNaming.getRuntimeTestBasePackage(this.getGrammar());
        String _plus_2 = (_runtimeTestBasePackage + ";x-internal=true");
        it.getExportedPackages().add(_plus_2);
      };
      ObjectExtensions.<ManifestAccess>operator_doubleArrow(_manifest_1, _function);
    }
    ManifestAccess _manifest_2 = this.getProjectConfig().getEclipsePluginTest().getManifest();
    boolean _tripleNotEquals_1 = (_manifest_2 != null);
    if (_tripleNotEquals_1) {
      ManifestAccess _manifest_3 = this.getProjectConfig().getEclipsePluginTest().getManifest();
      final Procedure1<ManifestAccess> _function_1 = (ManifestAccess it) -> {
        CollectionExtensions.<String>addAll(it.getRequiredBundles(), 
          this.getTestingPackage(), 
          this.getXbaseTestingPackage(), 
          this.getUiTestingPackage(), 
          this.getXbaseUiTestingPackage(), 
          "org.eclipse.core.runtime", 
          "org.eclipse.ui.workbench;resolution:=optional");
        String _eclipsePluginTestBasePackage = this._xtextGeneratorNaming.getEclipsePluginTestBasePackage(this.getGrammar());
        String _plus = (_eclipsePluginTestBasePackage + ";x-internal=true");
        it.getExportedPackages().add(_plus);
      };
      ObjectExtensions.<ManifestAccess>operator_doubleArrow(_manifest_3, _function_1);
    }
    ManifestAccess _manifest_4 = this.getProjectConfig().getEclipsePlugin().getManifest();
    boolean _tripleNotEquals_2 = (_manifest_4 != null);
    if (_tripleNotEquals_2) {
      this.getProjectConfig().getEclipsePlugin().getManifest().getExportedPackages().add(this._xtextGeneratorNaming.getEclipsePluginActivator().getPackageName());
    }
    this.generateInjectorProvider().writeTo(this.getProjectConfig().getRuntimeTest().getSrcGen());
    boolean _isGenerateStub = this.isGenerateStub();
    if (_isGenerateStub) {
      boolean _isGenerateXtendStub = this.isGenerateXtendStub();
      if (_isGenerateXtendStub) {
        this.generateExampleRuntimeTest().writeTo(this.getProjectConfig().getRuntimeTest().getSrc());
      } else {
        this.generateJavaExampleRuntimeTest().writeTo(this.getProjectConfig().getRuntimeTest().getSrc());
      }
    }
    IXtextGeneratorFileSystemAccess _srcGen = this.getProjectConfig().getEclipsePlugin().getSrcGen();
    boolean _tripleNotEquals_3 = (_srcGen != null);
    if (_tripleNotEquals_3) {
      this.generateUiInjectorProvider().writeTo(this.getProjectConfig().getEclipsePluginTest().getSrcGen());
    }
  }

  protected JavaFileAccess generateExampleRuntimeTest() {
    String _testingPackage = this.getTestingPackage();
    String _plus = (_testingPackage + ".XtextRunner");
    final TypeReference xtextRunner = new TypeReference(_plus);
    final TypeReference runWith = new TypeReference("org.junit.runner.RunWith");
    String _testingPackage_1 = this.getTestingPackage();
    String _plus_1 = (_testingPackage_1 + ".InjectWith");
    final TypeReference injectWith = new TypeReference(_plus_1);
    final TypeReference extendWith = new TypeReference("org.junit.jupiter.api.^extension.ExtendWith");
    final TypeReference injectionExtension = new TypeReference("org.eclipse.xtext.testing.extensions.InjectionExtension");
    String _testingPackage_2 = this.getTestingPackage();
    String _plus_2 = (_testingPackage_2 + ".util.ParseHelper");
    final TypeReference parseHelper = new TypeReference(_plus_2);
    TypeReference _switchResult = null;
    final JUnitVersion junitVersion = this.junitVersion;
    if (junitVersion != null) {
      switch (junitVersion) {
        case JUNIT_4:
          _switchResult = new TypeReference("org.junit.Test");
          break;
        case JUNIT_6:
          _switchResult = new TypeReference("org.junit.jupiter.api.Test");
          break;
        default:
          break;
      }
    }
    final TypeReference test = _switchResult;
    TypeReference _switchResult_1 = null;
    final JUnitVersion junitVersion_1 = this.junitVersion;
    if (junitVersion_1 != null) {
      switch (junitVersion_1) {
        case JUNIT_4:
          _switchResult_1 = new TypeReference("org.junit.Assert");
          break;
        case JUNIT_6:
          _switchResult_1 = new TypeReference("org.junit.jupiter.api.Assertions");
          break;
        default:
          break;
      }
    }
    final TypeReference assert_ = _switchResult_1;
    EClassifier _classifier = IterableExtensions.<AbstractRule>head(this.getGrammar().getRules()).getType().getClassifier();
    ResourceSet _resourceSet = this.getGrammar().eResource().getResourceSet();
    final TypeReference rootType = new TypeReference(((EClass) _classifier), _resourceSet);
    TypeReference _exampleRuntimeTest = this.exampleRuntimeTest();
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          boolean _equals = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_4);
          if (_equals) {
            _builder.append("@");
            _builder.append(runWith);
            _builder.append("(");
            _builder.append(xtextRunner);
            _builder.append(")");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          boolean _equals_1 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_6);
          if (_equals_1) {
            _builder.append("@");
            _builder.append(extendWith);
            _builder.append("(");
            _builder.append(injectionExtension);
            _builder.append(")");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("@");
        _builder.append(injectWith);
        _builder.append("(");
        TypeReference _injectorProvider = JUnitFragment.this.injectorProvider();
        _builder.append(_injectorProvider);
        _builder.append(")");
        _builder.newLineIfNotEmpty();
        _builder.append("class ");
        TypeReference _exampleRuntimeTest = JUnitFragment.this.exampleRuntimeTest();
        _builder.append(_exampleRuntimeTest);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(Inject.class, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(parseHelper, "\t");
        _builder.append("<");
        _builder.append(rootType, "\t");
        _builder.append("> parseHelper");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(test, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("def void loadModel() {");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("val result = parseHelper.parse(\'\'");
        _builder.append("\'");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("Hello Xtext!");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("\'\'");
        _builder.append("\')");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append(assert_, "\t\t");
        _builder.append(".assertNotNull(result)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("val errors = result.eResource.errors");
        _builder.newLine();
        {
          boolean _equals_2 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_4);
          if (_equals_2) {
            _builder.append("\t\t");
            _builder.append(assert_, "\t\t");
            _builder.append(".assertTrue(\'\'");
            _builder.append("\'Unexpected errors: ");
            _builder.append("«", "\t\t");
            _builder.append("errors.join(\", \")");
            _builder.append("»", "\t\t");
            _builder.append("\'\'");
            _builder.append("\', errors.isEmpty)");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          boolean _equals_3 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_6);
          if (_equals_3) {
            _builder.append("\t\t");
            _builder.append(assert_, "\t\t");
            _builder.append(".assertTrue(errors.isEmpty, \'\'");
            _builder.append("\'Unexpected errors: ");
            _builder.append("«", "\t\t");
            _builder.append("errors.join(\", \")");
            _builder.append("»", "\t\t");
            _builder.append("\'\'");
            _builder.append("\')");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    return this.fileAccessFactory.createXtendFile(_exampleRuntimeTest, _client);
  }

  protected JavaFileAccess generateJavaExampleRuntimeTest() {
    String _testingPackage = this.getTestingPackage();
    String _plus = (_testingPackage + ".XtextRunner");
    final TypeReference xtextRunner = new TypeReference(_plus);
    final TypeReference runWith = new TypeReference("org.junit.runner.RunWith");
    final TypeReference extendWith = new TypeReference("org.junit.jupiter.api.extension.ExtendWith");
    final TypeReference injectionExtension = new TypeReference("org.eclipse.xtext.testing.extensions.InjectionExtension");
    String _testingPackage_1 = this.getTestingPackage();
    String _plus_1 = (_testingPackage_1 + ".InjectWith");
    final TypeReference injectWith = new TypeReference(_plus_1);
    String _testingPackage_2 = this.getTestingPackage();
    String _plus_2 = (_testingPackage_2 + ".util.ParseHelper");
    final TypeReference parseHelper = new TypeReference(_plus_2);
    TypeReference _switchResult = null;
    final JUnitVersion junitVersion = this.junitVersion;
    if (junitVersion != null) {
      switch (junitVersion) {
        case JUNIT_4:
          _switchResult = new TypeReference("org.junit.Test");
          break;
        case JUNIT_6:
          _switchResult = new TypeReference("org.junit.jupiter.api.Test");
          break;
        default:
          break;
      }
    }
    final TypeReference test = _switchResult;
    TypeReference _switchResult_1 = null;
    final JUnitVersion junitVersion_1 = this.junitVersion;
    if (junitVersion_1 != null) {
      switch (junitVersion_1) {
        case JUNIT_4:
          _switchResult_1 = new TypeReference("org.junit.Assert");
          break;
        case JUNIT_6:
          _switchResult_1 = new TypeReference("org.junit.jupiter.api.Assertions");
          break;
        default:
          break;
      }
    }
    final TypeReference assert_ = _switchResult_1;
    EClassifier _classifier = IterableExtensions.<AbstractRule>head(this.getGrammar().getRules()).getType().getClassifier();
    ResourceSet _resourceSet = this.getGrammar().eResource().getResourceSet();
    final TypeReference rootType = new TypeReference(((EClass) _classifier), _resourceSet);
    final TypeReference list = new TypeReference("java.util.List");
    final TypeReference diagnostic = new TypeReference("org.eclipse.emf.ecore.resource", "Resource.Diagnostic");
    final TypeReference iterableExtensions = new TypeReference("org.eclipse.xtext.xbase.lib.IterableExtensions");
    TypeReference _exampleRuntimeTest = this.exampleRuntimeTest();
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          boolean _equals = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_4);
          if (_equals) {
            _builder.append("@");
            _builder.append(runWith);
            _builder.append("(");
            _builder.append(xtextRunner);
            _builder.append(".class)");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          boolean _equals_1 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_6);
          if (_equals_1) {
            _builder.append("@");
            _builder.append(extendWith);
            _builder.append("(");
            _builder.append(injectionExtension);
            _builder.append(".class)");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("@");
        _builder.append(injectWith);
        _builder.append("(");
        TypeReference _injectorProvider = JUnitFragment.this.injectorProvider();
        _builder.append(_injectorProvider);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("public class ");
        TypeReference _exampleRuntimeTest = JUnitFragment.this.exampleRuntimeTest();
        _builder.append(_exampleRuntimeTest);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(Inject.class, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("private ");
        _builder.append(parseHelper, "\t");
        _builder.append("<");
        _builder.append(rootType, "\t");
        _builder.append("> parseHelper;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(test, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("public void loadModel() throws Exception {");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("Model result = parseHelper.parse(\"Hello Xtext!\");");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append(assert_, "\t\t");
        _builder.append(".assertNotNull(result);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append(list, "\t\t");
        _builder.append("<");
        _builder.append(diagnostic, "\t\t");
        _builder.append("> errors = result.eResource().getErrors();");
        _builder.newLineIfNotEmpty();
        {
          boolean _equals_2 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_4);
          if (_equals_2) {
            _builder.append("\t\t");
            _builder.append(assert_, "\t\t");
            _builder.append(".assertTrue(\"Unexpected errors: \" + ");
            _builder.append(iterableExtensions, "\t\t");
            _builder.append(".join(errors, \", \"), errors.isEmpty());");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          boolean _equals_3 = Objects.equals(JUnitFragment.this.junitVersion, JUnitVersion.JUNIT_6);
          if (_equals_3) {
            _builder.append("\t\t");
            _builder.append(assert_, "\t\t");
            _builder.append(".assertTrue(errors.isEmpty(), \"Unexpected errors: \" + ");
            _builder.append(iterableExtensions, "\t\t");
            _builder.append(".join(errors, \", \"));");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    return this.fileAccessFactory.createJavaFile(_exampleRuntimeTest, _client);
  }

  protected TypeReference exampleRuntimeTest() {
    String _runtimeTestBasePackage = this._xtextGeneratorNaming.getRuntimeTestBasePackage(this.getGrammar());
    String _simpleName = GrammarUtil.getSimpleName(this.getGrammar());
    String _plus = (_simpleName + "ParsingTest");
    return new TypeReference(_runtimeTestBasePackage, _plus);
  }

  protected JavaFileAccess generateInjectorProvider() {
    JavaFileAccess _xblockexpression = null;
    {
      final JavaFileAccess file = this.fileAccessFactory.createJavaFile(this.injectorProvider());
      String _testingPackage = this.getTestingPackage();
      String _plus = (_testingPackage + ".GlobalRegistries");
      final TypeReference globalRegistries = new TypeReference(_plus);
      String _testingPackage_1 = this.getTestingPackage();
      final TypeReference globalStateMemento = new TypeReference(_testingPackage_1, "GlobalRegistries.GlobalStateMemento");
      String _testingPackage_2 = this.getTestingPackage();
      String _plus_1 = (_testingPackage_2 + ".IRegistryConfigurator");
      final TypeReference iRegistryConfigurator = new TypeReference(_plus_1);
      final TypeReference classLoader = new TypeReference("java.lang.ClassLoader");
      final TypeReference guice = new TypeReference("com.google.inject.Guice");
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = JUnitFragment.this.injectorProvider().getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" implements ");
          TypeReference _iInjectorProvider = JUnitFragment.this.iInjectorProvider();
          _builder.append(_iInjectorProvider);
          _builder.append(", ");
          _builder.append(iRegistryConfigurator);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected ");
          _builder.append(globalStateMemento, "\t");
          _builder.append(" stateBeforeInjectorCreation;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("protected ");
          _builder.append(globalStateMemento, "\t");
          _builder.append(" stateAfterInjectorCreation;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("protected ");
          _builder.append(Injector.class, "\t");
          _builder.append(" injector;");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("static {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append(globalRegistries, "\t\t");
          _builder.append(".initializeDefaults();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          _builder.append(Injector.class, "\t");
          _builder.append(" getInjector() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("if (injector == null) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("this.injector = internalCreateInjector();");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("stateAfterInjectorCreation = ");
          _builder.append(globalRegistries, "\t\t\t");
          _builder.append(".makeCopyOfGlobalState();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("return injector;");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected ");
          _builder.append(Injector.class, "\t");
          _builder.append(" internalCreateInjector() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return new ");
          TypeReference _runtimeSetup = JUnitFragment.this._xtextGeneratorNaming.getRuntimeSetup(JUnitFragment.this.getGrammar());
          _builder.append(_runtimeSetup, "\t\t");
          _builder.append("() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("public Injector createInjector() {");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("return ");
          _builder.append(guice, "\t\t\t\t");
          _builder.append(".createInjector(createRuntimeModule());");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}.createInjectorAndDoEMFRegistration();");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected ");
          TypeReference _runtimeModule = JUnitFragment.this._xtextGeneratorNaming.getRuntimeModule(JUnitFragment.this.getGrammar());
          _builder.append(_runtimeModule, "\t");
          _builder.append(" createRuntimeModule() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("// make it work also with Maven/Tycho and OSGI");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("// allows for bindClassLoaderToInstance to get the class loader of the bundle");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("// containing the instance of the injector provider (possibly inherited)");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("return new ");
          TypeReference _runtimeModule_1 = JUnitFragment.this._xtextGeneratorNaming.getRuntimeModule(JUnitFragment.this.getGrammar());
          _builder.append(_runtimeModule_1, "\t\t");
          _builder.append("() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("public ");
          _builder.append(classLoader, "\t\t\t");
          _builder.append(" bindClassLoaderToInstance() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t");
          _builder.append("return ");
          String _simpleName_1 = JUnitFragment.this.injectorProvider().getSimpleName();
          _builder.append(_simpleName_1, "\t\t\t\t");
          _builder.append(".this.getClass()");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t\t\t");
          _builder.append(".getClassLoader();");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("};");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public void restoreRegistry() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("stateBeforeInjectorCreation.restoreGlobalState();");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("stateBeforeInjectorCreation = null;");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public void setupRegistry() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("stateBeforeInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("if (injector == null) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("getInjector();");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("stateAfterInjectorCreation.restoreGlobalState();");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      file.setContent(_client);
      _xblockexpression = file;
    }
    return _xblockexpression;
  }

  protected TypeReference iInjectorProvider() {
    String _testingPackage = this.getTestingPackage();
    String _plus = (_testingPackage + ".IInjectorProvider");
    return new TypeReference(_plus);
  }

  protected TypeReference injectorProvider() {
    String _runtimeTestBasePackage = this._xtextGeneratorNaming.getRuntimeTestBasePackage(this.getGrammar());
    String _simpleName = GrammarUtil.getSimpleName(this.getGrammar());
    String _plus = (_simpleName + "InjectorProvider");
    return new TypeReference(_runtimeTestBasePackage, _plus);
  }

  protected JavaFileAccess generateUiInjectorProvider() {
    JavaFileAccess _xblockexpression = null;
    {
      final JavaFileAccess file = this.fileAccessFactory.createJavaFile(this.uiInjectorProvider());
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = JUnitFragment.this.uiInjectorProvider().getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" implements ");
          TypeReference _iInjectorProvider = JUnitFragment.this.iInjectorProvider();
          _builder.append(_iInjectorProvider);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          _builder.append(Injector.class, "\t");
          _builder.append(" getInjector() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return ");
          TypeReference _eclipsePluginActivator = JUnitFragment.this._xtextGeneratorNaming.getEclipsePluginActivator();
          _builder.append(_eclipsePluginActivator, "\t\t");
          _builder.append(".getInstance().getInjector(\"");
          String _name = JUnitFragment.this.getGrammar().getName();
          _builder.append(_name, "\t\t");
          _builder.append("\");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      file.setContent(_client);
      _xblockexpression = file;
    }
    return _xblockexpression;
  }

  protected TypeReference uiInjectorProvider() {
    String _eclipsePluginTestBasePackage = this._xtextGeneratorNaming.getEclipsePluginTestBasePackage(this.getGrammar());
    String _simpleName = GrammarUtil.getSimpleName(this.getGrammar());
    String _plus = (_simpleName + "UiInjectorProvider");
    return new TypeReference(_eclipsePluginTestBasePackage, _plus);
  }

  public void setSkipXbaseTestingPackage(final boolean skipXbaseTestingPackage) {
    this.skipXbaseTestingPackage = skipXbaseTestingPackage;
  }
}
