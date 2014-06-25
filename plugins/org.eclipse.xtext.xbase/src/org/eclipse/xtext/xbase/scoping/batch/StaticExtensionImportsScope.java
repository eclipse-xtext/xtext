/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.scoping.batch;

import java.beans.Introspector;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.scoping.featurecalls.OperatorMapping;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class StaticExtensionImportsScope extends AbstractStaticImportsScope {

	private final ExtensionScopeHelper helper;
	private final XExpression receiver;
	private final LightweightTypeReference receiverType;
	private final boolean implicit;

	public StaticExtensionImportsScope(IScope parent, IFeatureScopeSession session, 
			XExpression receiver, LightweightTypeReference receiverType, boolean implicit,
			XAbstractFeatureCall context, OperatorMapping operatorMapping) {
		super(parent, session, context, operatorMapping);
		this.receiver = receiver;
		this.receiverType = receiverType;
		this.implicit = implicit;
		this.helper = new ExtensionScopeHelper(receiverType);
	}

	@Override
	protected List<TypeBucket> getBuckets() {
		return getSession().getStaticallyImportedExtensionTypes();
	}
	
	@Override
	protected Iterable<IEObjectDescription> getAllLocalElements() {
		List<TypeBucket> buckets = getBuckets();
		if (buckets.isEmpty()) {
			return Collections.emptyList();
		}
		List<IEObjectDescription> result = Lists.newArrayList();
		for(TypeBucket bucket: buckets) {
			if (bucket.isRestrictingNames()) {
				for(Map.Entry<? extends JvmType, ? extends Set<String>> entry: bucket.getTypesToNames().entrySet()) {
					JvmType type = entry.getKey();
					if (type instanceof JvmDeclaredType) {
						Iterable<JvmFeature> features = ((JvmDeclaredType) type).getAllFeatures();
						for(JvmFeature feature: features) {
							if (feature.isStatic() && entry.getValue().contains(feature.getSimpleName())
									&& helper.isPossibleExtension(feature) && helper.isMatchingFirstParameter((JvmOperation) feature)) {
								fastAddDescriptions(feature, bucket, result);
							}
						}
					}
				}
			} else {
				for(JvmType type: bucket.getTypes()) {
					if (type instanceof JvmDeclaredType) {
						Iterable<JvmFeature> features = ((JvmDeclaredType) type).getAllFeatures();
						for(JvmFeature feature: features) {
							if (feature.isStatic() && helper.isPossibleExtension(feature) && helper.isMatchingFirstParameter((JvmOperation) feature)) {
								fastAddDescriptions(feature, bucket, result);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	@Override
	protected BucketedEObjectDescription createDescription(QualifiedName name, JvmFeature feature,
			TypeBucket bucket) {
		if (!helper.isPossibleExtension(feature)) {
			return null;
		}
		if (!helper.isMatchingFirstParameter((JvmOperation) feature)) {
			return null;
		}
		return doCreateDescription(name, feature, bucket);
	}

	protected BucketedEObjectDescription doCreateDescription(QualifiedName name, JvmFeature feature, TypeBucket bucket) {
		if (implicit) {
			return new StaticExtensionFeatureDescriptionWithImplicitFirstArgument(name, feature, receiver, receiverType, bucket.getId(), getSession().isVisible(feature));
		}
		return new StaticExtensionFeatureDescription(name, feature, receiver, receiverType, bucket.getId(), getSession().isVisible(feature));
	}

	protected void fastAddDescriptions(JvmFeature feature, TypeBucket bucket, List<IEObjectDescription> result) {
		if (receiverType != null && !receiverType.isResolved()) {
			return;
		}
		
		String simpleName = feature.getSimpleName();
		QualifiedName featureName = QualifiedName.create(simpleName);
		BucketedEObjectDescription description = doCreateDescription(featureName, feature, bucket);
		result.add(description);
		String propertyName = toProperty(simpleName, feature);
		if (propertyName != null) {
			result.add(createDescription(QualifiedName.create(propertyName), feature, bucket));
		}
		if (!implicit) {
			QualifiedName operator = getOperatorMapping().getOperator(featureName);
			if (operator != null) {
				result.add(createDescription(operator, feature, bucket));
			}
		}
	}
	
	@Override
	protected void addDescriptions(JvmFeature feature, TypeBucket bucket, List<IEObjectDescription> result) {
		String simpleName = feature.getSimpleName();
		QualifiedName featureName = QualifiedName.create(simpleName);
		BucketedEObjectDescription description = createDescription(featureName, feature, bucket);
		if (description != null) {
			result.add(description);
			String propertyName = toProperty(simpleName, feature);
			if (propertyName != null) {
				result.add(doCreateDescription(QualifiedName.create(propertyName), feature, bucket));
			}
			if (!implicit) {
				QualifiedName operator = getOperatorMapping().getOperator(featureName);
				if (operator != null) {
					result.add(doCreateDescription(operator, feature, bucket));
				}
			}
		}
	}
	
	@Override
	protected String toProperty(String methodName, JvmFeature feature) {
		if (feature instanceof JvmOperation) {
			JvmOperation operation = (JvmOperation) feature;
			if (methodName.length() > 3 && (methodName.startsWith("get") && operation.getParameters().size() == 1 || methodName.startsWith("set") && operation.getParameters().size() == 2) && Character.isUpperCase(methodName.charAt(3))) {
				return Introspector.decapitalize(methodName.substring(3));
			}
			if (methodName.length() > 3 && methodName.startsWith("is") && Character.isUpperCase(methodName.charAt(2)) && operation.getParameters().size() == 1) {
				return Introspector.decapitalize(methodName.substring(2));
			}
		}
		return null;
	}
}
