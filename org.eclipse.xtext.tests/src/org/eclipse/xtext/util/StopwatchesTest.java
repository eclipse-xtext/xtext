/*******************************************************************************
 * Copyright (c) 2026 Eclipse Foundation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.xtext.util.internal.Stopwatches;
import org.eclipse.xtext.util.internal.Stopwatches.NumbersForTask;
import org.junit.Test;

/**
 * Tests for {@link Stopwatches}.
 */
public class StopwatchesTest {

	@Test
	public void testAllNumbersCanBeIteratedWhileTasksAreAdded() throws Exception {
		Stopwatches.setEnabled(true);
		Stopwatches.resetAll();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Stopwatches.forTask("existing-task");
			// Create the iterator before another thread adds a task to the registry.
			Map<String, NumbersForTask> numbers = Stopwatches.allNumbers();
			Iterator<Map.Entry<String, NumbersForTask>> iterator = numbers.entrySet().iterator();
			CountDownLatch addTask = new CountDownLatch(1);
			CountDownLatch taskAdded = new CountDownLatch(1);
			Future<?> future = executor.submit(() -> {
				try {
					addTask.await();
					Stopwatches.forTask("concurrently-added-task");
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				} finally {
					taskAdded.countDown();
				}
			});

			// Wait for the structural change before advancing the iterator.
			addTask.countDown();
			assertTrue("Timed out while adding a stopwatch task", taskAdded.await(10, TimeUnit.SECONDS));
			future.get();

			// A snapshot must retain its original entry without throwing ConcurrentModificationException.
			assertEquals("existing-task", iterator.next().getKey());
			assertFalse(iterator.hasNext());
		} finally {
			executor.shutdownNow();
			Stopwatches.resetAll();
			Stopwatches.setEnabled(false);
		}
	}
}