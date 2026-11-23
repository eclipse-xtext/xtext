/*******************************************************************************
 * Copyright (c) 2026 Eclipse Xtext and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.naming.QualifiedName;
import org.junit.Test;

public class ResourceStorageInputFilterTest {

	private static class CustomClass implements Serializable {
		private static final long serialVersionUID = 1L;
	}

	@Test
	public void readsDefaultResourceDescription() throws Exception {
		URI uri = URI.createURI("file:/foo/bar.baz.foo");
		SerializableResourceDescription description = new SerializableResourceDescription();
		description.setURI(uri);
		SerializableEObjectDescription objectDescription = new SerializableEObjectDescription();
		objectDescription.setEObjectURI(uri.appendFragment("baz"));
		objectDescription.setEClass(EcorePackage.eINSTANCE.getEAttribute());
		objectDescription.qualifiedName = QualifiedName.create("foo", "baz");
		objectDescription.userData = new HashMap<>();
		objectDescription.userData.put("myKey", "myValue");
		description.setDescriptions(Collections.singletonList(objectDescription));
		SerializableReferenceDescription reference = new SerializableReferenceDescription();
		reference.setSourceEObjectUri(uri.appendFragment("foo"));
		reference.setTargetEObjectUri(uri.appendFragment("bar"));
		reference.setContainerEObjectURI(uri.appendFragment("baz"));
		reference.setEReference(EcorePackage.eINSTANCE.getEAnnotation_Contents());
		reference.setIndexInList(1);
		description.setReferences(Collections.singletonList(reference));
		description.setImportedNames(Arrays.asList(QualifiedName.create("foo"), QualifiedName.create("foo", "bar")));

		Object result = read(description, ResourceStorageInputFilter.DEFAULT);
		assertTrue(result instanceof SerializableResourceDescription);
		SerializableResourceDescription deserialized = (SerializableResourceDescription) result;
		assertEquals(description.getURI(), deserialized.getURI());
		assertEquals(description.getImportedNames(), deserialized.getImportedNames());
		assertEquals(description.getDescriptions().size(), deserialized.getDescriptions().size());
		assertEquals(description.getReferences().size(), deserialized.getReferences().size());
	}

	@Test
	public void readsAssociationMaps() throws Exception {
		Map<String, String> logicalMap = new LinkedHashMap<>();
		logicalMap.put("source", "target");
		Map<String, Set<String>> associations = new LinkedHashMap<>();
		associations.put("source", new LinkedHashSet<>(Arrays.asList("target1", "target2")));

		Object logicalResult = read(logicalMap, ResourceStorageInputFilter.DEFAULT);
		Object associationResult = read(associations, ResourceStorageInputFilter.DEFAULT);
		assertEquals(logicalMap, logicalResult);
		assertEquals(associations, associationResult);
	}

	@Test(expected = InvalidClassException.class)
	public void rejectsForeignClass() throws Exception {
		read(new CustomClass(), ResourceStorageInputFilter.DEFAULT);
	}

	@Test
	public void allowsAdditionalClass() throws Exception {
		ObjectInputFilter filter = new ResourceStorageInputFilter(
				Collections.singleton(CustomClass.class.getName()));
		Object result = read(new CustomClass(), filter);
		assertTrue(result instanceof CustomClass);
	}

	private Object read(Object value, ObjectInputFilter filter) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(value);
		}
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			in.setObjectInputFilter(filter);
			return in.readObject();
		}
	}
}
