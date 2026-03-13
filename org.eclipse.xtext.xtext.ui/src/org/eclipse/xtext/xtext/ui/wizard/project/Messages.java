/*******************************************************************************
 * Copyright (c) 2009, 2026 itemis AG (http://www.itemis.eu) and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.ui.wizard.project;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.xtext.xtext.ui.wizard.project.messages"; //$NON-NLS-1$
	public static String XtextProjectCreator_CreatingProjectsMessage2;
	public static String NewXtextProjectWizard_WindowTitle;
	public static String WizardNewXtextProjectCreationPage_TestingSupport;
	public static String WizardNewXtextProjectCreationPage_Description;
	public static String WizardNewXtextProjectCreationPage_EEButton;
	public static String WizardNewXtextProjectCreationPage_EECombo;
	public static String WizardNewXtextProjectCreationPage_EEGrTitle;
	public static String WizardNewXtextProjectCreationPage_eeInfo_0;
	public static String WizardNewXtextProjectCreationPage_eeInfo_1;
	public static String WizardNewXtextProjectCreationPage_ErrorMessageExtensions;
	public static String WizardNewXtextProjectCreationPage_MessageAtLeastJava21;
	public static String WizardNewXtextProjectCreationPage_ErrorMessageLanguageName;
	public static String WizardNewXtextProjectCreationPage_ErrorMessageLanguageNameWithoutPackage;
	public static String WizardNewXtextProjectCreationPage_ErrorMessageProjectName;
	public static String WizardNewXtextProjectCreationPage_LabelExtensions;
	public static String WizardNewXtextProjectCreationPage_LabelLanguage;
	public static String WizardNewXtextProjectCreationPage_LabelName;
	public static String WizardNewXtextProjectCreationPage_WindowTitle;
	public static String AdvancedNewProjectPage_WindowTitle;
	public static String AdvancedNewProjectPage_Description;
	public static String AdvancedNewProjectPage_LabelFacets;
	public static String AdvancedNewProjectPage_eclipseAndGradleWarn;
	
	public static String AdvancedNewProjectPage_noBuildship;
	public static String AdvancedNewProjectPage_noM2e;
	public static String AdvancedNewProjectPage_prefBuildSys;
	public static String AdvancedNewProjectPage_projEclipse;
	public static String AdvancedNewProjectPage_projEclipseSDKFeature;
	public static String AdvancedNewProjectPage_projEclipseP2;
	public static String AdvancedNewProjectPage_projIde;
	public static String AdvancedNewProjectPage_projIde_description;
	public static String AdvancedNewProjectPage_languageServer;
	public static String AdvancedNewProjectPage_languageServer_description;
	public static String AdvancedNewProjectPage_projWeb;
	public static String AdvancedNewProjectPage_srcLayout;
	public static String AdvancedNewProjectPage_p2AndSdkInfo;
	public static String AdvancedNewProjectPage_junitVersion;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
