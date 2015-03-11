/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.editor.actions;

import static com.google.common.collect.Lists.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.ui.util.ClipboardUtil;
import org.eclipse.xtext.ui.util.ClipboardUtil.JavaImportData;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.TextRegion;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.conversion.IJavaCodeConverter;
import org.eclipse.xtext.xbase.ui.imports.ImportsUtil;
import org.eclipse.xtext.xbase.ui.internal.XtypeActivator;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.MembersInjector;

/**
 * @author Dennis H�bner (dhubner) - Initial contribution and API
 */
public class ImportsAwareClipboardAction extends TextEditorAction {
	public static class Factory implements IClipboardActionFactory {
		@Inject
		private MembersInjector<ImportsAwareClipboardAction> injector;

		@Override
		public TextEditorAction create(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
			ImportsAwareClipboardAction action = new ImportsAwareClipboardAction(bundle, prefix, editor, operationCode);
			injector.injectMembers(action);
			return action;
		}

	}

	private static final XbaseClipboardTransfer TRANSFER_INSTANCE = new XbaseClipboardTransfer();
	private final int operationCode;
	private ITextOperationTarget textOperationTarget;
	private @Inject ImportsUtil importsUtil;
	/**
	 * Some Xbase Languages like Xtend have a possibility to convert Java code.
	 */
	private @Inject(optional = true) IJavaCodeConverter javaCodeConverter;

	@Inject
	private EObjectAtOffsetHelper helper;

	/**
	 * Creates the action.
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param prefix
	 *            a prefix to be prepended to the various resource keys (described in <code>ResourceAction</code>
	 *            constructor), or <code>null</code> if none
	 * @param editor
	 *            the text editor. May not be <code>null</code>.
	 * @param operationCode
	 *            the operation code
	 */
	public ImportsAwareClipboardAction(ResourceBundle bundle, String prefix, ITextEditor editor, final int operationCode) {
		super(bundle, prefix, editor);
		this.operationCode = operationCode;

		if (operationCode == ITextOperationTarget.CUT) {
			setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
			setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
		} else if (operationCode == ITextOperationTarget.COPY) {
			setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
			setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);
		} else if (operationCode == ITextOperationTarget.PASTE) {
			setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
			setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);
		} else {
			Assert.isTrue(false, "Invalid operation code"); //$NON-NLS-1$
		}
		update();
	}

	protected void internalDoOperation() {
		if (operationCode == ITextOperationTarget.PASTE) {
			doPasteWithImportsOperation();
		} else {
			doCutCopyWithImportsOperation();
		}
	}

	private void doCutCopyWithImportsOperation() {
		try {
			final XbaseClipboardData cbData = createClipboardData();
			if (cbData != null) {
				ClipboardUtil.clipboardOperation(new Function<Clipboard, Boolean>() {

					@Override
					public Boolean apply(Clipboard clipboard) {
						TextTransfer textTransfer = TextTransfer.getInstance();
						Object txtTransferContent = clipboard.getContents(textTransfer);
						if (txtTransferContent == null) {
							// StyledText copied any data to ClipBoard
							return Boolean.FALSE;
						}
						String textData = (String) txtTransferContent;
						List<Object> datas = newArrayList();
						List<Transfer> transfers = newArrayList();
						datas.add(textData);
						transfers.add(textTransfer);
						RTFTransfer rtfTransfer = RTFTransfer.getInstance();
						String rtfData = (String) clipboard.getContents(rtfTransfer);
						if (rtfData != null) {
							datas.add(rtfData);
							transfers.add(rtfTransfer);
						}
						datas.add(cbData);
						transfers.add(TRANSFER_INSTANCE);
						clipboard.setContents(datas.toArray(), transfers.toArray(new Transfer[] {}));
						return Boolean.TRUE;
					}
				});
			}
		} finally {
			textOperationTarget.doOperation(operationCode);
		}
	}

	private void doPasteWithImportsOperation() {
		XbaseClipboardData xbaseClipboardData = ClipboardUtil
				.clipboardOperation(new Function<Clipboard, XbaseClipboardData>() {
					@Override
					public XbaseClipboardData apply(Clipboard input) {
						Object content = input.getContents(TRANSFER_INSTANCE);
						if (content instanceof XbaseClipboardData) {
							return (XbaseClipboardData) content;
						}
						return null;
					}
				});
		JavaImportData javaImportsContent = ClipboardUtil.getJavaImportsContent();
		String textFromClipboard = ClipboardUtil.getTextFromClipboard();
		if (xbaseClipboardData != null && !sameTarget(xbaseClipboardData)) {
			doPasteXbaseCode(xbaseClipboardData);
		} else if (javaImportsContent != null && canPasteJava(textFromClipboard, getJavaCodeConverter())) {
			doPasteJavaCode(textFromClipboard, javaImportsContent, getJavaCodeConverter());
		} else {
			textOperationTarget.doOperation(operationCode);
		}
	}

	private boolean canPasteJava(String javaToConvert, IJavaCodeConverter javaCodeConverter) {
		if (javaCodeConverter == null) {
			return true;
		}
		XtextEditor xtextEditor = EditorUtils.getXtextEditor(getTextEditor());
		EObject targetElement = xtextEditor.getDocument().priorityReadOnly(new IUnitOfWork<EObject, XtextResource>() {

			@Override
			public EObject exec(XtextResource state) throws Exception {
				EObject elementAtOffset = helper.resolveContainedElementAt(state, 10);
				return elementAtOffset;
			}
		});

		return javaCodeConverter.isCompatibleTargetObject(javaToConvert, targetElement);
	}

	private void doPasteXbaseCode(XbaseClipboardData xbaseClipboardData) {
		IRewriteTarget target = (IRewriteTarget) getTextEditor().getAdapter(IRewriteTarget.class);
		if (target != null) {
			target.beginCompoundChange();
		}
		try {
			textOperationTarget.doOperation(operationCode);
			importsUtil.addImports(xbaseClipboardData.getImports(), xbaseClipboardData.getStaticImports(),
					xbaseClipboardData.getExtensionImports(), getXtextDocument());
		} catch (Exception e) {
			XtypeActivator
					.getInstance()
					.getLog()
					.log(new Status(IStatus.ERROR, XtypeActivator.getInstance().getBundle().getSymbolicName(),
							"Unexpected internal error: ", e));
		} finally {
			if (target != null) {
				target.endCompoundChange();
			}
		}
	}

	private void doPasteJavaCode(String textFromClipboard, JavaImportData javaImportsContent,
			IJavaCodeConverter iJavaCodeConverter) {
		IRewriteTarget target = (IRewriteTarget) getTextEditor().getAdapter(IRewriteTarget.class);
		if (target != null) {
			target.beginCompoundChange();
		}
		try {
			textOperationTarget.doOperation(operationCode);
			importsUtil.addImports(javaImportsContent.getImports(), javaImportsContent.getStaticImports(),
					new String[] {}, getXtextDocument());
		} catch (Exception e) {
			XtypeActivator
					.getInstance()
					.getLog()
					.log(new Status(IStatus.ERROR, XtypeActivator.getInstance().getBundle().getSymbolicName(),
							"Unexpected internal error: ", e));
		} finally {
			if (target != null) {
				target.endCompoundChange();
			}
		}
	}

	private boolean sameTarget(XbaseClipboardData xbaseClipboardData) {
		IEditorInput editorInput = getTextEditor().getEditorInput();
		if (editorInput == null) {
			return false;
		}
		return xbaseClipboardData.getSourceIndentifier().equals(editorInput.toString());
	}

	private XbaseClipboardData createClipboardData() {
		try {
			IEditorInput editorInput = getTextEditor().getEditorInput();
			final String sourceIdentifier = editorInput != null ? editorInput.toString() : "nullEditorInput";
			IXtextDocument document = getXtextDocument();
			final ISelection selection = getTextEditor().getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection && !selection.isEmpty()) {
				final ITextSelection textSelection = (ITextSelection) selection;
				return document.readOnly(new IUnitOfWork<XbaseClipboardData, XtextResource>() {
					@Override
					public XbaseClipboardData exec(XtextResource state) throws Exception {
						ITextRegion region = new TextRegion(textSelection.getOffset(), textSelection.getLength() - 1);
						Triple<Set<String>, Set<String>, Set<String>> imports = importsUtil.collectImports(state,
								region);
						XbaseClipboardData clipboardData = new XbaseClipboardData(sourceIdentifier, Iterables.toArray(
								imports.getFirst(), String.class),
								Iterables.toArray(imports.getSecond(), String.class), Iterables.toArray(
										imports.getThird(), String.class));
						return clipboardData;
					}
				});
			}
		} catch (Exception e) {
			//TODO Log exception
			return null;
		}
		return null;
	}

	private IXtextDocument getXtextDocument() {
		XtextEditor xtextEditor = EditorUtils.getXtextEditor(getTextEditor());
		IXtextDocument document = xtextEditor.getDocument();
		return document;
	}

	@Override
	public void update() {
		super.update();
		if (isModifyOperation() && !canModifyEditor()) {
			setEnabled(false);
			return;
		}
		ITextEditor editor = getTextEditor();
		if (textOperationTarget == null && editor != null && operationCode != -1)
			textOperationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
		boolean isEnabled = (textOperationTarget != null && textOperationTarget.canDoOperation(operationCode));
		setEnabled(isEnabled);
	}

	@Override
	public void run() {
		if (textOperationTarget == null)
			return;
		ITextEditor editor = getTextEditor();
		if (editor == null)
			return;
		if (isModifyOperation() && !validateEditorInputState())
			return;
		BusyIndicator.showWhile(getDisplay(), new Runnable() {
			@Override
			public void run() {
				internalDoOperation();
			}
		});
	}

	private boolean isModifyOperation() {
		return operationCode != ITextOperationTarget.COPY;
	}

	private Shell getShell() {
		ITextEditor editor = getTextEditor();
		if (editor != null) {
			IWorkbenchPartSite site = editor.getSite();
			Shell shell = site.getShell();
			if (shell != null && !shell.isDisposed()) {
				return shell;
			}
		}
		return null;
	}

	private Display getDisplay() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getDisplay();
		}
		return null;
	}

	@Override
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		this.textOperationTarget = null;
	}

	protected IJavaCodeConverter getJavaCodeConverter() {
		return javaCodeConverter;
	}

	public static final class XbaseClipboardData {
		private String sourceIndentifier;
		private String[] imports;
		private String[] staticImports;
		private String[] extensionImports;

		public XbaseClipboardData(String sourceIndentifier, String[] imports, String[] staticImports,
				String[] extensionImports) {
			this.sourceIndentifier = sourceIndentifier;
			this.imports = imports;
			this.staticImports = staticImports;
			this.extensionImports = extensionImports;
		}

		public XbaseClipboardData(byte[] bytes) throws IOException {
			DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(bytes));
			try {
				sourceIndentifier = dataIn.readUTF();
				imports = readArray(dataIn);
				staticImports = readArray(dataIn);
				extensionImports = readArray(dataIn);
			} finally {
				dataIn.close();
			}
		}

		protected final String[] readArray(DataInputStream dataIn) throws IOException {
			int count = dataIn.readInt();
			String[] array = new String[count];
			for (int i = 0; i < count; i++) {
				array[i] = dataIn.readUTF();
			}
			return array;
		}

		public byte[] serialize() throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dataOut = new DataOutputStream(out);
			try {
				dataOut.writeUTF(sourceIndentifier);
				writeArray(dataOut, imports);
				writeArray(dataOut, staticImports);
				writeArray(dataOut, extensionImports);
			} finally {
				dataOut.close();
				out.close();
			}
			return out.toByteArray();
		}

		protected final void writeArray(DataOutputStream dataOut, String[] array) throws IOException {
			dataOut.writeInt(array.length);
			for (int i = 0; i < array.length; i++) {
				dataOut.writeUTF(array[i]);
			}
		}

		public String getSourceIndentifier() {
			return sourceIndentifier;
		}

		public String[] getImports() {
			return imports;
		}

		public String[] getStaticImports() {
			return staticImports;
		}

		public String[] getExtensionImports() {
			return extensionImports;
		}
	}

	private static class XbaseClipboardTransfer extends ByteArrayTransfer {

		private static final String TYPE_NAME = "xbase-source-with-imports-transfer-format";

		private static final int TYPEID = registerType(TYPE_NAME);

		@Override
		protected int[] getTypeIds() {
			return new int[] { TYPEID };
		}

		@Override
		protected String[] getTypeNames() {
			return new String[] { TYPE_NAME };
		}

		@Override
		protected void javaToNative(Object data, TransferData transferData) {
			if (data instanceof XbaseClipboardData) {
				try {
					super.javaToNative(((XbaseClipboardData) data).serialize(), transferData);
				} catch (IOException e) {
					//it's best to send nothing if there were problems
				}
			}
		}

		@Override
		protected Object nativeToJava(TransferData transferData) {
			byte[] bytes = (byte[]) super.nativeToJava(transferData);
			if (bytes != null) {
				try {
					return new XbaseClipboardData(bytes);
				} catch (IOException e) {
				}
			}
			return null;
		}

	}

}
