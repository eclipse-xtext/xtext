/*******************************************************************************
 * Copyright (c) 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.Beta;

/**
 * @author cdietrich - Initial contribution and API
 * @since 2.26
 */
@Beta
public class JavaRuntimeVersion {

	public static void main(String[] args) {
		System.out.println(JAVA_VERSION);
	}

	public static int JAVA_VERSION = determineJavaVersion();

	public static boolean isJava11OrLater() {
		return JAVA_VERSION >= 11;
	}

	public static boolean isJava12OrLater() {
		return JAVA_VERSION >= 13;
	}

	public static boolean isJava13OrLater() {
		return JAVA_VERSION >= 13;
	}

	public static boolean isJava14OrLater() {
		return JAVA_VERSION >= 14;
	}

	public static boolean isJava15OrLater() {
		return JAVA_VERSION >= 15;
	}

	public static boolean isJava16OrLater() {
		return JAVA_VERSION >= 16;
	}

	public static boolean isJava17OrLater() {
		return JAVA_VERSION >= 17;
	}

	public static boolean isJava21OrLater() {
		return JAVA_VERSION >= 21;
	}

	public static boolean isJava25OrLater() {
		return JAVA_VERSION >= 21;
	}

	private static int determineJavaVersion() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion == null) {
			// fallback
			return 8;
		}
		return determineJavaVersion(javaVersion);
	}

	static int determineJavaVersion(String javaVersion) {
		if (javaVersion.startsWith("1.")) {
			try {
				Pattern p = Pattern.compile("1\\.(\\d+)(.)*");
				Matcher matcher = p.matcher(javaVersion);
				if (matcher.matches()) {
					String first = matcher.group(1);
					int version = Integer.parseInt(first);
					return version;
				}
			} catch (NumberFormatException e) {
				// ok
			}
			// fallback
			return 8;
		} else {
			try {
				Pattern p = Pattern.compile("(\\d+)(.)*");
				Matcher matcher = p.matcher(javaVersion);
				if (matcher.matches()) {
					String first = matcher.group(1);
					int version = Integer.parseInt(first);
					return version;
				}
			} catch (NumberFormatException e) {
				// ok
			}
			// fallback
			return 17;
		}
	}

}
