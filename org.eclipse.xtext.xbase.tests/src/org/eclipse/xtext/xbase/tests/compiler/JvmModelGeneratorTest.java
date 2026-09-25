/**
 * Copyright (c) 2012, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.compiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.Arrays;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmAnnotationAnnotationValue;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.TemporaryFolder;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.JvmAnnotationReferenceBuilder;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelCompleter;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.testing.OnTheFlyJavaCompiler2;
import org.eclipse.xtext.xbase.tests.AbstractXbaseTestCase;
import org.eclipse.xtext.xbase.tests.typesystem.XbaseWithLogicalContainerInjectorProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import foo.TestAnnotation;
import foo.TestAnnotation2;
import foo.TestAnnotations;

@RunWith(XtextRunner.class)
@InjectWith(XbaseWithLogicalContainerInjectorProvider.class)
public class JvmModelGeneratorTest extends AbstractXbaseTestCase {
	@Rule
	@Inject
	public TemporaryFolder temporaryFolder;

	@Inject
	private JvmTypesBuilder builder;

	@Inject
	private TypeReferences references;

	@Inject
	private ValidationTestHelper helper;

	@Inject
	private JvmModelGenerator generator;

	@Inject
	private OnTheFlyJavaCompiler2 javaCompiler;

	@Inject
	private TypesFactory typesFactory;

	@Inject
	private JvmModelCompleter completer;

	@Inject
	private JvmAnnotationReferenceBuilder.Factory jvmAnnotationReferenceBuilderFactory;

	@Test
	public void bug390290InnerClassMemberImport() throws Exception {
		XExpression expression = expression("null");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Outer", (JvmGenericType it) -> {
			JvmGenericType innerClass = builder.toClass(it, "InnerClass");
			JvmGenericType innerClassString = builder.toClass(it, "String");
			add(it.getMembers(), innerClass);
			add(it.getMembers(), innerClassString);
			add(it.getMembers(),
					builder.toMethod(it, "foo", references.getTypeForName(String.class, expression), (JvmOperation fooMethod) -> {
						add(fooMethod.getParameters(), builder.toParameter(it, "p1", references.createTypeRef(innerClass)));
						add(fooMethod.getParameters(), builder.toParameter(it, "p2", references.createTypeRef(innerClassString)));
						builder.setBody(fooMethod, expression);
					}));
		});
		expression.eResource().eSetDeliver(false);
		add(expression.eResource().getContents(), clazz);
		expression.eResource().eSetDeliver(true);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(expression.eResource(), fsa);
		String code = fsa.getTextFiles().get(IFileSystemAccess.DEFAULT_OUTPUT + clazz.getIdentifier().replace(".", "/") + ".java")
				.toString();
		Assert.assertFalse(code.contains("import"));
		Assert.assertTrue(code, code.contains("java.lang.String foo"));
		Class<?> compiledClass = javaCompiler.compileToClass(clazz.getIdentifier(), code);
		helper.assertNoErrors(Iterables.getFirst(expression.eResource().getContents(), null));
		Assert.assertEquals(2, compiledClass.getDeclaredClasses().length);
		Assert.assertNotNull(compiledClass.getMethod("foo", Iterables.getFirst(Arrays.asList(compiledClass.getDeclaredClasses()), null),
				Iterables.getLast(Arrays.asList(compiledClass.getDeclaredClasses()))));
	}

	@Test
	public void testSimple() throws Exception {
		XExpression expression = expression("return s.toUpperCase", false);
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName(String.class, expression), (JvmOperation o) -> {
						add(o.getParameters(), builder.toParameter(expression, "s", references.getTypeForName(String.class, expression)));
						builder.setBody(o, expression);
					}));
		});
		Class<?> compiledClass = compile(expression.eResource(), clazz);
		Object instance = compiledClass.getDeclaredConstructor().newInstance();
		Assert.assertEquals("FOO", compiledClass.getMethod("doStuff", String.class).invoke(instance, "foo"));
	}

	@Test
	public void testAnnotation_1() throws Exception {
		XExpression expression = expression("42", false);
		JvmAnnotationType clazz = builder.toAnnotationType(expression, "my.test.Foo", (JvmAnnotationType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "theTruth", references.getTypeForName(int.class, expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
					}));
		});
		Class<?> compiledClass = compile(expression.eResource(), clazz);
		Assert.assertTrue(compiledClass.isAnnotation());
		Method method = compiledClass.getMethods()[0];
		Assert.assertEquals("theTruth", method.getName());
		Assert.assertEquals(Integer.valueOf(42), method.getDefaultValue());
	}

	@Test
	public void testAnnotation_2() throws Exception {
		XExpression expression = expression("typeof(String)", false);
		JvmAnnotationType clazz = builder.toAnnotationType(expression, "my.test.Foo", (JvmAnnotationType it) -> {
			add(it.getMembers(), builder.toMethod(expression, "value",
					references.getTypeForName(Class.class, expression, references.wildCard()), (JvmOperation o) -> {
						builder.setBody(o, expression);
					}));
			add(it.getMembers(),
					builder.toMethod(expression, "otherValue", references.getTypeForName(int.class, expression), (JvmOperation o) -> {
					}));
		});
		Class<?> compiledClass = compile(expression.eResource(), clazz);
		Assert.assertTrue(compiledClass.isAnnotation());
		Assert.assertEquals(String.class, IterableExtensions
				.findFirst(Arrays.asList(compiledClass.getMethods()), (Method it) -> "value".equals(it.getName())).getDefaultValue());
		Assert.assertNull(IterableExtensions
				.findFirst(Arrays.asList(compiledClass.getMethods()), (Method it) -> "otherValue".equals(it.getName())).getDefaultValue());
	}

	@Test
	public void testImplements() throws Exception {
		XExpression expression = expression("null", false);
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			it.setAbstract(true);
			add(it.getSuperTypes(), typeRef(expression, Iterable.class, String.class));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Assert.assertTrue(Iterable.class.isAssignableFrom(compiled));
	}

	@Test
	public void testExtends() throws Exception {
		XExpression expression = expression("null", false);
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			it.setAbstract(true);
			add(it.getSuperTypes(), typeRef(expression, AbstractList.class, String.class));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Assert.assertTrue(Iterable.class.isAssignableFrom(compiled));
		Assert.assertTrue(AbstractList.class.isAssignableFrom(compiled));
	}

	@Test
	public void testCompilationStrategy() throws Exception {
		XExpression expression = expression("null", false);
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toField(expression, "x", typeRef(expression, String.class)));
			add(it.getMembers(), builder.toGetter(expression, "x", typeRef(expression, String.class)));
			add(it.getMembers(), builder.toSetter(expression, "x", typeRef(expression, String.class)));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Object inst = compiled.getDeclaredConstructor().newInstance();
		Method getter = compiled.getMethod("getX");
		Method setter = compiled.getMethod("setX", String.class);
		setter.invoke(inst, "FOO");
		Assert.assertEquals("FOO", getter.invoke(inst));
	}

	@Test
	public void testEnumeration() throws Exception {
		XExpression expression = expression("null", false);
		JvmEnumerationType enumeration = builder.toEnumerationType(expression, "my.test.Foo", (JvmEnumerationType it) -> {
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "BAR", (JvmEnumerationLiteral literal) -> {
				literal.setType(references.createTypeRef(it));
			}));
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "BAZ", (JvmEnumerationLiteral literal) -> {
				literal.setType(references.createTypeRef(it));
			}));
		});
		Class<?> compiled = compile(expression.eResource(), enumeration);
		Method valuesMethod = compiled.getMethod("values");
		Object[] values = (Object[]) valuesMethod.invoke(null);
		Assert.assertEquals("BAR", values[0].toString());
		Assert.assertEquals("BAZ", values[1].toString());
	}

	@Test
	public void testEnumeration2() throws Exception {
		XExpression expression = expression("null", false);
		JvmEnumerationType enumeration = builder.toEnumerationType(expression, "my.test.Foo");
		add(enumeration.getMembers(), builder.toEnumerationLiteral(expression, "BAR", (JvmEnumerationLiteral literal) -> {
			literal.setType(references.createTypeRef(enumeration));
		}));
		add(enumeration.getMembers(), builder.toEnumerationLiteral(expression, "BAZ", (JvmEnumerationLiteral literal) -> {
			literal.setType(references.createTypeRef(enumeration));
		}));
		Class<?> compiled = compile(expression.eResource(), enumeration);
		Method valuesMethod = compiled.getMethod("values");
		Object[] values = (Object[]) valuesMethod.invoke(null);
		Assert.assertEquals("BAR", values[0].toString());
		Assert.assertEquals("BAZ", values[1].toString());
	}

	@Test
	public void testEnumerationWithCompleter() throws Exception {
		XExpression expression = expression("null", false);
		JvmEnumerationType enumeration = builder.toEnumerationType(expression, "my.test.Foo", (JvmEnumerationType it) -> {
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "BAR"));
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "BAZ"));
		});
		expression.eResource().getContents().add(enumeration);
		completer.complete(enumeration);
		Class<?> compiled = compile(expression.eResource(), enumeration);
		Method valuesMethod = compiled.getMethod("values");
		Object[] values = (Object[]) valuesMethod.invoke(null);
		Assert.assertEquals("BAR", values[0].toString());
		Assert.assertEquals("BAZ", values[1].toString());
	}

	@Test
	public void testBug377925No_Nullpointer() throws Exception {
		XExpression expression = expression("[Object o| null]");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
					}));
		});
		compile(expression.eResource(), clazz);
	}

	@Test
	public void testBug380754() throws Exception {
		XExpression expression = expression("null");
		JvmAnnotationReferenceBuilder jvmAnnotationReferenceBuilder = jvmAnnotationReferenceBuilderFactory
				.create(expression.eResource().getResourceSet());
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
						JvmAnnotationReference annotation = jvmAnnotationReferenceBuilder.annotationRef(TestAnnotations.class);
						JvmAnnotationAnnotationValue annotationAnnotationValue = typesFactory.createJvmAnnotationAnnotationValue();
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotation.getExplicitValues(), annotationAnnotationValue);
						add(o.getAnnotations(), annotation);
					}));
		});
		compile(expression.eResource(), clazz);
	}

	@Test
	public void testBug380754_2() throws Exception {
		XExpression expression = expression("null");
		JvmAnnotationReferenceBuilder jvmAnnotationReferenceBuilder = jvmAnnotationReferenceBuilderFactory
				.create(expression.eResource().getResourceSet());
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
						JvmFormalParameter parameter = builder.toParameter(expression, "s",
								references.getTypeForName(String.class, expression));
						add(o.getParameters(), parameter);
						add(parameter.getAnnotations(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(parameter.getAnnotations(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation2.class));
					}));
		});
		compile(expression.eResource(), clazz);
	}

	@Test
	public void testBug419430() throws Exception {
		XExpression expression = expression("null");
		JvmAnnotationReferenceBuilder jvmAnnotationReferenceBuilder = jvmAnnotationReferenceBuilderFactory
				.create(expression.eResource().getResourceSet());
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
						JvmAnnotationReference annotation = jvmAnnotationReferenceBuilder.annotationRef(TestAnnotations.class);
						JvmAnnotationAnnotationValue annotationAnnotationValue = typesFactory.createJvmAnnotationAnnotationValue();
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotationAnnotationValue.getValues(), jvmAnnotationReferenceBuilder.annotationRef(TestAnnotation.class));
						add(annotation.getExplicitValues(), annotationAnnotationValue);
						add(o.getAnnotations(), annotation);
					}));
		});
		String code = generate(expression.eResource(), clazz);
		Assert.assertTrue(code, code.contains("@TestAnnotations({ @TestAnnotation, @TestAnnotation, @TestAnnotation })"));
		Class<?> compiledClazz = compileToClass(expression.eResource(), clazz, code);
		Method method = compiledClazz.getMethod("doStuff");
		TestAnnotations methodAnnotation = method.getAnnotation(TestAnnotations.class);
		Assert.assertEquals(3, methodAnnotation.value().length);
	}

	@Test
	public void testBug377002() throws Exception {
		XExpression expression = expression("null");
		JvmEnumerationType clazz = builder.toEnumerationType(expression, "my.test.Level", (JvmEnumerationType it) -> {
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "WARN", (JvmEnumerationLiteral literal) -> {
				literal.setType(references.createTypeRef(it));
			}));
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "ERROR", (JvmEnumerationLiteral literal) -> {
				literal.setType(references.createTypeRef(it));
			}));
			add(it.getMembers(), builder.toEnumerationLiteral(expression, "DEBUG", (JvmEnumerationLiteral literal) -> {
				literal.setType(references.createTypeRef(it));
			}));
			add(it.getMembers(),
					builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
					}));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Assert.assertNotNull(compiled.getField("WARN"));
		Assert.assertNotNull(compiled.getField("ERROR"));
		Assert.assertNotNull(compiled.getField("DEBUG"));
		Assert.assertNotNull(compiled.getMethod("doStuff"));
	}

	@Test
	public void testBug426442_EnclosingClassMethodCall() throws Exception {
		XExpression expression = expression("enclosingClassMethod", false);
		JvmGenericType enclosingClass = builder.toClass(expression, "my.test.EnclosingClass", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toMethod(expression, "enclosingClassMethod", references.getTypeForName(String.class, expression),
					(JvmOperation o) -> {
						builder.setBody(o, (ITreeAppendable a) -> {
							a.append("return \"enclosingClassMethodResult\";");
						});
					}));
			add(it.getMembers(), builder.toClass(expression, it.getQualifiedName() + ".InnerClass", (JvmGenericType t) -> {
				add(t.getMembers(), builder.toMethod(expression, "enclosingClassMethodCall",
						references.getTypeForName(String.class, expression), (JvmOperation o) -> {
							builder.setBody(o, expression);
						}));
			}));
		});
		Class<?> compiledEnclosingClass = compile(expression.eResource(), enclosingClass);
		Assert.assertNotNull(compiledEnclosingClass);
		Class<?> compiledInnerClass = IterableExtensions.findFirst(Arrays.asList(compiledEnclosingClass.getDeclaredClasses()),
				(Class<?> it) -> "InnerClass".equals(it.getSimpleName()));
		Assert.assertNotNull(compiledInnerClass);
		Object enclosingClassInstance = compiledEnclosingClass.getDeclaredConstructor().newInstance();
		Assert.assertNotNull(enclosingClassInstance);
		Constructor<?> innerClassConstructor = compiledInnerClass.getDeclaredConstructor(compiledEnclosingClass);
		Assert.assertNotNull(innerClassConstructor);
		Object innerClassInstance = innerClassConstructor.newInstance(enclosingClassInstance);
		Assert.assertNotNull(innerClassInstance);
		Method enclosingClassMethodCallMethod = compiledInnerClass.getMethod("enclosingClassMethodCall");
		Assert.assertNotNull(enclosingClassMethodCallMethod);
		Object invocationResult = enclosingClassMethodCallMethod.invoke(innerClassInstance);
		Assert.assertNotNull(invocationResult);
		Assert.assertEquals("enclosingClassMethodResult", invocationResult);
	}

	@Test
	public void testBug426442_InnerClassLocalMethodCall() throws Exception {
		XExpression expression = expression("innerClassMethod", false);
		JvmGenericType enclosingClass = builder.toClass(expression, "my.test.EnclosingClass", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toClass(expression, it.getQualifiedName() + ".InnerClass", (JvmGenericType t) -> {
				add(t.getMembers(), builder.toMethod(expression, "innerClassMethod", references.getTypeForName(String.class, expression),
						(JvmOperation o) -> {
							builder.setBody(o, (ITreeAppendable a) -> {
								a.append("return \"innerClassMethodResult\";");
							});
						}));
				add(t.getMembers(), builder.toMethod(expression, "innerClassMethodCall",
						references.getTypeForName(String.class, expression), (JvmOperation o) -> {
							builder.setBody(o, expression);
						}));
			}));
		});
		Class<?> compiledEnclosingClass = compile(expression.eResource(), enclosingClass);
		Assert.assertNotNull(compiledEnclosingClass);
		Class<?> compiledInnerClass = IterableExtensions.findFirst(Arrays.asList(compiledEnclosingClass.getDeclaredClasses()),
				(Class<?> it) -> "InnerClass".equals(it.getSimpleName()));
		Assert.assertNotNull(compiledInnerClass);
		Object enclosingClassInstance = compiledEnclosingClass.getDeclaredConstructor().newInstance();
		Assert.assertNotNull(enclosingClassInstance);
		Constructor<?> innerClassConstructor = compiledInnerClass.getDeclaredConstructor(compiledEnclosingClass);
		Assert.assertNotNull(innerClassConstructor);
		Object innerClassInstance = innerClassConstructor.newInstance(enclosingClassInstance);
		Assert.assertNotNull(innerClassInstance);
		Method enclosingClassMethodCallMethod = compiledInnerClass.getMethod("innerClassMethodCall");
		Assert.assertNotNull(enclosingClassMethodCallMethod);
		Object invocationResult = enclosingClassMethodCallMethod.invoke(innerClassInstance);
		Assert.assertNotNull(invocationResult);
		Assert.assertEquals("innerClassMethodResult", invocationResult);
	}

	@Test
	public void testBug426442_InnerStaticClassLocalMethodCall() throws Exception {
		XExpression expression = expression("innerStaticClassMethod", false);
		JvmGenericType enclosingClass = builder.toClass(expression, "my.test.EnclosingClass", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toClass(expression, it.getQualifiedName() + ".InnerStaticClass", (JvmGenericType t) -> {
				t.setStatic(true);
				add(t.getMembers(), builder.toMethod(expression, "innerStaticClassMethod",
						references.getTypeForName(String.class, expression), (JvmOperation o) -> {
							builder.setBody(o, (ITreeAppendable a) -> {
								a.append("return \"innerStaticClassMethodResult\";");
							});
						}));
				add(t.getMembers(), builder.toMethod(expression, "innerStaticClassMethodCall",
						references.getTypeForName(String.class, expression), (JvmOperation o) -> {
							builder.setBody(o, expression);
						}));
			}));
		});
		Class<?> compiledEnclosingClass = compile(expression.eResource(), enclosingClass);
		Assert.assertNotNull(compiledEnclosingClass);
		Class<?> compiledInnerStaticClass = IterableExtensions.findFirst(Arrays.asList(compiledEnclosingClass.getDeclaredClasses()),
				(Class<?> it) -> "InnerStaticClass".equals(it.getSimpleName()));
		Assert.assertNotNull(compiledInnerStaticClass);
		Object innerStaticClassInstance = compiledInnerStaticClass.getDeclaredConstructor().newInstance();
		Assert.assertNotNull(innerStaticClassInstance);
		Method enclosingClassMethodCallMethod = compiledInnerStaticClass.getMethod("innerStaticClassMethodCall");
		Assert.assertNotNull(enclosingClassMethodCallMethod);
		Object invocationResult = enclosingClassMethodCallMethod.invoke(innerStaticClassInstance);
		Assert.assertNotNull(invocationResult);
		Assert.assertEquals("innerStaticClassMethodResult", invocationResult);
	}

	@Test
	public void testNestedAnnotationType() throws Exception {
		XExpression expression = expression("42");
		JvmGenericType outerClass = builder.toClass(expression, "my.outer.Clazz");
		add(outerClass.getMembers(), builder.toAnnotationType(expression, "MyAnnotation", (JvmAnnotationType it) -> {
			add(it.getMembers(),
					builder.toMethod(expression, "theTruth", references.getTypeForName(int.class, expression), (JvmOperation o) -> {
						builder.setBody(o, expression);
					}));
		}));
		Class<?> compiled = Iterables.getFirst(Arrays.asList(compile(expression.eResource(), outerClass).getDeclaredClasses()), null);
		Assert.assertNotNull(compiled);
		Assert.assertEquals("my.outer.Clazz.MyAnnotation", compiled.getCanonicalName());
		Assert.assertEquals(Integer.valueOf(42), Iterables.getFirst(Arrays.asList(compiled.getDeclaredMethods()), null).getDefaultValue());
	}

	@Test
	public void testNestedEnumerationType() throws Exception {
		XExpression expression = expression("null");
		JvmGenericType outerClass = builder.toClass(expression, "my.outer.Clazz");
		add(outerClass.getMembers(),
				builder.toEnumerationType(expression, "Level", (JvmEnumerationType it) -> {
					add(it.getMembers(), builder.toEnumerationLiteral(expression, "WARN", (JvmEnumerationLiteral literal) -> {
						literal.setType(references.createTypeRef(it));
					}));
					add(it.getMembers(), builder.toEnumerationLiteral(expression, "ERROR", (JvmEnumerationLiteral literal) -> {
						literal.setType(references.createTypeRef(it));
					}));
					add(it.getMembers(), builder.toEnumerationLiteral(expression, "DEBUG", (JvmEnumerationLiteral literal) -> {
						literal.setType(references.createTypeRef(it));
					}));
					add(it.getMembers(), builder.toMethod(expression, "doStuff", references.getTypeForName("java.lang.Object", expression),
							(JvmOperation o) -> {
								builder.setBody(o, expression);
							}));
				}));
		Class<?> compiled = Iterables.getFirst(Arrays.asList(compile(expression.eResource(), outerClass).getDeclaredClasses()), null);
		Assert.assertNotNull(compiled);
		Assert.assertNotNull(compiled.getField("WARN"));
		Assert.assertNotNull(compiled.getField("ERROR"));
		Assert.assertNotNull(compiled.getField("DEBUG"));
		Assert.assertNotNull(compiled.getMethod("doStuff"));
	}

	@Test
	public void testClassModifiers() throws Exception {
		XExpression expression = expression("null");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toClass(expression, "AbstractClass", (JvmGenericType t) -> {
				t.setAbstract(true);
			}));
			add(it.getMembers(), builder.toClass(expression, "StaticClass", (JvmGenericType t) -> {
				t.setStatic(true);
			}));
			add(it.getMembers(), builder.toClass(expression, "FinalClass", (JvmGenericType t) -> {
				t.setFinal(true);
			}));
			add(it.getMembers(), builder.toClass(expression, "StrictFpClass", (JvmGenericType t) -> {
				t.setStrictFloatingPoint(true);
			}));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Class<?>[] classes = compiled.getClasses();
		Assert.assertTrue(Modifier.isAbstract(IterableExtensions
				.findFirst(Arrays.asList(classes), (Class<?> it) -> it.getName().endsWith("AbstractClass")).getModifiers()));
		Assert.assertTrue(Modifier.isStatic(IterableExtensions
				.findFirst(Arrays.asList(classes), (Class<?> it) -> it.getName().endsWith("StaticClass")).getModifiers()));
		Assert.assertTrue(Modifier.isFinal(
				IterableExtensions.findFirst(Arrays.asList(classes), (Class<?> it) -> it.getName().endsWith("FinalClass")).getModifiers()));
	}

	@Test
	public void testFieldModifiers() throws Exception {
		XExpression expression = expression("null");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toField(expression, "staticField", typeRef(expression, Integer.TYPE), (JvmField f) -> {
				f.setStatic(true);
				f.setVisibility(JvmVisibility.PUBLIC);
			}));
			add(it.getMembers(), builder.toField(expression, "finalField", typeRef(expression, Integer.TYPE), (JvmField f) -> {
				f.setFinal(true);
				builder.setInitializer(f, (ITreeAppendable a) -> {
					a.append("0");
				});
				f.setVisibility(JvmVisibility.PUBLIC);
			}));
			add(it.getMembers(), builder.toField(expression, "volatileField", typeRef(expression, Integer.TYPE), (JvmField f) -> {
				f.setVolatile(true);
				f.setVisibility(JvmVisibility.PUBLIC);
			}));
			add(it.getMembers(), builder.toField(expression, "transientField", typeRef(expression, Integer.TYPE), (JvmField f) -> {
				f.setTransient(true);
				f.setVisibility(JvmVisibility.PUBLIC);
			}));
		});
		Class<?> compiled = compile(expression.eResource(), clazz);
		Assert.assertTrue(Modifier.isStatic(compiled.getField("staticField").getModifiers()));
		Assert.assertTrue(Modifier.isFinal(compiled.getField("finalField").getModifiers()));
		Assert.assertTrue(Modifier.isVolatile(compiled.getField("volatileField").getModifiers()));
		Assert.assertTrue(Modifier.isTransient(compiled.getField("transientField").getModifiers()));
	}

	@Test
	public void testMethodModifiers() throws Exception {
		XExpression expression = expression("null");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toMethod(expression, "staticMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setStatic(true);
				builder.setBody(o, (ITreeAppendable a) -> {
					a.append("");
				});
			}));
			add(it.getMembers(), builder.toMethod(expression, "finalMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setFinal(true);
				builder.setBody(o, (ITreeAppendable a) -> {
					a.append("");
				});
			}));
			add(it.getMembers(), builder.toMethod(expression, "abstractMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setAbstract(true);
			}));
			add(it.getMembers(), builder.toMethod(expression, "synchronizedMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setSynchronized(true);
				builder.setBody(o, (ITreeAppendable a) -> {
					a.append("");
				});
			}));
			add(it.getMembers(), builder.toMethod(expression, "strictFpMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setStrictFloatingPoint(true);
				builder.setBody(o, (ITreeAppendable a) -> {
					a.append("");
				});
			}));
			add(it.getMembers(), builder.toMethod(expression, "nativeMethod", typeRef(expression, Void.TYPE), (JvmOperation o) -> {
				o.setNative(true);
			}));
			it.setAbstract(true);
		});
		String code = generate(expression.eResource(), clazz);
		// since Java 17 (JEP 306), javac/ecj no longer emit the ACC_STRICT class file flag, so
		// strictfp can only be verified in the generated source, not via Modifier.isStrict(..)
		Assert.assertTrue(code, code.contains("strictfp"));
		Class<?> compiled = compileToClass(expression.eResource(), clazz, code);
		Assert.assertTrue(Modifier.isStatic(compiled.getMethod("staticMethod").getModifiers()));
		Assert.assertTrue(Modifier.isFinal(compiled.getMethod("finalMethod").getModifiers()));
		Assert.assertTrue(Modifier.isAbstract(compiled.getMethod("abstractMethod").getModifiers()));
		Assert.assertTrue(Modifier.isSynchronized(compiled.getMethod("synchronizedMethod").getModifiers()));
		Assert.assertNotNull(compiled.getMethod("strictFpMethod"));
		Assert.assertTrue(Modifier.isNative(compiled.getMethod("nativeMethod").getModifiers()));
	}

	@Test
	public void testBug426073() throws Exception {
		XExpression expression = expression("org.eclipse.emf.common.util.URI.createURI(\"dummy\")");
		JvmGenericType clazz = builder.toClass(expression, "my.test.Foo", (JvmGenericType it) -> {
			add(it.getMembers(), builder.toField(expression, "URI", typeRef(expression, URI.class), (JvmField f) -> {
				builder.setInitializer(f, expression);
			}));
		});
		compile(expression.eResource(), clazz);
	}

	public <T> boolean add(EList<? super T> list, T element) {
		return builder.operator_add(list, element);
	}

	public JvmTypeReference typeRef(EObject ctx, Class<?> clazz) {
		return references.getTypeForName(clazz, ctx);
	}

	public JvmTypeReference typeRef(EObject ctx, Class<?> clazz, Class<?> param) {
		return references.getTypeForName(clazz, ctx, typeRef(ctx, param));
	}

	public Class<?> compile(Resource res, JvmDeclaredType type) {
		return compileToClass(res, type, generate(res, type));
	}

	public String generate(Resource res, JvmDeclaredType type) {
		res.eSetDeliver(false);
		add(res.getContents(), type);
		res.eSetDeliver(true);
		InMemoryFileSystemAccess fsa = new InMemoryFileSystemAccess();
		generator.doGenerate(res, fsa);
		return fsa.getTextFiles().get(IFileSystemAccess.DEFAULT_OUTPUT + type.getIdentifier().replace(".", "/") + ".java").toString();
	}

	public Class<?> compileToClass(Resource res, JvmDeclaredType type, String code) {
		Class<?> compiledClass = javaCompiler.compileToClass(type.getIdentifier(), code);
		helper.assertNoErrors(Iterables.getFirst(res.getContents(), null));
		return compiledClass;
	}
}
