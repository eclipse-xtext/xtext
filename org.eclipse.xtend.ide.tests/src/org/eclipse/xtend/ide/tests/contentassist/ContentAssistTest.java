/*******************************************************************************
 * Copyright (c) 2011, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.contentassist;

import static com.google.common.collect.Lists.*;
import static org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.ide.internal.XtendActivator;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class ContentAssistTest extends AbstractXbaseContentAssistInBlockTest {
	
	protected static String[] VARIABLE_DECL = {"val", "var", "extension"};
	
	protected static String[] EXPRESSION_TEMPLATES = {
		"do - do-while loop with condition",
		"for - for loop over an Iterable",
		"if - if expression",
		"ifelse - if-else expression",
		"instanceof - type test and autocast",
		"switch - switch expression",
		"synchronized - synchronized expression",
		"syserr - print to standard error stream",
		"sysout - print to standard output",
		"try - try-catch block",
		"val - unmodifiable local variable",
		"var - modifiable local variable",
		"while - while loop with condition"
	};

	private static IProject project;
	
	@BeforeClass
	public static void setUpProject() throws Exception {
		project = createPluginProject(PROJECT_NAME);
		doInitFeatures(JavaCore.create(project));
	}

	@AfterClass
	public static void tearDownProject() throws Exception {
		WorkbenchTestHelper.deleteProject(project);
		project = null;
	}
	
	@Override
	protected String[] getVariableDeclarationKeywords() {
		return VARIABLE_DECL;
	}
	
	protected String[] getExpressionTemplates() {
		return EXPRESSION_TEMPLATES;
	}
	
	@Override
	@Test public void testEmptyInput() throws Exception {
		newBuilder().assertText(expect(getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_01() throws Exception {
		newBuilder().append("''").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_03() throws Exception {
		newBuilder().append("''.").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_04() throws Exception {
		newBuilder().append("''+''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_10() throws Exception {
		newBuilder().append("'' ").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_12() throws Exception {
		newBuilder().append("'' .").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_13() throws Exception {
		newBuilder().append("'' + ''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_28() throws Exception {
		newBuilder().append("''.toString.toString").assertTextAtCursorPosition(".", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_30() throws Exception {
		newBuilder().append("('')").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_32() throws Exception {
		newBuilder().append("(''.toString)").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_34() throws Exception {
		newBuilder().append("''.toString ").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_35() throws Exception {
		newBuilder().append("''.toString .").assertTextAtCursorPosition("g .", 2, expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnStringLiteral_36() throws Exception {
		newBuilder().append("''.toString +''").assertTextAtCursorPosition("+", expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_01() throws Exception {
		newBuilder().appendNl("var x = '';").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_02() throws Exception {
		newBuilder().appendNl("var x = '';").appendNl("var y = '';").assertTextAtCursorPosition("var y", expect(new String[] {"x"}, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_03() throws Exception {
		newBuilder().appendNl("var x = ''").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), CAST_INSTANCEOF, getVariableDeclarationKeywords(), getExpressionTemplates(), STRING_OPERATORS));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_04() throws Exception {
		newBuilder().appendNl("var x = ''").appendNl("var y = ''").assertTextAtCursorPosition("var y", expect(new String[] {"x"}, getKeywordsAndStatics(), CAST_INSTANCEOF, getVariableDeclarationKeywords(), getExpressionTemplates(), STRING_OPERATORS));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_09() throws Exception {
		newBuilder().appendNl("var x = ''").appendNl("var y = ").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testOnVoidMethod_01() throws Exception {
		newBuilder().append("(null as java.util.List).clear ").assertText(expect(getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterBinaryOperation_01() throws Exception {
		newBuilder().append("''+''").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterBinaryOperation_02() throws Exception {
		newBuilder().append("'' + ''+''").assertTextAtCursorPosition("''+", 2, expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterBinaryOperation_03() throws Exception {
		newBuilder().append("(''+'')").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterBinaryOperation_05() throws Exception {
		newBuilder().append("((''+''))").assertText(expect(STRING_OPERATORS, CAST_INSTANCEOF, getKeywordsAndStatics(), getVariableDeclarationKeywords(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testCatchParameter_01() throws Exception {
		newBuilder().append("try {} catch(NullPointerException e) e").assertTextAtCursorPosition(") e", 2, expect(new String[]{"e"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testCatchParameter_02() throws Exception {
		newBuilder().append("try {} catch(NullPointerException e) ").assertText(expect(new String[]{"e"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Test public void testTryResource_01() throws Exception {
		newBuilder().append("try (val a = new StringReader(\"Test\"); val b =  ").assertText(expect(new String[]{"a"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Test public void testTryResource_02() throws Exception {
		newBuilder().append("try (val a = new StringReader(\"Test\")) { val b = ").assertText(expect(new String[]{"a"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testSwitchOnEnum_01() throws Exception {
		newBuilder().append("switch java.lang.annotation.RetentionPolicy.SOURCE { case ").assertText(expect(new String[]{"SOURCE", "CLASS", "RUNTIME"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testSwitchOnEnum_03() throws Exception {
		newBuilder().append("switch java.lang.annotation.RetentionPolicy.SOURCE { case SOURCE: ").assertText(expect(getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testForLoop_01() throws Exception {
		newBuilder().append("for (String s: null) ").assertText(expect(new String[]{"s"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testForLoop_04() throws Exception {
		newBuilder().append("for (String string: ").assertText(expect(getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testForLoop_05() throws Exception {
		newBuilder().append("for (String string: )").assertTextAtCursorPosition(")", expect(getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testStaticFeatures_02() throws Exception {
		newBuilder().append("String.").assertText(expect(new String[]{ "this", "super" }, getStaticStringFeatures(), getClassFeatures()));
	}

	@Override
	@Test public void testNestedTypes_01() throws Exception {
		newBuilder().append("java.util.Map.").assertText(expect(new String[] { "Entry", "super" }, getClassFeatures(), getStaticMapFeatures()));
	}
	
	// all these test cases declared a local variable 'this' which is not allowed in Xtend
	@Override
	@Test public void testForLoop_06() throws Exception {
		newBuilder().append("for (String s: null) ").assertText(expect(new String[]{"s"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testForLoop_07() throws Exception {
		newBuilder().append("for (String s: null) ").assertText(expect(new String[]{"s"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_05() throws Exception {
		newBuilder().appendNl("var x = '';").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates(), VARIABLE_DECL));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_06() throws Exception {
		newBuilder().appendNl("var x = '';").appendNl("var y = '';").assertTextAtCursorPosition("var y", expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates(), VARIABLE_DECL));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_07() throws Exception {
		newBuilder().appendNl("var x = ''").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates(), CAST_INSTANCEOF, VARIABLE_DECL, STRING_OPERATORS));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_08() throws Exception {
		newBuilder().appendNl("var x = ''").appendNl("var y = ''").assertTextAtCursorPosition("var y", expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates(), CAST_INSTANCEOF, VARIABLE_DECL, STRING_OPERATORS));
	}
	
	@Override
	@Test public void testAfterVariableDeclaration_10() throws Exception {
		newBuilder().appendNl("var x = ''").appendNl("var y = ").assertText(expect(new String[] {"x"}, getKeywordsAndStatics(), getExpressionTemplates()));
	}
	
	@Test public void testRichString_01() throws Exception {
		newBuilder().append("'''foobar'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_02() throws Exception {
		newBuilder().append("'''foobar«null»'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar«null»''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar«null»'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''foobar«null»").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_03() throws Exception {
		newBuilder().append("'''«null»zonkfoobar'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_04() throws Exception {
		newBuilder().append("'''«null»zonkfoobar«null»'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar«null»''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar«null»'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''«null»zonkfoobar«null»").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_05() throws Exception {
		newBuilder().append("'''\n««« comment \nfoobar'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_06() throws Exception {
		newBuilder().append("'''\n««« comment \nfoobar«null»'''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar«null»''").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar«null»'").assertTextAtCursorPosition("foobar", "«»");
		newBuilder().append("'''\n««« comment \nfoobar«null»").assertTextAtCursorPosition("foobar", "«»");
	}
	
	@Test public void testRichString_07() throws Exception {
		newBuilder().append("'''\n««« comment foobar'''").assertTextAtCursorPosition("foobar");
	}
	
	@Test public void testRichString_08() throws Exception {
		newBuilder().append("'''\n««« comment foobar«null»'''").assertTextAtCursorPosition("foobar");
	}
	
	@Override
	@Test
	public void testClosure_01() throws Exception {
		newBuilder().append("[String a, String b|").assertText(expect(new String[]{"a", "b"}, getKeywordsAndStatics(), getExpressionTemplates(), VARIABLE_DECL));
	}
	
	@Override
	@Test
	public void testClosure_02() throws Exception {
		newBuilder().append("#['a', 'b'].filter[it==it]").assertTextAtCursorPosition("it==", expect(
				new String[]{"it", "self", "this", "super", "finalize" /* this.finalize() */, "clone" /* this.clone() */}, 
				VARIABLE_DECL, 
				baseGetKeywordsAndStatics(), // no other members of this. because they are made available via it.
				getExpressionTemplates(),
				getStringFeatures()));
	}

	@Override
	@Test
	public void testClosure_03() throws Exception {
		newBuilder().append("{val slow = #['a', 'b'].filter[it==it] }").assertTextAtCursorPosition("it==", expect(
				new String[]{"it", "self", "this", "super", "finalize" /* this.finalize() */, "clone" /* this.clone() */}, 
				VARIABLE_DECL, 
				baseGetKeywordsAndStatics(), // no other members of this. because they are made available via it.
				getExpressionTemplates(),
				getStringFeatures()));
	}
	
	@Override
	@Test
	public void testClosure_04() throws Exception {
		newBuilder().append("{val slow = #[0bd, 1bd].filter[i > 0]}").assertTextAtCursorPosition("i ", expect(
				new String[]{"it", "self", "this", "super", "finalize" /* this.finalize() */, "clone" /* this.clone() */}, 
				VARIABLE_DECL, 
				baseGetKeywordsAndStatics(), // no other members of this. because they are made available via it.
				getExpressionTemplates(),
				getBigDecimalFeatures()));
	}
	
	protected String[] baseGetKeywordsAndStatics() {
		return super.getKeywordsAndStatics();
	}
		
	@Override
	protected String[] getKeywordsAndStatics() {
		List<String> result = Lists.newArrayList(super.getKeywordsAndStatics());
		result.add("super");
		result.add("this");
		result.add("class");
		result.add("clone");
		result.add("hashCode");
		result.add("toString");
		result.add("finalize");
		result.add("notify");
		result.add("notifyAll");
		result.add("equals()");
		result.add("wait");
		result.add("wait()");
		result.add("wait()");
		// ObjectExtensions
		result.add("identityEquals()");
		return result.toArray(new String[result.size()]);
	}
	
	@Override
	public String[] getStringFeatures() {
		ArrayList<String> features = newArrayList(super.getStringFeatures());
		return features.toArray(new String[features.size()]);
	}
	
	@Override
	protected String getSuffix() {
		return "\n}\n}";
	}
	
	@Override
	protected String getPrefix() {
		return "class Name { def _operation() {";
	}
	
	protected static final String PROJECT_NAME = "ContentAssistTestProject";

	private IProject demandCreateProject;
	
	@Override
	protected Injector getInjector() {
		return XtendActivator.getInstance().getInjector("org.eclipse.xtend.core.Xtend");
	}
	
	@Override
	public void tearDown() throws Exception {
		if (demandCreateProject != null)
			deleteProject(demandCreateProject);
		super.tearDown();
	}

	@Override
	protected boolean doCleanWorkspace() {
		return false;
	}
	
	@Override
	public IJavaProject getJavaProject(ResourceSet resourceSet) {
		IJavaProject javaProject = findJavaProject(PROJECT_NAME);
		if (javaProject == null || !javaProject.exists()) {
			try {
				demandCreateProject = createPluginProject(PROJECT_NAME);
				javaProject = findJavaProject(PROJECT_NAME);
			} catch (CoreException e) {
				fail("cannot create java project due to: " + e.getMessage() + " / " + e);
			}
		}
		return javaProject;
	}

	public static IProject createPluginProject(String name) throws CoreException {
		Injector injector = XtendActivator.getInstance().getInjector("org.eclipse.xtend.core.Xtend");
		PluginProjectFactory projectFactory = injector.getInstance(PluginProjectFactory.class);
		projectFactory.setBreeToUse(JREContainerProvider.PREFERRED_BREE);
		projectFactory.setProjectName(name);
		projectFactory.setProjectDefaultCharset(StandardCharsets.ISO_8859_1.name());
		projectFactory.addFolders(Collections.singletonList("src"));
		projectFactory.addBuilderIds(
			JavaCore.BUILDER_ID, 
			"org.eclipse.pde.ManifestBuilder",
			"org.eclipse.pde.SchemaBuilder",
			XtextProjectHelper.BUILDER_ID);
		projectFactory.addProjectNatures(JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature", XtextProjectHelper.NATURE_ID);
		projectFactory.addRequiredBundles(Lists.newArrayList(
				"org.eclipse.xtext.xbase.lib",
				"org.eclipse.xtend.lib"));
		IProject result = projectFactory.createProject(new NullProgressMonitor(), null);
		JavaProjectSetupUtil.makeDefaultCompliant(JavaCore.create(result));
		return result;
	}
}
