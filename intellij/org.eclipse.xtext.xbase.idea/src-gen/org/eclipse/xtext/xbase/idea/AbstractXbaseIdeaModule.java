package org.eclipse.xtext.xbase.idea;

public class AbstractXbaseIdeaModule extends org.eclipse.xtext.idea.DefaultIdeaModule {
	
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider> bindIAntlrTokenFileProvider() {
		return org.eclipse.xtext.xbase.idea.lang.parser.antlr.XbaseAntlrTokenFileProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.parser.antlr.Lexer> bindLexer() {
		return org.eclipse.xtext.xbase.idea.parser.antlr.internal.PsiInternalXbaseLexer.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public void configureRuntimeLexer(com.google.inject.Binder binder) {
		binder.bind(org.eclipse.xtext.parser.antlr.Lexer.class).annotatedWith(com.google.inject.name.Names.named(org.eclipse.xtext.parser.antlr.LexerBindings.RUNTIME)).to(org.eclipse.xtext.xbase.idea.parser.antlr.internal.PsiInternalXbaseLexer.class);
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends com.intellij.lang.PsiParser> bindPsiParser() {
		return org.eclipse.xtext.xbase.idea.lang.parser.XbasePsiParser.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.idea.parser.TokenTypeProvider> bindTokenTypeProvider() {
		return org.eclipse.xtext.xbase.idea.lang.parser.XbaseTokenTypeProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends com.intellij.lang.ParserDefinition> bindParserDefinition() {
		return org.eclipse.xtext.xbase.idea.lang.parser.XbaseParserDefinition.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	@org.eclipse.xtext.service.SingletonBinding
	public Class<? extends org.eclipse.xtext.idea.lang.IElementTypeProvider> bindIElementTypeProvider() {
		return org.eclipse.xtext.xbase.idea.lang.XbaseElementTypeProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.common.types.access.IJvmTypeProvider.Factory> bindIJvmTypeProvider$Factory() {
		return org.eclipse.xtext.xbase.idea.types.access.StubTypeProviderFactory.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.common.types.xtext.AbstractTypeScopeProvider> bindAbstractTypeScopeProvider() {
		return org.eclipse.xtext.xbase.idea.types.StubBasedTypeScopeProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator> bindJvmModelAssociator() {
		return org.eclipse.xtext.xbase.idea.jvmmodel.PsiJvmModelAssociator.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	@org.eclipse.xtext.service.SingletonBinding
	public Class<? extends org.eclipse.xtext.xbase.idea.types.stubs.JvmDeclaredTypeShortNameIndex> bindJvmDeclaredTypeShortNameIndex() {
		return org.eclipse.xtext.xbase.idea.types.stubs.JvmDeclaredTypeShortNameIndex.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	public Class<? extends org.eclipse.xtext.xbase.typesystem.internal.IFeatureScopeTracker.Provider> bindIFeatureScopeTracker$Provider() {
		return org.eclipse.xtext.xbase.typesystem.internal.OptimizingFeatureScopeTrackerProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	@org.eclipse.xtext.service.SingletonBinding
	public Class<? extends com.intellij.ide.hierarchy.type.JavaTypeHierarchyProvider> bindJavaTypeHierarchyProvider() {
		return org.eclipse.xtext.xbase.idea.ide.hierarchy.JvmDeclaredTypeHierarchyProvider.class;
	}
	// contributed by org.eclipse.xtext.idea.generator.IdeaPluginGenerator
	@org.eclipse.xtext.service.SingletonBinding
	public Class<? extends com.intellij.ide.hierarchy.call.JavaCallHierarchyProvider> bindJavaCallHierarchyProvider() {
		return org.eclipse.xtext.xbase.idea.ide.hierarchy.JvmExecutableCallHierarchyProvider.class;
	}
	
	
}
