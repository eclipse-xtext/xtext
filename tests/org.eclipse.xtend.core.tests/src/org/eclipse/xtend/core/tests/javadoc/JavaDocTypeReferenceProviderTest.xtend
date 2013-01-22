package org.eclipse.xtend.core.tests.javadoc

import org.eclipse.xtend.core.tests.AbstractXtendTestCase
import org.eclipse.xtext.documentation.IJavaDocTypeReferenceProvider
import org.eclipse.xtext.resource.XtextResource
import org.junit.Test
import com.google.inject.Inject

class JavaDocTypeReferenceProviderTest extends AbstractXtendTestCase {
	
	@Inject IJavaDocTypeReferenceProvider javaDocTypeReferenceProvider
	@Test
	def void testComputation() {
		val String input = '''
			package foo
			
			/**
			* {@link List}
			* {@link String}
			* {@link Integer
			* {@link ArrayList}
			*/
			class Foo{}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeTypeRefRegions(rootNode)
		assertEquals(3,regions.size)
		assertEquals("List", regions.head.text);
		assertEquals(input.indexOf("List"), regions.head.offset)
		assertEquals("List".length, regions.head.length)
	}

	@Test
	def void testComputation_2() {
		val String input = '''
			package foo

			/**
			* {@link java.util.ArrayList @see Integer}
			*/
			class Foo{}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeTypeRefRegions(rootNode)
		assertEquals(1,regions.size)
		assertEquals("java.util.ArrayList", regions.head.text);
		assertEquals(input.indexOf("java.util.ArrayList"), regions.head.offset)
		assertEquals("java.util.ArrayList".length, regions.head.length)
	}

	@Test
	def void testComputation_3() {
		val String input = '''
			package foo

			/**
			* {@link java.util.ArrayList
			*/
			class Foo{}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeTypeRefRegions(rootNode)
		assertEquals(0,regions.size)
	}

	@Test
	def void testComputation_4() {
		val String input = '''
			package foo

			/**
			* {@link java.util.ArrayList
			 */
			class Foo{}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeTypeRefRegions(rootNode)
		assertEquals(0,regions.size)
	}

	@Test
	def void testComputation_5() {
		val String input = '''
			package foo

			class Foo{
				/**
				* @param string - a fancy string
				* @param zonk
				*/
				def bar(String string, Integer zonk){}
			}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeParameterTypeRefRegions(rootNode)
		assertEquals(2,regions.size)
		assertEquals("string", regions.head.text);
		assertEquals(input.indexOf("string"), regions.head.offset)
		assertEquals("string".length, regions.head.length)
		assertEquals("zonk", regions.get(1).text);
		assertEquals(input.indexOf("zonk"), regions.get(1).offset)
		assertEquals("zonk".length, regions.get(1).length)
	}

	@Test
	def void testComputation_6() {
		val String input = '''
			package foo

			/**
			* {@link }
			* @see
			* @see
			* {@link}
			*/
			class Foo{}
		'''
		val resource = clazz(input).eResource as XtextResource
		val rootNode = resource.parseResult.rootNode
		val regions = javaDocTypeReferenceProvider.computeTypeRefRegions(rootNode)
		assertEquals(0,regions.size)
	}

}