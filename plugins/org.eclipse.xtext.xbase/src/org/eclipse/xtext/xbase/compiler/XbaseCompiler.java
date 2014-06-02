/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.compiler;

import static com.google.common.collect.Sets.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.generator.trace.ILocationData;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XBasicForLoopExpression;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XCatchClause;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XCollectionLiteral;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XDoWhileExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XListLiteral;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XNullLiteral;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XSetLiteral;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XSynchronizedExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XWhileExpression;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationElementValuePair;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.controlflow.IEarlyExitComputer;
import org.eclipse.xtext.xbase.featurecalls.IdentifiableSimpleNameProvider;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.scoping.featurecalls.OperatorMapping;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.internal.FeatureLinkHelper;
import org.eclipse.xtext.xbase.typesystem.references.FunctionTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightMergedBoundTypeArgument;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.OwnedConverter;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.UnknownTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.DeclaratorTypeArgumentCollector;
import org.eclipse.xtext.xbase.typesystem.util.StandardTypeParameterSubstitutor;
import org.eclipse.xtext.xbase.util.XExpressionHelper;
import org.eclipse.xtext.xbase.util.XSwitchExpressions;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class XbaseCompiler extends FeatureCallCompiler {
	
	@Inject 
	private XExpressionHelper expressionHelper;
	
	@Inject 
	private IEarlyExitComputer earlyExitComputer;
	
	@Inject 
	private IBatchTypeResolver batchTypeResolver;
	
	@Inject
	private XSwitchExpressions switchExpressions;
	
	@Inject
	private FeatureLinkHelper featureLinkHelper;
	
	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XListLiteral literal, ITreeAppendable b, boolean isReferenced) {
		for(XExpression element: literal.getElements()) 
			internalToJavaStatement(element, b, true);
	}

	protected void _toJavaStatement(final XSetLiteral literal, ITreeAppendable b, boolean isReferenced) {
		LightweightTypeReference literalType = resolveType(literal, Map.class);
		if(literalType != null) {
			if(isReferenced)
				declareSyntheticVariable(literal, b);
			for(XExpression element: literal.getElements()) {
				if (expressionHelper.isOperatorFromExtension(element, OperatorMapping.MAPPED_TO, ObjectExtensions.class)) {
					XBinaryOperation binaryOperation = (XBinaryOperation) element;
					internalToJavaStatement(binaryOperation.getLeftOperand(), b, true);
					internalToJavaStatement(binaryOperation.getRightOperand(), b, true);
				} else if (isType(element, Pair.class)) {
					internalToJavaStatement(element, b, true);
				}
			}
			LightweightTypeReference keyType = literalType.getTypeArguments().get(0);
			LightweightTypeReference valueType = literalType.getTypeArguments().get(1);
			JvmType mapsClass = findKnownTopLevelType(Maps.class, literal);
			
			final String tempMapName = b.declareSyntheticVariable(Tuples.create(literal,  "_tempMap"), "_tempMap");
			b.newLine();
			serialize(literalType.toTypeReference(), literal, b);
			b.append(" ").append(tempMapName).append(" = ");
			b.append(mapsClass).append(".<").append(keyType).append(", ").append(valueType).append(">newHashMap()").append(";").newLine();
			for(XExpression element: literal.getElements())  {
				if (expressionHelper.isOperatorFromExtension(element, OperatorMapping.MAPPED_TO, ObjectExtensions.class)) {
					XBinaryOperation binaryOperation = (XBinaryOperation) element;
					b.append(tempMapName).append(".put(");
					internalToJavaExpression(binaryOperation.getLeftOperand(), b);
					b.append(", ");
					internalToJavaExpression(binaryOperation.getRightOperand(), b);
					b.append(");").newLine();
				} else if (isType(element, Pair.class)) {
					b.append(tempMapName).append(".put(");
					internalToJavaExpression(element, b);
					b.append(" == null ? null : ");
					internalToJavaExpression(element, b);
					b.append(".getKey()");
					b.append(", ");
					internalToJavaExpression(element, b);
					b.append(" == null ? null : ");
					internalToJavaExpression(element, b);
					b.append(".getValue()");
					b.append(");").newLine();
				}
			}
			if(isReferenced) 
				b.append(getVarName(literal, b)).append(" = ");
			JvmType collectionsClass = findKnownTopLevelType(Collections.class, literal);
			b.append(collectionsClass)
				.append(".<").append(keyType).append(", ").append(valueType)
				.append(">unmodifiableMap(").append(tempMapName).append(");");
		} else {
			for(XExpression element: literal.getElements()) 
				internalToJavaStatement(element, b, true);
		}
	}

	protected boolean isType(XExpression element, Class<?> clazz) {
		return resolveType(element, clazz) != null;
	}

	/* @Nullable */
	protected LightweightTypeReference resolveType(XExpression element, Class<?> clazz) {
		LightweightTypeReference elementType = batchTypeResolver.resolveTypes(element).getActualType(element);
		return elementType != null && elementType.isType(clazz) ? elementType : null;
	}
	
	protected LightweightTypeReference getCollectionElementType(XCollectionLiteral literal) {
		LightweightTypeReference type = getLightweightType(literal);
		if (type == null)
			throw new IllegalStateException();
		if(type.isArray()) {
			LightweightTypeReference result = type.getComponentType();
			if (result == null)
				throw new IllegalStateException();
			return result;
		}
		else if(type.isSubtypeOf(Collection.class) && !type.getTypeArguments().isEmpty()) 
			return type.getTypeArguments().get(0).getInvariantBoundSubstitute();
		JvmType objectType = findKnownTopLevelType(Object.class, literal);
		if (objectType == null) {
			return new UnknownTypeReference(type.getOwner(), "Object");
		}
		return new ParameterizedTypeReference(type.getOwner(), objectType);
	}

	protected void _toJavaExpression(XListLiteral literal, ITreeAppendable b) {
		LightweightTypeReference literalType = batchTypeResolver.resolveTypes(literal).getActualType(literal);
		if (literalType != null && literalType.isArray()) {
			LightweightTypeReference expectedType = batchTypeResolver.resolveTypes(literal).getExpectedType(literal);
			boolean skipTypeName = false;
			if (expectedType != null && expectedType.isArray()) {
				if (canUseArrayInitializer(literal, b)) {
					skipTypeName = true;
				}
			}
			if (!skipTypeName) {
				b.append("new ")
					.append(literalType)
					.append(" ");
			}
			if (literal.getElements().isEmpty()) {
				b.append("{}");
			} else {
				b.append("{ ");
				boolean isFirst = true;
				for(XExpression element: literal.getElements())  {
					if(!isFirst)
						b.append(", ");
					isFirst = false;
					internalToJavaExpression(element, b);
				}
				b.append(" }");
			}
			return;
		} else {
			appendImmutableCollectionExpression(literal, b, "unmodifiableList", Lists.class, "newArrayList");
		}
	}

	protected void _toJavaExpression(XSetLiteral literal, ITreeAppendable b) {
		LightweightTypeReference literalType = batchTypeResolver.resolveTypes(literal).getActualType(literal);
		if(literalType != null && !literalType.isType(Map.class)) 
			appendImmutableCollectionExpression(literal, b, "unmodifiableSet", Sets.class, "newHashSet");
		else
			b.trace(literal, false).append(getVarName(literal, b));
	}
	
	protected void appendImmutableCollectionExpression(XCollectionLiteral literal,
			ITreeAppendable b, String collectionsMethod, Class<?> guavaHelper, String guavaHelperMethod) {
		LightweightTypeReference collectionElementType = getCollectionElementType(literal);
		ITypeReferenceOwner owner = collectionElementType.getOwner();
		JvmType collectionsClass = findKnownTopLevelType(Collections.class, literal);
		JvmType guavaHelperType = findKnownTopLevelType(guavaHelper, literal);
		LightweightTypeReference guavaClass = guavaHelperType == null ? new UnknownTypeReference(owner, guavaHelper.getName()) :new ParameterizedTypeReference(owner, guavaHelperType);
		if (collectionsClass != null) {
			b.append(collectionsClass);
		} else {
			b.append(Collections.class.getSimpleName());
		}
		b.append(".<").append(collectionElementType).append(">").append(collectionsMethod).append("(")
			.append(guavaClass).append(".<").append(collectionElementType).append(">").append(guavaHelperMethod).append("(");
		boolean isFirst = true;
		for(XExpression element: literal.getElements())  {
			if(!isFirst)
				b.append(", ");
			isFirst = false;
			if(element instanceof XNullLiteral) {
				b.append("(").append(collectionElementType).append(")");
			}
			internalToJavaExpression(element, b);
		}
		b.append("))");
		return;
	}
	
	protected boolean canUseArrayInitializer(XListLiteral literal, ITreeAppendable appendable) {
		if (literal.eContainingFeature() == XbasePackage.Literals.XVARIABLE_DECLARATION__RIGHT
			|| literal.eContainingFeature() == XAnnotationsPackage.Literals.XANNOTATION_ELEMENT_VALUE_PAIR__VALUE
			|| literal.eContainingFeature() == XAnnotationsPackage.Literals.XANNOTATION__VALUE
			) {
			return canUseArrayInitializerImpl(literal, appendable);
		}
		return false;
	}
	
	protected boolean canUseArrayInitializerImpl(XListLiteral literal, ITreeAppendable appendable) {
		for(XExpression element: literal.getElements()) {
			if (isVariableDeclarationRequired(element, appendable))
				return false;
		}
		return true;
	}

	@Override
	protected List<XExpression> getActualArguments(XAbstractFeatureCall featureCall) {
		EList<XExpression> actualArguments = featureCall.getActualArguments();
		List<XExpression> normalizedArguments = normalizeBlockExpression(actualArguments);
		return normalizedArguments;
	}
	
	@Override
	protected ITreeAppendable appendTypeArguments(XAbstractFeatureCall call, ITreeAppendable original) {
		if (!call.getTypeArguments().isEmpty()) {
			return super.appendTypeArguments(call, original);
		}
		ILocationData completeLocationData = getLocationWithTypeArguments(call);
		ITreeAppendable completeFeatureCallAppendable = completeLocationData != null ? original.trace(completeLocationData) : original;
		IResolvedTypes resolvedTypes = batchTypeResolver.resolveTypes(call);
		List<LightweightTypeReference> typeArguments = resolvedTypes.getActualTypeArguments(call);
		if (!typeArguments.isEmpty()) {
			for(LightweightTypeReference typeArgument: typeArguments) {
				if (typeArgument.isWildcard()) {
					return completeFeatureCallAppendable;
				}
			}
			completeFeatureCallAppendable.append("<");
			for (int i = 0; i < typeArguments.size(); i++) {
				if (i != 0) {
					completeFeatureCallAppendable.append(", ");
				}
				completeFeatureCallAppendable.append(typeArguments.get(i));
			}
			completeFeatureCallAppendable.append(">");
		}
		return completeFeatureCallAppendable;
	}
	
	@Override
	protected void internalToConvertedExpression(XExpression obj, ITreeAppendable appendable) {
		if (obj instanceof XBlockExpression) {
			_toJavaExpression((XBlockExpression) obj, appendable);
		} else if (obj instanceof XCastedExpression) {
			_toJavaExpression((XCastedExpression) obj, appendable);
		} else if (obj instanceof XClosure) {
			_toJavaExpression((XClosure) obj, appendable);
		} else if (obj instanceof XAnnotation) {
			_toJavaExpression((XAnnotation) obj, appendable);
		} else if (obj instanceof XConstructorCall) {
			_toJavaExpression((XConstructorCall) obj, appendable);
		} else if (obj instanceof XIfExpression) {
			_toJavaExpression((XIfExpression) obj, appendable);
		} else if (obj instanceof XInstanceOfExpression) {
			_toJavaExpression((XInstanceOfExpression) obj, appendable);
		} else if (obj instanceof XSwitchExpression) {
			_toJavaExpression((XSwitchExpression) obj, appendable);
		} else if (obj instanceof XTryCatchFinallyExpression) {
			_toJavaExpression((XTryCatchFinallyExpression) obj, appendable);
		} else if (obj instanceof XListLiteral) {
			_toJavaExpression((XListLiteral) obj, appendable);
		} else if (obj instanceof XSetLiteral) {
			_toJavaExpression((XSetLiteral) obj, appendable);
		} else if (obj instanceof XSynchronizedExpression) {
			_toJavaExpression((XSynchronizedExpression) obj, appendable);
		} else {
			super.internalToConvertedExpression(obj, appendable);
		}
	}
	
	@Override
	protected void doInternalToJavaStatement(XExpression obj, ITreeAppendable appendable, boolean isReferenced) {
		if (obj instanceof XBlockExpression) {
			_toJavaStatement((XBlockExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XCastedExpression) {
			_toJavaStatement((XCastedExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XClosure) {
			_toJavaStatement((XClosure) obj, appendable, isReferenced);
		} else if (obj instanceof XConstructorCall) {
			_toJavaStatement((XConstructorCall) obj, appendable, isReferenced);
		} else if (obj instanceof XDoWhileExpression) {
			_toJavaStatement((XDoWhileExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XForLoopExpression) {
			_toJavaStatement((XForLoopExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XBasicForLoopExpression) {
			_toJavaStatement((XBasicForLoopExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XIfExpression) {
			_toJavaStatement((XIfExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XInstanceOfExpression) {
			_toJavaStatement((XInstanceOfExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XReturnExpression) {
			_toJavaStatement((XReturnExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XSwitchExpression) {
			_toJavaStatement((XSwitchExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XThrowExpression) {
			_toJavaStatement((XThrowExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XTryCatchFinallyExpression) {
			_toJavaStatement((XTryCatchFinallyExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XVariableDeclaration) {
			_toJavaStatement((XVariableDeclaration) obj, appendable, isReferenced);
		} else if (obj instanceof XWhileExpression) {
			_toJavaStatement((XWhileExpression) obj, appendable, isReferenced);
		} else if (obj instanceof XListLiteral) {
			_toJavaStatement((XListLiteral) obj, appendable, isReferenced);
		} else if (obj instanceof XSetLiteral) {
			_toJavaStatement((XSetLiteral) obj, appendable, isReferenced);
		} else if (obj instanceof XSynchronizedExpression) {
			_toJavaStatement((XSynchronizedExpression) obj, appendable, isReferenced);
		} else {
			super.doInternalToJavaStatement(obj, appendable, isReferenced);
		}
	}
	
	protected void _toJavaStatement(XBlockExpression expr, ITreeAppendable b, boolean isReferenced) {
		b = b.trace(expr, false);
		if (expr.getExpressions().isEmpty())
			return;
		if (expr.getExpressions().size()==1) {
			internalToJavaStatement(expr.getExpressions().get(0), b, isReferenced);
			return;
		}
		if (isReferenced)
			declareSyntheticVariable(expr, b);
		boolean needsBraces = isReferenced || !bracesAreAddedByOuterStructure(expr);
		if (needsBraces) {
			b.newLine().append("{").increaseIndentation();
			b.openPseudoScope();
		}
		final EList<XExpression> expressions = expr.getExpressions();
		for (int i = 0; i < expressions.size(); i++) {
			XExpression ex = expressions.get(i);
			if (i < expressions.size() - 1) {
				internalToJavaStatement(ex, b, false);
			} else {
				internalToJavaStatement(ex, b, isReferenced);
				if (isReferenced) {
					b.newLine().append(getVarName(expr, b)).append(" = ");
					internalToConvertedExpression(ex, b, getLightweightType(expr));
					b.append(";");
				}
			}
		}
		if (needsBraces) {
			b.closeScope();
			b.decreaseIndentation().newLine().append("}");
		}
	}
	
	protected boolean bracesAreAddedByOuterStructure(XExpression expression) {
		EObject container = expression.eContainer();
		if (container instanceof XTryCatchFinallyExpression 
				|| container instanceof XIfExpression
				|| container instanceof XClosure) {
			return true;
		}
		if (container instanceof XBlockExpression) {
			XBlockExpression blockExpression = (XBlockExpression) container;
			EList<XExpression> expressions = blockExpression.getExpressions();
			if (expressions.size() == 1 && expressions.get(0) == expression) {
				return bracesAreAddedByOuterStructure(blockExpression);
			}
		}
		if (!(container instanceof XExpression)) {
			return true;
		}
		return false;
	}

	protected void _toJavaExpression(XBlockExpression expr, ITreeAppendable b) {
		if (expr.getExpressions().isEmpty()) {
			b.append("null");
			return;
		}
		if (expr.getExpressions().size()==1) {
			// conversion was already performed for single expression blocks
			internalToConvertedExpression(expr.getExpressions().get(0), b, null);
			return;
		}
		b = b.trace(expr, false);
		b.append(getVarName(expr, b));
	}

	protected void _toJavaStatement(XTryCatchFinallyExpression expr, ITreeAppendable outerAppendable, boolean isReferenced) {
		ITreeAppendable b = outerAppendable.trace(expr, false);
		if (isReferenced && !isPrimitiveVoid(expr)) {
			declareSyntheticVariable(expr, b);
		}
		b.newLine().append("try {").increaseIndentation();
		final boolean canBeReferenced = isReferenced && !isPrimitiveVoid(expr.getExpression());
		internalToJavaStatement(expr.getExpression(), b, canBeReferenced);
		if (canBeReferenced) {
			b.newLine().append(getVarName(expr, b)).append(" = ");
			internalToConvertedExpression(expr.getExpression(), b, getLightweightType(expr));
			b.append(";");
		}
		b.decreaseIndentation().newLine().append("}");
		appendCatchAndFinally(expr, b, isReferenced);
	}

	protected void appendCatchAndFinally(XTryCatchFinallyExpression expr, ITreeAppendable b, boolean isReferenced) {
		final EList<XCatchClause> catchClauses = expr.getCatchClauses();
		if (!catchClauses.isEmpty()) {
			String variable = b.declareSyntheticVariable(Tuples.pair(expr, "_catchedThrowable"), "_t");
			b.append(" catch (final Throwable ").append(variable).append(") ");
			b.append("{").increaseIndentation();
			b.newLine();
			Iterator<XCatchClause> iterator = catchClauses.iterator();
			while (iterator.hasNext()) {
				XCatchClause catchClause = iterator.next();
				ITreeAppendable catchClauseAppendable = b.trace(catchClause);
				appendCatchClause(catchClause, isReferenced, variable, catchClauseAppendable);
				if (iterator.hasNext()) {
					b.append(" else ");
				}
			}
			b.append(" else {");
			b.increaseIndentation();
			final JvmType sneakyThrowType = findKnownTopLevelType(Exceptions.class, expr);
			if (sneakyThrowType == null) {
				b.append("COMPILE ERROR : '"+Exceptions.class.getCanonicalName()+"' could not be found on the classpath!");
			} else {
				b.newLine().append("throw ");
				b.append(sneakyThrowType);
				b.append(".sneakyThrow(");
				b.append(variable);
				b.append(");");
			}
			b.decreaseIndentation();
			b.newLine().append("}");
			b.decreaseIndentation();
			b.newLine().append("}");
		}
		final XExpression finallyExp = expr.getFinallyExpression();
		if (finallyExp != null) {
			b.append(" finally {").increaseIndentation();
			internalToJavaStatement(finallyExp, b, false);
			b.decreaseIndentation().newLine().append("}");
		}
	}

	protected void appendCatchClause(XCatchClause catchClause, boolean parentIsReferenced, String parentVariable,
			ITreeAppendable appendable) {
		JvmTypeReference type = catchClause.getDeclaredParam().getParameterType();
		final String declaredParamName = makeJavaIdentifier(catchClause.getDeclaredParam().getName());
		final String name = appendable.declareVariable(catchClause.getDeclaredParam(), declaredParamName);
		appendable.append("if (").append(parentVariable).append(" instanceof ");
		serialize(type, catchClause, appendable);
		appendable.append(") ").append("{");
		appendable.increaseIndentation();
		ITreeAppendable withDebugging = appendable.trace(catchClause, true);
		ITreeAppendable parameterAppendable = withDebugging.trace(catchClause.getDeclaredParam());
		appendCatchClauseParameter(catchClause, type, name, parameterAppendable.newLine());
		withDebugging.append(" = (");
		serialize(type, catchClause, withDebugging);
		withDebugging.append(")").append(parentVariable).append(";");
		final boolean canBeReferenced = parentIsReferenced && ! isPrimitiveVoid(catchClause.getExpression());
		internalToJavaStatement(catchClause.getExpression(), withDebugging, canBeReferenced);
		if (canBeReferenced) {
			appendable.newLine().append(getVarName(catchClause.eContainer(), appendable)).append(" = ");
			internalToConvertedExpression(catchClause.getExpression(), appendable, getLightweightType((XExpression) catchClause.eContainer()));
			appendable.append(";");
		}
		appendable.decreaseIndentation();
		appendable.newLine().append("}");
	}

	protected void appendCatchClauseParameter(XCatchClause catchClause, JvmTypeReference parameterType, final String parameterName, ITreeAppendable appendable) {
		appendable.append("final ");
		serialize(parameterType, catchClause, appendable);
		appendable.append(" ");
		appendable.trace(catchClause.getDeclaredParam(), TypesPackage.Literals.JVM_FORMAL_PARAMETER__NAME, 0).append(parameterName);
	}

	protected void _toJavaExpression(XTryCatchFinallyExpression expr, ITreeAppendable b) {
		b.trace(expr, false).append(getVarName(expr, b));
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XThrowExpression expr, ITreeAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getExpression(), b, true);
		b.newLine().append("throw ");
		internalToJavaExpression(expr.getExpression(), b);
		b.append(";");
	}

	protected void _toJavaExpression(XInstanceOfExpression expr, ITreeAppendable b) {
		b.append("(");
		internalToJavaExpression(expr.getExpression(), b);
		b.append(" instanceof ");
		serialize(expr.getType(), expr, b);
		b.append(")");
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XInstanceOfExpression expr, ITreeAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getExpression(), b, true);
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XVariableDeclaration varDeclaration, ITreeAppendable b, boolean isReferenced) {
		if (varDeclaration.getRight() != null) {
			internalToJavaStatement(varDeclaration.getRight(), b, true);
		}
		b.newLine();
		LightweightTypeReference type = appendVariableTypeAndName(varDeclaration, b);
		b.append(" = ");
		if (varDeclaration.getRight() != null) {
			internalToConvertedExpression(varDeclaration.getRight(), b, type);
		} else {
			appendDefaultLiteral(b, type);
		}
		b.append(";");
	}

	protected LightweightTypeReference appendVariableTypeAndName(XVariableDeclaration varDeclaration, ITreeAppendable appendable) {
		if (!varDeclaration.isWriteable()) {
			appendable.append("final ");
		}
		LightweightTypeReference type = null;
		if (varDeclaration.getType() != null) {
			serialize(varDeclaration.getType(), varDeclaration, appendable);
			type = getLightweightType((JvmIdentifiableElement) varDeclaration);
		} else {
			type = getLightweightType(varDeclaration.getRight());
			if (type.isAny()) {
				type = getTypeForVariableDeclaration(varDeclaration.getRight());
			}
			appendable.append(type);
		}
		appendable.append(" ");
		appendable.append(appendable.declareVariable(varDeclaration, makeJavaIdentifier(varDeclaration.getName())));
		return type;
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XWhileExpression expr, ITreeAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getPredicate(), b, true);
		final String varName = b.declareSyntheticVariable(expr, "_while");
		b.newLine().append("boolean ").append(varName).append(" = ");
		internalToJavaExpression(expr.getPredicate(), b);
		b.append(";");
		b.newLine().append("while (");
		b.append(varName);
		b.append(") {").increaseIndentation();
		b.openPseudoScope();
		internalToJavaStatement(expr.getBody(), b, false);
		if (!earlyExitComputer.isEarlyExit(expr.getBody())) {
			internalToJavaStatement(expr.getPredicate(), b, true);
			b.newLine();
			b.append(varName).append(" = ");
			internalToJavaExpression(expr.getPredicate(), b);
			b.append(";");
		}
		b.closeScope();
		b.decreaseIndentation().newLine().append("}");
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature  
	 */
	protected void _toJavaStatement(XDoWhileExpression expr, ITreeAppendable b, boolean isReferenced) {
		String variable = b.declareSyntheticVariable(expr, "_dowhile");
		b.newLine().append("boolean ").append(variable).append(" = false;");
		b.newLine().append("do {").increaseIndentation();
		internalToJavaStatement(expr.getBody(), b, false);
		internalToJavaStatement(expr.getPredicate(), b, true);
		b.newLine();
		if (!earlyExitComputer.isEarlyExit(expr.getBody())) {
			b.append(variable).append(" = ");
			internalToJavaExpression(expr.getPredicate(), b);
			b.append(";");
		}
		b.decreaseIndentation().newLine().append("} while(");
		b.append(variable);
		b.append(");");
	}
	
	protected void _toJavaStatement(XBasicForLoopExpression expr, ITreeAppendable b, boolean isReferenced) {
		if (canCompileToJavaBasicForStatement(expr, b)) {
			toJavaBasicForStatement(expr, b, isReferenced);
		} else {
			toJavaWhileStatement(expr, b, isReferenced);
		}
	}

	protected boolean canCompileToJavaBasicForStatement(XBasicForLoopExpression expr, ITreeAppendable b) {
		EList<XExpression> initExpressions = expr.getInitExpressions();
		XExpression firstInitExpression = IterableExtensions.head(initExpressions);
		if (initExpressions.size() == 1 && firstInitExpression instanceof XVariableDeclaration) {
			XVariableDeclaration variableDeclaration = (XVariableDeclaration) firstInitExpression;
			XExpression right = variableDeclaration.getRight();
			if (right != null && !canCompileToJavaExpression(right, b)) {
				return false;
			}
		} else {
			for (XExpression expression : initExpressions) {
				if (!canCompileToJavaExpression(expression, b)) {
					return false;
				}
			}
		}
		XExpression predicate = expr.getExpression();
		if (predicate != null && !canCompileToJavaExpression(predicate, b)) {
			return false;
		}
		for (XExpression expression : expr.getUpdateExpressions()) {
			if (!canCompileToJavaExpression(expression, b)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void toJavaBasicForStatement(XBasicForLoopExpression expr, ITreeAppendable b, boolean isReferenced) {
		ITreeAppendable loopAppendable = b.trace(expr);
		loopAppendable.openPseudoScope();
		loopAppendable.newLine().append("for (");
		
		EList<XExpression> initExpressions = expr.getInitExpressions();
		XExpression firstInitExpression = IterableExtensions.head(initExpressions);
		if (firstInitExpression instanceof XVariableDeclaration) {
			XVariableDeclaration variableDeclaration = (XVariableDeclaration) firstInitExpression;
			LightweightTypeReference type = appendVariableTypeAndName(variableDeclaration, loopAppendable);
			loopAppendable.append(" = ");
			if (variableDeclaration.getRight() != null) {
				compileAsJavaExpression(variableDeclaration.getRight(), loopAppendable, type);
			} else {
				appendDefaultLiteral(loopAppendable, type);
			}
		} else {
			for (int i = 0; i < initExpressions.size(); i++) {
				if (i != 0) {
					loopAppendable.append(", ");
				}
				XExpression initExpression = initExpressions.get(i);
				compileAsJavaExpression(initExpression, loopAppendable, getLightweightType(initExpression));
			}
		}
		
		loopAppendable.append(";");
		
		XExpression expression = expr.getExpression();
		if (expression != null) {
			loopAppendable.append(" ");
			internalToJavaExpression(expression, loopAppendable);
		}
		loopAppendable.append(";");
		
		EList<XExpression> updateExpressions = expr.getUpdateExpressions();
		for (int i = 0; i < updateExpressions.size(); i++) {
			if (i != 0) {
				loopAppendable.append(",");
			}
			loopAppendable.append(" ");
			XExpression updateExpression = updateExpressions.get(i);
			internalToJavaExpression(updateExpression, loopAppendable);
		}
		loopAppendable.append(") {").increaseIndentation();
		
		XExpression eachExpression = expr.getEachExpression();
		internalToJavaStatement(eachExpression, loopAppendable, false);
		
		loopAppendable.decreaseIndentation().newLine().append("}");
		loopAppendable.closeScope();
	}

	protected void toJavaWhileStatement(XBasicForLoopExpression expr, ITreeAppendable b, boolean isReferenced) {
		ITreeAppendable loopAppendable = b.trace(expr);
		
		boolean needBraces = !bracesAreAddedByOuterStructure(expr);
		if (needBraces) {
			loopAppendable.newLine().increaseIndentation().append("{");
			loopAppendable.openPseudoScope();
		}
		
		EList<XExpression> initExpressions = expr.getInitExpressions();
		for (int i = 0; i < initExpressions.size(); i++) {
			XExpression initExpression = initExpressions.get(i);
			if (i < initExpressions.size() - 1) {
				internalToJavaStatement(initExpression, loopAppendable, false);
			} else {
				internalToJavaStatement(initExpression, loopAppendable, isReferenced);
				if (isReferenced) {
					loopAppendable.newLine().append(getVarName(expr, loopAppendable)).append(" = (");
					internalToConvertedExpression(initExpression, loopAppendable, getLightweightType(expr));
					loopAppendable.append(");");
				}
			}
		}

		final String varName = loopAppendable.declareSyntheticVariable(expr, "_while");
		
		XExpression expression = expr.getExpression();
		if (expression != null) {
			internalToJavaStatement(expression, loopAppendable, true);
			loopAppendable.newLine().append("boolean ").append(varName).append(" = ");
			internalToJavaExpression(expression, loopAppendable);
			loopAppendable.append(";");
		} else {
			loopAppendable.newLine().append("boolean ").append(varName).append(" = true;");
		}
		loopAppendable.newLine();
		loopAppendable.append("while (");
		loopAppendable.append(varName);
		loopAppendable.append(") {").increaseIndentation();
		loopAppendable.openPseudoScope();
		
		XExpression eachExpression = expr.getEachExpression();
		internalToJavaStatement(eachExpression, loopAppendable, false);
		
		EList<XExpression> updateExpressions = expr.getUpdateExpressions();
		if (!updateExpressions.isEmpty()) {
			for (XExpression updateExpression : updateExpressions) {
				internalToJavaStatement(updateExpression, loopAppendable, false);
			}
		}
		
		if (!earlyExitComputer.isEarlyExit(eachExpression)) {
			if (expression != null) {
				internalToJavaStatement(expression, loopAppendable, true);
				loopAppendable.newLine().append(varName).append(" = ");
				internalToJavaExpression(expression, loopAppendable);
				loopAppendable.append(";");
			} else {
				loopAppendable.newLine().append(varName).append(" = true;");
			}
		}
		
		loopAppendable.closeScope();
		loopAppendable.decreaseIndentation().newLine().append("}");
		
		if (needBraces) {
			loopAppendable.closeScope();
			loopAppendable.decreaseIndentation().newLine().append("}");
		}
	}

	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XForLoopExpression expr, ITreeAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getForExpression(), b, true);
		b.newLine();
		ITreeAppendable loopAppendable = b.trace(expr);
		loopAppendable.append("for (");
		ITreeAppendable parameterAppendable = loopAppendable.trace(expr.getDeclaredParam());
		appendForLoopParameter(expr, parameterAppendable);
		loopAppendable.append(" : ");
		internalToJavaExpression(expr.getForExpression(), loopAppendable);
		loopAppendable.append(") {").increaseIndentation();
		internalToJavaStatement(expr.getEachExpression(), loopAppendable, false);
		loopAppendable.decreaseIndentation().newLine().append("}");
	}

	protected void appendForLoopParameter(XForLoopExpression expr, ITreeAppendable appendable) {
		appendable.append("final ");
		JvmTypeReference paramType = getForLoopParameterType(expr);
		serialize(paramType, expr, appendable);
		appendable.append(" ");
		final String name = makeJavaIdentifier(expr.getDeclaredParam().getName());
		String varName = appendable.declareVariable(expr.getDeclaredParam(), name);
		appendable.trace(expr.getDeclaredParam(), TypesPackage.Literals.JVM_FORMAL_PARAMETER__NAME, 0).append(varName);
	}

	protected JvmTypeReference getForLoopParameterType(XForLoopExpression expr) {
		JvmFormalParameter declaredParam = expr.getDeclaredParam();
		return getParameterType(declaredParam);
	}

	protected JvmTypeReference getParameterType(JvmFormalParameter declaredParam) {
		JvmTypeReference declaredType = declaredParam.getParameterType();
		if (declaredType != null) {
			return declaredType;
		}
		return getType(declaredParam);
	}

	protected void _toJavaStatement(final XConstructorCall expr, ITreeAppendable b, final boolean isReferenced) {
		for (XExpression arg : expr.getArguments()) {
			prepareExpression(arg, b);
		}
		
		if (!isReferenced) {
			b.newLine();
			constructorCallToJavaExpression(expr, b);
			b.append(";");
		} else if (isVariableDeclarationRequired(expr, b)) {
			Later later = new Later() {
				public void exec(ITreeAppendable appendable) {
					constructorCallToJavaExpression(expr, appendable);
				}
			};
			declareFreshLocalVariable(expr, b, later);
		}
	}
	
	protected void constructorCallToJavaExpression(final XConstructorCall expr, ITreeAppendable b) {
		ILocationData locationWithNewKeyword = getLocationWithNewKeyword(expr);
		ITreeAppendable appendableWithNewKeyword = locationWithNewKeyword != null ? b.trace(locationWithNewKeyword) : b;
		appendableWithNewKeyword.append("new ");
		IResolvedTypes resolvedTypes = batchTypeResolver.resolveTypes(expr);
		List<LightweightTypeReference> typeArguments = resolvedTypes.getActualTypeArguments(expr);
		JvmConstructor constructor = expr.getConstructor();
		List<JvmTypeParameter> constructorTypeParameters = constructor.getTypeParameters();
		boolean hasTypeArguments = !typeArguments.isEmpty() && (featureLinkHelper.getDeclaredTypeParameters(constructor).size() == typeArguments.size());
		List<JvmTypeReference> explicitTypeArguments = expr.getTypeArguments();
		List<LightweightTypeReference> constructorTypeArguments = Collections.emptyList();
		if (hasTypeArguments) {
			constructorTypeArguments = typeArguments.subList(0, constructorTypeParameters.size());
			typeArguments = typeArguments.subList(constructorTypeParameters.size(), typeArguments.size());
			hasTypeArguments = !typeArguments.isEmpty();
			for(LightweightTypeReference typeArgument: typeArguments) {
				if (typeArgument.isWildcard()) {
					// cannot serialize wildcard as constructor type argument in Java5 as explicit type argument, skip all
					hasTypeArguments = false;
					break;
					// diamond operator would work in later versions
				}
			}
			for(LightweightTypeReference typeArgument: constructorTypeArguments) {
				if (typeArgument.isWildcard()) {
					// cannot serialize wildcard as constructor type argument in Java5 as explicit type argument, skip all
					constructorTypeArguments = Collections.emptyList();
					break;
					// diamond operator would work in later versions
				}
			}
		}
		if (!constructorTypeArguments.isEmpty()) {
			appendableWithNewKeyword.append("<");
			for(int i = 0; i < constructorTypeArguments.size(); i++) {
				if (i != 0) {
					appendableWithNewKeyword.append(", ");
				}
				appendableWithNewKeyword.append(constructorTypeArguments.get(i));
			}
			appendableWithNewKeyword.append(">");
		}
		ITreeAppendable typeAppendable = appendableWithNewKeyword.trace(expr, XbasePackage.Literals.XCONSTRUCTOR_CALL__CONSTRUCTOR, 0);
		appendConstructedTypeName(expr, typeAppendable);
		if (hasTypeArguments || (expr.isAnonymousClassConstructorCall() && !explicitTypeArguments.isEmpty() && ((JvmGenericType) constructor.getDeclaringType()).isAnonymous())) {
			if (typeArguments.isEmpty()) {
				LightweightTypeReference createdType = resolvedTypes.getActualType(expr);
				typeArguments = createdType.getNamedType().getTypeArguments();
			}
			if (!typeArguments.isEmpty()) {
				typeAppendable.append("<");
				for(int i = 0; i < typeArguments.size(); i++) {
					if (i != 0) {
						typeAppendable.append(", ");
					}
					if (explicitTypeArguments.isEmpty()) {
						typeAppendable.append(typeArguments.get(i));
					} else {
						typeAppendable.trace(explicitTypeArguments.get(i), false).append(typeArguments.get(i));
					}
				}
				typeAppendable.append(">");
			}
		}
		b.append("(");
		appendArguments(expr.getArguments(), b);
		b.append(")");
	}

	protected void appendConstructedTypeName(XConstructorCall constructorCall, ITreeAppendable typeAppendable) {
		JvmDeclaredType type = constructorCall.getConstructor().getDeclaringType();
		if (type instanceof JvmGenericType && ((JvmGenericType) type).isAnonymous()) {
			typeAppendable.append(type.getSuperTypes().get(0).getType());
		} else {
			typeAppendable.append(constructorCall.getConstructor().getDeclaringType());
		}
	}
	
	/* @Nullable */
	protected ILocationData getLocationWithNewKeyword(XConstructorCall call) {
		final ICompositeNode startNode = NodeModelUtils.getNode(call);
		if (startNode != null) {
			List<INode> resultNodes = Lists.newArrayList();
			for (INode child : startNode.getChildren()) {
				if (child.getGrammarElement() instanceof Keyword && "(".equals(child.getText()))
					break;
				resultNodes.add(child);
			}
			return toLocationData(resultNodes);
		}
		return null;
	}

	protected void _toJavaExpression(XConstructorCall expr, ITreeAppendable b) {
		String varName = getReferenceName(expr, b);
		if (varName != null) {
			b.trace(expr, false).append(varName);
		} else {
			constructorCallToJavaExpression(expr, b);
		}
	}
	
	/**
	 * @param isReferenced unused in this context but necessary for dispatch signature 
	 */
	protected void _toJavaStatement(XReturnExpression expr, ITreeAppendable b, boolean isReferenced) {
		if (expr.getExpression()!=null) {
			internalToJavaStatement(expr.getExpression(), b, true);
			b.newLine().append("return ");
			LightweightTypeReference returnTypeToCompile = findRealReturnType(expr);
			internalToConvertedExpression(expr.getExpression(), b, returnTypeToCompile);
			b.append(";");
		} else {
			b.newLine().append("return;");
		}
	}
	
	private LightweightTypeReference findRealReturnType(XExpression expression) {
		if (expression == null)
			return null;
		JvmIdentifiableElement logicalContainer = getLogicalContainerProvider().getLogicalContainer(expression);
		if (logicalContainer instanceof JvmOperation) {
			return getLightweightType(logicalContainer);
		}
		if (expression instanceof XClosure) {
			IResolvedTypes resolvedTypes = batchTypeResolver.resolveTypes(expression);
			LightweightTypeReference type = resolvedTypes.getExpectedType(expression);
			if (type == null) {
				type = resolvedTypes.getActualType(expression);
			}
			if (type == null) {
				return null;
			}
			FunctionTypeReference functionType = type.tryConvertToFunctionTypeReference(false);
			if (functionType != null) {
				return functionType.getReturnType();
			}
			return null;
		}
		XExpression containerExpression = EcoreUtil2.getContainerOfType(expression.eContainer(), XExpression.class);
		if (containerExpression == null) {
			LightweightTypeReference returnType = getLightweightReturnType(expression);
			return returnType;
		}
		return findRealReturnType(containerExpression);
	}
	
	protected void _toJavaExpression(XCastedExpression expr, ITreeAppendable b) {
		b.append("((");
		serialize(expr.getType(), expr, b);
		b.append(") ");
		internalToConvertedExpression(expr.getTarget(), b, getLightweightType(expr));
		b.append(")");
	}

	protected void _toJavaStatement(XCastedExpression expr, ITreeAppendable b, boolean isReferenced) {
		internalToJavaStatement(expr.getTarget(), b, isReferenced);
	}

	protected void _toJavaStatement(XIfExpression expr, ITreeAppendable b, boolean isReferenced) {
		if (isReferenced)
			declareSyntheticVariable(expr, b);
		internalToJavaStatement(expr.getIf(), b, true);
		b.newLine().append("if (");
		internalToJavaExpression(expr.getIf(), b);
		b.append(") {").increaseIndentation();
		final boolean canBeReferenced = isReferenced && !isPrimitiveVoid(expr.getThen());
		internalToJavaStatement(expr.getThen(), b, canBeReferenced);
		if (canBeReferenced) {
			b.newLine();
			b.append(getVarName(expr, b));
			b.append(" = ");
			internalToConvertedExpression(expr.getThen(), b, getLightweightType(expr));
			b.append(";");
		}
		b.decreaseIndentation().newLine().append("}");
		if (expr.getElse() != null) {
			b.append(" else {").increaseIndentation();
			final boolean canElseBeReferenced = isReferenced && !isPrimitiveVoid(expr.getElse());
			internalToJavaStatement(expr.getElse(), b, canElseBeReferenced);
			if (canElseBeReferenced) {
				b.newLine();
				b.append(getVarName(expr, b));
				b.append(" = ");
				internalToConvertedExpression(expr.getElse(), b, getLightweightType(expr));
				b.append(";");
			}
			b.decreaseIndentation().newLine().append("}");
		}
	}

	protected void _toJavaExpression(XIfExpression expr, ITreeAppendable b) {
		b.trace(expr, false).append(getVarName(expr, b));
	}

	protected void _toJavaStatement(XSwitchExpression expr, ITreeAppendable b, boolean isReferenced) {
		if (isCompiledToJavaSwitch(expr)) {
			_toJavaSwitchStatement(expr, b, isReferenced);
		} else {
			_toJavaIfElseStatement(expr, b, isReferenced);
		}
	}

	protected void _toJavaSwitchStatement(XSwitchExpression expr, ITreeAppendable b, boolean isReferenced) {
		final LightweightTypeReference switchType = batchTypeResolver.resolveTypes(expr).getActualType(expr.getSwitch());
		final boolean enumeration = switchType.isSubtypeOf(Enum.class);
		final boolean needNullCheck = enumeration || switchType.isWrapper();
		
		final String switchResultName = declareSwitchResultVariable(expr, b, isReferenced);
		internalToJavaStatement(expr.getSwitch(), b, true);
		final String variableName = declareLocalVariable(expr, b);
		
		if (needNullCheck) {
			b.newLine().append("if (").append(variableName).append(" != null) {").increaseIndentation();
		}
		b.newLine().append("switch (").append(variableName).append(") {").increaseIndentation();
		for (XCasePart casePart : expr.getCases()) {
			ITreeAppendable caseAppendable = b.trace(casePart, true);
			caseAppendable.newLine().increaseIndentation().append("case ");
			
			ITreeAppendable conditionAppendable = caseAppendable.trace(casePart.getCase(), true);
			if (!enumeration) {
				internalToJavaExpression(casePart.getCase(), conditionAppendable);
			} else {
				XAbstractFeatureCall featureCall = (XAbstractFeatureCall) casePart.getCase();
				JvmEnumerationLiteral enumerationLiteral = (JvmEnumerationLiteral) featureCall.getFeature();
				conditionAppendable.append(enumerationLiteral.getSimpleName());
			}
			
			caseAppendable.append(":");
			XExpression then = casePart.getThen();
			if (then != null) {
				executeThenPart(expr, switchResultName, then, caseAppendable, isReferenced);
			
				if (!earlyExitComputer.isEarlyExit(then)) {
					caseAppendable.newLine().append("break;");
				}
			}
			caseAppendable.decreaseIndentation();
		}
		if (expr.getDefault() != null || enumeration) {
			ILocationData location = getLocationOfDefault(expr);
			ITreeAppendable defaultAppendable = location != null ? b.trace(location) : b;
			
			defaultAppendable.newLine().increaseIndentation().append("default:");

			if (expr.getDefault() != null) {
				defaultAppendable.openPseudoScope();
				executeThenPart(expr, switchResultName, expr.getDefault(), defaultAppendable, isReferenced);
				defaultAppendable.closeScope();
			}
			
			if (!earlyExitComputer.isEarlyExit(expr.getDefault())) {
				defaultAppendable.newLine().append("break;");
			}
			defaultAppendable.decreaseIndentation();
		}
		b.decreaseIndentation().newLine().append("}");
		if (needNullCheck) {
			b.decreaseIndentation().newLine().append("}");
			if (expr.getDefault() != null) {
				b.append(" else {").increaseIndentation();

				ILocationData location = getLocationOfDefault(expr);
				ITreeAppendable defaultAppendable = location != null ? b.trace(location) : b;
				executeThenPart(expr, switchResultName, expr.getDefault(), defaultAppendable, isReferenced);
				
				b.decreaseIndentation().newLine().append("}");
			}
		}
	}

	protected String declareLocalVariable(XSwitchExpression expr, ITreeAppendable b) {
		// declare local var for the switch expression
		String variableName = getSwitchLocalVariableName(expr, b); 
		if (variableName != null) {
			return variableName;
		}
		String name = createSwitchLocalVariableName(expr);
		JvmTypeReference variableType = getSwitchLocalVariableType(expr);
		b.newLine().append("final ");
		serialize(variableType, expr, b);
		b.append(" ");
		variableName = declareAndAppendSwitchSyntheticLocalVariable(expr, name, b);
		b.append(" = ");
		internalToJavaExpression(expr.getSwitch(), b);
		b.append(";");
		return variableName;
	}

	protected String getSwitchLocalVariableName(XSwitchExpression expr, ITreeAppendable b) {
		JvmFormalParameter declaredParam = expr.getDeclaredParam();
		if (declaredParam != null) {
			if (b.hasName(declaredParam)) {
				return b.getName(declaredParam);
			}
			return null;
		}
		XExpression switchExpression = expr.getSwitch();
		if (b.hasName(switchExpression)) {
			return b.getName(switchExpression);
		} 
		if (switchExpression instanceof XFeatureCall) {
			XFeatureCall featureCall = (XFeatureCall) switchExpression;
			JvmIdentifiableElement feature = featureCall.getFeature();
			if (b.hasName(feature)) {
				return b.getName(feature);
			}
		}
		return null;
	}

	protected String declareAndAppendSwitchSyntheticLocalVariable(XSwitchExpression expr, String name, ITreeAppendable b) {
		JvmFormalParameter declaredParam = expr.getDeclaredParam();
		if (declaredParam == null) {
			String declareSyntheticVariable = b.declareSyntheticVariable(expr, name);
			b.append(declareSyntheticVariable);
			return declareSyntheticVariable;
		}
		String declareSyntheticVariable = b.declareSyntheticVariable(declaredParam, name);
		b.trace(declaredParam, TypesPackage.Literals.JVM_FORMAL_PARAMETER__NAME, 0).append(declareSyntheticVariable);
		return declareSyntheticVariable;
	}

	protected String createSwitchLocalVariableName(XSwitchExpression expr) {
		String name = getSwitchLocalVariableSimpleName(expr);
		if (name != null) { 
			return makeJavaIdentifier(name);
		}
		// define synthetic name
		return "_switchValue";
	}

	protected String getSwitchLocalVariableSimpleName(XSwitchExpression expr) {
		IdentifiableSimpleNameProvider nameProvider = getNameProvider();
		String varName = nameProvider.getSimpleName(expr.getDeclaredParam());
		if (varName != null) {
			return varName;
		}
		XExpression expression = expr.getSwitch();
		if (!(expression instanceof XFeatureCall)) {
			return null;
		}
		XFeatureCall featureCall = (XFeatureCall) expression;
		JvmIdentifiableElement feature = featureCall.getFeature();
		return nameProvider.getSimpleName(feature);
	}

	protected JvmTypeReference getSwitchLocalVariableType(XSwitchExpression expr) {
		JvmFormalParameter declaredParam = expr.getDeclaredParam();
		if (declaredParam == null) {
			return getType(expr.getSwitch());
		} 
		return getParameterType(declaredParam);
	}

	protected String declareSwitchResultVariable(XSwitchExpression expr, ITreeAppendable b, boolean isReferenced) {
		LightweightTypeReference type = getTypeForVariableDeclaration(expr);
		String switchResultName = b.declareSyntheticVariable(getSwitchExpressionKey(expr), "_switchResult");
		if (isReferenced) {
			b.newLine();
			b.append(type);
			b.append(" ").append(switchResultName).append(" = ");
			b.append(getDefaultValueLiteral(expr));
			b.append(";");
		}
		return switchResultName;
	}

	protected void executeThenPart(XSwitchExpression expr, String switchResultName, XExpression then,
			ITreeAppendable b, boolean isReferenced) {
		final boolean canBeReferenced = isReferenced && !isPrimitiveVoid(then);
		internalToJavaStatement(then, b, canBeReferenced);
		if (canBeReferenced) {
			b.newLine().append(switchResultName).append(" = ");
			internalToConvertedExpression(then, b, getLightweightType(expr));
			b.append(";");
		}
	}

	protected void _toJavaIfElseStatement(XSwitchExpression expr, ITreeAppendable b, boolean isReferenced) {
		String switchResultName = declareSwitchResultVariable(expr, b, isReferenced);
		internalToJavaStatement(expr.getSwitch(), b, true);
		
		// declare the matched variable outside the pseudo scope
		String matchedVariable = b.declareSyntheticVariable(Tuples.pair(expr, "matches"), "_matched");
		
		String variableName = declareLocalVariable(expr, b);

		// declare 'boolean matched' to check whether a case has matched already
		b.newLine().append("boolean ");
		b.append(matchedVariable).append(" = false;");
		List<XCasePart> fallThroughCases = Lists.newArrayList();
		for (XCasePart casePart : expr.getCases()) {
			XExpression then = casePart.getThen();
			if (then == null) {
				fallThroughCases.add(casePart);
				continue;
			}
			_toJavaIfStatement(casePart, fallThroughCases, expr, then, b, isReferenced, switchResultName, matchedVariable, variableName);
		}
		XExpression then = expr.getDefault();
		if (then != null) {
			if (!fallThroughCases.isEmpty()) {
				XCasePart casePart = fallThroughCases.remove(fallThroughCases.size() - 1);
				_toJavaIfStatement(casePart, fallThroughCases, expr, then, b, isReferenced, switchResultName, matchedVariable, variableName);
			}
			ILocationData location = getLocationOfDefault(expr);
			ITreeAppendable defaultAppendable = location != null ? b.trace(location) : b;
			boolean needsMatcherIf = isReferenced || !allCasesAreExitedEarly(expr);
			if(needsMatcherIf) {
				defaultAppendable.newLine().append("if (!").append(matchedVariable).append(") {");
				defaultAppendable.increaseIndentation();
			}
			executeThenPart(expr, switchResultName, then, defaultAppendable, isReferenced);
			if(needsMatcherIf) {
				defaultAppendable.decreaseIndentation();
				defaultAppendable.newLine().append("}");
			}
		}
	}

	protected void _toJavaIfStatement(XCasePart casePart, List<XCasePart> fallThroughCases, XSwitchExpression expr, XExpression then,
			ITreeAppendable b, boolean isReferenced, String switchResultName, String matchedVariable, String variableName) {
		ITreeAppendable caseAppendable = b;
		if (!fallThroughCases.isEmpty()) {
			boolean first = true;
			Iterator<XCasePart> i = fallThroughCases.iterator();
			while(i.hasNext()) {
				XCasePart fallThroughCase = i.next();
				caseAppendable = appendOpenIfStatement(fallThroughCase, caseAppendable, matchedVariable, variableName);
				appendCloseIfStatement(fallThroughCase, caseAppendable);
				if (first) {
					first = false;
				} else {
					caseAppendable.decreaseIndentation();
					caseAppendable.newLine().append("}");
				}
				i.remove();
			}
			caseAppendable = appendOpenIfStatement(casePart, caseAppendable, matchedVariable, variableName);
			appendCloseIfStatement(casePart, caseAppendable);
			if (!first) {
				caseAppendable.decreaseIndentation();
				caseAppendable.newLine().append("}");
			}
			
			caseAppendable.newLine().append("if (").append(matchedVariable).append(") {").increaseIndentation();
			executeThenPart(expr, switchResultName, then, caseAppendable, isReferenced);
			caseAppendable.decreaseIndentation().newLine().append("}");
			
		} else {
			caseAppendable = appendOpenIfStatement(casePart, caseAppendable, matchedVariable, variableName);
			executeThenPart(expr, switchResultName, then, caseAppendable, isReferenced);
			appendCloseIfStatement(casePart, caseAppendable);
		}
		caseAppendable.decreaseIndentation();
		caseAppendable.newLine().append("}");
	}

	protected ITreeAppendable appendOpenIfStatement(XCasePart casePart, ITreeAppendable b, String matchedVariable, String variableName) {
		ITreeAppendable caseAppendable = b.trace(casePart, true);
		caseAppendable.newLine().append("if (!").append(matchedVariable).append(") {");
		caseAppendable.increaseIndentation();
		if (casePart.getTypeGuard() != null) {
			ITreeAppendable typeGuardAppendable = caseAppendable.trace(casePart.getTypeGuard(), true);
			typeGuardAppendable.newLine().append("if (");
			typeGuardAppendable.append(variableName);
			typeGuardAppendable.append(" instanceof ");
			typeGuardAppendable.trace(casePart.getTypeGuard()).append(casePart.getTypeGuard().getType());
			typeGuardAppendable.append(") {");
			typeGuardAppendable.increaseIndentation();
			typeGuardAppendable.openPseudoScope();
		}
		if (casePart.getCase() != null) {
			ITreeAppendable conditionAppendable = caseAppendable.trace(casePart.getCase(), true);
			internalToJavaStatement(casePart.getCase(), conditionAppendable, true);
			conditionAppendable.newLine().append("if (");
			LightweightTypeReference convertedType = getLightweightType(casePart.getCase());
			if (convertedType.isType(Boolean.TYPE) || convertedType.isType(Boolean.class)) {
				internalToJavaExpression(casePart.getCase(), conditionAppendable);
			} else {
				conditionAppendable.append(Objects.class);
				conditionAppendable.append(".equal(").append(variableName).append(", ");
				internalToJavaExpression(casePart.getCase(), conditionAppendable);
				conditionAppendable.append(")");
			}
			conditionAppendable.append(")");
			caseAppendable.append(" {");
			caseAppendable.increaseIndentation();
		}
		// set matched to true
		return caseAppendable.newLine().append(matchedVariable).append("=true;");
	}
	
	protected ITreeAppendable appendCloseIfStatement(XCasePart casePart, ITreeAppendable caseAppendable) {
		// close surrounding if statements
		if (casePart.getCase() != null) {
			caseAppendable.decreaseIndentation().newLine().append("}");
		}
		
		if (casePart.getTypeGuard() != null) {
			caseAppendable.decreaseIndentation().newLine().append("}");
			caseAppendable.closeScope();
		}
		return caseAppendable;
	}
	
	protected boolean isCompiledToJavaSwitch(XSwitchExpression expr) {
		if (!switchExpressions.isJavaSwitchExpression(expr)) {
			return false;
		}
		for (XCasePart casePart : expr.getCases()) {
			if (!switchExpressions.isJavaCaseExpression(expr, casePart)) {
				return false;
			}
			if (!switchExpressions.isConstant(casePart)) {
				return false;
			}
		}
		return true;
	}

	protected boolean allCasesAreExitedEarly(XSwitchExpression expr) {
		for(XCasePart casePart: expr.getCases()) {
			if(!earlyExitComputer.isEarlyExit(casePart.getThen())) {
				return false;
			}
		}
		return true;
	}
	
	protected boolean isSimpleFeatureCall(XExpression switch1) {
		if (switch1 instanceof XFeatureCall)  {
			XFeatureCall featureCall = (XFeatureCall) switch1;
			return !(featureCall.getFeature() instanceof JvmOperation);
		}
		return false;
	}

	protected Object getSwitchExpressionKey(XSwitchExpression expr) {
		return new Pair<XSwitchExpression, String>(expr, "key");
	}
	
	@Override
	/* @Nullable */
	protected String getReferenceName(XExpression expr, ITreeAppendable b) {
		if (expr instanceof XSwitchExpression) {
			Object key = getSwitchExpressionKey((XSwitchExpression) expr);
			if (b.hasName(key))
				return b.getName(key);
		}
		return super.getReferenceName(expr, b);
	}

	/* @Nullable */
	protected ILocationData getLocationOfDefault(XSwitchExpression expression) {
		final ICompositeNode startNode = NodeModelUtils.getNode(expression);
		if (startNode != null) {
			List<INode> resultNodes = Lists.newArrayList();
			boolean defaultSeen = false;
			for (INode child : startNode.getChildren()) {
				if (defaultSeen) {
					resultNodes.add(child);
					if (GrammarUtil.containingAssignment(child.getGrammarElement()) != null) {
						break;
					}
				} else if (child.getGrammarElement() instanceof Keyword && "default".equals(child.getText())) {
					defaultSeen = true;
					resultNodes.add(child);
				}
			}
			return toLocationData(resultNodes);
		}
		return null;
	}

	protected void _toJavaExpression(XSwitchExpression expr, ITreeAppendable b) {
		final String referenceName = getReferenceName(expr, b);
		if (referenceName != null)
			b.trace(expr, false).append(referenceName);
		else
			throw new IllegalStateException("Switch expression wasn't translated to Java statements before.");
	}

	protected void _toJavaStatement(final XSynchronizedExpression synchronizedExpression, final ITreeAppendable b, boolean isReferenced) {
		if (isReferenced) {
			declareSyntheticVariable(synchronizedExpression, b);
		}
		ITreeAppendable synchronizedAppendable = b.trace(synchronizedExpression, true);
		XExpression param = synchronizedExpression.getParam();
		internalToJavaStatement(param, synchronizedAppendable, isReferenced);
		
		synchronizedAppendable.newLine().append("synchronized (");
		internalToJavaExpression(param, synchronizedAppendable);
		synchronizedAppendable.append(") {").increaseIndentation();
		synchronizedAppendable.openPseudoScope();
		
		XExpression expression = synchronizedExpression.getExpression();
		internalToJavaStatement(expression, b, isReferenced);
		if (isReferenced) {
			b.newLine().append(getVarName(synchronizedExpression, synchronizedAppendable)).append(" = ");
			internalToConvertedExpression(expression, b, getLightweightType(synchronizedExpression));
			b.append(";");
		}
		
		synchronizedAppendable.closeScope();
		synchronizedAppendable.decreaseIndentation().newLine().append("}");
	}

	protected void _toJavaExpression(XSynchronizedExpression synchronizedExpression, ITreeAppendable b) {
		b.trace(synchronizedExpression, false).append(getVarName(synchronizedExpression, b));
	}

	protected void _toJavaStatement(final XClosure closure, final ITreeAppendable b, boolean isReferenced) {
		if (!isReferenced)
			throw new IllegalArgumentException("a closure definition does not cause any side-effects");
		LightweightTypeReference type = getLightweightType(closure);
		b.newLine().append("final ");
		b.append(type);
		b.append(" ");
		String variableName = b.declareSyntheticVariable(closure, "_function");
		b.append(variableName).append(" = ");
		toAnonymousClass(closure, b, type).append(";");
	}

	protected ITreeAppendable toAnonymousClass(final XClosure closure, final ITreeAppendable b, LightweightTypeReference type) {
		b.append("new ");
		// TODO parameters in type arguments are safe to be a wildcard
		b.append(type);
		b.append("() {");
		b.increaseIndentation();
		try {
			b.openScope();
			JvmOperation operation = findImplementingOperation(type);
			if (operation != null) {
				final LightweightTypeReference returnType = getClosureOperationReturnType(type, operation);
				appendOperationVisibility(b, operation);
				b.append(returnType);
				b.append(" ").append(operation.getSimpleName());
				b.append("(");
				List<JvmFormalParameter> closureParams = closure.getFormalParameters();
				for (int i = 0; i < closureParams.size(); i++) {
					JvmFormalParameter closureParam = closureParams.get(i);
					LightweightTypeReference parameterType = getClosureOperationParameterType(type, operation, i);
					appendClosureParameter(closureParam, parameterType, b);
					if (i != closureParams.size() - 1)
						b.append(", ");
				}
				b.append(")");
				if(!operation.getExceptions().isEmpty()) {
					b.append(" throws ");
					for (int i = 0; i < operation.getExceptions().size(); ++i) {
						serialize(operation.getExceptions().get(i), closure, b, false, false, false, false);
						if(i != operation.getExceptions().size() -1)
							b.append(", ");
					}
				}
				b.append(" {");
				b.increaseIndentation();
				reassignThisInClosure(b, type.getType());
				compile(closure.getExpression(), b, returnType, newHashSet(operation.getExceptions()));
				b.decreaseIndentation();
				b.newLine().append("}");
			}
		} finally {
			b.closeScope();
		}
		return b.decreaseIndentation().newLine().append("}");
	}

	protected void appendClosureParameter(JvmFormalParameter closureParam, LightweightTypeReference parameterType, ITreeAppendable appendable) {
		appendable.append("final ");
		appendable.append(parameterType);
		appendable.append(" ");
		final String proposedParamName = makeJavaIdentifier(closureParam.getName());
		String name = appendable.declareVariable(closureParam, proposedParamName);
		appendable.append(name);
	}

	protected void appendOperationVisibility(final ITreeAppendable b, JvmOperation operation) {
		b.newLine();
		JvmDeclaredType declaringType = operation.getDeclaringType();
		if (declaringType instanceof JvmGenericType && !((JvmGenericType) declaringType).isInterface()) {
			b.append("@Override").newLine();
		}
		switch(operation.getVisibility()) {
			case DEFAULT: break;
			case PUBLIC: b.append("public "); return;
			case PROTECTED: b.append("protected "); return;
			case PRIVATE: b.append("private "); return;
		}
	}

	protected LightweightTypeReference getClosureOperationParameterType(LightweightTypeReference closureType, JvmOperation operation, int i) {
		ITypeReferenceOwner owner = newTypeReferenceOwner(operation);
		OwnedConverter converter = new OwnedConverter(newTypeReferenceOwner(operation));
		Map<JvmTypeParameter, LightweightMergedBoundTypeArgument> mapping = new DeclaratorTypeArgumentCollector().getTypeParameterMapping(closureType);
		LightweightTypeReference parameterType = converter.toLightweightReference(operation.getParameters().get(i).getParameterType());
		return new StandardTypeParameterSubstitutor(mapping, owner).substitute(parameterType);
	}

	protected LightweightTypeReference getClosureOperationReturnType(LightweightTypeReference closureType, JvmOperation operation) {
		ITypeReferenceOwner owner = newTypeReferenceOwner(operation);
		OwnedConverter converter = new OwnedConverter(owner);
		Map<JvmTypeParameter, LightweightMergedBoundTypeArgument> mapping = new DeclaratorTypeArgumentCollector().getTypeParameterMapping(closureType);
		LightweightTypeReference parameterType = converter.toLightweightReference(operation.getReturnType());
		return new StandardTypeParameterSubstitutor(mapping, owner).substitute(parameterType);
	}
	
	protected void _toJavaExpression(final XClosure closure, final ITreeAppendable b) {
		if (b.hasName(closure)) {
			b.trace(closure, false).append(getVarName(closure, b));
		} else {
			toAnonymousClass(closure, b.trace(closure, false), getLightweightType(closure));
		}
	}
	
	@Override
	protected boolean internalCanCompileToJavaExpression(XExpression expression, ITreeAppendable appendable) {
		if (expression instanceof XListLiteral) {
			XListLiteral listLiteral = (XListLiteral) expression;
			for (XExpression element : listLiteral.getElements()) {
				if (!internalCanCompileToJavaExpression(element, appendable)) {
					return false;
				}
			}
			return true;
		}
		if (expression instanceof XSetLiteral) {
			if (isType(expression, Map.class)) {
				return false;
			}
			XSetLiteral setLiteral = (XSetLiteral) expression;
			for (XExpression element : setLiteral.getElements()) {
				if (!internalCanCompileToJavaExpression(element, appendable)) {
					return false;
				}
			}
			return true;
		}
		if (expression instanceof XBlockExpression) {
			return false;
		}
		if (expression instanceof XTryCatchFinallyExpression) {
			return false;
		}
		if (expression instanceof XThrowExpression) {
			return false;
		}
		if (expression instanceof XInstanceOfExpression) {
			XInstanceOfExpression instanceOfExpression = (XInstanceOfExpression) expression;
			return internalCanCompileToJavaExpression(instanceOfExpression.getExpression(), appendable);
		}
		if (expression instanceof XVariableDeclaration) {
			return false;
		}
		if (expression instanceof XWhileExpression) {
			return false;
		}
		if (expression instanceof XDoWhileExpression) {
			return false;
		}
		if (expression instanceof XBasicForLoopExpression) {
			return false;
		}
		if (expression instanceof XForLoopExpression) {
			return false;
		}
		if (expression instanceof XCastedExpression) {
			XCastedExpression castedExpression = (XCastedExpression) expression;
			return internalCanCompileToJavaExpression(castedExpression.getTarget(), appendable);
		}
		if (expression instanceof XReturnExpression) {
			return false;
		}
		if (expression instanceof XIfExpression) {
			return false;
		}
		if (expression instanceof XSwitchExpression) {
			return false;
		}
		if (expression instanceof XSynchronizedExpression) {
			return false;
		}
		return super.internalCanCompileToJavaExpression(expression, appendable);
	}
	
	@Override
	protected boolean isVariableDeclarationRequired(XExpression expr, ITreeAppendable b) {
		if (expr instanceof XAnnotation) {
			return false;
		}
		if (expr instanceof XListLiteral) {
			return false;
		}
		if (expr instanceof XSetLiteral) {
			LightweightTypeReference literalType = batchTypeResolver.resolveTypes(expr).getActualType(expr);
			return literalType != null && literalType.isType(Map.class);
		}
		if (expr instanceof XCastedExpression) {
			return false;
		}
		if (expr instanceof XInstanceOfExpression) {
			return false;
		}
		if (expr instanceof XMemberFeatureCall && isVariableDeclarationRequired((XMemberFeatureCall) expr, b))
			return true;
		EObject container = expr.eContainer();
		if ((container instanceof XVariableDeclaration)
			|| (container instanceof XReturnExpression) 
			|| (container instanceof XThrowExpression)) {
			return false;
		}
		if (container instanceof XIfExpression) {
			XIfExpression ifExpression = (XIfExpression) container;
			if (ifExpression.getThen() == expr || ifExpression.getElse() == expr) {
				return false;
			}
		}
		if (container instanceof XCasePart) {
			XCasePart casePart = (XCasePart) container;
			if (casePart.getThen() == expr) {
				return false;
			}
		}
		if (container instanceof XSwitchExpression) {
			XSwitchExpression switchExpression = (XSwitchExpression) container;
			if (switchExpression.getDefault() == expr) {
				return false;
			}
		}
		if (container instanceof XBlockExpression) {
			List<XExpression> siblings = ((XBlockExpression) container).getExpressions();
			if (siblings.get(siblings.size() - 1) == expr) {
				return false;
			}
		}
		if (container instanceof XClosure) {
			if (((XClosure) container).getExpression() == expr) {
				return false;
			}
		}
		return super.isVariableDeclarationRequired(expr, b);
	}
	
	protected void _toJavaExpression(final XAnnotation annotation, final ITreeAppendable b) {
		b.append("@");
		b.append(annotation.getAnnotationType());
		XExpression value = annotation.getValue();
		if (value != null) {
			b.append("(");
			internalToJavaExpression(value, b);
			b.append(")");
		} else {
			EList<XAnnotationElementValuePair> valuePairs = annotation.getElementValuePairs();
			if (valuePairs.isEmpty())
				return;
			b.append("(");
			for (int i = 0; i < valuePairs.size(); i++) {
				XAnnotationElementValuePair pair = valuePairs.get(i);
				b.append(pair.getElement().getSimpleName());
				b.append(" = ");
				internalToJavaExpression(pair.getValue(), b);
				if (i < valuePairs.size()-1) {
					b.append(", ");
				}
			}
			b.append(")");
		}
	}
	
}
