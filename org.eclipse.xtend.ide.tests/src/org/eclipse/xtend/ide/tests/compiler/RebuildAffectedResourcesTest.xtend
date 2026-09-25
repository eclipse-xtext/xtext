/*******************************************************************************
 * Copyright (c) 2013, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.compiler

import com.google.inject.Inject
import java.util.Set
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase
import org.eclipse.xtend.ide.tests.StopwatchRule
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil
import org.eclipse.xtext.util.StringInputStream
import org.junit.After
import org.junit.Rule
import org.junit.Test

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*

import static extension org.eclipse.ui.texteditor.MarkerUtilities.*
import org.eclipse.xtext.testing.Flaky

class RebuildAffectedResourcesTest extends AbstractXtendUITestCase {
	
	@Rule
	public Flaky.Rule flakyRule = new Flaky.Rule();

	@Inject 
	WorkbenchTestHelper workbenchTestHelper;
	
	Set<IResource> cleanUp = newHashSet();
	
	def <T extends IResource> T registerForCleanUp(T t) {
		cleanUp.add(t)
		return t
	}
	
	@Rule public StopwatchRule rule = new StopwatchRule(true)
	
	@After override void tearDown() {
		workbenchTestHelper.tearDown
		for (res : cleanUp) {
			if (res.exists)
				res.delete(true, null)
		}
	}
	
	@Test def void testRemoveReferencedType() {
		val type1 = workbenchTestHelper.createFile('Type1.xtend', '''
			class Type1 {
			}
		''')
		val type2 = workbenchTestHelper.createFile('Type2.xtend', '''
			class Type2 {
				Type1 myRef
			}
		''')
		waitForBuild()
		assertNoErrorsInWorkspace
		type1.setContents(new StringInputStream('''
			class Type3 {
			}
		'''),true,true, null)
		waitForBuild()
		type2.assertHasErrors("Type1")
	}
	
	@Test def void testAddReferencedType() {
		val type1 = workbenchTestHelper.createFile('Type1.xtend', '''
			class Type3 {
			}
		''')
		val type2 = workbenchTestHelper.createFile('Type2.xtend', '''
			class Type2 {
				Type1 myRef
			}
		''')
		waitForBuild
		type2.assertHasErrors("Type1")
		type1.setContents(new StringInputStream('''
			class Type1 {
			}
		'''),true,true, null)
		waitForBuild()
		assertNoErrorsInWorkspace
	}
	
	@Test def void testAddMethod() {
		val type3 = workbenchTestHelper.createFile('Type3.xtend', '''
			class Type3 {
			}
		''')
		workbenchTestHelper.createFile('Type1.xtend', '''
			class Type1 {
				def Type3 foo() { return new Type3()}
			}
		''')
		val type2 = workbenchTestHelper.createFile('Type2.xtend', '''
			class Type2 {
				def void doStuff() {
					new Type1().foo().bar()
				}
			}
		''')
		waitForBuild()
		type2.assertHasErrors("bar")
		type3.setContents(new StringInputStream('''
			class Type3 {
				def void bar() {}
			}
		'''),true,true, null)
		waitForBuild()
		assertNoErrorsInWorkspace
	}
	
	@Test def void testExtendedClassChanges() {
		val type3 = workbenchTestHelper.createFile('Type3.xtend', '''
			class Type3 {
			}
		''')
		workbenchTestHelper.createFile('Type1.xtend', '''
			class Type1 extends Type3 {
			}
		''')
		val type2 = workbenchTestHelper.createFile('Type2.xtend', '''
			class Type2 {
				def void doStuff(Type1 foo) {
					foo.bar
				}
			}
		''')
		waitForBuild()
		type2.assertHasErrors("bar")
		type3.setContents(new StringInputStream('''
			class Type3 {
				def void bar() {}
			}
		'''),true,true, null)
		waitForBuild()
		assertNoErrorsInWorkspace
		
		type3.setContents(new StringInputStream('''
			class Type3 {
				def void baz() {}
			}
		'''),true,true, null)
		waitForBuild()
		type2.assertHasErrors("bar")
	}
	
	/**
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=388828
	 */
	@Test def void testBug388828() {
		val typeA = workbenchTestHelper.createFile('A.xtend', '''
			abstract class A {}
		''')
		val typeB = workbenchTestHelper.createFile('B.xtend', '''
			class B extends A {
			}
		''')
		waitForBuild()
		assertNoErrorsInWorkspace
		typeA.setContents(new StringInputStream('''
			abst ract class A {
			}
		'''),true,true, null)
		waitForBuild()
		typeA.assertHasErrors('abst')
		typeB.assertHasErrors('A')
		
		typeA.setContents(new StringInputStream('''
			abstract class A {}
		'''),true,true, null)
		waitForBuild()
		assertNoErrorsInWorkspace
	}
	
	@Test
	def void testChangeInAnnotationProcessor() {
		val macroProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-anno").registerForCleanUp)
		macroProject.createFile('src/anno/Anno.xtend', '''
			package anno
			import org.eclipse.xtend.lib.macro.Active
			
			@Active(AnnoProcessor)
			annotation Anno {}
		''')
		val processorClass = macroProject.createFile('src/anno/AnnoProcessor.xtend', '''
			package anno
			import org.eclipse.xtend.lib.macro.AbstractFieldProcessor
			import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
			import org.eclipse.xtend.lib.macro.TransformationContext
			
			class AnnoProcessor extends AbstractFieldProcessor {
				override doTransform(MutableFieldDeclaration it, extension TransformationContext context) {
				}
			}
		''')
		WorkbenchTestHelper.addExportedPackages(macroProject.project, "anno")
		
		val clientProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-client"))
		JavaProjectSetupUtil.addProjectReference(clientProject, macroProject)
		val clientClass = clientProject.createFile('src/Foo.xtend', '''
			import anno.Anno
			class Foo {
				@Anno String foo
				
				def bar() {
					foo - 3
				}
			}
		''')
		waitForBuild
		clientClass.assertHasErrors("- cannot be resolved")
		
		processorClass.setContents(new StringInputStream('''
			package anno
			import org.eclipse.xtend.lib.macro.AbstractFieldProcessor
			import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
			import org.eclipse.xtend.lib.macro.TransformationContext
			
			class AnnoProcessor extends AbstractFieldProcessor {
				override doTransform(MutableFieldDeclaration it, extension TransformationContext context) {
					type = primitiveInt
				}
			}
		'''),true,true, null)
		waitForBuild
		assertNoErrorsInWorkspace
		WorkbenchTestHelper.deleteProject(clientProject.project)
	}
	
	@Test
	def void testChangeInResourceReadFromAnnotationProcessor() {
		val macroProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-anno").registerForCleanUp)
		macroProject.createFile('src/anno/Anno.xtend', '''
			package anno
			import com.google.common.base.Splitter
			import java.util.regex.Pattern
			import org.eclipse.xtend.lib.macro.AbstractClassProcessor
			import org.eclipse.xtend.lib.macro.Active
			import org.eclipse.xtend.lib.macro.TransformationContext
			import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
			
			@Active(AnnoProcessor)
			annotation Anno {
			}
			
			class AnnoProcessor extends AbstractClassProcessor {
				override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {
					val content = context.getContents(cls.compilationUnit.filePath.parent.append("constants.txt"))
					val constants = Splitter.on(Pattern.compile("\\r?\\n")).split(content)
					constants.forEach [ constant |
						cls.addField(constant) [
							static = true
							final = true
							initializer = ['"' + constant + '"']
							type = string
						]
					]
				}
			}
		''')
		WorkbenchTestHelper.addExportedPackages(macroProject.project, "anno")
		
		val clientProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-client").registerForCleanUp)
		JavaProjectSetupUtil.addProjectReference(clientProject, macroProject)
		val constants = clientProject.createFile('src/constants.txt', '''
			A
			B''')
		val fooClass = clientProject.createFile('src/Foo.xtend', '''
			import anno.Anno
			@Anno
			class Foo {
				def test() {
					C
				}
			}
		''')
		waitForBuild
		fooClass.assertHasErrors("C is undefined")
		
		constants.setContents(new StringInputStream('''
			A
			B
			C'''),true,true, null)
		waitForBuild
		assertNoErrorsInWorkspace
		WorkbenchTestHelper.deleteProject(clientProject.project)
	}
	
	@Test @Flaky(trials=3)
	def void testChangeJavaClassLookedUpInAnnotationProcessor() {
		val macroProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-macro").registerForCleanUp)
		macroProject.createFile('src/anno/Anno.xtend', '''
			package anno
			import org.eclipse.xtend.lib.macro.Active
			import org.eclipse.xtend.lib.macro.AbstractClassProcessor
			import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
			import org.eclipse.xtend.lib.macro.TransformationContext
			
			@Active(AnnoProcessor)
			annotation Anno {}
			
			class AnnoProcessor extends AbstractClassProcessor {
				override doTransform(MutableClassDeclaration cls, extension TransformationContext context) {
					val theType = context.findTypeGlobally("anno.A")?:context.findTypeGlobally("anno.B")
					cls.addField("foo") [
						type = theType.newTypeReference
					]
				}
			}
		''')
		macroProject.createFile('src/anno/B.xtend', '''
			package anno
			class B {}
		''')
		WorkbenchTestHelper.addExportedPackages(macroProject.project, "anno")
		
		val clientProject = JavaCore.create(WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME + "-client").registerForCleanUp)
		JavaProjectSetupUtil.addProjectReference(clientProject, macroProject)
		val fooClass = clientProject.createFile('src/Foo.xtend', '''
			import anno.Anno
			@Anno
			class Foo {
				def test() {
					foo.bar
				}
			}
		''')
		waitForBuild
		fooClass.assertHasErrors("bar is undefined")
		
		macroProject.createFile('src/anno/A.xtend', '''
			package anno
			class A {
				def bar() {}
			}
		''')
		waitForBuild
		assertNoErrorsInWorkspace
		WorkbenchTestHelper.deleteProject(clientProject.project)
	}
	
	def IFile createFile(IJavaProject jp, String path, String contents) {
		workbenchTestHelper.createFileImpl(jp.project.fullPath.toString + "/"+ path, contents)
	}
	
	
	def void assertNoErrorsInWorkspace() {
		val findMarkers = ResourcesPlugin.workspace.root.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		for (iMarker : findMarkers) {
			assertFalse(iMarker.message, iMarker.severity == IMarker.SEVERITY_ERROR)
		}
	}
	
	def void assertHasErrors(IFile file, String msgPart) {
		val findMarkers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)
		for (iMarker : findMarkers) {
			if (iMarker.severity == IMarker.SEVERITY_ERROR && iMarker.message.contains(msgPart)) {
				return;
			}
		}
		fail("Expected an error marker containing '"+msgPart+"' on "+file.fullPath+" but found "+findMarkers.map[message].join(','))
	}
	
}