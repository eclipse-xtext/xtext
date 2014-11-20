package org.eclipse.xtend.core.idea.lang;

import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.eclipse.xtend.core.parser.antlr.internal.InternalXtendLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

public class XtendSyntaxHighlighter extends SyntaxHighlighterBase {

	@Inject TokenTypeProvider tokenTypeProvider;
	@Inject Provider<Lexer> lexerProvider; 

    @NotNull
    public Lexer getHighlightingLexer() {
        return lexerProvider.get();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (tokenTypeProvider.getStringLiteralTokens().contains(tokenType)) {
            return pack(DefaultLanguageHighlighterColors.STRING);
        }
		if (tokenTypeProvider.getIElementType(InternalXtendLexer.RULE_SL_COMMENT) == tokenType) {
			return pack(DefaultLanguageHighlighterColors.LINE_COMMENT);
		}
		if (tokenTypeProvider.getIElementType(InternalXtendLexer.RULE_ML_COMMENT) == tokenType) {
			return pack(DefaultLanguageHighlighterColors.BLOCK_COMMENT);
		}
        String myDebugName = tokenType.toString();
		if (myDebugName.matches("^'.*\\w.*'$")) {
			return pack(DefaultLanguageHighlighterColors.KEYWORD);
        }
        return new TextAttributesKey[0];
    }

}
