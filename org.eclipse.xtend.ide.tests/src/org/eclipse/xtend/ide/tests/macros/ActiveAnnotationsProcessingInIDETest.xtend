/*******************************************************************************
 * Copyright (c) 2013, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.macros

import com.google.inject.Inject
import com.google.inject.Provider
import java.nio.charset.StandardCharsets
import org.eclipse.core.resources.IContainer
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl
import org.eclipse.xtend.core.xtend.XtendClass
import org.eclipse.xtend.core.xtend.XtendField
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtend.ide.tests.StopwatchRule
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.editor.hover.html.IEObjectHoverDocumentationProvider
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.util.StringInputStream
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*
import static org.junit.Assert.*

import static extension org.eclipse.xtend.ide.tests.WorkbenchTestHelper.*

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(XtendIDEInjectorProvider))
class ActiveAnnotationsProcessingInIDETest extends AbstractReusableActiveAnnotationTests {

	@Inject
	extension IEObjectHoverDocumentationProvider documentationProvider
	
	@Inject IResourceValidator validator
	
	@Inject WorkbenchTestHelper workbenchTestHelper

	@Test
	def void testDocumentationProvider() {
		assertProcessing(
			'myannotation/ChangeDoc.xtend' -> '''
				package myannotation
				
				import java.util.List
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
				import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.TransformationParticipant
				import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration
				import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration
				
				@Active(ChangeDocProcessor)
				annotation ChangeDoc {
				}
				
				class ChangeDocProcessor implements RegisterGlobalsParticipant<MemberDeclaration>, TransformationParticipant<MutableMemberDeclaration> {
				
					override doRegisterGlobals(List<? extends MemberDeclaration> annotatedSourceElements, RegisterGlobalsContext context) {
						//do nothing
					}
				
					override doTransform(List<? extends MutableMemberDeclaration> annotatedTargetElements,
						extension TransformationContext context) {
						for (it : annotatedTargetElements) {
							docComment = "Hello World!"
						}
					}
				
				}
			''',
			'usercode/UserCode.xtend' -> '''
				package usercode
				
				import myannotation.ChangeDoc
				
				/** 
				 *	 Comment
				 */
				@ChangeDoc
				class UserClass {
				
					private UserClass object
				
				}
			''') [
			val xtendClass = xtendFile.xtendTypes.filter(XtendClass).head
			assertDocumentation('''@<a href="eclipse-xtext-doc:platform:/resource/macroProject/src/myannotation/ChangeDoc.xtend%23/1">ChangeDoc</a><br>Comment''', xtendClass)
			assertDocumentation('''@<a href="eclipse-xtext-doc:platform:/resource/macroProject/src/myannotation/ChangeDoc.xtend%23/1">ChangeDoc</a><br>Hello World!''', xtendClass.members.filter(XtendField).filter[name.equals("object")].head.type.type)
		]
	}
	
	def assertDocumentation(CharSequence charSequence, EObject sourceElement) {
		assertEquals(charSequence.toString, sourceElement.documentation)
	}

	@Inject XtextResourceSetProvider resourceSetProvider
	@Inject Provider<CompilationUnitImpl> compilationUnitProvider

	@Rule public StopwatchRule stopwatch = new StopwatchRule(true);
	
	static IJavaProject macroProject
	static IJavaProject userProject
	
	@BeforeClass
	static def void createProjects() {
		macroProject = JavaCore.create(createPluginProject("macroProject"))
		userProject = JavaCore::create(
			createPluginProject("userProject", "com.google.inject", "org.eclipse.xtend.lib",
				"org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit", "macroProject"))
		macroProject.project.addExportedPackages("myannotation")
	}
	
	@AfterClass
	static def void deleteProjects() {
		cleanWorkspace();
		macroProject = null
		userProject = null
	}

	@After
	def void tearDown() throws Exception {
		workbenchTestHelper.closeAllEditors(false)
		clientFile.delete(true, null)
		macroFile.delete(true, null)
		if ("myannotation" != exportedPackage)
			if (macroProject.project.removeExportedPackages(exportedPackage))
				waitForBuild
	}
	
	IFile macroFile
	IFile clientFile
	String exportedPackage
	
	override assertProcessing(Pair<String, String> macroContent, Pair<String, String> clientContent,
		(CompilationUnitImpl)=>void expectations) {
		
		macroFile = macroProject.newSource(macroContent.key, macroContent.value.toString)
		val lidx = macroContent.key.lastIndexOf('/')
		if (lidx != -1) {
			exportedPackage = macroContent.key.substring(0, lidx).replace('/', '.')
			if(macroProject.project.addExportedPackages(exportedPackage))
				reallyWaitForAutoBuild()
		}
		
		clientFile = userProject.newSource(clientContent.key, clientContent.value.toString)
		waitForBuild()
		
		val resourceSet = resourceSetProvider.get(userProject.project)
		val resource = resourceSet.getResource(URI.createPlatformResourceURI(clientFile.fullPath.toString, true), true)
		EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl)
		validator.validate(resource, CheckMode.FAST_ONLY, CancelIndicator.NullImpl)
		val unit = compilationUnitProvider.get
		unit.xtendFile = resource.contents.filter(XtendFile).head
		expectations.apply(unit)
	}

	def IFile newSource(IJavaProject it, String fileName, String contents) {
		val result = it.project.getFile("src/" + fileName)
		createIfNotExistent(result.parent)
		result.create(new StringInputStream(contents, StandardCharsets.ISO_8859_1.name), true, null)
		return result
	}
	
	private def void createIfNotExistent(IContainer container) {
		if (!container.exists) {
			createIfNotExistent(container.parent)
			(container as IFolder).create(true, false, null)
		}
	}

}
