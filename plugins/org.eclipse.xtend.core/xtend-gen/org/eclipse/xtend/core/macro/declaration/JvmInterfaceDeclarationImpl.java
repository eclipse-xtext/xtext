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
import org.eclipse.xtend.core.macro.declaration.JvmTypeDeclarationImpl;
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class JvmInterfaceDeclarationImpl extends JvmTypeDeclarationImpl<JvmGenericType> implements MutableInterfaceDeclaration {
  public List<TypeReference> getSuperInterfaces() {
    List<TypeReference> _xblockexpression = null;
    {
      JvmGenericType _delegate = this.getDelegate();
      EList<JvmTypeReference> _superTypes = _delegate.getSuperTypes();
      final Function1<JvmTypeReference,Boolean> _function = new Function1<JvmTypeReference,Boolean>() {
          public Boolean apply(final JvmTypeReference it) {
            JvmType _type = it.getType();
            boolean _isInterface = ((JvmGenericType) _type).isInterface();
            return Boolean.valueOf(_isInterface);
          }
        };
      final Iterable<JvmTypeReference> filtered = IterableExtensions.<JvmTypeReference>filter(_superTypes, _function);
      final Function1<JvmTypeReference,TypeReference> _function_1 = new Function1<JvmTypeReference,TypeReference>() {
          public TypeReference apply(final JvmTypeReference it) {
            CompilationUnitImpl _compilationUnit = JvmInterfaceDeclarationImpl.this.getCompilationUnit();
            TypeReference _typeReference = _compilationUnit.toTypeReference(it);
            return _typeReference;
          }
        };
      Iterable<TypeReference> _map = IterableExtensions.<JvmTypeReference, TypeReference>map(filtered, _function_1);
      List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
      _xblockexpression = (_list);
    }
    return _xblockexpression;
  }
  
  public List<? extends TypeParameterDeclaration> getTypeParameters() {
    JvmGenericType _delegate = this.getDelegate();
    EList<JvmTypeParameter> _typeParameters = _delegate.getTypeParameters();
    final Function1<JvmTypeParameter,MutableTypeParameterDeclaration> _function = new Function1<JvmTypeParameter,MutableTypeParameterDeclaration>() {
        public MutableTypeParameterDeclaration apply(final JvmTypeParameter it) {
          CompilationUnitImpl _compilationUnit = JvmInterfaceDeclarationImpl.this.getCompilationUnit();
          MutableTypeParameterDeclaration _typeParameterDeclaration = _compilationUnit.toTypeParameterDeclaration(it);
          return _typeParameterDeclaration;
        }
      };
    List<MutableTypeParameterDeclaration> _map = ListExtensions.<JvmTypeParameter, MutableTypeParameterDeclaration>map(_typeParameters, _function);
    return _map;
  }
}
