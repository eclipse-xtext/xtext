/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.model.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.xtext.xtext.generator.CodeConfig;
import org.eclipse.xtext.xtext.generator.Issues;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.PluginXmlAccess;
import org.eclipse.xtext.xtext.generator.model.XtextGeneratorFileSystemAccess;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Configuration of the subprojects for one or more Xtext languages. If the
 * standard project names and layout is sufficient, use
 * {@link StandardProjectConfig}.
 * 
 * @noextend This class should not be extended by clients.
 */
public class XtextProjectConfig implements IXtextProjectConfig {
	private RuntimeProjectConfig runtime = new RuntimeProjectConfig();

	private BundleProjectConfig runtimeTest = new BundleProjectConfig();

	private BundleProjectConfig genericIde = new BundleProjectConfig();

	private BundleProjectConfig eclipsePlugin = new BundleProjectConfig();

	private BundleProjectConfig eclipsePluginTest = new BundleProjectConfig();

	@Inject
	private CodeConfig codeConfig;

	public void checkConfiguration(Issues issues) {
		for (SubProjectConfig it : getEnabledProjects()) {
			it.checkConfiguration(issues);
		}
	}

	public List<? extends SubProjectConfig> getAllProjects() {
		return Lists.newArrayList(runtime, runtimeTest, genericIde, eclipsePlugin, eclipsePluginTest);
	}

	@Override
	public List<? extends SubProjectConfig> getTestProjects() {
		return Lists.newArrayList(runtimeTest, eclipsePluginTest);
	}

	@Override
	public List<? extends SubProjectConfig> getEnabledProjects() {
		List<SubProjectConfig> enabledProjects = new ArrayList<>();
		Iterables.addAll(enabledProjects, Iterables.filter(getAllProjects(), SubProjectConfig::isEnabled));
		return enabledProjects;
	}

	@Override
	public void initialize(Injector injector) {
		setDefaults();
		injector.injectMembers(this);
		for (SubProjectConfig it : getEnabledProjects()) {
			it.initialize(injector);
		}
	}

	public void setDefaults() {
		runtime.setEnabled(true);
		if (eclipsePlugin.isEnabled()) {
			genericIde.setEnabled(true);
		}
	}

	protected ManifestAccess newManifestAccess() {
		ManifestAccess manifestAccess = new ManifestAccess();
		manifestAccess.setLineDelimiter(codeConfig.getLineDelimiter());
		return manifestAccess;
	}

	protected PluginXmlAccess newPluginXmlAccess() {
		return new PluginXmlAccess();
	}

	protected XtextGeneratorFileSystemAccess newFileSystemAccess(String path, boolean overWrite) {
		return new XtextGeneratorFileSystemAccess(path, overWrite);
	}

	public RuntimeProjectConfig getRuntime() {
		return runtime;
	}

	public void setRuntime(RuntimeProjectConfig runtime) {
		this.runtime = runtime;
	}

	public BundleProjectConfig getRuntimeTest() {
		return runtimeTest;
	}

	public void setRuntimeTest(BundleProjectConfig runtimeTest) {
		this.runtimeTest = runtimeTest;
	}

	public BundleProjectConfig getGenericIde() {
		return genericIde;
	}

	public void setGenericIde(BundleProjectConfig genericIde) {
		this.genericIde = genericIde;
	}

	public BundleProjectConfig getEclipsePlugin() {
		return eclipsePlugin;
	}

	public void setEclipsePlugin(BundleProjectConfig eclipsePlugin) {
		this.eclipsePlugin = eclipsePlugin;
	}

	public BundleProjectConfig getEclipsePluginTest() {
		return eclipsePluginTest;
	}

	public void setEclipsePluginTest(BundleProjectConfig eclipsePluginTest) {
		this.eclipsePluginTest = eclipsePluginTest;
	}

	public CodeConfig getCodeConfig() {
		return codeConfig;
	}

	public void setCodeConfig(CodeConfig codeConfig) {
		this.codeConfig = codeConfig;
	}
}
