/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.conversion;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

/**
 * @author dhuebner - Initial contribution and API
 */
@SuppressWarnings("all")
public class ASTFlattenerUtils {
  public boolean isDummyType(final TypeDeclaration it) {
    boolean _and = false;
    boolean _and_1 = false;
    SimpleName _name = it.getName();
    String _identifier = _name.getIdentifier();
    boolean _equals = "MISSING".equals(_identifier);
    if (!_equals) {
      _and_1 = false;
    } else {
      boolean _isInterface = it.isInterface();
      boolean _not = (!_isInterface);
      _and_1 = _not;
    }
    if (!_and_1) {
      _and = false;
    } else {
      int _modifiers = it.getModifiers();
      boolean _equals_1 = (_modifiers == 0);
      _and = _equals_1;
    }
    return _and;
  }
  
  public boolean isOverrideMethode(final MethodDeclaration declaration) {
    List _modifiers = declaration.modifiers();
    Iterable<Annotation> _filter = Iterables.<Annotation>filter(_modifiers, Annotation.class);
    final Function1<Annotation, Boolean> _function = new Function1<Annotation, Boolean>() {
      public Boolean apply(final Annotation it) {
        Name _typeName = it.getTypeName();
        String _string = _typeName.toString();
        return Boolean.valueOf(Objects.equal("Override", _string));
      }
    };
    Iterable<Annotation> _filter_1 = IterableExtensions.<Annotation>filter(_filter, _function);
    boolean _isEmpty = IterableExtensions.isEmpty(_filter_1);
    boolean _not = (!_isEmpty);
    if (_not) {
      return true;
    }
    final IMethodBinding iMethodBinding = declaration.resolveBinding();
    boolean _notEquals = (!Objects.equal(iMethodBinding, null));
    if (_notEquals) {
      ITypeBinding _declaringClass = iMethodBinding.getDeclaringClass();
      return this.checkOverride(iMethodBinding, _declaringClass);
    }
    return false;
  }
  
  public boolean checkOverride(final IMethodBinding method, final ITypeBinding type) {
    final ITypeBinding superclass = type.getSuperclass();
    boolean overrides = false;
    boolean _notEquals = (!Objects.equal(superclass, null));
    if (_notEquals) {
      IMethodBinding[] _declaredMethods = superclass.getDeclaredMethods();
      final Function1<IMethodBinding, Boolean> _function = new Function1<IMethodBinding, Boolean>() {
        public Boolean apply(final IMethodBinding it) {
          return Boolean.valueOf(method.overrides(it));
        }
      };
      boolean _exists = IterableExtensions.<IMethodBinding>exists(((Iterable<IMethodBinding>)Conversions.doWrapArray(_declaredMethods)), _function);
      if (_exists) {
        overrides = true;
      } else {
        boolean _checkOverride = this.checkOverride(method, superclass);
        overrides = _checkOverride;
      }
    }
    if ((!overrides)) {
      ITypeBinding[] _interfaces = type.getInterfaces();
      for (final ITypeBinding interfaze : _interfaces) {
        boolean _or = false;
        IMethodBinding[] _declaredMethods_1 = interfaze.getDeclaredMethods();
        final Function1<IMethodBinding, Boolean> _function_1 = new Function1<IMethodBinding, Boolean>() {
          public Boolean apply(final IMethodBinding it) {
            return Boolean.valueOf(method.overrides(it));
          }
        };
        boolean _exists_1 = IterableExtensions.<IMethodBinding>exists(((Iterable<IMethodBinding>)Conversions.doWrapArray(_declaredMethods_1)), _function_1);
        if (_exists_1) {
          _or = true;
        } else {
          boolean _checkOverride_1 = this.checkOverride(method, interfaze);
          _or = _checkOverride_1;
        }
        if (_or) {
          return true;
        }
      }
    }
    return overrides;
  }
  
  public String handleVariableDeclaration(final Iterable<? extends ASTNode> modifier) {
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(modifier, Modifier.class);
    boolean _isFinal = this.isFinal(_filter);
    if (_isFinal) {
      return "val";
    } else {
      return "var";
    }
  }
  
  public boolean isNotSupportedInnerType(final TypeDeclaration it) {
    boolean _and = false;
    boolean _and_1 = false;
    boolean _isInterface = it.isInterface();
    boolean _not = (!_isInterface);
    if (!_not) {
      _and_1 = false;
    } else {
      _and_1 = ((it.getParent() instanceof TypeDeclaration) || (it.getParent() instanceof Block));
    }
    if (!_and_1) {
      _and = false;
    } else {
      List _modifiers = it.modifiers();
      Iterable<Modifier> _filter = Iterables.<Modifier>filter(_modifiers, Modifier.class);
      final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
        public Boolean apply(final Modifier it) {
          return Boolean.valueOf(it.isStatic());
        }
      };
      boolean _exists = IterableExtensions.<Modifier>exists(_filter, _function);
      boolean _not_1 = (!_exists);
      _and = _not_1;
    }
    return _and;
  }
  
  public boolean isNotSupportedInnerType(final TypeDeclarationStatement it) {
    ASTNode _parent = it.getParent();
    return (_parent instanceof Block);
  }
  
  public boolean isFinal(final Iterable<Modifier> modifiers) {
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(modifiers, Modifier.class);
    final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
      public Boolean apply(final Modifier it) {
        return Boolean.valueOf(it.isFinal());
      }
    };
    return IterableExtensions.<Modifier>exists(_filter, _function);
  }
  
  public boolean isStatic(final Iterable<IExtendedModifier> modifiers) {
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(modifiers, Modifier.class);
    final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
      public Boolean apply(final Modifier it) {
        return Boolean.valueOf(it.isStatic());
      }
    };
    return IterableExtensions.<Modifier>exists(_filter, _function);
  }
  
  public boolean isPackageVisibility(final Iterable<Modifier> modifier) {
    final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
      public Boolean apply(final Modifier it) {
        boolean _or = false;
        boolean _or_1 = false;
        boolean _isPublic = it.isPublic();
        if (_isPublic) {
          _or_1 = true;
        } else {
          boolean _isPrivate = it.isPrivate();
          _or_1 = _isPrivate;
        }
        if (_or_1) {
          _or = true;
        } else {
          boolean _isProtected = it.isProtected();
          _or = _isProtected;
        }
        return Boolean.valueOf(_or);
      }
    };
    Iterable<Modifier> _filter = IterableExtensions.<Modifier>filter(modifier, _function);
    return IterableExtensions.isEmpty(_filter);
  }
  
  public boolean canHandleAnnotation(final ASTNode node) {
    if ((node instanceof VariableDeclarationStatement)) {
      return false;
    }
    return true;
  }
  
  public boolean shouldConvertName(final SimpleName it) {
    boolean _or = false;
    ASTNode _parent = it.getParent();
    if ((_parent instanceof FieldAccess)) {
      _or = true;
    } else {
      _or = ((it.getParent() instanceof VariableDeclarationFragment) && (it.getParent().getParent() instanceof FieldDeclaration));
    }
    return _or;
  }
}
