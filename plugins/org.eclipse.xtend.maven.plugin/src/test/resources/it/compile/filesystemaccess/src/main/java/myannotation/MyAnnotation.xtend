package myannotation

import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

import static org.junit.Assert.*

import static extension org.eclipse.xtend.core.macro.fsaccess.IOUtils.*

@Active(typeof(MyAnnotationProcessor))
annotation MyAnnotation {
}

class MyAnnotationProcessor extends AbstractClassProcessor {

	override doTransform(MutableClassDeclaration clazz, extension TransformationContext context) {
		assertNotNull(sourceFolder)
		assertTrue(sourceFolder.exists)
		assertEquals("java", sourceFolder.name)
		assertTrue(sourceFolder.path.endsWith("/it/compile/filesystemaccess-client/src/main/java"))

		assertNotNull(targetFolder)
		assertTrue(targetFolder.exists)
		assertEquals("xtend", targetFolder.name)
		assertTrue(targetFolder.path.endsWith("/it/compile/filesystemaccess-client/src/main/generated-sources/xtend"))

		assertNotNull(rootFolder)
		assertTrue(rootFolder.exists)
		assertEquals("filesystemaccess-client", rootFolder.name)
		assertTrue(rootFolder.path.endsWith("/it/compile/filesystemaccess-client"))

		val myusercodeFolder = sourceFolder.getFolder("myusercode")
		assertNotNull(myusercodeFolder)
		assertTrue(myusercodeFolder.exists)
		assertEquals("myusercode", myusercodeFolder.name)
		assertTrue(myusercodeFolder.path.endsWith("/it/compile/filesystemaccess-client/src/main/java/myusercode"))

		try {
			sourceFolder.getFolder("myusercode/UserCode.xtend")
			fail
		} catch (IllegalStateException e) {
			assertTrue(e.message.contains("/it/compile/filesystemaccess-client/src/main/java/myusercode/UserCode.xtend"))
		}

		val userCodeFile = sourceFolder.getFile("myusercode/UserCode.xtend")
		assertNotNull(userCodeFile)
		assertTrue(userCodeFile.exists)
		assertEquals("UserCode.xtend", userCodeFile.name)
		assertTrue(
			userCodeFile.path.endsWith("/it/compile/filesystemaccess-client/src/main/java/myusercode/UserCode.xtend"))

		try {
			sourceFolder.getFile("myusercode")
			fail
		} catch (IllegalStateException e) {
			assertTrue(e.message.contains("/it/compile/filesystemaccess-client/src/main/java/myusercode"))
		}

		val userCodeContent = '''
			package myusercode
			
			@myannotation.MyAnnotation
			class MyClass {
			}
		'''
		assertEquals(userCodeContent, userCodeFile.contents)
		userCodeFile.read[assertEquals(userCodeContent, asString)]

		val userCodeCss = sourceFolder.getFile("myusercode/UserCode.css")
		assertNotNull(userCodeCss)
		assertFalse(userCodeCss.exists)
		assertEquals("UserCode.css", userCodeCss.name)
		assertTrue(
			userCodeCss.path.endsWith("/it/compile/filesystemaccess-client/src/main/java/myusercode/UserCode.css"))
		val helloWorldCssClassDeclaration = ".helloWorldCssClass {}"
		userCodeCss.writeContents[helloWorldCssClassDeclaration as CharSequence]

		val userCodeCss2 = sourceFolder.getFile("myusercode/UserCode.css")
		assertNotNull(userCodeCss2)
		assertTrue(userCodeCss2.exists)
		assertEquals(helloWorldCssClassDeclaration, userCodeCss2.contents)

		val userCode2Css = sourceFolder.getFile("com/itemis/myusercode/UserCode2.css")
		assertNotNull(userCode2Css)
		assertFalse(userCode2Css.exists)
		assertEquals("UserCode2.css", userCode2Css.name)
		assertTrue(
			userCode2Css.path.endsWith(
				"/it/compile/filesystemaccess-client/src/main/java/com/itemis/myusercode/UserCode2.css"))
		userCode2Css.write [
			tryWith([|writer.buffered]) [
				write(helloWorldCssClassDeclaration)
			]
		]

		val userCode2Css2 = sourceFolder.getFile("com/itemis/myusercode/UserCode2.css")
		assertNotNull(userCode2Css2)
		assertTrue(userCode2Css2.exists)
		assertEquals(helloWorldCssClassDeclaration, userCode2Css2.contents)
	}

}
