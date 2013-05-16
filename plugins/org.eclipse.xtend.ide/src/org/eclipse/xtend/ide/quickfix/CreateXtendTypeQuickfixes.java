/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.quickfix;

import static org.eclipse.xtext.ui.util.DisplayRunHelper.*;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.ide.codebuilder.AbstractAnnotationBuilder;
import org.eclipse.xtend.ide.codebuilder.AbstractClassBuilder;
import org.eclipse.xtend.ide.codebuilder.AbstractInterfaceBuilder;
import org.eclipse.xtend.ide.codebuilder.CodeBuilderFactory;
import org.eclipse.xtend.ide.wizards.NewXtendAnnotationWizard;
import org.eclipse.xtend.ide.wizards.NewXtendAnnotationWizardPage;
import org.eclipse.xtend.ide.wizards.NewXtendClassWizard;
import org.eclipse.xtend.ide.wizards.NewXtendClassWizardPage;
import org.eclipse.xtend.ide.wizards.NewXtendInterfaceWizard;
import org.eclipse.xtend.ide.wizards.NewXtendInterfaceWizardPage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.edit.IModification;
import org.eclipse.xtext.ui.editor.model.edit.IModificationContext;
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.ui.quickfix.CreateJavaTypeQuickfixes;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Quickfixes to create new Xtend types.
 * 
 * @author Jan Koehnlein - Initial contribution and API
 */
@NonNullByDefault
public class CreateXtendTypeQuickfixes extends CreateJavaTypeQuickfixes {

	@Inject
	private Provider<NewXtendClassWizard> newXtendClassWizardProvider;
	
	@Inject
	private Provider<NewXtendInterfaceWizard> newXtendInterfaceWizardProvider;

	@Inject
	private Provider<NewXtendAnnotationWizard> newXtendAnnotationWizardProvider;

	@Inject
	private IXtendJvmAssociations associations;
	
	@Inject
	private CodeBuilderFactory codeBuilderFactory;
	
	@Inject
	private CodeBuilderQuickfix codeBuilderQuickfix;

	@Override
	public void addQuickfixes(Issue issue, IssueResolutionAcceptor issueResolutionAcceptor,
			IXtextDocument xtextDocument, XtextResource resource, 
			EObject referenceOwner, EReference unresolvedReference)
			throws Exception {
		String typeName = xtextDocument.get(issue.getOffset(), issue.getLength());
		if (unresolvedReference == XbasePackage.Literals.XCONSTRUCTOR_CALL__CONSTRUCTOR) {
			if(((XConstructorCall)referenceOwner).getConstructor().eIsProxy()) {
				newJavaClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newLocalXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			}
		} else if(unresolvedReference == XbasePackage.Literals.XTYPE_LITERAL__TYPE
				|| unresolvedReference == TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE) {
			EStructuralFeature eContainingFeature = referenceOwner.eContainingFeature();
			if(eContainingFeature == XtendPackage.Literals.XTEND_CLASS__EXTENDS) {
				newJavaClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newLocalXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			} else if(eContainingFeature == XtendPackage.Literals.XTEND_CLASS__IMPLEMENTS
					|| eContainingFeature == XtendPackage.Literals.XTEND_INTERFACE__EXTENDS) {
				newJavaInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newXtendInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newLocalXtendInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			} else {
				newJavaClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newJavaInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newXtendInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
				newLocalXtendClassQuickfix(typeName, resource, issue, issueResolutionAcceptor);				
				newLocalXtendInterfaceQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			}
		} else if(unresolvedReference == XAnnotationsPackage.Literals.XANNOTATION__ANNOTATION_TYPE) {
			newJavaAnnotationQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			newXtendAnnotationQuickfix(typeName, resource, issue, issueResolutionAcceptor);
			newLocalXtendAnnotationQuickfix(typeName, resource, issue, issueResolutionAcceptor);
		}
	}

	protected void newLocalXtendClassQuickfix(String typeName, XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		EObject eObject = resource.getEObject(issue.getUriToProblem().fragment());
		XtendTypeDeclaration xtendType = getAnnotationTarget(eObject);
		if(xtendType != null) {
			JvmDeclaredType inferredType = associations.getInferredType(xtendType);
			if(inferredType != null) {
				AbstractClassBuilder classBuilder = codeBuilderFactory.createClassBuilder(inferredType);
				classBuilder.setClassName(typeName);
				classBuilder.setVisibility(JvmVisibility.PUBLIC);
				classBuilder.setContext(xtendType);
				codeBuilderQuickfix.addQuickfix(classBuilder, "Create local Xtend class '" + typeName + "'", issue, issueResolutionAcceptor);
			}
		}
	}

	protected void newLocalXtendInterfaceQuickfix(String typeName, XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		EObject eObject = resource.getEObject(issue.getUriToProblem().fragment());
		XtendTypeDeclaration xtendType = getAnnotationTarget(eObject);
		if(xtendType != null) {
			JvmDeclaredType inferredType = associations.getInferredType(xtendType);
			if(inferredType != null) {
				AbstractInterfaceBuilder interfaceBuilder = codeBuilderFactory.createInterfaceBuilder(inferredType);
				interfaceBuilder.setInterfaceName(typeName);
				interfaceBuilder.setVisibility(JvmVisibility.PUBLIC);
				interfaceBuilder.setContext(xtendType);
				codeBuilderQuickfix.addQuickfix(interfaceBuilder, "Create local Xtend interface '" + typeName + "'", issue, issueResolutionAcceptor);
			}
		}
	}

	protected void newLocalXtendAnnotationQuickfix(String typeName, XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		EObject eObject = resource.getEObject(issue.getUriToProblem().fragment());
		XtendTypeDeclaration xtendType = getAnnotationTarget(eObject);
		if(xtendType != null) {
			JvmDeclaredType inferredType = associations.getInferredType(xtendType);
			if(inferredType != null) {
				AbstractAnnotationBuilder annotationBuilder = codeBuilderFactory.createAnnotationBuilder(inferredType);
				annotationBuilder.setAnnotationName(typeName);
				annotationBuilder.setVisibility(JvmVisibility.PUBLIC);
				annotationBuilder.setContext(xtendType);
				codeBuilderQuickfix.addQuickfix(annotationBuilder, "Create local Xtend annotation '@" + typeName + "'", issue, issueResolutionAcceptor);
			}
		}
	}

	@Nullable
	protected XtendTypeDeclaration getAnnotationTarget(EObject eObject) {
		XtendTypeDeclaration containerType = EcoreUtil2.getContainerOfType(eObject, XtendTypeDeclaration.class);
		if(containerType != null && containerType.eContainingFeature() == XtendPackage.Literals.XTEND_MEMBER__ANNOTATION_INFO)
			return getAnnotationTarget(containerType.eContainer());
		else 
			return containerType;
	}

	protected void newXtendClassQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Xtend class '" + typeName + "'",
				"Opens the new Xtend class wizard to create the type '" + typeName + "'", "xtend_file.png",
				new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewElementWizard newXtendClassWizard = newXtendClassWizardProvider.get();
								WizardDialog dialog = createWizardDialog(newXtendClassWizard);
								NewXtendClassWizardPage page = (NewXtendClassWizardPage) newXtendClassWizard.getStartingPage();
								configureWizardPage(page, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

	protected void newXtendInterfaceQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Xtend interface '" + typeName + "'",
				"Opens the new Xtend interface wizard to create the type '" + typeName + "'", "xtend_file.png",
				new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewElementWizard newXtendInterfaceWizard = newXtendInterfaceWizardProvider.get();
								WizardDialog dialog = createWizardDialog(newXtendInterfaceWizard);
								NewXtendInterfaceWizardPage page = (NewXtendInterfaceWizardPage) newXtendInterfaceWizard.getStartingPage();
								configureWizardPage(page, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

	protected void newXtendAnnotationQuickfix(final String typeName, final XtextResource resource, Issue issue,
			IssueResolutionAcceptor issueResolutionAcceptor) {
		issueResolutionAcceptor.accept(issue, "Create Xtend annotation '" + typeName + "'",
				"Opens the new Xtend annotation wizard to create the type '" + typeName + "'", "xtend_file.png",
				new IModification() {
					public void apply(@Nullable IModificationContext context) throws Exception {
						runAsyncInDisplayThread(new Runnable() {
							public void run() {
								NewElementWizard newXtendAnnotationWizard = newXtendAnnotationWizardProvider.get();
								WizardDialog dialog = createWizardDialog(newXtendAnnotationWizard);
								NewXtendAnnotationWizardPage page = (NewXtendAnnotationWizardPage) newXtendAnnotationWizard.getStartingPage();
								configureWizardPage(page, resource.getURI(), typeName);
								dialog.open();
							}
						});
					}
				});
	}

}
