/*******************************************************************************
 * Copyright (c) 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
public class JavaVersionTest {
	@Test
	public void test_fromBree() {
		assertEquals(JavaVersion.JAVA25, JavaVersion.fromBree("JavaSE-25"));
		assertEquals(JavaVersion.JAVA21, JavaVersion.fromBree("JavaSE-21"));
		assertEquals(JavaVersion.JAVA17, JavaVersion.fromBree("JavaSE-17"));
		assertEquals(JavaVersion.JAVA11, JavaVersion.fromBree("JavaSE-11"));
		assertEquals(JavaVersion.JAVA10, JavaVersion.fromBree("JavaSE-10"));
		assertEquals(JavaVersion.JAVA9, JavaVersion.fromBree("JavaSE-9"));
		assertEquals(JavaVersion.JAVA8, JavaVersion.fromBree("JavaSE-1.8"));
		assertNull(JavaVersion.fromBree("JavaSE-1.11"));
		assertNull(JavaVersion.fromBree("JavaSE-1.10"));
		assertNull(JavaVersion.fromBree(null));
	}
	
	@Test
	public void testFromQualifier() {
		assertEquals(JavaVersion.JAVA25, JavaVersion.fromQualifier("25"));
		assertEquals(JavaVersion.JAVA25, JavaVersion.fromQualifier("1.25"));
		assertEquals(JavaVersion.JAVA21, JavaVersion.fromQualifier("21"));
		assertEquals(JavaVersion.JAVA21, JavaVersion.fromQualifier("1.21"));
		assertEquals(JavaVersion.JAVA17, JavaVersion.fromQualifier("17"));
		assertEquals(JavaVersion.JAVA17, JavaVersion.fromQualifier("1.17"));
		assertEquals(JavaVersion.JAVA11, JavaVersion.fromQualifier("11"));
		assertEquals(JavaVersion.JAVA11, JavaVersion.fromQualifier("1.11"));
		assertEquals(JavaVersion.JAVA10, JavaVersion.fromQualifier("10"));
		assertEquals(JavaVersion.JAVA10, JavaVersion.fromQualifier("1.10"));
		assertEquals(JavaVersion.JAVA9, JavaVersion.fromQualifier("9"));
		assertEquals(JavaVersion.JAVA9, JavaVersion.fromQualifier("1.9"));
		assertEquals(JavaVersion.JAVA8, JavaVersion.fromQualifier("8"));
		assertEquals(JavaVersion.JAVA8, JavaVersion.fromQualifier("1.8"));
		assertNull(JavaVersion.fromQualifier("7"));
		assertNull(JavaVersion.fromQualifier("1.7"));
		assertNull(JavaVersion.fromQualifier("6"));
		assertNull(JavaVersion.fromQualifier("1.6"));
		assertNull(JavaVersion.fromQualifier("1.5"));
		assertNull(JavaVersion.fromQualifier("1.4"));
		assertNull(JavaVersion.fromQualifier("5"));
		assertNull(JavaVersion.fromQualifier(null));
	}
	
	@Test
	public void test_getQualifier() {
		assertEquals("25", JavaVersion.JAVA25.getQualifier());
		assertEquals("21", JavaVersion.JAVA21.getQualifier());
		assertEquals("17", JavaVersion.JAVA17.getQualifier());
		assertEquals("11", JavaVersion.JAVA11.getQualifier());
		assertEquals("10", JavaVersion.JAVA10.getQualifier());
		assertEquals("9", JavaVersion.JAVA9.getQualifier());
		assertEquals("1.8", JavaVersion.JAVA8.getQualifier());
	}
	

	@Test
	public void test_isAtLeast() {
		assertTrue(JavaVersion.JAVA9.isAtLeast(JavaVersion.JAVA9));
		assertTrue(JavaVersion.JAVA9.isAtLeast(JavaVersion.JAVA8));
		assertFalse(JavaVersion.JAVA9.isAtLeast(JavaVersion.JAVA10));
	}

	@Test
	public void test_toJdtClassFileConstant () {
		assertEquals(4521984, JavaVersion.JAVA25.toJdtClassFileConstant());
		assertEquals(4259840, JavaVersion.JAVA21.toJdtClassFileConstant());
		assertEquals(3997696, JavaVersion.JAVA17.toJdtClassFileConstant());
		assertEquals(3604480, JavaVersion.JAVA11.toJdtClassFileConstant());
		assertEquals(3538944, JavaVersion.JAVA10.toJdtClassFileConstant());
		assertEquals(3473408, JavaVersion.JAVA9.toJdtClassFileConstant());
		assertEquals(3407872, JavaVersion.JAVA8.toJdtClassFileConstant());
	}

}
