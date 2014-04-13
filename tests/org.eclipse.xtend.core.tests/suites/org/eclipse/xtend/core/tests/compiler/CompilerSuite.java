/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler;

import org.eclipse.xtend.core.tests.annotations.AnnotationsCompilerTest;
import org.eclipse.xtend.core.tests.compiler.batch.TestBatchCompiler;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@RunWith(Suite.class)
@SuiteClasses({
	AnnotationCompilerTest.class,
	AnnotationsCompilerTest.class,
	AnnotationValueCompilerTest.class,
	AutocastCompilerTest.class,
	CompilerTest.class,
	CompilerBugTest.class,
	CompilerBug342021Test.class,
	CompilerBug383534Test.class,
	CompilerBug404051Test.class,
	CompilerBug405825Test.class,
	CompilerBug406425Test.class,
	CompilerBug406549Test.class,
	CompilerBug410555Test.class,
	CompilerBug410556Test.class,
	CompilerBug410767Test.class,
	CompilerBug410797Test.class,
	CompilerBug413138Test.class,
	CompilerBug416305Test.class,
	CompilerBug417522Test.class,
	CompilerBug418364Test.class,
	CompilerBug419050Test.class,
	CompilerBug419688Test.class,
	CompilerBug421999Test.class,
	CompilerBug422864Test.class,
	CompilerBug423631Test.class,
	CompilerBug423907Test.class,
	CompilerBug424329Test.class,
	CompilerBug424763Test.class,
	CompilerBug424839Test.class,
	CompilerBug427637Test.class,
	CompilerBug427660Test.class,
	CompilerBug428063Test.class,
	CompilerBug430484Test.class,
	CompilerBug432193Test.class,
	CompilerBugDependentTypeParametersTest.class,
	CompilerTraceTest.class,
	CompoundAssignmentOperatorCompilerTest.class,
	ConfiguredCompilerTest.class,
	DispatchCompilerTest.class,
	ExtensionsCompilerTest.class,
	DataCompilerTest.class,
	Java8OverloadCompilerTest.class,
	OverloadedExtensionCompilerTest.class,
	ParameterizedTypeCompilerTest.class,
	PostfixOperatorCompilerTest.class,
	PropertyCompilerTest.class,
	TestBatchCompiler.class,
	XtendCompilerTest.class,
	XtendCompilerErrorHandlingTest.class,
	XbaseIntegrationTest.class
})
public class CompilerSuite {
}
