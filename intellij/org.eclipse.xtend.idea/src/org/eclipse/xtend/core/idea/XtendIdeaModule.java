/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.idea;

import org.eclipse.xtend.core.idea.macro.IdeaProcessorProvider;
import org.eclipse.xtend.core.idea.presentation.XtendItemPresentationProvider;
import org.eclipse.xtend.core.idea.structureview.XtendStructureViewBuilder;
import org.eclipse.xtend.core.idea.validation.XtendIdeaValidator;
import org.eclipse.xtend.core.linking.Linker;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider;
import org.eclipse.xtend.ide.common.contentassist.antlr.DisabledInternalLexer;
import org.eclipse.xtend.ide.common.contentassist.antlr.FlexerBasedContentAssistContextFactory;
import org.eclipse.xtend.ide.common.contentassist.antlr.FlexerBasedContentAssistParser;
import org.eclipse.xtend.ide.common.contentassist.antlr.internal.InternalXtendLexer;
import org.eclipse.xtend.ide.common.editor.bracketmatching.XtendBracePairProvider;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.ide.editor.bracketmatching.IBracePairProvider;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.ide.editor.contentassist.antlr.IContentAssistParser;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.Lexer;
import org.eclipse.xtext.idea.presentation.ItemPresentationProvider;
import org.eclipse.xtext.idea.structureview.XtextFileAwareStructureViewBuilder;
import org.eclipse.xtext.linking.ILinker;
import org.eclipse.xtext.parser.antlr.LexerProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.xbase.file.AbstractFileSystemSupport;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.idea.filesystem.IdeaFileSystemSupport;
import org.eclipse.xtext.xbase.idea.filesystem.IdeaWorkspaceConfigProvider;

import com.google.inject.Binder;
import com.google.inject.name.Names;

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
	
	@Override
	@SingletonBinding
	public Class<? extends IBracePairProvider> bindIBracePairProvider() {
		return XtendBracePairProvider.class;
	}
	
	public void configureWorkspaceConfig(Binder binder) {
		binder.bind(WorkspaceConfig.class).toProvider(IdeaWorkspaceConfigProvider.class);
	}
	
	public Class<? extends AbstractFileSystemSupport> bindAbstractFileSystemSupport() {
		return IdeaFileSystemSupport.class;
	}

	@SingletonBinding
	public Class<? extends ItemPresentationProvider> bindItemPresentationProvider() {
		return XtendItemPresentationProvider.class;
	}

	public Class<? extends XtextFileAwareStructureViewBuilder> bindXtextFileAwareStructureViewBuilder() {
		return XtendStructureViewBuilder.class;
	}

	@org.eclipse.xtext.service.SingletonBinding(eager = true)
	public Class<? extends XtendIdeaValidator> bindXtendIdeaValidator() {
		return XtendIdeaValidator.class;
	}

	public Class<? extends ILinker> bindILinker() {
		return Linker.class;
	}

}
