/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.macro.ConditionUtils;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmAnnotationTypeDeclarationImpl;
import org.eclipse.xtend.core.macro.declaration.JvmElementImpl;
import org.eclipse.xtend.core.macro.declaration.ProblemSupportImpl;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.EnumerationValueDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmCustomAnnotationValue;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.interpreter.ConstantExpressionEvaluationException;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class JvmAnnotationReferenceImpl extends JvmElementImpl<JvmAnnotationReference> implements AnnotationReference {
  public AnnotationTypeDeclaration getAnnotationTypeDeclaration() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmAnnotationReference _delegate = this.getDelegate();
    JvmAnnotationType _annotation = _delegate.getAnnotation();
    TypeDeclaration _typeDeclaration = _compilationUnit.toTypeDeclaration(_annotation);
    return ((AnnotationTypeDeclaration) _typeDeclaration);
  }
  
  public Expression getExpression(final String property) {
    final JvmOperation op = this.findOperation(property);
    JvmAnnotationReference _delegate = this.getDelegate();
    EList<JvmAnnotationValue> _values = _delegate.getValues();
    final Function1<JvmAnnotationValue, Boolean> _function = new Function1<JvmAnnotationValue, Boolean>() {
      public Boolean apply(final JvmAnnotationValue it) {
        boolean _or = false;
        JvmOperation _operation = it.getOperation();
        boolean _equals = Objects.equal(_operation, op);
        if (_equals) {
          _or = true;
        } else {
          boolean _and = false;
          JvmOperation _operation_1 = it.getOperation();
          boolean _equals_1 = Objects.equal(_operation_1, null);
          if (!_equals_1) {
            _and = false;
          } else {
            String _simpleName = op.getSimpleName();
            boolean _equals_2 = Objects.equal(_simpleName, "value");
            _and = _equals_2;
          }
          _or = _and;
        }
        return Boolean.valueOf(_or);
      }
    };
    final JvmAnnotationValue annotationValue = IterableExtensions.<JvmAnnotationValue>findFirst(_values, _function);
    boolean _matched = false;
    if (!_matched) {
      if (annotationValue instanceof JvmCustomAnnotationValue) {
        _matched=true;
        CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
        EList<Object> _values_1 = ((JvmCustomAnnotationValue)annotationValue).getValues();
        Object _head = IterableExtensions.<Object>head(_values_1);
        return _compilationUnit.toExpression(((XExpression) _head));
      }
    }
    return null;
  }
  
  public Object getValue(final String property) {
    try {
      final JvmOperation op = this.findOperation(property);
      JvmAnnotationReference _delegate = this.getDelegate();
      EList<JvmAnnotationValue> _values = _delegate.getValues();
      final Function1<JvmAnnotationValue, Boolean> _function = new Function1<JvmAnnotationValue, Boolean>() {
        public Boolean apply(final JvmAnnotationValue it) {
          boolean _or = false;
          JvmOperation _operation = it.getOperation();
          boolean _equals = Objects.equal(_operation, op);
          if (_equals) {
            _or = true;
          } else {
            boolean _and = false;
            JvmOperation _operation_1 = it.getOperation();
            boolean _equals_1 = Objects.equal(_operation_1, null);
            if (!_equals_1) {
              _and = false;
            } else {
              String _simpleName = op.getSimpleName();
              boolean _equals_2 = Objects.equal(_simpleName, "value");
              _and = _equals_2;
            }
            _or = _and;
          }
          return Boolean.valueOf(_or);
        }
      };
      final JvmAnnotationValue annotationValue = IterableExtensions.<JvmAnnotationValue>findFirst(_values, _function);
      boolean _and = false;
      boolean _notEquals = (!Objects.equal(op, null));
      if (!_notEquals) {
        _and = false;
      } else {
        CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
        TypeReferences _typeReferences = _compilationUnit.getTypeReferences();
        JvmTypeReference _returnType = op.getReturnType();
        boolean _isArray = _typeReferences.isArray(_returnType);
        _and = _isArray;
      }
      final boolean isArrayType = _and;
      boolean _notEquals_1 = (!Objects.equal(annotationValue, null));
      if (_notEquals_1) {
        CompilationUnitImpl _compilationUnit_1 = this.getCompilationUnit();
        return _compilationUnit_1.translateAnnotationValue(annotationValue, isArrayType);
      }
    } catch (final Throwable _t) {
      if (_t instanceof ConstantExpressionEvaluationException) {
        final ConstantExpressionEvaluationException e = (ConstantExpressionEvaluationException)_t;
        CompilationUnitImpl _compilationUnit_2 = this.getCompilationUnit();
        ProblemSupportImpl _problemSupport = _compilationUnit_2.getProblemSupport();
        String _message = e.getMessage();
        _problemSupport.addError(this, _message);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
    return null;
  }
  
  private JvmOperation findOperation(final String name) {
    ConditionUtils.checkJavaIdentifier(name, "name");
    AnnotationTypeDeclaration _annotationTypeDeclaration = this.getAnnotationTypeDeclaration();
    final JvmAnnotationType jvmAnnoType = ((JvmAnnotationTypeDeclarationImpl) _annotationTypeDeclaration).getDelegate();
    Iterable<JvmOperation> _declaredOperations = jvmAnnoType.getDeclaredOperations();
    final Function1<JvmOperation, Boolean> _function = new Function1<JvmOperation, Boolean>() {
      public Boolean apply(final JvmOperation it) {
        String _simpleName = it.getSimpleName();
        return Boolean.valueOf(Objects.equal(_simpleName, name));
      }
    };
    final JvmOperation jvmOperation = IterableExtensions.<JvmOperation>findFirst(_declaredOperations, _function);
    boolean _equals = Objects.equal(jvmOperation, null);
    if (_equals) {
      String _identifier = jvmAnnoType.getIdentifier();
      String _plus = ((("The annotation property \'" + name) + "\' is not declared on the annotation type \'") + _identifier);
      String _plus_1 = (_plus + "\'.");
      throw new IllegalArgumentException(_plus_1);
    }
    return jvmOperation;
  }
  
  public AnnotationReference getAnnotationValue(final String name) {
    Object _value = this.getValue(name);
    return ((AnnotationReference) _value);
  }
  
  public AnnotationReference[] getAnnotationArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((AnnotationReference[]) _value);
  }
  
  public boolean[] getBooleanArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((boolean[]) _value);
  }
  
  public boolean getBooleanValue(final String name) {
    Boolean _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return false;
      }
      _xblockexpression = ((Boolean) value);
    }
    return (_xblockexpression).booleanValue();
  }
  
  public byte[] getByteArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((byte[]) _value);
  }
  
  public byte getByteValue(final String name) {
    Byte _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return ((byte) 0);
      }
      _xblockexpression = ((Byte) value);
    }
    return (_xblockexpression).byteValue();
  }
  
  public char[] getCharArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((char[]) _value);
  }
  
  public char getCharValue(final String name) {
    Character _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return ((char) 0);
      }
      Character _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Character.valueOf(((char) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Character) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).charValue();
  }
  
  public TypeReference getClassValue(final String name) {
    Object _value = this.getValue(name);
    return ((TypeReference) _value);
  }
  
  public TypeReference[] getClassArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((TypeReference[]) _value);
  }
  
  public double[] getDoubleArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((double[]) _value);
  }
  
  public double getDoubleValue(final String name) {
    Double _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return 0;
      }
      Double _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Character) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Character) value).charValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Short) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Short) value).shortValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Integer) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Integer) value).intValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Long) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Long) value).longValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Float) {
          _matched=true;
          _switchResult = Double.valueOf(((double) ((Float) value).floatValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Double) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).doubleValue();
  }
  
  public EnumerationValueDeclaration getEnumValue(final String name) {
    Object _value = this.getValue(name);
    return ((EnumerationValueDeclaration) _value);
  }
  
  public EnumerationValueDeclaration[] getEnumArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((EnumerationValueDeclaration[]) _value);
  }
  
  public float[] getFloatArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((float[]) _value);
  }
  
  public float getFloatValue(final String name) {
    Float _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return 0;
      }
      Float _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Character) {
          _matched=true;
          _switchResult = Float.valueOf(((float) ((Character) value).charValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Float.valueOf(((float) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Short) {
          _matched=true;
          _switchResult = Float.valueOf(((float) ((Short) value).shortValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Integer) {
          _matched=true;
          _switchResult = Float.valueOf(((float) ((Integer) value).intValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Long) {
          _matched=true;
          _switchResult = Float.valueOf(((float) ((Long) value).longValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Float) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).floatValue();
  }
  
  public int[] getIntArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((int[]) _value);
  }
  
  public int getIntValue(final String name) {
    Integer _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return 0;
      }
      Integer _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Character) {
          _matched=true;
          _switchResult = Integer.valueOf(((int) ((Character) value).charValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Integer.valueOf(((int) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Short) {
          _matched=true;
          _switchResult = Integer.valueOf(((int) ((Short) value).shortValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Integer) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).intValue();
  }
  
  public long[] getLongArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((long[]) _value);
  }
  
  public long getLongValue(final String name) {
    Long _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return 0;
      }
      Long _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Character) {
          _matched=true;
          _switchResult = Long.valueOf(((long) ((Character) value).charValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Long.valueOf(((long) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Short) {
          _matched=true;
          _switchResult = Long.valueOf(((long) ((Short) value).shortValue()));
        }
      }
      if (!_matched) {
        if (value instanceof Integer) {
          _matched=true;
          _switchResult = Long.valueOf(((long) ((Integer) value).intValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Long) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).longValue();
  }
  
  public short[] getShortArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((short[]) _value);
  }
  
  public short getShortValue(final String name) {
    Short _xblockexpression = null;
    {
      final Object value = this.getValue(name);
      boolean _equals = Objects.equal(value, null);
      if (_equals) {
        return ((short) 0);
      }
      Short _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (value instanceof Byte) {
          _matched=true;
          _switchResult = Short.valueOf(((short) ((Byte) value).byteValue()));
        }
      }
      if (!_matched) {
        _switchResult = ((Short) value);
      }
      _xblockexpression = _switchResult;
    }
    return (_xblockexpression).shortValue();
  }
  
  public String[] getStringArrayValue(final String name) {
    Object _value = this.getValue(name);
    return ((String[]) _value);
  }
  
  public String getStringValue(final String name) {
    Object _value = this.getValue(name);
    return ((String) _value);
  }
}
