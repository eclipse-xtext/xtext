/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard

import static org.eclipse.xtext.xtext.wizard.ExternalDependency.*

class UiProjectDescriptor extends TestedProjectDescriptor {
	UiTestProjectDescriptor testProject

	new(WizardConfiguration config) {
		super(config)
		this.testProject = new UiTestProjectDescriptor(this)
	}

	override getTestProject() {
		testProject
	}

	override getUpstreamProjects() {
		#[config.runtimeProject, config.ideProject].filter[enabled].toSet
	}

	override getNameQualifier() {
		".ui"
	}

	override isEclipsePluginProject() {
		true
	}

	override isPartOfGradleBuild() {
		false
	}

	override isPartOfMavenBuild() {
		true
	}

	override getExternalDependencies() {
		val deps = newLinkedHashSet
		deps += super.externalDependencies
		deps += createXtextDependency("org.eclipse.xtext.ui")

		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=480097
		deps += createXtextDependency("org.eclipse.xtext.ui.shared")
		deps += createXtextDependency("org.eclipse.xtext.ui.codetemplates.ui")

		deps += new ExternalDependency => [
			p2 [
				bundleId = "org.eclipse.ui.editors"
				version = "3.14.300"
			]
		]
		deps += new ExternalDependency => [
			p2 [
				bundleId = "org.eclipse.ui.ide"
				version = "3.18.500"
			]
		]
		if (!config.ideProject.enabled) {
			deps += config.ideProject.externalDependencies
		}
		deps
	}

	override getImportedPackages() {
		val packs = super.getImportedPackages()
		packs += "org.apache.log4j"
		return packs
	}

	override getBinIncludes() {
		val includes = newLinkedHashSet
		includes += super.binIncludes
		includes += "plugin.xml"
		includes
	}

	override pom() {
		super.pom => [
			buildSection = '''
				<build>
					<plugins>
						<plugin>
							<groupId>org.eclipse.xtext</groupId>
							<artifactId>xtend-maven-plugin</artifactId>
						</plugin>
					</plugins>
				</build>
			'''
			packaging = "eclipse-plugin"
		]
	}
}