/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmMemberDeclarationImpl;
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTypeElementDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtext.common.types.JvmAnnotationValue;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;

@SuppressWarnings("all")
public class JvmAnnotationTypeElementDeclarationImpl extends JvmMemberDeclarationImpl<JvmOperation> implements MutableAnnotationTypeElementDeclaration {
  public Object getDefaultValue() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmOperation _delegate = this.getDelegate();
    JvmAnnotationValue _defaultValue = _delegate.getDefaultValue();
    return _compilationUnit.translateAnnotationValue(_defaultValue);
  }
  
  public TypeReference getType() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmOperation _delegate = this.getDelegate();
    JvmTypeReference _returnType = _delegate.getReturnType();
    TypeReference _typeReference = _compilationUnit.toTypeReference(_returnType);
    return _typeReference;
  }
  
  public Expression getDefaultValueExpression() {
    return null;
  }
}
