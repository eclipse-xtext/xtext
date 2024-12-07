/**
 * Copyright (c) 2016, 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ui.tests.editor;

import static org.junit.Assume.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.ui.testing.AbstractEditorTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class DirtyStateEditorSupportIntegrationTest extends AbstractEditorTest {
	@Inject
	private SyncUtil syncUtil;

	private XtextEditor editor;

	private List<IResourceDescription.Event> events;

	private Display myDisplay;

	private StyledText styledText;

	@Before
	public void setUpEditor() throws Exception {
		IResourceServiceProvider rsp = IResourceServiceProvider.Registry.INSTANCE
				.getResourceServiceProvider(URI.createURI("dummy.testlanguage"));
		rsp.get(Injector.class).injectMembers(this);
		IFile file = IResourcesSetupUtil.createFile("test/foo.testlanguage", "stuff foo");
		editor = openEditor(file);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		editor.getSite().getPage().activate(editor);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		events = new ArrayList<>();
		editor.getDirtyStateEditorSupport().getDirtyStateManager().addListener(e -> events.add(e));
		myDisplay = editor.getSite().getShell().getDisplay();
		myDisplay.getShells()[0].forceActive();
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		styledText = editor.getInternalSourceViewer().getTextWidget();
		styledText.setCaretOffset(9);
		styledText.setFocus();
		syncUtil.waitForReconciler(editor);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		Thread.sleep(20);
		Assert.assertTrue(events.isEmpty());
	}

	/**
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=488380
	 */
	@Test
	public void testSaveAndEdit() throws Exception {
		pushKey('a', 0);
		Assert.assertEquals(1, events.size());
		Assert.assertEquals("fooa", getFirstExportedObjectInLastEventDelta().getQualifiedName().getLastSegment());
		editor.doSave(new NullProgressMonitor());
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		Assert.assertEquals(2, events.size());
		Assert.assertNull(Iterables.getLast(events).getDeltas().get(0).getNew());
		pushKey((char) 0, SWT.BS);
		Assert.assertEquals(3, events.size());
		Assert.assertEquals("foo", getFirstExportedObjectInLastEventDelta()
				.getQualifiedName().getLastSegment());
	}

	/**
	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=354123
	 */
	@Test
	public void testUndoRedo() throws Exception {
		pushKey('a', 0);
		Assert.assertEquals(1, events.size());
		Assert.assertEquals("fooa", getFirstExportedObjectInLastEventDelta()
				.getQualifiedName().getLastSegment());
		IUndoManager undoManager = ((XtextSourceViewer) editor.getInternalSourceViewer()).getUndoManager();
		undoManager.undo();
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		syncUtil.waitForReconciler(editor);
		Assert.assertEquals(2, events.size());
		Assert.assertNull(Iterables.getLast(events).getDeltas().get(0).getNew());
		undoManager.redo();
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		syncUtil.waitForReconciler(editor);
		Assert.assertEquals(3, events.size());
		Assert.assertEquals("fooa", getFirstExportedObjectInLastEventDelta()
				.getQualifiedName().getLastSegment());
		Iterables.getLast(events);
	}
	
	/*
	 * @see https://github.com/eclipse-xtext/xtext/issues/2385
	 */
	@Test public void testModifyFileInExternEditor() throws Exception {
		// This test is flaky on mac operating systems maven build.
		assumeFalse(System.getProperty("os.name").startsWith("Mac"));

		IXtextDocument document = editor.getDocument();

		Display.getDefault().readAndDispatch();
		
		assertNotEquals("", document.get());
		assertTrue(editor.getDocumentProvider() instanceof FileDocumentProvider);
		
		FileDocumentProvider fileDocumentProvider = (FileDocumentProvider)editor.getDocumentProvider();
		
		getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()[0].getView(false).setFocus();
		
		File externalEditFile = ((IFileEditorInput)editor.getEditorInput()).getFile().getLocation().toFile();
		try (FileWriter fw = new FileWriter(externalEditFile)) {
			fw.write("");
		}
		((IFileEditorInput)editor.getEditorInput()).getFile().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
		assertTrue(fileDocumentProvider.isSynchronized(editor.getEditorInput()));

		editor.setFocus();
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		
		// This was added to avoid flakyness for testing on mac os
		syncUtil.waitForReconciler(editor);
		
		assertEquals("", document.get());
	}

	/**
	 * This test needs a manual button click.
	 */
	@Ignore
	@Test public void testModifyDirtyFileInExternEditor() throws Exception {
		IXtextDocument document = editor.getDocument();

		Display.getDefault().readAndDispatch();
		
		assertNotEquals(document.get(), "");
		document.set("/* Hello World! */");
		assertEquals("/* Hello World! */", document.get());
		assertTrue(editor.isDirty());
		
		getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences()[0].getView(false).setFocus();
		
		File externalEditFile = ((IFileEditorInput)editor.getEditorInput()).getFile().getLocation().toFile();
		try (FileWriter fw = new FileWriter(externalEditFile)) {
			fw.write("");
		}
		editor.setFocus();
		// Dialog opens and the "Ignore file change" button has to be clicked.
		Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		syncUtil.waitForReconciler(editor);
		assertEquals(document.get(), "/* Hello World! */");
		
		assertTrue(editor.isDirty());
		editor.doSave(new NullProgressMonitor());
		assertFalse(editor.isDirty());
	}
	
	private IEObjectDescription getFirstExportedObjectInLastEventDelta() {
		return Iterables.getFirst(Iterables.getLast(events).getDeltas().get(0).getNew().getExportedObjects(), null);
	}

	protected void pushKey(char c, int k) throws Exception {
		StyledText textWidget = editor.getInternalSourceViewer().getTextWidget();
		String textBefore = editor.getDocument().get();
		Event event = new Event();
		event.type = SWT.KeyDown;
		event.character = c;
		event.keyCode = k;
		event.doit = true;
		if (c == SWT.ESC) {
			event.keyCode = 27;
		}
		textWidget.notifyListeners(SWT.KeyDown, event);
		syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		Event event2 = new Event();
		event2.type = SWT.KeyUp;
		event2.character = c;
		event2.keyCode = k;
		event2.doit = true;
		if (c == SWT.ESC) {
			event2.keyCode = 27;
		}
		textWidget.notifyListeners(SWT.KeyUp, event2);
		int maxTries = 10;
		while (maxTries-- > 0) {
			if (!Objects.equals(editor.getDocument().get(), textBefore)) {
				syncUtil.waitForReconciler(editor);
				return;
			}
			Thread.sleep(10);
			syncUtil.yieldToQueuedDisplayJobs(new NullProgressMonitor());
		}
		Assert.fail("Document didn't change on keystroke");
	}

	@Override
	protected String getEditorId() {
		return "org.eclipse.xtext.ui.tests.TestLanguage";
	}
}
