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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.MarkerTypes;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.hover.AbstractProblemHover;
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

/**
 * @author Christoph Kulla - Initial contribution and API
 */
public class AbstractProblemHoverTest extends AbstractEditorTest {
	
	protected IFile file;

	protected XtextEditor editor;
	
	protected IXtextDocument document;

	protected String modelAsText;
	
	private MockHover hover;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		modelAsText = "stuff mystuff\nstuff yourstuff refs _mystuff// Comment";
		file = IResourcesSetupUtil.createFile("test/test.testlanguage", modelAsText);
		editor = openEditor(file);
		document = editor.getDocument();
		hover = TestsActivator.getInstance().getInjector(getEditorId()).getInstance(MockHover.class);
		hover.setSourceViewer(editor.getInternalSourceViewer());
		List<Issue> issues = document.readOnly(new IUnitOfWork<List<Issue>, XtextResource>() {
			@Override
			public List<Issue> exec(XtextResource state) throws Exception {
				return state.getResourceServiceProvider().getResourceValidator().validate(state, CheckMode.ALL, null);
			}	
		});
		MarkerCreator markerCreator =  TestsActivator.getInstance().getInjector(getEditorId()).getInstance(MarkerCreator.class);
		for (Issue issue : issues) {
			markerCreator.createMarker(issue, file, MarkerTypes.forCheckType(issue.getType()));
		}
		TestsActivator.getInstance().getInjector(getEditorId()).getInstance(SyncUtil.class).waitForReconciler(editor);
		waitForEventProcessing();
	}

	@Override
	protected String getEditorId() {
		return "org.eclipse.xtext.ui.tests.TestLanguage";
	}

	@Test public void testAnnotations() {	
		assertEquals(0, hover.getAnnotations(0,0).size());
		assertEquals(0, hover.getAnnotations(1,34).size());
		assertEquals(1, hover.getAnnotations(1,35).size());
		assertEquals(1, hover.getAnnotations(1,43).size());
		assertEquals(0, hover.getAnnotations(1,44).size());
	}
	
	@Test public void testMultipleAnnotationTypes() throws Exception {
		IMarker warning = file.createMarker(MarkerTypes.NORMAL_VALIDATION);
		warning.setAttribute(IMarker.LOCATION, "line: 2 " + file.getFullPath().toString());
		warning.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		warning.setAttribute(IMarker.CHAR_START, 14);
		warning.setAttribute(IMarker.CHAR_END, 19);
		warning.setAttribute(IMarker.LINE_NUMBER, 2);
		warning.setAttribute(IMarker.MESSAGE, "Foo");
		
		IMarker info = file.createMarker(MarkerTypes.NORMAL_VALIDATION);
		info.setAttribute(IMarker.LOCATION, "line: 2 " + file.getFullPath().toString());
		info.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
		info.setAttribute(IMarker.CHAR_START, 20);
		info.setAttribute(IMarker.CHAR_END, 29);
		info.setAttribute(IMarker.LINE_NUMBER, 2);
		info.setAttribute(IMarker.MESSAGE, "Bar");
		
		List<Annotation> annotations = hover.getAnnotations(1, -1);
		List<Annotation> sorted = hover.sortBySeverity(annotations);
		assertEquals(3, sorted.size());
		// First errors, then warnings, then the rest
		assertEquals("org.eclipse.xtext.ui.editor.error", sorted.get(0).getType());
		assertEquals("org.eclipse.xtext.ui.editor.warning", sorted.get(1).getType());
		assertEquals("org.eclipse.xtext.ui.editor.info", sorted.get(2).getType());
		
		warning.delete();
		info.delete();
	}
	
	@Test public void testAnnotationOnMultipleLines() throws Exception {
		IMarker warning = file.createMarker(MarkerTypes.NORMAL_VALIDATION);
		warning.setAttribute(IMarker.LOCATION, "line: 1 " + file.getFullPath().toString());
		warning.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		warning.setAttribute(IMarker.CHAR_START, 0);
		warning.setAttribute(IMarker.CHAR_END, 43);
		warning.setAttribute(IMarker.LINE_NUMBER, 1);
		warning.setAttribute(IMarker.MESSAGE, "Foo");
		
		List<Annotation> annotations = hover.getAnnotations(1, 40);
		assertEquals(2, annotations.size());
		// The error is on the same line as the requested position, so it should be returned first
		assertEquals("org.eclipse.xtext.ui.editor.error", annotations.get(0).getType());
		assertEquals("org.eclipse.xtext.ui.editor.warning", annotations.get(1).getType());
		
		warning.delete();
	}

	protected void activate(IWorkbenchPart part) {
		editor.getSite().getPage().activate(part);
	}
	
	static class MockHover extends AbstractProblemHover {
		
		@Override
		protected IRegion getHoverRegionInternal(int lineNumber, int offset) {
			return null;
		}

		@Override
		protected Object getHoverInfoInternal(ITextViewer textViewer, int lineNumber, int offset) {
			return null;
		}
		
		/**
		 * Override to make this method accessible from the test.
		 */
		@Override
		public List<Annotation> sortBySeverity(List<Annotation> annotations) {
			return super.sortBySeverity(annotations);
		}

	}
	
}
