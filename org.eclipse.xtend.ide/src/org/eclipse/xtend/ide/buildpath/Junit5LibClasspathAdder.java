/*******************************************************************************
 * Copyright (c) 2019, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.buildpath;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author vivienjovet - Initial contribution and API
 */
public class Junit5LibClasspathAdder extends AbstractLibClasspathAdder {

	public static final IPath JUNIT5_LIBRARY_PATH = new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/5");
	public static final String[] BUNDLE_IDS = new String[] { 
			"org.junit",
			"org.hamcrest.core",
			"org.opentest4j"
	};

	@Override
	protected IClasspathEntry createContainerClasspathEntry() {
		return JavaCore.newContainerEntry(JUNIT5_LIBRARY_PATH);
	}

	@Override
	protected String[] getBundleIds() {
		return BUNDLE_IDS;
	}
	
	@Override
	protected String[] getImportedPackages() {
		return new String[] {
			"org.junit.jupiter.api;version=\"[5.1.0,6.0.0)\"",
			"org.junit.jupiter.api.condition;version=\"[5.1.0,6.0.0)\"",
			"org.junit.jupiter.api.extension;version=\"[5.1.0,6.0.0)\"",
			"org.junit.jupiter.api.function;version=\"[5.1.0,6.0.0)\"",
			"org.junit.jupiter.api.io;version=\"[5.1.0,6.0.0)\"",
			"org.junit.jupiter.api.parallel;version=\"[5.1.0,6.0.0)\"",
			"org.junit.platform.commons.support;version=\"[1.0.0,2.0.0)\";resolution:=optional"
		};
	}

}
