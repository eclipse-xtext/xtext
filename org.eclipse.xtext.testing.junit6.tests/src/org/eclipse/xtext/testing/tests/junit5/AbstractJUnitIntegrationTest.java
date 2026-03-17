package org.eclipse.xtext.testing.tests.junit5;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Base class for testing JUnit specific integration. For JUnit 4 this is the Test Runner {@link XtextRunner},
 * while JUnit 5 uses extensions, here the {@link InjectionExtension}.
 * <p>
 * Copied from initial XtextRunnerTest.
 * 
 * @author Sebastian Benz - Initial contribution and API
 * @author Karsten Thoms - Initial contribution and API
 */
public abstract class AbstractJUnitIntegrationTest {
	private static boolean injectorCreated = false;
	private static boolean registrySaved = false;
	private static boolean registryRestored = false;
	
	@Inject
	private Boolean fieldsInjected = false;

	public static class MyInjectorProvider implements IRegistryConfigurator, IInjectorProvider {
		@Override
		public Injector getInjector() {
			injectorCreated = true;
			assertTrue(registrySaved);
			
			return Guice.createInjector(new Module(){
				@Override
				public void configure(Binder binder) {
					binder.bind(Boolean.class).toInstance(Boolean.TRUE);
				}
			});
		}

		@Override
		public void setupRegistry() {
			registrySaved = true;
			if (!registryRestored) {
				assertFalse(injectorCreated);
			} else {
				registryRestored = false;
			}
		}

		@Override
		public void restoreRegistry() {
			assertTrue(registrySaved);
			registryRestored = true;
			registrySaved = false;
		}
	}
	
	@AfterAll
	public static void resetStaticBooleans() {
		injectorCreated = false;
		registrySaved = false;
		registryRestored = false;
	}
	
	@BeforeEach
	public final void beforeShouldBeExecutedAfterTheRegistriesAreInitialized(){
		assertTrue(registrySaved);
		assertTrue(injectorCreated);
		assertTrue(fieldsInjected);
	}
	
	public abstract void shouldSaveRegistriesBeforeCreatingAnInjector();
	
	@AfterEach
	public final void afterShouldBeExecutedBeforeTheRegistriesAreRestored(){
		assertFalse(registryRestored);
	}
}
