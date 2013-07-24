package org.eclipse.xtend.ide.tests.codebuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import org.eclipse.xtend.ide.codebuilder.AbstractConstructorBuilder;
import org.eclipse.xtend.ide.codebuilder.CodeBuilderFactory;
import org.eclipse.xtend.ide.tests.codebuilder.AbstractBuilderTest;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

@SuppressWarnings("all")
public class ConstructorBuilderTest extends AbstractBuilderTest {
  @Inject
  @Extension
  private CodeBuilderFactory _codeBuilderFactory;
  
  @Inject
  @Extension
  private TypeReferences _typeReferences;
  
  @Test
  public void testXtendConstructor() {
    JvmDeclaredType _xtendClass = this.getXtendClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_xtendClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _xtendClass = ConstructorBuilderTest.this.getXtendClass();
          it.setContext(_xtendClass);
          it.setVisibility(JvmVisibility.PROTECTED);
          JvmDeclaredType _xtendClass_1 = ConstructorBuilderTest.this.getXtendClass();
          JvmParameterizedTypeReference _createTypeRef = ConstructorBuilderTest.this._typeReferences.createTypeRef(_xtendClass_1);
          JvmDeclaredType _xtendClass_2 = ConstructorBuilderTest.this.getXtendClass();
          JvmParameterizedTypeReference _createTypeRef_1 = ConstructorBuilderTest.this._typeReferences.createTypeRef(_xtendClass_2);
          ArrayList<JvmTypeReference> _newArrayList = CollectionLiterals.<JvmTypeReference>newArrayList(_createTypeRef, _createTypeRef_1);
          it.setParameterTypes(_newArrayList);
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("protected new(Foo foo, Foo foo2) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testJavaConstructor() {
    JvmDeclaredType _javaClass = this.getJavaClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_javaClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _javaClass = ConstructorBuilderTest.this.getJavaClass();
          it.setContext(_javaClass);
          it.setVisibility(JvmVisibility.PRIVATE);
          JvmDeclaredType _javaClass_1 = ConstructorBuilderTest.this.getJavaClass();
          JvmParameterizedTypeReference _createTypeRef = ConstructorBuilderTest.this._typeReferences.createTypeRef(_javaClass_1);
          JvmDeclaredType _javaClass_2 = ConstructorBuilderTest.this.getJavaClass();
          JvmParameterizedTypeReference _createTypeRef_1 = ConstructorBuilderTest.this._typeReferences.createTypeRef(_javaClass_2);
          ArrayList<JvmTypeReference> _newArrayList = CollectionLiterals.<JvmTypeReference>newArrayList(_createTypeRef, _createTypeRef_1);
          it.setParameterTypes(_newArrayList);
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("private Bar(Bar bar, Bar bar2) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testXtendParameterNames() {
    JvmDeclaredType _xtendClass = this.getXtendClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_xtendClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _xtendClass = ConstructorBuilderTest.this.getXtendClass();
          it.setContext(_xtendClass);
          it.setVisibility(JvmVisibility.PROTECTED);
          JvmDeclaredType _xtendClass_1 = ConstructorBuilderTest.this.getXtendClass();
          JvmParameterizedTypeReference _createTypeRef = ConstructorBuilderTest.this._typeReferences.createTypeRef(_xtendClass_1);
          JvmDeclaredType _javaClass = ConstructorBuilderTest.this.getJavaClass();
          JvmParameterizedTypeReference _createTypeRef_1 = ConstructorBuilderTest.this._typeReferences.createTypeRef(_javaClass);
          ArrayList<JvmTypeReference> _newArrayList = CollectionLiterals.<JvmTypeReference>newArrayList(_createTypeRef, _createTypeRef_1);
          it.setParameterTypes(_newArrayList);
          it.setParameterNames(Collections.<String>unmodifiableList(Lists.<String>newArrayList("bar", "foo")));
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("protected new(Foo bar, Bar foo) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testJavaParameterNames() {
    JvmDeclaredType _javaClass = this.getJavaClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_javaClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _javaClass = ConstructorBuilderTest.this.getJavaClass();
          it.setContext(_javaClass);
          it.setVisibility(JvmVisibility.PRIVATE);
          JvmDeclaredType _xtendClass = ConstructorBuilderTest.this.getXtendClass();
          JvmParameterizedTypeReference _createTypeRef = ConstructorBuilderTest.this._typeReferences.createTypeRef(_xtendClass);
          JvmDeclaredType _javaClass_1 = ConstructorBuilderTest.this.getJavaClass();
          JvmParameterizedTypeReference _createTypeRef_1 = ConstructorBuilderTest.this._typeReferences.createTypeRef(_javaClass_1);
          ArrayList<JvmTypeReference> _newArrayList = CollectionLiterals.<JvmTypeReference>newArrayList(_createTypeRef, _createTypeRef_1);
          it.setParameterTypes(_newArrayList);
          it.setParameterNames(Collections.<String>unmodifiableList(Lists.<String>newArrayList("bar", "foo")));
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("private Bar(Foo bar, Bar foo) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testXtendExceptions() {
    JvmDeclaredType _xtendClass = this.getXtendClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_xtendClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _xtendClass = ConstructorBuilderTest.this.getXtendClass();
          it.setContext(_xtendClass);
          JvmDeclaredType _xtendClass_1 = ConstructorBuilderTest.this.getXtendClass();
          JvmTypeReference _typeForName = ConstructorBuilderTest.this._typeReferences.getTypeForName(Exception.class, _xtendClass_1);
          JvmDeclaredType _xtendClass_2 = ConstructorBuilderTest.this.getXtendClass();
          JvmTypeReference _typeForName_1 = ConstructorBuilderTest.this._typeReferences.getTypeForName(RuntimeException.class, _xtendClass_2);
          it.setExceptions(Collections.<JvmTypeReference>unmodifiableList(Lists.<JvmTypeReference>newArrayList(_typeForName, _typeForName_1)));
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("new() throws Exception, RuntimeException {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testJavaException() {
    JvmDeclaredType _javaClass = this.getJavaClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_javaClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _javaClass = ConstructorBuilderTest.this.getJavaClass();
          it.setContext(_javaClass);
          JvmDeclaredType _javaClass_1 = ConstructorBuilderTest.this.getJavaClass();
          JvmTypeReference _typeForName = ConstructorBuilderTest.this._typeReferences.getTypeForName(Exception.class, _javaClass_1);
          JvmDeclaredType _javaClass_2 = ConstructorBuilderTest.this.getJavaClass();
          JvmTypeReference _typeForName_1 = ConstructorBuilderTest.this._typeReferences.getTypeForName(RuntimeException.class, _javaClass_2);
          it.setExceptions(Collections.<JvmTypeReference>unmodifiableList(Lists.<JvmTypeReference>newArrayList(_typeForName, _typeForName_1)));
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Bar() throws Exception, RuntimeException {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append(AbstractBuilderTest.DEFAULT_BODY, "  ");
    _builder.append(";");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testXtendCustomBody() {
    JvmDeclaredType _xtendClass = this.getXtendClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_xtendClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _xtendClass = ConstructorBuilderTest.this.getXtendClass();
          it.setContext(_xtendClass);
          it.setBody("return");
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("new() {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("return");
    _builder.newLine();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
  
  @Test
  public void testJavaCustomBody() {
    JvmDeclaredType _javaClass = this.getJavaClass();
    AbstractConstructorBuilder _createConstructorBuilder = this._codeBuilderFactory.createConstructorBuilder(_javaClass);
    final Procedure1<AbstractConstructorBuilder> _function = new Procedure1<AbstractConstructorBuilder>() {
        public void apply(final AbstractConstructorBuilder it) {
          JvmDeclaredType _javaClass = ConstructorBuilderTest.this.getJavaClass();
          it.setContext(_javaClass);
          it.setBody("return");
        }
      };
    AbstractConstructorBuilder _doubleArrow = ObjectExtensions.<AbstractConstructorBuilder>operator_doubleArrow(_createConstructorBuilder, _function);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Bar() {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("return;");
    _builder.newLine();
    _builder.append("}");
    this.assertBuilds(_doubleArrow, _builder.toString());
  }
}
