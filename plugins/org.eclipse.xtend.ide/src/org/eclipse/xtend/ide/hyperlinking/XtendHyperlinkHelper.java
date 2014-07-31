/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.hyperlinking;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkAcceptor;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations;
import org.eclipse.xtext.xbase.typesystem.computation.IAmbiguousLinkingCandidate;
import org.eclipse.xtext.xbase.typesystem.computation.IConstructorLinkingCandidate;
import org.eclipse.xtext.xbase.typesystem.computation.IFeatureLinkingCandidate;
import org.eclipse.xtext.xbase.typesystem.computation.ILinkingCandidate;
import org.eclipse.xtext.xbase.typesystem.computation.ISuspiciouslyOverloadedCandidate;
import org.eclipse.xtext.xbase.ui.navigation.XbaseHyperLinkHelper;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Customized hyperlinker for Xtend source files.
 * 
 * First and foremost, it redirects hyperlinks to the source elements if the referenced target is an inferred element.
 * That means, all contributing dispatch methods are available on hyperlink requests.
 * 
 * This implementation also provides hyperlinks to all ambiguous candidates if a feature or constructor call was
 * resolved to multiple equally valid features.
 * 
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sebastian Zarnekow - Navigation to all ambiguous candidates.
 */
public class XtendHyperlinkHelper extends XbaseHyperLinkHelper {

	@Inject
	private IJvmModelAssociations associations;

	/**
	 * This redirects the hyperlinks to all source elements of an inferred element.
	 */
	@Override
	public void createHyperlinksTo(XtextResource from, Region region, EObject to, IHyperlinkAcceptor acceptor) {
		Set<EObject> sourceElements = associations.getSourceElements(to);
		if (sourceElements.isEmpty()) {
			super.createHyperlinksTo(from, region, to, acceptor);
		} else {
			for (EObject sourceElement : sourceElements) {
				super.createHyperlinksTo(from, region, sourceElement, acceptor);
			}
		}
	}

	@Override
	protected void createHyperlinksForCrossRef(XtextResource resource, INode crossRefNode,
			IHyperlinkAcceptor acceptor) {
		EObject crossLinkedEObject = getEObjectAtOffsetHelper().getCrossReferencedElement(crossRefNode);
		if (crossLinkedEObject != null && !crossLinkedEObject.eIsProxy()) {
			EObject containedElementAt = getEObjectAtOffsetHelper().resolveContainedElementAt(resource,
					crossRefNode.getOffset());
			if (containedElementAt instanceof XAbstractFeatureCall) {
				XAbstractFeatureCall casted = (XAbstractFeatureCall) containedElementAt;
				if (casted.getFeature() == crossLinkedEObject) {
					IFeatureLinkingCandidate candidate = getBatchTypeResolver().resolveTypes(casted).getLinkingCandidate(
							casted);
					if (candidate instanceof IAmbiguousLinkingCandidate) {
						createMultipleLinks(resource, crossRefNode,
								((IAmbiguousLinkingCandidate) candidate).getAlternatives(), acceptor);
					} else if (candidate instanceof ISuspiciouslyOverloadedCandidate) {
						ISuspiciouslyOverloadedCandidate castedCandidate = (ISuspiciouslyOverloadedCandidate) candidate;
						createMultipleLinks(
								resource,
								crossRefNode,
								Lists.newArrayList(castedCandidate.getChosenCandidate(),
										castedCandidate.getRejectedCandidate()), acceptor);
					} else {
						createHyperlinksTo(resource, crossRefNode, crossLinkedEObject, acceptor);
					}
				}
			} else if (containedElementAt instanceof XConstructorCall) {
				XConstructorCall casted = (XConstructorCall) containedElementAt;
				if (casted.getConstructor() == crossLinkedEObject) {
					IConstructorLinkingCandidate candidate = getBatchTypeResolver().resolveTypes(casted)
							.getLinkingCandidate(casted);
					if (candidate instanceof IAmbiguousLinkingCandidate) {
						createMultipleLinks(resource, crossRefNode,
								((IAmbiguousLinkingCandidate) candidate).getAlternatives(), acceptor);
					} else {
						createHyperlinksTo(resource, crossRefNode, crossLinkedEObject, acceptor);
					}
				} else {
					createHyperlinksTo(resource, crossRefNode, crossLinkedEObject, acceptor);
				}
			} else {
				createHyperlinksTo(resource, crossRefNode, crossLinkedEObject, acceptor);
			}
		}
		super.createHyperlinksForCrossRef(resource, crossRefNode, acceptor);
	}

	private void createMultipleLinks(XtextResource resource, INode crossRefNode,
			List<? extends ILinkingCandidate> alternatives, IHyperlinkAcceptor acceptor) {
		for (ILinkingCandidate alternative : alternatives) {
			createHyperlinksTo(resource, crossRefNode, alternative.getFeature(), acceptor);
		}
	}
}
