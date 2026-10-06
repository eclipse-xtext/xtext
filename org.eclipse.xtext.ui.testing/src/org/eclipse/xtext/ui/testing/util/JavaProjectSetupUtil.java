/*******************************************************************************
 * Copyright (c) 2009, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.testing.util;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.launching.environments.IExecutionEnvironmentsManager;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.RuntimeIOException;
import org.eclipse.xtext.util.Wrapper;
import org.eclipse.xtext.xbase.lib.Pair;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sven Efftinge
 * 
 * @since 2.12
 */
public class JavaProjectSetupUtil {

	@Deprecated
	private static final String JRE_CONTAINER_1_5 = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/J2SE-1.5";

	public static class TextFile {
		public TextFile(String path, String content) {
			this.path = path;
			this.content = content;
		}

		public String path;
		public String content;
	}

	/**
	 * creates a JarInputStream containing the passed text files. Each Pair<String
	 */
	public static InputStream jarInputStream(TextFile... files) {
		try (ByteArrayOutputStream out2 = new ByteArrayOutputStream();
				JarOutputStream jo = new JarOutputStream(new BufferedOutputStream(out2))) {
			for (TextFile textFile : files) {
				JarEntry je = new JarEntry(textFile.path);
				jo.putNextEntry(je);
				byte[] bytes = textFile.content.getBytes();
				jo.write(bytes, 0, bytes.length);
			}
			jo.close();
			return new ByteArrayInputStream(out2.toByteArray());
		} catch (IOException e) {
			throw new WrappedException(e);
		}
	}
	
	public static InputStream jarInputStream(@SuppressWarnings("unchecked") Pair<String, InputStream> ...entries) {
		try (ByteArrayOutputStream out2 = new ByteArrayOutputStream(); JarOutputStream jo = new JarOutputStream(new BufferedOutputStream(out2))) {
			for (Pair<String, InputStream> entry : entries) {
				JarEntry je = new JarEntry(entry.getKey());
				jo.putNextEntry(je);
				ByteStreams.copy(entry.getValue(), jo);
			}
			jo.close();
			return new ByteArrayInputStream(out2.toByteArray());
		} catch (IOException e) {
			throw new WrappedException(e);
		}
	}

	public static IJavaProject createJavaProject(String projectName) throws CoreException {
		return createJavaProject(projectName, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static IJavaProject createJavaProject(String projectName, boolean build) throws CoreException {
		IProject project = createSimpleProject(projectName);
		JavaCore.initializeAfterLoad(monitor());
		IJavaProject javaProject = makeJavaProject(project, build);
		return javaProject;
	}
	
	public static IJavaProject findJavaProject(String projectName) {
		IProject project = root().getProject(projectName);
		if (project != null)
			return JavaCore.create(project);
		return null;
	}

	public static IProject createSimpleProject(String projectName) throws CoreException {
		IProject project = root().getProject(projectName);
		deleteProject(project);
		project.create(null);
		project.open(null);
		return project;
	}

	public static IFolder createExternalFolder(String folderName) throws CoreException {
		final Wrapper<IFolder> wrapper = Wrapper.forType(IFolder.class);
		ResourcesPlugin.getWorkspace().run((monitor)->{
			IPath externalFolderPath = new Path(folderName);
			IProject externalFoldersProject = JavaModelManager.getExternalManager().getExternalFoldersProject();
			if (!externalFoldersProject.isAccessible()) {
				if (!externalFoldersProject.exists())
					externalFoldersProject.create(monitor());
				externalFoldersProject.open(monitor());
			}
			IFolder result = externalFoldersProject.getFolder(externalFolderPath);
			result.create(true, false, null);
//				JavaModelManager.getExternalManager().addFolder(result.getFullPath());
			wrapper.set(result);
		}, monitor());
		return wrapper.get();
	}
	
	public static void deleteExternalFolder(IFolder folder) throws CoreException {
		ResourcesPlugin.getWorkspace().run((monitor)->{
			JavaModelManager.getExternalManager().removeFolder(folder.getFullPath());
			folder.delete(true, null);
		}, monitor());
	}
	
	public static IFolder deleteSourceFolder(IJavaProject project, String folderPath) throws JavaModelException,
			CoreException {
		IFolder folder = project.getProject().getFolder(folderPath);
		deleteClasspathEntry(project, folder.getFullPath());
		folder.delete(true, monitor());
		return folder;
	}

	public static void addProjectReference(IJavaProject from, IJavaProject to) throws CoreException {
		addProjectReference(from, to, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static void addProjectReference(IJavaProject from, IJavaProject to, boolean build) throws CoreException {
		addToClasspath(from, JavaCore.newProjectEntry(to.getPath()), build);
	}
	
	public static void removeProjectReference(IJavaProject from, IJavaProject to) throws CoreException {
		removeFromClasspath(from, IClasspathEntry.CPE_PROJECT, to.getPath());
	}
	
	public static void removeJarFromClasspath(IJavaProject from, IFile to) throws CoreException {
		removeFromClasspath(from, IClasspathEntry.CPE_LIBRARY, to.getFullPath());
	}
	
	public static void removeFromClasspath(IJavaProject from, int entryKind, IPath path) throws CoreException {
		List<IClasspathEntry> classpath = Lists.newArrayList(from.getRawClasspath());
		Iterator<IClasspathEntry> iterator = classpath.iterator();
		while (iterator.hasNext()) {
			IClasspathEntry entry = iterator.next();
			if (entry.getEntryKind() == entryKind) {
				if (entry.getPath().equals(path))
					iterator.remove();
			}
		}
		from.setRawClasspath(classpath.toArray(new IClasspathEntry[classpath.size()]), monitor());
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

	public static IJavaProject makeJavaProject(IProject project) throws CoreException {
		return makeJavaProject(project, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static IJavaProject makeJavaProject(IProject project, boolean build) throws CoreException {
		IJavaProject javaProject = JavaCore.create(project);
		javaProject.save(null, true);
		addNature(project, JavaCore.NATURE_ID);
		addSourceFolder(javaProject, "src", false);
		addJreClasspathEntry(javaProject, build);
		return javaProject;
	}

	public static IFolder addSourceFolder(IJavaProject javaProject, String folderName) throws CoreException,
			JavaModelException {
		return addSourceFolder(javaProject, folderName, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static IFolder addSourceFolder(IJavaProject javaProject, String folderName, boolean build) throws CoreException,
	JavaModelException {
		return addSourceFolder(javaProject, folderName, null, null, build);
	}

	public static IFolder addSourceFolder(IJavaProject javaProject, String folderName, String[] inclusionPatterns, String[] exclusionPatterns) throws CoreException,
			JavaModelException {
		return addSourceFolder(javaProject, folderName, inclusionPatterns, exclusionPatterns, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static IFolder addSourceFolder(IJavaProject javaProject, String folderName, String[] inclusionPatterns, String[] exclusionPatterns, boolean build) throws CoreException,
			JavaModelException {
		
		IProject project = javaProject.getProject();
		IPath projectPath = project.getFullPath();
		
		deleteClasspathEntry(javaProject, projectPath);
		
		IFolder srcFolder = createSubFolder(project, folderName); //$NON-NLS-1$
		IClasspathEntry srcFolderClasspathEntry = JavaCore.newSourceEntry(srcFolder.getFullPath(), getInclusionPatterns(inclusionPatterns), getExclusionPatterns(exclusionPatterns), null);
		addToClasspath(javaProject, srcFolderClasspathEntry, build);
		return srcFolder;
	}

	private static IPath[] getExclusionPatterns(String[] exclusionPatterns) {
		return toPatterns(exclusionPatterns, ClasspathEntry.EXCLUDE_NONE);
	}

	private static IPath[] getInclusionPatterns(String[] inclusionPatterns) {
		return toPatterns(inclusionPatterns, ClasspathEntry.INCLUDE_ALL);
	}

	private static IPath[] toPatterns(String[] patterns, IPath[] defaultResult) {
		if (patterns == null || patterns.length == 0) {
			return defaultResult;
		}
		IPath[] result = new IPath[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			result[i] = new Path(patterns[i]);
		}
		return result;
	}

	public static void deleteClasspathEntry(IJavaProject javaProject, IPath path) throws JavaModelException {
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		for (int i = 0; i < classpath.length; ++i) {
			IPath entryPath = classpath[i].getPath();
			if (path.equals(entryPath)) {
				IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, newClasspath, 0, i);
				System.arraycopy(classpath, i + 1, newClasspath, i, classpath.length - i - 1);
				javaProject.setRawClasspath(newClasspath, null);
			}
		}
	}

	public static void addToClasspath(IJavaProject javaProject, IClasspathEntry newClassPathEntry)
			throws JavaModelException {
		addToClasspath(javaProject, newClassPathEntry, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static void addToClasspath(IJavaProject javaProject, IClasspathEntry newClassPathEntry, boolean build)
			throws JavaModelException {
		IClasspathEntry[] newClassPath;
		IClasspathEntry[] classPath = javaProject.getRawClasspath();
		for (IClasspathEntry classPathEntry : classPath) {
			if (classPathEntry.equals(newClassPathEntry)) {
				return;
			}
		}
		newClassPath = new IClasspathEntry[classPath.length + 1];
		System.arraycopy(classPath, 0, newClassPath, 1, classPath.length);
		newClassPath[0] = newClassPathEntry;
		javaProject.setRawClasspath(newClassPath, null);
		if (build) {
			waitForBuild();
		}
	}

	public static IFolder createSubFolder(IProject project, String folderName) throws CoreException {
		IFolder folder = project.getFolder(folderName);
		if (folder.exists()) {
			folder.delete(true, null);
		}
		try {
			return createFolder(folder.getFullPath());
		} catch (Exception e) {
			throw new RuntimeIOException(e);
		}
	}

	/**
	 * @deprecated {@link #addJreClasspathEntry(IJavaProject)} should be used instead of this method
	 */
	@Deprecated
	public static void addJre15ClasspathEntry(IJavaProject javaProject) throws JavaModelException {
		IClasspathEntry existingJreContainerClasspathEntry = getJreContainerClasspathEntry(javaProject);
		if (existingJreContainerClasspathEntry == null) {
			addToClasspath(javaProject, JavaCore.newContainerEntry(new Path(JRE_CONTAINER_1_5)));
		}
	}
	
	public static void addJreClasspathEntry(IJavaProject javaProject) throws JavaModelException {
		addJreClasspathEntry(javaProject, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static void addJreClasspathEntry(IJavaProject javaProject, boolean build) throws JavaModelException {
		IClasspathEntry existingJreContainerClasspathEntry = getJreContainerClasspathEntry(javaProject);
		if (existingJreContainerClasspathEntry == null) {
			addToClasspath(javaProject, JREContainerProvider.getDefaultJREContainerEntry(), build);
		}
	}
	
	public static void addJreClasspathEntry(IJavaProject javaProject, String bree) throws JavaModelException {
		IPath jreContainerPath = JavaRuntime.newJREContainerPath(StandardVMType.ID_STANDARD_VM_TYPE, bree);
		addToClasspath(javaProject, JavaCore.newContainerEntry(jreContainerPath));
	}
	
	/**
	 * @since 2.17
	 */
	public static void addJreClasspathEntry(IJavaProject javaProject, String bree, boolean build) throws JavaModelException {
		IPath jreContainerPath = JavaRuntime.newJREContainerPath(StandardVMType.ID_STANDARD_VM_TYPE, bree);
		addToClasspath(javaProject, JavaCore.newContainerEntry(jreContainerPath), build);
	}

	private static boolean isJava7Default = false;
	
	@Deprecated
	public static void makeJava7Default() {
		if (!isJava7Default) {
			IExecutionEnvironmentsManager manager = JavaRuntime.getExecutionEnvironmentsManager();
			IExecutionEnvironment[] environments = manager.getExecutionEnvironments();
			for (int i = 0; i < environments.length; i++) {
				IExecutionEnvironment environment = environments[i];
				if (environment.getId().equals("JavaSE-1.6") && environment.getDefaultVM() == null) {
					IVMInstall[] compatibleVMs = environment.getCompatibleVMs();
					for (IVMInstall ivmInstall : compatibleVMs) {
						if (ivmInstall instanceof IVMInstall2) {
							IVMInstall2 install2 = (IVMInstall2) ivmInstall;
							if (install2.getJavaVersion().startsWith("1.7")) {
								environment.setDefaultVM(ivmInstall);
							}
						}
					}
				}
			}
			isJava7Default = true;
		}
	}
	
	/**
	 * Java 5 is long dead.
	 */
	@Deprecated
	public static void makeJava5Compliant(IJavaProject javaProject) {
		Map<String, String> options= javaProject.getOptions(false);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		javaProject.setOptions(options);
	}
	
	/**
	 * @deprecated use {@link #makeDefaultCompliant(IJavaProject)} instead, which derives the compliance
	 *             level from the project's JRE container instead of pinning it to Java 8
	 * @since 2.21
	 */
	@Deprecated
	public static void makeJava8Compliant(IJavaProject javaProject) {
		makeCompliantFor(javaProject, JavaVersion.JAVA8);
	}

	/**
	 * Sets the JDT compiler compliance/source/target options for the given {@code javaVersion}.
	 *
	 * @since 2.45
	 */
	public static void makeCompliantFor(IJavaProject javaProject, JavaVersion javaVersion) {
		Map<String, String> options= javaProject.getOptions(false);
		String jreLevel = javaVersion.getQualifier();
		options.put(JavaCore.COMPILER_COMPLIANCE, jreLevel);
		options.put(JavaCore.COMPILER_SOURCE, jreLevel);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, jreLevel);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		javaProject.setOptions(options);
	}

	/**
	 * Makes the given project compliant with the execution environment of its JRE container entry,
	 * falling back to {@link JREContainerProvider#PREFERRED_BREE} if none can be determined.
	 *
	 * @since 2.45
	 */
	public static void makeDefaultCompliant(IJavaProject javaProject) throws JavaModelException {
		String executionEnvironmentId = null;
		IClasspathEntry jreContainerEntry = getJreContainerClasspathEntry(javaProject);
		if (jreContainerEntry != null) {
			executionEnvironmentId = JavaRuntime.getExecutionEnvironmentId(jreContainerEntry.getPath());
		}
		if (executionEnvironmentId == null) {
			executionEnvironmentId = JREContainerProvider.PREFERRED_BREE;
		}
		BuildPathSupport.setEEComplianceOptions(javaProject, executionEnvironmentId, null);
	}

	public static IClasspathEntry getJreContainerClasspathEntry(IJavaProject javaProject) throws JavaModelException {
		return JREContainerProvider.getJREContainerEntry(javaProject);
	}

	public static IClasspathEntry addPlatformJarToClasspath(final Plugin srcPlugin, final String jarFileName,
			final IJavaProject destProject) throws JavaModelException, IOException {
		final IPath jarFilePath = PluginUtil.findPathInPlugin(srcPlugin, jarFileName);
		final IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(jarFilePath, null, null);
		addToClasspath(destProject, newLibraryEntry);
		return newLibraryEntry;
	}

	public static IClasspathEntry addJarToClasspath(IJavaProject javaProject, IFile jarFile) throws JavaModelException {
		return addJarToClasspath(javaProject, jarFile, true);
	}
	
	/**
	 * @since 2.17
	 */
	public static IClasspathEntry addJarToClasspath(IJavaProject javaProject, IFile jarFile, boolean build) throws JavaModelException {
		IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null);
		addToClasspath(javaProject, newLibraryEntry, build);
		return newLibraryEntry;
	}
	
	public static IClasspathEntry addExternalFolderToClasspath(IJavaProject javaProject, IFolder folder) throws JavaModelException {
		IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(folder.getLocation(), null, null);
		addToClasspath(javaProject, newLibraryEntry);
		return newLibraryEntry;
	}
	
	public static IClasspathEntry addExternalJarToClasspath(IJavaProject javaProject, File file) throws JavaModelException {
		IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(new Path(file.getAbsolutePath()), null, null);
		addToClasspath(javaProject, newLibraryEntry);
		return newLibraryEntry;
	}
	

	public static File createExternalJar(InputStream data, String nameWithoutJarSuffix) throws IOException,
			FileNotFoundException {
		File tempFile = Files.createTempFile(nameWithoutJarSuffix, ".jar").toFile();
		tempFile.createNewFile();
		tempFile.deleteOnExit();
		FileOutputStream stream = new FileOutputStream(tempFile);
		int i = -1;
		while ((i = data.read()) != -1)
			stream.write(i);
		stream.close();
		data.close();
		return tempFile;
	}
	
	public static void setUnixLineEndings(IProject project) {
		IEclipsePreferences prefs = new ProjectScope(project).getNode(Platform.PI_RUNTIME);
		prefs.put(Platform.PREF_LINE_SEPARATOR, "\n");
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

}
