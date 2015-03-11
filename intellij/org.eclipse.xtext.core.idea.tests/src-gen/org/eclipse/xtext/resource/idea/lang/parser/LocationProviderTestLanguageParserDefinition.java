package org.eclipse.xtext.resource.idea.lang.parser;

import org.eclipse.xtext.psi.impl.PsiEObjectReference;
import org.eclipse.xtext.resource.idea.lang.LocationProviderTestLanguageElementTypeProvider;
import org.eclipse.xtext.resource.idea.lang.psi.impl.LocationProviderTestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class LocationProviderTestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private LocationProviderTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new LocationProviderTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getModel_ElementsElementParserRuleCall_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getElement_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getComponent_BusParserRuleCall_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBus_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getComponent_ProcessorParserRuleCall_0_1ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getProcessor_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getComponent_ModeModeParserRuleCall_1_1_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getMode_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getComponent_TransitionTransitionParserRuleCall_1_1_1_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getTransition_NameIDTerminalRuleCall_0_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getBus_PortPortParserRuleCall_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPort_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getProcessor_DataDataParserRuleCall_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getData_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getElement_SinglerefElementCrossReference_2_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getElement_MultirefsElementCrossReference_3_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getTransition_SourceModeCrossReference_2_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getTransition_DestinationModeCrossReference_4_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		return super.createElement(node);
	}

}
