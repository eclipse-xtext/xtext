package org.eclipse.xtext.lexer.idea.lang.parser;

import org.eclipse.xtext.lexer.idea.lang.BacktrackingLexerTestLanguageElementTypeProvider;
import org.eclipse.xtext.lexer.idea.lang.psi.impl.BacktrackingLexerTestLanguageFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class BacktrackingLexerTestLanguageParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private BacktrackingLexerTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new BacktrackingLexerTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
