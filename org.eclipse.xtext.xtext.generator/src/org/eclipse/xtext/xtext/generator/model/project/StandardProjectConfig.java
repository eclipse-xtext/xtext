/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.model.project;

import java.util.Objects;

import org.eclipse.emf.mwe2.runtime.Mandatory;
import org.eclipse.xtext.xtext.generator.Issues;

/**
 * Specialized project configuration that uses standard names and paths. Usually
 * it is sufficient to set the {@code baseName} and {@code rootPath} properties
 * and to enable the required subprojects.
 * 
 * @noextend This class should not be extended by clients.
 */
public class StandardProjectConfig extends XtextProjectConfig {
	/**
	 * set to {@code true} by the project wizard(s) in case "Maven/Gradle"
	 * source layout is selected.
	 */
	private boolean mavenLayout;

	private boolean createEclipseMetaData;

	private String rootPath;

	private String baseName;

	/**
	 * The base name of the project, which is usually equal to the runtime
	 * subproject.
	 */
	@Mandatory
	public String setBaseName(String baseName) {
		return this.baseName = baseName;
	}

	/**
	 * The root path, usually {@code ".."}.
	 */
	@Mandatory
	public String setRootPath(String rootPath) {
		return this.rootPath = rootPath;
	}

	@Override
	public void checkConfiguration(Issues issues) {
		super.checkConfiguration(issues);
		if (rootPath == null) {
			issues.addError("The property 'rootPath' must be set", this);
		}
		if (baseName == null) {
			issues.addError("The property 'baseName' must be set", this);
		}
	}

	@Override
	public void setDefaults() {
		super.setDefaults();
		for(SubProjectConfig it : getEnabledProjects()) {
			if (it.getName() == null) {
				it.setName(computeName(it));
			}
			if (it.getRootPath() == null) {
				it.setRoot(computeRoot(it));
			}
			if (it.getMetaInfPath() == null) {
				it.setMetaInf(computeMetaInf(it));
			}
			if (it.getSrcPath() == null) {
				it.setSrc(computeSrc(it));
			}
			if (it.getSrcGenPath() == null) {
				it.setSrcGen(computeSrcGen(it));
			}
			if (it.getIconsPath() == null) {
				it.setIcons(computeIcons(it));
			}
			if (it instanceof BundleProjectConfig) {
				if (createEclipseMetaData) {
					BundleProjectConfig bundleProjectConfig = (BundleProjectConfig) it;
					if (bundleProjectConfig.getManifest() == null) {
						bundleProjectConfig.setManifest(newManifestAccess());
					}
					if (bundleProjectConfig.getPluginXml() == null) {
						bundleProjectConfig.setPluginXml(newPluginXmlAccess());
					}
				}
			}
			if (it instanceof RuntimeProjectConfig) {
				RuntimeProjectConfig runtimeProjectConfig = (RuntimeProjectConfig) it;
				if (!(runtimeProjectConfig.getEcoreModelPath() != null)) {
					runtimeProjectConfig.setEcoreModel(computeEcoreModel(runtimeProjectConfig));
				}
			}
		};
	}

	protected String computeName(SubProjectConfig project) {
		if (Objects.equals(project, getRuntime())) {
			return baseName;
		} else if (Objects.equals(project, getRuntimeTest())) {
			if (!mavenLayout) {
				return baseName + ".tests";
			} else {
				return baseName;
			}
		} else if (Objects.equals(project, getGenericIde())) {
			return baseName + ".ide";
		} else if (Objects.equals(project, getEclipsePlugin())) {
			return baseName + ".ui";
		} else if (Objects.equals(project, getEclipsePluginTest())) {
			if (!mavenLayout) {
				return baseName + ".ui.tests";
			} else {
				return baseName + ".ui";
			}
		}
		return null;
	}

	protected String computeRoot(SubProjectConfig project) {
		return rootPath + "/"+ project.getName();
	}

	/**
	 * In case of "Maven/Gradle" source layout the src outlet is named
	 * 'src/main/java', test classes go into 'src/test/java' instead of any
	 * dedicated '...tests' project.
	 */
	protected String computeSrc(SubProjectConfig project) {
		if (mavenLayout) {
			return project.getRootPath() + "/" + "src/"+ computeSourceSet(project) + "/java";
		} else {
			return project.getRootPath() + "/" + "src";
		}
	}

	/**
	 * In case of "Maven/Gradle" source layout the srcGen outlet is named
	 * 'src/main/xtext-gen', test-related srcGen classes go into
	 * 'src/test/xtext-gen' instead of any dedicated '...tests' project. Don't
	 * confuse it with 'src/main/xtend-gen'!
	 */
	protected String computeSrcGen(SubProjectConfig project) {
		if (mavenLayout) {
			return project.getRootPath() + "/" + "src/"+ computeSourceSet(project) + "/xtext-gen";
		} else {
			return project.getRootPath() + "/" + "src-gen";
		}
	}

	protected String computeMetaInf(SubProjectConfig project) {
		if (mavenLayout) {
			return project.getRootPath() + "/" + "src/"+ computeSourceSet(project) + "/resources/META-INF";
		} else {
			return project.getRootPath() + "/" + "META-INF";
		}
	}

	protected String computeEcoreModel(RuntimeProjectConfig project) {
		return project.getRootPath() + "/" + "model/generated";
	}

	protected String computeSourceSet(SubProjectConfig project) {
		if (getTestProjects().contains(project)) {
			return "test";
		} else {
			return "main";
		}
	}

	protected String computeIcons(SubProjectConfig project) {
		return project.getRootPath() + "/" + "icons";
	}

	public boolean isMavenLayout() {
		return mavenLayout;
	}

	public void setMavenLayout(boolean mavenLayout) {
		this.mavenLayout = mavenLayout;
	}

	public boolean isCreateEclipseMetaData() {
		return createEclipseMetaData;
	}

	public void setCreateEclipseMetaData(boolean createEclipseMetaData) {
		this.createEclipseMetaData = createEclipseMetaData;
	}

	public String getRootPath() {
		return rootPath;
	}

	public String getBaseName() {
		return baseName;
	}
}
