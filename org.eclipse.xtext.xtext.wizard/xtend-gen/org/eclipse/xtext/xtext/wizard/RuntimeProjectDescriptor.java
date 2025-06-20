/**
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import com.google.common.collect.Iterables;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor;
import org.eclipse.xtext.xtext.wizard.ecore2xtext.Ecore2XtextGrammarCreator;

/**
 * @author Dennis Huebner - Initial contribution and API
 * @author Sven Efftinge
 * @author Miro Spoenemann
 * @author Stefan Oehme
 * @author Christian Schneider
 * @author Moritz Eysholdt
 * @author Lorenzo Bettini - bug#494176
 * @author Karsten Thoms - bug#484500, issue#108
 * @since 2.9
 */
@SuppressWarnings("all")
public class RuntimeProjectDescriptor extends TestedProjectDescriptor {
  private final Ecore2XtextGrammarCreator grammarCreator = new Ecore2XtextGrammarCreator();

  private final RuntimeTestProjectDescriptor testProject;

  @Accessors
  private boolean withPluginXml = true;

  public RuntimeProjectDescriptor(final WizardConfiguration config) {
    super(config);
    this.setEnabled(true);
    RuntimeTestProjectDescriptor _runtimeTestProjectDescriptor = new RuntimeTestProjectDescriptor(this);
    this.testProject = _runtimeTestProjectDescriptor;
  }

  @Override
  public void setEnabled(final boolean enabled) {
    if ((!enabled)) {
      throw new IllegalArgumentException("The runtime project is always enabled");
    }
    super.setEnabled(enabled);
  }

  private String nameQualifier = "";

  @Override
  public String getNameQualifier() {
    return this.nameQualifier;
  }

  public void setNameQualifier(final String nameQualifier) {
    this.nameQualifier = nameQualifier;
  }

  @Override
  public boolean isEclipsePluginProject() {
    return (Objects.equals(this.getConfig().getPreferredBuildSystem(), BuildSystem.NONE) || this.getConfig().getUiProject().isEnabled());
  }

  @Override
  public boolean isPartOfGradleBuild() {
    return true;
  }

  @Override
  public boolean isPartOfMavenBuild() {
    return true;
  }

  @Override
  public TestProjectDescriptor getTestProject() {
    return this.testProject;
  }

  @Override
  public Set<ExternalDependency> getExternalDependencies() {
    LinkedHashSet<ExternalDependency> _xblockexpression = null;
    {
      final LinkedHashSet<ExternalDependency> deps = CollectionLiterals.<ExternalDependency>newLinkedHashSet();
      Set<ExternalDependency> _externalDependencies = super.getExternalDependencies();
      Iterables.<ExternalDependency>addAll(deps, _externalDependencies);
      ExternalDependency _createXtextDependency = ExternalDependency.createXtextDependency("org.eclipse.xtext");
      deps.add(_createXtextDependency);
      ExternalDependency _createXtextDependency_1 = ExternalDependency.createXtextDependency("org.eclipse.xtext.xbase");
      deps.add(_createXtextDependency_1);
      ExternalDependency _externalDependency = new ExternalDependency();
      final Procedure1<ExternalDependency> _function = (ExternalDependency it) -> {
        final Procedure1<ExternalDependency.P2Coordinates> _function_1 = (ExternalDependency.P2Coordinates it_1) -> {
          it_1.setBundleId("org.eclipse.equinox.common");
          it_1.setVersion("3.19.0");
        };
        it.p2(_function_1);
      };
      ExternalDependency _doubleArrow = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency, _function);
      deps.add(_doubleArrow);
      if (((!this.isEclipsePluginProject()) && this.getConfig().needsMavenBuild())) {
        ExternalDependency _createXtextDependency_2 = ExternalDependency.createXtextDependency("org.eclipse.xtext.xtext.generator");
        final Procedure1<ExternalDependency> _function_1 = (ExternalDependency it) -> {
          ExternalDependency.MavenCoordinates _maven = it.getMaven();
          _maven.setOptional(true);
        };
        ExternalDependency _doubleArrow_1 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_createXtextDependency_2, _function_1);
        deps.add(_doubleArrow_1);
        ExternalDependency _externalDependency_1 = new ExternalDependency();
        final Procedure1<ExternalDependency> _function_2 = (ExternalDependency it) -> {
          final Procedure1<ExternalDependency.MavenCoordinates> _function_3 = (ExternalDependency.MavenCoordinates it_1) -> {
            it_1.setGroupId("org.eclipse.emf");
            it_1.setArtifactId("org.eclipse.emf.mwe2.launch");
            it_1.setOptional(true);
          };
          it.maven(_function_3);
        };
        ExternalDependency _doubleArrow_2 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_1, _function_2);
        deps.add(_doubleArrow_2);
        ExternalDependency _externalDependency_2 = new ExternalDependency();
        final Procedure1<ExternalDependency> _function_3 = (ExternalDependency it) -> {
          final Procedure1<ExternalDependency.MavenCoordinates> _function_4 = (ExternalDependency.MavenCoordinates it_1) -> {
            it_1.setGroupId("org.eclipse.xtext");
            it_1.setArtifactId("xtext-antlr-generator");
            it_1.setOptional(true);
          };
          it.maven(_function_4);
        };
        ExternalDependency _doubleArrow_3 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_2, _function_3);
        deps.add(_doubleArrow_3);
      }
      _xblockexpression = deps;
    }
    return _xblockexpression;
  }

  @Override
  public Set<String> getDevelopmentBundles() {
    final LinkedHashSet<String> result = CollectionLiterals.<String>newLinkedHashSet(
      "org.eclipse.xtext.xtext.generator.dependencies");
    boolean _isFromExistingEcoreModels = this.isFromExistingEcoreModels();
    if (_isFromExistingEcoreModels) {
      final Function1<EPackageInfo, Boolean> _function = (EPackageInfo it) -> {
        String _fileExtension = it.getGenmodelURI().fileExtension();
        return Boolean.valueOf(Objects.equals(_fileExtension, "xcore"));
      };
      boolean _exists = IterableExtensions.<EPackageInfo>exists(this.getConfig().getEcore2Xtext().getEPackageInfos(), _function);
      if (_exists) {
        result.add("org.eclipse.emf.ecore.xcore");
      }
    }
    return result;
  }

  @Override
  public Set<String> getBinIncludes() {
    LinkedHashSet<String> _xblockexpression = null;
    {
      final LinkedHashSet<String> includes = CollectionLiterals.<String>newLinkedHashSet();
      Set<String> _binIncludes = super.getBinIncludes();
      Iterables.<String>addAll(includes, _binIncludes);
      if (this.withPluginXml) {
        includes.add("plugin.xml");
      }
      _xblockexpression = includes;
    }
    return _xblockexpression;
  }

  @Override
  public Iterable<? extends AbstractFile> getFiles() {
    final ArrayList<AbstractFile> files = CollectionLiterals.<AbstractFile>newArrayList();
    Iterable<? extends AbstractFile> _files = super.getFiles();
    Iterables.<AbstractFile>addAll(files, _files);
    PlainTextFile _grammarFile = this.getGrammarFile();
    files.add(_grammarFile);
    PlainTextFile _file = this.file(Outlet.MAIN_JAVA, this.getWorkflowFilePath(), this.workflow());
    files.add(_file);
    PlainTextFile _workflowLaunchConfigFile = this.getWorkflowLaunchConfigFile();
    files.add(_workflowLaunchConfigFile);
    boolean _isEclipsePluginProject = this.getConfig().getRuntimeProject().isEclipsePluginProject();
    if (_isEclipsePluginProject) {
      PlainTextFile _launchConfigFile = this.getLaunchConfigFile();
      files.add(_launchConfigFile);
    }
    boolean _isPlainMavenBuild = this.isPlainMavenBuild();
    if (_isPlainMavenBuild) {
      PlainTextFile _file_1 = this.file(Outlet.ROOT, "jar-with-ecore-model.xml", this.jarDescriptor());
      files.add(_file_1);
    }
    return files;
  }

  private boolean isPlainMavenBuild() {
    return (this.getConfig().needsMavenBuild() && (!this.isEclipsePluginProject()));
  }

  public PlainTextFile getGrammarFile() {
    return this.file(Outlet.MAIN_JAVA, this.getGrammarFilePath(), this.grammar());
  }

  public String getGrammarFilePath() {
    StringConcatenation _builder = new StringConcatenation();
    String _basePackagePath = this.getConfig().getLanguage().getBasePackagePath();
    _builder.append(_basePackagePath);
    _builder.append("/");
    String _simpleName = this.getConfig().getLanguage().getSimpleName();
    _builder.append(_simpleName);
    _builder.append(".xtext");
    return _builder.toString();
  }

  public CharSequence grammar() {
    CharSequence _xifexpression = null;
    boolean _isFromExistingEcoreModels = this.isFromExistingEcoreModels();
    if (_isFromExistingEcoreModels) {
      _xifexpression = this.grammarCreator.grammar(this.getConfig());
    } else {
      _xifexpression = this.defaultGrammar();
    }
    return _xifexpression;
  }

  private CharSequence defaultGrammar() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("grammar ");
    String _name = this.getConfig().getLanguage().getName();
    _builder.append(_name);
    _builder.append(" with org.eclipse.xtext.common.Terminals");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("generate ");
    String _firstLower = StringExtensions.toFirstLower(this.getConfig().getLanguage().getSimpleName());
    _builder.append(_firstLower);
    _builder.append(" \"");
    String _nsURI = this.getConfig().getLanguage().getNsURI();
    _builder.append(_nsURI);
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("Model:");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("greetings+=Greeting*;");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("Greeting:");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("\'Hello\' name=ID \'!\';");
    _builder.newLine();
    return _builder;
  }

  public String getWorkflowFilePath() {
    StringConcatenation _builder = new StringConcatenation();
    String _basePackagePath = this.getConfig().getLanguage().getBasePackagePath();
    _builder.append(_basePackagePath);
    _builder.append("/Generate");
    String _simpleName = this.getConfig().getLanguage().getSimpleName();
    _builder.append(_simpleName);
    _builder.append(".mwe2");
    return _builder.toString();
  }

  public CharSequence workflow() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("module ");
    String _basePackagePath = this.getConfig().getLanguage().getBasePackagePath();
    String _plus = (_basePackagePath + "/Generate");
    String _simpleName = this.getConfig().getLanguage().getSimpleName();
    String _replaceAll = (_plus + _simpleName).replaceAll("/", ".");
    _builder.append(_replaceAll);
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xtext.generator.*");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xtext.generator.model.project.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("var rootPath = \"..\"");
    _builder.newLine();
    _builder.newLine();
    _builder.append("Workflow {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("component = XtextGenerator {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("configuration = {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("project = StandardProjectConfig {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("baseName = \"");
    String _name = this.getName();
    _builder.append(_name, "\t\t\t\t");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t\t");
    _builder.append("rootPath = rootPath");
    _builder.newLine();
    {
      boolean _isEnabled = this.testProject.isEnabled();
      if (_isEnabled) {
        _builder.append("\t\t\t\t");
        _builder.append("runtimeTest = {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("\t");
        _builder.append("enabled = true");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      if ((this.getConfig().getIdeProject().isEnabled() && (!IterableExtensions.exists(Collections.<ProjectDescriptor>unmodifiableList(CollectionLiterals.<ProjectDescriptor>newArrayList(this.getConfig().getWebProject(), this.getConfig().getUiProject())), ((Function1<ProjectDescriptor, Boolean>) (ProjectDescriptor it) -> {
        return Boolean.valueOf(it.isEnabled());
      }))))) {
        _builder.append("\t\t\t\t");
        _builder.append("genericIde = {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("\t");
        _builder.append("enabled = true");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      boolean _isEnabled_1 = this.getConfig().getUiProject().isEnabled();
      if (_isEnabled_1) {
        _builder.append("\t\t\t\t");
        _builder.append("eclipsePlugin = {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("\t");
        _builder.append("enabled = true");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      boolean _isEnabled_2 = this.getConfig().getUiProject().getTestProject().isEnabled();
      if (_isEnabled_2) {
        _builder.append("\t\t\t\t");
        _builder.append("eclipsePluginTest = {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("\t");
        _builder.append("enabled = true");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      boolean _isEnabled_3 = this.getConfig().getWebProject().isEnabled();
      if (_isEnabled_3) {
        _builder.append("\t\t\t\t");
        _builder.append("web = {");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("\t");
        _builder.append("enabled = true");
        _builder.newLine();
        _builder.append("\t\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    {
      SourceLayout _sourceLayout = this.getConfig().getSourceLayout();
      boolean _equals = Objects.equals(_sourceLayout, SourceLayout.MAVEN);
      if (_equals) {
        _builder.append("\t\t\t\t");
        _builder.append("mavenLayout = true");
        _builder.newLine();
      }
    }
    {
      boolean _isEclipsePluginProject = this.isEclipsePluginProject();
      if (_isEclipsePluginProject) {
        _builder.append("\t\t\t\t");
        _builder.append("createEclipseMetaData = true");
        _builder.newLine();
      }
    }
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("code = {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("encoding = \"");
    Charset _encoding = this.getConfig().getEncoding();
    _builder.append(_encoding, "\t\t\t\t");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t\t");
    _builder.append("lineDelimiter = \"");
    String _convertToJavaString = Strings.convertToJavaString(this.getConfig().getLineDelimiter());
    _builder.append(_convertToJavaString, "\t\t\t\t");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t\t");
    _builder.append("fileHeader = \"/*\\n * generated by Xtext \\${version}\\n */\"");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("preferXtendStubs = false");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("language = StandardLanguage {");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("name = \"");
    String _name_1 = this.getConfig().getLanguage().getName();
    _builder.append(_name_1, "\t\t\t");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t\t");
    _builder.append("fileExtensions = \"");
    LanguageDescriptor.FileExtensions _fileExtensions = this.getConfig().getLanguage().getFileExtensions();
    _builder.append(_fileExtensions, "\t\t\t");
    _builder.append("\"");
    _builder.newLineIfNotEmpty();
    {
      boolean _isEmpty = this.getConfig().getEcore2Xtext().getEPackageInfos().isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        {
          final Function1<EPackageInfo, String> _function = (EPackageInfo it) -> {
            return it.getGenmodelURI().toString();
          };
          Set<String> _set = IterableExtensions.<String>toSet(IterableExtensions.<EPackageInfo, String>map(this.getConfig().getEcore2Xtext().getEPackageInfos(), _function));
          for(final String genmodelURI : _set) {
            _builder.append("\t\t\t");
            _builder.append("referencedResource = \"");
            _builder.append(genmodelURI, "\t\t\t");
            _builder.append("\"");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    }
    {
      boolean _isFromExistingEcoreModels = this.isFromExistingEcoreModels();
      if (_isFromExistingEcoreModels) {
        _builder.append("\t\t\t");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("fragment = ecore2xtext.Ecore2XtextValueConverterServiceFragment2 auto-inject {}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("formatter = {");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("generateStub = true");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("generateXtendStub = true");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("serializer = {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("generateStub = false");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("validator = {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("// composedCheck = \"org.eclipse.xtext.validation.NamesAreUniqueValidator\"");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("// Enables several checks on the inferred Jvm model for Xbase languages concerning Java inheritance relations");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("// composedCheck = \"org.eclipse.xtext.xbase.validation.JvmGenericTypeValidator\"");
    _builder.newLine();
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("// Generates checks for @Deprecated grammar annotations, an IssueProvider and a corresponding PropertyPage");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("generateDeprecationValidation = true");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("generator = {");
    _builder.newLine();
    _builder.append("\t\t\t\t");
    _builder.append("generateXtendStub = true");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("}");
    _builder.newLine();
    {
      JUnitVersion _junitVersion = this.getConfig().getJunitVersion();
      boolean _equals_1 = Objects.equals(_junitVersion, JUnitVersion.JUNIT_5);
      if (_equals_1) {
        _builder.append("\t\t\t");
        _builder.append("junitSupport = {");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("\t");
        _builder.append("junitVersion = \"5\"");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("}");
        _builder.newLine();
      }
    }
    _builder.append("\t\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }

  @Override
  public GradleBuildFile buildGradle() {
    GradleBuildFile _buildGradle = super.buildGradle();
    final Procedure1<GradleBuildFile> _function = (GradleBuildFile it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("configurations {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mwe2 {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("extendsFrom compileClasspath");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("dependencies {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mwe2 \'org.eclipse.emf:org.eclipse.emf.mwe2.launch\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mwe2 \"org.eclipse.xtext:org.eclipse.xtext.common.types:${xtextVersion}\"");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mwe2 \"org.eclipse.xtext:org.eclipse.xtext.xtext.generator:${xtextVersion}\"");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mwe2 \'org.eclipse.xtext:xtext-antlr-generator\'");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("task generateXtextLanguage(type: JavaExec) {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("mainClass = \'org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher\'");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("classpath = configurations.mwe2");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("inputs.file \"");
      String _sourceFolder = this.sourceFolder(Outlet.MAIN_JAVA);
      _builder.append(_sourceFolder, "\t");
      _builder.append("/");
      String _workflowFilePath = this.getWorkflowFilePath();
      _builder.append(_workflowFilePath, "\t");
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("inputs.file \"");
      String _sourceFolder_1 = this.sourceFolder(Outlet.MAIN_JAVA);
      _builder.append(_sourceFolder_1, "\t");
      _builder.append("/");
      String _grammarFilePath = this.getGrammarFilePath();
      _builder.append(_grammarFilePath, "\t");
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("outputs.dir \"");
      String _sourceFolder_2 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
      _builder.append(_sourceFolder_2, "\t");
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("args += \"");
      String _sourceFolder_3 = this.sourceFolder(Outlet.MAIN_JAVA);
      _builder.append(_sourceFolder_3, "\t");
      _builder.append("/");
      String _workflowFilePath_1 = this.getWorkflowFilePath();
      _builder.append(_workflowFilePath_1, "\t");
      _builder.append("\"");
      _builder.newLineIfNotEmpty();
      _builder.append("\t");
      _builder.append("args += \"-p\"");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("args += \"rootPath=/${projectDir}/..\"");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      {
        boolean _isInlined = this.testProject.isInlined();
        if (_isInlined) {
          String _additionalContent = this.testProject.buildGradle().getAdditionalContent();
          _builder.append(_additionalContent);
          _builder.newLineIfNotEmpty();
        }
      }
      _builder.append("processResources.dependsOn(generateXtextLanguage)");
      _builder.newLine();
      _builder.append("generateXtext.dependsOn(generateXtextLanguage)");
      _builder.newLine();
      _builder.append("clean.dependsOn(cleanGenerateXtextLanguage)");
      _builder.newLine();
      _builder.append("eclipse.classpath.plusConfigurations += [configurations.mwe2]");
      _builder.newLine();
      it.setAdditionalContent(_builder.toString());
    };
    return ObjectExtensions.<GradleBuildFile>operator_doubleArrow(_buildGradle, _function);
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      String _xifexpression = null;
      boolean _isEclipsePluginProject = this.isEclipsePluginProject();
      if (_isEclipsePluginProject) {
        _xifexpression = "eclipse-plugin";
      } else {
        _xifexpression = "jar";
      }
      it.setPackaging(_xifexpression);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<build>");
      _builder.newLine();
      {
        if (((!this.isEclipsePluginProject()) && Objects.equals(this.getConfig().getSourceLayout(), SourceLayout.PLAIN))) {
          _builder.append("\t");
          _builder.append("<sourceDirectory>");
          String _sourceFolder = this.sourceFolder(Outlet.MAIN_JAVA);
          _builder.append(_sourceFolder, "\t");
          _builder.append("</sourceDirectory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("<resources>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t");
          _builder.append("<resource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("<directory>");
          String _sourceFolder_1 = this.sourceFolder(Outlet.MAIN_RESOURCES);
          _builder.append(_sourceFolder_1, "\t\t\t");
          _builder.append("</directory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("<excludes>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.java</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.xtend</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.xtext</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.mwe2</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("</excludes>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t");
          _builder.append("</resource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("</resources>");
          _builder.newLine();
        }
      }
      _builder.append("\t");
      _builder.append("<plugins>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.codehaus.mojo</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>exec-maven-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<version>3.0.0</version>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<executions>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<execution>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<?m2e ignore?>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<id>mwe2Launcher</id>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<phase>generate-sources</phase>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<goals>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<goal>java</goal>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("</goals>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("</execution>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("</executions>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<mainClass>org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher</mainClass>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<arguments>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<argument>/${project.basedir}/");
      String _sourceFolder_2 = this.sourceFolder(Outlet.MAIN_JAVA);
      _builder.append(_sourceFolder_2, "\t\t\t\t\t");
      _builder.append("/");
      String _workflowFilePath = this.getWorkflowFilePath();
      _builder.append(_workflowFilePath, "\t\t\t\t\t");
      _builder.append("</argument>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t\t\t");
      _builder.append("<argument>-p</argument>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<argument>rootPath=/${project.basedir}/..</argument>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("</arguments>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<classpathScope>compile</classpathScope>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<includePluginDependencies>true</includePluginDependencies>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<cleanupDaemonThreads>false</cleanupDaemonThreads><!-- see https://bugs.eclipse.org/bugs/show_bug.cgi?id=475098#c3 -->");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("</configuration>");
      _builder.newLine();
      {
        boolean _needsTychoBuild = this.getConfig().needsTychoBuild();
        if (_needsTychoBuild) {
          _builder.append("\t\t\t");
          _builder.append("<dependencies>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("\t");
          _builder.append("<dependency>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("\t\t");
          _builder.append("<groupId>org.eclipse.xtext</groupId>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("\t\t");
          _builder.append("<artifactId>org.eclipse.xtext.xtext.generator.dependencies</artifactId>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("\t\t");
          _builder.append("<version>${xtextVersion}</version>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("\t");
          _builder.append("</dependency>");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("</dependencies>");
          _builder.newLine();
        }
      }
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.eclipse.xtext</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>xtend-maven-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.apache.maven.plugins</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>maven-clean-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<filesets combine.children=\"append\">");
      _builder.newLine();
      {
        IdeProjectDescriptor _ideProject = this.getConfig().getIdeProject();
        UiProjectDescriptor _uiProject = this.getConfig().getUiProject();
        WebProjectDescriptor _webProject = this.getConfig().getWebProject();
        for(final ProjectDescriptor p : Collections.<ProjectDescriptor>unmodifiableList(CollectionLiterals.<ProjectDescriptor>newArrayList(this, _ideProject, _uiProject, _webProject))) {
          {
            boolean _isEnabled = p.isEnabled();
            if (_isEnabled) {
              _builder.append("\t\t\t\t\t");
              _builder.append("<fileset>");
              _builder.newLine();
              _builder.append("\t\t\t\t\t");
              _builder.append("\t");
              _builder.append("<directory>${basedir}/../");
              String _name = p.getName();
              _builder.append(_name, "\t\t\t\t\t\t");
              _builder.append("/");
              String _sourceFolder_3 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
              _builder.append(_sourceFolder_3, "\t\t\t\t\t\t");
              _builder.append("/</directory>");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t\t\t\t");
              _builder.append("\t");
              _builder.append("<includes>");
              _builder.newLine();
              _builder.append("\t\t\t\t\t");
              _builder.append("\t\t");
              _builder.append("<include>**/*</include>");
              _builder.newLine();
              _builder.append("\t\t\t\t\t");
              _builder.append("\t");
              _builder.append("</includes>");
              _builder.newLine();
              _builder.append("\t\t\t\t\t");
              _builder.append("</fileset>");
              _builder.newLine();
              {
                if ((p instanceof TestedProjectDescriptor)) {
                  {
                    boolean _isEnabled_1 = ((TestedProjectDescriptor)p).getTestProject().isEnabled();
                    if (_isEnabled_1) {
                      _builder.append("\t\t\t\t\t");
                      _builder.append("<fileset>");
                      _builder.newLine();
                      _builder.append("\t\t\t\t\t");
                      _builder.append("\t");
                      _builder.append("<directory>${basedir}/../");
                      String _xifexpression_1 = null;
                      boolean _isInlined = ((TestedProjectDescriptor)p).getTestProject().isInlined();
                      if (_isInlined) {
                        _xifexpression_1 = ((TestedProjectDescriptor)p).getName();
                      } else {
                        _xifexpression_1 = ((TestedProjectDescriptor)p).getTestProject().getName();
                      }
                      _builder.append(_xifexpression_1, "\t\t\t\t\t\t");
                      _builder.append("/");
                      String _sourceFolder_4 = this.sourceFolder(Outlet.TEST_SRC_GEN);
                      _builder.append(_sourceFolder_4, "\t\t\t\t\t\t");
                      _builder.append("/</directory>");
                      _builder.newLineIfNotEmpty();
                      _builder.append("\t\t\t\t\t");
                      _builder.append("\t");
                      _builder.append("<includes>");
                      _builder.newLine();
                      _builder.append("\t\t\t\t\t");
                      _builder.append("\t\t");
                      _builder.append("<include>**/*</include>");
                      _builder.newLine();
                      _builder.append("\t\t\t\t\t");
                      _builder.append("\t");
                      _builder.append("</includes>");
                      _builder.newLine();
                      _builder.append("\t\t\t\t\t");
                      _builder.append("</fileset>");
                      _builder.newLine();
                    }
                  }
                }
              }
            }
          }
        }
      }
      _builder.append("\t\t\t\t\t");
      _builder.append("<fileset>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<directory>${basedir}/model/generated/</directory>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("</fileset>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("</filesets>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("</configuration>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      {
        boolean _isEclipsePluginProject_1 = this.isEclipsePluginProject();
        boolean _not = (!_isEclipsePluginProject_1);
        if (_not) {
          _builder.append("\t\t");
          _builder.append("<plugin>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<groupId>org.codehaus.mojo</groupId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<artifactId>build-helper-maven-plugin</artifactId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<version>3.3.0</version>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<executions>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t");
          _builder.append("<execution>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<?m2e ignore?>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<id>add-source</id>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<phase>initialize</phase>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<goals>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<goal>add-source</goal>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<goal>add-resource</goal>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("</goals>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<sources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("<source>");
          String _sourceFolder_5 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
          _builder.append(_sourceFolder_5, "\t\t\t\t\t\t\t");
          _builder.append("</source>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("</sources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<resources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("<resource>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("<directory>");
          String _sourceFolder_6 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
          _builder.append(_sourceFolder_6, "\t\t\t\t\t\t\t\t");
          _builder.append("</directory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("<excludes>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t\t");
          _builder.append("<exclude>**/*.java</exclude>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t\t");
          _builder.append("<exclude>**/*.g</exclude>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("</excludes>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("</resource>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("</resources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("</configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t");
          _builder.append("</execution>");
          _builder.newLine();
          {
            boolean _isInlined_1 = this.testProject.isInlined();
            if (_isInlined_1) {
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<execution>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<id>add-test-source</id>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<phase>initialize</phase>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<goals>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<goal>add-test-source</goal>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<goal>add-test-resource</goal>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("</goals>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<configuration>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<sources>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<source>");
              String _sourceFolder_7 = this.sourceFolder(Outlet.TEST_SRC_GEN);
              _builder.append(_sourceFolder_7, "\t\t\t\t\t\t\t");
              _builder.append("</source>");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("</sources>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<resources>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<resource>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t\t");
              _builder.append("<directory>");
              String _sourceFolder_8 = this.sourceFolder(Outlet.TEST_SRC_GEN);
              _builder.append(_sourceFolder_8, "\t\t\t\t\t\t\t\t");
              _builder.append("</directory>");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t\t");
              _builder.append("<excludes>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t\t\t");
              _builder.append("<exclude>**/*.java</exclude>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t\t");
              _builder.append("</excludes>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("</resource>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("</resources>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("</configuration>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("</execution>");
              _builder.newLine();
            }
          }
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("</executions>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("</plugin>");
          _builder.newLine();
          {
            boolean _isPlainMavenBuild = this.isPlainMavenBuild();
            if (_isPlainMavenBuild) {
              _builder.append("\t\t");
              _builder.append("<plugin>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<artifactId>maven-assembly-plugin</artifactId>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<version>3.4.1</version>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<configuration>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<descriptors>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<descriptor>jar-with-ecore-model.xml</descriptor>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("</descriptors>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<appendAssemblyId>false</appendAssemblyId>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("</configuration>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("<executions>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("<execution>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<id>make-assembly</id>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<phase>package</phase>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("<goals>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t\t");
              _builder.append("<goal>single</goal>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("</goals>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("</execution>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("</executions>");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("</plugin>");
              _builder.newLine();
            }
          }
        }
      }
      _builder.append("\t");
      _builder.append("</plugins>");
      _builder.newLine();
      _builder.append("</build>");
      _builder.newLine();
      it.setBuildSection(_builder.toString());
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }

  public CharSequence jarDescriptor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<assembly xmlns=\"http://maven.apache.org/ASSEMBLY/2.0.0\" ");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("xsi:schemaLocation=\"http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd\">");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<id>jar-with-ecore-model</id>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<formats>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<format>jar</format>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</formats>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<includeBaseDirectory>false</includeBaseDirectory>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<fileSets>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<fileSet>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<outputDirectory>/</outputDirectory>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<directory>target/classes</directory>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</fileSet>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<fileSet>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<outputDirectory>model/generated</outputDirectory>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<directory>model/generated</directory>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</fileSet>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</fileSets>");
    _builder.newLine();
    _builder.append("</assembly>");
    _builder.newLine();
    return _builder;
  }

  /**
   * @since 2.11
   */
  @Override
  public Set<String> getBinExcludes() {
    return Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("**/*.xtend", "**/*.mwe2"));
  }

  /**
   * @since 2.11
   */
  private PlainTextFile getWorkflowLaunchConfigFile() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(".launch/Generate ");
    String _simpleName = this.getConfig().getLanguage().getSimpleName();
    _builder.append(_simpleName);
    _builder.append(" (");
    String _head = IterableExtensions.<String>head(this.getConfig().getLanguage().getFileExtensions());
    _builder.append(_head);
    _builder.append(") Language Infrastructure.launch");
    return this.file(Outlet.ROOT, _builder.toString(), this.workflowLaunchConfig());
  }

  /**
   * @since 2.11
   */
  private CharSequence workflowLaunchConfig() {
    CharSequence _xblockexpression = null;
    {
      ArrayList<ProjectDescriptor> projectsToRefresh = CollectionLiterals.<ProjectDescriptor>newArrayList();
      Set<ProjectDescriptor> _enabledProjects = this.getConfig().getEnabledProjects();
      Iterables.<ProjectDescriptor>addAll(projectsToRefresh, _enabledProjects);
      boolean _isEnabled = this.getConfig().getRuntimeProject().testProject.isEnabled();
      if (_isEnabled) {
        projectsToRefresh.add(this.getConfig().getRuntimeProject().testProject);
      }
      boolean _isEnabled_1 = this.getConfig().getUiProject().getTestProject().isEnabled();
      if (_isEnabled_1) {
        TestProjectDescriptor _testProject = this.getConfig().getUiProject().getTestProject();
        projectsToRefresh.add(_testProject);
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;");
      {
        for(final ProjectDescriptor it : projectsToRefresh) {
          _builder.append("&lt;item path=&quot;/");
          String _name = it.getName();
          _builder.append(_name);
          _builder.append("&quot; type=&quot;4&quot;/&gt;&#10;");
        }
      }
      _builder.append(";&lt;/resources&gt;}");
      final String refreshAttr = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
      _builder_1.newLine();
      {
        boolean _isEclipsePluginProject = this.getConfig().getRuntimeProject().isEclipsePluginProject();
        if (_isEclipsePluginProject) {
          _builder_1.append("<launchConfiguration type=\"org.eclipse.emf.mwe2.launch.Mwe2LaunchConfigurationType\">");
          _builder_1.newLine();
          _builder_1.append("<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">");
          _builder_1.newLine();
          _builder_1.append("<listEntry value=\"/");
          String _name_1 = this.getConfig().getRuntimeProject().getName();
          _builder_1.append(_name_1);
          _builder_1.append("\"/>");
          _builder_1.newLineIfNotEmpty();
          _builder_1.append("</listAttribute>");
          _builder_1.newLine();
          _builder_1.append("<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">");
          _builder_1.newLine();
          _builder_1.append("<listEntry value=\"4\"/>");
          _builder_1.newLine();
          _builder_1.append("</listAttribute>");
          _builder_1.newLine();
          _builder_1.append("<listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">");
          _builder_1.newLine();
          _builder_1.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.debug\"/>");
          _builder_1.newLine();
          _builder_1.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.run\"/>");
          _builder_1.newLine();
          _builder_1.append("</listAttribute>");
          _builder_1.newLine();
          _builder_1.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"");
          _builder_1.append(refreshAttr);
          _builder_1.append("\"/>");
          _builder_1.newLineIfNotEmpty();
          _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher\"/>");
          _builder_1.newLine();
          _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"");
          String _basePackagePath = this.getConfig().getLanguage().getBasePackagePath();
          String _plus = (_basePackagePath + "/Generate");
          String _simpleName = this.getConfig().getLanguage().getSimpleName();
          String _replaceAll = (_plus + _simpleName).replaceAll("/", ".");
          _builder_1.append(_replaceAll);
          _builder_1.append("\"/>");
          _builder_1.newLineIfNotEmpty();
          _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"");
          String _name_2 = this.getConfig().getRuntimeProject().getName();
          _builder_1.append(_name_2);
          _builder_1.append("\"/>");
          _builder_1.newLineIfNotEmpty();
          _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Xmx512m\"/>");
          _builder_1.newLine();
          _builder_1.append("</launchConfiguration>");
          _builder_1.newLine();
        } else {
          BuildSystem _preferredBuildSystem = this.getConfig().getPreferredBuildSystem();
          boolean _equals = Objects.equals(_preferredBuildSystem, BuildSystem.MAVEN);
          if (_equals) {
            _builder_1.append("<launchConfiguration type=\"org.eclipse.m2e.Maven2LaunchConfigurationType\">");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_DEBUG_OUTPUT\" value=\"false\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"M2_GOALS\" value=\"clean generate-sources\"/>");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_NON_RECURSIVE\" value=\"false\"/>");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_OFFLINE\" value=\"false\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"M2_PROFILES\" value=\"\"/>");
            _builder_1.newLine();
            _builder_1.append("<listAttribute key=\"M2_PROPERTIES\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"M2_RUNTIME\" value=\"EMBEDDED\"/>");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_SKIP_TESTS\" value=\"false\"/>");
            _builder_1.newLine();
            _builder_1.append("<intAttribute key=\"M2_THREADS\" value=\"1\"/>");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_UPDATE_SNAPSHOTS\" value=\"false\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"M2_USER_SETTINGS\" value=\"\"/>");
            _builder_1.newLine();
            _builder_1.append("<booleanAttribute key=\"M2_WORKSPACE_RESOLUTION\" value=\"true\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"");
            _builder_1.append(refreshAttr);
            _builder_1.append("\"/>");
            _builder_1.newLineIfNotEmpty();
            _builder_1.append("<booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>");
            _builder_1.newLine();
            _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.WORKING_DIRECTORY\" value=\"${workspace_loc:/");
            String _name_3 = this.getConfig().getRuntimeProject().getName();
            _builder_1.append(_name_3);
            _builder_1.append("}\"/>");
            _builder_1.newLineIfNotEmpty();
            _builder_1.append("</launchConfiguration>");
            _builder_1.newLine();
          } else {
            BuildSystem _preferredBuildSystem_1 = this.getConfig().getPreferredBuildSystem();
            boolean _equals_1 = Objects.equals(_preferredBuildSystem_1, BuildSystem.GRADLE);
            if (_equals_1) {
              _builder_1.append("<launchConfiguration type=\"org.eclipse.buildship.core.launch.runconfiguration\">");
              _builder_1.newLine();
              _builder_1.append("<listAttribute key=\"arguments\"/>");
              _builder_1.newLine();
              _builder_1.append("<stringAttribute key=\"gradle_distribution\" value=\"GRADLE_DISTRIBUTION(WRAPPER)\"/>");
              _builder_1.newLine();
              _builder_1.append("<listAttribute key=\"jvm_arguments\"/>");
              _builder_1.newLine();
              _builder_1.append("<booleanAttribute key=\"show_console_view\" value=\"true\"/>");
              _builder_1.newLine();
              _builder_1.append("<booleanAttribute key=\"show_execution_view\" value=\"true\"/>");
              _builder_1.newLine();
              _builder_1.append("<listAttribute key=\"tasks\">");
              _builder_1.newLine();
              _builder_1.append("<listEntry value=\"build\"/>");
              _builder_1.newLine();
              _builder_1.append("</listAttribute>");
              _builder_1.newLine();
              _builder_1.append("<stringAttribute key=\"org.eclipse.debug.core.ATTR_REFRESH_SCOPE\" value=\"");
              _builder_1.append(refreshAttr);
              _builder_1.append("\"/>");
              _builder_1.newLineIfNotEmpty();
              _builder_1.append("<booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>");
              _builder_1.newLine();
              _builder_1.append("<stringAttribute key=\"org.eclipse.jdt.launching.WORKING_DIRECTORY\" value=\"${workspace_loc:/");
              String _name_4 = this.getConfig().getRuntimeProject().getName();
              _builder_1.append(_name_4);
              _builder_1.append("}\"/>");
              _builder_1.newLineIfNotEmpty();
              _builder_1.append("</launchConfiguration>");
              _builder_1.newLine();
            }
          }
        }
      }
      _xblockexpression = _builder_1;
    }
    return _xblockexpression;
  }

  /**
   * @since 2.11
   */
  private PlainTextFile getLaunchConfigFile() {
    return this.file(Outlet.ROOT, ".launch/Launch Runtime Eclipse.launch", this.launchConfig());
  }

  /**
   * @since 2.11
   */
  private CharSequence launchConfig() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    _builder.newLine();
    _builder.append("<launchConfiguration type=\"org.eclipse.pde.ui.RuntimeWorkbench\">");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"append.args\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"askclear\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"automaticAdd\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"automaticValidate\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"bad_container_name\" value=\"/");
    String _name = this.getConfig().getRuntimeProject().getName();
    _builder.append(_name);
    _builder.append("/.launch/\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("<stringAttribute key=\"bootstrap\" value=\"\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"checked\" value=\"[NONE]\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearConfig\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearws\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"clearwslog\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"configLocation\" value=\"${workspace_loc}/.metadata/.plugins/org.eclipse.pde.core/Launch Runtime Eclipse\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"default\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"includeOptional\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"location\" value=\"${workspace_loc}/../runtime-EclipseXtext\"/>");
    _builder.newLine();
    _builder.append("<listAttribute key=\"org.eclipse.debug.ui.favoriteGroups\">");
    _builder.newLine();
    _builder.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.debug\"/>");
    _builder.newLine();
    _builder.append("<listEntry value=\"org.eclipse.debug.ui.launchGroup.run\"/>");
    _builder.newLine();
    _builder.append("</listAttribute>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.JRE_CONTAINER\" value=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/");
    String _bree = this.getConfig().getJavaVersion().getBree();
    _builder.append(_bree);
    _builder.append("\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.PROGRAM_ARGUMENTS\" value=\"-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl}\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER\" value=\"org.eclipse.pde.ui.workbenchClasspathProvider\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"org.eclipse.jdt.launching.VM_ARGUMENTS\" value=\"-Xms40m -Xmx512m\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"pde.version\" value=\"3.3\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"product\" value=\"org.eclipse.platform.ide\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"show_selected_only\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<stringAttribute key=\"templateConfig\" value=\"${target_home}/configuration/config.ini\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"tracing\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useDefaultConfig\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useDefaultConfigArea\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"useProduct\" value=\"true\"/>");
    _builder.newLine();
    _builder.append("<booleanAttribute key=\"usefeatures\" value=\"false\"/>");
    _builder.newLine();
    _builder.append("</launchConfiguration>");
    _builder.newLine();
    return _builder;
  }

  @Pure
  public boolean isWithPluginXml() {
    return this.withPluginXml;
  }

  public void setWithPluginXml(final boolean withPluginXml) {
    this.withPluginXml = withPluginXml;
  }
}
