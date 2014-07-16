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
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.core.jvmmodel.AnonymousClassUtil;
import org.eclipse.xtend.core.jvmmodel.DispatchHelper;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.xtend.AnonymousClass;
import org.eclipse.xtend.core.xtend.CreateExtensionInfo;
import org.eclipse.xtend.core.xtend.XtendConstructor;
import org.eclipse.xtend.core.xtend.XtendExecutable;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendFunction;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.core.xtend.XtendParameter;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociator;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures;
import org.eclipse.xtext.xbase.scoping.batch.IFeatureScopeSession;
import org.eclipse.xtext.xbase.typesystem.InferredTypeIndicator;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationResult;
import org.eclipse.xtext.xbase.typesystem.conformance.ConformanceHint;
import org.eclipse.xtext.xbase.typesystem.conformance.TypeConformanceComputer;
import org.eclipse.xtext.xbase.typesystem.internal.LogicalContainerAwareReentrantTypeResolver;
import org.eclipse.xtext.xbase.typesystem.internal.OperationBodyComputationState;
import org.eclipse.xtext.xbase.typesystem.internal.ResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.override.BottomResolvedOperation;
import org.eclipse.xtext.xbase.typesystem.override.IResolvedOperation;
import org.eclipse.xtext.xbase.typesystem.override.OverrideTester;
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
 * The customized reentrant type resolver is responsible for proper typing of dispatch methods' return type and
 * parameter types as well as adding extension fields to the scope.
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class XtendReentrantTypeResolver extends LogicalContainerAwareReentrantTypeResolver {

	public static class DispatchReturnTypeReferenceProvider extends AbstractDemandTypeReferenceProvider {
		private final JvmOperation operation;
		private final ResolvedTypes resolvedTypes;
		private final IFeatureScopeSession session;
		private final XtendReentrantTypeResolver typeResolver;

		public DispatchReturnTypeReferenceProvider(
				JvmOperation operation,
				ResolvedTypes resolvedTypes,
				IFeatureScopeSession session,
				XtendReentrantTypeResolver typeResolver) {
			this.operation = operation;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
			this.typeResolver = typeResolver;
		}

		@Override
		/* @Nullable */
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			try {
				LightweightTypeReference expectedType = typeResolver.getReturnTypeOfOverriddenOperation(operation, resolvedTypes, session);
				if (expectedType != null) {
					return typeResolver.toJavaCompliantTypeReference(expectedType, session);
				}

				List<JvmOperation> cases = typeResolver.dispatchHelper.getAllDispatchCases(operation);
				List<LightweightTypeReference> types = Lists.newArrayListWithCapacity(cases.size());
				for (JvmOperation operation : cases) {
					LightweightTypeReference caseType = resolvedTypes.getActualType(operation);
					types.add(caseType);
				}
				TypeConformanceComputer conformanceComputer = typeResolver.getServices().getTypeConformanceComputer();
				if (types.isEmpty())
					return null;
				LightweightTypeReference result = conformanceComputer.getCommonSuperType(types, resolvedTypes.getReferenceOwner());
				if (result == null) {
					Iterator<LightweightTypeReference> iterator = types.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().isPrimitiveVoid()) {
							iterator.remove();
						}
					}
					result = conformanceComputer.getCommonSuperType(types, resolvedTypes.getReferenceOwner());
					if (result == null) {
						throw new UnsupportedOperationException("Cannot determine common super type of: " + types);
					}
				}
				return typeResolver.toJavaCompliantTypeReference(result, session);
			} finally {
				context.unsetTypeProviderWithoutNotification();
			}
		}

		@Override
		protected JvmTypeReference handleReentrantInvocation(XComputedTypeReferenceImplCustom context) {
			resolvedTypes.addDiagnostic(new EObjectDiagnosticImpl(
					Severity.WARNING,
					IssueCodes.TOO_LITTLE_TYPE_INFORMATION,
					"Cannot infer type from recursive usage. Type 'Object' is used.",
					typeResolver.getSourceElement(operation),
					null,
					-1,
					null));
			AnyTypeReference result = new AnyTypeReference(resolvedTypes.getReferenceOwner());
			return typeResolver.toJavaCompliantTypeReference(result, session);
		}
	}

	public static class DispatchParameterTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final JvmOperation operation;
		private final ResolvedTypes resolvedTypes;
		private final int idx;
		private final IFeatureScopeSession session;
		private final XtendReentrantTypeResolver typeResolver;

		public DispatchParameterTypeReferenceProvider(
				JvmOperation operation,
				int idx,
				ResolvedTypes resolvedTypes,
				IFeatureScopeSession session,
				XtendReentrantTypeResolver typeResolver) {
			this.idx = idx;
			this.operation = operation;
			this.resolvedTypes = resolvedTypes;
			this.session = session;
			this.typeResolver = typeResolver;
		}

		@Override
		/* @Nullable */
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			try {
				// TODO type parameters on dispatch operations
				List<JvmOperation> cases = typeResolver.dispatchHelper.getAllDispatchCases(operation);
				TypeConformanceComputer conformanceComputer = typeResolver.getServices().getTypeConformanceComputer();
				List<LightweightTypeReference> parameterTypes = Lists.newArrayListWithCapacity(cases.size());
				JvmOperation inheritedDispatcher = null;
				LightweightTypeReference inheritedParameterType = null;
				for (JvmOperation caseOperation : cases) {
					if (caseOperation.eContainer() != operation.eContainer()) {
						// inherited case, use the signature of the inherited dispatcher
						inheritedDispatcher = typeResolver.dispatchHelper.getDispatcherOperation(caseOperation);
						if (inheritedDispatcher != null) {
							JvmFormalParameter inheritedParameter = inheritedDispatcher.getParameters().get(idx);
							inheritedParameterType = resolvedTypes.getActualType(inheritedParameter);
							break;
						}
					}
				}
				for (JvmOperation caseOperation : cases) {
					JvmFormalParameter parameter = caseOperation.getParameters().get(idx);
					LightweightTypeReference parameterType = resolvedTypes.getActualType(parameter);
					if (parameterType != null && !parameterType.isType(Void.class)) {
						if (inheritedParameterType != null) {
							if (caseOperation.eContainer() == operation.eContainer()) {
								if (!inheritedParameterType.isAssignableFrom(parameterType)) {
									XtendParameter xtendParameter = (XtendParameter) typeResolver.getSourceElement(parameter);
									resolvedTypes.addDiagnostic(new EObjectDiagnosticImpl(
										Severity.ERROR,
										org.eclipse.xtend.core.validation.IssueCodes.DISPATCH_FUNCTIONS_INVALID_PARAMETER_TYPE,
										"Dispatch function cannot widen inherited parameter type " + inheritedParameterType.getHumanReadableName(),
										xtendParameter.getParameterType(),
										null,
										-1,
										null));
								}
							}
						} else {
							parameterTypes.add(parameterType);
						}
					}
				}
				// every parameter type is java.lang.Void so we use Object
				// otherwise it would only be possible to pass the null literal but not a null value, e.g. of type String
				// to the dispatcher
				if (parameterTypes.isEmpty()) {
					if (inheritedParameterType != null)
						return typeResolver.toJavaCompliantTypeReference(inheritedParameterType, session);
					return typeResolver.getServices().getTypeReferences().getTypeForName(Object.class, operation);
				}
				LightweightTypeReference parameterType = conformanceComputer.getCommonSuperType(parameterTypes, resolvedTypes.getReferenceOwner());
				if (parameterType == null) {
					throw new IllegalStateException("TODO: handle broken models properly");
				}
				return typeResolver.toJavaCompliantTypeReference(parameterType, session);
			} finally {
				context.unsetTypeProviderWithoutNotification();
			}
		}
	}

	public static class InitializerParameterTypeReferenceProvider extends AbstractReentrantTypeReferenceProvider {
		private final ResolvedTypes resolvedTypes;
		private final XtendFunction createFunction;
		private final IFeatureScopeSession featureScopeSession;
		private final Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext;
		private final XtendReentrantTypeResolver typeResolver;

		public InitializerParameterTypeReferenceProvider(
				XtendFunction createFunction,
				Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext,
				ResolvedTypes resolvedTypes,
				IFeatureScopeSession featureScopeSession,
				XtendReentrantTypeResolver typeResolver) {
			this.createFunction = createFunction;
			this.resolvedTypesByContext = resolvedTypesByContext;
			this.resolvedTypes = resolvedTypes;
			this.featureScopeSession = featureScopeSession;
			this.typeResolver = typeResolver;
		}

		@Override
		/* @Nullable */
		protected JvmTypeReference doGetTypeReference(XComputedTypeReferenceImplCustom context) {
			try {
				CreateExtensionInfo createExtensionInfo = createFunction.getCreateExtensionInfo();
				XExpression expression = createExtensionInfo.getCreateExpression();
				LightweightTypeReference actualType = resolvedTypes.getReturnType(expression);
				if (actualType == null) {
					JvmOperation operation = typeResolver.associations.getDirectlyInferredOperation(createFunction);
					IFeatureScopeSession session = operation.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext();
					typeResolver.computeTypes(resolvedTypesByContext, resolvedTypes, session, operation);
					actualType = resolvedTypes.getReturnType(expression);
				}
				if (actualType == null)
					return null;
				return typeResolver.toJavaCompliantTypeReference(actualType, featureScopeSession);
			} finally {
				context.unsetTypeProviderWithoutNotification();
			}
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
		/* @Nullable */
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
	
	@Inject
	private IJvmModelAssociator associator;
	
	@Inject
	private AnonymousClassUtil anonymousClassUtil;
	
	@Inject
	private JvmTypesBuilder typesBuilder;
	
	@Inject
	private OverrideTester overrideTester;

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
			for (XtendMember member : typeDeclaration.getMembers()) {
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
				for (XtendParameter parameter : function.getParameters()) {
					computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, parameter.getAnnotations());
				}
			} else if (member instanceof XtendConstructor) {
				XtendConstructor constructor = (XtendConstructor) member;
				expression = constructor.getExpression();
				for (XtendParameter parameter : constructor.getParameters()) {
					computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, parameter.getAnnotations());
				}
			} else if (member instanceof XtendField) {
				expression = ((XtendField) member).getInitialValue();
			}
			if (expression != null) {
				if (getLogicalContainerProvider().getLogicalContainer(expression) == null) {
					computeTypes(resolvedTypes, featureScopeSession, expression);
				} else {
					TreeIterator<EObject> iterator = EcoreUtil2.getAllNonDerivedContents(expression);
					while(iterator.hasNext()) {
						EObject next = iterator.next();
						if (next instanceof AnonymousClass) {
							computeTypes(resolvedTypes, featureScopeSession, next);
							iterator.prune();
						}
					}
				}
			}
			computeXtendAnnotationTypes(resolvedTypes, featureScopeSession, member.getAnnotations());
		} else {
			super.computeTypes(resolvedTypes, featureScopeSession, element);
		}
	}

	@Override
	protected boolean isHandled(XExpression expression) {
		if (getRoot() instanceof XtendTypeDeclaration) {
			XtendMember member = EcoreUtil2.getContainerOfType(expression, XtendMember.class);
			if (member != null) {
				XExpression memberExpression = getExpression(member);
				if (getLogicalContainerProvider().getLogicalContainer(memberExpression) == null) {
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
					XExpression memberExpression = getExpression(member);
					if (getLogicalContainerProvider().getLogicalContainer(memberExpression) == null) {
						return false;
					}
				}
			}
		}
		return super.isHandled(expression);
	}

	protected XExpression getExpression(XtendMember member) {
		if (member instanceof XtendField) {
			return ((XtendField) member).getInitialValue();
		} 
		if (member instanceof XtendExecutable) {
			return ((XtendExecutable) member).getExpression();
		}
		EObject container = member.eContainer();
		if (container instanceof XtendMember) {
			return getExpression((XtendMember) container);
		}
		return null;
	}

	/* @Nullable */
	protected XAnnotation getOutermostAnnotation(XExpression expression) {
		XAnnotation annotation = EcoreUtil2.getContainerOfType(expression, XAnnotation.class);
		while (annotation != null) {
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
				&& (identifiableElement.eContainingFeature() == XbasePackage.Literals.XCLOSURE__IMPLICIT_FORMAL_PARAMETERS
				|| identifiableElement.eContainingFeature() == XbasePackage.Literals.XCLOSURE__DECLARED_FORMAL_PARAMETERS)) {
			XtendMember member = EcoreUtil2.getContainerOfType(identifiableElement, XtendMember.class);
			if (member != null) {
				XExpression expression = getExpression(member);
				if (expression != null && getLogicalContainerProvider().getLogicalContainer(expression) == null) {
					return false;
				}
			}
		}
		return super.isHandled(identifiableElement);
	}

	protected boolean isAnnotationHolder(XtendMember member) {
		return member.eClass() == XtendPackage.Literals.XTEND_MEMBER
				|| member.eClass() == XtendPackage.Literals.XTEND_TYPE_DECLARATION;
	}

	@Override
	protected void _computeTypes(Map<JvmIdentifiableElement, ResolvedTypes> preparedResolvedTypes,
			ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmOperation operation) {
		ResolvedTypes childResolvedTypes = preparedResolvedTypes.get(operation);
		if (childResolvedTypes == null) {
			if (preparedResolvedTypes.containsKey(operation))
				return;
			throw new IllegalStateException("No resolved type found. Type was: " + operation.getIdentifier());
		}

		if (dispatchHelper.isDispatcherFunction(operation)) {
			// no associated expression, we just resolve it to the common super type of all associated cases
			// see #createTypeProvider and #_doPrepare
			preparedResolvedTypes.put(operation, null);
			computeAnnotationTypes(childResolvedTypes, featureScopeSession, operation);

			mergeChildTypes(childResolvedTypes);
		} else if (dispatchHelper.isDispatchFunction(operation)
				&& InferredTypeIndicator.isInferred(operation.getReturnType())) {
			JvmOperation dispatcher = dispatchHelper.getDispatcherOperation(operation);
			if (dispatcher == null) {
				super._computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, operation);
				return;
			}
			List<JvmOperation> dispatchCasesWithDeclaredReturnType = new ArrayList<JvmOperation>();
			List<JvmOperation> dispatchCasesWithInferredReturnType = new ArrayList<JvmOperation>();
			
			List<JvmOperation> dispatchCases = dispatchHelper.getLocalDispatchCases(dispatcher);
			for (JvmOperation dispatchCase : dispatchCases) {
				if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
					dispatchCasesWithInferredReturnType.add(dispatchCase);
				} else {
					dispatchCasesWithDeclaredReturnType.add(dispatchCase);
				}
			}

			try {
				markComputing(dispatcher.getReturnType());
				LightweightTypeReference declaredDispatcherType = getReturnTypeOfOverriddenOperation(dispatcher, childResolvedTypes, featureScopeSession);				
				List<LightweightTypeReference> dispatchCaseResults = Lists.newArrayListWithCapacity(dispatchCases.size());
				LightweightTypeReference implicitVoid = null;
				LightweightTypeReference thrownVoid = null;
				for (JvmOperation dispatchCase : dispatchCasesWithDeclaredReturnType) {
					ResolvedTypes dispatchCaseResolvedTypes = preparedResolvedTypes.get(dispatchCase);
					if (dispatchCaseResolvedTypes == null) {
						if (preparedResolvedTypes.containsKey(dispatchCase)) {
							dispatchCaseResolvedTypes = childResolvedTypes;
						}
					}
					if (dispatchCaseResolvedTypes == null) {
						throw new IllegalStateException("No resolved type found. Type was: " + dispatchCase.getIdentifier());
					}
					dispatchCaseResults.add(dispatchCaseResolvedTypes.getActualType(dispatchCase));
				}
				
				for (JvmOperation dispatchCase : dispatchCasesWithInferredReturnType) {
					ResolvedTypes dispatchCaseResolvedTypes = dispatchCase == operation ? childResolvedTypes : preparedResolvedTypes.get(dispatchCase);
					if (dispatchCaseResolvedTypes == null) {
						if (preparedResolvedTypes.containsKey(dispatchCase)) {
							if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
								if (declaredDispatcherType == null) {
									dispatchCaseResults.add(childResolvedTypes.getActualType(dispatchCase));
								}
							} else {
								dispatchCaseResults.add(childResolvedTypes.getActualType(dispatchCase));
							}
						} else {
							throw new IllegalStateException("No resolved type found. Type was: " + dispatchCase.getIdentifier());
						}
					} else {
						preparedResolvedTypes.put(dispatchCase, null);
						OperationBodyComputationState state = new DispatchOperationBodyComputationState(
								dispatchCaseResolvedTypes,
								dispatchCase.isStatic() ? featureScopeSession : featureScopeSession.toInstanceContext(),
								dispatchCase,
								dispatcher,
								declaredDispatcherType);
						addExtensionProviders(state, dispatchCase.getParameters());
						ITypeComputationResult dispatchCaseResult = null;
						try {
							markComputing(dispatchCase.getReturnType());
							dispatchCaseResult = state.computeTypes();
						} finally {
							unmarkComputing(dispatchCase.getReturnType());
						}
						if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
							if (declaredDispatcherType == null) {
								LightweightTypeReference returnType = dispatchCaseResult.getReturnType();
								if (returnType != null) {
									if (returnType.isPrimitiveVoid()) {
										Set<ConformanceHint> conformanceHints = dispatchCaseResult.getConformanceHints();
										if (!conformanceHints.contains(ConformanceHint.THROWN_EXCEPTION)) {
											if (conformanceHints.contains(ConformanceHint.NO_IMPLICIT_RETURN)) {
												dispatchCaseResults.add(returnType);
											} else {
												implicitVoid = returnType;
											}
										} else {
											thrownVoid = returnType;
										}
									} else {
										dispatchCaseResults.add(returnType);
									}
								}
							}
						} else {
							LightweightTypeReference explicitType = dispatchCaseResolvedTypes.getActualType(dispatchCase);
							dispatchCaseResults.add(explicitType);
						}
						computeAnnotationTypes(dispatchCaseResolvedTypes, featureScopeSession, dispatchCase);
						computeLocalTypes(preparedResolvedTypes, dispatchCaseResolvedTypes, featureScopeSession, dispatchCase);
						mergeChildTypes(dispatchCaseResolvedTypes);
					}
				}
				LightweightTypeReference commonDispatchType = normalizeDispatchReturnType(
						declaredDispatcherType,
						dispatchCaseResults,
						implicitVoid,
						thrownVoid,
						childResolvedTypes);
				if (commonDispatchType != null) {
					resolveDispatchCaseTypes(dispatcher, dispatchCasesWithInferredReturnType, commonDispatchType, featureScopeSession);
				}
			} finally {
				unmarkComputing(dispatcher.getReturnType());
			}
		} else {
			super._computeTypes(preparedResolvedTypes, resolvedTypes, featureScopeSession, operation);
		}
	}

	protected void resolveDispatchCaseTypes(JvmOperation dispatcher, List<JvmOperation> dispatchCases, LightweightTypeReference type,
			IFeatureScopeSession featureScopeSession) {
		if (InferredTypeIndicator.isInferred(dispatcher.getReturnType())) {
			InferredTypeIndicator.resolveTo(dispatcher.getReturnType(), toJavaCompliantTypeReference(type, featureScopeSession));
		}
		for (JvmOperation dispatchCase : dispatchCases) {
			if (InferredTypeIndicator.isInferred(dispatchCase.getReturnType())) {
				InferredTypeIndicator.resolveTo(dispatchCase.getReturnType(), toJavaCompliantTypeReference(type, featureScopeSession));
			}
		}
	}

	protected LightweightTypeReference normalizeDispatchReturnType(LightweightTypeReference declaredType,
			List<LightweightTypeReference> computedTypes,
			LightweightTypeReference implicitVoidOrNull,
			LightweightTypeReference thrownVoidOrNull,
			ResolvedTypes resolvedTypes) {
		LightweightTypeReference result = null;
		if (declaredType != null) {
			result = declaredType;
		} else {
			if (implicitVoidOrNull != null && !computedTypes.isEmpty()) {
				List<LightweightTypeReference> wrapped = Lists.newArrayListWithCapacity(computedTypes.size());
				for(int i = 0; i < computedTypes.size(); i++) {
					wrapped.add(computedTypes.get(i).getWrapperTypeIfPrimitive());
				}
				computedTypes = wrapped;
			}
			if (computedTypes.isEmpty() && implicitVoidOrNull != null) {
				result = implicitVoidOrNull;
			} else {
				if (computedTypes.isEmpty()) {
					if (thrownVoidOrNull == null) {
						throw new IllegalStateException("thrownVoidOrNull may not be null in this situation");
					}
					result = thrownVoidOrNull;
				} else {
					result = getServices().getTypeConformanceComputer().getCommonSuperType(computedTypes, resolvedTypes.getReferenceOwner());
				}
			}
		}
		return result;
	}

	protected void computeXtendAnnotationTypes(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, List<XAnnotation> annotations) {
		for (XAnnotation annotation : annotations) {
			if (getInferredElements(annotation).isEmpty())
				computeTypes(resolvedTypes, featureScopeSession, annotation);
		}
	}

	/**
	 * Initializes the type inference strategy for the cache field for create extensions.
	 */
	@Override
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession, JvmField field,
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
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
		doPrepareLocalTypes(resolvedTypes, featureScopeSession, field, resolvedTypesByContext);
	}
	
	@Override
	protected void _doPrepare(ResolvedTypes resolvedTypes, IFeatureScopeSession featureScopeSession,
			JvmConstructor constructor, Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		super._doPrepare(resolvedTypes, featureScopeSession, constructor, resolvedTypesByContext);
		doPrepareLocalTypes(resolvedTypes, featureScopeSession, constructor, resolvedTypesByContext);
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
			for (int i = 0; i < parameters.size(); i++) {
				JvmFormalParameter parameter = parameters.get(i);
				JvmTypeReference parameterType = parameter.getParameterType();
				if (InferredTypeIndicator.isInferred(parameterType)) {
					XComputedTypeReference casted = (XComputedTypeReference) parameterType;
					XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
					computedParameterType.setTypeProvider(new DispatchParameterTypeReferenceProvider(operation, i,
							resolvedTypes, featureScopeSession, this));
					casted.setEquivalent(computedParameterType);
				} else if (parameterType == null) {
					XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
					computedParameterType.setTypeProvider(new DispatchParameterTypeReferenceProvider(operation, i, resolvedTypes, featureScopeSession, this));
					parameter.setParameterType(computedParameterType);
				}
			}
		} else if (operation.getParameters().size() >= 1) {
			EObject sourceElement = associations.getPrimarySourceElement(operation);
			if (sourceElement instanceof XtendFunction) {
				XtendFunction function = (XtendFunction) sourceElement;
				if (function.getCreateExtensionInfo() != null) {
					JvmFormalParameter firstParameter = operation.getParameters().get(0);
					JvmTypeReference parameterType = firstParameter.getParameterType();
					if (InferredTypeIndicator.isInferred(parameterType)) {
						XComputedTypeReference casted = (XComputedTypeReference) parameterType;
						XComputedTypeReference computedParameterType = getServices().getXtypeFactory().createXComputedTypeReference();
						computedParameterType.setTypeProvider(new InitializerParameterTypeReferenceProvider(function, resolvedTypesByContext, resolvedTypes, featureScopeSession, this));
						casted.setEquivalent(computedParameterType);
					}
				}
			}
		}
		doPrepareLocalTypes(resolvedTypes, featureScopeSession, operation, resolvedTypesByContext);
	}

	protected void doPrepareLocalTypes(
			final ResolvedTypes resolvedTypes,
			IFeatureScopeSession featureScopeSession,
			JvmFeature container,
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext) {
		List<JvmGenericType> localClasses = container.getLocalClasses();
		for(final JvmGenericType localClass: localClasses) {
			JvmTypeReference superType = localClass.getSuperTypes().get(0);
			final IFeatureScopeSession nestedSession = featureScopeSession;
			if (InferredTypeIndicator.isInferred(superType)) {
				final XComputedTypeReference casted = (XComputedTypeReference) superType;
				InferredTypeIndicator typeProvider = (InferredTypeIndicator) casted.getTypeProvider();
				final AnonymousClass anonymousClass = (AnonymousClass) typeProvider.getExpression();
				XConstructorCall constructorCall = anonymousClass.getConstructorCall();
				IScope typeScope = featureScopeSession.getScope(constructorCall, TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE, resolvedTypes);
				final JvmDeclaredType type = anonymousClassUtil.getSuperTypeNonResolving(anonymousClass, typeScope);
				if (type == null) {
					break;
				}
				final JvmParameterizedTypeReference superTypeReference = createSuperTypeReference(type, constructorCall);
				requestCapturedLocalVariables(superTypeReference, localClass, resolvedTypes, resolvedTypesByContext, new IAcceptor<JvmTypeReference>() {
					public void accept(JvmTypeReference capturingTypeReference) {
						casted.setEquivalent(capturingTypeReference);
						IFeatureScopeSession mySession = addThisAndSuper(nestedSession, resolvedTypes.getReferenceOwner(), localClass, superTypeReference, false);
						if(type.eClass() == TypesPackage.Literals.JVM_GENERIC_TYPE && ((JvmGenericType) type).isInterface()) {
							inferAnonymousClassConstructor(anonymousClass, localClass, type);
						} else {
							for(JvmMember superMember: type.getMembers()) {
								if (superMember instanceof JvmConstructor) {
									JvmConstructor superTypeConstructor = (JvmConstructor) superMember;
									boolean visible = mySession.isVisible(superTypeConstructor);
									inferAnonymousClassConstructor(anonymousClass, localClass, superTypeConstructor, visible);
								}
							}
						}
					}
				});
				
			}
		}
	}
	
	protected JvmParameterizedTypeReference createSuperTypeReference(JvmType superType, XConstructorCall constructorCall) {
		JvmParameterizedTypeReference result = TypesFactory.eINSTANCE.createJvmParameterizedTypeReference();
		result.setType(superType);
		for(JvmTypeReference typeReference: constructorCall.getTypeArguments()) {
			result.getArguments().add(typesBuilder.cloneWithProxies(typeReference));
		}
		return result;
	}
	
	protected JvmConstructor inferAnonymousClassConstructor(AnonymousClass anonymousClass, JvmGenericType inferredLocalClass, JvmConstructor superConstructor, boolean visible) {
		JvmConstructor constructor = TypesFactory.eINSTANCE.createJvmConstructor();
		inferredLocalClass.getMembers().add(constructor);
		associator.associatePrimary(anonymousClass.getConstructorCall(), constructor);
		if (visible) {
			constructor.setVisibility(JvmVisibility.DEFAULT);
		} else
			constructor.setVisibility(JvmVisibility.PRIVATE);
		constructor.setSimpleName(inferredLocalClass.getSimpleName());
		final List<JvmFormalParameter> parameters = superConstructor.getParameters();
		for(JvmFormalParameter parameter: parameters) 
			constructor.getParameters().add(typesBuilder.cloneWithProxies(parameter));
		
		for (JvmTypeReference exception : superConstructor.getExceptions()) 
			constructor.getExceptions().add(typesBuilder.cloneWithProxies(exception));
		
		if (!parameters.isEmpty()) {
			typesBuilder.setBody(constructor, new Procedures.Procedure1<ITreeAppendable>() {
				public void apply(ITreeAppendable a) {
					a.append("super(");
					for(int i = 0; i < parameters.size(); i++) {
						if (i != 0) {
							a.append(", ");
						}
						a.append(parameters.get(i).getSimpleName());
					}
					a.append(");");
				}
				
			});
		}
		return constructor;
	}

	protected JvmConstructor inferAnonymousClassConstructor(AnonymousClass anonymousClass, JvmGenericType inferredLocalClass, JvmDeclaredType superInterface) {
		XConstructorCall constructorCall = anonymousClass.getConstructorCall();
		JvmConstructor constructor = TypesFactory.eINSTANCE.createJvmConstructor();
		inferredLocalClass.getMembers().add(constructor);
		associator.associatePrimary(constructorCall, constructor);
		constructor.setVisibility(JvmVisibility.DEFAULT);
		constructor.setSimpleName(inferredLocalClass.getSimpleName());
		return constructor;
	}
	
	@Override
	protected AbstractReentrantTypeReferenceProvider createTypeProvider(
			Map<JvmIdentifiableElement, ResolvedTypes> resolvedTypesByContext,
			ResolvedTypes resolvedTypes,
			IFeatureScopeSession featureScopeSession,
			JvmMember member,
			boolean returnType) {
		if (member instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) member;
			if (dispatchHelper.isDispatcherFunction(operation)) {
				return new DispatchReturnTypeReferenceProvider(operation, resolvedTypes, featureScopeSession, this);
			}
		}
		return super.createTypeProvider(resolvedTypesByContext, resolvedTypes, featureScopeSession, member, returnType);
	}
	
	@Override
	protected String getInvalidWritableVariableAccessMessage(XVariableDeclaration variable,
			XAbstractFeatureCall featureCall) {
		// TODO this should be part of a separate validation service
		EObject containingStructure = getNearestClosureOrTypeDeclaration(featureCall);
		if (containingStructure != null && !EcoreUtil.isAncestor(containingStructure, variable)) {
			if (containingStructure instanceof XClosure)
				return String.format("Cannot refer to the non-final variable %s inside a lambda expression", variable.getSimpleName());
			return String.format("Cannot refer to the non-final variable %s inside a local class", variable.getSimpleName());
		}
		return null;
	}
	
	@Override
	protected LightweightTypeReference getReturnTypeOfOverriddenOperation(JvmOperation operation,
			ResolvedTypes resolvedTypes, IFeatureScopeSession session) {
		if (operation.getVisibility() == JvmVisibility.PRIVATE)
			return null;
		if (InferredTypeIndicator.isInferred(operation.getReturnType())) {
			LightweightTypeReference declaringType = resolvedTypes.getActualType(operation.getDeclaringType());
			if (declaringType == null) {
				throw new IllegalStateException("Cannot determine declaring type of operation: " + operation);
			}
			BottomResolvedOperation resolvedOperation = new BottomResolvedOperation(operation, declaringType, overrideTester);
			List<IResolvedOperation> overriddenMethods = resolvedOperation.getOverriddenAndImplementedMethods();
			if (overriddenMethods.isEmpty())
				return null;
			IResolvedOperation overriddenMethod = overriddenMethods.get(0);
			JvmOperation declaration = overriddenMethod.getDeclaration();
			XExpression inferredFrom = getInferredFrom(declaration.getReturnType());
			// guard against active annotations that put an expression into a second method
			// namely in a synthesized super type - in that case, the expression should not be
			// inferred in the context of the super type but the subtype thus the return type
			// of a super method has to be ignored
			
			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=439535
			if (inferredFrom != null && (inferredFrom == getInferredFrom(operation.getReturnType()) || isHandled(inferredFrom))) {
				return null;
			}
			LightweightTypeReference result = overriddenMethod.getResolvedReturnType();
			return result;
		}
		return null;
	}
	
	private EObject getNearestClosureOrTypeDeclaration(EObject object) {
		while(object != null) {
			if (object instanceof XClosure) {
				return object;
			}
			if (object instanceof XtendTypeDeclaration) {
				return object;
			}
			object = object.eContainer();
		}
		return null;
	}
}
