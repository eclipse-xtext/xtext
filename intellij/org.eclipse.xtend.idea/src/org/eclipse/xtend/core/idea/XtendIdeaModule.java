package org.eclipse.xtend.core.idea;

import org.eclipse.xtend.core.idea.macro.IdeaProcessorProvider;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider;
import org.eclipse.xtend.ide.common.contentassist.antlr.DisabledInternalLexer;
import org.eclipse.xtend.ide.common.contentassist.antlr.FlexerBasedContentAssistContextFactory;
import org.eclipse.xtend.ide.common.contentassist.antlr.FlexerBasedContentAssistParser;
import org.eclipse.xtend.ide.common.contentassist.antlr.internal.InternalXtendLexer;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.ide.editor.contentassist.antlr.IContentAssistParser;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.Lexer;
import org.eclipse.xtext.parser.antlr.LexerProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.xbase.file.AbstractFileSystemSupport;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.idea.filesystem.IdeaFileSystemSupport;
import org.eclipse.xtext.xbase.idea.filesystem.IdeaWorkspaceConfigProvider;
import org.eclipse.xtext.xbase.idea.ide.hierarchy.JvmDeclaredTypeHierarchyProvider;
import org.eclipse.xtext.xbase.idea.ide.hierarchy.JvmExecutableCallHierarchyProvider;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.intellij.ide.hierarchy.call.JavaCallHierarchyProvider;
import com.intellij.ide.hierarchy.type.JavaTypeHierarchyProvider;

public class XtendIdeaModule extends AbstractXtendIdeaModule {

	public Class<? extends IContentAssistParser> bindIContentAssistParser() {
		return FlexerBasedContentAssistParser.class;
	}
	
	public void configureContentAssistLexerProvider(Binder binder) {
		binder.bind(InternalXtendLexer.class).toProvider(LexerProvider.create(DisabledInternalLexer.class));
		binder.bind(DisabledInternalLexer.class).toProvider(LexerProvider.create(DisabledInternalLexer.class));
	}

	public void configureContentAssistLexer(Binder binder) {
		binder.bind(Lexer.class).annotatedWith(Names.named(LexerIdeBindings.CONTENT_ASSIST)).to(DisabledInternalLexer.class);
	}
	
	public Class<? extends ContentAssistContextFactory> bindContentAssistContextFactory() {
		return FlexerBasedContentAssistContextFactory.class;
	}
	
	public Class<? extends ProcessorInstanceForJvmTypeProvider> bindProcessorInstanceForJvmTypeProvider() {
		return IdeaProcessorProvider.class;
	}

	@SingletonBinding
	public Class<? extends JavaTypeHierarchyProvider> bindJavaTypeHierarchyProvider() {
		return JvmDeclaredTypeHierarchyProvider.class;
	}

	
	@SingletonBinding
	public Class<? extends JavaCallHierarchyProvider> bindJavaCallHierarchyProvider() {
		return JvmExecutableCallHierarchyProvider.class;
	}
	
	public void configureWorkspaceConfig(Binder binder) {
		binder.bind(WorkspaceConfig.class).toProvider(IdeaWorkspaceConfigProvider.class);
	}
	
	public Class<? extends AbstractFileSystemSupport> bindAbstractFileSystemSupport() {
		return IdeaFileSystemSupport.class;
	}

}
