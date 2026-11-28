/*******************************************************************************
 * Copyright (c) 2011, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.tests.editor.occurrences;

import static org.eclipse.xtext.ui.editor.occurrences.DefaultOccurrenceComputer.*;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.occurrences.IOccurrenceComputer;
import org.eclipse.xtext.ui.editor.occurrences.MarkOccurrenceActionContributor;
import org.eclipse.xtext.ui.editor.occurrences.OccurrenceMarker;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.testing.AbstractEditorTest;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.tests.editor.outline.ui.tests.OutlineTestLanguageUiInjectorProvider;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(OutlineTestLanguageUiInjectorProvider.class)
public class MarkOccurrencesTest extends AbstractEditorTest {

	private static final int TIMEOUT = 10000;

	@Inject
	private IPreferenceStoreAccess preferenceStoreAccess;

	@Inject
	private MarkOccurrenceActionContributor contributor;

	@Inject
	private IOccurrenceComputer occurrenceComputer;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("MarkOccurrencesTest");
		IResourcesSetupUtil.addNature(project.getProject(), XtextProjectHelper.NATURE_ID);
		IResourcesSetupUtil.waitForBuild();
		setMarkOccurrences(false);
	}

	@Override
	public void tearDown() throws Exception {
		setMarkOccurrences(false);
		super.tearDown();
	}

	@Test public void testMarkOccurrences() throws Exception {
		String model = "Foo { Bar(Foo) {} Baz(Foo Bar) {} }";
		IFile modelFile = IResourcesSetupUtil.createFile("MarkOccurrencesTest/src/Test.outlinetestlanguage", model);
		XtextEditor editor = openEditor(modelFile);
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		selectionProvider.setSelection(new TextSelection(0, 1));
		IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		ExpectationBuilder expectations = new ExpectationBuilder(editor, annotationModel).added(DECLARATION_ANNOTATION_TYPE, 0, 3)
				.added(OCCURRENCE_ANNOTATION_TYPE, model.indexOf("Foo", 3), 3)
				.added(OCCURRENCE_ANNOTATION_TYPE, model.lastIndexOf("Foo"), 3);
		setMarkOccurrences(true);
		expectations.verify(TIMEOUT);

		expectations.removed(DECLARATION_ANNOTATION_TYPE, 0, 3)
				.removed(OCCURRENCE_ANNOTATION_TYPE, model.indexOf("Foo", 3), 3)
				.removed(OCCURRENCE_ANNOTATION_TYPE, model.lastIndexOf("Foo"), 3)
				.added(DECLARATION_ANNOTATION_TYPE, model.indexOf("Bar"), 3)
				.added(OCCURRENCE_ANNOTATION_TYPE, model.lastIndexOf("Bar"), 3);
		selectionProvider.setSelection(new TextSelection(model.lastIndexOf("Bar"), 1));
		expectations.verify(TIMEOUT);

		expectations.removed(DECLARATION_ANNOTATION_TYPE, model.indexOf("Bar"), 3).removed(OCCURRENCE_ANNOTATION_TYPE,
				model.lastIndexOf("Bar"), 3);
		setMarkOccurrences(false);
		expectations.verify(TIMEOUT);
	}
	
	@Test public void testMarkOccurrencesCrossFile() throws Exception {
		String model1 = "Zonk { Bar(Foo) {} Baz(Foo Bar) {} }";
		String model2 = "Foo {}";
		IFile modelFile1 = IResourcesSetupUtil.createFile("MarkOccurrencesTest/src/Test1.outlinetestlanguage", model1);
		IResourcesSetupUtil.createFile("MarkOccurrencesTest/src/Test2.outlinetestlanguage", model2);
		IResourcesSetupUtil.waitForBuild();
		XtextEditor editor = openEditor(modelFile1);
		ISelectionProvider selectionProvider = editor.getSelectionProvider();
		selectionProvider.setSelection(new TextSelection(model1.indexOf("Foo"), 1));
		IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
		ExpectationBuilder expectations = new ExpectationBuilder(editor, annotationModel)
				.added(OCCURRENCE_ANNOTATION_TYPE, model1.indexOf("Foo", 3), 3)
				.added(OCCURRENCE_ANNOTATION_TYPE, model1.lastIndexOf("Foo"), 3);
		setMarkOccurrences(true);
		expectations.verify(TIMEOUT);
	}

	protected void setMarkOccurrences(boolean isMarkOccurrences) {
		preferenceStoreAccess.getWritablePreferenceStore().setValue(contributor.getPreferenceKey(), isMarkOccurrences);
	}

	public class ExpectationBuilder {

		private final Set<Triple<String, Integer, Integer>> expected = new LinkedHashSet<>();

		private final IAnnotationModel annotationModel;

		private final XtextEditor editor;

		public ExpectationBuilder(XtextEditor editor, IAnnotationModel annotationModel) {
			this.editor = editor;
			this.annotationModel = annotationModel;
		}

		public ExpectationBuilder added(String type, int offset, int length) {
			assertTrue("Duplicate expected occurrence annotation", expected.add(Tuples.create(type, offset, length)));
			return this;
		}

		public ExpectationBuilder removed(String type, int offset, int length) {
			assertTrue("Expected occurrence annotation was not present", expected.remove(Tuples.create(type, offset, length)));
			return this;
		}

		protected Triple<String, Integer, Integer> tuple(Annotation a, Position position) {
			return Tuples.create(a.getType(), position.getOffset(), position.getLength());
		}

		public void verify(int timeout) throws TimeoutException, InterruptedException {
			OccurrenceMarker occurrenceMarker = contributor.findOccurrenceMarker(editor);
			assertNotNull(occurrenceMarker);
			Display display = Display.getCurrent();
			assertNotNull("Occurrence verification must run on the UI thread", display);
			long deadline = System.nanoTime() + timeout * 1_000_000L;
			while (display.readAndDispatch() && System.nanoTime() < deadline) {
			}
			occurrenceMarker.joinMarkOccurrenceJob(remainingMillis(deadline));
			Set<Triple<String, Integer, Integer>> actual = getOccurrenceAnnotations();
			while (!expected.equals(actual) && System.nanoTime() < deadline) {
				if (!display.readAndDispatch()) {
					Thread.sleep(1);
				}
				actual = getOccurrenceAnnotations();
			}
			assertEquals("Occurrence annotations did not reach the expected state within " + timeout
					+ " ms. Missing: " + difference(expected, actual) + ", unexpected: " + difference(actual, expected),
					expected, actual);
		}

		protected Set<Triple<String, Integer, Integer>> getOccurrenceAnnotations() {
			Set<Triple<String, Integer, Integer>> result = new LinkedHashSet<>();
			for (Iterator<Annotation> i = annotationModel.getAnnotationIterator(); i.hasNext();) {
				Annotation annotation = i.next();
				if (occurrenceComputer.hasAnnotationType(annotation.getType())) {
					Position position = annotationModel.getPosition(annotation);
					Triple<String, Integer, Integer> tuple = tuple(annotation, position);
					assertTrue("Duplicate actual occurrence annotation: " + tuple, result.add(tuple));
				}
			}
			return result;
		}

		private int remainingMillis(long deadline) {
			return Math.max(0, (int) Math.ceil((deadline - System.nanoTime()) / 1_000_000.0));
		}

		private Set<Triple<String, Integer, Integer>> difference(Set<Triple<String, Integer, Integer>> minuend,
				Set<Triple<String, Integer, Integer>> subtrahend) {
			Set<Triple<String, Integer, Integer>> result = new LinkedHashSet<>(minuend);
			result.removeAll(subtrahend);
			return result;
		}
	}
}
