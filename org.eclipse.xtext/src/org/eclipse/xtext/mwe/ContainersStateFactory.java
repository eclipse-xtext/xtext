/*******************************************************************************
 * Copyright (c) 2010, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.mwe;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.containers.IAllContainersState;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersState;

public class ContainersStateFactory {
	/** Instead use {@link #getContainersState(List, Map)}. */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public IAllContainersState getContainersState(List<String> paths, com.google.common.collect.Multimap<String, URI> uris) {
		return getContainersState(paths, com.google.common.collect.Multimaps.asMap(uris));
	}

	public IAllContainersState getContainersState(List<String> paths, Map<String, ? extends Collection<URI>> uris) {
		ResourceSetBasedAllContainersState containersState = new ResourceSetBasedAllContainersState();
		containersState.configure(paths, uris);
		return containersState;
	}
}