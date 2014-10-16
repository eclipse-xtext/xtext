/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.conversion

import com.google.inject.Inject
import java.net.URLClassLoader
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser

import org.eclipse.jdt.core.ICompilationUnit

/**
 * @author Dennis H�bner - Initial contribution and API
 */
class JavaConverter {

	@Inject JavaASTFlattener astFlattener
	String complianceLevel = "1.5"
	boolean fallbackConversionStartegy = false

	def ConversionResult toXtend(ICompilationUnit cu) {
		val parser = ASTParser.newParser(AST.JLS3)
		val options = JavaCore.getOptions()
		JavaCore.setComplianceOptions(complianceLevel, options)
		parser.compilerOptions = options
		parser.statementsRecovery = true
		parser.resolveBindings = true
		parser.bindingsRecovery = true
		parser.source = cu
		astFlattener.reset
		astFlattener.useFallBackStrategy = fallbackConversionStartegy
		astFlattener.setJavaSources(cu.source)
		parser.createAST(null).accept(astFlattener)
		val result = ConversionResult.create(astFlattener)
		return result

	}

	def ConversionResult toXtend(String unitName, String javaSrc) {
		toXtend(unitName, javaSrc, ASTParser.K_COMPILATION_UNIT)
	}

	/**
 	* @param unitName some CU name e.g. Clazz. See org.eclipse.jdt.core.dom.ASTParser.setUnitName(String)
 	* @param javaSrc Java source code as String
 	* @param javaSourceKind ASTParser.K_COMPILATION_UNIT || ASTParser.K_CLASS_BODY_DECLARATION
 	*/
	def ConversionResult toXtend(String unitName, String javaSrc, int javaSourceKind) {
		val parser = ASTParser.newParser(AST.JLS3)
		val options = JavaCore.getOptions()
		JavaCore.setComplianceOptions(complianceLevel, options)
		parser.compilerOptions = options
		parser.statementsRecovery = true
		parser.resolveBindings = true
		parser.bindingsRecovery = true

		val sysClassLoader = ClassLoader.getSystemClassLoader();
		val cpEntries = (sysClassLoader as URLClassLoader).getURLs().map[file]
		parser.setEnvironment(cpEntries, null, null, true)

		parser.kind = javaSourceKind
		parser.unitName = unitName
		parser.source = javaSrc.toCharArray

		astFlattener.reset
		astFlattener.useFallBackStrategy = fallbackConversionStartegy
		astFlattener.setJavaSourceKind(javaSourceKind)
		astFlattener.setJavaSources(javaSrc)
		parser.createAST(null).accept(astFlattener)
		return ConversionResult.create(astFlattener)
	}

	static class ConversionResult {
		String xtendCode
		Iterable<String> problems = newArrayList()

		def getXtendCode() {
			xtendCode
		}

		def getProblems() {
			problems
		}

		def static create(JavaASTFlattener flattener) {
			val result = new ConversionResult
			result.xtendCode = flattener.result
			if (flattener.problems != null)
				result.problems = flattener.problems
			return result
		}

	}

	/**
	 * @param fallBackStrategy - if <code>true</code> JavaConverter uses a strategy which is less error prone,<br>
	 *  but may produces more noisy syntax.
	 */
	def JavaConverter useRobustSyntax() {
		this.fallbackConversionStartegy = true
		return this
	}

}
