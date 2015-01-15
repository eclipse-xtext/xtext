/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.tests.builder;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtend.ide.tests.builder.AbstractSingleEditorQueuedBuildTest;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

/**
 * @author Anton Kosyakov - Initial contribution and API
 */
@SuppressWarnings("all")
public class SaveWithoutReconciliationQueuedBuildDataTest extends AbstractSingleEditorQueuedBuildTest {
  @Override
  public ITextEditor doSave(final ITextEditor editor, final Function1<? super ITextEditor, ? extends ITextEditor> consumer) {
    return this._javaEditorExtension.save(editor, consumer);
  }
  
  @Override
  public ITextEditor doClose(final ITextEditor editor, final Function1<? super ITextEditor, ? extends ITextEditor> consumer) {
    return this._javaEditorExtension.close(editor, consumer);
  }
}
