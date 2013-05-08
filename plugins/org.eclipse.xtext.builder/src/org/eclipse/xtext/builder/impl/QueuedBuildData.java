/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.builder.impl;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.impl.ChangedResourceDescriptionDelta;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.util.Pair;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Singleton
public class QueuedBuildData {

	private Collection<IResourceDescription.Delta> deltas;
	private Collection<UnconfirmedResourceDescriptionDelta> unconfirmedDeltas;
	private LinkedList<URI> uris;
	private Map<String, LinkedList<URI>> projectNameToChangedResource;

	public QueuedBuildData() {
		deltas = Lists.newArrayList();
		unconfirmedDeltas = Lists.newArrayList();
		uris = Lists.newLinkedList();
		projectNameToChangedResource = Maps.newHashMap();
	}

	public void confirmChanges(IProject project, Set<QualifiedName> names) {
		Iterator<UnconfirmedResourceDescriptionDelta> iterator = unconfirmedDeltas.iterator();
		while (iterator.hasNext()) {
			UnconfirmedResourceDescriptionDelta unconfirmed = iterator.next();
			if (unconfirmed.getProject().equals(project)) {
				iterator.remove();
				if (namesIntersect(unconfirmed.getNew(), names) || namesIntersect(unconfirmed.getOld(), names)) {
					deltas.add(new ChangedResourceDescriptionDelta(unconfirmed.getOld(), unconfirmed.getNew()));
				}
			}
		}
	}

	protected boolean namesIntersect(IResourceDescription desc, Set<QualifiedName> names) {
		if (desc != null)
			for (IEObjectDescription name : desc.getExportedObjects())
				if (names.contains(name.getQualifiedName()))
					return true;
		return false;
	}

	public boolean hasUnconfirmedChanges() {
		return !unconfirmedDeltas.isEmpty();
	}

	public synchronized void queueChanges(Collection<IResourceDescription.Delta> deltas) {
		if (deltas != null && !deltas.isEmpty()) {
			for (IResourceDescription.Delta delta : deltas)
				if (delta instanceof UnconfirmedResourceDescriptionDelta)
					this.unconfirmedDeltas.add((UnconfirmedResourceDescriptionDelta) delta);
				else
					this.deltas.add(delta);
		}
	}

	public synchronized void queueURIs(Collection<URI> uris) {
		if (uris != null && !uris.isEmpty()) {
			for (URI uri : uris) {
				queueURI(uri);
			}
		}
	}

	@Inject
	private IStorage2UriMapper mapper;

	public void queueURI(URI uri) {
		Iterable<Pair<IStorage, IProject>> iterable = mapper.getStorages(uri);
		boolean associatedWithProject = false;
		for (Pair<IStorage, IProject> pair : iterable) {
			IProject project = pair.getSecond();
			if (XtextProjectHelper.hasNature(project)) {
				String projectName = project.getName();
				LinkedList<URI> list = projectNameToChangedResource.get(projectName);
				if (list == null) {
					list = Lists.newLinkedList();
					projectNameToChangedResource.put(projectName, list);
				}
				list.add(uri);
				associatedWithProject = true;
			}
		}
		if (!associatedWithProject) {
			this.uris.add(uri);
		}
	}

	public Collection<IResourceDescription.Delta> getAndRemovePendingDeltas() {
		Collection<IResourceDescription.Delta> result = deltas;
		deltas = Lists.newArrayList();
		return result;
	}
	
	public boolean isEmpty(String projectName) {
		return deltas.isEmpty() && getQueue(projectName).isEmpty();
	}

	public Queue<URI> getQueue(String projectName) {
		final LinkedList<URI> list = projectNameToChangedResource.get(projectName);
		if (list == null)
			return uris;
		return new AbstractQueue<URI>() {

			public boolean offer(URI o) {
				return list.offer(o);
			}

			public URI poll() {
				if (uris.isEmpty())
					return list.poll();
				return uris.poll();
			}

			public URI peek() {
				if (uris.isEmpty())
					return list.peek();
				return uris.peek();
			}

			@Override
			public Iterator<URI> iterator() {
				return Iterators.concat(uris.iterator(), list.iterator());
			}

			@Override
			public int size() {
				return uris.size() + list.size();
			}
		};
	}

	public Iterable<URI> getAllRemainingURIs() {
		return Iterables.concat(uris, Iterables.concat(projectNameToChangedResource.values()));
	}

	protected IStorage2UriMapper getMapper() {
		return mapper;
	}

	protected Collection<IResourceDescription.Delta> getDeltas() {
		return deltas;
	}

	protected Map<String, LinkedList<URI>> getProjectNameToChangedResource() {
		return projectNameToChangedResource;
	}

	protected LinkedList<URI> getUris() {
		return uris;
	}
}
