/*******************************************************************************
 * Copyright (c) 2009, 2024, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.testing.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.pde.internal.core.ClasspathComputer;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.Assert;

import com.google.common.io.ByteStreams;

/**
 * @since 2.12
 */
public class IResourcesSetupUtil {

	/**
	 * A utility monitor that prints the status on System.out
	 * 
	 * @author Lorenzo Bettini - Initial contribution and API
	 * @since 2.38
	 */
	public static class ConsoleLoggingProgressMonitor extends NullProgressMonitor {

		private String prefix;

		public ConsoleLoggingProgressMonitor(String prefix) {
			this.prefix = prefix + ": ";
		}

		@Override
		public void beginTask(String name, int totalWork) {
			System.out.println(prefix + name + " totalWork " + totalWork);
		}

		@Override
		public void subTask(String name) {
			System.out.println(prefix + name);
		}

		@Override
		public void done() {
			System.out.println(prefix + "DONE");
		}
	}

	public static IWorkspaceRoot root() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static IProject createProject(String name) throws CoreException,
			InvocationTargetException, InterruptedException {
		IProject project = root().getProject(name);
		createProject(project);
		return project;
	}

	public static IProject createProject(IProject project) throws CoreException {
		if (!project.exists())
			project.create(monitor());
		project.open(monitor());
		return project;
	}
	
	public static void assertNoErrorsInWorkspace() throws CoreException {
		IMarker[] findMarkers = root().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		String msg = "";
		for (IMarker iMarker : findMarkers) {
			if (MarkerUtilities.getSeverity(iMarker) == IMarker.SEVERITY_ERROR)
				msg += "\n - "+iMarker.getResource().getName()+":"+MarkerUtilities.getLineNumber(iMarker)+" - "+MarkerUtilities.getMessage(iMarker) + "("+MarkerUtilities.getMarkerType(iMarker)+")";
		}
		if (msg.length()>0)
			Assert.fail("Workspace contained errors: "+msg);
	}

	public static void addNature(IProject project, String nature)
			throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = nature;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
	
	public static void addBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] specs = description.getBuildSpec();
		ICommand command = description.newCommand();
		command.setBuilderName(builderId);
		// Add the nature
		ICommand[] specsModified = new ICommand[specs.length + 1];
		System.arraycopy(specs, 0, specsModified, 0, specs.length);
		specsModified[specs.length] = command;
		description.setBuildSpec(specsModified);
		project.setDescription(description, monitor());
	}

	public static void removeNature(IProject project, String nature)
			throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (nature.equals(natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length
						- i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				return;
			}
		}

	}
	
	public static void removeBuilder(IProject project, String builderId) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] builderSpecs = description.getBuildSpec();

		for (int i = 0; i < builderSpecs.length; ++i) {
			if (builderId.equals(builderSpecs[i].getBuilderName())) {
				// Remove the builder
				ICommand[] modifiedSpecs = new ICommand[builderSpecs.length - 1];
				System.arraycopy(builderSpecs, 0, modifiedSpecs, 0, i);
				System.arraycopy(builderSpecs, i + 1, modifiedSpecs, i, builderSpecs.length - i - 1);
				description.setBuildSpec(modifiedSpecs);
				project.setDescription(description, null);
				return;
			}
		}

	}

	public static void setReference(final IProject from, final IProject to)
			throws CoreException, InvocationTargetException,
			InterruptedException {
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				IProjectDescription projectDescription = from.getDescription();
				IProject[] projects = projectDescription
						.getReferencedProjects();
				IProject[] newProjects = new IProject[projects.length + 1];
				System.arraycopy(projects, 0, newProjects, 0, projects.length);
				newProjects[projects.length] = to;
				projectDescription.setReferencedProjects(newProjects);
				from.setDescription(projectDescription, monitor());
			}
		}.run(monitor());
	}
	
	public static void removeReference(final IProject from, final IProject to)
	throws CoreException, InvocationTargetException,
	InterruptedException {
		new WorkspaceModifyOperation() {
			
			@Override
			protected void execute(IProgressMonitor monitor)
			throws CoreException, InvocationTargetException,
			InterruptedException {
				IProjectDescription projectDescription = from.getDescription();
				IProject[] projects = projectDescription
				.getReferencedProjects();
				for (int i = 0; i < projects.length; ++i) {
					if (to.equals(projects[i])) {
						// Remove the nature
						IProject[] newProjects = new IProject[projects.length - 1];
						System.arraycopy(projects, 0, newProjects, 0, i);
						System.arraycopy(projects, i + 1, newProjects, i, projects.length
								- i - 1);
						projectDescription.setReferencedProjects(newProjects);
						from.setDescription(projectDescription, null);
						return;
					}
				}
			}
		}.run(monitor());
	}

	public static IFolder createFolder(String wsRelativePath) throws InvocationTargetException, InterruptedException {
		return createFolder(new Path(wsRelativePath));
	}
	
	public static IFolder createFolder(IPath wsRelativePath) throws InvocationTargetException, InterruptedException {
		final IFolder folder = root().getFolder(wsRelativePath);
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				create(folder.getParent());
				folder.delete(true, monitor());
				folder.create(true, true, monitor());
			}

		}.run(monitor());
		return folder;
	}
	
	/**
	 * @since 2.14
	 */
	public static IFile createFile(String projectName, String fileName, String fileExtension, String content)
			throws CoreException, InvocationTargetException, InterruptedException {
		return createFile(projectName + "/" + fileName + "." + fileExtension, content);
	}
	
	public static IFile createFile(String wsRelativePath, String s)
			throws CoreException, InvocationTargetException,
			InterruptedException {
		return createFile(new Path(wsRelativePath), s);
	}

	public static IFile createFile(IPath wsRelativePath, final String s)
			throws CoreException, InvocationTargetException,
			InterruptedException {
		final IFile file = root().getFile(wsRelativePath);
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				create(file.getParent());
				file.delete(true, monitor());
				try (InputStream stream = new StringInputStream(s, file.getCharset(true))) {
					file.create(stream, true, monitor());
				} catch (IOException exc) {
					throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.xtext.ui.testing", exc.getMessage(), exc));
				}
			}

		}.run(monitor());
		return file;
	}

	public static File createTempFile(String fileName, String suffix, String content)
			throws Exception {
		return createTempFile(fileName, suffix, content, Charset.defaultCharset());
	}
	
	/**
	 * since 2.36
	 */
	public static File createTempFile(String fileName, String suffix, String content, Charset charset)
			throws Exception {
		File file = Files.createTempFile(fileName, suffix).toFile();
		try (FileWriter writer = new FileWriter(file, charset)) {
			writer.write(content);
		}
		return file;
	}

	public static IResource file(String path) {
		return root().findMember(new Path(path));
	}
	
	public static byte[] fileToByteArray(IFile file) throws CoreException, IOException {
		try (InputStream contents = file.getContents()) {
			return ByteStreams.toByteArray(contents);
		}
	}

	public static String fileToString(IFile file) throws CoreException, IOException {
		return new String(fileToByteArray(file), file.getCharset());
	}

	public static boolean fileIsEmpty(IFile file) throws IOException, CoreException {
		InputStream contents = null;
		try {
			contents = file.getContents();
			return contents.read() == -1;
		} finally {
			if (contents != null)
				contents.close();
		}
	}

	private static void create(final IContainer container)
			throws CoreException, InvocationTargetException,
			InterruptedException {
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				if (!container.exists()) {
					create(container.getParent());
					if (container instanceof IFolder) {
						((IFolder) container).create(true, true, monitor());
					} else {
						IProject iProject = (IProject) container;
						createProject(iProject);
					}
				}
			}
		}.run(monitor());
	}

	public static IProgressMonitor monitor() {
		return new NullProgressMonitor();
	}

	public static void fullBuild() throws CoreException {
		waitForJdtIndex();
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor());
	}
	
	public static void cleanBuild() throws CoreException {
		waitForJdtIndex();
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, monitor());
	}

	/**
	 * @deprecated clients should use {@link #waitForBuild()} since it is much faster. Clients that really depend
	 * on the delay before the build can use {@link #reallyWaitForAutoBuild()}.
	 */
	@Deprecated
	public static void waitForAutoBuild() {
		reallyWaitForAutoBuild();
	}
	
	/**
	 * A test that really should test the mechanism including the delay
	 * after the resource change event, could wait for the auto build.
	 */
	public static void reallyWaitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
						null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	
	public static void waitForBuild() {
		waitForBuild(null);
	}

	/**
	 * @since 2.38
	 */
	public static boolean isAutobuild() {
		return ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
	}

	/**
	 * @deprecated use {@link #isAutobuild()} instead (the boolean parameter is unused).
	 */
	@Deprecated(forRemoval = true)
	public static boolean isAutobuild(boolean enable) {
		return isAutobuild();
	}

	public static boolean setAutobuild(boolean enable) {
		IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
		boolean oldValue = description.isAutoBuilding();
		if (oldValue != enable) {
			description.setAutoBuilding(enable);
			try {
				ResourcesPlugin.getWorkspace().setDescription(description);
			} catch (CoreException e) {
				Exceptions.sneakyThrow(e);
			}
		}
		return oldValue;
	}
	
	public static void waitForBuild(IProgressMonitor monitor) {
		try {
			waitForJdtIndex(monitor);
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
		} catch (CoreException e) {
			OperationCanceledException operationCanceledException = new OperationCanceledException(e.getMessage());
			operationCanceledException.addSuppressed(e);
			throw operationCanceledException;
		}
	}

	public static void cleanWorkspace() throws CoreException {
		try {
			new WorkspaceModifyOperation() {
	
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException,
						InterruptedException {
					IProject[] visibleProjects = root().getProjects();
					deleteProjects(visibleProjects);
					IProject[] hiddenProjects = root().getProjects(IContainer.INCLUDE_HIDDEN);
					deleteProjects(hiddenProjects);
				}
			}.run(monitor());
		} catch(InvocationTargetException e) {
			Exceptions.sneakyThrow(e.getCause());
		} catch(Exception e) {
			throw new RuntimeException();
		}
	}

	protected static void deleteProjects(IProject[] projects) throws CoreException {
		for (IProject iProject : projects) {
			if (iProject.exists()) {
				iProject.delete(true,true, monitor());
			}
		}
	}
	
	public static String printMarker(IMarker[] markers) throws CoreException {
		StringBuilder result = new StringBuilder();
		for (IMarker marker : markers) {
			if (result.length() != 0) {
				result.append(", ");
			}
			result.append(marker.getAttribute(IMarker.MESSAGE));
		}
		return result.toString();
	}

	/**
	 * @since 2.38
	 */
	public static void waitForJdtIndex() {
		waitForJdtIndex(null);
	}

	/**
	 * @since 2.38
	 */
	@SuppressWarnings("restriction")
	public static void waitForJdtIndex(IProgressMonitor monitor) {
		JavaModelManager.getIndexManager().waitForIndex(true, monitor);
	}

	/**
	 * Waits until PDE has computed the "Plug-in Dependencies" classpath container of plug-in projects.
	 *
	 * @since 2.45
	 */
	@SuppressWarnings("restriction")
	public static void waitForPdeClasspathUpdate() {
		if (Job.getJobManager().currentRule() != null) {
			return;
		}
		try {
			Job.getJobManager().join(ClasspathComputer.class, null);
			Job.getJobManager().join(PluginModelManager.class, null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (NoClassDefFoundError e) {
			// org.eclipse.pde.core is optional dependency
		}
	}
}
