/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.scoping.batch;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;
import org.eclipse.xtext.util.Wrapper;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XUnaryOperation;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.scoping.featurecalls.OperatorMapping;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.computation.SynonymTypesProvider;
import org.eclipse.xtext.xbase.typesystem.conformance.ConformanceHint;
import org.eclipse.xtext.xbase.typesystem.internal.ScopeProviderAccess;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.inject.Inject;

/**
 * Utility class when dealing with feature scopes or constructor scopes.
 * Clients may want to override this one if they want to 
 * add a new implicit receiver.
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 * 
 * TODO constructor scopes
 */
public class FeatureScopes implements IFeatureNames {

	@Inject
	private OperatorMapping operatorMapping;
	
	@Inject
	private SynonymTypesProvider synonymProvider;
	
	@Inject(optional = true)
	private XbaseFactory xbaseFactory = XbaseFactory.eINSTANCE;
	
	/**
	 * creates the feature scope for {@link XAbstractFeatureCall}, including the local variables in case it is a feature
	 * call without receiver (XFeatureCall).
	 * @param session the currently available session data
	 */
	public IScope createFeatureCallScope(EObject context, EReference reference, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		if (!(context instanceof XAbstractFeatureCall)) {
			return IScope.NULLSCOPE;
		}
		XAbstractFeatureCall call = (XAbstractFeatureCall) context;
		final XExpression syntacticalReceiver = getSyntacticalReceiver(call);
		if (syntacticalReceiver == null) {
			IScope result = createSimpleFeatureCallScope(call, reference, session, resolvedTypes);
			return result;
		}
		IScope result = createFeatureCallScopeForReceiver(call, syntacticalReceiver, reference, session, resolvedTypes);
		return result;
	}

	public boolean isFeatureCallScope(EReference reference) {
		return reference == XbasePackage.Literals.XABSTRACT_FEATURE_CALL__FEATURE;
	}
	
	/**
	 * This method serves as an entry point for the content assist scoping for simple feature calls.
	 * @param context the context e.g. a for loop expression, a block or a catch clause
	 * @param reference the reference who's value shall be scoped. Not necessarily a feature of the context.
	 */
	public IScope createSimpleFeatureCallScope(EObject context, EReference reference, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		if (context instanceof XFeatureCall) {
			XFeatureCall featureCall = (XFeatureCall) context;
			JvmDeclaredType declaringType = featureCall.getDeclaringType();
			if (declaringType != null) {
				if (declaringType.eIsProxy()) {
					return createFollowUpErrorScope();
				}
				return createStaticScope(featureCall, declaringType, null, null, IScope.NULLSCOPE, session);
			}
		}
		IScope staticImports = createStaticFeaturesScope(context, IScope.NULLSCOPE, session);
		IScope staticMembers = createStaticScope(asAbstractFeatureCall(context), null, null, staticImports, session, resolvedTypes);
		IScope staticExtensions = createStaticExtensionsScope(null, null, context, staticMembers, session, resolvedTypes);
		IScope dynamicExtensions = createDynamicExtensionsScope(null, null, context, staticExtensions, session, resolvedTypes);
		IScope implicitReceivers = createImplicitFeatureCallScope(context, dynamicExtensions, session, resolvedTypes);
		IScope constructors = new ConstructorDelegateScope(implicitReceivers, session, asAbstractFeatureCall(context));
		IScope localVariables = new LocalVariableScope(constructors, session, asAbstractFeatureCall(context));
		return localVariables;
	}

	protected XAbstractFeatureCall asAbstractFeatureCall(EObject context) {
		return context instanceof XAbstractFeatureCall ? (XAbstractFeatureCall) context : null;
	}

	protected IScope createFeatureScopeForTypeRef(
			final XExpression receiver,
			final LightweightTypeReference featureDeclarator,
			final boolean implicit,
			final EObject featureCall,
			final IFeatureScopeSession session,
			final JvmIdentifiableElement receiverFeature,
			IScope parent) {
		final Wrapper<IScope> wrapper = Wrapper.wrap(parent);
		synonymProvider.collectSynonymTypes(featureDeclarator, new SynonymTypesProvider.Acceptor() {

			int id = 100;
			
			@NonNullByDefault
			@Override
			protected boolean accept(LightweightTypeReference synonymType, EnumSet<ConformanceHint> hints) {
				List<JvmType> rawTypes = synonymType.getRawTypes();
				SynonymTypeBucket bucket = new SynonymTypeBucket(id++, rawTypes, hints);
				wrapper.set(new ReceiverFeatureScope(wrapper.get(), session, receiver, synonymType, implicit, asAbstractFeatureCall(featureCall), bucket, receiverFeature, operatorMapping));
				return true;
			}
			
		});
		List<JvmType> rawTypes = featureDeclarator.getRawTypes();
		TypeBucket typeBucket = new TypeBucket(-1, rawTypes);
		IScope result = new ReceiverFeatureScope(wrapper.get(), session, receiver, featureDeclarator, implicit, asAbstractFeatureCall(featureCall), typeBucket, receiverFeature, operatorMapping);
		return result;
	}

	/**
	 * This method serves as an entry point for the content assist scoping for features.
	 * @param featureCall the context provides access to the resource set. If it is an assignment, it 
	 *   will be used to restrict scoping.
	 * @param receiver the receiver of the feature call.
	 * @param resolvedTypes TODO
	 * @param session TODO
	 */
	public IScope createFeatureCallScopeForReceiver(final XExpression featureCall, final XExpression receiver, EReference reference, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		if (!isFeatureCallScope(reference))
			return IScope.NULLSCOPE;
		if (receiver == null || receiver.eIsProxy())
			return IScope.NULLSCOPE;
		LightweightTypeReference receiverType = resolvedTypes.getActualType(receiver);
		if (receiverType != null && !receiverType.isUnknown()) {
			JvmIdentifiableElement linkedReceiver = resolvedTypes.getLinkedFeature(asAbstractFeatureCall(receiver));
			// check if 'super' was used as receiver which renders extension features and static features invalid
			if (isValidFeatureCallArgument(receiver, linkedReceiver, session)) {
				
				// static members that are invoked on a receiver, e.g. myString.CASE_INSENSITIVE_ORDER
				IScope staticScope = createStaticScope(asAbstractFeatureCall(featureCall), receiver, receiverType, IScope.NULLSCOPE, session, resolvedTypes);
				
				// static extensions, if any, e.g. iterable.map [], or things that have been imported by means of import static extension MyType
				IScope staticExtensionScope = createStaticExtensionsScope(receiver, receiverType, featureCall, staticScope, session, resolvedTypes);
				
				// instance extensions, e.g. extension ReflectionUtils with myObject.get('privateField')
				IScope extensionScope = createDynamicExtensionsScope(receiver, receiverType, featureCall, staticExtensionScope, session, resolvedTypes);
				
				// instance members, e.g. this.toString
				return createFeatureScopeForTypeRef(receiver, receiverType, false, featureCall, session, linkedReceiver, extensionScope);
			} else {
				
				// put only instance members into the scope
				return createFeatureScopeForTypeRef(receiver, receiverType, false, featureCall, session, linkedReceiver, IScope.NULLSCOPE);
			}
		} else {
			return createFollowUpErrorScope();
		}
	}

	protected IScope createFollowUpErrorScope() {
		return new SimpleScope(Collections.<IEObjectDescription>emptyList()) {
			@Override
			public Iterable<IEObjectDescription> getElements(QualifiedName name) {
				return Collections.<IEObjectDescription>singletonList(new ScopeProviderAccess.ErrorDescription());
			}
		};
	}
	
	/**
	 * Returns <code>true</code> if the linked receiver may be passed as an argument. Basically everything could
	 * be passed as an argument except the linked receiver is null, a proxy or a reference to <code>super</code>.
	 */
	protected boolean isValidFeatureCallArgument(XExpression expression, JvmIdentifiableElement linkedReceiver, IFeatureScopeSession session) {
		if (linkedReceiver instanceof JvmType) {
			IEObjectDescription knownSuperType = session.getLocalElement(SUPER);
			if (knownSuperType != null && linkedReceiver == knownSuperType.getEObjectOrProxy()) {
				return false;
			}
		}
		return !(expression instanceof XAbstractFeatureCall) || linkedReceiver != null && !linkedReceiver.eIsProxy();
	}

	protected IScope createStaticExtensionsScope(XExpression receiver, LightweightTypeReference receiverType, EObject featureCall, IScope parent, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		IScope result = parent;
		if (receiver == null) {
			// 'this' is a valid implicit first argument, e.g. implementations of Iterable may use #filter on themselves
			result = createImplicitExtensionScope(THIS, featureCall, session, resolvedTypes, result);
			// 'it' has a higher priority than 'this' as implicit first argument
			result = createImplicitExtensionScope(IT, featureCall, session, resolvedTypes, result);
			return result;
		} else {
			result = createStaticExtensionsScope(receiver, receiverType, false, featureCall, parent, session);
		}
		return result;
	}
	
	protected IScope createStaticScope(XAbstractFeatureCall featureCall, XExpression receiver, LightweightTypeReference receiverType, IScope parent,
			IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		IScope result = parent;
		if (receiver == null) {
			result = createImplicitStaticScope(THIS, featureCall, session, resolvedTypes, result);
			result = createImplicitStaticScope(IT, featureCall, session, resolvedTypes, result);
			return result;
		} else {
			TypeBucket receiverBucket = new TypeBucket(-1, Collections.singletonList(receiverType.getType()));
			return new StaticFeatureScope(parent, session, featureCall, receiver, receiverType, receiverBucket, operatorMapping);
		}
	}

	protected IScope createStaticScope(XAbstractFeatureCall featureCall, JvmType type, XExpression receiver, LightweightTypeReference receiverType,
			IScope parent, IFeatureScopeSession session) {
		TypeBucket receiverBucket = new TypeBucket(-1, Collections.singletonList(type));
		return new StaticFeatureScope(parent, session, featureCall, receiver, receiverType, receiverBucket, operatorMapping);
	}
	
	protected IScope createDynamicExtensionsScope(XExpression firstArgument, LightweightTypeReference firstArgumentType, EObject featureCall, IScope parent, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		List<ExpressionBucket> extensionProviders = session.getExtensionProviders();
		if (extensionProviders.isEmpty()) {
			return parent;
		}
		IScope result = parent;
		if (firstArgument == null) {
			result = createDynamicExtensionsScope(IT, featureCall, session, resolvedTypes, result);
			return result;
		} else {
			result = createDynamicExtensionsScope(firstArgument, firstArgumentType, false, featureCall, parent, session);
		}
		return result;
	}
	
	protected IScope createDynamicExtensionsScope(QualifiedName implicitFirstArgumentName, EObject featureCall,
			IFeatureScopeSession session, IResolvedTypes resolvedTypes, IScope parent) {
		IEObjectDescription firstArgumentDescription = session.getLocalElement(implicitFirstArgumentName);
		if (firstArgumentDescription != null) {
			JvmIdentifiableElement feature = (JvmIdentifiableElement) firstArgumentDescription.getEObjectOrProxy();
			LightweightTypeReference type = resolvedTypes.getActualType(feature);
			if (type != null && !type.isUnknown()) {
				XFeatureCall implicitArgument = xbaseFactory.createXFeatureCall();
				implicitArgument.setFeature(feature);
				return createDynamicExtensionsScope(implicitArgument, type, true, featureCall, parent, session);
			}
			return parent;
		} else {
			return createDynamicExtensionsScope(null, null, true, featureCall, parent, session);
		}
	}
	
	protected DynamicExtensionsScope createDynamicExtensionsScope(XExpression firstArgument,
			LightweightTypeReference argumentType, boolean implicit, EObject featureCall, IScope parent, IFeatureScopeSession session) {
		return new DynamicExtensionsScope(parent, session, firstArgument, argumentType, implicit, asAbstractFeatureCall(featureCall), operatorMapping);
	}

	protected StaticExtensionImportsScope createStaticExtensionsScope(XExpression receiver,
			LightweightTypeReference receiverType, boolean implicit, EObject featureCall, IScope parent, IFeatureScopeSession session) {
		return new StaticExtensionImportsScope(parent, session, receiver, receiverType, implicit, asAbstractFeatureCall(featureCall), operatorMapping);
	}

	protected XExpression getSyntacticalReceiver(final XAbstractFeatureCall call) {
		if (call instanceof XMemberFeatureCall) {
			return ((XMemberFeatureCall) call).getMemberCallTarget();
		}
		if (call instanceof XBinaryOperation) {
			return ((XBinaryOperation) call).getLeftOperand();
		}
		if (call instanceof XUnaryOperation) {
			return ((XUnaryOperation) call).getOperand();
		}
		if (call instanceof XAssignment) {
			return ((XAssignment) call).getAssignable();
		}
		return null;
	}

	protected IScope createStaticFeaturesScope(EObject featureCall, IScope parent, IFeatureScopeSession session) {
		return new StaticImportsScope(parent, session, asAbstractFeatureCall(featureCall));
	}
	
	protected IScope createImplicitFeatureCallScope(EObject featureCall, IScope parent, IFeatureScopeSession session, IResolvedTypes resolvedTypes) {
		IScope result = parent;
		result = createImplicitFeatureCallScope(THIS, featureCall, session, resolvedTypes, result);
		result = createImplicitFeatureCallScope(IT, featureCall, session, resolvedTypes, result);
		return result;
	}

	protected IScope createImplicitFeatureCallScope(QualifiedName implicitName, EObject featureCall,
			IFeatureScopeSession session, IResolvedTypes resolvedTypes, IScope parent) {
		IEObjectDescription thisDescription = session.getLocalElement(implicitName);
		if (thisDescription != null) {
			JvmIdentifiableElement thisElement = (JvmIdentifiableElement) thisDescription.getEObjectOrProxy();
			LightweightTypeReference type = resolvedTypes.getActualType(thisElement);
			if (type !=null && !type.isUnknown()) {
				XFeatureCall implicitReceiver = xbaseFactory.createXFeatureCall();
				implicitReceiver.setFeature(thisElement);
				return createFeatureScopeForTypeRef(implicitReceiver, type, true, featureCall, session, thisElement, parent);
			}
		}
		return parent;
	}
	
	protected IScope createImplicitExtensionScope(QualifiedName implicitName, EObject featureCall,
			IFeatureScopeSession session, IResolvedTypes resolvedTypes, IScope parent) {
		IEObjectDescription thisDescription = session.getLocalElement(implicitName);
		if (thisDescription != null) {
			JvmIdentifiableElement thisElement = (JvmIdentifiableElement) thisDescription.getEObjectOrProxy();
			LightweightTypeReference type = resolvedTypes.getActualType(thisElement);
			if (type != null && !type.isUnknown()) {
				XFeatureCall implicitReceiver = xbaseFactory.createXFeatureCall();
				implicitReceiver.setFeature(thisElement);
				return createStaticExtensionsScope(implicitReceiver, type, true, featureCall, parent, session);
			}
		}
		return parent;
	}
	
	protected IScope createImplicitStaticScope(QualifiedName implicitName, XAbstractFeatureCall featureCall, IFeatureScopeSession session,
			IResolvedTypes resolvedTypes, IScope parent) {
		IEObjectDescription thisDescription = session.getLocalElement(implicitName);
		if (thisDescription != null) {
			JvmIdentifiableElement thisElement = (JvmIdentifiableElement) thisDescription.getEObjectOrProxy();
			LightweightTypeReference type = resolvedTypes.getActualType(thisElement);
			if (type != null && !type.isUnknown()) {
				TypeBucket receiverBucket = new TypeBucket(-1, Collections.singletonList(type.getType()));
				return new StaticFeatureScope(parent, session, featureCall, null, type, receiverBucket, operatorMapping);
			}
		}
		return parent;
	}
}
