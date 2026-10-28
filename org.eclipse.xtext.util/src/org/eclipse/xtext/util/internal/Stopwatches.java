/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util.internal;

import static com.google.common.collect.Maps.*;
import static java.util.Collections.*;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class Stopwatches {
	
	public interface StoppedTask {
		public void start();
		public void stop();
	}
	
	public static class StopwatchForTask implements StoppedTask {
		final static long notRunning = -1;
		
		private NumbersForTask numbers;
		private long lastStart = notRunning;
		private int reentrant = 0;
		
		StopwatchForTask(NumbersForTask numbers) {
			this.numbers = numbers;
		}
		
		@Override
		public void start() {
			if (!isRunning())
				lastStart = System.currentTimeMillis();
			reentrant++;
		}
		
		public boolean isRunning() {
			return lastStart != notRunning;
		}
		
		@Override
		public void stop() {
			if (isRunning()) {
				reentrant--;
				if (reentrant == 0) {
					long currentTimeMillis = System.currentTimeMillis();
					numbers.addMilliseconds(currentTimeMillis - lastStart);
					numbers.increaseMeasurements();
					lastStart = notRunning;
				}
			}
		}
	}
	
	public static class NumbersForTask {
		long milliseconds = 0;
		int numberOfMeasurements = 0;
		
		public long getMilliseconds() {
			return milliseconds;
		}
		
		synchronized void addMilliseconds(long milliseconds) {
			this.milliseconds += milliseconds;
		}
		
		public int getNumberOfMeasurements() {
			return numberOfMeasurements;
		}
		
		synchronized void increaseMeasurements() {
			this.numberOfMeasurements++;
		}
	}

	private static Map<String, NumbersForTask> data = newLinkedHashMap();
	private static boolean enabled = false;
	private static StoppedTask NULLIMPL = new StoppedTask() {
		@Override
		public void start() {}
		@Override
		public void stop() {}
	};
	
	public static StoppedTask forTask(String task) {
		if (!enabled)
			return NULLIMPL;
		synchronized (data) {
			NumbersForTask numbers = data.get(task);
			if (numbers == null) {
				numbers = new NumbersForTask();
				data.put(task, numbers);
			}
			return new StopwatchForTask(numbers);
		}
	}
	
	public static void setEnabled(boolean isEnabled) {
		enabled = isEnabled;
	}
	
	public static void resetAll() {
		synchronized (data) {
			data.clear();
		}
	}
	
	public static Map<String, NumbersForTask> allNumbers() {
		synchronized (data) {
			return unmodifiableMap(newLinkedHashMap(data));
		}
	}
	
	public static String getPrintableStopwatchData() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("-------------------------------------------------------------------------------------------------------------------------\n");
		for (Entry<String, NumbersForTask> task : Stopwatches.allNumbers().entrySet()) {
			sb.append("Task '"+task.getKey()+"' took "+task.getValue().getMilliseconds()+"ms ("+task.getValue().getNumberOfMeasurements()+" measurements).\n");
		}
		sb.append("-------------------------------------------------------------------------------------------------------------------------\n");
		return sb.toString();
	}
	
}
