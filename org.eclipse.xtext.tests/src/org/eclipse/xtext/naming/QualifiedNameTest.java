/*******************************************************************************
 * Copyright (c) 2010, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.naming;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl.EObjectInputStream;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl.EObjectOutputStream;
import org.eclipse.xtext.naming.IQualifiedNameConverter.DefaultImpl;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class QualifiedNameTest extends Assert {
	@Test public void testBug354473() {
		DefaultImpl impl = new IQualifiedNameConverter.DefaultImpl();
		QualifiedName name = impl.toQualifiedName(".");
		ImportNormalizer normalizer = new ImportNormalizer(QualifiedName.create("Test"), true, false);
		assertNull(normalizer.resolve(name));
	}
	
	@Test public void testDeserializeAsLowerCase() throws IOException {
		QualifiedName upperCase = QualifiedName.create("A", "B");
		QualifiedName lowerCase = upperCase.toLowerCase();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EObjectOutputStream out = new BinaryResourceImpl.EObjectOutputStream(bos, Collections.emptyMap());
		upperCase.writeToStream(out);
		lowerCase.writeToStream(out);
		out.flush();
		
		EObjectInputStream in = new BinaryResourceImpl.EObjectInputStream(new ByteArrayInputStream(bos.toByteArray()), Collections.emptyMap());
		QualifiedName readUpperCase = QualifiedName.createFromStream(in);
		QualifiedName readLowerCase = QualifiedName.createFromStream(in);
		assertEquals(QualifiedName.class.getName(), readUpperCase.getClass().getName());
		assertEquals(QualifiedName.class.getName() + "$QualifiedNameLowerCase", readLowerCase.getClass().getName());
		assertEquals(upperCase, readUpperCase);
		assertEquals(lowerCase, readLowerCase);
	}

	@Test public void testCreateNull() {
		assertEquals(QualifiedName.EMPTY, QualifiedName.create());
		assertEquals(QualifiedName.EMPTY, QualifiedName.create(new String[0]));
		assertEquals(QualifiedName.EMPTY, QualifiedName.create((String[]) null));
		try {
			QualifiedName.create((String) null);
			fail("Exception expected");
		} catch(IllegalArgumentException e) {}
	}
	
	public void testAppendNull() {
		try {
			QualifiedName.create().append((String) null);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {}
	}

	@Test public void testSegments() {
		QualifiedName qn = QualifiedName.create("foo", "bar", "baz");
		assertEquals(3, qn.getSegmentCount());
		assertEquals("foo", qn.getSegment(0));
		assertEquals("bar", qn.getSegment(1));
		assertEquals("baz", qn.getSegment(2));
		assertEquals("foo", qn.getFirstSegment());
		assertEquals("baz", qn.getLastSegment());
	}

	@Test public void testStartsWith() {
		QualifiedName qn = QualifiedName.create("foo", "bar", "baz");
		QualifiedName qn1 = QualifiedName.create("foo", "bar");
		assertTrue(qn.startsWith(qn1));
		assertFalse(qn1.startsWith(qn));
		assertTrue(qn.startsWith(qn));

		assertTrue(qn.startsWithIgnoreCase(qn));
		QualifiedName qn2 = QualifiedName.create("FoO", "bAr");
		assertFalse(qn.startsWith(qn2));
		assertTrue(qn.startsWithIgnoreCase(qn2));
		assertFalse(qn2.startsWithIgnoreCase(qn));
		
		assertThrows(IllegalArgumentException.class, ()->qn1.startsWith(null));
	}
	
	@Test public void testSkip() throws Exception {
		QualifiedName qn = QualifiedName.create("foo", "bar", "baz");
		QualifiedName baz = qn.skipFirst(2);
		assertEquals(1, baz.getSegmentCount());
		assertEquals("baz", baz.getSegment(0));
		assertEquals(QualifiedName.EMPTY, qn.skipFirst(3));
		try {
			qn.skipFirst(-1);
			fail("Exception expected");
		} catch(IllegalArgumentException e) {}
		try {
			qn.skipFirst(4);
			fail("Exception expected");
		} catch(IllegalArgumentException e) {}
		
		QualifiedName foo = qn.skipLast(2);
		assertEquals(1, foo.getSegmentCount());
		assertEquals("foo", foo.getSegment(0));
		assertEquals(QualifiedName.EMPTY, qn.skipLast(3));
		try {
			qn.skipLast(-1);
			fail("Exception expected");
		} catch(IllegalArgumentException e) {}
		try {
			qn.skipLast(4);
			fail("Exception expected");
		} catch(IllegalArgumentException e) {}
	}

	@Test public void testAppend() {
		QualifiedName qn = QualifiedName.create("foo");
		QualifiedName fooBar = qn.append("bar");
		assertEquals(2, fooBar.getSegmentCount());
		assertEquals("foo", fooBar.getSegment(0));
		assertEquals("bar", fooBar.getSegment(1));

		QualifiedName fooBarFooBar = fooBar.append(fooBar);
		assertEquals(4, fooBarFooBar.getSegmentCount());
		assertEquals("foo", fooBarFooBar.getSegment(0));
		assertEquals("bar", fooBarFooBar.getSegment(1));
		assertEquals("foo", fooBarFooBar.getSegment(2));
		assertEquals("bar", fooBarFooBar.getSegment(3));
	}

	@Test public void testEquals() {
		QualifiedName qn = QualifiedName.create("foo", "bar");
		assertEquals(qn, qn);
		assertNotSame(qn, qn.skipFirst(1));
		assertNotSame(qn, qn.skipLast(1));
		assertNotSame(qn, qn.append("baz"));
		
		QualifiedName qn1 = QualifiedName.create("Foo", "BAR");
		assertNotSame(qn, qn1);
		assertTrue(qn.equalsIgnoreCase(qn1));
		assertTrue(qn1.equalsIgnoreCase(qn));
		assertTrue(qn.equalsIgnoreCase(qn));
	}
	
	
	@Test public void testCompare() throws Exception {
		QualifiedName a = QualifiedName.create("a");
		QualifiedName b = QualifiedName.create("b");
		assertTrue(a.compareTo(b) < 0);
		assertEquals(0, a.compareTo(a));
		assertTrue(b.compareTo(a) > 0);
		assertTrue(a.compareToIgnoreCase(b) < 0);
		assertEquals(0, a.compareToIgnoreCase(a));
		assertTrue(b.compareToIgnoreCase(a) > 0);
		
		QualifiedName aa = a.append("a");
		assertTrue(a.compareTo(aa) < 0);
		assertEquals(0, aa.compareTo(aa));
		assertTrue(aa.compareTo(a) > 0);
		assertTrue(a.compareToIgnoreCase(aa) < 0);
		assertEquals(0, aa.compareToIgnoreCase(aa));
		assertTrue(aa.compareToIgnoreCase(a) > 0);
		assertTrue(aa.compareTo(b) < 0);
		assertTrue(b.compareTo(aa) > 0);
		assertTrue(aa.compareToIgnoreCase(b) < 0);
		assertTrue(b.compareToIgnoreCase(aa) > 0);

		QualifiedName aA = a.append("A");
		assertTrue(aA.compareTo(aa) < 0);
		assertTrue(aa.compareTo(aA) > 0);
		assertEquals(0, aa.compareToIgnoreCase(aA));
	}
	
	@Test public void testToString() throws Exception {
		QualifiedName qn = QualifiedName.create("foo", "bar");
		assertEquals("foo.bar", qn.toString());
	}
	
	@Test public void testToCase() throws Exception {
		QualifiedName qn = QualifiedName.create("foo", "bar");
		QualifiedName qnUpper = qn.toUpperCase();
		assertFalse(qnUpper.equals(qn));
		assertTrue(qnUpper.equalsIgnoreCase(qn));
		assertEquals(qnUpper.toLowerCase(), qn);
		assertEquals(qn.toString().toUpperCase(), qnUpper.toString());
		assertEquals(qn.toString(), qnUpper.toLowerCase().toString());
	}
	
	@Test public void testEmpty() {
		assertEquals(0, QualifiedName.EMPTY.getSegmentCount());
		assertTrue(QualifiedName.EMPTY.getSegments().isEmpty());
		try {
			 QualifiedName.EMPTY.getFirstSegment();
			 fail("Exception expected");
		} catch(IndexOutOfBoundsException e) {}
		assertEquals("", QualifiedName.EMPTY.toString());
		QualifiedName foo = QualifiedName.EMPTY.append("foo");
		assertEquals(1, foo.getSegmentCount());
		assertEquals("foo", foo.getFirstSegment());
		assertEquals("foo", foo.getLastSegment());
	}
	
	@Test public void testWrapper() throws Exception {
		Function<String, QualifiedName> wrapper = QualifiedName.wrapper(from -> from);
		assertEquals(QualifiedName.create(""), wrapper.apply(""));
		assertEquals(null, wrapper.apply(null));
		assertEquals("foo", wrapper.apply("foo").getLastSegment());
	}
}
