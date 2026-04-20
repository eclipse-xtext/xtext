/**
 * Copyright (c) 2019, 2026 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ui.tests.refactoring;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceDescriptor;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;

/**
 * @author Christian Schneider - Initial contribution and API
 */
public class ProgressReportingTest extends AbstractResourceRelocationTest {
	public static class TestProgressMonitor extends NullProgressMonitor {
		private ArrayList<String> events = new ArrayList<>();

		private double accumulatedWork = 0.0d;

		@Override
		public void worked(int work) {
			accumulatedWork = accumulatedWork + work;
			events.add("Worked " + work + " (" + accumulatedWork + ")");
		}

		@Override
		public void internalWorked(double work) {
			accumulatedWork = (accumulatedWork + work);
			events.add("InternalWorked " + work + " (" + accumulatedWork + ")");
		}

		@Override
		public void beginTask(String name, int totalWork) {
			events.add("BeginTask " + name + (StringExtensions.isNullOrEmpty(name) ? "(" : " (") + totalWork + ")");
		}

		@Override
		public void subTask(String name) {
			events.add("SubTask " + name);
		}

		@Override
		public void setTaskName(String name) {
			events.add("SetTaskName " + name);
		}

		@Override
		public void done() {
			events.add("Done");
		}

		@Override
		public void setCanceled(boolean value) {
			super.setCanceled(value);
			events.add("Canceled");
		}

		public void assertLogged(List<String> expectation) {
			String eventsDump = String.join("\n", events);
			String expectedEventsDump = String.join("\n", expectation);
			if (!eventsDump.startsWith(expectedEventsDump)) {
				throw new ComparisonFailure("", expectedEventsDump, eventsDump);
			}
		}

		public void assertLogged(String expectation) {
			String eventsDump = String.join("\n", events);
			assertEquals(expectation, eventsDump);
		}
	}

	@Test
	public void testProgressReportOfRenameCommonDir() throws Exception {
		String model = "package foo.bar\n" + "element X {\n" + "	ref X\n" + "}\n";
		IFile x = file("foo/X.fileawaretestlanguage", model);
		String model2 = "package foo\n" + "element Y {\n" + "	ref bar.X\n" + "}\n";
		IFile y = file("foo/Y.fileawaretestlanguage", model2);
		ProgressReportingTest.TestProgressMonitor monitor = new ProgressReportingTest.TestProgressMonitor();
		performRename(x.getParent(), "baz", monitor);
		Assert.assertFalse(x.exists());
		Assert.assertFalse(y.exists());
		String expected = "BeginTask (1000)\n"
				+ "Worked 90 (90.0)\n"
				+ "Worked 505 (595.0)\n"
				+ "Worked 40 (635.0)\n"
				+ "SetTaskName Preparing and applying file changes...\n"
				+ "Worked 19 (654.0)\n"
				+ "Worked 19 (673.0)\n"
				+ "Worked 9 (682.0)\n"
				+ "Worked 10 (692.0)\n"
				+ "Worked 9 (701.0)\n"
				+ "Worked 10 (711.0)\n"
				+ "SetTaskName Creating text changes...\n"
				+ "Worked 47 (758.0)\n"
				+ "Worked 20 (778.0)\n"
				+ "Worked 19 (797.0)\n"
				+ "Worked 203 (1000.0)\n"
				+ "BeginTask Creating workspace modifications... (1000)\n"
				+ "Worked 333 (1333.0)\n"
				+ "Worked 333 (1666.0)\n"
				+ "Worked 334 (2000.0)\n"
				+ "Done\n"
				+ "BeginTask Performing changes... (1000)\n"
				+ "Worked 41 (2041.0)\n"
				+ "Worked 42 (2083.0)\n"
				+ "Worked 79 (2162.0)\n"
				+ "Worked 4 (2166.0)\n"
				+ "Worked 84 (2250.0)\n"
				+ "Worked 41 (2291.0)\n"
				+ "Worked 42 (2333.0)\n"
				+ "Worked 79 (2412.0)\n"
				+ "Worked 4 (2416.0)\n"
				+ "Worked 84 (2500.0)\n"
				+ "Worked 5 (2505.0)\n"
				+ "Worked 125 (2630.0)\n"
				+ "Worked 25 (2655.0)\n"
				+ "Worked 75 (2730.0)\n"
				+ "Worked 25 (2755.0)\n"
				+ "Worked 120 (2875.0)\n"
				+ "Worked 120 (2995.0)\n"
				+ "Worked 5 (3000.0)\n"
				+ "Done";
		monitor.assertLogged(expected);
	}

	protected void performRename(IResource theResource, String theNewName, IProgressMonitor monitor) throws Exception {
		RenameResourceDescriptor renameResourceDescriptor = new RenameResourceDescriptor();
		renameResourceDescriptor.setResourcePath(theResource.getFullPath());
		renameResourceDescriptor.setNewName(theNewName);
		renameResourceDescriptor.setProject(project.getName());
		performRefactoring(renameResourceDescriptor, monitor);
	}
}
