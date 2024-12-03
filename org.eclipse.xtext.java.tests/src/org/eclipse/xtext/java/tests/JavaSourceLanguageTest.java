/**
 * Copyright (c) 2016, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.java.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmBooleanAnnotationValue;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.java.resource.JavaConfig;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ChunkedResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsData;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.testing.util.InMemoryURIHandler;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;

@RunWith(XtextRunner.class)
@InjectWith(JavaInjectorProvider.class)
public class JavaSourceLanguageTest {
	@Test
	public void testNestedTypes() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("MySuperClass.java", 
						"public class MySuperClass {\n" +
						"    interface InnerType {}\n" +
						"}\n")
				.put("MySubClass.java",
						"public class MySubClass extends MySuperClass {\n" +
						"    public InnerType doStuff(InnerType some) {\n" +
						"        return some;\n" +
						"    }\n" +
						"}\n")
				.build();
		XtextResourceSet rs = this.resourceSet(files);
		Resource superResource = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MySuperClass.java"));
		EObject root = Iterables.getFirst(superResource.getContents(), null);
		Assert.assertNotNull(root);
		JvmDeclaredType nestedType = Iterables.getFirst(
				((JvmGenericType) root).getAllNestedTypes(), null);
		Resource resource = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MySubClass.java"));
		JvmGenericType clazz = (JvmGenericType) Iterables.getFirst(resource.getContents(), null);
		Assert.assertNotNull(clazz);
		JvmOperation firstOperation = Iterables.getFirst(clazz.getDeclaredOperations(), null);
		Assert.assertNotNull(firstOperation);
		JvmTypeReference returnType = firstOperation.getReturnType();
		JvmType referenced = returnType.getType();
		Assert.assertSame(nestedType, referenced);
	}

	@Test
	public void testPackageInfo() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("a/package-info.java", "package a;")
				.put("a/MyClass.java", 
						"package a;\n" +
						"public class MyClass {\n" +
						"}\n").build();
		XtextResourceSet rs = this.resourceSet(files);
		Resource r1 = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("package-info.java"));
		Assert.assertEquals(0, r1.getContents().size());
		Resource r2 = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MyClass.java"));
		Assert.assertEquals(1, r2.getContents().size());
	}

	@Test
	public void testOverridenInterfaceMethod() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("MySuperClass.java",
						"public interface MySuperClass {\n" +
						"    public java.util.Collection<? extends CharSequence> getThem();\n" +
						"}\n")
				.put("MySubClass.java",
						"public interface MySubClass extends MySuperClass {\n" +
						"    @Override\n" +
						"    public java.util.List<? extends String> getThem();\n" +
						"}\n")
				.build();
		XtextResourceSet rs = this.resourceSet(files);
		Resource resource = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MySubClass.java"));
		JvmGenericType clazz = (JvmGenericType) Iterables.getFirst(resource.getContents(), null);
		Assert.assertNotNull(clazz);
		JvmOperation firstOperation = Iterables.getFirst(clazz.getDeclaredOperations(), null);
		Assert.assertNotNull(firstOperation);
		JvmTypeReference referenced = firstOperation.getReturnType();
		Assert.assertNotNull(Iterables.getFirst(((JvmParameterizedTypeReference) referenced).getArguments(), null));
	}

	@Test
	public void testAnnotation() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("MyAnnotation.java",
					"public @interface MyAnnotation {\n" +
					"    String value();\n" +
					"    Class<?>[] imported() default {};\n" +
					"    boolean statementExpression() default false;\n" +
					"    boolean constantExpression() default false;\n" +
					"}\n")
				.put("MyClass.java",
						"@MyAnnotation(value=\"foo\", constantExpression = true)\n" +
						"public interface MyClass {\n" +
				"		}\n")
				.build();
		;
		XtextResourceSet rs = this.resourceSet(files);
		EObject annotation = Iterables.getFirst(IterableExtensions
				.findFirst(rs.getResources(), it -> it.getURI().toString().endsWith("MyAnnotation.java")).getContents(),
				null);
		Resource resource = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MyClass.java"));
		JvmGenericType clazz = (JvmGenericType) Iterables.getFirst(resource.getContents(), null);
		Assert.assertNotNull(clazz);
		JvmAnnotationReference annotationRef = Iterables.getFirst(clazz.getAnnotations(), null);
		Assert.assertNotNull(annotationRef);
		JvmAnnotationValue value = IterableExtensions.findFirst(annotationRef.getValues(),
				it -> "constantExpression".equals(it.getOperation().getSimpleName()));
		Assert.assertTrue(Iterables.getFirst(((JvmBooleanAnnotationValue) value).getValues(), null).booleanValue());
		JvmAnnotationValue value2 = IterableExtensions.findFirst(annotationRef.getValues(),
				it -> "statementExpression".equals(it.getOperation().getSimpleName()));
		Assert.assertFalse(Iterables.getFirst(((JvmBooleanAnnotationValue) value2).getValues(), null).booleanValue());
		Assert.assertSame(annotation, annotationRef.getAnnotation());
	}

	@Test
	public void testClassShadowing() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("org/eclipse/xtext/java/tests/MySuperClass2.java",
					"package org.eclipse.xtext.java.tests;\n" +
					"public class MySuperClass2 {\n" +
					"    public void doSomething() {\n" +
					"        \n" +
					"    }\n" +
					"}\n")
				.put("org/eclipse/xtext/java/tests/MySubClass2.java",
					"package org.eclipse.xtext.java.tests;\n" +
					"public class MySubClass2 extends MySuperClass2 {\n" +
					"}\n")
				.build();
		XtextResourceSet rs = this.resourceSet(files);
		Resource superResource = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MySuperClass2.java"));
		JvmGenericType clazz = (JvmGenericType) Iterables.getFirst(superResource.getContents(), null);
		Assert.assertNotNull(clazz);
		Assert.assertNotNull(Iterables.getFirst(clazz.getDeclaredOperations(), null));
	}

	@Test
	public void testJavaRecord() {
		ImmutableMap<String, String> files = ImmutableMap.<String, String>builder()
				.put("example/MyRecord.java", 
					"package example;\n"
					+ "\n"
					+ "public record MyRecord(String name, int age) {\n"
					+ "\n"
					+ "}\n"
				+ "").build();
		XtextResourceSet rs = this.resourceSet(files, JavaVersion.JAVA21);
		Resource r1 = IterableExtensions.findFirst(rs.getResources(),
				it -> it.getURI().toString().endsWith("MyRecord.java"));
		Assert.assertEquals(1, r1.getContents().size());
		JvmGenericType clazz = (JvmGenericType) Iterables.getFirst(r1.getContents(), null);
		Assert.assertNotNull(clazz);
		Assert.assertNotNull(Iterables.getFirst(clazz.getDeclaredOperations(), null));
	}

	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;

	@Inject
	private IResourceDescription.Manager resourceDesriptionManager;

	@Inject
	private IJvmTypeProvider.Factory typeProviderFactory;

	protected XtextResourceSet resourceSet(Map<String, String> files) {
		return this.resourceSet(files, null);
	}

	protected XtextResourceSet resourceSet(Map<String, String> files, JavaVersion javaVersion) {
		XtextResourceSet result = resourceSetProvider.get();
		if (javaVersion != null) {
			/*
			 * we need to configure the java version first before loading any resources into
			 * the resourceset to make sure on the load / install stubs the correct java
			 * version is picked
			 */
			JavaConfig javaConfig = new JavaConfig();
			javaConfig.setJavaSourceLevel(javaVersion);
			javaConfig.setJavaTargetLevel(javaVersion);
			javaConfig.attachToEmfObject(result);
		}
		typeProviderFactory.createTypeProvider(result);
		result.setClasspathURIContext(getClass().getClassLoader());
		result.getURIConverter().getURIHandlers().clear();
		InMemoryURIHandler uriHandler = new InMemoryURIHandler();
		List<URI> uris = files.entrySet().stream().map((Map.Entry<String, String> it) -> {
			try {
				URI uri = URI.createURI(InMemoryURIHandler.SCHEME + ":/" + it.getKey());
				OutputStream out = uriHandler.createOutputStream(uri, Collections.emptyMap());
				out.write(it.getValue().getBytes());
				out.close();
				return uri;
			} catch (IOException e) {
				throw Exceptions.sneakyThrow(e);
			}
		}).collect(Collectors.toList());
		result.getURIConverter().getURIHandlers().add(uriHandler);
		List<IResourceDescription> descriptions = new ArrayList<>();
		for (URI uri : uris) {
			Resource resource = result.getResource(uri, true);
			descriptions.add(this.resourceDesriptionManager.getResourceDescription(resource));
		}
		ChunkedResourceDescriptions chunkedResourceDescriptions = new ChunkedResourceDescriptions(
				ImmutableMap.<String, ResourceDescriptionsData>builder()
						.put("default", new ResourceDescriptionsData(descriptions)).build());
		chunkedResourceDescriptions.attachToEmfObject(result);
		return result;
	}
}
