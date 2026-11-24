/**
 * Copyright (c) 2014, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.resource.persistence;

import static org.eclipse.xtext.xbase.lib.IterableExtensions.*;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.xtext.nodemodel.impl.SerializableNodeModel;
import org.eclipse.xtext.nodemodel.serialization.DeserializationConversionContext;
import org.eclipse.xtext.parser.ParseResult;

import com.google.common.io.CharStreams;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class ResourceStorageLoadable {
	private static final Logger LOG = Logger.getLogger(ResourceStorageLoadable.class);

	private final InputStream in;

	private final boolean storeNodeModel;

	public ResourceStorageLoadable(InputStream in, boolean storeNodeModel) {
		this.in = in;
		this.storeNodeModel = storeNodeModel;
	}

	protected void loadIntoResource(StorageAwareResource resource) throws IOException {
		if (!resource.isLoadedFromStorage())
			throw new IllegalStateException("Please use StorageAwareResource#load(ResourceStorageLoadable).");
		try (ZipInputStream zin = new ZipInputStream(in)) {
			loadEntries(resource, zin);
		}
	}

	/**
	 * Load entries from the storage. Overriding methods should first delegate to super before adding their own entries.
	 */
	protected void loadEntries(StorageAwareResource resource, ZipInputStream zipIn) throws IOException {
		zipIn.getNextEntry();
		readContents(resource, new BufferedInputStream(zipIn));
		zipIn.getNextEntry();
		readResourceDescription(resource, new BufferedInputStream(zipIn));
		if (storeNodeModel) {
			zipIn.getNextEntry();
			readNodeModel(resource, new BufferedInputStream(zipIn));
		}
	}

	protected void readContents(StorageAwareResource resource, InputStream inputStream) throws IOException {
		new BinaryResourceImpl.EObjectInputStream(inputStream, Collections.emptyMap()) {
			@Override
			public int readCompressedInt() throws IOException {
				//HACK! null resource set, to avoid usage of resourceSet's package registry
				resourceSet = null;
				return super.readCompressedInt();
			}

			@Override
			public InternalEObject loadEObject() throws IOException {
				InternalEObject result = super.loadEObject();
				handleLoadEObject(result, this);
				return result;
			}
		}.loadResource(resource);
	}

	protected void handleLoadEObject(InternalEObject loaded, BinaryResourceImpl.EObjectInputStream input)
			throws IOException {
	}

	protected void readResourceDescription(StorageAwareResource resource, InputStream inputStream) throws IOException {
		try {
			@SuppressWarnings("resource")
			ObjectInputStream objectInputStream = createObjectInputStream(inputStream);
			SerializableResourceDescription description = (SerializableResourceDescription) objectInputStream.readObject();
			description.updateResourceURI(resource.getURI());
			resource.setResourceDescription(description);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Creates the filtered input stream used to deserialize the resource description.
	 * <p>
	 * To customize the filter, override {@link #createObjectInputFilter()}. The filter is composed
	 * with the process-global serialization filter here.
	 * <p>
	 * If this method is overridden to customize the underlying stream, wrap the input before calling
	 * {@code super.createObjectInputStream(inputStream)}. Do not call
	 * {@link ObjectInputStream#setObjectInputFilter(ObjectInputFilter)} on the returned stream; the
	 * filter is already set.
	 *
	 * @since 2.45
	 */
	protected ObjectInputStream createObjectInputStream(InputStream inputStream) throws IOException {
		ObjectInputStream result = new ObjectInputStream(inputStream);
		result.setObjectInputFilter(ResourceStorageInputFilter.composeWithGlobalFilter(createObjectInputFilter()));
		return result;
	}

	/**
	 * Returns the filter used for storage deserialization. Subclasses that add their own serializable
	 * types may override this method and return a {@link ResourceStorageInputFilter} with additional
	 * class names. The returned filter is composed with the process-global serialization filter by
	 * {@link #createObjectInputStream(InputStream)}; a rejection from either filter wins. Overriders
	 * do not need to wrap their result themselves.
	 *
	 * @since 2.45
	 */
	protected ObjectInputFilter createObjectInputFilter() {
		return ResourceStorageInputFilter.DEFAULT;
	}

	protected void readNodeModel(StorageAwareResource resource, InputStream inputStream) throws IOException {
		if (!resource.customReadNodeModel(inputStream)) {
			SerializableNodeModel serializableNodeModel = new SerializableNodeModel(resource);
			// if this is a synthetic resource (i.e. tests or so, don't load the node model)
			URIConverter uriConverter = resource.getResourceSet().getURIConverter();
			URI resourceUri = resource.getURI();
			if (!uriConverter.exists(resourceUri, resource.getResourceSet().getLoadOptions())) {
				LOG.info("Skipping loading node model for synthetic resource " + resourceUri);
				return;
			}
			try (InputStreamReader reader = new InputStreamReader(
					uriConverter.createInputStream(resourceUri),
					resource.getEncoding())) {
				String completeContent = CharStreams.toString(reader);
				DeserializationConversionContext deserializationContext = new DeserializationConversionContext(resource,
						completeContent);
				serializableNodeModel.readObjectData(new DataInputStream(inputStream), deserializationContext);
				resource.setParseResult(new ParseResult(head(resource.getContents()), serializableNodeModel.root,
						deserializationContext.hasErrors()));
			}
		}
	}

}
