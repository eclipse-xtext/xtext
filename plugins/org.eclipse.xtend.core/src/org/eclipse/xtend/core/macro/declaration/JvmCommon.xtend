/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.xtend.core.macro.declaration

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.macro.declaration.NamedElement
import org.eclipse.xtend.lib.macro.declaration.PrimitiveType
import org.eclipse.xtend.lib.macro.declaration.Type
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclarator
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.VoidType
import org.eclipse.xtext.common.types.JvmExecutable
import org.eclipse.xtext.common.types.JvmPrimitiveType
import org.eclipse.xtext.common.types.JvmTypeParameter
import org.eclipse.xtext.common.types.JvmUpperBound
import org.eclipse.xtext.common.types.JvmVoid
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference
import org.eclipse.xtext.xtype.impl.XComputedTypeReferenceImplCustom
import org.eclipse.xtext.xbase.typesystem.^override.IResolvedOperation
import org.eclipse.xtend.lib.macro.declaration.ResolvedMethod
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.ResolvedParameter
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.ResolvedConstructor
import org.eclipse.xtend.lib.macro.declaration.ConstructorDeclaration
import org.eclipse.xtext.xbase.typesystem.^override.IResolvedExecutable
import org.eclipse.xtend.lib.macro.declaration.ResolvedExecutable
import org.eclipse.xtend.lib.macro.declaration.ExecutableDeclaration
import org.eclipse.xtext.xbase.typesystem.^override.IResolvedConstructor

abstract class AbstractElementImpl<T> {
	@Property T delegate
	@Property CompilationUnitImpl compilationUnit
}

abstract class AbstractNamedElementImpl<T extends EObject> extends AbstractElementImpl<T> implements NamedElement {
	
	override toString() {
		class.name+"["+simpleName+"]"
	}
	
}

@Data
class ResolvedParameterImpl implements ResolvedParameter {
	ParameterDeclaration declaration
	TypeReference resolvedType
	
	override toString() {
		'''�resolvedType� �declaration.simpleName�'''
	}
}

class ResolvedExecutableImpl<T extends IResolvedExecutable, D extends ExecutableDeclaration> extends AbstractElementImpl<T> implements ResolvedExecutable {
	
	Iterable<? extends ResolvedParameter> resolvedParameters 
	
	override getResolvedParameters() {
		if (resolvedParameters === null) {
			resolvedParameters = (0 ..< delegate.declaration.parameters.size).map[i|
				new ResolvedParameterImpl(
					compilationUnit.toParameterDeclaration(delegate.declaration.parameters.get(i)), 
					compilationUnit.toTypeReference(delegate.resolvedParameterTypes.get(i))
				)
			]
			
		}
		resolvedParameters
	}
	
	override getResolvedExceptions() {
		delegate.resolvedExceptions.map[compilationUnit.toTypeReference(it)]
	}
	
	override D getDeclaration() {
		compilationUnit.toMemberDeclaration(delegate.declaration) as D
	}
	
	override toString() {
		delegate.toString
	}
}

class ResolvedMethodImpl extends ResolvedExecutableImpl<IResolvedOperation, MethodDeclaration> implements ResolvedMethod {
	override getResolvedReturnType() {
		compilationUnit.toTypeReference(delegate.resolvedReturnType)
	}
	
	override getResolvedTypeParameters() {
		delegate.resolvedTypeParameters.map[compilationUnit.toTypeParameterDeclaration(it)]
	}
}

class ResolvedConstructorImpl extends ResolvedExecutableImpl<IResolvedConstructor, ConstructorDeclaration> implements ResolvedConstructor {
}

class TypeReferenceImpl extends AbstractElementImpl<LightweightTypeReference> implements TypeReference {

	override getName() {
		delegate.javaIdentifier
	}
	
	override getSimpleName() {
		delegate.simpleName
	}
	
	override getType() {
		switch it: delegate {
			case unknown : new UnknownType(compilationUnit, compilationUnit.packageName + '.' + simpleName)
			default : compilationUnit.toType(type)
		}
	}
	override toString() {
		return delegate.toString
	}

	override getActualTypeArguments() {
		delegate.typeArguments.map[compilationUnit.toTypeReference(it)]
	}
	
	override getArrayComponentType() {
		val componentType = delegate.componentType
		if (componentType == null)
			return null
		return compilationUnit.toTypeReference(componentType)
	}
	
	override getLowerBound() {
		compilationUnit.toTypeReference(delegate.lowerBoundSubstitute)
	}
	
	override getPrimitiveIfWrapper() {
		if (!wrapper)
			return this
		compilationUnit.toTypeReference(delegate.primitiveIfWrapperType)
	}
	
	override getUpperBound() {
		compilationUnit.toTypeReference(delegate.upperBoundSubstitute)
	}
	
	override getWrapperIfPrimitive() {
		compilationUnit.toTypeReference(delegate.wrapperTypeIfPrimitive)
	}
	
	override isAnyType() {
		delegate.any
	}
	
	override isArray() {
		delegate.array
	}
	
	override isAssignableFrom(TypeReference typeReference) {
		if (typeReference.isInferred) {
			throw new UnsupportedOperationException("Cannot check assignability with an inferred type reference.")
		}
		delegate.isAssignableFrom((typeReference as TypeReferenceImpl).delegate)
	}
	
	override isPrimitive() {
		delegate.primitive
	}
	
	override isVoid() {
		delegate.primitiveVoid
	}
	
	override isWildCard() {
		delegate.wildcard
	}
	
	override isWrapper() {
		delegate.wrapper
	}
	
	def LightweightTypeReference getLightWeightTypeReference() {
		delegate
	}
	
	override getSuperTypes() {
		delegate.superTypes.map[compilationUnit.toTypeReference(it)]
	}
	
	override getDeclaredResolvedMethods() {
		compilationUnit.overrideHelper.getResolvedOperations(delegate)
			.declaredOperations.map[compilationUnit.toResolvedMethod(it)]
	}
	
	override getDeclaredResolvedConstructors() {
		compilationUnit.overrideHelper.getResolvedOperations(delegate)
			.declaredConstructors.map[compilationUnit.toResolvedConstructor(it)]
	}
	
	override equals(Object obj) {
		if (obj === this)
			return true
		if (obj instanceof TypeReference) {
			// TODO this logic is flawed since rawtype / generic types are assignable in both directions
			return obj.isAssignableFrom(this) && this.isAssignableFrom(obj)
		}
		return false
	}
	
	override hashCode() {
		// TODO type may be null, e.g. for wildcards or unknown types
		delegate.type.qualifiedName.hashCode
	}
	
	override isInferred() {
		return false
	}
	
}

class InferredTypeReferenceImpl extends AbstractElementImpl<XComputedTypeReferenceImplCustom> implements TypeReference {
	TypeReference equivalent
	@Getter LightweightTypeReference lightweightTypeReference
	
	@Delegate
	private def TypeReference getEquivalent(String methodName) {
		if (getEquivalent == null) {
			throw new UnsupportedOperationException("Cannot call method '"+methodName+"()' on a inferred type reference before the compilation phase. Check isInferred() before calling any methods.")
		}
		getEquivalent
	}
	
	def TypeReference getEquivalent() {
		if (equivalent == null) {
			if (delegate.equivalentComputed) {
				lightweightTypeReference = compilationUnit.typeRefConverter.toLightweightReference(delegate)
				equivalent = 	compilationUnit.toTypeReference(lightweightTypeReference)
			}
		}
		equivalent
	}
	
	override isInferred() {
		true
	}
	
	override toString() {
		delegate.toString
	}
	
}

// types

class VoidTypeImpl extends AbstractElementImpl<JvmVoid> implements VoidType {
	
	override getSimpleName() {
		'void'
	}
	
	override isAssignableFrom(Type otherType) {
		if (otherType == null)
			return false;
		val thisTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(this)
		val thatTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(otherType)
		return thisTypeRef.isAssignableFrom(thatTypeRef);
	}
	
	override getQualifiedName() {
		simpleName
	}
	
} 

class PrimitiveTypeImpl extends AbstractElementImpl<JvmPrimitiveType> implements PrimitiveType {

	override getKind() {
		switch simpleName {
			case 'boolean' : PrimitiveType.Kind.BOOLEAN
			case 'int' : PrimitiveType.Kind.INT
			case 'char' : PrimitiveType.Kind.CHAR
			case 'double' : PrimitiveType.Kind.DOUBLE
			case 'long' : PrimitiveType.Kind.LONG
			case 'short' : PrimitiveType.Kind.SHORT
			case 'float' : PrimitiveType.Kind.FLOAT
			case 'byte' : PrimitiveType.Kind.BYTE
		}
	}
	
	override getSimpleName() {
		delegate.identifier
	}
	
	override isAssignableFrom(Type otherType) {
		if (otherType == null)
			return false;
		val thisTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(this)
		val thatTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(otherType)
		return thisTypeRef.isAssignableFrom(thatTypeRef);
	}
	
	override getQualifiedName() {
		simpleName
	}
	
}

class TypeParameterDeclarationImpl extends AbstractElementImpl<JvmTypeParameter> implements TypeParameterDeclaration {
	
	override getUpperBounds() {
		delegate.constraints.filter(JvmUpperBound).map[compilationUnit.toTypeReference(typeReference)].toList
	}
	
	override getSimpleName() {
		delegate.name
	}
	
	override getQualifiedName() {
		delegate.identifier
	}
	
	override getTypeParameterDeclarator() {
		compilationUnit.toMemberDeclaration(delegate.eContainer as JvmExecutable) as TypeParameterDeclarator
	}
	
	override getAnnotations() {
		emptyList
	}
	
	override findAnnotation(Type annotationType) {
		null
	}
	
	override isAssignableFrom(Type otherType) {
		if (otherType == null)
			return false;
		val thisTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(this)
		val thatTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(otherType)
		return thisTypeRef.isAssignableFrom(thatTypeRef);
	}
	
}
