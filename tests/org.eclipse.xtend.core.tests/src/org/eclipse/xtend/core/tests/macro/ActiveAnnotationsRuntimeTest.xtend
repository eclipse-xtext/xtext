package org.eclipse.xtend.core.tests.macro

import com.google.common.collect.Lists
import com.google.inject.Inject
import com.google.inject.Provider
import java.io.File
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.core.macro.AnnotationProcessor
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl
import org.eclipse.xtend.core.tests.RuntimeInjectorProvider
import org.eclipse.xtend.core.validation.IssueCodes
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration
import org.eclipse.xtend.lib.macro.file.MutableFileSystemSupport
import org.eclipse.xtend.lib.macro.file.Path
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.TemporaryFolder
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.Issue
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper
import org.eclipse.xtext.xbase.file.ProjectConfig
import org.eclipse.xtext.xbase.file.RuntimeWorkspaceConfigProvider
import org.eclipse.xtext.xbase.file.WorkspaceConfig
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(RuntimeInjectorProvider)
class ActiveAnnotationsRuntimeTest extends AbstractReusableActiveAnnotationTests {

	@Rule 
	@Inject public TemporaryFolder temporaryFolder

	@Inject CompilationTestHelper compiler
	@Inject Provider<CompilationUnitImpl> compilationUnitProvider
	@Inject ProcessorInstanceForJvmTypeProvider processorProvider
	@Inject Provider<XtextResourceSet> resourceSetProvider;
	@Inject RuntimeWorkspaceConfigProvider configProvider
	@Inject extension MutableFileSystemSupport fileSystemSupport
	
	val macroProject = "macroProject"
	val clientProject = "userProject"

	@Before
	def void setUp() {
		compiler.setJavaCompilerClassPath(typeof(MutableTypeDeclaration), typeof(IterableExtensions), typeof(Lists))
		configureFreshWorkspace()
	}
	
	protected def void configureFreshWorkspace() {
		val tempDir = createFreshTempDir()
		configProvider.workspaceConfig = new WorkspaceConfig(tempDir.absolutePath) => [
			addProjectConfig(new ProjectConfig(macroProject) => [
				sourceFolderMappings.put(new Path('/'+macroProject+'/src'), new Path('/'+macroProject+'/xtend-gen'))
			])
			addProjectConfig(new ProjectConfig(clientProject) => [
				sourceFolderMappings.put(new Path('/'+clientProject+'/src'), new Path('/'+clientProject+'/xtend-gen'))
			])
		]
	}
	
	protected def File createFreshTempDir() {
		return temporaryFolder.newFolder()
	}
	
	protected def URI copyToDisk(String projectName, Pair<String,String> fileRepresentation) {
		val path = new Path("/"+projectName+"/src/"+fileRepresentation.key)
		path.setContents(fileRepresentation.value)
		return URI.createFileURI(configProvider.workspaceConfig.absoluteFileSystemPath+path.toString)
	}

	override assertProcessing(Pair<String, String> macroFile, Pair<String, String> clientFile, (CompilationUnitImpl)=>void expectations) {
		val resourceSet = macroFile.compileMacroResourceSet(clientFile)
		val singleResource = resourceSet.resources.head
		singleResource.load(emptyMap)
		compiler.compile(resourceSet) [
			it.generatedCode // ensure everything is compiled
			val unit = compilationUnitProvider.get
			val xtendFile = singleResource.contents.filter(XtendFile).head
			unit.setXtendFile(xtendFile)
			expectations.apply(unit)
		]
	}
	
	def assertIssues(Pair<String, String> macroFile, Pair<String, String> clientFile, (List<Issue>)=>void expectations) {
		val resourceSet = macroFile.compileMacroResourceSet(clientFile)
		val singleResource = resourceSet.resources.head as XtextResource
		if (!singleResource.isLoaded()) {
			singleResource.load(resourceSet.getLoadOptions());
		}
		val validator = singleResource.resourceServiceProvider.resourceValidator
		expectations.apply(validator.validate(singleResource, CheckMode.ALL, CancelIndicator.NullImpl))
	}
	
	def compileMacroResourceSet(Pair<String, String> macroFile, Pair<String, String> clientFile) {
		val macroURI = copyToDisk(macroProject, macroFile)
		val clientURI = copyToDisk(clientProject, clientFile)
		
		val macroResourceSet = resourceSetProvider.get
		macroResourceSet.classpathURIContext = getClass.classLoader
		macroResourceSet.createResource(macroURI)
		
		val resourceSet = resourceSetProvider.get
		resourceSet.createResource(clientURI)
		
		compiler.compile(macroResourceSet) [ result |
			val classLoader = new DelegatingClassloader(getClass().classLoader, result)
			resourceSet.classpathURIContext = classLoader
			processorProvider.classLoader = classLoader
		]
		
		resourceSet
	}

	// dummy override to enable launch configs
	@Test override testSimpleModification() {
		super.testSimpleModification()
	}

	@Test def void testBug403563() {
		assertIssues(
			'myannotation/ThrowExceptionAnnotationProcessor.xtend' -> '''
				package myannotation
				
				import org.eclipse.xtend.lib.macro.Active
				import org.eclipse.xtend.lib.macro.TransformationContext
				import org.eclipse.xtend.lib.macro.AbstractClassProcessor
				import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
				
				@Active(ThrowExceptionAnnotationProcessor)
				annotation ThrowExceptionAnnotation{ }
				class ThrowExceptionAnnotationProcessor extends AbstractClassProcessor {
				
					override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
						throw new IllegalStateException
					}
				
				}
			''',
			'myusercode/UserCode.xtend' -> '''
				package myusercode
				
				@myannotation.ThrowExceptionAnnotation class MyClass {
				}
			'''
		) [ 
			assertEquals(1, size)
			
			val error = head
			assertNotNull(error)
			assertEquals(IssueCodes.PROCESSING_ERROR, error.code)
			assertTrue(error.message.contains(IllegalStateException.name))
			assertFalse(error.message.contains(AnnotationProcessor.name))
		]
	}

}

class DelegatingClassloader extends ClassLoader {
	CompilationTestHelper.Result classFinder

	new(ClassLoader parent, CompilationTestHelper.Result classFinder) {
		super(parent)
		this.classFinder = classFinder
	}
	
	override protected findResource(String name) {
		val result = classFinder.classLoader.getResource(name)
		return result ?: super.findResource(name)
	}
	
	override findClass(String name) throws ClassNotFoundException {
		val result = classFinder.getCompiledClass(name)
		if (result != null)
			return result
		super.findClass(name)
	}

	override loadClass(String name) throws ClassNotFoundException {
		super.loadClass(name)
	}

}
