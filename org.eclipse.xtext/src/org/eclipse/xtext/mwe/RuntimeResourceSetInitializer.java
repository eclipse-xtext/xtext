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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.DelegatingIAllContainerAdapter;
import org.eclipse.xtext.resource.containers.IAllContainersState;

import com.google.common.collect.Multimap;
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
		List<String> paths = new ArrayList<>();
		String classPath = System.getProperty("java.class.path");
		String separator = System.getProperty("path.separator");
		String[] strings = classPath.split(separator);
		Collections.addAll(paths, strings);
		return paths;
	}

	protected Multimap<String, URI> getPathToUriMap(List<String> pathes) {
		return getPathToUriMap(pathes, null);
	}

	protected Multimap<String, URI> getPathToUriMap(List<String> pathes, final UriFilter filter) {
		return traverser.resolvePathes(pathes, input -> {
			boolean result = filter == null || filter.matches(input);
			return result && registry.getResourceServiceProvider(input) != null;
		});
	}

	public ResourceSet getInitializedResourceSet(List<String> pathes) {
		return getInitializedResourceSet(pathes, null);
	}

	public ResourceSet getInitializedResourceSet(List<String> pathes, UriFilter filter) {
		ResourceSet resourceSet = resourceSetProvider.get();
		Multimap<String, URI> pathToUriMap = getPathToUriMap(pathes, filter);
		IAllContainersState containersState = factory.getContainersState(pathes, pathToUriMap);
		resourceSet.eAdapters().add(new DelegatingIAllContainerAdapter(containersState));
		for (URI uri : pathToUriMap.values()) {
			resourceSet.createResource(uri);
		}
		return resourceSet;
	}

	public IResourceDescriptions getDescriptions(ResourceSet resourceSet) {
		return provider.get(resourceSet);
	}

}
