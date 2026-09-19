/**
 * Copyright (c) 2016, 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.xtext.testing.TestCompletionConfiguration;
import org.junit.Test;

/**
 * @author kosyakov - Initial contribution and API
 */
public class CompletionTest extends AbstractTestLangLanguageServerTest {
	@Test
	public void testCompletion_01() {
		testCompletion((TestCompletionConfiguration it) -> {
			it.setModel("");
			String expectedCompletionItems =
					"type declaration template (Add type declaration) -> type ${1:name} {\n  $0\n}\n [[0, 0] .. [0, 0]]\n" +
					"package -> package [[0, 0] .. [0, 0]]\n" +
					"type -> type [[0, 0] .. [0, 0]]\n" +
					"Sample Snippet -> type ${1|A,B,C|} {\n" +
					"    \n" +
					"} [[0, 0] .. [0, 0]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testCompletion_02() {
		testCompletion((TestCompletionConfiguration it) -> {
			it.setModel("type ");
			it.setColumn(5);
			String expectedCompletionItems = "name (ID) -> name [[0, 5] .. [0, 5]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testCompletion_03() {
		testCompletion((TestCompletionConfiguration it) -> {
			String model =
					"type Foo {}\n" +
					"type Bar {}\n";
			it.setModel(model);
			it.setLine(1);
			it.setColumn("type Bar {".length());
			String expectedCompletionItems =
					"Bar (TypeDeclaration) -> Bar [[1, 10] .. [1, 10]]\n" +
					"Foo (TypeDeclaration) -> Foo [[1, 10] .. [1, 10]]\n" +
					"boolean -> boolean [[1, 10] .. [1, 10]]\n" +
					"int -> int [[1, 10] .. [1, 10]]\n" +
					"op -> op [[1, 10] .. [1, 10]]\n" +
					"string -> string [[1, 10] .. [1, 10]]\n" +
					"void -> void [[1, 10] .. [1, 10]]\n" +
					"} -> } [[1, 10] .. [1, 10]]\n" +
					"{ -> { [[1, 9] .. [1, 10]]\n" +
					"   + } [[1, 11] .. [1, 11]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testCompletion_04() {
		testCompletion((TestCompletionConfiguration it) -> {
			String model =
					"type Foo {\n" +
					"    Foo foo\n" +
					"}\n";
			it.setModel(model);
			it.setLine(1);
			it.setColumn("    Fo".length());
			String expectedCompletionItems =
					"Foo (TypeDeclaration) -> Foo [[1, 4] .. [1, 6]]\n" +
					"[ -> [ [[1, 6] .. [1, 6]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testCompletion_05() {
		testCompletion((TestCompletionConfiguration it) -> {
			String model =
					"type Foo {}\n" +
					"type foo {}\n" +
					"type Boo {}\n" +
					"type boo {}\n";
			it.setModel(model);
			it.setLine(1);
			it.setColumn("type Bar {".length());
			String expectedCompletionItems = "Boo (TypeDeclaration) -> Boo [[1, 10] .. [1, 10]]\n" +
				"boo (TypeDeclaration) -> boo [[1, 10] .. [1, 10]]\n" +
				"Foo (TypeDeclaration) -> Foo [[1, 10] .. [1, 10]]\n" +
				"foo (TypeDeclaration) -> foo [[1, 10] .. [1, 10]]\n" +
				"boolean -> boolean [[1, 10] .. [1, 10]]\n" +
				"int -> int [[1, 10] .. [1, 10]]\n" +
				"op -> op [[1, 10] .. [1, 10]]\n" +
				"string -> string [[1, 10] .. [1, 10]]\n" +
				"void -> void [[1, 10] .. [1, 10]]\n" +
				"} -> } [[1, 10] .. [1, 10]]\n" +
				"{ -> { [[1, 9] .. [1, 10]]\n" +
				"   + } [[1, 11] .. [1, 11]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testSnippet() {
		withKind = true;
		testCompletion((TestCompletionConfiguration it) -> {
			String model = "type Foo {}\n";
			it.setModel(model);
			it.setLine(1);
			it.setColumn(0);
			String expectedCompletionItems =
					"(Snippet|Snippet) type declaration template (Add type declaration) -> type ${1:name} {\n  $0\n}\n [[1, 0] .. [1, 0]]\n" +
					"(Keyword) package -> package [[1, 0] .. [1, 0]]\n" +
					"(Keyword) type -> type [[1, 0] .. [1, 0]]\n" +
					"(Snippet|Snippet) Sample Snippet -> type ${1|A,B,C|} {\n" +
					"    \n" +
					"} [[1, 0] .. [1, 0]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
		withKind = false;
	}

	@Test
	public void testCompletion_AdditionalEdits_01() {
		testCompletion((TestCompletionConfiguration it) -> {
			String model = "type Foo \n";
			it.setModel(model);
			it.setLine(0);
			it.setColumn("type Foo ".length());
			String expectedCompletionItems =
					"extends -> extends [[0, 9] .. [0, 9]]\n" +
					"{ -> { [[0, 9] .. [0, 9]]\n" +
					"   + } [[1, 0] .. [1, 0]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	@Test
	public void testCompletion_AdditionalEdits_02() {
		testCompletion((TestCompletionConfiguration it) -> {
			String model = "type Foo  \n";
			it.setModel(model);
			it.setLine(0);
			it.setColumn("type Foo ".length());
			String expectedCompletionItems = "extends -> extends [[0, 9] .. [0, 9]]\n" +
				"{ -> { [[0, 9] .. [0, 9]]\n" +
				"   + } [[0, 10] .. [0, 10]]\n";
			it.setExpectedCompletionItems(expectedCompletionItems);
		});
	}

	private boolean withKind = false;

	@Override
	protected String _toExpectation(CompletionItem it) {
		if (withKind) {
			return "(" + it.getKind() + (it.getInsertTextFormat() != null ? "|" + it.getInsertTextFormat() : "") + ") "
					+ super._toExpectation(it);
		} else {
			return super._toExpectation(it);
		}
	}

}
