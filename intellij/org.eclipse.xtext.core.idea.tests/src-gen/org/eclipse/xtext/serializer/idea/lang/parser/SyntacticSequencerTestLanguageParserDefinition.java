package org.eclipse.xtext.serializer.idea.lang.parser;

import org.eclipse.xtext.psi.impl.PsiEObjectReference;
import org.eclipse.xtext.serializer.idea.lang.SyntacticSequencerTestLanguageElementTypeProvider;
import org.eclipse.xtext.serializer.idea.lang.psi.impl.SyntacticSequencerTestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class SyntacticSequencerTestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private SyntacticSequencerTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new SyntacticSequencerTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getAddition0_Prim0ParserRuleCall_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim0_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getPrim0_Val0Action_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim0_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getAddition0_RightPrim0ParserRuleCall_1_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim0_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getAddition1_Prim1ParserRuleCall_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim1_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getPrim1_Val1Action_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim1_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getAddition1_RightPrim1ParserRuleCall_1_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim1_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getMultiplication2_Prim2ParserRuleCall_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim2_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getPrim2_Val2Action_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim2_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getMultiplication2_RightPrim2ParserRuleCall_1_2_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getPrim2_NameIDTerminalRuleCall_0_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getModel_X5SingleCrossReferenceParserRuleCall_4_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getSingleCrossReference_NameTERMINAL_IDTerminalRuleCall_1_0_0ElementType(),
				elementTypeProvider.getSingleCrossReference_NameIDTerminalRuleCall_1_0_1ElementType()
			);
		}
		if (elementType == elementTypeProvider.getSingleCrossReference_Ref1SingleCrossReferenceCrossReference_2_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getSingleCrossReference_Ref2SingleCrossReferenceCrossReference_3_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getSingleCrossReference_Ref3SingleCrossReferenceCrossReference_4_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getSingleCrossReference_Ref4SingleCrossReferenceCrossReference_5_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		return super.createElement(node);
	}

}
