/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.serializer.diagnostic;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EnumLiteralDeclaration;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.linking.impl.LinkingHelper;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.util.EmfFormatter;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@Singleton
public class TokenDiagnosticProvider implements ITokenDiagnosticProvider {

	@Inject
	private LinkingHelper linkingHelper;
	
	@Inject
	private IGrammarAccess grammarAccess;

	protected String getFullReferenceName(EObject semanticObject, CrossReference reference) {
		EReference ref = GrammarUtil.getReference(reference);
		String clazz = semanticObject.eClass().getName();
		if (ref.getEContainingClass() != semanticObject.eClass())
			clazz = ref.getEContainingClass().getName() + "(" + clazz + ")";
		return clazz + "." + ref.getName();
	}

	@Override
	public ISerializationDiagnostic getInvalidEnumValueDiagnostic(EObject semanticObject, RuleCall rc, Object value) {
		List<String> valid = Lists.newArrayList();
		for (EnumLiteralDeclaration eld : org.eclipse.xtext.EcoreUtil2.getAllContentsOfType(rc.getRule(),
				EnumLiteralDeclaration.class))
			valid.add(eld.getEnumLiteral().getInstance().getName());
		StringBuilder msg = new StringBuilder();
		msg.append("The value '" + value + "' is invalid for enum " + rc.getRule().getName() + "\n");
		msg.append("Valid values are: " + Joiner.on(", ").join(valid));
		return new SerializationDiagnostic(INVALID_ENUM_VALUE, semanticObject, rc, grammarAccess.getGrammar(), msg.toString());
	}

	@Override
	public ISerializationDiagnostic getNoEObjectDescriptionFoundDiagnostic(EObject semanticObject,
			CrossReference element, EObject target, IScope scope) {
		String msg = "No EObjectDescription could be found in Scope " + getFullReferenceName(semanticObject, element)
				+ " for " + EmfFormatter.objPath(target);
		return new SerializationDiagnostic(NO_EOBJECT_DESCRIPTION_FOUND, semanticObject, element, grammarAccess.getGrammar(), msg);
	}

	@Override
	public ISerializationDiagnostic getNoScopeFoundDiagnostic(EObject semanticObject, CrossReference element,
			EObject target) {
		String msg = "Could not create Scope for EReference " + getFullReferenceName(semanticObject, element);
		return new SerializationDiagnostic(NO_SCOPE_FOUND, semanticObject, element, grammarAccess.getGrammar(), msg);
	}

	@Override
	public ISerializationDiagnostic getNullNotAllowedDiagnostic(EObject semanticObject, AbstractElement ele) {
		return new SerializationDiagnostic(NULL_NOT_ALLOWED, semanticObject, ele, grammarAccess.getGrammar(), "Must not be null");
	}

	@Override
	public ISerializationDiagnostic getValueConversionExceptionDiagnostic(EObject semantic, AbstractElement element,
			Object value, Throwable exception) {
		String ruleName = linkingHelper.getRuleNameFrom(element);
		StringBuilder msg = new StringBuilder();
		msg.append("Error while converting value '");
		msg.append(value);
		msg.append("' for grammar rule '");
		msg.append(ruleName);
		msg.append("' to string via ValueConverter.");
		return new SerializationDiagnostic(VALUE_CONVERSION_EXCEPTION, semantic, element, grammarAccess.getGrammar(), msg.toString(), exception);
	}

}
