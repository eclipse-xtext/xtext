/*******************************************************************************
 * Copyright (c) 2013, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.imports

import com.google.inject.Inject
import org.eclipse.xtend.core.tests.AbstractXtendTestCase
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.ReplaceRegion
import org.eclipse.xtext.xbase.imports.ImportOrganizer
import org.junit.Test

/**
 * @author Jan Koehnlein - Initial conribution and API
 */
class ImportOrganizerTest extends AbstractXtendTestCase {

	@Inject ImportOrganizer importOrganizer

	def protected assertIsOrganizedTo(CharSequence model, CharSequence expected) {
		assertIsOrganizedTo(model, expected, true)
	}
	
	def protected assertIsOrganizedTo(CharSequence model, CharSequence expected, boolean validate) {
		assertFalse (expected.toString.contains("$"))
		val xtendFile = file(model.toString, validate)
		val changes = importOrganizer.getOrganizedImportChanges(xtendFile.eResource as XtextResource)
		val builder = new StringBuilder(model)
		val sortedChanges= changes.sortBy[offset]
		var ReplaceRegion lastChange = null
		for(it: sortedChanges) {
			if(lastChange !== null && lastChange.endOffset > offset)
				fail("Overlapping text edits: " + lastChange + ' and ' +it)
			lastChange = it
		}
		for(it: sortedChanges.reverse)
			builder.replace(offset, offset + length, text)
		assertEquals(expected.toString, builder.toString)
	}

	@Test def testEnumerationLiteralInAnnotation() {
		'''
			import static java.lang.annotation.RetentionPolicy.*
			
			@java.lang.annotation.Retention(CLASS)
			annotation A {
			}
		'''.assertIsOrganizedTo('''
			import java.lang.annotation.Retention
			
			@Retention(CLASS)
			annotation A {
			}
		''')
	}

	@Test def testEnumerationLiteralInSwitch() {
		'''
			import static java.lang.annotation.RetentionPolicy.*
			
			class C {
				def m(java.lang.annotation.RetentionPolicy p) {
					switch p {
						case CLASS: true
						default: false
					}
				}
			}
		'''.assertIsOrganizedTo('''
			import java.lang.annotation.RetentionPolicy
			
			class C {
				def m(RetentionPolicy p) {
					switch p {
						case CLASS: true
						default: false
					}
				}
			}
		''')
	}

	@Test def testSimple() {
		'''
			package foo

			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			package foo

			import java.io.Serializable
			
			class Foo implements Serializable {}
		''')
	}
	
	@Test def testInstanceOf() {
		'''
			class C {
				def void m(Object o) {
					if (o instanceof String) {
					}
				}
			}
		'''.assertIsOrganizedTo('''
			class C {
				def void m(Object o) {
					if (o instanceof String) {
					}
				}
			}
		''')
	}

	@Test def testDefaultPackage() {
		'''
			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable

			class Foo implements Serializable {}
		''')
	}

	@Test def testDefaultPackageLeadingWhitespace() {
		'''
			«»
			   	
			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable

			class Foo implements Serializable {}
		''')
	}

	@Test def testDefaultPackageWithJavaDoc() {
		'''
			/**
			 * some doc
			 */
			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable

			/**
			 * some doc
			 */
			class Foo implements Serializable {}
		''')
	}

	@Test def testWithHeader() {
		'''
			/**
			 * some header
			 */
			package foo
			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			/**
			 * some header
			 */
			package foo

			import java.io.Serializable

			class Foo implements Serializable {}
		''')
	}

	@Test def testWithHeaderAndJavaDoc() {
		'''
			/**
			 * some header
			 */
			package foo
			/**
			 * some doc
			 */
			class Foo implements java.io.Serializable {}
		'''.assertIsOrganizedTo('''
			/**
			 * some header
			 */
			package foo

			import java.io.Serializable

			/**
			 * some doc
			 */
			class Foo implements Serializable {}
		''')
	}

	@Test def testGetOrganizedImportSection_01() {
		'''
			import java.lang.String
			import java.util.List

			class Foo {
			  def void test(List<String> s) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List

			class Foo {
			  def void test(List<String> s) {
			  }
			}
		''')
	}

	@Test def testGetOrganizedImportSection_02() {
		'''
			import java.lang.String
			import java.util.List
			import java.util.List
			import java.util.List
			import java.lang.Integer

			class Foo {
			  def void test(List<String> s) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List

			class Foo {
			  def void test(List<String> s) {
			  }
			}
		''', false)
	}

	@Test def testGetOrganizedImportSection_03() {
		'''
			import java.util.*

			class Foo {
			  def void test(List<String> s) {
			    val x = newArrayList('foo','bar')
			    Collections::sort(x)
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.Collections
			import java.util.List

			class Foo {
			  def void test(List<String> s) {
			    val x = newArrayList('foo','bar')
			    Collections::sort(x)
			  }
			}
		''', false)
	}

	@Test def testGetOrganizedImportSection_04() {
		'''
			import java.util.*
			import java.io.*

			class Foo {
			  def void test(List<String> s) {
			    val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.io.Serializable
			import java.util.ArrayList
			import java.util.List
			import java.util.Map

			class Foo {
			  def void test(List<String> s) {
			    val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()
			  }
			}
		''', false)
	}

	@Test def testGetOrganizedImportSection_05() {
		'''
			import java.util.*
			import java.io.*
			import com.google.inject.Inject

			class Foo {
			  @Inject String s
			  def void test(List<String> s) {
			    val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()
			  }
			}
		'''.assertIsOrganizedTo('''
			import com.google.inject.Inject
			import java.io.Serializable
			import java.util.ArrayList
			import java.util.List
			import java.util.Map

			class Foo {
			  @Inject String s
			  def void test(List<String> s) {
			    val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()
			  }
			}
		''', false)
	}

	@Test def testInnerClasses_01() {
		'''
			class Foo {
			  def void test(org.eclipse.emf.ecore.resource.Resource$Factory a, org.eclipse.emf.ecore.resource.Resource$Factory$Registry b) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def void test(Resource.Factory a, Resource.Factory.Registry b) {
			  }
			}
		''')
	}

	@Test def testInnerClasses_02() {
		'''
			import org.eclipse.emf.ecore.resource.Resource
			import org.eclipse.emf.ecore.EPackage

			class Foo {
			  def test() {
			    val x = Resource$Factory$Registry::INSTANCE
			    val y = EPackage$Registry::INSTANCE
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EPackage
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
			    val x = Resource.Factory.Registry::INSTANCE
			    val y = EPackage.Registry::INSTANCE
			  }
			}
		''', false)
	}

	@Test def testInnerClasses_03() {
		'''
			class Foo {
			  def test() {
				 typeof(org.eclipse.emf.ecore.resource.Resource$Factory) == typeof(org.eclipse.emf.ecore.resource.Resource$Factory$Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
				 typeof(Resource.Factory) == typeof(Resource.Factory.Registry)
			  }
			}
		''')
	}

	@Test def testInnerClasses_04() {
		'''
			import org.eclipse.emf.ecore.resource.Resource
			import org.eclipse.emf.ecore.EPackage

			class Foo {
			  def test() {
			    typeof(Resource$Factory$Registry) == typeof(EPackage$Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EPackage
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
			    typeof(Resource.Factory.Registry) == typeof(EPackage.Registry)
			  }
			}
		''')
	}

	@Test def testInnerClasses_05() {
		'''
			import org.eclipse.emf.ecore.resource.Resource$Factory$Registry

			class Foo {
			  def test() {
			    typeof(Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource.Factory.Registry

			class Foo {
			  def test() {
			    typeof(Registry)
			  }
			}
		''')
	}
	
	@Test def testInnerClasses_06() {
		'''
			class Resource {
			  def test() {
			    val x = org::eclipse::emf::ecore::resource::Resource$Factory$Registry::INSTANCE
			  }
			}
		'''.assertIsOrganizedTo('''
			class Resource {
			  def test() {
			    val x = org.eclipse.emf.ecore.resource.Resource.Factory.Registry::INSTANCE
			  }
			}
		''', false)
	}
	
	@Test def testInnerClasses_07() {
		'''
			class Foo {
			  def void test(org.eclipse.emf.ecore.resource.Resource.Factory a, org.eclipse.emf.ecore.resource.Resource.Factory.Registry b) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def void test(Resource.Factory a, Resource.Factory.Registry b) {
			  }
			}
		''')
	}
	
	@Test def testInnerClasses_08() {
		'''
			import org.eclipse.emf.ecore.resource.Resource
			import org.eclipse.emf.ecore.EPackage

			class Foo {
			  def test() {
			    val x = Resource::Factory::Registry::INSTANCE
			    val y = EPackage::Registry::INSTANCE
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EPackage
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
			    val x = Resource.Factory.Registry::INSTANCE
			    val y = EPackage.Registry::INSTANCE
			  }
			}
		''', false)
	}

	@Test def testInnerClasses_09() {
		'''
			class Foo {
			  def test() {
				 typeof(org.eclipse.emf.ecore.resource.Resource.Factory) == typeof(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
				 typeof(Resource.Factory) == typeof(Resource.Factory.Registry)
			  }
			}
		''')
	}

	@Test def testInnerClasses_10() {
		'''
			import org.eclipse.emf.ecore.resource.Resource
			import org.eclipse.emf.ecore.EPackage

			class Foo {
			  def test() {
			    typeof(Resource.Factory.Registry) == typeof(EPackage.Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EPackage
			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def test() {
			    typeof(Resource.Factory.Registry) == typeof(EPackage.Registry)
			  }
			}
		''')
	}

	@Test def testInnerClasses_11() {
		'''
			import org.eclipse.emf.ecore.resource.Resource.Factory.Registry

			class Foo {
			  def test() {
			    typeof(Registry)
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.resource.Resource.Factory.Registry

			class Foo {
			  def test() {
			    typeof(Registry)
			  }
			}
		''')
	}
	
	@Test def testInnerClasses_12() {
		'''
			class Resource {
			  def test() {
			    val x = org::eclipse::emf::ecore::resource::Resource::Factory::Registry::INSTANCE
			  }
			}
		'''.assertIsOrganizedTo('''
			class Resource {
			  def test() {
			    val x = org.eclipse.emf.ecore.resource.Resource.Factory.Registry::INSTANCE
			  }
			}
		''', false)
	}
	
	@Test def testInnerClasses_13() {
		'''
			package inner
			class Foo {
				static class Bar {}
				def m() {
					new Bar
				}
			}
		'''.assertIsOrganizedTo('''
			package inner
			
			class Foo {
				static class Bar {}
				def m() {
					new Bar
				}
			}
		''', false)
	}
	
	@Test def testInnerClasses_14() {
		'''
			class Test {
				def baz(Bar bar) {}
			
				interface Bar {}
			}
		'''.assertIsOrganizedTo('''
			class Test {
				def baz(Bar bar) {}
			
				interface Bar {}
			}
		''', false)
	}
	
	@Test def testInnerClasses_15() {
		'''
			package p
			class Test {
				def baz(Bar bar) {}
			
				interface Bar {}
			}
		'''.assertIsOrganizedTo('''
			package p
			
			class Test {
				def baz(Bar bar) {}
			
				interface Bar {}
			}
		''', false)
	}
	
	@Test def testInnerClasses_16() {
		'''
			package p
			class Test {
				def baz(Bar.XYZ x) {}
			
				interface Bar {
					class XYZ {}
				}
			}
		'''.assertIsOrganizedTo('''
			package p
			
			class Test {
				def baz(Bar.XYZ x) {}
			
				interface Bar {
					class XYZ {}
				}
			}
		''', false)
	}
	
	@Test def testInnerClasses_17() {
		'''
			class Test {
				static String s
				interface Bar {
					class XYZ {
						def foo() {s}
					}
				}
			}
		'''.assertIsOrganizedTo('''
			class Test {
				static String s
				interface Bar {
					class XYZ {
						def foo() {s}
					}
				}
			}
		''', false)
	}

	@Test def testInnerClasses_18() {
		'''
			package p
			class Test {
				/** @see Bar$XYZ */
				def baz(Bar.XYZ x) {}
			
				interface Bar {
					class XYZ {}
				}
			}
		'''.assertIsOrganizedTo('''
			package p
			
			class Test {
				/** @see Bar.XYZ */
				def baz(Bar.XYZ x) {}
			
				interface Bar {
					class XYZ {}
				}
			}
		''', false)
	}
	
	/**https://bugs.eclipse.org/bugs/show_bug.cgi?id=448728 */
	@Test def testBug448728() {
		'''
			package p
			class SomeObject {
				static class OtherObject {
					def static createNew() {
						val result = new OtherObject
						return result
					}
				}
			}
		'''.assertIsOrganizedTo('''
			package p
			
			class SomeObject {
				static class OtherObject {
					def static createNew() {
						val result = new OtherObject
						return result
					}
				}
			}
		''', false)
	}
	
	@Test def testNameClashSameFileWins_1() {
		'''
			package foo

			class Foo {
				java.lang.String x
			}

			class String {}
		'''.assertIsOrganizedTo('''
			package foo

			class Foo {
				java.lang.String x
			}

			class String {}
		''')
	}

	@Test def testNameClashSameFileWins() {
		'''
			package foo

			class Foo implements java.io.Serializable {}

			class Serializable {}
		'''.assertIsOrganizedTo('''
			package foo

			class Foo implements java.io.Serializable {}

			class Serializable {}
		''')
	}

	@Test def testNameClashOrder_01() {
		'''
			class Foo {
			  def java.awt.List test(java.util.List<String> s) {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List

			class Foo {
			  def java.awt.List test(List<String> s) {
			    return null
			  }
			}
		''')
	}

	@Test def testNameClashOrder_02() {
		'''
			class Foo {
			  def java.util.List<String> test(java.awt.List s) {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.awt.List

			class Foo {
			  def java.util.List<String> test(List s) {
			    return null
			  }
			}
		''')
	}

	@Test def testNameClashMoreCommon() {
		'''
			class Foo {
			  def java.util.List<String> test(java.awt.List s) {
			  	new java.awt.List()
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.awt.List

			class Foo {
			  def java.util.List<String> test(List s) {
			  	new List()
			    return null
			  }
			}
		''')
	}

	@Test def testNameClashInnerClasses_01() {
		'''
			import org.eclipse.xtext.xbase.XbasePackage
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(XbasePackage$Literals x, XAnnotationsPackage$Literals y) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.xbase.XbasePackage
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(XbasePackage.Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		''')
	}

	@Test def testNameClashInnerClasses_02() {
		'''
			import org.eclipse.xtext.xbase.XbasePackage
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(XbasePackage.Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.xbase.XbasePackage
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(XbasePackage.Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		''')
	}

	@Test def testNameClashInnerClassesWithPreference_01() {
		'''
			import org.eclipse.xtext.xbase.XbasePackage$Literals
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(Literals x, XAnnotationsPackage$Literals y) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.xbase.XbasePackage.Literals
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		''')
	}

	@Test def testNameClashInnerClassesWithPreference_02() {
		'''
			import org.eclipse.xtext.xbase.XbasePackage.Literals
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.xbase.XbasePackage.Literals
			import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage

			class Foo {
			  def void test(Literals x, XAnnotationsPackage.Literals y) {
			  }
			}
		''')
	}

	@Test def testStaticImport_01() {
		'''
			import static extension java.util.Collections.*
			class Foo {
			  def void test(java.util.List<String> s) {
			  sort(s)
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List
			
			import static java.util.Collections.*

			class Foo {
			  def void test(List<String> s) {
			  sort(s)
			  }
			}
		''')
	}

	@Test def testStaticImport_02() {
		'''
			import static extension java.util.Collections.*
			class Foo {
			  def void test(java.util.List<String> s) {
			  s.sort
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List

			import static extension java.util.Collections.*

			class Foo {
			  def void test(List<String> s) {
			  s.sort
			  }
			}
		''')
	}


	@Test def testImportedOperators_01() {
		'''
			import static extension test.StaticOperators.*
			class Foo {
			  def void test(String s) {
			    -s
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension test.StaticOperators.*

			class Foo {
			  def void test(String s) {
			    -s
			  }
			}
		''')
	}

	@Test def testImportedOperators_02() {
		'''
			import static extension test.StaticOperators.*
			class Foo {
			  def void test(String s) {
			    s-s
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension test.StaticOperators.*

			class Foo {
			  def void test(String s) {
			    s-s
			  }
			}
		''')
	}

	@Test def testImportedOperators_03() {
		'''
			import static extension test.StaticOperators.*
			class Foo {
			  def void test(String s) {
			    s.assign = s
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension test.StaticOperators.*

			class Foo {
			  def void test(String s) {
			    s.assign = s
			  }
			}
		''')
	}

	@Test def testImportedOperators_04() {
		'''
			import static extension test.StaticOperators.*
			class Foo {
			  def void test(String it) {
			    assign = it
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension test.StaticOperators.*

			class Foo {
			  def void test(String it) {
			    assign = it
			  }
			}
		''')
	}
	
	@Test def testLocalOperator_01() {
		'''
			class Foo {
				def foo() {
					'' * ''
				}
				def operator_multiply(String x, String y) {
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def foo() {
					'' * ''
				}
				def operator_multiply(String x, String y) {
				}
			}
		''')
	}
	
	@Test def testLocalOperator_02() {
		'''
			class Foo {
				def foo() {
					'' * ''
				}
				def *(String x, String y) {
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def foo() {
					'' * ''
				}
				def *(String x, String y) {
				}
			}
		''')
	}

	@Test def testStaticLocalOperator_01() {
		'''
			class Foo {
				def foo() {
					'' * ''
				}
				def static *(String x, String y) {
					
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def foo() {
					'' * ''
				}
				def static *(String x, String y) {
					
				}
			}
		''')
	}
	
	@Test def testStaticLocalOperator_02() {
		'''
			class Foo {
				def foo() {
					'' * ''
				}
				def static operator_multiply(String x, String y) {
					
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def foo() {
					'' * ''
				}
				def static operator_multiply(String x, String y) {
					
				}
			}
		''')
	}

	@Test def testImplicitExtensions() {
		'''
			class Foo {
			  def void test(java.util.List<String> s) {
			    s.head
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.List

			class Foo {
			  def void test(List<String> s) {
			    s.head
			  }
			}
		''')
	}

	@Test def testStaticExtensions() {
		'''
			import static extension java.util.Collections.*
			class Foo {
			  def void test() {
			    typeof(java.util.Collections).singleton
			  }
			}
		'''.assertIsOrganizedTo('''
			import java.util.Collections

			import static extension java.util.Collections.*

			class Foo {
			  def void test() {
			    typeof(Collections).singleton
			  }
			}
		''')
	}

	@Test def testStaticExtensionSameClass() {
		'''
			class Foo {
			  static def void test() {
			    "".foo
			  }
			
			  static def void getFoo(String x) {
			  } 
			}
		'''.assertIsOrganizedTo('''
			class Foo {
			  static def void test() {
			    "".foo
			  }
			
			  static def void getFoo(String x) {
			  } 
			}
		''')
	}

	@Test def testObsoleteStatic() {
		'''
			import static java.util.Collections.*
			import static extension java.util.Collections.*
			class Foo {
			  def test() {
			    ''.singleton
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension java.util.Collections.*

			class Foo {
			  def test() {
			    ''.singleton
			  }
			}
		''')
	}

	@Test def testObsoleteStatic_2() {
		'''
			import static java.util.Collections.*
			import static extension java.util.Collections.*
			class Foo {
			  def test() {
			    ''.singleton
			    singleton('')
			  }
			}
		'''.assertIsOrganizedTo('''
			import static extension java.util.Collections.*

			class Foo {
			  def test() {
			    ''.singleton
			    singleton('')
			  }
			}
		''')
	}

	@Test def testObsoleteStaticExtension() {
		'''
			import static java.util.Collections.*
			import static extension java.util.Collections.*
			class Foo {
			  def test() {
			    singleton('')
			  }
			}
		'''.assertIsOrganizedTo('''
			import static java.util.Collections.*

			class Foo {
			  def test() {
			    singleton('')
			  }
			}
		''')
	}

	@Test def testInnerClassImport_01() {
		'''
			package foo.bar
			import java.util.Map$Entry
			class Foo {
			  def Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map.Entry

			class Foo {
			  def Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_02() {
		'''
			package foo.bar
			import java.util.Map
			class Foo {
			  def Map$Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_03() {
		'''
			package foo.bar
			import java.util.Map$Entry
			class Foo {
			  def java.util.Map$Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_04() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource
			class Foo {
			  def Resource$Factory$Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_05() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor
			class Foo {
			  def org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_06() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor
			class Foo {
			  def Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor

			class Foo {
			  def Descriptor test() {
			    return null
			  }
			}
		''')
	}
	
	@Test def testInnerClassImport_07() {
		'''
			package foo.bar
			import java.util.Map.Entry
			class Foo {
			  def Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map.Entry

			class Foo {
			  def Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_08() {
		'''
			package foo.bar
			import java.util.Map
			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_09() {
		'''
			package foo.bar
			import java.util.Map.Entry
			class Foo {
			  def java.util.Map.Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_10() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource
			class Foo {
			  def Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_11() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor
			class Foo {
			  def org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource

			class Foo {
			  def Resource.Factory.Descriptor test() {
			    return null
			  }
			}
		''')
	}

	@Test def testInnerClassImport_12() {
		'''
			package foo.bar
			import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor
			class Foo {
			  def Descriptor test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor

			class Foo {
			  def Descriptor test() {
			    return null
			  }
			}
		''')
	}
	
	@Test def testInnerClassImport_13() {
		'''
			package foo.bar
			import java.util.Map.Entry
			class Foo {
			  def java.util.Map$Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}
	
	@Test def testInnerClassImport_14() {
		'''
			package foo.bar
			import java.util.Map$Entry
			class Foo {
			  def java.util.Map.Entry test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map

			class Foo {
			  def Map.Entry test() {
			    return null
			  }
			}
		''')
	}

	@Test def testFunctionTypes_afterResolve_01() {
		'''
			package foo.bar
			import java.util.Map$Entry
			class Foo {
			  def (Entry)=>void test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map.Entry

			class Foo {
			  def (Entry)=>void test() {
			    return null
			  }
			}
		''')
	}

	@Test def testFunctionTypes_afterResolve_02() {
		'''
			package foo.bar
			import java.util.Map.Entry
			class Foo {
			  def (Entry)=>void test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.util.Map.Entry

			class Foo {
			  def (Entry)=>void test() {
			    return null
			  }
			}
		''')
	}

	@Test def testImport_PairOf() {
		'''
			package foo.bar
			import static org.eclipse.xtext.xbase.lib.Pair.*
			class Foo {
			  def Object test() {
			    return of('', '')
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import static org.eclipse.xtext.xbase.lib.Pair.*

			class Foo {
			  def Object test() {
			    return of('', '')
			  }
			}
		''')

	}

	@Test def testArrayType() {
		'''
			package foo.bar
			import java.io.Serializable
			class Foo {
			  def Serializable[][] test() {
			    return null
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import java.io.Serializable

			class Foo {
			  def Serializable[][] test() {
			    return null
			  }
			}
		''')
	}

	@Test def testAnnotationWithSameName() {
		'''
			package foo.bar
			annotation Serializable {
			  Class<? extends java.io.Serializable> clazz
			}
		'''.assertIsOrganizedTo('''
			package foo.bar
			
			annotation Serializable {
			  Class<? extends java.io.Serializable> clazz
			}
		''')
	}

	@Test def testClassWithSameName() {
		'''
			package foo.bar
			class Serializable {
			  Class<? extends java.io.Serializable> clazz
			}
		'''.assertIsOrganizedTo('''
			package foo.bar
			
			class Serializable {
			  Class<? extends java.io.Serializable> clazz
			}
		''')
	}

	@Test def testStaticImport_IterableExtensions() {
		'''
			package foo.bar
			import static org.eclipse.xtext.xbase.lib.IterableExtensions.*
			class Foo {
			  def Object test() {
			    return filter(null, null as Class<?>)
			  }
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import static org.eclipse.xtext.xbase.lib.IterableExtensions.*

			class Foo {
			  def Object test() {
			    return filter(null, null as Class<?>)
			  }
			}
		''')
	}

	@Test def testPropertyAnnotation() {
		'''
			package foo.bar
			class Foo {
			  @Property int prop
			}
		'''.assertIsOrganizedTo('''
			package foo.bar
			
			import org.eclipse.xtend.lib.Property
			
			class Foo {
			  @Property int prop
			}
		''')
	}

	@Test def testNestedAnnotation_01() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider$Runtime

			@Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider.Runtime

			@Runtime
			class Foo {
			}
		''')
	}

	@Test def testNestedAnnotation_02() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider$Runtime

			@org.eclipse.xtext.service.DispatchingProvider$Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider

			@DispatchingProvider.Runtime
			class Foo {
			}
		''')
	}
	
	@Test def testNestedAnnotation_03() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider.Runtime

			@Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider.Runtime

			@Runtime
			class Foo {
			}
		''')
	}

	@Test def testNestedAnnotation_04() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider.Runtime

			@org.eclipse.xtext.service.DispatchingProvider.Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider

			@DispatchingProvider.Runtime
			class Foo {
			}
		''')
	}
	
	@Test def testNestedAnnotation_05() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider.Runtime

			@org.eclipse.xtext.service.DispatchingProvider$Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider

			@DispatchingProvider.Runtime
			class Foo {
			}
		''')
	}
	
	@Test def testNestedAnnotation_06() {
		'''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider$Runtime

			@org.eclipse.xtext.service.DispatchingProvider.Runtime
			class Foo {
			}
		'''.assertIsOrganizedTo('''
			package foo.bar

			import org.eclipse.xtext.service.DispatchingProvider

			@DispatchingProvider.Runtime
			class Foo {
			}
		''')
	}

	@Test def testBug388933_01() {
		'''
			import static extension MyExtensionLib.*

			class MyExtensionLib {
			    def static func1 (String a, int b) {  }
			}
			class MyClient {
			    def sample (String it) { func1(47)  }
			}
		'''.assertIsOrganizedTo('''
			import static extension MyExtensionLib.*

			class MyExtensionLib {
			    def static func1 (String a, int b) {  }
			}
			class MyClient {
			    def sample (String it) { func1(47)  }
			}
		''')
	}

	@Test def testBug388933_02() {
		'''
			import static java.util.Arrays.*

			class C {
			    var x = asList
			}
		'''.assertIsOrganizedTo('''
			import static java.util.Arrays.*

			class C {
			    var x = asList
			}
		''')
	}

	@Test def testBug380637_01() {
		'''
			class MyExtensionLib {
			    def static func1 (String a, int b) {
			       func1(a,b)
			    }
			}
		'''.assertIsOrganizedTo('''
			class MyExtensionLib {
			    def static func1 (String a, int b) {
			       func1(a,b)
			    }
			}
		''')
	}

	@Test def testBug380637_02() {
		'''
			abstract class MyClass implements org.eclipse.xtext.scoping.IScope {
			    org.eclipse.xtext.scoping.IScope scope = NULLSCOPE
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.scoping.IScope

			abstract class MyClass implements IScope {
			    IScope scope = NULLSCOPE
			}
		''')
	}

	@Test def testBug380637_03() {
		'''
			abstract class MyClass extends org.eclipse.xtext.scoping.impl.AbstractScope {
			    new() {
			        super(null, false)
			    }
			    Object field = NULLSCOPE
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.xtext.scoping.impl.AbstractScope

			abstract class MyClass extends AbstractScope {
			    new() {
			        super(null, false)
			    }
			    Object field = NULLSCOPE
			}
		''')
	}

	@Test def testDontOverrideTypeParameter() {
		'''
			class Foo<String> {
				java.lang.String s
			}
		'''.assertIsOrganizedTo('''
			class Foo<String> {
				java.lang.String s
			}
		''')
	}
	
	@Test def testInheritedInnerClass_01() {
		'''
			class Foo extends types.OuterClass {
				types.OuterClass$MiddleClass foo
			}
		'''.assertIsOrganizedTo('''
			import types.OuterClass
			
			class Foo extends OuterClass {
				OuterClass.MiddleClass foo
			}
		''')
	}
	
	@Test def testInheritedInnerClass_02() {
		'''
			class Foo extends types.OuterClass {
				types.OuterClass.MiddleClass foo
			}
		'''.assertIsOrganizedTo('''
			import types.OuterClass
			
			class Foo extends OuterClass {
				OuterClass.MiddleClass foo
			}
		''')
	}

	@Test def testDontOverrideInheritedInnerClass() {
		// 'OuterClass' has an inner class 'MiddleClass'
		// that cannot be shadowed by an import
		'''
			class Foo extends types.OuterClass {
				types.MiddleClass foo
			}
		'''.assertIsOrganizedTo('''
			import types.OuterClass
			
			class Foo extends OuterClass {
				types.MiddleClass foo
			}
		''')
	}
	
	@Test def testEscapedPackageName() {
		'''
			class Foo extends types.^override.EscapePackageName {
			}
		'''.assertIsOrganizedTo('''
			import types.^override.EscapePackageName
			
			class Foo extends EscapePackageName {
			}
		''')
	}
	
	@Test def testJavaDoc() {
		'''
			/**
			 * {@link java.util.List}
			 */
			class Foo {}
		'''.assertIsOrganizedTo('''
			import java.util.List
			
			/**
			 * {@link List}
			 */
			class Foo {}
		''')
	}
	
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=397903 
	@Test def testAssignmentToStaticLocalField() {
		'''
			class Foo {
				static int foo
				def bar() {
					foo = 1
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				static int foo
				def bar() {
					foo = 1
				}
			}
		''')
	}
	
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=397903 
	@Test def testFeatureCallToStaticLocalField() {
		'''
			class Foo {
				static int foo
				def bar() {
					foo
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				static int foo
				def bar() {
					foo
				}
			}
		''')
	}
	
	@Test def testExtensionFeatureCallToStaticLocal() {
		'''
			import static extension Foo.*
			
			class Foo {
				def foo() {
					''.bar
				}
				
				def static bar(String s) {
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def foo() {
					''.bar
				}
				
				def static bar(String s) {
				}
			}
		''')
	}
	
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=399519
	@Test def testExtensionFeatureCallToStaticLocalFieldFromSuper() {
		'''
			import java.util.ArrayList
			
			import static extension java.util.List.*

			class Foo extends ArrayList {
				def foo() {
					''.of
				}
			}
		'''.assertIsOrganizedTo('''
			import java.util.ArrayList
			
			class Foo extends ArrayList {
				def foo() {
					''.of
				}
			}
		''')
	}
	
	@Test def testFunctionType() {
		'''
			class Foo {
				def bar() {
					val (String) => boolean foo = null	
				}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def bar() {
					val (String) => boolean foo = null	
				}
			}
		''')
	}
	
	@Test def testFunctionType_1() {
		'''
			class Foo {
				def bar() {
					val (java.io.File) => java.io.File foo = null	
				}
			}
		'''.assertIsOrganizedTo('''
			import java.io.File
			
			class Foo {
				def bar() {
					val (File) => File foo = null	
				}
			}
		''')
	}
	
	@Test def testWildcard() {
		'''
			class Foo {
				var java.util.List<?> x
			}
		'''.assertIsOrganizedTo('''
			import java.util.List
			
			class Foo {
				var List<?> x
			}
		''')
	}
	
	@Test def testWildcard_1() {
		'''
			class Foo {
				var java.util.List<? extends java.io.File> x
			}
		'''.assertIsOrganizedTo('''
			import java.io.File
			import java.util.List
			
			class Foo {
				var List<? extends File> x
			}
		''')
	}
	
	// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=406086
	@Test def testEscapedKeyword_01() {
		'''
			package pack

			class Bug406086 extends bug406086.^def.Bug406086 {}
		'''.assertIsOrganizedTo('''
			package pack

			class Bug406086 extends bug406086.^def.Bug406086 {}
		''')
	}
	
	@Test def testEscapedKeyword_02() {
		'''
			package pack

			class Bug extends bug406086.^def.Bug406086 {}
		'''.assertIsOrganizedTo('''
			package pack

			import bug406086.^def.Bug406086
			
			class Bug extends Bug406086 {}
		''')
	}
	
	@Test def testEscapedKeyword_03() {
		'''
			package pack

			class Bug406086 {
				val x = bug406086::^def::Bug406086::FIELD
			}
		'''.assertIsOrganizedTo('''
			package pack

			class Bug406086 {
				val x = bug406086.^def.Bug406086::FIELD
			}
		''')
	}
	
	@Test def testTryMultiCatch() {
		'''
			package pack
			
			import java.io.FileNotFoundException
			
			class SomeClass {
				def void test() {
					try {
						println();
					} catch (FileNotFoundException | IllegalArgumentException e) {
						e.printStackTrace
					}
				}
			}
		'''.assertIsOrganizedTo('''
			package pack
			
			import java.io.FileNotFoundException
			
			class SomeClass {
				def void test() {
					try {
						println();
					} catch (FileNotFoundException | IllegalArgumentException e) {
						e.printStackTrace
					}
				}
			}
		''')
	}
	
	@Test def testTryMultiCatch_02() {
		'''
			package pack
			
			class SomeClass {
				def void test() {
					try {
						println();
					} catch (java.io.FileNotFoundException | IllegalArgumentException e) {
						e.printStackTrace
					}
				}
			}
		'''.assertIsOrganizedTo('''
			package pack
			
			import java.io.FileNotFoundException
			
			class SomeClass {
				def void test() {
					try {
						println();
					} catch (FileNotFoundException | IllegalArgumentException e) {
						e.printStackTrace
					}
				}
			}
		''')
	}
	
	@Test def void testSwitchMultiType() {
		'''
		package abc
		
		import java.util.Collection
		import java.util.List
		import java.util.Set
		
		class Sample {
			
			def doit(Collection<String> l) {
				switch(l) {
					List<String> | Set<String> : println("a")
					default: println("b")
				}
			}
			
		}
		'''.assertIsOrganizedTo('''
		package abc
		
		import java.util.Collection
		import java.util.List
		import java.util.Set
		
		class Sample {
			
			def doit(Collection<String> l) {
				switch(l) {
					List<String> | Set<String> : println("a")
					default: println("b")
				}
			}
			
		}
		''')
	}
	
	@Test def void testSwitchMultiType_02() {
		'''
		package abc
		
		class Sample {
			
			def doit(java.util.Collection<java.lang.String> l) {
				switch(l) {
					java.util.List<java.lang.String> | java.util.Set<java.lang.String> : println("a")
					default: println("b")
				}
			}
			
		}
		'''.assertIsOrganizedTo('''
		package abc
		
		import java.util.Collection
		import java.util.List
		import java.util.Set
		
		class Sample {
			
			def doit(Collection<String> l) {
				switch(l) {
					List<String> | Set<String> : println("a")
					default: println("b")
				}
			}
			
		}
		''')
	}
	
	@Test def testBug409648() {
		'''
			import static extension org.eclipse.emf.ecore.util.EcoreUtil.*
			import org.eclipse.emf.ecore.EObject
			import org.eclipse.emf.common.util.URI
			
			class Foo {
			  def foo(EObject it) {
			    URI::createURI("someString")
			  }
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.common.util.URI
			import org.eclipse.emf.ecore.EObject
			
			class Foo {
			  def foo(EObject it) {
			    URI::createURI("someString")
			  }
			}
		''')
	}
	
	@Test def testBug409251() {
		'''
			package foo;

			class Bar implements java.io.Serializable {
			}
		'''.assertIsOrganizedTo('''
			package foo;
			
			import java.io.Serializable

			class Bar implements Serializable {
			}
		''')
	} 
	
	@Test def testBug409251_1() {
		'''
			package foo ;

			class Bar implements java.io.Serializable {
			}
		'''.assertIsOrganizedTo('''
			package foo ;
			
			import java.io.Serializable

			class Bar implements Serializable {
			}
		''')
	} 
	
	@Test def testBug475238 () {
		'''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							foo
						}
					}
				}
				def static foo() {}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							foo
						}
					}
				}
				def static foo() {}
			}
		''')
	}
	
	@Test def testBug475238_1 () {
		'''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							Foo.foo
						}
					}
				}
				def static foo() {}
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							Foo.foo
						}
					}
				}
				def static foo() {}
			}
		''')
	} 
	@Test def testBug475238_2 () {
		'''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							val i = Foo.foo return
						}
					}
				}
				static int foo
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							val i = Foo.foo return
						}
					}
				}
				static int foo
			}
		''')
	} 
	
	@Test def testBug475238_3 () {
		'''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							val i = foo return
						}
					}
				}
				static int foo
			}
		'''.assertIsOrganizedTo('''
			class Foo {
				def bar() {
					new Runnable() {
						override run() {
							val i = foo return
						}
					}
				}
				static int foo
			}
		''')
	} 

	@Test def test_xtext_xtend_issues_536_1() {
		'''
			import org.eclipse.emf.ecore.EObject
			
			import static extension org.eclipse.emf.ecore.util.EcoreUtil.getURI
			
			class Test {
				
				def void doSomething() {
					val EObject e = null
					val a = e.URI // Extended call
					val b = getURI(e) // Static call
				}
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EObject
			
			import static extension org.eclipse.emf.ecore.util.EcoreUtil.getURI
			
			class Test {
				
				def void doSomething() {
					val EObject e = null
					val a = e.URI // Extended call
					val b = getURI(e) // Static call
				}
			}
		''')
	} 


	@Test def test_xtext_xtend_issues_536_2() {
		'''
			import org.eclipse.emf.ecore.EObject
			
			import static org.eclipse.emf.ecore.util.EcoreUtil.*
			
			import static extension org.eclipse.emf.ecore.util.EcoreUtil.getURI
			
			class Test {
				
				def void doSomething() {
					val EObject e = null
					val a = e.URI // Extended call
					val b = getURI(e) // Static call
				}
			}
		'''.assertIsOrganizedTo('''
			import org.eclipse.emf.ecore.EObject
			
			import static extension org.eclipse.emf.ecore.util.EcoreUtil.getURI
			
			class Test {
				
				def void doSomething() {
					val EObject e = null
					val a = e.URI // Extended call
					val b = getURI(e) // Static call
				}
			}
		''')
	} 
}