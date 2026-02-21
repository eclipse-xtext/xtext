/*******************************************************************************
 * Copyright (c) 2009, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.scoping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.impl.SimpleScope;
import org.eclipse.xtext.util.SimpleAttributeResolver;

import com.google.common.collect.Iterables;

/**
 * This class contains static utility functions to create and work on {@link IScope} and {@link IEObjectDescription}
 *
 * @author Sven Efftinge - Initial contribution and API
 * @author Jan Koehnlein - introduced QualifiedName
 */
public class Scopes {

	public static Iterable<IEObjectDescription> selectCompatible(Iterable<IEObjectDescription> exportedObjects, final EClass clazz) {
		return Iterables.filter(exportedObjects, input -> EcoreUtil2.isAssignableFrom(clazz, input.getEClass()));
	}

	/**
	 * creates a scope using {@link SimpleAttributeResolver#NAME_RESOLVER} to compute the names and sets
	 * {@link IScope#NULLSCOPE} as parent scope
	 */
	public static IScope scopeFor(Iterable<? extends EObject> elements) {
		return scopeFor(elements, IScope.NULLSCOPE);
	}

	/**
	 * creates a scope using {@link SimpleAttributeResolver#NAME_RESOLVER} to compute the names
	 */
	public static IScope scopeFor(Iterable<? extends EObject> elements, IScope outer) {
		return scopeFor(elements, QualifiedName.wrapper((Function<EObject, String>) SimpleAttributeResolver.NAME_RESOLVER), outer);
	}

	/**
	 * creates a scope using the passed function to compute the names and sets the passed scope as the parent scope
	 * @deprecated Instead use {@link #scopeFor(Iterable, Function, IScope)}
	 */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public static <T extends EObject> IScope scopeFor(Iterable<? extends T> elements,
			com.google.common.base.Function<T, QualifiedName> nameComputation, IScope outer) {
		return scopeFor(elements, (java.util.function.Function<T, QualifiedName>) nameComputation, outer);
	}

	/**
	 * creates a scope using the passed function to compute the names and sets the passed scope as the parent scope
	 */
	public static <T extends EObject> IScope scopeFor(Iterable<? extends T> elements,
			Function<T, QualifiedName> nameComputation, IScope outer) {
		return new SimpleScope(outer, scopedElementsFor(elements, nameComputation));
	}

	/**
	 * transforms an {@link Iterable} of {@link EObject}s into an {@link Iterable} of {@link IEObjectDescription}s computing
	 * the {@link EAttribute} 'name' to compute the {@link IEObjectDescription}'s name. If not existent the object is
	 * filtered out.
	 */
	public static Iterable<IEObjectDescription> scopedElementsFor(Iterable<? extends EObject> elements) {
		return scopedElementsFor(elements, QualifiedName.wrapper((Function<EObject, String>) SimpleAttributeResolver.NAME_RESOLVER));
	}

	/**
	 * transforms an {@link Iterable} of {@link EObject}s into an {@link Iterable} of {@link IEObjectDescription}s computing
	 * the name of the elements using the passed {@link Function} If the passed function returns null the object is
	 * filtered out.
	 * @deprecated Instead use {@link #scopedElementsFor(Iterable, Function)}
	 */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public static <T extends EObject> Iterable<IEObjectDescription> scopedElementsFor(Iterable<? extends T> elements,
			com.google.common.base.Function<T, QualifiedName> nameComputation) {
		return scopedElementsFor(elements, (java.util.function.Function<T, QualifiedName>) nameComputation);
	}

	/**
	 * transforms an {@link Iterable} of {@link EObject}s into an {@link Iterable} of {@link IEObjectDescription}s computing
	 * the name of the elements using the passed {@link Function} If the passed function returns null the object is
	 * filtered out.
	 */
	public static <T extends EObject> Iterable<IEObjectDescription> scopedElementsFor(Iterable<? extends T> elements,
			Function<T, QualifiedName> nameComputation) {
		Iterable<IEObjectDescription> transformed = Iterables.transform(elements, from -> {
			final QualifiedName qualifiedName = nameComputation.apply(from);
			if (qualifiedName != null)
				return new EObjectDescription(qualifiedName, from, null);
			return null;
		});
		return Iterables.filter(transformed, Objects::nonNull);
	}

	/**
	 * indexes the IEObject description using the given
	 * @deprecated Instead use {@link #index(Iterable, Function)}
	 */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public static <T> com.google.common.collect.Multimap<T, IEObjectDescription> index(
			Iterable<IEObjectDescription> descriptions,
			com.google.common.base.Function<IEObjectDescription, T> indexer) {
		Map<T, Set<IEObjectDescription>> index = index(descriptions, (Function<IEObjectDescription, T>) indexer);
		com.google.common.collect.Multimap<T, IEObjectDescription> multimap = com.google.common.collect.LinkedHashMultimap.create();
		index.forEach(multimap::putAll);
		return multimap;
	}

	/**
	 * indexes the IEObject description using the given
	 */
	public static <T> Map<T, Set<IEObjectDescription>> index(Iterable<IEObjectDescription> descriptions,
			Function<IEObjectDescription, T> indexer) {
		List<IEObjectDescription> list = new ArrayList<>();
		descriptions.forEach(list::add);
		Map<T, Set<IEObjectDescription>> multimap = new LinkedHashMap<>(list.size() * 4 / 3 + 1);
		for (IEObjectDescription desc : list) {
			multimap.computeIfAbsent(indexer.apply(desc), n -> new LinkedHashSet<>(1)).add(desc);
		}
		return multimap;
	}

	/**
	 * indexes the IEObject description using the given
	 * @deprecated Instead use {@link #indexByName(Iterable)}
	 */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public static com.google.common.collect.Multimap<QualifiedName,IEObjectDescription> index(Iterable<IEObjectDescription> descriptions) {
		return index(descriptions, from -> from.getName().toLowerCase());
	}

	/**
	 * indexes the IEObject description using the given
	 */
	public static Map<QualifiedName, Set<IEObjectDescription>> indexByName(Iterable<IEObjectDescription> descriptions) {
		return index(descriptions, (Function<IEObjectDescription, QualifiedName>) from -> from.getName().toLowerCase());
	}

	public static Iterable<IEObjectDescription> filterDuplicates(Iterable<IEObjectDescription> filtered) {
		Map<QualifiedName, IEObjectDescription> result = new LinkedHashMap<>();
		for (IEObjectDescription e : filtered) {
			QualifiedName qualifiedName = e.getName();
			if (result.containsKey(qualifiedName)) {
				result.put(qualifiedName, null);
			} else {
				result.put(qualifiedName, e);
			}
		}
		return Iterables.filter(result.values(), Objects::nonNull);
	}

}
