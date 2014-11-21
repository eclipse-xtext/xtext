/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.formatting2;

public interface IFormattableSubDocument extends IFormattableDocument {
	IFormattableSubDocument requireFitsInLine(); // TODO: fits in line completely vs. first line fits in line.

	//		IFormattableSubDocument requireSingleLine();

	//		IFormattableSubDocument requireFitsInLine(int maxLineWidth);

	//		IFormattableSubDocument requireFitsInLine(int offset, int length);

	IFormattableSubDocument requireFitsInLine(int offset, int length, int maxLineWidth);

}