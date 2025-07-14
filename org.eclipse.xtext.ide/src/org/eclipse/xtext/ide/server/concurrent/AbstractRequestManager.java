/*******************************************************************************
 * Copyright (c) 2025 Avaloq Group AG and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server.concurrent;

import java.util.concurrent.CompletionException;

import org.eclipse.xtext.service.OperationCanceledManager;

/**
 * @author Rubén Porras Campo - Initial Contribution and API
 *
 * @since 2.40
 */
public abstract class AbstractRequestManager implements IRequestManager {

	private final OperationCanceledManager operationCanceledManager;

	public AbstractRequestManager(OperationCanceledManager operationCanceledManager) {
		this.operationCanceledManager = operationCanceledManager;
	}
	
	protected final OperationCanceledManager getOperationCanceledManager() {
		return operationCanceledManager;
	}

	/**
	 * Check if the given throwable is an indicator for a cancellation.
	 */
	protected boolean isCancelException(Throwable t) {
		if (t == null) {
			return false;
		}
		Throwable cause = t;
		if (t instanceof CompletionException) {
			cause = t.getCause();
		}
		return operationCanceledManager.isOperationCanceledException(cause);
	}
}
