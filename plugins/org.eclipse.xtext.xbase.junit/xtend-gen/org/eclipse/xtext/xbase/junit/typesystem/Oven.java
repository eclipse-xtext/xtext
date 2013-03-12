/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.junit.typesystem;

import com.google.common.annotations.Beta;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.junit4.util.ParseHelper;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.junit.typesystem.SimpleBloomFilter;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.util.ReflectExtensions;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.internal.CompoundReentrantTypeResolver;
import org.eclipse.xtext.xbase.typesystem.internal.RootResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.internal.TypeData;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.junit.Assert;

/**
 * Utility to check a given expression for exceptions and integrity
 * after it was processed by the {@link IBatchTypeResolver}
 * 
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@Beta
@Singleton
@SuppressWarnings("all")
public class Oven extends Assert {
  @Inject
  private IBatchTypeResolver typeResolver;
  
  private final SimpleBloomFilter alreadyBaked = new Function0<SimpleBloomFilter>() {
    public SimpleBloomFilter apply() {
      SimpleBloomFilter _create = SimpleBloomFilter.create(5000000);
      return _create;
    }
  }.apply();
  
  @Inject
  @Extension
  private ReflectExtensions _reflectExtensions;
  
  @Inject
  @Extension
  private ParseHelper<EObject> _parseHelper;
  
  public void fireproof(final String input) throws Exception {
    boolean _put = this.alreadyBaked.put(input);
    boolean _not = (!_put);
    if (_not) {
      return;
    }
    try {
      final EObject file = this._parseHelper.parse(input);
      final IResolvedTypes resolvedTypes = this.typeResolver.resolveTypes(file);
      Assert.assertNotNull(resolvedTypes);
      boolean _notEquals = (!Objects.equal(file, null));
      if (_notEquals) {
        TreeIterator<EObject> _eAllContents = file.eAllContents();
        Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(_eAllContents);
        for (final EObject content : _iterable) {
          boolean _matched = false;
          if (!_matched) {
            if (content instanceof XSwitchExpression) {
              final XSwitchExpression _xSwitchExpression = (XSwitchExpression)content;
              _matched=true;
              this.assertExpressionTypeIsResolved(_xSwitchExpression, resolvedTypes);
              String _localVarName = _xSwitchExpression.getLocalVarName();
              boolean _notEquals_1 = (!Objects.equal(_localVarName, null));
              if (_notEquals_1) {
                this.assertIdentifiableTypeIsResolved(_xSwitchExpression, resolvedTypes);
              }
            }
          }
          if (!_matched) {
            if (content instanceof XAbstractFeatureCall) {
              final XAbstractFeatureCall _xAbstractFeatureCall = (XAbstractFeatureCall)content;
              _matched=true;
              this.assertExpressionTypeIsResolved(_xAbstractFeatureCall, resolvedTypes);
              XExpression _implicitReceiver = _xAbstractFeatureCall.getImplicitReceiver();
              boolean _notEquals_1 = (!Objects.equal(_implicitReceiver, null));
              if (_notEquals_1) {
                XExpression _implicitReceiver_1 = _xAbstractFeatureCall.getImplicitReceiver();
                this.assertExpressionTypeIsResolved(_implicitReceiver_1, resolvedTypes);
              }
              XExpression _implicitFirstArgument = _xAbstractFeatureCall.getImplicitFirstArgument();
              boolean _notEquals_2 = (!Objects.equal(_implicitFirstArgument, null));
              if (_notEquals_2) {
                XExpression _implicitFirstArgument_1 = _xAbstractFeatureCall.getImplicitFirstArgument();
                this.assertExpressionTypeIsResolved(_implicitFirstArgument_1, resolvedTypes);
              }
            }
          }
          if (!_matched) {
            if (content instanceof XClosure) {
              final XClosure _xClosure = (XClosure)content;
              _matched=true;
              this.assertExpressionTypeIsResolved(_xClosure, resolvedTypes);
              JvmFormalParameter _implicitParameter = _xClosure.getImplicitParameter();
              boolean _notEquals_1 = (!Objects.equal(_implicitParameter, null));
              if (_notEquals_1) {
                JvmFormalParameter _implicitParameter_1 = _xClosure.getImplicitParameter();
                this.assertIdentifiableTypeIsResolved(_implicitParameter_1, resolvedTypes);
              }
            }
          }
          if (!_matched) {
            if (content instanceof XExpression) {
              final XExpression _xExpression = (XExpression)content;
              _matched=true;
              this.assertExpressionTypeIsResolved(_xExpression, resolvedTypes);
            }
          }
          if (!_matched) {
            if (content instanceof JvmIdentifiableElement) {
              final JvmIdentifiableElement _jvmIdentifiableElement = (JvmIdentifiableElement)content;
              _matched=true;
              this.assertIdentifiableTypeIsResolved(_jvmIdentifiableElement, resolvedTypes);
            }
          }
        }
      }
    } catch (final Throwable _t) {
      if (_t instanceof Throwable) {
        final Throwable t = (Throwable)_t;
        t.printStackTrace();
        InputOutput.<String>println(input);
        String _plus = ("Expression was: \'" + input);
        String _plus_1 = (_plus + "\"");
        RuntimeException _runtimeException = new RuntimeException(_plus_1, t);
        throw _runtimeException;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  public void assertExpressionTypeIsResolved(final XExpression expression, final IResolvedTypes types) {
    try {
      final Object internalTypes = this._reflectExtensions.invoke(types, "delegate");
      TypeData _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (internalTypes instanceof CompoundReentrantTypeResolver) {
          final CompoundReentrantTypeResolver _compoundReentrantTypeResolver = (CompoundReentrantTypeResolver)internalTypes;
          _matched=true;
          TypeData _xblockexpression = null;
          {
            final Object delegate = this._reflectExtensions.invoke(_compoundReentrantTypeResolver, "getDelegate", expression);
            TypeData _xifexpression = null;
            if ((delegate instanceof RootResolvedTypes)) {
              Object _invoke = this._reflectExtensions.invoke(delegate, "getTypeData", expression, Boolean.FALSE);
              _xifexpression = ((TypeData) _invoke);
            }
            _xblockexpression = (_xifexpression);
          }
          _switchResult = _xblockexpression;
        }
      }
      if (!_matched) {
        Object _invoke = this._reflectExtensions.invoke(internalTypes, "getTypeData", expression, Boolean.FALSE);
        _switchResult = ((TypeData) _invoke);
      }
      final TypeData type = _switchResult;
      String _string = expression.toString();
      String _plus = ("Type is not resolved. Expression: " + _string);
      Assert.assertNotNull(_plus, type);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void assertIdentifiableTypeIsResolved(final JvmIdentifiableElement identifiable, final IResolvedTypes types) {
    String _simpleName = identifiable.getSimpleName();
    boolean _equals = Objects.equal(_simpleName, null);
    if (_equals) {
      return;
    }
    final LightweightTypeReference type = types.getActualType(identifiable);
    String _string = identifiable.toString();
    Assert.assertNotNull(_string, type);
    String _string_1 = identifiable.toString();
    String _plus = (_string_1 + " / ");
    String _plus_1 = (_plus + type);
    String _identifier = type.getIdentifier();
    Assert.assertNotNull(_plus_1, _identifier);
  }
}
