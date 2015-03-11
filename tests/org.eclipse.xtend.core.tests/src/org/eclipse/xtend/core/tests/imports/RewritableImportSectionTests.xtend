package org.eclipse.xtend.core.tests.imports

import com.google.inject.Inject
import java.util.Collections
import java.util.List
import java.util.Set
import org.eclipse.xtend.core.tests.AbstractXtendTestCase
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.util.TypeReferences
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.xbase.imports.RewritableImportSection
import org.junit.Test

class RewritableImportSectionTest_0 extends AbstractRewritableImportSectionTest {

	override protected getModel(boolean isStatic, Class<? extends Object>[] types) '''
		package foo
		
		�FOR type : types�
			�IF(isStatic)�
				import static �type.canonicalName�.*
			�ELSE�
				import �type.canonicalName�
			�ENDIF�
		�ENDFOR�
		
		class Foo{}
	'''

	override protected getExpectedModel(String sectionAsString) '''
		package foo
		
		�sectionAsString�
		
		class Foo{}
	'''
}

class RewritableImportSectionTest_1 extends AbstractRewritableImportSectionTest {

	override protected getModel(boolean isStatic, Class<? extends Object>[] types) '''
		�IF types.size > 0�
			�FOR type: types�
				�IF(isStatic)�
					import static �type.canonicalName�.*
				�ELSE�
					import �type.canonicalName�
				�ENDIF�
			�ENDFOR�
			
		�ENDIF�
		class Foo{}
	'''

	override protected getExpectedModel(String sectionAsString) '''
		�sectionAsString�
		
		class Foo{}
	'''
}

class RewritableImportSectionTest_2 extends AbstractRewritableImportSectionTest {

	override protected isIgnoreLinebreaks() {
		// The RIS will insert newlines before and after the section depending on added / removed imports.
		// It doesn't make sense to check for those, but we must make sure there is at least one whitespace
		// to separate elements  
		true
	}

	override protected getModel(boolean isStatic, Class<? extends Object>[] types) '''
		package foo
		�FOR type : types�
			�IF(isStatic)�
				import static �type.canonicalName�.*
			�ELSE�
				import �type.canonicalName�
			�ENDIF�
		�ENDFOR�
		class Foo{}
	'''

	override protected getExpectedModel(String sectionAsString) '''
		package foo
		�sectionAsString�
		
		class Foo{}
	'''
}

abstract class AbstractRewritableImportSectionTest extends AbstractXtendTestCase {

	@Inject extension RewritableImportSection.Factory

	@Inject extension TypeReferences
	XtendFile xtendFile

	String model

	@Inject IWhitespaceInformationProvider whitespaceInformationProvider

	@Test def testSimpleAdd() {
		val section = getSection(typeof(Set))
		section.addImport(typeof(List))
		section.assertEquals('''
			import java.util.Set
			import java.util.List
		''')
	}

	@Test def testSimpleAddAsString() {
		val section = getSection(typeof(Set))
		section.addImport("java.util.List")
		section.assertEquals('''
			import java.util.Set
			import java.util.List
		''')
	}

	@Test def testSimpleAddAsString_1() {
		val section = getSection(typeof(Set))
		section.addImport("org.eclipse.xtext.xbase.lib.InputOutput")
		section.assertEquals('''
			import java.util.Set
		''')
	}

	@Test def void testAddNullMemberAsString() {
		val section = getSection(typeof(Set))
		try {
			section.addStaticImport("java.util.List", null)
			fail
		} catch (IllegalArgumentException e) {
		}
		try {
			section.addStaticExtensionImport("java.util.List", null)
			fail
		} catch (IllegalArgumentException e) {
		}
	}

	@Test def void testAddNullTypeAsString() {
		val section = getSection(typeof(Set))
		try {
			section.addStaticExtensionImport(null as String, "")
			fail
		} catch (IllegalArgumentException e) {
		}
		try {
			section.addStaticImport(null as String, "")
			fail
		} catch (IllegalArgumentException e) {
		}
	}

	@Test def testVariousAdd() {
		val section = getSection()
		section.addExtensionImport(typeof(Set))
		section.addStaticImport(typeof(Collections))
		section.addImport(typeof(List))
		section.assertEquals('''
			import static extension java.util.Set.*
			import static java.util.Collections.*
			import java.util.List
		''')
	}

	@Test def testVariousAdd_2() {
		val section = getSection()
		section.addExtensionImport(typeof(Set))
		section.addStaticImport(typeof(Set))
		section.addImport(typeof(Set))
		section.assertEquals('''
			import static extension java.util.Set.*
			import java.util.Set
		''')
	}

	@Test def testVariousAdd_3() {
		val section = getSection()
		section.addStaticImport(typeof(Set))
		section.addExtensionImport(typeof(Set))
		section.addImport(typeof(Set))
		section.assertEquals('''
			import static extension java.util.Set.*
			import java.util.Set
		''')
	}

	@Test def testVariousAddAsString() {
		val section = getSection()
		section.addStaticExtensionImport('java.util.Set', '*')
		section.addStaticImport('java.util.Collections', '*')
		section.addStaticImport('org.eclipse.xtext.xbase.lib.InputOutput', 'println')
		section.addImport('java.util.List')
		section.assertEquals('''
			import static extension java.util.Set.*
			import static java.util.Collections.*
			import java.util.List
		''')
	}

	@Test def testVariousAddAsString_2() {
		val section = getSection()
		section.addStaticExtensionImport('java.util.Set', '*')
		section.addStaticImport('java.util.Collections', '*')
		section.addStaticImport('org.eclipse.xtext.xbase.lib.InputOutput', 'println')
		section.addImport('java.util.List')
		section.assertEquals('''
			import static extension java.util.Set.*
			import static java.util.Collections.*
			import java.util.List
		''')
	}

	@Test def testVariousAddAsString_3() {
		val section = getSection()
		section.addStaticExtensionImport('com.google.common.base.Strings', '*')
		section.addStaticImport('com.google.common.base.Strings', '*')
		section.addStaticImport('com.google.common.base.Strings', 'emptyToNull')
		section.addImport('com.google.common.base.Strings')
		section.assertEquals('''
			import static extension com.google.common.base.Strings.*
			import com.google.common.base.Strings
		''')
	}

	@Test def testVariousAddAsString_4() {
		val section = getStaticSection(typeof(Collections))
		section.addStaticImport('java.util.Collections', '*')
		section.addStaticImport('java.util.Collections', 'sort')
		section.addImport('java.util.Collections')
		section.assertEquals(
		'''
			import static java.util.Collections.*
			import java.util.Collections
		''')
	}

	@Test def testDoubleAdd() {
		val section = getSection(typeof(List))
		section.addImport(typeof(List))
		section.addImport(typeof(List))
		section.assertEquals('''
			import java.util.List
		''')
	}

	@Test def testDoubleAdd_2() {
		val section = getSection()
		section.addExtensionImport(typeof(Collections))
		section.addExtensionImport(typeof(Collections))
		section.assertEquals('''
			import static extension java.util.Collections.*
		''')
	}

	@Test def testDoubleAdd_3() {
		val section = getSection()
		section.addStaticImport(typeof(Collections))
		section.addStaticImport(typeof(Collections))
		section.assertEquals('''
			import static java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString() {
		val section = getSection(typeof(List))
		section.addImport('java.util.List')
		section.addImport('java.util.List')
		section.assertEquals('''
			import java.util.List
		''')
	}

	@Test def testDoubleAddAsString_2() {
		val section = getSection()
		section.addStaticExtensionImport('java.util.Collections', '*')
		section.addStaticExtensionImport('java.util.Collections', '*')
		section.assertEquals('''
			import static extension java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString_3() {
		val section = getSection()
		section.addStaticImport('java.util.Collections', '*')
		section.addStaticImport('java.util.Collections', '*')
		section.assertEquals('''
			import static java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString_4() {
		val section = getSection(typeof(Collections))
		section.addStaticImport('java.util.Collections', '*')
		section.addStaticImport('java.util.Collections', 'sort')
		section.assertEquals('''
			import java.util.Collections
			import static java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString_5() {
		val section = getSection(typeof(Collections))
		section.addStaticImport('java.util.Collections', 'sort')
		section.addStaticImport('java.util.Collections', '*')
		section.assertEquals('''
			import java.util.Collections
			import static java.util.Collections.sort
			import static java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString_6() {
		val section = getSection(typeof(Collections))
		section.addStaticExtensionImport('java.util.Collections', '*')
		section.addStaticExtensionImport('java.util.Collections', 'sort')
		section.assertEquals('''
			import java.util.Collections
			import static extension java.util.Collections.*
		''')
	}

	@Test def testDoubleAddAsString_7() {
		val section = getSection(typeof(Collections))
		section.addStaticExtensionImport('java.util.Collections', 'sort')
		section.addStaticExtensionImport('java.util.Collections', '*')
		section.assertEquals('''
			import java.util.Collections
			import static extension java.util.Collections.sort
			import static extension java.util.Collections.*
		''')
	}

	@Test def testSort() {
		val section = getSection()
		section.addExtensionImport(typeof(Set))
		section.addImport(typeof(Set))
		section.addStaticImport(typeof(Collections))
		section.addImport(typeof(List))
		section.sort = true
		section.assertEquals('''
			import java.util.List
			import java.util.Set
			
			import static java.util.Collections.*
			
			import static extension java.util.Set.*
		''')
	}

	@Test def testSimpleRemove() {
		val section = getSection(typeof(List), typeof(Set))
		section.removeImport(typeof(Set))
		section.assertEquals('''
			import java.util.List
		''')
	}

	@Test def testRemoveAll() {
		val section = getSection(typeof(List), typeof(Set))
		section.removeImport(typeof(Set))
		section.removeImport(typeof(List))
		section.assertEquals('''
		''')
	}

	@Test def testRemoveAdd() {
		val section = getSection(typeof(List))
		section.removeImport(typeof(List))
		section.addImport(typeof(List))
		section.assertEquals('''
			import java.util.List
		''')
	}

	@Test def testRemoveAdd_2() {
		val section = getSection(typeof(List), typeof(Set))
		section.removeImport(typeof(List))
		section.addImport(typeof(List))
		section.assertEquals('''
			import java.util.Set
			import java.util.List
		''')
	}

	@Test def testRemoveAddRemove() {
		val section = getSection(typeof(List))
		section.removeImport(typeof(List))
		section.addImport(typeof(List))
		section.removeImport(typeof(List))
		section.assertEquals('''
		''')
	}

	@Test def testRemoveAddRemoveAdd() {
		val section = getSection(typeof(List))
		section.removeImport(typeof(List))
		section.addImport(typeof(List))
		section.removeImport(typeof(List))
		section.addImport(typeof(List))
		section.assertEquals('''
			import java.util.List
		''')
	}

	@Test def testRemoveAdd_3() {
		val section = getSection(typeof(List))
		section.removeImport(typeof(List))
		section.addStaticImport(typeof(List))
		section.assertEquals('''
			import static java.util.List.*
		''')
	}

	@Test def testRemoveAdd_4() {
		val section = getSection(typeof(List))
		section.removeImport(typeof(List))
		section.addStaticImport(typeof(List))
		section.addExtensionImport(typeof(List))
		section.removeStaticImport(typeof(List))
		section.assertEquals('''
			import static extension java.util.List.*
		''')
	}

	@Test def testRenameRefactoringScenario() {
		val section = getSection(typeof(List))
		val importedType = section.getImportedTypes("List").head
		assertNotNull(importedType)
		importedType.setSimpleName("NewList")
		section.removeImport(importedType)
		section.addImport(importedType)
		section.assertEquals('''
			import java.util.NewList
		''')
	}

	def protected getImportSection(CharSequence model) {
		parse(file(model.toString).eResource as XtextResource)
	}

	def protected addImport(RewritableImportSection section, Class<?> javaClass) {
		section.addImport(jvmType(javaClass))
	}

	def protected removeImport(RewritableImportSection section, Class<?> javaClass) {
		section.removeImport(jvmType(javaClass))
	}

	def protected addStaticImport(RewritableImportSection section, Class<?> javaClass) {
		section.addStaticImport(jvmType(javaClass))
	}

	def protected removeStaticImport(RewritableImportSection section, Class<?> javaClass) {
		section.removeStaticImport(jvmType(javaClass), null)
	}

	def protected addExtensionImport(RewritableImportSection section, Class<?> javaClass) {
		section.addStaticExtensionImport(jvmType(javaClass))
	}

	def protected removeExtensionImport(RewritableImportSection section, Class<?> javaClass) {
		section.removeStaticExtensionImport(jvmType(javaClass), null)
	}

	def protected jvmType(Class<? extends Object> javaClass) {
		val type = findDeclaredType(javaClass, xtendFile)
		assertTrue(type instanceof JvmDeclaredType)
		type as JvmDeclaredType
	}

	def protected getSection(Class<?>... types) {
		model = getModel(false, types).toString
		xtendFile = file(model)
		parse(xtendFile.eResource as XtextResource)
	}

	def protected getStaticSection(Class<?>... types) {
		model = getModel(true, types).toString
		xtendFile = file(model)
		parse(xtendFile.eResource as XtextResource)
	}

	def protected assertEquals(RewritableImportSection section, CharSequence sectionAsString) {
		val builder = new StringBuilder(model)
		val changes = section.rewrite
		for (it : changes.sortBy[offset].reverse)
			builder.replace(offset, offset + length, text)
		assertEquals(getExpectedModel(sectionAsString.toString).processLinebreaks, builder.processLinebreaks)
	}

	def protected processLinebreaks(CharSequence sequence) {
		val lineSeparator = whitespaceInformationProvider.getLineSeparatorInformation(xtendFile.eResource.URI).
			lineSeparator
		if (ignoreLinebreaks)
			sequence.toString.replaceAll("(" + lineSeparator + ")+", " ")
		else
			sequence.toString
	}

	def protected isIgnoreLinebreaks() {
		false
	}

	def protected CharSequence getModel(boolean isStatic, Class<? extends Object>[] types)

	def protected CharSequence getExpectedModel(String sectionAsString)
}