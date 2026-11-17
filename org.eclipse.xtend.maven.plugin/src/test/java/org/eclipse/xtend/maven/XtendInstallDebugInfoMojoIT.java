/*******************************************************************************
 * Copyright (c) 2013, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.maven;

import java.io.IOException;

import org.eclipse.xtend.maven.MavenExecutorUtil.MavenExecution;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class XtendInstallDebugInfoMojoIT {

	private static String ROOT = MavenExecutorUtil.IT_ROOT + "/install_debug_info";
	
	@BeforeClass
	static public void setUpOnce() throws IOException {
		MavenExecutorUtil.checkMavenExecutable(ROOT);
	}
	
	@Test
	public void simpleSMAP() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog(ROOT + "/simple_smap");
		String xtendGenDir = verifier.getBasedir() + "/target/classes/test";
		String debugInfo = new ClassFileDebugInfoExtractor().getDebugInfo(xtendGenDir);

		// please note that here we have source information for both Java and Xtend
		String xpt = 
				  "// test {\n" 
				+ "  package test; // XtendA.class\n"
				+ "  class XtendA {\n"
				+ "    void <init>() { // XtendA.java:11\n"
				+ "      // XtendA.java:12 -> XtendA.xtend:5\n"
				+ "      // XtendA.java:13:16 -> XtendA.xtend:6\n"
				+ "      void this;\n"
				+ "      void _function;\n"
				+ "    }\n"
				+ "    void lambda$new$0() { // XtendA.java:14\n"
				+ "      // XtendA.java:14 -> XtendA.xtend:6\n"
				+ "      void it;\n"
				+ "    }\n"
				+ "  }\n"
				+ "  \n"
				+ "  package test; // XtendA2.class\n"
				+ "  class XtendA2 {\n"
				+ "    void <init>() { // XtendA2.java:7\n"
				+ "      // XtendA2.java:8 -> XtendA.xtend:12\n"
				+ "      void this;\n"
				+ "    }\n"
				+ "  }\n"
				+ "}";
		Assert.assertEquals(debugInfo, xpt.toString(), debugInfo);
	}

	@Test
	public void simpleXtendAsPrimary() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog(ROOT + "/xtend_as_primary");
		String xtendGenDir = verifier.getBasedir() + "/target/classes/test";
		String debugInfo = new ClassFileDebugInfoExtractor().getDebugInfo(xtendGenDir);

		// please note that here we have source information only for Xtend
		String xpt = 
				"// test {\n" + 
				"  package test; // XtendA.class\n" + 
				"  class XtendA {\n" + 
				"    void <init>() { // XtendA.xtend:5\n" + 
				"      void this;\n" + 
				"      void myvar;\n" + 
				"    }\n" + 
				"    void lambda$new$0() { // XtendA.xtend:6\n" + 
				"      void it;\n" + 
				"    }\n" + 
				"  }\n" + 
				"  \n" + 
				"  package test; // XtendA2.class\n" + 
				"  class XtendA2 {\n" + 
				"    void <init>() { // XtendA.xtend:13\n" + 
				"      void this;\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
		Assert.assertEquals(debugInfo, xpt.toString(), debugInfo);
	}

	@Test
	public void simpleXtendAsPrimaryWithSyntheticVars() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog(ROOT + "/xtend_as_primary_with_synthetic_vars");
		String xtendGenDir = verifier.getBasedir() + "/target/classes/test";
		String debugInfo = new ClassFileDebugInfoExtractor().getDebugInfo(xtendGenDir);

		// please note that here we have source information only for Xtend
		String xpt = 
				"// test {\n" + 
				"  package test; // XtendA.class\n" + 
				"  class XtendA {\n" + 
				"    void <init>() { // XtendA.xtend:5\n" + 
				"      void this;\n" + 
				"      void _function;\n" + 
				"      void myvar;\n" + 
				"    }\n" + 
				"    void lambda$new$0() { // XtendA.xtend:6\n" + 
				"      void it;\n" + 
				"    }\n" + 
				"  }\n" + 
				"  \n" + 
				"  package test; // XtendA2.class\n" + 
				"  class XtendA2 {\n" + 
				"    void <init>() { // XtendA.xtend:13\n" + 
				"      void this;\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
		Assert.assertEquals(debugInfo, xpt.toString(), debugInfo);
	}

	@Test
	public void simpleSmapMultisource() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog(ROOT + "/smap_multisource");
		String info1 = new ClassFileDebugInfoExtractor().getDebugInfo(verifier.getBasedir() + "/target/classes");

		String xpt1 = 
				"// classes {\n" + 
				"  // test {\n" + 
				"    package test; // JavaB.class\n" + 
				"    class JavaB {\n" + 
				"      void <init>() { // JavaB.java:14\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void foo() { // JavaB.java:16\n" + 
				"        void this;\n" + 
				"        void test;\n" + 
				"      }\n" + 
				"    }\n" + 
				"    \n" + 
				"    package test; // XtendA.class\n" + 
				"    class XtendA {\n" + 
				"      void <init>() { // XtendA.java:4\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void test2() { // XtendA.java:6\n" + 
				"        // XtendA.java:6 -> XtendA.xtend:6\n" + 
				"        void this;\n" + 
				"        void s;\n" + 
				"      }\n" + 
				"      void newJavaB() { // XtendA.java:10\n" + 
				"        // XtendA.java:10 -> XtendA.xtend:10\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"    }\n" + 
				"    \n" + 
				"    package test; // XtendC.class\n" + 
				"    class XtendC {\n" + 
				"      void <init>() { // XtendC.java:4\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void test() { // XtendC.java:6\n" + 
				"        // XtendC.java:6 -> XtendC.xtend:5\n" + 
				"        void this;\n" + 
				"        void s;\n" + 
				"      }\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
		Assert.assertEquals(xpt1.toString(), info1);

		String info2 = new ClassFileDebugInfoExtractor().getDebugInfo(verifier.getBasedir() + "/target/test-classes");

		String xpt2 = 
				"// test-classes {\n" + 
				"  // foo {\n" + 
				"    package foo; // FooClass.class\n" + 
				"    class FooClass {\n" + 
				"      void <init>() { // FooClass.java:6\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void count() { // FooClass.java:8\n" + 
				"        // FooClass.java:8 -> FooClass.xtend:8\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void newJavaB() { // FooClass.java:12\n" + 
				"        // FooClass.java:12 -> FooClass.xtend:12\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"    }\n" + 
				"    \n" + 
				"    package foo; // FooTest.class\n" + 
				"    class FooTest {\n" + 
				"      void <init>() { // FooTest.java:7\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"      void assertTrue() { // FooTest.java:10\n" + 
				"        // FooTest.java:10 -> FooTest.xtend:11\n" + 
				"        void this;\n" + 
				"      }\n" + 
				"    }\n" + 
				"  }\n" + 
				"}";
		Assert.assertEquals(xpt2.toString(), info2);
	}

	private MavenExecution verifyErrorFreeLog(String pathToTestProject) throws IOException {
		return verifyErrorFreeLog(pathToTestProject, "verify");
	}

	private MavenExecution verifyErrorFreeLog(String pathToTestProject, String goal) throws IOException {
		MavenExecution verifier = newVerifier(pathToTestProject);
		verifier.executeGoal(goal);
		verifier.verifyErrorFreeLog();
		return verifier;
	}

	private MavenExecution newVerifier(String pathToTestProject) throws IOException {
		return MavenExecutorUtil.newVerifier(pathToTestProject);
	}
}
