package org.eclipse.xtext.linking.idea.lang.parser;

import org.eclipse.xtext.psi.impl.PsiEObjectReference;
import org.eclipse.xtext.linking.idea.lang.Bug313089TestLanguageElementTypeProvider;
import org.eclipse.xtext.linking.idea.lang.psi.impl.Bug313089TestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug313089TestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug313089TestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug313089TestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getFoo_BarBarParserRuleCall_1_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBar_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getBar_BarAction_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBar_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getFoo_BazBazParserRuleCall_1_1_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBaz_NameIDTerminalRuleCall_1_0ElementType(),
				elementTypeProvider.getBaz_NameIDTerminalRuleCall_2_2_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getBaz_BazChildAction_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBaz_NameIDTerminalRuleCall_1_0ElementType(),
				elementTypeProvider.getBaz_NameIDTerminalRuleCall_2_2_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getFoo_RefFooCrossReference_2_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		return super.createElement(node);
	}

}
