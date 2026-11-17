/*******************************************************************************
 * Copyright (c) 2015, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.CompilerPhases;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionDelta;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @since 2.9
 */
public class Indexer {

	public static class IndexResult {
		private final List<IResourceDescription.Delta> resourceDeltas;

		private final ResourceDescriptionsData newIndex;

		public IndexResult(List<IResourceDescription.Delta> resourceDeltas, ResourceDescriptionsData newIndex) {
			this.resourceDeltas = resourceDeltas;
			this.newIndex = newIndex;
		}

		public List<IResourceDescription.Delta> getResourceDeltas() {
			return resourceDeltas;
		}

		public ResourceDescriptionsData getNewIndex() {
			return newIndex;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = prime * result + (resourceDeltas == null ? 0 : resourceDeltas.hashCode());
			return prime * result + (newIndex == null ? 0 : newIndex.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Indexer.IndexResult other = (Indexer.IndexResult) obj;
			if (resourceDeltas == null) {
				if (other.resourceDeltas != null) {
					return false;
				}
			} else if (!resourceDeltas.equals(other.resourceDeltas)) {
				return false;
			}
			if (newIndex == null) {
				if (other.newIndex != null) {
					return false;
				}
			} else if (!newIndex.equals(other.newIndex)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("resourceDeltas", resourceDeltas);
			b.add("newIndex", newIndex);
			return b.toString();
		}

	}

	@Inject
	private CompilerPhases compilerPhases;

	@Inject
	private OperationCanceledManager operationCanceledManager;

	/**
	 * Compute an initial index. Explicit calls to {@link #computeAndIndexAffected(Collection, IndexResult, BuildRequest, BuildContext)}
	 * are needed to complete indexing.
	 * 
	 * @since 2.33
	 */
	public Indexer.IndexResult initialIndex(final BuildRequest request, final BuildContext context) {
		ResourceDescriptionsData previousIndex = context.getOldState().getResourceDescriptions();
		ResourceDescriptionsData newIndex = request.getState().getResourceDescriptions();
		List<IResourceDescription.Delta> deltas = new ArrayList<>();
		deltas.addAll(getDeltasForDeletedResources(request, previousIndex, context));
		deltas.addAll(getDeltasForChangedResources(request.getDirtyFiles(), previousIndex, context));
		for (IResourceDescription.Delta delta : deltas) {
			newIndex.register(delta);
		}
		return new Indexer.IndexResult(deltas, newIndex);
	}

	/**
	 * Compute an updated index based on new deltas and a previously calculated {@link #initialIndex(BuildRequest, BuildContext)}.
	 *  
	 * @since 2.33
	 */
	public List<IResourceDescription.Delta> computeAndIndexAffected(
			final Collection<IResourceDescription.Delta> newDeltas, final Indexer.IndexResult result,
			final BuildRequest request, final BuildContext context) {
		List<IResourceDescription.Delta> deltas = result.getResourceDeltas();
		Set<IResourceDescription.Delta> allDeltas = new HashSet<>(deltas);
		allDeltas.addAll(request.getExternalDeltas());
		Set<URI> deltaSet = FluentIterable.from(deltas).transform(Delta::getUri).toSet();
		ResourceDescriptionsData previousIndex = context.getOldState().getResourceDescriptions();
		List<URI> allAffected = FluentIterable.from(previousIndex.getAllResourceDescriptions())
				.transform(IResourceDescription::getURI).filter(it -> !deltaSet.contains(it)).filter(it -> {
					IResourceServiceProvider resourceServiceProvider = context.getResourceServiceProvider(it);
					if (resourceServiceProvider != null) {
						IResourceDescription.Manager manager = resourceServiceProvider.getResourceDescriptionManager();
						IResourceDescription resourceDescription = previousIndex.getResourceDescription(it);
						return isAffected(resourceDescription, manager, newDeltas, allDeltas,
								result.getNewIndex());
					} else {
						IResourceDescription.Delta delta = getDeltaForDeletedResource(it, previousIndex);
						if (delta != null) {
							deltas.add(delta);
						}
						return false;
					}
				}).toList();
		List<IResourceDescription.Delta> affectedDeltas = getDeltasForChangedResources(allAffected, previousIndex,
				context);
		deltas.addAll(affectedDeltas);
		return affectedDeltas;
	}

	/**
	 * Compute an updated index.
	 */
	public Indexer.IndexResult computeAndIndexAffected(BuildRequest request, BuildContext context) {
	    Indexer.IndexResult result = initialIndex(request, context);
	    computeAndIndexAffected(result.getResourceDeltas(), result, request, context);
	    return result;
	}

	/**
	 * Process the deleted resources.
	 */
	protected List<IResourceDescription.Delta> getDeltasForDeletedResources(BuildRequest request,
			ResourceDescriptionsData oldIndex, BuildContext context) {
		List<IResourceDescription.Delta> deltas = new ArrayList<>();
		if (oldIndex != null) {
			for (URI deleted : request.getDeletedFiles()) {
				IResourceServiceProvider resourceServiceProvider = context.getResourceServiceProvider(deleted);
				if (resourceServiceProvider != null) {
					operationCanceledManager.checkCanceled(context.getCancelIndicator());
					IResourceDescription.Delta delta = getDeltaForDeletedResource(deleted, oldIndex, resourceServiceProvider.getResourceDescriptionManager());
					if (delta != null) {
						deltas.add(delta);
					}
				}
			}
		}
		return deltas;
	}

	/**
	 * Gets a delta for a resource that shall be deleted.
	 *
	 * @since 2.26
	 *
	 */
	protected IResourceDescription.Delta getDeltaForDeletedResource(URI uri, ResourceDescriptionsData oldIndex) {
		return getDeltaForDeletedResource(uri, oldIndex, null);
	}
	
	/**
	 * Gets a delta for a resource that shall be deleted using the resource description manager if provided.
	 *
	 * @since 2.33
	 *
	 */
	protected IResourceDescription.Delta getDeltaForDeletedResource(URI uri, ResourceDescriptionsData oldIndex, IResourceDescription.Manager manager) {
		IResourceDescription oldDescription = oldIndex.getResourceDescription(uri);
		if (oldDescription != null) {
			if (manager != null) {
				return manager.createDelta(oldDescription, null);
			}
			return new DefaultResourceDescriptionDelta(oldDescription, null);
		}
		return null;
	}

	/**
	 * Process the changed resources.
	 */
	protected List<IResourceDescription.Delta> getDeltasForChangedResources(Iterable<URI> affectedUris,
			ResourceDescriptionsData oldIndex, BuildContext context) {
		try {
			compilerPhases.setIndexing(context.getResourceSet(), true);
			// Since context.executeClustered, we can avoid a copy due of the list due to the impl detail of
			// IterableExtensions.toList
			return IterableExtensions
					.toList(context.executeClustered(affectedUris, it -> addToIndex(it, true, oldIndex, context)));
		} finally {
			compilerPhases.setIndexing(context.getResourceSet(), false);
		}
	}

	/**
	 * Return a new resolved resource description from a {@link IResourceDescription}.
	 *
	 * @param description
	 *            the resource description, must not be {@code null}
	 * @return the new resolved resource description, never {@code null}
	 * @since 2.26
	 */
	protected IResourceDescription getResolvedResourceDescription(IResourceDescription description) {
		return new ResolvedResourceDescription(description);
	}

	/**
	 * Index the given resource.
	 *
	 * @param isPreIndexing
	 *            can be evaluated to produce different index entries depending on the phase
	 */
	protected IResourceDescription.Delta addToIndex(Resource resource, boolean isPreIndexing,
			ResourceDescriptionsData oldIndex, BuildContext context) {
		operationCanceledManager.checkCanceled(context.getCancelIndicator());
		URI uri = resource.getURI();
		IResourceServiceProvider serviceProvider = context.getResourceServiceProvider(uri);
		IResourceDescription.Manager manager = serviceProvider.getResourceDescriptionManager();
		IResourceDescription newDescription = manager.getResourceDescription(resource);
		IResourceDescription toBeAdded = getResolvedResourceDescription(newDescription);
		return manager.createDelta(oldIndex != null ? oldIndex.getResourceDescription(uri) : null, toBeAdded);
	}

	/**
	 * Return true, if the given resource must be processed due to the given changes.
	 */
	protected boolean isAffected(IResourceDescription affectionCandidate, IResourceDescription.Manager manager,
			Collection<IResourceDescription.Delta> newDeltas, Collection<IResourceDescription.Delta> allDeltas,
			IResourceDescriptions resourceDescriptions) {
		if (manager instanceof IResourceDescription.Manager.AllChangeAware) {
			return ((IResourceDescription.Manager.AllChangeAware) manager).isAffectedByAny(allDeltas,
					affectionCandidate, resourceDescriptions);
		} else {
			if (newDeltas.isEmpty()) {
				return false;
			} else {
				return manager.isAffected(newDeltas, affectionCandidate, resourceDescriptions);
			}
		}
	}
}
