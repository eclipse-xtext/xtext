/**
 * Copyright (c) 2026 TypeFox GmbH (http://www.typefox.io) and others.
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

import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

import com.google.inject.Inject;

public class InterleavingRequestManager extends RequestManager {
	private final AtomicBoolean gateNextRead = new AtomicBoolean();
	private final CountDownLatch readEntered = new CountDownLatch(1);
	private final CountDownLatch continueRead = new CountDownLatch(1);

	@Inject
	public InterleavingRequestManager(ExecutorService parallel, OperationCanceledManager operationCanceledManager) {
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
					if (!continueRead.await(10, TimeUnit.SECONDS)) {
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
