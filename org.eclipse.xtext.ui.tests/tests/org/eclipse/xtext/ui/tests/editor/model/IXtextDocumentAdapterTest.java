/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.xtext.ui.tests.editor.model;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.model.DocumentTokenSource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for IXtextDocument adapter behavior using a real XtextDocument and a workspace file.
 */
public class IXtextDocumentAdapterTest {

	private IProject project;
	private IFile file;
	private XtextResource resource;
	private XtextDocument doc;

	/**
	 * Dummy token source that doesn't rely on injected lexer provider. It returns an EOF token stream.
	 */
	private static class DummyDocumentTokenSource extends DocumentTokenSource {
		@Override
		protected TokenSource createTokenSource(String string) {
			return new org.antlr.runtime.TokenSource() {
				@Override
				public Token nextToken() {
					return Token.EOF_TOKEN;
				}

				@Override
				public String getSourceName() {
					return "";
				}
			};
		}
	}

	@Before
	public void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProjectAdapter");
		if (!project.exists()) {
			project.create(null);
		}
		if (!project.isOpen()) {
			project.open(null);
		}
		IFolder src = project.getFolder("src");
		if (!src.exists()) {
			src.create(true, true, null);
		}
		file = src.getFile("test.xtext");
		if (!file.exists()) {
			try (ByteArrayInputStream in = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8))) {
				file.create(in, true, null);
			}
		}

		// create an XtextResource with a platform:/resource URI that points to the created file
		XtextResourceSet set = new XtextResourceSet();
		URI emfUri = URI.createURI("platform:/resource/" + project.getName() + "/src/test.xtext");
		resource = (XtextResource) set.createResource(emfUri);

		// create document with a dummy token source (avoids needing injected lexer provider) and set input
		doc = new XtextDocument(new DummyDocumentTokenSource(), null);
		doc.setInput(resource);
	}

	@After
	public void tearDown() throws CoreException {
		if (project != null && project.exists()) {
			project.delete(true, true, null);
		}
	}

	@Test
	public void testAdapterSelf() throws Exception {
		IXtextDocument self = doc.getAdapter(IXtextDocument.class);
		assertSame("getAdapter(IXtextDocument) should return self", doc, self);
	}

	@Test
	public void adapterEmfUri() throws Exception {
		URI adapted = doc.getAdapter(URI.class);
		assertEquals("getAdapter(EMF URI) should return resource URI", resource.getURI(), adapted);
	}

	@Test
	public void testAdapterIFile() throws Exception {
		IFile adaptedFile = doc.getAdapter(IFile.class);
		// should map to the workspace file
		assertEquals("getAdapter(IFile) should return workspace IFile", file.getFullPath(), adaptedFile.getFullPath());
	}

	@Test
	public void testAdapterJavaNetUri() throws Exception {
		java.net.URI javaUri = doc.getAdapter(java.net.URI.class);
		// should map to file system location URI
		assertEquals("getAdapter(java.net.URI) should return file location URI", file.getLocationURI(), javaUri);
	}
}