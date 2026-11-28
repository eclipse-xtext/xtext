/**
 * Copyright (c) 2014, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.resource.persistence;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;

/**
 * @since 2.8
 */
public class SerializationExtensions {

	@SuppressWarnings("unchecked")
	public static <T extends ENamedElement> T readEcoreElement(ObjectInput in) throws IOException {
		URI uri = readURI(in);
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(uri.trimFragment().toString());
		if (ePackage == null)
			return null;
		Resource eResource = ePackage.eResource();
		if (eResource == null)
			return null;
		return (T) eResource.getEObject(uri.fragment());
	}

	public static void writeEcoreElement(ObjectOutput out, ENamedElement namedElement) throws IOException {
		writeURI(out, EcoreUtil.getURI(namedElement));
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> T readCastedObject(ObjectInput in) throws IOException {
		try {
			return ((T) in.readObject());
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public static URI readURI(ObjectInput in) throws IOException {
		String stringRep = in.readUTF();
		if ("NULL".equals(stringRep))
			return null;
		return URI.createURI(stringRep);
	}

	public static void writeURI(ObjectOutput out, URI uri) throws IOException {
		if (uri == null)
			out.writeUTF("NULL");
		else
			out.writeUTF(uri.toString());
	}

	@SuppressWarnings("unchecked")
	public static QualifiedName readQualifiedName(ObjectInput in) throws IOException {
		try {
			return QualifiedName.create((List<String>) in.readObject());
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public static void writeQualifiedName(ObjectOutput out, QualifiedName name) throws IOException {
		out.writeObject(new ArrayList<String>(name.getSegments()));
	}
}
