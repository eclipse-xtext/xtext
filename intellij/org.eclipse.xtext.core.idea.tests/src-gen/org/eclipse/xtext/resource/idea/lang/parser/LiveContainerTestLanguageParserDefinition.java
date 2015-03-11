package org.eclipse.xtext.resource.idea.lang.parser;

import org.eclipse.xtext.resource.idea.lang.LiveContainerTestLanguageElementTypeProvider;
import org.eclipse.xtext.resource.idea.lang.psi.impl.LiveContainerTestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class LiveContainerTestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private LiveContainerTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LiveContainerTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
