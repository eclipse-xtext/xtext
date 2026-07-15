/*******************************************************************************
 * Copyright (c) 2026 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.quickfix;

/**
 * Contains information about the diagnostic resolution that can be applied by calling
 * {@link IQuickFixProvider#resolveResolution(String)}
 * 
 * @since 2.44
 */
public class DiagnosticResolutionInfo {

	private String resolutionId;
	private String documentUri;

	public String getResolutionId() {
		return resolutionId;
	}

	public void setResolutionId(String resolutionId) {
		this.resolutionId = resolutionId;
	}
	
	public String getDocumentUri() {
		return documentUri;
	}

	public void setDocumentUri(String documentUri) {
		this.documentUri = documentUri;
	}

}