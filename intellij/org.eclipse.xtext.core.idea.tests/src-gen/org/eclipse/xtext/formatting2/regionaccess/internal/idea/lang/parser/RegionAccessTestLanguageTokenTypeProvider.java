package org.eclipse.xtext.formatting2.regionaccess.internal.idea.lang.parser;

import static org.eclipse.xtext.formatting2.regionaccess.internal.idea.parser.antlr.internal.PsiInternalRegionAccessTestLanguageParser.*;

import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.formatting2.regionaccess.internal.idea.lang.RegionAccessTestLanguageLanguage;
import org.eclipse.xtext.formatting2.regionaccess.internal.idea.parser.antlr.internal.PsiInternalRegionAccessTestLanguageParser;

import com.google.inject.Singleton;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

@Singleton public class RegionAccessTestLanguageTokenTypeProvider implements TokenTypeProvider {

	private static final String[] TOKEN_NAMES = new PsiInternalRegionAccessTestLanguageParser(null).getTokenNames();

	private static final IElementType[] tokenTypes = new IElementType[TOKEN_NAMES.length];
	
	static {
		for (int i = 0; i < TOKEN_NAMES.length; i++) {
			tokenTypes[i] = new IndexedElementType(TOKEN_NAMES[i], i, RegionAccessTestLanguageLanguage.INSTANCE);
		}
	}

	private static final TokenSet WHITESPACE_TOKENS = TokenSet.create(tokenTypes[RULE_WS]);
	private static final TokenSet COMMENT_TOKENS = TokenSet.create(tokenTypes[RULE_SL_COMMENT], tokenTypes[RULE_ML_COMMENT]);
	private static final TokenSet STRING_TOKENS = TokenSet.create(tokenTypes[RULE_STRING]);

	@Override
    public int getAntlrType(IElementType iElementType) {
        return ((IndexedElementType)iElementType).getLocalIndex();
    }
    
    @Override
    public IElementType getIElementType(int antlrType) {
    	return tokenTypes[antlrType];
    }

	@Override
	public TokenSet getWhitespaceTokens() {
		return WHITESPACE_TOKENS;
	}

	@Override
	public TokenSet getCommentTokens() {
		return COMMENT_TOKENS;
	}

	@Override
	public TokenSet getStringLiteralTokens() {
		return STRING_TOKENS;
	}

}
