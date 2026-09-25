/**
 * Copyright (c) 2016, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.testing.TemporaryFolder;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;
import org.eclipse.xtext.xbase.compiler.GeneratorConfigProvider;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.testing.CompilationTestHelper;
import org.eclipse.xtext.xbase.tests.jvmmodel.AbstractJvmModelTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
public class CompilationTestHelperTest extends AbstractJvmModelTest {
	@Rule
	@Inject
	public TemporaryFolder temporaryFolder;

	@Inject
	private CompilationTestHelper compilationTestHelper;

	@Inject
	private GeneratorConfigProvider generatorConfigProvider;

	@Inject
	private FileExtensionProvider extensionProvider;

	@Test
	public void testSwitchJavaVersion8() throws Exception {
		String source =
				"{\n" +
				"	val s = \"\"\n" +
				"	switch (s) {\n" +
				"		case \"foo\": \"foo\"\n" +
				"		default: \"bar\"\n" +
				"	}\n" +
				"}\n";
		compilationTestHelper.compile(source, it -> {
			String expectation =
					"@SuppressWarnings(\"all\")\n" + 
					"public class Test {\n" + 
					"  public String doStuff(final String s) {\n" + 
					"    String _xblockexpression = null;\n" + 
					"    {\n" + 
					"      final String s_1 = \"\";\n" + 
					"      String _switchResult = null;\n" + 
					"      if (s_1 != null) {\n" + 
					"        switch (s_1) {\n" + 
					"          case \"foo\":\n" + 
					"            _switchResult = \"foo\";\n" + 
					"            break;\n" + 
					"          default:\n" + 
					"            _switchResult = \"bar\";\n" + 
					"            break;\n" + 
					"        }\n" + 
					"      } else {\n" + 
					"        _switchResult = \"bar\";\n" + 
					"      }\n" + 
					"      _xblockexpression = _switchResult;\n" + 
					"    }\n" + 
					"    return _xblockexpression;\n" + 
					"  }\n" + 
					"}\n" + 
					"";
			Assert.assertEquals(expectation, Strings.toUnixLineSeparator(it.getSingleGeneratedCode()));
			it.getCompiledClass();
		});
	}

	@Test
	public void testLambdaJavaVersion8() throws Exception {
		String source =
				"{\n" +
				"	val f = [ int i | i + 1 ]\n" +
				"}\n";
		compilationTestHelper.compile(source, it -> {
			String expectation =
					"import org.eclipse.xtext.xbase.lib.Functions.Function1;\n" +
					"\n" +
					"@SuppressWarnings(\"all\")\n" +
					"public class Test {\n" +
					"  public String doStuff(final String s) {\n" +
					"    final Function1<Integer, Integer> _function = (Integer i) -> {\n" +
					"      return Integer.valueOf((i + 1));\n" +
					"    };\n" +
					"    final Function1<Integer, Integer> f = _function;\n" +
					"    return null;\n" +
					"  }\n" +
					"}\n";
			Assert.assertEquals(expectation, Strings.toUnixLineSeparator(it.getSingleGeneratedCode()));
			it.getCompiledClass();
		});
	}

	@Test
	public void testCustomConfigIsNotLost() throws Exception {
		String model =
				"{\n" +
				"	val f = [ int i | i + 1 ]\n" +
				"}\n";
		@SuppressWarnings("unchecked")
		ResourceSet resourceSet = compilationTestHelper
				.resourceSet(Pair.of("MyFile." + extensionProvider.getPrimaryFileExtension(), model));
		GeneratorConfig generatorConfig = new GeneratorConfig();
		generatorConfig.setGenerateSyntheticSuppressWarnings(false);
		generatorConfigProvider.install(resourceSet, generatorConfig);
		compilationTestHelper.compile(resourceSet, it -> {
			String expectation =
					"import org.eclipse.xtext.xbase.lib.Functions.Function1;\n" +
					"\n" +
					"public class Test {\n" +
					"  public String doStuff(final String s) {\n" +
					"    final Function1<Integer, Integer> _function = (Integer i) -> {\n" +
					"      return Integer.valueOf((i + 1));\n" +
					"    };\n" +
					"    final Function1<Integer, Integer> f = _function;\n" +
					"    return null;\n" +
					"  }\n" +
					"}\n";
			Assert.assertEquals(expectation, Strings.toUnixLineSeparator(it.getSingleGeneratedCode()));
		});
	}

	@Test
	public void testAssertNoErrorsWithErrors() throws Exception {
		String source = "{var int i = true; }";
		compilationTestHelper.compile(source, it -> {
			var thrown = assertThrows(AssertionError.class, () -> it.assertNoErrors());
			assertTrue(thrown.getMessage(),
					thrown.getMessage().contains("cannot convert from boolean to int"));
		});
	}

	@Test
	public void testAssertNoErrors() throws Exception {
		String source = "{ var int i = 0; null }";
		compilationTestHelper.compile(source, it -> it.assertNoErrors());
	}

	@Test
	public void testAssertNoIssuesWithWarnings() throws Exception {
		String source = "{var int i = 0; null }";
		compilationTestHelper.compile(source, it -> {
			var thrown = assertThrows(AssertionError.class, () -> it.assertNoIssues());
			assertTrue(thrown.getMessage(),
					thrown.getMessage().contains("is not used"));
		});
	}

	@Test
	public void testAssertNoIssues() throws Exception {
		String source = "{ \"a string\" }";
		compilationTestHelper.compile(source, it -> it.assertNoIssues());
	}

}
