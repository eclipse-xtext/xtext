/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.Constants;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.resource.impl.BinaryGrammarResourceFactoryImpl;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.GeneratedJavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.JavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.TextFileAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.model.annotations.IClassAnnotation;
import org.eclipse.xtext.xtext.generator.model.annotations.SuppressWarningsAnnotation;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;

/**
 * Templates for generating the common language infrastructure.
 */
@Singleton
@SuppressWarnings("all")
public class XtextGeneratorTemplates {
  @Inject
  private CodeConfig codeConfig;

  @Inject
  private FileAccessFactory fileAccessFactory;

  @Inject
  @Extension
  private XtextGeneratorNaming naming;

  public JavaFileAccess createRuntimeSetup(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _runtimeSetup = this.naming.getRuntimeSetup(it);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Initialization support for running Xtext languages without Equinox extension registry.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeGenSetup = XtextGeneratorTemplates.this.naming.getRuntimeGenSetup(it);
          _builder.append(_runtimeGenSetup);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("def static void doSetup() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("new ");
          String _simpleName_1 = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it).getSimpleName();
          _builder.append(_simpleName_1, "\t\t");
          _builder.append("().createInjectorAndDoEMFRegistration()");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createXtendFile(_runtimeSetup, _client);
    } else {
      TypeReference _runtimeSetup_1 = this.naming.getRuntimeSetup(it);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Initialization support for running Xtext languages without Equinox extension registry.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeGenSetup = XtextGeneratorTemplates.this.naming.getRuntimeGenSetup(it);
          _builder.append(_runtimeGenSetup);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public static void doSetup() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("new ");
          String _simpleName_1 = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it).getSimpleName();
          _builder.append(_simpleName_1, "\t\t");
          _builder.append("().createInjectorAndDoEMFRegistration();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createJavaFile(_runtimeSetup_1, _client_1);
    }
  }

  @Deprecated
  private static void addBackwardsCompabibleImportsTo(final IXtextGeneratorLanguage langConfig, final GeneratedJavaFileAccess file) {
    Set<TypeReference> _imports = langConfig.getRuntimeGenSetup().getImports();
    for (final TypeReference type : _imports) {
      file.importType(type);
    }
  }

  public JavaFileAccess createRuntimeGenSetup(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this.naming.getRuntimeGenSetup(it));
    XtextGeneratorTemplates.addBackwardsCompabibleImportsTo(langConfig, file);
    List<IClassAnnotation> _annotations = file.getAnnotations();
    SuppressWarningsAnnotation _suppressWarningsAnnotation = new SuppressWarningsAnnotation();
    _annotations.add(_suppressWarningsAnnotation);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public class ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeGenSetup(it).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" implements ");
        _builder.append(ISetup.class);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public ");
        _builder.append(Injector.class, "\t");
        _builder.append(" createInjectorAndDoEMFRegistration() {");
        _builder.newLineIfNotEmpty();
        {
          EList<Grammar> _usedGrammars = langConfig.getGrammar().getUsedGrammars();
          for(final Grammar usedGrammar : _usedGrammars) {
            _builder.append("\t\t");
            TypeReference _runtimeSetup = XtextGeneratorTemplates.this.naming.getRuntimeSetup(usedGrammar);
            _builder.append(_runtimeSetup, "\t\t");
            _builder.append(".doSetup();");
            _builder.newLineIfNotEmpty();
          }
        }
        {
          boolean _isEmpty = langConfig.getGrammar().getUsedGrammars().isEmpty();
          if (_isEmpty) {
            _builder.append("\t\t");
            _builder.append("// register default ePackages");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("if (!");
            TypeReference _typeRef = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef, "\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey(\"ecore\"))");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            TypeReference _typeRef_1 = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef_1, "\t\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"ecore\", new ");
            TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl");
            _builder.append(_typeRef_2, "\t\t\t\t");
            _builder.append("());");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("if (!");
            TypeReference _typeRef_3 = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef_3, "\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey(\"xmi\"))");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            TypeReference _typeRef_4 = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef_4, "\t\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"xmi\", new ");
            TypeReference _typeRef_5 = TypeReference.typeRef("org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl");
            _builder.append(_typeRef_5, "\t\t\t\t");
            _builder.append("());");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("if (!");
            TypeReference _typeRef_6 = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef_6, "\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey(\"xtextbin\"))");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            TypeReference _typeRef_7 = TypeReference.typeRef(Resource.class);
            _builder.append(_typeRef_7, "\t\t\t");
            _builder.append(".Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"xtextbin\", new ");
            _builder.append(BinaryGrammarResourceFactoryImpl.class, "\t\t\t\t");
            _builder.append("());");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("if (!");
            TypeReference _typeRef_8 = TypeReference.typeRef(EPackage.class);
            _builder.append(_typeRef_8, "\t\t");
            _builder.append(".Registry.INSTANCE.containsKey(");
            _builder.append(XtextPackage.class, "\t\t");
            _builder.append(".eNS_URI))");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            TypeReference _typeRef_9 = TypeReference.typeRef(EPackage.class);
            _builder.append(_typeRef_9, "\t\t\t");
            _builder.append(".Registry.INSTANCE.put(");
            _builder.append(XtextPackage.class, "\t\t\t");
            _builder.append(".eNS_URI, ");
            _builder.append(XtextPackage.class, "\t\t\t");
            _builder.append(".eINSTANCE);");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append(Injector.class, "\t\t");
        _builder.append(" injector = createInjector();");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("register(injector);");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("return injector;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public ");
        _builder.append(Injector.class, "\t");
        _builder.append(" createInjector() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return ");
        _builder.append(Guice.class, "\t\t");
        _builder.append(".createInjector(new ");
        TypeReference _runtimeModule = XtextGeneratorTemplates.this.naming.getRuntimeModule(it);
        _builder.append(_runtimeModule, "\t\t");
        _builder.append("());");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void register(");
        _builder.append(Injector.class, "\t");
        _builder.append(" injector) {");
        _builder.newLineIfNotEmpty();
        {
          List<StringConcatenationClient> _registrations = langConfig.getRuntimeGenSetup().getRegistrations();
          for(final StringConcatenationClient reg : _registrations) {
            _builder.append("\t\t");
            _builder.append(reg, "\t\t");
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
    file.setContent(_client);
    return file;
  }

  private CharSequence getBindMethodName(final GuiceModuleAccess.Binding it) {
    StringConcatenation _builder = new StringConcatenation();
    {
      if (((!it.getValue().isProvider()) && it.getValue().getStatements().isEmpty())) {
        _builder.append("bind");
      } else {
        boolean _isEmpty = it.getValue().getStatements().isEmpty();
        if (_isEmpty) {
          _builder.append("provide");
        } else {
          _builder.append("configure");
        }
      }
    }
    String _elvis = null;
    String _name = it.getKey().getName();
    String _replace = null;
    if (_name!=null) {
      _replace=_name.replace(".", "$");
    }
    if (_replace != null) {
      _elvis = _replace;
    } else {
      String _simpleMethodName = this.getSimpleMethodName(it.getKey().getType());
      _elvis = _simpleMethodName;
    }
    _builder.append(_elvis);
    {
      if (((it.getValue().getExpression() != null) && (!it.getValue().isProvider()))) {
        _builder.append("ToInstance");
      }
    }
    return _builder;
  }

  private String getSimpleMethodName(final TypeReference type) {
    String _join = IterableExtensions.join(type.getSimpleNames(), "$");
    final Function1<TypeReference, CharSequence> _function = (TypeReference it) -> {
      return this.getSimpleMethodName(it);
    };
    String _join_1 = IterableExtensions.<TypeReference>join(type.getTypeArguments(), "$", "$", "", _function);
    return (_join + _join_1);
  }

  private StringConcatenationClient createBindingMethod(final GuiceModuleAccess.Binding it) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          if (((!it.getValue().isProvider()) && it.getValue().getStatements().isEmpty())) {
            _builder.append("// contributed by ");
            String _contributedBy = it.getContributedBy();
            _builder.append(_contributedBy);
            _builder.newLineIfNotEmpty();
            {
              boolean _isSingleton = it.getKey().isSingleton();
              if (_isSingleton) {
                _builder.append("@");
                _builder.append(SingletonBinding.class);
                {
                  boolean _isEagerSingleton = it.getKey().isEagerSingleton();
                  if (_isEagerSingleton) {
                    _builder.append("(eager=true)");
                  }
                }
              }
            }
            _builder.newLineIfNotEmpty();
            _builder.append("public ");
            {
              Object _expression = it.getValue().getExpression();
              boolean _tripleEquals = (_expression == null);
              if (_tripleEquals) {
                _builder.append("Class<? extends ");
                TypeReference _type = it.getKey().getType();
                _builder.append(_type);
                _builder.append(">");
              } else {
                TypeReference _type_1 = it.getKey().getType();
                _builder.append(_type_1);
              }
            }
            _builder.append(" ");
            CharSequence _bindMethodName = XtextGeneratorTemplates.this.getBindMethodName(it);
            _builder.append(_bindMethodName);
            _builder.append("() {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("return ");
            {
              Object _expression_1 = it.getValue().getExpression();
              boolean _tripleNotEquals = (_expression_1 != null);
              if (_tripleNotEquals) {
                Object _expression_2 = it.getValue().getExpression();
                _builder.append(_expression_2, "\t");
              } else {
                TypeReference _type_2 = it.getValue().getType();
                boolean _tripleNotEquals_1 = (_type_2 != null);
                if (_tripleNotEquals_1) {
                  TypeReference _type_3 = it.getValue().getType();
                  _builder.append(_type_3, "\t");
                  _builder.append(".class");
                } else {
                  _builder.append("null");
                }
              }
            }
            _builder.append(";");
            _builder.newLineIfNotEmpty();
            _builder.append("}");
            _builder.newLine();
          } else {
            boolean _isEmpty = it.getValue().getStatements().isEmpty();
            if (_isEmpty) {
              _builder.append("// contributed by ");
              String _contributedBy_1 = it.getContributedBy();
              _builder.append(_contributedBy_1);
              _builder.newLineIfNotEmpty();
              {
                boolean _isSingleton_1 = it.getKey().isSingleton();
                if (_isSingleton_1) {
                  _builder.append("@");
                  _builder.append(SingletonBinding.class);
                  {
                    boolean _isEagerSingleton_1 = it.getKey().isEagerSingleton();
                    if (_isEagerSingleton_1) {
                      _builder.append("(eager=true)");
                    }
                  }
                }
              }
              _builder.newLineIfNotEmpty();
              _builder.append("public ");
              {
                Object _expression_3 = it.getValue().getExpression();
                boolean _tripleEquals_1 = (_expression_3 == null);
                if (_tripleEquals_1) {
                  _builder.append("Class<? extends ");
                  _builder.append(Provider.class);
                  _builder.append("<? extends ");
                  TypeReference _type_4 = it.getKey().getType();
                  _builder.append(_type_4);
                  _builder.append(">>");
                } else {
                  _builder.append(Provider.class);
                  _builder.append("<? extends ");
                  TypeReference _type_5 = it.getKey().getType();
                  _builder.append(_type_5);
                  _builder.append(">");
                }
              }
              _builder.append(" ");
              CharSequence _bindMethodName_1 = XtextGeneratorTemplates.this.getBindMethodName(it);
              _builder.append(_bindMethodName_1);
              _builder.append("() {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("return ");
              {
                Object _expression_4 = it.getValue().getExpression();
                boolean _tripleNotEquals_2 = (_expression_4 != null);
                if (_tripleNotEquals_2) {
                  Object _expression_5 = it.getValue().getExpression();
                  _builder.append(_expression_5, "\t");
                } else {
                  TypeReference _type_6 = it.getValue().getType();
                  _builder.append(_type_6, "\t");
                  _builder.append(".class");
                }
              }
              _builder.append(";");
              _builder.newLineIfNotEmpty();
              _builder.append("}");
              _builder.newLine();
            } else {
              _builder.append("// contributed by ");
              String _contributedBy_2 = it.getContributedBy();
              _builder.append(_contributedBy_2);
              _builder.newLineIfNotEmpty();
              _builder.append("public void ");
              CharSequence _bindMethodName_2 = XtextGeneratorTemplates.this.getBindMethodName(it);
              _builder.append(_bindMethodName_2);
              _builder.append("(");
              _builder.append(Binder.class);
              _builder.append(" binder) {");
              _builder.newLineIfNotEmpty();
              {
                List<Object> _statements = it.getValue().getStatements();
                for(final Object statement : _statements) {
                  _builder.append("\t");
                  _builder.append(statement, "\t");
                  _builder.newLineIfNotEmpty();
                }
              }
              _builder.append("}");
              _builder.newLine();
            }
          }
        }
      }
    };
    return _client;
  }

  public JavaFileAccess createRuntimeModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _runtimeModule = this.naming.getRuntimeModule(it);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register components to be used at runtime / without the Equinox extension registry.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeGenModule = XtextGeneratorTemplates.this.naming.getRuntimeGenModule(it);
          _builder.append(_runtimeGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createXtendFile(_runtimeModule, _client);
    } else {
      TypeReference _runtimeModule_1 = this.naming.getRuntimeModule(it);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register components to be used at runtime / without the Equinox extension registry.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeGenModule = XtextGeneratorTemplates.this.naming.getRuntimeGenModule(it);
          _builder.append(_runtimeGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createJavaFile(_runtimeModule_1, _client_1);
    }
  }

  public JavaFileAccess createRuntimeGenModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    TypeReference _elvis = null;
    TypeReference _superClass = langConfig.getRuntimeGenModule().getSuperClass();
    if (_superClass != null) {
      _elvis = _superClass;
    } else {
      TypeReference _runtimeDefaultModule = this.naming.getRuntimeDefaultModule(it);
      _elvis = _runtimeDefaultModule;
    }
    final TypeReference superClass = _elvis;
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this.naming.getRuntimeGenModule(it));
    file.setImportNestedTypeThreshold(JavaFileAccess.DONT_IMPORT_NESTED_TYPES);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* Manual modifications go to {@link ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeModule(it).getSimpleName();
        _builder.append(_simpleName, " ");
        _builder.append("}.");
        _builder.newLineIfNotEmpty();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
      }
    };
    file.setTypeComment(_client);
    List<IClassAnnotation> _annotations = file.getAnnotations();
    SuppressWarningsAnnotation _suppressWarningsAnnotation = new SuppressWarningsAnnotation();
    _annotations.add(_suppressWarningsAnnotation);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public abstract class ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getRuntimeGenModule(it).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        _builder.append(superClass);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(Properties.class, "\t");
        _builder.append(" properties = null;");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void configure(");
        _builder.append(Binder.class, "\t");
        _builder.append(" binder) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("properties = tryBindProperties(binder, \"");
        String _replaceAll = langConfig.getGrammar().getName().replaceAll("\\.", "/");
        _builder.append(_replaceAll, "\t\t");
        _builder.append(".properties\");");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("super.configure(binder);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void configureLanguageName(");
        _builder.append(Binder.class, "\t");
        _builder.append(" binder) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("binder.bind(String.class).annotatedWith(");
        _builder.append(Names.class, "\t\t");
        _builder.append(".named(");
        _builder.append(Constants.class, "\t\t");
        _builder.append(".LANGUAGE_NAME)).toInstance(\"");
        String _name = langConfig.getGrammar().getName();
        _builder.append(_name, "\t\t");
        _builder.append("\");");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void configureFileExtensions(");
        _builder.append(Binder.class, "\t");
        _builder.append(" binder) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("if (properties == null || properties.getProperty(Constants.FILE_EXTENSIONS) == null)");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("binder.bind(String.class).annotatedWith(");
        _builder.append(Names.class, "\t\t\t");
        _builder.append(".named(");
        _builder.append(Constants.class, "\t\t\t");
        _builder.append(".FILE_EXTENSIONS)).toInstance(\"");
        String _join = IterableExtensions.join(langConfig.getFileExtensions(), ",");
        _builder.append(_join, "\t\t\t");
        _builder.append("\");");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        {
          Set<GuiceModuleAccess.Binding> _bindings = langConfig.getRuntimeGenModule().getBindings();
          for(final GuiceModuleAccess.Binding binding : _bindings) {
            _builder.append("\t");
            StringConcatenationClient _createBindingMethod = XtextGeneratorTemplates.this.createBindingMethod(binding);
            _builder.append(_createBindingMethod, "\t");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.newLine();
          }
        }
        _builder.append("}");
        _builder.newLine();
      }
    };
    file.setContent(_client_1);
    file.setMarkedAsGenerated(true);
    return file;
  }

  public JavaFileAccess createIdeModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _genericIdeModule = this.naming.getGenericIdeModule(it);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register ide components.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _genericIdeGenModule = XtextGeneratorTemplates.this.naming.getGenericIdeGenModule(it);
          _builder.append(_genericIdeGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createXtendFile(_genericIdeModule, _client);
    } else {
      TypeReference _genericIdeModule_1 = this.naming.getGenericIdeModule(it);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register ide components.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _genericIdeGenModule = XtextGeneratorTemplates.this.naming.getGenericIdeGenModule(it);
          _builder.append(_genericIdeGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createJavaFile(_genericIdeModule_1, _client_1);
    }
  }

  public JavaFileAccess createIdeGenModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    TypeReference _elvis = null;
    TypeReference _superClass = langConfig.getIdeGenModule().getSuperClass();
    if (_superClass != null) {
      _elvis = _superClass;
    } else {
      TypeReference _genericIdeDefaultModule = this.naming.getGenericIdeDefaultModule(it);
      _elvis = _genericIdeDefaultModule;
    }
    final TypeReference superClass = _elvis;
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this.naming.getGenericIdeGenModule(it));
    file.setImportNestedTypeThreshold(JavaFileAccess.DONT_IMPORT_NESTED_TYPES);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* Manual modifications go to {@link ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeModule(it).getSimpleName();
        _builder.append(_simpleName, " ");
        _builder.append("}.");
        _builder.newLineIfNotEmpty();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
      }
    };
    file.setTypeComment(_client);
    List<IClassAnnotation> _annotations = file.getAnnotations();
    SuppressWarningsAnnotation _suppressWarningsAnnotation = new SuppressWarningsAnnotation();
    _annotations.add(_suppressWarningsAnnotation);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public abstract class ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeGenModule(it).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        _builder.append(superClass);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        {
          Set<GuiceModuleAccess.Binding> _bindings = langConfig.getIdeGenModule().getBindings();
          for(final GuiceModuleAccess.Binding binding : _bindings) {
            _builder.append("\t");
            StringConcatenationClient _createBindingMethod = XtextGeneratorTemplates.this.createBindingMethod(binding);
            _builder.append(_createBindingMethod, "\t");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.newLine();
          }
        }
        _builder.append("}");
        _builder.newLine();
      }
    };
    file.setContent(_client_1);
    file.setMarkedAsGenerated(true);
    return file;
  }

  public JavaFileAccess createIdeSetup(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _genericIdeSetup = this.naming.getGenericIdeSetup(it);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Initialization support for running Xtext languages as language servers.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeSetup(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeSetup = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it);
          _builder.append(_runtimeSetup);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("override createInjector() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append(Guice.class, "\t\t");
          _builder.append(".createInjector(");
          _builder.append(Modules2.class, "\t\t");
          _builder.append(".mixin(new ");
          TypeReference _runtimeModule = XtextGeneratorTemplates.this.naming.getRuntimeModule(it);
          _builder.append(_runtimeModule, "\t\t");
          _builder.append(", new ");
          TypeReference _genericIdeModule = XtextGeneratorTemplates.this.naming.getGenericIdeModule(it);
          _builder.append(_genericIdeModule, "\t\t");
          _builder.append("))");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createXtendFile(_genericIdeSetup, _client);
    } else {
      TypeReference _genericIdeSetup_1 = this.naming.getGenericIdeSetup(it);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Initialization support for running Xtext languages as language servers.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getGenericIdeSetup(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _runtimeSetup = XtextGeneratorTemplates.this.naming.getRuntimeSetup(it);
          _builder.append(_runtimeSetup);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          _builder.append(Injector.class, "\t");
          _builder.append(" createInjector() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return ");
          _builder.append(Guice.class, "\t\t");
          _builder.append(".createInjector(");
          _builder.append(Modules2.class, "\t\t");
          _builder.append(".mixin(new ");
          TypeReference _runtimeModule = XtextGeneratorTemplates.this.naming.getRuntimeModule(it);
          _builder.append(_runtimeModule, "\t\t");
          _builder.append("(), new ");
          TypeReference _genericIdeModule = XtextGeneratorTemplates.this.naming.getGenericIdeModule(it);
          _builder.append(_genericIdeModule, "\t\t");
          _builder.append("()));");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createJavaFile(_genericIdeSetup_1, _client_1);
    }
  }

  public JavaFileAccess createEclipsePluginModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _eclipsePluginModule = this.naming.getEclipsePluginModule(it);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register components to be used within the Eclipse IDE.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("@");
          _builder.append(FinalFieldsConstructor.class);
          _builder.newLineIfNotEmpty();
          _builder.append("class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getEclipsePluginModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _eclipsePluginGenModule = XtextGeneratorTemplates.this.naming.getEclipsePluginGenModule(it);
          _builder.append(_eclipsePluginGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createXtendFile(_eclipsePluginModule, _client);
    } else {
      TypeReference _eclipsePluginModule_1 = this.naming.getEclipsePluginModule(it);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Use this class to register components to be used within the Eclipse IDE.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = XtextGeneratorTemplates.this.naming.getEclipsePluginModule(it).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _eclipsePluginGenModule = XtextGeneratorTemplates.this.naming.getEclipsePluginGenModule(it);
          _builder.append(_eclipsePluginGenModule);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          String _simpleName_1 = XtextGeneratorTemplates.this.naming.getEclipsePluginModule(it).getSimpleName();
          _builder.append(_simpleName_1, "\t");
          _builder.append("(");
          TypeReference _typeRef = TypeReference.typeRef("org.eclipse.ui.plugin.AbstractUIPlugin");
          _builder.append(_typeRef, "\t");
          _builder.append(" plugin) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("super(plugin);");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      return this.fileAccessFactory.createJavaFile(_eclipsePluginModule_1, _client_1);
    }
  }

  public JavaFileAccess createEclipsePluginGenModule(final IXtextGeneratorLanguage langConfig) {
    final Grammar it = langConfig.getGrammar();
    TypeReference _elvis = null;
    TypeReference _superClass = langConfig.getEclipsePluginGenModule().getSuperClass();
    if (_superClass != null) {
      _elvis = _superClass;
    } else {
      TypeReference _eclipsePluginDefaultModule = this.naming.getEclipsePluginDefaultModule(it);
      _elvis = _eclipsePluginDefaultModule;
    }
    final TypeReference superClass = _elvis;
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this.naming.getEclipsePluginGenModule(it));
    file.setImportNestedTypeThreshold(JavaFileAccess.DONT_IMPORT_NESTED_TYPES);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* Manual modifications go to {@link ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getEclipsePluginModule(it).getSimpleName();
        _builder.append(_simpleName, " ");
        _builder.append("}.");
        _builder.newLineIfNotEmpty();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
      }
    };
    file.setTypeComment(_client);
    List<IClassAnnotation> _annotations = file.getAnnotations();
    SuppressWarningsAnnotation _suppressWarningsAnnotation = new SuppressWarningsAnnotation();
    _annotations.add(_suppressWarningsAnnotation);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public abstract class ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getEclipsePluginGenModule(it).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        _builder.append(superClass);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public ");
        String _simpleName_1 = XtextGeneratorTemplates.this.naming.getEclipsePluginGenModule(it).getSimpleName();
        _builder.append(_simpleName_1, "\t");
        _builder.append("(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.ui.plugin.AbstractUIPlugin");
        _builder.append(_typeRef, "\t");
        _builder.append(" plugin) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("super(plugin);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        {
          Set<GuiceModuleAccess.Binding> _bindings = langConfig.getEclipsePluginGenModule().getBindings();
          for(final GuiceModuleAccess.Binding binding : _bindings) {
            _builder.append("\t");
            StringConcatenationClient _createBindingMethod = XtextGeneratorTemplates.this.createBindingMethod(binding);
            _builder.append(_createBindingMethod, "\t");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.newLine();
          }
        }
        _builder.append("}");
        _builder.newLine();
      }
    };
    file.setContent(_client_1);
    file.setMarkedAsGenerated(true);
    return file;
  }

  public JavaFileAccess createEclipsePluginExecutableExtensionFactory(final IXtextGeneratorLanguage langConfig, final IXtextGeneratorLanguage activatorLanguage) {
    final Grammar grammar = langConfig.getGrammar();
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this.naming.getEclipsePluginExecutableExtensionFactory(grammar));
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* This class was generated. Customizations should only happen in a newly");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* introduced subclass. ");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
      }
    };
    file.setTypeComment(_client);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public class ");
        String _simpleName = XtextGeneratorTemplates.this.naming.getEclipsePluginExecutableExtensionFactory(grammar).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory");
        _builder.append(_typeRef);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        TypeReference _typeRef_1 = TypeReference.typeRef("org.osgi.framework.Bundle");
        _builder.append(_typeRef_1, "\t");
        _builder.append(" getBundle() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return ");
        TypeReference _typeRef_2 = TypeReference.typeRef("org.osgi.framework.FrameworkUtil");
        _builder.append(_typeRef_2, "\t\t");
        _builder.append(".getBundle(");
        TypeReference _eclipsePluginActivator = XtextGeneratorTemplates.this.naming.getEclipsePluginActivator();
        _builder.append(_eclipsePluginActivator, "\t\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(Injector.class, "\t");
        _builder.append(" getInjector() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        TypeReference _eclipsePluginActivator_1 = XtextGeneratorTemplates.this.naming.getEclipsePluginActivator();
        _builder.append(_eclipsePluginActivator_1, "\t\t");
        _builder.append(" activator = ");
        TypeReference _eclipsePluginActivator_2 = XtextGeneratorTemplates.this.naming.getEclipsePluginActivator();
        _builder.append(_eclipsePluginActivator_2, "\t\t");
        _builder.append(".getInstance();");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return activator != null ? activator.getInjector(");
        TypeReference _eclipsePluginActivator_3 = XtextGeneratorTemplates.this.naming.getEclipsePluginActivator();
        _builder.append(_eclipsePluginActivator_3, "\t\t");
        _builder.append(".");
        String _replaceAll = langConfig.getGrammar().getName().toUpperCase().replaceAll("\\.", "_");
        _builder.append(_replaceAll, "\t\t");
        _builder.append(") : null;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    file.setContent(_client_1);
    return file;
  }

  public JavaFileAccess createEclipsePluginActivator(final IXtextProjectConfig projectConfig, final List<? extends IXtextGeneratorLanguage> langConfigs) {
    final TypeReference activator = this.naming.getEclipsePluginActivator();
    final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(activator);
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* This class was generated. Customizations should only happen in a newly");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* introduced subclass. ");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
      }
    };
    file.setTypeComment(_client);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public class ");
        String _simpleName = activator.getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.ui.plugin.AbstractUIPlugin");
        _builder.append(_typeRef);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public static final String PLUGIN_ID = \"");
        String _name = projectConfig.getEclipsePlugin().getName();
        _builder.append(_name, "\t");
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
        {
          for(final IXtextGeneratorLanguage lang : langConfigs) {
            _builder.append("\t");
            _builder.append("public static final String ");
            String _replaceAll = lang.getGrammar().getName().toUpperCase().replaceAll("\\.", "_");
            _builder.append(_replaceAll, "\t");
            _builder.append(" = \"");
            String _name_1 = lang.getGrammar().getName();
            _builder.append(_name_1, "\t");
            _builder.append("\";");
            _builder.newLineIfNotEmpty();
          }
        }
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("private static final ");
        _builder.append(Logger.class, "\t");
        _builder.append(" logger = ");
        _builder.append(Logger.class, "\t");
        _builder.append(".getLogger(");
        String _simpleName_1 = activator.getSimpleName();
        _builder.append(_simpleName_1, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("private static ");
        String _simpleName_2 = activator.getSimpleName();
        _builder.append(_simpleName_2, "\t");
        _builder.append(" INSTANCE;");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("private ");
        _builder.append(Map.class, "\t");
        _builder.append("<String, ");
        _builder.append(Injector.class, "\t");
        _builder.append("> injectors = ");
        _builder.append(Collections.class, "\t");
        _builder.append(".synchronizedMap(new ");
        _builder.append(HashMap.class, "\t");
        _builder.append("<>(2));");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void start(");
        TypeReference _typeRef_1 = TypeReference.typeRef("org.osgi.framework.BundleContext");
        _builder.append(_typeRef_1, "\t");
        _builder.append(" context) throws Exception {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("super.start(context);");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("INSTANCE = this;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void stop(");
        TypeReference _typeRef_2 = TypeReference.typeRef("org.osgi.framework.BundleContext");
        _builder.append(_typeRef_2, "\t");
        _builder.append(" context) throws Exception {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("injectors.clear();");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("INSTANCE = null;");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("super.stop(context);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public static ");
        String _simpleName_3 = activator.getSimpleName();
        _builder.append(_simpleName_3, "\t");
        _builder.append(" getInstance() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return INSTANCE;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public ");
        _builder.append(Injector.class, "\t");
        _builder.append(" getInjector(String language) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("synchronized (injectors) {");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append(Injector.class, "\t\t\t");
        _builder.append(" injector = injectors.get(language);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("if (injector == null) {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("injectors.put(language, injector = createInjector(language));");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("return injector;");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(Injector.class, "\t");
        _builder.append(" createInjector(String language) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("try {");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append(com.google.inject.Module.class, "\t\t\t");
        _builder.append(" runtimeModule = getRuntimeModule(language);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append(com.google.inject.Module.class, "\t\t\t");
        _builder.append(" sharedStateModule = getSharedStateModule();");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append(com.google.inject.Module.class, "\t\t\t");
        _builder.append(" uiModule = getUiModule(language);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append(com.google.inject.Module.class, "\t\t\t");
        _builder.append(" mergedModule = ");
        _builder.append(Modules2.class, "\t\t\t");
        _builder.append(".mixin(runtimeModule, sharedStateModule, uiModule);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("return ");
        _builder.append(Guice.class, "\t\t\t");
        _builder.append(".createInjector(mergedModule);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("} catch (Exception e) {");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("logger.error(\"Failed to create injector for \" + language);");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("logger.error(e.getMessage(), e);");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("throw new RuntimeException(\"Failed to create injector for \" + language, e);");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(com.google.inject.Module.class, "\t");
        _builder.append(" getRuntimeModule(String grammar) {");
        _builder.newLineIfNotEmpty();
        {
          for(final IXtextGeneratorLanguage lang_1 : langConfigs) {
            _builder.append("\t\t");
            _builder.append("if (");
            String _replaceAll_1 = lang_1.getGrammar().getName().toUpperCase().replaceAll("\\.", "_");
            _builder.append(_replaceAll_1, "\t\t");
            _builder.append(".equals(grammar)) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("return new ");
            TypeReference _runtimeModule = XtextGeneratorTemplates.this.naming.getRuntimeModule(lang_1.getGrammar());
            _builder.append(_runtimeModule, "\t\t\t");
            _builder.append("();");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("}");
            _builder.newLine();
          }
        }
        _builder.append("\t\t");
        _builder.append("throw new IllegalArgumentException(grammar);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(com.google.inject.Module.class, "\t");
        _builder.append(" getUiModule(String grammar) {");
        _builder.newLineIfNotEmpty();
        {
          for(final IXtextGeneratorLanguage lang_2 : langConfigs) {
            _builder.append("\t\t");
            _builder.append("if (");
            String _replaceAll_2 = lang_2.getGrammar().getName().toUpperCase().replaceAll("\\.", "_");
            _builder.append(_replaceAll_2, "\t\t");
            _builder.append(".equals(grammar)) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("return new ");
            TypeReference _eclipsePluginModule = XtextGeneratorTemplates.this.naming.getEclipsePluginModule(lang_2.getGrammar());
            _builder.append(_eclipsePluginModule, "\t\t\t");
            _builder.append("(this);");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("}");
            _builder.newLine();
          }
        }
        _builder.append("\t\t");
        _builder.append("throw new IllegalArgumentException(grammar);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        _builder.append(com.google.inject.Module.class, "\t");
        _builder.append(" getSharedStateModule() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return new ");
        TypeReference _typeRef_3 = TypeReference.typeRef("org.eclipse.xtext.ui.shared.SharedStateModule");
        _builder.append(_typeRef_3, "\t\t");
        _builder.append("();");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    file.setContent(_client_1);
    return file;
  }

  public TextFileAccess createMetaInfServices(final List<? extends IXtextGeneratorLanguage> languageConfigs) {
    TextFileAccess _xblockexpression = null;
    {
      final TextFileAccess file = new TextFileAccess();
      file.setPath("META-INF/services/org.eclipse.xtext.ISetup");
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          {
            for(final IXtextGeneratorLanguage lang : languageConfigs) {
              TypeReference _genericIdeSetup = XtextGeneratorTemplates.this.naming.getGenericIdeSetup(lang.getGrammar());
              _builder.append(_genericIdeSetup);
              _builder.newLineIfNotEmpty();
            }
          }
        }
      };
      file.setContent(_client);
      _xblockexpression = file;
    }
    return _xblockexpression;
  }
}
