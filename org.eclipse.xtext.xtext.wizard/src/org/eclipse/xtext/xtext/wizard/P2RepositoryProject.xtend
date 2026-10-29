/*******************************************************************************
 * Copyright (c) 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard

import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
@FinalFieldsConstructor
class P2RepositoryProject extends ProjectDescriptor {

	override getNameQualifier() {
		".repository"
	}

	override isEclipsePluginProject() {
		false
	}

	override isPartOfGradleBuild() {
		false
	}

	override isPartOfMavenBuild() {
		true
	}

	override isEnabled() {
		super.enabled && config.runtimeProject.isEclipsePluginProject
	}

	override getFiles() {
		val files = newArrayList
		files += super.files
		files += file(Outlet.ROOT, "category.xml", categoryXml)
		files
	}

	override getSourceFolders() {
		#{}
	}

	def categoryXml() '''
		<?xml version="1.0" encoding="UTF-8"?>
		<site>
			<feature id="«config.sdkProject.name»" version="0.0.0">
				<category name="main"/>
			</feature>
			<category-def name="main" label="«config.language.simpleName»"/>
		</site>
	'''

	override buildGradle() {
		throw new UnsupportedOperationException("Eclipse repositories are not yet supported in Gradle")
	}

	override pom() {
		super.pom => [
			packaging = "eclipse-repository"
			buildSection = '''
				<build>
					<plugins>
						<plugin>
							<groupId>org.eclipse.tycho</groupId>
							<artifactId>tycho-p2-repository-plugin</artifactId>
							<version>${tycho-version}</version>
							<configuration>
								<includeAllSources>true</includeAllSources>
							</configuration>
						</plugin>
					</plugins>
				</build>
			'''
		]
	}

}