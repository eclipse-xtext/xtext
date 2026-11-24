/*******************************************************************************
 * Copyright (c) 2026 Eclipse Xtext and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.persistence;

import java.io.ObjectInputFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Restricts the classes that may be deserialized from Xtext resource storage.
 * <p>
 * Resource storage is a generated artifact and may come from a JAR, a dependency or a build cache.
 * It is loaded with Java serialization, so without a filter a crafted storage file could instantiate
 * arbitrary classes. This filter allows only the types used by the default storage format and limits
 * the object graph size.
 * <p>
 * This filter does not bound allocations performed directly inside
 * {@code Externalizable.readExternal} or collection deserialization. Those count/size limits are a
 * separate hardening step.
 * <p>
 * Subclasses of the three {@code Serializable*} storage types are allowed so that
 * {@link SerializableEObjectDescriptionProvider} implementations can return specialized
 * descriptions. Collection types remain exact matches.
 * <p>
 * The default limits can be tuned with the system properties
 * {@value #MAX_DEPTH_PROPERTY}, {@value #MAX_REFERENCES_PROPERTY} and
 * {@value #MAX_ARRAY_LENGTH_PROPERTY}. The properties are read when the class is initialized.
 *
 * @since 2.45
 */
public class ResourceStorageInputFilter implements ObjectInputFilter {

	public static final String MAX_DEPTH_PROPERTY = "org.eclipse.xtext.resource.storage.maxDepth";

	public static final String MAX_REFERENCES_PROPERTY = "org.eclipse.xtext.resource.storage.maxReferences";

	public static final String MAX_ARRAY_LENGTH_PROPERTY = "org.eclipse.xtext.resource.storage.maxArrayLength";

	public static final int DEFAULT_MAX_DEPTH = 100;

	public static final int DEFAULT_MAX_REFERENCES = 5_000_000;

	/**
	 * Maximum array length. For {@link java.util.HashMap} and {@link java.util.LinkedHashMap}, the
	 * filter sees the internal table array, whose length is the map capacity and may be larger than
	 * the number of entries. This limit therefore also bounds map capacity.
	 */
	public static final int DEFAULT_MAX_ARRAY_LENGTH = 1 << 21;

	protected static final Set<Class<?>> DEFAULT_ALLOWED_TYPES = Collections.unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList(
					SerializableResourceDescription.class,
					SerializableEObjectDescription.class,
					SerializableReferenceDescription.class)));

	protected static final Set<String> DEFAULT_ALLOWED_CLASSES = Collections.unmodifiableSet(
			new LinkedHashSet<>(Arrays.asList(
					"java.lang.String",
					"java.util.ArrayList",
					"java.util.HashMap",
					"java.util.LinkedHashMap",
					"java.util.HashSet",
					"java.util.LinkedHashSet")));

	public static final ResourceStorageInputFilter DEFAULT = new ResourceStorageInputFilter(Collections.emptySet(),
			getIntProperty(MAX_DEPTH_PROPERTY, DEFAULT_MAX_DEPTH),
			getIntProperty(MAX_REFERENCES_PROPERTY, DEFAULT_MAX_REFERENCES),
			getIntProperty(MAX_ARRAY_LENGTH_PROPERTY, DEFAULT_MAX_ARRAY_LENGTH));

	private final Set<String> allowedClasses;

	private final int maxDepth;

	private final int maxReferences;

	private final int maxArrayLength;

	public ResourceStorageInputFilter(Collection<String> additionalClasses) {
		this(additionalClasses,
				getIntProperty(MAX_DEPTH_PROPERTY, DEFAULT_MAX_DEPTH),
				getIntProperty(MAX_REFERENCES_PROPERTY, DEFAULT_MAX_REFERENCES),
				getIntProperty(MAX_ARRAY_LENGTH_PROPERTY, DEFAULT_MAX_ARRAY_LENGTH));
	}

	/**
	 * @param additionalClasses additional exact class names, must not be {@code null}
	 * @param maxDepth maximum serialization depth
	 * @param maxReferences maximum number of object references
	 * @param maxArrayLength maximum array length
	 */
	public ResourceStorageInputFilter(Collection<String> additionalClasses, int maxDepth, int maxReferences,
			int maxArrayLength) {
		Objects.requireNonNull(additionalClasses, "additionalClasses");
		Set<String> classes = new LinkedHashSet<>(DEFAULT_ALLOWED_CLASSES);
		classes.addAll(additionalClasses);
		this.allowedClasses = Collections.unmodifiableSet(classes);
		this.maxDepth = maxDepth;
		this.maxReferences = maxReferences;
		this.maxArrayLength = maxArrayLength;
	}

	@Override
	public Status checkInput(FilterInfo info) {
		if (info.depth() > maxDepth || info.references() > maxReferences) {
			return Status.REJECTED;
		}
		Class<?> serialClass = info.serialClass();
		if (serialClass == null) {
			return Status.UNDECIDED;
		}
		if (serialClass.isArray()) {
			if (info.arrayLength() > maxArrayLength) {
				return Status.REJECTED;
			}
			return isAllowedArray(serialClass) ? Status.ALLOWED : Status.REJECTED;
		}
		if (isAllowedType(serialClass)) {
			return Status.ALLOWED;
		}
		return allowedClasses.contains(serialClass.getName()) ? Status.ALLOWED : Status.REJECTED;
	}

	protected boolean isAllowedType(Class<?> type) {
		for (Class<?> allowedType : DEFAULT_ALLOWED_TYPES) {
			if (allowedType.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isAllowedArray(Class<?> arrayClass) {
		Class<?> componentType = arrayClass.getComponentType();
		if (componentType == Object.class || componentType == String.class
				|| Map.Entry.class.isAssignableFrom(componentType)) {
			return true;
		}
		return componentType != null && (isAllowedType(componentType) || allowedClasses.contains(componentType.getName()));
	}

	private static int getIntProperty(String name, int defaultValue) {
		String value = System.getProperty(name);
		if (value == null) {
			return defaultValue;
		}
		try {
			int parsed = Integer.parseInt(value);
			return parsed > 0 ? parsed : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * Composes a process-global filter with a storage-specific filter. Both filters are consulted;
	 * a rejection from either one wins. This prevents a stream filter from weakening a stricter
	 * process-global serialization policy.
	 *
	 * @since 2.45
	 */
	public static ObjectInputFilter compose(ObjectInputFilter globalFilter, ObjectInputFilter storageFilter) {
		if (globalFilter == null) {
			return storageFilter;
		}
		if (storageFilter == null) {
			return globalFilter;
		}
		return info -> {
			Status globalStatus = globalFilter.checkInput(info);
			if (globalStatus == Status.REJECTED) {
				return Status.REJECTED;
			}
			Status storageStatus = storageFilter.checkInput(info);
			if (storageStatus == Status.REJECTED) {
				return Status.REJECTED;
			}
			return storageStatus == Status.UNDECIDED ? globalStatus : storageStatus;
		};
	}

	/**
	 * Composes the process-global filter (if any) with the given storage filter.
	 *
	 * @since 2.45
	 */
	public static ObjectInputFilter composeWithGlobalFilter(ObjectInputFilter storageFilter) {
		return compose(ObjectInputFilter.Config.getSerialFilter(), storageFilter);
	}

}
