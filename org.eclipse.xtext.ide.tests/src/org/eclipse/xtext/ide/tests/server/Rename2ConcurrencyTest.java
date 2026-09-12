/**
 * Copyright (c) 2017, 2026 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.testing.AbstractLanguageServerTest;
import org.eclipse.xtext.util.Modules2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author koehnlein - Initial contribution and API
 */
public class Rename2ConcurrencyTest extends AbstractLanguageServerTest {
	public Rename2ConcurrencyTest() {
		super("fileawaretestlanguage");
	}

	@Test(timeout = 30000)
	public void testRenameDoesNotDeadlockWithQueuedWrite() throws Exception {
		String model =
				"package foo\n" +
				"\n" +
				"element Foo {\n" +
				" ref Foo\n" +
				"}\n";
		String file = writeFile("foo/Foo.fileawaretestlanguage", model);
		initialize();
		TextDocumentIdentifier identifier = new TextDocumentIdentifier(file);
		Position position = new Position(2, 9);
		RenameParams params = new RenameParams(identifier, position, "Bar");
		InterleavingRequestManager requestManager = (InterleavingRequestManager) languageServer.getRequestManager();

		requestManager.gateNextRead();
		CompletableFuture<WorkspaceEdit> renameResult = languageServer.rename(params);
		Assert.assertTrue("Timed out waiting for rename read request to start.",
				requestManager.awaitReadEntered(10, TimeUnit.SECONDS));
		CompletableFuture<Object> writeResult = requestManager.<Object, Object>runWrite(() -> null,
				(cancelIndicator, it) -> null);
		requestManager.continueRead();

		WorkspaceEdit workspaceEdit = renameResult.get(10, TimeUnit.SECONDS);
		String expectation =
				"changes :\n" +
				"documentChanges : \n" +
				"    Foo.fileawaretestlanguage <1> : Bar [[2, 8] .. [2, 11]]\n" +
				"    Bar [[3, 5] .. [3, 8]]\n";
		assertEquals(expectation.toString(), toExpectation(workspaceEdit));
		writeResult.get(10, TimeUnit.SECONDS);
	}

	@Override
	protected InitializeResult initialize() {
		return super.initialize((InitializeParams params) -> {
			ClientCapabilities clientCapabilities = new ClientCapabilities();
			WorkspaceClientCapabilities workspaceClientCapabilities = new WorkspaceClientCapabilities();
			workspaceClientCapabilities.setWorkspaceFolders(true);
			WorkspaceEditCapabilities workspaceEditCapabilities = new WorkspaceEditCapabilities();
			workspaceEditCapabilities.setDocumentChanges(true);
			workspaceClientCapabilities.setWorkspaceEdit(workspaceEditCapabilities);
			clientCapabilities.setWorkspace(workspaceClientCapabilities);
			TextDocumentClientCapabilities textDocumentClientCapabilities = new TextDocumentClientCapabilities();
			textDocumentClientCapabilities.setRename(new RenameCapabilities(true, false));
			clientCapabilities.setTextDocument(textDocumentClientCapabilities);
			params.setCapabilities(clientCapabilities);
		});
	}

	@Override
	protected com.google.inject.Module getServerModule() {
		com.google.inject.Module defaultModule = super.getServerModule();
		com.google.inject.Module customModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(IRequestManager.class).to(InterleavingRequestManager.class).in(Scopes.SINGLETON);
			}
		};
		return Modules2.mixin(defaultModule, customModule);
	}

	@Override
	@After
	public void cleanup() {
		try {
			super.cleanup();
		} finally {
			if (languageServer != null) {
				languageServer.getRequestManager().shutdown();
			}
		}
	}
}
