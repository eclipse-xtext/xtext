/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import com.google.inject.Inject;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xtext.generator.model.TypeReference;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;

/**
 * Configuration of the names of the generated classes. Create a subclass and
 * register it with a specialized {@link DefaultGeneratorModule} in order to
 * customize some class or package names.
 */
public class XtextGeneratorNaming {
	@Inject
	private IXtextProjectConfig projectConfig;

	public String getRuntimeBasePackage(Grammar grammar) {
		return GrammarUtil.getNamespace(grammar);
	}

	public String getRuntimeTestBasePackage(Grammar grammar) {
		return getRuntimeBasePackage(grammar) + ".tests";
	}

	public TypeReference getRuntimeModule(Grammar grammar) {
		return new TypeReference(getRuntimeBasePackage(grammar), GrammarUtil.getSimpleName(grammar) + "RuntimeModule");
	}

	public TypeReference getRuntimeGenModule(Grammar grammar) {
		return new TypeReference(getRuntimeBasePackage(grammar),
				"Abstract" + GrammarUtil.getSimpleName(grammar) + "RuntimeModule");
	}

	public TypeReference getRuntimeDefaultModule(Grammar grammar) {
		return new TypeReference("org.eclipse.xtext.service.DefaultRuntimeModule");
	}

	public TypeReference getRuntimeSetup(Grammar grammar) {
		return new TypeReference(getRuntimeBasePackage(grammar),
				GrammarUtil.getSimpleName(grammar) + "StandaloneSetup");
	}

	public TypeReference getRuntimeGenSetup(Grammar grammar) {
		return new TypeReference(getRuntimeBasePackage(grammar),
				GrammarUtil.getSimpleName(grammar) + "StandaloneSetupGenerated");
	}

	public String getGenericIdeBasePackage(Grammar grammar) {
		return getRuntimeBasePackage(grammar) + ".ide";
	}

	public String getGenericIdeTestBasePackage(Grammar grammar) {
		return getGenericIdeBasePackage(grammar) + ".tests";
	}

	public TypeReference getGenericIdeModule(Grammar grammar) {
		return new TypeReference(getGenericIdeBasePackage(grammar), GrammarUtil.getSimpleName(grammar) + "IdeModule");
	}

	public TypeReference getGenericIdeGenModule(Grammar grammar) {
		return new TypeReference(getGenericIdeBasePackage(grammar),
				"Abstract" + GrammarUtil.getSimpleName(grammar) + "IdeModule");
	}

	public TypeReference getGenericIdeDefaultModule(Grammar grammar) {
		return new TypeReference("org.eclipse.xtext.ide.DefaultIdeModule");
	}

	public TypeReference getGenericIdeSetup(Grammar grammar) {
		return new TypeReference(getGenericIdeBasePackage(grammar), GrammarUtil.getSimpleName(grammar) + "IdeSetup");
	}

	public String getEclipsePluginBasePackage(Grammar grammar) {
		return GrammarUtil.getNamespace(grammar) + ".ui";
	}

	public String getEclipsePluginTestBasePackage(Grammar grammar) {
		return getEclipsePluginBasePackage(grammar) + ".tests";
	}

	public TypeReference getEclipsePluginModule(Grammar grammar) {
		return new TypeReference(getEclipsePluginBasePackage(grammar), GrammarUtil.getSimpleName(grammar) + "UiModule");
	}

	public TypeReference getEclipsePluginGenModule(Grammar grammar) {
		return new TypeReference(getEclipsePluginBasePackage(grammar),
				"Abstract" + GrammarUtil.getSimpleName(grammar) + "UiModule");
	}

	public TypeReference getEclipsePluginDefaultModule(Grammar grammar) {
		return new TypeReference("org.eclipse.xtext.ui.DefaultUiModule");
	}

	public TypeReference getEclipsePluginExecutableExtensionFactory(Grammar grammar) {
		return new TypeReference(getEclipsePluginBasePackage(grammar),
				GrammarUtil.getSimpleName(grammar) + "ExecutableExtensionFactory");
	}

	public TypeReference getEclipsePluginEditor(Grammar grammar) {
		return new TypeReference(getEclipsePluginBasePackage(grammar) + ".editor",
				GrammarUtil.getSimpleName(grammar) + "Editor");
	}

	public TypeReference getEclipsePluginXbaseEditor(Grammar grammar) {
		return new TypeReference("org.eclipse.xtext.xbase.ui.editor.XbaseEditor");
	}

	public TypeReference getEclipsePluginDefaultEditor(Grammar grammar) {
		return new TypeReference("org.eclipse.xtext.ui.editor.XtextEditor");
	}

	/**
	 * @return name for the eclipsePlugin Activator or <code>null</code> if
	 * eclipsePlugin has no name
	 */
	public TypeReference getEclipsePluginActivator() {
		String pluginName = projectConfig.getEclipsePlugin().getName();
		if (pluginName == null) {
			return null;
		}
		String activatorName = pluginName.replaceAll("\\.ui$", "");
		activatorName = StringExtensions.toFirstUpper(activatorName.substring(activatorName.lastIndexOf('.') + 1))
				+ "Activator";
		return new TypeReference(pluginName + ".internal", activatorName);
	}

}
