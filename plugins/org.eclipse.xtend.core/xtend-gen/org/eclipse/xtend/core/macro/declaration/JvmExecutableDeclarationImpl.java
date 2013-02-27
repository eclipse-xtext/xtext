/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmMemberDeclarationImpl;
import org.eclipse.xtend.lib.macro.declaration.CompilationStrategy;
import org.eclipse.xtend.lib.macro.declaration.MutableExecutableDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.expression.Expression;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUpperBound;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;

@SuppressWarnings("all")
public abstract class JvmExecutableDeclarationImpl<T extends JvmExecutable> extends JvmMemberDeclarationImpl<T> implements MutableExecutableDeclaration {
  public List<MutableTypeParameterDeclaration> getTypeParameters() {
    T _delegate = this.getDelegate();
    EList<JvmTypeParameter> _typeParameters = _delegate.getTypeParameters();
    final Function1<JvmTypeParameter,MutableTypeParameterDeclaration> _function = new Function1<JvmTypeParameter,MutableTypeParameterDeclaration>() {
        public MutableTypeParameterDeclaration apply(final JvmTypeParameter it) {
          CompilationUnitImpl _compilationUnit = JvmExecutableDeclarationImpl.this.getCompilationUnit();
          MutableTypeParameterDeclaration _typeParameterDeclaration = _compilationUnit.toTypeParameterDeclaration(it);
          return ((MutableTypeParameterDeclaration) _typeParameterDeclaration);
        }
      };
    List<MutableTypeParameterDeclaration> _map = ListExtensions.<JvmTypeParameter, MutableTypeParameterDeclaration>map(_typeParameters, _function);
    return _map;
  }
  
  public boolean isVarArgs() {
    T _delegate = this.getDelegate();
    boolean _isVarArgs = _delegate.isVarArgs();
    return _isVarArgs;
  }
  
  public List<MutableParameterDeclaration> getParameters() {
    T _delegate = this.getDelegate();
    EList<JvmFormalParameter> _parameters = _delegate.getParameters();
    final Function1<JvmFormalParameter,MutableParameterDeclaration> _function = new Function1<JvmFormalParameter,MutableParameterDeclaration>() {
        public MutableParameterDeclaration apply(final JvmFormalParameter it) {
          CompilationUnitImpl _compilationUnit = JvmExecutableDeclarationImpl.this.getCompilationUnit();
          MutableParameterDeclaration _parameterDeclaration = _compilationUnit.toParameterDeclaration(it);
          return ((MutableParameterDeclaration) _parameterDeclaration);
        }
      };
    List<MutableParameterDeclaration> _map = ListExtensions.<JvmFormalParameter, MutableParameterDeclaration>map(_parameters, _function);
    return _map;
  }
  
  public List<TypeReference> getExceptions() {
    T _delegate = this.getDelegate();
    EList<JvmTypeReference> _exceptions = _delegate.getExceptions();
    final Function1<JvmTypeReference,TypeReference> _function = new Function1<JvmTypeReference,TypeReference>() {
        public TypeReference apply(final JvmTypeReference it) {
          CompilationUnitImpl _compilationUnit = JvmExecutableDeclarationImpl.this.getCompilationUnit();
          TypeReference _typeReference = _compilationUnit.toTypeReference(it);
          return _typeReference;
        }
      };
    List<TypeReference> _map = ListExtensions.<JvmTypeReference, TypeReference>map(_exceptions, _function);
    return _map;
  }
  
  public Expression getBody() {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("Auto-Jvm function stub");
    throw _unsupportedOperationException;
  }
  
  public void setBody(final Expression body) {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("Auto-Jvm function stub");
    throw _unsupportedOperationException;
  }
  
  public void setExceptions(final TypeReference... exceptions) {
    T _delegate = this.getDelegate();
    EList<JvmTypeReference> _exceptions = _delegate.getExceptions();
    _exceptions.clear();
    for (final TypeReference exceptionType : exceptions) {
      boolean _notEquals = ObjectExtensions.operator_notEquals(exceptionType, null);
      if (_notEquals) {
        T _delegate_1 = this.getDelegate();
        EList<JvmTypeReference> _exceptions_1 = _delegate_1.getExceptions();
        CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
        JvmTypeReference _jvmTypeReference = _compilationUnit.toJvmTypeReference(exceptionType);
        _exceptions_1.add(_jvmTypeReference);
      }
    }
  }
  
  public void setVarArgs(final boolean isVarArgs) {
    T _delegate = this.getDelegate();
    _delegate.setVarArgs(isVarArgs);
  }
  
  public MutableTypeParameterDeclaration addTypeParameter(final String name, final TypeReference[] upperBounds) {
    final JvmTypeParameter param = TypesFactory.eINSTANCE.createJvmTypeParameter();
    param.setName(name);
    T _delegate = this.getDelegate();
    EList<JvmTypeParameter> _typeParameters = _delegate.getTypeParameters();
    _typeParameters.add(param);
    for (final TypeReference upper : upperBounds) {
      {
        CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
        final JvmTypeReference typeRef = _compilationUnit.toJvmTypeReference(upper);
        final JvmUpperBound jvmUpperBound = TypesFactory.eINSTANCE.createJvmUpperBound();
        jvmUpperBound.setTypeReference(typeRef);
        EList<JvmTypeConstraint> _constraints = param.getConstraints();
        _constraints.add(jvmUpperBound);
      }
    }
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    MutableTypeParameterDeclaration _typeParameterDeclaration = _compilationUnit.toTypeParameterDeclaration(param);
    return ((MutableTypeParameterDeclaration) _typeParameterDeclaration);
  }
  
  public void setBody(final CompilationStrategy compilationStrategy) {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    T _delegate = this.getDelegate();
    _compilationUnit.setCompilationStrategy(_delegate, compilationStrategy);
  }
  
  public MutableParameterDeclaration addParameter(final String name, final TypeReference type) {
    final JvmFormalParameter param = TypesFactory.eINSTANCE.createJvmFormalParameter();
    param.setName(name);
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmTypeReference _jvmTypeReference = _compilationUnit.toJvmTypeReference(type);
    param.setParameterType(_jvmTypeReference);
    T _delegate = this.getDelegate();
    EList<JvmFormalParameter> _parameters = _delegate.getParameters();
    _parameters.add(param);
    CompilationUnitImpl _compilationUnit_1 = this.getCompilationUnit();
    MutableParameterDeclaration _parameterDeclaration = _compilationUnit_1.toParameterDeclaration(param);
    return ((MutableParameterDeclaration) _parameterDeclaration);
  }
}
