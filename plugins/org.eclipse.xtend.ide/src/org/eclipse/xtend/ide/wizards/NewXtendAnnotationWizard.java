/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.wizards;

import org.eclipse.xtext.ui.IImageHelper;

import com.google.inject.Inject;

/**
 * @author Holger Schill - Initial contribution and API
 */
public class NewXtendAnnotationWizard extends AbstractNewXtendElementWizard {
public static final String TITLE = "Xtend Annotation"; //$NON-NLS-1$

	@Inject
	public NewXtendAnnotationWizard(IImageHelper imgHelper, NewXtendAnnotationWizardPage page) {
		super(imgHelper, page, TITLE);
	}
}
