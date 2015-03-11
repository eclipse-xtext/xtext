package org.eclipse.xtext.parser.terminalrules

import com.google.common.io.CharStreams
import com.google.inject.Injector
import java.io.InputStream
import java.io.InputStreamReader
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.idea.tests.TestDecorator
import org.eclipse.xtext.idea.tests.parsing.AbstractLanguageParsingTestCase
import org.eclipse.xtext.idea.tests.parsing.ModelChecker
import org.eclipse.xtext.parser.terminalrules.idea.lang.Bug292245TestLanguageFileType
import org.eclipse.xtext.parser.terminalrules.idea.lang.Bug292245TestLanguageLanguage
import org.junit.Ignore

@TestDecorator
class IdeaBug292245Test extends AbstractLanguageParsingTestCase {
	
	IdeaBug292245TestDelegate delegate
	
	new() {
		super(Bug292245TestLanguageFileType.INSTANCE)
		delegate = new IdeaBug292245TestDelegate(this)
	}
	
	override protected getTestDataPath() {
		"./testData/parsing/terminalrules/bug292245";
	}
	
	override protected setUp() throws Exception {
		super.setUp
		delegate.setUp
	}
	
}

@Ignore
@FinalFieldsConstructor
class IdeaBug292245TestDelegate extends Bug292245Test {
	
	val ModelChecker modelChecker
	
	override setUp() throws Exception {
		super.setUp
		injector = Bug292245TestLanguageLanguage.INSTANCE.getInstance(Injector)
	}
	
	override protected doGetResource(InputStream in, URI uri) throws Exception {
		modelChecker.checkResource(CharStreams.toString(new InputStreamReader(in)), false)
	}
	
}