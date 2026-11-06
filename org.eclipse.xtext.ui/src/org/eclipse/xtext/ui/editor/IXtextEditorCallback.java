/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor;

import com.google.inject.ImplementedBy;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Michael Clay
 */
@ImplementedBy(IXtextEditorCallback.NullImpl.class)
public interface IXtextEditorCallback {
	
	void beforeSetInput(XtextEditor xtextEditor);
	
	void afterSetInput(XtextEditor xtextEditor);
	
	void afterCreatePartControl(XtextEditor editor);
	
	/**
	 * Announce that the given editor's state is the same as the persisted state.
	 * @param editor the editor which has not any unsaved changes
	 */
	void afterSave(XtextEditor editor);
	
	default void afterRevert(XtextEditor editor) {
		afterSave(editor);
	}
	
	void beforeDispose(XtextEditor editor);
	
	boolean onValidateEditorInputState(XtextEditor editor);
	
	public static class NullImpl implements IXtextEditorCallback {

		@Override
		public void beforeSetInput(XtextEditor xtextEditor) {
		}
		
		@Override
		public void afterSetInput(XtextEditor xtextEditor) {
		}
		
		@Override
		public void afterCreatePartControl(XtextEditor editor) {
		}

		@Override
		public void afterSave(XtextEditor editor) {
		}

		@Override
		public void beforeDispose(XtextEditor editor) {
		}

		@Override
		public boolean onValidateEditorInputState(XtextEditor editor) {
			return true;
		}
	}

}
