/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator.types;

import static org.eclipse.xtext.xtext.generator.model.TypeReference.*;

import java.util.Set;

import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.scoping.IGlobalScopeProvider;
import org.eclipse.xtext.xtext.generator.AbstractXtextGeneratorFragment;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.util.BooleanGeneratorOption;
import org.eclipse.xtext.xtext.generator.xbase.XbaseUsageDetector;

import com.google.inject.Inject;

public class TypesGeneratorFragment2 extends AbstractXtextGeneratorFragment {
	@Inject
	private XbaseUsageDetector xbaseUsageDetector;

	@Accessors(AccessorType.PUBLIC_GETTER)
	private final BooleanGeneratorOption onlyEnabledIfGrammarIsUsed = new BooleanGeneratorOption(false);

	public void setOnlyEnabledIfGrammarIsUsed(boolean onlyEnabledIfGrammarIsUsed) {
		this.onlyEnabledIfGrammarIsUsed.set(onlyEnabledIfGrammarIsUsed);
	}

	@Override
	public void generate() {
		if (onlyEnabledIfGrammarIsUsed.get() && !xbaseUsageDetector.inheritsXtype(getLanguage().getGrammar())) {
			return;
		}
		new GuiceModuleAccess.BindingFactory()
				.addTypeToType(typeRef(IGlobalScopeProvider.class),
						typeRef("org.eclipse.xtext.common.types.xtext.TypesAwareDefaultGlobalScopeProvider"))
				.contributeTo(getLanguage().getRuntimeGenModule());
		getLanguage().getRuntimeGenModule()
				.setSuperClass(typeRef("org.eclipse.xtext.common.types.DefaultCommonTypesRuntimeModule"));
		new GuiceModuleAccess.BindingFactory()
				.addTypeToType(typeRef("org.eclipse.xtext.ui.editor.contentassist.PrefixMatcher"),
						typeRef("org.eclipse.xtext.ui.editor.contentassist.FQNPrefixMatcher"))
				.contributeTo(getLanguage().getEclipsePluginGenModule());
		getLanguage().getEclipsePluginGenModule()
				.setSuperClass(typeRef("org.eclipse.xtext.common.types.ui.DefaultCommonTypesUiModule"));
		if (getProjectConfig().getRuntime().getManifest() != null) {
			Set<String> requiredBundles = getProjectConfig().getRuntime().getManifest().getRequiredBundles();
			requiredBundles.add("org.eclipse.xtext.common.types");
			requiredBundles.add("org.objectweb.asm;bundle-version=\"[9.9.0,10.0.0)\";resolution:=optional");
		}
		if (getProjectConfig().getRuntimeTest().getManifest() != null) {
			Set<String> requiredBundles = getProjectConfig().getRuntimeTest().getManifest().getRequiredBundles();
			requiredBundles.add("org.objectweb.asm;bundle-version=\"[9.9.0,10.0.0)\";resolution:=optional");
		}
		if (getProjectConfig().getEclipsePlugin().getManifest() != null) {
			Set<String> requiredBundles = getProjectConfig().getEclipsePlugin().getManifest().getRequiredBundles();
			requiredBundles.add("org.eclipse.xtext.common.types.ui");
		}
	}

	public BooleanGeneratorOption getOnlyEnabledIfGrammarIsUsed() {
		return onlyEnabledIfGrammarIsUsed;
	}
}
