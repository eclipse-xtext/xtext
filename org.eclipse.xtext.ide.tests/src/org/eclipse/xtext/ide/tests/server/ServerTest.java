/**
 * Copyright (c) 2016, 2026 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException;
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class ServerTest extends AbstractTestLangLanguageServerTest {
	@Test
	public void testInitializeBuild() {
		String model = 
				"type Test {\n" +
				"    string foo\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		Assert.assertTrue(Joiner.on(",").join(getDiagnostics().entrySet()),
				Iterables.getFirst(getDiagnostics().values(), null).isEmpty());
	}

	@Test
	public void testInitializeBuildWithError() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		Diagnostic firstDiagnostic = getFirstDiagnostic();
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.", firstDiagnostic.getMessage().getLeft());
		Assert.assertEquals(1, firstDiagnostic.getRange().getStart().getLine());
		Assert.assertEquals(4, firstDiagnostic.getRange().getStart().getCharacter());
		Assert.assertEquals(1, firstDiagnostic.getRange().getEnd().getLine());
		Assert.assertEquals(15, firstDiagnostic.getRange().getEnd().getCharacter());
	}

	private Diagnostic getFirstDiagnostic() {
		return Iterables.getFirst(Iterables.getFirst(getDiagnostics().values(), null), null);
	}

	@Test
	public void testPublishDiagnosticsAfterInitialized() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize(null, false);
		Assert.assertTrue("Diagnostics produced before 'initialized' was called", getDiagnostics().isEmpty());
	}

	@Test
	public void testIncrementalBuildWithError() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}\n";
		writeFile("MyType1.testlang", model);
		initialize();
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.", getFirstDiagnostic().getMessage().getLeft());
		String model2 = 
				"type NonExisting {\n" +
				"}\n";
		String path = writeFile("MyType2.testlang", model2);
		languageServer.getWorkspaceService().didChangeWatchedFiles(
				new DidChangeWatchedFilesParams(Lists.newArrayList(new FileEvent(path, FileChangeType.Created))));
		Assert.assertNotNull(getDiagnostics().get(path));
		for (List<Diagnostic> d : getDiagnostics().values()) {
			Assert.assertTrue(Joiner.on(",").join(d), d.isEmpty());
		}
	}

	@Test
	public void testIncrementalDeletion() {
		String model = 
				"type Test {\n" +
				"    NonExisting foo\n" +
				"}\n";
		String path = writeFile("MyType1.testlang", model);
		initialize();
		assertEquals("Couldn't resolve reference to TypeDeclaration 'NonExisting'.", getFirstDiagnostic().getMessage().getLeft());
		deleteFile("MyType1.testlang");
		languageServer.getWorkspaceService().didChangeWatchedFiles(
				new DidChangeWatchedFilesParams(Lists.newArrayList(new FileEvent(path, FileChangeType.Deleted))));
		Assert.assertTrue(Iterables.getFirst(getDiagnostics().values(), null).isEmpty());
	}

	@Test
	public void testTwoFilesDeleteClose() {
		String fileURI = writeFile("Foo.testlang", "");
		initialize();
		String referencingFileURI = getVirtualFile("Bar.testlang");
		String model = 
				"type Bar {\n" +
				"    Foo foo\n" +
				"}\n";
		open(referencingFileURI, model);
		Assert.assertFalse("Bar.testlang references missing type Foo from Foo.testlang: expect error",
				getDiagnostics().get(referencingFileURI).isEmpty());
		open(fileURI, "type Foo {}");
		Assert.assertTrue("Bar.testlang references type Foo from Foo.testlang: expect no error",
				getDiagnostics().get(referencingFileURI).isEmpty());
		deleteFile(fileURI);
		languageServer.getWorkspaceService().didChangeWatchedFiles(
				new DidChangeWatchedFilesParams(Lists.newArrayList(new FileEvent(fileURI, FileChangeType.Deleted))));
		Assert.assertTrue("delete file on disk: expect no error", getDiagnostics().get(referencingFileURI).isEmpty());
		close(fileURI);
		Assert.assertFalse("close deleted file: expect error", getDiagnostics().get(referencingFileURI).isEmpty());
	}

	@Test
	public void testMissingInitialize() throws Exception {
		try {
			DefinitionParams definitionParams = new DefinitionParams();
			definitionParams.setTextDocument(new TextDocumentIdentifier("file:/home/test/workspace/mydoc.testlang"));
			languageServer.definition(definitionParams).get();
			Assert.fail("Expected a ResponseErrorException");
		} catch (ExecutionException exception) {
			Assert.assertEquals(ResponseErrorCode.ServerNotInitialized.getValue(),
					((ResponseErrorException) exception.getCause()).getResponseError().getCode());
		}
	}
}
