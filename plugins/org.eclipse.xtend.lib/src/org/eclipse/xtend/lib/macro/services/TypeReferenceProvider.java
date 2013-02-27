/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.lib.macro.services;

import java.util.List;
import java.util.Set;

import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;

import com.google.common.annotations.Beta;

/**
 * Provides and creates {@link TypeReference} instances.
 * 
 * @author Sven Efftinge
 */
@Beta
public interface TypeReferenceProvider {
	/**
	 * Creates a new array type reference using the given component type.
	 * If the component type is itself an array this method adds a dimension.
	 * 
	 * @param componentType the component type of the array type reference
	 * @return an array type reference
	 */
	TypeReference newArrayTypeReference(TypeReference componentType);
	
	/**
	 * Creates a new type reference for the given name and the given type arguments.
	 * 
	 * @param typeName the name of the type to point to
	 * @param typeArguments the type arguments
	 * @return a {@link TypeReference} pointing to the type with given name, or <code>null</code> if no such type could be found.
	 */
	TypeReference newTypeReference(String typeName, TypeReference... typeArguments);
	
	/**
	 * Creates a new type reference for the given typeDeclaration and the type arguments.
	 * 
	 * @param typeDeclaration the type to point to
	 * @param typeArguments the type arguments
	 * @return a {@link TypeReference} pointing to the given type.
	 */
	TypeReference newTypeReference(Type typeDeclaration, TypeReference... typeArguments);
	
	/**
	 * @return a wildcard type references with upperBound {@link Object}
	 */
	TypeReference newWildcardTypeReference();
	 /**
	  * @param upperBound the upper bound of the wildcard type
	  * @return a wildcard type references with the given upperBound.
	  */
	TypeReference newWildcardTypeReference(TypeReference upperBound);
	
	/**
	 * @return a type reference pointing to {@link Object}
	 */
	TypeReference getObject();
	
	/**
	 * @return a type reference pointing to {@link String}
	 */
	TypeReference getString();
	
	/**
	 * @param param the type argument for the type reference
	 * @return a type reference for {@link List} with the given type argument.
	 */
	TypeReference getList(TypeReference param);
	
	/**
	 * @param param the type argument for the type reference
	 * @return a type reference for {@link Set} with the given type argument.
	 */
	TypeReference getSet(TypeReference param);
	
	/**
	 * The any type is the type of the null pointer and has no concrete
	 * representation in Java. It is assignable to every reference type.
	 * 
	 * @return a type reference representing the "any type".
	 */
	TypeReference getAnyType();
	
	/**
	 * @return a type reference representing 'void'
	 */
	TypeReference getPrimitiveVoid();
	
	/**
	 * @return a type reference representing 'boolean'
	 */
	TypeReference getPrimitiveBoolean();
	
	/**
	 * @return a type reference representing 'short'
	 */
	TypeReference getPrimitiveShort();
	
	/**
	 * @return a type reference representing 'int'
	 */
	TypeReference getPrimitiveInt();
	
	/**
	 * @return a type reference representing 'long'
	 */
	TypeReference getPrimitiveLong();
	
	/**
	 * @return a type reference representing 'float'
	 */
	TypeReference getPrimitiveFloat();
	
	/**
	 * @return a type reference representing 'double'
	 */
	TypeReference getPrimitiveDouble();
	
	/**
	 * @return a type reference representing 'char'
	 */
	TypeReference getPrimitiveChar();
	
	/**
	 * @return a type reference representing 'byte'
	 */
	TypeReference getPrimitiveByte();
}