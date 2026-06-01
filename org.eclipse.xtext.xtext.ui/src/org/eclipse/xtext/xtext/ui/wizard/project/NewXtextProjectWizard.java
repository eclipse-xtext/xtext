/*******************************************************************************
 * Copyright (c) 2009, 2026 Dakshinamurthy Karra, itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dakshinamurthy Karra (Jalian Systems)
 *******************************************************************************/
package org.eclipse.xtext.xtext.ui.wizard.project;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.xtext.ui.wizard.IProjectCreator;
import org.eclipse.xtext.ui.wizard.IProjectInfo;
import org.eclipse.xtext.ui.wizard.XtextNewProjectWizard;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xtext.ui.Activator;
import org.eclipse.xtext.xtext.wizard.BuildSystem;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor.FileExtensions;
import org.eclipse.xtext.xtext.wizard.ProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.ProjectLayout;
import org.eclipse.xtext.xtext.wizard.TestedProjectDescriptor;

import com.google.inject.Inject;

/**
 * A project wizard to create Xtext projects.
 */
public class NewXtextProjectWizard extends XtextNewProjectWizard {

	private WizardNewXtextProjectCreationPage mainPage;
	private AdvancedNewProjectPage advancedPage;

	/**
	 * Constructs a new wizard
	 */
	@Inject
	public NewXtextProjectWizard(IProjectCreator projectCreator) {
		super(projectCreator);
		setWindowTitle(Messages.NewXtextProjectWizard_WindowTitle);
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("icons/wizban/newxprj_wiz.gif")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewXtextProjectCreationPage("mainPage", this.selection); //$NON-NLS-1$
		advancedPage = new AdvancedNewProjectPage("advancedPage");
		addPage(mainPage);
		addPage(advancedPage);
	}

	@Override
	protected IProjectInfo getProjectInfo() {
		XtextProjectInfo projectInfo = createProjectInfo();
		LanguageDescriptor language = projectInfo.getLanguage();
		language.setFileExtensions(FileExtensions.fromString(mainPage.getFileExtensions()));
		language.setName(mainPage.getLanguageName());
		projectInfo.setBaseName(mainPage.getProjectName());
		projectInfo.setWorkingSets(Arrays.asList(mainPage.getSelectedWorkingSets()));
		projectInfo.setRootLocation(mainPage.getLocationPath().toString());
		Charset encoding = null;
		try {
			encoding = Charset.forName(ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset());
		}
		catch (final CoreException e) {
			encoding = Charset.defaultCharset();
		}
		projectInfo.setEncoding(encoding);
		String lineDelimiter = InstanceScope.INSTANCE.getNode("org.eclipse.core.runtime").get("line.separator", Strings.newLine());
		projectInfo.setLineDelimiter(lineDelimiter);
		projectInfo.setWorkbench(getWorkbench());
		JavaVersion selectedBree = mainPage.getJavaVersion();
		// Use old default for wizard as fall back, when something goes wrong
		if (selectedBree != null) {
			projectInfo.setJavaVersion(selectedBree);
		}

		BuildSystem buildSystem = advancedPage.getPreferredBuildSystem();
		projectInfo.setPreferredBuildSystem(buildSystem);
		projectInfo.setSourceLayout(advancedPage.getSourceLayout());
		
		projectInfo.getUiProject().setEnabled(advancedPage.isCreateUiProject());
		if (buildSystem != BuildSystem.NONE) {
			projectInfo.setProjectLayout(ProjectLayout.HIERARCHICAL);
		}
		projectInfo.getIdeProject().setEnabled(advancedPage.isCreateIdeProject());
		projectInfo.getSdkProject().setEnabled(advancedPage.isCreateSdkProject());
		projectInfo.getP2Project().setEnabled(advancedPage.isCreateP2Project());
		projectInfo.setLanguageServer(advancedPage.getLanguageServer());
		projectInfo.setJunitVersion(advancedPage.getSelectedJUnitVersion());
		
		if (advancedPage.isCreateTestProject()) {
			for (ProjectDescriptor project : projectInfo.getEnabledProjects()) {
				if (project instanceof TestedProjectDescriptor) {
					((TestedProjectDescriptor) project).getTestProject().setEnabled(true);
				}
			}
		}
		return projectInfo;
	}

	protected XtextProjectInfo createProjectInfo() {
		return new XtextProjectInfo();
	}
	
}