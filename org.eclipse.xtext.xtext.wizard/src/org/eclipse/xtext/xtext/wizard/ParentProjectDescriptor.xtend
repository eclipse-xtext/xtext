/*******************************************************************************
 * Copyright (c) 2015, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard

import com.google.common.io.Resources
import java.nio.charset.StandardCharsets
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

@FinalFieldsConstructor
class ParentProjectDescriptor extends ProjectDescriptor {
	String nameQualifier = '.parent'

	override getNameQualifier() {
		nameQualifier
	}

	def void setNameQualifier(String nameQualifier) {
		this.nameQualifier = nameQualifier
	}

	override isEnabled() {
		config.needsGradleBuild || config.needsMavenBuild || config.projectLayout == ProjectLayout.HIERARCHICAL
	}

	override setEnabled(boolean enabled) {
		throw new UnsupportedOperationException("The parent project is automatically enabled depending on the build system")
	}

	override getLocation() {
		config.rootLocation + "/" + name
	}

	override isEclipsePluginProject() {
		false
	}

	override isPartOfGradleBuild() {
		true
	}

	override isPartOfMavenBuild() {
		true
	}

	override getFiles() {
		val files = newArrayList
		files += super.files
		if (config.needsGradleBuild) {
			files += file(Outlet.ROOT, 'settings.gradle', settingsGradle)
			files += file(Outlet.ROOT, 'gradle/source-layout.gradle', sourceLayoutGradle)
			if(config.needsGradleWrapper) {
				files += file(Outlet.ROOT, 'gradlew', loadResource("gradlew/gradlew"), true)
				files += file(Outlet.ROOT, 'gradlew.bat', loadResource("gradlew/gradlew.bat"))
				files += file(Outlet.ROOT, 'gradle/wrapper/gradle-wrapper.properties', loadResource("gradlew/gradle-wrapper.properties"))
				files += binaryFile(Outlet.ROOT, 'gradle/wrapper/gradle-wrapper.jar', class.classLoader.getResource("gradlew/gradle-wrapper.jar"))
			}
		}
		return files
	}

	def String getJavaVersion() {
		config.javaVersion.qualifier
	}

	def String getTychoVersion() {
		'4.0.13'
	}

	def private CharSequence loadResource(String resourcePath) {
		Resources.toString(class.classLoader.getResource(resourcePath), StandardCharsets.ISO_8859_1)
	}

	override buildGradle() {
		super.buildGradle => [
			additionalContent = '''
				buildscript {
					repositories {
						mavenCentral()
						gradlePluginPortal()
					}
					dependencies {
						classpath 'org.xtext:xtext-gradle-plugin:«config.xtextVersion.xtextGradlePluginVersion»'
					}
				}

				subprojects {
					ext.xtextVersion = '«config.xtextVersion»'
					repositories {
						mavenCentral()
						«IF config.xtextVersion.isSnapshot»
							maven {
								url = 'https://central.sonatype.com/repository/maven-snapshots'
							}
							maven {
								url = 'https://oss.sonatype.org/content/repositories/snapshots'
							}
«««							maven {
«««								url = 'https://ci.eclipse.org/xtext/job/xtext/job/<branch>/lastSuccessfulBuild/artifact/build/maven-repository/'
«««							}
						«ENDIF»
					}

					apply plugin: 'java-library'
					dependencies {
						api platform("org.eclipse.xtext:xtext-dev-bom:${xtextVersion}")
					}

					apply plugin: 'org.xtext.xtend'
					apply from: "${rootDir}/gradle/source-layout.gradle"
					apply plugin: 'eclipse'

					group = '«config.baseName»'
					version = '1.0.0-SNAPSHOT'

					java {
						sourceCompatibility = JavaVersion.VERSION_«javaVersion»
						targetCompatibility = JavaVersion.VERSION_«javaVersion»
					}

					configurations.all {
						exclude group: 'asm'
					}
				}
			'''
		]
	}

	def settingsGradle() '''
		«FOR p : config.enabledProjects.filter[it != this && partOfGradleBuild]»
			«IF config.projectLayout == ProjectLayout.FLAT»includeFlat«ELSE»include«ENDIF» '«p.name»'
		«ENDFOR»
	'''

	def sourceLayoutGradle() '''
		«IF config.sourceLayout == SourceLayout.PLAIN»
			if (name.endsWith(".tests")) {
				sourceSets {
					main {
						java.srcDirs = []
						resources.srcDirs = []
					}
					test {
						java.srcDirs = ['«Outlet.TEST_JAVA.sourceFolder»', '«Outlet.TEST_SRC_GEN.sourceFolder»', '«Outlet.MAIN_XTEND_GEN.sourceFolder»']
						resources.srcDirs = ['«Outlet.TEST_RESOURCES.sourceFolder»', '«Outlet.TEST_SRC_GEN.sourceFolder»']
						xtend.outputDir = '«Outlet.TEST_XTEND_GEN.sourceFolder»'
					}
				}
			} else {
				sourceSets {
					main {
						java.srcDirs = ['«Outlet.MAIN_JAVA.sourceFolder»', '«Outlet.MAIN_SRC_GEN.sourceFolder»', '«Outlet.TEST_XTEND_GEN.sourceFolder»']
						resources.srcDirs = ['«Outlet.MAIN_RESOURCES.sourceFolder»', '«Outlet.MAIN_SRC_GEN.sourceFolder»']
						xtend.outputDir = '«Outlet.MAIN_XTEND_GEN.sourceFolder»'
					}
					test {
						java.srcDirs = []
						resources.srcDirs = []
					}
				}
			}

			sourceSets.all {
				resources.exclude '**/*.g', '**/*.mwe2', '**/*.xtend', '**/*._trace'
			}
		«ELSE»
			sourceSets {
				main {
					java.srcDir '«Outlet.MAIN_SRC_GEN.sourceFolder»'
					resources.srcDir '«Outlet.MAIN_SRC_GEN.sourceFolder»'
					xtend.outputDir = '«Outlet.MAIN_XTEND_GEN.sourceFolder»'
				}
				test {
					java.srcDir '«Outlet.TEST_SRC_GEN.sourceFolder»'
					resources.srcDir '«Outlet.TEST_SRC_GEN.sourceFolder»'
					xtend.outputDir = '«Outlet.TEST_XTEND_GEN.sourceFolder»'
				}
			}
		«ENDIF»

		jar {
			from('model') {
				into('model')
			}
			«IF config.sourceLayout != SourceLayout.PLAIN»
				from(sourceSets.main.allSource) {
					include '**/*.xtext'
				}
			«ENDIF»
			manifest {
				attributes 'Bundle-SymbolicName': project.name
			}
		}

		plugins.withId('war') {
			war {
				webAppDirectory = file("«Outlet.WEBAPP.sourceFolder»")
			}
		}
	'''

	@Deprecated
	def CharSequence mavenDeploymentGradle() {
		throw new UnsupportedOperationException("Removed with 2.17")
	}

	override pom() {
		super.pom => [
			packaging = "pom"
			buildSection = '''
				<properties>
					<xtextVersion>«config.xtextVersion»</xtextVersion>
					«IF config.needsTychoBuild»
					<mwe2Version>«config.xtextVersion.mweVersion»</mwe2Version>
					«ENDIF»
					<project.build.sourceEncoding>«config.encoding»</project.build.sourceEncoding>
					<maven.compiler.source>«javaVersion»</maven.compiler.source>
					<maven.compiler.target>«javaVersion»</maven.compiler.target>
					«IF config.needsTychoBuild»
						<!-- Tycho settings -->
						<tycho-version>«tychoVersion»</tycho-version>
						<!-- Define overridable properties for tycho-surefire-plugin -->
						<platformSystemProperties></platformSystemProperties>
						<moduleProperties></moduleProperties>
						<systemProperties></systemProperties>
						<additionalTestArguments></additionalTestArguments>
					«ENDIF»
				</properties>
				<modules>
					«FOR p : config.enabledProjects.filter[it != this && partOfMavenBuild]»
						<module>«IF config.projectLayout == ProjectLayout.FLAT»../«ENDIF»«p.name»</module>
					«ENDFOR»
				</modules>
				<dependencyManagement>
					<dependencies>
						<dependency>
							<groupId>org.eclipse.xtext</groupId>
							<artifactId>xtext-dev-bom</artifactId>
							<version>${xtextVersion}</version>
							<type>pom</type>
							<scope>import</scope>
						</dependency>
					</dependencies>
				</dependencyManagement>
				<build>
					«IF config.needsTychoBuild»
						<plugins>
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>tycho-maven-plugin</artifactId>
								<version>${tycho-version}</version>
								<extensions>true</extensions>
							</plugin>
							«IF config.p2Project.enabled»
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>tycho-source-plugin</artifactId>
								<version>${tycho-version}</version>
								<executions>
									<execution>
										<id>plugin-source</id>
										<goals>
											<goal>plugin-source</goal>
										</goals>
									</execution>
									<execution>
										<id>feature-source</id>
										<goals>
											<goal>feature-source</goal>
										</goals>
									</execution>
								</executions>
							</plugin>
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>tycho-p2-plugin</artifactId>
								<version>${tycho-version}</version>
								<executions>
									<execution>
										<id>attach-p2-metadata</id>
										<phase>package</phase>
										<goals>
											<goal>p2-metadata</goal>
										</goals>
									</execution>
								</executions>
							</plugin>
							«ENDIF»
							<plugin>
								<groupId>org.eclipse.tycho</groupId>
								<artifactId>target-platform-configuration</artifactId>
								<version>${tycho-version}</version>
								<configuration>
									<target>
										<artifact>
											<groupId>«config.baseName»</groupId>
											<artifactId>«config.targetPlatformProject.name»</artifactId>
											<version>${project.version}</version>
										</artifact>
									</target>
									<environments>
										<environment>
											<os>macosx</os>
											<ws>cocoa</ws>
											<arch>x86_64</arch>
										</environment>
										<environment>
											<os>win32</os>
											<ws>win32</ws>
											<arch>x86_64</arch>
										</environment>
										<environment>
											<os>linux</os>
											<ws>gtk</ws>
											<arch>x86_64</arch>
										</environment>
									</environments>
									<dependency-resolution>
										<extraRequirements>
											<requirement>
												<type>eclipse-plugin</type>
												<id>org.eclipse.xtext.logging</id>
												<versionRange>0.0.0</versionRange>
											</requirement>
											<!-- to get the org.eclipse.osgi.compatibility.state plugin if the target
												platform is Luna or later. (backward compatible with kepler and previous
												versions) see https://bugs.eclipse.org/bugs/show_bug.cgi?id=492149 -->
											<requirement>
												<type>eclipse-feature</type>
												<id>org.eclipse.rcp</id>
												<versionRange>0.0.0</versionRange>
											</requirement>
										</extraRequirements>
									</dependency-resolution>
								</configuration>
							</plugin>
						</plugins>
					«ENDIF»
					<pluginManagement>
						<plugins>
							<plugin>
								<groupId>org.eclipse.xtext</groupId>
								<artifactId>xtend-maven-plugin</artifactId>
								<version>${xtextVersion}</version>
								<executions>
									<execution>
										<goals>
											<goal>compile</goal>
											<goal>xtend-install-debug-info</goal>
											<goal>testCompile</goal>
											<goal>xtend-test-install-debug-info</goal>
										</goals>
									</execution>
								</executions>
								<configuration>
									<outputDirectory>${basedir}/«Outlet.MAIN_XTEND_GEN.sourceFolder»</outputDirectory>
									<testOutputDirectory>${basedir}/«Outlet.TEST_XTEND_GEN.sourceFolder»</testOutputDirectory>
								</configuration>
							</plugin>
							<plugin>
								<groupId>org.apache.maven.plugins</groupId>
								<artifactId>maven-clean-plugin</artifactId>
								<version>3.2.0</version>
								<configuration>
									<filesets>
										«FOR dir : #[Outlet.MAIN_XTEND_GEN, Outlet.TEST_XTEND_GEN].map[sourceFolder].toSet»
											<fileset>
												<directory>${basedir}/«dir»</directory>
												<includes>
													<include>**/*</include>
												</includes>
											</fileset>
										«ENDFOR»
									</filesets>
								</configuration>
							</plugin>
							«IF !config.needsTychoBuild»
								<plugin>
									<groupId>org.apache.maven.plugins</groupId>
									<artifactId>maven-surefire-plugin</artifactId>
									<version>3.0.0</version>
								</plugin>
							«ENDIF»
							«IF config.needsTychoBuild»
								<plugin>
									<!--
										Can be removed after first generator execution
										https://bugs.eclipse.org/bugs/show_bug.cgi?id=480097
									-->
									<groupId>org.eclipse.tycho</groupId>
									<artifactId>tycho-compiler-plugin</artifactId>
									<version>${tycho-version}</version>
									<configuration>
										<compilerArgument>-err:-forbidden</compilerArgument>
										<useProjectSettings>false</useProjectSettings>
									</configuration>
								</plugin>
								<!-- to skip running (and compiling) tests use commandline flag: -Dmaven.test.skip
									To skip tests, but still compile them, use: -DskipTests
									To allow all tests in a pom to pass/fail, use commandline flag: -fae (fail
									at end) -->
								<plugin>
									<groupId>org.eclipse.tycho</groupId>
									<artifactId>tycho-surefire-plugin</artifactId>
									<version>${tycho-version}</version>
									<configuration>
										<!-- THE FOLLOWING LINE MUST NOT BE BROKEN BY AUTOFORMATTING -->
										<argLine>${platformSystemProperties} ${systemProperties} ${moduleProperties} ${additionalTestArguments}</argLine>
										<failIfNoTests>false</failIfNoTests>
										<useUIHarness>false</useUIHarness>
									</configuration>
								</plugin>
							«ENDIF»
						</plugins>
					</pluginManagement>
				</build>
				<repositories>
					<repository>
						<id>codehaus-snapshots</id>
						<name>disable dead 'Codehaus Snapshots' repository, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=481478</name>
						<url>http://nexus.codehaus.org/snapshots/</url>
						<releases>
							<enabled>false</enabled>
						</releases>
						<snapshots>
							<enabled>false</enabled>
						</snapshots>
					</repository>
					«IF config.needsTychoBuild»
						<!-- This must be disabled explicitly, otherwise it is enabled by https://github.com/mojohaus/mojo-parent 
							which is taken from exec-maven-plugin from at least version 1.6.0 -->
						<repository>
							<id>ossrh-snapshots</id>
							<name>ossrh-snapshots</name>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>false</enabled>
							</snapshots>
							<url>http://oss.sonatype.org/content/repositories/snapshots</url>
						</repository>
						<!-- This is enabled by /org/sonatype/oss/oss-parent/7 used as parent by 
							org/xtext/antlr-generator/3.2.1 -->
						<repository>
							<id>sonatype-nexus-snapshots</id>
							<name>Sonatype Nexus Snapshots</name>
							<url>https://oss.sonatype.org/content/repositories/snapshots</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>false</enabled>
							</snapshots>
						</repository>
					«ENDIF»
					«IF config.xtextVersion.isSnapshot»
						<repository>
							<name>Central Portal Snapshots</name>
							<id>central-portal-snapshots</id>
							<url>https://central.sonatype.com/repository/maven-snapshots/</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>true</enabled>
							</snapshots>
						</repository>
						<repository>
							<id>sonatype-snapshots</id>
							<url>https://oss.sonatype.org/content/repositories/snapshots</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>true</enabled>
							</snapshots>
						</repository>
«««						<repository>
«««							<id>xtext</id>
«««							<url>https://ci.eclipse.org/xtext/job/xtext/job/<branch>/lastSuccessfulBuild/artifact/build/maven-repository/</url>
«««							<releases>
«««								<enabled>false</enabled>
«««							</releases>
«««							<snapshots>
«««								<enabled>true</enabled>
«««							</snapshots>
«««						</repository>
					«ENDIF»
				</repositories>
				<pluginRepositories>
					<pluginRepository>
						<id>codehaus-snapshots</id>
						<name>disable dead 'Codehaus Snapshots' repository, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=481478</name>
						<url>http://nexus.codehaus.org/snapshots/</url>
						<releases>
							<enabled>false</enabled>
						</releases>
						<snapshots>
							<enabled>false</enabled>
						</snapshots>
					</pluginRepository>
					«IF config.needsTychoBuild»
						<pluginRepository>
							<id>ossrh-snapshots</id>
							<name>ossrh-snapshots</name>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>false</enabled>
							</snapshots>
							<url>http://oss.sonatype.org/content/repositories/snapshots</url>
						</pluginRepository>
						<pluginRepository>
							<id>sonatype-nexus-snapshots</id>
							<name>Sonatype Nexus Snapshots</name>
							<url>https://oss.sonatype.org/content/repositories/snapshots</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>false</enabled>
							</snapshots>
						</pluginRepository>
					«ENDIF»
					«IF config.xtextVersion.isSnapshot»
						<pluginRepository>
							<name>Central Portal Snapshots</name>
							<id>central-portal-snapshots</id>
							<url>https://central.sonatype.com/repository/maven-snapshots/</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>true</enabled>
							</snapshots>
						</pluginRepository>
						<pluginRepository>
							<id>sonatype-snapshots</id>
							<url>https://oss.sonatype.org/content/repositories/snapshots</url>
							<releases>
								<enabled>false</enabled>
							</releases>
							<snapshots>
								<enabled>true</enabled>
							</snapshots>
						</pluginRepository>
«««						<pluginRepository>
«««							<id>xtext-p</id>
«««							<url>https://ci.eclipse.org/xtext/job/xtext/job/<branch>/lastSuccessfulBuild/artifact/build/maven-repository/</url>
«««							<releases>
«««								<enabled>false</enabled>
«««							</releases>
«««							<snapshots>
«««								<enabled>true</enabled>
«««							</snapshots>
«««						</pluginRepository>
					«ENDIF»
					«IF config.needsTychoBuild && tychoVersion.endsWith("-SNAPSHOT")»
						<pluginRepository>
							<id>tycho-snapshots</id>
							<url>https://repo.eclipse.org/content/repositories/tycho-snapshots/</url>
						</pluginRepository>
					«ENDIF»
				</pluginRepositories>
				<profiles>
					<profile>
						<id>macos</id>
						<activation>
							<os>
								<family>mac</family>
							</os>
						</activation>
						<properties>
							<!-- THE FOLLOWING LINE MUST NOT BE BROKEN BY AUTOFORMATTING -->
							<platformSystemProperties>-XstartOnFirstThread</platformSystemProperties>
						</properties>
					</profile>
					<profile>
						<id>jdk9-or-newer</id>
						<activation>
							<jdk>[9,)</jdk>
						</activation>
						<properties>
							<moduleProperties>--add-modules=ALL-SYSTEM</moduleProperties>
						</properties>
					</profile>
				</profiles>
			'''
		]
	}

	override getSourceFolders() {
		#{}
	}

}
