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
import com.google.common.collect.Sets

abstract class TestProjectDescriptor extends ProjectDescriptor {
	@Accessors val ProjectDescriptor testedProject
	
	new(TestedProjectDescriptor testedProject) {
		super(testedProject.config)
		this.testedProject = testedProject
	}
	
	override getSourceFolders() {
		#[Outlet.TEST_JAVA, Outlet.TEST_RESOURCES, Outlet.TEST_SRC_GEN, Outlet.TEST_XTEND_GEN].map [
			new SourceFolderDescriptor(sourceFolder, isTest)
		].toSet
	}
	
	def isInlined() {
		enabled && config.sourceLayout != SourceLayout.PLAIN
	}
	
	def isSeparate() {
		enabled && config.sourceLayout == SourceLayout.PLAIN
	}
	
	override getNameQualifier() {
		testedProject.nameQualifier + ".tests"
	}
	
	override getUpstreamProjects() {
		#{testedProject}
	}
	
	override isEclipsePluginProject() {
		testedProject.isEclipsePluginProject
	}
	
	override getExternalDependencies() {
		val deps = newLinkedHashSet
		deps += super.externalDependencies
		if (config.junitVersion == JUnitVersion.JUNIT_4) {
			deps += new ExternalDependency()=>[
				p2.bundleId = "org.junit"
				p2.version = "4.13.2"
				maven.groupId = "junit"
				maven.artifactId = "junit"
				maven.scope = Scope.TESTCOMPILE
			]
		}
		if (config.junitVersion == JUnitVersion.JUNIT_5) {
			deps += new ExternalDependency()=>[
				p2.packages = Sets.newLinkedHashSet => [
					it += 'org.junit.jupiter.api;version="[5.1.0,6.0.0)"'
					it += 'org.junit.jupiter.api.extension;version="[5.1.0,6.0.0)"'
				]
				p2.version = "[5.1.0,6.0.0)"
				maven.groupId = "org.junit.jupiter"
				maven.artifactId = "junit-jupiter-api"
				maven.scope = Scope.TESTCOMPILE
			]
			deps += new ExternalDependency()=>[
				maven.groupId = "org.junit.jupiter"
				maven.artifactId = "junit-jupiter-engine"
				maven.scope = Scope.TESTRUNTIME
			]
			deps += new ExternalDependency()=>[
				maven.groupId = "org.junit.platform"
				maven.artifactId = "junit-platform-launcher"
				maven.scope = Scope.TESTRUNTIME
				maven.gradleOnly = true
			]
		}
		return deps
	}
	
	override pom() {
		super.pom => [
			packaging = if(isEclipsePluginProject) "eclipse-test-plugin" else "jar"
			buildSection = '''
				<build>
					«IF !isEclipsePluginProject && config.sourceLayout == SourceLayout.PLAIN»
						<testSourceDirectory>«Outlet.TEST_JAVA.sourceFolder»</testSourceDirectory>
						<testResources>
							<testResource>
								<directory>«Outlet.TEST_RESOURCES.sourceFolder»</directory>
								<excludes>
									<exclude>**/*.java</exclude>
									<exclude>**/*.xtend</exclude>
								</excludes>
							</testResource>
						</testResources>
					«ENDIF»
					<plugins>
						<plugin>
							<groupId>org.eclipse.xtext</groupId>
							<artifactId>xtend-maven-plugin</artifactId>
						</plugin>
						«IF isEclipsePluginProject && needsUiHarness»
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>tycho-surefire-plugin</artifactId>
								<configuration>
									<useUIHarness>true</useUIHarness>
								</configuration>
							</plugin>
						«ENDIF»
						«IF !isEclipsePluginProject»
							<plugin>
								<groupId>org.codehaus.mojo</groupId>
								<artifactId>build-helper-maven-plugin</artifactId>
								<version>3.3.0</version>
								<executions>
									<execution>
										<?m2e ignore?>
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
								</executions>
							</plugin>
						«ENDIF»
					</plugins>
				</build>
			'''
		]
	}
	
	def needsUiHarness() {
		false
	}
	
	override buildGradle() {
		super.buildGradle => [
			if (config.junitVersion == JUnitVersion.JUNIT_5) {
				additionalContent = '''
					test {
						useJUnitPlatform()
					}
				'''
			}
		]
	}
	
}
