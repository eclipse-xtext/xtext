/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.generator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.lib.AbstractWorkflowComponent2;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.emf.mwe.utils.StandaloneSetup;
import org.eclipse.xtext.GeneratedMetamodel;
import org.eclipse.xtext.Grammar;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.MergeableManifest2;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.Tuples;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xtext.generator.model.IXtextGeneratorFileSystemAccess;
import org.eclipse.xtext.xtext.generator.model.ManifestAccess;
import org.eclipse.xtext.xtext.generator.model.PluginXmlAccess;
import org.eclipse.xtext.xtext.generator.model.project.BundleProjectConfig;
import org.eclipse.xtext.xtext.generator.model.project.IXtextProjectConfig;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * The Xtext language infrastructure generator. Use the {@code configuration}
 * block to add general configuration for your Xtext project and the generated
 * code, e.g.
 * 
 * <pre>
 * configuration = {
 *     project = model.project.StandardProjectConfig {
 *         baseName = "org.example.language"
 *         rootPath = ".."
 *     }
 *     code = {
 *         encoding = 'ISO-8859-1'
 *     }
 * }
 * </pre>
 * 
 * You can generate code for one or more Xtext languages within the same
 * project. For each language, add a {@code language} block, e.g.
 * 
 * <pre>
 * language = StandardLanguage {
 *     name = "org.example.language.MyExampleLanguage"
 * }
 * </pre>
 * 
 * @noextend This class should not be extended by clients.
 */
public class XtextGenerator extends AbstractWorkflowComponent2 {
	private static final Logger LOG = Logger.getLogger(XtextGenerator.class);

	private DefaultGeneratorModule configuration = new DefaultGeneratorModule();

	private final List<XtextGeneratorLanguage> languageConfigs = new ArrayList<>();

	private XtextDirectoryCleaner cleaner = new XtextDirectoryCleaner();

	private XtextGeneratorStandaloneSetup standaloneSetup = new XtextGeneratorStandaloneSetup();

	private String grammarEncoding;

	private Injector injector;

	@Inject
	private IXtextProjectConfig projectConfig;

	@Inject
	private XtextGeneratorTemplates templates;

	@Inject
	private XtextGeneratorNaming naming;

	@Inject
	private CodeConfig codeConfig;

	public DefaultGeneratorModule getConfiguration() {
		return configuration;
	}

	public void setConfiguration(DefaultGeneratorModule configuration) {
		this.configuration = configuration;
	}

	public List<XtextGeneratorLanguage> getLanguageConfigs() {
		return languageConfigs;
	}

	public XtextDirectoryCleaner getCleaner() {
		return cleaner;
	}

	public void setCleaner(XtextDirectoryCleaner cleaner) {
		this.cleaner = cleaner;
	}

	public XtextGeneratorStandaloneSetup getStandaloneSetup() {
		return standaloneSetup;
	}

	public void setStandaloneSetup(XtextGeneratorStandaloneSetup standaloneSetup) {
		this.standaloneSetup = standaloneSetup;
	}

	public String getGrammarEncoding() {
		return grammarEncoding;
	}

	public void setGrammarEncoding(String grammarEncoding) {
		this.grammarEncoding = grammarEncoding;
	}

	public XtextGenerator() {
		new XtextStandaloneSetup().createInjectorAndDoEMFRegistration();
	}

	/**
	 * Add a language configuration to be included in the code generation
	 * process.
	 */
	public void addLanguage(final XtextGeneratorLanguage language) {
		languageConfigs.add(language);
	}

	@Override
	protected void checkConfigurationInternal(Issues issues) {
		initialize();
		MweIssues generatorIssues = new MweIssues(this, issues);
		configuration.checkConfiguration(generatorIssues);
		Map<String, Grammar> uris = new HashMap<>();
		for (XtextGeneratorLanguage language : languageConfigs) {
			language.checkConfiguration(generatorIssues);
			for (GeneratedMetamodel generatedMetamodel : Iterables
					.filter(language.getGrammar().getMetamodelDeclarations(), GeneratedMetamodel.class)) {
				String nsURI = generatedMetamodel.getEPackage().getNsURI();
				if (uris.containsKey(nsURI)) {
					generatorIssues.addError("Duplicate generated grammar with nsURI '" + nsURI + "' in "
							+ uris.get(nsURI).getName() + " and " + language.getGrammar().getName());
				} else {
					uris.put(nsURI, language.getGrammar());
				}
			}
		}
	}

	public void initialize() {
		if (injector == null) {
			XtextGenerator.LOG.info("Initializing Xtext generator");
			new StandaloneSetup().addRegisterGeneratedEPackage("org.eclipse.xtext.common.types.TypesPackage");
			initializeEncoding();
			injector = createInjector();
			injector.injectMembers(this);
			injector.getInstance(CodeConfig.class).initialize(injector);
			projectConfig.initialize(injector);
			cleaner.initialize(injector);
			standaloneSetup.initialize(injector);
			for (XtextGeneratorLanguage language : languageConfigs) {
				Injector languageInjector = createLanguageInjector(injector, language);
				language.initialize(languageInjector);
			}
		}
	}

	protected void initializeEncoding() {
		IResourceServiceProvider.Registry serviceProviderRegistry = IResourceServiceProvider.Registry.INSTANCE;
		IResourceServiceProvider serviceProvider = (IResourceServiceProvider) serviceProviderRegistry
				.getExtensionToFactoryMap().get("xtext");
		String encoding = null;
		if (grammarEncoding != null) {
			encoding = grammarEncoding;
		} else {
			encoding = configuration.getCode().getEncoding();
		}
		if (serviceProvider != null && encoding != null) {
			IEncodingProvider encodingProvider = serviceProvider.get(IEncodingProvider.class);
			if (encodingProvider instanceof IEncodingProvider.Runtime) {
				((IEncodingProvider.Runtime) encodingProvider).setDefaultEncoding(encoding);
			}
		}
	}

	protected Injector createInjector() {
		return Guice.createInjector(configuration);
	}

	protected Injector createLanguageInjector(Injector parent, XtextGeneratorLanguage language) {
		return parent.createChildInjector(new LanguageModule(language));
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		initialize();
		try {
			cleaner.clean();
			for (XtextGeneratorLanguage language : languageConfigs) {
				try {
					XtextGenerator.LOG.info("Generating " + language.getGrammar().getName());
					language.generate();
					generateSetups(language);
					generateModules(language);
					generateExecutableExtensionFactory(language);
				} catch (Exception e) {
					handleException(e, issues);
				}
			}
			XtextGenerator.LOG.info("Generating common infrastructure");
			generatePluginXmls();
			generateManifests();
			generateActivator();
			generateServices();
		} catch (Exception e) {
			handleException(e, issues);
		}
	}

	private void handleException(Exception ex, Issues issues) {
		if (ex instanceof CompositeGeneratorException) {
			((CompositeGeneratorException) ex).getExceptions().forEach(it -> handleException(it, issues));
		} else {
			issues.addError(this, "GeneratorException: ", null, ex, null);
		}
	}

	protected void generateSetups(IXtextGeneratorLanguage language) {
		templates.createRuntimeGenSetup(language).writeTo(projectConfig.getRuntime().getSrcGen());
		templates.createRuntimeSetup(language).writeTo(projectConfig.getRuntime().getSrc());
		templates.createIdeSetup(language).writeTo(projectConfig.getGenericIde().getSrc());
	}

	protected void generateModules(IXtextGeneratorLanguage language) {
		templates.createRuntimeGenModule(language).writeTo(projectConfig.getRuntime().getSrcGen());
		templates.createRuntimeModule(language).writeTo(projectConfig.getRuntime().getSrc());
		templates.createIdeModule(language).writeTo(projectConfig.getGenericIde().getSrc());
		templates.createIdeGenModule(language).writeTo(projectConfig.getGenericIde().getSrcGen());
		templates.createEclipsePluginGenModule(language).writeTo(projectConfig.getEclipsePlugin().getSrcGen());
		templates.createEclipsePluginModule(language).writeTo(projectConfig.getEclipsePlugin().getSrc());
	}

	protected void generateExecutableExtensionFactory(IXtextGeneratorLanguage language) {
		if (projectConfig.getEclipsePlugin().getSrcGen() != null) {
			XtextGeneratorLanguage firstLanguageConfig = Iterables.getFirst(languageConfigs, null);
			templates.createEclipsePluginExecutableExtensionFactory(language, firstLanguageConfig)
					.writeTo(projectConfig.getEclipsePlugin().getSrcGen());
		}
	}

	protected void generateManifests() {
		try {
			// Filter null values and merge duplicate entries
			Iterable<BundleProjectConfig> bundleProjects = Iterables.filter(projectConfig.getEnabledProjects(),
					BundleProjectConfig.class);
			List<Triple<ManifestAccess, IXtextGeneratorFileSystemAccess, String>> manifests = IterableExtensions
					.toList(Iterables.transform(bundleProjects, (BundleProjectConfig it) -> {
						return Tuples.create(it.getManifest(), it.getMetaInf(), it.getName());
					}));
			HashMap<URI, ManifestAccess> uri2Manifest = Maps.newHashMapWithExpectedSize(manifests.size());
			ListIterator<Triple<ManifestAccess, IXtextGeneratorFileSystemAccess, String>> manifestIter = manifests
					.listIterator();
			while (manifestIter.hasNext()) {
				Triple<ManifestAccess, IXtextGeneratorFileSystemAccess, String> entry = manifestIter.next();
				ManifestAccess manifest = entry.getFirst();
				IXtextGeneratorFileSystemAccess metaInf = entry.getSecond();
				if (manifest == null || metaInf == null) {
					manifestIter.remove();
				} else {
					if (manifest.getActivator() == null && manifest == projectConfig.getEclipsePlugin().getManifest()) {
						manifest.setActivator(naming.getEclipsePluginActivator());
					}
					URI uri = metaInf.getURI(manifest.getPath());
					if (uri2Manifest.containsKey(uri)) {
						uri2Manifest.get(uri).merge(manifest);
						manifestIter.remove();
					} else {
						uri2Manifest.put(uri, manifest);
					}
				}
			}
			for (Triple<ManifestAccess, IXtextGeneratorFileSystemAccess, String> entry : manifests) {
				ManifestAccess manifest = entry.getFirst();
				IXtextGeneratorFileSystemAccess metaInf = entry.getSecond();
				if (manifest.getBundleName() == null) {
					manifest.setBundleName(entry.getThird());
				}
				if (metaInf.isFile(manifest.getPath())) {
					if (manifest.isMerge()) {
						mergeManifest(manifest, metaInf);
					} else {
						if (manifest.getPath().endsWith(".MF")) {
							manifest.setPath(manifest.getPath() + "_gen");
							manifest.writeTo(metaInf);
						}
					}
				} else {
					manifest.writeTo(metaInf);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void mergeManifest(ManifestAccess manifest, IXtextGeneratorFileSystemAccess metaInf) throws IOException {
		InputStream in = null;
		try {
			in = metaInf.readBinaryFile(manifest.getPath());
			MergeableManifest2 merge = new MergeableManifest2(in, manifest.getBundleName());
			merge.setLineDelimiter(codeConfig.getLineDelimiter());
			merge.addExportedPackages(manifest.getExportedPackages());
			merge.addRequiredBundles(manifest.getRequiredBundles());
			merge.addImportedPackages(manifest.getImportedPackages());
			if (manifest.getActivator() != null && Strings.isNullOrEmpty(merge.getBundleActivator())) {
				merge.setBundleActivator(manifest.getActivator().getName());
			}
			if (merge.isModified()) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				merge.write(out);
				metaInf.generateFile(manifest.getPath(), new ByteArrayInputStream(out.toByteArray()));
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	protected void generateServices() {
		if (projectConfig.getGenericIde().getSrcGen() == null || languageConfigs.isEmpty()) {
			return;
		}
		templates.createMetaInfServices(languageConfigs).writeTo(projectConfig.getGenericIde().getSrcGen());
	}

	protected void generateActivator() {
		if (projectConfig.getEclipsePlugin().getSrcGen() != null && !languageConfigs.isEmpty()) {
			templates.createEclipsePluginActivator(projectConfig, languageConfigs)
					.writeTo(projectConfig.getEclipsePlugin().getSrcGen());
		}
	}

	protected void generatePluginXmls() {
		// Filter null values and merge duplicate entries
		Iterable<BundleProjectConfig> bundleProjects = Iterables.filter(projectConfig.getEnabledProjects(),
				BundleProjectConfig.class);
		List<Pair<PluginXmlAccess, IXtextGeneratorFileSystemAccess>> pluginXmls = IterableExtensions
				.toList(Iterables.transform(bundleProjects, (BundleProjectConfig it) -> {
					return Pair.of(it.getPluginXml(), it.getRoot());
				}));
		HashMap<URI, PluginXmlAccess> uri2PluginXml = Maps
				.newHashMapWithExpectedSize(pluginXmls.size());
		ListIterator<Pair<PluginXmlAccess, IXtextGeneratorFileSystemAccess>> pluginXmlIter = pluginXmls.listIterator();
		while (pluginXmlIter.hasNext()) {
			Pair<PluginXmlAccess, IXtextGeneratorFileSystemAccess> entry = pluginXmlIter.next();
			PluginXmlAccess pluginXml = entry.getKey();
			IXtextGeneratorFileSystemAccess root = entry.getValue();
			if (pluginXml == null || root == null) {
				pluginXmlIter.remove();
			} else {
				URI uri = root.getURI(pluginXml.getPath());
				if (uri2PluginXml.containsKey(uri)) {
					uri2PluginXml.get(uri).merge(pluginXml);
					pluginXmlIter.remove();
				} else {
					uri2PluginXml.put(uri, pluginXml);
				}
			}
		}
		for (Pair<PluginXmlAccess, IXtextGeneratorFileSystemAccess> entry : pluginXmls) {
			PluginXmlAccess pluginXml = entry.getKey();
			IXtextGeneratorFileSystemAccess root = entry.getValue();
			if (root.isFile(pluginXml.getPath())) {
				// only write plugin.xml_gen if entries exist and content
				// differs
				if (!pluginXml.getEntries().isEmpty()) {
					String textFileContent = null;
					CharSequence textFile = root.readTextFile(pluginXml.getPath());
					if (textFile != null) {
						textFileContent = textFile.toString();
					}
					if (!Objects.equals(textFileContent, pluginXml.getContentString())) {
						if (pluginXml.getPath().endsWith(".xml")) {
							pluginXml.setPath(pluginXml.getPath() + "_gen");
							pluginXml.writeTo(root);
						}
					}
				}
			} else {
				pluginXml.writeTo(root);
			}
		}
	}

}
