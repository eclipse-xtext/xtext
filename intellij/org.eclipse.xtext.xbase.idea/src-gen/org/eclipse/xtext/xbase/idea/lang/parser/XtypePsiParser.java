package org.eclipse.xtext.xbase.idea.lang.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.TokenStream;
import org.eclipse.xtext.idea.parser.AbstractXtextPsiParser;
import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.xbase.idea.lang.XtypeElementTypeProvider;
import org.eclipse.xtext.xbase.idea.parser.antlr.internal.PsiInternalXtypeParser;
import org.eclipse.xtext.xbase.services.XtypeGrammarAccess;

import com.google.inject.Inject;
import com.intellij.lang.PsiBuilder;

public class XtypePsiParser extends AbstractXtextPsiParser {

	private static final Set<String> INITIAL_HIDDEN_TOKENS = new HashSet<String>(Arrays.asList("RULE_WS", "RULE_ML_COMMENT", "RULE_SL_COMMENT"));

	@Inject 
	private XtypeGrammarAccess grammarAccess;

	@Inject 
	private XtypeElementTypeProvider elementTypeProvider;

	@Override
	protected AbstractPsiAntlrParser createParser(PsiBuilder builder, TokenStream tokenStream) {
		return new PsiInternalXtypeParser(builder, tokenStream, elementTypeProvider, grammarAccess);
	}

	@Override
	protected Set<String> getInitialHiddenTokens() {
		return INITIAL_HIDDEN_TOKENS;
	}

}
