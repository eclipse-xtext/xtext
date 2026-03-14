/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.parser.antlr;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.UnorderedGroup;
import org.eclipse.xtext.generator.LineSeparatorHarmonizer;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.CodeConfig;
import org.eclipse.xtext.xtext.generator.Issues;
import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.parser.antlr.postProcessing.SuppressWarningsProcessor;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.AntlrCodeQualityHelper;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.AntlrLexerSplitter;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.AntlrParserSplitter;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.BacktrackingGuardForUnorderedGroupsRemover;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.BacktrackingGuardRemover;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.PartialClassExtractor;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.SyntacticPredicateFixup;
import org.eclipse.xtext.xtext.generator.parser.antlr.splitting.UnorderedGroupsSplitter;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public abstract class AbstractAntlrGeneratorFragment2 extends AbstractXtextGeneratorFragment {
	private AntlrToolFacade antlrTool;

	@Inject
	private AntlrCodeQualityHelper codeQualityHelper;

	@Inject
	private LineSeparatorHarmonizer newLineNormalizer;

	@Inject
	private CodeConfig codeConfig;

	private AntlrOptions options = new AntlrOptions();

	private ArrayList<String> antlrParams = new ArrayList<>();

	private String dowloadUrl;
	
	@Inject
	private void setAntlrToolFacade(AntlrToolFacade antlrTool) {
		if (dowloadUrl != null) {
			antlrTool.setSecureDownloadFrom(dowloadUrl);
		}
		this.antlrTool = antlrTool;
	}

	public void addAntlrParam(String param) {
		antlrParams.add(param);
	}

	public String[] getAntlrParams() {
		ArrayList<String> params = Lists.newArrayList(antlrParams);
		// setting the default conversion timeout to 100secs.
		// There seem to be no practical situations where the NFA conversion
		// would hang,
		// so Terence suggested here [1] to remove the option all together
		// [1] -
		// http://antlr.1301665.n2.nabble.com/Xconversiontimeout-td5294411.html
		if (!params.contains("-Xconversiontimeout")) {
			params.add(0, "-Xconversiontimeout");
			params.add(1, "100000");
		}
		return params.toArray(new String[0]);
	}

	@Override
	public void checkConfiguration(Issues issues) {
		super.checkConfiguration(issues);
		if (!antlrTool.isWorkable()) {
			issues.addError(
					"\n\n*ATTENTION*\nIt is required to use ANTLR's parser generator (get it from 'https://artifacts.itemis.cloud/repository/p2/antlr-generator-3.2.0-patch.jar').");
		}
	}

	@Override
	public void generate() {
		checkGrammar();
		doGenerate();
	}

	protected abstract void doGenerate();

	protected void checkGrammar() {
		if (!hasProductionRules(getGrammar())) {
			throw new IllegalArgumentException(
					"You may not generate an ANTLR parser for a grammar without production rules.");
		}
	}

	protected boolean hasProductionRules(Grammar grammar) {
		AbstractRule firstRule = grammar.getRules().get(0);
		return firstRule instanceof ParserRule && !GrammarUtil.isDatatypeRule((ParserRule) firstRule);
	}

	protected void splitLexerClassFile(IXtextGeneratorFileSystemAccess fsa, TypeReference lexer) {
		String content = fsa.readTextFile(lexer.getJavaPath()).toString();
		AntlrLexerSplitter splitter = new AntlrLexerSplitter(content);
		splitter.setCasesPerSpecialStateSwitch(options.getCasesPerSpecialStateSwitch());
		fsa.generateFile(lexer.getJavaPath(), splitter.transform());
	}

	protected void splitParserClassFile(IXtextGeneratorFileSystemAccess fsa, TypeReference parser) {
		String content = fsa.readTextFile(parser.getJavaPath()).toString();
		AntlrParserSplitter splitter = new AntlrParserSplitter(content, getOptions().getFieldsPerClass());
		PartialClassExtractor extractor = new PartialClassExtractor(splitter.transform(), getOptions().getMethodsPerClass());
		fsa.generateFile(parser.getJavaPath(), extractor.transform());
	}

	protected void simplifyUnorderedGroupPredicatesIfRequired(Grammar grammar, IXtextGeneratorFileSystemAccess fsa,
			TypeReference parser) {
		if (containsUnorderedGroup(grammar) || hasParameterizedRules(grammar)) {
			simplifyUnorderedGroupPredicates(fsa, parser);
		}
	}

	protected boolean hasParameterizedRules(Grammar grammar) {
		for (ParserRule rule : GrammarUtil.allParserRules(grammar)) {
			if (!rule.getParameters().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	protected void simplifyUnorderedGroupPredicates(IXtextGeneratorFileSystemAccess fsa, TypeReference parser) {
		String content = fsa.readTextFile(parser.getJavaPath()).toString();
		UnorderedGroupsSplitter splitter = new UnorderedGroupsSplitter(content);
		String transformed = splitter.transform();
		SyntacticPredicateFixup fixup = new SyntacticPredicateFixup(transformed);
		transformed = fixup.transform();
		BacktrackingGuardForUnorderedGroupsRemover remover = new BacktrackingGuardForUnorderedGroupsRemover(
				transformed);
		String newContent = remover.transform();
		fsa.generateFile(parser.getJavaPath(), newContent);
	}

	protected void suppressWarnings(IXtextGeneratorFileSystemAccess fsa, TypeReference type) {
		String content = fsa.readTextFile(type.getJavaPath()).toString();
		String newContent = new SuppressWarningsProcessor().process(content);
		fsa.generateFile(type.getJavaPath(), newContent);
	}

	protected void suppressWarnings(IXtextGeneratorFileSystemAccess fsa, TypeReference... types) {
		if (types != null) {
			for (TypeReference it : types) {
				suppressWarnings(fsa, it);
			}
		}
	}

	protected void normalizeLineDelimiters(IXtextGeneratorFileSystemAccess fsa, TypeReference type) {
		String content = fsa.readTextFile(type.getJavaPath()).toString();
		content = newLineNormalizer.postProcess(fsa.getURI(type.getJavaPath()), content).toString();
		content = content.replaceAll("\"\\+(\\r)?\\n\\s+\"", "");
		fsa.generateFile(type.getJavaPath(), content);
	}

	protected void normalizeLineDelimiters(IXtextGeneratorFileSystemAccess fsa, TypeReference... types) {
		if (types != null) {
			for (TypeReference it : types) {
				normalizeLineDelimiters(fsa, it);
			}
		}
	}

	protected void normalizeTokens(IXtextGeneratorFileSystemAccess fsa, String tokenFile) {
		String content = fsa.readTextFile(tokenFile).toString();
		content = newLineNormalizer.postProcess(fsa.getURI(tokenFile), content).toString();
		List<String> splitted = Strings.split(content, codeConfig.getLineDelimiter());
		Collections.sort(splitted);
		content = Strings.concat(codeConfig.getLineDelimiter(), splitted) + codeConfig.getLineDelimiter();
		fsa.generateFile(tokenFile, content);
	}

	protected void splitParserAndLexerIfEnabled(IXtextGeneratorFileSystemAccess fsa, TypeReference parser,
			TypeReference lexer) {
		improveCodeQuality(fsa, lexer, parser);
		if (getOptions().isClassSplitting()) {
			splitLexerClassFile(fsa, lexer);
			splitParserClassFile(fsa, parser);
		}
	}

	protected void improveCodeQuality(IXtextGeneratorFileSystemAccess fsa, TypeReference lexer, TypeReference parser) {
		improveLexerCodeQuality(fsa, lexer);
		improveParserCodeQuality(fsa, parser);
	}

	protected void improveParserCodeQuality(IXtextGeneratorFileSystemAccess fsa, TypeReference parser) {
		String parserContent = fsa.readTextFile(parser.getJavaPath()).toString();
		parserContent = codeQualityHelper.stripUnnecessaryComments(parserContent, options);
		parserContent = codeQualityHelper.removeDuplicateBitsets(parserContent, options);
		parserContent = codeQualityHelper.removeDuplicateDFAs(parserContent, options);
		fsa.generateFile(parser.getJavaPath(), parserContent);
	}

	protected void improveLexerCodeQuality(IXtextGeneratorFileSystemAccess fsa, TypeReference lexer) {
		String lexerContent = fsa.readTextFile(lexer.getJavaPath()).toString();
		lexerContent = codeQualityHelper.stripUnnecessaryComments(lexerContent, options);
		fsa.generateFile(lexer.getJavaPath(), lexerContent);
	}

	protected void cleanupLexerTokensFile(AntlrGrammar lexerGrammar, KeywordHelper helper,
			IXtextGeneratorFileSystemAccess fsa) {
		try {
			if (options.isBacktrackLexer()) {
				MutableTokenDefProvider provider = createLexerTokensProvider(lexerGrammar, helper, fsa);
				Iterator<Map.Entry<Integer, String>> entries = provider.getTokenDefMap().entrySet().iterator();
				while (entries.hasNext()) {
					String value = entries.next().getValue();
					if (!helper.isKeywordRule(value) && !value.startsWith("RULE_") && !value.startsWith("SUPER_")) {
						entries.remove();
					}
				}
				CharArrayWriter writer = new CharArrayWriter();
				provider.writeTokenFile(new PrintWriter(writer));
				fsa.generateFile(lexerGrammar.getTokensFileName(), new String(writer.toCharArray()));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected MutableTokenDefProvider createLexerTokensProvider(AntlrGrammar lexerGrammar, KeywordHelper helper,
			IXtextGeneratorFileSystemAccess fsa) {
		MutableTokenDefProvider provider = new MutableTokenDefProvider(helper,
				Charset.forName(codeConfig.getEncoding()));
		provider.setAntlrTokenFileProvider(() -> {
			return fsa.readBinaryFile(lexerGrammar.getTokensFileName());
		});
		return provider;
	}

	protected void cleanupParserTokensFile(AntlrGrammar lexerGrammar, AntlrGrammar parserGrammar, KeywordHelper helper,
			IXtextGeneratorFileSystemAccess fsa) {
		try {
			MutableTokenDefProvider provider = createLexerTokensProvider(lexerGrammar, helper, fsa);
			for (Map.Entry<Integer, String> entry : provider.getTokenDefMap().entrySet()) {
				String value = entry.getValue();
				if (helper.isKeywordRule(value)) {
					String keywordAsAntlrString = AntlrGrammarGenUtil.toAntlrString(helper.getKeywordValue(value));
					entry.setValue("'" + keywordAsAntlrString + "'");
				} else {
					if (value.startsWith("'")) {
						entry.setValue("'" + AntlrGrammarGenUtil.toAntlrString(value) + "'");
					}
				}
			}
			CharArrayWriter writer = new CharArrayWriter();
			provider.writeTokenFile(new PrintWriter(writer));
			fsa.generateFile(parserGrammar.getTokensFileName(), new String(writer.toCharArray()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected void removeBackTrackingGuards(IXtextGeneratorFileSystemAccess fsa, TypeReference parser,
			int lookaheadThreshold) {
		String content = fsa.readTextFile(parser.getJavaPath()).toString();
		BacktrackingGuardRemover remover = new BacktrackingGuardRemover(content, lookaheadThreshold);
		String newContent = remover.transform();
		fsa.generateFile(parser.getJavaPath(), newContent);
	}

	protected boolean containsUnorderedGroup(Grammar grammar) {
		for (ParserRule rule : GrammarUtil.allParserRules(grammar)) {
			if (Iterators.filter(rule.eAllContents(), UnorderedGroup.class).hasNext()) {
				return true;
			}
		}
		return false;
	}

	protected AntlrToolFacade getAntlrTool() {
		return antlrTool;
	}

	protected AntlrCodeQualityHelper getCodeQualityHelper() {
		return codeQualityHelper;
	}

	protected LineSeparatorHarmonizer getNewLineNormalizer() {
		return newLineNormalizer;
	}

	protected CodeConfig getCodeConfig() {
		return codeConfig;
	}

	public AntlrOptions getOptions() {
		return options;
	}

	public void setOptions(AntlrOptions options) {
		this.options = options;
	}
	
	/**
	 * @since 2.26
	 */
	public void setDownloadUrl(String url) {
		this.dowloadUrl = url;
	}
}
