/*******************************************************************************
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator

import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Provider
import com.google.inject.Singleton
import com.google.inject.name.Names
import java.util.Collections
import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Properties
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.Constants
import org.eclipse.xtext.ISetup
import org.eclipse.xtext.XtextPackage
import org.eclipse.xtext.resource.impl.BinaryGrammarResourceFactoryImpl
import org.eclipse.xtext.service.SingletonBinding
import org.eclipse.xtext.util.Modules2
import org.eclipse.xtext.xtext.generator.model.FileAccessFactory
import org.eclipse.xtext.xtext.generator.model.GeneratedJavaFileAccess
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess
import org.eclipse.xtext.xtext.generator.model.JavaFileAccess
import org.eclipse.xtext.xtext.generator.model.TextFileAccess
import org.eclipse.xtext.xtext.generator.model.TypeReference
import org.eclipse.xtext.xtext.generator.model.annotations.SuppressWarningsAnnotation
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig

import static extension org.eclipse.xtext.xtext.generator.model.TypeReference.*

/**
 * Templates for generating the common language infrastructure.
 */
@Singleton
class XtextGeneratorTemplates {
	
	@Inject CodeConfig codeConfig

	@Inject FileAccessFactory fileAccessFactory
	
	@Inject extension XtextGeneratorNaming naming
	
	def JavaFileAccess createRuntimeSetup(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		if (codeConfig.preferXtendStubs) {
			return fileAccessFactory.createXtendFile(runtimeSetup,'''
				/**
				 * Initialization support for running Xtext languages without Equinox extension registry.
				 */
				class «runtimeSetup.simpleName» extends «runtimeGenSetup» {
				
					def static void doSetup() {
						new «runtimeSetup.simpleName»().createInjectorAndDoEMFRegistration()
					}
				}
		 	''')
		} else {
			return fileAccessFactory.createJavaFile(runtimeSetup,'''
				/**
				 * Initialization support for running Xtext languages without Equinox extension registry.
				 */
				public class «runtimeSetup.simpleName» extends «runtimeGenSetup» {
				
					public static void doSetup() {
						new «runtimeSetup.simpleName»().createInjectorAndDoEMFRegistration();
					}
				}
		 	''')
		}
	}
	
	@Deprecated
	private static def addBackwardsCompabibleImportsTo(IXtextGeneratorLanguage langConfig, GeneratedJavaFileAccess file) {
		for (type : langConfig.runtimeGenSetup.imports) {
			file.importType(type)
		}
	}
	
	def JavaFileAccess createRuntimeGenSetup(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		val file = fileAccessFactory.createGeneratedJavaFile(runtimeGenSetup)
		// The following imports are added for backwards-compatibility
		langConfig.addBackwardsCompabibleImportsTo(file);
		
		file.annotations += new SuppressWarningsAnnotation
		file.content = '''
			public class «runtimeGenSetup.simpleName» implements «ISetup» {
			
				@Override
				public «Injector» createInjectorAndDoEMFRegistration() {
					«FOR usedGrammar : langConfig.grammar.usedGrammars»
						«usedGrammar.runtimeSetup».doSetup();
					«ENDFOR»
					«IF langConfig.grammar.usedGrammars.isEmpty»
						// register default ePackages
						if (!«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("ecore"))
							«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
								"ecore", new «'org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl'.typeRef»());
						if (!«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("xmi"))
							«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
								"xmi", new «'org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl'.typeRef»());
						if (!«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("xtextbin"))
							«Resource.typeRef».Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
								"xtextbin", new «BinaryGrammarResourceFactoryImpl»());
						if (!«EPackage.typeRef».Registry.INSTANCE.containsKey(«XtextPackage».eNS_URI))
							«EPackage.typeRef».Registry.INSTANCE.put(«XtextPackage».eNS_URI, «XtextPackage».eINSTANCE);
					«ENDIF»
			
					«Injector» injector = createInjector();
					register(injector);
					return injector;
				}
				
				public «Injector» createInjector() {
					return «Guice».createInjector(new «runtimeModule»());
				}
				
				public void register(«Injector» injector) {
					«FOR reg : langConfig.runtimeGenSetup.registrations»
						«reg»
					«ENDFOR»
				}
			}
		 '''
		 return file
	}
	
	private def getBindMethodName(GuiceModuleAccess.Binding it) {
		'''«IF !value.provider && value.statements.isEmpty
			»bind«
		ELSEIF value.statements.isEmpty
			»provide«
		ELSE
			»configure«
		ENDIF
		»«key.name?.replace('.', '$') ?: key.type.simpleMethodName
		»«IF value.expression !== null && !value.provider»ToInstance«ENDIF»'''
	}
	
	private def String getSimpleMethodName(TypeReference type) {
		type.simpleNames.join('$') + type.typeArguments.join('$', '$', '', [simpleMethodName])
	}
	
	private def StringConcatenationClient createBindingMethod(GuiceModuleAccess.Binding it) '''
		«IF !value.provider && value.statements.isEmpty»
			// contributed by «contributedBy»
			«IF key.singleton»@«SingletonBinding»«IF key.eagerSingleton»(eager=true)«ENDIF»«ENDIF»
			public «IF value.expression === null»Class<? extends «key.type»>«ELSE»«key.type»«ENDIF» «bindMethodName»() {
				return «IF value.expression !== null»«value.expression»«ELSEIF value.type !== null»«value.type».class«ELSE»null«ENDIF»;
			}
		«ELSEIF value.statements.isEmpty»
			// contributed by «contributedBy»
			«IF key.singleton»@«SingletonBinding»«IF key.eagerSingleton»(eager=true)«ENDIF»«ENDIF»
			public «IF value.expression===null»Class<? extends «Provider»<? extends «key.type»>>«ELSE»«Provider»<? extends «key.type»>«ENDIF» «bindMethodName»() {
				return «IF value.expression!==null»«value.expression»«ELSE»«value.type».class«ENDIF»;
			}
		«ELSE»
			// contributed by «contributedBy»
			public void «bindMethodName»(«Binder» binder) {
				«FOR statement : value.statements»
					«statement»
				«ENDFOR»
			}
		«ENDIF»
	'''
	
	def JavaFileAccess createRuntimeModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		if (codeConfig.preferXtendStubs) {
			return fileAccessFactory.createXtendFile(runtimeModule,'''
				/**
				 * Use this class to register components to be used at runtime / without the Equinox extension registry.
				 */
				class «runtimeModule.simpleName» extends «runtimeGenModule» {
				}
			''')
		} else {
			return fileAccessFactory.createJavaFile(runtimeModule,'''
				/**
				 * Use this class to register components to be used at runtime / without the Equinox extension registry.
				 */
				public class «runtimeModule.simpleName» extends «runtimeGenModule» {
				}
			''')
		}
	}
	
	def JavaFileAccess createRuntimeGenModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		val superClass = langConfig.runtimeGenModule.superClass ?: runtimeDefaultModule
		val file = fileAccessFactory.createGeneratedJavaFile(runtimeGenModule)
		file.importNestedTypeThreshold = JavaFileAccess.DONT_IMPORT_NESTED_TYPES
		
		file.typeComment = '''
			/**
			 * Manual modifications go to {@link «runtimeModule.simpleName»}.
			 */
		'''
		file.annotations += new SuppressWarningsAnnotation
		file.content = '''
			public abstract class «runtimeGenModule.simpleName» extends «superClass» {
			
				protected «Properties» properties = null;
			
				@Override
				public void configure(«Binder» binder) {
					properties = tryBindProperties(binder, "«langConfig.grammar.name.replaceAll("\\.","/")».properties");
					super.configure(binder);
				}
				
				public void configureLanguageName(«Binder» binder) {
					binder.bind(String.class).annotatedWith(«Names».named(«Constants».LANGUAGE_NAME)).toInstance("«langConfig.grammar.name»");
				}
				
				public void configureFileExtensions(«Binder» binder) {
					if (properties == null || properties.getProperty(Constants.FILE_EXTENSIONS) == null)
						binder.bind(String.class).annotatedWith(«Names».named(«Constants».FILE_EXTENSIONS)).toInstance("«langConfig.fileExtensions.join(',')»");
				}
				
				«FOR binding : langConfig.runtimeGenModule.bindings»
					«binding.createBindingMethod»
					
				«ENDFOR»
			}
		'''
		file.markedAsGenerated = true
		return file
	}
	
	
	def JavaFileAccess createIdeModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		if (codeConfig.preferXtendStubs) {
			return fileAccessFactory.createXtendFile(genericIdeModule,'''
				/**
				 * Use this class to register ide components.
				 */
				class «genericIdeModule.simpleName» extends «genericIdeGenModule» {
				}
			''')
		} else {
			return fileAccessFactory.createJavaFile(genericIdeModule,'''
				/**
				 * Use this class to register ide components.
				 */
				public class «genericIdeModule.simpleName» extends «genericIdeGenModule» {
				}
			''')
		}
	}
	
	def JavaFileAccess createIdeGenModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		val superClass = langConfig.ideGenModule.superClass ?: genericIdeDefaultModule
		val file = fileAccessFactory.createGeneratedJavaFile(genericIdeGenModule)
		file.importNestedTypeThreshold = JavaFileAccess.DONT_IMPORT_NESTED_TYPES
		
		file.typeComment = '''
			/**
			 * Manual modifications go to {@link «genericIdeModule.simpleName»}.
			 */
		'''
		file.annotations += new SuppressWarningsAnnotation
		file.content = '''
			public abstract class «genericIdeGenModule.simpleName» extends «superClass» {
			
				«FOR binding : langConfig.ideGenModule.bindings»
					«binding.createBindingMethod»
					
				«ENDFOR»
			}
		'''
		file.markedAsGenerated = true
		return file
	}
	
	def JavaFileAccess createIdeSetup(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		if (codeConfig.preferXtendStubs) {
			return fileAccessFactory.createXtendFile(genericIdeSetup,'''
				/**
				 * Initialization support for running Xtext languages as language servers.
				 */
				class «genericIdeSetup.simpleName» extends «runtimeSetup» {
				
					override createInjector() {
						«Guice».createInjector(«Modules2».mixin(new «runtimeModule», new «genericIdeModule»))
					}
					
				}
		 	''')
		} else {
			return fileAccessFactory.createJavaFile(genericIdeSetup,'''
				/**
				 * Initialization support for running Xtext languages as language servers.
				 */
				public class «genericIdeSetup.simpleName» extends «runtimeSetup» {
				
					@Override
					public «Injector» createInjector() {
						return «Guice».createInjector(«Modules2».mixin(new «runtimeModule»(), new «genericIdeModule»()));
					}
					
				}
		 	''')
		}
	}
	
	
	def JavaFileAccess createEclipsePluginModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		if (codeConfig.preferXtendStubs) {
			return fileAccessFactory.createXtendFile(eclipsePluginModule,'''
				/**
				 * Use this class to register components to be used within the Eclipse IDE.
				 */
				@«FinalFieldsConstructor»
				class «eclipsePluginModule.simpleName» extends «eclipsePluginGenModule» {
				}
			''')
		} else {
			return fileAccessFactory.createJavaFile(eclipsePluginModule,'''
				/**
				 * Use this class to register components to be used within the Eclipse IDE.
				 */
				public class «eclipsePluginModule.simpleName» extends «eclipsePluginGenModule» {

					public «eclipsePluginModule.simpleName»(«'org.eclipse.ui.plugin.AbstractUIPlugin'.typeRef» plugin) {
						super(plugin);
					}
				}
			''')
		}
	}
	
	def JavaFileAccess createEclipsePluginGenModule(IXtextGeneratorLanguage langConfig) {
		val it = langConfig.grammar
		val superClass = langConfig.eclipsePluginGenModule.superClass ?: eclipsePluginDefaultModule
		val file = fileAccessFactory.createGeneratedJavaFile(eclipsePluginGenModule)
		file.importNestedTypeThreshold = JavaFileAccess.DONT_IMPORT_NESTED_TYPES
		
		file.typeComment = '''
			/**
			 * Manual modifications go to {@link «eclipsePluginModule.simpleName»}.
			 */
		'''
		file.annotations += new SuppressWarningsAnnotation
		file.content = '''
			public abstract class «eclipsePluginGenModule.simpleName» extends «superClass» {
			
				public «eclipsePluginGenModule.simpleName»(«'org.eclipse.ui.plugin.AbstractUIPlugin'.typeRef» plugin) {
					super(plugin);
				}
				
				«FOR binding : langConfig.eclipsePluginGenModule.bindings»
					«binding.createBindingMethod»
					
				«ENDFOR»
			}
		'''
		file.markedAsGenerated = true
		return file
	}

	def JavaFileAccess createEclipsePluginExecutableExtensionFactory(IXtextGeneratorLanguage langConfig, IXtextGeneratorLanguage activatorLanguage) {
		val grammar = langConfig.grammar
		
		val file = fileAccessFactory.createGeneratedJavaFile(grammar.eclipsePluginExecutableExtensionFactory)
		file.typeComment = '''
			/**
			 * This class was generated. Customizations should only happen in a newly
			 * introduced subclass. 
			 */
		'''
		file.content = '''
			public class «grammar.eclipsePluginExecutableExtensionFactory.simpleName» extends «'org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory'.typeRef» {
			
				@Override
				protected «'org.osgi.framework.Bundle'.typeRef» getBundle() {
					return «'org.osgi.framework.FrameworkUtil'.typeRef».getBundle(«eclipsePluginActivator».class);
				}
				
				@Override
				protected «Injector» getInjector() {
					«eclipsePluginActivator» activator = «eclipsePluginActivator».getInstance();
					return activator != null ? activator.getInjector(«eclipsePluginActivator».«langConfig.grammar.name.toUpperCase.replaceAll('\\.', '_')») : null;
				}
			
			}
		'''
		return file
	}
	
	def JavaFileAccess createEclipsePluginActivator(IXtextProjectConfig projectConfig, List<? extends IXtextGeneratorLanguage> langConfigs) {
		val activator = eclipsePluginActivator
		val file = fileAccessFactory.createGeneratedJavaFile(activator)
		
		file.typeComment = '''
			/**
			 * This class was generated. Customizations should only happen in a newly
			 * introduced subclass. 
			 */
		'''
		file.content = '''
			public class «activator.simpleName» extends «'org.eclipse.ui.plugin.AbstractUIPlugin'.typeRef» {
			
				public static final String PLUGIN_ID = "«projectConfig.eclipsePlugin.name»";
				«FOR lang : langConfigs»
					public static final String «lang.grammar.name.toUpperCase.replaceAll('\\.', '_')» = "«lang.grammar.name»";
				«ENDFOR»
				
				private static final «Logger» logger = «Logger».getLogger(«activator.simpleName».class);
				
				private static «activator.simpleName» INSTANCE;
				
				private «Map»<String, «Injector»> injectors = «Collections».synchronizedMap(new «HashMap»<>(2));
				
				@Override
				public void start(«'org.osgi.framework.BundleContext'.typeRef» context) throws Exception {
					super.start(context);
					INSTANCE = this;
				}
				
				@Override
				public void stop(«'org.osgi.framework.BundleContext'.typeRef» context) throws Exception {
					injectors.clear();
					INSTANCE = null;
					super.stop(context);
				}
				
				public static «activator.simpleName» getInstance() {
					return INSTANCE;
				}
				
				public «Injector» getInjector(String language) {
					synchronized (injectors) {
						«Injector» injector = injectors.get(language);
						if (injector == null) {
							injectors.put(language, injector = createInjector(language));
						}
						return injector;
					}
				}
				
				protected «Injector» createInjector(String language) {
					try {
						«Module» runtimeModule = getRuntimeModule(language);
						«Module» sharedStateModule = getSharedStateModule();
						«Module» uiModule = getUiModule(language);
						«Module» mergedModule = «Modules2».mixin(runtimeModule, sharedStateModule, uiModule);
						return «Guice».createInjector(mergedModule);
					} catch (Exception e) {
						logger.error("Failed to create injector for " + language);
						logger.error(e.getMessage(), e);
						throw new RuntimeException("Failed to create injector for " + language, e);
					}
				}
				
				protected «Module» getRuntimeModule(String grammar) {
					«FOR lang : langConfigs»
						if («lang.grammar.name.toUpperCase.replaceAll('\\.', '_')».equals(grammar)) {
							return new «getRuntimeModule(lang.grammar)»();
						}
					«ENDFOR»
					throw new IllegalArgumentException(grammar);
				}
				
				protected «Module» getUiModule(String grammar) {
					«FOR lang : langConfigs»
						if («lang.grammar.name.toUpperCase.replaceAll('\\.', '_')».equals(grammar)) {
							return new «getEclipsePluginModule(lang.grammar)»(this);
						}
					«ENDFOR»
					throw new IllegalArgumentException(grammar);
				}
				
				protected «Module» getSharedStateModule() {
					return new «'org.eclipse.xtext.ui.shared.SharedStateModule'.typeRef»();
				}
				
				
			}
		'''
		return file
	}
	
	def TextFileAccess createMetaInfServices(List<? extends IXtextGeneratorLanguage> languageConfigs) {
		val file = new TextFileAccess()
		file.path = "META-INF/services/org.eclipse.xtext.ISetup"
		file.content = '''
			«FOR lang : languageConfigs»
				«naming.getGenericIdeSetup(lang.grammar)»
			«ENDFOR»
		'''
		file
	}
	
}
