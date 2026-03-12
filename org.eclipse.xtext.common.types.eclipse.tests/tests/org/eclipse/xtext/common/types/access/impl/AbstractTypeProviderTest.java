/*******************************************************************************
 * Copyright (c) 2009, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.impl;

import static com.google.common.collect.Iterables.*;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.impl.EValidatorRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmAnnotationAnnotationValue;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationTarget;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmArrayType;
import org.eclipse.xtext.common.types.JvmBooleanAnnotationValue;
import org.eclipse.xtext.common.types.JvmByteAnnotationValue;
import org.eclipse.xtext.common.types.JvmCharAnnotationValue;
import org.eclipse.xtext.common.types.JvmComponentType;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmDoubleAnnotationValue;
import org.eclipse.xtext.common.types.JvmEnumAnnotationValue;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFloatAnnotationValue;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmInnerTypeReference;
import org.eclipse.xtext.common.types.JvmIntAnnotationValue;
import org.eclipse.xtext.common.types.JvmLongAnnotationValue;
import org.eclipse.xtext.common.types.JvmLowerBound;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmPrimitiveType;
import org.eclipse.xtext.common.types.JvmShortAnnotationValue;
import org.eclipse.xtext.common.types.JvmStringAnnotationValue;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeAnnotationValue;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeParameterDeclarator;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUpperBound;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.JvmWildcardTypeReference;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.testSetups.AbstractMethods;
import org.eclipse.xtext.common.types.testSetups.AnnotatedClassWithStringDefault;
import org.eclipse.xtext.common.types.testSetups.AnnotatedInterfaceWithStringDefault;
import org.eclipse.xtext.common.types.testSetups.Bug334943Client;
import org.eclipse.xtext.common.types.testSetups.Bug347739;
import org.eclipse.xtext.common.types.testSetups.Bug347739OneTypeParam;
import org.eclipse.xtext.common.types.testSetups.Bug347739ThreeTypeParams;
import org.eclipse.xtext.common.types.testSetups.Bug347739ThreeTypeParamsSuper;
import org.eclipse.xtext.common.types.testSetups.Bug347739ThreeTypeParamsSuperSuper;
import org.eclipse.xtext.common.types.testSetups.Bug427098;
import org.eclipse.xtext.common.types.testSetups.Bug428340;
import org.eclipse.xtext.common.types.testSetups.Bug456328;
import org.eclipse.xtext.common.types.testSetups.Bug470767;
import org.eclipse.xtext.common.types.testSetups.CallableThrowsExceptions;
import org.eclipse.xtext.common.types.testSetups.ClassContainingEnum;
import org.eclipse.xtext.common.types.testSetups.ClassWithVarArgs;
import org.eclipse.xtext.common.types.testSetups.DeprecatedMembers;
import org.eclipse.xtext.common.types.testSetups.EmptyAbstractClass;
import org.eclipse.xtext.common.types.testSetups.Fields;
import org.eclipse.xtext.common.types.testSetups.InitializerWithConstructor;
import org.eclipse.xtext.common.types.testSetups.InitializerWithoutConstructor;
import org.eclipse.xtext.common.types.testSetups.Methods;
import org.eclipse.xtext.common.types.testSetups.NestedInterfaces;
import org.eclipse.xtext.common.types.testSetups.NestedTypes;
import org.eclipse.xtext.common.types.testSetups.ParameterizedMethods;
import org.eclipse.xtext.common.types.testSetups.ParameterizedTypes;
import org.eclipse.xtext.common.types.testSetups.ParameterizedTypes2;
import org.eclipse.xtext.common.types.testSetups.RawIterable;
import org.eclipse.xtext.common.types.testSetups.StaticNestedTypes;
import org.eclipse.xtext.common.types.testSetups.TestAnnotation;
import org.eclipse.xtext.common.types.testSetups.TestAnnotation.Annotated;
import org.eclipse.xtext.common.types.testSetups.TestAnnotationWithDefaults;
import org.eclipse.xtext.common.types.testSetups.TestAnnotationWithStringDefault;
import org.eclipse.xtext.common.types.testSetups.TestConstants;
import org.eclipse.xtext.common.types.testSetups.TestEnum;
import org.eclipse.xtext.common.types.testSetups.TypeWithInnerAnnotation;
import org.eclipse.xtext.common.types.testSetups.TypeWithInnerEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public abstract class AbstractTypeProviderTest extends Assert {

	private Diagnostician diagnostician;

	protected abstract IJvmTypeProvider getTypeProvider();

	@Before
	public void setUp() throws Exception {
		EValidator.Registry registry = new EValidatorRegistryImpl(EValidator.Registry.INSTANCE);
		registry.put(TypesPackage.eINSTANCE, new EObjectValidator());
		diagnostician = new Diagnostician(registry);
	}

	protected void diagnose(EObject object) {
		Resource resource = object.eResource();
		for (EObject content : resource.getContents()) {
			Diagnostic diagnostic = diagnostician.validate(content);
			if (diagnostic.getSeverity() != Diagnostic.OK) {
				diagnose(diagnostic);
			}

		}
	}

	protected void diagnose(Diagnostic diagnostic) {
		if (diagnostic.getChildren().isEmpty()) {
			assertEquals(String.valueOf(diagnostic), diagnostic.getSeverity(), Diagnostic.OK);
		} else {
			for (Diagnostic child : diagnostic.getChildren()) {
				diagnose(child);
			}
		}
	}

	protected void diagnose(EObject object, String... expectedUnresolvedProxies) {
		Resource resource = object.eResource();
		for (EObject content : resource.getContents()) {
			Diagnostic diagnostic = diagnostician.validate(content);
			if (diagnostic.getSeverity() != Diagnostic.OK) {
				URI[] expectedUnresolvedProxyURIs = new URI[expectedUnresolvedProxies.length];
				for (int i = 0; i < expectedUnresolvedProxies.length; i++) {
					expectedUnresolvedProxyURIs[i] = URI.createURI(expectedUnresolvedProxies[i]);
				}
				diagnose(diagnostic, expectedUnresolvedProxyURIs);
			}

		}
	}

	protected void diagnose(Diagnostic diagnostic, URI[] expectedUnresolvedProxies) {
		if (diagnostic.getChildren().isEmpty()) {
			if (diagnostic.getSeverity() != Diagnostic.OK) {
				if (diagnostic.getCode() == EObjectValidator.EOBJECT__EVERY_PROXY_RESOLVES) {
					EObject proxy = (EObject) diagnostic.getData().get(2); // magic number ...
					if (org.eclipse.xtext.util.Arrays.contains(expectedUnresolvedProxies, EcoreUtil.getURI(proxy))) {
						return;
					}
				}
				assertEquals(String.valueOf(diagnostic), diagnostic.getSeverity(), Diagnostic.OK);
			}
		} else {
			for (Diagnostic child : diagnostic.getChildren()) {
				diagnose(child, expectedUnresolvedProxies);
			}
		}
	}

	protected void recomputeAndCheckIdentifiers(Resource resource) {
		Iterator<JvmMember> iter = Iterators.filter(EcoreUtil.getAllContents(resource, false), JvmMember.class);
		while (iter.hasNext()) {
			JvmMember member = iter.next();
			String identifier = member.getIdentifier();
			member.internalSetIdentifier(null);
			String computed = member.getIdentifier();
			assertNotNull(String.valueOf(member), computed);
			assertEquals(identifier, computed);
			checkDeprecatedBitSet(member);
		}
		Iterator<JvmFormalParameter> params = Iterators.filter(EcoreUtil.getAllContents(resource, false),
				JvmFormalParameter.class);
		while (params.hasNext()) {
			JvmFormalParameter parameter = params.next();
			assertNotNull(parameter.eContainer().toString(), parameter.getName());
		}
	}

	protected void checkDeprecatedBitSet(JvmMember member) {
		assertTrue(member.isSetDeprecated());
	}

	protected void getAndResolveAllFragments(Resource resource) {
		Iterator<EObject> iter = EcoreUtil.getAllContents(resource, false);
		while (iter.hasNext()) {
			EObject next = iter.next();
			String fragment = resource.getURIFragment(next);
			EObject resolved = resource.getEObject(fragment);
			assertSame(fragment + " / " + next.eClass().getName(), next, resolved);
		}
	}

	protected void assertMembers(String typeName, Set<String> memberNames) {
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertEquals(memberNames.size(), type.getMembers().size());
		for (org.eclipse.xtext.common.types.JvmMember member : type.getMembers()) {
			assertTrue(member.getIdentifier(), member instanceof JvmOperation);
			JvmOperation op = (JvmOperation) member;
			assertTrue(op.getSimpleName(), memberNames.remove(op.getSimpleName()));
		}
		assertTrue(memberNames.isEmpty());
	}

	@Test
	public void testFindTypeByName_int() {
		String typeName = "int";
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmPrimitiveType);
		assertEquals(typeName, type.getIdentifier());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_int_twice() {
		String typeName = "int";
		JvmType firstType = getTypeProvider().findTypeByName(typeName);
		JvmType secondType = getTypeProvider().findTypeByName(typeName);
		assertSame(firstType, secondType);
	}

	@Test
	public void testFindTypeByName_int_array_01() {
		String typeName = "int[]";
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmArrayType);
		assertEquals(typeName, type.getIdentifier());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_int_array_02() {
		String typeName = int[].class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmArrayType);
		assertEquals("int[]", type.getIdentifier());
	}

	@Test
	public void testFindTypeByName_int_array_03() {
		String typeName = int[][][].class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmArrayType);
		assertEquals("int[][][]", type.getIdentifier());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_int_array_04() {
		String typeName = "int[][][]";
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmArrayType);
		assertEquals(typeName, type.getIdentifier());
	}

	@Test
	public void testFindTypeByName_javaLangCharSequence_01() {
		String typeName = CharSequence.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmGenericType);
		assertEquals(typeName, type.getIdentifier());
		assertTrue(((JvmDeclaredType) type).isAbstract());
		assertFalse(((JvmDeclaredType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_javaLangCharSequence_02() {
		String typeName = CharSequence.class.getName();
		int javaVersion = Runtime.version().feature();
		Set<String> expectedMemberNames = new HashSet<>(
				Set.of("length", "chars", "charAt", "codePoints", "subSequence", "toString"));
		if (javaVersion >= 11) {
			expectedMemberNames.add("compare");
		}
		if (javaVersion >= 15) {
			expectedMemberNames.add("isEmpty");
		}
		if (javaVersion >= 25) {
			expectedMemberNames.add("getChars");
		}
		assertMembers(typeName, expectedMemberNames);
	}

	@Test
	public void testFindTypeByName_javaLangCharSequence_03() {
		String typeName = CharSequence.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation length = (JvmOperation) type.eResource().getEObject("java.lang.CharSequence.length()");
		assertNotNull(length);
		JvmType returnType = length.getReturnType().getType();
		assertNotNull(returnType);
		assertFalse(returnType.eIsProxy());
		assertEquals("int", returnType.getIdentifier());
	}

	@Test
	public void testFindTypeByName_javaLangCharSequence_04() {
		String typeName = CharSequence.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation charAt = (JvmOperation) type.eResource().getEObject("java.lang.CharSequence.charAt(int)");
		assertNotNull(charAt);
		JvmType returnType = charAt.getReturnType().getType();
		assertNotNull(returnType);
		assertFalse(returnType.eIsProxy());
		assertEquals("char", returnType.getIdentifier());
		assertEquals(1, charAt.getParameters().size());
		JvmType intType = getTypeProvider().findTypeByName("int");
		assertSame(intType, charAt.getParameters().get(0).getParameterType().getType());
	}

	@Test
	public void testFindTypeByName_javaLangNumber_01() {
		String typeName = Number.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertFalse("toplevel type is not static", type.isStatic());
		assertEquals(type.getSuperTypes().toString(), 2, type.getSuperTypes().size());
		JvmType objectType = type.getSuperTypes().get(0).getType();
		assertFalse("isProxy: " + objectType, objectType.eIsProxy());
		assertEquals(Object.class.getName(), objectType.getIdentifier());
		JvmType serializableType = type.getSuperTypes().get(1).getType();
		assertFalse("isProxy: " + serializableType, serializableType.eIsProxy());
		assertEquals(Serializable.class.getName(), serializableType.getIdentifier());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_javaLangNumber_02() {
		String typeName = Number[][].class.getName();
		JvmArrayType type = (JvmArrayType) getTypeProvider().findTypeByName(typeName);
		JvmOperation longValue = (JvmOperation) type.eResource().getEObject("java.lang.Number.longValue()");
		assertNotNull(longValue);
		JvmDeclaredType number = longValue.getDeclaringType();
		assertNotNull(number.getArrayType());
		assertSame(type, number.getArrayType().getArrayType());
		assertNull(type.eGet(TypesPackage.Literals.JVM_COMPONENT_TYPE__ARRAY_TYPE));
		// array will created on the fly
		assertNotNull(type.getArrayType());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_javaUtilList_01() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(typeName, type.getIdentifier());
		assertEquals(1, type.getTypeParameters().size());
		JvmTypeParameter typeVariable = type.getTypeParameters().get(0);
		assertEquals("E", typeVariable.getName());
		assertEquals(1, typeVariable.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeVariable.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertSame(typeVariable, upperBound.getOwner());
		assertNotNull(upperBound.getTypeReference());
		assertFalse(upperBound.getTypeReference().getType().eIsProxy());
		assertEquals(Object.class.getName(), upperBound.getTypeReference().getIdentifier());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_javaUtilList_02() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation toArray = (JvmOperation) type.eResource().getEObject("java.util.List.toArray()");
		assertNotNull(toArray);
		assertEquals("java.lang.Object[]", toArray.getReturnType().getIdentifier());
	}

	@Test
	public void testFindTypeByName_javaUtilList_03() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation toArray = (JvmOperation) type.eResource().getEObject("java.util.List.toArray(T[])");
		assertNotNull(toArray);
		assertEquals("T[]", toArray.getReturnType().getIdentifier());
	}

	@Test
	public void testFindTypeByName_javaUtilList_04() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation addAll = (JvmOperation) type.eResource().getEObject("java.util.List.addAll(java.util.Collection)");
		assertNotNull(addAll);
		assertEquals("boolean", addAll.getReturnType().getIdentifier());
	}

	@Test
	public void testFindTypeByName_javaUtilList_05() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation addAll = (JvmOperation) type.eResource().getEObject("java.util.List.addAll(java.util.Collection)");
		assertEquals(1, addAll.getParameters().size());
		assertEquals(getCollectionParamName(), addAll.getParameters().get(0).getName());
		JvmType parameterType = addAll.getParameters().get(0).getParameterType().getType();
		assertFalse(parameterType.toString(), parameterType.eIsProxy());
	}

	@Test
	public void testFindTypeByName_javaUtilList_06() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation containsAll = (JvmOperation) type.eResource()
				.getEObject("java.util.List.containsAll(java.util.Collection)");
		assertEquals(1, containsAll.getParameters().size());
		assertEquals(getCollectionParamName(), containsAll.getParameters().get(0).getName());
		JvmType parameterType = containsAll.getParameters().get(0).getParameterType().getType();
		assertFalse(parameterType.toString(), parameterType.eIsProxy());
	}

	@Test
	public void testFindTypeByName_javaUtilList_07() {
		String typeName = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		JvmParameterizedTypeReference superType = (JvmParameterizedTypeReference) type.getSuperTypes().get(0);
		assertFalse(superType.getType().eIsProxy());
		// in Java 21 List extends SequencedCollection
		// but this test relies on sources so we cannot use stubbed classes
		// like in xtext.java.tests or common.types.tests
		// so we remove "Sequenced" from the super type
		// Lorenzo: that's the best and simplest solution I could find
		assertEquals("java.util.Collection<E>",
				superType.getIdentifier().replace("Sequenced", ""));
		assertEquals(1, type.getTypeParameters().size());
		JvmType rawType = superType.getType();
		assertFalse(rawType.eIsProxy());
		assertEquals(1, superType.getArguments().size());
		JvmTypeReference superTypeParameter = superType.getArguments().get(0);
		JvmType parameterType = superTypeParameter.getType();
		assertFalse(parameterType.eIsProxy());
		assertSame(parameterType, type.getTypeParameters().get(0));
	}

	@Test
	public void testSuperTypeOfInterface() {
		String typeName = Serializable.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertEquals(Object.class.getName(), type.getSuperTypes().get(0).getIdentifier());
	}

	protected JvmOperation getMethodFromParameterizedMethods(String method) {
		String typeName = ParameterizedMethods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		JvmOperation result = (JvmOperation) type.eResource()
				.getEObject(ParameterizedMethods.class.getName() + "." + method);
		assertNotNull(result);
		return result;
	}

	@Test
	public void testMemberCount_01() {
		String typeName = ParameterizedMethods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = ParameterizedMethods.class.getDeclaredMethods().length;
		assertEquals(10, methodCount);
		int constructorCount = ParameterizedMethods.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount);
		assertEquals(methodCount + constructorCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_02() {
		String typeName = InitializerWithConstructor.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = InitializerWithConstructor.class.getDeclaredMethods().length;
		assertEquals(0, methodCount);
		int constructorCount = InitializerWithConstructor.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount);
		assertEquals(methodCount + constructorCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_03() {
		String typeName = InitializerWithoutConstructor.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = InitializerWithoutConstructor.class.getDeclaredMethods().length;
		assertEquals(0, methodCount);
		int constructorCount = InitializerWithoutConstructor.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		assertEquals(methodCount + constructorCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_04() {
		String typeName = NestedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = NestedTypes.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = NestedTypes.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int nestedTypesCount = NestedTypes.class.getClasses().length;
		assertEquals(1, nestedTypesCount);
		assertEquals(methodCount + constructorCount + nestedTypesCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_05() {
		String typeName = NestedTypes.Outer.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_05(type);
	}

	@Test
	public void testMemberCount_05_01() {
		String typeName = NestedTypes.Outer.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_05(type);
	}

	@Test
	public void testMemberCount_05_02() {
		String typeName = NestedTypes.Outer.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_05(JvmGenericType type) {
		int methodCount = NestedTypes.Outer.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = NestedTypes.Outer.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int nestedTypesCount = NestedTypes.Outer.class.getClasses().length;
		assertEquals(1, nestedTypesCount);
		assertEquals(methodCount + constructorCount + nestedTypesCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_06() {
		String typeName = NestedTypes.Outer.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_06(type);
	}

	@Test
	public void testMemberCount_06_01() {
		String typeName = NestedTypes.Outer.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_06(type);
	}

	@Test
	public void testMemberCount_06_02() {
		String typeName = NestedTypes.Outer.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_06(JvmGenericType type) {
		int methodCount = NestedTypes.Outer.Inner.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = NestedTypes.Outer.Inner.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		assertEquals(methodCount + constructorCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_07() {
		String typeName = StaticNestedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = StaticNestedTypes.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = StaticNestedTypes.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int nestedTypesCount = StaticNestedTypes.class.getClasses().length;
		assertEquals(1, nestedTypesCount);
		assertEquals(methodCount + constructorCount + nestedTypesCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_08() {
		String typeName = StaticNestedTypes.Outer.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_08(type);
	}

	@Test
	public void testMemberCount_08_01() {
		String typeName = StaticNestedTypes.Outer.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_08(type);
	}

	@Test
	public void testMemberCount_08_02() {
		String typeName = StaticNestedTypes.Outer.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_08(JvmGenericType type) {
		int methodCount = StaticNestedTypes.Outer.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = StaticNestedTypes.Outer.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int nestedTypesCount = StaticNestedTypes.Outer.class.getClasses().length;
		assertEquals(1, nestedTypesCount);
		assertEquals(methodCount + constructorCount + nestedTypesCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_09() {
		String typeName = StaticNestedTypes.Outer.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_09(type);
	}

	@Test
	public void testMemberCount_09_01() {
		String typeName = StaticNestedTypes.Outer.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_09(type);
	}

	@Test
	public void testMemberCount_09_02() {
		String typeName = StaticNestedTypes.Outer.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_09(JvmGenericType type) {
		int methodCount = StaticNestedTypes.Outer.Inner.class.getDeclaredMethods().length;
		assertEquals(1, methodCount);
		int constructorCount = StaticNestedTypes.Outer.Inner.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		assertEquals(methodCount + constructorCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_10() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int methodCount = ParameterizedTypes.class.getDeclaredMethods().length;
		assertEquals(7, methodCount);
		int constructorCount = ParameterizedTypes.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int nestedTypesCount = ParameterizedTypes.class.getClasses().length;
		assertEquals(1, nestedTypesCount);
		assertEquals(methodCount + constructorCount + nestedTypesCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_11() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_11(type);
	}

	@Test
	public void testMemberCount_11_01() {
		String typeName = ParameterizedTypes.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_11(type);
	}

	@Test
	public void testMemberCount_11_02() {
		String typeName = ParameterizedTypes.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_11(JvmGenericType type) {
		int methodCount = ParameterizedTypes.Inner.class.getDeclaredMethods().length;
		assertEquals(7, methodCount);
		int fieldCount = ParameterizedTypes.Inner.class.getDeclaredFields().length;
		assertEquals(1, fieldCount);
		assertEquals(methodCount + fieldCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_12() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int constructorCount = Fields.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int fieldCount = Fields.class.getDeclaredFields().length;
		assertEquals(7, fieldCount);
		int nestedCount = Fields.class.getDeclaredClasses().length;
		assertEquals(1, nestedCount);
		assertEquals(nestedCount + constructorCount + fieldCount, type.getMembers().size());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testMemberCount_13() {
		String typeName = Fields.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestMemberCount_13(type);
	}

	@Test
	public void testMemberCount_13_01() {
		String typeName = Fields.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestMemberCount_13(type);
	}

	@Test
	public void testMemberCount_13_02() {
		String typeName = Fields.Inner.class.getName().replace('$', '.');
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	private void doTestMemberCount_13(JvmGenericType type) {
		int constructorCount = Fields.Inner.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount); // default constructor
		int fieldCount = Fields.Inner.class.getDeclaredFields().length;
		assertEquals(1, fieldCount);
		assertEquals(constructorCount + fieldCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_14() {
		String typeName = TestAnnotation.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		int methodCount = TestAnnotation.class.getDeclaredMethods().length;
		assertEquals(15, methodCount);
		int innerTypesCount = TestAnnotation.class.getDeclaredClasses().length;
		assertEquals(2, innerTypesCount);
		int fieldCount = TestAnnotation.class.getDeclaredFields().length;
		assertEquals(1, fieldCount);
		assertEquals(fieldCount + methodCount + innerTypesCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_15() {
		String typeName = EmptyAbstractClass.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		int constructorCount = EmptyAbstractClass.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount);
		assertEquals(1, type.getMembers().size());
	}

	@Test
	public void testMemberCount_16() {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		int innerTypesCount = TestEnum.class.getDeclaredClasses().length;
		// Nested
		assertEquals(1, innerTypesCount);
		int methodCount = TestEnum.class.getDeclaredMethods().length;
		// TestEnum.values + TestEnum.valueOf
		assertEquals(2, methodCount);
		int constructorCount = TestEnum.class.getDeclaredConstructors().length;
		// TestEnum(String, int, String), TestEnum(String, int, String, EnumType)
		assertEquals(1, constructorCount);
		int fieldCount = TestEnum.class.getDeclaredFields().length;
		// FirstValue, SecondValue, string, ENUM$VALUES
		assertEquals(Arrays.toString(TestEnum.class.getDeclaredFields()), 4, fieldCount);
		// ENUM$VALUES is synthetic
		// TestEnum(String, String, EnumType) is synthetic
		assertEquals(type.getMembers().toString(), innerTypesCount + methodCount + constructorCount + fieldCount - 1,
				type.getMembers().size());
	}

	@Test
	public void testMemberCount_17() {
		String typeName = TestAnnotationWithDefaults.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		int methodCount = TestAnnotationWithDefaults.class.getDeclaredMethods().length;
		assertEquals(15, methodCount);
		int innerTypesCount = TestAnnotationWithDefaults.class.getDeclaredClasses().length;
		assertEquals(1, innerTypesCount);
		assertEquals(methodCount + innerTypesCount, type.getMembers().size());
	}

	@Test
	public void testMemberCount_18() {
		String typeName = RawIterable.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		int constructorCount = RawIterable.class.getDeclaredConstructors().length;
		assertEquals(1, constructorCount);
		assertEquals(constructorCount, type.getMembers().size());
	}

	@Test
	public void test_twoListParamsNoResult_01() {
		JvmOperation twoListParamsNoResult = getMethodFromParameterizedMethods(
				"twoListParamsNoResult(java.util.List,java.util.List)");
		assertEquals(2, twoListParamsNoResult.getParameters().size());
	}

	@Test
	public void test_twoListParamsNoResult_02() {
		JvmOperation twoListParamsNoResult = getMethodFromParameterizedMethods(
				"twoListParamsNoResult(java.util.List,java.util.List)");
		JvmFormalParameter firstParam = twoListParamsNoResult.getParameters().get(0);
		JvmTypeReference paramType = firstParam.getParameterType();
		assertNotNull(paramType);
		assertFalse(paramType.getType().eIsProxy());
		assertEquals("java.util.List<T>", paramType.getIdentifier());
		assertTrue(paramType instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) paramType;
		JvmType rawType = parameterized.getType();
		assertFalse(rawType.eIsProxy());
		assertEquals("java.util.List", rawType.getIdentifier());
	}

	@Test
	public void test_twoListParamsNoResult_03() {
		JvmOperation twoListParamsNoResult = getMethodFromParameterizedMethods(
				"twoListParamsNoResult(java.util.List,java.util.List)");
		JvmFormalParameter firstParam = twoListParamsNoResult.getParameters().get(0);
		JvmTypeReference paramType = firstParam.getParameterType();
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) paramType;
		assertEquals(1, parameterized.getArguments().size());
		JvmTypeReference typeParameter = parameterized.getArguments().get(0);
		JvmType referencedType = typeParameter.getType();
		assertFalse(referencedType.eIsProxy());
		assertTrue(referencedType instanceof JvmTypeParameter);
		JvmTypeParameter typeVar = (JvmTypeParameter) referencedType;
		assertEquals("T", typeVar.getName());
		assertSame(twoListParamsNoResult, typeVar.getDeclarator());
	}

	@Test
	public void test_twoListParamsListResult_01() {
		JvmOperation twoListParamsListResult = getMethodFromParameterizedMethods(
				"twoListParamsListResult(java.util.List,java.util.List)");
		assertEquals(2, twoListParamsListResult.getParameters().size());
	}

	@Test
	public void test_twoListParamsListResult_02() {
		JvmOperation twoListParamsListResult = getMethodFromParameterizedMethods(
				"twoListParamsListResult(java.util.List,java.util.List)");
		JvmTypeReference returnType = twoListParamsListResult.getReturnType();
		assertNotNull(returnType);
		assertEquals("java.util.List<T>", returnType.getIdentifier());
		assertTrue(returnType instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) returnType;
		JvmType rawType = parameterized.getType();
		assertFalse(rawType.eIsProxy());
		assertEquals("java.util.List", rawType.getIdentifier());
	}

	@Test
	public void test_twoListParamsListResult_03() {
		JvmOperation twoListParamsListResult = getMethodFromParameterizedMethods(
				"twoListParamsListResult(java.util.List,java.util.List)");
		JvmTypeReference returnType = twoListParamsListResult.getReturnType();
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) returnType;
		assertEquals(1, parameterized.getArguments().size());
		JvmTypeReference typeParameter = parameterized.getArguments().get(0);
		JvmType referencedType = typeParameter.getType();
		assertFalse(referencedType.eIsProxy());
		assertTrue(referencedType instanceof JvmTypeParameter);
		JvmTypeParameter typeVar = (JvmTypeParameter) referencedType;
		assertEquals("T", typeVar.getName());
		assertSame(twoListParamsListResult, typeVar.getDeclarator());
	}

	@Test
	public void test_twoListWildcardsNoResult_01() {
		JvmOperation twoListWildcardsNoResult = getMethodFromParameterizedMethods(
				"twoListWildcardsNoResult(java.util.List,java.util.List)");
		assertEquals(2, twoListWildcardsNoResult.getParameters().size());
	}

	@Test
	public void test_twoListWildcardsNoResult_02() {
		JvmOperation twoListWildcardsNoResult = getMethodFromParameterizedMethods(
				"twoListWildcardsNoResult(java.util.List,java.util.List)");
		JvmFormalParameter firstParam = twoListWildcardsNoResult.getParameters().get(0);
		JvmTypeReference paramType = firstParam.getParameterType();
		assertNotNull(paramType);
		assertEquals("java.util.List<? extends java.lang.Object>", paramType.getIdentifier());
		assertTrue(paramType instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) paramType;
		JvmType rawType = parameterized.getType();
		assertFalse(rawType.eIsProxy());
		assertEquals("java.util.List", rawType.getIdentifier());
	}

	@Test
	public void test_twoListWildcardsNoResult_03() {
		JvmOperation twoListWildcardsNoResult = getMethodFromParameterizedMethods(
				"twoListWildcardsNoResult(java.util.List,java.util.List)");
		JvmFormalParameter firstParam = twoListWildcardsNoResult.getParameters().get(0);
		JvmTypeReference paramType = firstParam.getParameterType();
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) paramType;
		assertEquals(1, parameterized.getArguments().size());
		JvmTypeReference typeParameter = parameterized.getArguments().get(0);
		assertTrue(typeParameter instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcard = (JvmWildcardTypeReference) typeParameter;
		assertEquals(1, wildcard.getConstraints().size());
		assertTrue(wildcard.getConstraints().get(0) instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) wildcard.getConstraints().get(0);
		assertNotNull(upperBound.getTypeReference());
		JvmType upperBoundType = upperBound.getTypeReference().getType();
		assertFalse(upperBoundType.eIsProxy());
		assertEquals("java.lang.Object", upperBoundType.getIdentifier());
	}

	@Test
	public void test_twoListWildcardsListResult_01() {
		JvmOperation twoListWildcardsListResult = getMethodFromParameterizedMethods(
				"twoListWildcardsListResult(java.util.List,java.util.List)");
		assertEquals(2, twoListWildcardsListResult.getParameters().size());
	}

	@Test
	public void test_twoListWildcardsListResult_02() {
		JvmOperation twoListWildcardsListResult = getMethodFromParameterizedMethods(
				"twoListWildcardsListResult(java.util.List,java.util.List)");
		JvmTypeReference returnType = twoListWildcardsListResult.getReturnType();
		assertNotNull(returnType);
		assertFalse(returnType.eIsProxy());
		assertEquals("java.util.List<? extends java.lang.Object>", returnType.getIdentifier());
		assertTrue(returnType instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) returnType;
		JvmType rawType = parameterized.getType();
		assertFalse(rawType.eIsProxy());
		assertEquals("java.util.List", rawType.getIdentifier());
	}

	@Test
	public void test_twoListWildcardsListResult_03() {
		JvmOperation twoListWildcardsListResult = getMethodFromParameterizedMethods(
				"twoListWildcardsListResult(java.util.List,java.util.List)");
		JvmTypeReference returnType = twoListWildcardsListResult.getReturnType();
		JvmParameterizedTypeReference parameterized = (JvmParameterizedTypeReference) returnType;
		assertEquals(1, parameterized.getArguments().size());
		JvmTypeReference typeParameter = parameterized.getArguments().get(0);
		assertTrue(typeParameter instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcard = (JvmWildcardTypeReference) typeParameter;
		assertEquals(1, wildcard.getConstraints().size());
		assertTrue(wildcard.getConstraints().get(0) instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) wildcard.getConstraints().get(0);
		assertNotNull(upperBound.getTypeReference());
		JvmType upperBoundType = upperBound.getTypeReference().getType();
		assertFalse(upperBoundType.eIsProxy());
		assertEquals("java.lang.Object", upperBoundType.getIdentifier());
	}

	@Test
	public void test_arrayWildcard_01() {
		JvmOperation arrayWildcard = getMethodFromParameterizedMethods("arrayWildcard(java.util.List[])");
		assertEquals(1, arrayWildcard.getParameters().size());
	}

	@Test
	public void test_arrayWildcard_02() {
		JvmOperation arrayWildcard = getMethodFromParameterizedMethods("arrayWildcard(java.util.List[])");
		JvmTypeReference paramType = arrayWildcard.getParameters().get(0).getParameterType();
		assertEquals("java.util.List<? extends java.lang.Object>[]", paramType.getIdentifier());
		assertTrue(paramType.getType() instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType.getType();
		assertTrue(arrayType.getComponentType() instanceof JvmDeclaredType);
		assertTrue(paramType instanceof JvmGenericArrayTypeReference);
		assertTrue(
				((JvmGenericArrayTypeReference) paramType).getComponentType() instanceof JvmParameterizedTypeReference);
	}

	@Test
	public void test_nestedArrayWildcard_01() {
		JvmOperation nestedArrayWildcard = getMethodFromParameterizedMethods("nestedArrayWildcard(java.util.List[][])");
		assertEquals(1, nestedArrayWildcard.getParameters().size());
	}

	@Test
	public void test_nestedArrayWildcard_02() {
		JvmOperation nestedArrayWildcard = getMethodFromParameterizedMethods("nestedArrayWildcard(java.util.List[][])");
		JvmTypeReference paramType = nestedArrayWildcard.getParameters().get(0).getParameterType();
		assertTrue(paramType.getType() instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType.getType();
		assertTrue(arrayType.getComponentType() instanceof JvmArrayType);
		arrayType = (JvmArrayType) arrayType.getComponentType();
		assertTrue(arrayType.getComponentType() instanceof JvmDeclaredType);

		assertTrue(paramType instanceof JvmGenericArrayTypeReference);
		JvmGenericArrayTypeReference paramTypeAsArray = (JvmGenericArrayTypeReference) paramType;
		assertTrue(paramTypeAsArray.getComponentType() instanceof JvmGenericArrayTypeReference);
		paramTypeAsArray = (JvmGenericArrayTypeReference) paramTypeAsArray.getComponentType();
		assertTrue(paramTypeAsArray.getComponentType() instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference listWithWildcard = (JvmParameterizedTypeReference) paramTypeAsArray
				.getComponentType();
		assertTrue(listWithWildcard.getArguments().get(0) instanceof JvmWildcardTypeReference);
	}

	@Test
	public void test_arrayParameterized_01() {
		JvmOperation arrayParameterized = getMethodFromParameterizedMethods("arrayParameterized(java.util.List[])");
		assertEquals(1, arrayParameterized.getParameters().size());
	}

	@Test
	public void test_arrayParameterized_02() {
		JvmOperation arrayParameterized = getMethodFromParameterizedMethods("arrayParameterized(java.util.List[])");
		JvmTypeReference paramType = arrayParameterized.getParameters().get(0).getParameterType();
		assertEquals("java.util.List<T>[]", paramType.getIdentifier());
		assertTrue(paramType.getType() instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType.getType();
		assertTrue(arrayType.getComponentType() instanceof JvmDeclaredType);
		assertTrue(paramType instanceof JvmGenericArrayTypeReference);
		assertTrue(
				((JvmGenericArrayTypeReference) paramType).getComponentType() instanceof JvmParameterizedTypeReference);
	}

	@Test
	public void test_nestedArrayParameterized_01() {
		JvmOperation nestedArrayParameterized = getMethodFromParameterizedMethods(
				"nestedArrayParameterized(java.util.List[][])");
		assertEquals(1, nestedArrayParameterized.getParameters().size());
	}

	@Test
	public void test_nestedArrayParameterized_02() {
		JvmOperation nestedArrayParameterized = getMethodFromParameterizedMethods(
				"nestedArrayParameterized(java.util.List[][])");
		JvmTypeReference paramType = nestedArrayParameterized.getParameters().get(0).getParameterType();
		assertTrue(paramType.getType() instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType.getType();
		assertTrue(arrayType.getComponentType() instanceof JvmArrayType);
		arrayType = (JvmArrayType) arrayType.getComponentType();
		assertTrue(arrayType.getComponentType() instanceof JvmDeclaredType);

		assertTrue(paramType instanceof JvmGenericArrayTypeReference);
		JvmGenericArrayTypeReference paramTypeAsArray = (JvmGenericArrayTypeReference) paramType;
		assertTrue(paramTypeAsArray.getComponentType() instanceof JvmGenericArrayTypeReference);
		paramTypeAsArray = (JvmGenericArrayTypeReference) paramTypeAsArray.getComponentType();
		assertTrue(paramTypeAsArray.getComponentType() instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference listWithT = (JvmParameterizedTypeReference) paramTypeAsArray.getComponentType();
		assertTrue(listWithT.getArguments().get(0) instanceof JvmParameterizedTypeReference);
		assertEquals("T", listWithT.getArguments().get(0).getType().getSimpleName());
	}

	@Test
	public void test_arrayVariable_01() {
		JvmOperation arrayVariable = getMethodFromParameterizedMethods("arrayVariable(T[])");
		assertEquals(1, arrayVariable.getParameters().size());
	}

	@Test
	public void test_arrayVariable_02() {
		JvmOperation arrayVariable = getMethodFromParameterizedMethods("arrayVariable(T[])");
		JvmType paramType = arrayVariable.getParameters().get(0).getParameterType().getType();
		assertEquals("T[]", paramType.getIdentifier());
		assertTrue(paramType instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType;
		assertTrue(arrayType.getComponentType() instanceof JvmTypeParameter);
	}

	@Test
	public void test_nestedArrayVariable_01() {
		JvmOperation nestedArrayVariable = getMethodFromParameterizedMethods("nestedArrayVariable(T[][])");
		assertEquals(1, nestedArrayVariable.getParameters().size());
	}

	@Test
	public void test_nestedArrayVariable_02() {
		JvmOperation nestedArrayVariable = getMethodFromParameterizedMethods("nestedArrayVariable(T[][])");
		JvmType paramType = nestedArrayVariable.getParameters().get(0).getParameterType().getType();
		assertTrue(paramType instanceof JvmArrayType);
		JvmArrayType arrayType = (JvmArrayType) paramType;
		assertTrue(arrayType.getComponentType() instanceof JvmArrayType);
		arrayType = (JvmArrayType) arrayType.getComponentType();
		assertTrue(arrayType.getComponentType() instanceof JvmTypeParameter);
	}

	@Test
	public void test_nestedInterface_Outer_01() {
		String typeName = NestedInterfaces.class.getName() + "$Outer";
		String expectedSuffix = NestedInterfaces.class.getSimpleName() + "$Outer";
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(NestedInterfaces.class.getName(), outerType.getIdentifier());
	}

	@Test
	public void test_nestedInterface_Outer_02() {
		String typeName = NestedInterfaces.class.getName() + "$Outer";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmConstructor> constructors = Lists.newArrayList(type.getDeclaredConstructors());
		assertEquals(0, constructors.size());
		assertEquals(JvmVisibility.DEFAULT, type.getVisibility());
		assertTrue(type.isStatic());
		assertTrue(type.isAbstract());
	}

	@Test
	public void test_nestedInterface_Outer_Inner_01() {
		String typeName = NestedInterfaces.class.getName() + "$Outer$Inner";
		String expectedSuffix = NestedInterfaces.class.getSimpleName() + "$Outer$Inner";
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(NestedInterfaces.class.getName() + "$Outer", outerType.getIdentifier());
	}

	@Test
	public void test_nestedInterface_Outer_Inner_02() {
		String typeName = NestedInterfaces.class.getName() + "$Outer$Inner";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmConstructor> constructors = Lists.newArrayList(type.getDeclaredConstructors());
		assertEquals(0, constructors.size());
		assertEquals(JvmVisibility.PUBLIC, type.getVisibility());
		assertTrue(type.isStatic());
		assertTrue(type.isAbstract());
	}

	@Test
	public void test_nestedTypes_Outer_01() {
		String typeName = NestedTypes.Outer.class.getName();
		String expectedSuffix = NestedTypes.class.getSimpleName() + "$" + NestedTypes.Outer.class.getSimpleName();
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertFalse(type.isStatic());
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(NestedTypes.class.getName(), outerType.getIdentifier());
	}

	@Test
	public void test_nestedTypes_Outer_02() {
		String typeName = NestedTypes.Outer.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmConstructor> constructors = Lists.newArrayList(type.getDeclaredConstructors());
		assertEquals(1, constructors.size());
		JvmConstructor parameterlessConstructor = constructors.get(0);
		assertEquals(0, parameterlessConstructor.getParameters().size());
	}

	@Test
	public void test_nestedTypes_Outer_Inner_01() {
		String typeName = NestedTypes.Outer.Inner.class.getName();
		String expectedSuffix = NestedTypes.class.getSimpleName() + "$" + NestedTypes.Outer.class.getSimpleName() + "$"
				+ NestedTypes.Outer.Inner.class.getSimpleName();
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(NestedTypes.Outer.class.getName(), outerType.getIdentifier());
	}

	@Test
	public void test_nestedTypes_Outer_Inner_02() {
		String typeName = NestedTypes.Outer.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmConstructor> constructors = Lists.newArrayList(type.getDeclaredConstructors());
		assertEquals(1, constructors.size());
		JvmConstructor parameterlessConstructor = constructors.get(0);
		assertEquals(1, parameterlessConstructor.getParameters().size());
	}

	@Test
	public void test_staticNestedTypes_method() {
		String typeName = StaticNestedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, StaticNestedTypes.class, "method()");
		assertEquals("boolean", operation.getReturnType().getIdentifier());
	}

	@Test
	public void test_staticNestedTypes_constructor() {
		String typeName = Bug347739.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmMember> members = type.getMembers();
		for (JvmMember member : members) {
			if (member instanceof JvmGenericType) {
				if ("StackItem".equals(member.getSimpleName())) {
					JvmGenericType stackItem = (JvmGenericType) member;
					Iterable<JvmConstructor> constructors = stackItem.getDeclaredConstructors();
					for (JvmConstructor constructor : constructors) {
						assertEquals(2, constructor.getParameters().size());
					}
					return;
				}
			}
		}
		fail("could not find inner class");
	}

	@Test
	public void test_staticNestedTypes_Outer() {
		String typeName = StaticNestedTypes.Outer.class.getName();
		String expectedSuffix = StaticNestedTypes.class.getSimpleName() + "$"
				+ StaticNestedTypes.Outer.class.getSimpleName();
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type.isStatic());
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(StaticNestedTypes.class.getName(), outerType.getIdentifier());
	}

	@Test
	public void test_staticNestedTypes_Outer_method() {
		String typeName = StaticNestedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, StaticNestedTypes.Outer.class, "method()");
		assertEquals("int", operation.getReturnType().getIdentifier());
	}

	@Test
	public void test_staticNestedTypes_Outer_Inner() {
		String typeName = StaticNestedTypes.Outer.Inner.class.getName();
		String expectedSuffix = StaticNestedTypes.class.getSimpleName() + "$"
				+ StaticNestedTypes.Outer.class.getSimpleName() + "$"
				+ StaticNestedTypes.Outer.Inner.class.getSimpleName();
		assertTrue(typeName + " endsWith " + expectedSuffix, typeName.endsWith(expectedSuffix));
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(typeName, type.getIdentifier());
		JvmType outerType = (JvmType) type.eContainer();
		assertEquals(StaticNestedTypes.Outer.class.getName(), outerType.getIdentifier());
	}

	@Test
	public void test_staticNestedTypes_Outer_Inner_method() {
		String typeName = StaticNestedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, StaticNestedTypes.Outer.Inner.class, "method()");
		assertEquals("void", operation.getReturnType().getIdentifier());
	}

	protected JvmOperation getMethodFromType(EObject context, Class<?> type, String method) {
		String methodName = type.getName() + "." + method;
		assertNotNull(context);
		JvmOperation result = (JvmOperation) context.eResource().getEObject(methodName);
		assertNotNull(methodName, result);
		return result;
	}

	protected JvmConstructor getConstructorFromType(EObject context, Class<?> type, String constructor) {
		String methodName = type.getName() + "." + constructor;
		assertNotNull(context);
		JvmConstructor result = (JvmConstructor) context.eResource().getEObject(methodName);
		assertNotNull(methodName, result);
		return result;
	}

	@Test
	public void test_ParameterizedTypes_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(5, type.getTypeParameters().size());
	}

	@Test
	public void test_ParameterizedTypes_02() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodS = getMethodFromType(type, ParameterizedTypes.class, "methodS(S)");
		JvmTypeReference listS = methodS.getReturnType();
		assertFalse(listS.toString(), listS.eIsProxy());
		assertEquals("java.util.List<? extends S>", listS.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listS;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertTrue(typeArgument instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcardTypeArgument = (JvmWildcardTypeReference) typeArgument;
		assertEquals("? extends S", wildcardTypeArgument.getIdentifier());
		assertEquals(1, wildcardTypeArgument.getConstraints().size());
		JvmUpperBound upperBound = (JvmUpperBound) wildcardTypeArgument.getConstraints().get(0);
		JvmTypeParameter s = type.getTypeParameters().get(0);
		assertSame(s, upperBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_03() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodT = getMethodFromType(type, ParameterizedTypes.class, "methodT(T)");
		JvmTypeReference listS = methodT.getReturnType();
		assertFalse(listS.toString(), listS.eIsProxy());
		assertEquals("java.util.List<? extends java.lang.Object & super T>", listS.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listS;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertTrue(typeArgument instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcardTypeArgument = (JvmWildcardTypeReference) typeArgument;
		assertEquals("? extends java.lang.Object & super T", wildcardTypeArgument.getIdentifier());
		assertEquals(2, wildcardTypeArgument.getConstraints().size());
		JvmUpperBound uperBound = (JvmUpperBound) wildcardTypeArgument.getConstraints().get(0);
		assertEquals("java.lang.Object", uperBound.getTypeReference().getIdentifier());
		JvmLowerBound lowerBound = (JvmLowerBound) wildcardTypeArgument.getConstraints().get(1);
		JvmTypeParameter t = type.getTypeParameters().get(1);
		assertSame(t, lowerBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_04() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodY = getMethodFromType(type, ParameterizedTypes.class, "methodY(Y)");
		assertEquals(1, methodY.getParameters().size());
		JvmType parameterType = methodY.getParameters().get(0).getParameterType().getType();
		assertFalse(parameterType.eIsProxy());
		assertEquals("Y", parameterType.getIdentifier());
		assertTrue(parameterType instanceof JvmTypeParameter);
		assertSame(methodY, ((JvmTypeParameter) parameterType).getDeclarator());
		JvmTypeParameter y = (JvmTypeParameter) parameterType;
		assertEquals(1, y.getConstraints().size());
		JvmUpperBound upperBound = (JvmUpperBound) y.getConstraints().get(0);
		JvmTypeParameter t = type.getTypeParameters().get(1);
		assertSame(t, upperBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_05() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodZ = getMethodFromType(type, ParameterizedTypes.class, "methodZ(java.util.List)");
		assertEquals(1, methodZ.getParameters().size());
		assertEquals(1, methodZ.getTypeParameters().size());
		assertEquals("Z", methodZ.getTypeParameters().get(0).getIdentifier());
		JvmTypeReference listZ = methodZ.getReturnType();
		assertFalse(listZ.toString(), listZ.getType().eIsProxy());
		assertEquals("java.util.List<? extends java.lang.Object & super Z>", listZ.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listZ;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertTrue(typeArgument instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcardTypeArgument = (JvmWildcardTypeReference) typeArgument;
		assertEquals("? extends java.lang.Object & super Z", wildcardTypeArgument.getIdentifier());
		assertEquals(2, wildcardTypeArgument.getConstraints().size());
		JvmUpperBound upperBound = (JvmUpperBound) wildcardTypeArgument.getConstraints().get(0);
		assertEquals("java.lang.Object", upperBound.getTypeReference().getIdentifier());
		JvmLowerBound lowerBound = (JvmLowerBound) wildcardTypeArgument.getConstraints().get(1);
		assertEquals("Z", lowerBound.getTypeReference().getIdentifier());
		JvmTypeParameter z = methodZ.getTypeParameters().get(0);
		assertSame(z, lowerBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_06() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodMap = getMethodFromType(type, ParameterizedTypes.class, "methodMap(java.util.Map)");
		assertEquals(1, methodMap.getParameters().size());
		assertEquals(1, methodMap.getTypeParameters().size());
		assertEquals("Z", methodMap.getTypeParameters().get(0).getIdentifier());
		JvmType z = methodMap.getReturnType().getType();
		assertSame(methodMap.getTypeParameters().get(0), z);
		JvmTypeReference mapType = methodMap.getParameters().get(0).getParameterType();
		assertEquals("java.util.Map<? extends java.lang.Object & super Z, ? extends S>", mapType.getIdentifier());
		JvmParameterizedTypeReference parameterizedMapType = (JvmParameterizedTypeReference) mapType;
		assertEquals(2, parameterizedMapType.getArguments().size());
		JvmWildcardTypeReference extendsS = (JvmWildcardTypeReference) parameterizedMapType.getArguments().get(1);
		assertEquals(1, extendsS.getConstraints().size());
		JvmType s = type.getTypeParameters().get(0);
		assertSame(s, extendsS.getConstraints().get(0).getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_S_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeVariable = type.getTypeParameters().get(0);
		assertEquals("S", typeVariable.getIdentifier());
		assertSame(type, typeVariable.getDeclarator());
		assertEquals(1, typeVariable.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeVariable.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertEquals("java.lang.Object", upperBound.getTypeReference().getIdentifier());
	}

	@Test
	public void test_ParameterizedTypes_T_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeVariable = type.getTypeParameters().get(1);
		assertEquals("T", typeVariable.getIdentifier());
		assertSame(type, typeVariable.getDeclarator());
		assertEquals(1, typeVariable.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeVariable.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertFalse(upperBound.getTypeReference().toString(), upperBound.getTypeReference().eIsProxy());
		assertEquals("S", upperBound.getTypeReference().getIdentifier());
		JvmTypeParameter s = type.getTypeParameters().get(0);
		assertSame(s, upperBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_U_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeVariable = type.getTypeParameters().get(2);
		assertEquals("U", typeVariable.getIdentifier());
		assertSame(type, typeVariable.getDeclarator());
		assertEquals(1, typeVariable.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeVariable.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertFalse(upperBound.getTypeReference().toString(), upperBound.getTypeReference().eIsProxy());
		assertEquals("java.util.List<S>", upperBound.getTypeReference().getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) upperBound.getTypeReference();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		JvmTypeParameter s = type.getTypeParameters().get(0);
		assertSame(s, typeArgument.getType());
	}

	@Test
	public void test_ParameterizedTypes_V_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterV = type.getTypeParameters().get(3);
		assertEquals("V", typeParameterV.getIdentifier());
		assertSame(type, typeParameterV.getDeclarator());
		assertEquals(1, typeParameterV.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeParameterV.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertFalse(upperBound.getTypeReference().toString(), upperBound.getTypeReference().eIsProxy());
		assertEquals("java.util.List<java.util.List<? extends V>>", upperBound.getTypeReference().getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) upperBound.getTypeReference();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertEquals("java.util.List<? extends V>", typeArgument.getIdentifier());
		JvmParameterizedTypeReference nestedListType = (JvmParameterizedTypeReference) typeArgument;
		assertEquals(1, nestedListType.getArguments().size());
		JvmTypeReference nestedArgument = nestedListType.getArguments().get(0);
		assertTrue(nestedArgument instanceof JvmWildcardTypeReference);
		assertEquals(1, ((JvmWildcardTypeReference) nestedArgument).getConstraints().size());
		JvmUpperBound nestedUpperBound = (JvmUpperBound) ((JvmWildcardTypeReference) nestedArgument).getConstraints()
				.get(0);
		assertSame(typeParameterV, nestedUpperBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_W_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterW = type.getTypeParameters().get(4);
		assertEquals("W", typeParameterW.getIdentifier());
		assertSame(type, typeParameterW.getDeclarator());
		assertEquals(2, typeParameterW.getConstraints().size());
		JvmTypeConstraint firstTypeConstraint = typeParameterW.getConstraints().get(0);
		assertTrue(firstTypeConstraint instanceof JvmUpperBound);
		JvmUpperBound firstUpperBound = (JvmUpperBound) firstTypeConstraint;
		assertNotNull(firstUpperBound.getTypeReference());
		assertFalse(firstUpperBound.getTypeReference().toString(), firstUpperBound.getTypeReference().eIsProxy());
		assertEquals("java.lang.Comparable<S>", firstUpperBound.getTypeReference().getIdentifier());
		JvmParameterizedTypeReference comparableType = (JvmParameterizedTypeReference) firstUpperBound
				.getTypeReference();
		assertEquals(1, comparableType.getArguments().size());
		JvmTypeReference typeArgument = comparableType.getArguments().get(0);
		assertEquals("S", typeArgument.getIdentifier());
		JvmTypeParameter s = type.getTypeParameters().get(0);
		assertSame(s, typeArgument.getType());
	}

	@Test
	public void test_ParameterizedTypes_W_02() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterW = type.getTypeParameters().get(4);
		assertEquals("W", typeParameterW.getIdentifier());
		assertSame(type, typeParameterW.getDeclarator());
		assertEquals(2, typeParameterW.getConstraints().size());

		JvmTypeConstraint secondTypeConstraint = typeParameterW.getConstraints().get(1);
		assertTrue(secondTypeConstraint instanceof JvmUpperBound);
		JvmUpperBound secondUpperBound = (JvmUpperBound) secondTypeConstraint;
		assertNotNull(secondUpperBound.getTypeReference());
		assertFalse(secondUpperBound.getTypeReference().toString(), secondUpperBound.getTypeReference().eIsProxy());
		assertEquals("java.io.Serializable", secondUpperBound.getTypeReference().getIdentifier());
	}

	@Test
	public void test_ParameterizedTypes_inner_param_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, ParameterizedTypes.class,
				"plainInner(org.eclipse.xtext.common.types.testSetups.ParameterizedTypes$Inner)");
		JvmTypeReference parameterType = operation.getParameters().get(0).getParameterType();
		assertTrue(parameterType.getIdentifier(), parameterType instanceof JvmInnerTypeReference);
		assertEquals("ParameterizedTypes<S, T, U, V, W>$Inner<W, List<W>, List<W>>", parameterType.getSimpleName());
	}

	@Test
	public void test_ParameterizedTypes_inner_return_01() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, ParameterizedTypes.class,
				"plainInner(org.eclipse.xtext.common.types.testSetups.ParameterizedTypes$Inner)");
		JvmTypeReference returnType = operation.getReturnType();
		assertTrue(returnType.getIdentifier(), returnType instanceof JvmInnerTypeReference);
		assertEquals("ParameterizedTypes<S, T, U, V, W>$Inner<W, List<W>, List<W>>", returnType.getSimpleName());
	}

	@Test
	public void test_ParameterizedTypes_inner_return_02() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, ParameterizedTypes.class, "concreteInner()");
		JvmTypeReference returnType = operation.getReturnType();
		assertTrue(returnType.getIdentifier(), returnType instanceof JvmInnerTypeReference);
		assertEquals(
				"ParameterizedTypes<String, String, List<String>, V, String>$Inner<String, List<String>, List<String>>",
				returnType.getSimpleName());
	}

	@Test
	public void test_ParameterizedTypes2_inner_return_01() {
		String typeName = ParameterizedTypes2.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, ParameterizedTypes2.class, "plainInner()");
		JvmTypeReference returnType = operation.getReturnType();
		assertTrue(returnType.getIdentifier(), returnType instanceof JvmInnerTypeReference);
		assertEquals("ParameterizedTypes2<P>$Inner<String>", returnType.getSimpleName());
	}

	@Test
	public void test_ParameterizedTypes2_inner_return_02() {
		String typeName = ParameterizedTypes2.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, ParameterizedTypes2.class, "concreteInner()");
		JvmTypeReference returnType = operation.getReturnType();
		assertTrue(returnType.getIdentifier(), returnType instanceof JvmInnerTypeReference);
		assertEquals("ParameterizedTypes2<Number>$Inner<String>", returnType.getSimpleName());
	}

	@Test
	public void test_ParameterizedTypes_Inner_01() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertEquals(3, type.getTypeParameters().size());
	}

	@Test
	public void test_ParameterizedTypes_Inner_02() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodS = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodS()");
		JvmType s = methodS.getReturnType().getType();
		assertFalse(s.toString(), s.eIsProxy());
		assertTrue(s instanceof JvmTypeParameter);
		JvmTypeParameter sParam = (JvmTypeParameter) s;
		assertSame(type, sParam.getDeclarator());
	}

	@Test
	public void test_ParameterizedTypes_Inner_03() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodX = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodX()");
		JvmType x = methodX.getReturnType().getType();
		assertFalse(x.toString(), x.eIsProxy());
		assertTrue(x instanceof JvmTypeParameter);
		JvmTypeParameter xParam = (JvmTypeParameter) x;
		assertSame(type, xParam.getDeclarator());
	}

	@Test
	public void test_ParameterizedTypes_Inner_04() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodT = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodT()");
		JvmTypeReference listT = methodT.getReturnType();
		assertEquals("java.util.List<T>", listT.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listT;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertEquals("T", typeArgument.getIdentifier());
		JvmTypeParameter t = ((JvmTypeParameterDeclarator) type.getDeclaringType()).getTypeParameters().get(1);
		assertSame(t, typeArgument.getType());
	}

	@Test
	public void test_ParameterizedTypes_Inner_05() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodV = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodVArray_01()");
		JvmTypeReference listT = methodV.getReturnType();
		assertEquals("java.util.List<? extends V>[]", listT.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) ((JvmGenericArrayTypeReference) listT)
				.getComponentType();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertTrue(typeArgument instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcardTypeArgument = (JvmWildcardTypeReference) typeArgument;
		assertEquals("? extends V", wildcardTypeArgument.getIdentifier());
		assertEquals(1, wildcardTypeArgument.getConstraints().size());
		JvmUpperBound upperBound = (JvmUpperBound) wildcardTypeArgument.getConstraints().get(0);
		JvmTypeParameter v = type.getTypeParameters().get(3);
		assertSame(v, upperBound.getTypeReference().getType());
	}

	@Test
	public void test_ParameterizedTypes_Inner_06() {
		String typeName = ParameterizedTypes.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodV = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodVArray_02()");
		JvmTypeReference listV = methodV.getReturnType();
		assertEquals("java.util.List<? extends V[]>", listV.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listV;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertTrue(typeArgument instanceof JvmWildcardTypeReference);
		JvmWildcardTypeReference wildcardTypeArgument = (JvmWildcardTypeReference) typeArgument;
		assertEquals("? extends V[]", wildcardTypeArgument.getIdentifier());
		assertEquals(1, wildcardTypeArgument.getConstraints().size());
		JvmUpperBound upperBound = (JvmUpperBound) wildcardTypeArgument.getConstraints().get(0);
		JvmType upperBoundType = upperBound.getTypeReference().getType();
		assertTrue(upperBoundType instanceof JvmArrayType);
		assertTrue(((JvmArrayType) upperBoundType).getComponentType() instanceof JvmTypeParameter);
		JvmTypeParameter v = type.getTypeParameters().get(3);
		assertSame(v, ((JvmArrayType) upperBoundType).getComponentType());
	}

	@Test
	public void test_ParameterizedTypes_Inner_07() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodV = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodZArray_01()");
		JvmTypeReference listZ = methodV.getReturnType();
		assertEquals("java.util.List<Z[][]>", listZ.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) listZ;
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		JvmType argumentType = typeArgument.getType();
		assertTrue(argumentType instanceof JvmArrayType);
		assertTrue(((JvmArrayType) argumentType).getComponentType() instanceof JvmArrayType);
		JvmComponentType componentType = ((JvmArrayType) ((JvmArrayType) argumentType).getComponentType())
				.getComponentType();
		JvmTypeParameter z = type.getTypeParameters().get(2);
		assertSame(z, componentType);
	}

	@Test
	public void test_ParameterizedTypes_Inner_08() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation methodV = getMethodFromType(type, ParameterizedTypes.Inner.class, "methodZArray_02()");
		JvmTypeReference listZ = methodV.getReturnType();
		assertEquals("java.util.List<Z[]>[]", listZ.getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) ((JvmGenericArrayTypeReference) listZ)
				.getComponentType();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		JvmType argumentType = typeArgument.getType();
		assertTrue(argumentType instanceof JvmArrayType);
		JvmComponentType componentType = ((JvmArrayType) argumentType).getComponentType();
		JvmTypeParameter z = type.getTypeParameters().get(2);
		assertSame(z, componentType);
	}

	@Test
	public void test_ParameterizedTypes_Inner_X_01() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterX = type.getTypeParameters().get(0);
		assertEquals("X", typeParameterX.getIdentifier());
		assertSame(type, typeParameterX.getDeclarator());
		assertEquals(1, typeParameterX.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeParameterX.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertEquals("W", upperBound.getTypeReference().getIdentifier());
		JvmType upperBoundType = upperBound.getTypeReference().getType();
		assertTrue(upperBoundType instanceof JvmTypeParameter);
		JvmTypeParameter typeParameterW = (JvmTypeParameter) upperBoundType;
		assertSame(type.getDeclaringType(), typeParameterW.getDeclarator());
	}

	@Test
	public void test_ParameterizedTypes_Inner_Y_01() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterY = type.getTypeParameters().get(1);
		assertEquals("Y", typeParameterY.getIdentifier());
		assertSame(type, typeParameterY.getDeclarator());
		assertEquals(1, typeParameterY.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeParameterY.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertEquals("java.util.List<X>", upperBound.getTypeReference().getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) upperBound.getTypeReference();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertEquals("X", typeArgument.getIdentifier());
		JvmTypeParameter x = type.getTypeParameters().get(0);
		assertSame(x, typeArgument.getType());
	}

	@Test
	public void test_ParameterizedTypes_Inner_Z_01() {
		String typeName = ParameterizedTypes.Inner.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmTypeParameter typeParameterZ = type.getTypeParameters().get(2);
		assertEquals("Z", typeParameterZ.getIdentifier());
		assertSame(type, typeParameterZ.getDeclarator());
		assertEquals(1, typeParameterZ.getConstraints().size());
		JvmTypeConstraint typeConstraint = typeParameterZ.getConstraints().get(0);
		assertTrue(typeConstraint instanceof JvmUpperBound);
		JvmUpperBound upperBound = (JvmUpperBound) typeConstraint;
		assertNotNull(upperBound.getTypeReference());
		assertEquals("java.util.List<W>", upperBound.getTypeReference().getIdentifier());
		JvmParameterizedTypeReference listType = (JvmParameterizedTypeReference) upperBound.getTypeReference();
		assertEquals(1, listType.getArguments().size());
		JvmTypeReference typeArgument = listType.getArguments().get(0);
		assertEquals("W", typeArgument.getIdentifier());
		JvmTypeParameter w = ((JvmTypeParameterDeclarator) type.getDeclaringType()).getTypeParameters().get(4);
		assertSame(w, typeArgument.getType());
	}

	@Test
	public void testFields_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
	}

	protected JvmField getFieldFromType(EObject context, Class<?> type, String field) {
		String fieldName = type.getName() + "." + field;
		assertNotNull(context);
		JvmField result = (JvmField) context.eResource().getEObject(fieldName);
		assertNotNull(fieldName, result);
		return result;
	}

	@Test
	public void testFields_privateT_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "privateT");
		assertSame(type, field.getDeclaringType());
		assertEquals(JvmVisibility.PRIVATE, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertSame(type.getTypeParameters().get(0), fieldType);
	}

	@Test
	public void testFields_defaultListT_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "defaultListT");
		assertSame(type, field.getDeclaringType());
		assertEquals(JvmVisibility.DEFAULT, field.getVisibility());
		JvmTypeReference fieldType = field.getType();
		assertEquals("java.util.List<T>", fieldType.getIdentifier());
		assertTrue(fieldType instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterizedFieldType = (JvmParameterizedTypeReference) fieldType;
		JvmTypeReference refTypeArg = parameterizedFieldType.getArguments().get(0);
		assertSame(type.getTypeParameters().get(0), refTypeArg.getType());
	}

	@Test
	public void testFields_protectedString_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "protectedString");
		assertSame(type, field.getDeclaringType());
		assertEquals(JvmVisibility.PROTECTED, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertEquals("java.lang.String", fieldType.getIdentifier());
	}

	@Test
	public void testFields_protectedStaticString_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "protectedStaticString");
		assertSame(type, field.getDeclaringType());
		assertTrue(field.isStatic());
		assertEquals(JvmVisibility.PROTECTED, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertEquals("java.lang.String", fieldType.getIdentifier());
	}

	@Test
	public void testFields_volatileInt_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "volatileInt");
		assertSame(type, field.getDeclaringType());
		assertTrue(field.isVolatile());
		assertFalse(field.isTransient());
		assertEquals(JvmVisibility.DEFAULT, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertEquals("int", fieldType.getIdentifier());
	}

	@Test
	public void testFields_transientInt_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "transientInt");
		assertSame(type, field.getDeclaringType());
		assertTrue(field.isTransient());
		assertFalse(field.isVolatile());
		assertEquals(JvmVisibility.DEFAULT, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertEquals("int", fieldType.getIdentifier());
	}

	@Test
	public void testFields_publicInt_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.class, "publicInt");
		assertSame(type, field.getDeclaringType());
		assertEquals(JvmVisibility.PUBLIC, field.getVisibility());
		JvmType fieldType = field.getType().getType();
		assertEquals("int", fieldType.getIdentifier());
		assertTrue(field.getType().getType() instanceof JvmPrimitiveType);
	}

	@Test
	public void testFields_innerFields_01() {
		String typeName = Fields.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, Fields.Inner.class, "innerFields");
		JvmGenericType innerType = (JvmGenericType) getTypeProvider().findTypeByName(Fields.Inner.class.getName());
		assertSame(innerType, field.getDeclaringType());
		assertSame(type, innerType.getDeclaringType());
		assertEquals(JvmVisibility.PUBLIC, field.getVisibility());
		JvmTypeReference fieldType = field.getType();
		assertEquals(typeName + "<java.lang.String>", fieldType.getIdentifier());
		assertTrue(field.getType() instanceof JvmParameterizedTypeReference);
		JvmParameterizedTypeReference parameterizedFieldType = (JvmParameterizedTypeReference) fieldType;
		assertSame(type, parameterizedFieldType.getType());
	}

	@Test
	public void testMethods_publicAbstractMethod_01() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, Methods.class, "publicAbstractMethod()");
		assertSame(type, method.getDeclaringType());
		assertTrue(method.isAbstract());
		assertFalse(method.isFinal());
		assertFalse(method.isStatic());
		assertFalse(method.isSynchronized());
		assertFalse(method.isStrictFloatingPoint());
		assertFalse(method.isNative());
		assertEquals(JvmVisibility.PUBLIC, method.getVisibility());
		JvmType methodType = method.getReturnType().getType();
		assertEquals("void", methodType.getIdentifier());
	}

	@Test
	public void testMethods_protectedFinalMethod_01() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, Methods.class, "protectedFinalMethod()");
		assertSame(type, method.getDeclaringType());
		assertFalse(method.isAbstract());
		assertTrue(method.isFinal());
		assertFalse(method.isStatic());
		assertFalse(method.isSynchronized());
		assertFalse(method.isStrictFloatingPoint());
		assertFalse(method.isNative());
		assertEquals(JvmVisibility.PROTECTED, method.getVisibility());
		JvmType methodType = method.getReturnType().getType();
		assertEquals("void", methodType.getIdentifier());
	}

	@Test
	public void testMethods_defaultStaticMethod_01() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, Methods.class, "defaultStaticMethod()");
		assertSame(type, method.getDeclaringType());
		assertFalse(method.isAbstract());
		assertFalse(method.isFinal());
		assertTrue(method.isStatic());
		assertFalse(method.isSynchronized());
		assertFalse(method.isStrictFloatingPoint());
		assertFalse(method.isNative());
		assertEquals(JvmVisibility.DEFAULT, method.getVisibility());
		JvmType methodType = method.getReturnType().getType();
		assertEquals("void", methodType.getIdentifier());
	}

	@Test
	public void testMethods_privateSynchronizedMethod_01() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, Methods.class, "privateSynchronizedMethod()");
		assertSame(type, method.getDeclaringType());
		assertFalse(method.isAbstract());
		assertFalse(method.isFinal());
		assertFalse(method.isStatic());
		assertTrue(method.isSynchronized());
		assertFalse(method.isStrictFloatingPoint());
		assertFalse(method.isNative());
		assertEquals(JvmVisibility.PRIVATE, method.getVisibility());
		JvmType methodType = method.getReturnType().getType();
		assertEquals("void", methodType.getIdentifier());
	}

	@Test
	public void testStrictFpType() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertFalse(type.isStrictFloatingPoint());
		// strictfp on class declarations is not reflected 
	}

	@Test
	public void publicNativeMethod() {
		String typeName = Methods.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, Methods.class, "publicNativeMethod()");
		assertSame(type, method.getDeclaringType());
		assertFalse(method.isAbstract());
		assertFalse(method.isFinal());
		assertFalse(method.isStatic());
		assertFalse(method.isSynchronized());
		assertFalse(method.isStrictFloatingPoint());
		assertTrue(method.isNative());
		assertEquals(JvmVisibility.PUBLIC, method.getVisibility());
		JvmType methodType = method.getReturnType().getType();
		assertEquals("void", methodType.getIdentifier());
	}

	@Test
	public void testHashMap_01() {
		String typeName = HashMap.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testInnerEnumType() throws Exception {
		JvmDeclaredType declaredType = (JvmDeclaredType) getTypeProvider()
				.findTypeByName(TypeWithInnerEnum.class.getName());
		assertEquals(2, declaredType.getMembers().size());
		// default constructor
		assertTrue(Iterables.any(declaredType.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmConstructor)
						&& input.getSimpleName().equals(TypeWithInnerEnum.class.getSimpleName());
			}
		}));
		// inner enum type
		assertTrue(Iterables.any(declaredType.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmEnumerationType)
						&& input.getIdentifier().equals(TypeWithInnerEnum.MyEnum.class.getName())
						&& input.getVisibility() == JvmVisibility.PUBLIC;
			}
		}));
	}

	@Test
	public void testInnerAnnotationType() throws Exception {
		JvmDeclaredType declaredType = (JvmDeclaredType) getTypeProvider()
				.findTypeByName(TypeWithInnerAnnotation.class.getName());
		assertEquals(3, declaredType.getMembers().size());
		// default constructor
		assertTrue(Iterables.any(declaredType.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmConstructor)
						&& input.getSimpleName().equals(TypeWithInnerAnnotation.class.getSimpleName());
			}
		}));
		// inner annotation type
		assertTrue(Iterables.any(declaredType.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmAnnotationType)
						&& input.getIdentifier().equals(TypeWithInnerAnnotation.MyAnnotation.class.getName())
						&& input.getVisibility() == JvmVisibility.PUBLIC;
			}
		}));
		assertTrue(Iterables.any(declaredType.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmOperation) && ((JvmOperation) input).getParameters().size() == 1;
			}
		}));
	}

	@Test
	public void testInnerType_WrappedIterator_01() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider()
				.findTypeByName("org.eclipse.xtext.common.types.testSetups.Bug438740$Coll$Iter");
		doTestInnerType_WrappedIterator_01(wrappedIterator);
	}

	@Test
	public void testInnerType_WrappedIterator_01b() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider()
				.findTypeByName("org.eclipse.xtext.common.types.testSetups.Bug438740.Coll.Iter", false);
		doTestInnerType_WrappedIterator_01(wrappedIterator);
	}

	private void doTestInnerType_WrappedIterator_01(JvmGenericType wrappedIterator) {
		assertEquals(2, Iterables.size(wrappedIterator.getDeclaredConstructors()));
		// default constructor
		assertTrue(Iterables.any(wrappedIterator.getMembers(), new Predicate<JvmMember>() {
			@Override
			public boolean apply(JvmMember input) {
				return (input instanceof JvmConstructor) && input.getSimpleName().equals("Iter")
						&& ((JvmConstructor) input).getParameters().size() == 0;
			}
		}));
		// second constructor
		JvmConstructor secondConstructor = (JvmConstructor) Iterables.find(wrappedIterator.getMembers(),
				new Predicate<JvmMember>() {
					@Override
					public boolean apply(JvmMember input) {
						return (input instanceof JvmConstructor) && input.getSimpleName().equals("Iter")
								&& ((JvmConstructor) input).getParameters().size() == 1;
					}
				});
		assertNotNull(secondConstructor);
		JvmFormalParameter firstParameter = secondConstructor.getParameters().get(0);
		assertEquals("java.util.Iterator<V>", firstParameter.getParameterType().getIdentifier());
	}

	@Test
	public void testInnerType_WrappedIterator_02() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider().findTypeByName(
				"org.eclipse.xtext.common.types.testSetups.NestedParameterizedTypes$WrappedCollection$WrappedIterator");
		doTestInnerType_WrappedIterator_02(wrappedIterator);
	}

	@Test
	public void testInnerType_WrappedIterator_02b() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider().findTypeByName(
				"org.eclipse.xtext.common.types.testSetups.NestedParameterizedTypes.WrappedCollection.WrappedIterator",
				false);
		doTestInnerType_WrappedIterator_02(wrappedIterator);
	}

	protected void doTestInnerType_WrappedIterator_02(JvmGenericType wrappedIterator) {
		assertEquals(3, Iterables.size(wrappedIterator.getDeclaredConstructors()));
		JvmConstructor constructor = (JvmConstructor) Iterables.find(wrappedIterator.getMembers(),
				new Predicate<JvmMember>() {
					@Override
					public boolean apply(JvmMember input) {
						return (input instanceof JvmConstructor) && input.getSimpleName().equals("WrappedIterator")
								&& ((JvmConstructor) input).getParameters().size() == 3;
					}
				});
		assertNotNull(constructor);
		JvmFormalParameter firstParameter = constructor.getParameters().get(0);
		assertEquals(1, firstParameter.getAnnotations().size());
		assertEquals("java.lang.String", firstParameter.getParameterType().getIdentifier());
		assertEquals(TestAnnotationWithDefaults.class.getName(),
				firstParameter.getAnnotations().get(0).getAnnotation().getQualifiedName());
		JvmFormalParameter secondParameter = constructor.getParameters().get(1);
		assertEquals(0, secondParameter.getAnnotations().size());
		assertEquals("int", secondParameter.getParameterType().getIdentifier());
		JvmFormalParameter thirdParameter = constructor.getParameters().get(2);
		assertEquals(1, thirdParameter.getAnnotations().size());
		assertEquals("java.util.Iterator<V>", thirdParameter.getParameterType().getIdentifier());
		assertEquals(TestAnnotation.NestedAnnotation.class.getName(),
				thirdParameter.getAnnotations().get(0).getAnnotation().getQualifiedName());
	}

	@Test
	public void testInnerType_WrappedIterator_03() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider().findTypeByName(
				"org.eclipse.xtext.common.types.testSetups.NestedParameterizedTypes$WrappedCollection$WrappedIterator");
		doTestInnerType_WrappedIterator_03(wrappedIterator);
	}

	@Test
	public void testInnerType_WrappedIterator_03b() throws Exception {
		JvmGenericType wrappedIterator = (JvmGenericType) getTypeProvider().findTypeByName(
				"org.eclipse.xtext.common.types.testSetups.NestedParameterizedTypes.WrappedCollection.WrappedIterator",
				false);
		doTestInnerType_WrappedIterator_03(wrappedIterator);
	}

	protected void doTestInnerType_WrappedIterator_03(JvmGenericType wrappedIterator) {
		assertEquals(3, Iterables.size(wrappedIterator.getDeclaredConstructors()));
		JvmConstructor constructor = (JvmConstructor) Iterables.find(wrappedIterator.getMembers(),
				new Predicate<JvmMember>() {
					@Override
					public boolean apply(JvmMember input) {
						return (input instanceof JvmConstructor) && input.getSimpleName().equals("WrappedIterator")
								&& ((JvmConstructor) input).getParameters().size() == 4;
					}
				});
		assertNotNull(constructor);
		JvmFormalParameter firstParameter = constructor.getParameters().get(0);
		assertEquals(0, firstParameter.getAnnotations().size());
		assertEquals("int", firstParameter.getParameterType().getIdentifier());
		JvmFormalParameter secondParameter = constructor.getParameters().get(1);
		assertEquals(1, secondParameter.getAnnotations().size());
		assertEquals("java.lang.String", secondParameter.getParameterType().getIdentifier());
		assertEquals(TestAnnotationWithDefaults.class.getName(),
				secondParameter.getAnnotations().get(0).getAnnotation().getQualifiedName());
		JvmFormalParameter thirdParameter = constructor.getParameters().get(2);
		assertEquals(0, thirdParameter.getAnnotations().size());
		assertEquals("int", thirdParameter.getParameterType().getIdentifier());
		JvmFormalParameter forthParameter = constructor.getParameters().get(3);
		assertEquals(1, forthParameter.getAnnotations().size());
		assertEquals("java.lang.String", forthParameter.getParameterType().getIdentifier());
		assertEquals(TestAnnotation.NestedAnnotation.class.getName(),
				forthParameter.getAnnotations().get(0).getAnnotation().getQualifiedName());
	}

	@Test
	public void testAnnotationType_01() throws Exception {
		String typeName = TestAnnotation.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmAnnotationType);
		assertTrue(((JvmDeclaredType) type).isAbstract());
		assertFalse(((JvmDeclaredType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testAnnotationType_02() throws Exception {
		String typeName = TestAnnotation.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		assertEquals(Annotation.class.getName(), type.getSuperTypes().get(0).getIdentifier());
	}

	@Test
	public void testNestedAnnotationType_01() throws Exception {
		String typeName = TestAnnotation.NestedAnnotation.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmAnnotationType);
		assertTrue(((JvmDeclaredType) type).isAbstract());
		assertTrue(((JvmDeclaredType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testNestedAnnotationType_02() throws Exception {
		String typeName = TestAnnotation.NestedAnnotation.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		assertEquals(Annotation.class.getName(), type.getSuperTypes().get(0).getIdentifier());
	}

	@Test
	public void testAnnotationType_03() throws Exception {
		String typeName = TestAnnotationWithDefaults.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmAnnotationType);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testAnnotationType_04() throws Exception {
		String typeName = TestAnnotationWithDefaults.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		assertEquals(Annotation.class.getName(), type.getSuperTypes().get(0).getIdentifier());
	}

	@Test
	public void testEnum_01() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmEnumerationType);
		assertTrue(((JvmEnumerationType) type).isFinal());
		assertFalse(((JvmEnumerationType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testEnum_02() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		assertEquals(Enum.class.getName(), type.getSuperTypes().get(0).getType().getIdentifier());
		String superTypeName = Enum.class.getName() + "<" + typeName + ">";
		assertEquals(superTypeName, type.getSuperTypes().get(0).getIdentifier());
	}

	@Test
	public void testEnum_03() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(2, type.getLiterals().size());
		Set<String> expectedLiterals = Sets.newHashSet(TestEnum.FirstValue.name(), TestEnum.SecondValue.name());
		for (JvmEnumerationLiteral literal : type.getLiterals()) {
			assertTrue(expectedLiterals.remove(literal.getSimpleName()));
			assertSame(type, literal.getEnumType());
			assertEquals(JvmVisibility.PUBLIC, literal.getVisibility());
		}
		assertTrue(expectedLiterals.toString(), expectedLiterals.isEmpty());
	}

	@Test
	public void testEnum_04() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		List<JvmMember> members = type.getMembers();
		boolean constructorFound = false;
		for (JvmMember member : members) {
			if (member instanceof JvmConstructor) {
				assertFalse(constructorFound);
				constructorFound = true;
				List<JvmFormalParameter> parameters = ((JvmConstructor) member).getParameters();
				assertEquals(1, parameters.size()); // synthetic parameters are not returned
			}
		}
		assertTrue(constructorFound);
	}

	@Test
	public void testEnum_05() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		JvmOperation valuesOperation = (JvmOperation) Iterables.getOnlyElement(type.findAllFeaturesByName("values"));
		String qualifiedReturnValue = valuesOperation.getReturnType().getQualifiedName();
		assertEquals(typeName + "[]", qualifiedReturnValue);
	}

	@Test
	public void testEnum_06() throws Exception {
		String typeName = TestEnum.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		JvmOperation valuesOperation = (JvmOperation) Iterables.getFirst(type.findAllFeaturesByName("valueOf"), null);
		String qualifiedReturnValue = valuesOperation.getReturnType().getQualifiedName();
		assertEquals(typeName, qualifiedReturnValue);
	}

	@Test
	public void testNestedEnum_01() throws Exception {
		String typeName = TestEnum.Nested.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmEnumerationType);
		assertTrue(((JvmEnumerationType) type).isFinal());
		assertTrue(((JvmEnumerationType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testNestedEnum_02() throws Exception {
		String typeName = TestEnum.Nested.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getSuperTypes().size());
		assertEquals(Enum.class.getName(), type.getSuperTypes().get(0).getType().getIdentifier());
		String superTypeName = Enum.class.getName() + "<" + typeName + ">";
		assertEquals(superTypeName, type.getSuperTypes().get(0).getIdentifier());
	}

	@Test
	public void testNestedEnum_03() throws Exception {
		String typeName = TestEnum.Nested.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		assertEquals(1, type.getLiterals().size());
		Set<String> expectedLiterals = Sets.newHashSet(TestEnum.Nested.SINGLETON.name());
		for (JvmEnumerationLiteral literal : type.getLiterals()) {
			assertTrue(expectedLiterals.remove(literal.getSimpleName()));
			assertSame(type, literal.getEnumType());
			assertEquals(JvmVisibility.PUBLIC, literal.getVisibility());
		}
		assertTrue(expectedLiterals.toString(), expectedLiterals.isEmpty());
	}

	@Test
	public void testNestedEnum_04() throws Exception {
		String typeName = TestEnum.Nested.class.getName();
		JvmEnumerationType type = (JvmEnumerationType) getTypeProvider().findTypeByName(typeName);
		List<JvmMember> members = type.getMembers();
		boolean constructorFound = false;
		for (JvmMember member : members) {
			if (member instanceof JvmConstructor) {
				assertFalse(constructorFound);
				constructorFound = true;
				List<JvmFormalParameter> parameters = ((JvmConstructor) member).getParameters();
				assertEquals(0, parameters.size()); // synthetic parameters are not returned
			}
		}
		assertTrue(constructorFound);
	}

	@Test
	public void testNestedEnum_05() throws Exception {
		String typeName = ClassContainingEnum.InnerEnumWithInt.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		assertTrue(type instanceof JvmEnumerationType);
		assertTrue(((JvmEnumerationType) type).isFinal());
		assertTrue(((JvmEnumerationType) type).isStatic());
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testAnnotations_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationTarget target = (JvmAnnotationTarget) getTypeProvider().findTypeByName(typeName);
		doTestAnnotation_01(target);
	}

	@Test
	public void testAnnotations_01_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName().replace('$', '.');
		JvmAnnotationTarget target = (JvmAnnotationTarget) getTypeProvider().findTypeByName(typeName, false);
		doTestAnnotation_01(target);
	}

	@Test
	public void testAnnotations_01_02() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName().replace('$', '.');
		JvmAnnotationTarget target = (JvmAnnotationTarget) getTypeProvider().findTypeByName(typeName, true);
		assertNull(target);
	}

	private void doTestAnnotation_01(JvmAnnotationTarget target) {
		JvmAnnotationType annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.class.getName());
		assertEquals(1, target.getAnnotations().size());
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		assertSame(annotationType, annotationReference.getAnnotation());
		if (isDefaultValueSupported())
			assertEquals(14, annotationReference.getExplicitValues().size());
		assertEquals(15, annotationReference.getValues().size());
	}

	@Test
	public void testIntAnnotationValue_01() throws Exception {
		JvmIntAnnotationValue value = (JvmIntAnnotationValue) getAnnotationValue("intValue", true);
		assertEquals(1, value.getValues().size());
		Integer integer = value.getValues().get(0);
		assertEquals(34, integer.intValue());
	}

	@Test
	public void testIntAnnotationValue_02() throws Exception {
		JvmIntAnnotationValue value = (JvmIntAnnotationValue) getMethodParameterAnnotationValue("intValue", true);
		assertEquals(1, value.getValues().size());
		Integer integer = value.getValues().get(0);
		assertEquals(34, integer.intValue());
	}

	@Test
	public void testIntAnnotationValue_03() throws Exception {
		JvmIntAnnotationValue value = (JvmIntAnnotationValue) getConstructorParameterAnnotationValue("intValue", true);
		assertEquals(1, value.getValues().size());
		Integer integer = value.getValues().get(0);
		assertEquals(34, integer.intValue());
	}
	
	@Test
	public void testShortAnnotationValue_WithConstant() throws Exception {
		JvmShortAnnotationValue value = (JvmShortAnnotationValue) getAnnotationValue("shortValue2", false);
		assertEquals(1, value.getValues().size());
		Short shortValue = value.getValues().get(0);
		assertEquals(Annotated.CONSTANT, shortValue.shortValue());
	}

	@Test
	public void testBooleanAnnotationValue_01() throws Exception {
		JvmBooleanAnnotationValue value = (JvmBooleanAnnotationValue) getAnnotationValue("booleanValue");
		assertEquals(1, value.getValues().size());
		assertFalse(value.getValues().get(0));
	}

	@Test
	public void testBooleanAnnotationValue_02() throws Exception {
		JvmBooleanAnnotationValue value = (JvmBooleanAnnotationValue) getMethodParameterAnnotationValue("booleanValue");
		assertEquals(1, value.getValues().size());
		assertFalse(value.getValues().get(0));
	}

	@Test
	public void testBooleanAnnotationValue_03() throws Exception {
		JvmBooleanAnnotationValue value = (JvmBooleanAnnotationValue) getConstructorParameterAnnotationValue(
				"booleanValue");
		assertEquals(1, value.getValues().size());
		assertFalse(value.getValues().get(0));
	}

	@Test
	public void testFloatAnnotationValue_01() throws Exception {
		JvmFloatAnnotationValue value = (JvmFloatAnnotationValue) getAnnotationValue("floatValue");
		assertEquals(1, value.getValues().size());
		Float f = value.getValues().get(0);
		assertEquals(0.8f, f.floatValue(), 0.0001);
	}

	@Test
	public void testFloatAnnotationValue_02() throws Exception {
		JvmFloatAnnotationValue value = (JvmFloatAnnotationValue) getMethodParameterAnnotationValue("floatValue");
		assertEquals(1, value.getValues().size());
		Float f = value.getValues().get(0);
		assertEquals(0.8f, f.floatValue(), 0.0001);
	}

	@Test
	public void testFloatAnnotationValue_03() throws Exception {
		JvmFloatAnnotationValue value = (JvmFloatAnnotationValue) getConstructorParameterAnnotationValue("floatValue");
		assertEquals(1, value.getValues().size());
		Float f = value.getValues().get(0);
		assertEquals(0.8f, f.floatValue(), 0.0001);
	}

	@Test
	public void testDoubleAnnotationValue_01() throws Exception {
		JvmDoubleAnnotationValue value = (JvmDoubleAnnotationValue) getAnnotationValue("doubleValue");
		assertEquals(1, value.getValues().size());
		Double d = value.getValues().get(0);
		assertEquals(0.5, d.doubleValue(), 0.0001);
	}

	@Test
	public void testDoubleAnnotationValue_02() throws Exception {
		JvmDoubleAnnotationValue value = (JvmDoubleAnnotationValue) getMethodParameterAnnotationValue("doubleValue");
		assertEquals(1, value.getValues().size());
		Double d = value.getValues().get(0);
		assertEquals(0.5, d.doubleValue(), 0.0001);
	}

	@Test
	public void testDoubleAnnotationValue_03() throws Exception {
		JvmDoubleAnnotationValue value = (JvmDoubleAnnotationValue) getConstructorParameterAnnotationValue(
				"doubleValue");
		assertEquals(1, value.getValues().size());
		Double d = value.getValues().get(0);
		assertEquals(0.5, d.doubleValue(), 0.0001);
	}

	@Test
	public void testShortAnnotationValue_01() throws Exception {
		JvmShortAnnotationValue value = (JvmShortAnnotationValue) getAnnotationValue("shortValue");
		assertEquals(1, value.getValues().size());
		Short s = value.getValues().get(0);
		assertEquals(4, s.shortValue());
	}

	@Test
	public void testShortAnnotationValue_02() throws Exception {
		JvmShortAnnotationValue value = (JvmShortAnnotationValue) getMethodParameterAnnotationValue("shortValue");
		assertEquals(1, value.getValues().size());
		Short s = value.getValues().get(0);
		assertEquals(4, s.shortValue());
	}

	@Test
	public void testShortAnnotationValue_03() throws Exception {
		JvmShortAnnotationValue value = (JvmShortAnnotationValue) getConstructorParameterAnnotationValue("shortValue");
		assertEquals(1, value.getValues().size());
		Short s = value.getValues().get(0);
		assertEquals(4, s.shortValue());
	}

	@Test
	public void testByteAnnotationValue_01() throws Exception {
		JvmByteAnnotationValue value = (JvmByteAnnotationValue) getAnnotationValue("byteValue");
		assertEquals(1, value.getValues().size());
		Byte b = value.getValues().get(0);
		assertEquals(2, b.byteValue());
	}

	@Test
	public void testByteAnnotationValue_02() throws Exception {
		JvmByteAnnotationValue value = (JvmByteAnnotationValue) getMethodParameterAnnotationValue("byteValue");
		assertEquals(1, value.getValues().size());
		Byte b = value.getValues().get(0);
		assertEquals(2, b.byteValue());
	}

	@Test
	public void testByteAnnotationValue_03() throws Exception {
		JvmByteAnnotationValue value = (JvmByteAnnotationValue) getConstructorParameterAnnotationValue("byteValue");
		assertEquals(1, value.getValues().size());
		Byte b = value.getValues().get(0);
		assertEquals(2, b.byteValue());
	}

	@Test
	public void testLongAnnotationValue_01() throws Exception {
		JvmLongAnnotationValue value = (JvmLongAnnotationValue) getAnnotationValue("longValue");
		assertEquals(2, value.getValues().size());
		Long l = value.getValues().get(0);
		assertEquals(6, l.longValue());
		l = value.getValues().get(1);
		assertEquals(7, l.longValue());
	}

	@Test
	public void testLongAnnotationValue_02() throws Exception {
		JvmLongAnnotationValue value = (JvmLongAnnotationValue) getMethodParameterAnnotationValue("longValue");
		assertEquals(2, value.getValues().size());
		Long l = value.getValues().get(0);
		assertEquals(6, l.longValue());
		l = value.getValues().get(1);
		assertEquals(7, l.longValue());
	}

	@Test
	public void testLongAnnotationValue_03() throws Exception {
		JvmLongAnnotationValue value = (JvmLongAnnotationValue) getConstructorParameterAnnotationValue("longValue");
		assertEquals(2, value.getValues().size());
		Long l = value.getValues().get(0);
		assertEquals(6, l.longValue());
		l = value.getValues().get(1);
		assertEquals(7, l.longValue());
	}

	@Test
	public void testCharAnnotationValue_01() throws Exception {
		JvmCharAnnotationValue value = (JvmCharAnnotationValue) getAnnotationValue("charValue");
		assertEquals(1, value.getValues().size());
		Character c = value.getValues().get(0);
		assertEquals('a', c.charValue());
	}

	@Test
	public void testCharAnnotationValue_03() throws Exception {
		JvmCharAnnotationValue value = (JvmCharAnnotationValue) getConstructorParameterAnnotationValue("charValue");
		assertEquals(1, value.getValues().size());
		Character c = value.getValues().get(0);
		assertEquals('a', c.charValue());
	}

	@Test
	public void testCharAnnotationValue_02() throws Exception {
		JvmCharAnnotationValue value = (JvmCharAnnotationValue) getMethodParameterAnnotationValue("charValue");
		assertEquals(1, value.getValues().size());
		Character c = value.getValues().get(0);
		assertEquals('a', c.charValue());
	}

	@Test
	public void testStringAnnotationValue_01() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getAnnotationValue("stringValue");
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("stringValue", s);
	}

	@Test
	public void testStringAnnotationValue_02() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getAnnotationValue("stringArrayValue");
		assertEquals(4, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("array", s);
		s = value.getValues().get(1);
		assertEquals("value", s);
		s = value.getValues().get(2);
		assertEquals("duplicate", s);
		s = value.getValues().get(3);
		assertEquals("duplicate", s);
	}

	@Test
	public void testStringAnnotationValue_03() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getMethodParameterAnnotationValue("stringValue");
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("stringValue", s);
	}

	@Test
	public void testStringAnnotationValue_04() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getMethodParameterAnnotationValue(
				"stringArrayValue");
		assertEquals(2, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("array", s);
		s = value.getValues().get(1);
		assertEquals("value", s);
	}

	@Test
	public void testStringAnnotationValue_05() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getConstructorParameterAnnotationValue(
				"stringValue");
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("stringValue", s);
	}

	@Test
	public void testStringAnnotationValue_06() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getConstructorParameterAnnotationValue(
				"stringArrayValue");
		assertEquals(2, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("array", s);
		s = value.getValues().get(1);
		assertEquals("value", s);
	}

	@Test
	public void testClassAnnotationValue_01() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getAnnotationValue("charSequenceClass");
		assertEquals(1, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(String.class.getName(), type.getIdentifier());
	}

	@Test
	public void testClassAnnotationValue_02() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getAnnotationValue("classArray");
		assertEquals(2, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
		type = value.getValues().get(1).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
	}

	@Test
	public void testClassAnnotationValue_04() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getMethodParameterAnnotationValue("charSequenceClass");
		assertEquals(1, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(String.class.getName(), type.getIdentifier());
	}

	@Test
	public void testClassAnnotationValue_03() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getMethodParameterAnnotationValue("classArray");
		assertEquals(2, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
		type = value.getValues().get(1).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
	}

	@Test
	public void testClassAnnotationValue_05() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getConstructorParameterAnnotationValue(
				"charSequenceClass");
		assertEquals(1, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(String.class.getName(), type.getIdentifier());
	}

	@Test
	public void testClassAnnotationValue_06() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getConstructorParameterAnnotationValue("classArray");
		assertEquals(2, value.getValues().size());
		JvmType type = value.getValues().get(0).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
		type = value.getValues().get(1).getType();
		assertNotNull(type);
		assertFalse(type.eIsProxy());
		assertEquals(TestAnnotation.Annotated.class.getName(), type.getIdentifier());
	}

	@Test
	public void testAnnotationAnnotationValue_01() throws Exception {
		JvmAnnotationAnnotationValue value = (JvmAnnotationAnnotationValue) getAnnotationValue("annotationValue");
		assertEquals(1, value.getValues().size());
		JvmAnnotationReference annotationReference = value.getValues().get(0);
		assertEquals(TestAnnotation.NestedAnnotation.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		assertEquals(1, annotationReference.getValues().size());
		JvmStringAnnotationValue nestedValue = (JvmStringAnnotationValue) annotationReference.getValues().get(0);
		assertEquals(1, nestedValue.getValues().size());
		assertEquals("MyString", nestedValue.getValues().get(0));
	}

	@Test
	public void testAnnotationAnnotationValue_02() throws Exception {
		JvmAnnotationAnnotationValue value = (JvmAnnotationAnnotationValue) getConstructorParameterAnnotationValue(
				"annotationValue");
		assertEquals(1, value.getValues().size());
		JvmAnnotationReference annotationReference = value.getValues().get(0);
		assertEquals(TestAnnotation.NestedAnnotation.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		assertEquals(1, annotationReference.getValues().size());
		JvmStringAnnotationValue nestedValue = (JvmStringAnnotationValue) annotationReference.getValues().get(0);
		assertEquals(1, nestedValue.getValues().size());
		assertEquals("MyString", nestedValue.getValues().get(0));
	}

	@Test
	public void testAnnotationAnnotationValue_03() throws Exception {
		JvmAnnotationAnnotationValue value = (JvmAnnotationAnnotationValue) getMethodParameterAnnotationValue(
				"annotationValue");
		assertEquals(1, value.getValues().size());
		JvmAnnotationReference annotationReference = value.getValues().get(0);
		assertEquals(TestAnnotation.NestedAnnotation.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		assertEquals(1, annotationReference.getValues().size());
		JvmStringAnnotationValue nestedValue = (JvmStringAnnotationValue) annotationReference.getValues().get(0);
		assertEquals(1, nestedValue.getValues().size());
		assertEquals("MyString", nestedValue.getValues().get(0));
	}

	@Test
	public void testEnumAnnotationValue_01() throws Exception {
		JvmEnumAnnotationValue value = (JvmEnumAnnotationValue) getAnnotationValue("enumValue");
		assertEquals(1, value.getValues().size());
		JvmEnumerationLiteral enumLiteral = value.getValues().get(0);
		assertFalse(enumLiteral.eIsProxy());
		assertEquals(TestEnum.FirstValue.name(), enumLiteral.getSimpleName());
	}

	@Test
	public void testEnumAnnotationValue_02() throws Exception {
		JvmEnumAnnotationValue value = (JvmEnumAnnotationValue) getConstructorParameterAnnotationValue("enumValue");
		assertEquals(1, value.getValues().size());
		JvmEnumerationLiteral enumLiteral = value.getValues().get(0);
		assertFalse(enumLiteral.eIsProxy());
		assertEquals(TestEnum.FirstValue.name(), enumLiteral.getSimpleName());
	}

	@Test
	public void testEnumAnnotationValue_03() throws Exception {
		JvmEnumAnnotationValue value = (JvmEnumAnnotationValue) getMethodParameterAnnotationValue("enumValue");
		assertEquals(1, value.getValues().size());
		JvmEnumerationLiteral enumLiteral = value.getValues().get(0);
		assertFalse(enumLiteral.eIsProxy());
		assertEquals(TestEnum.FirstValue.name(), enumLiteral.getSimpleName());
	}

	public JvmAnnotationValue getAnnotationValue(String name) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationTarget target = (JvmAnnotationTarget) getTypeProvider().findTypeByName(typeName);
		return getExplicitAnnotationValue(name, target);
	}

	public JvmAnnotationValue getAnnotationValue(String name, boolean defaultValue) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationTarget target = (JvmAnnotationTarget) getTypeProvider().findTypeByName(typeName);
		JvmAnnotationValue result = getDefaultOrExplicitAnnotationValue(name, target);
		if (defaultValue) {
			if (isDefaultValueSupported()) {
				assertTrue(result.eContainer() instanceof JvmOperation);
			} else {
				assertFalse(result.eContainer() instanceof JvmOperation);
			}
		} else {
			assertFalse(result.eContainer() instanceof JvmOperation);
		}
		return result;
	}

	public JvmAnnotationValue getConstructorParameterAnnotationValue(String name) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = constructor.getParameters().get(0);
		return getExplicitAnnotationValue(name, target);
	}

	public JvmAnnotationValue getConstructorParameterAnnotationValue(String name, boolean defaultValue) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = constructor.getParameters().get(0);
		JvmAnnotationValue result = getDefaultOrExplicitAnnotationValue(name, target);
		if (defaultValue) {
			if (isDefaultValueSupported()) {
				assertTrue(result.eContainer() instanceof JvmOperation);
			} else {
				assertFalse(result.eContainer() instanceof JvmOperation);
			}
		} else {
			assertFalse(result.eContainer() instanceof JvmOperation);
		}
		return result;
	}

	public JvmAnnotationValue getMethodParameterAnnotationValue(String name) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, TestAnnotation.Annotated.class,
				"method(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = method.getParameters().get(0);
		return getExplicitAnnotationValue(name, target);
	}

	public JvmAnnotationValue getMethodParameterAnnotationValue(String name, boolean defaultValue) {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, TestAnnotation.Annotated.class,
				"method(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = method.getParameters().get(0);
		JvmAnnotationValue result = getDefaultOrExplicitAnnotationValue(name, target);
		if (defaultValue) {
			if (isDefaultValueSupported()) {
				assertTrue(result.eContainer() instanceof JvmOperation);
			} else {
				assertFalse(result.eContainer() instanceof JvmOperation);
			}
		} else {
			assertFalse(result.eContainer() instanceof JvmOperation);
		}
		return result;
	}

	protected boolean isDefaultValueSupported() {
		return true;
	}

	public JvmAnnotationValue getDefaultOrExplicitAnnotationValue(String name, JvmAnnotationTarget target) {
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		for (JvmAnnotationValue value : annotationReference.getValues()) {
			if (name.equals(value.getValueName()))
				return value;
		}
		fail("Cannot find annotationValue " + name);
		return null;
	}

	public JvmAnnotationValue getExplicitAnnotationValue(String name, JvmAnnotationTarget target) {
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		for (JvmAnnotationValue value : annotationReference.getExplicitValues()) {
			if (name.equals(value.getValueName()))
				return value;
		}
		fail("Cannot find annotationValue " + name);
		return null;
	}

	@Test
	public void testAnnotatedMethod_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, TestAnnotation.Annotated.class, "method()");
		assertNotNull(operation);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getExplicitAnnotationValue("value", operation);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("method", s);
	}

	@Test
	public void testAnnotatedConstructor_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class, "Annotated()");
		assertNotNull(constructor);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getExplicitAnnotationValue("value", constructor);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("constructor", s);
	}

	@Test
	public void testAnnotatedConstructor_02() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String)");
		assertNotNull(constructor);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getExplicitAnnotationValue("value", constructor);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("secondConstructor", s);
	}

	@Test
	public void testAnnotatedConstructor_03() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,T)");
		assertNotNull(constructor);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getExplicitAnnotationValue("value", constructor);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("parameterizedConstructor", s);
	}

	@Test
	public void testAnnotatedConstructor_04() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String)");
		assertNotNull(constructor);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getExplicitAnnotationValue("value", constructor);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("thirdConstructorWithBody", s);
	}

	@Test
	public void testAnnotatedField_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, TestAnnotation.Annotated.class, "field");
		assertNotNull(field);
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getDefaultOrExplicitAnnotationValue("value", field);
		assertEquals(1, value.getValues().size());
		String s = value.getValues().get(0);
		assertEquals("MyString", s);
	}

	@Test
	public void testAnnotatedParameter_01() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationType annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.class.getName());
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = constructor.getParameters().get(0);
		assertEquals(2, target.getAnnotations().size());
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		assertSame(annotationType, annotationReference.getAnnotation());

		annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.NestedAnnotation.class.getName());
		annotationReference = target.getAnnotations().get(1);
		assertSame(annotationType, annotationReference.getAnnotation());
	}

	@Test
	public void testAnnotatedParameter_02() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = constructor.getParameters().get(1);
		assertEquals(0, target.getAnnotations().size());
	}

	@Test
	public void testAnnotatedParameter_03() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationType annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.NestedAnnotation.class.getName());
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, TestAnnotation.Annotated.class,
				"Annotated(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = constructor.getParameters().get(2);
		assertEquals(1, target.getAnnotations().size());
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		assertSame(annotationType, annotationReference.getAnnotation());
	}

	@Test
	public void testAnnotatedParameter_04() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationType annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.class.getName());
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, TestAnnotation.Annotated.class,
				"method(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = method.getParameters().get(0);
		assertEquals(2, target.getAnnotations().size());
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		assertSame(annotationType, annotationReference.getAnnotation());

		annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.NestedAnnotation.class.getName());
		annotationReference = target.getAnnotations().get(1);
		assertSame(annotationType, annotationReference.getAnnotation());
	}

	@Test
	public void testAnnotatedParameter_05() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, TestAnnotation.Annotated.class,
				"method(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = method.getParameters().get(1);
		assertEquals(0, target.getAnnotations().size());
	}

	@Test
	public void testAnnotatedParameter_06() throws Exception {
		String typeName = TestAnnotation.Annotated.class.getName();
		JvmAnnotationType annotationType = (JvmAnnotationType) getTypeProvider()
				.findTypeByName(TestAnnotation.NestedAnnotation.class.getName());
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, TestAnnotation.Annotated.class,
				"method(java.lang.String,java.lang.String,java.lang.String)");
		JvmAnnotationTarget target = method.getParameters().get(2);
		assertEquals(1, target.getAnnotations().size());
		JvmAnnotationReference annotationReference = target.getAnnotations().get(0);
		assertSame(annotationType, annotationReference.getAnnotation());
	}

	@Test
	public void testAnnotationWithStringDefault_01() throws Exception {
		String typeName = TestAnnotationWithStringDefault.Annotated.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		List<JvmAnnotationReference> annotations = type.getAnnotations();
		assertEquals(1, annotations.size());
		JvmAnnotationReference annotationReference = annotations.get(0);
		assertEquals(TestAnnotationWithStringDefault.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		checkDefaultAnnotationValues(annotationReference);
	}

	@Test
	public void testAnnotationWithStringDefault_02() throws Exception {
		String typeName = AnnotatedInterfaceWithStringDefault.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		List<JvmAnnotationReference> annotations = type.getAnnotations();
		assertEquals(1, annotations.size());
		JvmAnnotationReference annotationReference = annotations.get(0);
		assertEquals(TestAnnotationWithStringDefault.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		checkDefaultAnnotationValuesAnnotatedExternalClass(annotationReference);
	}

	@Test
	public void testAnnotationWithStringDefault_03() throws Exception {
		String typeName = TestAnnotationWithStringDefault.AnnotatedInterface.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		List<JvmAnnotationReference> annotations = type.getAnnotations();
		assertEquals(1, annotations.size());
		JvmAnnotationReference annotationReference = annotations.get(0);
		assertEquals(TestAnnotationWithStringDefault.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		checkDefaultAnnotationValues(annotationReference);
	}

	@Test
	public void testAnnotationWithStringDefault_04() throws Exception {
		String typeName = AnnotatedClassWithStringDefault.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		List<JvmAnnotationReference> annotations = type.getAnnotations();
		assertEquals(1, annotations.size());
		JvmAnnotationReference annotationReference = annotations.get(0);
		assertEquals(TestAnnotationWithStringDefault.class.getName(),
				annotationReference.getAnnotation().getIdentifier());
		checkDefaultAnnotationValuesAnnotatedExternalClass(annotationReference);
	}

	@Test
	public void testAbstractMethod() throws Exception {
		String typeName = AbstractMethods.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertEquals(3, size(type.getDeclaredOperations()));
		for (JvmOperation op : type.getDeclaredOperations()) {
			if (op.getSimpleName().startsWith("abstract")) {
				assertTrue(op.isAbstract());
			} else {
				assertFalse(op.isAbstract());
			}
		}
	}

	@Test
	public void testDefaultIntAnnotationValue_01() throws Exception {
		JvmIntAnnotationValue value = (JvmIntAnnotationValue) getDefaultAnnotationValue("intValue");
		assertEquals(1, value.getValues().size());
		Integer integer = value.getValues().get(0);
		assertEquals(34, integer.intValue());
	}

	@Test
	public void testDefaultByteAnnotationValue_01() throws Exception {
		JvmByteAnnotationValue value = (JvmByteAnnotationValue) getDefaultAnnotationValue("byteValue");
		assertEquals(1, value.getValues().size());
		Byte b = value.getValues().get(0);
		assertEquals(1, b.byteValue());
	}

	@Test
	public void testDefaultFloatAnnotationValue_01() throws Exception {
		JvmFloatAnnotationValue value = (JvmFloatAnnotationValue) getDefaultAnnotationValue("floatValue");
		assertEquals(1, value.getValues().size());
		Float f = value.getValues().get(0);
		assertEquals(5f, f.floatValue(), 0.0001);
	}

	@Test
	public void testDefaultDoubleAnnotationValue_01() throws Exception {
		JvmDoubleAnnotationValue value = (JvmDoubleAnnotationValue) getDefaultAnnotationValue("doubleValue");
		assertEquals(1, value.getValues().size());
		Double d = value.getValues().get(0);
		assertEquals(23d, d.doubleValue(), 0.0001);
	}

	@Test
	public void testDefaultBooleanAnnotationValue_01() throws Exception {
		JvmBooleanAnnotationValue value = (JvmBooleanAnnotationValue) getDefaultAnnotationValue("booleanValue");
		assertEquals(1, value.getValues().size());
		Boolean b = value.getValues().get(0);
		assertEquals(true, b.booleanValue());
	}

	@Test
	public void testDefaultShortAnnotationValue_01() throws Exception {
		JvmShortAnnotationValue value = (JvmShortAnnotationValue) getDefaultAnnotationValue("shortValue");
		assertEquals(1, value.getValues().size());
		Short s = value.getValues().get(0);
		assertEquals(12, s.shortValue());
	}

	@Test
	public void testDefaultLongArrayAnnotationValue_01() throws Exception {
		JvmLongAnnotationValue value = (JvmLongAnnotationValue) getDefaultAnnotationValue("longArrayValue");
		assertEquals(2, value.getValues().size());
		Long l1 = value.getValues().get(0);
		assertEquals(50, l1.longValue());
		Long l2 = value.getValues().get(1);
		assertEquals(60, l2.longValue());
	}

	@Test
	public void testDefaultCharArrayAnnotationValue_01() throws Exception {
		JvmCharAnnotationValue value = (JvmCharAnnotationValue) getDefaultAnnotationValue("charArrayValue");
		assertEquals(1, value.getValues().size());
		Character c = value.getValues().get(0);
		assertEquals('a', c.charValue());
	}

	@Test
	public void testDefaultEnumAnnotationValue_01() throws Exception {
		JvmEnumAnnotationValue value = (JvmEnumAnnotationValue) getDefaultAnnotationValue("enumValue");
		assertEquals(1, value.getValues().size());
		JvmEnumerationLiteral literal = value.getValues().get(0);
		assertEquals("org.eclipse.xtext.common.types.testSetups.TestEnum.FirstValue", literal.getIdentifier());
	}

	@Test
	public void testDefaultAnnotationAnnotationValue_01() throws Exception {
		JvmAnnotationAnnotationValue value = (JvmAnnotationAnnotationValue) getDefaultAnnotationValue(
				"annotationValue");
		assertEquals(1, value.getValues().size());
		JvmAnnotationReference reference = value.getValues().get(0);
		assertEquals(TestAnnotationWithDefaults.NestedAnnotation.class.getName(),
				reference.getAnnotation().getIdentifier());
		JvmAnnotationValue nestedAnnotationValue = reference.getValues().get(0);
		assertTrue(nestedAnnotationValue instanceof JvmStringAnnotationValue);
		assertEquals("AnotherString", ((JvmStringAnnotationValue) nestedAnnotationValue).getValues().get(0));
	}

	@Test
	public void testDefaultAnnotationAnnotationValue_02() throws Exception {
		JvmAnnotationAnnotationValue value = (JvmAnnotationAnnotationValue) getDefaultAnnotationValue(
				"annotationArrayValue");
		assertEquals(2, value.getValues().size());
		JvmAnnotationReference reference1 = value.getValues().get(0);
		assertEquals(TestAnnotationWithDefaults.NestedAnnotation.class.getName(),
				reference1.getAnnotation().getIdentifier());
		JvmAnnotationValue nestedAnnotationValue1 = reference1.getValues().get(0);
		assertTrue(nestedAnnotationValue1 instanceof JvmStringAnnotationValue);
		assertEquals("AnotherString", ((JvmStringAnnotationValue) nestedAnnotationValue1).getValues().get(0));
		JvmAnnotationReference reference2 = value.getValues().get(1);
		assertEquals(TestAnnotationWithDefaults.NestedAnnotation.class.getName(),
				reference2.getAnnotation().getIdentifier());
		JvmAnnotationValue nestedAnnotationValue2 = reference2.getValues().get(0);
		assertTrue(nestedAnnotationValue2 instanceof JvmStringAnnotationValue);
		assertEquals("MyString", ((JvmStringAnnotationValue) nestedAnnotationValue2).getValues().get(0));
	}

	@Test
	public void testDefaultAnnotationAnnotationValueByReference() throws Exception {
		String typeName = Bug334943Client.class.getName();
		JvmDeclaredType client = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = Iterables.get(client.getDeclaredOperations(), 0);
		List<JvmAnnotationReference> annotations = operation.getAnnotations();
		assertEquals(1, annotations.size());
		JvmAnnotationReference annotation = annotations.get(0);
		for (JvmAnnotationValue value : annotation.getValues()) {
			if ("enumValue".equals(value.getValueName())) {
				JvmEnumAnnotationValue enumValue = (JvmEnumAnnotationValue) value;
				assertEquals(1, enumValue.getValues().size());
				assertEquals("FirstValue", enumValue.getValues().get(0).getSimpleName());
			}
		}
	}

	@Test
	public void testDefaultStringAnnotationValue_01() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getDefaultAnnotationValue("stringValue");
		assertEquals(1, value.getValues().size());
		String string = value.getValues().get(0);
		assertEquals("", string);
	}

	@Test
	public void testDefaultStringAnnotationValue_02() throws Exception {
		JvmStringAnnotationValue value = (JvmStringAnnotationValue) getDefaultAnnotationValue("stringArrayValue");
		assertEquals(1, value.getValues().size());
		String string = value.getValues().get(0);
		assertEquals("arrayValue", string);
	}

	@Test
	public void testDefaultTypeAnnotationValue_01() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getDefaultAnnotationValue("charSequenceClass");
		assertEquals(1, value.getValues().size());
		JvmTypeReference typeReference = value.getValues().get(0);
		assertEquals(String.class.getName(), typeReference.getIdentifier());
	}

	@Test
	public void testDefaultTypeAnnotationValue_02() throws Exception {
		JvmTypeAnnotationValue value = (JvmTypeAnnotationValue) getDefaultAnnotationValue("classArray");
		assertEquals(0, value.getValues().size());
	}

	public JvmAnnotationValue getDefaultAnnotationValue(String name) {
		String typeName = TestAnnotationWithDefaults.class.getName();
		JvmAnnotationType type = (JvmAnnotationType) getTypeProvider().findTypeByName(typeName);
		JvmOperation operation = getMethodFromType(type, TestAnnotationWithDefaults.class, name + "()");
		JvmAnnotationValue result = operation.getDefaultValue();
		assertNotNull(result);
		assertSame(operation, result.getOperation());
		return result;
	}

	protected void checkDefaultAnnotationValuesAnnotatedExternalClass(JvmAnnotationReference annotationReference) {
		checkDefaultAnnotationValues(annotationReference);
	}

	protected void checkDefaultAnnotationValues(JvmAnnotationReference annotationReference) {
		List<JvmAnnotationValue> values = annotationReference.getValues();
		assertEquals(2, values.size());
		Map<String, String> nameToValue = Maps.newHashMap();
		nameToValue.put("emptyString", "");
		nameToValue.put("string", "string");
		for (JvmAnnotationValue value : values) {
			String defaultValue = nameToValue.remove(value.getValueName());
			assertNotNull(value.getValueName(), defaultValue);
			JvmStringAnnotationValue castedValue = (JvmStringAnnotationValue) value;
			assertEquals(1, castedValue.getValues().size());
			assertEquals(defaultValue, castedValue.getValues().get(0));
		}
		assertTrue(nameToValue.isEmpty());
	}

	@Test
	public void testVarArgs_01() {
		String typeName = ClassWithVarArgs.class.getName();
		JvmType type = getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testVarArgs_02() {
		String typeName = ClassWithVarArgs.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation method = getMethodFromType(type, ClassWithVarArgs.class, "method(java.lang.String[])");
		assertTrue(method.isVarArgs());
		assertEquals(1, method.getParameters().size());
		assertTrue(method.getParameters().get(0).getParameterType() instanceof JvmGenericArrayTypeReference);
	}

	@Test
	public void testVarArgs_03() {
		String typeName = ClassWithVarArgs.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmConstructor constructor = getConstructorFromType(type, ClassWithVarArgs.class,
				"ClassWithVarArgs(int,java.lang.String[])");
		assertTrue(constructor.isVarArgs());
		assertEquals(2, constructor.getParameters().size());
		assertTrue(constructor.getParameters().get(0).getParameterType() instanceof JvmParameterizedTypeReference);
		assertTrue(constructor.getParameters().get(1).getParameterType() instanceof JvmGenericArrayTypeReference);
	}

	@Test
	public void testArraysArraylist_01() {
		assertNull(getTypeProvider().findTypeByName("java.util.Arrays.ArrayList"));
	}

	@Test
	public void testArraysArraylist_02() {
		assertNotNull(getTypeProvider().findTypeByName("java.util.Arrays$ArrayList"));
	}

	@Test
	public void testArraysArraylist_03() {
		assertNotNull(getTypeProvider().findTypeByName("java.util.Arrays.ArrayList", false));
	}

	@Test
	public void testArraysArraylist_04() {
		assertNotNull(getTypeProvider().findTypeByName("java.util.Arrays$ArrayList", false));
	}

	@Test
	public void testArraysArraylist_05() {
		assertNull(getTypeProvider().findTypeByName("java.util.Arrays.ArrayList", true));
	}

	@Test
	public void testGoogleFunction() {
		String functionType = Function.class.getName();
		assertNotNull(getTypeProvider().findTypeByName(functionType));
	}

	protected abstract String getCollectionParamName();

	@Test
	public void testPerformance() {
		for (int i = 0; i < 10/*00*/; i++) {
			getTypeProvider().getResourceSet().getResources().clear();
			getTypeProvider().findTypeByName(TestAnnotation.class.getName());
			getTypeProvider().findTypeByName(ParameterizedMethods.class.getName());
			getTypeProvider().findTypeByName(ParameterizedTypes.class.getName());
		}
		//		fail();
	}

	@Test
	public void testBug347739_01() {
		String typeName = Bug347739.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testBug347739_02() {
		String typeName = Bug347739OneTypeParam.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testBug347739_03() {
		String typeName = Bug347739ThreeTypeParams.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testBug347739_04() {
		String typeName = Bug347739ThreeTypeParamsSuper.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testBug347739_05() {
		String typeName = Bug347739ThreeTypeParamsSuperSuper.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testBug347739_06() {
		String typeNameList = List.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeNameList);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		diagnose(type);
	}

	@Test
	public void testRawIterable_01() {
		String typeName = RawIterable.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		List<JvmTypeReference> superTypes = type.getSuperTypes();
		JvmParameterizedTypeReference iterableSuperType = (JvmParameterizedTypeReference) superTypes.get(1);
		assertEquals("java.lang.Iterable", iterableSuperType.getIdentifier());
		assertTrue(iterableSuperType.getArguments().isEmpty());
	}

	@Test
	public void testFindTypeByName_$StartsWithDollar_01() {
		//		Class<org.eclipse.xtext.common.types.testSetups.$StartsWithDollar> clazz = org.eclipse.xtext.common.types.testSetups.$StartsWithDollar.class;
		String typeName = "org.eclipse.xtext.common.types.testSetups.$StartsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		doTestFindTypeByName_$StartsWithDollar(type);
	}

	@Test
	public void testFindTypeByName_$StartsWithDollar_02() {
		//		Class<org.eclipse.xtext.common.types.testSetups.$StartsWithDollar> clazz = org.eclipse.xtext.common.types.testSetups.$StartsWithDollar.class;
		String typeName = "org.eclipse.xtext.common.types.testSetups.$StartsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		doTestFindTypeByName_$StartsWithDollar(type);
	}

	private void doTestFindTypeByName_$StartsWithDollar(JvmGenericType type) {
		assertNotNull(type);
		Iterable<String> innerTypes = Iterables.transform(Iterables.filter(type.getMembers(), JvmType.class),
				new Function<JvmType, String>() {
					@Override
					public String apply(JvmType input) {
						return input.getSimpleName();
					}
				});
		assertTrue("Missing member type $Builder", Iterables.contains(innerTypes, "Builder"));
		assertEquals(1, Iterables.size(innerTypes));
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_TypeParamEndsWithDollar() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.TypeParamEndsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_AbstractMultimap() {
		String typeName = "com.google.common.collect.AbstractMultimap";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		diagnose(type, "java:/Objects/javax.annotation.Nullable#javax.annotation.Nullable");
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_ClassWithDefaultPackage() {
		String typeName = "ClassWithDefaultPackage";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		assertEquals(typeName, type.getSimpleName());
		assertEquals(typeName, type.getQualifiedName());
		assertEquals(typeName, type.getIdentifier());
		assertNotNull(type);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
	}

	@Test
	public void testFindTypeByName_NestedTypeQualifiedWithSubType_01() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.NestedTypeInheritance.Sub.Inner";
		String identifier = "org.eclipse.xtext.common.types.testSetups.NestedTypeInheritance$Super$Inner";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, false);
		assertEquals(identifier, type.getIdentifier());
	}

	@Test
	public void testFindTypeByName_NestedTypeQualifiedWithSubType_02() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.NestedTypeInheritance$Sub$Inner";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName, true);
		assertNull(type);
	}

	@Test
	public void testTypeParamEndsWithDollar_01() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.TypeParamEndsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation function = (JvmOperation) type.findAllFeaturesByName("function1").iterator().next();
		JvmFormalParameter parameter = function.getParameters().get(0);
		JvmType parameterType = parameter.getParameterType().getType();
		assertEquals(function.getTypeParameters().get(0), parameterType);
	}

	@Test
	public void testTypeParamEndsWithDollar_02() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.TypeParamEndsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation function = (JvmOperation) type.findAllFeaturesByName("function2").iterator().next();
		JvmFormalParameter parameter = function.getParameters().get(0);
		JvmType parameterType = parameter.getParameterType().getType();
		assertEquals(function.getTypeParameters().get(0), parameterType);
		JvmFormalParameter secondParameter = function.getParameters().get(1);
		JvmType secondParameterType = secondParameter.getParameterType().getType();
		assertEquals(type.getTypeParameters().get(0), secondParameterType);
	}

	@Test
	public void testTypeParamEndsWithDollar_03() {
		String typeName = "org.eclipse.xtext.common.types.testSetups.TypeParamEndsWithDollar";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmOperation function = (JvmOperation) type.findAllFeaturesByName("function3").iterator().next();
		JvmFormalParameter parameter = function.getParameters().get(0);
		JvmType parameterType = parameter.getParameterType().getType();
		assertEquals(function.getTypeParameters().get(0), parameterType);
	}

	protected JvmField doTestConstantValue(String fieldName, Object fieldValue) {
		String typeName = "org.eclipse.xtext.common.types.testSetups.TestConstants";
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeName);
		JvmField field = getFieldFromType(type, TestConstants.class, fieldName);
		assertTrue(field.isSetConstant());
		assertTrue(field.isConstant());
		assertEquals(fieldValue, field.getConstantValue());
		return field;
	}

	protected void doTestParameterName(Class<?> type, String methodSignature, String parameterName) {
		JvmGenericType jvmType = (JvmGenericType) getTypeProvider().findTypeByName(type.getCanonicalName());
		JvmOperation method = getMethodFromType(jvmType, type, methodSignature);
		JvmFormalParameter singleParameter = method.getParameters().get(0);
		assertEquals(parameterName, singleParameter.getSimpleName());
	}

	@Test
	public void testConstantValue_01() {
		JvmField field = doTestConstantValue("stringConstant", TestConstants.stringConstant);
		assertEquals(TestConstants.stringConstant, field.getConstantValueAsString());
	}

	@Test
	public void testConstantValue_02() {
		JvmField field = doTestConstantValue("longConstant", TestConstants.longConstant);
		assertEquals(TestConstants.longConstant, field.getConstantValueAsLong());
	}

	@Test
	public void testConstantValue_03() {
		JvmField field = doTestConstantValue("intConstant", TestConstants.intConstant);
		assertEquals(TestConstants.intConstant, field.getConstantValueAsInt());
	}

	@Test
	public void testConstantValue_04() {
		JvmField field = doTestConstantValue("shortConstant", TestConstants.shortConstant);
		assertEquals(TestConstants.shortConstant, field.getConstantValueAsShort());
	}

	@Test
	public void testConstantValue_05() {
		JvmField field = doTestConstantValue("charConstant", TestConstants.charConstant);
		assertEquals(TestConstants.charConstant, field.getConstantValueAsChar());
	}

	@Test
	public void testConstantValue_06() {
		JvmField field = doTestConstantValue("byteConstant", TestConstants.byteConstant);
		assertEquals(TestConstants.byteConstant, field.getConstantValueAsByte());
	}

	@Test
	public void testConstantValue_07() {
		JvmField field = doTestConstantValue("doubleConstant", TestConstants.doubleConstant);
		assertEquals(TestConstants.doubleConstant, field.getConstantValueAsDouble(), 0.00000001);
	}

	@Test
	public void testConstantValue_07a() {
		JvmField field = doTestConstantValue("doubleConstantNaN", TestConstants.doubleConstantNaN);
		assertEquals(TestConstants.doubleConstantNaN, field.getConstantValueAsDouble(), 0.00000001);
	}

	@Test
	public void testConstantValue_07b() {
		JvmField field = doTestConstantValue("doubleConstantPosInf", TestConstants.doubleConstantPosInf);
		assertEquals(TestConstants.doubleConstantPosInf, field.getConstantValueAsDouble(), 0.00000001);
	}

	@Test
	public void testConstantValue_07c() {
		JvmField field = doTestConstantValue("doubleConstantNegInf", TestConstants.doubleConstantNegInf);
		assertEquals(TestConstants.doubleConstantNegInf, field.getConstantValueAsDouble(), 0.00000001);
	}

	@Test
	public void testConstantValue_08() {
		JvmField field = doTestConstantValue("floatConstant", TestConstants.floatConstant);
		assertEquals(TestConstants.floatConstant, field.getConstantValueAsFloat(), 0.00000001);
	}

	@Test
	public void testConstantValue_08a() {
		JvmField field = doTestConstantValue("floatConstantNaN", TestConstants.floatConstantNaN);
		assertEquals(TestConstants.floatConstantNaN, field.getConstantValueAsFloat(), 0.00000001);
	}

	@Test
	public void testConstantValue_08b() {
		JvmField field = doTestConstantValue("floatConstantPosInf", TestConstants.floatConstantPosInf);
		assertEquals(TestConstants.floatConstantPosInf, field.getConstantValueAsFloat(), 0.00000001);
	}

	@Test
	public void testConstantValue_08c() {
		JvmField field = doTestConstantValue("floatConstantNegInf", TestConstants.floatConstantNegInf);
		assertEquals(TestConstants.floatConstantNegInf, field.getConstantValueAsFloat(), 0.00000001);
	}

	@Test
	public void testConstantValue_09() {
		JvmField field = doTestConstantValue("booleanConstant", TestConstants.booleanConstant);
		assertEquals(TestConstants.booleanConstant, field.getConstantValueAsBoolean());
	}

	@Test
	public void testParameterNames_01() {
		doTestParameterName(Bug347739ThreeTypeParamsSuperSuper.class, "getToken(A)", "owner");
	}

	@Test
	public void testParameterNames_02() {
		doTestParameterName(AbstractMethods.class, "abstractMethodWithParameter(java.lang.String)", "parameterName");
	}

	@Test
	public void testParameterNames_03() {
		doTestParameterName(ClassWithVarArgs.class, "method(java.lang.String[])", "strings");
	}

	@Test
	public void testBug427098() {
		String typeName = Bug427098.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmAnnotationReference annotationReference = type.getAnnotations().get(0);
		JvmTypeAnnotationValue annotationValue = getClassArrayAnnotationValue(annotationReference);
		assertEquals("classArray", annotationValue.getOperation().getSimpleName());
		List<JvmTypeReference> typeReferences = annotationValue.getValues();
		assertEquals(5, typeReferences.size());
		assertTypeReference("int", typeReferences, 0);
		assertTypeReference("void", typeReferences, 1);
		assertTypeReference("double[][][]", typeReferences, 2);
		assertTypeReference("CharSequence[]", typeReferences, 3);
		assertTypeReference("Iterable", typeReferences, 4);
	}

	@Test
	public void testBug428340() {
		String typeName = Bug428340.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		JvmAnnotationReference myAnnotation = type.getAnnotations().get(0);
		assertEquals("MyAnnotation", myAnnotation.getAnnotation().getSimpleName());
	}

	protected JvmTypeAnnotationValue getClassArrayAnnotationValue(JvmAnnotationReference annotationReference) {
		return (JvmTypeAnnotationValue) annotationReference.getExplicitValues().get(0);
	}

	protected void assertTypeReference(String name, List<JvmTypeReference> references, int idx) {
		JvmTypeReference typeReference = references.get(idx);
		assertEquals(name, typeReference.getSimpleName());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDeprecatedBit_01() {
		String typeName = org.eclipse.xtext.common.types.testSetups.DeprecatedClass.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertTrue(type.isSetDeprecated());
		assertTrue(type.isDeprecated());

		for (JvmMember member : type.getMembers()) {
			assertTrue(member.isSetDeprecated());
			assertFalse(member.isDeprecated());
		}
	}

	@Test
	public void testDeprecatedBit_02() {
		String typeName = DeprecatedMembers.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertTrue(type.isSetDeprecated());
		assertFalse(type.isDeprecated());

		for (JvmMember member : type.getMembers()) {
			assertTrue(member.isSetDeprecated());
			assertTrue(member.isDeprecated());
		}
	}

	@Test
	public void testBug456328_01() {
		String typeName = Bug456328.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
	}

	@Test
	public void testBug456328_02() {
		String typeName = Bug456328.class.getName() + "PackageVisible";
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
	}

	@Test
	public void testConstructorThrowsException() {
		String typeUnderTest = CallableThrowsExceptions.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeUnderTest);
		assertNotNull(type);
		Iterable<JvmConstructor> constructors = filter(type.getMembers(), JvmConstructor.class);
		assertEquals(3, size(constructors));
		assertTrue(hasExecutableWithException(constructors, UnsupportedOperationException.class.getName()));
		assertTrue(hasExecutableWithException(constructors, "E"));
		assertTrue(
				hasExecutableWithException(constructors, Exception.class.getName(), RuntimeException.class.getName()));
	}
	
	@Test
	public void testMethodThrowsException() {
		String typeUnderTest = CallableThrowsExceptions.class.getName();
		JvmGenericType type = (JvmGenericType) getTypeProvider().findTypeByName(typeUnderTest);
		assertNotNull(type);
		Iterable<JvmOperation> methods = filter(type.findAllFeaturesByName("throwsExceptions"), JvmOperation.class);
		assertEquals(3, size(methods));
		assertTrue(hasExecutableWithException(methods, UnsupportedOperationException.class.getName()));
		assertTrue(hasExecutableWithException(methods, "E"));
		assertTrue(
				hasExecutableWithException(methods, Exception.class.getName(), RuntimeException.class.getName()));
	}

	private boolean hasExecutableWithException(Iterable<? extends JvmExecutable> executables, final String... exceptions) {
		return tryFind(executables, new Predicate<JvmExecutable>() {
			@Override
			public boolean apply(JvmExecutable executable) {
				EList<JvmTypeReference> declrExceptions = executable.getExceptions();
				if (declrExceptions.size() != exceptions.length) {
					return false;
				}
				Iterable<String> asStrings = transform(declrExceptions, new Function<JvmTypeReference, String>() {

					@Override
					public String apply(JvmTypeReference input) {
						return input.getIdentifier();
					}
				});
				for (String ex : exceptions) {
					if (!contains(asStrings, ex)) {
						return false;
					}
				}
				return true;
			}
		}).isPresent();
	}
	
	@Test
	public void testBug470767() {
		String typeName = Bug470767.class.getName();
		JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
		assertNotNull(type);
		diagnose(type);
		diagnose(type);
		Resource resource = type.eResource();
		getAndResolveAllFragments(resource);
		recomputeAndCheckIdentifiers(resource);
		Iterable<JvmFeature> methods = type.findAllFeaturesByName("paramIsAnnotated");
		JvmOperation method = (JvmOperation) Iterables.getOnlyElement(methods);
		JvmTypeReference paramType = method.getParameters().get(0).getParameterType();
		assertEquals("int", paramType.getSimpleName());
	}
}
