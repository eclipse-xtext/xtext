package org.eclipse.xtext.testlanguages.serializer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.serializer.acceptor.ISemanticSequenceAcceptor;
import org.eclipse.xtext.serializer.diagnostic.ISemanticSequencerDiagnosticProvider;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic.Acceptor;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.GenericSequencer;
import org.eclipse.xtext.serializer.sequencer.ISemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService;
import org.eclipse.xtext.testlanguages.actionLang2.ActionLang2Package;
import org.eclipse.xtext.testlanguages.actionLang2.ORing;
import org.eclipse.xtext.testlanguages.actionLang2.Value;
import org.eclipse.xtext.testlanguages.services.ActionTestLanguage2GrammarAccess;

@SuppressWarnings("all")
public class ActionTestLanguage2SemanticSequencer extends AbstractDelegatingSemanticSequencer {

	@Inject
	private ActionTestLanguage2GrammarAccess grammarAccess;
	
	public void createSequence(EObject context, EObject semanticObject) {
		if(semanticObject.eClass().getEPackage() == ActionLang2Package.eINSTANCE) switch(semanticObject.eClass().getClassifierID()) {
			case ActionLang2Package.ORING:
				if(context == grammarAccess.getORingRule() ||
				   context == grammarAccess.getORingAccess().getORingDisjunctsAction_1_0()) {
					sequence_ORing(context, (ORing) semanticObject); 
					return; 
				}
				else break;
			case ActionLang2Package.VALUE:
				if(context == grammarAccess.getORingRule() ||
				   context == grammarAccess.getORingAccess().getORingDisjunctsAction_1_0() ||
				   context == grammarAccess.getValueRule()) {
					sequence_Value(context, (Value) semanticObject); 
					return; 
				}
				else break;
			}
		if (errorAcceptor != null) errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Constraint:
	 *     (disjuncts+=ORing_ORing_1_0 disjuncts+=Value)
	 */
	protected void sequence_ORing(EObject context, ORing semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Constraint:
	 *     value='a'
	 */
	protected void sequence_Value(EObject context, Value semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
}
