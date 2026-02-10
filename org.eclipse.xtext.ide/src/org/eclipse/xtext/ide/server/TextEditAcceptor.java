/*******************************************************************************
 * Copyright (c) 2022, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server;

/**
 * @author Rubén Porras Campo - Initial contribution and API, factored out of ChangeConverter2
 *
 * @since 2.27
 */
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.SnippetTextEdit;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class TextEditAcceptor {
	private final boolean useDocumentChanges;
	private final WorkspaceEdit edit;

	public TextEditAcceptor(WorkspaceEdit edit, ILanguageServerAccess access) {
		this.edit = edit;

		Boolean documentChanges = null;
		if (access != null) {
			InitializeParams initializeParams = access.getInitializeParams();
			if (initializeParams != null) {
				ClientCapabilities clientCapabilities = initializeParams.getCapabilities();
				if (clientCapabilities != null) {
					WorkspaceClientCapabilities workspaceClientCapabilities = clientCapabilities.getWorkspace();
					if (workspaceClientCapabilities != null) {
						WorkspaceEditCapabilities workspaceEditCapabilities = workspaceClientCapabilities.getWorkspaceEdit();
						if (workspaceEditCapabilities != null) {
							documentChanges = workspaceEditCapabilities.getDocumentChanges();
						}
					}
				}
			}
		}
		this.useDocumentChanges = documentChanges == Boolean.TRUE;
		if (this.useDocumentChanges) {
			this.edit.setDocumentChanges(new ArrayList<>());
		} else {
			this.edit.setChanges(new LinkedHashMap<>());
		}
	}

	public void accept(String theUri, Document document, List<TextEdit> textEdits) {
		if (useDocumentChanges) {
			TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
			VersionedTextDocumentIdentifier versionedTextDocumentIdentifier = new VersionedTextDocumentIdentifier();
			versionedTextDocumentIdentifier.setUri(theUri);
			versionedTextDocumentIdentifier.setVersion(document.getVersion());
			textDocumentEdit.setTextDocument(versionedTextDocumentIdentifier);
			textDocumentEdit.setEdits(
					textEdits.stream().map(Either::<TextEdit, SnippetTextEdit>forLeft).collect(Collectors.toList()));
			edit.getDocumentChanges().add(Either.forLeft(textDocumentEdit));
		} else {
			edit.getChanges().put(theUri, textEdits);
		}
	}
}