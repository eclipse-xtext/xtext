/*******************************************************************************
 * Copyright (c) 2010, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.mwe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.DelegatingIAllContainerAdapter;
import org.eclipse.xtext.resource.containers.IAllContainersState;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class RuntimeResourceSetInitializer {

	@Inject
	private Provider<ResourceSet> resourceSetProvider;
	@Inject
	private ResourceDescriptionsProvider provider;
	@Inject
	private ContainersStateFactory factory;
	@Inject
	private PathTraverser traverser;
	@Inject
	private IResourceServiceProvider.Registry registry;

	public List<String> getClassPathEntries() {
		List<String> pathes = new ArrayList<>();
		String classPath = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String[] strings = classPath.split(separator);
		Collections.addAll(pathes, strings);
		return pathes;
	}

	protected Map<String, Set<URI>> getPathToUriMap(List<String> pathes) {
		return getPathToUriMap(pathes, null);
	}

	protected Map<String, Set<URI>> getPathToUriMap(List<String> pathes, UriFilter filter) {
		return traverser.resolvePathes(pathes, (Predicate<URI>) input -> {
			boolean result = true;
			if (filter != null) {
				result = filter.matches(input);
			}
			if (result) {
				result = registry.getResourceServiceProvider(input) != null;
			}
			return result;
		});
	}

	public ResourceSet getInitializedResourceSet(List<String> pathes) {
		return getInitializedResourceSet(pathes, null);
	}

	public ResourceSet getInitializedResourceSet(List<String> pathes, UriFilter filter) {
		ResourceSet resourceSet = resourceSetProvider.get();
		Map<String, Set<URI>> pathToUriMap = getPathToUriMap(pathes, filter);
		IAllContainersState containersState = factory.getContainersState(pathes, pathToUriMap);
		resourceSet.eAdapters().add(new DelegatingIAllContainerAdapter(containersState));
		pathToUriMap.values().stream().flatMap(Set::stream).forEach(resourceSet::createResource);
		return resourceSet;
	}

	public IResourceDescriptions getDescriptions(ResourceSet resourceSet) {
		return provider.get(resourceSet);
	}

}
