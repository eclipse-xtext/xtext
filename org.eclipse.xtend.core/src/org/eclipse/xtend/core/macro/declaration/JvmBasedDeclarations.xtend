/*******************************************************************************
 * Copyright (c) 2013, 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.macro.declaration

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtend.core.macro.ActiveAnnotationContexts.AnnotationCallback
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference
import org.eclipse.xtend.lib.macro.declaration.AnnotationTarget
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeElementDeclaration
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy
import org.eclipse.xtend.lib.macro.declaration.ConstructorDeclaration
import org.eclipse.xtend.lib.macro.declaration.EnumerationTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration
import org.eclipse.xtend.lib.macro.declaration.ExecutableDeclaration
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.InterfaceDeclaration
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTarget
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTypeElementDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableConstructorDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableEnumerationTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableEnumerationValueDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableExecutableDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclarator
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.Type
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclarator
import org.eclipse.xtend.lib.macro.declaration.TypeReference
import org.eclipse.xtend.lib.macro.declaration.Visibility
import org.eclipse.xtend.lib.macro.expression.Expression
import org.eclipse.xtend2.lib.StringConcatenationClient
import org.eclipse.xtext.common.types.JvmAnnotationTarget
import org.eclipse.xtext.common.types.JvmAnnotationType
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmEnumerationLiteral
import org.eclipse.xtext.common.types.JvmEnumerationType
import org.eclipse.xtext.common.types.JvmExecutable
import org.eclipse.xtext.common.types.JvmField
import org.eclipse.xtext.common.types.JvmFormalParameter
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmIdentifiableElement
import org.eclipse.xtext.common.types.JvmMember
import org.eclipse.xtext.common.types.JvmOperation
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.TypesFactory
import org.eclipse.xtext.common.types.impl.JvmMemberImplCustom
import org.eclipse.xtext.common.types.util.DeprecationUtil
import org.eclipse.xtext.xbase.compiler.DocumentationAdapter
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

import static org.eclipse.xtend.core.macro.ConditionUtils.*

import static extension org.eclipse.xtext.EcoreUtil2.*
import org.eclipse.xtext.util.Strings

abstract class JvmElementImpl<T extends EObject> extends AbstractElementImpl<T> {
	
	def remove() {
		checkMutable
		Preconditions.checkState(delegate.eResource !== null, '''This element has already been removed: «delegate»''')
		
		compilationUnit.jvmModelAssociator.removeAllAssociation(delegate)
		EcoreUtil.remove(delegate)
		
		Preconditions.checkState(delegate.eResource === null, '''Couldn't remove: «delegate»''')
	}
	
	protected final def checkMutable() {
		if (compilationUnit.lastPhase != AnnotationCallback.INFERENCE) 
			throw new IllegalStateException("Element cannot be modified outside the transformation phase")
	}
	
}

abstract class JvmNamedElementImpl<T extends JvmIdentifiableElement> extends JvmElementImpl<T> {
	
	def getSimpleName() {
		delegate.simpleName
	}
	
	override toString() {
		class.name+"["+simpleName+"]"
	}
	
}

abstract class JvmAnnotationTargetImpl<T extends JvmAnnotationTarget> extends JvmNamedElementImpl<T> {

	def Iterable<? extends AnnotationReference> getAnnotations() {
		ImmutableList.copyOf(delegate.annotations.map[compilationUnit.toAnnotationReference(it)])
	}
	
	def addAnnotation(AnnotationReference annotationReference) {
		checkMutable
		Preconditions.checkArgument(annotationReference !== null, "annotationReference cannot be null")
		if (annotationReference instanceof JvmAnnotationReferenceImpl) {
			val jvmAnnotationReference = annotationReference.delegate.cloneWithProxies
			this.delegate.annotations += jvmAnnotationReference
			compilationUnit.toAnnotationReference(jvmAnnotationReference)
		} else {
			throw new IllegalArgumentException('''«annotationReference» is not annotation reference''')
		}
	}
	
	def boolean removeAnnotation(AnnotationReference annotationReference) {
		checkMutable
		if (annotationReference instanceof JvmAnnotationReferenceImpl) {
			return this.delegate.annotations.remove(annotationReference.delegate)
		}
		return false
	}
	
	def findAnnotation(Type annotationType) {
		annotations.findFirst[annotationTypeDeclaration == annotationType]
	}

}

abstract class JvmMemberDeclarationImpl<T extends JvmMember> extends JvmAnnotationTargetImpl<T> implements MemberDeclaration {
	
	override getDocComment() {
		val adapter = EcoreUtil.getAdapter(delegate.eAdapters(), DocumentationAdapter) as DocumentationAdapter;
		return adapter?.getDocumentation();
	}
	
	def setDocComment(String docComment) {
		checkMutable
		var adapter = EcoreUtil.getAdapter(delegate.eAdapters(), DocumentationAdapter) as DocumentationAdapter;
		if (adapter === null) {
			adapter = new DocumentationAdapter
			delegate.eAdapters += adapter
		}
		adapter.setDocumentation(docComment)
	}
	
	override getVisibility() {
		compilationUnit.toVisibility(delegate.visibility)
	}
	
	def setVisibility(Visibility visibility) {
		checkMutable
		delegate.visibility = switch visibility {
			case Visibility.DEFAULT : JvmVisibility.DEFAULT
			case Visibility.PUBLIC : JvmVisibility.PUBLIC
			case Visibility.PRIVATE : JvmVisibility.PRIVATE
			case Visibility.PROTECTED : JvmVisibility.PROTECTED
		}
	}
	
	override getDeclaringType() {
		compilationUnit.toTypeDeclaration(delegate.declaringType)
	}
	
	def setSimpleName(String name) {
		checkMutable
		checkJavaIdentifier(name, "name");
		switch (it: delegate) {
			JvmMemberImplCustom : clearIdentifierCache
		}
		delegate.simpleName = name
	}
	
	override getModifiers() {
		#{}
	}
	
	override isDeprecated() {
		return DeprecationUtil.isDeprecatedMember(delegate)
	}
	
	def void setDeprecated(boolean deprecated) {
		checkMutable
		if (deprecated) {
			delegate.deprecated = true
			addAnnotation(compilationUnit.annotationReferenceProvider.newAnnotationReference(Deprecated))
		} else {
			delegate.deprecated = false
			val existingReference = findAnnotation(compilationUnit.typeLookup.findTypeGlobally(Deprecated))
			if (existingReference !== null) {
				removeAnnotation(existingReference)
			}
		}
	}
	
}

abstract class JvmTypeDeclarationImpl<T extends JvmDeclaredType> extends JvmMemberDeclarationImpl<T> {
	
	def Iterable<? extends MemberDeclaration> getDeclaredMembers() {
		ImmutableList.copyOf(delegate.members.map[compilationUnit.toMemberDeclaration(it)])
	}
	
	override getSimpleName() {
		delegate.simpleName
	}
	
	def getQualifiedName() {
		delegate.getQualifiedName('.')
	}
	
	def isAssignableFrom(Type otherType) {
		if (otherType === null)
			return false;
		val thisTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(this as Type)
		val thatTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(otherType)
		return thisTypeRef.isAssignableFrom(thatTypeRef);
	}
	
	def addConstructor(Procedure1<MutableConstructorDeclaration> initializer) {
		checkMutable
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		// remove default constructor
		val constructor = delegate.members.filter(JvmConstructor).findFirst[compilationUnit.typeExtensions.isSingleSyntheticDefaultConstructor(it)]
		if (constructor !== null) {
			val mutableConstructorDeclaration = compilationUnit.toMemberDeclaration(constructor) as MutableConstructorDeclaration
			mutableConstructorDeclaration.remove
		}
		val newConstructor = TypesFactory.eINSTANCE.createJvmConstructor
		newConstructor.visibility = JvmVisibility.PUBLIC
		newConstructor.simpleName = simpleName
		delegate.members.add(newConstructor)
		val mutableConstructorDeclaration = compilationUnit.toMemberDeclaration(newConstructor) as MutableConstructorDeclaration
		initializer.apply(mutableConstructorDeclaration)
		return mutableConstructorDeclaration
	}
	
	def addField(String name, Procedure1<MutableFieldDeclaration> initializer) {
		checkMutable
		checkJavaIdentifier(name, "name")
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		val newField = TypesFactory.eINSTANCE.createJvmField
		newField.simpleName = name
		newField.visibility = JvmVisibility.PRIVATE
		delegate.members.add(newField)
		val mutableFieldDeclaration = compilationUnit.toMemberDeclaration(newField) as MutableFieldDeclaration
		initializer.apply(mutableFieldDeclaration)
		return mutableFieldDeclaration
	}
	
	def addMethod(String name, Procedure1<MutableMethodDeclaration> initializer) {
		checkMutable
		checkJavaIdentifier(name, "name")
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		val newMethod = TypesFactory.eINSTANCE.createJvmOperation
		newMethod.visibility = JvmVisibility.PUBLIC
		newMethod.simpleName = name
		newMethod.returnType = compilationUnit.toJvmTypeReference(compilationUnit.typeReferenceProvider.primitiveVoid)
		delegate.members.add(newMethod)
		val mutableMethodDeclaration = compilationUnit.toMemberDeclaration(newMethod) as MutableMethodDeclaration
		initializer.apply(mutableMethodDeclaration)
		return mutableMethodDeclaration
	}
	
	def findDeclaredConstructor(TypeReference... parameterTypes) {
		checkIterable(parameterTypes, "parameterTypes")
		declaredConstructors.findFirst[constructor | constructor.parameters.map[type].toList == parameterTypes.toList]
	}
	
	def findDeclaredField(String name) {
		declaredFields.findFirst[field | field.simpleName == name]
	}
	
	def findDeclaredType(String name) {
		declaredTypes.findFirst[type | type.simpleName == name]
	}
	
	def findDeclaredMethod(String name, TypeReference... parameterTypes) {
		checkIterable(parameterTypes, "parameterTypes")
		declaredMethods.findFirst[method | method.simpleName == name && method.parameters.map[type].toList == parameterTypes.toList]
	}
	
	def Iterable<? extends MethodDeclaration> getDeclaredMethods() {
		declaredMembers.filter(MethodDeclaration)
	}
	
	def Iterable<? extends FieldDeclaration> getDeclaredFields() {
		declaredMembers.filter(FieldDeclaration)
	}
	
	def Iterable<? extends ClassDeclaration> getDeclaredClasses() {
		declaredMembers.filter(ClassDeclaration)
	}
	
	def Iterable<? extends ConstructorDeclaration> getDeclaredConstructors() {
		declaredMembers.filter(ConstructorDeclaration)
	}
	
	def Iterable<? extends InterfaceDeclaration> getDeclaredInterfaces() {
		declaredMembers.filter(InterfaceDeclaration)
	}
	
	def Iterable<? extends TypeDeclaration> getDeclaredTypes() {
		declaredMembers.filter(TypeDeclaration)
	}
	
	def Iterable<? extends AnnotationTypeDeclaration> getDeclaredAnnotationTypes() {
		declaredMembers.filter(AnnotationTypeDeclaration)
	}
	
	def Iterable<? extends EnumerationTypeDeclaration> getDeclaredEnumerationTypes() {
		declaredMembers.filter(EnumerationTypeDeclaration)
	}
	
	override setSimpleName(String name) {
		throw new UnsupportedOperationException("The type cannot be renamed.")
	}
	
	override remove() {
		throw new UnsupportedOperationException("The type cannot be removed.")
	}
	
}

class MutableJvmInterfaceDeclarationImpl extends JvmInterfaceDeclarationImpl implements MutableInterfaceDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableMethodDeclaration findDeclaredMethod(String name, TypeReference... parameterTypes) {
		super.findDeclaredMethod(name, parameterTypes) as MutableMethodDeclaration
	}
	
	override MutableFieldDeclaration findDeclaredField(String name) {
		super.findDeclaredField(name) as MutableFieldDeclaration
	}
	
	override MutableTypeDeclaration findDeclaredType(String name) {
		super.findDeclaredType(name) as MutableTypeDeclaration
	}
	
	override MutableConstructorDeclaration findDeclaredConstructor(TypeReference... parameterTypes) {
		super.findDeclaredConstructor(parameterTypes) as MutableConstructorDeclaration
	}
	
	override Iterable<? extends MutableMethodDeclaration> getDeclaredMethods() {
		super.declaredMethods as Iterable<? extends MutableMethodDeclaration>
	}
	
	override Iterable<? extends MutableFieldDeclaration> getDeclaredFields() {
		super.declaredFields as Iterable<? extends MutableFieldDeclaration>
	}
	
	override Iterable<? extends MutableClassDeclaration> getDeclaredClasses() {
		super.declaredClasses as Iterable<? extends MutableClassDeclaration>
	}
	
	override Iterable<? extends MutableConstructorDeclaration> getDeclaredConstructors() {
		super.declaredConstructors as Iterable<? extends MutableConstructorDeclaration>
	}
	
	override Iterable<? extends MutableInterfaceDeclaration> getDeclaredInterfaces() {
		super.declaredInterfaces as Iterable<? extends MutableInterfaceDeclaration>
	}
	
	override Iterable<? extends MutableAnnotationTypeDeclaration> getDeclaredAnnotationTypes() {
		super.declaredAnnotationTypes as Iterable<? extends MutableAnnotationTypeDeclaration>
	}
	
	override Iterable<? extends MutableEnumerationTypeDeclaration> getDeclaredEnumerationTypes() {
		super.declaredEnumerationTypes as Iterable<? extends MutableEnumerationTypeDeclaration>
	}
	
	override Iterable<? extends MutableTypeDeclaration> getDeclaredTypes() {
		super.declaredTypes as Iterable<? extends MutableTypeDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override Iterable<? extends MutableMemberDeclaration> getDeclaredMembers() {
		super.declaredMembers as Iterable<? extends MutableMemberDeclaration>
	}
	
	override Iterable<? extends MutableTypeParameterDeclaration> getTypeParameters() {
		super.typeParameters as Iterable<? extends MutableTypeParameterDeclaration>
	}
	
	override setExtendedInterfaces(Iterable<? extends TypeReference> superinterfaces) {
		checkMutable
		checkIterable(superinterfaces, "superinterfaces")
		delegate.superTypes.clear
		for (typeRef : superinterfaces) {
			if (typeRef.inferred)
				throw new IllegalArgumentException("Cannot use inferred type as extended interface.")
			delegate.superTypes += compilationUnit.toJvmTypeReference(typeRef)
		}
	}
	
	override setStrictFloatingPoint(boolean isStrictFloatingPoint) {
		checkMutable
		delegate.setStrictFloatingPoint(isStrictFloatingPoint)
	}
	
	override addTypeParameter(String name, TypeReference... upperBounds) {
		checkMutable
		checkJavaIdentifier(name, "name");
		checkIterable(upperBounds, "upperBounds")
		checkInferredTypeReferences("parameter type", upperBounds)
		val param = TypesFactory.eINSTANCE.createJvmTypeParameter
		param.name = name
		delegate.typeParameters.add(param)
		for (upper : upperBounds) {
			val typeRef = compilationUnit.toJvmTypeReference(upper)
			val jvmUpperBound = TypesFactory.eINSTANCE.createJvmUpperBound
			jvmUpperBound.setTypeReference(typeRef)
			param.constraints.add(jvmUpperBound)
		}
		return compilationUnit.toTypeParameterDeclaration(param) as MutableTypeParameterDeclaration
	}
	
}

class JvmInterfaceDeclarationImpl extends JvmTypeDeclarationImpl<JvmGenericType> implements InterfaceDeclaration {
	
	override getExtendedInterfaces() {
		val filtered = delegate.superTypes.filter[(it.type as JvmGenericType).interface]
		filtered.map[compilationUnit.toTypeReference(it)].toList
	}
	
	override getTypeParameters() {
		delegate.typeParameters.map[compilationUnit.toTypeParameterDeclaration(it)]
	}
	
	override isStrictFloatingPoint() {
		delegate.isStrictFloatingPoint	
	}
	
	override addMethod(String name, Procedure1<MutableMethodDeclaration> initializer) {
		checkMutable
		checkJavaIdentifier(name, "name")
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		val newMethod = TypesFactory.eINSTANCE.createJvmOperation
		newMethod.visibility = JvmVisibility.PUBLIC
		newMethod.simpleName = name
		newMethod.returnType = compilationUnit.toJvmTypeReference(compilationUnit.typeReferenceProvider.primitiveVoid)
		newMethod.setAbstract(true)
		delegate.members.add(newMethod)
		val mutableMethodDeclaration = compilationUnit.toMemberDeclaration(newMethod) as MutableMethodDeclaration
		initializer.apply(mutableMethodDeclaration)
		return mutableMethodDeclaration
	}
	
	override addConstructor(Procedure1<MutableConstructorDeclaration> initializer) {
		throw new UnsupportedOperationException("The interface '"+simpleName+"' cannot declare any constructors.")
	}

}

class MutableJvmAnnotationTypeDeclarationImpl extends JvmAnnotationTypeDeclarationImpl implements MutableAnnotationTypeDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableMethodDeclaration findDeclaredMethod(String name, TypeReference... parameterTypes) {
		super.findDeclaredMethod(name, parameterTypes) as MutableMethodDeclaration
	}
	
	override MutableFieldDeclaration findDeclaredField(String name) {
		super.findDeclaredField(name) as MutableFieldDeclaration
	}
	
	override MutableTypeDeclaration findDeclaredType(String name) {
		super.findDeclaredType(name) as MutableTypeDeclaration
	}
	
	override MutableConstructorDeclaration findDeclaredConstructor(TypeReference... parameterTypes) {
		super.findDeclaredConstructor(parameterTypes) as MutableConstructorDeclaration
	}
	
	override Iterable<? extends MutableMethodDeclaration> getDeclaredMethods() {
		super.declaredMethods as Iterable<? extends MutableMethodDeclaration>
	}
	
	override Iterable<? extends MutableFieldDeclaration> getDeclaredFields() {
		super.declaredFields as Iterable<? extends MutableFieldDeclaration>
	}
	
	override Iterable<? extends MutableClassDeclaration> getDeclaredClasses() {
		super.declaredClasses as Iterable<? extends MutableClassDeclaration>
	}
	
	override Iterable<? extends MutableConstructorDeclaration> getDeclaredConstructors() {
		super.declaredConstructors as Iterable<? extends MutableConstructorDeclaration>
	}
	
	override Iterable<? extends MutableInterfaceDeclaration> getDeclaredInterfaces() {
		super.declaredInterfaces as Iterable<? extends MutableInterfaceDeclaration>
	}
	
	override Iterable<? extends MutableAnnotationTypeDeclaration> getDeclaredAnnotationTypes() {
		super.declaredAnnotationTypes as Iterable<? extends MutableAnnotationTypeDeclaration>
	}
	
	override Iterable<? extends MutableEnumerationTypeDeclaration> getDeclaredEnumerationTypes() {
		super.declaredEnumerationTypes as Iterable<? extends MutableEnumerationTypeDeclaration>
	}
	
	override Iterable<? extends MutableTypeDeclaration> getDeclaredTypes() {
		super.declaredTypes as Iterable<? extends MutableTypeDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override Iterable<? extends MutableMemberDeclaration> getDeclaredMembers() {
		super.declaredMembers as Iterable<? extends MutableMemberDeclaration>
	}
	
	override MutableAnnotationTypeElementDeclaration addAnnotationTypeElement(String name, Procedure1<MutableAnnotationTypeElementDeclaration> initializer) {
		checkMutable
		checkJavaIdentifier(name, "name")
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		val newAnnotationElement = TypesFactory.eINSTANCE.createJvmOperation
		newAnnotationElement.simpleName = name
		newAnnotationElement.visibility = JvmVisibility.PUBLIC
		delegate.members.add(newAnnotationElement)
		val mutableAnnotationTypeElementDeclaration = compilationUnit.toMemberDeclaration(newAnnotationElement) as MutableAnnotationTypeElementDeclaration
		initializer.apply(mutableAnnotationTypeElementDeclaration)
		return mutableAnnotationTypeElementDeclaration
	}
	
	override MutableAnnotationTypeElementDeclaration findDeclaredAnnotationTypeElement(String name) {
		declaredAnnotationTypeElements.findFirst[simpleName == name]
	}
	
	override Iterable<? extends MutableAnnotationTypeElementDeclaration> getDeclaredAnnotationTypeElements() {
		delegate.members.map[compilationUnit.toMemberDeclaration(it)].filter(MutableAnnotationTypeElementDeclaration)
	}
	
}

class JvmAnnotationTypeDeclarationImpl extends JvmTypeDeclarationImpl<JvmAnnotationType> implements AnnotationTypeDeclaration {
	
	override addConstructor(Procedure1<MutableConstructorDeclaration> initializer) {
		throw new UnsupportedOperationException("The annotation '"+simpleName+"' cannot declare any constructors.")
	}
	
	override addField(String name, Procedure1<MutableFieldDeclaration> initializer) {
		throw new UnsupportedOperationException("The annotation '"+simpleName+"' cannot declare any fields.")
	}
	
	override addMethod(String name, Procedure1<MutableMethodDeclaration> initializer) {
		throw new UnsupportedOperationException("The annotation '"+simpleName+"' cannot declare any methods.")
	}
	
	override AnnotationTypeElementDeclaration findDeclaredAnnotationTypeElement(String name) {
		declaredAnnotationTypeElements.findFirst[simpleName == name]
	}
	
	override Iterable<? extends AnnotationTypeElementDeclaration> getDeclaredAnnotationTypeElements() {
		delegate.members.map[compilationUnit.toMemberDeclaration(it)].filter(AnnotationTypeElementDeclaration)
	}
	
}

class MutableJvmEnumerationTypeDeclarationImpl extends JvmEnumerationTypeDeclarationImpl implements MutableEnumerationTypeDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableMethodDeclaration findDeclaredMethod(String name, TypeReference... parameterTypes) {
		super.findDeclaredMethod(name, parameterTypes) as MutableMethodDeclaration
	}
	
	override MutableFieldDeclaration findDeclaredField(String name) {
		super.findDeclaredField(name) as MutableFieldDeclaration
	}
	
	override MutableTypeDeclaration findDeclaredType(String name) {
		super.findDeclaredType(name) as MutableTypeDeclaration
	}
	
	override MutableConstructorDeclaration findDeclaredConstructor(TypeReference... parameterTypes) {
		super.findDeclaredConstructor(parameterTypes) as MutableConstructorDeclaration
	}
	
	override Iterable<? extends MutableMethodDeclaration> getDeclaredMethods() {
		super.declaredMethods as Iterable<? extends MutableMethodDeclaration>
	}
	
	override Iterable<? extends MutableFieldDeclaration> getDeclaredFields() {
		super.declaredFields as Iterable<? extends MutableFieldDeclaration>
	}
	
	override Iterable<? extends MutableClassDeclaration> getDeclaredClasses() {
		super.declaredClasses as Iterable<? extends MutableClassDeclaration>
	}
	
	override Iterable<? extends MutableConstructorDeclaration> getDeclaredConstructors() {
		super.declaredConstructors as Iterable<? extends MutableConstructorDeclaration>
	}
	
	override Iterable<? extends MutableInterfaceDeclaration> getDeclaredInterfaces() {
		super.declaredInterfaces as Iterable<? extends MutableInterfaceDeclaration>
	}
	
	override Iterable<? extends MutableAnnotationTypeDeclaration> getDeclaredAnnotationTypes() {
		super.declaredAnnotationTypes as Iterable<? extends MutableAnnotationTypeDeclaration>
	}
	
	override Iterable<? extends MutableEnumerationTypeDeclaration> getDeclaredEnumerationTypes() {
		super.declaredEnumerationTypes as Iterable<? extends MutableEnumerationTypeDeclaration>
	}
	
	override Iterable<? extends MutableTypeDeclaration> getDeclaredTypes() {
		super.declaredTypes as Iterable<? extends MutableTypeDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override Iterable<? extends MutableMemberDeclaration> getDeclaredMembers() {
		super.declaredMembers as Iterable<? extends MutableMemberDeclaration> 
	}
	
	override Iterable<? extends MutableEnumerationValueDeclaration> getDeclaredValues() {
		declaredMembers.filter(MutableEnumerationValueDeclaration)
	}
	
	override MutableEnumerationValueDeclaration findDeclaredValue(String name) {
		declaredValues.findFirst[value | value.simpleName == name]
	}
	
	override addValue(String name, Procedure1<MutableEnumerationValueDeclaration> initializer) {
		checkMutable
		checkJavaIdentifier(name, "name")
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		val jvmLiteral = TypesFactory.eINSTANCE.createJvmEnumerationLiteral
		jvmLiteral.simpleName = name
		jvmLiteral.visibility = JvmVisibility.PUBLIC
		delegate.members.add(jvmLiteral)
		val mutableEnumerationValueDeclaration = compilationUnit.toMemberDeclaration(jvmLiteral) as MutableEnumerationValueDeclaration
		initializer.apply(mutableEnumerationValueDeclaration)
		return mutableEnumerationValueDeclaration
	}
	
}

class JvmEnumerationTypeDeclarationImpl extends JvmTypeDeclarationImpl<JvmEnumerationType> implements EnumerationTypeDeclaration {
	
	override getDeclaredValues() {
		declaredMembers.filter(EnumerationValueDeclaration)
	}
	
	override findDeclaredValue(String name) {
		declaredValues.findFirst[value | value.simpleName == name]
	}
	
}

class MutableJvmClassDeclarationImpl extends JvmClassDeclarationImpl implements MutableClassDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableMethodDeclaration findDeclaredMethod(String name, TypeReference... parameterTypes) {
		super.findDeclaredMethod(name, parameterTypes) as MutableMethodDeclaration
	}
	
	override MutableFieldDeclaration findDeclaredField(String name) {
		super.findDeclaredField(name) as MutableFieldDeclaration
	}
	
	override MutableTypeDeclaration findDeclaredType(String name) {
		super.findDeclaredType(name) as MutableTypeDeclaration
	}
	
	override MutableConstructorDeclaration findDeclaredConstructor(TypeReference... parameterTypes) {
		super.findDeclaredConstructor(parameterTypes) as MutableConstructorDeclaration
	}
	
	override Iterable<? extends MutableMethodDeclaration> getDeclaredMethods() {
		super.declaredMethods as Iterable<? extends MutableMethodDeclaration>
	}
	
	override Iterable<? extends MutableFieldDeclaration> getDeclaredFields() {
		super.declaredFields as Iterable<? extends MutableFieldDeclaration>
	}
	
	override Iterable<? extends MutableClassDeclaration> getDeclaredClasses() {
		super.declaredClasses as Iterable<? extends MutableClassDeclaration>
	}
	
	override Iterable<? extends MutableConstructorDeclaration> getDeclaredConstructors() {
		super.declaredConstructors as Iterable<? extends MutableConstructorDeclaration>
	}
	
	override Iterable<? extends MutableInterfaceDeclaration> getDeclaredInterfaces() {
		super.declaredInterfaces as Iterable<? extends MutableInterfaceDeclaration>
	}
	
	override Iterable<? extends MutableAnnotationTypeDeclaration> getDeclaredAnnotationTypes() {
		super.declaredAnnotationTypes as Iterable<? extends MutableAnnotationTypeDeclaration>
	}
	
	override Iterable<? extends MutableEnumerationTypeDeclaration> getDeclaredEnumerationTypes() {
		super.declaredEnumerationTypes as Iterable<? extends MutableEnumerationTypeDeclaration>
	}
	
	override Iterable<? extends MutableTypeDeclaration> getDeclaredTypes() {
		super.declaredTypes as Iterable<? extends MutableTypeDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override Iterable<? extends MutableMemberDeclaration> getDeclaredMembers() {
		super.declaredMembers as Iterable<? extends MutableMemberDeclaration>
	}
	
	override Iterable<? extends MutableTypeParameterDeclaration> getTypeParameters() {
		super.typeParameters as Iterable<? extends MutableTypeParameterDeclaration> 
	}

	override setStrictFloatingPoint(boolean isStrictFloatingPoint) {
		checkMutable
		delegate.setStrictFloatingPoint(isStrictFloatingPoint)
	}

	override setAbstract(boolean isAbstract) {
		checkMutable
		delegate.setAbstract(isAbstract)
	}
	
	override setFinal(boolean isFinal) {
		checkMutable
		delegate.setFinal(isFinal)
	}
	
	override setStatic(boolean isStatic) {
		checkMutable
		delegate.setStatic(isStatic)
	}
	
	override setExtendedClass(TypeReference superclass) {
		checkMutable
		checkInferredTypeReferences("extended class", superclass)
		val newTypeRef = 
			if (superclass !== null) {
				compilationUnit.toJvmTypeReference(superclass)
			} else {
				compilationUnit.typeReferences.getTypeForName(Object, compilationUnit.xtendFile)
			}
		val oldType = delegate.superTypes.findFirst[it.type instanceof JvmGenericType && !(type as JvmGenericType).isInterface]
		if (oldType !== null)
			delegate.superTypes.remove(oldType)
		delegate.superTypes.add(newTypeRef)
	}
	
	override setImplementedInterfaces(Iterable<? extends TypeReference> superInterfaces) {
		checkMutable
		checkIterable(superInterfaces, "superIntefaces")
		checkInferredTypeReferences("implemented interface", superInterfaces)
		val oldInterfaces = delegate.superTypes.filter[it.type instanceof JvmGenericType && (type as JvmGenericType).isInterface]
		delegate.superTypes.removeAll(oldInterfaces)
		delegate.superTypes.addAll(superInterfaces.map [
			compilationUnit.toJvmTypeReference(it)
		])
	}
	
	override addTypeParameter(String name, TypeReference... upperBounds) {
		checkMutable
		checkJavaIdentifier(name, "name")
		checkIterable(upperBounds, "upperBounds")
		checkInferredTypeReferences("parameter type", upperBounds)
		val param = TypesFactory.eINSTANCE.createJvmTypeParameter
		param.name = name
		delegate.typeParameters.add(param)
		for (upper : upperBounds) {
			val typeRef = compilationUnit.toJvmTypeReference(upper)
			val jvmUpperBound = TypesFactory.eINSTANCE.createJvmUpperBound
			jvmUpperBound.setTypeReference(typeRef)
			param.constraints.add(jvmUpperBound)
		}
		return compilationUnit.toTypeParameterDeclaration(param) as MutableTypeParameterDeclaration
	}
	
}

class JvmClassDeclarationImpl extends JvmTypeDeclarationImpl<JvmGenericType> implements ClassDeclaration {
	
	override getImplementedInterfaces() {
		val filtered = delegate.superTypes.filter[(it.type as JvmGenericType).interface]
		filtered.map[compilationUnit.toTypeReference(it)].toList
	}
	
	override getExtendedClass() {
		compilationUnit.toTypeReference(delegate.superTypes.findFirst[
			switch it: type {
				JvmGenericType case !interface : true
				default : false
			}
		])
	}
	
	override isAbstract() {
		delegate.isAbstract
	}
	
	override isFinal() {
		delegate.isFinal
	}
	
	override isStatic() {
		delegate.isStatic
	}
	
	override isStrictFloatingPoint() {
		delegate.isStrictFloatingPoint	
	}

	override getTypeParameters() {
		delegate.typeParameters.map[compilationUnit.toTypeParameterDeclaration(it)]
	}

	override findDeclaredField(String name) {
		declaredMembers.filter(FieldDeclaration).findFirst[it.simpleName == name]
	}
	
	override findDeclaredMethod(String name, TypeReference... parameterTypes) {
		checkIterable(parameterTypes, "parameterTypes")
		declaredMembers.filter(MethodDeclaration).findFirst[
			it.simpleName == name 
			&& it.parameters.map[type].toList == parameterTypes.toList
		]
	}
	
}

abstract class JvmExecutableDeclarationImpl<T extends JvmExecutable> extends JvmMemberDeclarationImpl<T> {
	
	def Iterable<? extends TypeParameterDeclaration> getTypeParameters() {
		delegate.typeParameters.map[compilationUnit.toTypeParameterDeclaration(it)]
	}
	
	def isVarArgs() {
		delegate.varArgs
	}
	
	def Iterable<? extends ParameterDeclaration> getParameters() {
		delegate.parameters.map[compilationUnit.toParameterDeclaration(it)]
	}
	
	def getExceptions() {
		delegate.exceptions.map[compilationUnit.toTypeReference(it)]
	}
	
	def getBody() {
		val expression = compilationUnit.jvmTypesBuilder.getExpression(delegate)
		compilationUnit.toExpression(expression)
	}
	
	def setBody(Expression body) {
		checkMutable
		if (body === null) {
			compilationUnit.jvmTypesBuilder.removeExistingBody(delegate)
		} else {
			compilationUnit.jvmTypesBuilder.setBody(delegate, (body as ExpressionImpl).delegate)
		}
	}
	
	def setExceptions(TypeReference... exceptions) {
		checkMutable
		checkIterable(exceptions, "exceptions")
		checkInferredTypeReferences("exception type", exceptions)
		delegate.exceptions.clear
		for (exceptionType : exceptions) {
			if (exceptionType !== null) {
				delegate.exceptions.add(compilationUnit.toJvmTypeReference(exceptionType))
			}
		}
	}
	
	def setVarArgs(boolean isVarArgs) {
		checkMutable
		delegate.setVarArgs(isVarArgs)
	}
	
	def MutableTypeParameterDeclaration addTypeParameter(String name, TypeReference... upperBounds) {
		checkMutable
		checkJavaIdentifier(name, "name")
		checkIterable(upperBounds, "upperBounds")
		checkInferredTypeReferences("parameter type", upperBounds)
		val param = TypesFactory.eINSTANCE.createJvmTypeParameter
		param.name = name
		delegate.typeParameters.add(param)
		for (upper : upperBounds) {
			val typeRef = compilationUnit.toJvmTypeReference(upper)
			val jvmUpperBound = TypesFactory.eINSTANCE.createJvmUpperBound
			jvmUpperBound.setTypeReference(typeRef)
			param.constraints.add(jvmUpperBound)
		}
		return compilationUnit.toTypeParameterDeclaration(param) as MutableTypeParameterDeclaration
	}
	
	def setBody(CompilationStrategy compilationStrategy) {
		checkMutable
		Preconditions.checkArgument(compilationStrategy !== null, "compilationStrategy cannot be null")
		compilationUnit.setCompilationStrategy(delegate, compilationStrategy)
	}
	
	def setBody(StringConcatenationClient compilationTemplate) {
		checkMutable
		Preconditions.checkArgument(compilationTemplate !== null, "compilationTemplate cannot be null")
		compilationUnit.setCompilationTemplate(delegate, compilationTemplate)
	}
	
	def MutableParameterDeclaration addParameter(String name, TypeReference type) {
		return addParameter(delegate.parameters.size, name, type) 
	}
	
	def MutableParameterDeclaration addParameter(int index, String name, TypeReference type) {
		checkMutable
		checkJavaIdentifier(name, "name");
		Preconditions.checkArgument(type !== null, "type cannot be null");
		if (type.inferred)
			throw new IllegalArgumentException("Cannot use inferred type as parameter type.")
		val param = TypesFactory.eINSTANCE.createJvmFormalParameter
		param.name = name
		param.parameterType = compilationUnit.toJvmTypeReference(type)
		delegate.parameters.add(index, param)
		return compilationUnit.toParameterDeclaration(param) as MutableParameterDeclaration 
	}
	
	override remove() {
		compilationUnit.jvmModelAssociator.removeLogicalChildAssociation(delegate)
		super.remove()
	}
	
}

class MutableJvmParameterDeclarationImpl extends JvmParameterDeclarationImpl implements MutableParameterDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableExecutableDeclaration getDeclaringExecutable() {
		super.declaringExecutable as MutableExecutableDeclaration
	}
	
	override setSimpleName(String name) {
		checkMutable
		checkJavaIdentifier(name, "name");
		delegate.name = name
	}
	
}

class JvmParameterDeclarationImpl extends JvmAnnotationTargetImpl<JvmFormalParameter> implements ParameterDeclaration {

	override getType() {
		compilationUnit.toTypeReference(delegate.parameterType)
	}
	
	override getDeclaringExecutable() {
		compilationUnit.toMemberDeclaration(delegate.eContainer as JvmMember) as ExecutableDeclaration
	}
	
}

class MutableJvmMethodDeclarationImpl extends JvmMethodDeclarationImpl implements MutableMethodDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override Iterable<? extends MutableParameterDeclaration> getParameters() {
		super.parameters as Iterable<? extends MutableParameterDeclaration> 
	}
	
	override Iterable<? extends MutableTypeParameterDeclaration> getTypeParameters() {
		super.typeParameters as Iterable<? extends MutableTypeParameterDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override setStrictFloatingPoint(boolean isStrictFloatingPoint) {
		checkMutable
		delegate.setStrictFloatingPoint(isStrictFloatingPoint)
	}

	override setNative(boolean isNative) {
		checkMutable
		delegate.setNative(isNative)
	}

	override setReturnType(TypeReference type) {
		checkMutable
		Preconditions.checkArgument(type !== null, "returnType cannot be null");
		delegate.setReturnType(compilationUnit.toJvmTypeReference(type))
	}
	
	override setAbstract(boolean isAbstract) {
		checkMutable
		delegate.setAbstract(isAbstract)
		if (isAbstract) {
			delegate.^default = false
		}
	}
	
	override setFinal(boolean isFinal) {
		checkMutable
		delegate.setFinal(isFinal)
	}
	
//	override setOverride(boolean isOverride) {
//		delegate.setOverride(isOverride)
//	}
	
	override setStatic(boolean isStatic) {
		checkMutable
		delegate.setStatic(isStatic)
		if (isStatic) {
			delegate.^default = false
		}
	}
	
	override setSynchronized(boolean isSynchronized) {
		checkMutable
		delegate.setSynchronized(isSynchronized)
	}
	
	override setDefault(boolean isDefault) {
		checkMutable
		delegate.setDefault(isDefault)
		if (isDefault) {
			delegate.abstract = false
			delegate.static = false
		}
	}
	
}

class JvmMethodDeclarationImpl extends JvmExecutableDeclarationImpl<JvmOperation> implements MethodDeclaration {
	
	override isAbstract() {
		delegate.isAbstract
	}
	
	override isFinal() {
		delegate.isFinal
	}
	
	override isStatic() {
		delegate.isStatic
	}
	
	override isSynchronized() {
		delegate.isSynchronized
	}
	
	override isDefault() {
		delegate.isDefault
	}
	
	override isStrictFloatingPoint() {
		delegate.isStrictFloatingPoint	
	}
	
	override isNative() {
		delegate.isNative	
	}
	
	override getReturnType() {
		compilationUnit.toTypeReference(delegate.returnType)
	}
	
	override getOverriddenOrImplementedMethods() {
		val resolvedFeatures = compilationUnit.overrideHelper.getResolvedFeatures(delegate.declaringType)
		val resolvedOperation = resolvedFeatures.getResolvedOperation(delegate)
		val overriddenOrImplemented = resolvedOperation.overriddenAndImplementedMethods
		return overriddenOrImplemented.map[compilationUnit.toMemberDeclaration(it.declaration)].filter(MethodDeclaration)
	}
	
}

class MutableJvmConstructorDeclarationImpl extends JvmConstructorDeclarationImpl implements MutableConstructorDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override Iterable<? extends MutableParameterDeclaration> getParameters() {
		super.parameters as Iterable<? extends MutableParameterDeclaration>
	}
	
	override Iterable<? extends MutableTypeParameterDeclaration> getTypeParameters() {
		super.typeParameters as Iterable<? extends MutableTypeParameterDeclaration>
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
}

class JvmConstructorDeclarationImpl extends JvmExecutableDeclarationImpl<JvmConstructor> implements ConstructorDeclaration {
	
	override getSimpleName() {
		declaringType.simpleName
	}
	
}

class MutableJvmEnumerationValueDeclarationImpl extends JvmEnumerationValueDeclarationImpl implements MutableEnumerationValueDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableEnumerationTypeDeclaration getDeclaringType() {
		super.getDeclaringType() as MutableEnumerationTypeDeclaration
	}
	
}

class JvmEnumerationValueDeclarationImpl extends JvmMemberDeclarationImpl<JvmEnumerationLiteral> implements EnumerationValueDeclaration {
	
	override setVisibility(Visibility visibility) {
		throw new UnsupportedOperationException("It is not possible to change visibility of enumeration value.")
	}
	
	override EnumerationTypeDeclaration getDeclaringType() {
		super.getDeclaringType() as EnumerationTypeDeclaration
	}

}

class MutableJvmFieldDeclarationImpl extends JvmFieldDeclarationImpl implements MutableFieldDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override markAsInitializedBy(ConstructorDeclaration constructorDeclaration) {
		checkMutable
		val constructor = switch constructorDeclaration {
			JvmConstructorDeclarationImpl: constructorDeclaration.delegate
			XtendConstructorDeclarationImpl: {
				val jvmElement = compilationUnit.jvmModelAssociations.getPrimaryJvmElement(constructorDeclaration.delegate)
				if (jvmElement instanceof JvmConstructor) {
					jvmElement
				}
			}
		}
		compilationUnit.readAndWriteTracking.markInitialized(delegate, constructor)
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}
	
	override setInitializer(Expression initializer) {
		checkMutable
		if (initializer === null) {
			compilationUnit.jvmTypesBuilder.removeExistingBody(delegate)
		} else {
			compilationUnit.jvmTypesBuilder.setInitializer(delegate, (initializer as ExpressionImpl).delegate)
		}
	}
	
	override setInitializer(CompilationStrategy initializer) {
		checkMutable
		Preconditions.checkArgument(initializer !== null, "initializer cannot be null")
		compilationUnit.setCompilationStrategy(delegate, initializer)
	}
	
	override setInitializer(StringConcatenationClient template) {
		checkMutable
		Preconditions.checkArgument(template !== null, "template cannot be null")
		compilationUnit.setCompilationTemplate(delegate, template)
	}
	
	override setFinal(boolean isFinal) {
		checkMutable
		delegate.setFinal(isFinal)
	}
	
	override setStatic(boolean isStatic) {
		checkMutable
		delegate.setStatic(isStatic)
	}
	
	override setTransient(boolean isTransient) {
		checkMutable
		delegate.setTransient(isTransient)
	}
	
	override setVolatile(boolean isVolatile) {
		checkMutable
		delegate.setVolatile(isVolatile)
	}
	
	override setType(TypeReference type) {
		checkMutable
		Preconditions.checkArgument(type !== null, "type cannot be null");
		delegate.setType(compilationUnit.toJvmTypeReference(type))
	}
	
	override setConstantValueAsBoolean(boolean value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»''')
	}
	
	private def internalGenericSetConstantValue(Object value) {
		checkMutable
		Preconditions.checkArgument(value !== null, "value cannot be null");
		delegate.constant = true
		delegate.final = true
		delegate.static = true
		delegate.constantValue = value
	}
	
	override setConstantValueAsByte(byte value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»''')
	}
	
	override setConstantValueAsInt(int value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»''')
	}
	
	override setConstantValueAsShort(short value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»''')
	}
	
	override setConstantValueAsLong(long value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»L''')
	}
	
	override setConstantValueAsFloat(float value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»f''')
	}
	
	override setConstantValueAsDouble(double value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«value»d''')
	}
	
	override setConstantValueAsChar(char value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''«»'«Strings.convertToJavaString(Character.toString(value))»'«»''')
	}
	
	override setConstantValueAsString(String value) {
		internalGenericSetConstantValue(value)
		compilationUnit.setCompilationTemplate(delegate, '''"«Strings.convertToJavaString(value)»"''')
	}
	
}

class JvmFieldDeclarationImpl extends JvmMemberDeclarationImpl<JvmField> implements FieldDeclaration {
	
	override getInitializer() {
		compilationUnit.toExpression(compilationUnit.jvmTypesBuilder.getExpression(delegate))
	}
	
	override isFinal() {
		delegate.isFinal
	}
	
	override isStatic() {
		delegate.isStatic
	}
	
	override isTransient() {
		delegate.isTransient
	}
	
	override isVolatile() {
		delegate.isVolatile
	}
	
	override getType() {
		compilationUnit.toTypeReference(delegate.type)
	}
	
	override remove() {
		compilationUnit.jvmModelAssociator.removeAllAssociation(delegate.type)
		compilationUnit.jvmModelAssociator.removeAllAssociation(delegate)
		compilationUnit.jvmModelAssociator.removeLogicalChildAssociation(delegate)
		super.remove()
	}
	
}

class MutableJvmTypeParameterDeclarationImpl extends JvmTypeParameterDeclarationImpl implements MutableAnnotationTarget, MutableTypeParameterDeclaration {
	
	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableTypeParameterDeclarator getTypeParameterDeclarator() {
		super.typeParameterDeclarator as MutableTypeParameterDeclarator
	}
	
	override setSimpleName(String name) {
		checkMutable
		checkJavaIdentifier(name, "name");
		delegate.name = name
	}
	
	override remove() {
		checkMutable
		Preconditions.checkState(delegate.eResource !== null, '''This element has already been removed: «delegate»''')
		
		compilationUnit.jvmModelAssociator.removeAllAssociation(delegate)
		EcoreUtil.remove(delegate)
		
		Preconditions.checkState(delegate.eResource === null, '''Couldn't remove: «delegate»''')
	}
	
	override addAnnotation(AnnotationReference annotationReference) {
		throw new UnsupportedOperationException("It is not possible to add an annotation.")
	}
	
	override removeAnnotation(AnnotationReference annotationReference) {
		throw new UnsupportedOperationException("It is not possible to remove an annotation.")
	}
	
	override setUpperBounds(Iterable<? extends TypeReference> upperBounds) {
		checkMutable
		checkIterable(upperBounds, "upperBounds")
		checkInferredTypeReferences("parameter type", upperBounds)
		delegate.constraints.clear
		for (upper : upperBounds) {
			val typeRef = compilationUnit.toJvmTypeReference(upper)
			val jvmUpperBound = TypesFactory.eINSTANCE.createJvmUpperBound
			jvmUpperBound.setTypeReference(typeRef)
			delegate.constraints.add(jvmUpperBound)
		}
	}
	
	protected final def checkMutable() {
		if (compilationUnit.lastPhase != AnnotationCallback.INFERENCE) 
			throw new IllegalStateException("Element cannot be modified outside the transformation phase")
	}
}

class JvmTypeParameterDeclarationImpl extends TypeParameterDeclarationImpl implements AnnotationTarget, TypeParameterDeclaration {
	
	override TypeParameterDeclarator getTypeParameterDeclarator() {
		compilationUnit.toMemberDeclaration(delegate.eContainer as JvmMember) as TypeParameterDeclarator
	}
	
	override isAssignableFrom(Type otherType) {
		if (otherType === null)
			return false;
		val thisTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(this)
		val thatTypeRef = compilationUnit.typeReferenceProvider.newTypeReference(otherType)
		return thisTypeRef.isAssignableFrom(thatTypeRef);
	}
	
	override getQualifiedName() {
		delegate.identifier
	}
	
}

class MutableJvmAnnotationTypeElementDeclarationImpl extends JvmAnnotationTypeElementDeclarationImpl implements MutableAnnotationTypeElementDeclaration {

	override markAsRead() {
		checkMutable
		compilationUnit.readAndWriteTracking.markReadAccess(delegate)
	}
	
	override MutableTypeDeclaration getDeclaringType() {
		super.declaringType as MutableTypeDeclaration
	}

}

class JvmAnnotationTypeElementDeclarationImpl extends JvmMemberDeclarationImpl<JvmOperation> implements AnnotationTypeElementDeclaration {
	
	override getDefaultValue() {
		return compilationUnit.translateAnnotationValue(delegate.defaultValue, compilationUnit.typeReferences.isArray(delegate.returnType))
	}
	
	override getType() {
		compilationUnit.toTypeReference( delegate.returnType )
	}
	
	override getDefaultValueExpression() {
		return null
	}
	
	def setDefaultValueExpression(Expression body) {
		checkMutable
		if (body === null) {
			compilationUnit.jvmTypesBuilder.removeExistingBody(delegate)
		} else {
			compilationUnit.jvmTypesBuilder.setBody(delegate, (body as ExpressionImpl).delegate)
		}
	}
	
	def setDefaultValueExpression(CompilationStrategy compilationStrategy) {
		checkMutable
		Preconditions.checkArgument(compilationStrategy !== null, "compilationStrategy cannot be null")
		compilationUnit.setCompilationStrategy(delegate, compilationStrategy)
	}
	
	def setDefaultValueExpression(StringConcatenationClient compilationTemplate) {
		checkMutable
		Preconditions.checkArgument(compilationTemplate !== null, "compilationTemplate cannot be null")
		compilationUnit.setCompilationTemplate(delegate, compilationTemplate)
	}
	
	def setType(TypeReference type) {
		checkMutable
		Preconditions.checkArgument(type !== null, "returnType cannot be null");
		delegate.setReturnType(compilationUnit.toJvmTypeReference(type))
	}
	
}

