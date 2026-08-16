/*******************************************************************************
 * Copyright (c) 2013, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.performance.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.xtext.builder.nature.ToggleXtextNatureCommand;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class PerformanceTestProjectSetup {

	public static IJavaProject testProject;
	
	public static void setUp() throws Exception {
		testProject = createJavaProject("performance.test.project",
				new String[] {
						JavaCore.NATURE_ID,
						"org.eclipse.pde.PluginNature"
				}
		);
		new ToggleXtextNatureCommand().toggleNature(testProject.getProject());
		IFolder sourceFolder = JavaProjectSetupUtil.addSourceFolder(testProject, "src");
		JavaProjectSetupUtil.addSourceFolder(testProject, "xtend-gen");

		List<String> filesToCopy = readResource("/files.list");
		
		for(String fileToCopy: filesToCopy) {
			IPath filePath = new Path(fileToCopy);
			IFolder packageFolder = sourceFolder.getFolder(filePath.removeLastSegments(1));
			if (!packageFolder.exists())
				createFolderRecursively(packageFolder);
			List<String> content = readResource(fileToCopy);
			String contentAsString = Strings.concat("\n", content);
			String fileName = filePath.lastSegment();
			createFile(fileName.substring(0, fileName.length() - ".txt".length()), packageFolder, contentAsString);
		}
		waitForBuild();
	}

	protected static void createFolderRecursively(IFolder folder) throws CoreException {
		if (!folder.getParent().exists())
			createFolderRecursively((IFolder) folder.getParent());
		folder.create(true, true, null);
	}
	
	public static List<String> readResource(String name) throws Exception {
		InputStream stream = PerformanceTestProjectSetup.class.getResourceAsStream(name);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			String line = null;
			List<String> result = Lists.newArrayList();
			while( (line = reader.readLine()) != null) {
				result.add(line);
			}
			return result;
		} finally {
			reader.close();
		}
	}

	public static void tearDown() throws Exception {
		if (testProject != null)
			deleteJavaProject(testProject);
		testProject = null;
	}
	
	public static IJavaProject createJavaProject(
			final String projectName, 
			String[] projectNatures) {

		IProject project = null;
		IJavaProject javaProject = null;
		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			project = workspace.getRoot().getProject(projectName);
			deleteProject(project);

			javaProject = JavaCore.create(project);
			IProjectDescription projectDescription = ResourcesPlugin.getWorkspace().newProjectDescription(
					projectName);
			project.create(projectDescription, null);
			List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
			projectDescription.setNatureIds(projectNatures);

			final ICommand java = projectDescription.newCommand();
			java.setBuilderName(JavaCore.BUILDER_ID);

			final ICommand manifest = projectDescription.newCommand();
			manifest.setBuilderName("org.eclipse.pde.ManifestBuilder");

			final ICommand schema = projectDescription.newCommand();
			schema.setBuilderName("org.eclipse.pde.SchemaBuilder");

			projectDescription.setBuildSpec(new ICommand[] { java, manifest, schema });

			project.open(null);
			project.setDescription(projectDescription, null);

			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21")));
			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));

			javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]),
					null);
			
			makeJava21Compliant(javaProject);

			javaProject.setOutputLocation(new Path("/" + projectName + "/bin"), null);
			createManifest(projectName, project);
//			project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			refreshExternalArchives(javaProject);
			refresh(javaProject);
		}
		catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
		return javaProject ;
	}

	public static void makeJava21Compliant(IJavaProject javaProject) {
		Map<String, String> options= javaProject.getOptions(false);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_21);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_21);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_21);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		javaProject.setOptions(options);
	}
	
	protected static void refreshExternalArchives(IJavaProject p) throws JavaModelException {
		IResourcesSetupUtil.waitForBuild(); // ensure that the auto-build job doesn't interfere with external jar refreshing
		getJavaModel().refreshExternalArchives(new IJavaElement[] {p}, null);
	}
	
	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForBuild() {
		IResourcesSetupUtil.waitForBuild();
	}
	
	/**
	 * Returns the Java Model this test suite is running on.
	 */
	public static IJavaModel getJavaModel() {
		return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	public static void refresh(final IJavaProject javaProject) throws CoreException {
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
//		waitForManualRefresh();
	}
	
//	public static void waitForManualRefresh() {
//		boolean wasInterrupted = false;
//		do {
//			try {
//				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_REFRESH, null);
//				wasInterrupted = false;
//			} catch (OperationCanceledException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				wasInterrupted = true;
//			}
//		} while (wasInterrupted);
//	}
	
	private static void createManifest(final String projectName, final IProject project) throws CoreException {
		final StringBuilder mainContent = new StringBuilder("Manifest-Version: 1.0\n");
		mainContent.append("Bundle-ManifestVersion: 2\n");
		mainContent.append("Bundle-Name: " + projectName + "\n");
		mainContent.append("Bundle-Vendor: My Company\n");
		mainContent.append("Bundle-Version: 1.0.0\n");
		mainContent.append("Bundle-SymbolicName: " + projectName.toLowerCase() + "; singleton:=true\n");
		mainContent.append("Bundle-ActivationPolicy: lazy\n");
		mainContent.append("Bundle-RequiredExecutionEnvironment: JavaSE-1.8\n");
		mainContent.append("Require-Bundle: com.google.guava,\n");
		mainContent.append("  org.apache.log4j,\n");
		mainContent.append("  org.eclipse.xtend.lib,\n");
		mainContent.append("  org.eclipse.xtext,\n");
		mainContent.append("  org.eclipse.xtext.xbase\n");

		final IFolder metaInf = project.getFolder("META-INF");
		metaInf.create(false, true, null);
		createFile("MANIFEST.MF", metaInf, mainContent.toString());
	}
	
	public static IFile createFile(final String name, final IContainer container, final String content) {
		final IFile file = container.getFile(new Path(name));
		try {
			final InputStream stream = new ByteArrayInputStream(content.getBytes(file.getCharset()));
			if (file.exists()) {
				file.setContents(stream, true, true, null);
			}
			else {
				file.create(stream, true, null);
			}
			stream.close();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
		return file;
	}

	public static void deleteJavaProject(IJavaProject javaProject) throws CoreException {
		IProject project = javaProject.getProject();
		deleteProject(project);
	}

	public static void deleteProject(IProject project) throws CoreException {
		if (project.exists()) {
			project.delete(true, true, null);
		}
	}

}