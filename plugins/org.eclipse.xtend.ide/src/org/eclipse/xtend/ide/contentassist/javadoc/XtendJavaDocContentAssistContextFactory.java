/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.contentassist.javadoc;

import java.util.concurrent.ExecutorService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.xtend.ide.contentassist.antlr.FlexerBasedContentAssistContextFactory;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.antlr.DelegatingContentAssistContextFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Holger Schill - Initial contribution and API
 */
public class XtendJavaDocContentAssistContextFactory extends DelegatingContentAssistContextFactory {

	@Inject
	private Provider<StatefulFactory> statefulFactoryProvider;

	@Override
	public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource) {
		setStatefulFactoryProvider(statefulFactoryProvider);
		return super.create(viewer, offset, resource);
	}
	
	public static class StatefulFactory extends DelegatingContentAssistContextFactory.StatefulFactory {
		@Inject
		private FlexerBasedJavaDocContentAssistContextFactory delegate;
		@Override
		public ContentAssistContext[] create(ITextViewer viewer, int offset, XtextResource resource) {
			setDelegate(delegate);
			return super.create(viewer, offset, resource);
		}
	}

	public static class FlexerBasedJavaDocContentAssistContextFactory extends FlexerBasedContentAssistContextFactory {

		@Override
		public void setPool(ExecutorService pool) {
			super.setPool(pool);
		}
		
		@Override
		public String getPrefix(INode node) {
			if(node != null) {
				int offsetInNode = getCompletionOffset() - node.getOffset();
				String textInFront = node.getText().substring(0, offsetInNode);
				for (int i = offsetInNode - 1; i > 0; i--) {
					char c = textInFront.charAt(i);
					if (Character.isWhitespace(c)) {
						return textInFront.substring(i + 1);
					}
				}
			}
			return "";
		}
	}
}
