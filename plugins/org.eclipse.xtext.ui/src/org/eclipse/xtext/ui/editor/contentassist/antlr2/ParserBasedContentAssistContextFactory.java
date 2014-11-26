/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.contentassist.antlr2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.AbstractElement;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.contentassist.AbstractContentAssistContextFactory;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher;
import org.eclipse.xtext.util.TextRegionWithLineInformation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * @since 2.8
 */
@Singleton
public class ParserBasedContentAssistContextFactory extends AbstractContentAssistContextFactory {
	
	@Inject
	private Provider<StatefulFactory> statefulFactoryProvider;

	private ExecutorService pool;
	
	public ParserBasedContentAssistContextFactory() {
		pool = Executors.newFixedThreadPool(3);
	}
	
	public Provider<StatefulFactory> getStatefulFactoryProvider() {
		return statefulFactoryProvider;
	}
	
	@SuppressWarnings("unchecked") //safe cast
	public void setStatefulFactoryProvider(Provider<? extends StatefulFactory> statefulFactoryProvider) {
		this.statefulFactoryProvider = (Provider<StatefulFactory>) statefulFactoryProvider;
	}
	
	protected ExecutorService getPool() {
		return pool;
	}
	
	public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource) {
		StatefulFactory factory = statefulFactoryProvider.get();
		factory.setPool(pool);
		return factory.create(viewer, offset, resource);
	}
	
	public static class StatefulFactory {
		
		@Inject
		protected Provider<ContentAssistContext.Builder> contentAssistContextProvider;
		
		@Inject
		private ContentAssistContextFactory delegate;
		
		@Inject
		protected PrefixMatcher matcher;
		
		protected ITextViewer viewer;

		protected ITextSelection selection;

		protected XtextResource resource;

		public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource) {
			this.viewer = viewer;
			this.resource = resource;
			return doCreateContexts(offset);
		}
		
		public void setPool(ExecutorService pool) {
			delegate.setPool(pool);
		}
		
		public ContentAssistContextFactory getDelegate() {
			return delegate;
		}
		
		public void setDelegate(ContentAssistContextFactory delegate) {
			this.delegate = delegate;
		}

		protected ContentAssistContext[] doCreateContexts(int offset) {
			ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();
			TextRegionWithLineInformation region = new TextRegionWithLineInformation(selection.getOffset(), selection.getLength(), selection.getStartLine(), selection.getEndLine());
			org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext[] delegateContexts = delegate.create(viewer.getDocument().get(), region, offset, resource);
			ContentAssistContext[] contexts = new ContentAssistContext[delegateContexts.length];
			for (int i = 0; i < delegateContexts.length; i++) {
				contexts[i] = convert(delegateContexts[i]).toContext();
			}
			return contexts;
		}

		protected ContentAssistContext.Builder convert(org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext delegateContext) {
			ContentAssistContext.Builder context = contentAssistContextProvider.get();

			context.setRootNode(delegateContext.getRootNode());
			context.setLastCompleteNode(delegateContext.getLastCompleteNode());
			context.setCurrentNode(delegateContext.getCurrentNode());

			context.setRootModel(delegateContext.getRootModel());
			context.setCurrentModel(delegateContext.getCurrentModel());
			context.setPreviousModel(delegateContext.getPreviousModel());
			context.setOffset(delegateContext.getOffset());
			context.setViewer(viewer);
			context.setPrefix(delegateContext.getPrefix());
			Region region = new Region(delegateContext.getReplaceRegion().getOffset(), delegateContext.getReplaceRegion().getLength());
			context.setReplaceRegion(region);
			context.setSelectedText(delegateContext.getSelectedText());
			context.setMatcher(matcher);
			context.setResource(resource);
			for (AbstractElement grammarElement : delegateContext.getFirstSetGrammarElements()) {
				context.accept(grammarElement);
			}
			return context;
		}
	}
}
