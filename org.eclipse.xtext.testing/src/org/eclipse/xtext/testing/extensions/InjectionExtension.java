/*******************************************************************************
 * Copyright (c) 2018-2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.extensions;

import static org.eclipse.xtext.util.Exceptions.throwUncheckedException;

import java.util.Optional;

import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.eclipse.xtext.testing.InjectWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.Injector;

/**
 * Use this extension together with {@link InjectWith} for JUnit 5 based Xtext
 * tests.
 * <p>
 * The extension retrieves an {@link Injector} to inject members in the instance
 * under test before best execution and resets registries after execution.
 * </p>
 * It takes care about JUnit5 {@link Nested nested} test classes. They are inner classes that might
 * be annotated as well and must be handled for injection.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @author Karsten Thoms - Initial contribution and API
 * @since 2.14
 */
public class InjectionExtension implements BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

	/**
	 * Maintains a map of injector provider types to their instance.
	 * <p>
	 * {@link #getOrCreateInjectorProvider(ExtensionContext)} is used to obtain
	 * an valid injector provider in the context of a given {@link ExtensionContext}.
	 * </p>
	 */
	private static ClassToInstanceMap<IInjectorProvider> injectorProviderClassCache = MutableClassToInstanceMap.create();

	/**
	 * @noextend This class is not intended to be subclassed by clients.
	 * @since 2.24
	 */
	protected static class RegistryReset implements AutoCloseable {
		protected final IRegistryConfigurator resetter;
		protected boolean didSetup = false;

		public RegistryReset(IRegistryConfigurator resetter) {
			this.resetter = resetter;
			setup();
		}

		public void setup() {
			if (!didSetup) {
				didSetup = true;
				resetter.setupRegistry();
			}
		}

		@Override
		public void close() throws Exception {
			if (didSetup) {
				resetter.restoreRegistry();
				didSetup = false;
			}
		}
	}

	/**
	 * @since 2.24
	 */
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		if (injectorProvider != null) {
			setupRegistry(injectorProvider, context);
			Injector injector = injectorProvider.getInjector();
			if (injector != null) {
				injector.injectMembers(testInstance);
			}
		}
	}

	/**
	 * @since 2.24
	 */
	protected void setupRegistry(IInjectorProvider injectorProvider, ExtensionContext context) {
		if (injectorProvider instanceof IRegistryConfigurator) {
			IRegistryConfigurator registryConfigurator = (IRegistryConfigurator) injectorProvider;
			context.getStore(Namespace.create(this)).getOrComputeIfAbsent(registryConfigurator, RegistryReset::new, RegistryReset.class).setup();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		if (injectorProvider instanceof IRegistryConfigurator) {
			setupRegistry(injectorProvider, context);
		}
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		IInjectorProvider injectorProvider = getOrCreateInjectorProvider(context);
		restoreRegistry(injectorProvider, context);
	}

	/**
	 * @since 2.24
	 */
	protected void restoreRegistry(IInjectorProvider injectorProvider, ExtensionContext context) {
		if (injectorProvider instanceof IRegistryConfigurator) {
			IRegistryConfigurator registryConfigurator = (IRegistryConfigurator) injectorProvider;
			RegistryReset resetter = context.getStore(Namespace.create(this)).get(registryConfigurator, RegistryReset.class);
			if (resetter != null) {
				try {
					resetter.close();
				} catch (Throwable e) {
					throw new ExtensionConfigurationException("Failed to restore registry", e);
				}
			}
		}
	}

	/**
	 * Returns the {@link IInjectorProvider injector provider} for the given context. Tries to find an {@link InjectWith}
	 * annotation on the required test classes along the {@link ExtensionContext#getParent() hierarchy} of contexts.
	 *
	 * If the injector provider can be found, it will be reflectively instantiated and cached. Only one instance of any
	 * given injector provider will be created.
	 *
	 * @since 2.24
	 */
	protected static IInjectorProvider getOrCreateInjectorProvider(ExtensionContext context) {
		Optional<InjectWith> injectWithOpt = AnnotationSupport.findAnnotation(context.getRequiredTestClass(), InjectWith.class);
		if (injectWithOpt.isPresent()) {
			InjectWith injectWith = injectWithOpt.get();
			Class<? extends IInjectorProvider> klass = injectWith.value();
			IInjectorProvider injectorProvider = injectorProviderClassCache.get(klass);
			if (injectorProvider == null) {
				try {
					injectorProvider = klass.getDeclaredConstructor().newInstance();
					injectorProviderClassCache.put(klass, injectorProvider);
				} catch (Exception e) {
					throwUncheckedException(e);
				}
			}
			return injectorProvider;
		} else {
			Optional<ExtensionContext> parentContext = context.getParent().filter(p->p.getTestClass().isPresent());
			if (parentContext.isPresent()) {
				return getOrCreateInjectorProvider(parentContext.get());
			}
		}
		return null;
	}

}
