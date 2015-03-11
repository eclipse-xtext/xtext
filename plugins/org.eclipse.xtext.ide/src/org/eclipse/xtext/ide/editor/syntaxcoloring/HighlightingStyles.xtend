/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.syntaxcoloring

/**
 * @author Anton Kosyakov - Initial contribution and API
 */
interface HighlightingStyles {

	val KEYWORD_ID = "keyword"
	val PUNCTUATION_ID = "punctuation"
	val COMMENT_ID = "comment"
	val STRING_ID = "string"
	val NUMBER_ID = "number"
	val DEFAULT_ID = "default"
	val INVALID_TOKEN_ID = "error"
	val TASK_ID = "task"

}
