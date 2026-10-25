/**
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.common.types.descriptions;

import static java.util.stream.Collectors.*;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

@Singleton
public class ClasspathScanner {
	protected static class ClassLoaderPackageConfig {
		private final ClassLoader loader;

		private final boolean bootstrap;

		private final Collection<String> packagePrefixes;

		public ClassLoaderPackageConfig(ClassLoader loader, boolean bootstrap,
				Collection<String> packagePrefixes) {
			this.loader = loader;
			this.bootstrap = bootstrap;
			this.packagePrefixes = packagePrefixes;
		}

		public ClassLoader getLoader() {
			return loader;
		}

		public boolean isBootstrap() {
			return bootstrap;
		}

		public Collection<String> getPackagePrefixes() {
			return packagePrefixes;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (loader == null ? 0 : loader.hashCode());
			result = prime * result + (bootstrap ? 1231 : 1237);
			return prime * result + (packagePrefixes == null ? 0 : packagePrefixes.hashCode());
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
			ClassLoaderPackageConfig other = (ClassLoaderPackageConfig) obj;
			if (loader == null) {
				if (other.loader != null) {
					return false;
				}
			} else if (!loader.equals(other.loader)) {
				return false;
			}
			if (other.bootstrap != bootstrap) {
				return false;
			}
			if (packagePrefixes == null) {
				if (other.packagePrefixes != null) {
					return false;
				}
			} else if (!packagePrefixes.equals(other.packagePrefixes)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("loader", loader);
			b.add("bootstrap", bootstrap);
			b.add("packagePrefixes", packagePrefixes);
			return b.toString();
		}
	}

	@Inject
	private final Provider<ClassGraph> classGraphProvider = ClassGraph::new;

	private final Cache<ClassLoaderPackageConfig, Iterable<ITypeDescriptor>> classLoaderDescriptors = createClassLoaderCache();

	protected Cache<ClassLoaderPackageConfig, Iterable<ITypeDescriptor>> createClassLoaderCache() {
		return CacheBuilder.newBuilder()
			.initialCapacity(8)
			.concurrencyLevel(2)
			.maximumSize(32)
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();
	}

	public Iterable<ITypeDescriptor> getDescriptors(ClassLoader classLoader, Collection<String> packagePrefixes) {
		return getDescriptors(classLoader, false, packagePrefixes);
	}

	protected Iterable<ITypeDescriptor> getDescriptors(ClassLoader classLoader, boolean bootstrap, Collection<String> packagePrefixes) {
		ClassLoaderPackageConfig clPckCfg = new ClassLoaderPackageConfig(classLoader, bootstrap, packagePrefixes);
		try {
			return classLoaderDescriptors.get(clPckCfg, () -> loadDescriptors(classLoader, bootstrap, packagePrefixes));
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
	}

	public Iterable<ITypeDescriptor> getBootClasspathDescriptors(Collection<String> packagePrefixes) {
		return getDescriptors(ClassLoader.getSystemClassLoader(), true, packagePrefixes);
	}

	private String[] systemClasspath = null;

	public String[] getSystemClasspath() {
		if (systemClasspath != null) {
			return systemClasspath;
		}
		ClassGraph classGraph = classGraphProvider.get()
			.enableClassLoaders(ClassLoader.getSystemClassLoader())
			.enableClasspath()
			.enableSystemJars()
			.enableSystemModules();
		try (ScanResult scanResult = classGraph.scan()) {
			List<URI> classpathURIs = scanResult.getClasspathURIs();
			systemClasspath = classpathURIs.stream()
				.map((uri)-> {
					if ("jrt".equals(uri.getScheme())) {
						return null; // TODO what do we need to pass to ASTParser in JDT properly? anything at all?
					} else {
						return uri.getPath();
					}
				})
				.filter(Objects::nonNull)
				.toArray(String[]::new);
			return systemClasspath;
		}
	}

	protected Iterable<ITypeDescriptor> loadDescriptors(ClassLoader classLoader, boolean bootstrap,
			Collection<String> packagePrefixes) {
		ClassGraph classGraph = classGraphProvider.get()
			.enableClassLoaders(classLoader)
			.enableClasspath()
			.enableClassInfo()
			.ignoreClassVisibility()
			.acceptPackages(packagePrefixes.toArray(new String[packagePrefixes.size()]));
		if (bootstrap) {
			classGraph.enableSystemJars().enableSystemModules();
		}
		try (ScanResult scanResult = classGraph.scan()) {
			return loadDescriptors(scanResult);
		}
	}

	protected Iterable<ITypeDescriptor> loadDescriptors(ScanResult scanResult) {
		return scanResult.getAllClasses().stream()
			.filter(clazz -> clazz.getResource() != null)
			.map(clazz -> newDescriptor(clazz.getName(), clazz.getModifiers()))
			.collect(toList());
	}

	protected ITypeDescriptor newDescriptor(String name, int modifiers) {
		return new ClasspathTypeDescriptor(name, modifiers);
	}
}
