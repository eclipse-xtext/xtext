/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.ui.wizard.project;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xtext.ui.internal.Activator;
import org.eclipse.xtext.xtext.wizard.BuildSystem;
import org.eclipse.xtext.xtext.wizard.LanguageServer;
import org.eclipse.xtext.xtext.wizard.SourceLayout;
import org.osgi.framework.Bundle;

import com.google.common.collect.Iterables;

public class AdvancedNewProjectPage extends WizardPage {

	private Button createUiProject;

	private Button createSDKProject;

	private Button createP2Project;

	private Button createWebProject;

	private Button createIdeProject;

	private Button createTestProject;

	private Button junitVersion4;

	private Button junitVersion6;

	private Combo preferredBuildSystem;

	private Combo createLanguageServer;

	private Combo sourceLayout;

	private Group createUiProjectSubGroup;

	private StatusWidget statusWidget;

	private boolean autoSelectIdeProject;

	private boolean autoSelectSDKProject;

	public AdvancedNewProjectPage(String pageName) {
		super(pageName);
		setTitle(Messages.AdvancedNewProjectPage_WindowTitle);
		setDescription(Messages.AdvancedNewProjectPage_Description);
	}

	@Override
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, false));
		control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Group(control, (group) -> {
			group.setText(Messages.AdvancedNewProjectPage_LabelFacets);
			createUiProject = CheckBox(group, (button) -> button.setText(Messages.AdvancedNewProjectPage_projEclipse));
			createUiProjectSubGroup = Group(group, (subGroup) -> {
				createSDKProject = CheckBox(subGroup, (button) -> button.setText(Messages.AdvancedNewProjectPage_projEclipseSDKFeature));
				createP2Project = CheckBox(subGroup, (button) -> button.setText(Messages.AdvancedNewProjectPage_projEclipseP2));
			});
			createWebProject = CheckBox(group, (button) -> {
				button.setText(Messages.AdvancedNewProjectPage_projWeb);
				button.setEnabled(true);
			});
			createIdeProject = CheckBox(group, (button) -> {
				button.setText(Messages.AdvancedNewProjectPage_projIde);
				button.setEnabled(false);
				InfoDecoration(button, Messages.AdvancedNewProjectPage_projIde_description);
				button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
			});

			Composite childComposite = new Composite(group, SWT.NONE);
			GridLayout layout = new GridLayout(4, false);
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			childComposite.setLayout(layout);

			createTestProject = CheckBox(childComposite, (button) -> {
				button.setText(Messages.WizardNewXtextProjectCreationPage_TestingSupport);
				button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			});

			Label label = new Label(childComposite, SWT.LEFT);
			label.setText(Messages.AdvancedNewProjectPage_junitVersion);

			junitVersion4 = Radio(childComposite, (button) -> {
				button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				button.setText("4");
				button.setSelection(false);
			});

			junitVersion6 = Radio(childComposite, (button) -> {
				button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
				button.setText("6");
				button.setSelection(true);
			});
		});

		Group(control, (group) -> {
			group.setText(Messages.AdvancedNewProjectPage_prefBuildSys);
			preferredBuildSystem = DropDown(group, (combo) -> {
				combo.setEnabled(true);
				combo.setItems(Arrays.stream(BuildSystem.values()).map((sl) -> sl.toString()).toArray(String[] :: new));
			});
		});

		Group(control, (group) -> {
			group.setText(Messages.AdvancedNewProjectPage_languageServer);
			createLanguageServer = DropDown(group, (combo) -> {
				combo.setEnabled(false);
				combo.setItems(Arrays.stream(LanguageServer.values()).map((sl) -> sl.toString()).toArray(String[] :: new));
				InfoDecoration(combo, Messages.AdvancedNewProjectPage_languageServer_description);
			});
		});

		Group(control, (group) -> {
			group.setText(Messages.AdvancedNewProjectPage_srcLayout);
			sourceLayout = DropDown(group, (combo) -> {
				combo.setEnabled(true);
				combo.setItems(Arrays.stream(SourceLayout.values()).map((sl) -> sl.toString()).toArray(String[] :: new));
			});
		});

		statusWidget = new StatusWidget(control, SWT.NONE);
		statusWidget.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		List<Button> uiButtons = Arrays.asList(createUiProject, createWebProject);

		SelectionAdapter selectionControlUi = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.getSource()).getSelection()) {
					if (!createIdeProject.getSelection()) {
						autoSelectIdeProject = true;
					}
					createIdeProject.setSelection(true);
					createIdeProject.setEnabled(false);
				} else {
					if (Iterables.all(uiButtons, (uiButton) -> !uiButton.getSelection())) {
						createIdeProject.setEnabled(true);
					}
					// auto deselect SDK and p2 project when Eclipse plug-in is deselected
					if (e.getSource() == createUiProject) {
						createSDKProject.setSelection(false);
						createP2Project.setSelection(false);
					}
				}
				validate(e);
			}
		};

		SelectionAdapter selectionControlUpdateSite = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.getSource()).getSelection()) {
					if (!createSDKProject.getSelection()) {
						autoSelectSDKProject = true;
					}
					createSDKProject.setSelection(true);
					createSDKProject.setEnabled(false);
				} else {
					createSDKProject.setEnabled(true);
				}
				validate(e);
			}
		};

		createUiProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean uiProjectSelected = createUiProject.getSelection();
				createUiProjectSubGroup.setEnabled(uiProjectSelected);
				createSDKProject.setEnabled(uiProjectSelected);
				createP2Project.setEnabled(uiProjectSelected);
				validate(e);
			}
		});

		preferredBuildSystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createLanguageServer.setEnabled(getPreferredBuildSystem() != BuildSystem.NONE);
				validate(e);
			}
		});

		createLanguageServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (isSelected(createLanguageServer, LanguageServer.NONE)) {
					if (Iterables.all(uiButtons, (uiButton) -> !uiButton.getSelection())) {
						createIdeProject.setEnabled(true);
					}
				} else {
					if (!createIdeProject.getSelection()) {
						createIdeProject.setSelection(true);
						autoSelectIdeProject = true;
					}
				}
				validate(e);
			}
		});

		createTestProject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = createTestProject.getSelection() == true;
				junitVersion4.setEnabled(enabled);
				junitVersion6.setEnabled(enabled);
				validate(e);
			}
		});
		
		SelectionAdapter selectionControl = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate(e);
			}
		};

		sourceLayout.addSelectionListener(selectionControl);
		createUiProject.addSelectionListener(selectionControlUi);
		createWebProject.addSelectionListener(selectionControlUi);
		createIdeProject.addSelectionListener(selectionControl);
		createSDKProject.addSelectionListener(selectionControl);
		createP2Project.addSelectionListener(selectionControlUpdateSite);
		createLanguageServer.addSelectionListener(selectionControl);
		setDefaults();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), "org.eclipse.xtext.xtext.ui.newProject_Advanced");
		
		setControl(control);
	}

	public void validate(SelectionEvent e) {
		statusWidget.clearStatus();
		checkWidgets(e);
		setPageComplete(statusWidget.getSevertity() != IMessageProvider.ERROR);
	}

	public void checkWidgets(SelectionEvent e) {
		List<Button> uiButtons = Arrays.asList(createUiProject, createWebProject);

		if (isSelected(preferredBuildSystem, BuildSystem.MAVEN) && !isBundleResolved("org.eclipse.m2e.maven.runtime")) {
			reportIssue(WARNING, Messages.AdvancedNewProjectPage_noM2e);
		}
		if (isSelected(preferredBuildSystem, BuildSystem.GRADLE) && !isBundleResolved("org.eclipse.buildship.core")) {
			reportIssue(WARNING, Messages.AdvancedNewProjectPage_noBuildship);
		}
		if (isSelected(preferredBuildSystem, BuildSystem.GRADLE) && createUiProject.getSelection()) {
			reportIssue(WARNING, Messages.AdvancedNewProjectPage_eclipseAndGradleWarn);
		}
		if (createUiProject.getSelection() && createP2Project.getSelection() && !createSDKProject.getSelection()) {
			addIssue(INFORMATION, Messages.AdvancedNewProjectPage_p2AndSdkInfo);
		}

		if (e == null) {
			return;
		}

		Object source = e.getSource();

		if (createUiProject.getSelection() && !isSelected(sourceLayout, SourceLayout.PLAIN)) {
			if (createUiProject == source) {
				String message = "'" + createUiProject.getText() + "' requires " + SourceLayout.PLAIN + " source layout. " //
						+ "Please <a>select '" + SourceLayout.PLAIN + "'</a> source layout.";
				reportIssue(ERROR, message, () -> select(sourceLayout, SourceLayout.PLAIN));
			} else {
				String message = SourceLayout.MAVEN + " source layout is not supported by the '" + createUiProject.getText() + "' project. " //
						+ "Please <a>deselect '" + createUiProject.getText() + "'</a>.";
				reportIssue(ERROR, message, () -> createUiProject.setSelection(false));
			}
		}

		if (isSelected(preferredBuildSystem, BuildSystem.NONE) && isSelected(sourceLayout, SourceLayout.MAVEN)) {
			if (preferredBuildSystem == source) {
				String message = "Maven/Gradle source layout is not supported without a build system. " //
						+ "Please <a>select '" + SourceLayout.PLAIN + "'</a> source layout.";
				reportIssue(ERROR, message, () -> select(sourceLayout, SourceLayout.PLAIN));
			} else {
				String message = "Maven/Gradle source layout is only supported when using Maven or Gradle build system. " //
						+ "You need to choose Maven or Gradle as build system. Select <a>Gradle</a> build.";
				reportIssue(ERROR, message, () -> select(preferredBuildSystem, BuildSystem.GRADLE));
			}
		}

		if (createWebProject.getSelection() && isSelected(preferredBuildSystem, BuildSystem.NONE)) {
			if (preferredBuildSystem == source) {
				String message = "The '" + createWebProject.getText() + "' project can not be build without a build system. " //
						+ "Please <a>deselect '" + createWebProject.getText() + "'</a>.";
				reportIssue(ERROR, message, () -> createWebProject.setSelection(false));
			} else {
				String message = "To build the '" + createWebProject.getText() + "' " //
						+ "project, you need to choose Maven or Gradle as build system." //
						+ "Select <a>Gradle</a> build.";
				reportIssue(ERROR, message, () -> select(preferredBuildSystem, BuildSystem.GRADLE));
			}
		}

		boolean isSelected = Iterables.any(uiButtons, uiButton -> uiButton.getSelection());
		if (isSelected) {
			createIdeProject.setEnabled(false);
		}

		if (autoSelectIdeProject) {
			autoSelectIdeProject = false;
			String message = "'" + createIdeProject.getText() + "' project was automatically selected as option '"
					+ ((Button) source).getText() + "' requires it.";
			reportIssue(INFORMATION, message);
		}
		
		if (autoSelectSDKProject) {
			autoSelectSDKProject = false;
			String message = "'" + createSDKProject.getText() + "' was automatically selected as option '" //
					+ ((Button) source).getText() + "' requires it.";
			reportIssue(INFORMATION, message);
		}
	}

	protected void select(Combo combo, Enum<?> enu) {
		String[] items = combo.getItems();
		for (int i = 0; i < items.length; i++) {
			if (enu.toString().equals(items[i])) {
				combo.select(i);
				break;
			}
		}
	}

	protected boolean isSelected(Combo combo, Enum<?> enu) {
		return enu.toString().equals(combo.getText());
	}

	protected <T extends Control> void reportIssue(int severity, String text) {
		reportIssue(severity, text, () -> {});
	}

	protected <T extends Control> void reportIssue(int severity, String text, Procedure0 fix) {
		statusWidget.setStatus(severity, text, fix, () -> {
			validate(null);
		});
		((Composite) getControl()).layout();
	}

	protected <T extends Control> void addIssue(int severity, String text) {
		statusWidget.addStatus(severity, text);
		((Composite) getControl()).layout();
	}

	protected boolean isBundleResolved(String bundleId) {
		Bundle[] bundles = Activator.getInstance().getBundle().getBundleContext().getBundles();
		Optional<Bundle> bundle = Arrays.stream(bundles).filter(b -> bundleId.equals(b.getSymbolicName())).findFirst();
		return bundle.isPresent() && (bundle.get().getState() & (Bundle.RESOLVED | Bundle.STARTING) | Bundle.ACTIVE) != 0;
	}

	protected Group Group(Composite parent, Procedure1<? super Group> config) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		config.apply(group);
		return group;
	}

	protected Button CheckBox(Composite composite, Procedure1<? super Button> config) {
		Button button = new Button(composite, SWT.CHECK);
		button.setFont(button.getParent().getFont());
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		config.apply(button);
		return button;
	}

	protected Button Radio(Composite composite, Procedure1<? super Button> config) {
		Button button = new Button(composite, SWT.RADIO);
		button.setFont(button.getParent().getFont());
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		config.apply(button);
		return button;
	}

	protected Combo DropDown(Composite parent, Procedure1<? super Combo> config) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setFont(parent.getFont());
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		config.apply(combo);
		return combo;
	}

	protected ControlDecoration InfoDecoration(Control control, String text) {
		FieldDecoration infoField = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);
		ControlDecoration controlDecoration = new ControlDecoration(control, (SWT.TOP + SWT.RIGHT));
		controlDecoration.setImage(infoField.getImage());
		controlDecoration.setDescriptionText(text);
		controlDecoration.setShowHover(true);
		return controlDecoration;
	}

	protected void setDefaults() {
		createUiProject.setSelection(true);
		createIdeProject.setSelection(true);
		createTestProject.setSelection(true);
		createWebProject.setSelection(false);
		createSDKProject.setSelection(false);
		createP2Project.setSelection(false);

		select(preferredBuildSystem, BuildSystem.NONE);
		select(createLanguageServer, LanguageServer.NONE);
		select(sourceLayout, SourceLayout.PLAIN);
	}

	public boolean isCreateUiProject() {
		return createUiProject.getSelection();
	}

	public boolean isCreateTestProject() {
		return createTestProject.getSelection();
	}

	public boolean isCreateIdeProject() {
		return createIdeProject.getSelection();
	}

	public boolean isCreateWebProject() {
		return createWebProject.getSelection();
	}

	public boolean isCreateSdkProject() {
		return createUiProject.getSelection() && createSDKProject.getSelection();
	}

	public boolean isCreateP2Project() {
		return createUiProject.getSelection() && createP2Project.getSelection();
	}

	public BuildSystem getPreferredBuildSystem() {
		return BuildSystem.values()[preferredBuildSystem.getSelectionIndex()];
	}

	public SourceLayout getSourceLayout() {
		return SourceLayout.values()[sourceLayout.getSelectionIndex()];
	}

	public LanguageServer getLanguageServer() {
		if (getPreferredBuildSystem() == BuildSystem.NONE) {
			return LanguageServer.NONE;
		}
		return LanguageServer.values()[createLanguageServer.getSelectionIndex()];
	}

	public JUnitVersion getSelectedJUnitVersion() {
		if (junitVersion4.getSelection()) {
			return JUnitVersion.JUNIT_4;
		} else if (junitVersion6.getSelection()) {
			return JUnitVersion.JUNIT_6;
		} else {
			throw new IllegalStateException("JUnit version could not be determined");
		}
	}
}
