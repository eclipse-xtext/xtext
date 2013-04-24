/**
 */
package org.eclipse.xtext.ui.tests.editor.contentassist.unorderedGroupsTest;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.xtext.ui.tests.editor.contentassist.unorderedGroupsTest.UnorderedGroupsTestPackage
 * @generated
 */
public interface UnorderedGroupsTestFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  UnorderedGroupsTestFactory eINSTANCE = org.eclipse.xtext.ui.tests.editor.contentassist.unorderedGroupsTest.impl.UnorderedGroupsTestFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Model</em>'.
   * @generated
   */
  Model createModel();

  /**
   * Returns a new object of class '<em>Simple Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Simple Model</em>'.
   * @generated
   */
  SimpleModel createSimpleModel();

  /**
   * Returns a new object of class '<em>Mandatory Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Mandatory Model</em>'.
   * @generated
   */
  MandatoryModel createMandatoryModel();

  /**
   * Returns a new object of class '<em>Looped Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Looped Model</em>'.
   * @generated
   */
  LoopedModel createLoopedModel();

  /**
   * Returns a new object of class '<em>Group Looped Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Group Looped Model</em>'.
   * @generated
   */
  GroupLoopedModel createGroupLoopedModel();

  /**
   * Returns a new object of class '<em>Looped Alternative Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Looped Alternative Model</em>'.
   * @generated
   */
  LoopedAlternativeModel createLoopedAlternativeModel();

  /**
   * Returns a new object of class '<em>Bug304681 Model</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Bug304681 Model</em>'.
   * @generated
   */
  Bug304681Model createBug304681Model();

  /**
   * Returns a new object of class '<em>Bug304681 Feature</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Bug304681 Feature</em>'.
   * @generated
   */
  Bug304681Feature createBug304681Feature();

  /**
   * Returns a new object of class '<em>Bug304681 Attribute</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Bug304681 Attribute</em>'.
   * @generated
   */
  Bug304681Attribute createBug304681Attribute();

  /**
   * Returns a new object of class '<em>Bug304681 Reference</em>'.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return a new object of class '<em>Bug304681 Reference</em>'.
   * @generated
   */
  Bug304681Reference createBug304681Reference();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  UnorderedGroupsTestPackage getUnorderedGroupsTestPackage();

} //UnorderedGroupsTestFactory
