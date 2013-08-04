/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.typesystem.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtext.common.types.JvmAnnotationAnnotationValue;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationTarget;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmCustomAnnotationValue;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.util.AnnotationLookup;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.jvmmodel.ILogicalContainerProvider;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureNames;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureScopeSession;
import org.eclipse.xtext.xbase.typesystem.InferredTypeIndicator;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationResult;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.eclipse.xtext.xbase.typesystem.override.OverrideHelper;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.AbstractReentrantTypeReferenceProvider;
import org.eclipse.xtext.xbase.typesystem.util.Maps2;
import org.eclipse.xtext.xbase.typing.IJvmTypeReferenceProvider;
import org.eclipse.xtext.xbase.validation.IssueCodes;
import org.eclipse.xtext.xtype.XComputedTypeReference;
import org.eclipse.xtext.xtype.impl.XComputedTypeReferenceImplCustom;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * TODO JavaDoc, toString
 */
@NonNullByDefault
public class LogicalContainerAwareReentrantTypeResolver extends DefaultReentrantTypeResolver {

	public static class DemandTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmMember member;
		private final ResolvedTypes resolvedTypes;
		private final boolean returnType;
		private final IFeatureScopeSession session;
		private final XExpression expression;
		private final Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext;
		private final LogicalContainerAwareReentrantTypeResolver typeResolver;

		public DemandTypeReferenceProvider(
				JvmMember member, 
				XExpression expression, 
				Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext, 
				ResolvedTypes resolvedTypes, 
				IFeatureScopeSession session, 
				boolean returnType,
				LogicalContainerAwareReentrantTypeResolver typeResolver) {
			this.member = member;
			this.expression = expression;
			this.resolvedTypesByContext = resolvedTypesByContext;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
			this.returnType = returnType;
			this.typeResolver = typeResolver;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			try {
				LightweightTypeReference actualType = returnType ? resolvedTypes.getReturnType(expression) : resolvedTypes.getActualType(expression);
				if (actualType == null) {
					typeResolver.computeTypes(resolvedTypesByContext, resolvedTypes, session, member);
					actualType = returnType ? resolvedTypes.getExpectedReturnType(expression) : resolvedTypes.getExpectedType(expression);
					if (actualType == null)
						actualType = returnType ? resolvedTypes.getReturnType(expression) : resolvedTypes.getActualType(expression);
				}
				if (actualType == null)
					return null;
				return typeResolver.toJavaCompliantTypeReference(actualType, session);
			} finally {
				context.setTypeProvider(null);
			}
		}
		
		@Override
		protected JvmTypeReference handleReentrantInvocation(XComputedTypeReferenceImplCustom context) {
			EObject sourceElement = typeResolver.getSourceElement(member);
			EStructuralFeature feature = sourceElement.eClass().getEStructuralFeature("name");
			resolvedTypes.addDiagnostic(new EObjectDiagnosticImpl(
					Severity.WARNING, 
					IssueCodes.TOO_LITTLE_TYPE_INFORMATION, 
					"Cannot infer type from recursive usage. Type 'Object' is used.",
					sourceElement, 
					feature, 
					-1, 
					null));
			AnyTypeReference result = new AnyTypeReference(resolvedTypes.getReferenceOwner());
			return typeResolver.toJavaCompliantTypeReference(result, session);
		}

		/*
		 * Allows invocation from within the context of the class
		 */
		@Override
		protected void markComputing() {
			super.markComputing();
		}
		
	}
	
	public static class AnyTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmMember member;
		private final ResolvedTypes resolvedTypes;
		private final LogicalContainerAwareReentrantTypeResolver typeResolver;

		public AnyTypeReferenceProvider(JvmMember member, ResolvedTypes resolvedTypes, LogicalContainerAwareReentrantTypeResolver typeResolver) {
			this.member = member;
			this.resolvedTypes = resolvedTypes;
			this.typeResolver = typeResolver;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			try {
				resolvedTypes.addDiagnostic(new EObjectDiagnosticImpl(
						Severity.ERROR, 
						IssueCodes.TOO_LITTLE_TYPE_INFORMATION, 
						"Cannot infer type",
						typeResolver.getSourceElement(member), 
						null, 
						-1, 
						null));
				return TypesFactory.eINSTANCE.createJvmAnyTypeReference();
			} finally {
				context.setTypeProvider(null);
			}
		}
	}

	@Inject
	private ILogicalContainerProvider logicalContainerProvider;
	
	@Inject
	private IJvmModelAssociations associations;
	
	@Inject
	private OverrideHelper overrideHelper;
	
	@Inject
	private AnnotationLookup annotationLookup;
	
	protected JvmType getRootJvmType() {
		EObject result = getRoot();
		if (result instanceof JvmType)
			return (JvmType) result;
		throw new IllegalStateException();
	}
	
	@Override
	protected boolean isHandled(JvmIdentifiableElement identifiableElement) {
		if (identifiableElement instanceof XExpression) {
			return isHandled((XExpression) identifiableElement);
		}
		JvmIdentifiableElement container = logicalContainerProvider.getNearestLogicalContainer(identifiableElement);
		if (container != null) {
			return super.isHandled(container);
		}
		return super.isHandled(identifiableElement);
	}
	
	@Override
	protected boolean isHandled(XExpression expression) {
		JvmIdentifiableElement logicalContainer = logicalContainerProvider.getNearestLogicalContainer(expression);
		if (logicalContainer == null)
			return false;
		return isHandled(logicalContainer);
	}
	
	@Override
	protected boolean isHandled(EObject context) {
		JvmIdentifiableElement logicalContainer = logicalContainerProvider.getNearestLogicalContainer(context);
		if (logicalContainer == null)
			return false;
		return isHandled(logicalContainer);
	}
	
	/**
	 * Assign computed type references to the identifiable structural elements in the processed type.
	 * @return the stacked resolved types that shall be used in the computation.
	 */
	protected Map<JvmIdentifiableElement, ResolvedTypes> prepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession) {
		Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext = Maps.newHashMapWithExpectedSize(3); 
		doPrepare(resolvedTypes, featureScopeSession, getRootJvmType(), resolvedTypesByContext);
		return resolvedTypesByContext;
	}
	
	protected void doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmIdentifiableElement element, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		if (element instanceof JvmDeclaredType) {
			_doPrepare(resolvedTypes, featureScopeSession, (JvmDeclaredType) element, resolvedTypesByContext);
		} else if (element instanceof JvmConstructor) {
			_doPrepare(resolvedTypes, featureScopeSession, (JvmConstructor) element, resolvedTypesByContext);
		} else if (element instanceof JvmField) {
			_doPrepare(resolvedTypes, featureScopeSession, (JvmField) element, resolvedTypesByContext);
		} else if (element instanceof JvmOperation) {
			_doPrepare(resolvedTypes, featureScopeSession, (JvmOperation) element, resolvedTypesByContext);
		}
	}
	
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmDeclaredType type, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByType) {
		IFeatureScopeSession childSession = addThisAndSuper(featureScopeSession, resolvedTypes.getReferenceOwner(), type);
		prepareMembers(resolvedTypes, childSession, type, resolvedTypesByType);
	}

	protected void prepareMembers(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmDeclaredType type, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByType) {
		IFeatureScopeSession childSession = addExtensionsToMemberSession(resolvedTypes, featureScopeSession, type);
		
		StackedResolvedTypes childResolvedTypes = declareTypeParameters(resolvedTypes, type, resolvedTypesByType);
		
		JvmTypeReference superType = getExtendedClass(type);
		if (superType != null) {
			LightweightTypeReference lightweightSuperType = resolvedTypes.getConverter().toLightweightReference(superType);
			childResolvedTypes.reassignTypeWithoutMerge(superType.getType(), lightweightSuperType);
			/* 
			 * We use reassignType to make sure that the following works:
			 *
			 * StringList extends AbstractList<String> {
			 *   NestedIntList extends AbstractList<Integer> {
			 *   }
			 *   SubType extends StringList {}
			 * }
			 */
		}
		JvmParameterizedTypeReference thisType = getServices().getTypeReferences().createTypeRef(type);
		LightweightTypeReference lightweightThisType = resolvedTypes.getConverter().toLightweightReference(thisType);
		childResolvedTypes.reassignTypeWithoutMerge(type, lightweightThisType);
		
		List<JvmMember> members = type.getMembers();
		int size = members.size();
		for(int i = 0; i < size; i++) {
			doPrepare(childResolvedTypes, childSession, members.get(i), resolvedTypesByType);
		}
	}

	protected StackedResolvedTypes declareTypeParameters(ResolvedTypes resolvedTypes, JvmIdentifiableElement declarator,
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		StackedResolvedTypes childResolvedTypes = resolvedTypes.pushTypes();
		if (declarator instanceof JvmTypeParameterDeclarator) {
			JvmTypeParameterDeclarator casted = (JvmTypeParameterDeclarator) declarator;
			if (isStatic(declarator) && !(declarator instanceof JvmConstructor)) {
				childResolvedTypes.replaceDeclaredTypeParameters(casted.getTypeParameters());
			} else {
				childResolvedTypes.addDeclaredTypeParameters(casted.getTypeParameters());
			}
		}
		resolvedTypesByContext.put(declarator, childResolvedTypes);
		return childResolvedTypes;
	}
	
	protected boolean isStatic(JvmIdentifiableElement declarator) {
		if (declarator instanceof JvmFeature) {
			return ((JvmFeature) declarator).isStatic();
		}
		if (declarator instanceof JvmDeclaredType) {
			return ((JvmDeclaredType) declarator).isStatic();
		}
		return false;
	}

	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmField field, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		StackedResolvedTypes childResolvedTypes = declareTypeParameters(resolvedTypes, field, resolvedTypesByContext);
		
		JvmTypeReference knownType = field.getType();
		if (InferredTypeIndicator.isInferred(knownType)) {
			XComputedTypeReference casted = (XComputedTypeReference) knownType;
			JvmTypeReference reference = createComputedTypeReference(resolvedTypesByContext, childResolvedTypes, featureScopeSession, field, (InferredTypeIndicator) casted.getTypeProvider(), false);
			casted.setEquivalent(reference);
		} else if (knownType != null) {
			LightweightTypeReference lightweightReference = childResolvedTypes.getConverter().toLightweightReference(knownType);
			childResolvedTypes.setType(field, lightweightReference);
		} else {
			JvmTypeReference reference = createComputedTypeReference(resolvedTypesByContext, childResolvedTypes, featureScopeSession, field, null, false);
			field.setType(reference);
		}
	}
	
	@Nullable
	protected DemandTypeReferenceProvider getComputedTypeReference(JvmTypeReference knownType) {
		if (InferredTypeIndicator.isInferred(knownType)) {
			XComputedTypeReference casted = (XComputedTypeReference) knownType;
			JvmTypeReference equivalent = casted.getEquivalent();
			if (equivalent instanceof XComputedTypeReference) {
				IJvmTypeReferenceProvider typeProvider = ((XComputedTypeReference) equivalent).getTypeProvider();
				if (typeProvider instanceof DemandTypeReferenceProvider) {
					return (DemandTypeReferenceProvider) typeProvider;
				}
			}
		}
		return null;
	}
	
	protected void markComputing(JvmTypeReference knownType) {
		DemandTypeReferenceProvider demandTypeReferenceProvider = getComputedTypeReference(knownType);
		if (demandTypeReferenceProvider != null) {
			demandTypeReferenceProvider.markComputing();
		}
	}
	
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmConstructor constructor, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		StackedResolvedTypes childResolvedTypes = declareTypeParameters(resolvedTypes, constructor, resolvedTypesByContext);
		
		JvmDeclaredType producedType = constructor.getDeclaringType();
		JvmParameterizedTypeReference asReference = getServices().getTypeReferences().createTypeRef(producedType);
		LightweightTypeReference lightweightReference = childResolvedTypes.getConverter().toLightweightReference(asReference);
		childResolvedTypes.setType(constructor, lightweightReference);
	}
	
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmOperation operation, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		StackedResolvedTypes childResolvedTypes = declareTypeParameters(resolvedTypes, operation, resolvedTypesByContext);
		
		JvmTypeReference knownType = operation.getReturnType();
		if (InferredTypeIndicator.isInferred(knownType)) {
			XComputedTypeReference casted = (XComputedTypeReference) knownType;
			JvmTypeReference reference = createComputedTypeReference(
					resolvedTypesByContext, childResolvedTypes, featureScopeSession, operation, (InferredTypeIndicator) casted.getTypeProvider(), true);
			casted.setEquivalent(reference);
		} else if (knownType != null) {
			LightweightTypeReference lightweightReference = childResolvedTypes.getConverter().toLightweightReference(knownType);
			childResolvedTypes.setType(operation, lightweightReference);
		} else {
			JvmTypeReference reference = createComputedTypeReference(resolvedTypesByContext, childResolvedTypes, featureScopeSession, operation, null, true);
			operation.setReturnType(reference);
		}
	}
	
	protected JvmTypeReference createComputedTypeReference(
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext,
			ResolvedTypes resolvedTypes,
			IFeatureScopeSession featureScopeSession,
			JvmMember member,
			@Nullable InferredTypeIndicator indicator,
			boolean returnType) {
		XComputedTypeReference result = getServices().getXtypeFactory().createXComputedTypeReference();
		if (indicator == null || indicator.getExpression() == null)
			result.setTypeProvider(createTypeProvider(resolvedTypesByContext, resolvedTypes, featureScopeSession, member, returnType));
		else
			result.setTypeProvider(createTypeProvider(resolvedTypesByContext, resolvedTypes, featureScopeSession, member, indicator.getExpression(), returnType));
		// TODO do we need a lightweight computed type reference?
//		resolvedTypes.setType(member, result);
		return result;
	}
	
	protected AbstractReentrantTypeReferenceProvider createTypeProvider(Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmMember member, boolean returnType) {
		XExpression expression = logicalContainerProvider.getAssociatedExpression(member);
		return createTypeProvider(resolvedTypesByContext, resolvedTypes, featureScopeSession, member, expression, returnType);
	}
	
	protected AbstractReentrantTypeReferenceProvider createTypeProvider(
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmMember member, 
			@Nullable XExpression expression, boolean returnType) {
		if (expression != null) {
			resolvedTypes.markToBeInferred(expression);
			return new DemandTypeReferenceProvider(member, expression, resolvedTypesByContext, resolvedTypes, featureScopeSession, returnType, this);
		}
		return new AnyTypeReferenceProvider(member, resolvedTypes, this); 
	}
	
	@Override
	protected void computeTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
		Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes = prepare(resolvedTypes, session);
		computeTypes(preparedResolvedTypes, resolvedTypes, session, getRoot());
	}
	
	protected void computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, EObject element) {
		if (element instanceof JvmDeclaredType) {
			_computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, (JvmDeclaredType) element);
		} else if (element instanceof JvmConstructor) {
			_computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, (JvmConstructor) element);
		} else if (element instanceof JvmField) {
			_computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, (JvmField) element);
		} else if (element instanceof JvmOperation) {
			_computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, (JvmOperation) element);
		} else {
			computeTypes(resolvedTypes, featureScopeSession, element);
		}
	}
	
	@Override
	protected void computeTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, EObject element) {
		if (element instanceof JvmConstructor) {
			throw new IllegalStateException();
		} else if (element instanceof JvmField) {
			throw new IllegalStateException();
		} else if (element instanceof JvmOperation) {
			throw new IllegalStateException();
		} else if (element instanceof JvmDeclaredType) {
			throw new IllegalStateException();
		} else {
			super.computeTypes(resolvedTypes, featureScopeSession, element);
		}
	}

	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmField field) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(field);
		if (childResolvedTypes == null) {
			if (preparedResolvedTypes.containsKey(field))
				return;
			throw new IllegalStateException("No resolved type found. Field was: " + field.getIdentifier());
		} else {
			preparedResolvedTypes.put(field, null);
		}
		FieldTypeComputationState state = new FieldTypeComputationState(childResolvedTypes, field.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext(), field);
		// no need to unmark the computing state since we replace the equivalent in #resolveTo
		markComputing(field.getType());
		ITypeComputationResult result = state.computeTypes();
		if (InferredTypeIndicator.isInferred(field.getType())) {
			LightweightTypeReference fieldType = result.getActualExpressionType();
			if (fieldType != null)
				InferredTypeIndicator.resolveTo(field.getType(), toJavaCompliantTypeReference(fieldType, featureScopeSession));
		}
		computeAnnotationTypes(childResolvedTypes, featureScopeSession, field);
		
		mergeChildTypes(childResolvedTypes);
	}
	
	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmConstructor constructor) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(constructor);
		if (childResolvedTypes == null) {
			if (preparedResolvedTypes.containsKey(constructor))
				return;
			throw new IllegalStateException("No resolved type found. Constructor was: " + constructor.getIdentifier());
		} else {
			preparedResolvedTypes.put(constructor, null);
		}
		ConstructorBodyComputationState state = new ConstructorBodyComputationState(childResolvedTypes, featureScopeSession.toInstanceContext(), constructor);
		addExtensionProviders(state, constructor.getParameters());
		state.computeTypes();
		computeAnnotationTypes(childResolvedTypes, featureScopeSession, constructor);
		for(JvmFormalParameter parameter: constructor.getParameters()) {
			computeAnnotationTypes(childResolvedTypes, featureScopeSession, parameter);
		}
		
		mergeChildTypes(childResolvedTypes);
	}
	
	protected void addExtensionProviders(ITypeComputationState state, List<JvmFormalParameter> parameters) {
		List<JvmFormalParameter> extensionProviders = null;
		for(JvmFormalParameter parameter: parameters) {
			if (isExtensionProvider(parameter)) {
				if (extensionProviders == null)
					extensionProviders = Lists.newLinkedList();
				extensionProviders.add(parameter);
			}
		}
		if (extensionProviders != null)
			state.addExtensionsToCurrentScope(extensionProviders);
	}
	
	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmOperation operation) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(operation);
		if (childResolvedTypes == null) {
			if (preparedResolvedTypes.containsKey(operation))
				return;
			throw new IllegalStateException("No resolved type found. Operation was: " + operation.getIdentifier());
		} else {
			preparedResolvedTypes.put(operation, null);
		}
		OperationBodyComputationState state = new OperationBodyComputationState(childResolvedTypes, operation.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext(), operation);
		addExtensionProviders(state, operation.getParameters());
		// no need to unmark the computing state since we replace the equivalent in #resolveTo
		markComputing(operation.getReturnType());
		setReturnType(operation, state.computeTypes(), featureScopeSession);
		computeAnnotationTypes(childResolvedTypes, featureScopeSession, operation);
		
		mergeChildTypes(childResolvedTypes);
	}

	protected void computeAnnotationTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession sessions, JvmExecutable operation) {
		computeAnnotationTypes(resolvedTypes, sessions, (JvmAnnotationTarget) operation);
		for(JvmFormalParameter parameter: operation.getParameters()) {
			computeAnnotationTypes(resolvedTypes, sessions, parameter);
		}
	}

	protected void mergeChildTypes(ResolvedTypes childResolvedTypes) {
		if (childResolvedTypes instanceof StackedResolvedTypes)
			((StackedResolvedTypes) childResolvedTypes).mergeIntoParent();
	}

	protected void setReturnType(JvmOperation operation, ITypeComputationResult computedType, IFeatureScopeSession session) {
		if (InferredTypeIndicator.isInferred(operation.getReturnType())) {
			LightweightTypeReference returnType = computedType.getReturnType();
			if (returnType != null) {
				InferredTypeIndicator.resolveTo(operation.getReturnType(), toJavaCompliantTypeReference(returnType, session));
			}
		}
	}
	
	protected void computeAnnotationTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmAnnotationTarget annotable) {
		List<JvmAnnotationReference> annotations = annotable.getAnnotations();
		computeAnnotationTypes(resolvedTypes, featureScopeSession, annotations);
	}

	protected void computeAnnotationTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, List<JvmAnnotationReference> annotations) {
		for(JvmAnnotationReference annotation: annotations) {
			EObject sourceElement = getSourceElement(annotation);
			if (sourceElement != annotation) {
				computeTypes(resolvedTypes, featureScopeSession, sourceElement);
			} else {
				for(JvmAnnotationValue value: annotation.getValues()) {
					if (value instanceof JvmCustomAnnotationValue) {
						JvmCustomAnnotationValue custom = (JvmCustomAnnotationValue) value;
						for(Object object: custom.getValues()) {
							if (object instanceof XExpression) {
								AnnotationValueTypeComputationState state = new AnnotationValueTypeComputationState(resolvedTypes, featureScopeSession, value, (XExpression) object);
								state.computeTypes();
							}
						}
					} else if (value instanceof JvmAnnotationAnnotationValue) {
						computeAnnotationTypes(resolvedTypes, featureScopeSession, ((JvmAnnotationAnnotationValue) value).getValues());
					}
				}
			}
		}
	}
	
	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmDeclaredType type) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(type);
		if (childResolvedTypes == null)
			throw new IllegalStateException("No resolved type found. Type was: " + type.getIdentifier());
		IFeatureScopeSession childSession = addThisAndSuper(featureScopeSession, childResolvedTypes.getReferenceOwner(), type);
		computeMemberTypes(preparedResolvedTypes, childResolvedTypes, childSession, type);
		computeAnnotationTypes(childResolvedTypes, featureScopeSession, type);
		
		mergeChildTypes(childResolvedTypes);
	}

	protected void computeMemberTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession,
			JvmDeclaredType type) {
		IFeatureScopeSession childSession = addExtensionsToMemberSession(resolvedTypes, featureScopeSession, type);
		List<JvmMember> members = type.getMembers();
		for(int i = 0; i < members.size(); i++) {
			computeTypes(preparedResolvedTypes, resolvedTypes, childSession, members.get(i));
		}
	}
	
	protected IFeatureScopeSession addThisAndSuper(IFeatureScopeSession session, ITypeReferenceOwner owner, JvmDeclaredType type) {
		JvmTypeReference superType = getExtendedClass(type);
		return addThisAndSuper(session, owner, type, superType);
	}

	protected IFeatureScopeSession addThisAndSuper(IFeatureScopeSession session, ITypeReferenceOwner owner, JvmDeclaredType thisType,
			@Nullable JvmTypeReference superType) {
		IFeatureScopeSession childSession;
		if (superType != null) {
			ImmutableMap.Builder<QualifiedName, JvmIdentifiableElement> builder = ImmutableMap.builder();
			builder.put(IFeatureNames.THIS, thisType);
			builder.put(IFeatureNames.SUPER, superType.getType());
			childSession = session.addLocalElements(builder.build(), owner);
		} else {
			childSession = session.addLocalElement(IFeatureNames.THIS, thisType, owner);
		}
		childSession = addThisTypeToStaticScope(childSession, thisType);
		return childSession;
	}

	protected IFeatureScopeSession addThisTypeToStaticScope(IFeatureScopeSession session, JvmDeclaredType type) {
		return session.addTypesToStaticScope(Collections.singletonList(type), Collections.<JvmDeclaredType>emptyList());
	}
	
	@Nullable
	public JvmTypeReference getExtendedClass(JvmDeclaredType type) {
		for(JvmTypeReference candidate: type.getSuperTypes()) {
			if (candidate.getType() instanceof JvmGenericType && !((JvmGenericType) candidate.getType()).isInterface())
				return candidate;
		}
		return null;
	}
	
	protected IFeatureScopeSession addExtensionsToMemberSession(ResolvedTypes resolvedTypes,
			IFeatureScopeSession featureScopeSession, JvmDeclaredType type) {
		IEObjectDescription thisDescription = featureScopeSession.getLocalElement(IFeatureNames.THIS);
		if (thisDescription == null) {
			throw new IllegalStateException("Cannot find feature 'THIS'");
		}
		JvmIdentifiableElement thisFeature = (JvmIdentifiableElement) thisDescription.getEObjectOrProxy();
		IFeatureScopeSession childSession = addExtensionFieldsToMemberSession(
				resolvedTypes, featureScopeSession, type, thisFeature, Sets.<String>newHashSetWithExpectedSize(8), Sets.<JvmType>newHashSetWithExpectedSize(4));
		XFeatureCall thisAccess = getXbaseFactory().createXFeatureCall();
		thisAccess.setFeature(thisFeature);
		LightweightTypeReference thisType = resolvedTypes.getActualType(thisFeature);
		childSession = childSession.addToExtensionScope(Collections.<XExpression, LightweightTypeReference>singletonMap(thisAccess, thisType));
		return childSession;
	}
	
	protected IFeatureScopeSession addExtensionFieldsToMemberSession(
				ResolvedTypes resolvedTypes, 
				IFeatureScopeSession featureScopeSession, 
				JvmDeclaredType type, 
				JvmIdentifiableElement thisFeature,
				Set<String> seenNames,
				Set<JvmType> seenTypes) {
		if (seenTypes.add(type)) {
			Iterable<JvmField> fields = type.getDeclaredFields();
			// collect local fields first, to populate the set of names
			Map<XExpression, LightweightTypeReference> extensionProviders = null;
			for(JvmField field: fields) {
				if (featureScopeSession.isVisible(field) && seenNames.add(field.getSimpleName()) && isExtensionProvider(field)) {
					if (extensionProviders == null) {
						extensionProviders = Maps2.newLinkedHashMapWithExpectedSize(3);
					}
					XMemberFeatureCall extensionProvider = createExtensionProvider(thisFeature, field);
					LightweightTypeReference fieldType = resolvedTypes.getActualType(field);
					extensionProviders.put(extensionProvider, fieldType);
				}
			}
			// traverse the type hierarchy to create the feature scope sessions
			JvmTypeReference superType = getExtendedClass(type);
			IFeatureScopeSession result = featureScopeSession;
			if (superType != null) {
				result = addExtensionFieldsToMemberSession(resolvedTypes, featureScopeSession, (JvmDeclaredType) superType.getType(), thisFeature, seenNames, seenTypes);
			}
			if (extensionProviders != null) {
				result = result.addToExtensionScope(extensionProviders);
			}
			return result;
		}
		return featureScopeSession;
	}
	
	protected XMemberFeatureCall createExtensionProvider(JvmIdentifiableElement thisFeature, JvmField field) {
		XMemberFeatureCall extensionProvider = getXbaseFactory().createXMemberFeatureCall();
		extensionProvider.setFeature(field);
		XFeatureCall thisAccess = getXbaseFactory().createXFeatureCall();
		thisAccess.setFeature(thisFeature);
		extensionProvider.setMemberCallTarget(thisAccess);
		return extensionProvider;
	}
	
	protected boolean isExtensionProvider(JvmAnnotationTarget annotatedElement) {
		// coding style to simplify debugging
		if (annotationLookup.findAnnotation(annotatedElement, Extension.class) != null) {
			return true;
		}
		return false;
	}

	protected ILogicalContainerProvider getLogicalContainerProvider() {
		return logicalContainerProvider;
	}
	
	/**
	 * Returns <code>null</code> if the given operation declares it's own return type or if it does not override
	 * another operation.
	 */
	@Nullable
	protected LightweightTypeReference getReturnTypeOfOverriddenOperation(JvmOperation operation, ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
		if (operation.getVisibility() == JvmVisibility.PRIVATE)
			return null;
		if (InferredTypeIndicator.isInferred(operation.getReturnType())) {
			LightweightTypeReference declaringType = resolvedTypes.getActualType(operation.getDeclaringType());
			if (declaringType == null) {
				throw new IllegalStateException("Cannot determine declaring type of operation: " + operation);
			}
			LightweightTypeReference result = overrideHelper.getReturnTypeOfOverriddenOperation(operation, declaringType);
			return result;
		}
		return null;
	}

	@Override
	protected EObject getSourceElement(EObject element) {
		EObject result = associations.getPrimarySourceElement(element);
		if (result != null)
			return result;
		return element;
	}
	
	protected Set<EObject> getInferredElements(EObject element) {
		return associations.getJvmElements(element);
	}
	
	protected JvmTypeReference toJavaCompliantTypeReference(LightweightTypeReference result, IFeatureScopeSession session) {
		return result.toJavaCompliantTypeReference(session);
	}
}
