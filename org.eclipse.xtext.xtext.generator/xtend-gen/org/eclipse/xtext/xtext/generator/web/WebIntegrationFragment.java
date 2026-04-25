/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.web;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.eclipse.emf.mwe2.runtime.Mandatory;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.util.DisposableRegistry;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.CodeConfig;
import org.eclipse.xtext.xtext.generator.Issues;
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.TextFileAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.util.BooleanGeneratorOption;
import org.eclipse.xtext.xtext.generator.util.GeneratorOption;
import org.eclipse.xtext.xtext.generator.util.GrammarUtil2;
import org.eclipse.xtext.xtext.generator.xbase.XbaseUsageDetector;

/**
 * Main generator fragment for web integration.
 */
@SuppressWarnings("all")
public class WebIntegrationFragment extends AbstractXtextGeneratorFragment {
  public enum Framework {
    ORION,

    ACE,

    CODEMIRROR;
  }

  private static final String REQUIREJS_VERSION = "2.3.7";

  private static final String REQUIREJS_TEXT_VERSION = "2.0.15";

  private static final String JQUERY_VERSION = "3.6.0";

  private static final String ACE_VERSION = "1.3.3";

  private static final String CODEMIRROR_VERSION = "5.41.0";

  @Inject
  private FileAccessFactory fileAccessFactory;

  @Inject
  private CodeConfig codeConfig;

  @Inject
  @Extension
  private XtextGeneratorNaming _xtextGeneratorNaming;

  @Inject
  @Extension
  private XbaseUsageDetector _xbaseUsageDetector;

  private final HashSet<String> enabledPatterns = new HashSet<String>();

  private final HashSet<String> suppressedPatterns = new HashSet<String>();

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final GeneratorOption<WebIntegrationFragment.Framework> framework = new GeneratorOption<WebIntegrationFragment.Framework>();

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final BooleanGeneratorOption generateJsHighlighting = new BooleanGeneratorOption(true);

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final BooleanGeneratorOption generateServlet = new BooleanGeneratorOption(false);

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final BooleanGeneratorOption generateJettyLauncher = new BooleanGeneratorOption(false);

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final BooleanGeneratorOption generateWebXml = new BooleanGeneratorOption(false);

  @Accessors(AccessorType.PUBLIC_GETTER)
  private final BooleanGeneratorOption generateHtmlExample = new BooleanGeneratorOption(false);

  private String highlightingModuleName;

  private String highlightingPath;

  private String keywordsFilter = "\\w+";

  private boolean useServlet3Api = true;

  private boolean ignoreCase = false;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private String requireJsVersion = WebIntegrationFragment.REQUIREJS_VERSION;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private String requireJsTextVersion = WebIntegrationFragment.REQUIREJS_TEXT_VERSION;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private String jQueryVersion = WebIntegrationFragment.JQUERY_VERSION;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private String aceVersion = WebIntegrationFragment.ACE_VERSION;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private String codeMirrorVersion = WebIntegrationFragment.CODEMIRROR_VERSION;

  /**
   * Choose one of the supported frameworks: {@code "Orion"}, {@code "Ace"}, or {@code "CodeMirror"}
   */
  @Mandatory
  public void setFramework(final String frameworkName) {
    this.framework.set(WebIntegrationFragment.Framework.valueOf(frameworkName.toUpperCase()));
  }

  /**
   * Whether JavaScript-based syntax highlighting should be generated. The default is {@code true}.
   */
  public void setGenerateJsHighlighting(final boolean generateJsHighlighting) {
    this.generateJsHighlighting.set(generateJsHighlighting);
  }

  /**
   * Name of the syntax highlighting RequireJS module to be generated.
   */
  public void setHighlightingModuleName(final String moduleName) {
    this.highlightingModuleName = moduleName;
  }

  /**
   * The path of the syntax highlighting JavaScript file to be generated. The default is to
   * derive the path from the {@code highlightingModuleName} property.
   */
  public void setHighlightingPath(final String path) {
    this.highlightingPath = path;
  }

  /**
   * Regular expression for filtering those language keywords that should be highlighted. The default
   * is {@code \w+}, i.e. keywords consisting only of letters and digits.
   */
  public void setKeywordsFilter(final String keywordsFilter) {
    this.keywordsFilter = keywordsFilter;
  }

  /**
   * Whether a servlet for DSL-specific services should be generated. The default is {@code false}.
   */
  public void setGenerateServlet(final boolean generateServlet) {
    this.generateServlet.set(generateServlet);
  }

  /**
   * Whether a web.xml file should be generated. The default is {@code false} (not necessary for Servlet 3 compatible containers).
   */
  public void setGenerateWebXml(final boolean generateWebXml) {
    this.generateWebXml.set(generateWebXml);
  }

  /**
   * Whether the Servlet 3 API ({@code WebServlet} annotation) should be used for the generated servlet.
   * The default is {@code true}.
   */
  public void setUseServlet3Api(final boolean useServlet3Api) {
    this.useServlet3Api = useServlet3Api;
  }

  /**
   * Whether the generated syntax highlighting should ignore case for language keywords.
   */
  public void setIgnoreCase(final boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  /**
   * Whether a Java main-class for launching a local Jetty server should be generated. The default
   * is {@code false}.
   */
  public void setGenerateJettyLauncher(final boolean generateJettyLauncher) {
    this.generateJettyLauncher.set(generateJettyLauncher);
  }

  /**
   * Whether an example {@code index.html} file for testing the web-based editor should be generated.
   * The default is {@code false}.
   */
  public void setGenerateHtmlExample(final boolean generateHtmlExample) {
    this.generateHtmlExample.set(generateHtmlExample);
  }

  /**
   * Enable a default pattern for syntax highlighting. See the documentation of the chosen
   * framework for details.
   */
  public void addEnablePattern(final String pattern) {
    this.enabledPatterns.add(pattern);
  }

  /**
   * Suppress a default pattern for syntax highlighting. See the documentation of the chosen
   * framework for details.
   */
  public void addSuppressPattern(final String pattern) {
    this.suppressedPatterns.add(pattern);
  }

  protected TypeReference getServerLauncherClass(final Grammar grammar) {
    String _webBasePackage = this._xtextGeneratorNaming.getWebBasePackage(grammar);
    String _plus = (_webBasePackage + ".");
    String _plus_1 = (_plus + "ServerLauncher");
    return new TypeReference(_plus_1);
  }

  protected TypeReference getServletClass(final Grammar grammar) {
    String _webBasePackage = this._xtextGeneratorNaming.getWebBasePackage(grammar);
    String _plus = (_webBasePackage + ".");
    String _simpleName = GrammarUtil.getSimpleName(grammar);
    String _plus_1 = (_plus + _simpleName);
    String _plus_2 = (_plus_1 + "Servlet");
    return new TypeReference(_plus_2);
  }

  @Override
  public void checkConfiguration(final Issues issues) {
    super.checkConfiguration(issues);
    boolean _isSet = this.framework.isSet();
    boolean _not = (!_isSet);
    if (_not) {
      issues.addError("The property \'framework\' is required.");
    }
    final Function1<String, Boolean> _function = (String it) -> {
      return Boolean.valueOf(this.suppressedPatterns.contains(it));
    };
    Iterable<String> _filter = IterableExtensions.<String>filter(this.enabledPatterns, _function);
    for (final String pattern : _filter) {
      issues.addError((("The pattern \'" + pattern) + "\' cannot be enabled and suppressed."));
    }
  }

  @Override
  public void generate() {
    if (((this.highlightingModuleName != null) && this.highlightingModuleName.endsWith(".js"))) {
      int _length = this.highlightingModuleName.length();
      int _minus = (_length - 3);
      this.highlightingModuleName = this.highlightingModuleName.substring(0, _minus);
    }
    final String langId = IterableExtensions.<String>head(this.getLanguage().getFileExtensions());
    String _elvis = null;
    if (this.highlightingModuleName != null) {
      _elvis = this.highlightingModuleName;
    } else {
      String _switchResult = null;
      WebIntegrationFragment.Framework _get = this.framework.get();
      if (_get != null) {
        switch (_get) {
          case ORION:
            _switchResult = (("xtext-resources/generated/" + langId) + "-syntax");
            break;
          case ACE:
          case CODEMIRROR:
            _switchResult = ("xtext-resources/generated/mode-" + langId);
            break;
          default:
            break;
        }
      }
      _elvis = _switchResult;
    }
    final String hlModName = _elvis;
    if ((this.generateJsHighlighting.get() && (this.getProjectConfig().getWeb().getAssets() != null))) {
      boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(this.highlightingPath);
      if (_isNullOrEmpty) {
        this.highlightingPath = (hlModName + ".js");
      }
      this.generateJsHighlighting(langId);
    }
    if ((this.generateServlet.get() && (this.getProjectConfig().getWeb().getSrc() != null))) {
      this.generateServlet();
    }
    if ((this.generateJettyLauncher.get() && (this.getProjectConfig().getWeb().getSrc() != null))) {
      this.generateServerLauncher();
    }
    if ((this.generateHtmlExample.get() && (this.getProjectConfig().getWeb().getAssets() != null))) {
      this.generateIndexDoc(hlModName);
      this.generateStyleSheet();
    }
    if ((this.generateWebXml.get() && (this.getProjectConfig().getWeb().getAssets() != null))) {
      this.generateWebXml();
    }
  }

  private static final String DELIMITERS_PATTERN = new Function0<String>() {
    @Override
    public String apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("[\\\\s.:;,!?+\\\\-*/&|<>()[\\\\]{}]");
      return _builder.toString();
    }
  }.apply();

  protected void generateJsHighlighting(final String langId) {
    final Set<String> allKeywords = GrammarUtil.getAllKeywords(this.getGrammar());
    final ArrayList<String> wordKeywords = CollectionLiterals.<String>newArrayList();
    final ArrayList<String> nonWordKeywords = CollectionLiterals.<String>newArrayList();
    final Pattern keywordsFilterPattern = Pattern.compile(this.keywordsFilter);
    final Pattern wordKeywordPattern = Pattern.compile("\\w(.*\\w)?");
    final Function1<String, Boolean> _function = (String it) -> {
      return Boolean.valueOf(keywordsFilterPattern.matcher(it).matches());
    };
    final Consumer<String> _function_1 = (String it) -> {
      boolean _matches = wordKeywordPattern.matcher(it).matches();
      if (_matches) {
        wordKeywords.add(it);
      } else {
        nonWordKeywords.add(it);
      }
    };
    IterableExtensions.<String>filter(allKeywords, _function).forEach(_function_1);
    Collections.<String>sort(wordKeywords);
    Collections.<String>sort(nonWordKeywords);
    final TextFileAccess jsFile = this.fileAccessFactory.createTextFile();
    jsFile.setPath(this.highlightingPath);
    WebIntegrationFragment.Framework _get = this.framework.get();
    if (_get != null) {
      switch (_get) {
        case ORION:
          final Collection<String> patterns = this.createOrionPatterns(langId, allKeywords);
          boolean _isEmpty = wordKeywords.isEmpty();
          boolean _not = (!_isEmpty);
          if (_not) {
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("{name: \"keyword.");
            _builder.append(langId);
            _builder.append("\", match: ");
            CharSequence _generateKeywordsRegExp = this.generateKeywordsRegExp();
            _builder.append(_generateKeywordsRegExp);
            _builder.append("}");
            patterns.add(_builder.toString());
          }
          boolean _isEmpty_1 = nonWordKeywords.isEmpty();
          boolean _not_1 = (!_isEmpty_1);
          if (_not_1) {
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("{name: \"keyword.extra.");
            _builder_1.append(langId);
            _builder_1.append("\", match: ");
            CharSequence _generateExtraKeywordsRegExp = this.generateExtraKeywordsRegExp();
            _builder_1.append(_generateExtraKeywordsRegExp);
            _builder_1.append("}");
            patterns.add(_builder_1.toString());
          }
          StringConcatenationClient _client = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("define(");
              {
                boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(WebIntegrationFragment.this.highlightingModuleName);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                  _builder.append("\"");
                  _builder.append(WebIntegrationFragment.this.highlightingModuleName);
                  _builder.append("\", ");
                }
              }
              _builder.append("[], function() {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              CharSequence _generateKeywords = WebIntegrationFragment.this.generateKeywords(wordKeywords, nonWordKeywords);
              _builder.append(_generateKeywords, "\t");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("return {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("id: \"xtext.");
              _builder.append(langId, "\t\t");
              _builder.append("\",");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("contentTypes: [\"xtext/");
              _builder.append(langId, "\t\t");
              _builder.append("\"],");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("patterns: [");
              _builder.newLine();
              _builder.append("\t\t\t");
              {
                boolean _hasElements = false;
                for(final String pattern : patterns) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(",\n", "\t\t\t");
                  }
                  _builder.append(pattern, "\t\t\t");
                }
              }
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("]");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("};");
              _builder.newLine();
              _builder.append("});");
              _builder.newLine();
            }
          };
          jsFile.setContent(_client);
          break;
        case ACE:
          final Multimap<String, String> patterns_1 = this.createCodeMirrorPatterns(langId, allKeywords);
          boolean _isEmpty_2 = wordKeywords.isEmpty();
          boolean _not_2 = (!_isEmpty_2);
          if (_not_2) {
            StringConcatenation _builder_2 = new StringConcatenation();
            _builder_2.append("{token: \"keyword\", regex: ");
            CharSequence _generateKeywordsRegExp_1 = this.generateKeywordsRegExp();
            _builder_2.append(_generateKeywordsRegExp_1);
            _builder_2.append("}");
            patterns_1.put("start", _builder_2.toString());
          }
          boolean _isEmpty_3 = nonWordKeywords.isEmpty();
          boolean _not_3 = (!_isEmpty_3);
          if (_not_3) {
            StringConcatenation _builder_3 = new StringConcatenation();
            _builder_3.append("{token: \"keyword\", regex: ");
            CharSequence _generateExtraKeywordsRegExp_1 = this.generateExtraKeywordsRegExp();
            _builder_3.append(_generateExtraKeywordsRegExp_1);
            _builder_3.append("}");
            patterns_1.put("start", _builder_3.toString());
          }
          StringConcatenationClient _client_1 = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("define(");
              {
                boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(WebIntegrationFragment.this.highlightingModuleName);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                  _builder.append("\"");
                  _builder.append(WebIntegrationFragment.this.highlightingModuleName);
                  _builder.append("\", ");
                }
              }
              _builder.append("[\"ace/lib/oop\", \"ace/mode/text\", \"ace/mode/text_highlight_rules\"], function(oop, mText, mTextHighlightRules) {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("var HighlightRules = function() {");
              _builder.newLine();
              _builder.append("\t\t");
              CharSequence _generateKeywords = WebIntegrationFragment.this.generateKeywords(wordKeywords, nonWordKeywords);
              _builder.append(_generateKeywords, "\t\t");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("this.$rules = {");
              _builder.newLine();
              {
                Set<String> _keySet = patterns_1.keySet();
                boolean _hasElements = false;
                for(final String state : _keySet) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(",", "\t\t\t");
                  }
                  _builder.append("\t\t\t");
                  _builder.append("\"");
                  _builder.append(state, "\t\t\t");
                  _builder.append("\": [");
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t\t\t");
                  _builder.append("\t");
                  {
                    Collection<String> _get = patterns_1.get(state);
                    boolean _hasElements_1 = false;
                    for(final String rule : _get) {
                      if (!_hasElements_1) {
                        _hasElements_1 = true;
                      } else {
                        _builder.appendImmediate(",\n", "\t\t\t\t");
                      }
                      _builder.append(rule, "\t\t\t\t");
                    }
                  }
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t\t\t");
                  _builder.append("]");
                  _builder.newLine();
                }
              }
              _builder.append("\t\t");
              _builder.append("};");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("};");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("oop.inherits(HighlightRules, mTextHighlightRules.TextHighlightRules);");
              _builder.newLine();
              _builder.append("\t");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("var Mode = function() {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("this.HighlightRules = HighlightRules;");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("};");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("oop.inherits(Mode, mText.Mode);");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("Mode.prototype.$id = \"xtext/");
              _builder.append(langId, "\t");
              _builder.append("\";");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("Mode.prototype.getCompletions = function(state, session, pos, prefix) {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return [];");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("}");
              _builder.newLine();
              _builder.append("\t");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("return {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("Mode: Mode");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("};");
              _builder.newLine();
              _builder.append("});");
              _builder.newLine();
            }
          };
          jsFile.setContent(_client_1);
          break;
        case CODEMIRROR:
          final Multimap<String, String> patterns_2 = this.createCodeMirrorPatterns(langId, allKeywords);
          boolean _isEmpty_4 = wordKeywords.isEmpty();
          boolean _not_4 = (!_isEmpty_4);
          if (_not_4) {
            StringConcatenation _builder_4 = new StringConcatenation();
            _builder_4.append("{token: \"keyword\", regex: ");
            CharSequence _generateKeywordsRegExp_2 = this.generateKeywordsRegExp();
            _builder_4.append(_generateKeywordsRegExp_2);
            _builder_4.append("}");
            patterns_2.put("start", _builder_4.toString());
          }
          boolean _isEmpty_5 = nonWordKeywords.isEmpty();
          boolean _not_5 = (!_isEmpty_5);
          if (_not_5) {
            StringConcatenation _builder_5 = new StringConcatenation();
            _builder_5.append("{token: \"keyword\", regex: ");
            CharSequence _generateExtraKeywordsRegExp_2 = this.generateExtraKeywordsRegExp();
            _builder_5.append(_generateExtraKeywordsRegExp_2);
            _builder_5.append("}");
            patterns_2.put("start", _builder_5.toString());
          }
          StringConcatenationClient _client_2 = new StringConcatenationClient() {
            @Override
            protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
              _builder.append("define(");
              {
                boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(WebIntegrationFragment.this.highlightingModuleName);
                boolean _not = (!_isNullOrEmpty);
                if (_not) {
                  _builder.append("\"");
                  _builder.append(WebIntegrationFragment.this.highlightingModuleName);
                  _builder.append("\", ");
                }
              }
              _builder.append("[\"codemirror\", \"codemirror/addon/mode/simple\"], function(CodeMirror, SimpleMode) {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              CharSequence _generateKeywords = WebIntegrationFragment.this.generateKeywords(wordKeywords, nonWordKeywords);
              _builder.append(_generateKeywords, "\t");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("CodeMirror.defineSimpleMode(\"xtext/");
              _builder.append(langId, "\t");
              _builder.append("\", {");
              _builder.newLineIfNotEmpty();
              {
                Set<String> _keySet = patterns_2.keySet();
                boolean _hasElements = false;
                for(final String state : _keySet) {
                  if (!_hasElements) {
                    _hasElements = true;
                  } else {
                    _builder.appendImmediate(",", "\t\t");
                  }
                  _builder.append("\t\t");
                  _builder.append(state, "\t\t");
                  _builder.append(": ");
                  {
                    boolean _equals = Objects.equals(state, "meta");
                    if (_equals) {
                      _builder.append("{");
                    } else {
                      _builder.append("[");
                    }
                  }
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t\t");
                  _builder.append("\t");
                  {
                    Collection<String> _get = patterns_2.get(state);
                    boolean _hasElements_1 = false;
                    for(final String rule : _get) {
                      if (!_hasElements_1) {
                        _hasElements_1 = true;
                      } else {
                        _builder.appendImmediate(",\n", "\t\t\t");
                      }
                      _builder.append(rule, "\t\t\t");
                    }
                  }
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t\t");
                  {
                    boolean _equals_1 = Objects.equals(state, "meta");
                    if (_equals_1) {
                      _builder.append("}");
                    } else {
                      _builder.append("]");
                    }
                  }
                  _builder.newLineIfNotEmpty();
                }
              }
              _builder.append("\t");
              _builder.append("});");
              _builder.newLine();
              _builder.append("});");
              _builder.newLine();
            }
          };
          jsFile.setContent(_client_2);
          break;
        default:
          break;
      }
    }
    jsFile.writeTo(this.getProjectConfig().getWeb().getAssets());
  }

  protected CharSequence generateKeywords(final List<String> wordKeywords, final List<String> nonWordKeywords) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean _isEmpty = wordKeywords.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        _builder.append("var keywords = \"");
        {
          boolean _hasElements = false;
          for(final String kw : wordKeywords) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate("|", "");
            }
            String _regexpString = RegexpExtensions.toRegexpString(kw, ((!Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR)) && this.ignoreCase));
            _builder.append(_regexpString);
          }
        }
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
      }
    }
    {
      boolean _isEmpty_1 = nonWordKeywords.isEmpty();
      boolean _not_1 = (!_isEmpty_1);
      if (_not_1) {
        _builder.append("var extraKeywords = \"");
        {
          boolean _hasElements_1 = false;
          for(final String kw_1 : nonWordKeywords) {
            if (!_hasElements_1) {
              _hasElements_1 = true;
            } else {
              _builder.appendImmediate("|", "");
            }
            String _regexpString_1 = RegexpExtensions.toRegexpString(kw_1, ((!Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR)) && this.ignoreCase));
            _builder.append(_regexpString_1);
          }
        }
        _builder.append("\";");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }

  protected CharSequence generateKeywordsRegExp() {
    CharSequence _xifexpression = null;
    if ((Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR) && this.ignoreCase)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("new RegExp(\"\\\\b(?:\" + keywords + \")\\\\b\", \"gi\")");
      _xifexpression = _builder;
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("\"\\\\b(?:\" + keywords + \")\\\\b\"");
      _xifexpression = _builder_1;
    }
    return _xifexpression;
  }

  protected CharSequence generateExtraKeywordsRegExp() {
    CharSequence _xifexpression = null;
    if ((Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR) && this.ignoreCase)) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("new RegExp(\"(?:^|\\\\s)(?:\" + extraKeywords + \")(?=");
      _builder.append(WebIntegrationFragment.DELIMITERS_PATTERN);
      _builder.append("|$)\", \"gi\")");
      _xifexpression = _builder;
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("\"(?:^|\\\\s)(?:\" + extraKeywords + \")(?=");
      _builder_1.append(WebIntegrationFragment.DELIMITERS_PATTERN);
      _builder_1.append("|$)\"");
      _xifexpression = _builder_1;
    }
    return _xifexpression;
  }

  protected Collection<String> createOrionPatterns(final String langId, final Set<String> keywords) {
    final boolean inheritsTerminals = GrammarUtil2.inherits(this.getGrammar(), GrammarUtil2.TERMINALS);
    final boolean inheritsXbase = this._xbaseUsageDetector.inheritsXbase(this.getGrammar());
    final ArrayList<String> patterns = new ArrayList<String>();
    if ((this.enabledPatterns.contains("comment_singleLine") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("comment_singleLine"))))) {
      patterns.add("{include: \"orion.c-like#comment_singleLine\"}");
    }
    if ((this.enabledPatterns.contains("comment_block") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("comment_block"))))) {
      patterns.add("{include: \"orion.c-like#comment_block\"}");
    }
    if ((this.enabledPatterns.contains("string_doubleQuote") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("string_doubleQuote"))))) {
      patterns.add("{include: \"orion.lib#string_doubleQuote\"}");
    }
    if ((this.enabledPatterns.contains("string_singleQuote") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("string_singleQuote"))))) {
      patterns.add("{include: \"orion.lib#string_singleQuote\"}");
    }
    boolean _contains = this.enabledPatterns.contains("doc_block");
    if (_contains) {
      patterns.add("{include: \"orion.lib#doc_block\"}");
    }
    if ((this.enabledPatterns.contains("number_decimal") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("number_decimal"))))) {
      patterns.add("{include: \"orion.lib#number_decimal\"}");
    }
    if ((this.enabledPatterns.contains("number_hex") || (inheritsXbase && (!this.suppressedPatterns.contains("number_hex"))))) {
      patterns.add("{include: \"orion.lib#number_hex\"}");
    }
    if ((this.enabledPatterns.contains("brace_open") || (keywords.contains("{") && (!this.suppressedPatterns.contains("brace_open"))))) {
      patterns.add("{include: \"orion.lib#brace_open\"}");
    }
    if ((this.enabledPatterns.contains("brace_close") || (keywords.contains("}") && (!this.suppressedPatterns.contains("brace_close"))))) {
      patterns.add("{include: \"orion.lib#brace_close\"}");
    }
    if ((this.enabledPatterns.contains("bracket_open") || (keywords.contains("[") && (!this.suppressedPatterns.contains("bracket_open"))))) {
      patterns.add("{include: \"orion.lib#bracket_open\"}");
    }
    if ((this.enabledPatterns.contains("bracket_close") || (keywords.contains("]") && (!this.suppressedPatterns.contains("bracket_close"))))) {
      patterns.add("{include: \"orion.lib#bracket_close\"}");
    }
    if ((this.enabledPatterns.contains("parenthesis_open") || (keywords.contains("(") && (!this.suppressedPatterns.contains("parenthesis_open"))))) {
      patterns.add("{include: \"orion.lib#parenthesis_open\"}");
    }
    if ((this.enabledPatterns.contains("parenthesis_close") || (keywords.contains(")") && (!this.suppressedPatterns.contains("parenthesis_close"))))) {
      patterns.add("{include: \"orion.lib#parenthesis_close\"}");
    }
    return patterns;
  }

  protected Multimap<String, String> createCodeMirrorPatterns(final String langId, final Set<String> keywords) {
    final boolean inheritsTerminals = GrammarUtil2.inherits(this.getGrammar(), GrammarUtil2.TERMINALS);
    final boolean inheritsXbase = this._xbaseUsageDetector.inheritsXbase(this.getGrammar());
    final LinkedHashMultimap<String, String> patterns = LinkedHashMultimap.<String, String>create();
    final boolean hasSingleLineComment = (this.enabledPatterns.contains("comment_singleLine") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("comment_singleLine"))));
    if (hasSingleLineComment) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("{token: \"comment\", regex: \"\\\\/\\\\/.*$\"}");
      patterns.put("start", _builder.toString());
    }
    if ((this.enabledPatterns.contains("comment_block") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("comment_block"))))) {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("{token: \"comment\", regex: \"\\\\/\\\\*\", next : \"comment\"}");
      patterns.put("start", _builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("{token: \"comment\", regex: \".*?\\\\*\\\\/\", next : \"start\"}");
      patterns.put("comment", _builder_2.toString());
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("{token: \"comment\", regex: \".+\"}");
      patterns.put("comment", _builder_3.toString());
    }
    if ((this.enabledPatterns.contains("string_doubleQuote") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("string_doubleQuote"))))) {
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("{token: \"string\", regex: \'[\"](?:(?:\\\\\\\\.)|(?:[^\"\\\\\\\\]))*?[\"]\'}");
      patterns.put("start", _builder_4.toString());
    }
    if ((this.enabledPatterns.contains("string_singleQuote") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("string_singleQuote"))))) {
      StringConcatenation _builder_5 = new StringConcatenation();
      _builder_5.append("{token: \"string\", regex: \"[\'](?:(?:\\\\\\\\.)|(?:[^\'\\\\\\\\]))*?[\']\"}");
      patterns.put("start", _builder_5.toString());
    }
    if ((this.enabledPatterns.contains("number_decimal") || ((inheritsTerminals || inheritsXbase) && (!this.suppressedPatterns.contains("number_decimal"))))) {
      StringConcatenation _builder_6 = new StringConcatenation();
      _builder_6.append("{token: \"constant.numeric\", regex: \"[+-]?\\\\d+(?:(?:\\\\.\\\\d*)?(?:[eE][+-]?\\\\d+)?)?\\\\b\"}");
      patterns.put("start", _builder_6.toString());
    }
    if ((this.enabledPatterns.contains("number_hex") || (inheritsXbase && (!this.suppressedPatterns.contains("number_hex"))))) {
      StringConcatenation _builder_7 = new StringConcatenation();
      _builder_7.append("{token: \"constant.numeric\", regex: \"0[xX][0-9a-fA-F]+\\\\b\"}");
      patterns.put("start", _builder_7.toString());
    }
    final boolean bracketOpen = (this.enabledPatterns.contains("bracket_open") || (keywords.contains("[") && (!this.suppressedPatterns.contains("bracket_open"))));
    final boolean parenOpen = (this.enabledPatterns.contains("parenthesis_open") || (keywords.contains("(") && (!this.suppressedPatterns.contains("parenthesis_open"))));
    final boolean braceOpen = (this.enabledPatterns.contains("brace_open") || (keywords.contains("{") && (!this.suppressedPatterns.contains("brace_open"))));
    if (((bracketOpen || parenOpen) || braceOpen)) {
      StringConcatenation _builder_8 = new StringConcatenation();
      _builder_8.append("{token: \"lparen\", regex: \"[");
      {
        if (bracketOpen) {
          _builder_8.append("\\\\[");
        }
      }
      {
        if (parenOpen) {
          _builder_8.append("(");
        }
      }
      {
        if (braceOpen) {
          _builder_8.append("{");
        }
      }
      _builder_8.append("]\"}");
      patterns.put("start", _builder_8.toString());
    }
    final boolean bracketClose = (this.enabledPatterns.contains("bracket_close") || (keywords.contains("]") && (!this.suppressedPatterns.contains("bracket_close"))));
    final boolean parenClose = (this.enabledPatterns.contains("parenthesis_close") || (keywords.contains(")") && (!this.suppressedPatterns.contains("parenthesis_close"))));
    final boolean braceClose = (this.enabledPatterns.contains("brace_close") || (keywords.contains("}") && (!this.suppressedPatterns.contains("brace_close"))));
    if (((bracketClose || parenClose) || braceClose)) {
      StringConcatenation _builder_9 = new StringConcatenation();
      _builder_9.append("{token: \"rparen\", regex: \"[");
      {
        if (bracketClose) {
          _builder_9.append("\\\\]");
        }
      }
      {
        if (parenClose) {
          _builder_9.append(")");
        }
      }
      {
        if (braceClose) {
          _builder_9.append("}");
        }
      }
      _builder_9.append("]\"}");
      patterns.put("start", _builder_9.toString());
    }
    if ((Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR) && patterns.containsKey("comment"))) {
      StringConcatenation _builder_10 = new StringConcatenation();
      _builder_10.append("dontIndentStates: [\"comment\"]");
      patterns.put("meta", _builder_10.toString());
    }
    if ((Objects.equals(this.framework.get(), WebIntegrationFragment.Framework.CODEMIRROR) && hasSingleLineComment)) {
      StringConcatenation _builder_11 = new StringConcatenation();
      _builder_11.append("lineComment: \"//\"");
      patterns.put("meta", _builder_11.toString());
    }
    return patterns;
  }

  protected void generateIndexDoc(final String hlModName) {
    boolean _isFile = this.getProjectConfig().getWeb().getAssets().isFile("index.html");
    if (_isFile) {
      return;
    }
    final TextFileAccess indexFile = this.fileAccessFactory.createTextFile();
    indexFile.setPath("index.html");
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("<html>");
        _builder.newLine();
        _builder.append("<head>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<meta http-equiv=\"Content-Language\" content=\"en-us\">");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<title>Example Web Editor</title>");
        _builder.newLine();
        {
          WebIntegrationFragment.Framework _get = WebIntegrationFragment.this.framework.get();
          boolean _equals = Objects.equals(_get, WebIntegrationFragment.Framework.ORION);
          if (_equals) {
            _builder.append("\t");
            _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"orion/code_edit/built-codeEdit.css\"/>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"xtext/");
            XtextVersion _xtextVersion = WebIntegrationFragment.this.codeConfig.getXtextVersion();
            _builder.append(_xtextVersion, "\t");
            _builder.append("/xtext-orion.css\"/>");
            _builder.newLineIfNotEmpty();
          } else {
            WebIntegrationFragment.Framework _get_1 = WebIntegrationFragment.this.framework.get();
            boolean _equals_1 = Objects.equals(_get_1, WebIntegrationFragment.Framework.ACE);
            if (_equals_1) {
              _builder.append("\t");
              _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"xtext/");
              XtextVersion _xtextVersion_1 = WebIntegrationFragment.this.codeConfig.getXtextVersion();
              _builder.append(_xtextVersion_1, "\t");
              _builder.append("/xtext-ace.css\"/>");
              _builder.newLineIfNotEmpty();
            } else {
              WebIntegrationFragment.Framework _get_2 = WebIntegrationFragment.this.framework.get();
              boolean _equals_2 = Objects.equals(_get_2, WebIntegrationFragment.Framework.CODEMIRROR);
              if (_equals_2) {
                _builder.append("\t");
                _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"webjars/codemirror/");
                _builder.append(WebIntegrationFragment.this.codeMirrorVersion, "\t");
                _builder.append("/lib/codemirror.css\"/>");
                _builder.newLineIfNotEmpty();
                _builder.append("\t");
                _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"webjars/codemirror/");
                _builder.append(WebIntegrationFragment.this.codeMirrorVersion, "\t");
                _builder.append("/addon/hint/show-hint.css\"/>");
                _builder.newLineIfNotEmpty();
                _builder.append("\t");
                _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"xtext/");
                XtextVersion _xtextVersion_2 = WebIntegrationFragment.this.codeConfig.getXtextVersion();
                _builder.append(_xtextVersion_2, "\t");
                _builder.append("/xtext-codemirror.css\"/>");
                _builder.newLineIfNotEmpty();
              }
            }
          }
        }
        _builder.append("\t");
        _builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<script src=\"webjars/requirejs/");
        _builder.append(WebIntegrationFragment.this.requireJsVersion, "\t");
        _builder.append("/require.min.js\"></script>");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("<script type=\"text/javascript\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("var baseUrl = window.location.pathname;");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("var fileIndex = baseUrl.indexOf(\"index.html\");");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("if (fileIndex > 0)");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("baseUrl = baseUrl.slice(0, fileIndex);");
        _builder.newLine();
        {
          WebIntegrationFragment.Framework _get_3 = WebIntegrationFragment.this.framework.get();
          boolean _equals_3 = Objects.equals(_get_3, WebIntegrationFragment.Framework.ORION);
          if (_equals_3) {
            _builder.append("\t\t");
            _builder.append("require.config({");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("baseUrl: baseUrl,");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("paths: {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"text\": \"webjars/requirejs-text/");
            _builder.append(WebIntegrationFragment.this.requireJsTextVersion, "\t\t\t\t");
            _builder.append("/text\",");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"jquery\": \"webjars/jquery/");
            _builder.append(WebIntegrationFragment.this.jQueryVersion, "\t\t\t\t");
            _builder.append("/jquery.min\",");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("\"xtext/xtext-orion\": \"xtext/");
            XtextVersion _xtextVersion_3 = WebIntegrationFragment.this.codeConfig.getXtextVersion();
            _builder.append(_xtextVersion_3, "\t\t\t\t");
            _builder.append("/xtext-orion\"");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("});");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("require([\"orion/code_edit/built-codeEdit-amd\"], function() {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("require([\"xtext/xtext-orion\"], function(xtext) {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("xtext.createEditor({");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t\t\t");
            _builder.append("baseUrl: baseUrl,");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t\t\t");
            _builder.append("syntaxDefinition: \"");
            String _xifexpression = null;
            boolean _get_4 = WebIntegrationFragment.this.generateJsHighlighting.get();
            if (_get_4) {
              _xifexpression = hlModName;
            } else {
              _xifexpression = "none";
            }
            _builder.append(_xifexpression, "\t\t\t\t\t");
            _builder.append("\"");
            _builder.newLineIfNotEmpty();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("});");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("});");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("});");
            _builder.newLine();
          } else {
            WebIntegrationFragment.Framework _get_5 = WebIntegrationFragment.this.framework.get();
            boolean _equals_4 = Objects.equals(_get_5, WebIntegrationFragment.Framework.ACE);
            if (_equals_4) {
              _builder.append("\t\t");
              _builder.append("require.config({");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("baseUrl: baseUrl,");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("paths: {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\"jquery\": \"webjars/jquery/");
              _builder.append(WebIntegrationFragment.this.jQueryVersion, "\t\t\t\t");
              _builder.append("/jquery.min\",");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\"ace/ext/language_tools\": \"webjars/ace/");
              _builder.append(WebIntegrationFragment.this.aceVersion, "\t\t\t\t");
              _builder.append("/src/ext-language_tools\",");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("\"xtext/xtext-ace\": \"xtext/");
              XtextVersion _xtextVersion_4 = WebIntegrationFragment.this.codeConfig.getXtextVersion();
              _builder.append(_xtextVersion_4, "\t\t\t\t");
              _builder.append("/xtext-ace\"");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("}");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("});");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("require([\"webjars/ace/");
              _builder.append(WebIntegrationFragment.this.aceVersion, "\t\t");
              _builder.append("/src/ace\"], function() {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("require([\"xtext/xtext-ace\"], function(xtext) {");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("xtext.createEditor({");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("baseUrl: baseUrl,");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t\t\t");
              _builder.append("syntaxDefinition: \"");
              String _xifexpression_1 = null;
              boolean _get_6 = WebIntegrationFragment.this.generateJsHighlighting.get();
              if (_get_6) {
                _xifexpression_1 = hlModName;
              } else {
                _xifexpression_1 = "none";
              }
              _builder.append(_xifexpression_1, "\t\t\t\t\t");
              _builder.append("\"");
              _builder.newLineIfNotEmpty();
              _builder.append("\t\t");
              _builder.append("\t\t");
              _builder.append("});");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("\t");
              _builder.append("});");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("});");
              _builder.newLine();
            } else {
              WebIntegrationFragment.Framework _get_7 = WebIntegrationFragment.this.framework.get();
              boolean _equals_5 = Objects.equals(_get_7, WebIntegrationFragment.Framework.CODEMIRROR);
              if (_equals_5) {
                _builder.append("\t\t");
                _builder.append("require.config({");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("baseUrl: baseUrl,");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("paths: {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("\"jquery\": \"webjars/jquery/");
                _builder.append(WebIntegrationFragment.this.jQueryVersion, "\t\t\t\t");
                _builder.append("/jquery.min\",");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("\"xtext/xtext-codemirror\": \"xtext/");
                XtextVersion _xtextVersion_5 = WebIntegrationFragment.this.codeConfig.getXtextVersion();
                _builder.append(_xtextVersion_5, "\t\t\t\t");
                _builder.append("/xtext-codemirror\"");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("},");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("packages: [{");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("name: \"codemirror\",");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("location: \"webjars/codemirror/");
                _builder.append(WebIntegrationFragment.this.codeMirrorVersion, "\t\t\t\t");
                _builder.append("\",");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("main: \"lib/codemirror\"");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("}]");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("});");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("require([");
                {
                  boolean _get_8 = WebIntegrationFragment.this.generateJsHighlighting.get();
                  if (_get_8) {
                    _builder.append("\"");
                    _builder.append(hlModName, "\t\t");
                    _builder.append("\", ");
                  }
                }
                _builder.append("\"xtext/xtext-codemirror\"], function(");
                {
                  boolean _get_9 = WebIntegrationFragment.this.generateJsHighlighting.get();
                  if (_get_9) {
                    _builder.append("mode, ");
                  }
                }
                _builder.append("xtext) {");
                _builder.newLineIfNotEmpty();
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("xtext.createEditor({");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("baseUrl: baseUrl");
                {
                  boolean _get_10 = WebIntegrationFragment.this.generateJsHighlighting.get();
                  boolean _not = (!_get_10);
                  if (_not) {
                    _builder.append(",");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t\t");
                    _builder.append("\t\t");
                    _builder.append("syntaxDefinition: \"none\"");
                    _builder.newLine();
                  }
                }
                _builder.append("\t\t");
                _builder.append("\t");
                _builder.append("});");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("});");
                _builder.newLine();
              }
            }
          }
        }
        _builder.append("\t");
        _builder.append("</script>");
        _builder.newLine();
        _builder.append("</head>");
        _builder.newLine();
        _builder.append("<body>");
        _builder.newLine();
        _builder.newLine();
        _builder.append("<div class=\"container\">");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<div class=\"header\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<h1>Example ");
        String _simpleName = GrammarUtil.getSimpleName(WebIntegrationFragment.this.getGrammar());
        _builder.append(_simpleName, "\t\t");
        _builder.append(" Web Editor</h1>");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("</div>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<div class=\"content\">");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<div id=\"xtext-editor\" data-editor-xtext-lang=\"");
        String _head = IterableExtensions.<String>head(WebIntegrationFragment.this.getLanguage().getFileExtensions());
        _builder.append(_head, "\t\t");
        _builder.append("\"></div>");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("</div>");
        _builder.newLine();
        _builder.append("</div>");
        _builder.newLine();
        _builder.newLine();
        _builder.append("</body>");
        _builder.newLine();
        _builder.append("</html>");
        _builder.newLine();
      }
    };
    indexFile.setContent(_client);
    indexFile.writeTo(this.getProjectConfig().getWeb().getAssets());
  }

  protected void generateStyleSheet() {
    boolean _isFile = this.getProjectConfig().getWeb().getAssets().isFile("style.css");
    if (_isFile) {
      return;
    }
    final TextFileAccess styleFile = this.fileAccessFactory.createTextFile();
    styleFile.setPath("style.css");
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("body {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("width: 100%;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("height: 100%;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("overflow: hidden;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("font: 16px Helvetica,sans-serif;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("a {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("color: #22a;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("text-decoration: none;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("a:hover {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("text-decoration: underline;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append(".container {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("display: block;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("position: absolute;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("top: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("bottom: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("left: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("right: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("margin: 20px;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append(".header {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("display: block;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("position: absolute;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("background-color: #e8e8e8;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("top: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("left: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("right: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("height: 60px;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("padding: 10px;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append(".content {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("display: block;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("position: absolute;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("top: 90px;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("bottom: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("left: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("width: 640px;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("#xtext-editor {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("display: block;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("position: absolute;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("top: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("bottom: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("left: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("right: 0;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("padding: 4px;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("border: 1px solid #aaa;");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        {
          WebIntegrationFragment.Framework _get = WebIntegrationFragment.this.framework.get();
          boolean _equals = Objects.equals(_get, WebIntegrationFragment.Framework.ORION);
          if (_equals) {
            _builder.newLine();
            _builder.append("/************* Examples for custom icons *************/");
            _builder.newLine();
            _builder.newLine();
            _builder.append("/* For all elements of type Greeting or its subtypes */ ");
            _builder.newLine();
            _builder.append("/*");
            _builder.newLine();
            _builder.append("\t");
            _builder.append(".Greeting-icon {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("background-image: url(\'images/Greeting.gif\');");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("*/");
            _builder.newLine();
            _builder.newLine();
            _builder.append("/* Only in hovers */ ");
            _builder.newLine();
            _builder.append("/*");
            _builder.newLine();
            _builder.append("\t");
            _builder.append(".xtext-hover .Greeting-icon {");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("background-image: url(\'images/Greeting.gif\');");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
            _builder.append(" ");
            _builder.append("*/");
            _builder.newLine();
          }
        }
      }
    };
    styleFile.setContent(_client);
    styleFile.writeTo(this.getProjectConfig().getWeb().getAssets());
  }

  protected void generateServerLauncher() {
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _serverLauncherClass = this.getServerLauncherClass(this.getGrammar());
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* This program starts an HTTP server for testing the web integration of your DSL.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Just execute it and point a web browser to http://localhost:8080/");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("class ");
          String _simpleName = WebIntegrationFragment.this.getServerLauncherClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("static final ");
          TypeReference _typeRef = TypeReference.typeRef("org.slf4j.Logger");
          _builder.append(_typeRef, "\t");
          _builder.append(" LOG = ");
          TypeReference _typeRef_1 = TypeReference.typeRef("org.slf4j.LoggerFactory");
          _builder.append(_typeRef_1, "\t");
          _builder.append(".getLogger(");
          String _simpleName_1 = WebIntegrationFragment.this.getServerLauncherClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName_1, "\t");
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("def static void main(String[] args) {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("val server = new ");
          TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.jetty.server.Server");
          _builder.append(_typeRef_2, "\t\t");
          _builder.append("(new ");
          TypeReference _typeRef_3 = TypeReference.typeRef("java.net.InetSocketAddress");
          _builder.append(_typeRef_3, "\t\t");
          _builder.append("(\'localhost\', 8080))");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("server.handler = new ");
          TypeReference _typeRef_4 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebAppContext");
          _builder.append(_typeRef_4, "\t\t");
          _builder.append(" => [");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("baseResource = ");
          TypeReference _typeRef_5 = TypeReference.typeRef("org.eclipse.jetty.util.resource.ResourceFactory");
          _builder.append(_typeRef_5, "\t\t\t");
          _builder.append(".of(webAppContext).newResource(\'");
          String _path = WebIntegrationFragment.this.getProjectConfig().getWeb().getAssets().getPath();
          String _path_1 = WebIntegrationFragment.this.getProjectConfig().getWeb().getRoot().getPath();
          String _plus = (_path_1 + "/");
          String _replace = _path.replace(_plus, "");
          _builder.append(_replace, "\t\t\t");
          _builder.append("\')");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("welcomeFiles = #[\"index.html\"]");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("contextPath = \"/\"");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("configurations = #[");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_6 = TypeReference.typeRef("org.eclipse.jetty.ee10.annotations.AnnotationConfiguration");
          _builder.append(_typeRef_6, "\t\t\t\t");
          _builder.append(",");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_7 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebXmlConfiguration");
          _builder.append(_typeRef_7, "\t\t\t\t");
          _builder.append(",");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_8 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebInfConfiguration");
          _builder.append(_typeRef_8, "\t\t\t\t");
          _builder.append(",");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_9 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.MetaInfConfiguration");
          _builder.append(_typeRef_9, "\t\t\t\t");
          _builder.append(",");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_10 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebAppConfiguration");
          _builder.append(_typeRef_10, "\t\t\t\t");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("]");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("setAttribute(");
          TypeReference _typeRef_11 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.MetaInfConfiguration");
          _builder.append(_typeRef_11, "\t\t\t");
          _builder.append(".CONTAINER_JAR_PATTERN, \'.*/");
          String _replace_1 = WebIntegrationFragment.this.getProjectConfig().getWeb().getName().replace(".", "\\\\.");
          _builder.append(_replace_1, "\t\t\t");
          _builder.append("/.*,.*\\\\.jar\')");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("setInitParameter(\"org.eclipse.jetty.servlet.Default.useFileMappedBuffer\", \"false\")");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("]");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("try {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("server.start");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("LOG.info(\'Server started \' + server.getURI + \'...\')");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("new Thread[");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("LOG.info(\'Press enter to stop the server...\')");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("val key = System.in.read");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("if (key != -1) {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t");
          _builder.append("server.stop");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("} else {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t");
          _builder.append("LOG.warn(\'Console input is not available. In order to stop the server, you need to cancel process manually.\')");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("].start");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("server.join");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("} catch (Exception exception) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("LOG.warn(exception.message)");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("System.exit(1)");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      this.fileAccessFactory.createXtendFile(_serverLauncherClass, _client).writeTo(this.getProjectConfig().getWeb().getSrc());
    } else {
      TypeReference _serverLauncherClass_1 = this.getServerLauncherClass(this.getGrammar());
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* This program starts an HTTP server for testing the web integration of your DSL.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Just execute it and point a web browser to http://localhost:8080/");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          _builder.append("public class ");
          String _simpleName = WebIntegrationFragment.this.getServerLauncherClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("private static final ");
          TypeReference _typeRef = TypeReference.typeRef("org.slf4j.Logger");
          _builder.append(_typeRef, "\t");
          _builder.append(" LOG = ");
          TypeReference _typeRef_1 = TypeReference.typeRef("org.slf4j.LoggerFactory");
          _builder.append(_typeRef_1, "\t");
          _builder.append(".getLogger(");
          String _simpleName_1 = WebIntegrationFragment.this.getServerLauncherClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName_1, "\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public static void main(String[] args) {");
          _builder.newLine();
          _builder.append("\t\t");
          TypeReference _typeRef_2 = TypeReference.typeRef("org.eclipse.jetty.server.Server");
          _builder.append(_typeRef_2, "\t\t");
          _builder.append(" server = new ");
          TypeReference _typeRef_3 = TypeReference.typeRef("org.eclipse.jetty.server.Server");
          _builder.append(_typeRef_3, "\t\t");
          _builder.append("(new ");
          TypeReference _typeRef_4 = TypeReference.typeRef("java.net.InetSocketAddress");
          _builder.append(_typeRef_4, "\t\t");
          _builder.append("(\"localhost\", 8080));");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          TypeReference _typeRef_5 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebAppContext");
          _builder.append(_typeRef_5, "\t\t");
          _builder.append(" ctx = new ");
          TypeReference _typeRef_6 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebAppContext");
          _builder.append(_typeRef_6, "\t\t");
          _builder.append("();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("ctx.setBaseResource(");
          TypeReference _typeRef_7 = TypeReference.typeRef("org.eclipse.jetty.util.resource.ResourceFactory");
          _builder.append(_typeRef_7, "\t\t");
          _builder.append(".of(it).newResource(\"WebRoot\"));");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("ctx.setWelcomeFiles(new String[] {\"index.html\"});");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("ctx.setContextPath(\"/\");");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("ctx.setConfigurations(new ");
          TypeReference _typeRef_8 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.Configuration");
          _builder.append(_typeRef_8, "\t\t");
          _builder.append("[] {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_9 = TypeReference.typeRef("org.eclipse.jetty.ee10.annotations.AnnotationConfiguration");
          _builder.append(_typeRef_9, "\t\t\t");
          _builder.append("(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_10 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebXmlConfiguration");
          _builder.append(_typeRef_10, "\t\t\t");
          _builder.append("(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_11 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebInfConfiguration");
          _builder.append(_typeRef_11, "\t\t\t");
          _builder.append("(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_12 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.MetaInfConfiguration");
          _builder.append(_typeRef_12, "\t\t\t");
          _builder.append("(),");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("new ");
          TypeReference _typeRef_13 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.WebAppConfiguration");
          _builder.append(_typeRef_13, "\t\t\t");
          _builder.append("()");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("});");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("ctx.setAttribute(");
          TypeReference _typeRef_14 = TypeReference.typeRef("org.eclipse.jetty.ee10.webapp.MetaInfConfiguration");
          _builder.append(_typeRef_14, "\t\t");
          _builder.append(".CONTAINER_JAR_PATTERN,");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t\t");
          _builder.append("\".*/");
          String _replace = WebIntegrationFragment.this.getProjectConfig().getWeb().getName().replace(".", "\\\\.");
          _builder.append(_replace, "\t\t\t");
          _builder.append("/.*,.*\\\\.jar\");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("ctx.setInitParameter(\"org.eclipse.jetty.servlet.Default.useFileMappedBuffer\", \"false\");");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("server.setHandler(ctx);");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("try {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("server.start();");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("LOG.info(\"Server started \" + server.getURI() + \"...\");");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("new Thread() {");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("public void run() {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t");
          _builder.append("try {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("LOG.info(\"Press enter to stop the server...\");");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("int key = System.in.read();");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("if (key != -1) {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t\t");
          _builder.append("server.stop();");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("} else {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t\t");
          _builder.append("LOG.warn(");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t\t\t\t");
          _builder.append("\"Console input is not available. In order to stop the server, you need to cancel process manually.\");");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t\t\t\t");
          _builder.append("} catch (Exception e) {");
          _builder.newLine();
          _builder.append("\t\t\t\t\t\t");
          _builder.append("LOG.warn(e.getMessage());");
          _builder.newLine();
          _builder.append("\t\t\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("}.start();");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("server.join();");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("} catch (Exception exception) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("LOG.warn(exception.getMessage());");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("System.exit(1);");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      this.fileAccessFactory.createJavaFile(_serverLauncherClass_1, _client_1).writeTo(this.getProjectConfig().getWeb().getSrc());
    }
  }

  protected void generateServlet() {
    boolean _isPreferXtendStubs = this.codeConfig.isPreferXtendStubs();
    if (_isPreferXtendStubs) {
      TypeReference _servletClass = this.getServletClass(this.getGrammar());
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Deploy this class into a servlet container to enable DSL-specific services.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          {
            if (WebIntegrationFragment.this.useServlet3Api) {
              _builder.append("@");
              TypeReference _typeReference = new TypeReference("jakarta.servlet.annotation.WebServlet");
              _builder.append(_typeReference);
              _builder.append("(name = \'XtextServices\', urlPatterns = \'/xtext-service/*\')");
              _builder.newLineIfNotEmpty();
            }
          }
          _builder.append("class ");
          String _simpleName = WebIntegrationFragment.this.getServletClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.web.servlet.XtextServlet");
          _builder.append(_typeRef);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("static final long serialVersionUID = 1L");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append(DisposableRegistry.class, "\t");
          _builder.append(" disposableRegistry");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("override init() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("super.init()");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("val injector = new ");
          TypeReference _webSetup = WebIntegrationFragment.this._xtextGeneratorNaming.getWebSetup(WebIntegrationFragment.this.getGrammar());
          _builder.append(_webSetup, "\t\t");
          _builder.append("().createInjectorAndDoEMFRegistration()");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("disposableRegistry = injector.getInstance(");
          _builder.append(DisposableRegistry.class, "\t\t");
          _builder.append(")");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("override destroy() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("if (disposableRegistry !== null) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("disposableRegistry.dispose()");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("disposableRegistry = null");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("super.destroy()");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      this.fileAccessFactory.createXtendFile(_servletClass, _client).writeTo(this.getProjectConfig().getWeb().getSrc());
    } else {
      TypeReference _servletClass_1 = this.getServletClass(this.getGrammar());
      StringConcatenationClient _client_1 = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("/**");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("* Deploy this class into a servlet container to enable DSL-specific services.");
          _builder.newLine();
          _builder.append(" ");
          _builder.append("*/");
          _builder.newLine();
          {
            if (WebIntegrationFragment.this.useServlet3Api) {
              _builder.append("@");
              TypeReference _typeReference = new TypeReference("jakarta.servlet.annotation.WebServlet");
              _builder.append(_typeReference);
              _builder.append("(name = \"XtextServices\", urlPatterns = \"/xtext-service/*\")");
              _builder.newLineIfNotEmpty();
            }
          }
          _builder.append("public class ");
          String _simpleName = WebIntegrationFragment.this.getServletClass(WebIntegrationFragment.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.web.servlet.XtextServlet");
          _builder.append(_typeRef);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("private static final long serialVersionUID = 1L;");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append(DisposableRegistry.class, "\t");
          _builder.append(" disposableRegistry;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public void init() throws ");
          TypeReference _typeRef_1 = TypeReference.typeRef("jakarta.servlet.ServletException");
          _builder.append(_typeRef_1, "\t");
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("super.init();");
          _builder.newLine();
          _builder.append("\t\t");
          TypeReference _typeRef_2 = TypeReference.typeRef("com.google.inject.Injector");
          _builder.append(_typeRef_2, "\t\t");
          _builder.append(" injector = new ");
          TypeReference _webSetup = WebIntegrationFragment.this._xtextGeneratorNaming.getWebSetup(WebIntegrationFragment.this.getGrammar());
          _builder.append(_webSetup, "\t\t");
          _builder.append("().createInjectorAndDoEMFRegistration();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("this.disposableRegistry = injector.getInstance(");
          _builder.append(DisposableRegistry.class, "\t\t");
          _builder.append(".class);");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public void destroy() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("if (disposableRegistry != null) {");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("disposableRegistry.dispose();");
          _builder.newLine();
          _builder.append("\t\t\t");
          _builder.append("disposableRegistry = null;");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("super.destroy();");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
        }
      };
      this.fileAccessFactory.createJavaFile(_servletClass_1, _client_1).writeTo(this.getProjectConfig().getWeb().getSrc());
    }
  }

  protected void generateWebXml() {
    boolean _isFile = this.getProjectConfig().getWeb().getAssets().isFile("WEB-INF/web.xml");
    if (_isFile) {
      return;
    }
    final TextFileAccess xmlFile = this.fileAccessFactory.createTextFile();
    xmlFile.setPath("WEB-INF/web.xml");
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        _builder.newLine();
        _builder.append("<web-app version=\"");
        {
          if (WebIntegrationFragment.this.useServlet3Api) {
            _builder.append("3.0");
          } else {
            _builder.append("2.3");
          }
        }
        _builder.append("\">");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("<display-name>Xtext Example Application</display-name>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<description>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("This Example demonstrates the usage of Xtext with a servlet container.");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</description>");
        _builder.newLine();
        {
          boolean _get = WebIntegrationFragment.this.generateServlet.get();
          if (_get) {
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>XtextServices</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<description>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t\t");
            _builder.append("Back-end for the DSL-specific services of Xtext.");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("</description>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-class>");
            TypeReference _servletClass = WebIntegrationFragment.this.getServletClass(WebIntegrationFragment.this.getGrammar());
            _builder.append(_servletClass, "\t\t");
            _builder.append("</servlet-class>");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("</servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet-mapping>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>XtextServices</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<url-pattern>/xtext-service/*</url-pattern>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</servlet-mapping>");
            _builder.newLine();
          }
        }
        {
          if ((!WebIntegrationFragment.this.useServlet3Api)) {
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>XtextResourcesServlet</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-class>org.eclipse.xtext.web.servlet.XtextResourcesServlet</servlet-class>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet-mapping>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>XtextResourcesServlet</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<url-pattern>/xtext/*</url-pattern>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</servlet-mapping>");
            _builder.newLine();
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>WebjarsServlet</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-class>org.webjars.servlet.WebjarsServlet</servlet-class>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</servlet>");
            _builder.newLine();
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<servlet-mapping>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<servlet-name>WebjarsServlet</servlet-name>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<url-pattern>/webjars/*</url-pattern>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</servlet-mapping>");
            _builder.newLine();
          }
        }
        {
          boolean _get_1 = WebIntegrationFragment.this.generateHtmlExample.get();
          if (_get_1) {
            _builder.append("\t");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("<welcome-file-list>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("<welcome-file>index.html</welcome-file>");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("</welcome-file-list>");
            _builder.newLine();
          }
        }
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("<session-config>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("<session-timeout>30</session-timeout>");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("</session-config>");
        _builder.newLine();
        _builder.append("</web-app>");
        _builder.newLine();
      }
    };
    xmlFile.setContent(_client);
    xmlFile.writeTo(this.getProjectConfig().getWeb().getAssets());
  }

  @Pure
  public GeneratorOption<WebIntegrationFragment.Framework> getFramework() {
    return this.framework;
  }

  @Pure
  public BooleanGeneratorOption getGenerateJsHighlighting() {
    return this.generateJsHighlighting;
  }

  @Pure
  public BooleanGeneratorOption getGenerateServlet() {
    return this.generateServlet;
  }

  @Pure
  public BooleanGeneratorOption getGenerateJettyLauncher() {
    return this.generateJettyLauncher;
  }

  @Pure
  public BooleanGeneratorOption getGenerateWebXml() {
    return this.generateWebXml;
  }

  @Pure
  public BooleanGeneratorOption getGenerateHtmlExample() {
    return this.generateHtmlExample;
  }

  public void setRequireJsVersion(final String requireJsVersion) {
    this.requireJsVersion = requireJsVersion;
  }

  public void setRequireJsTextVersion(final String requireJsTextVersion) {
    this.requireJsTextVersion = requireJsTextVersion;
  }

  public void setJQueryVersion(final String jQueryVersion) {
    this.jQueryVersion = jQueryVersion;
  }

  public void setAceVersion(final String aceVersion) {
    this.aceVersion = aceVersion;
  }

  public void setCodeMirrorVersion(final String codeMirrorVersion) {
    this.codeMirrorVersion = codeMirrorVersion;
  }
}
