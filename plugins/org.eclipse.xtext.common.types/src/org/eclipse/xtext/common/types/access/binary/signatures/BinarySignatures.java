/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.binary.signatures;

/**
 * Facade to obtain instances of binary signatures from char arrays.
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 * @since 2.5
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BinarySignatures {

	/**
	 * @param signature the binary type name, e.g. {@code java/lang/String} or a binary signature
	 *  e.g {@code Ljava/lang/String;}
	 */
	public static BinaryTypeSignature createTypeSignature(char[] signature) {
		if (signature[signature.length - 1] != ';')
			return new BinaryTypeSignature(signature);
		return new BinaryGenericTypeSignature(signature);
	}

	public static BinaryMethodSignature createMethodSignature(char[] signature) {
		return new BinaryMethodSignature(signature);
	}

	public static BinarySuperTypeSignature createSuperTypeSignature(char[] signature) {
		return new BinarySuperTypeSignature(signature);
	}
	
}
