/**
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.xtend;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.xtext.xtype.XImportSection;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>File</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.xtend.core.xtend.XtendFile#getImportSection <em>Import Section</em>}</li>
 *   <li>{@link org.eclipse.xtend.core.xtend.XtendFile#getXtendTypes <em>Xtend Types</em>}</li>
 *   <li>{@link org.eclipse.xtend.core.xtend.XtendFile#getPackage <em>Package</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.xtend.core.xtend.XtendPackage#getXtendFile()
 * @model
 * @generated
 */
public interface XtendFile extends EObject
{
	/**
	 * Returns the value of the '<em><b>Import Section</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Import Section</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Import Section</em>' containment reference.
	 * @see #setImportSection(XImportSection)
	 * @see org.eclipse.xtend.core.xtend.XtendPackage#getXtendFile_ImportSection()
	 * @model containment="true"
	 * @generated
	 */
	XImportSection getImportSection();

	/**
	 * Sets the value of the '{@link org.eclipse.xtend.core.xtend.XtendFile#getImportSection <em>Import Section</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Import Section</em>' containment reference.
	 * @see #getImportSection()
	 * @generated
	 */
	void setImportSection(XImportSection value);

	/**
	 * Returns the value of the '<em><b>Xtend Types</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.xtend.core.xtend.XtendTypeDeclaration}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Xtend Types</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Xtend Types</em>' containment reference list.
	 * @see org.eclipse.xtend.core.xtend.XtendPackage#getXtendFile_XtendTypes()
	 * @model containment="true"
	 * @generated
	 */
	EList<XtendTypeDeclaration> getXtendTypes();

	/**
	 * Returns the value of the '<em><b>Package</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Package</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Package</em>' attribute.
	 * @see #setPackage(String)
	 * @see org.eclipse.xtend.core.xtend.XtendPackage#getXtendFile_Package()
	 * @model
	 * @generated
	 */
	String getPackage();

	/**
	 * Sets the value of the '{@link org.eclipse.xtend.core.xtend.XtendFile#getPackage <em>Package</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Package</em>' attribute.
	 * @see #getPackage()
	 * @generated
	 */
	void setPackage(String value);

} // XtendFile
