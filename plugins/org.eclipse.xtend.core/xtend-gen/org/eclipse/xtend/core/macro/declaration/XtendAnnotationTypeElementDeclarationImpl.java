/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.XtendMemberDeclarationImpl;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.lib.macro.declaration.AnnotationTypeElementDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.XExpression;

@SuppressWarnings("all")
public class XtendAnnotationTypeElementDeclarationImpl extends XtendMemberDeclarationImpl<XtendField> implements AnnotationTypeElementDeclaration {
  @Override
  public String getSimpleName() {
    XtendField _delegate = this.getDelegate();
    return _delegate.getName();
  }
  
  @Override
  public Object getDefaultValue() {
    Object _xblockexpression = null;
    {
      XtendField _delegate = this.getDelegate();
      XExpression _initialValue = _delegate.getInitialValue();
      boolean _equals = Objects.equal(_initialValue, null);
      if (_equals) {
        return null;
      }
      CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
      XtendField _delegate_1 = this.getDelegate();
      XExpression _initialValue_1 = _delegate_1.getInitialValue();
      XtendField _delegate_2 = this.getDelegate();
      JvmTypeReference _type = _delegate_2.getType();
      _xblockexpression = _compilationUnit.evaluate(_initialValue_1, _type);
    }
    return _xblockexpression;
  }
  
  @Override
  public Expression getDefaultValueExpression() {
    Expression _xblockexpression = null;
    {
      XtendField _delegate = this.getDelegate();
      XExpression _initialValue = _delegate.getInitialValue();
      boolean _equals = Objects.equal(_initialValue, null);
      if (_equals) {
        return null;
      }
      CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
      XtendField _delegate_1 = this.getDelegate();
      XExpression _initialValue_1 = _delegate_1.getInitialValue();
      _xblockexpression = _compilationUnit.toExpression(_initialValue_1);
    }
    return _xblockexpression;
  }
  
  @Override
  public TypeReference getType() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    XtendField _delegate = this.getDelegate();
    JvmTypeReference _type = _delegate.getType();
    return _compilationUnit.toTypeReference(_type);
  }
}
