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
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * @author kosyakov - Initial contribution and API
 * @since 2.11
 */
public class ReadRequest<V> extends AbstractRequest<V> {
	private static final Logger LOG = Logger.getLogger(ReadRequest.class);

	private final Function1<? super CancelIndicator, ? extends V> readOperation;

	/**
	 * The initializer future allows to track the running state of this request, e.g. if it was already started or not.
	 */
	private final CompletableFuture<Void> initializer;

	private final ExecutorService executor;

	public ReadRequest(IRequestManager requestManager, Function1<? super CancelIndicator, ? extends V> readOperation,
			ExecutorService executor) {
		super(requestManager);
		this.readOperation = readOperation;
		this.executor = executor;
		this.initializer = new CompletableFuture<>();
		this.initializer.thenRun(this::doRun);
	}
	
	@Override
	protected void cancel(boolean mayInterruptIfRunning) {
		super.cancel(mayInterruptIfRunning);
		if (initializer.cancel(mayInterruptIfRunning)) {
			cancelResult(mayInterruptIfRunning);
		}
	}
	
	@Override
	public void run() {
		initializer.complete(null);
	}

	private void doRun() {
		if (isDone()) {
			return;
		}
		this.executor.submit(() -> {
			try {
				if (isDone()) {
					return;
				}
				cancelIndicator.checkCanceled();
				V readResult = readOperation.apply(cancelIndicator);
				complete(readResult);
			} catch (Throwable t) {
				logAndCompleteExceptionally(t);
			}
		});
	}
	
	@Override
	protected Logger getLogger() {
		return LOG;
	}

}
