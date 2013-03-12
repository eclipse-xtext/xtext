/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.tests.typesystem;

import com.google.common.base.Objects;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.tests.typesystem.AbstractTypeResolverTest;
import org.eclipse.xtext.xbase.typing.ITypeProvider;
import org.eclipse.xtext.xtype.XFunctionTypeRef;
import org.junit.Assert;

/**
 * @author Sebastian Zarnekow
 */
@SuppressWarnings("all")
public abstract class AbstractOldAPITypeResolverTest extends AbstractTypeResolverTest<JvmTypeReference> {
  public JvmTypeReference resolvesTo(final String expression, final String type) {
    try {
      final XExpression xExpression = this.expression(expression, false);
      Resource _eResource = xExpression.eResource();
      EList<Diagnostic> _errors = _eResource.getErrors();
      boolean _isEmpty = _errors.isEmpty();
      Assert.assertTrue(_isEmpty);
      Resource _eResource_1 = xExpression.eResource();
      EList<Diagnostic> _warnings = _eResource_1.getWarnings();
      boolean _isEmpty_1 = _warnings.isEmpty();
      Assert.assertTrue(_isEmpty_1);
      ITypeProvider _typeProvider = this.getTypeProvider();
      final JvmTypeReference resolvedType = _typeProvider.getType(xExpression);
      String _simpleName = resolvedType==null?(String)null:resolvedType.getSimpleName();
      Assert.assertEquals(type, _simpleName);
      TreeIterator<EObject> _eAllContents = xExpression.eAllContents();
      Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(_eAllContents);
      for (final EObject content : _iterable) {
        boolean _matched = false;
        if (!_matched) {
          if (content instanceof XSwitchExpression) {
            final XSwitchExpression _xSwitchExpression = (XSwitchExpression)content;
            _matched=true;
            this.assertExpressionTypeIsResolved(_xSwitchExpression);
            String _localVarName = _xSwitchExpression.getLocalVarName();
            boolean _notEquals = (!Objects.equal(_localVarName, null));
            if (_notEquals) {
              this.assertIdentifiableTypeIsResolved(_xSwitchExpression);
            }
          }
        }
        if (!_matched) {
          if (content instanceof XExpression) {
            final XExpression _xExpression = (XExpression)content;
            _matched=true;
            this.assertExpressionTypeIsResolved(_xExpression);
          }
        }
        if (!_matched) {
          if (content instanceof JvmIdentifiableElement) {
            final JvmIdentifiableElement _jvmIdentifiableElement = (JvmIdentifiableElement)content;
            _matched=true;
            this.assertIdentifiableTypeIsResolved(_jvmIdentifiableElement);
          }
        }
      }
      Resource _eResource_2 = xExpression.eResource();
      Iterable<Diagnostic> _linkingAndSyntaxErrors = this.getLinkingAndSyntaxErrors(_eResource_2);
      String _string = _linkingAndSyntaxErrors.toString();
      Resource _eResource_3 = xExpression.eResource();
      Iterable<Diagnostic> _linkingAndSyntaxErrors_1 = this.getLinkingAndSyntaxErrors(_eResource_3);
      boolean _isEmpty_2 = IterableExtensions.isEmpty(_linkingAndSyntaxErrors_1);
      Assert.assertTrue(_string, _isEmpty_2);
      Resource _eResource_4 = xExpression.eResource();
      EList<Diagnostic> _warnings_1 = _eResource_4.getWarnings();
      boolean _isEmpty_3 = _warnings_1.isEmpty();
      Assert.assertTrue(_isEmpty_3);
      return resolvedType;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void isFunctionAndEquivalentTo(final JvmTypeReference reference, final String type) {
    Assert.assertTrue((reference instanceof XFunctionTypeRef));
    JvmTypeReference _equivalent = ((XFunctionTypeRef) reference).getEquivalent();
    String _simpleName = _equivalent.getSimpleName();
    Assert.assertEquals(type, _simpleName);
  }
  
  public void assertExpressionTypeIsResolved(final XExpression expression) {
    ITypeProvider _typeProvider = this.getTypeProvider();
    final JvmTypeReference type = _typeProvider.getType(expression);
    String _string = expression.toString();
    Assert.assertNotNull(_string, type);
    String _string_1 = expression.toString();
    String _plus = (_string_1 + " / ");
    String _plus_1 = (_plus + type);
    String _identifier = type.getIdentifier();
    Assert.assertNotNull(_plus_1, _identifier);
    ITypeProvider _typeProvider_1 = this.getTypeProvider();
    final JvmTypeReference expectedType = _typeProvider_1.getExpectedType(expression);
    String _string_2 = expression.toString();
    String _valueOf = String.valueOf(expectedType);
    Assert.assertNotNull(_string_2, _valueOf);
  }
  
  public void assertIdentifiableTypeIsResolved(final JvmIdentifiableElement identifiable) {
    ITypeProvider _typeProvider = this.getTypeProvider();
    final JvmTypeReference type = _typeProvider.getTypeForIdentifiable(identifiable);
    String _string = identifiable.toString();
    Assert.assertNotNull(_string, type);
    String _string_1 = identifiable.toString();
    String _plus = (_string_1 + " / ");
    String _plus_1 = (_plus + type);
    String _identifier = type.getIdentifier();
    Assert.assertNotNull(_plus_1, _identifier);
  }
  
  protected abstract ITypeProvider getTypeProvider();
}
