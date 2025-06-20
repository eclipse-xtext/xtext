/*******************************************************************************
 * Copyright (c) 2015, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

import static org.eclipse.xtext.xtext.wizard.ExternalDependency.*

@FinalFieldsConstructor
class WebProjectDescriptor extends ProjectDescriptor {
	
	static val REQUIREJS_VERSION = '2.3.7'
//	static val REQUIREJS_TEXT_VERSION = '2.0.15'
	static val JQUERY_VERSION = '3.6.0'
	static val JETTY_VERSION = '11.0.25'
	static val SLF4J_VERSION = '2.0.5'
	static val ACE_VERSION = '1.3.3'
	
	override getUpstreamProjects() {
		newLinkedHashSet(config.runtimeProject, config.ideProject)
	}
	
	override getNameQualifier() {
		".web"
	}
	
	override isEclipsePluginProject() {
		false
	}
	
	override isPartOfGradleBuild() {
		config.preferredBuildSystem == BuildSystem.GRADLE
	}
	
	override isPartOfMavenBuild() {
		config.preferredBuildSystem == BuildSystem.MAVEN
	}
	
	override getExternalDependencies() {
		val deps = newLinkedHashSet
		deps += super.externalDependencies
		deps += createXtextDependency("org.eclipse.xtext.xbase.web")
		deps += createXtextDependency("org.eclipse.xtext.web.servlet")
		deps += createMavenDependency("org.eclipse.xtext:org.eclipse.xtend.lib:${xtextVersion}")
		deps += createMavenDependency("org.webjars:requirejs:" + REQUIREJS_VERSION)
		// RequireJS-Text is only needed for Orion
//		deps += createMavenDependency("org.webjars:requirejs-text:" + REQUIREJS_TEXT_VERSION)
		deps += createMavenDependency("org.webjars:jquery:" + JQUERY_VERSION)
		deps += createMavenDependency("org.webjars:ace:" + ACE_VERSION)
		deps += createMavenDependency("org.eclipse.jetty:jetty-annotations:" + JETTY_VERSION) => [maven.scope = Scope.PROVIDED]
		deps += createMavenDependency("org.slf4j:slf4j-simple:" + SLF4J_VERSION) => [maven.scope = Scope.PROVIDED]
		return deps
	}
	
	override buildGradle() {
		super.buildGradle => [
			pluginsSection = '''
				plugins {
					id 'war'
				}
			'''
			additionalContent = '''
				task jettyRun(type:JavaExec) {
					dependsOn(sourceSets.main.runtimeClasspath)
					classpath = sourceSets.main.runtimeClasspath.filter{it.exists()}
					mainClass = '«config.language.basePackage».web.ServerLauncher'
					standardInput = System.in
					group = 'run'
					description = 'Starts an example Jetty server with your language'
				}
			'''
		]
	}

	override pom() {
		super.pom => [
			packaging = "war"
			buildSection = '''
				<build>
					«IF config.sourceLayout == SourceLayout.PLAIN»
						<sourceDirectory>«Outlet.MAIN_JAVA.sourceFolder»</sourceDirectory>
						<resources>
							<resource>
								<directory>«Outlet.MAIN_RESOURCES.sourceFolder»</directory>
								<excludes>
									<exclude>**/*.java</exclude>
									<exclude>**/*.xtend</exclude>
								</excludes>
							</resource>
						</resources>
					«ENDIF»
					<plugins>
						<plugin>
							<groupId>org.eclipse.xtext</groupId>
							<artifactId>xtend-maven-plugin</artifactId>
						</plugin>
						<plugin>
							<artifactId>maven-war-plugin</artifactId>
							<version>3.4.0</version>
							<configuration>
								<warSourceDirectory>«Outlet.WEBAPP.sourceFolder»</warSourceDirectory>
								<failOnMissingWebXml>false</failOnMissingWebXml>
							</configuration>
						</plugin>
						<plugin>
							<groupId>org.eclipse.jetty</groupId>
							<artifactId>jetty-maven-plugin</artifactId>
							<version>«JETTY_VERSION»</version>
							<configuration>
								<webAppSourceDirectory>«Outlet.WEBAPP.sourceFolder»</webAppSourceDirectory>
							</configuration>
						</plugin>
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
												</excludes>
											</resource>
										</resources>
									</configuration>
								</execution>
							</executions>
						</plugin>
					</plugins>
				</build>
			'''
		]
	}	
}
