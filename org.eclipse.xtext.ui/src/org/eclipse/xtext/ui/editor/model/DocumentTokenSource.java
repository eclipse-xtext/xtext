/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.apache.log4j.Logger;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.parser.antlr.Lexer;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * For languages with lexers without backtracking. For these we can start lexing from the 
 * first damaged token instead of from the beginning. This is a lot faster especially when 
 * editing the trunk of big files.
 *  
 * As opposed to the original {@link BacktrackingLexerDocumentTokenSource}, the damaged region
 * returned by this class always includes the token touching the region the event refers to,
 * i.e. the first token overlapping wit the region or the token that ends with the beginning
 * of the region.  
 *   
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Sven Efftinge
 */
public class DocumentTokenSource {

	public static class TokenInfo {

		private final int length;
		private final int type;

		public TokenInfo(CommonToken token) {
			length = token.getStopIndex() - token.getStartIndex() + 1;
			type = token.getType();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + length;
			result = prime * result + type;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TokenInfo other = (TokenInfo) obj;
			if (length != other.length)
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TokenInfo [length=" + length + ", type=" + type + "]";
		}

		public int getAntlrTokenType() {
			return this.type;
		}

		public int getLength() {
			return this.length;
		}
	}

	/**
	 * @author Sven Efftinge - Initial contribution and API
	 */
	public static class TokenAdapter implements ILexerTokenRegion {

		private TokenInfo token;
		private int offset;

		public TokenAdapter(TokenInfo token, int offset) {
			this.token = token;
			this.offset = offset;
		}

		@Override
		public int getLength() {
			return token.getLength();
		}

		@Override
		public int getOffset() {
			return offset;
		}

		@Override
		public int getLexerTokenType() {
			return token.getAntlrTokenType();
		}

	}

	public static class IRegionIterable implements Iterable<ILexerTokenRegion> {

		private Iterable<TokenInfo> tokens = null;

		public IRegionIterable(Iterable<TokenInfo> tokens) {
			this.tokens = tokens;
		}

		@Override
		public Iterator<ILexerTokenRegion> iterator() {
			return new AbstractIterator<ILexerTokenRegion>() {

				private int offset = 0;
				private Iterator<TokenInfo> infos = tokens.iterator();

				@Override
				protected ILexerTokenRegion computeNext() {
					if (!infos.hasNext()) {
						endOfData();
						return null;
					}
					TokenInfo next = infos.next();
					try {
						return new TokenAdapter(next, offset);
					} finally {
						offset += next.getLength();
					}
				}
			};
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof IRegionIterable))
				return false;
			return tokens == ((IRegionIterable)obj).tokens;
		}
		
		@Override
		public int hashCode() {
			return System.identityHashCode(tokens);
		}
	}

	/**
	 * @author Jan Koehnlein
	 * @since 2.4
	 */
	protected static class RepairEntryData {
		final int offset;
		final int index;
		final CommonToken newToken;
		final TokenSource tokenSource;
		
		public RepairEntryData(int offset, int index, CommonToken newToken, TokenSource lexer) {
			super();
			this.offset = offset;
			this.index = index;
			this.newToken = newToken;
			this.tokenSource = lexer;
		}
	}
	
	private static final Logger logger = Logger.getLogger(DocumentTokenSource.class);
	
	private boolean checkInvariant = false;
	private List<TokenInfo> internalModifyableTokenInfos = Collections.emptyList();
	private List<TokenInfo> tokenInfos = Collections.emptyList();
	private IRegion previousRegion;
	private DocumentEvent previousEvent;

	@Inject
	@Named(LexerIdeBindings.HIGHLIGHTING)
	private Provider<Lexer> lexer;

	public Iterable<ILexerTokenRegion> getTokenInfos() {
		return new IRegionIterable(tokenInfos);
	}

	public IRegion getLastDamagedRegion() {
		return previousRegion;
	}

	public void setLexer(Provider<Lexer> lexer) {
		this.lexer = lexer;
	}

	protected void setTokens(List<TokenInfo> infos) {
		this.internalModifyableTokenInfos = infos;
		this.tokenInfos = Collections.unmodifiableList(Lists.newArrayList(infos));
	}
	
	/**
	 * @since 2.4
	 */
	protected List<TokenInfo> getInternalModifyableTokenInfos() {
		return internalModifyableTokenInfos;
	}

	protected List<TokenInfo> createTokenInfos(String string) {
		List<TokenInfo> result = Lists.newArrayListWithExpectedSize(string.length() / 3);
		TokenSource source = createTokenSource(string);
		CommonToken token = (CommonToken) source.nextToken();
		while (token.getType() != Token.EOF) {
			TokenInfo info = createTokenInfo(token);
			result.add(info);
			token = (CommonToken) source.nextToken();
		}
		return result;
	}

	protected TokenInfo createTokenInfo(CommonToken token) {
		TokenInfo info = new TokenInfo(token);
		return info;
	}

	public void updateStructure(final DocumentEvent e) {
		try {
			if (previousEvent == e && previousRegion != null) {
				return;
			}
			previousRegion = computeDamageRegion(e);
		} finally {
			previousEvent = e;
			if (isCheckInvariant()) {
				doCheckInvariant(e);
			}
		}
	}

	protected void doCheckInvariant(final DocumentEvent e) {
		List<TokenInfo> parsedTokenInfos = createTokenInfos(e.fDocument.get());
		if (!parsedTokenInfos.equals(internalModifyableTokenInfos)) {
			throw new IllegalStateException("Expected: '" + parsedTokenInfos + "' but was: '"
					+ internalModifyableTokenInfos + "'.");
		}
	}

	protected IRegion computeDamageRegion(final DocumentEvent e) {
		// empty document -> no dirty region
		if (e.getDocument().getLength() == 0) {
			setTokens(createTokenInfos(e.fDocument.get()));
			return new Region(0, 0);
		}

		// previously empty -> full document dirty
		if (internalModifyableTokenInfos.isEmpty()) {
			setTokens(createTokenInfos(e.fDocument.get()));
			return new Region(0, e.getDocument().getLength());
		}
		try {
			RepairEntryData repairEntryData = getRepairEntryData(e);
			int tokenStartsAt = repairEntryData.offset;
			int tokenInfoIdx = repairEntryData.index;
			CommonToken token = repairEntryData.newToken;
			if (token.getType() == Token.EOF)
				internalModifyableTokenInfos.subList(tokenInfoIdx, internalModifyableTokenInfos.size()).clear();
			int regionOffset = tokenStartsAt;
			int regionLength = e.fDocument.getLength()- tokenStartsAt;

			int lengthDiff = e.fText.length() - e.fLength;
			// compute region length
			while (true) {
				if (token.getType() == Token.EOF || tokenInfoIdx >= internalModifyableTokenInfos.size())
					break;
				while (true) {
					if (tokenInfoIdx >= internalModifyableTokenInfos.size())
						break;
					TokenInfo tokenInfo = internalModifyableTokenInfos.get(tokenInfoIdx);
					if (token.getStartIndex() >= e.fOffset + e.fText.length()) {
						if (tokenStartsAt + lengthDiff == token.getStartIndex() && tokenInfo.type == token.getType()
								&& token.getStopIndex() - token.getStartIndex() + 1 == tokenInfo.length) {
							return new Region(regionOffset, token.getStartIndex() - regionOffset);
						}
					}
					if (tokenStartsAt + lengthDiff + tokenInfo.length > token.getStopIndex() + 1)
						break;
					internalModifyableTokenInfos.remove(tokenInfoIdx);
					tokenStartsAt += tokenInfo.length;
					if (tokenStartsAt + lengthDiff > token.getStartIndex())
						break;
				}
				internalModifyableTokenInfos.add(tokenInfoIdx++, createTokenInfo(token));
				token = (CommonToken) repairEntryData.tokenSource.nextToken();
			}
			internalModifyableTokenInfos.subList(tokenInfoIdx, internalModifyableTokenInfos.size()).clear();
			// add subsequent tokens
			if (tokenInfoIdx >= internalModifyableTokenInfos.size()) {
				while (token.getType() != Token.EOF) {
					internalModifyableTokenInfos.add(createTokenInfo(token));
					token = (CommonToken) repairEntryData.tokenSource.nextToken();
				}
			}
			return new Region(regionOffset, regionLength);
		} catch(Exception exc) {
			logger.error("Error computing damaged region", exc);
			internalModifyableTokenInfos = createTokenInfos(e.fDocument.get());
			return new Region(0, e.fDocument.getLength());
		} finally {
			setTokens(internalModifyableTokenInfos);
		}
	}

	/**
	 * @since 2.4
	 */
	protected RepairEntryData getRepairEntryData(DocumentEvent e) throws Exception {
		int tokenStartsAt = 0;
		int tokenInfoIdx = 0;
		for(tokenInfoIdx = 0; tokenInfoIdx< getInternalModifyableTokenInfos().size(); ++tokenInfoIdx) {
			TokenInfo oldToken = getInternalModifyableTokenInfos().get(tokenInfoIdx);
			if(tokenStartsAt <= e.getOffset() && tokenStartsAt + oldToken.getLength() >= e.getOffset())
				break;
			tokenStartsAt += oldToken.getLength();
		}
		final TokenSource delegate = createTokenSource(e.fDocument.get(tokenStartsAt, e.fDocument.getLength() - tokenStartsAt));
		final int offset = tokenStartsAt;
		TokenSource source = new TokenSource() {
			@Override
			public Token nextToken() {
				CommonToken commonToken = (CommonToken) delegate.nextToken();
				commonToken.setText(commonToken.getText());
				commonToken.setStartIndex(commonToken.getStartIndex()+offset);
				commonToken.setStopIndex(commonToken.getStopIndex()+offset);
				return commonToken;
			}

			@Override
			public String getSourceName() {
				return delegate.getSourceName();
			}
		};
		final CommonToken token = (CommonToken) source.nextToken();
		return new RepairEntryData(offset, tokenInfoIdx, token, source);
	}
	
	/**
	 * @deprecated use {@link #createTokenSource(String)} instead.
	 */
	@Deprecated
	protected Lexer createLexer(String string) {
		Lexer l = lexer.get();
		l.setCharStream(new ANTLRStringStream(string));
		return l;
	}

	/**
	 * @since 2.4
	 */
	protected TokenSource createTokenSource(String string) {
		Lexer l = lexer.get();
		l.setCharStream(new ANTLRStringStream(string));
		return l;
	}

	public void setCheckInvariant(boolean checkInvariant) {
		this.checkInvariant = checkInvariant;
	}

	public boolean isCheckInvariant() {
		return checkInvariant;
	}

}
