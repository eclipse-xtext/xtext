/**
 * Copyright (c) 2016, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.TemporaryFolder;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.util.JavaRuntimeVersion;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.testing.OnTheFlyJavaCompiler2;
import org.eclipse.xtext.xbase.tests.XbaseInjectorProvider;
import org.eclipse.xtext.xbase.tests.jvmmodel.AbstractJvmModelTest;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@InjectWith(XbaseInjectorProvider.class)
@RunWith(XtextRunner.class)
public class OnTheFlyJavaCompiler2Test extends AbstractJvmModelTest {
	@Rule
	@Inject
	public TemporaryFolder temporaryFolder;

	@Inject
	private OnTheFlyJavaCompiler2 javaCompiler;

	@Test
	public void testDefaultJavaVersion() {
		OnTheFlyJavaCompiler2 defaultCompiler = new OnTheFlyJavaCompiler2(getClass().getClassLoader());
		assertEquals(JavaVersion.JAVA21, defaultCompiler.getJavaVersion());
		defaultCompiler.compileToClass("tests.Main", "package tests; public class Main { record Hello(String s) {} }");
	}

	@Test
	public void testJavaVersion8_01() {
		assertJavaCompilation("java.util.List<String> list = new java.util.LinkedList<>();", JavaVersion.JAVA8);
	}

	@Test
	public void testJavaVersion8_02() {
		assertJavaCompilation("Runnable r = () -> {};", JavaVersion.JAVA8);
	}
	
	@Test
	public void testJavaVersion17() {
		Assume.assumeTrue("Active only on Java 17 and later", JavaRuntimeVersion.isJava17OrLater());
		assertJavaCompilation("record Hello(String s) {}", JavaVersion.JAVA17);
	}

	private Class<?> assertJavaCompilation(CharSequence input, JavaVersion javaVersion) {
			if (javaVersion != null) {
				this.javaCompiler.setJavaVersion(javaVersion);
			}
			StringBuilder builder = new StringBuilder();
			builder.append("package tests;\n");
			builder.append("public class Main {\n");
			builder.append("	public static void main(String args[]) {\n");
			builder.append("		" + input + "\n");
			builder.append("	}\n");
			builder.append("}");
			return javaCompiler.compileToClass("tests.Main", builder.toString());
	}
}
