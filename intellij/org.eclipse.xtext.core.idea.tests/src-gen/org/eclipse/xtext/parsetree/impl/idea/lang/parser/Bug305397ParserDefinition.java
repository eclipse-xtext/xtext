package org.eclipse.xtext.parsetree.impl.idea.lang.parser;

import org.eclipse.xtext.parsetree.impl.idea.lang.Bug305397ElementTypeProvider;
import org.eclipse.xtext.parsetree.impl.idea.lang.psi.impl.Bug305397FileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug305397ParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug305397ElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug305397FileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getModel_ElementsElementParserRuleCall_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getElement_NameIDTerminalRuleCall_3_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_ElementAction_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getElement_NameIDTerminalRuleCall_3_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_ElementsElementParserRuleCall_4_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getElement_NameIDTerminalRuleCall_3_0ElementType()
			);
		}
		return super.createElement(node);
	}

}
