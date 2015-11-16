/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.arithmetics.scoping;

import com.google.common.collect.Iterables;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.example.arithmetics.arithmetics.ArithmeticsPackage;
import org.eclipse.xtext.example.arithmetics.arithmetics.DeclaredParameter;
import org.eclipse.xtext.example.arithmetics.arithmetics.Definition;
import org.eclipse.xtext.example.arithmetics.arithmetics.Expression;
import org.eclipse.xtext.example.arithmetics.arithmetics.FunctionCall;
import org.eclipse.xtext.example.arithmetics.arithmetics.Import;
import org.eclipse.xtext.example.arithmetics.arithmetics.Module;
import org.eclipse.xtext.example.arithmetics.arithmetics.Statement;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.Scopes;
import org.eclipse.xtext.scoping.impl.AbstractGlobalScopeDelegatingScopeProvider;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

/**
 * This class contains custom scoping description.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
@SuppressWarnings("all")
public class ArithmeticsScopeProvider extends AbstractGlobalScopeDelegatingScopeProvider {
  @Override
  public IScope getScope(final EObject context, final EReference reference) {
    if ((reference == ArithmeticsPackage.Literals.IMPORT__MODULE)) {
      Resource _eResource = context.eResource();
      return super.getGlobalScope(_eResource, reference);
    }
    final Module module = EcoreUtil2.<Module>getContainerOfType(context, Module.class);
    IScope result = IScope.NULLSCOPE;
    EList<Import> _imports = module.getImports();
    for (final Import import_ : _imports) {
      Module _module = import_.getModule();
      boolean _eIsProxy = _module.eIsProxy();
      boolean _not = (!_eIsProxy);
      if (_not) {
        Module _module_1 = import_.getModule();
        IScope _moduleScope = this.getModuleScope(context, reference, _module_1, result);
        result = _moduleScope;
      }
    }
    IScope _moduleScope_1 = this.getModuleScope(context, reference, module, result);
    result = _moduleScope_1;
    return this.getDefinitionScope(context, reference, result);
  }
  
  public IScope getModuleScope(final EObject context, final EReference reference, final Module module, final IScope parent) {
    EList<Statement> _statements = module.getStatements();
    final Iterable<Definition> allDefinitions = Iterables.<Definition>filter(_statements, Definition.class);
    if ((context instanceof FunctionCall)) {
      final Function1<Definition, Boolean> _function = new Function1<Definition, Boolean>() {
        @Override
        public Boolean apply(final Definition it) {
          EList<Expression> _args = ((FunctionCall)context).getArgs();
          int _size = _args.size();
          EList<DeclaredParameter> _args_1 = it.getArgs();
          int _size_1 = _args_1.size();
          return Boolean.valueOf((_size == _size_1));
        }
      };
      Iterable<Definition> _filter = IterableExtensions.<Definition>filter(allDefinitions, _function);
      return Scopes.scopeFor(_filter, parent);
    } else {
      return Scopes.scopeFor(allDefinitions, parent);
    }
  }
  
  public IScope getDefinitionScope(final EObject context, final EReference reference, final IScope parent) {
    final Definition containingDef = EcoreUtil2.<Definition>getContainerOfType(context, Definition.class);
    if ((containingDef == null)) {
      return parent;
    }
    EList<DeclaredParameter> _args = containingDef.getArgs();
    return Scopes.scopeFor(_args, parent);
  }
}
