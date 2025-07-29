/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.core.resource.XtendResourceDescriptionManager;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Delta;
import org.eclipse.xtext.resource.IResourceDescriptions;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Singleton
public class XtendUIResourceDescriptionManager extends XtendResourceDescriptionManager {

	@Inject
	private IWorkspaceRoot workspaceRoot;
	
	@Override
	public boolean isAffected(Collection<Delta> deltas, IResourceDescription candidate, IResourceDescriptions context) {
		// skip collecting the outgoing references since we don't index references anyway
//		Set<URI> outgoingReferences = getDescriptionUtils().collectOutgoingReferences(candidate);
//		if (!outgoingReferences.isEmpty()) {
//			for (IResourceDescription.Delta delta : deltas)
//				if (hasChanges(delta, candidate) && outgoingReferences.contains(delta.getUri()))
//					return true;
//		}
		
		// this is a tradeoff - we could either check whether a given delta uri is contained
		// in a reachable container and check for intersecting names afterwards, or we can do
		// the other way round
		// unfortunately there is no way to decide reliably which algorithm scales better
		// note that this method is called for each description so we have something like a 
		// number of deltas x number of resources which is not really nice
		List<IContainer> containers = null;
		Collection<QualifiedName> importedNames = getImportedNames(candidate);
		Map<String, Boolean> checkedProjects = Maps.newHashMap();
		for (IResourceDescription.Delta delta : deltas) {
			if (hasChanges(delta, candidate)) {
				// not a java resource - delta's resource should be contained in a visible container
				// as long as we did not delete the resource
				URI uri = delta.getUri();
				if ((uri.isPlatform() || uri.isArchive()) && delta.getNew() != null) {
					if (containers == null)
						containers = getContainerManager().getVisibleContainers(candidate, context);
					boolean descriptionIsContained = false;
					for (int i = 0; i < containers.size() && !descriptionIsContained; i++) {
						descriptionIsContained = containers.get(i).hasResourceDescription(uri);
					}
					if (!descriptionIsContained && !isProjectDependency(uri, candidate.getURI(), checkedProjects)) {
						continue;
					}
				}
				if (isAffected(importedNames, delta.getNew()) || isAffected(importedNames, delta.getOld())) {
					return true;
				}
			}
		}
		return false;
    }

	private boolean isProjectDependency(URI deltaURI, URI candidateURI, Map<String, Boolean> checkedProjects) {
		if (deltaURI.isPlatformResource() && candidateURI.isPlatformResource()) {
			// replace escape sequence e.g. %20 by a proper char, e.g. ' ' (blank)
			String deltaProjectName = URI.decode(deltaURI.segment(1));
			// avoid checking the same project again
			Boolean prev = checkedProjects.get(deltaProjectName);
			if (prev == null) {
				IProject deltaProject = workspaceRoot.getProject(deltaProjectName);
				// check if the delta is for a resource from an IJavaProject
				if (deltaProject.isAccessible() && JavaCore.create(deltaProject).exists()) {
					// same here, replace escape sequences by proper chars
					String candidateProjectName = URI.decode(candidateURI.segment(1));
					IProject candidateProject = workspaceRoot.getProject(candidateProjectName);
					if (candidateProject.isAccessible() && JavaCore.create(deltaProject).exists()) {
						if (candidateProject.equals(deltaProject)) {
							return checked(checkedProjects, deltaProjectName, true);
						}
						try {
							if (Arrays.asList(candidateProject.getReferencedProjects()).contains(deltaProject)) {
								return checked(checkedProjects, deltaProjectName, true);
							}
						} catch (CoreException e) {
							return checked(checkedProjects, deltaProjectName, false);
						}
					}
				}
				return checked(checkedProjects, deltaProjectName, false);
			}
			return prev;
		}
		return false;
	}

	private boolean checked(Map<String, Boolean> checkedProjects, String deltaProjectName, boolean result) {
		checkedProjects.put(deltaProjectName, result);
		return result;
	}
	
}
