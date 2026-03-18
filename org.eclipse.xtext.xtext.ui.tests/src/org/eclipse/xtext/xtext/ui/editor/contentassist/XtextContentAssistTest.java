/*******************************************************************************
 * Copyright (c) 2009, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.ui.editor.contentassist;

import java.util.Collections;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.XtextRuntimeModule;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.testing.AbstractXtextTests;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.contentassist.ContentProposalLabelProvider;
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder;
import org.eclipse.xtext.ui.testing.util.ResourceLoadHelper;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.xtext.util.Modules2;
import org.eclipse.xtext.xtext.ui.Activator;
import org.eclipse.xtext.xtext.ui.internal.XtextUIModuleInternal;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("restriction")
public class XtextContentAssistTest extends AbstractXtextTests implements ResourceLoadHelper {
	
	protected ContentAssistProcessorTestBuilder newBuilder() throws Exception {
		with(getSetup());
		return new ContentAssistProcessorTestBuilder(getInjector(), this);
	}
	
	protected ISetup getSetup() {
		return doGetSetup();
	}
	
	private static final String TEST_PROJECT = "TestProject";
	private static final String MODEL_FILE_NAME = "mytestmodel";
	private static IProject project;

	@Before
	public void doSetupProject() throws Exception{
		if(project == null){
			with(doGetSetup());
			PluginProjectFactory projectFactory = getInjector().getInstance(PluginProjectFactory.class);
			projectFactory.setProjectName(TEST_PROJECT);
			projectFactory.addFolders(Collections.singletonList("src"));
			projectFactory.addBuilderIds(XtextProjectHelper.BUILDER_ID, JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
					"org.eclipse.pde.SchemaBuilder");
			projectFactory.addProjectNatures(
					XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature");
			projectFactory.addRequiredBundles(Lists.newArrayList("org.eclipse.xtext"));
			project = projectFactory.createProject(new NullProgressMonitor(), null);
			get(SyncUtil.class).waitForBuild(new NullProgressMonitor());
		}
	}
	
	@AfterClass
	public static void doDeleteProject() throws CoreException {
		if(project != null && project.exists())
			project.delete(true, new NullProgressMonitor());
		project = null;
	}
	
	public ISetup doGetSetup() {
		return new XtextStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(Modules2.mixin(new XtextRuntimeModule(),new XtextUIModuleInternal(Activator.getDefault()) {
					@Override
					public void configureContentProposalLabelProvider(com.google.inject.Binder binder) {
						binder.bind(ILabelProvider.class).annotatedWith(ContentProposalLabelProvider.class).to(DefaultEObjectLabelProvider.class);
					}
				}, new SharedStateModule()));
			}
		};
	}
	
	/**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=463215
     */
    @Test public void testBug463215_01() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with Terminal")
	        .appendNl("generate meta \"url\"")
	        .appendNl("Rule: name=ID;")
	        .assertTextAtCursorPosition("Terminal", 2, "org.eclipse.xtext.common.Terminals");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=463215
     */
    @Test public void testBug463215_02() throws Exception {
    	newBuilder()
    	.appendNl("grammar foo with or.e.xt")
    	.appendNl("generate meta \"url\"")
    	.appendNl("Rule: name=ID;")
    	.assertTextAtCursorPosition("xt", 1, "org.eclipse.xtext.Xtext", "org.eclipse.xtext.common.Terminals");
    }
	
	/**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=269680
     */
    @Test public void testCompletionOnDatatypeReference_01() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate meta \"url\"")
	        .appendNl("Rule: name=ID;")
	        .assertTextAtCursorPosition("org.eclipse.xtext", 2, "org.eclipse.xtext.Xtext", "org.eclipse.xtext.common.Terminals");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=269680
     */
    @Test public void testCompletionOnDatatypeReference_02() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate meta \"url\"")
	        .appendNl("Rule: name=ID;")
	        .assertTextAtCursorPosition("org.eclipse.xtext", 5, "org.eclipse.xtext.Xtext", "org.eclipse.xtext.common.Terminals");
    }
    
    @Test public void testCompletionOnDatatypeReference_03() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate meta \"url\"")
	        .appendNl("Rule: name=ID;")
	        .assertTextAtCursorPosition("org.eclipse.xtext", 4, "org.eclipse.xtext.Xtext", "org.eclipse.xtext.common.Terminals");
    }
    
    @Test public void testCompletionOnDatatypeReference_04() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals<|>")
	        .assertProposalDisplayedAtCursor("Terminals - org.eclipse.xtext.common.Terminals");
    }
    
    @Test public void testCompletionOnSyntaxError_01() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate foo 'bar'")
	        .appendNl("grammarA: child=Rule;")
	        .appendNl("Rule: call=grammar")
	        .assertTextAtCursorPosition("=grammar", "=grammar".length(), "grammarA");
    }
    
    @Test public void testCompletionOnSyntaxError_02() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate foo 'bar'")
	        .appendNl("grammarA: child=Rule;")
	        .append("Rule: call=grammar")
	        .assertText("grammarA");
    }
    
    @Test public void testCompletionOnSyntaxError_03() throws Exception {
        newBuilder()
	        .appendNl("grammar foo with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate foo 'bar'")
	        .appendNl("grammarA: child=Rule;")
	        .appendNl("Rule: call=grammar;")
	        .assertTextAtCursorPosition("grammar;", "grammar".length(), "grammarA");
    }

    @Test public void testCompleteGrammarName_01() throws Exception {
    	newBuilder()
        .append("grammar ")
        .assertText(MODEL_FILE_NAME);
    }
    
    @Test public void testCompleteAfterGrammarName_01() throws Exception {
    	newBuilder()
        .append("grammar org.foo.bar")
        .assertCount(0);
    }
    
    @Test public void testCompleteAfterGrammarName_02() throws Exception {
    	newBuilder()
        .append("grammar org.foo.bar ")
        .assertText("with", "Name", "enum", "terminal", "fragment", "hidden", "generate", "import", "@", "@Override", "@Deprecated", "@Exported", "@Final");
    }
    
    @Test public void testCompleteAfterGenerateName_01() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .append("generate name ")
        .assertCount(EPackage.Registry.INSTANCE.size());
    }
    
    @Test public void testCompleteAfterGenerateName_02() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .append("generate name \"")
        .assertCount(EPackage.Registry.INSTANCE.size());
    }
    
    @Test public void testCompleteCrossReference_01() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .appendNl("generate metamodelA 'http://foo.bar/A' as alias")
        .appendNl("generate metamodelB 'http://foo.bar/B'")
        .appendNl("RuleA: name=ID;")
        .append("RuleB returns ")
        .assertText("RuleA", "RuleB", "alias");
    }
    
    @Test public void testCompleteCrossReference_02() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .appendNl("generate metamodelA 'http://foo.bar/A' as alias")
        .appendNl("generate metamodelB 'http://foo.bar/B'")
        .appendNl("RuleA: name=ID;")
        .append("RuleB returns a")
        .assertText(
        		"alias",
        		"::",
        		":",
        		"a" // has already been inferred as type in metamodelA
        );
    }
    
    @Test public void testCompleteCrossReference_03() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .appendNl("generate metamodelA 'http://foo.bar/A' as alias")
        .appendNl("generate metamodelB 'http://foo.bar/B'")
        .appendNl("RuleA: name=ID;")
        .append("RuleB returns alias")
        .assertText("alias", "::", ":");
    }
    
    @Test public void testCompleteCrossReference_04() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .appendNl("generate metamodelA 'http://foo.bar/A' as alias")
        .appendNl("generate metamodelB 'http://foo.bar/B'")
        .appendNl("RuleA: name=ID;")
        .append("RuleB returns alias:")
        .assertText(
        		"::",
        		":",
        		"=>",
        		"->",
        		"Feature",
        		"\"Value\"",
        		"RuleA",
        		"RuleB",
        		"ID",
        		"STRING",
        		"INT",
        		"SL_COMMENT",
        		"WS",
        		"ML_COMMENT",
        		"ANY_OTHER",
        		"(",
        		"{",
        		"<", // guarded alternative
        		"name=");
    }
    
    @Test public void testCompleteCrossReference_05() throws Exception {
    	newBuilder()
        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
        .appendNl("generate metamodelA 'http://foo.bar/A' as alias")
        .appendNl("generate metamodelB 'http://foo.bar/B'")
        .appendNl("RuleA: name=ID;")
        .append("RuleB returns alias:=>")
        .assertText(
        		"=>",
        		"Feature",
        		"\"Value\"",
        		"RuleA",
        		"RuleB",
        		"ID",
        		"STRING",
        		"INT",
        		"SL_COMMENT",
        		"WS",
        		"ML_COMMENT",
        		"ANY_OTHER",
        		"(",
        		"name=");
    }
    
    @Test public void testEnumCompletion_01() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate myDsl1 \"http://www.xtext.org/example/MyDsl1\"")
	        .appendNl("import 'classpath:/org/eclipse/xtext/xtext/ui/editor/contentassist/enumsUi.ecore'")
	        .appendNl("enum ExistingEnum:")
	        .assertText("SameName", "DifferentName", "OverriddenLiteral");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=270116
     * @throws Exception
     */
    @Test public void testCompleteTypeRefReturnForEnumRule() throws Exception {
        doTestCompleteTypeRefSetup()
                .appendNl("enum NewEnum returns").assertText("NewEnum");
    }
    
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=270116
     * @throws Exception
     */
	@Test public void testCompleteTypeRefReturnForParserRule() throws Exception {
        doTestCompleteTypeRefSetup()
                .appendNl("NewType returns").assertText(
                                "Class", "Import", "Model", "NewType"
                );
    }
	
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=270116
     * @throws Exception
     */
    @Test public void testCompleteTypeRefReturnForTerminalRule_01() throws Exception {
        doTestCompleteTypeRefSetup()
                .appendNl("terminal NewType returns").assertText();
    }
    
    @Test public void testCompleteTypeRefReturnForTerminalRule_02() throws Exception {
        doTestCompleteTypeRefSetup()
        		.appendNl("enum NewEnum: Foobar;")
                .appendNl("terminal NewType returns").assertText("NewEnum");
    }
    
    @Test public void testCompleteTypeRefReturnForTerminalRule_03() throws Exception {
        doTestCompleteTypeRefSetup()
                .appendNl("terminal NewType returns X").assertTextAtCursorPosition("X");
    }
    
    @Test public void testCompleteTypeRefReturnForTerminalRule_04() throws Exception {
        doTestCompleteTypeRefSetup()
        		.appendNl("enum NewEnum: Foobar;")
                .appendNl("terminal NewType returns X").assertTextAtCursorPosition("X", "NewEnum");
    }
    
    @Test public void testCompleteTypeRefAction_01() throws Exception {
        doTestCompleteTypeRefSetup()
        		.appendNl("enum NewEnum: Foobar;")
                .appendNl("NewType: {").assertText(
                                "Class", "Import", "Model", "NewType"
                );
    }
    
    @Test public void testCompleteTypeRefAction_02() throws Exception {
    	doTestCompleteTypeRefSetup()
    	.appendNl("enum NewEnum: Foobar;")
    	.appendNl("NewType: { X").assertTextAtCursorPosition("X",
    			"Class", "Import", "Model", "NewType", "X"
    	);
    }

    @Test public void testCompleteTypeRefCrossReference_01() throws Exception {
    	doTestCompleteTypeRefSetup()
    	.appendNl("enum NewEnum: Foobar;")
    	.appendNl("NewType: ref=[").assertText(
    			"Class", "Import", "Model", "NewType"
    	);
    }
    
    @Test public void testCompleteTypeRefCrossReference_02() throws Exception {
        doTestCompleteTypeRefSetup()
        		.appendNl("enum NewEnum: Foobar;")
                .appendNl("NewType: ref=[ X").assertTextAtCursorPosition("X",
                                "Class", "Import", "Model", "NewType"
                );
    }
    
    @Test public void testCompleteFeatureName_01() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID;")
	        .append("RuleB returns RuleA: ")
	        .assertText(
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"=>",
	        		"->",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_02() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID ")
	        .assertText(
	        		"name=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"=>",
	        		"->",
	        		"*",
	        		"+",
	        		";",
	        		"?",
	        		"|",
	        		"&");
    }
    
    @Test public void testCompleteFeatureName_03() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID somethingElse=ID;")
	        .append("RuleB returns RuleA: ")
	        .assertText(
	        		"somethingElse=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"=>",
	        		"->",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_04() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID;")
	        .append("RuleB returns RuleA: =>")
	        .assertText(
	        		"=>",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_05() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID;")
	        .append("RuleB returns RuleA: => ")
	        .assertText(
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_06() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .append("RuleA: name=ID =>")
	        .assertText(
	        		"name=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"=>");
    }
    
    @Test public void testCompleteFeatureName_07() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID => ")
	        .assertText(
	        		"name=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(");
    }
    
    @Test public void testCompleteFeatureName_08() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID somethingElse=ID;")
	        .append("RuleB returns RuleA: =>")
	        .assertText(
	        		"somethingElse=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"=>",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_09() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID somethingElse=ID;")
	        .append("RuleB returns RuleA: => ")
	        .assertText(
	        		"somethingElse=",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_10() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID somethingElse=ID;")
	        .append("RuleB returns RuleA: =")
	        .assertText(
	        		"=>");
    }
    
    @Test public void testCompleteFeatureName_11() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID;")
	        .append("RuleB returns RuleA: =>(name=ID);")
	        .assertTextAtCursorPosition("(",
	        		"name=",
	        		"=>",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(");
    }
    
    @Test public void testCompleteFeatureName_12() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("RuleA: name=ID;")
	        .append("RuleB returns RuleA: =>  name=ID;")
	        .assertTextAtCursorPosition("  name",
	        		"name=",
	        		"=>",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"ID",
	        		"STRING",
	        		"INT",
	        		"SL_COMMENT",
	        		"WS",
	        		"ML_COMMENT",
	        		"ANY_OTHER",
	        		"(");
    }
    
    @Test public void testCompleteFeatureName_13() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("import 'http://www.eclipse.org/emf/2002/Ecore'")
	        .appendNl("RuleA: feature+='x';")
	        .append("RuleB returns RuleA:  ;")
	        .assertTextAtCursorPosition(" ;",
	        		"=>",
	        		"->",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"feature+=",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_14() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("import 'http://www.eclipse.org/emf/2002/Ecore'")
	        .appendNl("RuleA: feature+='x';")
	        .append("RuleB returns RuleA:   feature+='y' ;")
	        .assertTextAtCursorPosition("  feat",
	        		"=>",
	        		"->",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"feature+=",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_15() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("import 'http://www.eclipse.org/emf/2002/Ecore'")
	        .appendNl("RuleA: feature?='x';")
	        .append("RuleB returns RuleA:  ;")
	        .assertTextAtCursorPosition(" ;",
	        		"=>",
	        		"->",
	        		"Feature",
	        		"\"Value\"",
	        		"RuleA",
	        		"RuleB",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"feature?=",
	        		"name=");
    }
    
    @Test public void testCompleteFeatureName_16() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar")
	        .appendNl("generate metamodelA 'http://foo.bar/A'")
	        .appendNl("import 'http://www.eclipse.org/emf/2002/Ecore'")
	        .append("Import:  ;")
	        .assertTextAtCursorPosition(" ;",
	        		"=>",
	        		"->",
	        		"Feature",
	        		"\"Value\"",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"Import",
	        		"importedNamespace=",
	        		"importURI=");
    }
    
    @Test public void testCompleteFeatureName_17() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar")
	        .appendNl("fragment Foo *:")
	        .append("name =\"Bar\";")
	        .assertTextAtCursorPosition("=",
	        		"=>",
	        		"->",
	        		"\"Value\"",
	        		"Feature",
	        		"(",
	        		"{",
	        		"<", // guarded alternative
	        		"&",
	        		"*",
	        		"+",
	        		"+=",
	        		";",
	        		"=",
	        		"?",
	        		"?=",
	        		"|");
    }
    
    @Test public void testAliasCompletion_01() throws Exception {
    	newBuilder()
	        .appendNl("grammar org.foo.bar with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate metamodelA 'http://foo.bar/A' as ")
	        .assertText(
	        		"Alias",
	        		"metamodelA");
    }
    
    @Test public void testAliasCompletion_02() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate myDsl1 \"http://www.xtext.org/example/MyDsl1\"")
	        .appendNl("import 'classpath:/org/eclipse/xtext/xtext/ui/editor/contentassist/enumsUi.ecore' as")
	        .assertText("enumsUi", "Alias");
    }
    
    @Test public void testCompleteTypeRef_01() throws Exception {
        doTestCompleteTypeRefWithAliasSetup()
                .append("NewRule returns ").assertText(
                                "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias"
                );
    }
    
	@Test public void testCompleteTypeRef_02() throws Exception {
		doTestCompleteTypeRefWithAliasSetup()
	        .append("NewRule returns mYal").assertText(
	                        "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias", ":", "::"
	        );
    }
	
    @Test public void testCompleteTypeRef_03() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
	        .append("NewRule returns cl").assertText(
	                        "myAlias::Class", ":", "::"
	        );
    }
    
    @Test public void testCompleteTypeRef_04() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
	        .append("NewRule returns my::Cl").assertText(
	                        "myAlias::Class", ":"
	        );
    }
    
    @Test public void testCompleteTypeRef_05() throws Exception {
        doTestCompleteTypeRefWithAliasSetup()
                .append("NewRule returns myAlias::NewRule: reference=[").assertText(
                                "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias", "myAlias::NewRule", "["
                );
    }
    
	@Test public void testCompleteTypeRef_06() throws Exception {
		doTestCompleteTypeRefWithAliasSetup()
			.append("NewRule returns myAlias::NewRule: reference=[mYal").assertText(
	                        "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias::NewRule", "myAlias", "]", "::", "|"
	        );
    }
	
    @Test public void testCompleteTypeRef_07() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
    		.append("NewRule returns myAlias::NewRule: reference=[cl").assertText(
	                        "myAlias::Class", "]", "|", "::"
	        );
    }
    
    @Test public void testCompleteTypeRef_08() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
    		.append("NewRule returns myAlias::NewRule: reference=[my::Cl").assertText(
	                        "myAlias::Class", "]", "|"
	        );
    }
    
    @Test public void testCompleteTypeRef_09() throws Exception {
        doTestCompleteTypeRefWithAliasSetup()
                .append("NewRule returns myAlias::NewRule: {").assertText(
                                "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias", "myAlias::NewRule", "{"
                );
    }
    
	@Test public void testCompleteTypeRef_10() throws Exception {
		doTestCompleteTypeRefWithAliasSetup()
			.append("NewRule returns myAlias::NewRule: {mYal").assertText(
	                        "myAlias::Class", "myAlias::Import", "myAlias::Model", "myAlias::NewRule", "myAlias", "}", ".", "::"
	        );
    }
	
    @Test public void testCompleteTypeRef_11() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
    		.append("NewRule returns myAlias::NewRule: {cl").assertText(
	                        "myAlias::Class", "}", "::", "."
	        );
    }
    
    @Test public void testCompleteTypeRef_12() throws Exception {
    	doTestCompleteTypeRefWithAliasSetup()
    		.append("NewRule returns myAlias::NewRule: {my::Cl").assertText(
	                        "myAlias::Class", "}", "."
	        );
    }
    
    private ContentAssistProcessorTestBuilder doTestCompleteTypeRefSetup() throws Exception {
		return newBuilder()
			.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate myDsl1 \"http://www.xtext.org/example/MyDsl1\"")
	        .appendNl("Model :")
	        .appendNl("(imports+=Import)*")
	        .appendNl("(elements+=Class)*;")
	        .appendNl("Import :")
	        .appendNl("'import' importURI=STRING;")
	        .appendNl("Class :")
	        .appendNl("'class' name=ID ('extends' references=[Class])?;");
	}
    
    private ContentAssistProcessorTestBuilder doTestCompleteTypeRefWithAliasSetup() throws Exception {
		return newBuilder()
	        .appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("generate myDsl1 \"http://www.xtext.org/example/MyDsl1\" as myAlias")
	        .appendNl("Model returns myAlias::Model:")
	        .appendNl("(imports+=Import)*")
	        .appendNl("(elements+=Class)*;")
	        .appendNl("Import returns myAlias::Import:")
	        .appendNl("'import' importURI=STRING;")
	        .appendNl("Class returns myAlias::Class:")
	        .appendNl("'class' name=ID ('extends' references=[Class])?;");
	}
    
    @Test public void testCompleteRuleForReferencedType() throws Exception {
    	newBuilder()
			.appendNl("grammar Foo with org.eclipse.xtext.common.Terminals")
			.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\"") 
			.append("EPac")
			.assertText(
					"EPackage: \n;\n",
					"<", // parameterized rule 
					":");
    	newBuilder()
    		.appendNl("grammar Foo with org.eclipse.xtext.common.Terminals")
    		.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\"") 
    		.appendNl("FooBar returns EPackage: 'bar';") 
    		.append("EPac")
    		.assertText(
    				"<", // parameterized rule 
    				":");
    }

    @Test public void testCompleteRuleForReferencedTypeWithAlias() throws Exception {
    	newBuilder()
    		.appendNl("grammar Foo with org.eclipse.xtext.common.Terminals")
    		.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\" as ecore") 
    		.append("EPac")
    		.assertText(
    				"EPackage returns ecore::EPackage: \n;\n",
    				"<", // parameterized rule 
    				":");
    	newBuilder()
    		.appendNl("grammar Foo with org.eclipse.xtext.common.Terminals")
    		.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\" as ecore") 
    		.appendNl("FooBar returns ecore::EPackage : 'bar';") 
    		.append("EPac")
    		.assertText(
    				"<", // parameterized rule 
    				":");
    }
    
    @Test public void testBug317280_01() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("import 'classpath:/org/eclipse/xtext/Xtext.ecore'")
	        .append("En")
	        .assertText("EnumRule: \n;\n", "EnumLiteralDeclaration: \n;\n", 
	        		"enum",
	        		"<", // parameterized rule
	        		":");
    }
    
    @Test public void testBug317280_02() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .appendNl("import 'classpath:/org/eclipse/xtext/Xtext.ecore' as xtext")
	        .append("En")
	        .assertText("EnumRule returns xtext::EnumRule: \n;\n", 
	        		"EnumLiteralDeclaration returns xtext::EnumLiteralDeclaration: \n;\n", 
	        		"enum",
	        		"<", // parameterized rule
	        		":");
    }

    @Test public void testOverrideRule_01() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .append("ST")
	        .assertText(
	        		"terminal STRING:\n\tsuper\n;",
	        		"<", // parameterized rule
	        		":");
    }
    
    @Test public void testOverrideRule_02() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	        .append("IN")
	        .assertText(
	        		"terminal INT returns ecore::EInt:\n\tsuper\n;",
	        		"<", // parameterized rule
	        		":");
    }
    
    @Test public void testOverrideRule_03() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
    		.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\" as ec")
	        .append("IN")
	        .assertText(
	        		"terminal INT returns ec::EInt:\n\tsuper\n;",
	        		"<", // parameterized rule
	        		":");
    }
    
    @Test public void testOverrideRule_04() throws Exception {
    	newBuilder()
    		.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
    		.appendNl("import \"http://www.eclipse.org/emf/2002/Ecore\"")
	        .append("IN")
	        .assertText(
	        		"terminal INT returns EInt:\n\tsuper\n;",
	        		"<", // parameterized rule
	        		":");
    }
    
    @Test public void testOverrideRule_05() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.xtext.ui.editor.contentassist.GrammarWithTerminalFragment")
	    	.append("ESC")
	    	.assertText(
	    			"terminal fragment ESCAPED_CHAR:\n\tsuper\n;",
	    			"<", // parameterized rule
	    			":");
    }
    
    @Test public void testCompleteHiddenTokens_01() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals hidden()")
	    	.appendNl("Model: name=ID;")
	    	.appendNl("DataType: ID;")
	    	.appendNl("terminal fragment Fragment: 'a'..'z';")
	    	.appendNl("terminal Terminal: 'a'..'z';")
	    	.assertTextAtCursorPosition("hidden(", "hidden(".length(), 
	    			"(", 
	    			")", 
	    			"WS", 
	    			"SL_COMMENT", 
	    			"ML_COMMENT",
	    			"ID",
	    			"INT",
	    			"ANY_OTHER",
	    			"STRING",
	    			"Terminal");
    }
    
    @Test public void testCompleteHiddenTokens_02() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("Model hidden(): name=ID;")
	    	.appendNl("DataType: ID;")
	    	.appendNl("terminal fragment Fragment: 'a'..'z';")
	    	.appendNl("terminal Terminal: 'a'..'z';")
	    	.assertTextAtCursorPosition("hidden(", "hidden(".length(), 
	    			"(", 
	    			")", 
	    			"WS", 
	    			"SL_COMMENT", 
	    			"ML_COMMENT",
	    			"ID",
	    			"INT",
	    			"ANY_OTHER",
	    			"STRING",
	    			"Terminal");
    }
    
    @Test public void testCompleteRuleCall_01() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("generate test 'http://test'")
	    	.appendNl("Model: name=ID;")
	    	.appendNl("DataType: ID;")
	    	.appendNl("enum EnumType: A | B;")
	    	.appendNl("terminal fragment Fragment: 'a'..'z';")
	    	.appendNl("terminal Terminal: 'a'..'z';")
	    	.assertTextAtCursorPosition("ID",
	    			"=>",
	    			"=", 
	    			"[",
	    			"(",
	    			"\"Value\"",
	    			"WS", 
	    			"SL_COMMENT", 
	    			"ML_COMMENT",
	    			"ID",
	    			"INT",
	    			"ANY_OTHER",
	    			"STRING",
    				"Terminal",
    				"DataType",
    				"Model",
    				"EnumType");
    }
    
    @Test public void testCompleteRuleCall_02() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("generate test 'http://test'")
	    	.appendNl("Model: name=STRING;")
	    	.appendNl("DataType: STRING;")
	    	.appendNl("enum EnumType: A | B;")
	    	.appendNl("terminal fragment Fragment: 'a'..'z';")
	    	.appendNl("terminal Terminal: ID;")
	    	.assertTextAtCursorPosition("ID", 
	    			"\"Value\"", 
	    			"(",
	    			"->",
	    			".",
	    			"!",
	    			"WS", 
	    			"SL_COMMENT", 
	    			"ML_COMMENT",
	    			"ID",
	    			"INT",
	    			"ANY_OTHER",
	    			"STRING",
    				"Terminal",
    				"Fragment",
    				"EOF");
    }
    
    @Test public void testCompleteRuleCall_03() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("generate test 'http://test'")
	    	.appendNl("Model: name=super::STRING;")
	    	.assertTextAtCursorPosition("STRING", 
	    			"super::ANY_OTHER",
	    			"super::ID",
	    			"super::INT",
	    			"super::ML_COMMENT",
	    			"super::SL_COMMENT",
	    			"super::STRING",
	    			"super::WS");
    }
    
    @Test public void testCompleteRuleCall_04() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("generate test 'http://test'")
	    	.appendNl("terminal SUPERX: super::STRING;")
	    	.assertTextAtCursorPosition("super", 2, 
	    			"SUPERX",
	    			"super::ANY_OTHER",
	    			"super::ID",
	    			"super::INT",
	    			"super::ML_COMMENT",
	    			"super::SL_COMMENT",
	    			"super::STRING",
	    			"super::WS",
	    			"!",
	    			"\"Value\"",
	    			"(",
	    			"*",
	    			"+",
	    			"->",
	    			".",
	    			";",
	    			"?",
	    			"|");
    }
    
    @Test public void testCompleteRuleCall_05() throws Exception {
    	newBuilder()
	    	.appendNl("grammar org.xtext.example.MyDsl1 with org.eclipse.xtext.common.Terminals")
	    	.appendNl("generate test 'http://test'")
	    	.appendNl("terminal STRING: super::STRING;")
	    	.assertTextAtCursorPosition("super", 2, 
	    			"super",
	    			"super::ANY_OTHER",
	    			"super::ID",
	    			"super::INT",
	    			"super::ML_COMMENT",
	    			"super::SL_COMMENT",
//	    			"super::STRING", // already covered by 'super'
	    			"super::WS",
	    			"!",
	    			"\"Value\"",
	    			"(",
	    			"*",
	    			"+",
	    			"->",
	    			".",
	    			";",
	    			"?",
	    			"|");
    }
    
	@Override
	protected URI getTestModelURI() {
		return URI.createURI("platform:/resource/" + TEST_PROJECT + "/src/"+ MODEL_FILE_NAME + "."+getCurrentFileExtension());
	}

}
