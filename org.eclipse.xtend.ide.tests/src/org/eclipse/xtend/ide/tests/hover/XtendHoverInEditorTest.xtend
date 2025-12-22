/*******************************************************************************
 * Copyright (c) 2015, 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.hover

import com.google.inject.Inject
import org.apache.log4j.Level
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput
import org.eclipse.jface.text.ITextHoverExtension2
import org.eclipse.jface.text.ITextViewer
import org.eclipse.jface.text.Region
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper
import org.eclipse.xtext.testing.logging.LoggingTester
import org.eclipse.xtext.ui.editor.hover.IEObjectHover
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil
import org.eclipse.xtext.xbase.typesystem.internal.AbstractBatchTypeResolver
import org.eclipse.xtext.xbase.ui.hover.XbaseInformationControlInput
import org.junit.After
import org.junit.Test

/**
 * @author Sven Efftinge - Initial contribution and API
 */
class XtendHoverInEditorTest extends AbstractXtendUITestCase {
	
	@Inject extension WorkbenchTestHelper helper
	
	@Inject extension SyncUtil
	
	@Inject IEObjectHover hoverer
	
	@After override void tearDown() {
		helper.tearDown
	}
	
	@Test def void testHoverOfReferencedElement() {
		val contentFoo = '''
			/**
			 * Hello Foo
			 */
			class Foo {}
		'''
		val contentBar = '''
			class Bar extends Foo {}
		'''
		val fileFoo = createFile("Foo.xtend",contentFoo)
		val fileBar = createFile("Bar.xtend",contentBar)
		waitForBuild(null)
		
		val editor = openEditor(fileBar)
		val info = (hoverer as ITextHoverExtension2).getHoverInfo2(editor.internalSourceViewer as ITextViewer, new Region(19,1)) as XtextBrowserInformationControlInput
		assertTrue(info.html.contains("Hello Foo"))
		
		// check dirty State
		val fooEditor = openEditor(fileFoo)
		fooEditor.document.set('''
			/**
			 * Hello BAZ
			 */
			class Foo {}
		''')
		fooEditor.waitForReconciler
		editor.waitForReconciler
		val info2 = (hoverer as ITextHoverExtension2).getHoverInfo2(editor.internalSourceViewer as ITextViewer, new Region(19,1)) as XtextBrowserInformationControlInput
		assertFalse(info2.html.contains("Hello Foo"))
		assertTrue(info2.html.contains("Hello BAZ"))
	}
	
	@Test def void testHoverOfReferencedElementWithAnnotation() {
		val contentFoo = '''
			/**
			 * Hello Foo
			 */
			@SuppressWarnings("foo")
			class Foo {}
		'''
		val contentBar = '''
			class Bar extends Foo {}
		'''
		val fileFoo = createFile("Foo.xtend",contentFoo)
		val fileBar = createFile("Bar.xtend",contentBar)
		waitForBuild(null)
		
		val editor = openEditor(fileBar)
		editor.waitForReconciler
		val loggings = LoggingTester.captureLogging(Level.ERROR, AbstractBatchTypeResolver) [
			val info = (hoverer as ITextHoverExtension2).getHoverInfo2(editor.internalSourceViewer as ITextViewer, new Region(19,1)) as XtextBrowserInformationControlInput
			assertTrue(info.html,info.html.contains("Hello Foo"))
			assertTrue(info.html,info.html.contains('SuppressWarnings</a>("foo")'))
		]
		loggings.assertNoLogEntries
		
		// check dirty State
		val fooEditor = openEditor(fileFoo)
		fooEditor.document.set('''
			/**
			 * Hello BAZ
			 */
			@SuppressWarnings("bar")
			class Foo {}
		''')
		fooEditor.waitForReconciler
		editor.waitForReconciler
		
		val moreLoggings = LoggingTester.captureLogging(Level.ERROR, AbstractBatchTypeResolver) [
			val info2 = (hoverer as ITextHoverExtension2).getHoverInfo2(editor.internalSourceViewer as ITextViewer, new Region(19,1)) as XtextBrowserInformationControlInput
			assertFalse(info2.html.contains("Hello Foo"))
			assertTrue(info2.html.contains("Hello BAZ"))
			assertTrue(info2.html, info2.html.contains('SuppressWarnings</a>("bar")'))
		]
		moreLoggings.assertNoLogEntries
	}

	@Test def testHoverOfJavaType() {
		'''
			class Foo {
				Object object
			}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfJavaTypeInJavadocLink() {
		'''
			/**
			 * {@link Object}
			 */
			class Foo {}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfJavaTypeInJavadocSee() {
		'''
			/**
			 * @see Object
			 */
			class Foo {}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfFQNJavaType() {
		'''
			class Foo {
				java.lang.Object object
			}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfFQNJavaTypeInJavadocLink() {
		'''
			/**
			 * {@link java.lang.Object}
			 */
			class Foo {}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfFQNJavaTypeInJavadocSee() {
		'''
			/**
			 * @see java.lang.Object
			 */
			class Foo {}
		'''.hasHoverOverObject
	}

	@Test def testHoverOfXtendType() {
		"Bar.xtend".createFile('''
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			class Foo {
				Bar bar
			}
		'''.hasHoverOverBar
	}

	@Test def testHoverOfXtendTypeInJavadocLink() {
		"Bar.xtend".createFile('''
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			/**
			 * {@link Bar}
			 */
			class Foo {}
		'''.hasHoverOverBar
	}

	@Test def testHoverOfXtendTypeInJavadocSee() {
		"Bar.xtend".createFile('''
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			/**
			 * @see Bar
			 */
			class Foo {}
		'''.hasHoverOverBar
	}

	@Test def testHoverOfFQNXtendType() {
		"barpackage/Bar.xtend".createFile('''
			package barpackage
			
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			class Foo {
				barpackage.Bar bar
			}
		'''.hasHoverOverBar
	}

	@Test def testHoverOfFQNXtendTypeInJavadocLink() {
		"barpackage/Bar.xtend".createFile('''
			package barpackage
			
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			/**
			 * {@link barpackage.Bar}
			 */
			class Foo {}
		'''.hasHoverOverBar
	}

	@Test def testHoverOfFQNXtendTypeInJavadocSee() {
		"barpackage/Bar.xtend".createFile('''
			package barpackage
			
			/**
			 * Documentation of the Bar class.
			 */
			class Bar {}
		''')
		
		'''
			/**
			 * @see barpackage.Bar
			 */
			class Foo {}
		'''.hasHoverOverBar
	}

	private def hasHoverOverBar(CharSequence it) {
		hasHoverOverXtendType("Bar", '''Documentation of the Bar class.''')
	}

	private def hasHoverOverObject(CharSequence it) {
		hasHoverOverJavaType("Object", '''</code> is the root of the class hierarchy.''')
	}

	private def hasHoverOverJavaType(CharSequence it, String textUnderHover, String expectedHoverContent) {
		hasHoverOver(textUnderHover, JavadocBrowserInformationControlInput, expectedHoverContent)
	}

	private def hasHoverOverXtendType(CharSequence it, String textUnderHover, String expectedHoverContent) {
		hasHoverOver(textUnderHover, XbaseInformationControlInput, expectedHoverContent)
	}

	private def hasHoverOver(CharSequence it, String textUnderHover, Class<? extends BrowserInformationControlInput> expectedPopupType, String expectedHoverContent) {
		val fileFoo = "Foo.xtend".createFile(toString)
		
		waitForBuild(null)
		
		val editor = fileFoo.openEditor
		
		val region = new Region(toString.indexOf(textUnderHover), textUnderHover.length)
		
		val info = (hoverer as ITextHoverExtension2).getHoverInfo2(editor.internalSourceViewer, region) 
		
		assertTrue(expectedPopupType.isInstance(info))
		
		val html = (info as BrowserInformationControlInput).html
		
		assertTrue(html.contains(expectedHoverContent))
	}
}