/*******************************************************************************
 * Copyright (c) 2010, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.builder.impl;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.eclipse.core.internal.events.BuildManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Knut Wannheden - Initial contribution and API
 */
@SuppressWarnings("restriction")
public class BuildCancellationTest extends AbstractParticipatingBuilderTest {

	private OperationCanceledException cancelException;
	private boolean isExternalInterrupt;
	
	@Override
	@Before
	public void startLogging() {
		super.startLogging();
	}

	/** see https://bugs.eclipse.org/bugs/show_bug.cgi?id=325814 */
	@Test 
	public void testCancellationTriggersFullBuild() throws Exception {
		IJavaProject project = createJavaProject("foo");
		addNature(project.getProject(), XtextProjectHelper.NATURE_ID);
		IFolder folder = project.getProject().getFolder("src");
		IFile file = folder.getFile("Foo" + F_EXT);
		file.create(new StringInputStream("object Foo"), true, monitor());
		build();
		reset();
		cancel(false);
		try {
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor());
			fail("Expected OperationCanceledException");
		} catch (OperationCanceledException e) {
			assertSame(cancelException, e);
		} catch (Exception e) {
			fail("Expected OperationCanceledException");
		}
		reset();
		ResourcesPlugin.getWorkspace().build(
				IncrementalProjectBuilder.AUTO_BUILD, monitor());
		build();
		assertEquals(1, getInvocationCount());
		assertSame(BuildType.FULL, getContext().getBuildType());
		reset();
	}
	
	/**
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=455022  
	 */
	@Test
	public void testInterruptionTriggersIncrementalBuild() throws Exception {
		IJavaProject project = createJavaProject("foo");
		addNature(project.getProject(), XtextProjectHelper.NATURE_ID);
		build();
		cancel(true);
		IFolder folder = project.getProject().getFolder("src");
		try {
			IFile file = folder.getFile("Foo" + F_EXT);
			file.create(new StringInputStream("object Foo"), true, monitor());
			ResourcesPlugin.getWorkspace().build(
					IncrementalProjectBuilder.AUTO_BUILD, monitor());
		} catch (OperationCanceledException e) {
			// thrown by a different builder
			assertNotSame(cancelException, e);
		}
		reset();
		ResourcesPlugin.getWorkspace().build(
				IncrementalProjectBuilder.AUTO_BUILD, monitor());
		assertEquals(1, getInvocationCount());
		assertSame(BuildType.INCREMENTAL, getContext().getBuildType());
		reset();
	}
	
	/**
	 * URIs queued for a project (here: bar.buildertestlanguage, affected by a change in the referenced project foo)
	 * must not get lost if the build of that project is interrupted after it already polled them from the queue.
	 *
	 * @see https://github.com/eclipse-xtext/xtext/issues/3644
	 */
	@Test
	public void testInterruptionKeepsQueuedURIs() throws Exception {
		IProject foo = createProject("foo");
		addNature(foo, XtextProjectHelper.NATURE_ID);
		IProject bar = createProject("bar");
		addNature(bar, XtextProjectHelper.NATURE_ID);
		IFile fooFile = createFile("foo/foo" + F_EXT, "object Foo");
		createFile("bar/bar" + F_EXT, "object Bar references Foo");
		setReference(bar, foo);
		build();
		disableAutobuild(() -> {
			try {
				// building foo queues the affected bar.buildertestlanguage for project bar
				fooFile.setContents(new StringInputStream("object Baz"), true, true, monitor());
				foo.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor());
				// the auto build of bar polls the queued URI and gets interrupted afterwards
				reset();
				cancel(true);
				try {
					ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, monitor());
				} catch (OperationCanceledException e) {
					// may be rethrown by the build manager
				}
				// the next build has to process the queued URI again
				reset();
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.AUTO_BUILD, monitor());
				assertEquals(1, getInvocationCount());
				assertEquals(bar, getContext().getBuiltProject());
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public void build(IBuildContext context, IProgressMonitor monitor) throws CoreException {
		super.build(context, monitor);
		if(isExternalInterrupt) {
			try {
				// simulate a workspace operation that interrupts the auto build like in
				// {@link Workspace#prepareOperation(org.eclipse.core.runtime.jobs.ISchedulingRule, IProgressMonitor)}
				BuildManager buildManager = ((Workspace) ResourcesPlugin.getWorkspace()).getBuildManager();
				Field field0 = buildManager.getClass().getDeclaredField("autoBuildJob");
				field0.setAccessible(true);
				Object autoBuildJob = field0.get(buildManager);
				Field field1 = autoBuildJob.getClass().getDeclaredField("interrupted");
				field1.setAccessible(true);
				field1.set(autoBuildJob, true);
				isExternalInterrupt = false;
			} catch(Exception exc) {
				throw new RuntimeException(exc);
			}
		}
		if(cancelException != null) 
			throw cancelException;
	}
	
	public void cancel(boolean isExternalInterrupt) {
		this.isExternalInterrupt = isExternalInterrupt;
		this.cancelException = new OperationCanceledException();
	}
	
	@Override
	public void reset() {
		super.reset();
		isExternalInterrupt = false;
		cancelException = null;
	}
}
