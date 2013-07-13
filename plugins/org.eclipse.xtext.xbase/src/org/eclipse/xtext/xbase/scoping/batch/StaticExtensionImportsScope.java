/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.scoping.batch;

import java.util.List;

import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.scoping.featurecalls.OperatorMapping;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.OwnedConverter;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class StaticExtensionImportsScope extends AbstractStaticImportsScope {

	private final OperatorMapping operatorMapping;
	private final XExpression receiver;
	private final LightweightTypeReference receiverType;
	private final boolean implicit;

	public StaticExtensionImportsScope(IScope parent, IFeatureScopeSession session, 
			XExpression receiver, LightweightTypeReference receiverType, boolean implicit,
			XAbstractFeatureCall context, OperatorMapping operatorMapping) {
		super(parent, session, context);
		this.receiver = receiver;
		this.receiverType = receiverType;
		this.implicit = implicit;
		this.operatorMapping = operatorMapping;
	}

	@Override
	protected List<TypeBucket> getBuckets() {
		return getSession().getStaticallyImportedExtensionTypes();
	}
	
	@Override
	protected void processFeatureNames(QualifiedName name, NameAcceptor acceptor) {
		QualifiedName methodName = operatorMapping.getMethodName(name);
		if (methodName != null) {
			acceptor.accept(methodName.toString(), 2);
		} else {
			super.processFeatureNames(name, acceptor);
		}
	}
	
	@Override
	protected BucketedEObjectDescription createDescription(QualifiedName name, JvmFeature feature,
			TypeBucket bucket) {
		if (!(feature instanceof JvmOperation)) {
			return null;
		}
		List<JvmFormalParameter> parameters = ((JvmExecutable) feature).getParameters();
		if (parameters.isEmpty()) {
			return null;
		}
		JvmFormalParameter firstParameter = parameters.get(0);
		JvmTypeReference type = firstParameter.getParameterType();
		if (type == null)
			return null;
		JvmType rawParameterType = type.getType();
		if (!(rawParameterType instanceof JvmTypeParameter)) {
			LightweightTypeReference rawReceiverType = receiverType.getRawTypeReference();
			if (rawReceiverType.isResolved()) {
				// short circuit - limit extension scope entries to real candidates
				LightweightTypeReference parameterTypeReference = new OwnedConverter(rawReceiverType.getOwner()).toRawLightweightReference(rawParameterType);
				if (parameterTypeReference.isResolved() && !parameterTypeReference.isAssignableFrom(rawReceiverType)) {
					return null;
				}
				if (parameterTypeReference.isArray() && !rawReceiverType.isArray() && !rawReceiverType.isSubtypeOf(Iterable.class)) {
					return null;
				}
			} else if (isArrayTypeMismatch(rawReceiverType, rawParameterType)) {
				return null;
			}
		}
		if (implicit) {
			return new StaticExtensionFeatureDescriptionWithImplicitFirstArgument(name, feature, receiver, receiverType, bucket.getId(), getSession().isVisible(feature));
		}
		return new StaticExtensionFeatureDescription(name, feature, receiver, receiverType, bucket.getId(), getSession().isVisible(feature));
	}
	
	private boolean isArrayTypeMismatch(LightweightTypeReference rawReceiverType, JvmType rawParameterType) {
		if (rawReceiverType.isArray()) {
			LightweightTypeReference parameterTypeReference = new OwnedConverter(rawReceiverType.getOwner()).toRawLightweightReference(rawParameterType);
			if (parameterTypeReference.getSuperType(Iterable.class) == null && isArrayTypeMismatch(rawReceiverType, parameterTypeReference)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isArrayTypeMismatch(LightweightTypeReference receiverType, LightweightTypeReference parameterType) {
		if (receiverType.isArray()) {
			if (parameterType.isArray()) {
				LightweightTypeReference componentType = parameterType.getComponentType();
				if (isArrayTypeMismatch(receiverType.getComponentType(), componentType)) {
					return true;
				}
				return false;
			}
			return true;
		} else {
			if (!parameterType.isArray() && parameterType.isPrimitive()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void addDescriptions(JvmFeature feature, TypeBucket bucket, List<IEObjectDescription> result) {
		QualifiedName featureName = QualifiedName.create(feature.getSimpleName());
		result.add(createDescription(featureName, feature, bucket));
		QualifiedName operator = operatorMapping.getOperator(featureName);
		if (operator != null) {
			result.add(createDescription(operator, feature, bucket));
		}
	}
}
