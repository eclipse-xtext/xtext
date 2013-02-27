/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.XtendAnnotationTargetImpl;
import org.eclipse.xtend.core.macro.declaration.XtendMemberDeclarationImpl;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendParameter;
import org.eclipse.xtend.lib.macro.declaration.ExecutableDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;

@SuppressWarnings("all")
public class XtendParameterDeclarationImpl extends XtendAnnotationTargetImpl<XtendParameter> implements ParameterDeclaration {
  public TypeReference getType() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    XtendParameter _delegate = this.getDelegate();
    JvmTypeReference _parameterType = _delegate.getParameterType();
    TypeReference _typeReference = _compilationUnit.toTypeReference(_parameterType);
    return _typeReference;
  }
  
  public String getName() {
    XtendParameter _delegate = this.getDelegate();
    String _name = _delegate.getName();
    return _name;
  }
  
  public ExecutableDeclaration getDeclaringExecutable() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    XtendParameter _delegate = this.getDelegate();
    EObject _eContainer = _delegate.eContainer();
    XtendMemberDeclarationImpl<? extends XtendMember> _xtendMemberDeclaration = _compilationUnit.toXtendMemberDeclaration(((XtendMember) _eContainer));
    return ((ExecutableDeclaration) _xtendMemberDeclaration);
  }
}
