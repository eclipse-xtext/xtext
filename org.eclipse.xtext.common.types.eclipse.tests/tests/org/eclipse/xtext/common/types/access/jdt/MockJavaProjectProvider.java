/*******************************************************************************
 * Copyright (c) 2009, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.jdt;

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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.xtext.common.types.eclipse.tests.internal.TestsActivator;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.testing.util.PluginUtil;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class MockJavaProjectProvider implements IJavaProjectProvider {

	private static IJavaProject javaProject;
	
	private static IJavaProject javaProjectWithSources;

	private boolean useSources;
	
	@Override
	public IJavaProject getJavaProject(ResourceSet resourceSet) {
		if (javaProject == null || javaProjectWithSources == null)
			throw new IllegalStateException("javaProject is null || javaProjectWithSources == null");
		return useSources ? javaProjectWithSources : javaProject;
	}
	
	public void setUseSource(boolean useSources) {
		this.useSources = useSources;
	}

	public static void setUp() throws Exception {
		if(javaProject != null)
			return;
		javaProject = createJavaProject("projectWithoutSources",
				new String[] {
						JavaCore.NATURE_ID,
						"org.eclipse.pde.PluginNature"
				}
		);
		IResourcesSetupUtil.waitForBuild();
		String path = "/org/eclipse/xtext/common/types/testSetups";
		String jarFileName = "/testData.jar";
		IFile jarFile = PluginUtil.copyFileToWorkspace(TestsActivator.getInstance(), path + jarFileName, javaProject.getProject(), 
				jarFileName);
		JavaProjectSetupUtil.addJarToClasspath(javaProject, jarFile);
		IResourcesSetupUtil.waitForBuild();
		javaProjectWithSources = createJavaProject("projectWithSources",
				new String[] {
						JavaCore.NATURE_ID,
						"org.eclipse.pde.PluginNature"
				}
		);
		IResourcesSetupUtil.waitForBuild();
		IFolder sourceFolder = JavaProjectSetupUtil.addSourceFolder(javaProjectWithSources, "src");
		
		List<String> filesToCopy = readResource(path + "/files.list");
		IFolder srcFolder = sourceFolder.getFolder(new Path(path));
		createFolderRecursively(srcFolder);
		for(String fileToCopy: filesToCopy) {
			List<String> content = readResource(path + "/" + fileToCopy);
			String contentAsString = Strings.concat("\n", content);
			createFile(fileToCopy.substring(0, fileToCopy.length() - ".txt".length()), srcFolder, contentAsString);
		}
		createFile("ClassWithDefaultPackage.java", sourceFolder, "public class ClassWithDefaultPackage {}");
		PreferenceConstants.getPreferenceStore().putValue(PreferenceConstants.TYPEFILTER_ENABLED, "*.javafx.*;");
		IResourcesSetupUtil.waitForBuild();
	}

	protected static void createFolderRecursively(IFolder folder) throws CoreException {
		if (!folder.getParent().exists())
			createFolderRecursively((IFolder) folder.getParent());
		folder.create(true, true, null);
	}
	
	public static List<String> readResource(String name) throws Exception {
		InputStream stream = MockJavaProjectProvider.class.getResourceAsStream(name);
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
		if (javaProject != null)
			deleteJavaProject(javaProject);
		if (javaProject != null)
			deleteJavaProject(javaProjectWithSources);
	}
	
	public static IJavaProject createJavaProject(
			final String projectName, 
			String[] projectNatures) {

		IProject project = null;
		IJavaProject javaProject = null;
		try {
			project = IResourcesSetupUtil.root().getProject(projectName);
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

			classpathEntries.add(JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins")));

			javaProject.setRawClasspath(classpathEntries.toArray(new IClasspathEntry[classpathEntries.size()]),
					null);
			JavaProjectSetupUtil.addJreClasspathEntry(javaProject);
			
			makeJava8Compliant(javaProject);

			javaProject.setOutputLocation(new Path("/" + projectName + "/bin"), null);
			createManifest(projectName, project);

			refreshExternalArchives(javaProject);
			refresh(javaProject);
		}
		catch (final Exception exception) {
			throw new RuntimeException(exception);
		}
		return javaProject ;
	}

	public static void makeJava8Compliant(IJavaProject javaProject) {
		Map<String, String> options= javaProject.getOptions(false);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		javaProject.setOptions(options);
	}
	
	protected static void refreshExternalArchives(IJavaProject p) throws JavaModelException {
		IResourcesSetupUtil.waitForBuild();
		getJavaModel().refreshExternalArchives(new IJavaElement[] {p}, null);
	}
	
	/**
	 * Returns the Java Model this test suite is running on.
	 */
	public static IJavaModel getJavaModel() {
		return JavaCore.create(IResourcesSetupUtil.root());
	}
	
	public static void refresh(final IJavaProject javaProject) throws CoreException {
		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	private static void createManifest(final String projectName, final IProject project) throws CoreException {
		final StringBuilder mainContent = new StringBuilder("Manifest-Version: 1.0\n");
		mainContent.append("Bundle-ManifestVersion: 2\n");
		mainContent.append("Bundle-Name: " + projectName + "\n");
		mainContent.append("Bundle-Vendor: My Company\n");
		mainContent.append("Bundle-Version: 1.0.0\n");
		mainContent.append("Bundle-SymbolicName: " + projectName.toLowerCase() + "; singleton:=true\n");
		mainContent.append("Bundle-ActivationPolicy: lazy\n");
		mainContent.append("Bundle-RequiredExecutionEnvironment: " + JREContainerProvider.PREFERRED_BREE + "\n");
		mainContent.append("Require-Bundle: com.google.guava\n");

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
