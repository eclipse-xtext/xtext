package org.eclipse.xtext.resource.idea.lang.parser;

import org.eclipse.xtext.psi.impl.PsiEObjectReference;
import org.eclipse.xtext.resource.idea.lang.EObjectAtOffsetTestLanguageElementTypeProvider;
import org.eclipse.xtext.resource.idea.lang.psi.impl.EObjectAtOffsetTestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;
import org.eclipse.xtext.psi.impl.PsiNamedEObjectImpl;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class EObjectAtOffsetTestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private EObjectAtOffsetTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new EObjectAtOffsetTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		if (elementType == elementTypeProvider.getAbstractBar_BarParserRuleCall_2ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getBar_NameIDTerminalRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getModel_FoosFooParserRuleCall_0_0ElementType()) {
			return new PsiNamedEObjectImpl(node,
				elementTypeProvider.getFoo_NameQualifiedNameWithOtherDelimParserRuleCall_1_0ElementType()
			);
		}
		if (elementType == elementTypeProvider.getAbstractBar_FooFooCrossReference_3_2_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getAbstractBar_FooFooCrossReference_4_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getBar_FooFooCrossReference_2_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		if (elementType == elementTypeProvider.getBar_FooFooCrossReference_3_1_0ElementType()) {
			return new PsiEObjectReference(node);
		}
		return super.createElement(node);
	}

}
