/*******************************************************************************
 * Copyright (c) 2020, 2022 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.quickfix;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2.Options;

import com.google.common.annotations.Beta;

/**
 * Quickfix for code actions in files.<br>
 * The quickfix itself is applied to a temporary resource.<br>
 * The textual edit can be composed by calling DiagnosticResolution#apply().
 *
 * @author Heinrich Weichert
 *
 * @since 2.24
 * 
 *        Contributors: Rubén Porras Campo (Avaloq Evolution AG) - Add method to get fix methods.
 */
@Beta
public interface IQuickFixProvider {

	/**
	 *
	 * Collects all possible quickfixes for the given issue. If there are none, returns an empty list
	 *
	 * @param options
	 *            Contextual action options
	 * @param diagnostic
	 *            the diagnostic
	 * @return 0..n resolutions for the given issue
	 */
	List<DiagnosticResolution> getResolutions(Options options, Diagnostic diagnostic);

	/**
	 *
	 * If the provider handles (it has code to produce resolutions for) the given diagnostic.
	 *
	 * @param diagnostic
	 *            the diagnostic
	 * @return true if the provider handles the given diagnostic
	 * 
	 * @since 2.28
	 */
	default boolean handlesDiagnostic(Diagnostic diagnostic) {
		return true;
	}

	/**
	 * Resolves the given resolution and returns the resulting workspace edit. If the resolution is unknown, an empty
	 * WorkspaceEdit is returned.
	 * 
	 * @param resolutionId
	 *            Unique resolution retrieved from calling {@link #cacheResolution(DiagnosticResolution)}
	 * 
	 * @since 2.44
	 */
	WorkspaceEdit resolveResolution(DiagnosticResolutionInfo resolutionId);

	/**
	 * Creates a pending diagnostic resolution without applying the workspace edit
	 * 
	 * @see org.eclipse.xtext.ide.server.LanguageServerImpl#resolveCodeAction
	 * 
	 * @param resolution
	 *            A resolution for a quickfix
	 * 
	 * @since 2.44
	 */
	String cacheResolution(DiagnosticResolution resolution);
}
