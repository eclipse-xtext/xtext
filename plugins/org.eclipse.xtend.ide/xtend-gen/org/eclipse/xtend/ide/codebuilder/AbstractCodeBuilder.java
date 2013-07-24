/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.codebuilder;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.xtend.ide.codebuilder.ICodeBuilder;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeConstraint;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmUnknownTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.util.jdt.IJavaElementFinder;
import org.eclipse.xtext.xbase.compiler.IAppendable;
import org.eclipse.xtext.xbase.compiler.StringBuilderBasedAppendable;
import org.eclipse.xtext.xbase.compiler.TypeReferenceSerializer;
import org.eclipse.xtext.xbase.lib.Extension;

/**
 * @author Jan Koehnlein
 */
@NonNullByDefault
@SuppressWarnings("all")
public abstract class AbstractCodeBuilder implements ICodeBuilder {
  private Object _ownerSource;
  
  public Object getOwnerSource() {
    return this._ownerSource;
  }
  
  public void setOwnerSource(final Object ownerSource) {
    this._ownerSource = ownerSource;
  }
  
  private JvmDeclaredType _owner;
  
  public JvmDeclaredType getOwner() {
    return this._owner;
  }
  
  public void setOwner(final JvmDeclaredType owner) {
    this._owner = owner;
  }
  
  private JvmVisibility _visibility;
  
  public JvmVisibility getVisibility() {
    return this._visibility;
  }
  
  public void setVisibility(final JvmVisibility visibility) {
    this._visibility = visibility;
  }
  
  private EObject _context;
  
  public EObject getContext() {
    return this._context;
  }
  
  public void setContext(final EObject context) {
    this._context = context;
  }
  
  @Inject
  @Extension
  private IJavaElementFinder _iJavaElementFinder;
  
  public boolean isValid() {
    JvmDeclaredType _owner = this.getOwner();
    final IJavaElement javaElement = this._iJavaElementFinder.findElementFor(_owner);
    boolean _and = false;
    boolean _and_1 = false;
    boolean _and_2 = false;
    boolean _or = false;
    boolean _equals = Objects.equal(javaElement, null);
    if (_equals) {
      _or = true;
    } else {
      boolean _isReadOnly = javaElement.isReadOnly();
      boolean _not = (!_isReadOnly);
      _or = (_equals || _not);
    }
    if (!_or) {
      _and_2 = false;
    } else {
      Object _ownerSource = this.getOwnerSource();
      boolean _notEquals = (!Objects.equal(_ownerSource, null));
      _and_2 = (_or && _notEquals);
    }
    if (!_and_2) {
      _and_1 = false;
    } else {
      JvmDeclaredType _owner_1 = this.getOwner();
      boolean _notEquals_1 = (!Objects.equal(_owner_1, null));
      _and_1 = (_and_2 && _notEquals_1);
    }
    if (!_and_1) {
      _and = false;
    } else {
      EObject _context = this.getContext();
      boolean _notEquals_2 = (!Objects.equal(_context, null));
      _and = (_and_1 && _notEquals_2);
    }
    return _and;
  }
  
  public String getPreview() {
    String _xblockexpression = null;
    {
      StringBuilderBasedAppendable _stringBuilderBasedAppendable = new StringBuilderBasedAppendable();
      final StringBuilderBasedAppendable appendable = _stringBuilderBasedAppendable;
      IAppendable _append = appendable.append("...");
      IAppendable _newLine = _append.newLine();
      IAppendable _build = this.build(_newLine);
      IAppendable _newLine_1 = _build.newLine();
      _newLine_1.append("...");
      String _string = appendable.toString();
      _xblockexpression = (_string);
    }
    return _xblockexpression;
  }
  
  protected IAppendable appendVisibility(final IAppendable appendable, final JvmVisibility visibility, final JvmVisibility skippableDefault) {
    String _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(visibility,skippableDefault)) {
        _matched=true;
        _switchResult = "";
      }
    }
    if (!_matched) {
      if (Objects.equal(visibility,JvmVisibility.PRIVATE)) {
        _matched=true;
        _switchResult = "private ";
      }
    }
    if (!_matched) {
      if (Objects.equal(visibility,JvmVisibility.PROTECTED)) {
        _matched=true;
        _switchResult = "protected ";
      }
    }
    if (!_matched) {
      if (Objects.equal(visibility,JvmVisibility.PUBLIC)) {
        _matched=true;
        _switchResult = "public ";
      }
    }
    if (!_matched) {
      _switchResult = "";
    }
    IAppendable _append = appendable.append(_switchResult);
    return _append;
  }
  
  protected IAppendable appendType(final IAppendable appendable, final JvmTypeReference typeRef, final String surrogate) {
    IAppendable _xblockexpression = null;
    {
      boolean _equals = Objects.equal(typeRef, null);
      if (_equals) {
        appendable.append(surrogate);
      } else {
        TypeReferenceSerializer _typeReferenceSerializer = this.getTypeReferenceSerializer();
        EObject _context = this.getContext();
        _typeReferenceSerializer.serialize(typeRef, _context, appendable);
      }
      _xblockexpression = (appendable);
    }
    return _xblockexpression;
  }
  
  protected IAppendable appendTypeParameters(final IAppendable appendable, final List<JvmTypeParameter> typeParameters) {
    IAppendable _xblockexpression = null;
    {
      final Iterator<JvmTypeParameter> iterator = typeParameters.iterator();
      boolean _hasNext = iterator.hasNext();
      if (_hasNext) {
        appendable.append("<");
        boolean _dowhile = false;
        do {
          {
            final JvmTypeParameter typeParameter = iterator.next();
            boolean _notEquals = (!Objects.equal(typeParameter, null));
            if (_notEquals) {
              String _name = typeParameter.getName();
              appendable.append(_name);
              EList<JvmTypeConstraint> _constraints = typeParameter.getConstraints();
              boolean _notEquals_1 = (!Objects.equal(_constraints, null));
              if (_notEquals_1) {
              }
            }
            boolean _hasNext_1 = iterator.hasNext();
            if (_hasNext_1) {
              appendable.append(",");
            }
          }
          boolean _hasNext_1 = iterator.hasNext();
          _dowhile = _hasNext_1;
        } while(_dowhile);
        appendable.append("> ");
      }
      _xblockexpression = (appendable);
    }
    return _xblockexpression;
  }
  
  protected abstract TypeReferenceSerializer getTypeReferenceSerializer();
  
  protected String getIdentifierOrObject(final JvmTypeReference typeReference) {
    String _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (typeReference instanceof JvmUnknownTypeReference) {
        final JvmUnknownTypeReference _jvmUnknownTypeReference = (JvmUnknownTypeReference)typeReference;
        _matched=true;
        _switchResult = "java.lang.Object";
      }
    }
    if (!_matched) {
      String _identifier = typeReference.getIdentifier();
      _switchResult = _identifier;
    }
    return _switchResult;
  }
  
  protected boolean isInterface(final JvmType t) {
    boolean _switchResult = false;
    boolean _matched = false;
    if (!_matched) {
      if (t instanceof JvmGenericType) {
        final JvmGenericType _jvmGenericType = (JvmGenericType)t;
        _matched=true;
        boolean _isInterface = _jvmGenericType.isInterface();
        _switchResult = _isInterface;
      }
    }
    if (!_matched) {
      _switchResult = false;
    }
    return _switchResult;
  }
}
