/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.caliper.tests.clazz;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @since 2.5
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
class BinaryMethodSignature extends AbstractBinarySignature {

	BinaryMethodSignature(char[] signature) {
		super(signature);
	}

	public BinaryGenericTypeSignature getReturnType() {
		int paren = CharOperation.lastIndexOf(Signature.C_PARAM_END, chars);
		if (paren == -1) {
			throw new IllegalArgumentException();
		}
		// there could be thrown exceptions behind, thus scan one type exactly
		int last = JdtCompilerUtil.scanTypeSignature(chars, paren+1);
		return new BinaryGenericTypeSignature(chars, paren + 1, last - paren);
	}
	
	public List<BinaryGenericTypeSignature> getExceptionTypes() {
		// skip type parameters
		int exceptionStart = CharOperation.indexOf(Signature.C_EXCEPTION_START, chars, offset);
		if (exceptionStart == -1) {
			int paren = CharOperation.lastIndexOf(Signature.C_PARAM_END, chars, offset);
			if (paren == -1) {
				throw new IllegalArgumentException();
			}
			// ignore return type
			exceptionStart = JdtCompilerUtil.scanTypeSignature(chars, paren+1) + 1;
			int length = offset + this.length;
			if (exceptionStart == length) return Collections.emptyList();
		}
		
		int length = offset + this.length;
		int i = exceptionStart;
		List<BinaryGenericTypeSignature> result = Lists.newArrayListWithCapacity(2);
		while (i < length) {
			if (chars[i] == Signature.C_EXCEPTION_START) {
				exceptionStart++;
				i++;
			} else {
				throw new IllegalArgumentException();
			}
			i = JdtCompilerUtil.scanTypeSignature(chars, i) + 1;
			result.add(new BinaryGenericTypeSignature(chars, exceptionStart, i - exceptionStart));
			exceptionStart = i;
		}
		return result;
	}

	public List<BinaryGenericTypeSignature> getParameterTypes() {
		try {
			int i = CharOperation.indexOf(Signature.C_PARAM_START, chars, offset);
			if (i < 0) {
				throw new IllegalArgumentException();
			} else {
				i++;
			}
			if (chars[i] == Signature.C_PARAM_END) {
				return Collections.emptyList();
			}
			List<BinaryGenericTypeSignature> result = Lists.newArrayListWithExpectedSize(3);
			for (;;) {
				if (chars[i] == Signature.C_PARAM_END) {
					return result;
				}
				int e = JdtCompilerUtil.scanTypeSignature(chars, i);
				if (e < 0) {
					throw new IllegalArgumentException();
				}
				result.add(new BinaryGenericTypeSignature(chars, i, e - i + 1));
				i = e + 1;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException();
		}
	}
	
	@Override
	public List<BinaryTypeParameter> getTypeParameters() {
		return doGetTypeParameters();
	}

}
