/*******************************************************************************
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.xtext.builder.preferences.BuilderPreferenceAccess;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;

import com.google.inject.Inject;

/**
 * @author Miro Spoenemann - Initial contribution and API
 * @author Lorenzo Bettini - useXbaseGenerated
 */
public class XbaseBuilderPreferenceAccess {

	/**
	 * Preference identifier for generating <code>@SuppressWarnings</code>.
	 */
	public static final String PREF_GENERATE_SUPPRESS_WARNINGS = "generateSuppressWarnings"; //$NON-NLS-1$

	/**
	 * Preference identifier for generating <code>@XbaseGenerated</code>.
	 * @since 2.36
	 */
	public static final String USE_XBASE_GENERATED = "useXbaseGenerated"; //$NON-NLS-1$

	/**
	 * Preference identifier for generating <code>@Generated</code>.
	 */
	public static final String PREF_GENERATE_GENERATED = "generateGeneratedAnnotation"; //$NON-NLS-1$

	/**
	 * Preference identifier for including the current date in <code>@Generated</code> annotations.
	 */
	public static final String PREF_DATE_IN_GENERATED = "includeDateInGenerated"; //$NON-NLS-1$

	/**
	 * Preference identifier for the comment to add to <code>@Generated</code> annotations.
	 */
	public static final String PREF_GENERATED_COMMENT = "generatedAnnotationComment"; //$NON-NLS-1$
	
	/**
	 * Preference identifier for the Java language version of generated code.
	 */
	public static final String PREF_JAVA_VERSION = "targetJavaVersion"; //$NON-NLS-1$
	
	/**
	 * Preference identifier for using the version from the Java compiler source compatibility level.
	 */
	public static final String PREF_USE_COMPILER_SOURCE = "useJavaCompilerCompliance"; //$NON-NLS-1$
	
	public static class Initializer extends BuilderPreferenceAccess.Initializer {

		@Override
		protected void initializeBuilderPreferences(IPreferenceStore store) {
			super.initializeBuilderPreferences(store);
			store.setDefault(PREF_GENERATE_SUPPRESS_WARNINGS, true);
			store.setDefault(USE_XBASE_GENERATED, true);
			store.setDefault(PREF_GENERATE_GENERATED, false);
			store.setDefault(PREF_DATE_IN_GENERATED, false);
			store.setDefault(PREF_JAVA_VERSION, JavaVersion.DEFAULT.toString());
			store.setDefault(PREF_USE_COMPILER_SOURCE, true);
		}

	}

	@Inject	
	private IPreferenceStoreAccess preferenceStoreAccess;
	
	public void loadBuilderPreferences(GeneratorConfig generatorConfig, Object context) {
		IPreferenceStore preferenceStore = preferenceStoreAccess.getContextPreferenceStore(context);
		generatorConfig.setGenerateSyntheticSuppressWarnings(preferenceStore.getBoolean(PREF_GENERATE_SUPPRESS_WARNINGS));
		generatorConfig.setUseXbaseGenerated(preferenceStore.getBoolean(USE_XBASE_GENERATED));
		generatorConfig.setGenerateGeneratedAnnotation(preferenceStore.getBoolean(PREF_GENERATE_GENERATED));
		if (generatorConfig.isGenerateGeneratedAnnotation()) {
			generatorConfig.setIncludeDateInGeneratedAnnotation(preferenceStore.getBoolean(PREF_DATE_IN_GENERATED));
			generatorConfig.setGeneratedAnnotationComment(preferenceStore.getString(PREF_GENERATED_COMMENT));
		}
		generatorConfig.setJavaSourceVersion(getJavaVersion(context, preferenceStore));
	}

	public JavaVersion getJavaVersion(Object context, IPreferenceStore preferenceStore) {
		boolean useCompilerSource = preferenceStore.getBoolean(PREF_USE_COMPILER_SOURCE);
		if (useCompilerSource) {
			IJavaProject javaProject = JavaCore.create(context instanceof IProject ? (IProject) context : null);
			String compilerSource;
			if (javaProject != null && javaProject.exists() && javaProject.getProject().isAccessible()) {
				compilerSource = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
			} else {
				compilerSource = JavaCore.getOption(JavaCore.COMPILER_SOURCE);
			}
			return fromCompilerSourceLevel(compilerSource);
		} else {
			String stringValue = preferenceStore.getString(PREF_JAVA_VERSION);
			try {
				return JavaVersion.valueOf(stringValue);
			} catch (IllegalArgumentException exception) {
				// Fall back to default value
			}
		}
		return JavaVersion.DEFAULT;
	}
	
	public void setJavaVersion(Object context, JavaVersion version) {
		IPreferenceStore preferenceStore = preferenceStoreAccess.getWritablePreferenceStore(context);
		preferenceStore.setValue(PREF_USE_COMPILER_SOURCE, false);
		preferenceStore.setValue(PREF_JAVA_VERSION, version.toString());
	}
	
	public JavaVersion fromCompilerSourceLevel(String compilerSource) {
		JavaVersion javaVersion = JavaVersion.fromQualifier(compilerSource);
		if (javaVersion == null)
			javaVersion = JavaVersion.DEFAULT;
		return javaVersion;
	}

}
