/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.formatting2;

import java.util.List;

/**
 * Relevant state and methods during execution of a {@link ITextReplacer text replacer}.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.8
 * @author Moritz Eysholdt - Initial API and implementation
 */
public interface ITextReplacerContext {

	Integer canAutowrap();

	IFormattableDocument getDocument();

	AbstractFormatter2 getFormatter();

	int getIndentation();

	/**
	 * Returns the current indentation as a string.
	 * @see #getIndentationString(int)
	 * @see #getIndentation()
	 */
	String getIndentationString();

	String getIndentationString(int indentationLevel);

	String getNewLinesString(int count);

	int getLeadingCharsInLineCount();

	Iterable<ITextReplacement> getLocalReplacements();

	ITextReplacerContext getPreviousContext();

	List<ITextReplacement> getReplacementsUntil(ITextReplacerContext first);

	ITextReplacer getReplacer();

	boolean isAutowrap();

	boolean isInsideFormattedRegion();

	void replaceText(CharSequence text);

	void replaceText(int offset, int length, CharSequence text);

	void replaceText(ITextReplacement replacement);

	void replaceText(ITextSegment region, CharSequence text);

	void setAutowrap(boolean value);

	void setCanAutowrap(Integer value);

	void setNextReplacerIsChild();

	ITextReplacerContext withIndentation(int indentation);
	
	ITextReplacerContext withDocument(IFormattableDocument document);

	ITextReplacerContext withReplacer(ITextReplacer replacer);

}