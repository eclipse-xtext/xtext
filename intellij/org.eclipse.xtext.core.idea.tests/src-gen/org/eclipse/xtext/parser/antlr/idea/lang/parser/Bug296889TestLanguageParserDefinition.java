package org.eclipse.xtext.parser.antlr.idea.lang.parser;

import org.eclipse.xtext.parser.antlr.idea.lang.Bug296889TestLanguageElementTypeProvider;
import org.eclipse.xtext.parser.antlr.idea.lang.psi.impl.Bug296889TestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug296889TestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug296889TestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug296889TestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getPreop_ExprVariableParserRuleCall_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getVariable_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getVariable_VariableAction_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getVariable_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getPostop_VariableParserRuleCall_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getVariable_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		return super.createElement(node);
	}

}
