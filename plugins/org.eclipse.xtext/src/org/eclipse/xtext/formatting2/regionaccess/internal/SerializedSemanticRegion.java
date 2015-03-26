/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.formatting2.regionaccess.internal;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.formatting2.regionaccess.IHiddenRegion;
import org.eclipse.xtext.formatting2.regionaccess.ISemanticRegion;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class SerializedSemanticRegion extends SerializedRegion implements ISemanticRegion {

	private final AbstractElement grammarElement;
	private IHiddenRegion leading;
	private final EObject semanticElement;
	private IHiddenRegion trailing;

	protected SerializedSemanticRegion(SerializerBasedRegionAccess regionAccess, EObject semanticElement,
			AbstractElement grammarElement, int offset, String text) {
		super(regionAccess, offset, text);
		this.semanticElement = semanticElement;
		this.grammarElement = grammarElement;
	}

	@Override
	public AbstractElement getGrammarElement() {
		return grammarElement;
	}

	@Override
	public IHiddenRegion getNextHiddenRegion() {
		return trailing;
	}

	@Override
	public ISemanticRegion getNextSemanticRegion() {
		return trailing != null ? trailing.getNextSemanticRegion() : null;
	}

	@Override
	public IHiddenRegion getPreviousHiddenRegion() {
		return leading;
	}

	@Override
	public ISemanticRegion getPreviousSemanticRegion() {
		return leading != null ? leading.getPreviousSemanticRegion() : null;
	}

	@Override
	public EObject getSemanticElement() {
		return semanticElement;
	}

	protected void setLeadingGap(IHiddenRegion leading) {
		this.leading = leading;
	}

	protected void setTrailingGap(IHiddenRegion trailing) {
		this.trailing = trailing;
	}
}