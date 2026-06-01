/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard;

public enum Outlet {
	ROOT,
	MAIN_JAVA,
	TEST_JAVA,
	MAIN_RESOURCES,
	TEST_RESOURCES,
	MAIN_SRC_GEN,
	TEST_SRC_GEN,
	MAIN_XTEND_GEN,
	TEST_XTEND_GEN,
	META_INF;
	
	public static Outlet[] generateOutlets() {
		return new Outlet[] { MAIN_SRC_GEN, TEST_SRC_GEN, MAIN_XTEND_GEN, TEST_XTEND_GEN };
	}

	public static Outlet[] testOutlets() {
		return new Outlet[] { TEST_JAVA, TEST_RESOURCES, TEST_SRC_GEN, TEST_XTEND_GEN};
	}
	
}
