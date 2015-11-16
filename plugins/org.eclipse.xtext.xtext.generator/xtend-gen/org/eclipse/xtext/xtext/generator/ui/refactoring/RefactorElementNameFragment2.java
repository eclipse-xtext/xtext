/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xtext.generator.ui.refactoring;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.name.Names;
import java.util.List;
import java.util.Set;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.IXtextGeneratorLanguage;
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.PluginXmlAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.model.project.IBundleProjectConfig;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;
import org.eclipse.xtext.xtext.generator.util.BooleanGeneratorOption;
import org.eclipse.xtext.xtext.generator.xbase.XbaseUsageDetector;

/**
 * Contributes the registration of element renaming infrastructure.
 * 
 * @author Christian Schneider - Initial contribution and API
 */
@SuppressWarnings("all")
public class RefactorElementNameFragment2 extends AbstractXtextGeneratorFragment {
  @Inject
  @Extension
  private XtextGeneratorNaming _xtextGeneratorNaming;
  
  @Inject
  @Extension
  private XbaseUsageDetector _xbaseUsageDetector;
  
  private final BooleanGeneratorOption useJdtRefactoring = new BooleanGeneratorOption();
  
  protected boolean isUseJdtRefactoring(final Grammar grammar) {
    boolean _xifexpression = false;
    boolean _isSet = this.useJdtRefactoring.isSet();
    if (_isSet) {
      _xifexpression = this.useJdtRefactoring.get();
    } else {
      _xifexpression = this._xbaseUsageDetector.inheritsXbase(grammar);
    }
    return _xifexpression;
  }
  
  public void setUseJdtRefactoring(final boolean useJdtRefactoring) {
    this.useJdtRefactoring.set(useJdtRefactoring);
  }
  
  @Override
  public void generate() {
    IXtextProjectConfig _projectConfig = this.getProjectConfig();
    IBundleProjectConfig _eclipsePlugin = _projectConfig.getEclipsePlugin();
    ManifestAccess _manifest = null;
    if (_eclipsePlugin!=null) {
      _manifest=_eclipsePlugin.getManifest();
    }
    boolean _notEquals = (!Objects.equal(_manifest, null));
    if (_notEquals) {
      IXtextProjectConfig _projectConfig_1 = this.getProjectConfig();
      IBundleProjectConfig _eclipsePlugin_1 = _projectConfig_1.getEclipsePlugin();
      ManifestAccess _manifest_1 = _eclipsePlugin_1.getManifest();
      Set<String> _requiredBundles = _manifest_1.getRequiredBundles();
      _requiredBundles.add("org.eclipse.xtext.ui");
    }
    GuiceModuleAccess.BindingFactory _bindingFactory = new GuiceModuleAccess.BindingFactory();
    TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameStrategy");
    TypeReference _typeRef_1 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.impl.DefaultRenameStrategy");
    GuiceModuleAccess.BindingFactory _addTypeToType = _bindingFactory.addTypeToType(_typeRef, _typeRef_1);
    TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IReferenceUpdater");
    TypeReference _typeRef_3 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.impl.DefaultReferenceUpdater");
    GuiceModuleAccess.BindingFactory _addTypeToType_1 = _addTypeToType.addTypeToType(_typeRef_2, _typeRef_3);
    TypeReference _typeRef_4 = TypeReference.typeRef("org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer");
    List<String> _simpleNames = _typeRef_4.getSimpleNames();
    String _join = IterableExtensions.join(_simpleNames, ".");
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer");
        _builder.append(_typeRef, "");
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(\"RefactoringPreferences\"))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        TypeReference _typeReference = new TypeReference("org.eclipse.xtext.ui.refactoring.ui", "RefactoringPreferences.Initializer");
        _builder.append(_typeReference, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    final GuiceModuleAccess.BindingFactory bindings = _addTypeToType_1.addConfiguredBinding(_join, _client);
    Grammar _grammar = this.getGrammar();
    boolean _isUseJdtRefactoring = this.isUseJdtRefactoring(_grammar);
    if (_isUseJdtRefactoring) {
      TypeReference _typeRef_5 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.ui.IRenameContextFactory");
      TypeReference _typeRef_6 = TypeReference.typeRef("org.eclipse.xtext.common.types.ui.refactoring.JdtRefactoringContextFactory");
      GuiceModuleAccess.BindingFactory _addTypeToType_2 = bindings.addTypeToType(_typeRef_5, _typeRef_6);
      TypeReference _typeRef_7 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameRefactoringProvider");
      TypeReference _typeRef_8 = TypeReference.typeRef("org.eclipse.xtext.common.types.ui.refactoring.JvmRenameRefactoringProvider");
      GuiceModuleAccess.BindingFactory _addTypeToType_3 = _addTypeToType_2.addTypeToType(_typeRef_7, _typeRef_8);
      TypeReference _typeReference = new TypeReference("org.eclipse.xtext.ui.refactoring.ui", "IRenameSupport.Factory");
      TypeReference _typeReference_1 = new TypeReference("org.eclipse.xtext.common.types.ui.refactoring", "JdtRenameSupport.Factory");
      GuiceModuleAccess.BindingFactory _addTypeToType_4 = _addTypeToType_3.addTypeToType(_typeReference, _typeReference_1);
      TypeReference _typeReference_2 = new TypeReference("org.eclipse.xtext.ui.refactoring", "IRenameStrategy.Provider");
      TypeReference _typeReference_3 = new TypeReference("org.eclipse.xtext.common.types.ui.refactoring.participant", "JvmMemberRenameStrategy.Provider");
      GuiceModuleAccess.BindingFactory _addTypeToType_5 = _addTypeToType_4.addTypeToType(_typeReference_2, _typeReference_3);
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("binder.bind(");
          TypeReference _typeReference = new TypeReference("org.eclipse.xtext.ui.refactoring", "IRenameStrategy.Provider");
          _builder.append(_typeReference, "");
          _builder.append(".class).annotatedWith(");
          TypeReference _typeReference_1 = new TypeReference("org.eclipse.xtext.common.types.ui.refactoring.participant", "JvmMemberRenameStrategy.Provider.Delegate");
          _builder.append(_typeReference_1, "");
          _builder.append(".class).to(");
          TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.impl.DefaultRenameStrategyProvider");
          _builder.append(_typeRef, "");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
        }
      };
      _addTypeToType_5.addConfiguredBinding(
        "JvmMemberRenameStrategy.Provider.Delegate", _client_1);
    } else {
      TypeReference _typeRef_9 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.IRenameRefactoringProvider");
      TypeReference _typeRef_10 = TypeReference.typeRef("org.eclipse.xtext.ui.refactoring.impl.DefaultRenameRefactoringProvider");
      GuiceModuleAccess.BindingFactory _addTypeToType_6 = bindings.addTypeToType(_typeRef_9, _typeRef_10);
      TypeReference _typeReference_4 = new TypeReference("org.eclipse.xtext.ui.refactoring.ui", "IRenameSupport.Factory");
      TypeReference _typeReference_5 = new TypeReference("org.eclipse.xtext.ui.refactoring.ui", "DefaultRenameSupport.Factory");
      _addTypeToType_6.addTypeToType(_typeReference_4, _typeReference_5);
    }
    IXtextGeneratorLanguage _language = this.getLanguage();
    GuiceModuleAccess _eclipsePluginGenModule = _language.getEclipsePluginGenModule();
    bindings.contributeTo(_eclipsePluginGenModule);
    IXtextProjectConfig _projectConfig_2 = this.getProjectConfig();
    IBundleProjectConfig _eclipsePlugin_2 = _projectConfig_2.getEclipsePlugin();
    PluginXmlAccess _pluginXml = null;
    if (_eclipsePlugin_2!=null) {
      _pluginXml=_eclipsePlugin_2.getPluginXml();
    }
    boolean _notEquals_1 = (!Objects.equal(_pluginXml, null));
    if (_notEquals_1) {
      IXtextProjectConfig _projectConfig_3 = this.getProjectConfig();
      IBundleProjectConfig _eclipsePlugin_3 = _projectConfig_3.getEclipsePlugin();
      PluginXmlAccess _pluginXml_1 = _eclipsePlugin_3.getPluginXml();
      List<CharSequence> _entries = _pluginXml_1.getEntries();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<!-- Rename Refactoring -->");
      _builder.newLine();
      _builder.append("<extension point=\"org.eclipse.ui.handlers\">");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("<handler ");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("class=\"");
      Grammar _grammar_1 = this.getGrammar();
      TypeReference _eclipsePluginExecutableExtensionFactory = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(_grammar_1);
      _builder.append(_eclipsePluginExecutableExtensionFactory, "\t\t");
      _builder.append(":org.eclipse.xtext.ui.refactoring.ui.DefaultRenameElementHandler\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t");
      _builder.append("commandId=\"org.eclipse.xtext.ui.refactoring.RenameElement\">");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<activeWhen>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<reference");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("definitionId=\"");
      Grammar _grammar_2 = this.getGrammar();
      String _name = _grammar_2.getName();
      _builder.append(_name, "\t\t\t\t");
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
      _builder.append("</extension>");
      _builder.newLine();
      _builder.append("<extension point=\"org.eclipse.ui.menus\">");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("<menuContribution");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("locationURI=\"popup:#TextEditorContext?after=group.edit\">");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<command commandId=\"org.eclipse.xtext.ui.refactoring.RenameElement\"");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("style=\"push\">");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<visibleWhen checkEnabled=\"false\">");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<reference");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("definitionId=\"");
      Grammar _grammar_3 = this.getGrammar();
      String _name_1 = _grammar_3.getName();
      _builder.append(_name_1, "\t\t\t\t\t");
      _builder.append(".Editor.opened\">");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t\t");
      _builder.append("</reference>");
      _builder.newLine();
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
      _builder.append("<extension point=\"org.eclipse.ui.preferencePages\">");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("<page");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("category=\"");
      Grammar _grammar_4 = this.getGrammar();
      String _name_2 = _grammar_4.getName();
      _builder.append(_name_2, "\t\t");
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t");
      _builder.append("class=\"");
      Grammar _grammar_5 = this.getGrammar();
      TypeReference _eclipsePluginExecutableExtensionFactory_1 = this._xtextGeneratorNaming.getEclipsePluginExecutableExtensionFactory(_grammar_5);
      _builder.append(_eclipsePluginExecutableExtensionFactory_1, "\t\t");
      _builder.append(":org.eclipse.xtext.ui.refactoring.ui.RefactoringPreferencePage\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t");
      _builder.append("id=\"");
      Grammar _grammar_6 = this.getGrammar();
      String _name_3 = _grammar_6.getName();
      _builder.append(_name_3, "\t\t");
      _builder.append(".refactoring\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t");
      _builder.append("name=\"Refactoring\">");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<keywordReference id=\"");
      Grammar _grammar_7 = this.getGrammar();
      String _runtimeBasePackage = this._xtextGeneratorNaming.getRuntimeBasePackage(_grammar_7);
      String _plus = (_runtimeBasePackage + ".ui.keyword_");
      Grammar _grammar_8 = this.getGrammar();
      String _simpleName = GrammarUtil.getSimpleName(_grammar_8);
      String _plus_1 = (_plus + _simpleName);
      _builder.append(_plus_1, "\t\t");
      _builder.append("\"/>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("</page>");
      _builder.newLine();
      _builder.append("</extension>");
      _builder.newLine();
      _entries.add(_builder.toString());
    }
  }
}
