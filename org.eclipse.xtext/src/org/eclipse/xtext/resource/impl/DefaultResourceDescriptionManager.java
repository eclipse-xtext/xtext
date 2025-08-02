/*******************************************************************************
 * Copyright (c) 2009, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.DescriptionUtils;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IDefaultResourceDescriptionStrategy;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.util.IResourceScopeCache;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Default implementation of the {@link org.eclipse.xtext.resource.IResourceDescription.Manager}. Customize by binding another
 * {@link IDefaultResourceDescriptionStrategy}.
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Sven Efftinge
 * @author Jan Koehnlein
 */
@Singleton
public class DefaultResourceDescriptionManager implements IResourceDescription.Manager {

	@Inject
	private IDefaultResourceDescriptionStrategy strategy;
	
	@Inject
	private IContainer.Manager containerManager;
	
	@Inject
	private IResourceScopeCache cache = IResourceScopeCache.NullImpl.INSTANCE;
	
	@Inject
	private DescriptionUtils descriptionUtils;
	
	@Inject(optional = true)
	private ImmutableList<IsAffectedExtension> isAffectedExtensions = ImmutableList.of(); 
	
	private static final String CACHE_KEY = DefaultResourceDescriptionManager.class.getName() + "#getResourceDescription";
	
	@Override
	public IResourceDescription getResourceDescription(final Resource resource) {
		return cache.get(CACHE_KEY, resource, new Provider<IResourceDescription>() {
			@Override
			public IResourceDescription get() {
				return internalGetResourceDescription(resource, strategy);
			}
		});
	}
	
	@Override
	public Delta createDelta(IResourceDescription oldDescription, IResourceDescription newDescription) {
		return new DefaultResourceDescriptionDelta(oldDescription, newDescription);
	}

	protected IResourceDescription internalGetResourceDescription(Resource resource, IDefaultResourceDescriptionStrategy strategy) {
		return new DefaultResourceDescription(resource, strategy, cache);
	}
	
	public IContainer.Manager getContainerManager() {
		return containerManager;
	}
	
	public void setContainerManager(IContainer.Manager containerManager) {
		this.containerManager = containerManager;
	}
	
	public void setCache(IResourceScopeCache cache) {
		this.cache = cache;
	}
	
	public IResourceScopeCache getCache() {
		return cache;
	}
	
	@Override
	public boolean isAffected(Delta delta, IResourceDescription candidate) throws IllegalArgumentException {
		if (!hasChanges(delta, candidate))
			return false;
		Set<QualifiedName> names = Sets.newHashSet();
		addExportedNames(names,delta.getOld());
		addExportedNames(names,delta.getNew());
		Collection<QualifiedName> importedNames = getImportedNames(candidate);
		if (names.size() > importedNames.size()) {
			return !Collections.disjoint(names, importedNames);
		} else {
			return !Collections.disjoint(importedNames, names);
		}
	}

	protected Collection<QualifiedName> getImportedNames(IResourceDescription candidate) {
		return Sets.newHashSet(candidate.getImportedNames());
	}

	protected void addExportedNames(Set<QualifiedName> names, IResourceDescription resourceDescriptor) {
		if (resourceDescriptor==null)
			return;
		Iterable<IEObjectDescription> iterable = resourceDescriptor.getExportedObjects();
		for (IEObjectDescription ieObjectDescription : iterable) {
			names.add(ieObjectDescription.getName().toLowerCase());
		}
	}
	
	@Override
	public boolean isAffected(Collection<Delta> deltas, IResourceDescription candidate, IResourceDescriptions context) {
		Set<URI> outgoingReferences = descriptionUtils.collectOutgoingReferences(candidate);
		if (!outgoingReferences.isEmpty()) {
			for (IResourceDescription.Delta delta : deltas)
				if (hasChanges(delta, candidate) && outgoingReferences.contains(delta.getUri()))
					return true;
		}
		// this is a tradeoff - we could either check whether a given delta uri is contained
		// in a reachable container and check for intersecting names afterwards, or we can do
		// the other way round
		// unfortunately there is no way to decide reliably which algorithm scales better
		// note that this method is called for each description so we have something like a
		// number of deltas x number of resources which is not really nice
		List<IContainer> containers = null;
		Collection<QualifiedName> importedNames = getImportedNames(candidate);
		if (!importedNames.isEmpty()) {
			for (IResourceDescription.Delta delta : deltas) {
				if (hasChanges(delta, candidate)) {
					// not a java resource - delta's resource should be contained in a visible container
					// as long as we did not delete the resource
					URI uri = delta.getUri();
					if ((uri.isPlatform() || uri.isArchive()) && delta.getNew() != null) {
						if (containers == null)
							containers = containerManager.getVisibleContainers(candidate, context);
						boolean descriptionIsContained = false;
						for (int i = 0; i < containers.size() && !descriptionIsContained; i++) {
							descriptionIsContained = containers.get(i).hasResourceDescription(uri);
						}
						if (!descriptionIsContained) {
							continue;
						}
					}
					if (isAffected(importedNames, delta.getNew()) || isAffected(importedNames, delta.getOld())) {
						return true;
					}
				}
			}
		}
		return isAffectedByExtensions(deltas, candidate, context);
	}

	/**
	 * Query all registered extensions.
	 * 
	 * @since 2.22
	 */
	@Beta
	protected boolean isAffectedByExtensions(Collection<Delta> deltas, IResourceDescription candidate,
			IResourceDescriptions context) {
		for (int i = 0; i < isAffectedExtensions.size(); i++) {
			if (isAffectedExtensions.get(i).isAffected(deltas, candidate, context)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given delta is considered to have changed from the candidate's perspective. By default this will just call
	 * {@link org.eclipse.xtext.resource.IResourceDescription.Delta#haveEObjectDescriptionsChanged() Delta#haveEObjectDescriptionsChanged()}. 
	 * But in some cases even "internal" changes (that are not visible in the EObjectDescriptions) could be interesting. 
	 * @since 2.7
	 */
	protected boolean hasChanges(IResourceDescription.Delta delta, IResourceDescription candidate) {
		return delta.haveEObjectDescriptionsChanged();
	}

	protected boolean isAffected(Collection<QualifiedName> importedNames, IResourceDescription description) {
		if (description != null) {
			for (IEObjectDescription desc : description.getExportedObjects())
				if (importedNames.contains(desc.getName().toLowerCase()))
					return true;
		}
		return false;
	}
	
	public DescriptionUtils getDescriptionUtils() {
		return descriptionUtils;
	}
	
	public void setDescriptionUtils(DescriptionUtils descriptionUtils) {
		this.descriptionUtils = descriptionUtils;
	}
	
	public void setStrategy(IDefaultResourceDescriptionStrategy strategy) {
		this.strategy = strategy;
	}
}
