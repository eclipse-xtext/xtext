/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard;

public enum SourceLayout {
	PLAIN("Plain") {
		@Override
		public String getPathFor(Outlet outlet) {
			switch (outlet) {
			case ROOT:
				return "";
			case MAIN_JAVA:
				return "src";
			case MAIN_RESOURCES:
				return "src";
			case TEST_RESOURCES:
				return "src";
			case TEST_JAVA:
				return "src";
			case META_INF:
				return "META-INF";
			case MAIN_SRC_GEN:
				return "src-gen";
			case MAIN_XTEND_GEN:
				return "xtend-gen";
			case TEST_SRC_GEN:
				return "src-gen";
			case TEST_XTEND_GEN:
				return "xtend-gen";
			default:
				throw new IllegalArgumentException("Unknown Outlet " + outlet);
			}
		}
	},

	MAVEN("Maven/Gradle") {
		@Override
		public String getPathFor(Outlet outlet) {
			switch (outlet) {
			case ROOT:
				return "";
			case MAIN_JAVA:
				return "src/main/java";
			case MAIN_RESOURCES:
				return "src/main/resources";
			case TEST_JAVA:
				return "src/test/java";
			case TEST_RESOURCES:
				return "src/test/resources";
			case META_INF:
				return "src/main/resources/META-INF";
			case MAIN_SRC_GEN:
				return "src/main/xtext-gen";
			case MAIN_XTEND_GEN:
				return "src/main/xtend-gen";
			case TEST_SRC_GEN:
				return "src/test/xtext-gen";
			case TEST_XTEND_GEN:
				return "src/test/xtend-gen";
			default:
				throw new IllegalArgumentException("Unknown Outlet " + outlet);
			}
		}
	};

	private String humanReadableName;

	public abstract String getPathFor(Outlet outlet);

	private SourceLayout(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}

	public String toString() {
		return humanReadableName;
	}
}
