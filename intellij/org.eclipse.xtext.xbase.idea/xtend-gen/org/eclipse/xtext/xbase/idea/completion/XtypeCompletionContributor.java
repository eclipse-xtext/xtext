/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.idea.completion;

import org.eclipse.xtext.idea.lang.AbstractXtextLanguage;
import org.eclipse.xtext.xbase.idea.completion.AbstractXtypeCompletionContributor;
import org.eclipse.xtext.xbase.idea.lang.XtypeLanguage;

@SuppressWarnings("all")
public class XtypeCompletionContributor extends AbstractXtypeCompletionContributor {
  public XtypeCompletionContributor() {
    this(XtypeLanguage.INSTANCE);
  }
  
  public XtypeCompletionContributor(final AbstractXtextLanguage lang) {
    super(lang);
  }
}
