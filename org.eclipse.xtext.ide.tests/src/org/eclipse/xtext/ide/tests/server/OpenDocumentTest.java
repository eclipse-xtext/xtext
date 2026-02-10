/**
 * Copyright (c) 2016, 2025 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class OpenDocumentTest extends AbstractTestLangLanguageServerTest {
	@Test
	public void testOpenedDocumentShadowsPersistedFile() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}";
		String firstFile = writeFile("MyType1.testlang", model);
		initialize();
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.",
				Iterables.getFirst(getDiagnostics().get(firstFile), null).getMessage().getLeft());
		String path = writeFile("MyType2.testlang", "\n            type Foo {\n            }\n        ");
		languageServer.getWorkspaceService().didChangeWatchedFiles(new DidChangeWatchedFilesParams(
				Lists.newArrayList(new FileEvent(path, FileChangeType.Created))));
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.",
				Iterables.getFirst(getDiagnostics().get(firstFile), null).getMessage().getLeft());
		String model2 = "type NonExisting {}";
		open(path, model2);
		Assert.assertNull(Iterables.getFirst(getDiagnostics().get(firstFile), null));
		close(path);
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.",
				Iterables.getFirst(getDiagnostics().get(firstFile), null).getMessage().getLeft());
	}

	@Test
	public void testDidChange() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}\n";
		String firstFile = writeFile("MyType1.testlang",
				model);
		initialize();
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.",
				Iterables.getFirst(getDiagnostics().get(firstFile), null).getMessage().getLeft());
		open(firstFile, model);
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.",
				Iterables.getFirst(getDiagnostics().get(firstFile), null).getMessage().getLeft());
		DidChangeTextDocumentParams didChangeTextDocumentParams = new DidChangeTextDocumentParams();
		didChangeTextDocumentParams.setTextDocument(new VersionedTextDocumentIdentifier(firstFile, 2));
		TextDocumentContentChangeEvent textDocumentContentChangeEvent = new TextDocumentContentChangeEvent("Test");
		textDocumentContentChangeEvent.setRange(new Range(new Position(1, 4), new Position(1, 15)));
		didChangeTextDocumentParams.setContentChanges(Lists.newArrayList(textDocumentContentChangeEvent));
		languageServer.didChange(didChangeTextDocumentParams);
		Assert.assertNull(Iterables.getFirst(getDiagnostics().get(firstFile), null));
	}

	@Test
	public void testDidClose() {
		String fileURI = writeFile("Foo.testlang", "");
		initialize();
		String referencingFileURI = getVirtualFile("Bar.testlang");
		String model = 
				"type Bar {\n" +
				"    Foo foo\n" +
				"}\n";
		open(referencingFileURI, model);
		Assert.assertFalse(getDiagnostics().get(referencingFileURI).isEmpty());
		open(fileURI, "type Foo {}");
		Assert.assertTrue(getDiagnostics().get(referencingFileURI).isEmpty());
		close(fileURI);
		Assert.assertFalse(getDiagnostics().get(referencingFileURI).isEmpty());
	}

	@Test
	public void testDidCloseInMemory() {
		initialize();
		String fileURI = getVirtualFile("Foo.testlang");
		open(fileURI, "type Foo {}");
		String referencingFileURI = getVirtualFile("Bar.testlang");
		String model = 
				"type Bar {\n" +
				"    Foo foo\n" +
				"}\n";
		open(referencingFileURI, model);
		Assert.assertTrue(getDiagnostics().get(referencingFileURI).isEmpty());
		close(fileURI);
		Assert.assertFalse(getDiagnostics().get(referencingFileURI).isEmpty());
	}

	@Test
	public void testInMemory_GH_456() {
		initialize();
		String uri = "inmemory:/foo/bar.testlang";
		open(uri, "type Foo { }");
		Assert.assertNotNull("Diagnostics could be empty but never null.", getDiagnostics().get(uri));
	}
}
