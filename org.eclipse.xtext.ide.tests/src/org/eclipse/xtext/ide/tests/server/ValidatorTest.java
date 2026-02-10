/**
 * Copyright (c) 2019, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.xtext.ide.tests.testlanguage.validation.TestLanguageValidator;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
public class ValidatorTest extends AbstractTestLangLanguageServerTest {
	@Test
	public void testMultilineDiagnostic() {
		String model = 
				"type Multiline_Demo {\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		List<Diagnostic> problems = problems();
		Assert.assertEquals("problems found:\n" + Joiner.on("\n").join(problems), 1, problems.size());
		Diagnostic problem = Iterables.getFirst(problems, null);
		assertEquals("Test Validation to mark the whole type", problem.getMessage().getLeft());
		Assert.assertEquals(TestLanguageValidator.MULTILINE_PROBLEM, problem.getCode().get());
		Range range = problem.getRange();
		Assert.assertEquals(0, range.getStart().getLine());
		Assert.assertEquals(0, range.getStart().getCharacter());
		Assert.assertEquals(1, range.getEnd().getLine());
		Assert.assertEquals(1, range.getEnd().getCharacter());
	}

	@Test
	public void testMultilineDiagnostic_02() {
		String model = 
				"type Multiline_Demo2 {\n" +
				"string sample}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		List<Diagnostic> problems = problems();
		Assert.assertEquals("problems found:\n" + Joiner.on("\n").join(problems), 1, problems.size());
		Diagnostic problem = Iterables.getFirst(problems, null);
		assertEquals("Test Validation to mark the whole type", problem.getMessage().getLeft());
		Assert.assertEquals(TestLanguageValidator.MULTILINE_PROBLEM, problem.getCode().get());
		Range range = problem.getRange();
		Assert.assertEquals(0, range.getStart().getLine());
		Assert.assertEquals(0, range.getStart().getCharacter());
		Assert.assertEquals(1, range.getEnd().getLine());
		Assert.assertEquals(14, range.getEnd().getCharacter());
	}

	@Test
	public void testDiagnosticAtEndOfLineExcludingNewline() {
		String model = 
				"type lowercase\n" +
				"{\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		List<Diagnostic> problems = problems();
		Assert.assertEquals("problems found:\n" + Joiner.on("\n").join(problems), 1, problems.size());
		Diagnostic problem = Iterables.getFirst(problems, null);
		assertEquals("Name should start with a capital", problem.getMessage().getLeft());
		Assert.assertEquals(TestLanguageValidator.INVALID_NAME, problem.getCode().get());
		Range range = problem.getRange();
		Assert.assertEquals(0, range.getStart().getLine());
		Assert.assertEquals(5, range.getStart().getCharacter());
		Assert.assertEquals(0, range.getEnd().getLine());
		Assert.assertEquals(14, range.getEnd().getCharacter());
	}

	@Test
	public void testDiagnosticAtEndOfLineIncludingNewline() {
		String model = "type\n";
		writeFile("MyType1.testlang", model);
		initialize();
		List<Diagnostic> problems = problems();
		Assert.assertEquals("problems found:\n" + Joiner.on("\n").join(problems), 1, problems.size());
		Diagnostic problem = Iterables.getFirst(problems, null);
		assertEquals("mismatched input '<EOF>' expecting RULE_ID", problem.getMessage().getLeft());
		Assert.assertEquals(org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC, problem.getCode().get());
		Range range = problem.getRange();
		Assert.assertEquals(0, range.getStart().getLine());
		Assert.assertEquals(4, range.getStart().getCharacter());
		Assert.assertEquals(1, range.getEnd().getLine());
		Assert.assertEquals(0, range.getEnd().getCharacter());
	}
	

	private List<Diagnostic> problems() {
		return Iterables.getFirst(
				getDiagnostics().entrySet(), null).getValue();
	}
}
