package org.eclipse.xtend.ide.tests.macros

import com.google.inject.Inject
import com.google.inject.Provider
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.jar.Manifest
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl
import org.eclipse.xtend.core.tests.macro.AbstractReusableActiveAnnotationTests
import org.eclipse.xtend.core.xtend.XtendClass
import org.eclipse.xtend.core.xtend.XtendField
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.internal.StopwatchRule
import org.eclipse.xtext.ui.editor.hover.html.IEObjectHoverDocumentationProvider
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.util.StringInputStream
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static org.eclipse.xtend.ide.tests.WorkbenchTestHelper.*
import static org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil.*
import static org.junit.Assert.*
import org.eclipse.core.resources.IContainer

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(XtendIDEInjectorProvider))
class ActiveAnnotationsProcessingInIDETest extends AbstractReusableActiveAnnotationTests {

	@Inject
	private extension IEObjectHoverDocumentationProvider documentationProvider
	
	@Inject IResourceValidator validator

	@Test
	def void testDocumentationProvider() {
		assertProcessing(
			'annotation/ChangeDoc.xtend' -> '''
				package annotation
				
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
				
				import annotation.ChangeDoc
				
				/** 
				 *	 Comment
				 */
				@ChangeDoc
				class UserClass {
				
					private UserClass object
				
				}
			''') [
			val xtendClass = xtendFile.xtendTypes.filter(XtendClass).head
			assertDocumentation('''@<a href="eclipse-xtext-doc:platform:/resource/macroProject/src/annotation/ChangeDoc.xtend%23/1">ChangeDoc</a><br>Comment''', xtendClass)
			assertDocumentation('''@<a href="eclipse-xtext-doc:platform:/resource/macroProject/src/annotation/ChangeDoc.xtend%23/1">ChangeDoc</a><br>Hello World!''', xtendClass.members.filter(XtendField).filter[name.equals("object")].head.type.type)
		]
	}
	
	def assertDocumentation(CharSequence charSequence, EObject sourceElement) {
		assertEquals(charSequence.toString, sourceElement.documentation)
	}

	@Inject XtextResourceSetProvider resourceSetProvider
	@Inject Provider<CompilationUnitImpl> compilationUnitProvider

	@Rule public StopwatchRule stopwatch = new StopwatchRule(true);

	@After def tearDown() throws Exception {
		macroProject = null
		userProject = null
		sourceFile = null
		cleanWorkspace();
	}

	IJavaProject macroProject
	IJavaProject userProject
	IFile sourceFile
	
	override assertProcessing(Pair<String, String> macroFile, Pair<String, String> clientFile,
		(CompilationUnitImpl)=>void expectations) {
		macroProject = JavaCore.create(createPluginProject("macroProject"))
		macroProject.newSource(macroFile.key, macroFile.value.toString)
		val lidx = macroFile.key.lastIndexOf('/')
		if (lidx != -1) {
			val packageName = macroFile.key.substring(0, lidx).replace('/', '.')
			macroProject.addExportedPackage(packageName)
		}
		userProject = JavaCore::create(
			createPluginProject("userProject", "com.google.inject", "org.eclipse.xtend.lib",
				"org.eclipse.xtend.core.tests",
				"org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit", "macroProject"))
		sourceFile = userProject.newSource(clientFile.key, clientFile.value.toString)
		waitForAutoBuild()
		
		val resourceSet = resourceSetProvider.get(userProject.project)
		val resource = resourceSet.getResource(URI.createPlatformResourceURI(sourceFile.fullPath.toString, true), true)
		EcoreUtil2.resolveLazyCrossReferences(resource, CancelIndicator.NullImpl)
		validator.validate(resource, CheckMode.FAST_ONLY, CancelIndicator.NullImpl)
		val unit = compilationUnitProvider.get
		unit.xtendFile = resource.contents.filter(XtendFile).head
		expectations.apply(unit)
	}

	def IFile newSource(IJavaProject it, String fileName, String contents) {
		val result = it.project.getFile("src/" + fileName)
		createIfNotExistent(result.parent)
		result.create(new StringInputStream(contents), true, null)
		return result
	}
	
	private def void createIfNotExistent(IContainer container) {
		if (!container.exists) {
			createIfNotExistent(container.parent)
			(container as IFolder).create(true, false, null)
		}
	}

	def void addExportedPackage(IJavaProject pluginProject, String ... exportedPackages) {
		val manifestFile = pluginProject.project.getFile("META-INF/MANIFEST.MF")
		val manifestContent = manifestFile.contents
		val manifest = try {
			new Manifest(manifestContent)
		} finally {
			manifestContent.close
		}
		val attrs = manifest.getMainAttributes()
		if (attrs.containsKey("Export-Package")) {
			attrs.putValue("Export-Package", attrs.get("Export-Package") + "," + exportedPackages.join(","))
		} else {
			attrs.putValue("Export-Package", exportedPackages.join(","))
		}
		val out = new ByteArrayOutputStream()
		manifest.write(out)
		val in = new ByteArrayInputStream(out.toByteArray)
		manifestFile.setContents(new BufferedInputStream(in), true, true, null)
	}

}
