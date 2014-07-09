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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.jvmmodel.IXtendJvmAssociations;
import org.eclipse.xtend.core.macro.declaration.AbstractElementImpl;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.ProblemImpl;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendFunction;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Element;
import org.eclipse.xtend.lib.macro.services.Problem;
import org.eclipse.xtend.lib.macro.services.ProblemSupport;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.EObjectDiagnosticImpl;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotation;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;

@SuppressWarnings("all")
public class ProblemSupportImpl implements ProblemSupport {
  private CompilationUnitImpl compilationUnit;
  
  public ProblemSupportImpl(final CompilationUnitImpl compilationUnit) {
    this.compilationUnit = compilationUnit;
  }
  
  private void checkCanceled() {
    this.compilationUnit.checkCanceled();
  }
  
  private void checkValidationAllowed() {
    boolean _isValidationAllowed = this.compilationUnit.isValidationAllowed();
    boolean _not = (!_isValidationAllowed);
    if (_not) {
      throw new IllegalStateException("Adding issues is not allowed after the validation phase");
    }
  }
  
  public void addError(final Element element, final String message) {
    this.checkCanceled();
    this.checkValidationAllowed();
    final Pair<Resource, EObject> resAndObj = this.getResourceAndEObject(element);
    Resource _key = resAndObj.getKey();
    EList<Resource.Diagnostic> _errors = _key.getErrors();
    EObject _value = resAndObj.getValue();
    EObject _value_1 = resAndObj.getValue();
    EStructuralFeature _significantFeature = this.getSignificantFeature(_value_1);
    EObjectDiagnosticImpl _eObjectDiagnosticImpl = new EObjectDiagnosticImpl(Severity.ERROR, "user.issue", message, _value, _significantFeature, (-1), null);
    _errors.add(_eObjectDiagnosticImpl);
  }
  
  public void addWarning(final Element element, final String message) {
    this.checkCanceled();
    this.checkValidationAllowed();
    final Pair<Resource, EObject> resAndObj = this.getResourceAndEObject(element);
    Resource _key = resAndObj.getKey();
    EList<Resource.Diagnostic> _warnings = _key.getWarnings();
    EObject _value = resAndObj.getValue();
    EObject _value_1 = resAndObj.getValue();
    EStructuralFeature _significantFeature = this.getSignificantFeature(_value_1);
    EObjectDiagnosticImpl _eObjectDiagnosticImpl = new EObjectDiagnosticImpl(Severity.WARNING, "user.issue", message, _value, _significantFeature, (-1), null);
    _warnings.add(_eObjectDiagnosticImpl);
  }
  
  public List<? extends Problem> getProblems(final Element element) {
    this.checkCanceled();
    final Pair<Resource, EObject> resAndObj = this.getResourceAndEObject(element);
    final Resource resource = resAndObj.getKey();
    EList<Resource.Diagnostic> _errors = resource.getErrors();
    EList<Resource.Diagnostic> _warnings = resource.getWarnings();
    Iterable<Resource.Diagnostic> _plus = Iterables.<Resource.Diagnostic>concat(_errors, _warnings);
    final Iterable<EObjectDiagnosticImpl> issues = Iterables.<EObjectDiagnosticImpl>filter(_plus, EObjectDiagnosticImpl.class);
    final Function1<EObjectDiagnosticImpl, Boolean> _function = new Function1<EObjectDiagnosticImpl, Boolean>() {
      public Boolean apply(final EObjectDiagnosticImpl diag) {
        EObject _problematicObject = diag.getProblematicObject();
        EObject _value = resAndObj.getValue();
        return Boolean.valueOf(Objects.equal(_problematicObject, _value));
      }
    };
    Iterable<EObjectDiagnosticImpl> _filter = IterableExtensions.<EObjectDiagnosticImpl>filter(issues, _function);
    final Function1<EObjectDiagnosticImpl, Problem> _function_1 = new Function1<EObjectDiagnosticImpl, Problem>() {
      public Problem apply(final EObjectDiagnosticImpl diag) {
        String _code = diag.getCode();
        String _message = diag.getMessage();
        Severity _severity = diag.getSeverity();
        Problem.Severity _translateSeverity = ProblemSupportImpl.this.translateSeverity(_severity);
        ProblemImpl _problemImpl = new ProblemImpl(_code, _message, _translateSeverity);
        return ((Problem) _problemImpl);
      }
    };
    final Iterable<Problem> result = IterableExtensions.<EObjectDiagnosticImpl, Problem>map(_filter, _function_1);
    return IterableExtensions.<Problem>toList(result);
  }
  
  public EStructuralFeature getSignificantFeature(final EObject obj) {
    EStructuralFeature _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (obj instanceof XtendTypeDeclaration) {
        _matched=true;
        _switchResult = XtendPackage.eINSTANCE.getXtendTypeDeclaration_Name();
      }
    }
    if (!_matched) {
      if (obj instanceof XtendField) {
        _matched=true;
        _switchResult = XtendPackage.eINSTANCE.getXtendField_Name();
      }
    }
    if (!_matched) {
      if (obj instanceof XtendFunction) {
        _matched=true;
        _switchResult = XtendPackage.eINSTANCE.getXtendFunction_Name();
      }
    }
    if (!_matched) {
      if (obj instanceof JvmFormalParameter) {
        _matched=true;
        _switchResult = TypesPackage.eINSTANCE.getJvmFormalParameter_Name();
      }
    }
    if (!_matched) {
      if (obj instanceof XAnnotation) {
        _matched=true;
        _switchResult = XAnnotationsPackage.eINSTANCE.getXAnnotation_AnnotationType();
      }
    }
    return _switchResult;
  }
  
  private Pair<Resource, EObject> getResourceAndEObject(final Element element) {
    this.checkCanceled();
    boolean _matched = false;
    if (!_matched) {
      if (element instanceof AbstractElementImpl) {
        _matched=true;
        EObject _delegate = ((AbstractElementImpl<? extends EObject>)element).getDelegate();
        final Resource resource = _delegate.eResource();
        XtendFile _xtendFile = this.compilationUnit.getXtendFile();
        Resource _eResource = _xtendFile.eResource();
        boolean _equals = Objects.equal(resource, _eResource);
        if (_equals) {
          IXtendJvmAssociations _jvmAssociations = this.compilationUnit.getJvmAssociations();
          EObject _delegate_1 = ((AbstractElementImpl<? extends EObject>)element).getDelegate();
          final EObject eobject = _jvmAssociations.getPrimarySourceElement(_delegate_1);
          boolean _equals_1 = Objects.equal(eobject, null);
          if (_equals_1) {
            EObject _delegate_2 = ((AbstractElementImpl<? extends EObject>)element).getDelegate();
            return Pair.<Resource, EObject>of(resource, _delegate_2);
          }
          return Pair.<Resource, EObject>of(resource, eobject);
        }
      }
    }
    throw new IllegalArgumentException("You can only add issues on locally declared elements.");
  }
  
  private Problem.Severity translateSeverity(final Severity severity) {
    Problem.Severity _switchResult = null;
    if (severity != null) {
      switch (severity) {
        case ERROR:
          _switchResult = Problem.Severity.ERROR;
          break;
        case WARNING:
          _switchResult = Problem.Severity.WARNING;
          break;
        case INFO:
          _switchResult = Problem.Severity.INFO;
          break;
        case IGNORE:
          _switchResult = Problem.Severity.IGNORE;
          break;
        default:
          break;
      }
    }
    return _switchResult;
  }
}
