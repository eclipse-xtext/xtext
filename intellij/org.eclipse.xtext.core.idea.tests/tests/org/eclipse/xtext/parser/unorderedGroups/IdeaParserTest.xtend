package org.eclipse.xtext.parser.unorderedGroups

import com.google.common.io.CharStreams
import com.google.inject.Injector
import java.io.InputStream
import java.io.InputStreamReader
import org.eclipse.emf.common.util.URI
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.idea.tests.TestDecorator
import org.eclipse.xtext.idea.tests.parsing.AbstractLanguageParsingTestCase
import org.eclipse.xtext.idea.tests.parsing.ModelChecker
import org.eclipse.xtext.parser.unorderedGroups.idea.lang.SimpleUnorderedGroupsTestLanguageFileType
import org.eclipse.xtext.parser.unorderedGroups.idea.lang.SimpleUnorderedGroupsTestLanguageLanguage
import org.eclipse.xtext.resource.XtextResource
import org.junit.Ignore

@TestDecorator
class IdeaParserTest extends AbstractLanguageParsingTestCase {
	
	IdeaParserTestDelegate delegate
	
	new() {
		super(SimpleUnorderedGroupsTestLanguageFileType.INSTANCE)
		delegate = new IdeaParserTestDelegate(this)
	}
	
	override protected getTestDataPath() {
		"./testData/parsing/unorderedGroups/parserTest";
	}
	
	override protected setUp() throws Exception {
		super.setUp
		delegate.setUp
	}
	
}

@Ignore
@FinalFieldsConstructor
class IdeaParserTestDelegate extends ParserTest {
	
	val ModelChecker modelChecker
	
	override setUp() throws Exception {
		super.setUp
		injector = SimpleUnorderedGroupsTestLanguageLanguage.INSTANCE.getInstance(Injector)
	}
	
	override protected doGetResource(InputStream in, URI uri) throws Exception {
		modelChecker.checkModel(CharStreams.toString(new InputStreamReader(in)), false).eResource as XtextResource
	}
	
}