/**
 * Copyright (c) 2024 Sigasi (http://www.sigasi.com) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.textmate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.TerminalRule;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gson.annotations.Expose;

/**
 * A TextMate grammar with some additional, optional properties that can be assigned from MWE2 to configure
 * how rules are inferred.
 * 
 * @author David Medina
 * @author Sebastian Zarnekow
 * @since 2.35
 */
public class TextMateGrammar {

	private static final String ANY_OTHER = "ANY_OTHER";

	@Expose	private final List<TextMateRule> patterns;
	@Expose private String scopeName;
	@Expose private Map<String, TextMateRule> repository;
	private boolean inferPatterns = true;
	private boolean ignoreCase = false;
	private TerminalRuleToTextMateRule generator = new TerminalRuleToTextMateRule(); 
	
	public TextMateGrammar() {
		this.patterns = new ArrayList<>();
	}

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}
	
	public boolean isInferPatterns() {
		return inferPatterns;
	}

	public void setInferPatterns(boolean inferPatterns) {
		this.inferPatterns = inferPatterns;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public TerminalRuleToTextMateRule getGenerator() {
		return generator;
	}

	public void setGenerator(TerminalRuleToTextMateRule generator) {
		this.generator = generator;
	}

	public void addSkip(SkippedRule rule) {
		this.patterns.add(rule);
	}
	
	public void addAuto(AutoRule rule) {
		this.patterns.add(rule);
	}
	
	public void addMatch(MatchRule rule) {
		this.patterns.add(rule);
	}
	
	public void addInclude(String include) {
		this.patterns.add(new IncludeRule(include));
	}
	
	public void addBeginEnd(BeginEndRule rule) {
		this.patterns.add(rule);
	}
	
	public void addRepositoryMatch(MatchRule rule) {
		if (repository == null) {
			repository = new TreeMap<>();
		}
		this.repository.put(rule.getName(), rule);
		rule.setName(null);
	}
	
	public void addRepositoryBeginEnd(BeginEndRule rule) {
		if (repository == null) {
			repository = new TreeMap<>();
		}
		this.repository.put(rule.getName(), rule);
		rule.setName(null);
	}
	
	public void addRule(TextMateRule rule) {
		this.patterns.add(rule);
	}

	protected TextMateGrammar init(Grammar grammar) {
		String scopeName = this.scopeName;
		if (scopeName == null) {
			scopeName = "source." + getLanguageName(grammar);
		}
		TextMateGrammar result = new TextMateGrammar();
		result.setScopeName(scopeName);
		result.addRule(getKeywordControlRule(grammar, ignoreCase));
		
		Set<String> seenTerminalRules = new HashSet<>();
		for(TextMateRule pattern: patterns) {
			seenTerminalRules.add(pattern.getTerminalRule());
			if (pattern instanceof SkippedRule) {
				continue;
			}
			if (pattern instanceof AutoRule) {
				((AutoRule)pattern).init(grammar, ignoreCase, generator).ifPresent(result::addRule);
			} else {
				result.addRule(pattern);
			}
		}
		if (inferPatterns) {
			List<TerminalRule> terminals = GrammarUtil.allTerminalRules(grammar)
					.stream()
					.filter(r -> !r.isFragment() && !r.getName().equals(ANY_OTHER))
					.collect(Collectors.toList());
			for(TerminalRule terminal: terminals) {
				if (!seenTerminalRules.add(terminal.getName())) {
					continue;
				}
				AutoRule auto = newAutoRule();
				auto.setTerminalRule(terminal.getName());
				auto.init(grammar, ignoreCase, generator).ifPresent(result::addRule);
			}
		}

		result.addRule(getPunctuationRule(grammar, ignoreCase));
		// invalid rule must be last, otherwise it prevents other rules from matching
		if (inferPatterns && GrammarUtil.findRuleForName(grammar, ANY_OTHER) != null) {
			AutoRule auto = newAutoRule();
			auto.setTerminalRule(ANY_OTHER);
			auto.init(grammar, ignoreCase, generator).ifPresent(result::addRule);
		}
		return result;
	}

	protected AutoRule newAutoRule() {
		return new AutoRule();
	}

	protected String getLanguageName(Grammar grammar) {
		return GrammarUtil.getSimpleName(grammar).toLowerCase(Locale.ROOT);
	}

	protected TextMateRule getKeywordControlRule(Grammar grammar, boolean ignoreCase) {
		return createKeywordRule(grammar, "keyword.control", keyword -> keyword.matches("\\w+"), ignoreCase);
	}

	protected TextMateRule getPunctuationRule(Grammar grammar, boolean ignoreCase) {
		return createKeywordRule(grammar, "punctuation", keyword -> !keyword.matches("\\w+"), ignoreCase);
	}

	protected TextMateRule createKeywordRule(Grammar grammar, String namePrefix, Predicate<String> filter, boolean ignoreCase) {
		StringBuilder matchBuilder = new StringBuilder();
		if (ignoreCase) {
			matchBuilder.append("(?i)");
		}
		matchBuilder.append("(");
		List<String> allKeywords = GrammarUtil.getAllKeywords(grammar)
				.stream()
				.filter(filter)
				.sorted()
				.toList();
		Joiner.on("|").appendTo(matchBuilder, Iterables.transform(allKeywords, this::escapeAndAddWordBoundaries));
		matchBuilder.append(")");
		MatchRule result = new MatchRule();
		result.setName(namePrefix + "." + getLanguageName(grammar));
		result.setMatch(matchBuilder.toString());
		return result;
	}

	private static final Pattern START_IS_LETTER = Pattern.compile("^\\w");
	private static final Pattern END_IS_LETTER = Pattern.compile("\\w$");
	protected String escapeAndAddWordBoundaries(String token) {
		StringBuilder result = new StringBuilder();
		if (START_IS_LETTER.matcher(token).find()) {
			result.append("\\b");
		}
		result.append(escapeForRegex(token));
		if (END_IS_LETTER.matcher(token).find()) {
			result.append("\\b");
		}
		return result.toString();
	}

	private static final Pattern REGEX_CONTROL_CHARS = Pattern.compile("[\\\\^$.*+?()\\[\\]{}|]");
	private static String escapeForRegex(String input) {
		Matcher matcher = REGEX_CONTROL_CHARS.matcher(input);
		return matcher.replaceAll(match -> Matcher.quoteReplacement("\\" + match.group()));
	}

}
