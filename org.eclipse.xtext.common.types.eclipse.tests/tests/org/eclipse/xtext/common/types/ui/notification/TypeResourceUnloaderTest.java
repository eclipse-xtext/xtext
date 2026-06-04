/*******************************************************************************
 * Copyright (c) 2010, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.ui.notification;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtext.common.types.access.jdt.MockJavaProjectProvider;
import org.eclipse.xtext.common.types.eclipse.tests.internal.TestsActivator;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.ui.notification.IStateChangeEventBroker;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.xbase.lib.Procedures;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class TypeResourceUnloaderTest extends Assert implements IResourceDescription.Event.Listener {

	private static final String NESTED_TYPES = "org.eclipse.xtext.common.types.testSetups.NestedTypes";

	private static final String DUMMY_LANG_NAME = "org.eclipse.xtext.common.types.xtext.ui.ContentAssistTestLanguage";
	
	private MockJavaProjectProvider projectProvider;
	private IEditorPart editor;
	private IType type;
	private IJavaProject project;

	private IStateChangeEventBroker eventBroker;

	private volatile IResourceDescription.Event event;
	private List<IResourceDescription.Event> subsequentEvents;

	private ICompilationUnit compilationUnit;

	private IDocument document;

	private String originalContent;
	
	@BeforeClass public static void createMockJavaProject() throws Exception {
		MockJavaProjectProvider.setUp();
	}
	
	@Before
	public void setUp() throws Exception {
		eventBroker = TestsActivator.getInstance().getInjector(DUMMY_LANG_NAME).getInstance(IStateChangeEventBroker.class);
		projectProvider = new MockJavaProjectProvider();
		projectProvider.setUseSource(true);
		project = projectProvider.getJavaProject(null);
		// Ensure the JDT incremental builder state is fully populated before the test starts.
		// Without this, JavaBuilderState.getDefinedTypeNamesFor(typeLocator) may return null
		// and convertRemovedTypes(...) will only emit a delta for the primary type, causing
		// e.g. testRenameClass to see 4 deltas instead of the expected 6.
		IResourcesSetupUtil.waitForBuild();
		type = project.findType(NESTED_TYPES);
		compilationUnit = type.getCompilationUnit();
		assertBuilderStateContainsNestedTypes();
		compilationUnit.becomeWorkingCopy(null);
		
		// wait until the BackgroundThread for the reconciler was started
		editor = waitForElementChangedEvent(new Callable<IEditorPart>() {
			@Override
			public IEditorPart call() throws Exception {
				return JavaUI.openInEditor(compilationUnit);
			}
		}, true);
		
		eventBroker.addListener(this);
		document = getDocument();
		originalContent = document.get();
		subsequentEvents = Lists.newArrayList();
	}
	
	@After
	public void tearDown() throws Exception {
		document.set(originalContent);
		editor.doSave(null);
		closeEditors();
		eventBroker.removeListener(this);
		eventBroker = null;
		projectProvider = null;
		compilationUnit.discardWorkingCopy();
		// Make sure the rebuild triggered by doSave above is finished before the next
		// @Before runs, so that JavaBuilderState is consistent for the following test.
		IResourcesSetupUtil.waitForBuild();
		compilationUnit = null;
		editor = null;
		type = null;
		project = null;
		event = null;
		subsequentEvents = null;
	}

	/**
	 * Sanity check that the JDT builder state knows about all three nested types defined in
	 * {@code NestedTypes.java}. If this fails, the test environment is racing with the Java
	 * builder and the actual test assertions would be misleading.
	 */
	private void assertBuilderStateContainsNestedTypes() {
		JavaBuilderState builderState = JavaBuilderState.getLastBuiltState(project.getProject());
		assertNotNull("No JDT builder state available for project " + project.getElementName(), builderState);
		TypeNames typeNames = builderState.getQualifiedTypeNames(compilationUnit);
		Collection<String> names = typeNames.getTypeNames();
		assertTrue("Builder state is missing " + NESTED_TYPES + ", was: " + names, names.contains(NESTED_TYPES));
		assertTrue("Builder state is missing " + NESTED_TYPES + "$Outer, was: " + names,
				names.contains(NESTED_TYPES + "$Outer"));
		assertTrue("Builder state is missing " + NESTED_TYPES + "$Outer$Inner, was: " + names,
				names.contains(NESTED_TYPES + "$Outer$Inner"));
	}
	
	@Test public void testNullChange() throws BadLocationException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				try {
					int lastBrace = document.get().lastIndexOf("}");
					document.replace(lastBrace, 0, " ");
				} catch (BadLocationException e) {
					Assert.fail(e.getMessage());
				}
			}

		});
		assertNull(event);
	}
	
	@Test public void testCloseEditor() throws InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				((ITextEditor) editor).close(false);
			}

		}, false);
		assertNull(event);
	}
	
	@Test public void testCloseEditorAndDiscardWorkingCopy() throws InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				try {
					compilationUnit.discardWorkingCopy();
					((ITextEditor) editor).close(false);
				} catch (JavaModelException e) {
					throw new RuntimeException(e);
				}
			}

		}, false);
		assertNull(event);
	}
	
	@Test public void testCloseAndReopenEditor() throws InterruptedException, PartInitException, JavaModelException {
		waitForEvent(new Procedure0() {
			
			@Override
			public void apply() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(editor, false);
				try {
					editor = JavaUI.openInEditor(compilationUnit);
				} catch (PartInitException e) {
					fail(e.getMessage());
				} catch (JavaModelException e) {
					fail(e.getMessage());
				}		
			}
		});
		assertNull(event);
	}
	
	@Test
	public void testNonStructuralChange() throws BadLocationException, InterruptedException {
		waitForEvent(new Procedure0() {
			
			@Override
			public void apply() {
				int methodBody = document.get().indexOf("{}") + 1;
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(methodBody, 0, "ignored.toString();");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
				
			}
		});
		assertNull(event);
	}
	
	@Test public void testTypeParameterAdded() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				String lookup = "NestedTypes";
				int idx = document.get().indexOf(lookup) + "NestedTypes".length();
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx, 0, "<Abc>");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		IResourceDescription.Delta delta = event.getDeltas().get(0);
		assertNotNull(delta.getNew());
		assertNotNull(delta.getOld());
		assertEquals("java:/Objects/" + NESTED_TYPES, delta.getUri().toString());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}
	
	@Test public void testRemoveMethod() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				String lookup = "abstract boolean method();";
				int idx = document.get().lastIndexOf(lookup);
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx, lookup.length(), "");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		IResourceDescription.Delta delta = event.getDeltas().get(0);
		assertNotNull(delta.getNew());
		assertNotNull(delta.getOld());
		assertEquals("java:/Objects/" + NESTED_TYPES, delta.getUri().toString());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}

	protected void assertOriginalValues(Collection<String> allNames) {
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES));
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES + "$Outer"));
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES + "$Outer$Inner"));
	}
	
	@Test public void testAddParam() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().lastIndexOf("(");
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx + 1, 0, "int foobar");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}
	
	@Test public void testAddMethod() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				String lookup = "abstract boolean method();";
				int idx = document.get().lastIndexOf(lookup);
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx + lookup.length(), 0, "abstract int foobar();");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}
	
	@Test public void testRenameMethod() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().lastIndexOf("method(");
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx + "method".length(), 0, "2");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}
	
	@Test public void testRenameInnerMethod() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().indexOf("method(");
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx + "method".length(), 0, "2");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		Collection<String> allNames = getNames(event.getDeltas());
		assertOriginalValues(allNames);
		assertEquals("" + allNames, 3, allNames.size());
	}
	
	@Test public void testRenameClass() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().indexOf("NestedTypes");
				assertNull("event is null before the document was modified", event);
				try {
					document.replace(idx, "NestedTypes".length(), "FooBar");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		String foobar = "org.eclipse.xtext.common.types.testSetups.FooBar";
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 6, event.getDeltas().size());
		IResourceDescription.Delta delta = event.getDeltas().get(0);

		assertEquals("java:/Objects/" + foobar, event.getDeltas().get(0).getUri().toString());
		assertEquals("java:/Objects/" + foobar, event.getDeltas().get(1).getUri().toString());
		assertEquals("java:/Objects/" + foobar, event.getDeltas().get(2).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(3).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(4).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(5).getUri().toString());
		
		Collection<String> allNames = getNames(delta);
		addNames(event.getDeltas().get(0).getNew(), allNames); //FooBar
		addNames(event.getDeltas().get(1).getNew(), allNames); //FooBar$Outer
		addNames(event.getDeltas().get(2).getNew(), allNames); //FooBar$Outer$Inner
		addNames(event.getDeltas().get(3).getOld(), allNames); //NestedTypes
		addNames(event.getDeltas().get(4).getOld(), allNames); //NestedTypes$Outer
		addNames(event.getDeltas().get(5).getOld(), allNames); //NestedTypes$Outer$Inner
		assertTrue(allNames.toString(), allNames.contains(foobar));
		assertTrue(allNames.toString(), allNames.contains(foobar + "$Outer"));
		assertTrue(allNames.toString(), allNames.contains(foobar + "$Outer$Inner"));
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES));
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES + "$Outer"));
		assertTrue(allNames.toString(), allNames.contains(NESTED_TYPES + "$Outer$Inner"));
		assertEquals("" + allNames, 6, allNames.size());
	}
	
	@Test public void testRemoveNestedClass() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().indexOf("public abstract class Inner");
				assertNull("event is null before the document was modified", event);
				try {
					int closing = document.get().substring(idx).indexOf("\t}");
					document.replace(idx, closing + 2, "");
				} catch (BadLocationException e) {
					fail(e.getMessage());
				}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 3, event.getDeltas().size());
		assertNull(event.getDeltas().get(2).getNew());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(0).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(1).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(2).getUri().toString());
		
	}
	
	@Test public void testAddNestedClass() throws BadLocationException, JavaModelException, InterruptedException {
		waitForEvent(new Procedure0() {

			@Override
			public void apply() {
				int idx = document.get().indexOf("public abstract class Inner");
				assertNull("event is null before the document was modified", event);
				try {
						int closing = document.get().substring(idx).indexOf("\t}");
						document.replace(idx + closing, 0, " public abstract class InnerSibling {} ");
					} catch (BadLocationException e) {
						fail(e.getMessage());
					}
			}

		});
		assertNotNull(event);
		assertTrue(subsequentEvents.toString(), subsequentEvents.isEmpty());
		assertEquals("" + event.getDeltas(), 4, event.getDeltas().size());
		assertNull(event.getDeltas().get(3).getOld());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(0).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(1).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(2).getUri().toString());
		assertEquals("java:/Objects/" + NESTED_TYPES, event.getDeltas().get(3).getUri().toString());
		
	}
	
	protected Collection<String> getNames(List<IResourceDescription.Delta> deltas) {
		Set<String> result = Sets.newHashSet();
		for (IResourceDescription.Delta delta : deltas) {
			addNames(delta.getNew(), result);
			addNames(delta.getOld(), result);
		}
		return result;
	}
	
	protected Collection<String> getNames(IResourceDescription.Delta delta) {
		Set<String> result = Sets.newHashSet();
		addNames(delta.getNew(), result);
		addNames(delta.getOld(), result);
		return result;
	}
	
	protected void addNames(IResourceDescription description, Collection<String> names) {
		if (description == null)
			return;
		Iterable<IEObjectDescription> iterable = description.getExportedObjects();
		for (IEObjectDescription object : iterable) {
			names.add(object.getName().toString());
		}
	}
	
	/**
	 * Same as {@code waitForEvent(procedure, true} which is that an {@link ElementChangedEvent} is expected.
	 * @see #waitForEvent(Procedure0, boolean)
	 */
	protected void waitForEvent(final Procedures.Procedure0 procedure) throws InterruptedException {
		waitForEvent(procedure, true);
	}
	
	protected void waitForEvent(final Procedures.Procedure0 procedure, boolean expectEvent) throws InterruptedException {
		try {
			waitForElementChangedEvent(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					procedure.apply();
					return null;
				}
			}, expectEvent);
		} catch(InterruptedException e) {
			throw e;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Wait explicitly for an {@link ElementChangedEvent} that is propagated when the given Callable is executed.
	 * May also be used to assert that no such event occurred. 
	 */
	protected <T> T waitForElementChangedEvent(Callable<T> callMe, boolean expectEvent) throws Exception {
		class Listener implements IElementChangedListener {
			private volatile boolean eventSeen = false;
			@Override
			public void elementChanged(ElementChangedEvent event) {
				eventSeen = true;
				JavaCore.removeElementChangedListener(this);
			}
		}
		Listener listener = new Listener();
		JavaCore.addElementChangedListener(listener);
		try {
			return callMe.call();
		} finally {
			// usually a counter of 100 is way more than enough, but on the CI server we see a longer
			// delay, maybe the threading model on linux is slightly different. Anyway, the overall runtime
			// on dev boxes is not affected but the test is green on the server
			long before = System.currentTimeMillis();
			int counter = expectEvent ? 350 : 150;
			while(!listener.eventSeen && counter > 0) {
				counter--;
				Thread.sleep(15);
			}
			long timeWaited = System.currentTimeMillis() - before;
			assertEquals("Waited "+timeWaited+"ms.", expectEvent, listener.eventSeen);
		}
	}
	
	@Override
	public void descriptionsChanged(IResourceDescription.Event event) {
		if (this.event != null && subsequentEvents != null) {
			subsequentEvents.add(event);
		}
		this.event = event;
	}

	protected IDocument getDocument() {
		ITextEditor textEditor = (ITextEditor) editor;
		IDocument document = textEditor.getDocumentProvider().getDocument(editor.getEditorInput());
		return document;
	}
	
	protected void closeEditors() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
	}

}
