/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.AbstractMetamodelDeclaration;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.ReferencedMetamodel;
import org.eclipse.xtext.XtextPackage;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.xtext.RuleNames;
import org.eclipse.xtext.xtext.generator.model.GuiceModuleAccess;
import org.eclipse.xtext.xtext.generator.model.StandaloneSetupAccess;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * Configuration for an Xtext language. Use this class if you need to specify
 * exactly which generator fragments you want to include, or alternatively
 * {@link StandardLanguage} to work with the standard fragments.
 * 
 * <p>
 * The most important property is {@code name}, which should be set to the
 * language name exactly as specified in the grammar file. If the default
 * grammar resolution based on the name does not work in your particular
 * scenario, set the {@code grammarUri} property instead. Furthermore, use
 * {@code fileExtensions} to specify the file extensions for your language.
 * </p>
 * 
 * <p>
 * Generator fragments can be added with the {@code fragment} property. See
 * {@link StandardLanguage} for a list of useful fragments.
 * </p>
 * 
 * @noextend This class should not be extended by clients.
 */
public class XtextGeneratorLanguage extends CompositeGeneratorFragment2 implements IXtextGeneratorLanguage {
	private static final Logger LOG = Logger.getLogger(XtextGeneratorLanguage.class);

	private String grammarUri;

	private String name;

	private Boolean generateXtendStubs;

	private Grammar grammar;

	private RuleNames ruleNames;

	private List<String> fileExtensions;

	private List<String> referencedResources = new ArrayList<>();

	private ResourceSet resourceSet;

	private com.google.inject.Module guiceModule = (Binder it) -> {
	};

	private final StandaloneSetupAccess runtimeGenSetup = new StandaloneSetupAccess();

	private final GuiceModuleAccess runtimeGenModule = new GuiceModuleAccess();

	private final GuiceModuleAccess ideGenModule = new GuiceModuleAccess();

	private final GuiceModuleAccess eclipsePluginGenModule = new GuiceModuleAccess();

	@Inject
	private Provider<ResourceSet> resourceSetProvider;

	@Inject
	private IXtextProjectConfig projectConfig;

	@Inject
	private CodeConfig codeConfig;

	@Inject
	private XtextGeneratorResourceSetInitializer resourceSetInitializer;

	/**
	 * The URI of the grammar file. Use this property if the automatic grammar
	 * resolution based on {@link #setName(String)} does not work in your
	 * particular scenario.
	 */
	public void setGrammarUri(String uri) {
		this.grammarUri = uri;
	}

	public String getGrammarUri() {
		if (grammarUri != null) {
			return grammarUri;
		} else {
			return new File(projectConfig.getRuntime().getSrc().getPath(), name.replace(".", "/") + ".xtext").toURI()
					.toString();
		}
	}

	/**
	 * The language name exactly as specified in the grammar.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @since 2.26
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * Either a single file extension or a comma-separated list of extensions
	 * for which the language shall be registered.
	 */
	public void setFileExtensions(String fileExtensions) {
		this.fileExtensions = Arrays.asList(fileExtensions.trim().split("\\s*,\\s*"));
	}

	/**
	 * Add a resource to be included in the resource set that will be used to
	 * load the grammar file. This is often necessary if additional Ecore models
	 * are referenced by the grammar.
	 */
	public void addReferencedResource(String referencedResource) {
		referencedResources.add(referencedResource);
	}

	@Override
	public List<String> getFileExtensions() {
		if (fileExtensions == null || fileExtensions.isEmpty()) {
			setFileExtensions(GrammarUtil.getSimpleName(grammar).toLowerCase());
			LOG.info("No explicit fileExtensions configured. Using '*." + fileExtensions + "'.");
		}
		return fileExtensions;
	}

	/**
	 * @deprecated Use {@link CodeConfig#setPreferXtendStubs(boolean)} instead
	 */
	@Deprecated
	public void setGenerateXtendStubs(Boolean generateXtendStubs) {
		this.generateXtendStubs = generateXtendStubs;
	}

	/**
	 * @deprecated Use {@link CodeConfig#isPreferXtendStubs()} instead
	 */
	@Deprecated
	@Override
	public boolean isGenerateXtendStubs() {
		if (generateXtendStubs != null) {
			return generateXtendStubs.booleanValue();
		} else {
			return codeConfig.isPreferXtendStubs();
		}
	}

	@Override
	public void initialize(Injector injector) {
		getFragments().addAll(0, getImplicitFragments());
		injector.injectMembers(this);
		if (resourceSet == null) {
			resourceSet = resourceSetProvider.get();
		}
		resourceSetInitializer.initialize(resourceSet, referencedResources);
		if (!resourceSet.getResources().isEmpty()) {
			installIndex();
			int i = 0;
			int size = resourceSet.getResources().size();
			while (i < size) {
				Resource res = resourceSet.getResources().get(i);
				if (res.getContents().isEmpty()) {
					LOG.error("Error loading '" + res.getURI() + "'");
				} else {
					if (!res.getErrors().isEmpty()) {
						LOG.error("Error loading '" + res.getURI() + "':\n" + Joiner.on("\n").join(res.getErrors()));
					}
				}
				i++;
			}
			EcoreUtil.resolveAll(resourceSet);
		}
		if (getGrammarUri() == null) {
			throw new IllegalStateException("No grammarUri or language name given");
		}
		XtextResource resource = (XtextResource) resourceSet.getResource(URI.createURI(getGrammarUri()), true);
		if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException("Couldn't load grammar for '" + getGrammarUri() + "'.");
		}
		if (!resource.getErrors().isEmpty()) {
			LOG.error(resource.getErrors());
			throw new IllegalStateException(
					"Problem parsing '" + getGrammarUri() + "':\n" + Joiner.on("\n").join(resource.getErrors()));
		}
		Grammar grammar = (Grammar) resource.getContents().get(0);
		validateGrammar(grammar);
		initialize(grammar);
		super.initialize(injector);
	}

	protected List<? extends IXtextGeneratorFragment> getImplicitFragments() {
		List<ImplicitFragment> fragments = new ArrayList<>();
		fragments.add(new ImplicitFragment());
		return fragments;
	}

	public void initialize(Grammar grammar) {
		this.grammar = grammar;
		this.ruleNames = RuleNames.getRuleNames(grammar, true);
	}

	private void installIndex() {
		ResourceDescriptionsData index = new ResourceDescriptionsData(Collections.emptyList());
		List<Resource> resources = Lists.newArrayList(resourceSet.getResources());
		for (Resource resource : resources) {
			index(resource, resource.getURI(), index);
		}
		ResourceDescriptionsData.ResourceSetAdapter.installResourceDescriptionsData(resourceSet, index);
	}

	private void index(Resource resource, URI uri, ResourceDescriptionsData index) {
		IResourceServiceProvider serviceProvider = IResourceServiceProvider.Registry.INSTANCE
				.getResourceServiceProvider(uri);
		if (serviceProvider != null) {
			IResourceDescription resourceDescription = serviceProvider.getResourceDescriptionManager()
					.getResourceDescription(resource);
			if (resourceDescription != null) {
				index.addDescription(uri, resourceDescription);
			}
		}
	}

	protected void validateGrammar(Grammar grammar) {
		validateAllImports(grammar);
		EValidator validator = EValidator.Registry.INSTANCE.getEValidator(XtextPackage.eINSTANCE);
		if (validator != null) {
			DiagnosticChain chain = new DiagnosticChain() {
				@Override
				public void add(Diagnostic diagnostic) {
					if (diagnostic.getSeverity() == Diagnostic.ERROR) {
						String grammarName = "Validation Error in " + grammar.getName() + ": ";
						if (diagnostic.getException() == null) {
							throw new IllegalStateException(grammarName + diagnostic.getMessage());
						} else {
							throw new IllegalStateException(grammarName + diagnostic.getMessage(),
									diagnostic.getException());
						}
					}
				}

				@Override
				public void addAll(Diagnostic diagnostic) {
					add(diagnostic);
				}

				@Override
				public void merge(Diagnostic diagnostic) {
					throw new UnsupportedOperationException();
				}
			};
			validator.validate(grammar, chain, null);
			TreeIterator<EObject> iterator = grammar.eAllContents();
			while (iterator.hasNext()) {
				validator.validate(iterator.next(), chain, new HashMap<Object, Object>());
			}
		}
	}

	protected void validateAllImports(Grammar grammar) {
		for (AbstractMetamodelDeclaration amd : GrammarUtil.allMetamodelDeclarations(grammar)) {
			if (amd instanceof ReferencedMetamodel) {
				validateReferencedMetamodel(((ReferencedMetamodel) amd));
			}
		}
	}

	protected void validateReferencedMetamodel(ReferencedMetamodel ref) {
		if (ref.getEPackage() != null && !ref.getEPackage().eIsProxy()) {
			return;
		}
		EReference eref = XtextPackage.Literals.ABSTRACT_METAMODEL_DECLARATION__EPACKAGE;
		List<INode> nodes = NodeModelUtils.findNodesForFeature(ref, eref);
		String refName = null;
		if (nodes.isEmpty()) {
			refName = "(unknown)";
		} else {
			refName = NodeModelUtils.getTokenText(nodes.get(0));
		}
		String grammarName = GrammarUtil.getGrammar(ref).getName();
		String msg = "The EPackage " + refName + " in grammar " + grammarName + " could not be found. "
				+ "You might want to register that EPackage in your workflow file.";
		throw new IllegalStateException(msg);
	}

	@Override
	public Grammar getGrammar() {
		return grammar;
	}

	public RuleNames getRuleNames() {
		return ruleNames;
	}

	public List<String> getReferencedResources() {
		return referencedResources;
	}

	@Override
	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public void setResourceSet(ResourceSet resourceSet) {
		this.resourceSet = resourceSet;
	}

	public com.google.inject.Module getGuiceModule() {
		return guiceModule;
	}

	public void setGuiceModule(com.google.inject.Module guiceModule) {
		this.guiceModule = guiceModule;
	}

	@Override
	public StandaloneSetupAccess getRuntimeGenSetup() {
		return runtimeGenSetup;
	}

	@Override
	public GuiceModuleAccess getRuntimeGenModule() {
		return runtimeGenModule;
	}

	@Override
	public GuiceModuleAccess getIdeGenModule() {
		return ideGenModule;
	}

	@Override
	public GuiceModuleAccess getEclipsePluginGenModule() {
		return eclipsePluginGenModule;
	}

}
