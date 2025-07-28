/*******************************************************************************
 * Copyright (c) 2009, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.formatting.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.formatting.IElementMatcherProvider;
import org.eclipse.xtext.formatting.IElementMatcherProvider.IElementMatcher;
import org.eclipse.xtext.formatting.IIndentationInformation;
import org.eclipse.xtext.formatting.ILineSeparatorInformation;
import org.eclipse.xtext.formatting.IWhitespaceInformationProvider;
import org.eclipse.xtext.formatting.impl.AbstractFormattingConfig.ElementPattern;
import org.eclipse.xtext.parsetree.reconstr.IHiddenTokenHelper;
import org.eclipse.xtext.parsetree.reconstr.ITokenStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public abstract class AbstractDeclarativeFormatter extends BaseFormatter {

	private static final Logger log = Logger.getLogger(AbstractDeclarativeFormatter.class);

	@Singleton
	protected static class ConfigStore {
		protected FormattingConfig config;
	}

	@Inject
	private ConfigStore config;

	@Inject
	private IGrammarAccess grammarAccess;

	@Inject
	private IHiddenTokenHelper hiddenTokenHelper;

	@Inject
	private IWhitespaceInformationProvider whitespaceInformationProvider;

	private URI contextResourceURI;
	
	@Inject
	private IElementMatcherProvider matcherProvider;

	protected abstract void configureFormatting(FormattingConfig config);

	@Override
	public ITokenStream createFormatterStream(String indent, ITokenStream out, boolean preserveWhitespaces) {
		return new FormattingConfigBasedStream(out, indent, getConfig(), createMatcher(), hiddenTokenHelper,
				preserveWhitespaces);
	}
	
	/**
	 * @since 2.3
	 */
	@Override
	public ITokenStream createFormatterStream(EObject context, String indent, ITokenStream out, boolean preserveWhitespaces) {
		if(context != null && context.eResource() != null && context.eResource().getURI() != null) {
			contextResourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(context).trimFragment();
		} else if (context != null && context.eResource() == null) {
			log.warn("Model has no XtextResource. This is likely to cause follow-up errors");
		}
		return new FormattingConfigBasedStream(out, indent, getConfig(), createMatcher(), hiddenTokenHelper,
				preserveWhitespaces);
	}
	
	@SuppressWarnings("deprecation")
	protected FormattingConfig createFormattingConfig() {
		FormattingConfig cfg = new FormattingConfig2(grammarAccess, hiddenTokenHelper, getIndentInfo(), getLineSeparatorInfo());
		cfg.setWhitespaceRule(getWSRule());
		return cfg;
	}

	protected IElementMatcher<ElementPattern> createMatcher() {
		return matcherProvider.createMatcher(getConfig().getLocatorsForSemanticTokens());
	}

	protected synchronized FormattingConfig getConfig() {
		if (config.config == null) {
			config.config = createFormattingConfig();
			configureFormatting(config.config);
		}
		return config.config;
	}

	protected IGrammarAccess getGrammarAccess() {
		return grammarAccess;
	}

	protected IHiddenTokenHelper getHiddenTokenHelper() {
		return hiddenTokenHelper;
	}

	protected IIndentationInformation getIndentInfo() {
		return whitespaceInformationProvider.getIndentationInformation(contextResourceURI);
	}
	
	/**
	 * @since 2.3
	 */
	protected ILineSeparatorInformation getLineSeparatorInfo() {
		return whitespaceInformationProvider.getLineSeparatorInformation(contextResourceURI);
	}
	
	protected IElementMatcherProvider getMatcherProvider() {
		return matcherProvider;
	}

	protected void saveDebugGraphvizDiagram(String filename) {
		try {
			File file = new File(filename).getCanonicalFile();
			System.out.println("Saving Graphviz diagram to " + file);
			FileOutputStream fos = new FileOutputStream(file);
			PrintStream ps = new PrintStream(fos);
			new ElementMatcherToDot().draw(createMatcher(), ps);
			ps.flush();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * TODO Why deprecated?
	 */
	@Deprecated
	public void setGrammarAccess(IGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}

}
