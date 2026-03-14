/*******************************************************************************
 * Copyright (c) 2009, 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.parser.antlr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.WrappedException;

public class AntlrToolFacade {

	private final static Logger log = Logger.getLogger(AntlrToolFacade.class);

	public final static String className = "de.itemis.xtext.antlr.toolrunner.AntlrToolRunner";

	private ClassLoader loader = AntlrToolFacade.class.getClassLoader();

	public void setLoader(ClassLoader loader) {
		this.loader = loader;
	}

	private String downloadURL = "https://artifacts.itemis.cloud/repository/p2/antlr-generator-3.2.0-patch.jar";
	private String secureDownloadURL = "https://artifacts.itemis.cloud/repository/p2/antlr-generator-3.2.0-patch.jar";
	private boolean askBeforeDownload = false;

	public void setAskBeforeDownload(boolean shouldAsk) {
		this.askBeforeDownload = shouldAsk;
	}

	/**
	 * @deprecated use {@link #setSecureDownloadFrom(String)} instead
	 */
	@Deprecated
	public void setDownloadFrom(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public void setSecureDownloadFrom(String secureDownloadURL) {
		this.secureDownloadURL = secureDownloadURL;
	}

	private String downloadTo = "./.antlr-generator-3.2.0-patch.jar";

	public void setDownloadTo(String path) {
		this.downloadTo = path;
	}
	protected File file() {
		return new File(downloadTo);
	}
	
	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected String getToolRunnerClassName() {
		return className;
	}

	protected Class<?> getToolClass() {
		try {
			return loader.loadClass(getToolRunnerClassName());
		} catch (ClassNotFoundException e) {
			if (!file().exists()) {
				if (!download())
					return null;
			}
			if (file().exists()) {
				try {
					URL url = file().toURI().toURL();
					loader = new URLClassLoader(new URL[] { url }, loader);
					ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(loader);
						return loader.loadClass(className);
					} finally {
						Thread.currentThread().setContextClassLoader(contextClassLoader);
					}
				} catch (MalformedURLException e1) {
					log.error(e1.getMessage(), e1);
				} catch (ClassNotFoundException e1) {
					log.error(e1.getMessage(),e1);
				}
			}
			return null;
		}
	}

	public boolean download() {
		try {
			if (askBeforeDownload) {
				boolean ok = false;
				while (!ok) {
					System.err.print("\n*ATTENTION*\nIt is recommended to use the ANTLR 3 parser generator (BSD licence - http://www.antlr.org/license.html).\nDo you agree to download it (size 1MB) from '"
							+ secureDownloadURL + "'? (type 'y' or 'n' and hit enter)");
					int read = System.in.read();
					if (read == 'n') {
						return false;
					} else if (read == 'y') {
						ok = true;
					}
				}
			}
			log.info("Downloading file from '"+secureDownloadURL+"' ...");
			BufferedInputStream in = new BufferedInputStream(new URL(secureDownloadURL).openStream());
			try {
				FileOutputStream out = new FileOutputStream(file());
				try {
					byte[] buffer = new byte[2048];
					int readBytes = -1;
					while ((readBytes = in.read(buffer)) != -1) {
						out.write(buffer, 0, readBytes);
					}
				} finally {
					out.close();
				}
			} finally {
				in.close();
			}
			log.info("Finished downloading.");
		} catch (IOException e) {
			try {
				log.warn("Downloading file from '"+secureDownloadURL+"' failed.", e);
				log.info("Downloading file from '"+downloadURL+"' ...");
				BufferedInputStream in = new BufferedInputStream(new URL(downloadURL).openStream());
				try {
					FileOutputStream out = new FileOutputStream(file());
					try {
						byte[] buffer = new byte[2048];
						int readBytes = -1;
						while ((readBytes = in.read(buffer)) != -1) {
							out.write(buffer, 0, readBytes);
						}
					} finally {
						out.close();
					}
				} finally {
					in.close();
				}
				log.info("Finished downloading.");
			} catch (IOException e2) {
				System.err.println("Downloading ANTLR parser generator failed: " + e2.getMessage());
				System.err.println("Please install the feature 'Xtext Antlr SDK' manually using the external updatesite:");
				System.err.println();
				System.err.println("\t\t'https://artifacts.itemis.cloud/repository/p2/xtext-updates/releases/2.1.1/'."); 
				System.err.println();
				System.err.println("(see https://www.eclipse.org/Xtext/download.html for details)");
				return false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			return false;
		}
		if (getToolClass() == null) {
			log.error("Loaded jar doesn't contain "+className);
			return false;
		}
		return true;
	}

	private IllegalStateException getNoClassFoundException() {
		return new IllegalStateException("Couldn't find ANTLR generator on class path. Please download manually from '"
				+ secureDownloadURL + "' and put it on the classpath.");
	}
	
	public void runWithEncodingAndParams(String grammarFullPath, String explicitEncoding, String... furtherArgs) {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(loader);
			Class<?> class1 = getToolClass();
			if (class1 == null)
				throw getNoClassFoundException();
			Method method = class1.getMethod("runWithEncodingAndParams", new Class[] { String.class, String.class, String[].class });
			method.invoke(null, grammarFullPath, explicitEncoding, furtherArgs);
		} catch (Exception e) {
			throw new WrappedException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	public boolean isWorkable() {
		return getToolClass()!=null;
	}
}
