package org.eclipse.xtext.parsetree.transientvalues.idea.lang.parser;

import org.eclipse.xtext.parsetree.transientvalues.idea.lang.TransientValuesTestElementTypeProvider;
import org.eclipse.xtext.parsetree.transientvalues.idea.lang.psi.impl.TransientValuesTestFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class TransientValuesTestParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private TransientValuesTestElementTypeProvider elementTypeProvider;

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new TransientValuesTestFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		return super.createElement(node);
	}

}
