/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.core.macro.ConditionUtils;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmAnnotationTargetImpl;
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.impl.JvmMemberImplCustom;
import org.eclipse.xtext.xbase.compiler.DocumentationAdapter;

@SuppressWarnings("all")
public abstract class JvmMemberDeclarationImpl<T extends JvmMember> extends JvmAnnotationTargetImpl<T> {
  public String getDocComment() {
    T _delegate = this.getDelegate();
    EList<Adapter> _eAdapters = _delegate.eAdapters();
    Adapter _adapter = EcoreUtil.getAdapter(_eAdapters, DocumentationAdapter.class);
    final DocumentationAdapter adapter = ((DocumentationAdapter) _adapter);
    String _documentation = null;
    if (adapter!=null) {
      _documentation=adapter.getDocumentation();
    }
    return _documentation;
  }
  
  public void setDocComment(final String docComment) {
    this.checkMutable();
    T _delegate = this.getDelegate();
    EList<Adapter> _eAdapters = _delegate.eAdapters();
    Adapter _adapter = EcoreUtil.getAdapter(_eAdapters, DocumentationAdapter.class);
    DocumentationAdapter adapter = ((DocumentationAdapter) _adapter);
    boolean _equals = Objects.equal(adapter, null);
    if (_equals) {
      DocumentationAdapter _documentationAdapter = new DocumentationAdapter();
      adapter = _documentationAdapter;
      T _delegate_1 = this.getDelegate();
      EList<Adapter> _eAdapters_1 = _delegate_1.eAdapters();
      _eAdapters_1.add(adapter);
    }
    adapter.setDocumentation(docComment);
  }
  
  public Visibility getVisibility() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    T _delegate = this.getDelegate();
    JvmVisibility _visibility = _delegate.getVisibility();
    return _compilationUnit.toVisibility(_visibility);
  }
  
  public void setVisibility(final Visibility visibility) {
    this.checkMutable();
    T _delegate = this.getDelegate();
    JvmVisibility _switchResult = null;
    if (visibility != null) {
      switch (visibility) {
        case DEFAULT:
          _switchResult = JvmVisibility.DEFAULT;
          break;
        case PUBLIC:
          _switchResult = JvmVisibility.PUBLIC;
          break;
        case PRIVATE:
          _switchResult = JvmVisibility.PRIVATE;
          break;
        case PROTECTED:
          _switchResult = JvmVisibility.PROTECTED;
          break;
        default:
          break;
      }
    }
    _delegate.setVisibility(_switchResult);
  }
  
  public TypeDeclaration getDeclaringType() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    T _delegate = this.getDelegate();
    JvmDeclaredType _declaringType = _delegate.getDeclaringType();
    return _compilationUnit.toTypeDeclaration(_declaringType);
  }
  
  public void setSimpleName(final String name) {
    this.checkMutable();
    ConditionUtils.checkJavaIdentifier(name, "name");
    T _delegate = this.getDelegate();
    final T it = _delegate;
    boolean _matched = false;
    if (!_matched) {
      if (it instanceof JvmMemberImplCustom) {
        _matched=true;
        ((JvmMemberImplCustom)it).clearIdentifierCache();
      }
    }
    T _delegate_1 = this.getDelegate();
    _delegate_1.setSimpleName(name);
  }
}
