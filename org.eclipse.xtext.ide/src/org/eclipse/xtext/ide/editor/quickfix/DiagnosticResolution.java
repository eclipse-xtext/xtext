/*******************************************************************************
 * Copyright (c) 2020, 2022 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.quickfix;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticModificationContext.Factory;
import org.eclipse.xtext.ide.serializer.IChangeSerializer;
import org.eclipse.xtext.ide.serializer.IChangeSerializer.IModification;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.TextEditAcceptor;
import org.eclipse.xtext.ide.server.UriExtensions;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;
import org.eclipse.xtext.ide.server.rename.ChangeConverter2;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;

/**
 * @author Heinrich Weichert
 *
 * @since 2.24
 */
public class DiagnosticResolution {

	private String label;

	private ISemanticModification modification;

	private final ITextModification textModification;

	private Factory factory;

	private int relevance;

	private URI uri;

	private Document document;

	private ILanguageServerAccess access;

	private Diagnostic diagnostic;

	private final UriExtensions uriExtensions = new UriExtensions();

	@Deprecated(forRemoval = true, since = "2.27")
	public DiagnosticResolution(String label, Factory modificationContextFactory, IModification<EObject> modification) {
		this(label, modificationContextFactory, (diagnostic, object) -> modification, 0);
	}

	@Deprecated(forRemoval = true, since = "2.27")
	public DiagnosticResolution(String label, Factory modificationContextFactory, IModification<EObject> modification,
			int relevance) {
		this(label, modificationContextFactory, (diagnostic, object) -> modification, relevance);
	}

	/**
	 * @since 2.27
	 */
	public DiagnosticResolution(String label, Factory modificationContextFactory, ITextModification textModification) {
		this(label, modificationContextFactory, textModification, 0);
	}

	/**
	 * @since 2.27
	 */
	public DiagnosticResolution(String label, Factory modificationContextFactory, ITextModification textModification,
			int relevance) {
		this.label = label;
		this.factory = modificationContextFactory;
		this.modification = null;
		this.textModification = textModification;
		this.relevance = relevance;
	}

	/**
	 * @since 2.27
	 */
	public DiagnosticResolution(String label, Factory modificationContextFactory, ISemanticModification modification) {
		this(label, modificationContextFactory, modification, 0);
	}

	/**
	 * @since 2.27
	 */
	public DiagnosticResolution(String label, Factory modificationContext, ISemanticModification modification,
			int relevance) {
		this.label = label;
		this.factory = modificationContext;
		this.modification = modification;
		this.textModification = null;
		this.relevance = relevance;
	}

	public int getRelevance() {
		return relevance;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * @since 2.44
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @since 2.44
	 */
	public WorkspaceEdit apply() {
		return access.doSyncRead(uriExtensions.toUriString(uri), context -> {
			XtextResource resource = (XtextResource) context.getResource();
			EObjectAtOffsetHelper helper = resource.getResourceServiceProvider().get(EObjectAtOffsetHelper.class);
			WorkspaceEdit edit = new WorkspaceEdit();
			if (helper == null) {
				return edit;
			}
			int offset = document.getOffSet(diagnostic.getRange().getStart());
			EObject obj = helper.resolveContainedElementAt(resource, offset);
			if (obj == null) {
				return edit;
			}
			if (modification != null) {
				DiagnosticModificationContext modificationContext = factory.createModificationContext();
				ChangeConverter2 changeConverter = modificationContext.getConverterFactory().create(edit, access);
				IChangeSerializer serializer = modificationContext.getSerializer();
				serializer.addModification(obj, modification.apply(diagnostic, obj));
				serializer.applyModifications(changeConverter);
			} else {
				TextEditAcceptor textEditAcceptor = new TextEditAcceptor(edit, access);
				textEditAcceptor.accept(uri.toString(), document, textModification.apply(diagnostic, obj, document));
			}
			return edit;
		});
	}

	void configure(Options options, Diagnostic diagnostic) {
		this.uri = options.getResource().getURI();
		this.document = options.getDocument();
		this.access = options.getLanguageServerAccess();
		this.diagnostic = diagnostic;
	}
}
