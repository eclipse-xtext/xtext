/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * TODO measure whether this thing is worth the effort
 */
public class SimpleAttributeResolver<K extends EObject, T> implements Function<K, T>{
	
	public final static SimpleAttributeResolver<EObject, String> NAME_RESOLVER = newResolver(String.class, "name");

	private static final Object NO_ATTRIBUTE = new Object();

	private final ConcurrentMap<EClass, Object> attributeCache = new ConcurrentHashMap<>();

	private final Class<T> type;

	private final SimpleCache<EObject, T> valueCache;

	private final String attributeName;

	private final Adapter discardingAdapter;

	public static <K extends EObject, T> SimpleAttributeResolver<K, T> newResolver(final Class<T> type, final String attributeName) {
		return new SimpleAttributeResolver<K, T>(type, attributeName);
	}

	public EAttribute getAttribute(final EObject object) {
		return attributeOf(object.eClass());
	}

	private EAttribute attributeOf(final EClass eClass) {
		Object attribute = attributeCache.get(eClass);
		if (attribute == null) {
			// not computeIfAbsent: it would hold a lock while getInstanceClass() may load classes
			attribute = computeAttribute(eClass);
			final Object existing = attributeCache.putIfAbsent(eClass, attribute);
			if (existing != null) {
				attribute = existing;
			}
		}
		return attribute == NO_ATTRIBUTE ? null : (EAttribute) attribute;
	}

	private Object computeAttribute(final EClass eClass) {
		final EStructuralFeature structuralFeature = eClass.getEStructuralFeature(attributeName);
		if (structuralFeature instanceof EAttribute && !structuralFeature.isMany()
				&& type.isAssignableFrom(structuralFeature.getEType().getInstanceClass())) {
			return structuralFeature;
		}
		return NO_ATTRIBUTE;
	}

	protected SimpleAttributeResolver(final Class<T> type, final String attributeName) {
		this.type = type;
		this.attributeName = attributeName;
		this.discardingAdapter = new DiscardingAdapter(this);
		valueCache = new SimpleCache<EObject, T>(new Function<EObject, T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T apply(EObject param) {
				final EStructuralFeature feature = attributeOf(param.eClass());
				if (feature != null) {
					param.eAdapters().add(discardingAdapter);
				}
				return feature != null ? ((T) param.eGet(feature)) : null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public T getValue(final K object) {
		if (object == null) {
			return null;
		}
		final EAttribute attribute = attributeOf(object.eClass());
		if (attribute == null) {
			return null;
		}
		// reading a stored value is cheaper than the value cache, which locks globally for every new object
		if (!attribute.isDerived() && !attribute.isVolatile()) {
			return (T) object.eGet(attribute);
		}
		return valueCache.get(object);
	}

	public Iterable<K> getMatches(final Iterable<K> candidates, final T value) {
		return Iterables.filter(candidates, new Predicate<K>() {
			@Override
			public boolean apply(K param) {
				final T candidateValue = getValue(param);
				return value.equals(candidateValue);
			}
		});
	}

	private static class DiscardingAdapter implements Adapter {

		private SimpleAttributeResolver<?, ?> resolver;

		private DiscardingAdapter(SimpleAttributeResolver<?, ?> resolver) {
			this.resolver = resolver;
		}
		
		@Override
		public Notifier getTarget() {
			return null;
		}

		@Override
		public boolean isAdapterForType(final Object type) {
			return type instanceof DiscardingAdapter;
		}

		@Override
		public void notifyChanged(final Notification notification) {
			if (!notification.isTouch() && Notification.SET == notification.getEventType()) {
				final Object feature = notification.getFeature();
				if (feature != null) {
					if (resolver.attributeName.equals(((ENamedElement) feature).getName())) {
						resolver.valueCache.discard((EObject) notification.getNotifier());
						((EObject) notification.getNotifier()).eAdapters().remove(this);
					}
				}

			}
		}

		@Override
		public void setTarget(final Notifier newTarget) {
			// nothing to do
		}
	}

	@Override
	public T apply(final K from) {
		return getValue(from);
	}

}
