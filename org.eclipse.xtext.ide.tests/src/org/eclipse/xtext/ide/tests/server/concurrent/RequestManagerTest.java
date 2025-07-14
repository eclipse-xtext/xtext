/**
 * Copyright (c) 2016, 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.server.concurrent;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Level;
import org.eclipse.xtext.ide.server.ServerModule;
import org.eclipse.xtext.ide.server.concurrent.AbstractRequest;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.ide.server.concurrent.ReadRequest;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.ide.server.concurrent.WriteRequest;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.testing.RepeatedTest;
import org.eclipse.xtext.testing.logging.LoggingTester;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author kosyakov - Initial contribution and API
 */
public class RequestManagerTest {
	@Rule
	public RepeatedTest.Rule rule = new RepeatedTest.Rule(false);

	@Inject
	private IRequestManager requestManager;

	@Inject
	private Provider<ExecutorService> executorServiceProvider;

	@Inject
	private Provider<OperationCanceledManager> cancelManagerProvider;

	private AtomicInteger sharedState;

	@Before
	public void setUp() {
		sharedState = new AtomicInteger();
		Guice.createInjector(new ServerModule()).injectMembers(this);
	}

	@After
	public void tearDown() {
		requestManager.shutdown();
		sharedState = null;
	}

	@Test(timeout = 1000)
	public void testRunWriteLogExceptionNonCancellable() throws Exception {
		LoggingTester.LogCapture logResult = LoggingTester.captureLogging(Level.ALL, WriteRequest.class, () -> {
			CompletableFuture<Object> future = requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
				throw new RuntimeException();
			});
			try {
				future.join();
			} catch (Exception e) {
			}
		});
		logResult.assertLogEntry("Error during request:");
	}

	@Test(timeout = 1000)
	public void testRunWriteLogExceptionCancellable() throws Exception {
		LoggingTester.LogCapture logResult = LoggingTester.captureLogging(Level.ALL, WriteRequest.class, () -> {
			CompletableFuture<Object> future = requestManager.runWrite(() -> {
				throw new RuntimeException();
			}, (ci, o) -> null);
			try {
				future.join();
			} catch (Exception e) {
			}
		});
		logResult.assertLogEntry("Error during request:");
	}

	@Test(timeout = 1000, expected = ExecutionException.class)
	public void testRunWriteCatchException() throws Exception {
		LoggingTester.captureLogging(Level.ALL, WriteRequest.class, () -> {
			try {
				CompletableFuture<Object> future = requestManager.runWrite(() -> {
					throw new RuntimeException();
				}, (ci, o) -> null);
				Assert.assertEquals("Foo", future.get());
			} catch (Throwable t) {
				throw Exceptions.sneakyThrow(t);
			}
		});
		Assert.fail("unreachable");
	}

	@Test(timeout = 1000)
	public void testRunReadLogException() throws Exception {
		LoggingTester.LogCapture logResult = LoggingTester.captureLogging(Level.ALL, ReadRequest.class, () -> {
			CompletableFuture<Object> future = requestManager.runRead((CancelIndicator it) -> {
				throw new RuntimeException();
			});
			try {
				future.join();
			} catch (Exception e) {
			}
		});
		logResult.assertLogEntry("Error during request:");
	}

	@Test(timeout = 1000, expected = ExecutionException.class)
	public void testRunReadCatchException() throws Exception {
		LoggingTester.captureLogging(Level.ALL, ReadRequest.class, () -> {
			try {
				CompletableFuture<Object> future = requestManager.runRead((CancelIndicator it) -> {
					throw new RuntimeException();
				});
				Assert.assertEquals("Foo", future.get());
			} catch (Throwable e) {
				throw Exceptions.sneakyThrow(e);
			}
		});
		Assert.fail();
	}

	@Test
	public void testWriteWaitsForReadToFinish_01() throws Exception {
		AtomicBoolean marked = new AtomicBoolean(false);
		CountDownLatch countDownInRead = new CountDownLatch(1);
		CountDownLatch countDownInWrite = new CountDownLatch(1);
		CountDownLatch proceedWithWrite = new CountDownLatch(1);
		CompletableFuture<Object> reader = requestManager.runRead((CancelIndicator cancelIndicator) -> {
			countDownInRead.countDown();
			Uninterruptibles.awaitUninterruptibly(countDownInWrite);
			marked.set(true);
			proceedWithWrite.countDown();
			return null;
		});
		Uninterruptibles.awaitUninterruptibly(countDownInRead);
		CompletableFuture<Object> writer = requestManager.runWrite(() -> null,
				(CancelIndicator cancelIndicator, Object ignored) -> {
					countDownInWrite.countDown();
					Uninterruptibles.awaitUninterruptibly(proceedWithWrite);
					Assert.assertTrue(marked.get());
					return null;
				});
		try {
			Uninterruptibles.getUninterruptibly(writer, 100, TimeUnit.MILLISECONDS);
			Assert.fail("Expected timeout");
		} catch (TimeoutException e) {
			Assert.assertFalse(marked.get());
		}
		countDownInWrite.countDown();
		Uninterruptibles.getUninterruptibly(writer, 100, TimeUnit.MILLISECONDS);
		Assert.assertTrue(reader.isDone());
		Assert.assertFalse(reader.isCancelled());
	}

	@Test
	public void testWriteWaitsForReadToFinish_02() throws Exception {
		AtomicBoolean marked = new AtomicBoolean(false);
		CountDownLatch countDownInRead = new CountDownLatch(1);
		CountDownLatch countDownInWrite = new CountDownLatch(1);
		CountDownLatch proceedWithWrite = new CountDownLatch(1);
		CompletableFuture<Object> reader = requestManager.runRead((CancelIndicator cancelIndicator) -> {
			countDownInRead.countDown();
			Uninterruptibles.awaitUninterruptibly(countDownInWrite);
			marked.set(true);
			proceedWithWrite.countDown();
			throw new CancellationException();
		});
		Uninterruptibles.awaitUninterruptibly(countDownInRead);
		CompletableFuture<Object> writer = requestManager.runWrite(() -> null,
				(CancelIndicator cancelIndicator, Object ignored) -> {
					countDownInWrite.countDown();
					Uninterruptibles.awaitUninterruptibly(proceedWithWrite);
					Assert.assertTrue(marked.get());
					return null;
				});
		try {
			Uninterruptibles.<Object>getUninterruptibly(writer, 100, TimeUnit.MILLISECONDS);
			Assert.fail("Expected timeout");
		} catch (TimeoutException e) {
			Assert.assertFalse(marked.get());
		}
		countDownInWrite.countDown();
		Uninterruptibles.getUninterruptibly(writer, 100, TimeUnit.MILLISECONDS);
		Assert.assertTrue(reader.isDone());
		Assert.assertTrue(reader.isCancelled());
	}

	@Test(timeout = 1000)
	public void testRunRead() throws Exception {
		CompletableFuture<String> future = requestManager.runRead((CancelIndicator it) -> {
			return "Foo";
		});
		Assert.assertEquals("Foo", future.get());
	}

	@Test(timeout = 1000)
	public void testRunReadConcurrent() throws Exception {
		CompletableFuture<Integer> future = requestManager.runRead((CancelIndicator it) -> {
			try {
				while (sharedState.get() == 0) {
					Thread.sleep(10);
				}
				return sharedState.incrementAndGet();
			} catch (InterruptedException e) {
				throw Exceptions.sneakyThrow(e);
			}
		});
		requestManager.runRead((CancelIndicator it) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		});
		future.join();
		Assert.assertEquals(2, sharedState.get());
	}

	@Test(timeout = 1000)
	public void testRunReadAfterWrite() throws Exception {
		requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		});
		CompletableFuture<Integer> future = requestManager.runRead((CancelIndicator it) -> {
			return Integer.valueOf(sharedState.get());
		});
		Assert.assertEquals(1, (future.get()).intValue());
	}

	@Test(timeout = 1000)
	public void testRunWrite() throws Exception {
		requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		}).join();
		Assert.assertEquals(1, sharedState.get());
	}

	@Test(timeout = 1000)
	public void testRunWriteAfterWrite() throws Exception {
		requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		}).join();
		requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			if (sharedState.get() != 0) {
				return sharedState.incrementAndGet();
			}
			return null;
		}).join();
		Assert.assertEquals(2, sharedState.get());
	}

	@Test(timeout = 1000)
	@RepeatedTest(times = 50)
	public void testRunWriteAfterReadStarted() throws Exception {
		CountDownLatch readStarted = new CountDownLatch(1);
		requestManager.runRead((CancelIndicator it) -> {
			readStarted.countDown();
			return sharedState.incrementAndGet();
		});
		Uninterruptibles.awaitUninterruptibly(readStarted);
		requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			Assert.assertEquals(1, sharedState.get());
			return sharedState.incrementAndGet();
		}).join();
		Assert.assertEquals(2, sharedState.get());
	}

	@Test(timeout = 1000)
	@RepeatedTest(times = 50)
	public void testRunWriteBeforeReadStarted() throws Exception {
		CountDownLatch writeSubmitted = new CountDownLatch(1);
		AtomicBoolean firstWriteDone = new AtomicBoolean();
		requestManager.runWrite(() -> {
			Uninterruptibles.awaitUninterruptibly(writeSubmitted);
			firstWriteDone.set(true);
			return null;
		}, (CancelIndicator $0, Object $1) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		});
		requestManager.runRead((CancelIndicator it) -> {
			return Integer.valueOf(sharedState.incrementAndGet());
		});
		CompletableFuture<Integer> joinMe = requestManager.runWrite(() -> null, (CancelIndicator $0, Object $1) -> {
			Assert.assertEquals(0, sharedState.get());
			Assert.assertTrue(firstWriteDone.get());
			return sharedState.incrementAndGet();
		});
		writeSubmitted.countDown();
		joinMe.join();
		Assert.assertTrue(firstWriteDone.get());
		Assert.assertEquals(1, sharedState.get());
	}

	@Test(timeout = 1000)
	public void testCancelRead() throws Exception {
		AtomicBoolean isCanceled = new AtomicBoolean(false);
		CompletableFuture<Object> future = requestManager.runRead((CancelIndicator cancelIndicator) -> {
			try {
				try {
					sharedState.incrementAndGet();
					while ((!cancelIndicator.isCanceled())) {
						Thread.sleep(10);
					}
				} finally {
					isCanceled.set(true);
				}
				return null;
			} catch (Throwable t) {
				throw Exceptions.sneakyThrow(t);
			}
		});
		while (sharedState.get() == 0) {
			Thread.sleep(10);
		}
		future.cancel(true);
		while (!isCanceled.get()) {
			Thread.sleep(10);
		}
	}

	/**
	 * The tests assumes an implementation of a Command that has access to the
	 * request manager
	 */
	@Test(timeout = 5000)
	@RepeatedTest(times = 50)
	public void testReadCommandSubmitsWriteCommand() throws Exception {
		Thread mainThread = Thread.currentThread();
		CountDownLatch submittedFromMain = new CountDownLatch(1);
		CountDownLatch addedFromReader = new CountDownLatch(1);
		AtomicReference<Thread> readerThreadRef = new AtomicReference<Thread>();
		RequestManager myRequestManager = new RequestManager(executorServiceProvider.get(),
				cancelManagerProvider.get()) {
			@Override
			protected void addRequest(AbstractRequest<?> request) {
				if (((request instanceof WriteRequest)
						&& Objects.equals(Thread.currentThread(), readerThreadRef.get()))) {
					super.addRequest(request);
					addedFromReader.countDown();
					Uninterruptibles.awaitUninterruptibly(submittedFromMain, 100, TimeUnit.MILLISECONDS);
				} else {
					super.addRequest(request);
				}
			}

			@Override
			protected void submitRequest(AbstractRequest<?> request) {
				if (((request instanceof WriteRequest) && Objects.equals(Thread.currentThread(), mainThread))) {
					super.submitRequest(request);
					submittedFromMain.countDown();
				} else {
					super.submitRequest(request);
				}
			}

			@Override
			protected CompletableFuture<Void> cancel() {
				if (Objects.equals(Thread.currentThread(), mainThread)) {
					Uninterruptibles.awaitUninterruptibly(addedFromReader, 100, TimeUnit.MILLISECONDS);
				}
				return super.cancel();
			}
		};
		CountDownLatch threadSet = new CountDownLatch(1);
		CompletableFuture<CompletableFuture<Object>> readResult = myRequestManager.runRead((CancelIndicator it) -> {
			readerThreadRef.set(Thread.currentThread());
			threadSet.countDown();
			return myRequestManager.runWrite(() -> null, (ci, o) -> null);
		});
		Uninterruptibles.awaitUninterruptibly(threadSet);
		Assert.assertNotNull(readerThreadRef.get());
		CompletableFuture<Object> writeResult = myRequestManager.runWrite(() -> null, (ci, o) -> null);
		CompletableFuture<Object> writeFromReader = readResult.get();
		try {
			writeFromReader.get();
			try {
				writeResult.get();
			} catch (CancellationException e) {
				Assert.assertTrue(writeFromReader.isDone());
				Assert.assertTrue(writeResult.isDone());
				Assert.assertTrue(writeFromReader.isCancelled() != writeResult.isCancelled());
			}
		} catch (CancellationException e) {
			writeResult.get();
			Assert.assertTrue(writeFromReader.isDone());
			Assert.assertTrue(writeResult.isDone());
			Assert.assertTrue(writeFromReader.isCancelled() != writeResult.isCancelled());
		}
	}
}
