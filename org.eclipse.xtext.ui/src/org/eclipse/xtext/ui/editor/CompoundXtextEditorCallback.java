/*******************************************************************************
 * Copyright (c) 2010 Michael Clay and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.ui.editor;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * @author Michael Clay - Initial contribution and API
 */
public class CompoundXtextEditorCallback implements IXtextEditorCallback {
	private List<IXtextEditorCallback> editorCallbacks = Lists.newArrayList();
	private Logger log = Logger.getLogger(CompoundXtextEditorCallback.class);

	protected void handle(Exception e) {
		log.error(e.getMessage(), e);
	}

	@Inject
	public CompoundXtextEditorCallback(Injector injector) {
		List<Binding<IXtextEditorCallback>> bindingsByType = injector == null ? Lists
				.<Binding<IXtextEditorCallback>> newArrayList() : injector.findBindingsByType(TypeLiteral
				.get(IXtextEditorCallback.class));
		for (Binding<IXtextEditorCallback> binding : bindingsByType) {
			try {
				editorCallbacks.add(binding.getProvider().get());
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public void afterCreatePartControl(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				xtextEditorCallback.afterCreatePartControl(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public void afterSave(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				xtextEditorCallback.afterSave(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public void afterRevert(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				xtextEditorCallback.afterRevert(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public void beforeDispose(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				xtextEditorCallback.beforeDispose(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public boolean onValidateEditorInputState(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				if (!xtextEditorCallback.onValidateEditorInputState(xtextEditor)) {
					return false;
				}
			} catch (Exception e) {
				handle(e);
			}
		}
		return true;
	}

	@Override
	public void beforeSetInput(XtextEditor xtextEditor) {
		for (int i = editorCallbacks.size() - 1; i >= 0; i--) {
			try {
				editorCallbacks.get(i).beforeSetInput(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}

	@Override
	public void afterSetInput(XtextEditor xtextEditor) {
		for (IXtextEditorCallback xtextEditorCallback : editorCallbacks) {
			try {
				xtextEditorCallback.afterSetInput(xtextEditor);
			} catch (Exception e) {
				handle(e);
			}
		}
	}
}
