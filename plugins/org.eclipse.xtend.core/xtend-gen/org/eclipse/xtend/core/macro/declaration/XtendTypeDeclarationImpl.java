/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.XtendMemberDeclarationImpl;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ConstructorDeclaration;
import org.eclipse.xtend.lib.macro.declaration.FieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.InterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.ParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.declaration.Visibility;
import org.eclipse.xtend.lib.macro.services.TypeReferenceProvider;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public abstract class XtendTypeDeclarationImpl<T extends XtendTypeDeclaration> extends XtendMemberDeclarationImpl<T> implements TypeDeclaration {
  public String getPackageName() {
    T _delegate = this.getDelegate();
    EObject _eContainer = _delegate.eContainer();
    return ((XtendFile) _eContainer).getPackage();
  }
  
  public String getSimpleName() {
    T _delegate = this.getDelegate();
    String _name = _delegate.getName();
    return _name;
  }
  
  public String getName() {
    String _xifexpression = null;
    String _packageName = this.getPackageName();
    boolean _notEquals = (!Objects.equal(_packageName, null));
    if (_notEquals) {
      String _packageName_1 = this.getPackageName();
      String _plus = (_packageName_1 + ".");
      String _simpleName = this.getSimpleName();
      String _plus_1 = (_plus + _simpleName);
      _xifexpression = _plus_1;
    } else {
      String _simpleName_1 = this.getSimpleName();
      _xifexpression = _simpleName_1;
    }
    return _xifexpression;
  }
  
  public Visibility getVisibility() {
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    T _delegate = this.getDelegate();
    JvmVisibility _visibility = _delegate.getVisibility();
    return _compilationUnit.toVisibility(_visibility);
  }
  
  public Iterable<? extends MemberDeclaration> getDeclaredMembers() {
    T _delegate = this.getDelegate();
    EList<XtendMember> _members = _delegate.getMembers();
    final Function1<XtendMember,MemberDeclaration> _function = new Function1<XtendMember,MemberDeclaration>() {
        public MemberDeclaration apply(final XtendMember it) {
          CompilationUnitImpl _compilationUnit = XtendTypeDeclarationImpl.this.getCompilationUnit();
          MemberDeclaration _xtendMemberDeclaration = _compilationUnit.toXtendMemberDeclaration(it);
          return _xtendMemberDeclaration;
        }
      };
    return ListExtensions.<XtendMember, MemberDeclaration>map(_members, _function);
  }
  
  public boolean isAssignableFrom(final Type otherType) {
    boolean _equals = Objects.equal(otherType, null);
    if (_equals) {
      return false;
    }
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    TypeReferenceProvider _typeReferenceProvider = _compilationUnit.getTypeReferenceProvider();
    final TypeReference thisTypeRef = _typeReferenceProvider.newTypeReference(this);
    CompilationUnitImpl _compilationUnit_1 = this.getCompilationUnit();
    TypeReferenceProvider _typeReferenceProvider_1 = _compilationUnit_1.getTypeReferenceProvider();
    final TypeReference thatTypeRef = _typeReferenceProvider_1.newTypeReference(otherType);
    return thisTypeRef.isAssignableFrom(thatTypeRef);
  }
  
  public ConstructorDeclaration findConstructor(final TypeReference... parameterTypes) {
    Iterable<? extends ConstructorDeclaration> _declaredConstructors = this.getDeclaredConstructors();
    final Function1<ConstructorDeclaration,Boolean> _function = new Function1<ConstructorDeclaration,Boolean>() {
        public Boolean apply(final ConstructorDeclaration constructor) {
          List<? extends ParameterDeclaration> _parameters = constructor.getParameters();
          final Function1<ParameterDeclaration,TypeReference> _function = new Function1<ParameterDeclaration,TypeReference>() {
              public TypeReference apply(final ParameterDeclaration it) {
                TypeReference _type = it.getType();
                return _type;
              }
            };
          List<TypeReference> _map = ListExtensions.map(_parameters, _function);
          List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
          List<TypeReference> _list_1 = IterableExtensions.<TypeReference>toList(((Iterable<TypeReference>)Conversions.doWrapArray(parameterTypes)));
          boolean _equals = Objects.equal(_list, _list_1);
          return Boolean.valueOf(_equals);
        }
      };
    ConstructorDeclaration _findFirst = IterableExtensions.findFirst(_declaredConstructors, _function);
    return _findFirst;
  }
  
  public FieldDeclaration findField(final String name) {
    Iterable<? extends FieldDeclaration> _declaredFields = this.getDeclaredFields();
    final Function1<FieldDeclaration,Boolean> _function = new Function1<FieldDeclaration,Boolean>() {
        public Boolean apply(final FieldDeclaration field) {
          String _name = field.getName();
          boolean _equals = Objects.equal(_name, name);
          return Boolean.valueOf(_equals);
        }
      };
    FieldDeclaration _findFirst = IterableExtensions.findFirst(_declaredFields, _function);
    return _findFirst;
  }
  
  public MethodDeclaration findMethod(final String name, final TypeReference... parameterTypes) {
    Iterable<? extends MethodDeclaration> _declaredMethods = this.getDeclaredMethods();
    final Function1<MethodDeclaration,Boolean> _function = new Function1<MethodDeclaration,Boolean>() {
        public Boolean apply(final MethodDeclaration method) {
          boolean _and = false;
          String _name = method.getName();
          boolean _equals = Objects.equal(_name, name);
          if (!_equals) {
            _and = false;
          } else {
            List<? extends ParameterDeclaration> _parameters = method.getParameters();
            final Function1<ParameterDeclaration,TypeReference> _function = new Function1<ParameterDeclaration,TypeReference>() {
                public TypeReference apply(final ParameterDeclaration it) {
                  TypeReference _type = it.getType();
                  return _type;
                }
              };
            List<TypeReference> _map = ListExtensions.map(_parameters, _function);
            List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
            List<TypeReference> _list_1 = IterableExtensions.<TypeReference>toList(((Iterable<TypeReference>)Conversions.doWrapArray(parameterTypes)));
            boolean _equals_1 = Objects.equal(_list, _list_1);
            _and = (_equals && _equals_1);
          }
          return Boolean.valueOf(_and);
        }
      };
    MethodDeclaration _findFirst = IterableExtensions.findFirst(_declaredMethods, _function);
    return _findFirst;
  }
  
  public Iterable<? extends MethodDeclaration> getDeclaredMethods() {
    Iterable<? extends MemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MethodDeclaration> _filter = Iterables.<MethodDeclaration>filter(_declaredMembers, MethodDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends FieldDeclaration> getDeclaredFields() {
    Iterable<? extends MemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<FieldDeclaration> _filter = Iterables.<FieldDeclaration>filter(_declaredMembers, FieldDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends ClassDeclaration> getDeclaredClasses() {
    Iterable<? extends MemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<ClassDeclaration> _filter = Iterables.<ClassDeclaration>filter(_declaredMembers, ClassDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends ConstructorDeclaration> getDeclaredConstructors() {
    Iterable<? extends MemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<ConstructorDeclaration> _filter = Iterables.<ConstructorDeclaration>filter(_declaredMembers, ConstructorDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends InterfaceDeclaration> getDeclaredInterfaces() {
    Iterable<? extends MemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<InterfaceDeclaration> _filter = Iterables.<InterfaceDeclaration>filter(_declaredMembers, InterfaceDeclaration.class);
    return _filter;
  }
}
