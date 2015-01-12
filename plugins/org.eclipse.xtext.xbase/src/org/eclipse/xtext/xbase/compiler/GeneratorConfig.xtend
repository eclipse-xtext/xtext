/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.compiler

import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author Holger Schill - Initial contribution and API
 * @author Miro Spoenemann
 */
@Accessors
class GeneratorConfig {

	boolean generateExpressions = true

	boolean generateSyntheticSuppressWarnings = true

	JavaVersion targetVersion = JavaVersion.JAVA5
	
	/**
	 * Copy the values of the given generator configuration.
	 */
	def copy(GeneratorConfig other) {
		this.generateExpressions = other.generateExpressions
		this.generateSyntheticSuppressWarnings = other.generateSyntheticSuppressWarnings
		this.targetVersion = other.targetVersion
		return this
	}

}