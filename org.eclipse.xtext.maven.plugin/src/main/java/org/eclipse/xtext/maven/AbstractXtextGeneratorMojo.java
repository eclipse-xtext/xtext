/*******************************************************************************
 * Copyright (c) 2018, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.maven;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.LanguageAccessFactory;
import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.builder.standalone.compiler.CompilerConfiguration;
import org.eclipse.xtext.builder.standalone.compiler.IJavaCompiler;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.persistence.IResourceStorageFacade;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

import com.google.common.base.Predicate;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Dennis Huebner - Initial contribution and API
 */
public abstract class AbstractXtextGeneratorMojo extends AbstractXtextMojo {

	/**
	 * File encoding argument for the generator.
	 */
	@Parameter(property = "xtext.encoding", defaultValue = "${project.build.sourceEncoding}")
	private String encoding;

	public String getEncoding() {
		return encoding;
	}

	/**
	 * Location of the generated source files.
	 */
	@Parameter(defaultValue = "${project.build.directory}/xtext-temp")
	private String tmpClassDirectory;

	/**
	 * Java source roots. List of folders, where the java source files are
	 * located.<br>
	 * The default value is a reference to the project's
	 * ${project.compileSourceRoots}.<br>
	 * When adding a new entry the default value will be overwritten not
	 * extended.<br>
	 * Used when your language needs java.
	 */
	@Parameter(defaultValue = "${project.compileSourceRoots}", required = true)
	private List<String> javaSourceRoots;

	/**
	 * you can specify a list of project mappings that is used to populate the EMF
	 * Platform Resource Map.
	 * 
	 * <pre>
		&lt;projectMappings&gt;
			&lt;projectMapping&gt;
				&lt;projectName&gt;sample.emf&lt;/projectName&gt;
				&lt;path&gt;${project.basedir}&lt;/path&gt;
			&lt;/projectMapping&gt;
			&lt;projectMapping&gt;
				&lt;projectName&gt;sample.emf.edit&lt;/projectName&gt;
				&lt;path&gt;${project.basedir}/../sample.emf.edit&lt;/path&gt;
			&lt;/projectMapping&gt;
		&lt;/projectMappings&gt;
	 * </pre>
	 */
	@Parameter
	private List<ProjectMapping> projectMappings;

	@Parameter(property = "xtext.generator.skip", defaultValue = "false")
	private boolean skip;

	@Parameter(defaultValue = "true")
	private boolean failOnValidationError = true;

	@Parameter(property = "maven.compiler.source", defaultValue = "17")
	private String compilerSourceLevel;

	@Parameter(property = "maven.compiler.target", defaultValue = "17")
	private String compilerTargetLevel;

	@Parameter(defaultValue = "false")
	private boolean compilerSkipAnnotationProcessing;

	@Parameter(defaultValue = "false")
	private boolean compilerPreserveInformationAboutFormalParameters;

	/**
	 * RegEx expression to filter class path during model files look up
	 */
	@Parameter
	private String classPathLookupFilter;

	/**
	 * Clustering configuration to avoid OOME
	 */
	@Parameter
	private ClusteringConfig clusteringConfig;

	/**
	 * if enabled the plugin will scan the project and its siblings and add them to
	 * the platform resource map automatically
	 */
	@Parameter(defaultValue = "false")
	private boolean autoFillPlatformResourceMap;

	/**
	 * Automatically add all output directories of all configured languages to the
	 * compile source roots needed by maven-compiler
	 */
	@Parameter(defaultValue = "true")
	private boolean addOutputDirectoriesToCompileSourceRoots = true;

	/**
	 * Track dependencies between model files and perform incremental builds when re-running
	 * the build after some files were touched.
	 */
	@Parameter(defaultValue = "false")
	private boolean incrementalXtextBuild;

	/**
	 * Whether to use the {@link StorageAwareResource} to write the semantic model, the
	 * {@link IResourceDescription resource description} and optionally the node model to a ResourceStorage.<br>
	 * For details see {@link StorageAwareResource} and {@link IResourceStorageFacade}.
	 * 
	 * @since 2.35
	 */
	@Parameter(defaultValue = "false")
	private boolean writeStorageResources;
	
	@Parameter( readonly = true, defaultValue = "${plugin.artifacts}" )
	private List<Artifact> pluginDependencies;

	/**
	 * This way, a DSL file can refer to other DSL libraries only during compilation,
	 * by adding a dependency in the plugin section, not in the project.
	 */
	@Parameter(defaultValue = "false")
	private boolean includePluginDependencies;
	
	/**
	 * Location to which the class-path configuration shall be written. The file format is internal
	 * to the {@link StandaloneBuilder}.
	 * @see #writeClassPathConfigurationLocation
	 */
	@Parameter(defaultValue = "${project.build.directory}/xtext.classpath")
	private String classpathConfigurationLocation;
	
	/**
	 * Allows to write the class-path configuration to a file. The file format is internal
	 * to the {@link StandaloneBuilder}.
	 * @see #classpathConfigurationLocation
	 */
	@Parameter(defaultValue = "false")
	private boolean writeClasspathConfiguration = false;
	
	@Parameter( defaultValue = "${mojoExecution}", readonly = true )
	private MojoExecution mojoExecution;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("skipped.");
		} else {
			synchronized (lock) {
				new MavenLog4JConfigurator().configureLog4j(getLog());
				autoAddToPlatformResourceMap(getProject());
				manuallyAddToPlatformResourceMap();
				internalExecute();
			}
		}
	}

	protected void internalExecute() throws MojoExecutionException {
		if (addOutputDirectoriesToCompileSourceRoots) {
			configureMavenOutputs();
		}
		Map<String, LanguageAccess> languages = new LanguageAccessFactory().createLanguageAccess(getLanguages(),
				this.getClass().getClassLoader());
		Injector injector = Guice.createInjector(new MavenStandaloneBuilderModule());
		StandaloneBuilder builder = injector.getInstance(StandaloneBuilder.class);
		builder.setBaseDir(getProject().getBasedir().getAbsolutePath());
		builder.setLanguages(languages);
		builder.setEncoding(getEncoding());
		builder.setClassPathEntries(getClasspathEntries());
		builder.setClassPathLookUpFilter(classPathLookupFilter);
		builder.setSourceDirs(getSourceRoots());
		builder.setJavaSourceDirs(javaSourceRoots);
		builder.setFailOnValidationError(failOnValidationError);
		builder.setTempDir(createTempDir().getAbsolutePath());
		builder.setDebugLog(getLog().isDebugEnabled());
		builder.setIncrementalBuild(incrementalXtextBuild);
		builder.setWriteStorageResources(writeStorageResources);
		if (writeClasspathConfiguration) {
			builder.setClasspathConfigurationLocation(classpathConfigurationLocation, mojoExecution.getGoal(), getClassOutputDirectory());
		}
		if (clusteringConfig != null) {
			builder.setClusteringConfig(clusteringConfig.convertToStandaloneConfig());
		}
		configureCompiler(builder.getCompiler());
		logState();
		boolean errorDetected = !builder.launch();
		if (errorDetected && failOnValidationError) {
			throw new MojoExecutionException("Execution failed due to a severe validation error.");
		}
	}

	protected abstract String getClassOutputDirectory();
	
	protected abstract List<String> getSourceRoots();

	private void configureCompiler(IJavaCompiler compiler) {
		CompilerConfiguration conf = compiler.getConfiguration();
		conf.setSourceLevel(compilerSourceLevel);
		conf.setTargetLevel(compilerTargetLevel);
		conf.setVerbose(getLog().isDebugEnabled());
		conf.setSkipAnnotationProcessing(compilerSkipAnnotationProcessing);
		conf.setPreserveInformationAboutFormalParameters(compilerPreserveInformationAboutFormalParameters);
	}

	private void logState() {
		getLog().info(
				"Encoding: " + (getEncoding() == null ? "not set. Encoding provider will be used." : getEncoding()));
		getLog().info("Compiler source level: " + compilerSourceLevel);
		getLog().info("Compiler target level: " + compilerTargetLevel);
		if (getLog().isDebugEnabled()) {
			getLog().debug("Source dirs: " + IterableExtensions.join(getSourceRoots(), ", "));
			getLog().debug("Java source dirs: " + IterableExtensions.join(javaSourceRoots, ", "));
			getLog().debug("Classpath entries: " + IterableExtensions.join(getClasspathEntries(), ", "));
		}
	}

	private File createTempDir() {
		File tmpDir = new File(tmpClassDirectory + tmpDirSuffix());
		if (!tmpDir.mkdirs() && !tmpDir.exists()) {
			throw new IllegalArgumentException("Couldn't create directory '" + tmpClassDirectory + "'.");
		}
		return tmpDir;
	}

	protected String tmpDirSuffix() {
		return "";
	}

	protected Predicate<String> emptyStringFilter() {
		return new Predicate<String>() {
			public boolean apply(String input) {
				return !Strings.isEmpty(input.trim());
			}
		};
	}

	private Set<String> getClasspathEntries() {
		Set<String> classpathElements = getClasspathElements();
		if (isIncludePluginDependencies()) {
			getLog().info("Including plugin dependencies");
			List<String> pluginClasspathElements = pluginDependencies.stream()
					.map(e -> e.getFile().toPath().toString())
					.collect(Collectors.toList());
			classpathElements.addAll(pluginClasspathElements);
		}
		return classpathElements;
	}

	/**
	 * @return resolved class path elements
	 */
	public abstract Set<String> getClasspathElements();

	protected abstract void configureMavenOutputs();

	public List<ProjectMapping> getProjectMappings() {
		return projectMappings;
	}

	public void setProjectMappings(List<ProjectMapping> projectMappings) {
		this.projectMappings = projectMappings;
	}

	/**
	 * Adds the given project and its child modules
	 * ({@link MavenProject#getModules()}) to
	 * {@link EcorePlugin#getPlatformResourceMap()}. Furthermore, this traverses
	 * {@link MavenProject#getParent()} recursively, if set.
	 *
	 * @param project the project
	 */
	private void autoAddToPlatformResourceMap(final MavenProject project) {
		if (autoFillPlatformResourceMap) {
			addToPlatformResourceMap(project.getBasedir());
			project.getModules().stream().map(module -> new File(project.getBasedir(), module))
					.forEach(this::addToPlatformResourceMap);

			if (project.getParent() != null)
				autoAddToPlatformResourceMap(project.getParent());
		}
	}

	private void manuallyAddToPlatformResourceMap() {
		if (projectMappings != null) {
			for (ProjectMapping projectMapping : projectMappings) {
				if (projectMapping.getPath() != null && projectMapping.getProjectName() != null) {
					String path = projectMapping.getPath().toURI().toString();
					String name = projectMapping.getProjectName();
					getLog().info("Adding project '" + name + "' with path '" + path + "' to Platform Resource Map");
					final URI uri = URI.createURI(path);
					EcorePlugin.getPlatformResourceMap().put(name, uri);
				}
			}
		}
	}

	/**
	 * Adds the given file to EcorePlugin's platform resource map. The file's name
	 * will be used as the map entry's key.
	 *
	 * @param file a file to register
	 * @return the registered URI pointing to the given file
	 * @see EcorePlugin#getPlatformResourceMap()
	 */
	private URI addToPlatformResourceMap(final File file) {
		getLog().info("Adding project '" + file.getName() + "' with path '" + file.toURI().toString()
				+ "' to Platform Resource Map");
		final URI uri = URI.createURI(file.toURI().toString());
		return EcorePlugin.getPlatformResourceMap().put(file.getName(), uri);
	}

	protected boolean isIncludePluginDependencies() {
		return includePluginDependencies;
	}
}