/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.conversion;

import com.google.common.base.Predicate
import com.google.common.io.Files
import com.google.inject.Inject
import com.google.inject.Provider
import java.io.File
import java.nio.charset.Charset
import java.util.Set
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.core.conversion.JavaConverter
import org.eclipse.xtend.core.tests.AbstractXtendTestCase
import org.eclipse.xtend.core.xtend.XtendFile
import org.eclipse.xtext.mwe.PathTraverser
import org.junit.Test

/**
 * @author dhuebner - Initial contribution and API
 */
public class JavaFileConverterTest extends AbstractXtendTestCase {
	@Inject
	private Provider<JavaConverter> javaConverter;

	@Test
	def void testConvertFilesInThisProject() throws Exception {
		val File projectRoot = new File("").getAbsoluteFile();
		val testProject = new File("/Users/dhuebner/Entwicklung/xtext/master/ws/test-converter")
		System.out.println("Working in " + projectRoot.getPath());
		val PathTraverser pathTraverser = new PathTraverser();
		val Set<URI> allResourceUris = pathTraverser.findAllResourceUris(projectRoot.getAbsolutePath(),
			new Predicate<URI>() {
				override boolean apply(URI input) {
					val fileName = input.toFileString()
					return "java".equals(input.fileExtension()) && !fileName.contains("xtend-gen") &&
						!fileName.contains("ScenarioBug395002");
				}
			});
		for (URI uri : allResourceUris) {
			val File file = new File(uri.toFileString());
			System.out.println("Converting: " + file.getAbsolutePath());
			val String java = Files.toString(file, Charset.defaultCharset());
			val String xtendCode = javaToXtend(java)
			val projectRelative = uri.toFileString().replace(projectRoot.absolutePath, "")
			val targetFile = new File(testProject, projectRelative + ".xtend")
			println("Writing to: " + targetFile.absolutePath)
			if (!targetFile.exists) {
				Files.createParentDirs(targetFile)
				targetFile.createNewFile
			}
			Files.write(xtendCode, targetFile, Charset.defaultCharset)
			file(xtendCode, true);
		}
	}

	def XtendFile toValidFile(String javaCode) throws Exception {
		val String xtendCode = javaToXtend(javaCode)
		System.out.println(xtendCode);
		return file(xtendCode, true);
	}

	def javaToXtend(String javaCode) {
		val JavaConverter j2x = javaConverter.get();
		j2x.toXtend(javaCode);
	}

}
