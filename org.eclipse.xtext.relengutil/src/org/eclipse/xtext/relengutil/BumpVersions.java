/*******************************************************************************
 * Copyright (c) 2024, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.relengutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.interpolation.DefaultModelVersionProcessor;
import org.apache.maven.model.interpolation.StringVisitorModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.xtext.util.MergeableManifest2;
import org.eclipse.xtext.util.MergeableManifest2.BundleOrPackage;
import org.eclipse.xtext.util.MergeableManifest2.BundleOrPackageList;

import com.google.common.collect.ForwardingMap;

/**
 * @author Sebastian - Initial contribution and API
 */
public class BumpVersions {

	public static void main(String[] args) throws Exception {
		try {
			File gitRoot = new File("./..");
			Map<String, String> versionsFromDevBom = readDevBom(new File(gitRoot, "org.eclipse.xtext.dev-bom/pom.xml"));
			addMissingVersions(new ForwardingMap<String, String>() {
				Set<String> dups = new HashSet<>();
				@Override
				protected Map<String, String> delegate() {
					return versionsFromDevBom;
				}
				@Override
				public String put(String key, String value) {
					if (!dups.add(key)) {
						throw new IllegalArgumentException(key);
					}
					String previous = super.put(key, value);
					if (previous != null && value.indexOf(previous) == -1) {
						throw new IllegalArgumentException(value + " != " + previous);
					}
					return previous;
				}
			});
			String newVersion = versionsFromDevBom.get("xtext-dev-bom");
			for (File bundleDir : gitRoot.listFiles()) {
				if (bundleDir.isDirectory()) {
					File manifest = new File(bundleDir, "META-INF/MANIFEST.MF");
					if (manifest.exists()) {
						MergeableManifest2 mergable;
						try (FileInputStream is = new FileInputStream(manifest)) {
							mergable = new MergeableManifest2(is);
						}
						mergable.getMainAttributes().put(MergeableManifest2.BUNDLE_VERSION, newVersion + ".qualifier");
						updateRequiredXtextBundles(mergable, newVersion);
						updateImportedXtextPackages(mergable, newVersion);
						updateExportedXtextPackages(mergable, newVersion);
						updateRequiredBundle(mergable, versionsFromDevBom);
						
						if (mergable.isModified()) {
							try (FileOutputStream out = new FileOutputStream(manifest)) {
								mergable.write(out);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static final String LSP4J_VERSION = "[1.0.0,1.1.0)";
	
	private static void addMissingVersions(Map<String, String> bundleToVersion) {
		bundleToVersion.put("org.hamcrest.core", "2.2.0");
		bundleToVersion.put("org.hamcrest.library", "1.3.0");
		bundleToVersion.put("org.eclipse.lsp4j", LSP4J_VERSION);
		bundleToVersion.put("org.eclipse.lsp4j.jsonrpc", LSP4J_VERSION);
		
		bundleToVersion.put("org.eclipse.jdt.core", "3.37.0");
		bundleToVersion.put("org.eclipse.jdt.core.manipulation", "1.21.0");
		bundleToVersion.put("org.eclipse.jdt.debug", "3.21.300");
		bundleToVersion.put("org.eclipse.jdt.debug.ui", "3.13.300");
		bundleToVersion.put("org.eclipse.jdt.junit", "3.16.300");
		bundleToVersion.put("org.eclipse.jdt.launching", "3.21.100");
		bundleToVersion.put("org.eclipse.jdt.ui", "3.32.0");
		
		bundleToVersion.put("org.eclipse.ui", "3.205.100");
		bundleToVersion.put("org.eclipse.ui.console", "3.14.0");
		bundleToVersion.put("org.eclipse.ui.editors", "3.17.200");
		bundleToVersion.put("org.eclipse.ui.forms", "3.13.200");
		bundleToVersion.put("org.eclipse.ui.ide", "3.22.100");
		bundleToVersion.put("org.eclipse.ui.navigator", "3.12.300");
		bundleToVersion.put("org.eclipse.ui.views", "3.12.200");
		bundleToVersion.put("org.eclipse.ui.workbench", "3.131.100");
		bundleToVersion.put("org.eclipse.ui.workbench.texteditor", "3.17.300");
		
		bundleToVersion.put("org.eclipse.swtbot.eclipse.core", "4.1.0");
		bundleToVersion.put("org.eclipse.swtbot.eclipse.finder", "4.1.0");
		bundleToVersion.put("org.eclipse.swtbot.junit4_x", "4.1.0");
		bundleToVersion.put("org.eclipse.swtbot.swt.finder", "4.1.0");
		
		bundleToVersion.put("org.apache.ant", "1.10.14");
		bundleToVersion.put("org.eclipse.buildship.core", "3.1.9");
		
		bundleToVersion.put("org.eclipse.compare", "3.10.0");
		bundleToVersion.put("org.eclipse.e4.core.services", "2.4.300");
		bundleToVersion.put("org.eclipse.e4.ui.css.swt.theme", "0.14.300");
		
		bundleToVersion.put("jakarta.inject.jakarta.inject-api", "2.0.1");
		
		bundleToVersion.put("org.eclipse.core.databinding", "1.13.200");
		bundleToVersion.put("org.eclipse.core.databinding.beans", "1.10.200");
		bundleToVersion.put("org.eclipse.core.databinding.property", "1.10.200");
		
		bundleToVersion.put("org.eclipse.core.expressions", "3.9.300");
		bundleToVersion.put("org.eclipse.core.filesystem", "1.10.300");
		bundleToVersion.put("org.eclipse.core.resources", "3.20.100");
		bundleToVersion.put("org.eclipse.core.runtime", "3.31.0");
		bundleToVersion.put("org.eclipse.debug.core", "3.21.300");
		bundleToVersion.put("org.eclipse.debug.ui", "3.18.300");
		bundleToVersion.put("org.eclipse.draw2d", "3.15.0");
		bundleToVersion.put("org.eclipse.emf.codegen", "2.23.0");
		bundleToVersion.put("org.eclipse.emf.codegen.ecore", "2.37.0");
		bundleToVersion.put("org.eclipse.emf.common", "2.30.0");
		bundleToVersion.put("org.eclipse.emf.common.ui", "2.22.0");
		bundleToVersion.put("org.eclipse.emf.ecore", "2.36.0");
		bundleToVersion.put("org.eclipse.emf.ecore.change", "2.16.0");
		bundleToVersion.put("org.eclipse.emf.ecore.editor", "2.18.0");
		bundleToVersion.put("org.eclipse.emf.ecore.xmi", "2.37.0");
		bundleToVersion.put("org.eclipse.emf.edit", "2.21.0");
		bundleToVersion.put("org.eclipse.emf.edit.ui", "2.23.0");
		bundleToVersion.put("org.eclipse.equinox.common", "3.19.0");
		bundleToVersion.put("org.eclipse.help", "3.10.300");
		bundleToVersion.put("org.eclipse.jface", "3.33.0");
		bundleToVersion.put("org.eclipse.jface.databinding", "1.15.200");
		bundleToVersion.put("org.eclipse.jface.text", "3.25.0");
		bundleToVersion.put("org.eclipse.ltk.core.refactoring", "3.14.300");
		bundleToVersion.put("org.eclipse.ltk.ui.refactoring", "3.13.300");
		bundleToVersion.put("org.eclipse.m2e.core", "2.6.0");
		bundleToVersion.put("org.eclipse.m2e.maven.runtime", "3.9.600");
		bundleToVersion.put("org.eclipse.pde.core", "3.18.0");
		bundleToVersion.put("org.eclipse.pde.ui", "3.15.100");
		bundleToVersion.put("org.eclipse.search", "3.16.100");

		bundleToVersion.put("org.eclipse.team.core", "3.10.300");
		bundleToVersion.put("org.eclipse.team.ui", "3.10.300");
	}
	
	
	
	private static Map<String, String> readDevBom(File pomXml) throws IOException, Exception {
		if (!pomXml.exists()) {
			throw new IllegalArgumentException();
		}
		MavenXpp3Reader reader = new MavenXpp3Reader();
    	Model model = reader.read(new FileReader(pomXml));
    	
    	StringVisitorModelInterpolator interpolator = new StringVisitorModelInterpolator();
    	interpolator.setVersionPropertiesProcessor(new DefaultModelVersionProcessor());
    	List<Object> problems = new ArrayList<>();
    	DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
    	request.setRawModel(model);
    	interpolator.interpolateModel(model, null, request, problems::add);
    	
    	DependencyManagement dependencyManagement = model.getDependencyManagement();
    	List<Dependency> dependencies = dependencyManagement.getDependencies();
    	Map<String, String> versions = new HashMap<>();
    	String xtextVersion = model.getVersion();
    	if (xtextVersion.endsWith("-SNAPSHOT")) {
    		xtextVersion = xtextVersion.substring(0, xtextVersion.length() - "-SNAPSHOT".length());
    	}
    	versions.put("xtext-dev-bom", xtextVersion);
    	for(Dependency dependency: dependencies) {
    		String artifactId = dependency.getArtifactId();
    		if (!artifactId.startsWith("org.eclipse.x")) {    		
	    		String version = dependency.getVersion();
	    		int milestoneDelimiter = version.indexOf(".M");
	    		if (milestoneDelimiter != -1) {
	    			version = version.substring(0, milestoneDelimiter);
	    		}
	    		versions.put(artifactId, version);
    		}
    	}
    	return versions;
	}
	
	private static void updateRequiredBundle(MergeableManifest2 mergable, Map<String, String> bundleVersions) {
		String oldBundles = mergable.getMainAttributes().get(MergeableManifest2.REQUIRE_BUNDLE);
		if (oldBundles == null)
			return;
		BundleOrPackageList requiredBundles = BundleOrPackageList.fromInput(oldBundles, mergable.getLineDelimiter(),
				"bundle-version");
		List<String> updatedBundles = new ArrayList<>();
		for (BundleOrPackage requiredBundle : requiredBundles.list()) {
			String bundleName = requiredBundle.getName();
			String newVersion = bundleVersions.get(bundleName);
			if (newVersion != null) {
				updatedBundles.add(bundleName + ";bundle-version=\"" + newVersion + "\"");
			}
		}
		mergable.addRequiredBundles(updatedBundles.toArray(new String[0]), true);
	}

	private static void updateRequiredXtextBundles(MergeableManifest2 mergable, String newVersion) {
		String oldBundles = mergable.getMainAttributes().get(MergeableManifest2.REQUIRE_BUNDLE);
		if (oldBundles == null)
			return;
		BundleOrPackageList requiredBundles = BundleOrPackageList.fromInput(oldBundles, mergable.getLineDelimiter(),
				"bundle-version");
		List<String> updatedBundles = new ArrayList<>();
		for (BundleOrPackage requiredBundle : requiredBundles.list()) {
			String bundleName = requiredBundle.getName();
			if (bundleName.startsWith("org.eclipse.x")) {
				updatedBundles.add(bundleName + ";bundle-version=\"" + newVersion + "\"");
			}
		}
		mergable.addRequiredBundles(updatedBundles.toArray(new String[0]), true);
	}

	private static void updateImportedXtextPackages(MergeableManifest2 mergable, String newVersion) {
		String oldPackages = mergable.getMainAttributes().get(MergeableManifest2.IMPORT_PACKAGE);
		if (oldPackages == null)
			return;
		BundleOrPackageList importedPackages = BundleOrPackageList.fromInput(oldPackages, mergable.getLineDelimiter(),
				"version");
		List<String> updatedPackages = new ArrayList<>();
		for (BundleOrPackage importedPackage : importedPackages.list()) {
			String packageName = importedPackage.getName();
			if (packageName.startsWith("org.eclipse.x")) {
				updatedPackages.add(packageName + ";version=\"" + newVersion + "\"");
			}
		}
		mergable.addImportedPackages(updatedPackages.toArray(new String[0]), true);
	}

	private static void updateExportedXtextPackages(MergeableManifest2 mergable, String newVersion) {
		String oldPackages = mergable.getMainAttributes().get(MergeableManifest2.EXPORT_PACKAGE);
		if (oldPackages == null)
			return;
		BundleOrPackageList exportedPackages = BundleOrPackageList.fromInput(oldPackages, mergable.getLineDelimiter(),
				"version");
		List<String> updatedPackages = new ArrayList<>();
		for (BundleOrPackage exportedPackage : exportedPackages.list()) {
			String packageName = exportedPackage.getName();
			updatedPackages.add(packageName + ";version=\"" + newVersion + "\"");
		}
		mergable.addExportedPackages(updatedPackages.toArray(new String[0]), true);
	}
	
	
/*

@SuppressWarnings("restriction")
	private static void m() throws Exception {
		IProvisioningEventBus eventBus = new IProvisioningEventBus() {
			@Override
			public void dispatchEvent(ProvisioningListener eventListener, ProvisioningListener listenerObject,
					int eventAction, EventObject eventObject) {
				// ignore
			}

			@Override
			public void addListener(ProvisioningListener toAdd) {
				// ignore
			}

			@Override
			public void removeListener(ProvisioningListener toRemove) {
				// ignore
			}

			@Override
			public void publishEvent(EventObject event) {
				// ignore
			}

			@Override
			public void close() {
				// ignore
			}
			
		};
		IProvisioningAgent agent = new IProvisioningAgent() {
			
			@Override
			public void unregisterService(String serviceName, Object service) {
				// ignore
			}
			
			@Override
			public void stop() {
				// ignore				
			}
			
			@Override
			public void registerService(String serviceName, Object service) {
				// ignore				
			}
			
			@Override
			public Object getService(String serviceName) {
				if (IProvisioningEventBus.class.getName().equals(serviceName)) {
					return eventBus;
				}
				if (Transport.class.getName().equals(serviceName)) {
					return new RepositoryTransport(this) {
						@Override
						public Map<String, ProtocolRule> getProtocolRules() {
							var rules = new TreeMap<String, ProtocolRule>();
							rules.put("http", ProtocolRule.of(null));
							rules.put("ftp", ProtocolRule.of(null));
							return rules;
						}
						
						@Override
						public IStatus download(URI toDownload, OutputStream target, IProgressMonitor monitor) {
							
							try {
								HttpResponse<byte[]> bytes = HttpClient.newHttpClient().send(HttpRequest.newBuilder(toDownload).build(), BodyHandlers.ofByteArray());
								target.write(bytes.body());
							} catch (IOException e) {
								throw new RuntimeException(e);
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
							return Status.OK_STATUS;
						}
					};
				}
				// TODO Auto-generated method stub
				return null;
			}
		};
		MetadataRepositoryManager repositoryManager = new MetadataRepositoryManager(agent) {
			@Override
			protected String[] getAllSuffixes() {
				return new String[] {
						"content.xml",
						"content.xml.xz",
						"compositeContent.xml"
				};
			}
			@Override
			protected IExtension[] findMatchingRepositoryExtensions(String suffix, String type) {
				return new IExtension[1];
			}
			@Override
			protected IRepository<IInstallableUnit> factoryLoad(URI location, IExtension extension, int flags,
					SubMonitor monitor) throws ProvisionException {
				MetadataRepositoryFactory factory = new org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepositoryFactory();
				factory.setAgent(agent);
				return factory.load(location, flags, monitor);
			}
			@Override
			protected IRepository<IInstallableUnit> factoryCreate(URI location, String name, String type,
					Map<String, String> properties, IExtension extension) throws ProvisionException {
				MetadataRepositoryFactory factory = new org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepositoryFactory();
				factory.setAgent(agent);
				return factory.create(location, name, type, properties);
			}
		};
		URI location = URI.create("https://download.eclipse.org/releases/2024-03");
		try {
			// init the maps inside of a try catch to ignore the missing Activator context
			repositoryManager.getRepository(location);
		} catch(IllegalStateException ignored) {
			//
		}
		URLMetadataRepository repository = new URLMetadataRepository(
				null,
				location,
				null,
				Collections.emptyMap());
		repositoryManager.createRepository(location, "temp", URLMetadataRepository.class.getName(), null);
		repositoryManager.addRepository(repository);
		repositoryManager.refreshRepository(location, null);		
		
		IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
		Set<IInstallableUnit> queryResult = repository.query(query, null).toSet();
		System.out.println(queryResult);
	}

 */
}
