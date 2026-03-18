/*******************************************************************************
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard

import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.util.JUnitVersion
import org.eclipse.xtext.util.Strings
import org.eclipse.xtext.xtext.wizard.ecore2xtext.Ecore2XtextGrammarCreator

import static org.eclipse.xtext.xtext.wizard.ExternalDependency.*

/**
 * @author Dennis Huebner - Initial contribution and API
 * @author Sven Efftinge
 * @author Miro Spoenemann
 * @author Stefan Oehme
 * @author Christian Schneider
 * @author Moritz Eysholdt
 * @author Lorenzo Bettini - bug#494176
 * @author Karsten Thoms - bug#484500, issue#108
 * @since 2.9
 */
class RuntimeProjectDescriptor extends TestedProjectDescriptor {

	val grammarCreator = new Ecore2XtextGrammarCreator
	val RuntimeTestProjectDescriptor testProject
	@Accessors
	var boolean withPluginXml = true
	
	new(WizardConfiguration config) {
		super(config)
		enabled = true
		testProject = new RuntimeTestProjectDescriptor(this)
	}
	
	override setEnabled(boolean enabled) {
		if (!enabled)
			throw new IllegalArgumentException("The runtime project is always enabled")
		super.enabled = enabled
	}
	
	String nameQualifier = ''
	
	
	override getNameQualifier() {
		nameQualifier
	}
	
	def void setNameQualifier(String nameQualifier) {
		this.nameQualifier = nameQualifier
	}
	
	override isEclipsePluginProject() {
		config.preferredBuildSystem == BuildSystem.NONE || config.uiProject.enabled
	}
	
	override isPartOfGradleBuild() {
		true
	}
	
	override isPartOfMavenBuild() {
		true
	}
	
	override getTestProject() {
		testProject
	}
	
	override getExternalDependencies() {
		val deps = newLinkedHashSet
		deps += super.externalDependencies
		deps += createXtextDependency("org.eclipse.xtext")
		deps += createXtextDependency("org.eclipse.xtext.xbase")
		deps += new ExternalDependency => [
			p2 [
				bundleId = "org.eclipse.equinox.common"
				version = "3.19.0"
			]
		]
		if (!isEclipsePluginProject && config.needsMavenBuild) {
			deps += createXtextDependency("org.eclipse.xtext.xtext.generator")  => [maven.optional = true]
			deps += new ExternalDependency => [
				maven [
					groupId = "org.eclipse.emf"
					artifactId = "org.eclipse.emf.mwe2.launch"
					optional = true
				]
			]
			deps += new ExternalDependency => [
				maven [
					groupId = "org.eclipse.xtext"
					artifactId = "xtext-antlr-generator"
					optional = true
				]
			]
		}
		deps
	}
	
	override getDevelopmentBundles() {
		val result = newLinkedHashSet(
			"org.eclipse.xtext.xtext.generator.dependencies"
		)
		if (isFromExistingEcoreModels) {
			if (config.ecore2Xtext.EPackageInfos.exists[genmodelURI.fileExtension == "xcore"]) {
				result.add("org.eclipse.emf.ecore.xcore")
			}
		}
		return result
	}
	
	override getBinIncludes() {
		val includes = newLinkedHashSet
		includes += super.binIncludes
		if (withPluginXml)
			includes += "plugin.xml"
		includes
	}

	override getFiles() {
		val files = newArrayList
		files += super.files
		files += grammarFile
		files += file(Outlet.MAIN_JAVA, workflowFilePath, workflow)
		files += workflowLaunchConfigFile
		if (config.runtimeProject.isEclipsePluginProject) {
			files += launchConfigFile
		}
		if (isPlainMavenBuild) {
			files += file(Outlet.ROOT, "jar-with-ecore-model.xml", jarDescriptor)
		}
		return files
	}
	
	private def isPlainMavenBuild() {
		config.needsMavenBuild && !isEclipsePluginProject
	}

	def getGrammarFile() {
		file(Outlet.MAIN_JAVA, grammarFilePath, grammar)
	}
	
	def String getGrammarFilePath() {
		return '''«config.language.basePackagePath»/«config.language.simpleName».xtext'''
	}
	

	def grammar() {
		if (fromExistingEcoreModels)
			grammarCreator.grammar(config)
		else
			defaultGrammar
	}
	
	private def defaultGrammar() '''
		grammar «config.language.name» with org.eclipse.xtext.common.Terminals
		
		generate «config.language.simpleName.toFirstLower» "«config.language.nsURI»"
		
		Model:
			greetings+=Greeting*;
			
		Greeting:
			'Hello' name=ID '!';
	'''
	
	def String getWorkflowFilePath() {
		'''«config.language.basePackagePath»/Generate«config.language.simpleName».mwe2'''
	}
	
	def workflow() {
		''' 
			module «(config.language.basePackagePath+"/Generate"+config.language.simpleName).replaceAll("/", ".")»
			
			import org.eclipse.xtext.xtext.generator.*
			import org.eclipse.xtext.xtext.generator.model.project.*
			
			var rootPath = ".."
			
			Workflow {
				
				component = XtextGenerator {
					configuration = {
						project = StandardProjectConfig {
							baseName = "«name»"
							rootPath = rootPath
							«IF testProject.enabled»
								runtimeTest = {
									enabled = true
								}
							«ENDIF»
							«IF config.ideProject.enabled 
								&& !#[config.webProject, config.uiProject].exists[enabled]»
								genericIde = {
									enabled = true
								}
							«ENDIF»
							«IF config.uiProject.enabled»
								eclipsePlugin = {
									enabled = true
								}
							«ENDIF»
							«IF config.uiProject.testProject.enabled»
								eclipsePluginTest = {
									enabled = true
								}
							«ENDIF»
							«IF config.webProject.enabled»
								web = {
									enabled = true
								}
							«ENDIF»
							«IF config.sourceLayout == SourceLayout.MAVEN»
								mavenLayout = true
							«ENDIF»
							«IF isEclipsePluginProject»
								createEclipseMetaData = true
							«ENDIF»
						}
						code = {
							encoding = "«config.encoding»"
							lineDelimiter = "«Strings.convertToJavaString(config.lineDelimiter)»"
							fileHeader = "/*\n * generated by Xtext \${version}\n */"
							preferXtendStubs = false
						}
					}
					language = StandardLanguage {
						name = "«config.language.name»"
						fileExtensions = "«config.language.fileExtensions»"
						«IF !config.ecore2Xtext.EPackageInfos.empty»
							«FOR genmodelURI : config.ecore2Xtext.EPackageInfos.map[genmodelURI.toString].toSet»
								referencedResource = "«genmodelURI»"
							«ENDFOR»
						«ENDIF»
						«IF fromExistingEcoreModels»
							
							fragment = ecore2xtext.Ecore2XtextValueConverterServiceFragment2 auto-inject {}

							formatter = {
								generateStub = true
								generateXtendStub = true
							}
						«ENDIF»

						serializer = {
							generateStub = false
						}
						validator = {
							// composedCheck = "org.eclipse.xtext.validation.NamesAreUniqueValidator"
			
							// Enables several checks on the inferred Jvm model for Xbase languages concerning Java inheritance relations
							// composedCheck = "org.eclipse.xtext.xbase.validation.JvmGenericTypeValidator"
			
							// Generates checks for @Deprecated grammar annotations, an IssueProvider and a corresponding PropertyPage
							generateDeprecationValidation = true
						}
						generator = {
							generateXtendStub = true
						}
						«IF config.junitVersion == JUnitVersion.JUNIT_6»
							junitSupport = {
								junitVersion = "6"
							}
						«ENDIF»
					}
				}
			}
		'''
	}
	
	override buildGradle() {
		super.buildGradle => [
			additionalContent = '''
				configurations {
					mwe2 {
						extendsFrom compileClasspath
					}
				}

				dependencies {
					mwe2 'org.eclipse.emf:org.eclipse.emf.mwe2.launch'
					mwe2 "org.eclipse.xtext:org.eclipse.xtext.common.types:${xtextVersion}"
					mwe2 "org.eclipse.xtext:org.eclipse.xtext.xtext.generator:${xtextVersion}"
					mwe2 'org.eclipse.xtext:xtext-antlr-generator'
				}
				
				task generateXtextLanguage(type: JavaExec) {
					mainClass = 'org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher'
					classpath = configurations.mwe2
					inputs.file "«Outlet.MAIN_JAVA.sourceFolder»/«workflowFilePath»"
					inputs.file "«Outlet.MAIN_JAVA.sourceFolder»/«grammarFilePath»"
					outputs.dir "«Outlet.MAIN_SRC_GEN.sourceFolder»"
					args += "«Outlet.MAIN_JAVA.sourceFolder»/«workflowFilePath»"
					args += "-p"
					args += "rootPath=/${projectDir}/.."
				}
				
				«IF testProject.inlined»
					«testProject.buildGradle.additionalContent»
				«ENDIF»
				processResources.dependsOn(generateXtextLanguage)
				generateXtext.dependsOn(generateXtextLanguage)
				clean.dependsOn(cleanGenerateXtextLanguage)
				eclipse.classpath.plusConfigurations += [configurations.mwe2]
			'''
		]
	}
		
	override pom() {
		super.pom => [
			packaging = if (isEclipsePluginProject) "eclipse-plugin" else "jar"
			buildSection = '''
				<build>
					«IF !isEclipsePluginProject && config.sourceLayout == SourceLayout.PLAIN»
						<sourceDirectory>«Outlet.MAIN_JAVA.sourceFolder»</sourceDirectory>
						<resources>
							<resource>
								<directory>«Outlet.MAIN_RESOURCES.sourceFolder»</directory>
								<excludes>
									<exclude>**/*.java</exclude>
									<exclude>**/*.xtend</exclude>
									<exclude>**/*.xtext</exclude>
									<exclude>**/*.mwe2</exclude>
								</excludes>
							</resource>
						</resources>
					«ENDIF»
					<plugins>
						<plugin>
							<groupId>org.codehaus.mojo</groupId>
							<artifactId>exec-maven-plugin</artifactId>
							<version>3.0.0</version>
							<executions>
								<execution>
									<?m2e ignore?>
									<id>mwe2Launcher</id>
									<phase>generate-sources</phase>
									<goals>
										<goal>java</goal>
									</goals>
								</execution>
							</executions>
							<configuration>
								<mainClass>org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher</mainClass>
								<arguments>
									<argument>/${project.basedir}/«Outlet.MAIN_JAVA.sourceFolder»/«workflowFilePath»</argument>
									<argument>-p</argument>
									<argument>rootPath=/${project.basedir}/..</argument>
								</arguments>
								<classpathScope>compile</classpathScope>
								<includePluginDependencies>true</includePluginDependencies>
								<cleanupDaemonThreads>false</cleanupDaemonThreads><!-- see https://bugs.eclipse.org/bugs/show_bug.cgi?id=475098#c3 -->
							</configuration>
							«IF config.needsTychoBuild»
								<dependencies>
									<dependency>
										<groupId>org.eclipse.xtext</groupId>
										<artifactId>org.eclipse.xtext.xtext.generator.dependencies</artifactId>
										<version>${xtextVersion}</version>
									</dependency>
								</dependencies>
							«ENDIF»
						</plugin>
						<plugin>
							<groupId>org.eclipse.xtext</groupId>
							<artifactId>xtend-maven-plugin</artifactId>
						</plugin>

						<plugin>
							<groupId>org.apache.maven.plugins</groupId>
							<artifactId>maven-clean-plugin</artifactId>
							<configuration>
								<filesets combine.children="append">
									«FOR p : #[this, config.ideProject, config.uiProject, config.webProject]»
										«IF p.enabled»
											<fileset>
												<directory>${basedir}/../«p.name»/«Outlet.MAIN_SRC_GEN.sourceFolder»/</directory>
												<includes>
													<include>**/*</include>
												</includes>
											</fileset>
											«IF p instanceof TestedProjectDescriptor»
												«IF p.testProject.enabled»
													<fileset>
														<directory>${basedir}/../«if(p.testProject.isInlined) p.name else p.testProject.name»/«Outlet.TEST_SRC_GEN.sourceFolder»/</directory>
														<includes>
															<include>**/*</include>
														</includes>
													</fileset>
												«ENDIF»
											«ENDIF»
										«ENDIF»
									«ENDFOR»
									<fileset>
										<directory>${basedir}/model/generated/</directory>
									</fileset>
								</filesets>
							</configuration>
						</plugin>
						«IF !isEclipsePluginProject»
							<plugin>
								<groupId>org.codehaus.mojo</groupId>
								<artifactId>build-helper-maven-plugin</artifactId>
								<version>3.3.0</version>
								<executions>
									<execution>
										<?m2e ignore?>
										<id>add-source</id>
										<phase>initialize</phase>
										<goals>
											<goal>add-source</goal>
											<goal>add-resource</goal>
										</goals>
										<configuration>
											<sources>
												<source>«Outlet.MAIN_SRC_GEN.sourceFolder»</source>
											</sources>
											<resources>
												<resource>
													<directory>«Outlet.MAIN_SRC_GEN.sourceFolder»</directory>
													<excludes>
														<exclude>**/*.java</exclude>
														<exclude>**/*.g</exclude>
													</excludes>
												</resource>
											</resources>
										</configuration>
									</execution>
									«IF testProject.isInlined»
										<execution>
											<id>add-test-source</id>
											<phase>initialize</phase>
											<goals>
												<goal>add-test-source</goal>
												<goal>add-test-resource</goal>
											</goals>
											<configuration>
												<sources>
													<source>«Outlet.TEST_SRC_GEN.sourceFolder»</source>
												</sources>
												<resources>
													<resource>
														<directory>«Outlet.TEST_SRC_GEN.sourceFolder»</directory>
														<excludes>
															<exclude>**/*.java</exclude>
														</excludes>
													</resource>
												</resources>
											</configuration>
										</execution>
									«ENDIF»	
								</executions>
							</plugin>
							«IF isPlainMavenBuild»
								<plugin>
									<artifactId>maven-assembly-plugin</artifactId>
									<version>3.4.1</version>
									<configuration>
										<descriptors>
											<descriptor>jar-with-ecore-model.xml</descriptor>
										</descriptors>
										<appendAssemblyId>false</appendAssemblyId>
									</configuration>
									<executions>
										<execution>
											<id>make-assembly</id>
											<phase>package</phase>
											<goals>
												<goal>single</goal>
											</goals>
										</execution>
									</executions>
								</plugin>
							«ENDIF»
						«ENDIF»
					</plugins>
				</build>
			'''
		]
	}
	
	def jarDescriptor() '''
		<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0" 
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
			<id>jar-with-ecore-model</id>
			<formats>
				<format>jar</format>
			</formats>
			<includeBaseDirectory>false</includeBaseDirectory>
			<fileSets>
				<fileSet>
					<outputDirectory>/</outputDirectory>
					<directory>target/classes</directory>
				</fileSet>
				<fileSet>
					<outputDirectory>model/generated</outputDirectory>
					<directory>model/generated</directory>
				</fileSet>
			</fileSets>
		</assembly>
	'''
	
	/**
	 * @since 2.11
	 */
	override getBinExcludes() {
		#{"**/*.xtend","**/*.mwe2"}
	}
	
	/**
	 * @since 2.11
	 */
	def private getWorkflowLaunchConfigFile() {
		file(Outlet.ROOT, '''.launch/Generate «config.language.simpleName» («config.language.fileExtensions.head») Language Infrastructure.launch'''.toString, workflowLaunchConfig)
	}

	/**
	 * @since 2.11
	 */
	def private workflowLaunchConfig() {
		var projectsToRefresh = newArrayList
		
		projectsToRefresh += config.enabledProjects
		if (config.runtimeProject.testProject.enabled)
			projectsToRefresh += config.runtimeProject.testProject
		if (config.uiProject.testProject.enabled)
			projectsToRefresh += config.uiProject.testProject
		
		val refreshAttr = '''${working_set:&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&#10;&lt;resources&gt;&#10;«FOR it: projectsToRefresh»&lt;item path=&quot;/«name»&quot; type=&quot;4&quot;/&gt;&#10;«ENDFOR»;&lt;/resources&gt;}'''

		'''
			<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			«IF config.runtimeProject.isEclipsePluginProject»
			<launchConfiguration type="org.eclipse.emf.mwe2.launch.Mwe2LaunchConfigurationType">
			<listAttribute key="org.eclipse.debug.core.MAPPED_RESOURCE_PATHS">
			<listEntry value="/«config.runtimeProject.name»"/>
			</listAttribute>
			<listAttribute key="org.eclipse.debug.core.MAPPED_RESOURCE_TYPES">
			<listEntry value="4"/>
			</listAttribute>
			<listAttribute key="org.eclipse.debug.ui.favoriteGroups">
			<listEntry value="org.eclipse.debug.ui.launchGroup.debug"/>
			<listEntry value="org.eclipse.debug.ui.launchGroup.run"/>
			</listAttribute>
			<stringAttribute key="org.eclipse.debug.core.ATTR_REFRESH_SCOPE" value="«refreshAttr»"/>
			<stringAttribute key="org.eclipse.jdt.launching.MAIN_TYPE" value="org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher"/>
			<stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="«(config.language.basePackagePath+"/Generate"+config.language.simpleName).replaceAll("/", ".")»"/>
			<stringAttribute key="org.eclipse.jdt.launching.PROJECT_ATTR" value="«config.runtimeProject.name»"/>
			<stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value="-Xmx512m"/>
			</launchConfiguration>
			«ELSEIF config.preferredBuildSystem == BuildSystem.MAVEN»
			<launchConfiguration type="org.eclipse.m2e.Maven2LaunchConfigurationType">
			<booleanAttribute key="M2_DEBUG_OUTPUT" value="false"/>
			<stringAttribute key="M2_GOALS" value="clean generate-sources"/>
			<booleanAttribute key="M2_NON_RECURSIVE" value="false"/>
			<booleanAttribute key="M2_OFFLINE" value="false"/>
			<stringAttribute key="M2_PROFILES" value=""/>
			<listAttribute key="M2_PROPERTIES"/>
			<stringAttribute key="M2_RUNTIME" value="EMBEDDED"/>
			<booleanAttribute key="M2_SKIP_TESTS" value="false"/>
			<intAttribute key="M2_THREADS" value="1"/>
			<booleanAttribute key="M2_UPDATE_SNAPSHOTS" value="false"/>
			<stringAttribute key="M2_USER_SETTINGS" value=""/>
			<booleanAttribute key="M2_WORKSPACE_RESOLUTION" value="true"/>
			<stringAttribute key="org.eclipse.debug.core.ATTR_REFRESH_SCOPE" value="«refreshAttr»"/>
			<booleanAttribute key="org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD" value="true"/>
			<stringAttribute key="org.eclipse.jdt.launching.WORKING_DIRECTORY" value="${workspace_loc:/«config.runtimeProject.name»}"/>
			</launchConfiguration>
			«ELSEIF config.preferredBuildSystem == BuildSystem.GRADLE»
			<launchConfiguration type="org.eclipse.buildship.core.launch.runconfiguration">
			<listAttribute key="arguments"/>
			<stringAttribute key="gradle_distribution" value="GRADLE_DISTRIBUTION(WRAPPER)"/>
			<listAttribute key="jvm_arguments"/>
			<booleanAttribute key="show_console_view" value="true"/>
			<booleanAttribute key="show_execution_view" value="true"/>
			<listAttribute key="tasks">
			<listEntry value="build"/>
			</listAttribute>
			<stringAttribute key="org.eclipse.debug.core.ATTR_REFRESH_SCOPE" value="«refreshAttr»"/>
			<booleanAttribute key="org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD" value="true"/>
			<stringAttribute key="org.eclipse.jdt.launching.WORKING_DIRECTORY" value="${workspace_loc:/«config.runtimeProject.name»}"/>
			</launchConfiguration>
			«ENDIF»
		'''
	}

	/**
	 * @since 2.11
	 */
	def private getLaunchConfigFile() {
		file(Outlet.ROOT, ".launch/Launch Runtime Eclipse.launch", launchConfig)
	}

	/**
	 * @since 2.11
	 */
	def private launchConfig() {
		'''
			<?xml version="1.0" encoding="UTF-8" standalone="no"?>
			<launchConfiguration type="org.eclipse.pde.ui.RuntimeWorkbench">
			<booleanAttribute key="append.args" value="true"/>
			<booleanAttribute key="askclear" value="true"/>
			<booleanAttribute key="automaticAdd" value="true"/>
			<booleanAttribute key="automaticValidate" value="false"/>
			<stringAttribute key="bad_container_name" value="/«config.runtimeProject.name»/.launch/"/>
			<stringAttribute key="bootstrap" value=""/>
			<stringAttribute key="checked" value="[NONE]"/>
			<booleanAttribute key="clearConfig" value="true"/>
			<booleanAttribute key="clearws" value="false"/>
			<booleanAttribute key="clearwslog" value="false"/>
			<stringAttribute key="configLocation" value="${workspace_loc}/.metadata/.plugins/org.eclipse.pde.core/Launch Runtime Eclipse"/>
			<booleanAttribute key="default" value="true"/>
			<booleanAttribute key="includeOptional" value="true"/>
			<stringAttribute key="location" value="${workspace_loc}/../runtime-EclipseXtext"/>
			<listAttribute key="org.eclipse.debug.ui.favoriteGroups">
			<listEntry value="org.eclipse.debug.ui.launchGroup.debug"/>
			<listEntry value="org.eclipse.debug.ui.launchGroup.run"/>
			</listAttribute>
			<stringAttribute key="org.eclipse.jdt.launching.JRE_CONTAINER" value="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/«config.javaVersion.bree»"/>
			<stringAttribute key="org.eclipse.jdt.launching.PROGRAM_ARGUMENTS" value="-os ${target.os} -ws ${target.ws} -arch ${target.arch} -nl ${target.nl}"/>
			<stringAttribute key="org.eclipse.jdt.launching.SOURCE_PATH_PROVIDER" value="org.eclipse.pde.ui.workbenchClasspathProvider"/>
			<stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value="-Xms40m -Xmx512m"/>
			<stringAttribute key="pde.version" value="3.3"/>
			<stringAttribute key="product" value="org.eclipse.platform.ide"/>
			<booleanAttribute key="show_selected_only" value="false"/>
			<stringAttribute key="templateConfig" value="${target_home}/configuration/config.ini"/>
			<booleanAttribute key="tracing" value="false"/>
			<booleanAttribute key="useDefaultConfig" value="true"/>
			<booleanAttribute key="useDefaultConfigArea" value="true"/>
			<booleanAttribute key="useProduct" value="true"/>
			<booleanAttribute key="usefeatures" value="false"/>
			</launchConfiguration>
		'''
	}
		
}
