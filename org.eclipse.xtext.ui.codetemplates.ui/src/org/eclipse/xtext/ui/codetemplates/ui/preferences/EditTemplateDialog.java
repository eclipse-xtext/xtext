/*******************************************************************************
 * Copyright (c) 2010, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.codetemplates.ui.preferences;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.text.templates.ContextTypeRegistry;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.ui.codetemplates.ui.internal.CodetemplatesActivator;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory.Builder;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
import org.eclipse.xtext.ui.editor.validation.IValidationIssueProcessor;
import org.eclipse.xtext.validation.Issue;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class EditTemplateDialog extends StatusDialog implements IEditTemplateDialog {

	private Template fTemplate;

	private Text fNameText;
	private Text fDescriptionText;
	private Combo fContextCombo;
	private SourceViewer fPatternEditor;
	private EmbeddedEditorModelAccess partialModelEditor;
	private Button fInsertVariableButton;
	private Button fAutoInsertCheckbox;
	private boolean fIsNameModifiable;

	private String[][] fContextTypes;

	private ContextTypeRegistry fContextTypeRegistry;

	private final String languageName;

	private final TemplatesLanguageConfiguration configuration;

	private final IEditedResourceProvider resourceProvider;

	public EditTemplateDialog(Shell parent, Template template, boolean edit, boolean isNameModifiable, 
			org.eclipse.jface.text.templates.ContextTypeRegistry registry, TemplatesLanguageConfiguration configuration, 
			IEditedResourceProvider resourceProvider, String languageName) {
		super(parent);
		this.configuration = configuration;
		this.resourceProvider = resourceProvider;
		this.languageName = languageName;

		String title= edit
			? TemplateDialogMessages.EditTemplateDialog_title_edit
			: TemplateDialogMessages.EditTemplateDialog_title_new;
		setTitle(title);

		this.fTemplate= template;
		fIsNameModifiable= isNameModifiable;

		List<String[]> contexts= new ArrayList<>();
		for (Iterator<TemplateContextType> it= registry.contextTypes(); it.hasNext();) {
			TemplateContextType type= it.next();
			contexts.add(new String[] { type.getId(), type.getName() });
		}
		Collections.sort(contexts, new Comparator<String[]>() {
			Collator fCollator= Collator.getInstance();
			@Override
			public int compare(String[] o1, String[] o2) {
				return fCollator.compare(o1[1], o2[1]);
			}
		});
		fContextTypes= contexts.toArray(new String[contexts.size()][]);
		fContextTypeRegistry= registry;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public void create() {
		super.create();
		getButton(IDialogConstants.OK_ID).setEnabled(getStatus().isOK());
	}

	@Override
	protected Control createDialogArea(Composite ancestor) {
		Composite parent= new Composite(ancestor, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing= convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);

		parent.setLayout(layout);
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		ModifyListener listener= new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				doTextWidgetChanged(e.widget);
			}
		};

		if (fIsNameModifiable) {
			createLabel(parent, TemplateDialogMessages.EditTemplateDialog_name);

			Composite composite= new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			layout= new GridLayout();
			layout.numColumns= 4;
			layout.marginWidth= 0;
			layout.marginHeight= 0;
			composite.setLayout(layout);

			fNameText= createText(composite);

			createLabel(composite, TemplateDialogMessages.EditTemplateDialog_context);
			fContextCombo= new Combo(composite, SWT.READ_ONLY);
			fContextCombo.setVisibleItemCount(30); // default

			for (int i= 0; i < fContextTypes.length; i++) {
				fContextCombo.add(fContextTypes[i][1]);
			}

			fContextCombo.addModifyListener(listener);

			fAutoInsertCheckbox= createCheckbox(composite, TemplateDialogMessages.EditTemplateDialog_autoinsert);
			fAutoInsertCheckbox.setSelection(fTemplate.isAutoInsertable());
		}

		createLabel(parent, TemplateDialogMessages.EditTemplateDialog_description);

		int descFlags= fIsNameModifiable ? SWT.BORDER : SWT.BORDER | SWT.READ_ONLY;
		fDescriptionText= new Text(parent, descFlags );
		fDescriptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fDescriptionText.addModifyListener(listener);

		Label patternLabel= createLabel(parent, TemplateDialogMessages.EditTemplateDialog_pattern);
		patternLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		fPatternEditor= createEditor(parent);

		Label filler= new Label(parent, SWT.NONE);
		filler.setLayoutData(new GridData());

		Composite composite= new Composite(parent, SWT.NONE);
		layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData());

		fInsertVariableButton= new Button(composite, SWT.NONE);
		fInsertVariableButton.setLayoutData(getButtonGridData());
		fInsertVariableButton.setText(TemplateDialogMessages.EditTemplateDialog_insert_variable);
		fInsertVariableButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fPatternEditor.getTextWidget().setFocus();
				fPatternEditor.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		fDescriptionText.setText(fTemplate.getDescription());
		if (fIsNameModifiable) {
			fNameText.setText(fTemplate.getName());
			fNameText.addModifyListener(listener);
			fContextCombo.select(getIndex(fTemplate.getContextTypeId()));
		} else {
			fPatternEditor.getControl().setFocus();
		}
		applyDialogFont(parent);
		return composite;
	}

	protected void doTextWidgetChanged(Widget w) {
		if (w == fNameText || w == fContextCombo) {
			partialModelEditor.updatePrefix(getPrefix());
		}
	}

	protected String getContextName() {
		String result;
		if (fContextCombo != null && !fContextCombo.isDisposed()) {
			result = fContextCombo.getText();
		} else {
			String contextId = fTemplate.getContextTypeId();
			result = fContextTypeRegistry.getContextType(contextId).getName();	
		}
		if (result.startsWith("Keyword "))
			result = result.substring("Keyword ".length());
		return result;
	}

	protected String getContextId() {
		if (fContextCombo != null && !fContextCombo.isDisposed()) {
			String name= fContextCombo.getText();
			for (int i= 0; i < fContextTypes.length; i++) {
				if (name.equals(fContextTypes[i][1])) {
					return fContextTypes[i][0];
				}
			}
		}
		return fTemplate.getContextTypeId();
	}

	protected Status createErrorStatus(String message, TemplateException e) {
		return new Status(IStatus.ERROR, 
				CodetemplatesActivator.getInstance().getBundle().getSymbolicName(),
				message, e);
	}

	private static GridData getButtonGridData() {
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		return data;
	}

	private static Label createLabel(Composite parent, String name) {
		Label label= new Label(parent, SWT.NULL);
		label.setText(name);
		label.setLayoutData(new GridData());
		return label;
	}

	private static Button createCheckbox(Composite parent, String name) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(name);
		button.setLayoutData(new GridData());
		return button;
	}

	private static Text createText(Composite parent) {
		Text text= new Text(parent, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return text;
	}

	private SourceViewer createEditor(Composite parent) {
		SourceViewer viewer= createViewer(parent);
		int numberOfLines= viewer.getDocument().getNumberOfLines();
		if (numberOfLines < 7) {
			numberOfLines= 7;
		} else if (numberOfLines > 14) {
			numberOfLines= 14;
		}

		Control control= viewer.getControl();
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint= convertWidthInCharsToPixels(80);
		data.heightHint= convertHeightInCharsToPixels(numberOfLines);
		control.setLayoutData(data);
		return viewer;
	}
	
	protected SourceViewer createViewer(Composite parent) {
		Builder editorBuilder = configuration.getEmbeddedEditorFactory().newEditor(resourceProvider);
		editorBuilder.processIssuesBy(new IValidationIssueProcessor() {
			@Override
			public void processIssues(List<Issue> issues, IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;
				StringBuilder messages = new StringBuilder();
				for(Issue issue: issues) {
					if (issue.getSeverity() == Severity.ERROR) {
						if (messages.length() != 0)
							messages.append('\n');
						messages.append(issue.getMessage());
					}
				}
				if (messages.length() != 0) {
					result = createErrorStatus(messages.toString(), null);
				}
				final IStatus toBeUpdated = result;
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateStatus(toBeUpdated);
					}
				});
			}
		});
		EmbeddedEditor handle = editorBuilder.withParent(parent);
		partialModelEditor = handle.createPartialEditor(getPrefix(), fTemplate.getPattern(), "", true);
		return handle.getViewer();
	}
	
	protected String getPrefix() {
		String contextName = getContextName();
		String name = fTemplate.getName();
		if (fNameText != null && !fNameText.isDisposed()) {
			name = fNameText.getText();
		}
		String prefix = 
			"templates for " + languageName + 
			" '" + name + "'" + " for " + contextName + " >>";
		return prefix;
	}

	private int getIndex(String contextid) {
		if (contextid == null)
			return -1;

		for (int i= 0; i < fContextTypes.length; i++) {
			if (contextid.equals(fContextTypes[i][0])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void okPressed() {
		String name= fNameText == null ? fTemplate.getName() : fNameText.getText();
		boolean isAutoInsertable= fAutoInsertCheckbox != null && fAutoInsertCheckbox.getSelection();
		fTemplate= new Template(name, fDescriptionText.getText(), getContextId(), getPattern(), isAutoInsertable);
		super.okPressed();
	}

	@Override
	public Template getTemplate() {
		return fTemplate;
	}

	protected String getPattern() {
		return partialModelEditor.getEditablePart();
	}

	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		String sectionName= getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
		IDialogSettings section= configuration.getDialogSettings().getSection(sectionName);
		if (section == null)
			section= configuration.getDialogSettings().addNewSection(sectionName);
		return section;
	}

}
