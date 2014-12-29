/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.resource

import com.google.common.collect.Sets
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.Map
import java.util.Set
import java.util.zip.ZipInputStream
import org.eclipse.xtext.common.types.JvmIdentifiableElement
import org.eclipse.xtext.resource.persistence.ResourceStorageLoadable
import org.eclipse.xtext.resource.persistence.StorageAwareResource
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator

class BatchLinkableResourceStorageLoadable extends ResourceStorageLoadable {
	
	new(InputStream in) {
		super(in)
	}
	
	override protected loadEntries(StorageAwareResource resource, ZipInputStream zipIn) {
		super.loadEntries(resource, zipIn)
		if (resource instanceof BatchLinkableResource) {
			readAssociationsAdapter(resource, zipIn)
		}
	}
	
	protected def void readAssociationsAdapter(BatchLinkableResource resource, ZipInputStream stream) {
		val existing = resource.eAdapters.filter(JvmModelAssociator.Adapter).head
		val adapter = existing
			?: (new JvmModelAssociator.Adapter()=> [
				resource.eAdapters += it
			]) 
		
		stream.nextEntry
		val objIn = new ObjectInputStream(stream)
		val logicalMap = objIn.readObject as Map<String,String>
		logicalMap.entrySet.forEach [
			adapter.logicalContainerMap.put(resource.getEObject(key), resource.getEObject(value) as JvmIdentifiableElement)
		]
		val sourceToTargetMap = objIn.readObject as Map<String,Set<String>>
		sourceToTargetMap.entrySet.forEach [
			adapter.sourceToTargetMap.put(resource.getEObject(key), Sets.newHashSet(value.map[resource.getEObject(it)]))
		]
		val targetToSourceMap = objIn.readObject as Map<String,Set<String>>
		targetToSourceMap.entrySet.forEach [
			adapter.targetToSourceMap.put(resource.getEObject(key), Sets.newHashSet(value.map[resource.getEObject(it)]))
		]
	}
	
}

