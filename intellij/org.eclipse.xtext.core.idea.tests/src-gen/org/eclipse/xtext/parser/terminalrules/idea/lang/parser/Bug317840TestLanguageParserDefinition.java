package org.eclipse.xtext.parser.terminalrules.idea.lang.parser;

import org.eclipse.xtext.parser.terminalrules.idea.lang.Bug317840TestLanguageElementTypeProvider;
import org.eclipse.xtext.parser.terminalrules.idea.lang.psi.impl.Bug317840TestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug317840TestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug317840TestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug317840TestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getElement_FirstNamedParserRuleCall_1_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getNamed_NameNAMEParserRuleCall_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_SecondNamedParserRuleCall_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getNamed_NameNAMEParserRuleCall_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_ThirdNamedParserRuleCall_3_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getNamed_NameNAMEParserRuleCall_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_ForthNamedParserRuleCall_3_1_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getNamed_NameNAMEParserRuleCall_0ElementType()
			);
		}
		return super.createElement(node);
	}

}
