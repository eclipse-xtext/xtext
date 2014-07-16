/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.generator.parser.antlr.ex.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.eclipse.xtext.parser.antlr.AntlrTokenDefProvider;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class MutableTokenDefProvider extends AntlrTokenDefProvider {

	private final Charset encoding;
	private final KeywordHelper keywordHelper;

	/**
	 * @since 2.7
	 */
	public MutableTokenDefProvider(KeywordHelper keywordHelper, Charset encoding) {
		this.encoding = encoding;
		this.keywordHelper = keywordHelper;
	}
	
	/**
	 * @deprecated use {@link #MutableTokenDefProvider(KeywordHelper, Charset)} instead.
	 */
	@Deprecated
	public MutableTokenDefProvider() {
		this(null, Charset.defaultCharset());
	}
	
	@Override
	protected BufferedReader createReader(InputStream stream) {
		return new BufferedReader(new InputStreamReader(stream, encoding));
	}
	
	public void writeTokenFile(PrintWriter out) throws IOException {
		List<Map.Entry<Integer, String>> entries = Lists.newArrayList(getTokenDefMap().entrySet());
		ListExtensions.sortInplaceBy(entries, new Functions.Function1<Map.Entry<Integer, String>, String>() {
			public String apply(Map.Entry<Integer, String> p) {
				return p.getValue();
			}
		});
		for(Map.Entry<Integer, String> entry: entries) {
			out.print(entry.getValue());
			out.print('=');
			out.println(entry.getKey());
		}
		out.close();
	}
	
	@Override
	protected boolean isKeywordToken(String antlrTokenDef) {
		if (keywordHelper != null) {
			return keywordHelper.isKeywordRule(antlrTokenDef);
		}
		return super.isKeywordToken(antlrTokenDef);
	}

	@Override
	protected void setTokenDefMap(Map<Integer, String> tokenDefMap) {
		this.tokenDefMap = tokenDefMap;
	}

}
