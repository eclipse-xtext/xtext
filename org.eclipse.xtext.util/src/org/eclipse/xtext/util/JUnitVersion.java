/*******************************************************************************
 * Copyright (c) 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util;

/**
 * @author Karsten Thoms (karsten.thoms@itemis.de) - Initial contribution and API
 * @since 2.14
 */
public enum JUnitVersion {
	JUNIT_4 (4), JUNIT_6 (6);
	
	public static JUnitVersion DEFAULT = JUNIT_4;
	
	private int version;
	
	private JUnitVersion (int version) {
		this.version = version;
	}
	
	public int getVersion () {
		return version;
	}
	
	public static JUnitVersion fromString (String version) {
		switch (version) {
		case "4": return JUNIT_4;
		case "6": return JUNIT_6;
		}
		throw new IllegalArgumentException(version);
	}
}
