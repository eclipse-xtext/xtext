/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.parsetree.reconstr.impl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.google.inject.Singleton;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 * @author Jan Koehnlein - support for cross-resource containment
 */
@Singleton
public class DefaultTransientValueService extends AbstractTransientValueService {

	@Override
	public boolean isTransient(EObject owner, EStructuralFeature feature, int index) {
		return feature.isTransient() || !owner.eIsSet(feature) || isContainerReferenceInSameResource(owner, feature);
	}

	protected boolean isContainerReferenceInSameResource(EObject owner, EStructuralFeature feature) {
		if (feature instanceof EReference && ((EReference) feature).isContainer()) {
			Resource ownerResource = ((InternalEObject) owner).eDirectResource();
			// if eDirectResource is set, owner is a root element, so its container 
			// must be in another resource 
			return (ownerResource == null);
		}
		return false;
	}
}
