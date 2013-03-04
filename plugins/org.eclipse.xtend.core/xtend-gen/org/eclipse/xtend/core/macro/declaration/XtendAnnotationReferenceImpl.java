/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.macro.declaration.AbstractDeclarationImpl;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationElementValuePair;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;

@SuppressWarnings("all")
public class XtendAnnotationReferenceImpl extends AbstractDeclarationImpl<XAnnotation> implements AnnotationReference {
  public AnnotationTypeDeclaration getAnnotationTypeDeclaration() {
    AnnotationTypeDeclaration _switchResult = null;
    XAnnotation _delegate = this.getDelegate();
    JvmType _annotationType = _delegate.getAnnotationType();
    final JvmType type = _annotationType;
    boolean _matched = false;
    if (!_matched) {
      if (type instanceof JvmAnnotationType) {
        final JvmAnnotationType _jvmAnnotationType = (JvmAnnotationType)type;
        _matched=true;
        CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
        MutableTypeDeclaration _typeDeclaration = _compilationUnit.toTypeDeclaration(_jvmAnnotationType);
        _switchResult = ((AnnotationTypeDeclaration) _typeDeclaration);
      }
    }
    if (!_matched) {
      _switchResult = null;
    }
    return _switchResult;
  }
  
  public Expression getExpression(final String property) {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("TODO: auto-generated method stub");
    throw _unsupportedOperationException;
  }
  
  public Object getValue(final String property) {
    boolean _and = false;
    boolean _equals = ObjectExtensions.operator_equals(property, "value");
    if (!_equals) {
      _and = false;
    } else {
      XAnnotation _delegate = this.getDelegate();
      XExpression _value = _delegate.getValue();
      boolean _notEquals = ObjectExtensions.operator_notEquals(_value, null);
      _and = (_equals && _notEquals);
    }
    if (_and) {
      CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
      XAnnotation _delegate_1 = this.getDelegate();
      XExpression _value_1 = _delegate_1.getValue();
      return _compilationUnit.evaluate(_value_1);
    }
    XAnnotation _delegate_2 = this.getDelegate();
    EList<XAnnotationElementValuePair> _elementValuePairs = _delegate_2.getElementValuePairs();
    final Function1<XAnnotationElementValuePair,Boolean> _function = new Function1<XAnnotationElementValuePair,Boolean>() {
        public Boolean apply(final XAnnotationElementValuePair it) {
          JvmOperation _element = it.getElement();
          String _simpleName = _element.getSimpleName();
          boolean _equals = ObjectExtensions.operator_equals(_simpleName, property);
          return Boolean.valueOf(_equals);
        }
      };
    XAnnotationElementValuePair _findFirst = IterableExtensions.<XAnnotationElementValuePair>findFirst(_elementValuePairs, _function);
    final XExpression expression = _findFirst==null?(XExpression)null:_findFirst.getValue();
    boolean _notEquals_1 = ObjectExtensions.operator_notEquals(expression, null);
    if (_notEquals_1) {
      CompilationUnitImpl _compilationUnit_1 = this.getCompilationUnit();
      return _compilationUnit_1.evaluate(expression);
    }
    return null;
  }
}
