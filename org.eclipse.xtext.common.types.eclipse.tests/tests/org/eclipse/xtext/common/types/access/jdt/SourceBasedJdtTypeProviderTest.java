/*******************************************************************************
 * Copyright (c) 2009, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.jdt;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeAnnotationValue;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.testSetups.EmptyAbstractClass;
import org.eclipse.xtext.common.types.util.jdt.JavaElementFinder;
import org.eclipse.xtext.util.Files;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class SourceBasedJdtTypeProviderTest extends AbstractJdtTypeProviderTest {

	private ResourceSet resourceSet;
	private JdtTypeProvider typeProvider;
	private MockJavaProjectProvider projectProvider;
	private JavaElementFinder elementFinder;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		resourceSet = new ResourceSetImpl();
		projectProvider = new MockJavaProjectProvider();
		projectProvider.setUseSource(true);
		typeProvider = new JdtTypeProvider(projectProvider.getJavaProject(resourceSet), resourceSet);
		elementFinder = new JavaElementFinder();
		elementFinder.setProjectProvider(projectProvider);
	}
	
	@After
	public void tearDown() throws Exception {
		resourceSet = null;
		typeProvider = null;
	}

	@Override
	protected IJvmTypeProvider getTypeProvider() {
		return typeProvider;
	}
	
	@Override
	protected String getCollectionParamName() {
		return "c";
	}
	
	@Override
	protected JvmOperation getMethodFromParameterizedMethods(String method) {
		JvmOperation result = super.getMethodFromParameterizedMethods(method);
		if (result != null) {
			IJavaElement found = elementFinder.findElementFor(result);
			assertEquals(IJavaElement.METHOD, found.getElementType());
			assertEquals(result.getSimpleName(), found.getElementName());
			IMethod foundMethod = (IMethod) found;
			assertEquals(result.getParameters().size(), foundMethod.getNumberOfParameters());
		}
		return result;
	}
	
	@Override
	protected JvmOperation getMethodFromType(EObject context, Class<?> type, String method) {
		JvmOperation result = super.getMethodFromType(context, type, method);
		if (result != null) {
			IJavaElement found = elementFinder.findElementFor(result);
			assertEquals(IJavaElement.METHOD, found.getElementType());
			assertEquals(result.getSimpleName(), found.getElementName());
			IMethod foundMethod = (IMethod) found;
			assertEquals(result.getParameters().size(), foundMethod.getNumberOfParameters());
		}		
		return result;
	}
	
	@Override
	protected JvmConstructor getConstructorFromType(EObject context, Class<?> type, String constructor) {
		JvmConstructor result = super.getConstructorFromType(context, type, constructor);
		if (result != null) {
			IJavaElement found = elementFinder.findElementFor(result);
			assertEquals(IJavaElement.METHOD, found.getElementType());
			assertEquals(result.getSimpleName(), found.getElementName());
			IMethod foundMethod = (IMethod) found;
			assertEquals(result.getParameters().size(), foundMethod.getNumberOfParameters());
		}
		return result;
	}
	
	@Override
	protected void checkDefaultAnnotationValuesAnnotatedExternalClass(JvmAnnotationReference annotationReference) {
		// SKIP (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=334863)
		// TODO fix as soon as source types expose annotation defaults reliably
	}
	
	@Test public void testClassAnnotationValue_07() throws Exception {
		IJavaProject project = projectProvider.getJavaProject(null);
		String typeName = EmptyAbstractClass.class.getName();
		IFile javaFile = (IFile) project.getProject().findMember(new Path("src/" + typeName.replace('.', '/') + ".java"));
		assertNotNull(javaFile);
		try (InputStream contents = javaFile.getContents()) {
			String content = Files.readStreamIntoString(contents);
			try {
				String newContent = content.replace(
						"public abstract ", 
						"@SimpleAnnotation( type = DoesNotExist.class ) public abstract ");
				javaFile.setContents(new StringInputStream(newContent), IResource.NONE, new NullProgressMonitor());
				
				JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
				List<JvmAnnotationReference> annotations = type.getAnnotations();
				assertEquals(1, annotations.size());
				JvmAnnotationReference annotation = annotations.get(0);
				assertEquals(1, annotation.getExplicitValues().size());
				JvmAnnotationValue value = annotation.getExplicitValues().get(0);
				assertTrue(value instanceof JvmTypeAnnotationValue);
				assertTrue(((JvmTypeAnnotationValue) value).getValues().isEmpty());
			} finally {
				javaFile.setContents(new StringInputStream(content), IResource.NONE, new NullProgressMonitor());
			}
		}
	}
	
	@Test public void testClassAnnotationValue_08() throws Exception {
		IJavaProject project = projectProvider.getJavaProject(null);
		String typeName = EmptyAbstractClass.class.getName();
		IFile javaFile = (IFile) project.getProject().findMember(new Path("src/" + typeName.replace('.', '/') + ".java"));
		assertNotNull(javaFile);
		try (InputStream contents = javaFile.getContents()) {
			String content = Files.readStreamIntoString(contents);
			try {
				String newContent = content.replace(
						"public abstract ", 
						"@TestAnnotation( classArray = { String.class, DoesNotExist.class, String.class } ) public abstract ");
				javaFile.setContents(new StringInputStream(newContent), IResource.NONE, new NullProgressMonitor());
				
				JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
				List<JvmAnnotationReference> annotations = type.getAnnotations();
				assertEquals(1, annotations.size());
				JvmAnnotationReference annotation = annotations.get(0);
				assertEquals(1, annotation.getExplicitValues().size());
				JvmAnnotationValue value = annotation.getExplicitValues().get(0);
				assertTrue(value instanceof JvmTypeAnnotationValue);
				List<JvmTypeReference> typeLiterals = ((JvmTypeAnnotationValue) value).getValues();
				assertEquals(2, typeLiterals.size());
			} finally {
				javaFile.setContents(new StringInputStream(content), IResource.NONE, new NullProgressMonitor());
			}
		}
	}
	
	@Test public void testClassAnnotationValue_09() throws Exception {
		IJavaProject project = projectProvider.getJavaProject(null);
		String typeName = EmptyAbstractClass.class.getName();
		IFile javaFile = (IFile) project.getProject().findMember(new Path("src/" + typeName.replace('.', '/') + ".java"));
		assertNotNull(javaFile);
		try (InputStream contents = javaFile.getContents()) {
			String content = Files.readStreamIntoString(contents);
			try {
				String newContent = content.replace(
						"public abstract ", 
						"@TestAnnotation( classArray = ) public abstract ");
				javaFile.setContents(new StringInputStream(newContent), IResource.NONE, new NullProgressMonitor());
				
				JvmDeclaredType type = (JvmDeclaredType) getTypeProvider().findTypeByName(typeName);
				List<JvmAnnotationReference> annotations = type.getAnnotations();
				assertEquals(1, annotations.size());
				JvmAnnotationReference annotation = annotations.get(0);
				assertEquals(1, annotation.getExplicitValues().size());
				JvmAnnotationValue value = annotation.getExplicitValues().get(0);
				assertTrue(value instanceof JvmTypeAnnotationValue);
				List<JvmTypeReference> typeLiterals = ((JvmTypeAnnotationValue) value).getValues();
				assertEquals(0, typeLiterals.size());
			} finally {
				javaFile.setContents(new StringInputStream(content), IResource.NONE, new NullProgressMonitor());
			}
		}
	}
	
	@Override @Test @Ignore
	public void testFindTypeByName_$StartsWithDollar_01() {
		// https://github.com/eclipse-xtext/xtext/issues/3708
		super.testFindTypeByName_$StartsWithDollar_01();
	}
	@Override @Test @Ignore
	public void testFindTypeByName_$StartsWithDollar_02() {
		// https://github.com/eclipse-xtext/xtext/issues/3708
		super.testFindTypeByName_$StartsWithDollar_02();
	}
	
}
