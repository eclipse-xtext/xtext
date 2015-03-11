/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests;

import static com.google.common.collect.ImmutableList.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.ide.internal.XtendActivator;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.XtextEditorInfo;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.compiler.JavaVersion;
import org.eclipse.xtext.xbase.lib.Functions;
import org.junit.Assert;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
@Singleton
public class WorkbenchTestHelper extends Assert {

	public static final Logger log = Logger.getLogger(WorkbenchTestHelper.class);

	public static final String TESTPROJECT_NAME = "test.project";

	public static final ImmutableList<String> DEFAULT_REQ_BUNDLES = of("com.google.inject", "org.eclipse.xtend.lib",
			"org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit");

	private Set<IFile> files = newHashSet();
	
	@Inject
	private XtextEditorInfo editorInfo;

	@Inject
	private FileExtensionProvider fileExtensionProvider;

	@Inject
	private IWorkbench workbench;

	@Inject
	private IWorkspace workspace;

	@Inject
	private IResourceSetProvider resourceSetProvider;

	private boolean isLazyCreatedProject = false;

	public void tearDown() throws Exception {
		if (workbench.getActiveWorkbenchWindow() != null)
			workbench.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				for (IFile file : getFiles()) {
					try {
						file.delete(true, null);
					} catch (Exception exc) {
						throw new RuntimeException(exc);
					}
				}
				getFiles().clear();
				IFolder binFolder = getProject(false).getFolder("bin");
				if (binFolder.exists()) {
					for (IResource binMember : binFolder.members()) {
						try {
							binMember.delete(true, null);
						} catch (Exception exc) {
							throw new RuntimeException(exc);
						}
					}
				}
				if (isLazyCreatedProject) {
					deleteProject(getProject(false));
					isLazyCreatedProject = false;
				}
			}
		}.run(null);
		IResourcesSetupUtil.waitForAutoBuild();
	}

	public Set<IFile> getFiles() {
		return files;
	}

	public IProject getProject() {
		return getProject(true);
	}

	protected IProject getProject(boolean createOnDemand) {
		IProject project = workspace.getRoot().getProject(TESTPROJECT_NAME);
		if (createOnDemand && !project.exists()) {
			try {
				isLazyCreatedProject = true;
				project = createPluginProject(TESTPROJECT_NAME);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return project;
	}

	public XtextEditor openEditor(String fileName, String content) throws Exception {
		int cursor = content.indexOf('|');
		IFile file = createFile(fileName, content.replace("|", ""));
		XtextEditor editor = openEditor(file);
		editor.getInternalSourceViewer().setSelectedRange(cursor, 0);
		editor.getInternalSourceViewer().getTextWidget().setFocus();
		return editor;
	}

	public IFile createFile(String fileName, String content) throws Exception {
		String fullFileName = getFullFileName(fileName);
		return createFileImpl(fullFileName, content);
	}

	public IFile createFileImpl(String fullFileName, String content) throws Exception {
		IFile file = IResourcesSetupUtil.createFile(fullFileName, content);
		getFiles().add(file);
		return file;
	}

	public IFile getFile(String fileName) {
		return workspace.getRoot().getFile(new Path(getFullFileName(fileName)));
	}

	protected String getFullFileName(String fileName) {
		String extension = (fileName.indexOf(".") != -1) ? "" : "." + getFileExtension();
		String fullFileName = getProject().getName() + "/src/" + fileName + extension;
		return fullFileName;
	}

	public String getFileExtension() {
		return fileExtensionProvider.getFileExtensions().iterator().next();
	}

	public URI uri(IFile file) {
		return URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	}

	public XtendFile xtendFile(String fileName, String content) throws Exception {
		IFile file = createFile(fileName, content);
		Resource resource = getResourceSet().createResource(uri(file));
		resource.load(new StringInputStream(content), null);
		assertEquals(resource.getErrors().toString(), 0, resource.getErrors().size());
		XtendFile xtendFile = (XtendFile) resource.getContents().get(0);
		return xtendFile;
	}

	public XtendFile xtendFile(IProject project, String fileName, String content) throws Exception {
		IFile file = createFileImpl(project.getName() + "/src/" + fileName, content);
		Resource resource = resourceSetProvider.get(project).createResource(uri(file));
		resource.load(new StringInputStream(content), null);
		assertEquals(resource.getErrors().toString(), 0, resource.getErrors().size());
		XtendFile xtendFile = (XtendFile) resource.getContents().get(0);
		return xtendFile;
	}

	public ResourceSet getResourceSet() {
		ResourceSet resourceSet = resourceSetProvider.get(getProject());
		return resourceSet;
	}

	public String getEditorID() {
		return editorInfo.getEditorId();
	}

	public String getContents(IFile file) throws Exception {
		InputStream inputStream = file.getContents();
		try {
			byte[] buffer = new byte[2048];
			int bytesRead = 0;
			StringBuffer b = new StringBuffer();
			do {
				bytesRead = inputStream.read(buffer);
				if (bytesRead != -1)
					b.append(new String(buffer, 0, bytesRead));
			} while (bytesRead != -1);
			return b.toString();
		} finally {
			inputStream.close();
		}
	}
	
	public void closeWelcomePage() throws InterruptedException {
		if (PlatformUI.getWorkbench().getIntroManager().getIntro() != null) {
			PlatformUI.getWorkbench().getIntroManager().closeIntro(
					PlatformUI.getWorkbench().getIntroManager().getIntro());
		}
	}

	public static IProject createPluginProject(String name) throws CoreException {
		return createPluginProject(name, DEFAULT_REQ_BUNDLES.toArray(new String[DEFAULT_REQ_BUNDLES.size()]));
	}
	
	public static IProject createPluginProject(String name, JavaVersion javaVersion) throws CoreException {
		return createPluginProject(name, javaVersion, DEFAULT_REQ_BUNDLES.toArray(new String[DEFAULT_REQ_BUNDLES.size()]));
	}

	public static IProject createPluginProject(String name, String... requiredBundles) throws CoreException {
		return createPluginProject(name, null, requiredBundles);
	}
	
	public static IProject createPluginProject(String name, JavaVersion javaVersion, String... requiredBundles) throws CoreException {
		Injector injector = XtendActivator.getInstance().getInjector("org.eclipse.xtend.core.Xtend");
		PluginProjectFactory projectFactory = injector.getInstance(PluginProjectFactory.class);
		projectFactory.setProjectName(name);
		if (javaVersion == null) {
			projectFactory.setBreeToUse(JREContainerProvider.PREFERRED_BREE);
		} else {
			switch (javaVersion) {
				case JAVA8:
					projectFactory.setBreeToUse("JavaSE-1.8");
					break;
				case JAVA7:
					projectFactory.setBreeToUse("JavaSE-1.7");
					break;
				case JAVA6:
					projectFactory.setBreeToUse("JavaSE-1.6");
					break;
				default:
					projectFactory.setBreeToUse("J2SE-1.5");
			}
		}
		projectFactory.addFolders(Collections.singletonList("src"));
		projectFactory.addBuilderIds(XtextProjectHelper.BUILDER_ID, JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
				"org.eclipse.pde.SchemaBuilder");
		projectFactory.addProjectNatures(
				XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature");
		projectFactory.addRequiredBundles(newArrayList(requiredBundles));
		IProject result = projectFactory.createProject(new NullProgressMonitor(), null);
		IJavaProject javaProject = JavaCore.create(result);
		if (javaVersion == null)
			JavaProjectSetupUtil.makeJava5Compliant(javaProject);
		else
			makeCompliantFor(javaProject, javaVersion);
		JavaProjectSetupUtil.addJreClasspathEntry(javaProject);
		return result;
	}
	
	public static void makeCompliantFor(IJavaProject javaProject, JavaVersion javaVersion) {
		@SuppressWarnings("unchecked")
		Map<String, String> options= javaProject.getOptions(false);
		String jreLevel;
		switch (javaVersion) {
			case JAVA8:
				jreLevel = "1.8";
				break;
			case JAVA7:
				jreLevel = "1.7";
				break;
			case JAVA6:
				jreLevel = "1.6";
				break;
			default:
				jreLevel = JavaCore.VERSION_1_5;
		}
		options.put(JavaCore.COMPILER_COMPLIANCE, jreLevel);
		options.put(JavaCore.COMPILER_SOURCE, jreLevel);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, jreLevel);
		options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
		options.put(JavaCore.COMPILER_CODEGEN_INLINE_JSR_BYTECODE, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_LINE_NUMBER_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_SOURCE_FILE_ATTR, JavaCore.GENERATE);
		options.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		javaProject.setOptions(options);
	}
	
	public static void addExportedPackages(IProject project, String ... exportedPackages) throws Exception{
		IFile manifest = project.getFile("META-INF/MANIFEST.MF");
		Manifest mf = new Manifest(manifest.getContents());
		String value = mf.getMainAttributes().getValue("Export-Package");
		for (String exported : exportedPackages) {
			if (value == null) {
				value = exported;
			} else {
				value += ","+exported;
			}
		}
		mf.getMainAttributes().putValue("Export-Package", value);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		mf.write(stream);
		manifest.setContents(new ByteArrayInputStream(stream.toByteArray()), true, true, null);
	}

	public static void deleteProject(IProject project) throws CoreException {
		if (project != null && project.exists()) {
			project.delete(true, true, null);
		}
	}

	public XtextEditor openEditor(IFile file) throws Exception {
		IEditorPart openEditor = openEditor(file, getEditorID());
		XtextEditor xtextEditor = EditorUtils.getXtextEditor(openEditor);
		if (xtextEditor != null) {
			xtextEditor.selectAndReveal(0, 0);
			return xtextEditor;
		} else if (openEditor instanceof ErrorEditorPart) {
			Field field = openEditor.getClass().getDeclaredField("error");
			field.setAccessible(true);
			throw new IllegalStateException("Couldn't open the editor.",
					((Status) field.get(openEditor)).getException());
		} else {
			fail("Opened Editor with id:" + getEditorID() + ", is not an XtextEditor");
		}
		return null;
	}

	public IEditorPart openEditor(IFile file, String editorId) throws PartInitException {
		return workbench.getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), editorId);
	}

	public ITextEditor openLikeTextEditor(IFile file) throws PartInitException {
		IEditorPart editor = IDE.openEditor(workbench.getActiveWorkbenchWindow().getActivePage(), file);
		if (editor instanceof ITextEditor) {
			return (ITextEditor) editor;
		}
		return null;
	}

	public boolean closeEditor(IEditorPart editor, boolean save) {
		return workbench.getActiveWorkbenchWindow().getActivePage().closeEditor(editor, save);
	}
	
	public boolean closeAllEditors(boolean save) {
		return workbench.getActiveWorkbenchWindow().getActivePage().closeAllEditors(save);
	}

	public boolean saveEditor(IEditorPart editor, boolean confirm) {
		return workbench.getActiveWorkbenchWindow().getActivePage().saveEditor(editor, confirm);
	}
	
	/**
	 * Wait for an update in the UI.
	 * 
	 * @param test
	 * 		tester function that returns true if the target state of the UI has been reached
	 * @param timeout
	 * 		the time after which waiting is canceled
	 */
	public void awaitUIUpdate(Functions.Function0<Boolean> test, final long timeout) {
		long startTime = System.currentTimeMillis();
		final Display display = Display.getCurrent();
		new Thread("Display alarm") {
			@Override public void run() {
				try {
					Thread.sleep(timeout);
					display.wake();
				} catch (InterruptedException e) { }
			}
		}.start();
		while (!test.apply() && System.currentTimeMillis() - startTime < timeout) {
			boolean hasWork = display.sleep();
			while (hasWork) {
				hasWork = display.readAndDispatch();
			}
		}
	}

}
