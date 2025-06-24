/*******************************************************************************
 * Copyright (c) 2016, 2019 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server.concurrent;

import java.util.concurrent.CompletableFuture;

import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Functions.Function2;

/**
 * @author pcr - Initial contribution and API
 * @since 2.40
 */
public interface IRequestManager {

	/**
	 * Run the given cancellable logic as a read request.
	 */
	<V> CompletableFuture<V> runRead(Function1<? super CancelIndicator, ? extends V> cancellable);

	/**
	 * Perform the given write and run the cancellable logic afterwards.
	 */
	<U, V> CompletableFuture<V> runWrite(Function0<? extends U> nonCancellable,
			Function2<? super CancelIndicator, ? super U, ? extends V> cancellable);

	/**
	 * Check if the given throwable is an indicator for a cancellation.
	 */
	boolean isCancelException(Throwable t);
}
