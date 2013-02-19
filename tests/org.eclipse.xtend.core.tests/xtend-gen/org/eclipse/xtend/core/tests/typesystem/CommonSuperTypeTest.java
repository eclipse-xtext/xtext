/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.typesystem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.tests.typesystem.AbstractTestingTypeReferenceOwner;
import org.eclipse.xtend.core.xtend.XtendFunction;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.typesystem.conformance.TypeConformanceComputer;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.FunctionTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.util.CommonTypeComputationServices;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow
 */
@SuppressWarnings("all")
public class CommonSuperTypeTest extends AbstractTestingTypeReferenceOwner {
  @Inject
  private IXtendJvmAssociations _iXtendJvmAssociations;
  
  public Object isSuperTypeOf(final String superType, final String... types) {
    Pair<String,String> _mappedTo = Pair.<String, String>of(superType, null);
    Object _isSuperTypeOf = this.isSuperTypeOf(_mappedTo, types);
    return _isSuperTypeOf;
  }
  
  public Object isSuperTypeOf(final Pair<String,String> superTypeAndParam, final String... types) {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("def ");
      {
        String _value = superTypeAndParam.getValue();
        boolean _isNullOrEmpty = StringExtensions.isNullOrEmpty(_value);
        boolean _not = (!_isNullOrEmpty);
        if (_not) {
          _builder.append("<");
          String _value_1 = superTypeAndParam.getValue();
          _builder.append(_value_1, "");
          _builder.append("> ");
        }
      }
      _builder.append("void method(");
      {
        boolean _hasElements = false;
        for(final String type : types) {
          if (!_hasElements) {
            _hasElements = true;
          } else {
            _builder.appendImmediate(", ", "");
          }
          String _fixup = this.fixup(type);
          _builder.append(_fixup, "");
          _builder.append(" t");
        }
      }
      _builder.append(") {}");
      final String signature = _builder.toString();
      String _string = signature.toString();
      final XtendFunction function = this.function(_string);
      final JvmOperation operation = this._iXtendJvmAssociations.getDirectlyInferredOperation(function);
      EList<JvmFormalParameter> _parameters = operation.getParameters();
      final Function1<JvmFormalParameter,LightweightTypeReference> _function = new Function1<JvmFormalParameter,LightweightTypeReference>() {
          public LightweightTypeReference apply(final JvmFormalParameter it) {
            JvmTypeReference _parameterType = it.getParameterType();
            LightweightTypeReference _lightweightReference = CommonSuperTypeTest.this.toLightweightReference(_parameterType);
            return _lightweightReference;
          }
        };
      List<LightweightTypeReference> _map = ListExtensions.<JvmFormalParameter, LightweightTypeReference>map(_parameters, _function);
      ArrayList<LightweightTypeReference> _arrayList = new ArrayList<LightweightTypeReference>(_map);
      final ArrayList<LightweightTypeReference> typeReferences = _arrayList;
      CommonTypeComputationServices _services = this.getServices();
      final TypeConformanceComputer conformanceComputer = _services.getTypeConformanceComputer();
      LightweightTypeReference computedSuperType = conformanceComputer.getCommonSuperType(typeReferences);
      String _key = superTypeAndParam.getKey();
      String _simpleName = computedSuperType==null?(String)null:computedSuperType.getSimpleName();
      Assert.assertEquals(_key, _simpleName);
      CommonTypeComputationServices _services_1 = this.getServices();
      TypeConformanceComputer _typeConformanceComputer = _services_1.getTypeConformanceComputer();
      Iterable<LightweightTypeReference> _plus = Iterables.<LightweightTypeReference>concat(typeReferences, typeReferences);
      List<LightweightTypeReference> _list = IterableExtensions.<LightweightTypeReference>toList(_plus);
      LightweightTypeReference _commonSuperType = _typeConformanceComputer.getCommonSuperType(_list);
      computedSuperType = _commonSuperType;
      String _key_1 = superTypeAndParam.getKey();
      String _simpleName_1 = computedSuperType==null?(String)null:computedSuperType.getSimpleName();
      Assert.assertEquals(_key_1, _simpleName_1);
      CommonTypeComputationServices _services_2 = this.getServices();
      TypeConformanceComputer _typeConformanceComputer_1 = _services_2.getTypeConformanceComputer();
      List<LightweightTypeReference> _reverseView = ListExtensions.<LightweightTypeReference>reverseView(typeReferences);
      LightweightTypeReference _commonSuperType_1 = _typeConformanceComputer_1.getCommonSuperType(_reverseView);
      computedSuperType = _commonSuperType_1;
      String _key_2 = superTypeAndParam.getKey();
      String _simpleName_2 = computedSuperType==null?(String)null:computedSuperType.getSimpleName();
      Assert.assertEquals(_key_2, _simpleName_2);
      boolean _or = false;
      boolean _isPrimitiveVoid = computedSuperType==null?false:computedSuperType.isPrimitiveVoid();
      if (_isPrimitiveVoid) {
        _or = true;
      } else {
        boolean _isPrimitive = computedSuperType==null?false:computedSuperType.isPrimitive();
        _or = (_isPrimitiveVoid || _isPrimitive);
      }
      boolean _not_1 = (!_or);
      if (_not_1) {
        CommonTypeComputationServices _services_3 = this.getServices();
        TypeConformanceComputer _typeConformanceComputer_2 = _services_3.getTypeConformanceComputer();
        AnyTypeReference _anyTypeReference = new AnyTypeReference(this);
        AnyTypeReference _anyTypeReference_1 = new AnyTypeReference(this);
        List<LightweightTypeReference> _newImmutableList = CollectionLiterals.<LightweightTypeReference>newImmutableList(_anyTypeReference, _anyTypeReference_1);
        Iterable<LightweightTypeReference> _plus_1 = Iterables.<LightweightTypeReference>concat(typeReferences, _newImmutableList);
        List<LightweightTypeReference> _list_1 = IterableExtensions.<LightweightTypeReference>toList(_plus_1);
        LightweightTypeReference _commonSuperType_2 = _typeConformanceComputer_2.getCommonSuperType(_list_1);
        computedSuperType = _commonSuperType_2;
        String _key_3 = superTypeAndParam.getKey();
        String _simpleName_3 = computedSuperType==null?(String)null:computedSuperType.getSimpleName();
        Assert.assertEquals(_key_3, _simpleName_3);
      }
      boolean _notEquals = ObjectExtensions.operator_notEquals(computedSuperType, null);
      if (_notEquals) {
        final Procedure1<LightweightTypeReference> _function_1 = new Procedure1<LightweightTypeReference>() {
            public void apply(final LightweightTypeReference superType) {
              final Procedure1<LightweightTypeReference> _function = new Procedure1<LightweightTypeReference>() {
                  public void apply(final LightweightTypeReference it) {
                    String _key = superTypeAndParam.getKey();
                    List<LightweightTypeReference> _xlistliteral = null;
                    Builder<LightweightTypeReference> _builder = ImmutableList.builder();
                    _builder.add(it);
                    _builder.add(superType);
                    _xlistliteral = _builder.build();
                    LightweightTypeReference _commonSuperType = conformanceComputer.getCommonSuperType(_xlistliteral);
                    String _simpleName = _commonSuperType==null?(String)null:_commonSuperType.getSimpleName();
                    Assert.assertEquals(_key, _simpleName);
                  }
                };
              IterableExtensions.<LightweightTypeReference>forEach(typeReferences, _function);
            }
          };
        ObjectExtensions.<LightweightTypeReference>operator_doubleArrow(computedSuperType, _function_1);
      }
      boolean _notEquals_1 = ObjectExtensions.operator_notEquals(computedSuperType, null);
      if (_notEquals_1) {
        for (final LightweightTypeReference subType : typeReferences) {
          boolean _isAssignableFrom = computedSuperType.isAssignableFrom(subType);
          Assert.assertTrue(_isAssignableFrom);
        }
      }
      return computedSuperType;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void isFunctionAndEquivalentTo(final Object reference, final String type) {
    Assert.assertTrue((reference instanceof FunctionTypeReference));
    String _equivalent = this.getEquivalent(((FunctionTypeReference) reference));
    Assert.assertEquals(type, _equivalent);
  }
  
  public String getEquivalent(final ParameterizedTypeReference type) {
    List<LightweightTypeReference> _typeArguments = type.getTypeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    if (_isEmpty) {
      JvmType _type = type.getType();
      return _type.getSimpleName();
    }
    StringConcatenation _builder = new StringConcatenation();
    JvmType _type_1 = type.getType();
    String _simpleName = _type_1.getSimpleName();
    _builder.append(_simpleName, "");
    _builder.append("<");
    List<LightweightTypeReference> _typeArguments_1 = type.getTypeArguments();
    final Function1<LightweightTypeReference,String> _function = new Function1<LightweightTypeReference,String>() {
        public String apply(final LightweightTypeReference it) {
          String _simpleName = it.getSimpleName();
          return _simpleName;
        }
      };
    String _join = IterableExtensions.<LightweightTypeReference>join(_typeArguments_1, ", ", _function);
    _builder.append(_join, "");
    _builder.append(">");
    return _builder.toString();
  }
  
  protected String fixup(final String type) {
    String _elvis = null;
    String _replace = type==null?(String)null:type.replace("$Procedure", "org.eclipse.xtext.xbase.lib.Procedures$Procedure");
    String _replace_1 = _replace==null?(String)null:_replace.replace("$Function<", "com.google.common.base.Function<");
    String _replace_2 = _replace_1==null?(String)null:_replace_1.replace("$Predicate<", "com.google.common.base.Predicate<");
    String _replace_3 = _replace_2==null?(String)null:_replace_2.replace("$Function", "org.eclipse.xtext.xbase.lib.Functions$Function");
    if (_replace_3 != null) {
      _elvis = _replace_3;
    } else {
      _elvis = ObjectExtensions.<String>operator_elvis(_replace_3, "Object");
    }
    return _elvis;
  }
  
  @Test
  public void testCommonSuperType_01() {
    this.isSuperTypeOf("Serializable & CharSequence", "String", "StringBuilder");
  }
  
  @Test
  public void testCommonSuperType_02() {
    this.isSuperTypeOf("CharSequence", "String", "StringBuilder", "CharSequence");
  }
  
  @Test
  public void testCommonSuperType_03() {
    this.isSuperTypeOf("Object", "String", "StringBuilder", "CharSequence", "Object");
  }
  
  @Test
  public void testCommonSuperType_04() {
    this.isSuperTypeOf("String", "String", "String");
  }
  
  @Test
  public void testCommonSuperType_05() {
    this.isSuperTypeOf("String", "String");
  }
  
  @Test
  public void testCommonSuperType_06() {
    this.isSuperTypeOf("Serializable", "String", "StringBuilder", "java.io.Serializable");
  }
  
  @Test
  public void testCommonSuperType_07() {
    this.isSuperTypeOf("Appendable & CharSequence", "StringBuilder", "java.nio.CharBuffer");
  }
  
  @Test
  public void testCommonSuperType_08() {
    this.isSuperTypeOf("Collection<String>", "java.util.Set<String>", "java.util.List<String>");
  }
  
  @Test
  public void testCommonSuperType_09() {
    this.isSuperTypeOf("Collection", "java.util.Set<String>", "java.util.List");
  }
  
  @Test
  public void testCommonSuperType_10() {
    this.isSuperTypeOf("Collection<? extends CharSequence>", "java.util.Set<String>", "java.util.List<CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_11() {
    this.isSuperTypeOf("Comparable<?> & Serializable", "String", "Integer");
  }
  
  @Test
  public void testCommonSuperType_12() {
    this.isSuperTypeOf("Number & Comparable<?>", "Double", "Integer");
  }
  
  @Test
  public void testCommonSuperType_13() {
    this.isSuperTypeOf("AbstractStringBuilder & Serializable", "StringBuilder", "StringBuffer");
  }
  
  @Test
  public void testCommonSuperType_14() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Serializable & CharSequence", "T extends CharSequence & java.io.Serializable, V extends CharSequence & java.io.Serializable");
    this.isSuperTypeOf(_mappedTo, "T", "V");
  }
  
  @Test
  public void testCommonSuperType_15() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Serializable & CharSequence", "T extends CharSequence & java.io.Serializable, V extends java.io.Serializable & CharSequence");
    this.isSuperTypeOf(_mappedTo, "T", "V");
  }
  
  @Test
  public void testCommonSuperType_16() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("T", "T extends CharSequence & java.io.Serializable");
    this.isSuperTypeOf(_mappedTo, "T", "T");
  }
  
  @Test
  public void testCommonSuperType_17() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Serializable & CharSequence", "T extends CharSequence & java.io.Serializable & Comparable, V extends java.io.Serializable & CharSequence");
    this.isSuperTypeOf(_mappedTo, "T", "V");
  }
  
  @Test
  public void testCommonSuperType_18() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("T", "T, V extends T");
    this.isSuperTypeOf(_mappedTo, "T", "V");
  }
  
  @Test
  public void testCommonSuperType_19() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Collection<String>", "T extends java.util.List<String>, V extends T");
    this.isSuperTypeOf(_mappedTo, "java.util.Set<String>", "V");
  }
  
  @Test
  public void testCommonSuperType_20() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Object", "T extends CharSequence & java.io.Serializable, V extends Appendable & Comparable");
    this.isSuperTypeOf(_mappedTo, "T", "V");
  }
  
  @Test
  public void testCommonSuperType_21() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Object", "T extends CharSequence & java.io.Serializable");
    this.isSuperTypeOf(_mappedTo, "T", "CharSequence", "java.io.Serializable");
  }
  
  @Test
  public void testCommonSuperType_22() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("CharSequence", "T extends CharSequence & java.io.Serializable");
    this.isSuperTypeOf(_mappedTo, "T", "CharSequence");
  }
  
  @Test
  public void testCommonSuperType_23() {
    AnyTypeReference _anyTypeReference = new AnyTypeReference(this);
    AnyTypeReference _anyTypeReference_1 = new AnyTypeReference(this);
    final List<LightweightTypeReference> types = CollectionLiterals.<LightweightTypeReference>newImmutableList(_anyTypeReference, _anyTypeReference_1);
    CommonTypeComputationServices _services = this.getServices();
    TypeConformanceComputer _typeConformanceComputer = _services.getTypeConformanceComputer();
    final LightweightTypeReference superType = _typeConformanceComputer.getCommonSuperType(types);
    String _simpleName = superType.getSimpleName();
    Assert.assertEquals("null", _simpleName);
  }
  
  @Test
  public void testCommonSuperType_24() {
    this.isSuperTypeOf("Collection<? extends AbstractStringBuilder & Serializable>", "java.util.List<StringBuilder>", "java.util.Set<StringBuffer>");
  }
  
  @Test
  public void testCommonSuperType_25() {
    this.isSuperTypeOf("Iterable", "java.util.List<StringBuilder>", "org.eclipse.xtend.core.tests.typesystem.RawIterable");
  }
  
  @Test
  public void testCommonSuperType_26() {
    this.isSuperTypeOf("Iterable", "java.util.List<StringBuilder>", "org.eclipse.xtend.core.tests.typesystem.CharIterable");
  }
  
  @Test
  public void testCommonSuperType_27() {
    this.isSuperTypeOf("Iterable<? extends CharSequence>", "java.util.List<StringBuilder>", "org.eclipse.xtend.core.tests.typesystem.CharIterable<CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_28() {
    this.isSuperTypeOf("Number[] & Comparable<?>[]", "Integer[]", "Double[]");
  }
  
  @Test
  public void testCommonSuperType_29() {
    this.isSuperTypeOf("Serializable & Cloneable", "int[]", "double[]");
  }
  
  @Test
  public void testCommonSuperType_30() {
    this.isSuperTypeOf("Serializable & Cloneable", "int[][]", "int[]");
  }
  
  @Test
  public void testCommonSuperType_31() {
    this.isSuperTypeOf("Object[]", "CharSequence[][]", "CharSequence[]");
  }
  
  @Test
  public void testCommonSuperType_32() {
    this.isSuperTypeOf("Number[]", "Number[]", "Integer[]", "Double[]");
  }
  
  @Test
  public void testCommonSuperType_33() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("T[]", "T, V extends T");
    this.isSuperTypeOf(_mappedTo, "V[]", "T[]");
  }
  
  @Test
  public void testCommonSuperType_34() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("Iterable<? extends CharSequence>", "T extends CharSequence, V extends T");
    this.isSuperTypeOf(_mappedTo, "Iterable<CharSequence>", "Iterable<V>");
  }
  
  @Test
  public void testCommonSuperType_35() {
    this.isSuperTypeOf("Iterable<? extends CharSequence>", "Iterable<CharSequence>", "Iterable<? extends CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_36() {
    this.isSuperTypeOf("Iterable<? super CharSequence>", "Iterable<CharSequence>", "Iterable<? super CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_37() {
    this.isSuperTypeOf("Iterable<?>", "Iterable<? super CharSequence>", "Iterable<? extends CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_38() {
    Pair<String,String> _mappedTo = Pair.<String, String>of("CharSequence[]", "T extends CharSequence, V extends T");
    this.isSuperTypeOf(_mappedTo, "V[]", "String[]");
  }
  
  @Test
  public void testCommonSuperType_39() {
    this.isSuperTypeOf("Number[][][] & Comparable<?>[][][]", "Integer[][][]", "Double[][][]");
  }
  
  @Test
  public void testCommonSuperType_40() {
    this.isSuperTypeOf("Serializable[]", "Number[][]", "Number[]");
  }
  
  @Test
  public void testCommonSuperType_41() {
    this.isSuperTypeOf("Comparable<?> & Serializable", "String", "int");
  }
  
  @Test
  public void testCommonSuperType_42() {
    this.isSuperTypeOf("AbstractList<String> & Serializable & Cloneable", "java.util.LinkedList<String>", "java.util.ArrayList<String>");
  }
  
  @Test
  public void testCommonSuperType_43() {
    this.isSuperTypeOf(((String) null), "String", "void");
  }
  
  @Test
  public void testCommonSuperType_44() {
    this.isSuperTypeOf("void", "void", "void");
  }
  
  @Test
  public void testCommonSuperType_45() {
    this.isSuperTypeOf("Class<?>", "Class<Void>", "Class<String>");
  }
  
  @Test
  public void testCommonSuperType_46() {
    this.isSuperTypeOf("Collection<?>", "java.util.Collection<Void>", "java.util.List<String>");
  }
  
  @Test
  public void testCommonSuperType_47() {
    this.isSuperTypeOf("long", "long", "int");
  }
  
  @Test
  public void testCommonSuperType_48() {
    this.isSuperTypeOf("double", "long", "double");
  }
  
  @Test
  public void testCommonSuperType_49() {
    Object _isSuperTypeOf = this.isSuperTypeOf("()=>void", "()=>void", "()=>void");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Procedure0");
  }
  
  @Test
  public void testCommonSuperType_50() {
    Object _isSuperTypeOf = this.isSuperTypeOf("()=>long", "()=>long", "()=>long");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function0<? extends Long>");
  }
  
  @Test
  public void testCommonSuperType_51() {
    Object _isSuperTypeOf = this.isSuperTypeOf("()=>Number & Comparable<?>", "()=>int", "()=>long");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function0<? extends Number & Comparable<?>>");
  }
  
  @Test
  public void testCommonSuperType_52() {
    Object _isSuperTypeOf = this.isSuperTypeOf("()=>Number & Comparable<?>", "()=>Integer", "()=>Long");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function0<? extends Number & Comparable<?>>");
  }
  
  @Test
  public void testCommonSuperType_53() {
    this.isSuperTypeOf("Object", "()=>void", "()=>Void");
  }
  
  @Test
  public void testCommonSuperType_54() {
    this.isSuperTypeOf("Object", "(String)=>void", "()=>void");
  }
  
  @Test
  public void testCommonSuperType_55() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String)=>Integer", "(String)=>Integer", "(String)=>Integer");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function1<? super String, ? extends Integer>");
  }
  
  @Test
  public void testCommonSuperType_56() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String, String)=>int", "java.util.Comparator<String>", "(String, String)=>int");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function2<? super String, ? super String, ? extends Integer>");
  }
  
  @Test
  public void testCommonSuperType_57() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String)=>Integer", "(String)=>Integer", "(Object)=>Integer");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function1<? super String, ? extends Integer>");
  }
  
  @Test
  public void testCommonSuperType_58() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String)=>Object", "(String)=>Appendable", "(CharSequence)=>CharSequence");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function1<? super String, ?>");
  }
  
  @Test
  public void testCommonSuperType_59() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String, String)=>int", "java.util.Comparator<? super String>", "(String, String)=>int");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function2<? super String, ? super String, ? extends Integer>");
  }
  
  @Test
  public void testCommonSuperType_60() {
    this.isSuperTypeOf("Object", "java.util.Comparator<? extends String>", "(String, String)=>int");
  }
  
  @Test
  public void testCommonSuperType_61() {
    this.isSuperTypeOf("Comparator<String>", "java.util.Comparator<String>", "(CharSequence, CharSequence)=>int");
  }
  
  @Test
  public void testCommonSuperType_62() {
    this.isSuperTypeOf("Comparator<? super String>", "java.util.Comparator<? super String>", "(CharSequence, CharSequence)=>int");
  }
  
  @Test
  public void testCommonSuperType_63() {
    this.isSuperTypeOf("Object", "java.util.Comparator<? extends String>", "(CharSequence, CharSequence)=>int");
  }
  
  @Test
  public void testCommonSuperType_64() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String)=>Object", "(String)=>String", "(Object)=>Object");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function1<? super String, ?>");
  }
  
  @Test
  public void testCommonSuperType_65() {
    Object _isSuperTypeOf = this.isSuperTypeOf("(String)=>Object", "$Function1<? super Object, ? extends Object>", "$Function1<? super String, ? extends String>");
    this.isFunctionAndEquivalentTo(_isSuperTypeOf, "Function1<? super String, ?>");
  }
  
  @Test
  public void testCommonSuperType_66() {
    this.isSuperTypeOf("Function1<?, ?>", "$Function1<Object, Object>", "$Function1<String, String>");
  }
  
  @Test
  public void testCommonSuperType_67() {
    this.isSuperTypeOf("Comparable<?> & Serializable", "Integer", "String");
  }
  
  @Test
  public void testCommonSuperType_68() {
    this.isSuperTypeOf("Comparable<?> & Serializable", "Integer", "String", "String");
  }
  
  @Test
  public void testCommonSuperType_69() {
    this.isSuperTypeOf("Iterable<?>", "Iterable<?>", "Iterable<? extends CharSequence>");
  }
  
  @Test
  public void testCommonSuperType_70() {
    this.isSuperTypeOf("Iterable<?>", "Iterable<? super CharSequence>", "Iterable<?>");
  }
}
