/**
 * Copyright (c) 2019, 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.server.rename;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.formatting2.regionaccess.ITextReplacement;
import org.eclipse.xtext.ide.serializer.IEmfResourceChange;
import org.eclipse.xtext.ide.serializer.ITextDocumentChange;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.TextEditAcceptor;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.Exceptions;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author koehnlein - Initial contribution and API
 * @since 2.18
 */
public class ChangeConverter2 implements IAcceptor<IEmfResourceChange> {
	public static class Factory {
		@Inject
		protected IResourceServiceProvider.Registry registry;

		@Inject
		protected UriExtensions uriExtensions;

		/**
		 * @since 2.18
		 */
		public ChangeConverter2 create(WorkspaceEdit edit, ILanguageServerAccess access) {
			return new ChangeConverter2(registry, edit, access, uriExtensions);
		}
	}

	private IResourceServiceProvider.Registry registry;

	private UriExtensions uriExtensions;

	private ILanguageServerAccess access;

	private TextEditAcceptor textEditAcceptor;

	protected ChangeConverter2(IResourceServiceProvider.Registry registry, WorkspaceEdit edit,
			ILanguageServerAccess access, UriExtensions uriExtensions) {
		this.registry = registry;
		this.uriExtensions = uriExtensions;
		this.access = access;
		this.textEditAcceptor = new TextEditAcceptor(edit, access);
	}

	@Override
	public void accept(IEmfResourceChange change) {
		handleReplacements(change);
	}

	protected void _handleReplacements(IEmfResourceChange change) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			String uri = uriExtensions.toUriString(change.getResource().getURI());
			change.getResource().save(outputStream, null);
			String newContent = new String(outputStream.toByteArray(), getCharset(change.getResource()));
			access.doSyncRead(uri, (ILanguageServerAccess.Context context) -> {
				Document document = context.getDocument();
				Range range = new Range(document.getPosition(0), document.getPosition(document.getContents().length()));
				TextEdit textEdit = new TextEdit(range, newContent);
				addTextEdit(uri, document, textEdit);
				return null;
			});
		} catch (IOException e) {
			throw Exceptions.sneakyThrow(e);
		}
	}

	protected String getCharset(Resource resource) {
		IEncodingProvider xtextEncodingProvider = registry.getResourceServiceProvider(resource.getURI())
				.get(IEncodingProvider.class);
		if (xtextEncodingProvider != null) {
			return xtextEncodingProvider.getEncoding(resource.getURI());
		} else {
			if (resource instanceof XMLResource) {
				return ((XMLResource) resource).getEncoding();
			} else {
				return Charset.defaultCharset().toString();
			}
		}
	}

	protected void _handleReplacements(ITextDocumentChange change) {
		if (change.getReplacements().size() > 0) {
			String uri = uriExtensions.toUriString(change.getNewURI());
			access.doSyncRead(uri, (ILanguageServerAccess.Context context) -> {
				Document document = context.getDocument();
				List<TextEdit> textEdits = Lists.transform(change.getReplacements(),
						(ITextReplacement replacement) -> {
							Position start = document.getPosition(replacement.getOffset());
							Position end = document.getPosition(replacement.getEndOffset());
							Range range = new Range(start, end);
							return new TextEdit(range, replacement.getReplacementText());
						});
				addTextEdit(uri, document, textEdits.toArray(new TextEdit[textEdits.size()]));
				return null;
			});
		}
	}

	protected void addTextEdit(String theUri, Document document, TextEdit... textEdits) {
		textEditAcceptor.accept(theUri, document, Arrays.asList(textEdits));
	}

	protected void handleReplacements(IEmfResourceChange change) {
		if (change instanceof ITextDocumentChange) {
			_handleReplacements((ITextDocumentChange) change);
			return;
		} else if (change != null) {
			_handleReplacements(change);
			return;
		} else {
			throw new IllegalArgumentException(
					"Unhandled parameter types: " + Arrays.<Object>asList(change).toString());
		}
	}
}
