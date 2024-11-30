/*******************************************************************************
 * Copyright (c) 2024 Lorenzo Bettini and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.xtext.testing.TemporaryFolder;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.eclipse.xtext.xbase.tests.jvmmodel.AbstractJvmModelTest;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
public class Java21RecordCompilerTest extends AbstractJvmModelTest {
	@Rule
	@Inject
	public TemporaryFolder temporaryFolder;

	@Inject
	private CompilationTestHelper compilationTestHelper;

	@Test public void testUseJavaRecord() throws Exception {
		String source =
				"{\n"
				+ "  var interval = new test.RecordWithFields(0, 10);\n"
				+ "  var int begin = interval.begin\n"
				+ "  var end = interval.end()\n"
				+ "  interval.toString"
				+ "}";
		compilationTestHelper.compile(source, it -> {
			String expectation =
					"import test.RecordWithFields;\n"
					+ "\n"
					+ "@SuppressWarnings(\"all\")\n"
					+ "public class Test {\n"
					+ "  public String doStuff(final String s) {\n"
					+ "    String _xblockexpression = null;\n"
					+ "    {\n"
					+ "      RecordWithFields interval = new RecordWithFields(0, 10);\n"
					+ "      int begin = interval.begin();\n"
					+ "      int end = interval.end();\n"
					+ "      _xblockexpression = interval.toString();\n"
					+ "    }\n"
					+ "    return _xblockexpression;\n"
					+ "  }\n"
					+ "}\n"
					+ "";
			it.assertNoErrors();
			assertEquals(expectation, Strings.toUnixLineSeparator(it.getSingleGeneratedCode()));
			it.getCompiledClass(); // check that the generated Java code compiles
		});
	}
}
