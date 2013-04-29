package org.eclipse.xtend.core.tests.macro.declaration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.lang.reflect.AccessibleObject;
import java.util.Collections;
import java.util.List;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.TypeLookupImpl;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeElementDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy.CompilationContext;
import org.eclipse.xtend.lib.macro.declaration.CompilationUnit;
import org.eclipse.xtend.lib.macro.declaration.ConstructorDeclaration;
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.InterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclarator;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend.lib.macro.services.TypeReferenceProvider;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class DeclarationsTest extends AbstractXtendTestCase {
  @Inject
  private Provider<CompilationUnitImpl> compilationUnitProvider;
  
  @Test
  public void testAnnotation() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("@SuppressWarnings(\"unused\")");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("@com.google.inject.Inject(optional=true) MyClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          String _packageName = it.getPackageName();
          Assert.assertNull(_packageName);
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _head = IterableExtensions.head(_sourceTypeDeclarations);
          final ClassDeclaration clazz = ((ClassDeclaration) _head);
          String _qualifiedName = clazz.getQualifiedName();
          Assert.assertEquals("MyClass", _qualifiedName);
          Iterable<? extends AnnotationReference> _annotations = clazz.getAnnotations();
          final AnnotationReference suppressWarning = IterableExtensions.head(_annotations);
          final AnnotationTypeDeclaration supressWarningsDeclaration = suppressWarning.getAnnotationTypeDeclaration();
          String _qualifiedName_1 = supressWarningsDeclaration.getQualifiedName();
          Assert.assertEquals("java.lang.SuppressWarnings", _qualifiedName_1);
          Object _value = suppressWarning.getValue("value");
          Assert.assertEquals("unused", _value);
          Iterable<? extends AnnotationReference> _annotations_1 = supressWarningsDeclaration.getAnnotations();
          int _size = IterableExtensions.size(_annotations_1);
          Assert.assertEquals(2, _size);
          Iterable<? extends MemberDeclaration> _declaredMembers = supressWarningsDeclaration.getDeclaredMembers();
          Iterable<AnnotationTypeElementDeclaration> _filter = Iterables.<AnnotationTypeElementDeclaration>filter(_declaredMembers, AnnotationTypeElementDeclaration.class);
          final AnnotationTypeElementDeclaration valueProperty = IterableExtensions.<AnnotationTypeElementDeclaration>head(_filter);
          TypeReference _type = valueProperty.getType();
          String _string = _type.toString();
          Assert.assertEquals("String[]", _string);
          String _simpleName = valueProperty.getSimpleName();
          Assert.assertEquals("value", _simpleName);
          Iterable<? extends MemberDeclaration> _declaredMembers_1 = clazz.getDeclaredMembers();
          MemberDeclaration _head_1 = IterableExtensions.head(_declaredMembers_1);
          final FieldDeclaration field = ((FieldDeclaration) _head_1);
          Iterable<? extends AnnotationReference> _annotations_2 = field.getAnnotations();
          final AnnotationReference inject = IterableExtensions.head(_annotations_2);
          Object _value_1 = inject.getValue("optional");
          Assert.assertTrue((((Boolean) _value_1)).booleanValue());
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testAnnotation2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("@com.google.inject.Inject() MyClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _head = IterableExtensions.head(_sourceTypeDeclarations);
          final ClassDeclaration sourceClazz = ((ClassDeclaration) _head);
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          final MutableClassDeclaration javaClass = _typeLookup.findClass("MyClass");
          String _qualifiedName = javaClass.getQualifiedName();
          String _qualifiedName_1 = sourceClazz.getQualifiedName();
          Assert.assertEquals(_qualifiedName, _qualifiedName_1);
          Iterable<? extends FieldDeclaration> _declaredFields = sourceClazz.getDeclaredFields();
          final FieldDeclaration field = IterableExtensions.head(_declaredFields);
          Iterable<? extends AnnotationReference> _annotations = field.getAnnotations();
          AnnotationReference _head_1 = IterableExtensions.head(_annotations);
          Object _value = _head_1.getValue("optional");
          Assert.assertNull(_value);
          Iterable<? extends MutableFieldDeclaration> _declaredFields_1 = javaClass.getDeclaredFields();
          final MutableFieldDeclaration javaField = IterableExtensions.head(_declaredFields_1);
          Iterable<? extends MutableAnnotationReference> _annotations_1 = javaField.getAnnotations();
          MutableAnnotationReference _head_2 = IterableExtensions.head(_annotations_1);
          Object _value_1 = _head_2.getValue("optional");
          Assert.assertFalse((((Boolean) _value_1)).booleanValue());
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testSimpleClassWithField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class MyClass extends Object implements java.io.Serializable {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("MyClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          String _packageName = it.getPackageName();
          Assert.assertEquals("foo", _packageName);
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _head = IterableExtensions.head(_sourceTypeDeclarations);
          final ClassDeclaration clazz = ((ClassDeclaration) _head);
          String _qualifiedName = clazz.getQualifiedName();
          Assert.assertEquals("foo.MyClass", _qualifiedName);
          TypeReference _extendedClass = clazz.getExtendedClass();
          String _string = _extendedClass.toString();
          Assert.assertEquals("Object", _string);
          List<TypeReference> _implementedInterfaces = clazz.getImplementedInterfaces();
          TypeReference _head_1 = IterableExtensions.<TypeReference>head(_implementedInterfaces);
          String _string_1 = _head_1.toString();
          Assert.assertEquals("Serializable", _string_1);
          Iterable<? extends MemberDeclaration> _declaredMembers = clazz.getDeclaredMembers();
          MemberDeclaration _head_2 = IterableExtensions.head(_declaredMembers);
          final FieldDeclaration field = ((FieldDeclaration) _head_2);
          String _simpleName = field.getSimpleName();
          Assert.assertEquals("foo", _simpleName);
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          MutableClassDeclaration _findClass = _typeLookup.findClass("foo.MyClass");
          TypeReference _type = field.getType();
          Type _type_1 = _type.getType();
          Assert.assertSame(_findClass, _type_1);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testRemove() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class C {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def void m() {}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _head = IterableExtensions.head(_sourceTypeDeclarations);
          final ClassDeclaration c = ((ClassDeclaration) _head);
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          String _qualifiedName = c.getQualifiedName();
          final MutableClassDeclaration mutable = _typeLookup.findClass(_qualifiedName);
          Iterable<? extends MutableMemberDeclaration> _declaredMembers = mutable.getDeclaredMembers();
          final Procedure1<MutableMemberDeclaration> _function = new Procedure1<MutableMemberDeclaration>() {
              public void apply(final MutableMemberDeclaration it) {
                it.remove();
              }
            };
          IterableExtensions.forEach(_declaredMembers, _function);
          Iterable<? extends MutableMemberDeclaration> _declaredMembers_1 = mutable.getDeclaredMembers();
          boolean _isEmpty = IterableExtensions.isEmpty(_declaredMembers_1);
          Assert.assertTrue(_isEmpty);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testXtendClassWithMethodFieldAndConstructor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class MyClass<T extends CharSequence> {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("String myField");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("new(String initial) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.myField = initial");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def <T2 extends CharSequence> MyClass myMethod(T2 a, T b) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("myField = myField + a + b");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return this");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          String _packageName = it.getPackageName();
          Assert.assertEquals("foo", _packageName);
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _head = IterableExtensions.head(_sourceTypeDeclarations);
          final ClassDeclaration clazz = ((ClassDeclaration) _head);
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          final MutableClassDeclaration genClazz = _typeLookup.findClass("foo.MyClass");
          String _qualifiedName = clazz.getQualifiedName();
          Assert.assertEquals("foo.MyClass", _qualifiedName);
          TypeReference _extendedClass = clazz.getExtendedClass();
          Assert.assertNull(_extendedClass);
          List<TypeReference> _implementedInterfaces = clazz.getImplementedInterfaces();
          boolean _isEmpty = _implementedInterfaces.isEmpty();
          Assert.assertTrue(_isEmpty);
          Iterable<? extends MemberDeclaration> _declaredMembers = clazz.getDeclaredMembers();
          int _size = IterableExtensions.size(_declaredMembers);
          Assert.assertEquals(3, _size);
          Iterable<? extends TypeParameterDeclaration> _typeParameters = clazz.getTypeParameters();
          TypeParameterDeclaration _head_1 = IterableExtensions.head(_typeParameters);
          String _simpleName = _head_1.getSimpleName();
          Assert.assertEquals("T", _simpleName);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_1 = clazz.getTypeParameters();
          TypeParameterDeclaration _head_2 = IterableExtensions.head(_typeParameters_1);
          Iterable<? extends TypeReference> _upperBounds = _head_2.getUpperBounds();
          TypeReference _head_3 = IterableExtensions.head(_upperBounds);
          String _string = _head_3.toString();
          Assert.assertEquals("CharSequence", _string);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_2 = clazz.getTypeParameters();
          TypeParameterDeclaration _head_4 = IterableExtensions.head(_typeParameters_2);
          TypeParameterDeclarator _typeParameterDeclarator = _head_4.getTypeParameterDeclarator();
          Assert.assertSame(clazz, _typeParameterDeclarator);
          Iterable<? extends FieldDeclaration> _declaredFields = clazz.getDeclaredFields();
          final FieldDeclaration field = IterableExtensions.head(_declaredFields);
          TypeDeclaration _declaringType = field.getDeclaringType();
          Assert.assertSame(clazz, _declaringType);
          String _simpleName_1 = field.getSimpleName();
          Assert.assertEquals("myField", _simpleName_1);
          TypeReference _type = field.getType();
          String _string_1 = _type.toString();
          Assert.assertEquals("String", _string_1);
          boolean _isFinal = field.isFinal();
          Assert.assertFalse(_isFinal);
          Iterable<? extends ConstructorDeclaration> _declaredConstructors = clazz.getDeclaredConstructors();
          final ConstructorDeclaration constructor = IterableExtensions.head(_declaredConstructors);
          TypeDeclaration _declaringType_1 = constructor.getDeclaringType();
          Assert.assertSame(clazz, _declaringType_1);
          String _simpleName_2 = constructor.getSimpleName();
          Assert.assertEquals("MyClass", _simpleName_2);
          List<? extends ParameterDeclaration> _parameters = constructor.getParameters();
          ParameterDeclaration _head_5 = IterableExtensions.head(_parameters);
          String _simpleName_3 = _head_5.getSimpleName();
          Assert.assertEquals("initial", _simpleName_3);
          List<? extends ParameterDeclaration> _parameters_1 = constructor.getParameters();
          ParameterDeclaration _head_6 = IterableExtensions.head(_parameters_1);
          TypeReference _type_1 = _head_6.getType();
          String _string_2 = _type_1.toString();
          Assert.assertEquals("String", _string_2);
          Iterable<? extends MethodDeclaration> _declaredMethods = clazz.getDeclaredMethods();
          final MethodDeclaration method = IterableExtensions.head(_declaredMethods);
          Iterable<? extends MutableMethodDeclaration> _declaredMethods_1 = genClazz.getDeclaredMethods();
          final MutableMethodDeclaration genMethod = IterableExtensions.head(_declaredMethods_1);
          TypeDeclaration _declaringType_2 = method.getDeclaringType();
          Assert.assertSame(clazz, _declaringType_2);
          List<? extends ParameterDeclaration> _parameters_2 = method.getParameters();
          ParameterDeclaration _head_7 = IterableExtensions.head(_parameters_2);
          String _simpleName_4 = _head_7.getSimpleName();
          Assert.assertEquals("a", _simpleName_4);
          List<? extends ParameterDeclaration> _parameters_3 = method.getParameters();
          ParameterDeclaration _head_8 = IterableExtensions.head(_parameters_3);
          TypeReference _type_2 = _head_8.getType();
          String _string_3 = _type_2.toString();
          Assert.assertEquals("T2", _string_3);
          List<MutableTypeParameterDeclaration> _typeParameters_3 = genMethod.getTypeParameters();
          MutableTypeParameterDeclaration _head_9 = IterableExtensions.<MutableTypeParameterDeclaration>head(_typeParameters_3);
          List<? extends ParameterDeclaration> _parameters_4 = method.getParameters();
          ParameterDeclaration _head_10 = IterableExtensions.head(_parameters_4);
          TypeReference _type_3 = _head_10.getType();
          Type _type_4 = _type_3.getType();
          Assert.assertSame(_head_9, _type_4);
          List<? extends ParameterDeclaration> _parameters_5 = method.getParameters();
          ParameterDeclaration _get = _parameters_5.get(1);
          TypeReference _type_5 = _get.getType();
          String _string_4 = _type_5.toString();
          Assert.assertEquals("T", _string_4);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_4 = genClazz.getTypeParameters();
          TypeParameterDeclaration _head_11 = IterableExtensions.head(_typeParameters_4);
          List<? extends ParameterDeclaration> _parameters_6 = method.getParameters();
          ParameterDeclaration _get_1 = _parameters_6.get(1);
          TypeReference _type_6 = _get_1.getType();
          Type _type_7 = _type_6.getType();
          Assert.assertSame(_head_11, _type_7);
          TypeReference _returnType = method.getReturnType();
          Type _type_8 = _returnType.getType();
          Assert.assertSame(genClazz, _type_8);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_5 = method.getTypeParameters();
          TypeParameterDeclaration _head_12 = IterableExtensions.head(_typeParameters_5);
          String _simpleName_5 = _head_12.getSimpleName();
          Assert.assertEquals("T2", _simpleName_5);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_6 = method.getTypeParameters();
          TypeParameterDeclaration _head_13 = IterableExtensions.head(_typeParameters_6);
          Iterable<? extends TypeReference> _upperBounds_1 = _head_13.getUpperBounds();
          TypeReference _head_14 = IterableExtensions.head(_upperBounds_1);
          String _string_5 = _head_14.toString();
          Assert.assertEquals("CharSequence", _string_5);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_7 = method.getTypeParameters();
          TypeParameterDeclaration _head_15 = IterableExtensions.head(_typeParameters_7);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_8 = method.getTypeParameters();
          TypeParameterDeclaration _head_16 = IterableExtensions.head(_typeParameters_8);
          Assert.assertSame(_head_15, _head_16);
          Iterable<? extends TypeParameterDeclaration> _typeParameters_9 = method.getTypeParameters();
          TypeParameterDeclaration _head_17 = IterableExtensions.head(_typeParameters_9);
          TypeParameterDeclarator _typeParameterDeclarator_1 = _head_17.getTypeParameterDeclarator();
          Assert.assertSame(method, _typeParameterDeclarator_1);
          Iterable<? extends MemberDeclaration> _declaredMembers_1 = clazz.getDeclaredMembers();
          MemberDeclaration _get_2 = ((MemberDeclaration[])Conversions.unwrapArray(_declaredMembers_1, MemberDeclaration.class))[0];
          Assert.assertSame(field, _get_2);
          Iterable<? extends MemberDeclaration> _declaredMembers_2 = clazz.getDeclaredMembers();
          MemberDeclaration _get_3 = ((MemberDeclaration[])Conversions.unwrapArray(_declaredMembers_2, MemberDeclaration.class))[1];
          Assert.assertSame(constructor, _get_3);
          Iterable<? extends MemberDeclaration> _declaredMembers_3 = clazz.getDeclaredMembers();
          MemberDeclaration _get_4 = ((MemberDeclaration[])Conversions.unwrapArray(_declaredMembers_3, MemberDeclaration.class))[2];
          Assert.assertSame(method, _get_4);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testMutableClassDeclaration() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class MyClass<T extends CharSequence> {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("String myField");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("new(String initial) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("this.myField = initial");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def <T2 extends CharSequence> MyClass myMethod(T2 a, T b) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("myField = myField + a + b");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return this");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          final MutableClassDeclaration genClazz = _typeLookup.findClass("foo.MyClass");
          final Procedure1<MutableMethodDeclaration> _function = new Procedure1<MutableMethodDeclaration>() {
              public void apply(final MutableMethodDeclaration it) {
                CompilationUnit _compilationUnit = genClazz.getCompilationUnit();
                TypeReferenceProvider _typeReferenceProvider = ((CompilationUnitImpl) _compilationUnit).getTypeReferenceProvider();
                TypeReference _string = _typeReferenceProvider.getString();
                it.setReturnType(_string);
                it.setVisibility(Visibility.PRIVATE);
                final CompilationStrategy _function = new CompilationStrategy() {
                    public CharSequence compile(final CompilationContext it) {
                      StringConcatenation _builder = new StringConcatenation();
                      _builder.append("return \"foo\";");
                      _builder.newLine();
                      return _builder;
                    }
                  };
                it.setBody(_function);
              }
            };
          genClazz.addMethod("newMethod", _function);
          final MutableMethodDeclaration mutableMethod = genClazz.findMethod("newMethod");
          Iterable<? extends MutableMethodDeclaration> _declaredMethods = genClazz.getDeclaredMethods();
          MutableMethodDeclaration _get = ((MutableMethodDeclaration[])Conversions.unwrapArray(_declaredMethods, MutableMethodDeclaration.class))[1];
          Assert.assertSame(mutableMethod, _get);
          TypeReference _returnType = mutableMethod.getReturnType();
          String _string = _returnType.toString();
          Assert.assertEquals("String", _string);
          Visibility _visibility = mutableMethod.getVisibility();
          Assert.assertEquals(Visibility.PRIVATE, _visibility);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testTypeReferences() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          TypeReferenceProvider _typeReferenceProvider = it.getTypeReferenceProvider();
          final TypeReference anyType = _typeReferenceProvider.getAnyType();
          boolean _isAnyType = anyType.isAnyType();
          Assert.assertTrue(_isAnyType);
          TypeReferenceProvider _typeReferenceProvider_1 = it.getTypeReferenceProvider();
          final TypeReference stringType = _typeReferenceProvider_1.getString();
          TypeReferenceProvider _typeReferenceProvider_2 = it.getTypeReferenceProvider();
          String _name = CharSequence.class.getName();
          final TypeReference charsequenceType = _typeReferenceProvider_2.newTypeReference(_name);
          boolean _isAssignableFrom = charsequenceType.isAssignableFrom(stringType);
          Assert.assertTrue(_isAssignableFrom);
          boolean _isAssignableFrom_1 = stringType.isAssignableFrom(anyType);
          Assert.assertTrue(_isAssignableFrom_1);
          boolean _isAssignableFrom_2 = stringType.isAssignableFrom(charsequenceType);
          Assert.assertFalse(_isAssignableFrom_2);
          TypeReferenceProvider _typeReferenceProvider_3 = it.getTypeReferenceProvider();
          TypeReference _primitiveBoolean = _typeReferenceProvider_3.getPrimitiveBoolean();
          DeclarationsTest.this.checkPrimitive(_primitiveBoolean, "java.lang.Boolean");
          TypeReferenceProvider _typeReferenceProvider_4 = it.getTypeReferenceProvider();
          TypeReference _primitiveInt = _typeReferenceProvider_4.getPrimitiveInt();
          DeclarationsTest.this.checkPrimitive(_primitiveInt, "java.lang.Integer");
          TypeReferenceProvider _typeReferenceProvider_5 = it.getTypeReferenceProvider();
          TypeReference _primitiveLong = _typeReferenceProvider_5.getPrimitiveLong();
          DeclarationsTest.this.checkPrimitive(_primitiveLong, "java.lang.Long");
          TypeReferenceProvider _typeReferenceProvider_6 = it.getTypeReferenceProvider();
          TypeReference _primitiveShort = _typeReferenceProvider_6.getPrimitiveShort();
          DeclarationsTest.this.checkPrimitive(_primitiveShort, "java.lang.Short");
          TypeReferenceProvider _typeReferenceProvider_7 = it.getTypeReferenceProvider();
          TypeReference _primitiveChar = _typeReferenceProvider_7.getPrimitiveChar();
          DeclarationsTest.this.checkPrimitive(_primitiveChar, "java.lang.Character");
          TypeReferenceProvider _typeReferenceProvider_8 = it.getTypeReferenceProvider();
          TypeReference _primitiveByte = _typeReferenceProvider_8.getPrimitiveByte();
          DeclarationsTest.this.checkPrimitive(_primitiveByte, "java.lang.Byte");
          TypeReferenceProvider _typeReferenceProvider_9 = it.getTypeReferenceProvider();
          TypeReference _primitiveFloat = _typeReferenceProvider_9.getPrimitiveFloat();
          DeclarationsTest.this.checkPrimitive(_primitiveFloat, "java.lang.Float");
          TypeReferenceProvider _typeReferenceProvider_10 = it.getTypeReferenceProvider();
          TypeReference _primitiveDouble = _typeReferenceProvider_10.getPrimitiveDouble();
          DeclarationsTest.this.checkPrimitive(_primitiveDouble, "java.lang.Double");
          TypeReferenceProvider _typeReferenceProvider_11 = it.getTypeReferenceProvider();
          final TypeReference primitiveVoid = _typeReferenceProvider_11.getPrimitiveVoid();
          boolean _isVoid = primitiveVoid.isVoid();
          Assert.assertTrue(_isVoid);
          TypeReferenceProvider _typeReferenceProvider_12 = it.getTypeReferenceProvider();
          TypeReferenceProvider _typeReferenceProvider_13 = it.getTypeReferenceProvider();
          TypeReference _string = _typeReferenceProvider_13.getString();
          final TypeReference listOfStringType = _typeReferenceProvider_12.getList(_string);
          TypeReferenceProvider _typeReferenceProvider_14 = it.getTypeReferenceProvider();
          List<TypeReference> _actualTypeArguments = listOfStringType.getActualTypeArguments();
          TypeReference _head = IterableExtensions.<TypeReference>head(_actualTypeArguments);
          final TypeReference setOfString = _typeReferenceProvider_14.getSet(_head);
          String _string_1 = listOfStringType.toString();
          Assert.assertEquals("List<String>", _string_1);
          List<TypeReference> _actualTypeArguments_1 = listOfStringType.getActualTypeArguments();
          TypeReference _head_1 = IterableExtensions.<TypeReference>head(_actualTypeArguments_1);
          String _string_2 = _head_1.toString();
          Assert.assertEquals("String", _string_2);
          String _string_3 = setOfString.toString();
          Assert.assertEquals("Set<String>", _string_3);
          List<TypeReference> _actualTypeArguments_2 = setOfString.getActualTypeArguments();
          TypeReference _head_2 = IterableExtensions.<TypeReference>head(_actualTypeArguments_2);
          String _string_4 = _head_2.toString();
          Assert.assertEquals("String", _string_4);
          TypeReferenceProvider _typeReferenceProvider_15 = it.getTypeReferenceProvider();
          TypeReferenceProvider _typeReferenceProvider_16 = it.getTypeReferenceProvider();
          TypeReference _newWildcardTypeReference = _typeReferenceProvider_16.newWildcardTypeReference();
          TypeReference _set = _typeReferenceProvider_15.getSet(_newWildcardTypeReference);
          String _string_5 = _set.toString();
          Assert.assertEquals("Set<?>", _string_5);
          TypeReferenceProvider _typeReferenceProvider_17 = it.getTypeReferenceProvider();
          TypeReferenceProvider _typeReferenceProvider_18 = it.getTypeReferenceProvider();
          TypeReference _newWildcardTypeReference_1 = _typeReferenceProvider_18.newWildcardTypeReference(listOfStringType);
          TypeReference _set_1 = _typeReferenceProvider_17.getSet(_newWildcardTypeReference_1);
          String _string_6 = _set_1.toString();
          Assert.assertEquals("Set<? extends List<String>>", _string_6);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testIsAssignable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class BaseClass implements InterfaceA {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("class SubType extends BaseClass implements InterfaceA {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("interface InterfaceA {}");
    _builder.newLine();
    _builder.append("interface InterfaceB {}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations = it.getSourceTypeDeclarations();
          TypeDeclaration _get = ((TypeDeclaration[])Conversions.unwrapArray(_sourceTypeDeclarations, TypeDeclaration.class))[0];
          final ClassDeclaration baseClass = ((ClassDeclaration) _get);
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          final MutableClassDeclaration subClass = _typeLookup.findClass("foo.SubType");
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations_1 = it.getSourceTypeDeclarations();
          TypeDeclaration _get_1 = ((TypeDeclaration[])Conversions.unwrapArray(_sourceTypeDeclarations_1, TypeDeclaration.class))[2];
          final InterfaceDeclaration interfaceA = ((InterfaceDeclaration) _get_1);
          Iterable<? extends TypeDeclaration> _sourceTypeDeclarations_2 = it.getSourceTypeDeclarations();
          TypeDeclaration _get_2 = ((TypeDeclaration[])Conversions.unwrapArray(_sourceTypeDeclarations_2, TypeDeclaration.class))[3];
          final InterfaceDeclaration interfaceB = ((InterfaceDeclaration) _get_2);
          TypeReferenceProvider _typeReferenceProvider = it.getTypeReferenceProvider();
          TypeReference _object = _typeReferenceProvider.getObject();
          final Type object = _object.getType();
          boolean _isAssignableFrom = object.isAssignableFrom(baseClass);
          Assert.assertTrue(_isAssignableFrom);
          boolean _isAssignableFrom_1 = object.isAssignableFrom(subClass);
          Assert.assertTrue(_isAssignableFrom_1);
          boolean _isAssignableFrom_2 = object.isAssignableFrom(interfaceA);
          Assert.assertTrue(_isAssignableFrom_2);
          boolean _isAssignableFrom_3 = object.isAssignableFrom(interfaceB);
          Assert.assertTrue(_isAssignableFrom_3);
          boolean _isAssignableFrom_4 = baseClass.isAssignableFrom(baseClass);
          Assert.assertTrue(_isAssignableFrom_4);
          boolean _isAssignableFrom_5 = baseClass.isAssignableFrom(subClass);
          Assert.assertTrue(_isAssignableFrom_5);
          boolean _isAssignableFrom_6 = baseClass.isAssignableFrom(interfaceB);
          Assert.assertFalse(_isAssignableFrom_6);
          boolean _isAssignableFrom_7 = baseClass.isAssignableFrom(interfaceA);
          Assert.assertFalse(_isAssignableFrom_7);
          boolean _isAssignableFrom_8 = baseClass.isAssignableFrom(object);
          Assert.assertFalse(_isAssignableFrom_8);
          boolean _isAssignableFrom_9 = interfaceA.isAssignableFrom(baseClass);
          Assert.assertTrue(_isAssignableFrom_9);
          boolean _isAssignableFrom_10 = interfaceA.isAssignableFrom(subClass);
          Assert.assertTrue(_isAssignableFrom_10);
          boolean _isAssignableFrom_11 = interfaceA.isAssignableFrom(interfaceA);
          Assert.assertTrue(_isAssignableFrom_11);
          boolean _isAssignableFrom_12 = interfaceA.isAssignableFrom(interfaceB);
          Assert.assertFalse(_isAssignableFrom_12);
          boolean _isAssignableFrom_13 = interfaceA.isAssignableFrom(object);
          Assert.assertFalse(_isAssignableFrom_13);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  @Test
  public void testSetImplementedInterfaces() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class BaseClass {}");
    _builder.newLine();
    _builder.append("interface Interface {}");
    _builder.newLine();
    XtendFile _validFile = this.validFile(_builder);
    final Procedure1<CompilationUnitImpl> _function = new Procedure1<CompilationUnitImpl>() {
        public void apply(final CompilationUnitImpl it) {
          TypeLookupImpl _typeLookup = it.getTypeLookup();
          final MutableClassDeclaration baseClass = _typeLookup.findClass("BaseClass");
          TypeLookupImpl _typeLookup_1 = it.getTypeLookup();
          final MutableInterfaceDeclaration interf = _typeLookup_1.findInterface("Interface");
          final TypeReference objectType = baseClass.getExtendedClass();
          String _simpleName = objectType.getSimpleName();
          Assert.assertEquals("Object", _simpleName);
          List<TypeReference> _implementedInterfaces = baseClass.getImplementedInterfaces();
          boolean _isEmpty = _implementedInterfaces.isEmpty();
          Assert.assertTrue(_isEmpty);
          TypeReferenceProvider _typeReferenceProvider = it.getTypeReferenceProvider();
          final TypeReference superType = _typeReferenceProvider.newTypeReference(AccessibleObject.class);
          baseClass.setExtendedClass(superType);
          TypeReference _extendedClass = baseClass.getExtendedClass();
          String _simpleName_1 = _extendedClass.getSimpleName();
          Assert.assertEquals("AccessibleObject", _simpleName_1);
          List<TypeReference> _implementedInterfaces_1 = baseClass.getImplementedInterfaces();
          boolean _isEmpty_1 = _implementedInterfaces_1.isEmpty();
          Assert.assertTrue(_isEmpty_1);
          baseClass.setExtendedClass(null);
          TypeReference _extendedClass_1 = baseClass.getExtendedClass();
          String _simpleName_2 = _extendedClass_1.getSimpleName();
          Assert.assertEquals("Object", _simpleName_2);
          List<TypeReference> _implementedInterfaces_2 = baseClass.getImplementedInterfaces();
          boolean _isEmpty_2 = _implementedInterfaces_2.isEmpty();
          Assert.assertTrue(_isEmpty_2);
          TypeReferenceProvider _typeReferenceProvider_1 = it.getTypeReferenceProvider();
          TypeReference _newTypeReference = _typeReferenceProvider_1.newTypeReference(interf);
          baseClass.setImplementedInterfaces(Collections.<TypeReference>unmodifiableList(Lists.<TypeReference>newArrayList(_newTypeReference)));
          List<TypeReference> _implementedInterfaces_3 = baseClass.getImplementedInterfaces();
          TypeReference _head = IterableExtensions.<TypeReference>head(_implementedInterfaces_3);
          String _simpleName_3 = _head.getSimpleName();
          Assert.assertEquals("Interface", _simpleName_3);
          baseClass.setImplementedInterfaces(Collections.<TypeReference>unmodifiableList(Lists.<TypeReference>newArrayList()));
          List<TypeReference> _implementedInterfaces_4 = baseClass.getImplementedInterfaces();
          boolean _isEmpty_3 = _implementedInterfaces_4.isEmpty();
          Assert.assertTrue(_isEmpty_3);
        }
      };
    this.asCompilationUnit(_validFile, _function);
  }
  
  public void checkPrimitive(final TypeReference primitiveType, final String wrapperTypeName) {
    String _string = primitiveType.toString();
    boolean _isPrimitive = primitiveType.isPrimitive();
    Assert.assertTrue(_string, _isPrimitive);
    TypeReference _wrapperIfPrimitive = primitiveType.getWrapperIfPrimitive();
    Type _type = _wrapperIfPrimitive.getType();
    String _qualifiedName = _type.getQualifiedName();
    Assert.assertEquals(wrapperTypeName, _qualifiedName);
  }
  
  public XtendFile validFile(final CharSequence code) {
    try {
      String _string = code.toString();
      XtendFile _file = this.file(_string, true);
      return _file;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void asCompilationUnit(final XtendFile file, final Procedure1<? super CompilationUnitImpl> block) {
    final CompilationUnitImpl compilationUnit = this.compilationUnitProvider.get();
    compilationUnit.setXtendFile(file);
    block.apply(compilationUnit);
  }
}
