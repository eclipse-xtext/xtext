/*******************************************************************************
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.junit

import com.google.inject.Inject
import com.google.inject.Injector
import org.eclipse.emf.ecore.EClass
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.util.JUnitVersion
import org.eclipse.xtext.xtext.generator.AbstractStubGeneratingFragment
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory
import org.eclipse.xtext.xtext.generator.model.JavaFileAccess
import org.eclipse.xtext.xtext.generator.model.TypeReference

import static extension org.eclipse.xtext.GrammarUtil.*

/**
 * @since 2.14
 */
class JUnitFragment extends AbstractStubGeneratingFragment {

	@Inject extension XtextGeneratorNaming
	@Inject FileAccessFactory fileAccessFactory

	@Accessors(PUBLIC_SETTER)
	boolean skipXbaseTestingPackage = false

	JUnitVersion junitVersion = JUnitVersion.JUNIT_4

	new() {
		/**
		 * The old implementation generated JUnit stub by default. 
		 * We set the value of generateXtendStub to true to remain backward compatible.
		 */
		generateXtendStub = true
	}

	def void setJunitVersion(String version) {
		junitVersion = JUnitVersion.fromString(version)
	}

	def protected getTestingPackage() {
		"org.eclipse.xtext.testing"
	}

	protected def String getUiTestingPackage() {
		"org.eclipse.xtext.ui.testing"
	}

	def protected getXbaseTestingPackage() {
		if (skipXbaseTestingPackage)
			return ""
		else
			"org.eclipse.xtext.xbase.testing"
	}

	protected def String getXbaseUiTestingPackage() {
		if (skipXbaseTestingPackage)
			return ""
		return "org.eclipse.xtext.xbase.ui.testing"
	}

	override generate() {
		if (projectConfig.runtimeTest.manifest !== null) {
			projectConfig.runtimeTest.manifest => [
				requiredBundles.addAll(
					testingPackage,
					xbaseTestingPackage,
					'org.eclipse.xtext.xbase.lib;bundle-version="'+projectConfig.runtime.xbaseLibVersionLowerBound+'"'
				)
				exportedPackages.add(grammar.runtimeTestBasePackage + ";x-internal=true")
			]
		}
		if (projectConfig.eclipsePluginTest.manifest !== null) {
			projectConfig.eclipsePluginTest.manifest => [
				requiredBundles.addAll(
					testingPackage,
					xbaseTestingPackage,
					uiTestingPackage,
					xbaseUiTestingPackage,
					"org.eclipse.core.runtime",
					"org.eclipse.ui.workbench;resolution:=optional"
				)
				exportedPackages.add(grammar.eclipsePluginTestBasePackage + ";x-internal=true")
			]
		}
		if (projectConfig.eclipsePlugin.manifest !== null) {
			projectConfig.eclipsePlugin.manifest.exportedPackages.add(eclipsePluginActivator.packageName)
		}

		generateInjectorProvider.writeTo(projectConfig.runtimeTest.srcGen)
		if (isGenerateStub) {
			if (generateXtendStub) {
				generateExampleRuntimeTest.writeTo(projectConfig.runtimeTest.src)
			} else {
				generateJavaExampleRuntimeTest.writeTo(projectConfig.runtimeTest.src)
			}
		}
		if (projectConfig.eclipsePlugin.srcGen !== null)
			generateUiInjectorProvider.writeTo(projectConfig.eclipsePluginTest.srcGen)
	}

	def protected JavaFileAccess generateExampleRuntimeTest() {
		val xtextRunner = new TypeReference(testingPackage + ".XtextRunner")
		val runWith = new TypeReference("org.junit.runner.RunWith")
		val injectWith = new TypeReference(testingPackage + ".InjectWith")
		val extendWith = new TypeReference("org.junit.jupiter.api.^extension.ExtendWith")
		val injectionExtension = new TypeReference("org.eclipse.xtext.testing.extensions.InjectionExtension")
		val parseHelper = new TypeReference(testingPackage + ".util.ParseHelper")
		val test = switch (junitVersion) {
			case JUnitVersion.JUNIT_4: new TypeReference("org.junit.Test")
			case JUnitVersion.JUNIT_6: new TypeReference("org.junit.jupiter.api.Test")
		}
		val assert = switch (junitVersion) {
			case JUnitVersion.JUNIT_4: new TypeReference("org.junit.Assert")
			case JUnitVersion.JUNIT_6: new TypeReference("org.junit.jupiter.api.Assertions")
		}
		val rootType = new TypeReference(grammar.rules.head.type.classifier as EClass, grammar.eResource.resourceSet)
		return fileAccessFactory.createXtendFile(exampleRuntimeTest, '''
			«IF junitVersion==JUnitVersion.JUNIT_4»
				@«runWith»(«xtextRunner»)
			«ENDIF»
			«IF junitVersion==JUnitVersion.JUNIT_6»
				@«extendWith»(«injectionExtension»)
			«ENDIF»
			@«injectWith»(«injectorProvider»)
			class «exampleRuntimeTest» {
				@«Inject»
				«parseHelper»<«rootType»> parseHelper
				
				@«test»
				def void loadModel() {
					val result = parseHelper.parse(''«»'
						Hello Xtext!
					''«»')
					«assert».assertNotNull(result)
					val errors = result.eResource.errors
					«IF junitVersion==JUnitVersion.JUNIT_4»
						«assert».assertTrue(''«»'Unexpected errors: «"\u00AB"»errors.join(", ")«"\u00BB"»''«»', errors.isEmpty)
					«ENDIF»
					«IF junitVersion==JUnitVersion.JUNIT_6»
						«assert».assertTrue(errors.isEmpty, ''«»'Unexpected errors: «"\u00AB"»errors.join(", ")«"\u00BB"»''«»')
					«ENDIF»
				}
			}
		''')
	}

	def protected JavaFileAccess generateJavaExampleRuntimeTest() {
		val xtextRunner = new TypeReference(testingPackage + ".XtextRunner")
		val runWith = new TypeReference("org.junit.runner.RunWith")
		val extendWith = new TypeReference("org.junit.jupiter.api.extension.ExtendWith")
		val injectionExtension = new TypeReference("org.eclipse.xtext.testing.extensions.InjectionExtension")
		val injectWith = new TypeReference(testingPackage + ".InjectWith")
		val parseHelper = new TypeReference(testingPackage + ".util.ParseHelper")
		val test = switch (junitVersion) {
			case JUnitVersion.JUNIT_4: new TypeReference("org.junit.Test")
			case JUnitVersion.JUNIT_6: new TypeReference("org.junit.jupiter.api.Test")
		}
		val assert = switch (junitVersion) {
			case JUnitVersion.JUNIT_4: new TypeReference("org.junit.Assert")
			case JUnitVersion.JUNIT_6: new TypeReference("org.junit.jupiter.api.Assertions")
		}
		val rootType = new TypeReference(grammar.rules.head.type.classifier as EClass, grammar.eResource.resourceSet)
		val list = new TypeReference("java.util.List")
		val diagnostic = new TypeReference("org.eclipse.emf.ecore.resource", "Resource.Diagnostic")
		val iterableExtensions = new TypeReference("org.eclipse.xtext.xbase.lib.IterableExtensions")
		return fileAccessFactory.createJavaFile(exampleRuntimeTest, '''
			«IF junitVersion==JUnitVersion.JUNIT_4»
				@«runWith»(«xtextRunner».class)
			«ENDIF»
			«IF junitVersion==JUnitVersion.JUNIT_6»
				@«extendWith»(«injectionExtension».class)
			«ENDIF»
			@«injectWith»(«injectorProvider».class)
			public class «exampleRuntimeTest» {
				@«Inject»
				private «parseHelper»<«rootType»> parseHelper;
				
				@«test»
				public void loadModel() throws Exception {
					Model result = parseHelper.parse("Hello Xtext!");
					«assert».assertNotNull(result);
					«list»<«diagnostic»> errors = result.eResource().getErrors();
					«IF junitVersion==JUnitVersion.JUNIT_4»
						«assert».assertTrue("Unexpected errors: " + «iterableExtensions».join(errors, ", "), errors.isEmpty());
					«ENDIF»
					«IF junitVersion==JUnitVersion.JUNIT_6»
						«assert».assertTrue(errors.isEmpty(), "Unexpected errors: " + «iterableExtensions».join(errors, ", "));
					«ENDIF»
				}
			}
		''')
	}

	def protected exampleRuntimeTest() {
		new TypeReference(grammar.runtimeTestBasePackage, grammar.simpleName + "ParsingTest")
	}

	def protected JavaFileAccess generateInjectorProvider() {
		val file = fileAccessFactory.createJavaFile(injectorProvider)
		val globalRegistries = new TypeReference(testingPackage + ".GlobalRegistries")
		val globalStateMemento = new TypeReference(testingPackage, "GlobalRegistries.GlobalStateMemento")
		val iRegistryConfigurator = new TypeReference(testingPackage + ".IRegistryConfigurator")
		val classLoader = new TypeReference("java.lang.ClassLoader")
		val guice = new TypeReference("com.google.inject.Guice")
		file.content = '''
			public class «injectorProvider.simpleName» implements «iInjectorProvider», «iRegistryConfigurator» {
			
				protected «globalStateMemento» stateBeforeInjectorCreation;
				protected «globalStateMemento» stateAfterInjectorCreation;
				protected «Injector» injector;
			
				static {
					«globalRegistries».initializeDefaults();
				}
			
				@Override
				public «Injector» getInjector() {
					if (injector == null) {
						this.injector = internalCreateInjector();
						stateAfterInjectorCreation = «globalRegistries».makeCopyOfGlobalState();
					}
					return injector;
				}
			
				protected «Injector» internalCreateInjector() {
					return new «grammar.runtimeSetup»() {
						@Override
						public Injector createInjector() {
							return «guice».createInjector(createRuntimeModule());
						}
					}.createInjectorAndDoEMFRegistration();
				}
			
				protected «grammar.runtimeModule» createRuntimeModule() {
					// make it work also with Maven/Tycho and OSGI
					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=493672
					// allows for bindClassLoaderToInstance to get the class loader of the bundle
					// containing the instance of the injector provider (possibly inherited)
					return new «grammar.runtimeModule»() {
						@Override
						public «classLoader» bindClassLoaderToInstance() {
							return «injectorProvider.simpleName».this.getClass()
									.getClassLoader();
						}
					};
				}
			
				@Override
				public void restoreRegistry() {
					stateBeforeInjectorCreation.restoreGlobalState();
					stateBeforeInjectorCreation = null;
				}
			
				@Override
				public void setupRegistry() {
					stateBeforeInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();
					if (injector == null) {
						getInjector();
					}
					stateAfterInjectorCreation.restoreGlobalState();
				}
			}
		'''
		file
	}

	def protected TypeReference iInjectorProvider() {
		new TypeReference(testingPackage + ".IInjectorProvider")
	}

	def protected TypeReference injectorProvider() {
		new TypeReference(grammar.runtimeTestBasePackage, grammar.simpleName + "InjectorProvider")
	}

	def protected JavaFileAccess generateUiInjectorProvider() {
		val file = fileAccessFactory.createJavaFile(uiInjectorProvider)
		file.content = '''
			public class «uiInjectorProvider.simpleName» implements «iInjectorProvider» {
			
				@Override
				public «Injector» getInjector() {
					return «eclipsePluginActivator».getInstance().getInjector("«grammar.name»");
				}
			
			}
		'''
		file
	}

	def protected TypeReference uiInjectorProvider() {
		new TypeReference(grammar.eclipsePluginTestBasePackage, grammar.simpleName + "UiInjectorProvider")
	}
}
