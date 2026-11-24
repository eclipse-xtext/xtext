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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectInputFilter;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

	public static class CustomResourceDescription extends SerializableResourceDescription {
		public CustomResourceDescription() {
		}
	}

	public static class CustomEObjectDescription extends SerializableEObjectDescription {
		public CustomEObjectDescription() {
		}
	}

	public static class CustomReferenceDescription extends SerializableReferenceDescription {
		public CustomReferenceDescription() {
		}
	}

	private static class TestLoadable extends ResourceStorageLoadable {
		TestLoadable() {
			super(new ByteArrayInputStream(new byte[0]), false);
		}

		ObjectInputStream open(InputStream inputStream) throws IOException {
			return createObjectInputStream(inputStream);
		}
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
	public void qualifiedNameFilterRejectionIsIoException() throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(
				new ByteArrayInputStream(serialize(new ArrayList<>(Arrays.asList("a", "b")))))) {
			in.setObjectInputFilter(info -> ObjectInputFilter.Status.REJECTED);
			try {
				SerializationExtensions.readQualifiedName(in);
				fail("Expected an IOException");
			} catch (IOException expected) {
				assertTrue(expected instanceof InvalidClassException);
			}
		}
	}

	@Test
	public void castedObjectFilterRejectionIsIoException() throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(
				new ByteArrayInputStream(serialize(new CustomClass())))) {
			in.setObjectInputFilter(info -> ObjectInputFilter.Status.REJECTED);
			try {
				SerializationExtensions.readCastedObject(in);
				fail("Expected an IOException");
			} catch (IOException expected) {
				assertTrue(expected instanceof InvalidClassException);
			}
		}
	}

	@Test
	public void allowsStorageTypeSubclasses() throws Exception {
		URI uri = URI.createURI("file:/foo/bar.baz.foo");
		CustomResourceDescription description = new CustomResourceDescription();
		description.setURI(uri);
		CustomEObjectDescription objectDescription = new CustomEObjectDescription();
		objectDescription.setEObjectURI(uri.appendFragment("baz"));
		objectDescription.setEClass(EcorePackage.eINSTANCE.getEAttribute());
		objectDescription.qualifiedName = QualifiedName.create("foo", "baz");
		objectDescription.userData = new HashMap<>();
		description.setDescriptions(Collections.singletonList(objectDescription));
		CustomReferenceDescription reference = new CustomReferenceDescription();
		reference.setSourceEObjectUri(uri.appendFragment("foo"));
		reference.setTargetEObjectUri(uri.appendFragment("bar"));
		reference.setContainerEObjectURI(uri.appendFragment("baz"));
		reference.setEReference(EcorePackage.eINSTANCE.getEAnnotation_Contents());
		reference.setIndexInList(1);
		description.setReferences(Collections.singletonList(reference));

		Object result = read(description, ResourceStorageInputFilter.DEFAULT);
		assertTrue(result instanceof CustomResourceDescription);
		SerializableResourceDescription deserialized = (SerializableResourceDescription) result;
		assertTrue(deserialized.getDescriptions().get(0) instanceof CustomEObjectDescription);
		assertTrue(deserialized.getReferences().get(0) instanceof CustomReferenceDescription);
	}

	@Test
	public void allowsAdditionalClass() throws Exception {
		ObjectInputFilter filter = new ResourceStorageInputFilter(
				Collections.singleton(CustomClass.class.getName()));
		Object result = read(new CustomClass(), filter);
		assertTrue(result instanceof CustomClass);
	}

	@Test(expected = InvalidClassException.class)
	public void rejectsDeeplyNestedList() throws Exception {
		Object value = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			List<Object> parent = new ArrayList<>();
			parent.add(value);
			value = parent;
		}
		read(value, ResourceStorageInputFilter.DEFAULT);
	}

	@Test(expected = InvalidClassException.class)
	public void rejectsOversizedArray() throws Exception {
		ObjectInputFilter filter = new ResourceStorageInputFilter(Collections.emptySet(), 100, 1000, 10);
		read(new String[20], filter);
	}

	@Test(expected = InvalidClassException.class)
	public void rejectsTooManyReferences() throws Exception {
		ObjectInputFilter filter = new ResourceStorageInputFilter(Collections.emptySet(), 100, 1, 1000);
		read(new String[] { "a", "b" }, filter);
	}

	@Test(expected = InvalidClassException.class)
	public void composedFilterUsesStricterGlobalPolicy() throws Exception {
		ObjectInputFilter global = info -> info.serialClass() == CustomClass.class
				? ObjectInputFilter.Status.REJECTED
				: ObjectInputFilter.Status.UNDECIDED;
		ObjectInputFilter storage = info -> info.serialClass() == CustomClass.class
				? ObjectInputFilter.Status.ALLOWED
				: ObjectInputFilter.Status.UNDECIDED;
		read(new CustomClass(), ResourceStorageInputFilter.compose(global, storage));
	}

	@Test
	public void loadableUsesCustomFilter() throws Exception {
		TestLoadable loadable = new TestLoadable() {
			@Override
			protected ObjectInputFilter createObjectInputFilter() {
				return new ResourceStorageInputFilter(Collections.singleton(CustomClass.class.getName()));
			}
		};
		try (ObjectInputStream in = loadable.open(new ByteArrayInputStream(serialize(new CustomClass())))) {
			assertTrue(in.readObject() instanceof CustomClass);
		}
	}

	@Test
	public void limitsCanBeConfiguredWithSystemProperties() throws Exception {
		String key = ResourceStorageInputFilter.MAX_REFERENCES_PROPERTY;
		String previous = System.getProperty(key);
		try {
			System.setProperty(key, "1");
			ObjectInputFilter filter = new ResourceStorageInputFilter(Collections.emptySet());
			try {
				read(new String[] { "a", "b" }, filter);
				fail("Expected an IOException");
			} catch (InvalidClassException expected) {
				// expected
			}
		} finally {
			if (previous == null) {
				System.clearProperty(key);
			} else {
				System.setProperty(key, previous);
			}
		}
	}

	@Test(expected = InvalidClassException.class)
	public void loadableUsesFilter() throws Exception {
		TestLoadable loadable = new TestLoadable();
		try (ObjectInputStream in = loadable.open(new ByteArrayInputStream(serialize(new CustomClass())))) {
			in.readObject();
		}
	}

	private Object read(Object value, ObjectInputFilter filter) throws Exception {
		try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialize(value)))) {
			in.setObjectInputFilter(filter);
			return in.readObject();
		}
	}

	private byte[] serialize(Object value) throws IOException {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
			out.writeObject(value);
		}
		return bytes.toByteArray();
	}
}
