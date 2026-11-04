/**
 * Copyright (c) 2013, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ui.tests.core.resource;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.xtext.ui.resource.JarEntryLocator;
import org.eclipse.xtext.ui.resource.Storage2UriMapperJavaImpl;
import org.eclipse.xtext.ui.resource.Storage2UriMapperJavaImpl.PackageFragmentRootData;
import org.eclipse.xtext.ui.resource.UriValidator;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil;
import org.eclipse.xtext.ui.util.JavaProjectClasspathChangeAnalyzer;
import org.eclipse.xtext.ui.workspace.WorkspaceLockAccess;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Anton Kosyakov - Initial contribution and API
 */
public class Storage2UriMapperJavaImplTest extends Assert {
	private Storage2UriMapperJavaImpl storage2UriMapperJava;

	@Before
	public void setUp() {
		storage2UriMapperJava = createFreshStorage2UriMapper();
		JavaCore.addElementChangedListener(storage2UriMapperJava);
	}

	@After
	public void tearDown() throws Exception {
		JavaCore.removeElementChangedListener(storage2UriMapperJava);
		IResourcesSetupUtil.cleanWorkspace();
	}

	protected Storage2UriMapperJavaImpl createFreshStorage2UriMapper() {
		Storage2UriMapperJavaImpl mapper = new Storage2UriMapperJavaImpl();
		mapper.setUriValidator(new UriValidator() {
			@Override
			public boolean isPossiblyManaged(IStorage storage) {
				return "indexed".equals(storage.getFullPath().getFileExtension());
			}

			@Override
			public boolean isValid(URI uri, IStorage storage) {
				return "indexed".equals(storage.getFullPath().getFileExtension());
			}
		});
		mapper.setLocator(new JarEntryLocator());
		mapper.setWorkspace(ResourcesPlugin.getWorkspace());
		mapper.setWorkspaceLockAccess(new WorkspaceLockAccess());
		mapper.setJavaProjectClasspathChangeAnalyzer(new JavaProjectClasspathChangeAnalyzer());
		return mapper;
	}
	
	@Test
	public void testBug574908() throws Exception {
		IJavaProject p = newModularProject("p0");
		assertTrue(p.findType("java.lang.Module").exists());
		storage2UriMapperJava = createFreshStorage2UriMapper();
		storage2UriMapperJava.initializeCache();
		Map<String, PackageFragmentRootData> data = getCachedPackageFragmentRootData();
		assertNotNull(data);
		assertTrue(data.isEmpty());
	}
	
	@Test
	public void testBug574908_Performance() throws Exception {
		AtomicLong createProjectTimeMs = new AtomicLong();
		new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				Stopwatch sw = Stopwatch.createStarted();		
				for(int i = 0; i < 25; i++) {
					newModularProject("p" + i);
				}
				createProjectTimeMs.set(sw.elapsed(TimeUnit.MILLISECONDS));
			}
		}.run(null);
		
		Stopwatch sw = Stopwatch.createStarted();
		storage2UriMapperJava = createFreshStorage2UriMapper();
		storage2UriMapperJava.syncInitializeCache();
		long mapperTime = sw.elapsed(TimeUnit.MILLISECONDS);
		assertTrue("Creationg took: " + createProjectTimeMs.get()+ "ms; cacheInit took: " + mapperTime, mapperTime < 250);
	}
	
	@Ignore("Can be enabled for performance test purposes")
	@Test
	public void testBug574908_Performance_500() throws Exception {
		new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				Stopwatch sw = Stopwatch.createStarted();		
				for(int i = 0; i < 500; i++) {
					newModularProject("p" + i);
				}
				System.err.println(sw.elapsed(TimeUnit.MILLISECONDS));
			}
		}.run(null);
		
		for(int i = 0; i < 50; i++) {
			Stopwatch sw = Stopwatch.createStarted();
			storage2UriMapperJava = createFreshStorage2UriMapper();
			storage2UriMapperJava.syncInitializeCache();
			System.err.println(sw.elapsed(TimeUnit.MILLISECONDS));
		}
	}
	
	@Test
	public void testGetStorages_Performance() throws Exception {
		AtomicLong createProjectTimeMs = new AtomicLong();
		
		int projects = 50; // 200
		int jarsPerProject = 20;
		new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				Stopwatch sw = Stopwatch.createStarted();		
				for(int i = 0; i < projects; i++) {
					IJavaProject p = JavaProjectSetupUtil.createJavaProject("p" + i, false);
					for(int j = 0; j < jarsPerProject; j++) {
						JavaProjectSetupUtil.addJarToClasspath(p, createJar(p, "lib" + j), false);
					}
				}
				createProjectTimeMs.set(sw.elapsed(TimeUnit.MILLISECONDS));
			}
		}.run(null);
		IResourcesSetupUtil.waitForBuild();
		URI uri = URI.createPlatformPluginURI("doesntExist/some.file", false);
		Stopwatch sw = Stopwatch.createStarted();
		// simulate ToBeBuiltComputer.updateProject -> removeProject
		for(int k = 0; k < projects * jarsPerProject; k++) {
			Iterable<Pair<IStorage, IProject>> storages = storage2UriMapperJava.getStorages(uri);
			for(Pair<IStorage, IProject> p: storages) {
				assertNotNull(p.getFirst());
				assertNotNull(p.getSecond());
			}
		}
		long getStoragesTime = sw.elapsed(TimeUnit.MILLISECONDS);
		assertTrue("Creationg took: " + createProjectTimeMs.get()+ "ms; getStorages took: " + getStoragesTime, getStoragesTime < 150);
	}

	private IJavaProject newModularProject(String projectName) throws CoreException, JavaModelException, InvocationTargetException, InterruptedException {
		AtomicReference<IJavaProject> result = new AtomicReference<IJavaProject>();
		new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				IProject project = JavaProjectSetupUtil.createSimpleProject(projectName);
				JavaCore.initializeAfterLoad(monitor());
				IJavaProject javaProject = JavaCore.create(project);
				javaProject.save(null, true);
				result.set(javaProject);
				addNature(project, JavaCore.NATURE_ID);
				JavaProjectSetupUtil.addSourceFolder(javaProject, "src", false);
				JavaProjectSetupUtil.addJreClasspathEntry(javaProject, JavaVersion.DEFAULT.getBree());
				
				IClasspathEntry defaultEntry = JavaProjectSetupUtil.getJreContainerClasspathEntry(javaProject);
				if (isModular(defaultEntry)) {
					return;
				}
				
				IClasspathEntry newEntry = JavaCore.newContainerEntry(defaultEntry.getPath(), defaultEntry.getAccessRules(), new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true") }, defaultEntry.isExported());
				IClasspathEntry[] array = javaProject.getRawClasspath();
				for(int i = 0; i < array.length; i++) {
					if (array[i] == defaultEntry) {
						array[i] = newEntry;
						break;
					}
				}
				javaProject.setRawClasspath(array, null);
				assertTrue(isModular(JavaProjectSetupUtil.getJreContainerClasspathEntry(javaProject)));
			}
			
		}.run(null);
		return result.get();
	}
	
	private boolean isModular(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int i = 0, length = attributes.length; i < length; i++) {
			IClasspathAttribute attribute = attributes[i];
			if (IClasspathAttribute.MODULE.equals(attribute.getName()) && "true".equals(attribute.getValue())) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	@Test
	public void testOnClasspathChange() throws Exception {
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), 0, getCachedPackageFragmentRootData().size());
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
		Map<String, PackageFragmentRootData> map = getCachedPackageFragmentRootData();
		int sizeBefore = map.size();
		Assert.assertEquals("JRE is not cached", 0, sizeBefore);
		Assert.assertFalse(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().isPresent());
		IFile file = createJar(project);
		JavaProjectSetupUtil.addJarToClasspath(project, file);
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), sizeBefore + 1, getCachedPackageFragmentRootData().size());
		Assert.assertNotNull(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().get());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			Assert.assertEquals(it.getValue().associatedRoots.size() + " / " + it.getKey(), 1, it.getValue().associatedRoots.size());
			String head = Iterables.getFirst(it.getValue().associatedRoots.keySet(), null);
			Assert.assertTrue(head, head.startsWith("=testProject/"));
		});
		IJavaProject project2 = JavaProjectSetupUtil.createJavaProject("testProject2");
		JavaProjectSetupUtil.addJarToClasspath(project2, file);
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), (sizeBefore + 1), getCachedPackageFragmentRootData().size());
		Assert.assertNotNull(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().get());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			Assert.assertEquals(it.getValue().associatedRoots.size() + " / " + it.getKey(), 2, it.getValue().associatedRoots.size());
			String msg = Joiner.on("\n").join(it.getValue().associatedRoots.keySet());
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject/")));
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject2/")));
		});
		JavaProjectSetupUtil.removeJarFromClasspath(project, file);
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), sizeBefore + 1, getCachedPackageFragmentRootData().size());
		Assert.assertNotNull(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().get());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			if (it.getKey().contains("foo.jar")) {
				Assert.assertEquals(it.getKey(), 1, it.getValue().associatedRoots.size());
				String head = Iterables.getFirst(it.getValue().associatedRoots.keySet(), null);
				Assert.assertTrue(head, head.startsWith("=testProject2/"));
			} else {
				Assert.assertEquals(it.getValue().associatedRoots.size() + " / " + it.getKey(), 2, it.getValue().associatedRoots.size());
				String msg = Joiner.on("\n").join(it.getValue().associatedRoots.keySet());
				Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject/")));
				Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject2/")));
			}
		});
		JavaProjectSetupUtil.removeJarFromClasspath(project2, file);
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), sizeBefore, getCachedPackageFragmentRootData().size());
		Assert.assertFalse(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().isPresent());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			Assert.assertEquals(it.getValue().associatedRoots.size() + " / " + it.getKey(), 2, it.getValue().associatedRoots.size());
			String msg = Joiner.on("\n").join(it.getValue().associatedRoots.keySet());
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject/")));
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject2/")));
		});
	}

	@Test
	public void testOnCloseOpenRemoveProject() throws Exception {
		Assert.assertEquals(0, getCachedPackageFragmentRootData().size());
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
		IJavaProject project2 = JavaProjectSetupUtil.createJavaProject("testProject2");
		int sizeBefore = getCachedPackageFragmentRootData().size();
		IFile file = createJar(project);
		JavaProjectSetupUtil.addJarToClasspath(project, file);
		JavaProjectSetupUtil.addJarToClasspath(project2, file);
		assertBothProjects(sizeBefore);
		project2.getProject().close(IResourcesSetupUtil.monitor());
		assertFirstProject(sizeBefore);
		project.getProject().close(IResourcesSetupUtil.monitor());
		assertNonProjects();
		project.getProject().open(IResourcesSetupUtil.monitor());
		assertFirstProject(sizeBefore);
		project2.getProject().open(IResourcesSetupUtil.monitor());
		assertBothProjects(sizeBefore);
		project.getProject().delete(true, IResourcesSetupUtil.monitor());
		project2.getProject().delete(true, IResourcesSetupUtil.monitor());
		assertNonProjects();
	}

	@Test
	public void testOnRemoveTwoProjects() throws Exception {
		Assert.assertEquals(0, getCachedPackageFragmentRootData().size());
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
		IJavaProject project2 = JavaProjectSetupUtil.createJavaProject("testProject2");
		int sizeBefore = getCachedPackageFragmentRootData().size();
		IFile file = createJar(project);
		JavaProjectSetupUtil.addJarToClasspath(project, file);
		JavaProjectSetupUtil.addJarToClasspath(project2, file);
		assertBothProjects(sizeBefore);
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
				project.getProject().delete(true, monitor);
				project2.getProject().delete(true, monitor);
			}
		};
		op.run(IResourcesSetupUtil.monitor());
		assertNonProjects();
	}

	public void assertNonProjects() throws Exception {
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), 0, getCachedPackageFragmentRootData().size());
	}

	public void assertFirstProject(int sizeBefore) throws Exception {
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), sizeBefore + 1, getCachedPackageFragmentRootData().size());
		Assert.assertNotNull(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().get());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			boolean java9OrNewer = Iterables.any(it.getValue().associatedRoots.values(),
					r -> "JrtPackageFragmentRoot".equals(r.getClass().getSimpleName()));
			if (java9OrNewer) {
				Assert.assertTrue(it.getValue().associatedRoots.size() + " / " + it.getKey(),
						Lists.newArrayList(44 /* java11 */, 49, 63 /* java9 */, 51 /* java14 */, 50 /* java15 */)
								.contains(Integer.valueOf(it.getValue().associatedRoots.size())));
			} else {
				Assert.assertEquals(it.getKey(), 1, it.getValue().associatedRoots.size());
			}
			String head = Iterables.getFirst(it.getValue().associatedRoots.keySet(), null);
			Assert.assertTrue(head, head.startsWith("=testProject/"));
		});
	}

	public void assertBothProjects(int sizeBefore) throws Exception {
		Assert.assertEquals("" + getCachedPackageFragmentRootData(), sizeBefore + 1, getCachedPackageFragmentRootData().size());
		Assert.assertNotNull(getCachedPackageFragmentRootData().keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().get());
		getCachedPackageFragmentRootData().entrySet().forEach((Map.Entry<String, PackageFragmentRootData> it) -> {
			Assert.assertEquals(it.getValue().associatedRoots.size() + "/" + it.getKey(), 2, it.getValue().associatedRoots.size());
			String msg = Joiner.on("\n").join(it.getValue().associatedRoots.keySet());
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject/")));
			Assert.assertTrue(msg, Iterables.any(it.getValue().associatedRoots.keySet(), r -> r.startsWith("=testProject2/")));
		});
	}

	public IFile createJar(IJavaProject project) throws CoreException {
		return createJar(project, "foo");
	}
	
	public IFile createJar(IJavaProject project, String name) throws CoreException {
		IFile file = project.getProject().getFile(name + ".jar");
		file.create(JavaProjectSetupUtil.jarInputStream(new JavaProjectSetupUtil.TextFile("foo/bar.indexed", "//empty"),
				new JavaProjectSetupUtil.TextFile("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nBundle-SymbolicName: hubba.bubba\n")),
				true, IResourcesSetupUtil.monitor());
		return file;
	}

	public Map<String, PackageFragmentRootData> getCachedPackageFragmentRootData() throws Exception {
		for (Field field : storage2UriMapperJava.getClass().getDeclaredFields()) {
			if ("cachedPackageFragmentRootData".equals(field.getName())) {
				field.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, PackageFragmentRootData> result = (Map<String, PackageFragmentRootData>) field.get(storage2UriMapperJava);
				return result;
			}
		}
		return null;
	}
}
