/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.typesystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.xtend.core.jvmmodel.DispatchHelper;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.xtend.CreateExtensionInfo;
import org.eclipse.xtend.core.xtend.XtendConstructor;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendFunction;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.core.xtend.XtendParameter;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureScopeSession;
import org.eclipse.xtext.xbase.typesystem.InferredTypeIndicator;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationResult;
import org.eclipse.xtext.xbase.typesystem.conformance.TypeConformanceComputer;
import org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareReentrantTypeResolver;
import org.eclipse.xtext.xbase.typesystem.internal.OperationBodyComputationState;
import org.eclipse.xtext.xbase.typesystem.internal.ResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.OwnedConverter;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.WildcardTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.AbstractReentrantTypeReferenceProvider;
import org.eclipse.xtext.xbase.validation.IssueCodes;
import org.eclipse.xtext.xtype.XComputedTypeReference;
import org.eclipse.xtext.xtype.impl.XComputedTypeReferenceImplCustom;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * The customized reentrant type resolver is responsible for proper typing
 * of dispatch methods' return type and parameter types as well as adding
 * extension fields to the scope. 
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@NonNullByDefault
public class DispatchAndExtensionAwareReentrantTypeResolver extends LogicalContainerAwareReentrantTypeResolver {

	public class DispatchReturnTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmOperation operation;
		private final ResolvedTypes resolvedTypes;
		private final IFeatureScopeSession session;

		public DispatchReturnTypeReferenceProvider(JvmOperation operation, ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
			this.operation = operation;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			LightweightTypeReference expectedType = getReturnTypeOfOverriddenOperation(operation, resolvedTypes, session);
			if (expectedType != null) {
				return toJavaCompliantTypeReference(expectedType, session);
			}
			
			List<JvmOperation> cases = dispatchHelper.getAllDispatchCases(operation);
			List<LightweightTypeReference> types = Lists.newArrayListWithCapacity(cases.size());
			for(JvmOperation operation: cases) {
				LightweightTypeReference caseType = resolvedTypes.getActualType(operation);
				types.add(caseType);
			}
			TypeConformanceComputer conformanceComputer = getServices().getTypeConformanceComputer();
			if (types.isEmpty())
				return null;
			LightweightTypeReference result = conformanceComputer.getCommonSuperType(types, resolvedTypes.getReferenceOwner());
			if (result == null) {
				Iterator<LightweightTypeReference> iterator = types.iterator();
				while(iterator.hasNext()) {
					if (iterator.next().isPrimitiveVoid()) {
						iterator.remove();
					}
				}
				result = conformanceComputer.getCommonSuperType(types, resolvedTypes.getReferenceOwner());
				if (result == null) {
					throw new UnsupportedOperationException("Cannot determine common super type of: " + types);
				}
			}
			return toJavaCompliantTypeReference(result, session);
		}
		
		@Override
		protected JvmTypeReference handleReentrantInvocation(XComputedTypeReferenceImplCustom context) {
			resolvedTypes.addDiagnostic(new EObjectDiagnosticImpl(
					Severity.WARNING, 
					IssueCodes.TOO_LITTLE_TYPE_INFORMATION, 
					"Cannot infer type from recursive usage. Type 'Object' is used.",
					getSourceElement(operation), 
					null, 
					-1, 
					null));
			AnyTypeReference result = new AnyTypeReference(resolvedTypes.getReferenceOwner());
			return toJavaCompliantTypeReference(result, session);
		}
	}
	
	public class DispatchParameterTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmOperation operation;
		private final ResolvedTypes resolvedTypes;
		private final int idx;
		private final IFeatureScopeSession session;

		public DispatchParameterTypeReferenceProvider(JvmOperation operation, int idx, ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
			this.idx = idx;
			this.operation = operation;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			// TODO type parameters on dispatch operations
			List<JvmOperation> cases = dispatchHelper.getAllDispatchCases(operation);
			TypeConformanceComputer conformanceComputer = getServices().getTypeConformanceComputer();
			List<LightweightTypeReference> parameterTypes = Lists.newArrayListWithCapacity(cases.size());
			for(JvmOperation caseOperation: cases) {
				JvmFormalParameter parameter = caseOperation.getParameters().get(idx);
				LightweightTypeReference parameterType = resolvedTypes.getActualType(parameter);
				if (parameterType != null && !parameterType.isType(Void.class)) {
					parameterTypes.add(parameterType);
				}
			}
			// every parameter type is java.lang.Void so we use Object
			// otherwise it would only be possible to pass the null literal but not a null value, e.g. of type String
			// to the dispatcher
			if (parameterTypes.isEmpty()) {
				return getServices().getTypeReferences().getTypeForName(Object.class, operation);
			}
			LightweightTypeReference parameterType = conformanceComputer.getCommonSuperType(parameterTypes, resolvedTypes.getReferenceOwner());
			if (parameterType == null) {
				throw new IllegalStateException("TODO: handle broken models properly");
			}
			return toJavaCompliantTypeReference(parameterType, session);
		}
	}
	
	public class InitializerParameterTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final ResolvedTypes resolvedTypes;
		private final XtendFunction createFunction;
		private final IFeatureScopeSession featureScopeSession;
		private final Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext;

		public InitializerParameterTypeReferenceProvider(XtendFunction createFunction, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession) {
			this.createFunction = createFunction;
			this.resolvedTypesByContext = resolvedTypesByContext;
			this.resolvedTypes = resolvedTypes;
			this.featureScopeSession = featureScopeSession;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			CreateExtensionInfo createExtensionInfo = createFunction.getCreateExtensionInfo();
			XExpression expression = createExtensionInfo.getCreateExpression();
			LightweightTypeReference actualType = resolvedTypes.getReturnType(expression);
			if (actualType == null) {
				JvmOperation operation = associations.getDirectlyInferredOperation(createFunction);
				IFeatureScopeSession session = operation.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext();
				computeTypes(resolvedTypesByContext, resolvedTypes, session, operation);
				actualType = resolvedTypes.getReturnType(expression);
			}
			if (actualType == null)
				return null;
			return toJavaCompliantTypeReference(actualType, featureScopeSession);
		}
	}
	
	public class CreateCacheFieldTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmOperation createOperation;
		private final ResolvedTypes resolvedTypes;
		private final IFeatureScopeSession session;

		public CreateCacheFieldTypeReferenceProvider(JvmOperation createOperation, ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
			this.createOperation = createOperation;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
		}

		@Override
		@Nullable
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			JvmTypeReference declaredReturnType = createOperation.getReturnType();
			TypeReferences typeReferences = resolvedTypes.getServices().getTypeReferences();
			ITypeReferenceOwner owner = resolvedTypes.getReferenceOwner();
			JvmType arrayList = typeReferences.findDeclaredType(ArrayList.class, createOperation);
			ParameterizedTypeReference arrayListReference = new ParameterizedTypeReference(owner, arrayList);
			JvmType objectType = typeReferences.findDeclaredType(Object.class, createOperation);
			WildcardTypeReference wildcard = new WildcardTypeReference(owner);
			wildcard.addUpperBound(new ParameterizedTypeReference(owner, objectType));
			arrayListReference.addTypeArgument(wildcard);
			JvmType hashMap = typeReferences.findDeclaredType(HashMap.class, createOperation);
			ParameterizedTypeReference hashMapReference = new ParameterizedTypeReference(owner, hashMap);
			hashMapReference.addTypeArgument(arrayListReference);
			hashMapReference.addTypeArgument(new OwnedConverter(owner).toLightweightReference(declaredReturnType));
			return toJavaCompliantTypeReference(hashMapReference, session);
		}
	}
	
	@Inject
	private DispatchHelper dispatchHelper;
	
	@Inject
	private IXtendJvmAssociations associations;
	
	@Override
	protected void computeTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
		EObject root = getRoot();
		if (root instanceof XtendTypeDeclaration) {
			computeTypes(resolvedTypes, session, root);
		} else {
			super.computeTypes(resolvedTypes, session);
		}
	}
	
	@Override
	protected void computeTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, EObject element) {
		if (element instanceof XtendTypeDeclaration) {
			XtendTypeDeclaration typeDeclaration = (XtendTypeDeclaration) element;
			computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, ((XtendTypeDeclaration) element).getAnnotations());
			for(XtendMember member: typeDeclaration.getMembers()) {
				computeTypes(resolvedTypes, featureScopeSession, member);
			}
		} else if (element instanceof XtendMember) {
			XtendMember member = (XtendMember) element;
			XExpression expression = null;
			if (member instanceof XtendFunction) {
				XtendFunction function = (XtendFunction) member;
				expression = function.getExpression();
				CreateExtensionInfo createInfo = function.getCreateExtensionInfo();
				if (createInfo != null) {
					IFeatureScopeSession session = function.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext();
					computeTypes(resolvedTypes, session, createInfo.getCreateExpression());
				}
				for(XtendParameter parameter: function.getParameters()) {
					computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, parameter.getAnnotations());
				}
			} else if (member instanceof XtendConstructor) {
				XtendConstructor constructor = (XtendConstructor) member;
				expression = constructor.getExpression();
				for(XtendParameter parameter: constructor.getParameters()) {
					computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, parameter.getAnnotations());
				}
			} else if (member instanceof XtendField) {
				expression = ((XtendField) member).getInitialValue();
			}
			if (expression != null && getInferredElements(member).isEmpty()) {
				computeTypes(resolvedTypes, featureScopeSession, expression);
			}
			computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, member.getAnnotations());
		} else {
			super.computeTypes(resolvedTypes, featureScopeSession, element);
		}
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean isHandled(XExpression expression) {
		if (getRoot() instanceof XtendTypeDeclaration) {
			XtendMember member = EcoreUtil2.getContainerOfType(expression, XtendMember.class);
			if (member != null) {
				if (getInferredElements(member).isEmpty()) {
					boolean result = EcoreUtil.isAncestor(getRoot(), expression);
					return result;
				}
				XAnnotation annotation = getOutermostAnnotation(expression);
				if (annotation != null) {
					if (getInferredElements(annotation).isEmpty()) {
						return true;
					}
				}
				return false;
			}
		} else {
			XAnnotation annotation = getOutermostAnnotation(expression);
			if (annotation != null) {
				if (getInferredElements(annotation).isEmpty()) {
					return false;
				}
			} else {
				XtendMember member = EcoreUtil2.getContainerOfType(expression, XtendMember.class);
				if (member instanceof XtendField || member instanceof XtendFunction) {
					if (getInferredElements(member).isEmpty()) {
						return false;
					}
				}
			}
		}
		return super.isHandled(expression);
	}
	
	@Nullable
	protected XAnnotation getOutermostAnnotation(XExpression expression) {
		XAnnotation annotation = EcoreUtil2.getContainerOfType(expression, XAnnotation.class);
		while(annotation != null) {
			XAnnotation parent = EcoreUtil2.getContainerOfType(annotation.eContainer(), XAnnotation.class);
			if (parent != null) {
				annotation = parent;
			} else {
				break;
			}
		}
		return annotation;
	}
	
	@Override
	protected boolean isHandled(JvmIdentifiableElement identifiableElement) {
		if (getRoot() instanceof XtendTypeDeclaration) {
			boolean result = EcoreUtil.isAncestor(getRoot(), identifiableElement);
			return result;
		} else if (identifiableElement instanceof JvmFormalParameter 
				&& (identifiableElement.eContainingFeature() == XbasePackage.Literals.XCLOSURE__IMPLICIT_PARAMETER
				|| identifiableElement.eContainingFeature() == XbasePackage.Literals.XCLOSURE__DECLARED_FORMAL_PARAMETERS)) {
			XtendMember member = EcoreUtil2.getContainerOfType(identifiableElement, XtendMember.class);
			if (member != null && getInferredElements(member).isEmpty()) {
				return false;
			}
		}
		return super.isHandled(identifiableElement);
	}

	protected boolean isAnnotationHolder(XtendMember member) {
		return member.eClass() == XtendPackage.Literals.XTEND_MEMBER || member.eClass() == XtendPackage.Literals.XTEND_TYPE_DECLARATION;
	}

	@Override
	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession,
			JvmOperation operation) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(operation);
		if (childResolvedTypes == null) {
			if (preparedResolvedTypes.containsKey(operation))
				return;
			throw new IllegalStateException("No resolved type found. Type was: " + operation.getIdentifier());
		}
		
		if (dispatchHelper.isDispatcherFunction(operation)) {
			// TODO an inherited declared type should influence the expectation of the cases
			
			// no associated expression, we just resolve it to the common super type of all associated cases
			// see #createTypeProvider and #_doPrepare
			preparedResolvedTypes.put(operation, null);
			computeAnnotationTypes(childResolvedTypes, featureScopeSession, operation);
			
			mergeChildTypes(childResolvedTypes);
		} else if (dispatchHelper.isDispatchFunction(operation) && InferredTypeIndicator.isInferred(operation.getReturnType())) {
			JvmOperation dispatcher = dispatchHelper.getDispatcherOperation(operation);
			if (dispatcher == null) {
				super._computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, operation);
				return;
			}
			LightweightTypeReference declaredDispatcherType = getReturnTypeOfOverriddenOperation(dispatcher, childResolvedTypes, featureScopeSession);
			List<JvmOperation> dispatchCases = dispatchHelper.getLocalDispatchCases(dispatcher);
			List<LightweightTypeReference> dispatchCaseResults = Lists.newArrayListWithCapacity(dispatchCases.size());
			boolean hasInferredCase = false;
			for(JvmOperation dispatchCase: dispatchCases) {
				markComputing(dispatchCase.getReturnType());
			}
			for(JvmOperation dispatchCase: dispatchCases) {
				ResolvedTypes dispatchCaseResolvedTypes = dispatchCase == operation ? childResolvedTypes : preparedResolvedTypes.get(dispatchCase);
				if (dispatchCaseResolvedTypes == null) {
					if (preparedResolvedTypes.containsKey(dispatchCase)) {
						if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
							if (declaredDispatcherType == null) {
								dispatchCaseResults.add(childResolvedTypes.getActualType(dispatchCase));
							}
							hasInferredCase = true;
						} else {
							dispatchCaseResults.add(childResolvedTypes.getActualType(dispatchCase));
						}
					} else {
						throw new IllegalStateException("No resolved type found. Type was: " + dispatchCase.getIdentifier());
					}
				} else {
					preparedResolvedTypes.put(dispatchCase, null);
					OperationBodyComputationState state = new DispatchOperationBodyComputationState(dispatchCaseResolvedTypes, 
							dispatchCase.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext(), dispatchCase, dispatcher);
					ITypeComputationResult dispatchCaseResult = state.computeTypes();
					if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
						if (declaredDispatcherType == null) {
							dispatchCaseResults.add(dispatchCaseResult.getReturnType());
						}
						hasInferredCase = true;
					} else {
						dispatchCaseResults.add(dispatchCaseResolvedTypes.getActualType(dispatchCase));
					}
					computeAnnotationTypes(dispatchCaseResolvedTypes, featureScopeSession, dispatchCase);
					mergeChildTypes(dispatchCaseResolvedTypes); 
				}
			}
			if (hasInferredCase) {
				LightweightTypeReference commonDispatchType = 
						declaredDispatcherType != null 
							? declaredDispatcherType 
							: getServices().getTypeConformanceComputer().getCommonSuperType(dispatchCaseResults, childResolvedTypes.getReferenceOwner());
				if (commonDispatchType != null) {
					for(JvmOperation dispatchCase: dispatchCases) {
						JvmTypeReference returnType = dispatchCase.getReturnType();
						if (InferredTypeIndicator.isInferred(returnType)) {
							InferredTypeIndicator.resolveTo(returnType, toJavaCompliantTypeReference(commonDispatchType, featureScopeSession));
						}
					}
				}
			}
		} else {
			super._computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, operation);
		}
	}

	protected void computeXtendAnnotationTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, List<XAnnotation> annotations) {
		for(XAnnotation annotation: annotations) {
			if (getInferredElements(annotation).isEmpty())
				computeTypes(resolvedTypes, featureScopeSession, annotation);
		}
	}
	
	/**
	 * Initializes the type inference strategy for the cache field for create extensions.
	 */
	@Override
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmField field, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		JvmTypeReference knownType = field.getType();
		if (InferredTypeIndicator.isInferred(knownType)) {
			XComputedTypeReference castedKnownType = (XComputedTypeReference) knownType;
			EObject sourceElement = associations.getPrimarySourceElement(field);
			if (sourceElement instanceof XtendFunction) {
				XtendFunction function = (XtendFunction) sourceElement;
				if (function.getCreateExtensionInfo() != null) {
					JvmOperation operation = associations.getDirectlyInferredOperation(function);
					declareTypeParameters(resolvedTypes, field, resolvedTypesByContext);
					XComputedTypeReference fieldType = getServices().getXtypeFactory().createXComputedTypeReference();
					fieldType.setTypeProvider(new CreateCacheFieldTypeReferenceProvider(operation, resolvedTypes, featureScopeSession));
					castedKnownType.setEquivalent(fieldType);
					return;
				}
			}
		}
		super._doPrepare(resolvedTypes, featureScopeSession, field, resolvedTypesByContext);
	}
	
	@Override
	protected IFeatureScopeSession addThisTypeToStaticScope(IFeatureScopeSession session, JvmDeclaredType type) {
		return session.addTypesToStaticScope(Collections.singletonList(type), Collections.singletonList(type));
	}

	@Override
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession,
			JvmOperation operation, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		super._doPrepare(resolvedTypes, featureScopeSession, operation, resolvedTypesByContext);
		resolvedTypes = resolvedTypesByContext.get(operation);
		if (dispatchHelper.isDispatcherFunction(operation)) {
			List<JvmFormalParameter> parameters = operation.getParameters();
			for(int i = 0; i < parameters.size(); i++) {
				JvmFormalParameter parameter = parameters.get(i);
				JvmTypeReference parameterType = parameter.getParameterType();
				if (InferredTypeIndicator.isInferred(parameterType)) {
					XComputedTypeReference casted = (XComputedTypeReference) parameterType;
					XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
					computedParameterType.setTypeProvider(new DispatchParameterTypeReferenceProvider(operation, i, resolvedTypes, featureScopeSession));
					casted.setEquivalent(computedParameterType);
				} else if (parameterType == null) {
					XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
					computedParameterType.setTypeProvider(new DispatchParameterTypeReferenceProvider(operation, i, resolvedTypes, featureScopeSession));
					parameter.setParameterType(computedParameterType);
				}
			}
		} else if (operation.getParameters().size() >= 1){
			EObject sourceElement = associations.getPrimarySourceElement(operation);
			if (sourceElement instanceof XtendFunction) {
				XtendFunction function = (XtendFunction) sourceElement;
				if (function.getCreateExtensionInfo() != null) {
					JvmFormalParameter firstParameter = operation.getParameters().get(0);
					JvmTypeReference parameterType = firstParameter.getParameterType();
					if (InferredTypeIndicator.isInferred(parameterType)) {
						XComputedTypeReference casted = (XComputedTypeReference) parameterType;
						XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
						computedParameterType.setTypeProvider(new InitializerParameterTypeReferenceProvider(function, resolvedTypesByContext, resolvedTypes, featureScopeSession));
						casted.setEquivalent(computedParameterType);
					}
				}
			}
		}
	}
	
	@Override
	protected AbstractReentrantTypeReferenceProvider createTypeProvider(Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext, ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmMember member,
			boolean returnType) {
		if (member instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) member;
			if (dispatchHelper.isDispatcherFunction(operation)) {
				return new DispatchReturnTypeReferenceProvider(operation, resolvedTypes, featureScopeSession);
			}
		}
		return super.createTypeProvider(resolvedTypesByContext, resolvedTypes, featureScopeSession, member, returnType);
	}

}
