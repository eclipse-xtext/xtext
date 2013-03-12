/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.tests.compiler;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Provider;
import foo.TestAnnotation;
import foo.TestAnnotation2;
import foo.TestAnnotations;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.common.types.JvmAnnotationAnnotationValue;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmWildcardTypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.InMemoryFileSystemAccess;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.eclipse.xtext.xbase.compiler.OnTheFlyJavaCompiler.EclipseRuntimeDependentJavaCompiler;
import org.eclipse.xtext.xbase.junit.evaluation.AbstractXbaseEvaluationTest;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelCompleter;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.tests.AbstractXbaseTestCase;
import org.eclipse.xtext.xbase.tests.typesystem.XbaseWithLogicalContainerInjectorProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(XbaseWithLogicalContainerInjectorProvider.class)
@SuppressWarnings("all")
public class JvmModelGeneratorTest extends AbstractXbaseTestCase {
  @Inject
  @Extension
  private JvmTypesBuilder builder;
  
  @Inject
  private TypeReferences references;
  
  @Inject
  private ValidationTestHelper helper;
  
  @Inject
  private JvmModelGenerator generator;
  
  @Inject
  private EclipseRuntimeDependentJavaCompiler javaCompiler;
  
  @Inject
  private TypesFactory typesFactory;
  
  @Inject
  private JvmModelCompleter completer;
  
  @Before
  public void setUp() {
    this.javaCompiler.clearClassPath();
    Class<? extends JvmModelGeneratorTest> _class = this.getClass();
    this.javaCompiler.addClassPathOfClass(_class);
    this.javaCompiler.addClassPathOfClass(AbstractXbaseEvaluationTest.class);
    this.javaCompiler.addClassPathOfClass(Functions.class);
    this.javaCompiler.addClassPathOfClass(Provider.class);
    this.javaCompiler.addClassPathOfClass(Supplier.class);
  }
  
  @Test
  public void bug390290InnerClassMemberImport() {
    try {
      final XExpression expression = this.expression("null");
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            final JvmGenericType innerClass = JvmModelGeneratorTest.this.builder.toClass(it, "InnerClass");
            final JvmGenericType innerClassString = JvmModelGeneratorTest.this.builder.toClass(it, "String");
            EList<JvmMember> _members = it.getMembers();
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, innerClass);
            EList<JvmMember> _members_1 = it.getMembers();
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, innerClassString);
            EList<JvmMember> _members_2 = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(String.class, expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation fooMethod) {
                  EList<JvmFormalParameter> _parameters = fooMethod.getParameters();
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(innerClass);
                  JvmFormalParameter _parameter = JvmModelGeneratorTest.this.builder.toParameter(it, "p1", _createTypeRef);
                  JvmModelGeneratorTest.this.builder.<JvmFormalParameter>operator_add(_parameters, _parameter);
                  EList<JvmFormalParameter> _parameters_1 = fooMethod.getParameters();
                  JvmParameterizedTypeReference _createTypeRef_1 = JvmModelGeneratorTest.this.references.createTypeRef(innerClassString);
                  JvmFormalParameter _parameter_1 = JvmModelGeneratorTest.this.builder.toParameter(it, "p2", _createTypeRef_1);
                  JvmModelGeneratorTest.this.builder.<JvmFormalParameter>operator_add(_parameters_1, _parameter_1);
                  JvmModelGeneratorTest.this.builder.setBody(fooMethod, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(it, "foo", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_2, _method);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Outer", _function);
      Resource _eResource = expression.eResource();
      _eResource.eSetDeliver(false);
      Resource _eResource_1 = expression.eResource();
      EList<EObject> _contents = _eResource_1.getContents();
      this.builder.<EObject>operator_add(_contents, clazz);
      Resource _eResource_2 = expression.eResource();
      _eResource_2.eSetDeliver(true);
      InMemoryFileSystemAccess _inMemoryFileSystemAccess = new InMemoryFileSystemAccess();
      final InMemoryFileSystemAccess fsa = _inMemoryFileSystemAccess;
      Resource _eResource_3 = expression.eResource();
      this.generator.doGenerate(_eResource_3, fsa);
      Map<String,CharSequence> _files = fsa.getFiles();
      String _identifier = clazz.getIdentifier();
      String _replace = _identifier.replace(".", "/");
      String _plus = (IFileSystemAccess.DEFAULT_OUTPUT + _replace);
      String _plus_1 = (_plus + ".java");
      CharSequence _get = _files.get(_plus_1);
      final String code = _get.toString();
      boolean _contains = code.contains("import");
      Assert.assertFalse(_contains);
      boolean _contains_1 = code.contains("java.lang.String foo");
      Assert.assertTrue(_contains_1);
      String _identifier_1 = clazz.getIdentifier();
      final Class<? extends Object> compiledClass = this.javaCompiler.compileToClass(_identifier_1, code);
      Resource _eResource_4 = expression.eResource();
      EList<EObject> _contents_1 = _eResource_4.getContents();
      EObject _head = IterableExtensions.<EObject>head(_contents_1);
      this.helper.assertNoErrors(_head);
      Class<? extends Object>[] _declaredClasses = compiledClass.getDeclaredClasses();
      int _size = ((List<Class<? extends Object>>)Conversions.doWrapArray(_declaredClasses)).size();
      Assert.assertEquals(2, _size);
      Class<? extends Object>[] _declaredClasses_1 = compiledClass.getDeclaredClasses();
      Class<? extends Object> _head_1 = IterableExtensions.<Class<? extends Object>>head(((Iterable<Class<? extends Object>>)Conversions.doWrapArray(_declaredClasses_1)));
      Class<? extends Object>[] _declaredClasses_2 = compiledClass.getDeclaredClasses();
      Class<? extends Object> _last = IterableExtensions.<Class<? extends Object>>last(((Iterable<Class<? extends Object>>)Conversions.doWrapArray(_declaredClasses_2)));
      Method _method = compiledClass.getMethod("foo", _head_1, _last);
      Assert.assertNotNull(_method);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testSimple() {
    try {
      final XExpression expression = this.expression("return s.toUpperCase", false);
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(String.class, expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  EList<JvmFormalParameter> _parameters = it.getParameters();
                  JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(String.class, expression);
                  JvmFormalParameter _parameter = JvmModelGeneratorTest.this.builder.toParameter(expression, "s", _typeForName);
                  JvmModelGeneratorTest.this.builder.<JvmFormalParameter>operator_add(_parameters, _parameter);
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "doStuff", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiledClass = this.compile(_eResource, clazz);
      final Object instance = compiledClass.newInstance();
      Method _method = compiledClass.getMethod("doStuff", String.class);
      Object _invoke = _method.invoke(instance, "foo");
      Assert.assertEquals("FOO", _invoke);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAnnotation_1() {
    try {
      final XExpression expression = this.expression("42", false);
      final Procedure1<JvmAnnotationType> _function = new Procedure1<JvmAnnotationType>() {
          public void apply(final JvmAnnotationType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(int.class, expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "theTruth", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
          }
        };
      final JvmAnnotationType clazz = this.builder.toAnnotationType(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiledClass = this.compile(_eResource, clazz);
      boolean _isAnnotation = compiledClass.isAnnotation();
      Assert.assertTrue(_isAnnotation);
      Method[] _methods = compiledClass.getMethods();
      final Method method = IterableExtensions.<Method>head(((Iterable<Method>)Conversions.doWrapArray(_methods)));
      String _name = method.getName();
      Assert.assertEquals("theTruth", _name);
      Object _defaultValue = method.getDefaultValue();
      Assert.assertEquals(Integer.valueOf(42), _defaultValue);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAnnotation_2() {
    try {
      final XExpression expression = this.expression("typeof(String)", false);
      final Procedure1<JvmAnnotationType> _function = new Procedure1<JvmAnnotationType>() {
          public void apply(final JvmAnnotationType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmWildcardTypeReference _wildCard = JvmModelGeneratorTest.this.references.wildCard();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(Class.class, expression, _wildCard);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "value", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
            EList<JvmMember> _members_1 = it.getMembers();
            JvmTypeReference _typeForName_1 = JvmModelGeneratorTest.this.references.getTypeForName(int.class, expression);
            final Procedure1<JvmOperation> _function_1 = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                }
              };
            JvmOperation _method_1 = JvmModelGeneratorTest.this.builder.toMethod(expression, "otherValue", _typeForName_1, _function_1);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, _method_1);
          }
        };
      final JvmAnnotationType clazz = this.builder.toAnnotationType(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiledClass = this.compile(_eResource, clazz);
      boolean _isAnnotation = compiledClass.isAnnotation();
      Assert.assertTrue(_isAnnotation);
      Method[] _methods = compiledClass.getMethods();
      final Function1<Method,Boolean> _function_1 = new Function1<Method,Boolean>() {
          public Boolean apply(final Method it) {
            String _name = it.getName();
            boolean _equals = Objects.equal(_name, "value");
            return Boolean.valueOf(_equals);
          }
        };
      Method _findFirst = IterableExtensions.<Method>findFirst(((Iterable<Method>)Conversions.doWrapArray(_methods)), _function_1);
      Object _defaultValue = _findFirst.getDefaultValue();
      Assert.assertEquals(String.class, _defaultValue);
      Method[] _methods_1 = compiledClass.getMethods();
      final Function1<Method,Boolean> _function_2 = new Function1<Method,Boolean>() {
          public Boolean apply(final Method it) {
            String _name = it.getName();
            boolean _equals = Objects.equal(_name, "otherValue");
            return Boolean.valueOf(_equals);
          }
        };
      Method _findFirst_1 = IterableExtensions.<Method>findFirst(((Iterable<Method>)Conversions.doWrapArray(_methods_1)), _function_2);
      Object _defaultValue_1 = _findFirst_1.getDefaultValue();
      Assert.assertNull(_defaultValue_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testImplements() {
    try {
      final XExpression expression = this.expression("null", false);
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            it.setAbstract(true);
            EList<JvmTypeReference> _superTypes = it.getSuperTypes();
            JvmTypeReference _typeRef = JvmModelGeneratorTest.this.typeRef(expression, Iterable.class, String.class);
            JvmModelGeneratorTest.this.builder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, clazz);
      boolean _isAssignableFrom = Iterable.class.isAssignableFrom(compiled);
      Assert.assertTrue(_isAssignableFrom);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testExtends() {
    try {
      final XExpression expression = this.expression("null", false);
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            it.setAbstract(true);
            EList<JvmTypeReference> _superTypes = it.getSuperTypes();
            JvmTypeReference _typeRef = JvmModelGeneratorTest.this.typeRef(expression, AbstractList.class, String.class);
            JvmModelGeneratorTest.this.builder.<JvmTypeReference>operator_add(_superTypes, _typeRef);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, clazz);
      boolean _isAssignableFrom = Iterable.class.isAssignableFrom(compiled);
      Assert.assertTrue(_isAssignableFrom);
      boolean _isAssignableFrom_1 = AbstractList.class.isAssignableFrom(compiled);
      Assert.assertTrue(_isAssignableFrom_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCompilationStrategy() {
    try {
      final XExpression expression = this.expression("null", false);
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeRef = JvmModelGeneratorTest.this.typeRef(expression, String.class);
            JvmField _field = JvmModelGeneratorTest.this.builder.toField(expression, "x", _typeRef);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _field);
            EList<JvmMember> _members_1 = it.getMembers();
            JvmTypeReference _typeRef_1 = JvmModelGeneratorTest.this.typeRef(expression, String.class);
            JvmOperation _getter = JvmModelGeneratorTest.this.builder.toGetter(expression, "x", _typeRef_1);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, _getter);
            EList<JvmMember> _members_2 = it.getMembers();
            JvmTypeReference _typeRef_2 = JvmModelGeneratorTest.this.typeRef(expression, String.class);
            JvmOperation _setter = JvmModelGeneratorTest.this.builder.toSetter(expression, "x", _typeRef_2);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_2, _setter);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, clazz);
      final Object inst = compiled.newInstance();
      final Method getter = compiled.getMethod("getX");
      final Method setter = compiled.getMethod("setX", String.class);
      setter.invoke(inst, "FOO");
      Object _invoke = getter.invoke(inst);
      Assert.assertEquals("FOO", _invoke);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testEnumeration() {
    try {
      final XExpression expression = this.expression("null", false);
      final Procedure1<JvmEnumerationType> _function = new Procedure1<JvmEnumerationType>() {
          public void apply(final JvmEnumerationType it) {
            EList<JvmMember> _members = it.getMembers();
            final Procedure1<JvmEnumerationLiteral> _function = new Procedure1<JvmEnumerationLiteral>() {
                public void apply(final JvmEnumerationLiteral literal) {
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(it);
                  literal.setType(_createTypeRef);
                }
              };
            JvmEnumerationLiteral _enumerationLiteral = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "BAR", _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _enumerationLiteral);
            EList<JvmMember> _members_1 = it.getMembers();
            final Procedure1<JvmEnumerationLiteral> _function_1 = new Procedure1<JvmEnumerationLiteral>() {
                public void apply(final JvmEnumerationLiteral literal) {
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(it);
                  literal.setType(_createTypeRef);
                }
              };
            JvmEnumerationLiteral _enumerationLiteral_1 = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "BAZ", _function_1);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, _enumerationLiteral_1);
          }
        };
      final JvmEnumerationType enumeration = this.builder.toEnumerationType(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, enumeration);
      final Method valuesMethod = compiled.getMethod("values");
      Object _invoke = valuesMethod.invoke(null);
      final Object[] values = ((Object[]) _invoke);
      Object _get = values[0];
      String _string = _get.toString();
      Assert.assertEquals("BAR", _string);
      Object _get_1 = values[1];
      String _string_1 = _get_1.toString();
      Assert.assertEquals("BAZ", _string_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testEnumerationWithCompleter() {
    try {
      final XExpression expression = this.expression("null", false);
      final Procedure1<JvmEnumerationType> _function = new Procedure1<JvmEnumerationType>() {
          public void apply(final JvmEnumerationType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmEnumerationLiteral _enumerationLiteral = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "BAR");
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _enumerationLiteral);
            EList<JvmMember> _members_1 = it.getMembers();
            JvmEnumerationLiteral _enumerationLiteral_1 = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "BAZ");
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, _enumerationLiteral_1);
          }
        };
      final JvmEnumerationType enumeration = this.builder.toEnumerationType(expression, "my.test.Foo", _function);
      this.completer.complete(enumeration);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, enumeration);
      final Method valuesMethod = compiled.getMethod("values");
      Object _invoke = valuesMethod.invoke(null);
      final Object[] values = ((Object[]) _invoke);
      Object _get = values[0];
      String _string = _get.toString();
      Assert.assertEquals("BAR", _string);
      Object _get_1 = values[1];
      String _string_1 = _get_1.toString();
      Assert.assertEquals("BAZ", _string_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testBug377925No_Nullpointer() {
    try {
      final XExpression expression = this.expression("[Object o| null]");
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName("java.lang.Object", expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "doStuff", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      this.compile(_eResource, clazz);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testBug380754() {
    try {
      final XExpression expression = this.expression("null");
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName("java.lang.Object", expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                  final JvmAnnotationReference annotation = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotations.class);
                  final JvmAnnotationAnnotationValue annotationAnnotationValue = JvmModelGeneratorTest.this.typesFactory.createJvmAnnotationAnnotationValue();
                  EList<JvmAnnotationReference> _values = annotationAnnotationValue.getValues();
                  JvmAnnotationReference _annotation = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotation.class);
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_values, _annotation);
                  EList<JvmAnnotationReference> _values_1 = annotationAnnotationValue.getValues();
                  JvmAnnotationReference _annotation_1 = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotation.class);
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_values_1, _annotation_1);
                  EList<JvmAnnotationReference> _values_2 = annotationAnnotationValue.getValues();
                  JvmAnnotationReference _annotation_2 = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotation.class);
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_values_2, _annotation_2);
                  EList<JvmAnnotationValue> _values_3 = annotation.getValues();
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationValue>operator_add(_values_3, annotationAnnotationValue);
                  EList<JvmAnnotationReference> _annotations = it.getAnnotations();
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_annotations, annotation);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "doStuff", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      this.compile(_eResource, clazz);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testBug380754_2() {
    try {
      final XExpression expression = this.expression("null");
      final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
          public void apply(final JvmGenericType it) {
            EList<JvmMember> _members = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName("java.lang.Object", expression);
            final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                  JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName(String.class, expression);
                  final JvmFormalParameter parameter = JvmModelGeneratorTest.this.builder.toParameter(expression, "s", _typeForName);
                  EList<JvmFormalParameter> _parameters = it.getParameters();
                  JvmModelGeneratorTest.this.builder.<JvmFormalParameter>operator_add(_parameters, parameter);
                  EList<JvmAnnotationReference> _annotations = parameter.getAnnotations();
                  JvmAnnotationReference _annotation = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotation.class);
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_annotations, _annotation);
                  EList<JvmAnnotationReference> _annotations_1 = parameter.getAnnotations();
                  JvmAnnotationReference _annotation_1 = JvmModelGeneratorTest.this.builder.toAnnotation(expression, TestAnnotation2.class);
                  JvmModelGeneratorTest.this.builder.<JvmAnnotationReference>operator_add(_annotations_1, _annotation_1);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "doStuff", _typeForName, _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _method);
          }
        };
      final JvmGenericType clazz = this.builder.toClass(expression, "my.test.Foo", _function);
      Resource _eResource = expression.eResource();
      this.compile(_eResource, clazz);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testBug377002() {
    try {
      final XExpression expression = this.expression("null");
      final Procedure1<JvmEnumerationType> _function = new Procedure1<JvmEnumerationType>() {
          public void apply(final JvmEnumerationType it) {
            EList<JvmMember> _members = it.getMembers();
            final Procedure1<JvmEnumerationLiteral> _function = new Procedure1<JvmEnumerationLiteral>() {
                public void apply(final JvmEnumerationLiteral literal) {
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(it);
                  literal.setType(_createTypeRef);
                }
              };
            JvmEnumerationLiteral _enumerationLiteral = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "WARN", _function);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members, _enumerationLiteral);
            EList<JvmMember> _members_1 = it.getMembers();
            final Procedure1<JvmEnumerationLiteral> _function_1 = new Procedure1<JvmEnumerationLiteral>() {
                public void apply(final JvmEnumerationLiteral literal) {
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(it);
                  literal.setType(_createTypeRef);
                }
              };
            JvmEnumerationLiteral _enumerationLiteral_1 = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "ERROR", _function_1);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_1, _enumerationLiteral_1);
            EList<JvmMember> _members_2 = it.getMembers();
            final Procedure1<JvmEnumerationLiteral> _function_2 = new Procedure1<JvmEnumerationLiteral>() {
                public void apply(final JvmEnumerationLiteral literal) {
                  JvmParameterizedTypeReference _createTypeRef = JvmModelGeneratorTest.this.references.createTypeRef(it);
                  literal.setType(_createTypeRef);
                }
              };
            JvmEnumerationLiteral _enumerationLiteral_2 = JvmModelGeneratorTest.this.builder.toEnumerationLiteral(expression, "DEBUG", _function_2);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_2, _enumerationLiteral_2);
            EList<JvmMember> _members_3 = it.getMembers();
            JvmTypeReference _typeForName = JvmModelGeneratorTest.this.references.getTypeForName("java.lang.Object", expression);
            final Procedure1<JvmOperation> _function_3 = new Procedure1<JvmOperation>() {
                public void apply(final JvmOperation it) {
                  JvmModelGeneratorTest.this.builder.setBody(it, expression);
                }
              };
            JvmOperation _method = JvmModelGeneratorTest.this.builder.toMethod(expression, "doStuff", _typeForName, _function_3);
            JvmModelGeneratorTest.this.builder.<JvmMember>operator_add(_members_3, _method);
          }
        };
      final JvmEnumerationType clazz = this.builder.toEnumerationType(expression, "my.test.Level", _function);
      Resource _eResource = expression.eResource();
      final Class<? extends Object> compiled = this.compile(_eResource, clazz);
      Field _field = compiled.getField("WARN");
      Assert.assertNotNull(_field);
      Field _field_1 = compiled.getField("ERROR");
      Assert.assertNotNull(_field_1);
      Field _field_2 = compiled.getField("DEBUG");
      Assert.assertNotNull(_field_2);
      Method _method = compiled.getMethod("doStuff");
      Assert.assertNotNull(_method);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public JvmTypeReference typeRef(final EObject ctx, final Class<? extends Object> clazz) {
    return this.references.getTypeForName(clazz, ctx);
  }
  
  public JvmTypeReference typeRef(final EObject ctx, final Class<? extends Object> clazz, final Class<? extends Object> param) {
    JvmTypeReference _typeRef = this.typeRef(ctx, param);
    return this.references.getTypeForName(clazz, ctx, _typeRef);
  }
  
  public Class<? extends Object> compile(final Resource res, final JvmDeclaredType type) {
    res.eSetDeliver(false);
    EList<EObject> _contents = res.getContents();
    this.builder.<EObject>operator_add(_contents, type);
    res.eSetDeliver(true);
    InMemoryFileSystemAccess _inMemoryFileSystemAccess = new InMemoryFileSystemAccess();
    final InMemoryFileSystemAccess fsa = _inMemoryFileSystemAccess;
    this.generator.doGenerate(res, fsa);
    Map<String,CharSequence> _files = fsa.getFiles();
    String _identifier = type.getIdentifier();
    String _replace = _identifier.replace(".", "/");
    String _plus = (IFileSystemAccess.DEFAULT_OUTPUT + _replace);
    String _plus_1 = (_plus + ".java");
    CharSequence _get = _files.get(_plus_1);
    final String code = _get.toString();
    String _identifier_1 = type.getIdentifier();
    final Class<? extends Object> compiledClass = this.javaCompiler.compileToClass(_identifier_1, code);
    EList<EObject> _contents_1 = res.getContents();
    EObject _head = IterableExtensions.<EObject>head(_contents_1);
    this.helper.assertNoErrors(_head);
    return compiledClass;
  }
}
