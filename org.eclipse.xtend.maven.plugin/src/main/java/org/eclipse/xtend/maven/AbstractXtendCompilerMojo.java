/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.maven;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.xtext.util.Strings.concat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Michael Clay - Initial contribution and API
 */
public abstract class AbstractXtendCompilerMojo extends AbstractXtendMojo {
	protected static final Predicate<String> FILE_EXISTS = new Predicate<String>() {

		public boolean apply(String filePath) {
			return new File(filePath).exists();
		}
	};

	/**
	 * Create Java Source Code that is compatible to this Java version.
	 * 
	 * Supported values: 11, 17, 21 and so forth
	 */
	@Parameter(property="maven.compiler.source", defaultValue="11")
	private String javaSourceVersion;

	/**
	 * Create Java Source Code that targets this Java release.
	 * <p>
	 * If set, this implies a {@link #javaSourceVersion} of the same value and any value explicitly configured for the latter is ignored.
	 * Nevertheless specifying this parameter does not enforce strict Java API checks like the {@code release} option for a Java compiler
	 * does. Still it is possible to enforce strict Java API checks by specifying the {@code release} option for the compiler-plugin.
	 * </p>
	 * 
	 * Supported values: 11, 17, 21 and so forth
	 */
	@Parameter(property = "maven.compiler.release")
	private String javaReleaseVersion;

	/**
	 * The current build session instance. This is used for toolchain manager API calls.
	 */
	@Parameter(property="session", required=true, readonly=true)
	private MavenSession session;

	/**
	 * Xtend-File encoding argument for the compiler.
	 */
	@Parameter(property="encoding", defaultValue="${project.build.sourceEncoding}")
	private String encoding;

	/**
	 * Set this to false to suppress the creation of *._trace files.
	 */
	@Parameter(property="writeTraceFiles", defaultValue="true")
	private boolean writeTraceFiles;

	/**
	 * Location of the Xtend settings file.
	 */
	@Parameter(defaultValue="${basedir}/.settings/org.eclipse.xtend.core.Xtend.prefs", readonly=true)
	private String propertiesFileLocation;

	/**
	 * Whether <code>@SuppressWarnings</code> shall be generated for non-nested types.
	 */
	@Parameter(defaultValue="true")
	private boolean generateSyntheticSuppressWarnings;

	/**
	 * Whether <code>@XbaseGenerated</code> shall be generated for synthetic members.
	 * @since 2.36
	 */
	@Parameter(defaultValue="true")
	private boolean useXbaseGenerated;

	/**
	 * Whether <code>@Generated</code> shall be generated for non-nested types.
	 */
	@Parameter(defaultValue="false")
	private boolean generateGeneratedAnnotation;

	/**
	 * Whether the current time shall be added to <code>@Generated</code> annotations.
	 */
	@Parameter(defaultValue="false")
	private boolean includeDateInGeneratedAnnotation;

	/**
	 * The comment that is added to <code>@Generated</code> annotations, also supports variables:
	 * <ul>
	 * <li><code>${sourcefile}</code></li>
	 * </ul>
	 */
	@Parameter
	private String generatedAnnotationComment;
	
	/**
	 * Additional Java compiler arguments for the internal pre-built step of the XtendBatchCompiler.
	 */
	@Parameter(defaultValue="-proc:none")
	private String additionalPreCompileArgs;

	@Inject
	private Provider<XtendBatchCompiler> xtendBatchCompilerProvider;

	protected XtendBatchCompiler getBatchCompiler() {
		return xtendBatchCompilerProvider.get();
	}

	protected void compile(String classPath, List<String> sourcePaths, String outputPath) throws MojoExecutionException {
		XtendBatchCompiler compiler = getBatchCompiler();
		Log log = getLog();
		compiler.setResourceSetProvider(new MavenProjectResourceSetProvider(project));
		Iterable<String> filtered = filter(sourcePaths, FILE_EXISTS);
		if (Iterables.isEmpty(filtered)) {
			String dir = Iterables.toString(sourcePaths);
			log.info("skip compiling sources because the configured directory '" + dir + "' does not exist.");
			return;
		}
		String baseDir = project.getBasedir().getAbsolutePath();
		String sourceVersion = javaReleaseVersion != null && !javaReleaseVersion.isBlank() ? javaReleaseVersion : javaSourceVersion;
		log.debug("Set Java Compliance Level: " + sourceVersion);
		compiler.setJavaSourceVersion(sourceVersion);
		log.debug("Set generateSyntheticSuppressWarnings: " + generateSyntheticSuppressWarnings);
		compiler.setGenerateSyntheticSuppressWarnings(generateSyntheticSuppressWarnings);
		log.debug("Set useXbaseGenerated: " + useXbaseGenerated);
		compiler.setUseXbaseGenerated(useXbaseGenerated);
		log.debug("Set generateGeneratedAnnotation: " + generateGeneratedAnnotation);
		compiler.setGenerateGeneratedAnnotation(generateGeneratedAnnotation);
		log.debug("Set includeDateInGeneratedAnnotation: " + includeDateInGeneratedAnnotation);
		compiler.setIncludeDateInGeneratedAnnotation(includeDateInGeneratedAnnotation);
		log.debug("Set generatedAnnotationComment: " + generatedAnnotationComment);
		compiler.setGeneratedAnnotationComment(generatedAnnotationComment);
		log.debug("Set baseDir: " + baseDir);
		compiler.setBasePath(baseDir);
		log.debug("Set temp directory: " + getTempDirectory());
		compiler.setTempDirectory(getTempDirectory());
		log.debug("Set DeleteTempDirectory: " + false);
		compiler.setDeleteTempDirectory(false);
		log.debug("Set classpath: " + classPath);
		compiler.setClassPath(classPath);
		log.debug("Set source path: " + concat(File.pathSeparator, newArrayList(filtered)));
		compiler.setSourcePath(concat(File.pathSeparator, newArrayList(filtered)));
		log.debug("Set output path: " + outputPath);
		compiler.setOutputPath(outputPath);
		log.debug("Set encoding: " + encoding);
		compiler.setFileEncoding(encoding);
		log.debug("Set writeTraceFiles: " + writeTraceFiles);
		compiler.setWriteTraceFiles(writeTraceFiles);
		log.debug("Set additional precompile args: " + additionalPreCompileArgs);
		compiler.setAdditionalPreCompileArgs(additionalPreCompileArgs);
		if (!compiler.compile()) {
			String dir = concat(File.pathSeparator, newArrayList(filtered));
			throw new MojoExecutionException("Error compiling xtend sources in '" + dir + "'.");
		}
	}

	protected abstract String getTempDirectory();

	protected void readXtendEclipseSetting(String sourceDirectory, Procedure1<String> fieldSetter) {
		if (propertiesFileLocation != null) {
			File f = new File(propertiesFileLocation);
			if (f.canRead()) {
				Properties xtendSettings = new Properties();
				try {
					xtendSettings.load(new FileInputStream(f));
					// TODO read Xtend setup to compute the properties file loc and property name
					String xtendOutputDirProp = xtendSettings.getProperty("outlet.DEFAULT_OUTPUT.directory", null);
					if (xtendOutputDirProp != null) {
						File srcDir = new File(sourceDirectory);
						getLog().debug("Source dir : " + srcDir.getPath() + " exists " + srcDir.exists());
						if (srcDir.exists() && srcDir.getParent() != null) {
							String path = new File(srcDir.getParent(), xtendOutputDirProp).getPath();
							getLog().debug("Applying Xtend property: " + xtendOutputDirProp);
							fieldSetter.apply(path);
						}
					}
				} catch (FileNotFoundException e) {
					getLog().warn(e);
				} catch (IOException e) {
					getLog().warn(e);
				}
			} else {
				getLog().info(
						"Can't find Xtend properties under " + propertiesFileLocation + ", maven defaults are used.");
			}
		}
	}

	protected String resolveToBaseDir(final String directory) throws MojoExecutionException {
		File outDir = new File(directory);
		if (!outDir.isAbsolute()) {
			outDir = new File(project.getBasedir(), directory);
		}
		return outDir.getAbsolutePath();
	}
}
