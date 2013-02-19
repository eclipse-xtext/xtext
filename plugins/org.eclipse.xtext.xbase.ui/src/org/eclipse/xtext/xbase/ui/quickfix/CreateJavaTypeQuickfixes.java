/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.quickfix;

import static org.eclipse.xtext.ui.util.DisplayRunHelper.*;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.NewAnnotationCreationWizard;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewInterfaceCreationWizard;
import org.eclipse.jdt.ui.wizards.NewAnnotationWizardPage;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jdt.ui.wizards.NewInterfaceWizardPage;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.ui.refactoring.impl.ProjectUtil;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;

import com.google.inject.Inject;

/**
 * Quickfixes to create new Java types.
 *
 * @author Jan Koehnlein - Initial contribution and API
 */
@NonNullByDefault
public class CreateJavaTypeQuickfixes implements ILinkingIssueQuickfixProvider {

	private static final Logger LOG = Logger.getLogger(CreateJavaTypeQuickfixes.class);
	
	@Inject 
	private ProjectUtil projectUtil;

	public void addQuickfixes(Issue issue, IssueResolutionAcceptor issueResolutionAcceptor,
			IXtextDocument xtextDocument, XtextResource resource, 
			EObject referenceOwner, EReference unresolvedReference)
			throws Exception {
		String typeName = xtextDocument.get(issue.getOffset(), issue.getLength());
		if (unresolvedReference == XbasePackage.Literals.XCONSTRUCTOR_CALL__CONSTRUCTOR) {
			if(((XConstructorCall)referenceOwner).getConstructor().eIsProxy())
				newJavaClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
		} else if(unresolvedReference == XbasePackage.Literals.XTYPE_LITERAL__TYPE
				|| unresolvedReference == TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE) {
			newJavaClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			newJavaInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
		} else if(unresolvedReference == XAnnotationsPackage.Literals.XANNOTATION__ANNOTATION_TYPE) {
			newJavaAnnotationQuickfix(typeName, resource, issue, issueResolutionAcceptor);
		}
	}

	protected void newJavaInterfaceQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Java interface '" + typeName + "'",
				"Opens the new Java interface wizard to create the type '" + typeName + "'",
				"java_interface.gif", new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewInterfaceWizardPage classWizardPage = new NewInterfaceWizardPage();
								NewInterfaceCreationWizard wizard = new NewInterfaceCreationWizard(classWizardPage, true);
								WizardDialog dialog = createWizardDialog(wizard);
								configureWizardPage(classWizardPage, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

	protected void newJavaClassQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Java class '" + typeName + "'",
				"Opens the new Java class wizard to create the type '" + typeName + "'", "java_file.gif",
				new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewClassWizardPage classWizardPage = new NewClassWizardPage();
								NewClassCreationWizard wizard = new NewClassCreationWizard(classWizardPage, true);
								WizardDialog dialog = createWizardDialog(wizard);
								configureWizardPage(classWizardPage, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

	protected void newJavaAnnotationQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Java annotation '@" + typeName + "'",
				"Opens the new Java annotation wizard to create the type '@" + typeName + "'", "java_file.gif",
				new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewAnnotationWizardPage annotationWizardPage = new NewAnnotationWizardPage();
								NewAnnotationCreationWizard wizard = new NewAnnotationCreationWizard(annotationWizardPage, true);
								WizardDialog dialog = createWizardDialog(wizard);
								configureWizardPage(annotationWizardPage, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

	protected WizardDialog createWizardDialog(NewElementWizard newXtendClassWizard) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		newXtendClassWizard.init(workbench, new StructuredSelection());
		WizardDialog dialog = new WizardDialog(shell, newXtendClassWizard);
		dialog.create();
		return dialog;
	}

	
	protected void configureWizardPage(NewTypeWizardPage page, URI contextUri, String typeName) {
		setPackageName(page, contextUri);
		page.setTypeName(typeName, true);
	}

	protected void setPackageName(NewTypeWizardPage page, URI contextUri) {
		IJavaProject javaProject = getJavaProject(contextUri);
		String path = contextUri.trimSegments(1).toPlatformString(true);
		try {
			if(javaProject != null) {
				IPackageFragment packageFragment = javaProject.findPackageFragment(new Path(path));
				IPackageFragmentRoot root = (IPackageFragmentRoot) packageFragment
						.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				page.setPackageFragment(packageFragment, true);
				page.setPackageFragmentRoot(root, true);
			}
		} catch (JavaModelException e) {
			LOG.error("Could not find package for " + path, e);
		}
	}

	@Nullable
	protected IJavaProject getJavaProject(URI uri){
		IProject project = projectUtil.getProject(uri);
		if(project == null){
			return null;
		}
		return JavaCore.create(project);
	}

}
