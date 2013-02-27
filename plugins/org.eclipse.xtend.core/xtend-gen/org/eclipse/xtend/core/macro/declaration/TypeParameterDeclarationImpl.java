/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.macro.declaration.AbstractDeclarationImpl;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclarator;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtext.common.types.JvmExecutable;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUpperBound;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class TypeParameterDeclarationImpl extends AbstractDeclarationImpl<JvmTypeParameter> implements TypeParameterDeclaration {
  public List<TypeReference> getUpperBounds() {
    JvmTypeParameter _delegate = this.getDelegate();
    EList<JvmTypeConstraint> _constraints = _delegate.getConstraints();
    Iterable<JvmUpperBound> _filter = Iterables.<JvmUpperBound>filter(_constraints, JvmUpperBound.class);
    final Function1<JvmUpperBound,TypeReference> _function = new Function1<JvmUpperBound,TypeReference>() {
        public TypeReference apply(final JvmUpperBound it) {
          CompilationUnitImpl _compilationUnit = TypeParameterDeclarationImpl.this.getCompilationUnit();
          JvmTypeReference _typeReference = it.getTypeReference();
          TypeReference _typeReference_1 = _compilationUnit.toTypeReference(_typeReference);
          return _typeReference_1;
        }
      };
    Iterable<TypeReference> _map = IterableExtensions.<JvmUpperBound, TypeReference>map(_filter, _function);
    List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
    return _list;
  }
  
  public String getName() {
    JvmTypeParameter _delegate = this.getDelegate();
    String _name = _delegate.getName();
    return _name;
  }
  
  public TypeParameterDeclarator getTypeParameterDeclarator() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    JvmTypeParameter _delegate = this.getDelegate();
    EObject _eContainer = _delegate.eContainer();
    MutableMemberDeclaration _memberDeclaration = _compilationUnit.toMemberDeclaration(((JvmExecutable) _eContainer));
    return ((TypeParameterDeclarator) _memberDeclaration);
  }
  
  public List<AnnotationReference> getAnnotations() {
    List<AnnotationReference> _emptyList = CollectionLiterals.<AnnotationReference>emptyList();
    return _emptyList;
  }
}
