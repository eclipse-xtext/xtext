/*******************************************************************************
 * Copyright (c) 2018, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xtext.wizard.cli;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xtext.wizard.BuildSystem;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor;
import org.eclipse.xtext.xtext.wizard.LanguageServer;
import org.eclipse.xtext.xtext.wizard.LineDelimiter;
import org.eclipse.xtext.xtext.wizard.ProjectLayout;
import org.eclipse.xtext.xtext.wizard.SourceLayout;
import org.eclipse.xtext.xtext.wizard.WizardConfiguration;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * 
 * @author Karsten Thoms (karsten.thoms@itemis.de)
 * @since 2.15
 */
public class CliProjectsCreatorMain {
	public enum ProjectType {
		GenericIDE,
		UIProject,
		RuntimeTest,
		UITest,
		Web,
		P2Project;
	}

	private final static Logger LOG = Logger.getLogger(CliProjectsCreatorMain.class);

	@Option(name = "-targetDir", usage = "Output directory")
	private File targetDir = new File(".");

	@Option(name = "-languageName", usage = "Language name (qualified name, e.g. org.xtext.example.mydsl.MyDsl)", required = true)
	private String languageName;

	@Option(name = "-baseName", usage = "Language base name (used for name of the root directory at target location)", required = true)
	private String baseName;

	@Option(name = "-extension", usage = "Language\'s file extension (for multiple use comma separated list)", required = true)
	private String fileExtension;

	@Option(name = "-encoding", usage = "File encoding")
	private String encoding = StandardCharsets.UTF_8.name();

	@Option(name = "-xtextVersion", usage = "Xtext Version (for code header comments)")
	private String xtextVersion = "unspecified";

	@Option(name = "-buildSystem", usage = "Preferred build system", handler = OptionHandlers.BuildSystemOptionHandler.class)
	private BuildSystem buildSystem = BuildSystem.NONE;

	@Option(name = "-sourceLayout", usage = "Source Layout", handler = OptionHandlers.SourceLayoutOptionHandler.class)
	private SourceLayout sourceLayout = SourceLayout.PLAIN;

	@Option(name = "-projectLayout", usage = "Project Layout", handler = OptionHandlers.ProjectLayoutOptionHandler.class)
	private ProjectLayout projectLayout = ProjectLayout.HIERARCHICAL;

	@Option(name = "-languageServer", usage = "Language Server", handler = OptionHandlers.LanguageServerOptionHandler.class)
	private LanguageServer languageServer = LanguageServer.NONE;

	@Option(name = "-javaVersion", usage = "Java Version", handler = OptionHandlers.JavaVersionOptionHandler.class)
	private JavaVersion javaVersion = JavaVersion.JAVA21;

	@Option(name = "-junitVersion", usage = "JUnit Version", handler = OptionHandlers.JUnitVersionOptionHandler.class)
	private JUnitVersion junitVersion = JUnitVersion.JUNIT_5;

	@Option(name = "-lineDelimiter", usage = "Line delimiter style", handler = OptionHandlers.LineDelimiterOptionHandler.class)
	private LineDelimiter delimiter = LineDelimiter.UNIX;

	@Option(name = "-enabledProjects", usage = "Comma separated list of projects to enable", required = true, handler = OptionHandlers.MultiProjectTypeOptionHandler.class)
	private List<ProjectType> enabledProjects;

	public static void main(final String[] args) {
		CliProjectsCreatorMain bean = new CliProjectsCreatorMain();
		CliProjectsCreator projectsCreator = new CliProjectsCreator();
		CmdLineParser parser = new CmdLineParser(bean);
		try {
			parser.parseArgument(args);
			final WizardConfiguration config = bean.createProjectConfig();
			projectsCreator.setLineDelimiter(config.getLineDelimiter());
			projectsCreator.createProjects(config);
			LOG.info(String.format("Created projects for language %s in %s", config.getLanguage().getName(), config.getRootLocation()));
		} catch (final Throwable _t) {
			if (_t instanceof CmdLineException) {
				final CmdLineException e = (CmdLineException) _t;
				System.err.println(e.getMessage());
				parser.printUsage(System.err);
			} else {
				throw Exceptions.sneakyThrow(_t);
			}
		}
	}

	private WizardConfiguration createProjectConfig() {
		WizardConfiguration cfg = new WizardConfiguration();
		cfg.setXtextVersion(new XtextVersion(this.xtextVersion));
		cfg.setRootLocation(this.targetDir.getAbsolutePath());
		cfg.setBaseName(this.baseName);
		cfg.setPreferredBuildSystem(this.buildSystem);
		cfg.setSourceLayout(this.sourceLayout);
		cfg.setProjectLayout(this.projectLayout);
		cfg.setJavaVersion(this.javaVersion);
		cfg.setJunitVersion(this.junitVersion);
		cfg.setEncoding(Charset.forName(this.encoding));
		cfg.setLineDelimiter(this.delimiter.getValue());
		
		LanguageDescriptor langDesc = cfg.getLanguage();
			langDesc.setName(this.languageName);
			langDesc.setFileExtensions(LanguageDescriptor.FileExtensions.fromString(this.fileExtension));

		
		for (ProjectType projectType : enabledProjects) {
			switch (projectType) {
			case RuntimeTest: 
				cfg.getRuntimeProject().getTestProject().setEnabled(true);
				break;
			case UIProject:
				cfg.getUiProject().setEnabled(true);
				break;
			case UITest:
				cfg.getUiProject().getTestProject().setEnabled(true);
				break;
			case GenericIDE:
				cfg.getIdeProject().setEnabled(true);
				break;
			case Web:
				cfg.getWebProject().setEnabled(true);
				break;
			case P2Project:
				cfg.getP2Project().setEnabled(true);
				break;
			}
		}

		return cfg;
	}

}
