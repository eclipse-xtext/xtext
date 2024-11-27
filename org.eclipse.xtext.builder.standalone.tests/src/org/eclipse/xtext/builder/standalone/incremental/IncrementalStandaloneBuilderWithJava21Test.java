/*******************************************************************************
 * Copyright (c) 2024 Lorenzo Bettini and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.builder.standalone.incremental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.xtext.util.JavaVersion;
import org.junit.Before;
import org.junit.Test;

public class IncrementalStandaloneBuilderWithJava21Test extends AbstractIncrementalStandaloneBuilderTest {

	private static final File ORIGINAL_PROJECT_DIR = new File("test-data/standalone.incremental.java21");

	@Before
	public void setUp() {
		copy(ORIGINAL_PROJECT_DIR.toPath(), PROJECT_DIR.toPath());
		testBuilder.resetCallStatistic();
		testBuilder.resetTestSetup();
		testBuilder.setFailOnValidationError(false);
		testBuilder.setIncrementalBuild(true);
		testBuilder.getCompiler().getConfiguration().setSourceLevel(JavaVersion.JAVA21.getQualifier());
		testBuilder.getCompiler().getConfiguration().setTargetLevel(JavaVersion.JAVA21.getQualifier());
		testBuilder.setTempDir(new File(PROJECT_DIR, "tmp"));
	}

	@Test
	public void testCleanBuildAccessingJavaRecord() {
		initBuilder(new ContentAssistFragmentTestLangConfiguration());
		assertTrue(testBuilder.launch());
		
		assertEquals(1, testBuilder.getGenerateCalled());
		assertEquals(2, testBuilder.getGenerateResources());

		File javaFile = getFile("src-gen/my/test/First.java");
		assertTrue(javaFile.exists());
		javaFile = getFile("src-gen/my/test/Second.java");
		assertTrue(javaFile.exists());
	}

}
