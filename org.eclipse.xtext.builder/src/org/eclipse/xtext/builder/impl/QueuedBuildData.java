/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.builder.impl;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.shared.contribution.ISharedStateContributionRegistry;
import org.eclipse.xtext.util.Pair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * An extensible implementation that allows third party plugins to contribute deltas for changes which should be
 * processed by the Xtext builder in subsequent runs.
 * 
 * Clients who want to contribute plain {@link org.eclipse.xtext.resource.IResourceDescription.Delta deltas} should use
 * {@link #queueChanges(List)} or {@link #queueChange(Delta)} to announce a delta. If a client wants to announce a
 * special kind of delta, a {@link IQueuedBuildDataContribution} could be necessary that can handle this contribution.
 * 
 * @see IQueuedBuildDataContribution
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Singleton
public class QueuedBuildData {

	/**
	 * A composite structure for {@link IQueuedBuildDataContribution contributions}.
	 * 
	 * @author Sebastian Zarnekow - Initial contribution and API
	 */
	public static class CompositeContribution implements IQueuedBuildDataContribution {

		private final List<? extends IQueuedBuildDataContribution> components;

		public CompositeContribution(List<? extends IQueuedBuildDataContribution> components) {
			this.components = components;
		}

		/**
		 * Resets all known components.
		 */
		@Override
		public void reset() {
			for (int i = 0; i < components.size(); i++) {
				components.get(i).reset();
			}
		}

		/**
		 * Resets all known components.
		 */
		@Override
		public void reset(IProject project) {
			for (int i = 0; i < components.size(); i++) {
				components.get(i).reset(project);
			}
		}

		/**
		 * Ask each component to handle the delta. Stop on the first success.
		 */
		@Override
		public boolean queueChange(Delta delta) {
			for (int i = 0; i < components.size(); i++) {
				IQueuedBuildDataContribution contribution = components.get(i);
				if (contribution.queueChange(delta)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Ask each component whether a rebuild is necessary. Each contribution gets the chance to enhance the given
		 * list of deltas.
		 */
		@Override
		public boolean needsRebuild(IProject project, Collection<Delta> deltas) {
			boolean result = false;
			for (int i = 0; i < components.size(); i++) {
				IQueuedBuildDataContribution contribution = components.get(i);
				if (contribution.needsRebuild(project, deltas)) {
					result = true;
				}
			}
			return result;
		}

		@Override
		public void createCheckpoint() {
			for (int i = 0; i < components.size(); i++) {
				IQueuedBuildDataContribution contribution = components.get(i);
				contribution.createCheckpoint();
			}
		}
		
		@Override
		public void discardCheckpoint() {
			for (int i = 0; i < components.size(); i++) {
				IQueuedBuildDataContribution contribution = components.get(i);
				contribution.discardCheckpoint();
			}
		}
		
		@Override
		public void rollback() {
			for (int i = 0; i < components.size(); i++) {
				IQueuedBuildDataContribution contribution = components.get(i);
				contribution.rollback();
			}
		}
	}

	public static class NullContribution implements IQueuedBuildDataContribution {

		@Override
		public void reset() {
			// nothing to do
		}
		
		@Override
		public void reset(IProject project) {
			// nothing to do
		}

		@Override
		public boolean queueChange(Delta delta) {
			// nothing to do
			return false;
		}

		@Override
		public boolean needsRebuild(IProject project, Collection<Delta> deltas) {
			// nothing to do
			return false;
		}

		@Override
		public void createCheckpoint() {
			// nothing to do			
		}
		
		@Override
		public void discardCheckpoint() {
			// nothing to do			
		}
		
		@Override
		public void rollback() {
			// nothing to do			
		}
	}

	private LinkedHashSetQueue<URI> uris;
	private LinkedHashSetQueue<URI> urisCopy;
	private Collection<IResourceDescription.Delta> deltas;
	private Collection<IResourceDescription.Delta> deltasCopy;
	private Map<String, LinkedHashSetQueue<URI>> projectNameToChangedResource;
	private Map<String, LinkedHashSetQueue<URI>> projectNameToChangedResourceCopy;

	private IQueuedBuildDataContribution contribution = new NullContribution();
	private final IStorage2UriMapper mapper;

	@Inject
	public QueuedBuildData(IStorage2UriMapper mapper) {
		this.mapper = mapper;
		reset();
	}

	@Inject
	private void initializeContributions(ISharedStateContributionRegistry registry) {
		contribution = getContribution(registry.getContributedInstances(IQueuedBuildDataContribution.class));
		reset();
	}

	/**
	 * Public for testing purpose
	 * 
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public QueuedBuildData(IStorage2UriMapper mapper, IQueuedBuildDataContribution contribution) {
		this.mapper = mapper;
		this.contribution = contribution;
		reset();
	}

	private IQueuedBuildDataContribution getContribution(
			ImmutableList<? extends IQueuedBuildDataContribution> contributions) {
		switch (contributions.size()) {
			case 0:
				return new NullContribution();
			case 1:
				return contributions.get(0);
			default:
				return new CompositeContribution(contributions);
		}
	}

	public void reset() {
		uris = new LinkedHashSetQueue<>();
		deltas = Lists.newArrayList();
		projectNameToChangedResource = Maps.newHashMap();
		contribution.reset();
	}
	
	public void reset(IProject project) {
		projectNameToChangedResource.remove(project.getName());
		contribution.reset(project); 
	}

	/**
	 * <p>
	 * Returns <code>true</code> if rebuild is required; otherwise <code>false</code>.
	 * </p>
	 * 
	 * @param project
	 *            the project that may have to be rebuild.
	 * @return <code>true</code> if rebuild is required; otherwise <code>false</code>. Default is <code>false</code>.
	 * @see IQueuedBuildDataContribution#needsRebuild(IProject, Collection)
	 */
	public synchronized boolean needRebuild(IProject project) {
		return contribution.needsRebuild(project, deltas);
	}

	public synchronized void queueChanges(/* @NonNull */ List<IResourceDescription.Delta> deltas) {
		for (int i = 0, size = deltas.size(); i < size; i++) {
			queueChange(deltas.get(i));
		}
	}

	public synchronized void queueChange(/* @NonNull */ IResourceDescription.Delta delta) {
		if (!contribution.queueChange(delta)) {
			deltas.add(delta);
		}
	}

	public synchronized void queueURIs(Collection<URI> uris) {
		if (uris != null && !uris.isEmpty()) {
			for (URI uri : uris) {
				queueURI(uri);
			}
		}
	}

	public void queueURI(URI uri) {
		Iterable<Pair<IStorage, IProject>> iterable = mapper.getStorages(uri);
		boolean associatedWithProject = false;
		for (Pair<IStorage, IProject> pair : iterable) {
			IProject project = pair.getSecond();
			if (XtextProjectHelper.hasNature(project) && XtextProjectHelper.hasBuilder(project)) {
				String projectName = project.getName();
				LinkedHashSetQueue<URI> list = projectNameToChangedResource.get(projectName);
				if (list == null) {
					list = new LinkedHashSetQueue<>();
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

	public synchronized Collection<IResourceDescription.Delta> getAndRemovePendingDeltas() {
		if (deltas.isEmpty()) {
			return Collections.emptyList();
		}
		Collection<IResourceDescription.Delta> result = deltas;
		deltas = Lists.newArrayList();
		return result;
	}

	public boolean isEmpty(String projectName) {
		return deltas.isEmpty() && getQueue(projectName).isEmpty();
	}

	public Queue<URI> getQueue(String projectName) {
		final LinkedHashSetQueue<URI> list = projectNameToChangedResource.get(projectName);
		if (list == null)
			return uris;
		return new AbstractQueue<URI>() {

			@Override
			public boolean offer(URI o) {
				return list.offer(o);
			}

			@Override
			public URI poll() {
				if (uris.isEmpty())
					return list.poll();
				return uris.poll();
			}

			@Override
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

	public void createCheckpoint() {
		deltasCopy = new ArrayList<Delta>(deltas);
		urisCopy = new LinkedHashSetQueue<URI>(uris);
		// Copy the queues themselves, not just the map: the build drains the
		// live queues via getQueue(projectName), so a rollback after a cancel
		// would otherwise restore empty queues and lose the queued URIs.
		projectNameToChangedResourceCopy = new HashMap<String, LinkedHashSetQueue<URI>>();
		for (Map.Entry<String, LinkedHashSetQueue<URI>> entry : projectNameToChangedResource.entrySet()) {
			projectNameToChangedResourceCopy.put(entry.getKey(), new LinkedHashSetQueue<URI>(entry.getValue()));
		}
		contribution.createCheckpoint();
	}

	public void discardCheckpoint() {
		deltasCopy = null;
		urisCopy = null;
		projectNameToChangedResourceCopy = null;
		contribution.discardCheckpoint();
	}

	public void rollback() {
		deltas.clear();
		deltas.addAll(deltasCopy);
		uris.clear();
		uris.addAll(urisCopy);
		projectNameToChangedResource.clear();
		projectNameToChangedResource.putAll(projectNameToChangedResourceCopy);
		contribution.rollback();
	}
}
