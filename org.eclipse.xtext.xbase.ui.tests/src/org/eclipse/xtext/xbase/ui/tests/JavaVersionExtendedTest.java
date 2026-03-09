/*******************************************************************************
 * Copyright (c) 2018, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.tests;

import static org.junit.Assert.*;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.xtext.util.JavaVersion;
import org.junit.Test;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
public class JavaVersionExtendedTest {

	@Test
	public void testToJdtClassFileConstant() {
		assertEquals(ClassFileConstants.JDK1_8, JavaVersion.JAVA8.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK9, JavaVersion.JAVA9.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK10, JavaVersion.JAVA10.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK11, JavaVersion.JAVA11.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK17, JavaVersion.JAVA17.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK21, JavaVersion.JAVA21.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK25, JavaVersion.JAVA25.toJdtClassFileConstant());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSpoofedClassFileConstant() {
		assertEquals(ClassFileConstants.JDK1_8, JavaVersion.JAVA5.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK1_8, JavaVersion.JAVA6.toJdtClassFileConstant());
		assertEquals(ClassFileConstants.JDK1_8, JavaVersion.JAVA7.toJdtClassFileConstant());
	}

}
