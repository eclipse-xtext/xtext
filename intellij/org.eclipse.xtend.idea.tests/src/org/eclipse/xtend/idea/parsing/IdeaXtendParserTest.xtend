/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.idea.parsing

import org.eclipse.xtend.core.tests.parsing.ParserTest
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.idea.tests.parsing.ModelChecker
import org.junit.Ignore

/**
 * @author kosyakov - Initial contribution and API
 */
@FinalFieldsConstructor
@Ignore("Only here as a delegate")
class IdeaXtendParserTest extends ParserTest {
	
	val ModelChecker modelChecker

	override file(String string, boolean validate) {
		modelChecker.checkModel(string, validate)
	}
	
}