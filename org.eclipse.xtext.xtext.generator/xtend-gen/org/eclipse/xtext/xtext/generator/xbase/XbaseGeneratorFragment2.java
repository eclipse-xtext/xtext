/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.xbase;

import com.google.inject.Inject;
import com.google.inject.name.Names;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.GeneratedJavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.PluginXmlAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.model.project.IBundleProjectConfig;
import org.eclipse.xtext.xtext.generator.util.GenModelUtil2;

@SuppressWarnings("all")
public class XbaseGeneratorFragment2 extends AbstractXtextGeneratorFragment {
  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean generateXtendInferrer = true;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean useInferredJvmModel = true;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean jdtTypeHierarchy = true;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean jdtCallHierarchy = true;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean skipExportedPackage = false;

  @Inject
  private FileAccessFactory fileAccessFactory;

  @Inject
  @Extension
  private XtextGeneratorNaming _xtextGeneratorNaming;

  @Inject
  @Extension
  private XbaseUsageDetector _xbaseUsageDetector;

  protected TypeReference getJvmModelInferrer() {
    String _runtimeBasePackage = this._xtextGeneratorNaming.getRuntimeBasePackage(this.getGrammar());
    String _plus = (_runtimeBasePackage + ".jvmmodel.");
    String _simpleName = GrammarUtil.getSimpleName(this.getGrammar());
    String _plus_1 = (_plus + _simpleName);
    String _plus_2 = (_plus_1 + "JvmModelInferrer");
    return new TypeReference(_plus_2);
  }

  @Override
  public void generate() {
    boolean _inheritsXbase = this._xbaseUsageDetector.inheritsXbase(this.getGrammar());
    boolean _not = (!_inheritsXbase);
    if (_not) {
      return;
    }
    boolean _equals = this._xtextGeneratorNaming.getEclipsePluginEditor(this.getGrammar()).equals(this._xtextGeneratorNaming.getEclipsePluginXbaseEditor(this.getGrammar()));
    boolean _not_1 = (!_equals);
    if (_not_1) {
      this.contributeEditorStub();
    }
    this.contributeRuntimeGuiceBindings();
    this.contributeEclipsePluginGuiceBindings();
    PluginXmlAccess _pluginXml = this.getProjectConfig().getEclipsePlugin().getPluginXml();
    boolean _tripleNotEquals = (_pluginXml != null);
    if (_tripleNotEquals) {
      this.contributeEclipsePluginExtensions();
    }
    if (this.generateXtendInferrer) {
      this.doGenerateXtendInferrer();
    }
    ManifestAccess _manifest = this.getProjectConfig().getRuntime().getManifest();
    boolean _tripleNotEquals_1 = (_manifest != null);
    if (_tripleNotEquals_1) {
      String _xbaseLibVersionLowerBound = this.getProjectConfig().getRuntime().getXbaseLibVersionLowerBound();
      String _plus = ("org.eclipse.xtext.xbase.lib;bundle-version=\"" + _xbaseLibVersionLowerBound);
      String _plus_1 = (_plus + "\"");
      this.getProjectConfig().getRuntime().getManifest().getRequiredBundles().addAll(
        Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("org.eclipse.xtext.xbase", _plus_1)));
      if (((this.generateXtendInferrer || this.useInferredJvmModel) && (!this.skipExportedPackage))) {
        Set<String> _exportedPackages = this.getProjectConfig().getRuntime().getManifest().getExportedPackages();
        String _packageName = this.getJvmModelInferrer().getPackageName();
        _exportedPackages.add(_packageName);
      }
    }
    ManifestAccess _manifest_1 = this.getProjectConfig().getEclipsePlugin().getManifest();
    boolean _tripleNotEquals_2 = (_manifest_1 != null);
    if (_tripleNotEquals_2) {
      this.getProjectConfig().getEclipsePlugin().getManifest().getRequiredBundles().addAll(
        Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("org.eclipse.xtext.xbase.ui", "org.eclipse.jdt.debug.ui")));
    }
    GuiceModuleAccess _ideGenModule = this.getLanguage().getIdeGenModule();
    _ideGenModule.setSuperClass(TypeReference.typeRef("org.eclipse.xtext.xbase.ide.DefaultXbaseIdeModule"));
  }

  protected boolean contributeEditorStub() {
    boolean _xblockexpression = false;
    {
      IBundleProjectConfig _eclipsePlugin = this.getProjectConfig().getEclipsePlugin();
      IXtextGeneratorFileSystemAccess _srcGen = null;
      if (_eclipsePlugin!=null) {
        _srcGen=_eclipsePlugin.getSrcGen();
      }
      boolean _tripleNotEquals = (_srcGen != null);
      if (_tripleNotEquals) {
        final GeneratedJavaFileAccess file = this.fileAccessFactory.createGeneratedJavaFile(this._xtextGeneratorNaming.getEclipsePluginEditor(this.getGrammar()));
        StringConcatenationClient _client = new StringConcatenationClient() {
          @Override
          protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
            _builder.append("/**");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* This class was generated. Customizations should only happen in a newly");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("* introduced subclass.");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("*/");
            _builder.newLine();
            _builder.append("public class ");
            String _simpleName = XbaseGeneratorFragment2.this._xtextGeneratorNaming.getEclipsePluginEditor(XbaseGeneratorFragment2.this.getGrammar()).getSimpleName();
            _builder.append(_simpleName);
            _builder.append(" extends ");
            TypeReference _eclipsePluginXbaseEditor = XbaseGeneratorFragment2.this._xtextGeneratorNaming.getEclipsePluginXbaseEditor(XbaseGeneratorFragment2.this.getGrammar());
            _builder.append(_eclipsePluginXbaseEditor);
            _builder.append(" {");
            _builder.newLineIfNotEmpty();
            _builder.append("}");
            _builder.newLine();
          }
        };
        file.setContent(_client);
        file.writeTo(this.getProjectConfig().getEclipsePlugin().getSrcGen());
      }
      boolean _xifexpression = false;
      ManifestAccess _manifest = this.getProjectConfig().getEclipsePlugin().getManifest();
      boolean _tripleNotEquals_1 = (_manifest != null);
      if (_tripleNotEquals_1) {
        Set<String> _exportedPackages = this.getProjectConfig().getEclipsePlugin().getManifest().getExportedPackages();
        String _packageName = this._xtextGeneratorNaming.getEclipsePluginEditor(this.getGrammar()).getPackageName();
        _xifexpression = _exportedPackages.add(_packageName);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }

  protected void contributeRuntimeGuiceBindings() {
    final GuiceModuleAccess.BindingFactory bindingFactory = new GuiceModuleAccess.BindingFactory().addTypeToType(TypeReference.typeRef(IQualifiedNameProvider.class), 
      TypeReference.typeRef("org.eclipse.xtext.xbase.scoping.XbaseQualifiedNameProvider"));
    boolean _usesXImportSection = this._xbaseUsageDetector.usesXImportSection(this.getGrammar());
    boolean _not = (!_usesXImportSection);
    if (_not) {
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("binder.bind(");
          _builder.append(Boolean.class);
          _builder.append(".TYPE)");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(".annotatedWith(");
          _builder.append(Names.class, "\t");
          _builder.append(".named(");
          TypeReference _typeReference = new TypeReference("org.eclipse.xtext.xbase.imports", "RewritableImportSection.Factory");
          _builder.append(_typeReference, "\t");
          _builder.append(".REWRITABLEIMPORTSECTION_ENABLEMENT))");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append(".toInstance(");
          _builder.append(Boolean.class, "\t");
          _builder.append(".FALSE);");
          _builder.newLineIfNotEmpty();
        }
      };
      bindingFactory.addConfiguredBinding("RewritableImportSectionEnablement", _client);
    }
    if (this.useInferredJvmModel) {
      bindingFactory.addTypeToType(TypeReference.typeRef(ILocationInFileProvider.class), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.jvmmodel.JvmLocationInFileProvider")).addTypeToType(TypeReference.typeRef(IGlobalScopeProvider.class), 
        TypeReference.typeRef("org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.validation.FeatureNameValidator"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.validation.LogicalContainerAwareFeatureNameValidator")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.typesystem.internal.DefaultBatchTypeResolver"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareBatchTypeResolver")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.typesystem.internal.DefaultReentrantTypeResolver"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareReentrantTypeResolver")).addTypeToType(TypeReference.typeRef(IResourceValidator.class), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.annotations.validation.DerivedStateAwareResourceValidator"));
      if (this.generateXtendInferrer) {
        bindingFactory.addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.jvmmodel.IJvmModelInferrer"), this.getJvmModelInferrer());
      }
    } else {
      bindingFactory.addTypeToType(TypeReference.typeRef(ILocationInFileProvider.class), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.resource.XbaseLocationInFileProvider"));
    }
    bindingFactory.contributeTo(this.getLanguage().getRuntimeGenModule());
    boolean _inheritsXbaseWithAnnotations = this._xbaseUsageDetector.inheritsXbaseWithAnnotations(this.getLanguage().getGrammar());
    if (_inheritsXbaseWithAnnotations) {
      GuiceModuleAccess _runtimeGenModule = this.getLanguage().getRuntimeGenModule();
      _runtimeGenModule.setSuperClass(TypeReference.typeRef("org.eclipse.xtext.xbase.annotations.DefaultXbaseWithAnnotationsRuntimeModule"));
    } else {
      GuiceModuleAccess _runtimeGenModule_1 = this.getLanguage().getRuntimeGenModule();
      _runtimeGenModule_1.setSuperClass(TypeReference.typeRef("org.eclipse.xtext.xbase.DefaultXbaseRuntimeModule"));
    }
  }

  protected void contributeEclipsePluginGuiceBindings() {
    final GuiceModuleAccess.BindingFactory bindingFactory = new GuiceModuleAccess.BindingFactory();
    if (this.useInferredJvmModel) {
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("if (");
          TypeReference _typeRef = TypeReference.typeRef("org.eclipse.ui.PlatformUI");
          _builder.append(_typeRef);
          _builder.append(".isWorkbenchRunning()) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("binder.bind(");
          TypeReference _typeRef_1 = TypeReference.typeRef("org.eclipse.xtext.ui.editor.IURIEditorOpener");
          _builder.append(_typeRef_1, "\t");
          _builder.append(".class).annotatedWith(");
          TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.xtext.ui.LanguageSpecific");
          _builder.append(_typeRef_2, "\t");
          _builder.append(".class).to(");
          TypeReference _typeRef_3 = TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.navigation.DerivedMemberAwareEditorOpener");
          _builder.append(_typeRef_3, "\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("binder.bind(");
          TypeReference _typeRef_4 = TypeReference.typeRef("org.eclipse.xtext.common.types.ui.navigation.IDerivedMemberAwareEditorOpener");
          _builder.append(_typeRef_4, "\t");
          _builder.append(".class).to(");
          TypeReference _typeRef_5 = TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.navigation.DerivedMemberAwareEditorOpener");
          _builder.append(_typeRef_5, "\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("}");
          _builder.newLine();
        }
      };
      final StringConcatenationClient statement = _client;
      GuiceModuleAccess.BindingFactory _addTypeToType = bindingFactory.addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.editor.findrefs.FindReferencesHandler"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.findrefs.JvmModelFindReferenceHandler")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.editor.findrefs.ReferenceQueryExecutor"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.findrefs.JvmModelReferenceQueryExecutor")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IDependentElementsCalculator"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.JvmModelDependentElementsCalculator")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameRefactoringProvider"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.jdt.CombinedJvmJdtRenameRefactoringProvider")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IReferenceUpdater"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.refactoring.XbaseReferenceUpdater")).addfinalTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.ui.IRenameContextFactory"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.jdt.CombinedJvmJdtRenameContextFactory")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameStrategy"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.refactoring.DefaultJvmModelRenameStrategy"));
      TypeReference _typeReference = new TypeReference("org.eclipse.xtext.common.types.ui.refactoring.participant", "JdtRenameParticipant.ContextFactory");
      TypeReference _typeReference_1 = new TypeReference("org.eclipse.xtext.xbase.ui.jvmmodel.refactoring", "JvmModelJdtRenameParticipantContext.ContextFactory");
      GuiceModuleAccess.BindingFactory _addTypeToType_1 = _addTypeToType.addTypeToType(_typeReference, _typeReference_1).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.editor.outline.impl.OutlineNodeElementOpener"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.jvmmodel.outline.JvmOutlineNodeElementOpener")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.editor.GlobalURIEditorOpener"), 
        TypeReference.typeRef("org.eclipse.xtext.common.types.ui.navigation.GlobalDerivedMemberAwareURIEditorOpener"));
      TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.common.types.ui.query.IJavaSearchParticipation");
      TypeReference _typeReference_2 = new TypeReference("org.eclipse.xtext.common.types.ui.query", "IJavaSearchParticipation.No");
      _addTypeToType_1.addTypeToType(_typeRef, _typeReference_2).addConfiguredBinding("LanguageSpecificURIEditorOpener", statement);
    } else {
      bindingFactory.addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameStrategy"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.refactoring.XbaseRenameStrategy"));
    }
    boolean _usesXImportSection = this._xbaseUsageDetector.usesXImportSection(this.getLanguage().getGrammar());
    if (_usesXImportSection) {
      bindingFactory.addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.imports.IUnresolvedTypeResolver"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.imports.InteractiveUnresolvedTypeResolver")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.contentassist.ImportingTypesProposalProvider")).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ui.editor.templates.XtextTemplateContextType"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.templates.XbaseTemplateContextType"));
    } else {
      bindingFactory.addTypeToType(TypeReference.typeRef("org.eclipse.xtext.xbase.ui.quickfix.JavaTypeQuickfixes"), 
        TypeReference.typeRef("org.eclipse.xtext.xbase.ui.quickfix.JavaTypeQuickfixesNoImportSection"));
    }
    bindingFactory.addTypeToType(this._xtextGeneratorNaming.getEclipsePluginDefaultEditor(this.getGrammar()), this._xtextGeneratorNaming.getEclipsePluginEditor(this.getGrammar()));
    bindingFactory.contributeTo(this.getLanguage().getEclipsePluginGenModule());
    boolean _inheritsXbaseWithAnnotations = this._xbaseUsageDetector.inheritsXbaseWithAnnotations(this.getLanguage().getGrammar());
    if (_inheritsXbaseWithAnnotations) {
      GuiceModuleAccess _eclipsePluginGenModule = this.getLanguage().getEclipsePluginGenModule();
      _eclipsePluginGenModule.setSuperClass(TypeReference.typeRef("org.eclipse.xtext.xbase.annotations.ui.DefaultXbaseWithAnnotationsUiModule"));
    } else {
      GuiceModuleAccess _eclipsePluginGenModule_1 = this.getLanguage().getEclipsePluginGenModule();
      _eclipsePluginGenModule_1.setSuperClass(TypeReference.typeRef("org.eclipse.xtext.xbase.ui.DefaultXbaseUiModule"));
    }
  }

  protected void doGenerateXtendInferrer() {
    final TypeReference firstRuleType = TypeReference.typeRef(GenModelUtil2.getJavaTypeName(IterableExtensions.<AbstractRule>head(this.getLanguage().getGrammar().getRules()).getType().getClassifier(), this.getLanguage().getGrammar().eResource().getResourceSet()));
    TypeReference _jvmModelInferrer = this.getJvmModelInferrer();
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("/**");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* <p>Infers a JVM model from the source model.</p>");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("*");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* <p>The JVM model should contain all elements that would appear in the Java code");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("* which is generated from the source model. Other models link against the JVM model rather than the source model.</p>");
        _builder.newLine();
        _builder.append(" ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("class ");
        String _simpleName = XbaseGeneratorFragment2.this.getJvmModelInferrer().getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer");
        _builder.append(_typeRef);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* convenience API to build and initialize JVM types and their members.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(Inject.class, "\t");
        _builder.append(" extension ");
        TypeReference _typeRef_1 = TypeReference.typeRef("org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder");
        _builder.append(_typeRef_1, "\t");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("/**");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* The dispatch method {@code infer} is called for each instance of the");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* given element\'s type that is contained in a resource.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* @param element");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            the model to create one or more");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            {@link org.eclipse.xtext.common.types.JvmDeclaredType declared");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            types} from.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* @param acceptor");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            each created");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            {@link org.eclipse.xtext.common.types.JvmDeclaredType type}");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            without a container should be passed to the acceptor in order");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            get attached to the current resource. The acceptor\'s");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            accept(..)} method takes the constructed empty type for the");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            pre-indexing phase. This one is further initialized in the");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            indexing phase using the lambda you pass as the last argument.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("* @param isPreIndexingPhase");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            whether the method is called in a pre-indexing phase, i.e.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            when the global index is not yet fully updated. You must not");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            rely on linking using the index if isPreIndexingPhase is");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*            <code>true</code>.");
        _builder.newLine();
        _builder.append("\t ");
        _builder.append("*/");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("def dispatch void infer(");
        _builder.append(firstRuleType, "\t");
        _builder.append(" element, ");
        TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor");
        _builder.append(_typeRef_2, "\t");
        _builder.append(" acceptor, boolean isPreIndexingPhase) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("// Here you explain how your model is mapped to Java elements, by writing the actual translation code.");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("// An implementation for the initial hello world example could look like this:");
        _builder.newLine();
        _builder.append("//\t\tacceptor.accept(element.toClass(\"my.company.greeting.MyGreetings\")) [");
        _builder.newLine();
        _builder.append("//\t\t\tfor (greeting : element.greetings) {");
        _builder.newLine();
        _builder.append("//\t\t\t\tmembers += greeting.toMethod(\"hello\" + greeting.name, typeRef(String)) [");
        _builder.newLine();
        _builder.append("//\t\t\t\t\tbody = ");
        _builder.append("\'\'\'");
        _builder.newLineIfNotEmpty();
        _builder.append("//\t\t\t\t\t\treturn \"Hello ");
        _builder.append("«");
        _builder.append("greeting.name");
        _builder.append("»");
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
        _builder.append("//\t\t\t\t\t");
        _builder.append("\'\'\'");
        _builder.newLineIfNotEmpty();
        _builder.append("//\t\t\t\t]");
        _builder.newLine();
        _builder.append("//\t\t\t}");
        _builder.newLine();
        _builder.append("//\t\t]");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
      }
    };
    this.fileAccessFactory.createXtendFile(_jvmModelInferrer, _client).writeTo(this.getProjectConfig().getRuntime().getSrc());
  }

  protected boolean contributeEclipsePluginExtensions() {
    boolean _xblockexpression = false;
    {
      final String name = this.getLanguage().getGrammar().getName();
      if (this.jdtTypeHierarchy) {
        List<CharSequence> _entries = this.getProjectConfig().getEclipsePlugin().getPluginXml().getEntries();
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("<!-- Type Hierarchy  -->");
        _builder.newLine();
        _builder.append("<extension point=\"org.eclipse.ui.handlers\">");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<handler");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("class=\"");
        TypeReference _eclipsePluginExecutableExtensionFactory = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
        _builder.append(_eclipsePluginExecutableExtensionFactory, "\t\t");
        _builder.append(":org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchyHandler\"");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("commandId=\"org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchy\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<activeWhen>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<reference");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("definitionId=\"");
        _builder.append(name, "\t\t\t\t");
        _builder.append(".Editor.opened\">");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("</reference>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("</activeWhen>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</handler>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<handler");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("class=\"");
        TypeReference _eclipsePluginExecutableExtensionFactory_1 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
        _builder.append(_eclipsePluginExecutableExtensionFactory_1, "\t\t");
        _builder.append(":org.eclipse.xtext.xbase.ui.hierarchy.QuickTypeHierarchyHandler\"");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("commandId=\"org.eclipse.jdt.ui.edit.text.java.open.hierarchy\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<activeWhen>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<reference");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("definitionId=\"");
        _builder.append(name, "\t\t\t\t");
        _builder.append(".Editor.opened\">");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("</reference>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("</activeWhen>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</handler>");
        _builder.newLine();
        {
          boolean _usesXImportSection = this._xbaseUsageDetector.usesXImportSection(this.getLanguage().getGrammar());
          if (_usesXImportSection) {
            _builder.append("\t");
            _builder.append("<handler");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("class=\"");
            TypeReference _eclipsePluginExecutableExtensionFactory_2 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
            _builder.append(_eclipsePluginExecutableExtensionFactory_2, "\t\t");
            _builder.append(":org.eclipse.xtext.xbase.ui.imports.OrganizeImportsHandler\"");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("commandId=\"org.eclipse.xtext.xbase.ui.organizeImports\">");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<activeWhen>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("<reference");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t\t");
            _builder.append("definitionId=\"");
            _builder.append(name, "\t\t\t\t");
            _builder.append(".Editor.opened\">");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("</reference>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("</activeWhen>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</handler>");
            _builder.newLine();
          }
        }
        _builder.append("</extension>");
        _builder.newLine();
        _builder.append("<extension point=\"org.eclipse.ui.menus\">");
        _builder.newLine();
        {
          boolean _usesXImportSection_1 = this._xbaseUsageDetector.usesXImportSection(this.getLanguage().getGrammar());
          if (_usesXImportSection_1) {
            _builder.append("\t");
            _builder.append("<menuContribution");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("locationURI=\"popup:#TextEditorContext?after=group.edit\">");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<command");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("commandId=\"org.eclipse.xtext.xbase.ui.organizeImports\"");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("style=\"push\"");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("tooltip=\"Organize Imports\">");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("<visibleWhen checkEnabled=\"false\">");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t\t");
            _builder.append("<reference");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t\t\t");
            _builder.append("definitionId=\"");
            _builder.append(name, "\t\t\t\t\t");
            _builder.append(".Editor.opened\">");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("\t\t\t");
            _builder.append("</reference>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("</visibleWhen>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("</command>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</menuContribution>");
            _builder.newLine();
          }
        }
        _builder.append("\t");
        _builder.append("<menuContribution");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("locationURI=\"popup:#TextEditorContext?after=group.open\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<command commandId=\"org.eclipse.xtext.xbase.ui.hierarchy.OpenTypeHierarchy\"");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("style=\"push\"");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("tooltip=\"Open Type Hierarchy\">");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<visibleWhen checkEnabled=\"false\">");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("<reference definitionId=\"");
        _builder.append(name, "\t\t\t\t");
        _builder.append(".Editor.opened\"/>");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("</visibleWhen>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("</command>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</menuContribution>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<menuContribution");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("locationURI=\"popup:#TextEditorContext?after=group.open\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<command commandId=\"org.eclipse.jdt.ui.edit.text.java.open.hierarchy\"");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("style=\"push\"");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("tooltip=\"Quick Type Hierarchy\">");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<visibleWhen checkEnabled=\"false\">");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("<reference definitionId=\"");
        _builder.append(name, "\t\t\t\t");
        _builder.append(".Editor.opened\"/>");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t\t");
        _builder.append("</visibleWhen>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("</command>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</menuContribution>");
        _builder.newLine();
        _builder.append("</extension>");
        _builder.newLine();
        _entries.add(_builder.toString());
      }
      if (this.jdtCallHierarchy) {
        List<CharSequence> _entries_1 = this.getProjectConfig().getEclipsePlugin().getPluginXml().getEntries();
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("<!-- Call Hierachy -->");
        _builder_1.newLine();
        _builder_1.append("<extension point=\"org.eclipse.ui.handlers\">");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("<handler");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("class=\"");
        TypeReference _eclipsePluginExecutableExtensionFactory_3 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
        _builder_1.append(_eclipsePluginExecutableExtensionFactory_3, "\t\t");
        _builder_1.append(":org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierachyHandler\"");
        _builder_1.newLineIfNotEmpty();
        _builder_1.append("\t\t");
        _builder_1.append("commandId=\"org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierarchy\">");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("<activeWhen>");
        _builder_1.newLine();
        _builder_1.append("\t\t\t");
        _builder_1.append("<reference");
        _builder_1.newLine();
        _builder_1.append("\t\t\t\t");
        _builder_1.append("definitionId=\"");
        _builder_1.append(name, "\t\t\t\t");
        _builder_1.append(".Editor.opened\">");
        _builder_1.newLineIfNotEmpty();
        _builder_1.append("\t\t\t");
        _builder_1.append("</reference>");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("</activeWhen>");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("</handler>");
        _builder_1.newLine();
        _builder_1.append("</extension>");
        _builder_1.newLine();
        _builder_1.append("<extension point=\"org.eclipse.ui.menus\">");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("<menuContribution");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("locationURI=\"popup:#TextEditorContext?after=group.open\">");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("<command commandId=\"org.eclipse.xtext.xbase.ui.hierarchy.OpenCallHierarchy\"");
        _builder_1.newLine();
        _builder_1.append("\t\t\t");
        _builder_1.append("style=\"push\"");
        _builder_1.newLine();
        _builder_1.append("\t\t\t");
        _builder_1.append("tooltip=\"Open Call Hierarchy\">");
        _builder_1.newLine();
        _builder_1.append("\t\t\t");
        _builder_1.append("<visibleWhen checkEnabled=\"false\">");
        _builder_1.newLine();
        _builder_1.append("\t\t\t\t");
        _builder_1.append("<reference definitionId=\"");
        _builder_1.append(name, "\t\t\t\t");
        _builder_1.append(".Editor.opened\"/>");
        _builder_1.newLineIfNotEmpty();
        _builder_1.append("\t\t\t");
        _builder_1.append("</visibleWhen>");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("</command>");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("</menuContribution>");
        _builder_1.newLine();
        _builder_1.append("</extension>");
        _builder_1.newLine();
        _entries_1.add(_builder_1.toString());
      }
      List<CharSequence> _entries_2 = this.getProjectConfig().getEclipsePlugin().getPluginXml().getEntries();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("<extension point=\"org.eclipse.core.runtime.adapters\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<factory class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_4 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_4, "\t");
      _builder_2.append(":org.eclipse.xtext.builder.smap.StratumBreakpointAdapterFactory\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("adaptableType=\"");
      String _name = this._xtextGeneratorNaming.getEclipsePluginEditor(this.getGrammar()).getName();
      _builder_2.append(_name, "\t\t");
      _builder_2.append("\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("<adapter type=\"org.eclipse.debug.ui.actions.IToggleBreakpointsTarget\"/>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</factory>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.editorActions\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<editorContribution targetID=\"");
      _builder_2.append(name, "\t");
      _builder_2.append("\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t");
      _builder_2.append(".rulerActions\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("<action");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("label=\"Not Used\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_5 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_5, "\t\t\t");
      _builder_2.append(":org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("style=\"push\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("actionID=\"RulerDoubleClick\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t\t");
      _builder_2.append(".doubleClickBreakpointAction\"/>");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t");
      _builder_2.append("</editorContribution>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.popupMenus\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<viewerContribution");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("targetID=\"");
      _builder_2.append(name, "\t\t");
      _builder_2.append(".RulerContext\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t");
      _builder_2.append(".RulerPopupActions\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("<action");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("label=\"Toggle Breakpoint\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_6 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_6, "\t\t\t");
      _builder_2.append(":org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("menubarPath=\"debug\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t\t");
      _builder_2.append(".rulerContextMenu.toggleBreakpointAction\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("</action>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<action");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("label=\"Not used\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_7 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_7, "\t\t\t");
      _builder_2.append(":org.eclipse.debug.ui.actions.RulerEnableDisableBreakpointActionDelegate\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("menubarPath=\"debug\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t\t");
      _builder_2.append(".rulerContextMenu.enableDisableBreakpointAction\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("</action>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<action");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("label=\"Breakpoint Properties\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("helpContextId=\"breakpoint_properties_action_context\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_8 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_8, "\t\t\t");
      _builder_2.append(":org.eclipse.jdt.debug.ui.actions.JavaBreakpointPropertiesRulerActionDelegate\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("menubarPath=\"group.properties\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("id=\"");
      _builder_2.append(name, "\t\t\t");
      _builder_2.append(".rulerContextMenu.openBreapointPropertiesAction\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("</action>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</viewerContribution>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<!-- Introduce Local Variable Refactoring -->");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.handlers\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<handler");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_9 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_9, "\t\t");
      _builder_2.append(":org.eclipse.xtext.xbase.ui.refactoring.ExtractVariableHandler\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("commandId=\"org.eclipse.xtext.xbase.ui.refactoring.ExtractLocalVariable\">");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<activeWhen>");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("<reference");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("definitionId=\"");
      _builder_2.append(name, "\t\t\t\t");
      _builder_2.append(".Editor.opened\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("</reference>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("</activeWhen>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</handler>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.menus\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<menuContribution");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("locationURI=\"popup:#TextEditorContext?after=group.edit\">");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<command commandId=\"org.eclipse.xtext.xbase.ui.refactoring.ExtractLocalVariable\"");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("style=\"push\">");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("<visibleWhen checkEnabled=\"false\">");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("<reference");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t\t");
      _builder_2.append("definitionId=\"");
      _builder_2.append(name, "\t\t\t\t\t");
      _builder_2.append(".Editor.opened\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("</reference>");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("</visibleWhen>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("</command>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</menuContribution>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<!-- Open implementation -->");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.handlers\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<handler");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("class=\"");
      TypeReference _eclipsePluginExecutableExtensionFactory_10 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(this.getGrammar());
      _builder_2.append(_eclipsePluginExecutableExtensionFactory_10, "\t\t");
      _builder_2.append(":org.eclipse.xtext.xbase.ui.navigation.OpenImplementationHandler\"");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t");
      _builder_2.append("commandId=\"org.eclipse.xtext.xbase.ui.OpenImplementationCommand\">");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<activeWhen>");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("<reference");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("definitionId=\"");
      _builder_2.append(name, "\t\t\t\t");
      _builder_2.append(".Editor.opened\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t");
      _builder_2.append("</reference>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("</activeWhen>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</handler>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _builder_2.append("<extension point=\"org.eclipse.ui.menus\">");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("<menuContribution");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("locationURI=\"menu:navigate?after=open.ext4\">");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("<command commandId=\"org.eclipse.xtext.xbase.ui.OpenImplementationCommand\">");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("<visibleWhen checkEnabled=\"false\">");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("<reference");
      _builder_2.newLine();
      _builder_2.append("\t\t\t\t\t");
      _builder_2.append("definitionId=\"");
      _builder_2.append(name, "\t\t\t\t\t");
      _builder_2.append(".Editor.opened\">");
      _builder_2.newLineIfNotEmpty();
      _builder_2.append("\t\t\t\t");
      _builder_2.append("</reference>");
      _builder_2.newLine();
      _builder_2.append("\t\t\t");
      _builder_2.append("</visibleWhen>");
      _builder_2.newLine();
      _builder_2.append("\t\t");
      _builder_2.append("</command>");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("</menuContribution>");
      _builder_2.newLine();
      _builder_2.append("</extension>");
      _builder_2.newLine();
      _xblockexpression = _entries_2.add(_builder_2.toString());
    }
    return _xblockexpression;
  }

  public void setGenerateXtendInferrer(final boolean generateXtendInferrer) {
    this.generateXtendInferrer = generateXtendInferrer;
  }

  public void setUseInferredJvmModel(final boolean useInferredJvmModel) {
    this.useInferredJvmModel = useInferredJvmModel;
  }

  public void setJdtTypeHierarchy(final boolean jdtTypeHierarchy) {
    this.jdtTypeHierarchy = jdtTypeHierarchy;
  }

  public void setJdtCallHierarchy(final boolean jdtCallHierarchy) {
    this.jdtCallHierarchy = jdtCallHierarchy;
  }

  public void setSkipExportedPackage(final boolean skipExportedPackage) {
    this.skipExportedPackage = skipExportedPackage;
  }
}
