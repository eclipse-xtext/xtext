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

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @since 2.5
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
class BinaryErasureTypeSignature extends BinaryGenericTypeSignature {

	BinaryErasureTypeSignature(BinaryGenericTypeSignature genericSignature, int arrayDimensions) {
		super(genericSignature.chars, genericSignature.offset + arrayDimensions, genericSignature.length - arrayDimensions);
		this.arrayDimensions = arrayDimensions;
	}
	
	@Override
	protected void toIdentifier(StringBuilder builder, int fixup) {
		BinaryGenericTypeSignature componentType = getArrayComponentType();
		if (componentType != this) {
			componentType.toIdentifier(builder, fixup);
			int dim = getArrayDimensions();
			for(int i = 0; i < dim; i++) {
				builder.append("[]");
			}
		} else {
			int depth = 0;
			boolean wasGenericEnd = false;
			for(int i = offset + fixup, max = offset + length - fixup; i < max; i++) {
				char c = chars[i];
				if (c == Signature.C_GENERIC_START) {
					depth++;
				} else if (c == Signature.C_GENERIC_END) {
					depth--;
					wasGenericEnd = true;
				} else if (depth == 0) {
					if (c == '/') {
						builder.append('.');
					} else if (c == '.' && wasGenericEnd) {
						builder.append('$');
					} else {
						builder.append(c);
					}	
				}
			}
		}
	}
	
	@Override
	public BinaryGenericTypeSignature getArrayComponentType() {
		return new BinaryErasureTypeSignature(this, 0) {
			@Override
			public BinaryGenericTypeSignature getArrayComponentType() {
				return this;
			}
		};
	}
	
	@Override
	public BinaryGenericTypeSignature getTypeErasure() {
		return this;
	}
	
	@Override
	public List<BinaryTypeArgumentSignature> getTypeArguments() {
		return Collections.emptyList();
	}

}
