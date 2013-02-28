/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.annotations.typesystem;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationElementValueBinaryOperation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationElementValuePair;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationResult;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.computation.XbaseTypeComputer;
import org.eclipse.xtext.xbase.typesystem.references.CompoundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.validation.IssueCodes;

import com.google.common.collect.Iterables;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@NonNullByDefault
public class XbaseWithAnnotationsTypeComputer extends XbaseTypeComputer {

	@Override
	public void computeTypes(XExpression expression, ITypeComputationState state) {
		if (expression instanceof XAnnotation) {
			_computeTypes((XAnnotation)expression, state);
		} else if (expression instanceof XAnnotationElementValueBinaryOperation) {
			_computeTypes((XAnnotationElementValueBinaryOperation)expression, state);
		} else {
			super.computeTypes(expression, state);
		}
	}
	
	protected void _computeTypes(XAnnotation object, ITypeComputationState state) {
		JvmAnnotationType annotationType = object.getAnnotationType();
		if (annotationType != null && !annotationType.eIsProxy()) {
			XExpression expression = object.getValue();
			if (expression != null) {
				Iterable<JvmFeature> iterable = annotationType.findAllFeaturesByName("value");
				JvmFeature value = Iterables.getOnlyElement(iterable, null);
				if (value != null) {
					if (value instanceof JvmOperation) {
						computeTypes(object, (JvmOperation) value, expression, state);
					} else {
						throw new IllegalStateException("Unexpected feature type " + value);
					}
				} else {
					state.addDiagnostic(new EObjectDiagnosticImpl(
							Severity.ERROR, 
							IssueCodes.ANNOTATIONS_NO_VALUE_ATTRIBUTE, 
							"The attribute value is undefined for the annotation type " + annotationType.getSimpleName(), 
							object, 
							XAnnotationsPackage.Literals.XANNOTATION__VALUE, 
							-1, null));
					state.withNonVoidExpectation().computeTypes(expression);
				}
			} else {
				List<XAnnotationElementValuePair> valuePairs = object.getElementValuePairs();
				for(XAnnotationElementValuePair pair: valuePairs) {
					computeTypes(object, pair.getElement(), pair.getValue(), state);
				}
			}
			state.acceptActualType(new ParameterizedTypeReference(state.getReferenceOwner(), annotationType));
		} else {
			XExpression expression = object.getValue();
			state.withNonVoidExpectation().computeTypes(expression);
		}
	}

	protected void computeTypes(XAnnotation annotation, @Nullable JvmOperation operation, XExpression value,
			ITypeComputationState state) {
		LightweightTypeReference expectation = operation == null || operation.eIsProxy() ? null : state.getConverter().toLightweightReference(operation.getReturnType());
		if (expectation != null && expectation.isArray()) {
			LightweightTypeReference componentType = expectation.getComponentType();
			if (componentType == null) {
				throw new IllegalStateException("Array without component type: " + expectation.getIdentifier());
			}
			LightweightTypeReference actualExpectation =  (value instanceof XListLiteral)
					? expectation
					: componentType;
			ITypeComputationResult result = state.withExpectation(actualExpectation).computeTypes(value);
			LightweightTypeReference resultType = result.getActualExpressionType();
			if (resultType != null && !actualExpectation.isAssignableFrom(resultType)) {
				if (value instanceof XListLiteral) {
					// our children are incompatible so let's mark the array itself as compatible.
					state.refineExpectedType(value, resultType);
				} else {
					CompoundTypeReference bothExpectations = new CompoundTypeReference(state.getReferenceOwner(), true);
					bothExpectations.addComponent(componentType);
					bothExpectations.addComponent(expectation);
					state.refineExpectedType(value, bothExpectations);
				}
			}
		} else if (expectation == null) {
			state.withNonVoidExpectation().computeTypes(value);
		} else {
			ITypeComputationResult valueResult = state.withExpectation(expectation).computeTypes(value);
			LightweightTypeReference valueResultType = valueResult.getActualExpressionType();
			if (valueResultType != null && !expectation.isAssignableFrom(valueResultType)) {
				if (value instanceof XListLiteral) {
					String simpleName = valueResultType.getSimpleName();
					state.addDiagnostic(new EObjectDiagnosticImpl(
							Severity.ERROR, 
							IssueCodes.INCOMPATIBLE_TYPES, 
							"Type mismatch: cannot convert from " + simpleName + "[] to " + simpleName, 
							annotation, 
							XAnnotationsPackage.Literals.XANNOTATION__VALUE, 
							-1, null));
				}
			}
		}
	}

	protected void _computeTypes(XAnnotationElementValueBinaryOperation object, ITypeComputationState state) {
		state.computeTypes(object.getLeftOperand());
		state.computeTypes(object.getRightOperand());
	}
	
}
