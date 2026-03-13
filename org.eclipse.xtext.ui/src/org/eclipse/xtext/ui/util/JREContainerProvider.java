/*******************************************************************************
 * Copyright (c) 2014, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.util;

import static com.google.common.collect.Iterables.*;
import static org.eclipse.jdt.launching.JavaRuntime.*;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.xtext.util.JavaVersion;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

/**
 * 
 * @author kosyakov - Initial contribution and API
 * @author Dennis Huebner
 * 
 * @since 2.6
 */
public class JREContainerProvider {

	/**
	 * @since 2.7
	 */
	public static final String PREFERRED_BREE = "JavaSE-21";
	private static IVMInstall defaultVMInstall = null;
	private static boolean defaultVMinitialized = false;

	/**
	 * @since 2.6
	 */
	public static IClasspathEntry getDefaultJREContainerEntry() {
		IPath jreContainerPath = getDefaultJREContainerPath();
		return JavaCore.newContainerEntry(jreContainerPath);
	}

	/**
	 * @since 2.6
	 */
	public static IPath getDefaultJREContainerPath() {
		if (defaultVMInstall() != null) {
			IExecutionEnvironmentsManager executionEnvironmentsManager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment[] executionEnvironments = executionEnvironmentsManager.getExecutionEnvironments();
			for (IExecutionEnvironment executionEnvironment : executionEnvironments) {
				if (executionEnvironment.isStrictlyCompatible(defaultVMInstall())) {
					return newJREContainerPath(executionEnvironment);
				}
			}
		}
		return newPreferredContainerPath();
	}

	private static IVMInstall defaultVMInstall() {
		if (!defaultVMinitialized) {
			// remember result - getDefaultVMInstall() is very expensive operation
			defaultVMInstall = getDefaultVMInstall();
			defaultVMinitialized = true;
		}
		return defaultVMInstall;
	}

	/**
	 * @return JRE container path {@link IPath} for standard VM "J2SE-1.5"
	 */
	@Deprecated(since = "2.30", forRemoval = true) 
	protected static IPath newJRE15ContainerPath() {
		return newJREContainerPath(StandardVMType.ID_STANDARD_VM_TYPE, JavaVersion.JAVA5.getBree());
	}

	/**
	 * @deprecated use {@link #newPreferredContainerPath()} instead
	 * @since 2.7
	 */
	@Deprecated
	protected static IPath newPrefferedContainerPath() {
		return newPreferredContainerPath();
	}

	/**
	 * @since 2.8
	 * @return JRE container path {@link IPath} for standard VM {@value #PREFERRED_BREE}
	 */
	protected static IPath newPreferredContainerPath() {
		return newJREContainerPath(StandardVMType.ID_STANDARD_VM_TYPE, PREFERRED_BREE);
	}

	public static IClasspathEntry getJREContainerEntry(IJavaProject javaProject) throws JavaModelException {
		IPath defaultJREContainerPathPrefix = JavaRuntime.newDefaultJREContainerPath().append(StandardVMType.ID_STANDARD_VM_TYPE);
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		for (IClasspathEntry classpathEntry : rawClasspath) {
			int entryKind = classpathEntry.getEntryKind();
			if (entryKind == IClasspathEntry.CPE_CONTAINER && defaultJREContainerPathPrefix.isPrefixOf(classpathEntry.getPath())) {
				return classpathEntry;
			}
		}
		return null;
	}

	/**
	 * @since 2.9
	 */
	public static IClasspathEntry getJREContainerEntry(String bree) {
		IClasspathEntry jreContainerEntry = getDefaultJREContainerEntry();
		IExecutionEnvironment ee = JavaRuntime.getExecutionEnvironmentsManager().getEnvironment(bree);
		if (ee != null) {
			return JavaCore.newContainerEntry(newJREContainerPath(ee));
		}
		return jreContainerEntry;
	}

	/**
	 * @since 2.7
	 */
	public static String getDefaultBREE() {
		return getDefaultJREContainerPath().lastSegment();
	}

	/**
	 * @return an {@link Iterable} of BREE that have a configured default VM<br>
	 *         or have a strictly compatible installed JRE (aka "perfect match")
	 * @since 2.9
	 */
	public static Iterable<String> getConfiguredBREEs() {
		final Set<IVMInstall> vms = Sets.newHashSet();
		for (IVMInstallType vmType : JavaRuntime.getVMInstallTypes()) {
			vms.addAll(Arrays.asList(vmType.getVMInstalls()));
		}
		Iterable<IExecutionEnvironment> supportedEEs = filter(
				Arrays.asList(JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()),
				new Predicate<IExecutionEnvironment>() {
					@Override
					public boolean apply(final IExecutionEnvironment ee) {
						return ee.getDefaultVM() != null || any(vms, new Predicate<IVMInstall>() {
							@Override
							public boolean apply(IVMInstall vm) {
								return ee.isStrictlyCompatible(vm);
							}
						});
					}
				});
		return transform(supportedEEs, new Function<IExecutionEnvironment, String>() {
			@Override
			public String apply(IExecutionEnvironment input) {
				return input.getId();
			}
		});
	}
}
