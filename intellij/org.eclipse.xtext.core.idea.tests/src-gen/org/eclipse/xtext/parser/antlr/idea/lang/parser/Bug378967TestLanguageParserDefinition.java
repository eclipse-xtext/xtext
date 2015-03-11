package org.eclipse.xtext.parser.antlr.idea.lang.parser;

import org.eclipse.xtext.parser.antlr.idea.lang.Bug378967TestLanguageElementTypeProvider;
import org.eclipse.xtext.parser.antlr.idea.lang.psi.impl.Bug378967TestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug378967TestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug378967TestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug378967TestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
