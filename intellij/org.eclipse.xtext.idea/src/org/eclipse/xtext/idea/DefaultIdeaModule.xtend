/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.idea

import com.google.inject.Binder
import com.google.inject.name.Names
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.lang.PairedBraceMatcher
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.lang.refactoring.NamesValidator
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.util.Key
import com.intellij.psi.util.PsiModificationTracker
import org.eclipse.xtext.common.types.access.IJvmTypeProvider
import org.eclipse.xtext.formatting.IIndentationInformation
import org.eclipse.xtext.generator.IContextualOutputConfigurationProvider
import org.eclipse.xtext.generator.trace.ITraceForURIProvider
import org.eclipse.xtext.ide.LexerIdeBindings
import org.eclipse.xtext.ide.editor.bracketmatching.DefaultBracePairProvider
import org.eclipse.xtext.ide.editor.bracketmatching.IBracePairProvider
import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.idea.build.IdeaOutputConfigurationProvider
import org.eclipse.xtext.idea.common.types.StubTypeProviderFactory
import org.eclipse.xtext.idea.filesystem.IdeaProjectConfigProvider
import org.eclipse.xtext.idea.formatting.CodeStyleSettingsIndentationInformation
import org.eclipse.xtext.idea.formatting.DefaultCommenter
import org.eclipse.xtext.idea.formatting.DefaultFormattingModelBuilder
import org.eclipse.xtext.idea.highlighting.DefaultHighlightingConfiguration
import org.eclipse.xtext.idea.highlighting.DefaultPairedBraceMatcher
import org.eclipse.xtext.idea.highlighting.DefaultSyntaxHighlighter
import org.eclipse.xtext.idea.highlighting.IHighlightingConfiguration
import org.eclipse.xtext.idea.parser.AntlrDelegatingIdeaLexer
import org.eclipse.xtext.idea.refactoring.NullNamesValidator
import org.eclipse.xtext.idea.resource.IdeaEncodingProvider
import org.eclipse.xtext.idea.resource.IdeaResourceDescriptionsProvider
import org.eclipse.xtext.idea.service.IdeaOperationCanceledManager
import org.eclipse.xtext.idea.structureview.DefaultPsiStructureViewFactory
import org.eclipse.xtext.idea.trace.ITraceForVirtualFileProvider
import org.eclipse.xtext.idea.trace.TraceForVirtualFileProvider
import org.eclipse.xtext.parser.IEncodingProvider
import org.eclipse.xtext.parser.antlr.AntlrTokenDefProvider
import org.eclipse.xtext.parser.antlr.ITokenDefProvider
import org.eclipse.xtext.psi.IPsiModelAssociations
import org.eclipse.xtext.psi.IPsiModelAssociator
import org.eclipse.xtext.psi.PsiModelAssociations
import org.eclipse.xtext.psi.impl.BaseXtextFile
import org.eclipse.xtext.resource.IContainer
import org.eclipse.xtext.resource.IResourceDescriptionsProvider
import org.eclipse.xtext.resource.containers.ProjectDescriptionBasedContainerManager
import org.eclipse.xtext.service.AbstractGenericModule
import org.eclipse.xtext.service.LanguageSpecific
import org.eclipse.xtext.service.OperationCanceledManager
import org.eclipse.xtext.service.SingletonBinding
import org.eclipse.xtext.workspace.IProjectConfigProvider

/**
 * @author kosyakov - Initial contribution and API
 */
class DefaultIdeaModule extends AbstractGenericModule {

	def Class<? extends Lexer> bindIdeaLexer() {
		AntlrDelegatingIdeaLexer
	}
	
	def Class<? extends IJvmTypeProvider.Factory> bindIJvmTypeProvider$Factory() {
		StubTypeProviderFactory
	}

	def void configureLanguageSpecificPsiModelAssociations(Binder binder) {
		binder.bind(IPsiModelAssociations).annotatedWith(LanguageSpecific).to(PsiModelAssociations)
	}

	def Class<? extends IPsiModelAssociator> bindIPsiModelAssociator() {
		PsiModelAssociations
	}

	@SingletonBinding
	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		DefaultAntlrTokenToAttributeIdMapper
	}
	
	def Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		DefaultHighlightingConfiguration
	}
	
	def void configureHighlightingTokenDefProvider(Binder binder) {
		binder.bind(ITokenDefProvider).annotatedWith(Names.named(LexerIdeBindings.HIGHLIGHTING)).to(AntlrTokenDefProvider)
	}
	
	@SingletonBinding
	def Class<? extends SyntaxHighlighter> bindSyntaxHighlighter() {
		DefaultSyntaxHighlighter
	}
	
	@SingletonBinding
	def Class<? extends NamesValidator> bindNamesValidator() {
		NullNamesValidator
	}
	
	@SingletonBinding
	def Class<? extends PairedBraceMatcher> bindPairedBraceMatcher() {
		DefaultPairedBraceMatcher
	}
	
	@SingletonBinding
	def Class<? extends IBracePairProvider> bindIBracePairProvider() {
		DefaultBracePairProvider
	}

	@SingletonBinding
	def Class<? extends PsiStructureViewFactory> bindPsiStructureViewFactory() {
		DefaultPsiStructureViewFactory
	}

	def void configureGlobalModificationTracker(Binder binder) {
		binder.bind(Key).annotatedWith(Names.named(BaseXtextFile.GLOBAL_MODIFICATION_COUNT)).toInstance(PsiModificationTracker.MODIFICATION_COUNT)
	}

	def Class<? extends IEncodingProvider> bindEncodingProvider() {
		IdeaEncodingProvider
	}
	
	def Class<? extends IContainer.Manager> bindIContainer$Manager() {
		ProjectDescriptionBasedContainerManager
	}
	
	def Class<? extends IProjectConfigProvider> bindProjectConfigProvider() {
		IdeaProjectConfigProvider
	}
	
	def Class<? extends IContextualOutputConfigurationProvider> bindIContextualOutputConfigurationProvider() {
		IdeaOutputConfigurationProvider
	}

	def Class<? extends IResourceDescriptionsProvider> bindIResourceDescriptionsProvider() {
		IdeaResourceDescriptionsProvider
	}
	
	@SingletonBinding
	def Class<? extends FormattingModelBuilder> bindFormattingModelBuilder() {
		DefaultFormattingModelBuilder
	}

	@SingletonBinding
	def Class<? extends CodeDocumentationAwareCommenter> bindCodeDocumentationAwareCommenter() {
		DefaultCommenter
	}

	@SingletonBinding
	def Class<? extends IIndentationInformation> bindIIndentationInformation() {
		CodeStyleSettingsIndentationInformation
	}
	
	def Class<? extends OperationCanceledManager> bindOperationCanceledManager() {
		IdeaOperationCanceledManager
	}

	def Class<? extends ITraceForURIProvider> bindPlatformNeutralTraceInformation() {
		ITraceForVirtualFileProvider
	}
	
	def Class<? extends ITraceForVirtualFileProvider> bindTraceInformation() {
		TraceForVirtualFileProvider
	}
}
