/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro.declaration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.JvmMemberDeclarationImpl;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableConstructorDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableParameterDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.declaration.TypeReference;
import org.eclipse.xtend.lib.macro.services.TypeReferenceProvider;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public abstract class JvmTypeDeclarationImpl<T extends JvmDeclaredType> extends JvmMemberDeclarationImpl<T> implements MutableTypeDeclaration {
  public Iterable<? extends MutableMemberDeclaration> getDeclaredMembers() {
    T _delegate = this.getDelegate();
    EList<JvmMember> _members = _delegate.getMembers();
    final Function1<JvmMember,MutableMemberDeclaration> _function = new Function1<JvmMember,MutableMemberDeclaration>() {
        public MutableMemberDeclaration apply(final JvmMember it) {
          CompilationUnitImpl _compilationUnit = JvmTypeDeclarationImpl.this.getCompilationUnit();
          MutableMemberDeclaration _memberDeclaration = _compilationUnit.toMemberDeclaration(it);
          return _memberDeclaration;
        }
      };
    List<MutableMemberDeclaration> _map = ListExtensions.<JvmMember, MutableMemberDeclaration>map(_members, _function);
    ImmutableList<MutableMemberDeclaration> _copyOf = ImmutableList.<MutableMemberDeclaration>copyOf(_map);
    return _copyOf;
  }
  
  public String getSimpleName() {
    T _delegate = this.getDelegate();
    String _simpleName = _delegate.getSimpleName();
    return _simpleName;
  }
  
  public String getName() {
    T _delegate = this.getDelegate();
    String _identifier = _delegate.getIdentifier();
    return _identifier;
  }
  
  public void setName(final String name) {
    final int idx = name.lastIndexOf(".");
    int _minus = (-1);
    boolean _equals = (idx == _minus);
    if (_equals) {
      T _delegate = this.getDelegate();
      _delegate.setPackageName(null);
      T _delegate_1 = this.getDelegate();
      _delegate_1.setSimpleName(name);
    } else {
      T _delegate_2 = this.getDelegate();
      int _minus_1 = (idx - 1);
      String _substring = name.substring(0, _minus_1);
      _delegate_2.setPackageName(_substring);
      T _delegate_3 = this.getDelegate();
      String _substring_1 = name.substring(idx);
      _delegate_3.setSimpleName(_substring_1);
    }
  }
  
  public boolean isAssignableFrom(final Type otherType) {
    boolean _equals = ObjectExtensions.operator_equals(otherType, null);
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
  
  public MutableConstructorDeclaration addConstructor(final Procedure1<MutableConstructorDeclaration> initializer) {
    final JvmConstructor newConstructor = TypesFactory.eINSTANCE.createJvmConstructor();
    newConstructor.setVisibility(JvmVisibility.PUBLIC);
    String _simpleName = this.getSimpleName();
    newConstructor.setSimpleName(_simpleName);
    T _delegate = this.getDelegate();
    EList<JvmMember> _members = _delegate.getMembers();
    _members.add(newConstructor);
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    MutableMemberDeclaration _memberDeclaration = _compilationUnit.toMemberDeclaration(newConstructor);
    final MutableConstructorDeclaration mutableConstructorDeclaration = ((MutableConstructorDeclaration) _memberDeclaration);
    initializer.apply(mutableConstructorDeclaration);
    return mutableConstructorDeclaration;
  }
  
  public MutableFieldDeclaration addField(final String name, final Procedure1<MutableFieldDeclaration> initializer) {
    final JvmField newField = TypesFactory.eINSTANCE.createJvmField();
    newField.setSimpleName(name);
    newField.setVisibility(JvmVisibility.PRIVATE);
    T _delegate = this.getDelegate();
    EList<JvmMember> _members = _delegate.getMembers();
    _members.add(newField);
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    MutableMemberDeclaration _memberDeclaration = _compilationUnit.toMemberDeclaration(newField);
    final MutableFieldDeclaration mutableFieldDeclaration = ((MutableFieldDeclaration) _memberDeclaration);
    initializer.apply(mutableFieldDeclaration);
    return mutableFieldDeclaration;
  }
  
  public MutableMethodDeclaration addMethod(final String name, final Procedure1<MutableMethodDeclaration> initializer) {
    final JvmOperation newMethod = TypesFactory.eINSTANCE.createJvmOperation();
    newMethod.setVisibility(JvmVisibility.PUBLIC);
    newMethod.setSimpleName(name);
    CompilationUnitImpl _compilationUnit = this.getCompilationUnit();
    CompilationUnitImpl _compilationUnit_1 = this.getCompilationUnit();
    TypeReferenceProvider _typeReferenceProvider = _compilationUnit_1.getTypeReferenceProvider();
    TypeReference _primitiveVoid = _typeReferenceProvider.getPrimitiveVoid();
    JvmTypeReference _jvmTypeReference = _compilationUnit.toJvmTypeReference(_primitiveVoid);
    newMethod.setReturnType(_jvmTypeReference);
    T _delegate = this.getDelegate();
    EList<JvmMember> _members = _delegate.getMembers();
    _members.add(newMethod);
    CompilationUnitImpl _compilationUnit_2 = this.getCompilationUnit();
    MutableMemberDeclaration _memberDeclaration = _compilationUnit_2.toMemberDeclaration(newMethod);
    final MutableMethodDeclaration mutableMethodDeclaration = ((MutableMethodDeclaration) _memberDeclaration);
    initializer.apply(mutableMethodDeclaration);
    return mutableMethodDeclaration;
  }
  
  public MutableConstructorDeclaration findConstructor(final TypeReference... parameterTypes) {
    Iterable<? extends MutableConstructorDeclaration> _declaredConstructors = this.getDeclaredConstructors();
    final Function1<MutableConstructorDeclaration,Boolean> _function = new Function1<MutableConstructorDeclaration,Boolean>() {
        public Boolean apply(final MutableConstructorDeclaration constructor) {
          List<MutableParameterDeclaration> _parameters = constructor.getParameters();
          final Function1<MutableParameterDeclaration,TypeReference> _function = new Function1<MutableParameterDeclaration,TypeReference>() {
              public TypeReference apply(final MutableParameterDeclaration it) {
                TypeReference _type = it.getType();
                return _type;
              }
            };
          List<TypeReference> _map = ListExtensions.<MutableParameterDeclaration, TypeReference>map(_parameters, _function);
          List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
          List<TypeReference> _list_1 = IterableExtensions.<TypeReference>toList(((Iterable<TypeReference>)Conversions.doWrapArray(parameterTypes)));
          boolean _equals = ObjectExtensions.operator_equals(_list, _list_1);
          return Boolean.valueOf(_equals);
        }
      };
    MutableConstructorDeclaration _findFirst = IterableExtensions.findFirst(_declaredConstructors, _function);
    return _findFirst;
  }
  
  public MutableFieldDeclaration findField(final String name) {
    Iterable<? extends MutableFieldDeclaration> _declaredFields = this.getDeclaredFields();
    final Function1<MutableFieldDeclaration,Boolean> _function = new Function1<MutableFieldDeclaration,Boolean>() {
        public Boolean apply(final MutableFieldDeclaration field) {
          String _name = field.getName();
          boolean _equals = ObjectExtensions.operator_equals(_name, name);
          return Boolean.valueOf(_equals);
        }
      };
    MutableFieldDeclaration _findFirst = IterableExtensions.findFirst(_declaredFields, _function);
    return _findFirst;
  }
  
  public MutableMethodDeclaration findMethod(final String name, final TypeReference... parameterTypes) {
    Iterable<? extends MutableMethodDeclaration> _declaredMethods = this.getDeclaredMethods();
    final Function1<MutableMethodDeclaration,Boolean> _function = new Function1<MutableMethodDeclaration,Boolean>() {
        public Boolean apply(final MutableMethodDeclaration method) {
          boolean _and = false;
          String _name = method.getName();
          boolean _equals = ObjectExtensions.operator_equals(_name, name);
          if (!_equals) {
            _and = false;
          } else {
            List<MutableParameterDeclaration> _parameters = method.getParameters();
            final Function1<MutableParameterDeclaration,TypeReference> _function = new Function1<MutableParameterDeclaration,TypeReference>() {
                public TypeReference apply(final MutableParameterDeclaration it) {
                  TypeReference _type = it.getType();
                  return _type;
                }
              };
            List<TypeReference> _map = ListExtensions.<MutableParameterDeclaration, TypeReference>map(_parameters, _function);
            List<TypeReference> _list = IterableExtensions.<TypeReference>toList(_map);
            List<TypeReference> _list_1 = IterableExtensions.<TypeReference>toList(((Iterable<TypeReference>)Conversions.doWrapArray(parameterTypes)));
            boolean _equals_1 = ObjectExtensions.operator_equals(_list, _list_1);
            _and = (_equals && _equals_1);
          }
          return Boolean.valueOf(_and);
        }
      };
    MutableMethodDeclaration _findFirst = IterableExtensions.findFirst(_declaredMethods, _function);
    return _findFirst;
  }
  
  public Iterable<? extends MutableMethodDeclaration> getDeclaredMethods() {
    Iterable<? extends MutableMemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MutableMethodDeclaration> _filter = Iterables.<MutableMethodDeclaration>filter(_declaredMembers, MutableMethodDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends MutableFieldDeclaration> getDeclaredFields() {
    Iterable<? extends MutableMemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MutableFieldDeclaration> _filter = Iterables.<MutableFieldDeclaration>filter(_declaredMembers, MutableFieldDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends MutableClassDeclaration> getDeclaredClasses() {
    Iterable<? extends MutableMemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MutableClassDeclaration> _filter = Iterables.<MutableClassDeclaration>filter(_declaredMembers, MutableClassDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends MutableConstructorDeclaration> getDeclaredConstructors() {
    Iterable<? extends MutableMemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MutableConstructorDeclaration> _filter = Iterables.<MutableConstructorDeclaration>filter(_declaredMembers, MutableConstructorDeclaration.class);
    return _filter;
  }
  
  public Iterable<? extends MutableInterfaceDeclaration> getDeclaredInterfaces() {
    Iterable<? extends MutableMemberDeclaration> _declaredMembers = this.getDeclaredMembers();
    Iterable<MutableInterfaceDeclaration> _filter = Iterables.<MutableInterfaceDeclaration>filter(_declaredMembers, MutableInterfaceDeclaration.class);
    return _filter;
  }
}
