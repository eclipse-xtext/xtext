/**
 * Copyright (c) 2017, 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameCapabilities;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.testing.AbstractLanguageServerTest;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Scopes;

/**
 * @author koehnlein - Initial contribution and API
 */
public class Rename2Test extends AbstractLanguageServerTest {
	public static class InterleavingRequestManager extends RequestManager {
		private final AtomicBoolean gateNextRead = new AtomicBoolean();
		private final CountDownLatch readEntered = new CountDownLatch(1);
		private final CountDownLatch continueRead = new CountDownLatch(1);

		@Inject
		public InterleavingRequestManager(ExecutorService parallel,
				OperationCanceledManager operationCanceledManager) {
			super(parallel, operationCanceledManager);
		}

		public void gateNextRead() {
			gateNextRead.set(true);
		}

		public boolean awaitReadEntered(long timeout, TimeUnit unit) throws InterruptedException {
			return readEntered.await(timeout, unit);
		}

		public void continueRead() {
			continueRead.countDown();
		}

		@Override
		public synchronized <V> CompletableFuture<V> runRead(
				Function1<? super CancelIndicator, ? extends V> cancellable) {
			if (gateNextRead.compareAndSet(true, false)) {
				return super.runRead((CancelIndicator cancelIndicator) -> {
					readEntered.countDown();
					try {
						if (!continueRead.await(5, TimeUnit.SECONDS)) {
							throw new AssertionError("Timed out waiting to continue read request.");
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new AssertionError(e);
					}
					return cancellable.apply(cancelIndicator);
				});
			}
			return super.runRead(cancellable);
		}
	}

	public Rename2Test() {
		super("fileawaretestlanguage");
	}

	@Test
	public void testRenameSelfRef() throws Exception {
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
		Range range = languageServer.prepareRename(new PrepareRenameParams(identifier, position)).get().getLeft();
		assertEquals("Foo", new Document(0, model).getSubstring(range));
		RenameParams params = new RenameParams(identifier, position, "Bar");
		WorkspaceEdit workspaceEdit = languageServer.rename(params).get();
		String expectation =
				"changes :\n" +
				"documentChanges : \n" +
				"    Foo.fileawaretestlanguage <1> : Bar [[2, 8] .. [2, 11]]\n" +
				"    Bar [[3, 5] .. [3, 8]]\n";
		assertEquals(
				expectation
						.toString(),
				toExpectation(workspaceEdit));
	}

	@Test
	public void testRenameContainer() throws Exception {
		String model =
				"package foo\n" +
				"\n" +
				"element Foo {\n" +
				" element Bar {\n" +
				" }\n" +
				" ref foo.Foo.Bar\n" +
				" ref Foo.Bar\n" +
				" ref Bar\n" +
				"}\n";
		String file = writeFile("foo/Foo.fileawaretestlanguage", model);
		initialize();
		TextDocumentIdentifier identifier = new TextDocumentIdentifier(file);
		Position position = new Position(2, 9);
		Range range = languageServer.prepareRename(new PrepareRenameParams(identifier, position)).get().getLeft();
		assertEquals("Foo", new Document(0, model).getSubstring(range));
		RenameParams params = new RenameParams(identifier, position, "Baz");
		WorkspaceEdit workspaceEdit = languageServer.rename(params).get();
		String expectation =
				"changes :\n" +
				"documentChanges : \n" +
				"    Foo.fileawaretestlanguage <1> : Baz [[2, 8] .. [2, 11]]\n" +
				"    Bar [[5, 5] .. [5, 16]]\n" +
				"    Bar [[6, 5] .. [6, 12]]\n";
		assertEquals(
				expectation
						.toString(),
				toExpectation(workspaceEdit));
	}

	@Test(timeout = 10000)
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
				requestManager.awaitReadEntered(2, TimeUnit.SECONDS));
		CompletableFuture<Object> writeResult = requestManager.<Object, Object>runWrite(() -> null,
				(cancelIndicator, it) -> null);
		requestManager.continueRead();

		WorkspaceEdit workspaceEdit = renameResult.get(5, TimeUnit.SECONDS);
		String expectation =
				"changes :\n" +
				"documentChanges : \n" +
				"    Foo.fileawaretestlanguage <1> : Bar [[2, 8] .. [2, 11]]\n" +
				"    Bar [[3, 5] .. [3, 8]]\n";
		assertEquals(expectation.toString(), toExpectation(workspaceEdit));
		writeResult.get(5, TimeUnit.SECONDS);
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
			languageServer.getRequestManager().shutdown();
		}
	}
}
