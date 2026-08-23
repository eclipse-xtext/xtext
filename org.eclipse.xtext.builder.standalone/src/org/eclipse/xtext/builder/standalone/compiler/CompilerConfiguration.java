/**
 * Copyright (c) 2013, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.builder.standalone.compiler;

import java.io.File;

import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.util.JavaVersion;

/**
 * @author Dennis Huebner - Initial contribution and API
 */
public class CompilerConfiguration {
	private String sourceLevel = JavaVersion.DEFAULT.getQualifier();

	private String targetLevel = JavaVersion.DEFAULT.getQualifier();

	private boolean verbose;

	private boolean skipAnnotationProcessing;

	private boolean preserveInformationAboutFormalParameters;
	
	private File compilerStateDirectory;
	
	private IResourceDescription.Event.Listener eventListener;
	
	public String getSourceLevel() {
		return sourceLevel;
	}

	public void setSourceLevel(String sourceLevel) {
		this.sourceLevel = sourceLevel;
	}

	public String getTargetLevel() {
		return targetLevel;
	}

	public void setTargetLevel(String targetLevel) {
		this.targetLevel = targetLevel;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isSkipAnnotationProcessing() {
		return skipAnnotationProcessing;
	}

	public void setSkipAnnotationProcessing(boolean skipAnnotationProcessing) {
		this.skipAnnotationProcessing = skipAnnotationProcessing;
	}

	public boolean isPreserveInformationAboutFormalParameters() {
		return preserveInformationAboutFormalParameters;
	}

	public void setPreserveInformationAboutFormalParameters(boolean preserveInformationAboutFormalParameters) {
		this.preserveInformationAboutFormalParameters = preserveInformationAboutFormalParameters;
	}
	
	public File getCompilerStateDirectory() {
		return compilerStateDirectory;
	}
	
	public IResourceDescription.Event.Listener getEventListener() {
		return eventListener;
	}
	
	public void enableIncrementalCompilation(File compilerStateDirectory, IResourceDescription.Event.Listener eventListener) {
		this.compilerStateDirectory = compilerStateDirectory;
		this.eventListener = eventListener;
	}
	
	public void disableIncrementalCompilation() {
		this.compilerStateDirectory = null;
		this.eventListener = null;
	}
	
}
