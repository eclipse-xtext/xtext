/*******************************************************************************
 * Copyright (c) 2010, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;

/**
 * This implementation {@link IAllContainersState} associates resource (e.g. their URIs) to containers. It assumes that
 * all URIs and their containers are known when {@link #configure(List, Map)} is called.
 * 
 * @see FlatResourceSetBasedAllContainersState
 * 
 * @author Sven Efftinge - Initial contribution and API
 */
public class ResourceSetBasedAllContainersState implements IAllContainersState {

	private Map<String, Set<URI>> container2URIs;
	private Map<URI, Set<String>> uri2container;
	private List<String> containers;

	/** @deprecated Instead use {@link #configure(List, Map)} */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public void configure(List<String> containers, com.google.common.collect.Multimap<String, URI> container2Uris) {
		configure(containers, com.google.common.collect.Multimaps.asMap(container2Uris));
	}

	public void configure(List<String> containers, Map<String, ? extends Collection<URI>> container2Uris) {
		this.containers = containers;
		this.container2URIs = container2Uris.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> Set.copyOf(e.getValue())));
		this.uri2container = new HashMap<>();
		container2Uris.forEach((container, uris) -> {
			for (URI uri : uris) {
				uri2container.computeIfAbsent(uri, u -> new HashSet<>()).add(container);
			}
		});
	}

	@Override
	public List<String> getVisibleContainerHandles(String handle) {
		return containers;
	}

	@Override
	public Collection<URI> getContainedURIs(String containerHandle) {
		return container2URIs.getOrDefault(containerHandle, Set.of());
	}
	
	@Override
	public boolean isEmpty(String containerHandle) {
		return getContainedURIs(containerHandle).isEmpty();
	}

	@Override
	public String getContainerHandle(URI uri) {
		Set<String> set = uri2container.get(uri);
		if (set!=null && !set.isEmpty())
			return set.iterator().next();
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(getClass().getSimpleName());
		Set<String> invisibleContainers = new HashSet<>(container2URIs.keySet());
		invisibleContainers.removeAll(containers);
		if (!invisibleContainers.isEmpty()) {
			result.append("\n  WARNING: invisible containers: ");
			result.append(String.join(", ", invisibleContainers));
		}
		for (String container : containers) {
			Collection<URI> uris = getContainedURIs(container);
			result.append("\n  container ");
			result.append(container);
			result.append(" = ");
			if (uris.isEmpty())
				result.append("(empty)");
			else {
				result.append("{\n    ");
				result.append(uris.stream().map(URI::toString).collect(Collectors.joining("\n    ")));
				result.append("\n  }");
			}
		}
		result.append("\n]");
		return result.toString();
	}

}
