package org.eclipse.xtext.parser.assignments

import com.google.common.io.CharStreams
import com.google.inject.Injector
import java.io.InputStream
import java.io.InputStreamReader
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.idea.tests.TestDecorator
import org.eclipse.xtext.idea.tests.parsing.AbstractLanguageParsingTestCase
import org.eclipse.xtext.idea.tests.parsing.ModelChecker
import org.eclipse.xtext.parser.assignments.idea.lang.Bug287184TestLanguageFileType
import org.eclipse.xtext.parser.assignments.idea.lang.Bug287184TestLanguageLanguage
import org.junit.Ignore
import org.eclipse.xtext.resource.XtextResource

@TestDecorator
class IdeaBug287184Test extends AbstractLanguageParsingTestCase {
	
	IdeaBug287184TestDelegate delegate
	
	new() {
		super(Bug287184TestLanguageFileType.INSTANCE)
		delegate = new IdeaBug287184TestDelegate(this)
	}
	
	override protected getTestDataPath() {
		"./testData/parsing/assignments/bug287184";
	}
	
	override protected setUp() throws Exception {
		super.setUp
		delegate.setUp
	}
	
}

@Ignore
@FinalFieldsConstructor
class IdeaBug287184TestDelegate extends Bug287184Test {
	
	val ModelChecker modelChecker
	
	override setUp() throws Exception {
		super.setUp
		injector = Bug287184TestLanguageLanguage.INSTANCE.getInstance(Injector)
	}
	
	override protected doGetResource(InputStream in, URI uri) throws Exception {
		modelChecker.checkResource(CharStreams.toString(new InputStreamReader(in)), false)
	}
	
	override protected shouldTestSerializer(XtextResource resource) {
		false
	}
	
}