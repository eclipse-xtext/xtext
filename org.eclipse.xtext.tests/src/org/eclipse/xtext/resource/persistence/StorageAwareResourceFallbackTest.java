/*******************************************************************************
 * Copyright (c) 2026 Eclipse Xtext and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.IFileSystemAccessExtension3;
import org.eclipse.xtext.nodemodel.serialization.ResourceStorageTest;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testlanguages.simpleExpressions.Expression;
import org.junit.Test;

import com.google.inject.Injector;

public class StorageAwareResourceFallbackTest {

	@Test
	public void filterRejectionFallsBackToSourceParsing() throws Exception {
		Injector injector = new ResourceStorageTest.CustomSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		IResourceStorageFacade storageFacade = injector.getInstance(IResourceStorageFacade.class);

		URI originalUri = URI.createURI("synthetic:/Original.simpleexpressionstestlanguage");
		StorageAwareResource original = (StorageAwareResource) resourceSet.createResource(originalUri);
		ResourceStorageTest.InMemoryURIConverter originalConverter = new ResourceStorageTest.InMemoryURIConverter();
		originalConverter.addModel(originalUri.toString(), "(d - e) / e * d");
		resourceSet.setURIConverter(originalConverter);
		original.load(null);

		ByteArrayOutputStream validStorage = new ByteArrayOutputStream();
		storageFacade.createResourceStorageWritable(validStorage).writeResource(original);
		byte[] corruptedStorage = replaceResourceDescription(validStorage.toByteArray());

		URI targetUri = URI.createURI("synthetic:/Target.simpleexpressionstestlanguage");
		StorageAwareResource target = (StorageAwareResource) resourceSet.createResource(targetUri);
		ResourceStorageTest.InMemoryURIConverter targetConverter = new ResourceStorageTest.InMemoryURIConverter();
		targetConverter.addModel(targetUri.toString(), "a + b");
		resourceSet.setURIConverter(targetConverter);
		setResourceStorageFacade(target, new FailingStorageFacade(storageFacade, corruptedStorage));

		target.load(null);

		assertFalse(target.isLoadedFromStorage());
		assertFalse(target.getContents().isEmpty());
		assertTrue(target.getContents().get(0) instanceof Expression);
	}

	private void setResourceStorageFacade(StorageAwareResource resource, IResourceStorageFacade facade)
			throws Exception {
		Field field = StorageAwareResource.class.getDeclaredField("resourceStorageFacade");
		field.setAccessible(true);
		field.set(resource, facade);
	}

	private byte[] replaceResourceDescription(byte[] storage) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(storage));
				ZipOutputStream output = new ZipOutputStream(result)) {
			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				output.putNextEntry(new ZipEntry(entry.getName()));
				if ("resource-description".equals(entry.getName())) {
					ObjectOutputStream objects = new ObjectOutputStream(output);
					objects.writeObject(new Date());
					objects.flush();
				} else {
					input.transferTo(output);
				}
				output.closeEntry();
			}
		}
		return result.toByteArray();
	}

	private static class FailingStorageFacade implements IResourceStorageFacade {
		private final IResourceStorageFacade delegate;
		private final byte[] storage;

		FailingStorageFacade(IResourceStorageFacade delegate, byte[] storage) {
			this.delegate = delegate;
			this.storage = storage;
		}

		@Override
		public boolean shouldLoadFromStorage(StorageAwareResource resource) {
			return true;
		}

		@Override
		public boolean hasStorageFor(URI uri) {
			return true;
		}

		@Override
		public ResourceStorageLoadable getOrCreateResourceStorageLoadable(StorageAwareResource resource) {
			return new ResourceStorageLoadable(new ByteArrayInputStream(storage), false);
		}

		@Override
		public void saveResource(StorageAwareResource resource, IFileSystemAccessExtension3 fsa) {
			delegate.saveResource(resource, fsa);
		}

		@Override
		public ResourceStorageWritable createResourceStorageWritable(OutputStream outputStream) {
			return delegate.createResourceStorageWritable(outputStream);
		}

		@Override
		public ResourceStorageLoadable createResourceStorageLoadable(InputStream inputStream) {
			return delegate.createResourceStorageLoadable(inputStream);
		}
	}
}
