/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.ResourceLocator;

/**
 * @since 2.9
 */
public class XtextVersion {
	private static class Plugin extends EMFPlugin {
		public static final XtextVersion.Plugin INSTANCE = new XtextVersion.Plugin();

		private Plugin() {
			super(new ResourceLocator[] {});
		}

		@Override
		public ResourceLocator getPluginResourceLocator() {
			return null;
		}
	}

	private final String version;

	public XtextVersion(String version) {
		this.version = version;
	}

	public static XtextVersion getCurrent() {
		String versionFromManifest = XtextVersion.readVersionFromManifest();
		return new XtextVersion(versionFromManifest != null ? versionFromManifest : "unknown");
	}

	public String getXtextGradlePluginVersion() {
		return "4.0.0";
	}

	/**
	 * @noreference
	 */
	public String getMweVersion() {
		return "2.21.0";
	}

	/**
	 * @noreference
	 */
	public String getMweBuildNumber() {
		return "";
	}

	public String getAntlrGeneratorVersion() {
		return "2.1.1";
	}

	public String getXtendGradlePluginVersion() {
		return getXtextGradlePluginVersion();
	}

	/**
	 * @noreference
	 */
	public String getXtendAndroidGradlePluginVersion() {
		return getXtendGradlePluginVersion();
	}

	/**
	 * @return <code>true</code> if the current version ends with '-SNAPSHOT'
	 */
	public boolean isSnapshot() {
		return version.endsWith("-SNAPSHOT");
	}

	/**
	 * @return <code>true</code> if the current version is not a snapshot and
	 * not a release<br>
	 * Release builds must match a following pattern: N.N(.N)+<br>
	 * (N is a digit)<br>
	 * For example 2.9.2 is a release, 2.9.2.beta3 is stable.
	 */
	public boolean isStable() {
		return !isSnapshot() && !version.matches("\\d+\\.\\d+(\\.\\d+)+");
	}

	@Override
	public String toString() {
		return version;
	}

	private static String readVersionFromManifest() {
		URL baseURL = null;
		try {
			baseURL = XtextVersion.Plugin.INSTANCE.getBaseURL();
		} catch (Exception e) {
			return null;
		}
		try (InputStream is = new URL (baseURL + "META-INF/MANIFEST.MF").openStream()) {
			Manifest manifest = new Manifest(is);
			String version = manifest.getMainAttributes().getValue("Maven-Version");
			if (version == null) {
				version = manifest.getMainAttributes().getValue("Bundle-Version");
				if (version.endsWith(".qualifier")) {
					return version.replace(".qualifier", "-SNAPSHOT");
				} else {
					return version.substring(0, version.lastIndexOf("."));
				}
			}
			return version;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return 31 * 1 + ((this.version== null) ? 0 : this.version.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XtextVersion other = (XtextVersion) obj;
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}

	public String getVersion() {
		return version;
	}
}
