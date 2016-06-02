/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xpect.xtext.lib.setup.emf;

import org.eclipse.xpect.setup.XpectSetupComponent;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@Deprecated
@XpectSetupComponent
public class File extends org.eclipse.xpect.xtext.lib.setup.emf.Resource {

	public File() {
		super();
	}

	public File(org.eclipse.xpect.xtext.lib.setup.generic.File file) {
		super(file);
	}

	public File(String name) {
		super(new org.eclipse.xpect.xtext.lib.setup.generic.File(name));
	}

}
