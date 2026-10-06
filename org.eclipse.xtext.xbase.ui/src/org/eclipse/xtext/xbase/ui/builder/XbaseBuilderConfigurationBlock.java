/*******************************************************************************
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.builder;

import static org.eclipse.xtext.xbase.ui.builder.XbaseBuilderPreferenceAccess.*;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.xtext.builder.preferences.BuilderConfigurationBlock;
import org.eclipse.xtext.util.JavaVersion;

import com.google.inject.Inject;

/**
 * Builder configuration block that adds compiler settings for Xbase.
 * 
 * @author Miro Spoenemann - Initial contribution and API
 * @author Lorenzo Bettini - useXbaseGenerated
 */
public class XbaseBuilderConfigurationBlock extends BuilderConfigurationBlock {

	@Inject
	private XbaseBuilderPreferenceAccess preferenceAccess;

	private Combo versionCombo;

	private Button useComplianceButton;
	
	@Override
	protected void createGeneralSectionItems(Composite composite) {
		super.createGeneralSectionItems(composite);
		
		useComplianceButton = addCheckBox(composite,
				"Use source compatibility level from Java settings",
				PREF_USE_COMPILER_SOURCE, BOOLEAN_VALUES, 0);
		
		// JAVA5/6/7 are deprecated aliases of JAVA8 (kept only so that later constants keep their ordinal);
		// they must not be offered here, or the combo would show "Java 8" four times and could persist a
		// deprecated qualifier that - unlike its label suggests - does not behave like Java 8 (see
		// JavaVersion.isAtLeast).
		JavaVersion[] supportedVersions = getSupportedJavaVersions();
		String[] values = new String[supportedVersions.length];
		String[] valueLabels = new String[supportedVersions.length];
		for (int i = 0; i < supportedVersions.length; i++) {
			JavaVersion v = supportedVersions[i];
			values[i] = v.toString();
			valueLabels[i] = v.getLabel();
		}
		versionCombo = addComboBox(composite, "Source compatibility level of generated code:",
				PREF_JAVA_VERSION, 0, values, valueLabels);
		SelectionListener selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateVersionCombo();
			}
		};
		selectionListener.widgetSelected(null);
		useComplianceButton.addSelectionListener(selectionListener);
		
		addCheckBox(composite, "Generate @SuppressWarnings annotations",
				PREF_GENERATE_SUPPRESS_WARNINGS, BOOLEAN_VALUES, 0);
		addCheckBox(composite, "Annotate synthetic members with @XbaseGenerated",
				USE_XBASE_GENERATED, BOOLEAN_VALUES, 0);
		
		final Button generateGeneratedButton = addCheckBox(composite, "Generate @Generated annotations",
				PREF_GENERATE_GENERATED, BOOLEAN_VALUES, 0);
		
		final Button includeDateButton = addCheckBox(composite, "Include current time information",
				PREF_DATE_IN_GENERATED, BOOLEAN_VALUES, INDENT_AMOUNT);
		includeDateButton.setEnabled(generateGeneratedButton.getSelection());
		
		final Text commentText = addTextField(composite, "Comment:",
				PREF_GENERATED_COMMENT, INDENT_AMOUNT, 0);
		commentText.setEnabled(generateGeneratedButton.getSelection());
		generateGeneratedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				includeDateButton.setEnabled(generateGeneratedButton.getSelection());
				commentText.setEnabled(generateGeneratedButton.getSelection());
			}
		});
	}
	
	@Override
	protected void updateCombo(Combo curr) {
		if(curr == versionCombo)
			updateVersionCombo();
		else
			super.updateCombo(curr);
	}
	
	private void updateVersionCombo() {
		boolean useCompliance = useComplianceButton.getSelection();
		versionCombo.setEnabled(!useCompliance);
		if (useCompliance) {
			String javaSourceOption = javaValue(JavaCore.COMPILER_SOURCE);
			JavaVersion javaVersion = preferenceAccess.fromCompilerSourceLevel(javaSourceOption);
			JavaVersion[] supportedVersions = getSupportedJavaVersions();
			JavaVersion selectedVersion = supportedVersions[versionCombo.getSelectionIndex()];
			if (javaVersion != selectedVersion) {
				int index = indexOf(supportedVersions, javaVersion);
				if (index >= 0) {
					versionCombo.select(index);
				}
			}
		}
	}

	/**
	 * The subset of {@link JavaVersion#values()} that should actually be offered to users. This excludes the
	 * deprecated {@code JAVA5}/{@code JAVA6}/{@code JAVA7} aliases, which only exist to keep the ordinal of
	 * later constants stable.
	 */
	private JavaVersion[] getSupportedJavaVersions() {
		JavaVersion[] allVersions = JavaVersion.values();
		int firstSupportedOrdinal = JavaVersion.JAVA8.ordinal();
		JavaVersion[] result = new JavaVersion[allVersions.length - firstSupportedOrdinal];
		System.arraycopy(allVersions, firstSupportedOrdinal, result, 0, result.length);
		return result;
	}

	private int indexOf(JavaVersion[] versions, JavaVersion version) {
		for (int i = 0; i < versions.length; i++) {
			if (versions[i] == version) {
				return i;
			}
		}
		return -1;
	}

	protected String javaValue(final String javaPreference) {
		IJavaProject javaProject = JavaCore.create(getProject());
		if (javaProject != null && javaProject.exists() && javaProject.getProject().isAccessible()) {
			return javaProject.getOption(javaPreference, true);
		} else {
			return JavaCore.getOption(javaPreference);
		}
	}

}
