/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmExecutableDeclarationImpl;
import org.eclipse.xtend.core.macro.declaration.TypeReferenceImpl;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

@SuppressWarnings("all")
public class JvmMethodDeclarationImpl extends JvmExecutableDeclarationImpl<JvmOperation> implements MutableMethodDeclaration {
  public boolean isAbstract() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isAbstract = _delegate.isAbstract();
    return _isAbstract;
  }
  
  public boolean isFinal() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isFinal = _delegate.isFinal();
    return _isFinal;
  }
  
  public boolean isStatic() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isStatic = _delegate.isStatic();
    return _isStatic;
  }
  
  public boolean isSynchronized() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isSynchronized = _delegate.isSynchronized();
    return _isSynchronized;
  }
  
  public boolean isDefault() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isDefault = _delegate.isDefault();
    return _isDefault;
  }
  
  public boolean isStrictFloatingPoint() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isStrictFloatingPoint = _delegate.isStrictFloatingPoint();
    return _isStrictFloatingPoint;
  }
  
  public void setStrictFloatingPoint(final boolean isStrictFloatingPoint) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setStrictFloatingPoint(isStrictFloatingPoint);
  }
  
  public boolean isNative() {
    JvmOperation _delegate = this.getDelegate();
    boolean _isNative = _delegate.isNative();
    return _isNative;
  }
  
  public void setNative(final boolean isNative) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setNative(isNative);
  }
  
  public TypeReference getReturnType() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmOperation _delegate = this.getDelegate();
    JvmTypeReference _returnType = _delegate.getReturnType();
    TypeReference _typeReference = _compilationUnit.toTypeReference(_returnType);
    return _typeReference;
  }
  
  public void setReturnType(final TypeReference type) {
    boolean _notEquals = (!Objects.equal(type, null));
    Preconditions.checkArgument(_notEquals, "returnType cannot be null");
    JvmOperation _delegate = this.getDelegate();
    LightweightTypeReference _lightWeightTypeReference = ((TypeReferenceImpl) type).getLightWeightTypeReference();
    JvmTypeReference _javaCompliantTypeReference = _lightWeightTypeReference.toJavaCompliantTypeReference();
    _delegate.setReturnType(_javaCompliantTypeReference);
  }
  
  public void setAbstract(final boolean isAbstract) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setAbstract(isAbstract);
  }
  
  public void setFinal(final boolean isFinal) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setFinal(isFinal);
  }
  
  public void setStatic(final boolean isStatic) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setStatic(isStatic);
  }
  
  public void setSynchronized(final boolean isSynchronized) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setSynchronized(isSynchronized);
  }
  
  public void setDefault(final boolean isDefault) {
    JvmOperation _delegate = this.getDelegate();
    _delegate.setDefault(isDefault);
  }
}
