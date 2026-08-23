/*******************************************************************************
 * Copyright (c) 2011, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.XtextEditorInfo;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.util.JREContainerProvider;
import org.eclipse.xtext.ui.util.PluginProjectFactory;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.MergeableManifest2;
import org.eclipse.xtext.util.MergeableManifest2.Attributes;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.Assert;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
		IResourcesSetupUtil.waitForBuild();
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
		IResourcesSetupUtil.waitForBuild();
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

	public static String getContentsAsString(IFile file) throws Exception {
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
		Injector injector = XtendActivator.getInstance().getInjector(XtendActivator.ORG_ECLIPSE_XTEND_CORE_XTEND);
		PluginProjectFactory projectFactory = injector.getInstance(PluginProjectFactory.class);
		projectFactory.setProjectName(name);
		projectFactory.setProjectDefaultCharset(StandardCharsets.ISO_8859_1.name());
		projectFactory.setBreeToUse(getBree(javaVersion));
		projectFactory.addFolders(Collections.singletonList("src"));
		projectFactory.addBuilderIds(XtextProjectHelper.BUILDER_ID, JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder",
				"org.eclipse.pde.SchemaBuilder");
		projectFactory.addProjectNatures(
				XtextProjectHelper.NATURE_ID, JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature");
		projectFactory.addRequiredBundles(newArrayList(requiredBundles));
		IProject result = projectFactory.createProject(new NullProgressMonitor(), null);
		IJavaProject javaProject = JavaCore.create(result);
		if (javaVersion == null)
			JavaProjectSetupUtil.makeDefaultCompliant(javaProject);
		else
			makeCompliantFor(javaProject, javaVersion);
		JavaProjectSetupUtil.addJreClasspathEntry(javaProject);
		return result;
	}
	
	private static String getBree(JavaVersion javaVersion) {
		if (javaVersion == null) {
			return JREContainerProvider.PREFERRED_BREE;
		} else {
			return javaVersion.getBree();
		}
	}
	
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
	 * @return a {@link Pair}, where the <code>key</code> is the BREE that was set and the <code>value</code> indicates that the BREE was changed.
	 */
	public static Pair<String, Boolean> changeBree(IJavaProject javaProject, final JavaVersion javaVersion)
			throws Exception {
		final AtomicReference<String> bree = new AtomicReference<String>();
		boolean changed = changeManifest(javaProject.getProject(), new Function<MergeableManifest2, Boolean>() {
			@Override
			public Boolean apply(MergeableManifest2 mf) {
				mf.setBREE(getBree(javaVersion));
				return mf.isModified();
			}
		});
		if (changed) {
			JavaProjectSetupUtil.addJreClasspathEntry(javaProject, bree.get());
		}
		return Pair.of(bree.get(), changed);
	}

	public static boolean  addExportedPackages(IProject project, final String ... exportedPackages) throws Exception{
		return changeManifest(project, new Function<MergeableManifest2,Boolean>() {
			@Override
			public Boolean apply(MergeableManifest2 mf) {
				 mf.addExportedPackages(exportedPackages);
				return mf.isModified();
			}
		});
	}
	
	public static boolean  removeExportedPackages(IProject project, final String... exportedPackages) throws Exception {
		return changeManifest(project, new Function<MergeableManifest2, Boolean>() {
			@Override
			public Boolean apply(MergeableManifest2 mf) {
				Attributes attrs = mf.getMainAttributes();
				String expPackKey = "Export-Package";
				String oldValue = attrs.get(expPackKey);
				if (!Strings.isNullOrEmpty(oldValue)) {
					String[] existingExports = oldValue.split(",(\\s+)?");
					System.out.println(Arrays.toString(existingExports));
					List<String> exportsToKeep = new ArrayList<String>();
					for (String string : existingExports) {
						exportsToKeep.add(string.trim());
					}
					boolean changed = exportsToKeep.removeAll(Arrays.asList(exportedPackages));
					String valueToSet = Joiner.on(',').join(exportsToKeep);
					if (changed) {
						if (valueToSet.isEmpty()) {
							attrs.remove(expPackKey);
						} else {
							attrs.put(expPackKey, valueToSet);
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	public static boolean changeManifest(IProject project, Function<MergeableManifest2, Boolean> config) throws Exception {
		IFile manifest = project.getFile("META-INF/MANIFEST.MF");
		InputStream content = manifest.getContents();
		MergeableManifest2 mf;
		try {
			mf = new MergeableManifest2(content);
		} finally {
			content.close();
		}
		if (config.apply(mf)) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			mf.write(stream);
			manifest.setContents(new ByteArrayInputStream(stream.toByteArray()), true, false, null);
			return true;
		}
		return false;
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
					if (!display.isDisposed())
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
