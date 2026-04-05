/**
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.parser.antlr;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Group;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.conversion.impl.AbstractIDValueConverter;
import org.eclipse.xtext.conversion.impl.IgnoreCaseIDValueConverter;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.parser.ITokenToStringConverter;
import org.eclipse.xtext.parser.antlr.AbstractAntlrParser;
import org.eclipse.xtext.parser.antlr.AbstractIndentationTokenSource;
import org.eclipse.xtext.parser.antlr.AntlrTokenDefProvider;
import org.eclipse.xtext.parser.antlr.AntlrTokenToStringConverter;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;
import org.eclipse.xtext.parser.antlr.ITokenDefProvider;
import org.eclipse.xtext.parser.antlr.IUnorderedGroupHelper;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.parser.antlr.LexerBindings;
import org.eclipse.xtext.parser.antlr.LexerProvider;
import org.eclipse.xtext.parser.antlr.UnorderedGroupHelper;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parsetree.reconstr.ITokenSerializer;
import org.eclipse.xtext.parsetree.reconstr.impl.IgnoreCaseKeywordSerializer;
import org.eclipse.xtext.serializer.tokens.IKeywordSerializer;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xtext.FlattenedGrammarAccess;
import org.eclipse.xtext.xtext.RuleFilter;
import org.eclipse.xtext.xtext.RuleNames;
import org.eclipse.xtext.xtext.generator.Issues;
import org.eclipse.xtext.xtext.generator.grammarAccess.GrammarAccessExtensions;
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory;
import org.eclipse.xtext.xtext.generator.model.GeneratedJavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;
import org.eclipse.xtext.xtext.generator.model.JavaFileAccess;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.util.BooleanGeneratorOption;
import org.eclipse.xtext.xtext.generator.util.SyntheticTerminalDetector;

@SuppressWarnings("all")
public class XtextAntlrGeneratorFragment2 extends AbstractAntlrGeneratorFragment2 {
  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean debugGrammar;

  private final BooleanGeneratorOption combinedGrammar = new BooleanGeneratorOption();

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean removeBacktrackingGuards;

  private int lookaheadThreshold;

  @Accessors(AccessorType.PUBLIC_SETTER)
  private boolean partialParsing;

  @Inject
  private AntlrGrammarGenerator productionGenerator;

  @Inject
  private AntlrContentAssistGrammarGenerator contentAssistGenerator;

  @Inject
  private AntlrDebugGrammarGenerator debugGenerator;

  @Inject
  private FileAccessFactory fileFactory;

  @Inject
  private GrammarNaming productionNaming;

  @Inject
  private ContentAssistGrammarNaming contentAssistNaming;

  @Inject
  @Extension
  private GrammarAccessExtensions grammarUtil;

  @Inject
  @Extension
  private SyntheticTerminalDetector _syntheticTerminalDetector;

  public void setCombinedGrammar(final boolean combinedGrammar) {
    this.combinedGrammar.set(combinedGrammar);
  }

  protected boolean isCombinedGrammar() {
    boolean _xifexpression = false;
    boolean _isSet = this.combinedGrammar.isSet();
    if (_isSet) {
      _xifexpression = this.combinedGrammar.get();
    } else {
      _xifexpression = (((!this.getOptions().isBacktrackLexer()) && (!this.getOptions().isIgnoreCase())) && (!this.hasSyntheticTerminalRule()));
    }
    return _xifexpression;
  }

  @Override
  protected void doGenerate() {
    Grammar _grammar = this.getGrammar();
    boolean _isIgnoreCase = this.getOptions().isIgnoreCase();
    new KeywordHelper(_grammar, _isIgnoreCase, this.grammarUtil);
    boolean _isCombinedGrammar = this.isCombinedGrammar();
    new CombinedGrammarMarker(_isCombinedGrammar).attachToEmfObject(this.getGrammar());
    if (this.debugGrammar) {
      this.generateDebugGrammar();
    }
    this.generateProductionGrammar();
    IXtextGeneratorFileSystemAccess _srcGen = this.getProjectConfig().getGenericIde().getSrcGen();
    boolean _tripleNotEquals = (_srcGen != null);
    if (_tripleNotEquals) {
      this.generateContentAssistGrammar();
      this.addIdeBindingsAndImports();
    }
    this.generateProductionParser().writeTo(this.getProjectConfig().getRuntime().getSrcGen());
    this.generateAntlrTokenFileProvider().writeTo(this.getProjectConfig().getRuntime().getSrcGen());
    this.generateContentAssistParser().writeTo(this.getProjectConfig().getGenericIde().getSrcGen());
    boolean _hasSyntheticTerminalRule = this.hasSyntheticTerminalRule();
    if (_hasSyntheticTerminalRule) {
      this.generateProductionTokenSource().writeTo(this.getProjectConfig().getRuntime().getSrc());
      this.generateContentAssistTokenSource().writeTo(this.getProjectConfig().getGenericIde().getSrc());
    }
    this.addRuntimeBindingsAndImports();
    this.addIdeBindingsAndImports();
    this.addUiBindingsAndImports();
  }

  protected boolean hasSyntheticTerminalRule() {
    final Function1<TerminalRule, Boolean> _function = (TerminalRule it) -> {
      return Boolean.valueOf(this._syntheticTerminalDetector.isSyntheticTerminalRule(it));
    };
    return IterableExtensions.<TerminalRule>exists(GrammarUtil.allTerminalRules(this.getGrammar()), _function);
  }

  public void setLookaheadThreshold(final String lookaheadThreshold) {
    this.lookaheadThreshold = Integer.parseInt(lookaheadThreshold);
  }

  protected void generateProductionGrammar() {
    @Extension
    final GrammarNaming naming = this.productionNaming;
    final IXtextGeneratorFileSystemAccess fsa = this.getProjectConfig().getRuntime().getSrcGen();
    this.productionGenerator.generate(this.getGrammar(), this.getOptions(), fsa);
    this.runAntlr(naming.getParserGrammar(this.getGrammar()), naming.getLexerGrammar(this.getGrammar()), fsa);
    this.simplifyUnorderedGroupPredicatesIfRequired(this.getGrammar(), fsa, naming.getInternalParserClass(this.getGrammar()));
    this.splitParserAndLexerIfEnabled(fsa, naming.getInternalParserClass(this.getGrammar()), naming.getLexerClass(this.getGrammar()));
    this.normalizeTokens(fsa, naming.getLexerGrammar(this.getGrammar()).getTokensFileName());
    this.suppressWarnings(fsa, naming.getInternalParserClass(this.getGrammar()), naming.getLexerClass(this.getGrammar()));
    this.normalizeLineDelimiters(fsa, naming.getInternalParserClass(this.getGrammar()), naming.getLexerClass(this.getGrammar()));
  }

  protected void generateContentAssistGrammar() {
    @Extension
    final ContentAssistGrammarNaming naming = this.contentAssistNaming;
    final IXtextGeneratorFileSystemAccess fsa = this.getProjectConfig().getGenericIde().getSrcGen();
    this.contentAssistGenerator.generate(this.getGrammar(), this.getOptions(), fsa);
    this.runAntlr(naming.getParserGrammar(this.getGrammar()), naming.getLexerGrammar(this.getGrammar()), fsa);
    this.simplifyUnorderedGroupPredicatesIfRequired(this.getGrammar(), fsa, naming.getInternalParserClass(this.getGrammar()));
    this.splitParserAndLexerIfEnabled(fsa, naming.getInternalParserClass(this.getGrammar()), naming.getLexerClass(this.getGrammar()));
    this.normalizeTokens(fsa, naming.getLexerGrammar(this.getGrammar()).getTokensFileName());
    this.suppressWarnings(fsa, naming.getInternalParserClass(this.getGrammar()), naming.getLexerClass(this.getGrammar()));
    this.normalizeLineDelimiters(fsa, naming.getLexerClass(this.getGrammar()), naming.getInternalParserClass(this.getGrammar()));
    if (this.removeBacktrackingGuards) {
      this.removeBackTrackingGuards(fsa, naming.getInternalParserClass(this.getGrammar()), this.lookaheadThreshold);
    }
  }

  protected void runAntlr(final AntlrGrammar parserGrammar, final AntlrGrammar lexerGrammar, final IXtextGeneratorFileSystemAccess fsa) {
    final String encoding = this.getCodeConfig().getEncoding();
    StringConcatenation _builder = new StringConcatenation();
    String _path = fsa.getPath();
    _builder.append(_path);
    _builder.append("/");
    String _grammarFileName = lexerGrammar.getGrammarFileName();
    _builder.append(_grammarFileName);
    final String lexerGrammarFile = _builder.toString();
    final ArrayList<String> lexerAntlrParams = CollectionLiterals.<String>newArrayList(this.getAntlrParams());
    lexerAntlrParams.add("-fo");
    final String lexerOutputDir = lexerGrammarFile.substring(0, lexerGrammarFile.lastIndexOf("/"));
    lexerAntlrParams.add(lexerOutputDir);
    boolean _isCombinedGrammar = this.isCombinedGrammar();
    boolean _not = (!_isCombinedGrammar);
    if (_not) {
      this.getAntlrTool().runWithEncodingAndParams(lexerGrammarFile, encoding, ((String[])Conversions.unwrapArray(lexerAntlrParams, String.class)));
      this.cleanupLexerTokensFile(lexerGrammar, KeywordHelper.getHelper(this.getGrammar()), fsa);
    }
    StringConcatenation _builder_1 = new StringConcatenation();
    String _path_1 = fsa.getPath();
    _builder_1.append(_path_1);
    _builder_1.append("/");
    String _grammarFileName_1 = parserGrammar.getGrammarFileName();
    _builder_1.append(_grammarFileName_1);
    final String parserGrammarFile = _builder_1.toString();
    final ArrayList<String> parserAntlrParams = CollectionLiterals.<String>newArrayList(this.getAntlrParams());
    parserAntlrParams.add("-fo");
    String _substring = parserGrammarFile.substring(0, parserGrammarFile.lastIndexOf("/"));
    parserAntlrParams.add(_substring);
    boolean _isCombinedGrammar_1 = this.isCombinedGrammar();
    boolean _not_1 = (!_isCombinedGrammar_1);
    if (_not_1) {
      parserAntlrParams.add("-lib");
      parserAntlrParams.add(lexerOutputDir);
    }
    this.getAntlrTool().runWithEncodingAndParams(parserGrammarFile, encoding, ((String[])Conversions.unwrapArray(parserAntlrParams, String.class)));
    boolean _isCombinedGrammar_2 = this.isCombinedGrammar();
    boolean _not_2 = (!_isCombinedGrammar_2);
    if (_not_2) {
      this.cleanupParserTokensFile(lexerGrammar, parserGrammar, KeywordHelper.getHelper(this.getGrammar()), fsa);
    }
  }

  protected void generateDebugGrammar() {
    final IXtextGeneratorFileSystemAccess fsa = this.getProjectConfig().getRuntime().getSrcGen();
    this.debugGenerator.generate(this.getGrammar(), this.getOptions(), fsa);
  }

  public JavaFileAccess generateProductionParser() {
    GeneratedJavaFileAccess _xblockexpression = null;
    {
      @Extension
      final GrammarNaming naming = this.productionNaming;
      final GeneratedJavaFileAccess file = this.fileFactory.createGeneratedJavaFile(naming.getParserClass(this.getGrammar()));
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = naming.getParserClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          _builder.append(AbstractAntlrParser.class);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@");
          _builder.append(Inject.class, "\t");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("private ");
          TypeReference _grammarAccess = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_grammarAccess, "\t");
          _builder.append(" grammarAccess;");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected void setInitialHiddenTokens(");
          _builder.append(XtextTokenStream.class, "\t");
          _builder.append(" tokenStream) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("tokenStream.setInitialHiddenTokens(");
          {
            List<String> _initialHiddenTokens = XtextAntlrGeneratorFragment2.this.grammarUtil.initialHiddenTokens(XtextAntlrGeneratorFragment2.this.getGrammar());
            boolean _hasElements = false;
            for(final String hidden : _initialHiddenTokens) {
              if (!_hasElements) {
                _hasElements = true;
              } else {
                _builder.appendImmediate(", ", "\t\t");
              }
              _builder.append("\"");
              _builder.append(hidden, "\t\t");
              _builder.append("\"");
            }
          }
          _builder.append(");");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          {
            boolean _hasSyntheticTerminalRule = XtextAntlrGeneratorFragment2.this.hasSyntheticTerminalRule();
            if (_hasSyntheticTerminalRule) {
              _builder.append("\t");
              _builder.append("@Override");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("protected ");
              _builder.append(TokenSource.class, "\t");
              _builder.append(" createLexer(");
              _builder.append(CharStream.class, "\t");
              _builder.append(" stream) {");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("\t");
              _builder.append("return new ");
              TypeReference _tokenSourceClass = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_tokenSourceClass, "\t\t");
              _builder.append("(super.createLexer(stream));");
              _builder.newLineIfNotEmpty();
              _builder.append("\t");
              _builder.append("}");
              _builder.newLine();
              _builder.append("\t");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("/**");
              _builder.newLine();
              _builder.append("\t");
              _builder.append(" ");
              _builder.append("* Indentation aware languages do not support partial parsing since the lexer is inherently stateful.");
              _builder.newLine();
              _builder.append("\t");
              _builder.append(" ");
              _builder.append("* Override and return {@code true} if your terminal splitting is stateless.");
              _builder.newLine();
              _builder.append("\t");
              _builder.append(" ");
              _builder.append("*/");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("@Override");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("protected boolean isReparseSupported() {");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("\t");
              _builder.append("return false;");
              _builder.newLine();
              _builder.append("\t");
              _builder.append("}");
              _builder.newLine();
            }
          }
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected ");
          TypeReference _internalParserClass = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_internalParserClass, "\t");
          _builder.append(" createParser(");
          _builder.append(XtextTokenStream.class, "\t");
          _builder.append(" stream) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return new ");
          TypeReference _internalParserClass_1 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_internalParserClass_1, "\t\t");
          _builder.append("(stream, getGrammarAccess());");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override ");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected String getDefaultRuleName() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("return \"");
          String _name = AntlrGrammarGenUtil.<ParserRule>getOriginalElement(IterableExtensions.<ParserRule>head(GrammarUtil.allParserRules(XtextAntlrGeneratorFragment2.this.getGrammar()))).getName();
          _builder.append(_name, "\t\t");
          _builder.append("\";");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          TypeReference _grammarAccess_1 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_grammarAccess_1, "\t");
          _builder.append(" getGrammarAccess() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return this.grammarAccess;");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public void setGrammarAccess(");
          TypeReference _grammarAccess_2 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_grammarAccess_2, "\t");
          _builder.append(" grammarAccess) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("this.grammarAccess = grammarAccess;");
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

  public JavaFileAccess generateAntlrTokenFileProvider() {
    GeneratedJavaFileAccess _xblockexpression = null;
    {
      @Extension
      final GrammarNaming naming = this.productionNaming;
      final GeneratedJavaFileAccess file = this.fileFactory.createGeneratedJavaFile(naming.getAntlrTokenFileProviderClass(this.getGrammar()));
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = naming.getAntlrTokenFileProviderClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" implements ");
          _builder.append(IAntlrTokenFileProvider.class);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          _builder.append(InputStream.class, "\t");
          _builder.append(" getAntlrTokenFile() {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append(ClassLoader.class, "\t\t");
          _builder.append(" classLoader = getClass().getClassLoader();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return classLoader.getResourceAsStream(\"");
          String _tokensFileName = naming.getParserGrammar(XtextAntlrGeneratorFragment2.this.getGrammar()).getTokensFileName();
          _builder.append(_tokensFileName, "\t\t");
          _builder.append("\");");
          _builder.newLineIfNotEmpty();
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

  public JavaFileAccess generateProductionTokenSource() {
    JavaFileAccess _xblockexpression = null;
    {
      @Extension
      final GrammarNaming naming = this.productionNaming;
      final JavaFileAccess file = this.fileFactory.createJavaFile(naming.getTokenSourceClass(this.getGrammar()));
      final Function1<TerminalRule, Boolean> _function = (TerminalRule it) -> {
        return Boolean.valueOf(Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("BEGIN", "INDENT", "OPEN")).contains(it.getName().toUpperCase()));
      };
      final Iterable<TerminalRule> open = IterableExtensions.<TerminalRule>filter(GrammarUtil.allTerminalRules(this.getGrammar()), _function);
      final Function1<TerminalRule, Boolean> _function_1 = (TerminalRule it) -> {
        return Boolean.valueOf(Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("END", "DEDENT", "CLOSE")).contains(it.getName().toUpperCase()));
      };
      final Iterable<TerminalRule> close = IterableExtensions.<TerminalRule>filter(GrammarUtil.allTerminalRules(this.getGrammar()), _function_1);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          _builder.append(AbstractIndentationTokenSource.class);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          String _simpleName_1 = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName_1, "\t");
          _builder.append("(");
          _builder.append(TokenSource.class, "\t");
          _builder.append(" delegate) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("super(delegate);");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected boolean shouldSplitTokenImpl(");
          _builder.append(Token.class, "\t");
          _builder.append(" token) {");
          _builder.newLineIfNotEmpty();
          {
            final Function1<TerminalRule, TerminalRule> _function = (TerminalRule it) -> {
              return AntlrGrammarGenUtil.<TerminalRule>getOriginalElement(it);
            };
            final Function1<TerminalRule, Boolean> _function_1 = (TerminalRule it) -> {
              String _upperCase = it.getName().toUpperCase();
              return Boolean.valueOf(Objects.equals(_upperCase, "WS"));
            };
            boolean _exists = IterableExtensions.<TerminalRule>exists(ListExtensions.<TerminalRule, TerminalRule>map(GrammarUtil.allTerminalRules(XtextAntlrGeneratorFragment2.this.getGrammar()), _function), _function_1);
            if (_exists) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return token.getType() == ");
              TypeReference _internalParserClass = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass, "\t\t");
              _builder.append(".RULE_WS;");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected int getBeginTokenType() {");
          _builder.newLine();
          {
            int _size = IterableExtensions.size(open);
            boolean _equals = (_size == 1);
            if (_equals) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return ");
              TypeReference _internalParserClass_1 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass_1, "\t\t");
              _builder.append(".");
              String _ruleName = XtextAntlrGeneratorFragment2.this.grammarUtil.ruleName(IterableExtensions.<TerminalRule>head(open));
              _builder.append(_ruleName, "\t\t");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected int getEndTokenType() {");
          _builder.newLine();
          {
            int _size_1 = IterableExtensions.size(close);
            boolean _equals_1 = (_size_1 == 1);
            if (_equals_1) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return ");
              TypeReference _internalParserClass_2 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass_2, "\t\t");
              _builder.append(".");
              String _ruleName_1 = XtextAntlrGeneratorFragment2.this.grammarUtil.ruleName(IterableExtensions.<TerminalRule>head(close));
              _builder.append(_ruleName_1, "\t\t");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
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

  public JavaFileAccess generateContentAssistParser() {
    @Extension
    final ContentAssistGrammarNaming naming = this.contentAssistNaming;
    final GeneratedJavaFileAccess file = this.fileFactory.createGeneratedJavaFile(naming.getParserClass(this.getGrammar()));
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("public class ");
        String _simpleName = naming.getParserClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
        _builder.append(_simpleName);
        _builder.append(" extends ");
        TypeReference _parserSuperClass = naming.getParserSuperClass(XtextAntlrGeneratorFragment2.this.getGrammar(), XtextAntlrGeneratorFragment2.this.partialParsing);
        _builder.append(_parserSuperClass);
        _builder.append(" {");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        StringConcatenationClient _initNameMappings = XtextAntlrGeneratorFragment2.this.initNameMappings(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_initNameMappings, "\t");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@");
        _builder.append(Inject.class, "\t");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("private ");
        TypeReference _grammarAccess = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_grammarAccess, "\t");
        _builder.append(" grammarAccess;");
        _builder.newLineIfNotEmpty();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected ");
        TypeReference _internalParserClass = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_internalParserClass, "\t");
        _builder.append(" createParser() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        TypeReference _internalParserClass_1 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_internalParserClass_1, "\t\t");
        _builder.append(" result = new ");
        TypeReference _internalParserClass_2 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_internalParserClass_2, "\t\t");
        _builder.append("(null);");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("result.setGrammarAccess(grammarAccess);");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("return result;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        {
          boolean _hasSyntheticTerminalRule = XtextAntlrGeneratorFragment2.this.hasSyntheticTerminalRule();
          if (_hasSyntheticTerminalRule) {
            _builder.append("\t");
            _builder.append("@Override");
            _builder.newLine();
            _builder.append("\t");
            _builder.append("protected ");
            _builder.append(TokenSource.class, "\t");
            _builder.append(" createLexer(");
            _builder.append(CharStream.class, "\t");
            _builder.append(" stream) {");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("\t");
            _builder.append("return new ");
            TypeReference _tokenSourceClass = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar());
            _builder.append(_tokenSourceClass, "\t\t");
            _builder.append("(super.createLexer(stream));");
            _builder.newLineIfNotEmpty();
            _builder.append("\t");
            _builder.append("}");
            _builder.newLine();
            _builder.append("\t");
            _builder.newLine();
          }
        }
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected String getRuleName(");
        _builder.append(AbstractElement.class, "\t");
        _builder.append(" element) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return nameMappings.getRuleName(element);");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("@Override");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("protected String[] getInitialHiddenTokens() {");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("return new String[] { ");
        {
          List<String> _initialHiddenTokens = XtextAntlrGeneratorFragment2.this.grammarUtil.initialHiddenTokens(XtextAntlrGeneratorFragment2.this.getGrammar());
          boolean _hasElements = false;
          for(final String hidden : _initialHiddenTokens) {
            if (!_hasElements) {
              _hasElements = true;
            } else {
              _builder.appendImmediate(", ", "\t\t");
            }
            _builder.append("\"");
            _builder.append(hidden, "\t\t");
            _builder.append("\"");
          }
        }
        _builder.append(" };");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public ");
        TypeReference _grammarAccess_1 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_grammarAccess_1, "\t");
        _builder.append(" getGrammarAccess() {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("return this.grammarAccess;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void setGrammarAccess(");
        TypeReference _grammarAccess_2 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_grammarAccess_2, "\t");
        _builder.append(" grammarAccess) {");
        _builder.newLineIfNotEmpty();
        _builder.append("\t\t");
        _builder.append("this.grammarAccess = grammarAccess;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public NameMappings getNameMappings() {");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("return nameMappings;");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("}");
        _builder.newLine();
        _builder.append("\t");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("public void setNameMappings(NameMappings nameMappings) {");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("this.nameMappings = nameMappings;");
        _builder.newLine();
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

  /**
   * @since 2.14
   */
  protected StringConcatenationClient initNameMappings(final List<AbstractElement> partition) {
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        {
          for(final AbstractElement element : partition) {
            _builder.append("builder.put(grammarAccess.");
            String _grammarElementAccess = XtextAntlrGeneratorFragment2.this.grammarUtil.grammarElementAccess(AntlrGrammarGenUtil.<AbstractElement>getOriginalElement(element));
            _builder.append(_grammarElementAccess);
            _builder.append(", \"");
            String _contentAssistRuleName = AntlrGrammarGenUtil.getContentAssistRuleName(GrammarUtil.containingRule(AntlrGrammarGenUtil.<AbstractElement>getOriginalElement(element)));
            _builder.append(_contentAssistRuleName);
            _builder.append("__");
            String _gaElementIdentifier = XtextAntlrGeneratorFragment2.this.grammarUtil.gaElementIdentifier(AntlrGrammarGenUtil.<AbstractElement>getOriginalElement(element));
            _builder.append(_gaElementIdentifier);
            {
              if ((element instanceof Group)) {
                _builder.append("__0");
              }
            }
            _builder.append("\");");
            _builder.newLineIfNotEmpty();
          }
        }
      }
    };
    return _client;
  }

  /**
   * @since 2.14
   */
  protected StringConcatenationClient initNameMappings(final Grammar it) {
    StringConcatenationClient _xblockexpression = null;
    {
      final RuleFilter filter = new RuleFilter();
      filter.setDiscardUnreachableRules(this.getOptions().isSkipUnusedRules());
      final RuleNames ruleNames = RuleNames.getRuleNames(it, true);
      final Grammar flattened = new FlattenedGrammarAccess(ruleNames, filter).getFlattenedGrammar();
      final Set<AbstractElement> seenElements = CollectionLiterals.<AbstractElement>newHashSet();
      Collection<? extends AbstractElement> _allAlternatives = GrammarUtil.getAllAlternatives(flattened);
      Collection<? extends AbstractElement> _allGroups = GrammarUtil.getAllGroups(flattened);
      Iterable<AbstractElement> _plus = Iterables.<AbstractElement>concat(_allAlternatives, _allGroups);
      Collection<? extends AbstractElement> _allAssignments = GrammarUtil.getAllAssignments(flattened);
      Iterable<AbstractElement> _plus_1 = Iterables.<AbstractElement>concat(_plus, _allAssignments);
      Collection<? extends AbstractElement> _allUnorderedGroups = GrammarUtil.getAllUnorderedGroups(flattened);
      final Function1<AbstractElement, Boolean> _function = (AbstractElement it_1) -> {
        return Boolean.valueOf(seenElements.add(AntlrGrammarGenUtil.<AbstractElement>getOriginalElement(it_1)));
      };
      final List<AbstractElement> elements = IterableExtensions.<AbstractElement>toList(IterableExtensions.<AbstractElement>filter(Iterables.<AbstractElement>filter((Iterables.<AbstractElement>concat(_plus_1, _allUnorderedGroups)), AbstractElement.class), _function));
      final List<List<AbstractElement>> partitions = Lists.<AbstractElement>partition(elements, 2500);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("@");
          _builder.append(Singleton.class);
          _builder.newLineIfNotEmpty();
          _builder.append("public static final class NameMappings {");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          {
            int _size = partitions.size();
            boolean _greaterThan = (_size > 1);
            if (_greaterThan) {
              {
                Iterable<Pair<Integer, List<AbstractElement>>> _indexed = IterableExtensions.<List<AbstractElement>>indexed(partitions);
                for(final Pair<Integer, List<AbstractElement>> partition : _indexed) {
                  _builder.append("\t");
                  _builder.append("private static final class Init");
                  Integer _key = partition.getKey();
                  _builder.append(_key, "\t");
                  _builder.append(" {");
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t");
                  _builder.append("\t");
                  _builder.append("private static void doInit(");
                  _builder.append(ImmutableMap.class, "\t\t");
                  _builder.append(".Builder<");
                  _builder.append(AbstractElement.class, "\t\t");
                  _builder.append(", ");
                  _builder.append(String.class, "\t\t");
                  _builder.append("> builder, ");
                  TypeReference _grammarAccess = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
                  _builder.append(_grammarAccess, "\t\t");
                  _builder.append(" grammarAccess) {");
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t");
                  _builder.append("\t\t");
                  StringConcatenationClient _initNameMappings = XtextAntlrGeneratorFragment2.this.initNameMappings(partition.getValue());
                  _builder.append(_initNameMappings, "\t\t\t");
                  _builder.newLineIfNotEmpty();
                  _builder.append("\t");
                  _builder.append("\t");
                  _builder.append("}");
                  _builder.newLine();
                  _builder.append("\t");
                  _builder.append("}");
                  _builder.newLine();
                  _builder.append("\t");
                  _builder.newLine();
                }
              }
            }
          }
          _builder.append("\t");
          _builder.append("private final ");
          _builder.append(Map.class, "\t");
          _builder.append("<");
          _builder.append(AbstractElement.class, "\t");
          _builder.append(", ");
          _builder.append(String.class, "\t");
          _builder.append("> mappings;");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@");
          _builder.append(Inject.class, "\t");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("public NameMappings(");
          TypeReference _grammarAccess_1 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_grammarAccess_1, "\t");
          _builder.append(" grammarAccess) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append(ImmutableMap.class, "\t\t");
          _builder.append(".Builder<");
          _builder.append(AbstractElement.class, "\t\t");
          _builder.append(", ");
          _builder.append(String.class, "\t\t");
          _builder.append("> builder = ");
          _builder.append(ImmutableMap.class, "\t\t");
          _builder.append(".builder();");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("init(builder, grammarAccess);");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("this.mappings = builder.build();");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          _builder.append(String.class, "\t");
          _builder.append(" getRuleName(");
          _builder.append(AbstractElement.class, "\t");
          _builder.append(" element) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("return mappings.get(element);");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("\t");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("private static void init(");
          _builder.append(ImmutableMap.class, "\t");
          _builder.append(".Builder<");
          _builder.append(AbstractElement.class, "\t");
          _builder.append(", ");
          _builder.append(String.class, "\t");
          _builder.append("> builder, ");
          TypeReference _grammarAccess_2 = XtextAntlrGeneratorFragment2.this.grammarUtil.getGrammarAccess(XtextAntlrGeneratorFragment2.this.getGrammar());
          _builder.append(_grammarAccess_2, "\t");
          _builder.append(" grammarAccess) {");
          _builder.newLineIfNotEmpty();
          {
            int _size_1 = partitions.size();
            boolean _greaterThan_1 = (_size_1 > 1);
            if (_greaterThan_1) {
              {
                Iterable<Pair<Integer, List<AbstractElement>>> _indexed_1 = IterableExtensions.<List<AbstractElement>>indexed(partitions);
                for(final Pair<Integer, List<AbstractElement>> partition_1 : _indexed_1) {
                  _builder.append("\t\t");
                  _builder.append("Init");
                  Integer _key_1 = partition_1.getKey();
                  _builder.append(_key_1, "\t\t");
                  _builder.append(".doInit(builder, grammarAccess);");
                  _builder.newLineIfNotEmpty();
                }
              }
            } else {
              {
                for(final List<AbstractElement> partition_2 : partitions) {
                  _builder.append("\t\t");
                  StringConcatenationClient _initNameMappings_1 = XtextAntlrGeneratorFragment2.this.initNameMappings(partition_2);
                  _builder.append(_initNameMappings_1, "\t\t");
                  _builder.newLineIfNotEmpty();
                }
              }
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("@");
          _builder.append(Inject.class);
          _builder.newLineIfNotEmpty();
          _builder.append("private NameMappings nameMappings;");
          _builder.newLine();
        }
      };
      _xblockexpression = _client;
    }
    return _xblockexpression;
  }

  public JavaFileAccess generateContentAssistTokenSource() {
    JavaFileAccess _xblockexpression = null;
    {
      @Extension
      final ContentAssistGrammarNaming naming = this.contentAssistNaming;
      final JavaFileAccess file = this.fileFactory.createJavaFile(naming.getTokenSourceClass(this.getGrammar()));
      final Function1<TerminalRule, Boolean> _function = (TerminalRule it) -> {
        return Boolean.valueOf(Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("BEGIN", "INDENT", "OPEN")).contains(it.getName().toUpperCase()));
      };
      final Iterable<TerminalRule> open = IterableExtensions.<TerminalRule>filter(GrammarUtil.allTerminalRules(this.getGrammar()), _function);
      final Function1<TerminalRule, Boolean> _function_1 = (TerminalRule it) -> {
        return Boolean.valueOf(Collections.<String>unmodifiableSet(CollectionLiterals.<String>newHashSet("END", "DEDENT", "CLOSE")).contains(it.getName().toUpperCase()));
      };
      final Iterable<TerminalRule> close = IterableExtensions.<TerminalRule>filter(GrammarUtil.allTerminalRules(this.getGrammar()), _function_1);
      StringConcatenationClient _client = new StringConcatenationClient() {
        @Override
        protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
          _builder.append("public class ");
          String _simpleName = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName);
          _builder.append(" extends ");
          _builder.append(AbstractIndentationTokenSource.class);
          _builder.append(" {");
          _builder.newLineIfNotEmpty();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("public ");
          String _simpleName_1 = naming.getTokenSourceClass(XtextAntlrGeneratorFragment2.this.getGrammar()).getSimpleName();
          _builder.append(_simpleName_1, "\t");
          _builder.append("(");
          _builder.append(TokenSource.class, "\t");
          _builder.append(" delegate) {");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("super(delegate);");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected boolean shouldSplitTokenImpl(");
          _builder.append(Token.class, "\t");
          _builder.append(" token) {");
          _builder.newLineIfNotEmpty();
          {
            final Function1<TerminalRule, TerminalRule> _function = (TerminalRule it) -> {
              return AntlrGrammarGenUtil.<TerminalRule>getOriginalElement(it);
            };
            final Function1<TerminalRule, Boolean> _function_1 = (TerminalRule it) -> {
              String _upperCase = it.getName().toUpperCase();
              return Boolean.valueOf(Objects.equals(_upperCase, "WS"));
            };
            boolean _exists = IterableExtensions.<TerminalRule>exists(ListExtensions.<TerminalRule, TerminalRule>map(GrammarUtil.allTerminalRules(XtextAntlrGeneratorFragment2.this.getGrammar()), _function), _function_1);
            if (_exists) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return token.getType() == ");
              TypeReference _internalParserClass = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass, "\t\t");
              _builder.append(".RULE_WS;");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected int getBeginTokenType() {");
          _builder.newLine();
          {
            int _size = IterableExtensions.size(open);
            boolean _equals = (_size == 1);
            if (_equals) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return ");
              TypeReference _internalParserClass_1 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass_1, "\t\t");
              _builder.append(".");
              String _ruleName = XtextAntlrGeneratorFragment2.this.grammarUtil.ruleName(IterableExtensions.<TerminalRule>head(open));
              _builder.append(_ruleName, "\t\t");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected int getEndTokenType() {");
          _builder.newLine();
          {
            int _size_1 = IterableExtensions.size(close);
            boolean _equals_1 = (_size_1 == 1);
            if (_equals_1) {
              _builder.append("\t\t");
              _builder.append("// TODO Review assumption");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("return ");
              TypeReference _internalParserClass_2 = naming.getInternalParserClass(XtextAntlrGeneratorFragment2.this.getGrammar());
              _builder.append(_internalParserClass_2, "\t\t");
              _builder.append(".");
              String _ruleName_1 = XtextAntlrGeneratorFragment2.this.grammarUtil.ruleName(IterableExtensions.<TerminalRule>head(close));
              _builder.append(_ruleName_1, "\t\t");
              _builder.append(";");
              _builder.newLineIfNotEmpty();
            } else {
              _builder.append("\t\t");
              _builder.append("// TODO Implement me");
              _builder.newLine();
              _builder.append("\t\t");
              _builder.append("throw new UnsupportedOperationException(\"Implement me\");");
              _builder.newLine();
            }
          }
          _builder.append("\t");
          _builder.append("}");
          _builder.newLine();
          _builder.newLine();
          _builder.append("\t");
          _builder.append("@Override");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("protected boolean shouldEmitPendingEndTokens() {");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("return false;");
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

  @Override
  public void checkConfiguration(final Issues issues) {
    super.checkConfiguration(issues);
    if ((this.getOptions().isBacktrackLexer() && this.isCombinedGrammar())) {
      issues.addError("A combined grammar cannot have a backtracking lexer");
    }
    if ((this.getOptions().isIgnoreCase() && this.isCombinedGrammar())) {
      issues.addError("A combined grammar cannot have an ignorecase lexer");
    }
    if ((this.getOptions().isBacktrackLexer() && this.getOptions().isIgnoreCase())) {
      issues.addError("Backtracking lexer and ignorecase cannot be combined for now.");
    }
  }

  protected void addRuntimeBindingsAndImports() {
    @Extension
    final GrammarNaming naming = this.productionNaming;
    ManifestAccess _manifest = this.getProjectConfig().getRuntime().getManifest();
    boolean _tripleNotEquals = (_manifest != null);
    if (_tripleNotEquals) {
      ManifestAccess _manifest_1 = this.getProjectConfig().getRuntime().getManifest();
      final Procedure1<ManifestAccess> _function = (ManifestAccess it) -> {
        Set<String> _exportedPackages = it.getExportedPackages();
        String _packageName = naming.getLexerClass(this.getGrammar()).getPackageName();
        String _packageName_1 = naming.getParserClass(this.getGrammar()).getPackageName();
        String _packageName_2 = naming.getInternalParserClass(this.getGrammar()).getPackageName();
        Iterables.<String>addAll(_exportedPackages, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_packageName, _packageName_1, _packageName_2)));
        Set<String> _requiredBundles = it.getRequiredBundles();
        _requiredBundles.add("org.antlr.runtime;bundle-version=\"[3.5.3,4.5.4)\"");
      };
      ObjectExtensions.<ManifestAccess>operator_doubleArrow(_manifest_1, _function);
    }
    GuiceModuleAccess.BindingFactory _addTypeToType = new GuiceModuleAccess.BindingFactory().addTypeToType(TypeReference.typeRef(IParser.class), naming.getParserClass(this.getGrammar())).addTypeToType(TypeReference.typeRef(ITokenToStringConverter.class), TypeReference.typeRef(AntlrTokenToStringConverter.class)).addTypeToType(TypeReference.typeRef(IAntlrTokenFileProvider.class), naming.getAntlrTokenFileProviderClass(this.getGrammar())).addTypeToType(naming.getLexerSuperClass(this.getGrammar()), naming.getLexerClass(this.getGrammar())).addTypeToType(TypeReference.typeRef(ITokenDefProvider.class), TypeReference.typeRef(AntlrTokenDefProvider.class));
    TypeReference _lexerClass = naming.getLexerClass(this.getGrammar());
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append(LexerProvider.class);
        _builder.append(".create(");
        TypeReference _lexerClass = naming.getLexerClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerClass);
        _builder.append(".class)");
      }
    };
    GuiceModuleAccess.BindingFactory _addTypeToProviderInstance = _addTypeToType.addTypeToProviderInstance(_lexerClass, _client);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        _builder.append(Lexer.class);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(");
        _builder.append(LexerBindings.class, "\t");
        _builder.append(".RUNTIME))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        TypeReference _lexerClass = naming.getLexerClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerClass, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    final GuiceModuleAccess.BindingFactory rtBindings = _addTypeToProviderInstance.addConfiguredBinding("RuntimeLexer", _client_1);
    boolean _containsUnorderedGroup = this.containsUnorderedGroup(this.getGrammar());
    if (_containsUnorderedGroup) {
      rtBindings.addTypeToType(TypeReference.typeRef(IUnorderedGroupHelper.class), TypeReference.typeRef(UnorderedGroupHelper.class));
    }
    boolean _isIgnoreCase = this.getOptions().isIgnoreCase();
    if (_isIgnoreCase) {
      rtBindings.addTypeToType(TypeReference.typeRef(ITokenSerializer.IKeywordSerializer.class), TypeReference.typeRef(IgnoreCaseKeywordSerializer.class)).addTypeToType(TypeReference.typeRef(IKeywordSerializer.class), TypeReference.typeRef(org.eclipse.xtext.serializer.tokens.IgnoreCaseKeywordSerializer.class)).addTypeToType(TypeReference.typeRef(AbstractIDValueConverter.class), TypeReference.typeRef(IgnoreCaseIDValueConverter.class));
    }
    rtBindings.contributeTo(this.getLanguage().getRuntimeGenModule());
  }

  protected void addIdeBindingsAndImports() {
    @Extension
    final ContentAssistGrammarNaming naming = this.contentAssistNaming;
    ManifestAccess _manifest = this.getProjectConfig().getGenericIde().getManifest();
    boolean _tripleNotEquals = (_manifest != null);
    if (_tripleNotEquals) {
      ManifestAccess _manifest_1 = this.getProjectConfig().getGenericIde().getManifest();
      final Procedure1<ManifestAccess> _function = (ManifestAccess it) -> {
        Set<String> _exportedPackages = it.getExportedPackages();
        String _packageName = naming.getLexerClass(this.getGrammar()).getPackageName();
        String _packageName_1 = naming.getParserClass(this.getGrammar()).getPackageName();
        String _packageName_2 = naming.getInternalParserClass(this.getGrammar()).getPackageName();
        Iterables.<String>addAll(_exportedPackages, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_packageName, _packageName_1, _packageName_2)));
        Set<String> _requiredBundles = it.getRequiredBundles();
        _requiredBundles.add("org.antlr.runtime;bundle-version=\"[3.5.3,4.5.4)\"");
      };
      ObjectExtensions.<ManifestAccess>operator_doubleArrow(_manifest_1, _function);
    }
    GuiceModuleAccess.BindingFactory _bindingFactory = new GuiceModuleAccess.BindingFactory();
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        TypeReference _lexerSuperClass = naming.getLexerSuperClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerSuperClass);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ide.LexerIdeBindings");
        _builder.append(_typeRef, "\t");
        _builder.append(".CONTENT_ASSIST))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        TypeReference _lexerClass = naming.getLexerClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerClass, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    final GuiceModuleAccess.BindingFactory ideBindings = _bindingFactory.addConfiguredBinding("ContentAssistLexer", _client).addTypeToType(TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.antlr.IContentAssistParser"), naming.getParserClass(this.getGrammar())).addTypeToType(
      TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.IProposalConflictHelper"), 
      TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.antlr.AntlrProposalConflictHelper"));
    if (this.partialParsing) {
      ideBindings.addTypeToType(
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory"), 
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.antlr.PartialContentAssistContextFactory"));
    }
    boolean _hasSyntheticTerminalRule = this.hasSyntheticTerminalRule();
    if (_hasSyntheticTerminalRule) {
      ideBindings.addTypeToType(
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.CompletionPrefixProvider"), 
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.IndentationAwareCompletionPrefixProvider"));
    }
    ideBindings.contributeTo(this.getLanguage().getIdeGenModule());
  }

  protected void addUiBindingsAndImports() {
    @Extension
    final ContentAssistGrammarNaming naming = this.contentAssistNaming;
    final TypeReference caLexerClass = naming.getLexerClass(this.getGrammar());
    ManifestAccess _manifest = this.getProjectConfig().getGenericIde().getManifest();
    boolean _tripleNotEquals = (_manifest != null);
    if (_tripleNotEquals) {
      ManifestAccess _manifest_1 = this.getProjectConfig().getGenericIde().getManifest();
      final Procedure1<ManifestAccess> _function = (ManifestAccess it) -> {
        Set<String> _exportedPackages = it.getExportedPackages();
        String _packageName = caLexerClass.getPackageName();
        String _packageName_1 = naming.getParserClass(this.getGrammar()).getPackageName();
        String _packageName_2 = naming.getInternalParserClass(this.getGrammar()).getPackageName();
        Iterables.<String>addAll(_exportedPackages, Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList(_packageName, _packageName_1, _packageName_2)));
      };
      ObjectExtensions.<ManifestAccess>operator_doubleArrow(_manifest_1, _function);
    }
    GuiceModuleAccess.BindingFactory _addTypeToType = new GuiceModuleAccess.BindingFactory().addTypeToType(
      TypeReference.typeRef("org.eclipse.xtext.ui.editor.contentassist.IProposalConflictHelper"), 
      TypeReference.typeRef("org.eclipse.xtext.ui.editor.contentassist.antlr.AntlrProposalConflictHelper"));
    StringConcatenationClient _client = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        TypeReference _lexerSuperClass = naming.getLexerSuperClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerSuperClass);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ide.LexerIdeBindings");
        _builder.append(_typeRef, "\t");
        _builder.append(".CONTENT_ASSIST))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        _builder.append(caLexerClass, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    GuiceModuleAccess.BindingFactory _addConfiguredBinding = _addTypeToType.addConfiguredBinding("ContentAssistLexer", _client);
    StringConcatenationClient _client_1 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        _builder.append(Lexer.class);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ide.LexerIdeBindings");
        _builder.append(_typeRef, "\t");
        _builder.append(".HIGHLIGHTING))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        TypeReference _lexerClass = XtextAntlrGeneratorFragment2.this.productionNaming.getLexerClass(XtextAntlrGeneratorFragment2.this.getGrammar());
        _builder.append(_lexerClass, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    GuiceModuleAccess.BindingFactory _addConfiguredBinding_1 = _addConfiguredBinding.addConfiguredBinding("HighlightingLexer", _client_1);
    StringConcatenationClient _client_2 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        _builder.append(ITokenDefProvider.class);
        _builder.append(".class)");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".annotatedWith(");
        _builder.append(Names.class, "\t");
        _builder.append(".named(");
        TypeReference _typeRef = TypeReference.typeRef("org.eclipse.xtext.ide.LexerIdeBindings");
        _builder.append(_typeRef, "\t");
        _builder.append(".HIGHLIGHTING))");
        _builder.newLineIfNotEmpty();
        _builder.append("\t");
        _builder.append(".to(");
        _builder.append(AntlrTokenDefProvider.class, "\t");
        _builder.append(".class);");
        _builder.newLineIfNotEmpty();
      }
    };
    GuiceModuleAccess.BindingFactory _addConfiguredBinding_2 = _addConfiguredBinding_1.addConfiguredBinding("HighlightingTokenDefProvider", _client_2);
    TypeReference _typeReference = new TypeReference("org.eclipse.xtext.ui.editor.contentassist", "ContentAssistContext.Factory");
    GuiceModuleAccess.BindingFactory _addTypeToType_1 = _addConfiguredBinding_2.addTypeToType(_typeReference, 
      TypeReference.typeRef("org.eclipse.xtext.ui.editor.contentassist.antlr.DelegatingContentAssistContextFactory")).addTypeToType(
      TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.antlr.IContentAssistParser"), 
      naming.getParserClass(this.getGrammar()));
    StringConcatenationClient _client_3 = new StringConcatenationClient() {
      @Override
      protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
        _builder.append("binder.bind(");
        _builder.append(caLexerClass);
        _builder.append(".class).toProvider(");
        _builder.append(LexerProvider.class);
        _builder.append(".create(");
        _builder.append(caLexerClass);
        _builder.append(".class));");
        _builder.newLineIfNotEmpty();
      }
    };
    final GuiceModuleAccess.BindingFactory uiBindings = _addTypeToType_1.addConfiguredBinding("ContentAssistLexerProvider", _client_3);
    boolean _hasSyntheticTerminalRule = this.hasSyntheticTerminalRule();
    if (_hasSyntheticTerminalRule) {
      uiBindings.addTypeToType(
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.CompletionPrefixProvider"), 
        TypeReference.typeRef("org.eclipse.xtext.ide.editor.contentassist.IndentationAwareCompletionPrefixProvider"));
    }
    uiBindings.contributeTo(this.getLanguage().getEclipsePluginGenModule());
  }

  public void setDebugGrammar(final boolean debugGrammar) {
    this.debugGrammar = debugGrammar;
  }

  public void setRemoveBacktrackingGuards(final boolean removeBacktrackingGuards) {
    this.removeBacktrackingGuards = removeBacktrackingGuards;
  }

  public void setPartialParsing(final boolean partialParsing) {
    this.partialParsing = partialParsing;
  }
}
