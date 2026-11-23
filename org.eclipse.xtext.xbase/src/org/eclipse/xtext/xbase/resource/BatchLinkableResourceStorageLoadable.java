/**
 * Copyright (c) 2014, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.resource;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.resource.persistence.ResourceStorageLoadable;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.xbase.compiler.DocumentationAdapter;
import org.eclipse.xtext.xbase.jvmmodel.JvmIdentifiableMetaData;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;

import com.google.common.collect.FluentIterable;

public class BatchLinkableResourceStorageLoadable extends ResourceStorageLoadable {

	public BatchLinkableResourceStorageLoadable(InputStream in, boolean storeNodeModel) {
		super(in, storeNodeModel);
	}

	@Override
	protected void loadEntries(StorageAwareResource resource, ZipInputStream zipIn) throws IOException {
		super.loadEntries(resource, zipIn);
		if (resource instanceof BatchLinkableResource) {
			readAssociationsAdapter((BatchLinkableResource) resource, zipIn);
		}
	}

	@Override
	protected void handleLoadEObject(InternalEObject loaded, BinaryResourceImpl.EObjectInputStream input)
			throws IOException {
		super.handleLoadEObject(loaded, input);
		if (input.readBoolean()) {
			DocumentationAdapter documentationAdapter = new DocumentationAdapter(input.readString());
			loaded.eAdapters().add(documentationAdapter);
		}
		if (input.readBoolean()) {
			JvmIdentifiableMetaData metaData = new JvmIdentifiableMetaData();
			metaData.setSynthetic(input.readBoolean());
			loaded.eAdapters().add(metaData);
		}
	}

	@SuppressWarnings("unchecked")
	protected void readAssociationsAdapter(BatchLinkableResource resource, ZipInputStream stream) throws IOException {
		try {
			JvmModelAssociator.Adapter adapter = FluentIterable.from(resource.eAdapters())
					.filter(JvmModelAssociator.Adapter.class)
					.first()
					.or(()->{
						JvmModelAssociator.Adapter newAdapter = new JvmModelAssociator.Adapter();
						resource.eAdapters().add(newAdapter);
						return newAdapter;
					});
			
			stream.getNextEntry();
			
			try (ObjectInputStream objIn = createObjectInputStream(
					new NonClosingFilterInputStream(new BufferedInputStream(stream)))) {
				Map<String, String> logicalMap = (Map<String, String>) objIn.readObject();
				logicalMap.forEach((key, value)->{
					adapter.logicalContainerMap.put(resource.getEObject(key), (JvmIdentifiableElement)resource.getEObject(value));
				});
				Map<String, Set<String>> sourceToTargetMap = (Map<String, Set<String>>) objIn.readObject();
				sourceToTargetMap.forEach((source, targets)->{
					adapter.sourceToTargetMap.put(resource.getEObject(source), fragmentsToObjects(resource, targets));
				});
				Map<String, Set<String>> targetToSourceMap = (Map<String, Set<String>>) objIn.readObject();
				targetToSourceMap.forEach((target, sources)->{
					adapter.targetToSourceMap.put(resource.getEObject(target), fragmentsToObjects(resource, sources)); 
				});
			}
		} catch(ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	private Set<EObject> fragmentsToObjects(BatchLinkableResource resource, Set<String> fragments) {
		return FluentIterable.from(fragments)
			.transform(resource::getEObject)
			.copyInto(new LinkedHashSet<>());
	}
	
	private static class NonClosingFilterInputStream extends FilterInputStream {

		public NonClosingFilterInputStream(InputStream in) throws IOException {
			super(in);
		}
		@Override
	    public void close() throws IOException {
	        // don't close the delegate since it would close the underlying zip file
	    }
		
	}
}
