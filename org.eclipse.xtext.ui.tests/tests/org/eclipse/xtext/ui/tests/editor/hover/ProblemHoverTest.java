/*******************************************************************************
 * Copyright (c) 2010, 2017 Christoph Kulla
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Christoph Kulla - Initial API and implementation
 *******************************************************************************/
package org.eclipse.xtext.ui.tests.editor.hover;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.CreateMarkersOperation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.MarkerTypes;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.hover.ProblemAnnotationHover;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.validation.MarkerCreator;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.ui.testing.AbstractEditorTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.tests.internal.TestsActivator;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.junit.Test;

import com.google.inject.Injector;

/**
 * @author Christoph Kulla - Initial contribution and API
 * @author Holger Schill
 */
public class ProblemHoverTest extends AbstractEditorTest {

	private static final String CUSTOM_MARKER_ID = "org.eclipse.xtext.ui.tests.customMarker";

	private static final String CUSTOM_MARKER_TEST_MESSAGE = "CustomMarkerTest";

	protected XtextEditor editor;
	
	protected IXtextDocument document;

	protected String modelAsText;
	
	private ProblemAnnotationHover hover;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		modelAsText = "stuff mystuff\nstuff yourstuff refs _mystuff stuff hisstuff refs _yourstuff// Comment";
		IFile file = IResourcesSetupUtil.createFile("test/test.testlanguage", modelAsText);
		IResourcesSetupUtil.waitForBuild();
		editor = openEditor(file);
		document = editor.getDocument();
		Injector injector = TestsActivator.getInstance().getInjector(getEditorId());
		injector.getInstance(SyncUtil.class).waitForReconciler(editor);
		hover = injector.getInstance(ProblemAnnotationHover.class);
		hover.setSourceViewer(editor.getInternalSourceViewer());
		List<Issue> issues = document.readOnly(new IUnitOfWork<List<Issue>, XtextResource>() {
			@Override
			public List<Issue> exec(XtextResource state) throws Exception {
				return state.getResourceServiceProvider().getResourceValidator().validate(state, CheckMode.ALL, null);
			}
		});
		MarkerCreator markerCreator =  injector.getInstance(MarkerCreator.class);
		for (Issue issue : issues) {
			markerCreator.createMarker(issue, file, MarkerTypes.forCheckType(issue.getType()));
		}
		waitForAnnotations(1, issues.size());
	}

	@Override
	protected String getEditorId() {
		return "org.eclipse.xtext.ui.tests.TestLanguage";
	}

	@SuppressWarnings("deprecation")
	@Test public void testAnnotations () {	
		assertNull(hover.getHoverInfo(editor.getInternalSourceViewer(), new Region(34, 1)));
		assertEquals("Couldn't resolve reference to Stuff '_mystuff'.", hover.getHoverInfo(editor.getInternalSourceViewer(), new Region(35, 7)));
		
		//The order of annotations in the annotation model is not stable
		final String hoverInfo = hover.getHoverInfo(editor.getInternalSourceViewer(), 1);
		String expected1 = "Multiple markers at this line\n";
		String expected2 = "- Couldn't resolve reference to Stuff '_mystuff'.";
		String expected3 = "- Couldn't resolve reference to Stuff '_yourstuff'.";
		assertHoverInfoStartsWith(hoverInfo, expected1);
		assertHoverInfoContains(hoverInfo, expected2);
		assertHoverInfoContains(hoverInfo, expected3);
	}

	@Test public void testBug357516_warning() throws Exception {
		IResource resource = editor.getResource();
		createCustomMarkerOnResource(resource, IMarker.SEVERITY_WARNING);
		String hoverInfo = hover.getHoverInfo(editor.getInternalSourceViewer(), 0);
		assertNotNull(hoverInfo);
		assertHoverInfoContains(hoverInfo, CUSTOM_MARKER_TEST_MESSAGE);
	}

	@Test public void testBug357516_error() throws Exception {
		IResource resource = editor.getResource();
		createCustomMarkerOnResource(resource, IMarker.SEVERITY_ERROR);
		String hoverInfo = hover.getHoverInfo(editor.getInternalSourceViewer(), 0);
		assertNotNull(hoverInfo);
		assertHoverInfoContains(hoverInfo, CUSTOM_MARKER_TEST_MESSAGE);
	}

	@Test public void testBug357516_info() throws Exception {
		IResource resource = editor.getResource();
		createCustomMarkerOnResource(resource, IMarker.SEVERITY_INFO);
		String hoverInfo = hover.getHoverInfo(editor.getInternalSourceViewer(), 0);
		assertNotNull(hoverInfo);
		assertEquals(CUSTOM_MARKER_TEST_MESSAGE, hoverInfo);
	}

	@Test public void testBug357516_bookmark() throws Exception {
		IResource resource = editor.getResource();
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.MESSAGE, CUSTOM_MARKER_TEST_MESSAGE);
		attributes.put(IMarker.LINE_NUMBER, 1);
		attributes.put(IMarker.LOCATION, resource.getFullPath().toPortableString());
		IUndoableOperation operation= new CreateMarkersOperation(IMarker.BOOKMARK, attributes, resource, CUSTOM_MARKER_TEST_MESSAGE);
		IOperationHistory operationHistory= PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
		try {
			operationHistory.execute(operation, null, null);
		} catch (ExecutionException x) {
			fail(x.getMessage());
		}
		String hoverInfo = hover.getHoverInfo(editor.getInternalSourceViewer(), 0);
		assertNotNull(hoverInfo);
		assertHoverInfoContains(hoverInfo, CUSTOM_MARKER_TEST_MESSAGE);
	}

	private void createCustomMarkerOnResource(IResource resource, int severenity) throws CoreException{
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(IMarker.MESSAGE, CUSTOM_MARKER_TEST_MESSAGE);
		attributes.put(IMarker.LINE_NUMBER, 1);
		attributes.put(IMarker.LOCATION, resource.getFullPath().toPortableString());
		attributes.put(IMarker.SEVERITY, severenity); 
		MarkerUtilities.createMarker(resource, attributes, CUSTOM_MARKER_ID);
	}


	protected void activate(IWorkbenchPart part) {
		editor.getSite().getPage().activate(part);
	}

	private void assertHoverInfoStartsWith(String hoverInfo, String contains) {
		assertTrue("HOVER INFO: " + hoverInfo,
			hoverInfo.startsWith(contains));
	}

	private void assertHoverInfoContains(String hoverInfo, String contains) {
		assertTrue("HOVER INFO: " + hoverInfo,
			hoverInfo.contains(contains));
	}

	private void waitForAnnotations(int lineNumber, int expectedCount) {
		Display display = Display.getDefault();
		long end = System.currentTimeMillis() + 5000;
		int annotationCount;
		do {
			waitForEventProcessing();
			annotationCount = hover.getAnnotations(lineNumber, -1).size();
			if (annotationCount >= expectedCount) {
				return;
			}
			display.timerExec(10, () -> {
			});
			display.sleep();
		} while (System.currentTimeMillis() < end);
		fail("Expected at least " + expectedCount + " annotations on line " + lineNumber + " but found " + annotationCount);
	}
}
