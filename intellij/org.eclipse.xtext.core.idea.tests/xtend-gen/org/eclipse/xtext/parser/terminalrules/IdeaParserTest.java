package org.eclipse.xtext.parser.terminalrules;

import org.eclipse.xtext.idea.tests.TestDecorator;
import org.eclipse.xtext.idea.tests.parsing.AbstractLanguageParsingTestCase;
import org.eclipse.xtext.parser.terminalrules.IdeaParserTestDelegate;
import org.eclipse.xtext.parser.terminalrules.idea.lang.TerminalRulesTestLanguageFileType;

@TestDecorator
@SuppressWarnings("all")
public class IdeaParserTest extends AbstractLanguageParsingTestCase {
  private IdeaParserTestDelegate delegate;
  
  public IdeaParserTest() {
    super(TerminalRulesTestLanguageFileType.INSTANCE);
    IdeaParserTestDelegate _ideaParserTestDelegate = new IdeaParserTestDelegate(this);
    this.delegate = _ideaParserTestDelegate;
  }
  
  @Override
  protected String getTestDataPath() {
    return "./testData/parsing/terminalrules/parserTest";
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.delegate.setUp();
  }
  
  public void testAnyOther_01() throws Exception {
    delegate.testAnyOther_01();
  }
  
  public void testAnyOther_02() throws Exception {
    delegate.testAnyOther_02();
  }
  
  public void testAnyOther_03() throws Exception {
    delegate.testAnyOther_03();
  }
  
  public void testId_01() throws Exception {
    delegate.testId_01();
  }
  
  public void testId_02() throws Exception {
    delegate.testId_02();
  }
  
  public void testId_03() throws Exception {
    delegate.testId_03();
  }
  
  public void testId_04() throws Exception {
    delegate.testId_04();
  }
  
  public void testInt_01() throws Exception {
    delegate.testInt_01();
  }
  
  public void testInt_02() throws Exception {
    delegate.testInt_02();
  }
  
  public void testMlComment_01() throws Exception {
    delegate.testMlComment_01();
  }
  
  public void testMlComment_02() throws Exception {
    delegate.testMlComment_02();
  }
  
  public void testRichString_01() throws Exception {
    delegate.testRichString_01();
  }
  
  public void testRichString_02() throws Exception {
    delegate.testRichString_02();
  }
  
  public void testRichString_03() throws Exception {
    delegate.testRichString_03();
  }
  
  public void testRichString_04() throws Exception {
    delegate.testRichString_04();
  }
  
  public void testRichString_05() throws Exception {
    delegate.testRichString_05();
  }
  
  public void testRichString_06() throws Exception {
    delegate.testRichString_06();
  }
  
  public void testRichString_07() throws Exception {
    delegate.testRichString_07();
  }
  
  public void testSetup() {
    delegate.testSetup();
  }
  
  public void testSlComment_01() throws Exception {
    delegate.testSlComment_01();
  }
  
  public void testSlComment_02() throws Exception {
    delegate.testSlComment_02();
  }
  
  public void testSlComment_03() throws Exception {
    delegate.testSlComment_03();
  }
  
  public void testSlComment_04() throws Exception {
    delegate.testSlComment_04();
  }
  
  public void testString_01() throws Exception {
    delegate.testString_01();
  }
  
  public void testString_02() throws Exception {
    delegate.testString_02();
  }
  
  public void testString_03() throws Exception {
    delegate.testString_03();
  }
  
  public void testString_04() throws Exception {
    delegate.testString_04();
  }
  
  public void testString_05() throws Exception {
    delegate.testString_05();
  }
  
  public void testString_06() throws Exception {
    delegate.testString_06();
  }
  
  public void testWs_01() throws Exception {
    delegate.testWs_01();
  }
  
  public void testWs_02() throws Exception {
    delegate.testWs_02();
  }
  
  public void testWs_03() throws Exception {
    delegate.testWs_03();
  }
}
