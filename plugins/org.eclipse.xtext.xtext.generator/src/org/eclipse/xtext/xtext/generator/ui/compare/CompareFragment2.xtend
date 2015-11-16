/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xtext.generator.ui.compare

import com.google.inject.Inject
import org.eclipse.xtext.xtext.generator.XtextGeneratorNaming
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess
import org.eclipse.xtext.xtext.generator.model.TypeReference

import static extension org.eclipse.xtext.GrammarUtil.*
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment

/**
 * Contributes the registration of compare infrastructure. 
 * 
 * @author Christian Schneider - Initial contribution and API
 */
class CompareFragment2 extends AbstractXtextGeneratorFragment {

	@Inject
	extension XtextGeneratorNaming

	override generate() {
		if (projectConfig.eclipsePlugin?.manifest !== null) {
			projectConfig.eclipsePlugin.manifest.requiredBundles += #[
				"org.eclipse.compare", "org.eclipse.xtext.ui"
			]
		}

		new GuiceModuleAccess.BindingFactory()
				.addTypeToType(
					new TypeReference("org.eclipse.compare.IViewerCreator"),
					new TypeReference("org.eclipse.xtext.ui.compare.DefaultViewerCreator")
				).contributeTo(language.eclipsePluginGenModule);

		if (projectConfig.eclipsePlugin?.pluginXml != null) {
			projectConfig.eclipsePlugin.pluginXml.entries += '''
			<extension point="org.eclipse.compare.contentViewers">
				<viewer id="�grammar.name�.compare.contentViewers"
					class="�grammar.eclipsePluginExecutableExtensionFactory�:org.eclipse.xtext.ui.compare.InjectableViewerCreator"
					extensions="�language.fileExtensions.join(",")�">
				</viewer>
			</extension>
			<extension point="org.eclipse.compare.contentMergeViewers">
				<viewer id="�grammar.name�.compare.contentMergeViewers"
					class="�grammar.eclipsePluginExecutableExtensionFactory�:org.eclipse.xtext.ui.compare.InjectableViewerCreator"
					extensions="�language.fileExtensions.join(",")�" label="�grammar.simpleName� Compare">
				</viewer>
			</extension>
			<extension point="org.eclipse.ui.editors.documentProviders">
				<provider id="�grammar.name�.editors.documentProviders"
					class="�grammar.eclipsePluginExecutableExtensionFactory�:org.eclipse.xtext.ui.editor.model.XtextDocumentProvider"
					extensions="�language.fileExtensions.join(",")�">
				</provider>
			</extension>
			<extension point="org.eclipse.team.core.fileTypes">
				�FOR modelFileExtension : language.fileExtensions�
				<fileTypes
					extension="�modelFileExtension�"
					type="text">
				</fileTypes>
				�ENDFOR� 
			</extension>
			'''
		}
	}	
}