/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.model.project;

import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;

import com.google.common.base.CharMatcher;
import com.google.inject.Injector;

/**
 * Configuration of the runtime project, i.e. the main subproject containing the
 * language definition.
 * 
 * @noextend This class should not be extended by clients.
 */
public class RuntimeProjectConfig extends BundleProjectConfig implements IRuntimeProjectConfig {
	private String ecoreModelPath;

	private IXtextGeneratorFileSystemAccess ecoreModel;

	public void setEcoreModel(String path) {
		this.ecoreModelPath = path;
	}

	/**
	 * Returns the root-relative path of the folder where the generated .ecore
	 * and .genmodel can be found. The path is delimited by '/', but does not
	 * begin or end with a separator.
	 */
	@Override
	public String getEcoreModelFolder() {
		String rootPath = getRoot().getPath();
		String ecoreModelPath = ecoreModel.getPath();
		if (ecoreModelPath.startsWith(rootPath)) {
			String relativePath = ecoreModelPath.substring(rootPath.length())
					.replace("\\", "/");
			return CharMatcher.is('/').trimFrom(relativePath);
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Could not derive the Ecore model folder from the project configuration.\n");
		builder.append("Please make sure that 'root' is a prefix of 'ecoreModel'.\n");
		builder.append("was (root='").append(rootPath).append("', ecoreModel='").append(ecoreModelPath).append("')");
		throw new RuntimeException(builder.toString());
	}

	@Override
	public void initialize(final Injector injector) {
		super.initialize(injector);
		if (ecoreModelPath != null) {
			ecoreModel = getOwner().newFileSystemAccess(ecoreModelPath, true);
			ecoreModel.initialize(injector);
		}
	}

	@Override
	public String getXbaseLibVersionLowerBound() {
		// We re-export Guava from xbase.lib thus we need to restrict the version of the library to the latest and greatest
		return "2.39.0";
	}

	@Override
	public String getXtendLibVersionLowerBound() {
		return getXbaseLibVersionLowerBound();
	}

	public String getEcoreModelPath() {
		return ecoreModelPath;
	}

	@Override
	public IXtextGeneratorFileSystemAccess getEcoreModel() {
		return ecoreModel;
	}
}
