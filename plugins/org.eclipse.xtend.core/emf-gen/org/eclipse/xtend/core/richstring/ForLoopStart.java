/**
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.richstring;

import org.eclipse.xtend.core.xtend.RichStringForLoop;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>For Loop Start</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.xtend.core.richstring.ForLoopStart#getLoop <em>Loop</em>}</li>
 *   <li>{@link org.eclipse.xtend.core.richstring.ForLoopStart#getEnd <em>End</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.xtend.core.richstring.ProcessedRichStringPackage#getForLoopStart()
 * @model
 * @generated
 */
public interface ForLoopStart extends LinePart
{
	/**
	 * Returns the value of the '<em><b>Loop</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Loop</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Loop</em>' reference.
	 * @see #setLoop(RichStringForLoop)
	 * @see org.eclipse.xtend.core.richstring.ProcessedRichStringPackage#getForLoopStart_Loop()
	 * @model
	 * @generated
	 */
	RichStringForLoop getLoop();

	/**
	 * Sets the value of the '{@link org.eclipse.xtend.core.richstring.ForLoopStart#getLoop <em>Loop</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Loop</em>' reference.
	 * @see #getLoop()
	 * @generated
	 */
	void setLoop(RichStringForLoop value);

	/**
	 * Returns the value of the '<em><b>End</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.xtend.core.richstring.ForLoopEnd#getStart <em>Start</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>End</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End</em>' reference.
	 * @see #setEnd(ForLoopEnd)
	 * @see org.eclipse.xtend.core.richstring.ProcessedRichStringPackage#getForLoopStart_End()
	 * @see org.eclipse.xtend.core.richstring.ForLoopEnd#getStart
	 * @model opposite="start"
	 * @generated
	 */
	ForLoopEnd getEnd();

	/**
	 * Sets the value of the '{@link org.eclipse.xtend.core.richstring.ForLoopStart#getEnd <em>End</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End</em>' reference.
	 * @see #getEnd()
	 * @generated
	 */
	void setEnd(ForLoopEnd value);

} // ForLoopStart
