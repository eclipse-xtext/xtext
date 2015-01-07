package org.eclipse.xtext.parser.indentation.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.indentation.indentationAwareTestLanguage.IndentationAwareTestLanguagePackage;
import org.eclipse.xtext.parser.indentation.indentationAwareTestLanguage.Tree;
import org.eclipse.xtext.parser.indentation.indentationAwareTestLanguage.TreeNode;
import org.eclipse.xtext.parser.indentation.services.IndentationAwareTestLanguageGrammarAccess;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.diagnostic.ISemanticSequencerDiagnosticProvider;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.GenericSequencer;
import org.eclipse.xtext.serializer.sequencer.ISemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService;

@SuppressWarnings("all")
public abstract class AbstractIndentationAwareTestLanguageSemanticSequencer extends AbstractDelegatingSemanticSequencer {

	@Inject
	private IndentationAwareTestLanguageGrammarAccess grammarAccess;
	
	@Override
	public void createSequence(EObject context, EObject semanticObject) {
		if(semanticObject.eClass().getEPackage() == IndentationAwareTestLanguagePackage.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case IndentationAwareTestLanguagePackage.TREE:
				if(context == grammarAccess.getTreeRule()) {
					sequence_Tree(context, (Tree) semanticObject); 
					return; 
				}
				else break;
			case IndentationAwareTestLanguagePackage.TREE_NODE:
				if(context == grammarAccess.getTreeNodeRule()) {
					sequence_TreeNode(context, (TreeNode) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (name=ID children+=TreeNode*)
	 */
	protected void sequence_TreeNode(EObject context, TreeNode semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     (nodes+=TreeNode*)
	 */
	protected void sequence_Tree(EObject context, Tree semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
}
