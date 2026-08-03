/**
 * Copyright (c) 2014, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.controlflow;

import com.google.common.base.Optional;
import java.util.List;
import java.util.Objects;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.xbase.XTypeLiteral;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.XbaseGenerated;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

@Data
@SuppressWarnings("all")
class EvaluationResult implements IConstantEvaluationResult<Object> {
  protected static final EvaluationResult NOT_A_CONSTANT = new EvaluationResult(new Object(), false) {
    @Override
    public Object equalValue(final EvaluationResult other) {
      return Boolean.valueOf(false);
    }
  };

  private final Object rawValue;

  private final boolean compileTimeConstant;

  public boolean isNotAConstant() {
    return Objects.equals(this.rawValue, EvaluationResult.NOT_A_CONSTANT.rawValue);
  }

  @Override
  public Optional<Object> getValue() {
    return Optional.<Object>fromNullable(this.rawValue);
  }

  public Object equalValue(final EvaluationResult other) {
    return this.equalValue(this.rawValue, other.rawValue);
  }

  private Object _equalValue(final Object myValue, final Object otherValue) {
    return Boolean.valueOf(Objects.equals(myValue, otherValue));
  }

  private Object _equalValue(final Void myValue, final Object otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final Object myValue, final JvmIdentifiableElement otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final Object myValue, final ThisReference otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final ThisReference myValue, final Object otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final ThisReference myValue, final ThisReference otherValue) {
    return Boolean.valueOf(Objects.equals(myValue, otherValue));
  }

  private Object _equalValue(final Void myValue, final JvmIdentifiableElement otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final Object myValue, final List<?> otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final Void myValue, final List<?> otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final Void myValue, final JvmType otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final Void myValue, final Void otherValue) {
    return Boolean.valueOf(true);
  }

  private Object _equalValue(final Object myValue, final Void otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final JvmType myValue, final JvmIdentifiableElement otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmIdentifiableElement myValue, final Void otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmIdentifiableElement myValue, final JvmType otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmIdentifiableElement myValue, final Object otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final List<?> myValue, final Void otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final List<?> myValue, final Object otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final List<?> myValue, final List<?> otherValue) {
    final boolean equalLists = Objects.equals(myValue, otherValue);
    if (equalLists) {
      return Boolean.TRUE;
    }
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmIdentifiableElement myValue, final JvmIdentifiableElement otherValue) {
    return Boolean.valueOf(Objects.equals(myValue, otherValue));
  }

  private Object _equalValue(final JvmFormalParameter myValue, final JvmFormalParameter otherValue) {
    boolean _equals = Objects.equals(myValue, otherValue);
    if (_equals) {
      return Boolean.TRUE;
    }
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmEnumerationLiteral myValue, final JvmIdentifiableElement otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmIdentifiableElement myValue, final JvmEnumerationLiteral otherValue) {
    return EvaluationResult.NOT_A_CONSTANT.rawValue;
  }

  private Object _equalValue(final JvmEnumerationLiteral myValue, final JvmEnumerationLiteral otherValue) {
    return Boolean.valueOf(Objects.equals(myValue, otherValue));
  }

  private Object _equalValue(final JvmType myValue, final Void otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final XTypeLiteral myValue, final XTypeLiteral otherTypeLiteral) {
    return Boolean.valueOf((Objects.equals(myValue.getType(), otherTypeLiteral.getType()) && Objects.equals(myValue.getArrayDimensions(), otherTypeLiteral.getArrayDimensions())));
  }

  private Object _equalValue(final JvmType myValue, final XTypeLiteral otherTypeLiteral) {
    return Boolean.valueOf((Objects.equals(myValue, otherTypeLiteral.getType()) && otherTypeLiteral.getArrayDimensions().isEmpty()));
  }

  private Object _equalValue(final XTypeLiteral myValue, final JvmType otherType) {
    return Boolean.valueOf((Objects.equals(myValue.getType(), otherType) && myValue.getArrayDimensions().isEmpty()));
  }

  private Object _equalValue(final JvmType myValue, final ThisReference otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final ThisReference myValue, final JvmType otherValue) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final XTypeLiteral myValue, final ThisReference otherType) {
    return Boolean.valueOf(false);
  }

  private Object _equalValue(final ThisReference myValue, final XTypeLiteral otherType) {
    return Boolean.valueOf(false);
  }

  @XbaseGenerated
  private Object equalValue(final Object myValue, final Object otherValue) {
    if (myValue instanceof JvmEnumerationLiteral
         && otherValue instanceof JvmEnumerationLiteral) {
      return _equalValue((JvmEnumerationLiteral)myValue, (JvmEnumerationLiteral)otherValue);
    } else if (myValue instanceof JvmEnumerationLiteral
         && otherValue instanceof JvmIdentifiableElement) {
      return _equalValue((JvmEnumerationLiteral)myValue, (JvmIdentifiableElement)otherValue);
    } else if (myValue instanceof JvmFormalParameter
         && otherValue instanceof JvmFormalParameter) {
      return _equalValue((JvmFormalParameter)myValue, (JvmFormalParameter)otherValue);
    } else if (myValue instanceof List
         && otherValue instanceof List) {
      return _equalValue((List<?>)myValue, (List<?>)otherValue);
    } else if (myValue instanceof JvmType
         && otherValue instanceof XTypeLiteral) {
      return _equalValue((JvmType)myValue, (XTypeLiteral)otherValue);
    } else if (myValue instanceof XTypeLiteral
         && otherValue instanceof JvmType) {
      return _equalValue((XTypeLiteral)myValue, (JvmType)otherValue);
    } else if (myValue instanceof XTypeLiteral
         && otherValue instanceof XTypeLiteral) {
      return _equalValue((XTypeLiteral)myValue, (XTypeLiteral)otherValue);
    } else if (myValue instanceof JvmType
         && otherValue instanceof JvmIdentifiableElement) {
      return _equalValue((JvmType)myValue, (JvmIdentifiableElement)otherValue);
    } else if (myValue instanceof List
         && otherValue == null) {
      return _equalValue((List<?>)myValue, (Void)null);
    } else if (myValue instanceof JvmType
         && otherValue == null) {
      return _equalValue((JvmType)myValue, (Void)null);
    } else if (myValue instanceof JvmType
         && otherValue instanceof ThisReference) {
      return _equalValue((JvmType)myValue, (ThisReference)otherValue);
    } else if (myValue instanceof XTypeLiteral
         && otherValue instanceof ThisReference) {
      return _equalValue((XTypeLiteral)myValue, (ThisReference)otherValue);
    } else if (myValue instanceof List
         && otherValue != null) {
      return _equalValue((List<?>)myValue, otherValue);
    } else if (myValue instanceof JvmIdentifiableElement
         && otherValue instanceof JvmEnumerationLiteral) {
      return _equalValue((JvmIdentifiableElement)myValue, (JvmEnumerationLiteral)otherValue);
    } else if (myValue instanceof JvmIdentifiableElement
         && otherValue instanceof JvmType) {
      return _equalValue((JvmIdentifiableElement)myValue, (JvmType)otherValue);
    } else if (myValue instanceof JvmIdentifiableElement
         && otherValue instanceof JvmIdentifiableElement) {
      return _equalValue((JvmIdentifiableElement)myValue, (JvmIdentifiableElement)otherValue);
    } else if (myValue instanceof JvmIdentifiableElement
         && otherValue == null) {
      return _equalValue((JvmIdentifiableElement)myValue, (Void)null);
    } else if (myValue instanceof JvmIdentifiableElement
         && otherValue != null) {
      return _equalValue((JvmIdentifiableElement)myValue, otherValue);
    } else if (myValue == null
         && otherValue instanceof List) {
      return _equalValue((Void)null, (List<?>)otherValue);
    } else if (myValue == null
         && otherValue instanceof JvmType) {
      return _equalValue((Void)null, (JvmType)otherValue);
    } else if (myValue instanceof ThisReference
         && otherValue instanceof JvmType) {
      return _equalValue((ThisReference)myValue, (JvmType)otherValue);
    } else if (myValue instanceof ThisReference
         && otherValue instanceof XTypeLiteral) {
      return _equalValue((ThisReference)myValue, (XTypeLiteral)otherValue);
    } else if (myValue == null
         && otherValue instanceof JvmIdentifiableElement) {
      return _equalValue((Void)null, (JvmIdentifiableElement)otherValue);
    } else if (myValue == null
         && otherValue == null) {
      return _equalValue((Void)null, (Void)null);
    } else if (myValue instanceof ThisReference
         && otherValue instanceof ThisReference) {
      return _equalValue((ThisReference)myValue, (ThisReference)otherValue);
    } else if (myValue == null
         && otherValue != null) {
      return _equalValue((Void)null, otherValue);
    } else if (myValue instanceof ThisReference
         && otherValue != null) {
      return _equalValue((ThisReference)myValue, otherValue);
    } else if (myValue != null
         && otherValue instanceof List) {
      return _equalValue(myValue, (List<?>)otherValue);
    } else if (myValue != null
         && otherValue instanceof JvmIdentifiableElement) {
      return _equalValue(myValue, (JvmIdentifiableElement)otherValue);
    } else if (myValue != null
         && otherValue == null) {
      return _equalValue(myValue, (Void)null);
    } else if (myValue != null
         && otherValue instanceof ThisReference) {
      return _equalValue(myValue, (ThisReference)otherValue);
    } else {
      return _equalValue(myValue, otherValue);
    }
  }

  public EvaluationResult(final Object rawValue, final boolean compileTimeConstant) {
    super();
    this.rawValue = rawValue;
    this.compileTimeConstant = compileTimeConstant;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.rawValue== null) ? 0 : this.rawValue.hashCode());
    return prime * result + (this.compileTimeConstant ? 1231 : 1237);
  }

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EvaluationResult other = (EvaluationResult) obj;
    if (this.rawValue == null) {
      if (other.rawValue != null)
        return false;
    } else if (!this.rawValue.equals(other.rawValue))
      return false;
    if (other.compileTimeConstant != this.compileTimeConstant)
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("rawValue", this.rawValue);
    b.add("compileTimeConstant", this.compileTimeConstant);
    return b.toString();
  }

  @Pure
  public Object getRawValue() {
    return this.rawValue;
  }

  @Pure
  @Override
  public boolean isCompileTimeConstant() {
    return this.compileTimeConstant;
  }
}
