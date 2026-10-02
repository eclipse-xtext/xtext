/**
 * Copyright (c) 2026 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ui.tests.refactoring;

import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ltk.core.refactoring.resource.CopyResourcesDescriptor;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.testlanguages.fileAware.ui.tests.FileAwareTestLanguageUiInjectorProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author koehnlein - Initial contribution and API
 * @author mx990 - adapted from ResourceMoveTest
 */
@InjectWith(FileAwareTestLanguageUiInjectorProvider.class)
@RunWith(XtextRunner.class)
public class ResourceCopyTest extends AbstractResourceRelocationTest {
	@Test
	public void testCopyFile() throws Exception {
		String model1 =
				"package foo.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		IFile x = file("foo/bar/X.fileawaretestlanguage", model1);
		String model2 =
				"package foo\n" +
				"element Y {\n" +
				"	ref bar.X\n" +
				"}\n";
		file("foo/Y.fileawaretestlanguage", model2);
		performCopy(folder("foo/baz"), x);
		Assert.assertTrue(x.exists());
		assertFileContents("foo/bar/X.fileawaretestlanguage", model1);
		assertFileContents("foo/Y.fileawaretestlanguage", model2);
		String expectation1 =
				"package foo.baz\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		assertFileContents("foo/baz/X.fileawaretestlanguage", expectation1);
	}

	@Test
	public void testCopyFile_2() throws Exception {
		String model1 =
				"package foo.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		file("foo/bar/X.fileawaretestlanguage", model1);
		String model2 =
				"package foo\n" +
				"element Y {\n" +
				"	ref bar.X\n" +
				"}\n";
		IFile y = file("foo/Y.fileawaretestlanguage", model2);
		performCopy(folder("foo/baz"), y);
		Assert.assertTrue(y.exists());
		assertFileContents("foo/bar/X.fileawaretestlanguage", model1);
		assertFileContents("foo/Y.fileawaretestlanguage", model2);
		String expectation1 =
				"package foo.baz\n" +
				"element Y {\n" +
				"	ref foo.bar.X\n" +
				"}\n";
		assertFileContents("foo/baz/Y.fileawaretestlanguage", expectation1);
	}

	@Test
	public void testCopyFiles() throws Exception {
		String model1 =
				"package foo.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		IFile x = file("foo/X.fileawaretestlanguage", model1);
		String model2 =
				"package foo\n" +
				"element Y {\n" +
				"	ref bar.X\n" +
				"}\n";
		IFile y = file("foo/Y.fileawaretestlanguage", model2);
		performCopy(folder("foo/baz"), x, y);
		Assert.assertTrue(y.exists());
		assertFileContents("foo/X.fileawaretestlanguage", model1);
		assertFileContents("foo/Y.fileawaretestlanguage", model2);
		String expectation1 =
				"package foo.baz\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		assertFileContents("foo/baz/X.fileawaretestlanguage", expectation1);
		String expectation2 =
				"package foo.baz\n" +
				"element Y {\n" +
				"	ref X\n" +
				"}\n";
		assertFileContents("foo/baz/Y.fileawaretestlanguage", expectation2);
	}

	@Test
	public void testCopyDirectory() throws Exception {
		String model1 =
				"package foo.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		IFile x = file("foo/bar/X.fileawaretestlanguage", model1);
		String model2 =
				"package foo\n" +
				"element Y {\n" +
				"	ref bar.X\n" +
				"}\n";
		file("foo/Y.fileawaretestlanguage", model2);
		performCopy(folder("foo/baz"), x.getParent());
		Assert.assertTrue(x.exists());
		assertFileContents("foo/bar/X.fileawaretestlanguage", model1);
		assertFileContents("foo/Y.fileawaretestlanguage", model2);
		String expectation1 =
				"package foo.baz.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		assertFileContents("foo/baz/bar/X.fileawaretestlanguage", expectation1);
	}

	@Test
	public void testCopyDirectoryToRoot() throws Exception {
		String model1 =
				"package foo.bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		IFile x = file("foo/bar/X.fileawaretestlanguage", model1);
		String model2 =
				"package foo\n" +
				"element Y {\n" +
				"	ref bar.X\n" +
				"}\n";
		file("foo/Y.fileawaretestlanguage", model2);
		performCopy(project, x.getParent());
		Assert.assertTrue(x.exists());
		assertFileContents("foo/bar/X.fileawaretestlanguage", model1);
		assertFileContents("foo/Y.fileawaretestlanguage", model2);
		String expectation1 =
				"package bar\n" +
				"element X {\n" +
				"	ref X\n" +
				"}\n";
		assertFileContents("bar/X.fileawaretestlanguage", expectation1);
	}

	protected void performCopy(IContainer theDestination, IResource... theResources) throws Exception {
		CopyResourcesDescriptor copyResourcesDescriptor = new CopyResourcesDescriptor();
		copyResourcesDescriptor.setResourcePaths(Stream.of(theResources).map(IResource::getFullPath).toArray(IPath[]::new));
		copyResourcesDescriptor.setDestinationPaths(Stream.of(theResources).map(IResource::getFullPath).map(IPath::lastSegment)
				.map(theDestination.getFullPath()::append).toArray(IPath[]::new));
		performRefactoring(copyResourcesDescriptor);
	}
}
