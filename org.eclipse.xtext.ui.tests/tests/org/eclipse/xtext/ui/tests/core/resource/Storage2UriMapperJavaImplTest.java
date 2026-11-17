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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
				JavaProjectSetupUtil.addJreClasspathEntry(javaProject, JavaVersion.JAVA11.getBree());
				
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
		List<String> observedDeltas = new CopyOnWriteArrayList<>();
		// Records delta-driven notifications only, not updateCache calls from the async cache
		// initializer. That is fine here: every cache change under test is preceded by a delta.
		IElementChangedListener deltaRecorder = event -> collectProjectDeltas(event, Thread.currentThread().getName(), observedDeltas);
		JavaCore.addElementChangedListener(deltaRecorder);
		try {
			CacheSnapshot initial = snapshotCache();
			Assert.assertEquals(initial.dump + deltasMsg(observedDeltas), 0, initial.rootsByKey.size());
			IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
			IJavaProject project2 = JavaProjectSetupUtil.createJavaProject("testProject2");
			int sizeBefore = getCachedPackageFragmentRootData().size();
			IFile file = createJar(project);
			JavaProjectSetupUtil.addJarToClasspath(project, file);
			JavaProjectSetupUtil.addJarToClasspath(project2, file);
			assertBothProjects(sizeBefore, deltasMsg(observedDeltas));
			project2.getProject().close(IResourcesSetupUtil.monitor());
			assertFirstProject(sizeBefore, deltasMsg(observedDeltas));
			project.getProject().close(IResourcesSetupUtil.monitor());
			assertNonProjects(deltasMsg(observedDeltas));
			project.getProject().open(IResourcesSetupUtil.monitor());
			assertFirstProject(sizeBefore, deltasMsg(observedDeltas));
			project2.getProject().open(IResourcesSetupUtil.monitor());
			assertBothProjects(sizeBefore, deltasMsg(observedDeltas));
			project.getProject().delete(true, IResourcesSetupUtil.monitor());
			project2.getProject().delete(true, IResourcesSetupUtil.monitor());
			assertNonProjects(deltasMsg(observedDeltas));
		} finally {
			JavaCore.removeElementChangedListener(deltaRecorder);
		}
	}

	/**
	 * A project delta is only a trigger: the cache is reconciled against real project state.
	 * Combined flags (e.g. from batched concurrent changes) must therefore not be matched
	 * exactly — what matters is whether the project is actually open or closed when the
	 * delta is handled.
	 */
	@Test
	public void testCloseOpenFlags() throws Exception {
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
		int sizeBefore = getCachedPackageFragmentRootData().size();
		IFile file = createJar(project);
		JavaProjectSetupUtil.addJarToClasspath(project, file);
		CacheSnapshot afterAdd = snapshotCache();
		Assert.assertEquals(afterAdd.dump, sizeBefore + 1, afterAdd.rootsByKey.size());
		// The dispatch reconciles against actual project state, so stage that state for real:
		// detach the mapper (real close/open below must not touch the cache), then deliver
		// the synthetic combined-flag deltas directly against the still-populated cache.
		JavaCore.removeElementChangedListener(storage2UriMapperJava);
		try {
			project.getProject().close(IResourcesSetupUtil.monitor());
			// a combined close delta on the actually-closed project must evict the roots
			// (JRE roots are never cached, so the cache is empty afterwards)
			storage2UriMapperJava.elementChanged(new ElementChangedEvent(
					newDelta(project, IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_CHILDREN),
					ElementChangedEvent.POST_CHANGE));
			assertNonProjects();
			CacheSnapshot afterClose = snapshotCache();
			Assert.assertFalse(afterClose.dump,
					afterClose.rootsByKey.keySet().stream().anyMatch(it -> it.contains("foo.jar")));
			project.getProject().open(IResourcesSetupUtil.monitor());
			// a delta carrying both bits on the open project must reconcile with the current state
			storage2UriMapperJava.elementChanged(new ElementChangedEvent(
					newDelta(project, IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_OPENED),
					ElementChangedEvent.POST_CHANGE));
			assertFirstProject(sizeBefore);
		} finally {
			JavaCore.addElementChangedListener(storage2UriMapperJava);
		}
	}

	/**
	 * State wins over flags: a close-flavoured delta for an actually open project must not
	 * evict anything. Flag-driven clearing (the old exact-match switch) fails here.
	 */
	@Test
	public void testCloseFlagsOnOpenProjectKeepsRoots() throws Exception {
		IJavaProject project = JavaProjectSetupUtil.createJavaProject("testProject");
		int sizeBefore = getCachedPackageFragmentRootData().size();
		IFile file = createJar(project);
		JavaProjectSetupUtil.addJarToClasspath(project, file);
		CacheSnapshot afterAdd = snapshotCache();
		Assert.assertEquals(afterAdd.dump, sizeBefore + 1, afterAdd.rootsByKey.size());
		storage2UriMapperJava.elementChanged(new ElementChangedEvent(
				newDelta(project, IJavaElementDelta.F_CLOSED | IJavaElementDelta.F_CHILDREN),
				ElementChangedEvent.POST_CHANGE));
		assertFirstProject(sizeBefore);
	}

	private static IJavaElementDelta newDelta(IJavaProject project, int flags) {
		return (IJavaElementDelta) Proxy.newProxyInstance(Storage2UriMapperJavaImplTest.class.getClassLoader(),
				new Class<?>[] { IJavaElementDelta.class }, (proxy, method, args) -> {
					switch (method.getName()) {
						case "getElement":
							return project;
						case "getKind":
							return IJavaElementDelta.CHANGED;
						case "getFlags":
							return flags;
						case "getAffectedChildren":
							return new IJavaElementDelta[0];
						case "toString":
							return "newDelta(" + project.getElementName() + ", 0x" + Integer.toHexString(flags) + ")";
						case "hashCode":
							return System.identityHashCode(proxy);
						case "equals":
							return proxy == args[0];
						default:
							throw new UnsupportedOperationException("Unexpected call to " + method);
					}
				});
	}

	private static void collectProjectDeltas(ElementChangedEvent event, String threadName, List<String> out) {
		String prefix = "[" + threadName + " " + typeToString(event.getType()) + "] ";
		collectProjectDeltas(event.getDelta(), prefix, out);
	}

	private static void collectProjectDeltas(IJavaElementDelta delta, String prefix, List<String> out) {
		if (delta == null) {
			return;
		}
		IJavaElement element = delta.getElement();
		if (element != null && element.getElementType() == IJavaElement.JAVA_PROJECT) {
			out.add(prefix + element.getElementName() + " kind=" + kindToString(delta.getKind()) + "(" + delta.getKind() + ")"
					+ " flags=0x" + Integer.toHexString(delta.getFlags()) + flagsToString(delta.getFlags()));
		}
		IJavaElementDelta[] children = delta.getAffectedChildren();
		if (children != null) {
			for (IJavaElementDelta child : children) {
				collectProjectDeltas(child, prefix, out);
			}
		}
	}

	private static String typeToString(int type) {
		if (type == ElementChangedEvent.POST_CHANGE) {
			return "POST_CHANGE";
		}
		return "type=" + type;
	}

	private static String kindToString(int kind) {
		if ((kind & IJavaElementDelta.REMOVED) != 0) {
			return "REMOVED";
		}
		if ((kind & IJavaElementDelta.ADDED) != 0) {
			return "ADDED";
		}
		return "CHANGED";
	}

	private static String flagsToString(int flags) {
		StringBuilder result = new StringBuilder();
		if ((flags & IJavaElementDelta.F_OPENED) != 0) {
			result.append("|F_OPENED");
		}
		if ((flags & IJavaElementDelta.F_CLOSED) != 0) {
			result.append("|F_CLOSED");
		}
		if ((flags & IJavaElementDelta.F_CHILDREN) != 0) {
			result.append("|F_CHILDREN");
		}
		if ((flags & IJavaElementDelta.F_CONTENT) != 0) {
			result.append("|F_CONTENT");
		}
		if ((flags & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
			result.append("|F_CLASSPATH_CHANGED");
		}
		if ((flags & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) {
			result.append("|F_RESOLVED_CLASSPATH_CHANGED");
		}
		return result.toString();
	}

	private static String deltasMsg(List<String> observedDeltas) {
		return "Observed project deltas:\n" + Joiner.on("\n").join(observedDeltas);
	}

	/**
	 * Copies the live cache entry-wise under the same locks production code uses (the map
	 * itself, then each entry's {@code associatedRoots}), so failure messages can't hit a
	 * ConcurrentModificationException or print a torn state when another thread reconciles
	 * concurrently. The dump renders each entry's registered roots, which tells which
	 * project's registration survived: the plain map dump can't, because the entry path
	 * always comes from the jar's own project.
	 */
	private static final class CacheSnapshot {
		final Map<String, Map<String, IPackageFragmentRoot>> rootsByKey;
		final String dump;

		CacheSnapshot(Map<String, Map<String, IPackageFragmentRoot>> rootsByKey, String dump) {
			this.rootsByKey = rootsByKey;
			this.dump = dump;
		}
	}

	private CacheSnapshot snapshotCache() throws Exception {
		Map<String, PackageFragmentRootData> live = getCachedPackageFragmentRootData();
		List<Map.Entry<String, PackageFragmentRootData>> entries;
		synchronized (live) {
			entries = new ArrayList<>(live.entrySet());
		}
		Map<String, Map<String, IPackageFragmentRoot>> rootsByKey = new LinkedHashMap<>();
		StringBuilder dump = new StringBuilder();
		for (Map.Entry<String, PackageFragmentRootData> entry : entries) {
			Map<String, IPackageFragmentRoot> roots = entry.getValue().associatedRoots;
			Map<String, IPackageFragmentRoot> rootsCopy;
			synchronized (roots) {
				rootsCopy = new LinkedHashMap<>(roots);
			}
			rootsByKey.put(entry.getKey(), rootsCopy);
			dump.append(entry.getKey()).append(" roots=").append(rootsCopy.keySet()).append('\n');
		}
		return new CacheSnapshot(rootsByKey, dump.toString());
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
		assertNonProjects("");
	}

	public void assertNonProjects(String context) throws Exception {
		CacheSnapshot snapshot = snapshotCache();
		Assert.assertEquals(snapshot.dump + context, 0, snapshot.rootsByKey.size());
	}

	public void assertFirstProject(int sizeBefore) throws Exception {
		assertFirstProject(sizeBefore, "");
	}

	public void assertFirstProject(int sizeBefore, String context) throws Exception {
		CacheSnapshot snapshot = snapshotCache();
		Assert.assertEquals(snapshot.dump + context, sizeBefore + 1, snapshot.rootsByKey.size());
		Assert.assertNotNull(snapshot.dump + context,
				snapshot.rootsByKey.keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().orElse(null));
		snapshot.rootsByKey.entrySet().forEach((Map.Entry<String, Map<String, IPackageFragmentRoot>> it) -> {
			boolean java9OrNewer = Iterables.any(it.getValue().values(),
					r -> "JrtPackageFragmentRoot".equals(r.getClass().getSimpleName()));
			if (java9OrNewer) {
				Assert.assertTrue(it.getValue().size() + " / " + it.getKey() + "\n" + context,
						Lists.newArrayList(44 /* java11 */, 49, 63 /* java9 */, 51 /* java14 */, 50 /* java15 */)
								.contains(Integer.valueOf(it.getValue().size())));
			} else {
				Assert.assertEquals(it.getKey() + "\n" + context, 1, it.getValue().size());
			}
			String head = Iterables.getFirst(it.getValue().keySet(), null);
			Assert.assertTrue(head + "\n" + context, head.startsWith("=testProject/"));
		});
	}

	public void assertBothProjects(int sizeBefore) throws Exception {
		assertBothProjects(sizeBefore, "");
	}

	public void assertBothProjects(int sizeBefore, String context) throws Exception {
		CacheSnapshot snapshot = snapshotCache();
		Assert.assertEquals(snapshot.dump + context, sizeBefore + 1, snapshot.rootsByKey.size());
		Assert.assertNotNull(snapshot.dump + context,
				snapshot.rootsByKey.keySet().stream().filter(it -> it.contains("foo.jar")).findFirst().orElse(null));
		snapshot.rootsByKey.entrySet().forEach((Map.Entry<String, Map<String, IPackageFragmentRoot>> it) -> {
			Assert.assertEquals(it.getValue().size() + "/" + it.getKey() + "\n" + context, 2, it.getValue().size());
			String msg = Joiner.on("\n").join(it.getValue().keySet()) + "\n" + context;
			Assert.assertTrue(msg, Iterables.any(it.getValue().keySet(), r -> r.startsWith("=testProject/")));
			Assert.assertTrue(msg, Iterables.any(it.getValue().keySet(), r -> r.startsWith("=testProject2/")));
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
