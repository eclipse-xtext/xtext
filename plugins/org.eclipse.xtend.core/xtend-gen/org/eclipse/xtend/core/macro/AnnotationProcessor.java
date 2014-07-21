/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.xtend.core.macro.ActiveAnnotationContext;
import org.eclipse.xtend.core.macro.RegisterGlobalsContextImpl;
import org.eclipse.xtend.core.macro.TransformationContextImpl;
import org.eclipse.xtend.core.macro.ValidationContextImpl;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.xtend.XtendAnnotationTarget;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendParameter;
import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.ValidationParticipant;
import org.eclipse.xtend.lib.macro.declaration.Declaration;
import org.eclipse.xtend.lib.macro.declaration.Element;
import org.eclipse.xtend.lib.macro.declaration.MemberDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableNamedElement;
import org.eclipse.xtend.lib.macro.declaration.NamedElement;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.internal.Stopwatches;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;

/**
 * It checks whether the files contain macro annotations and calls their register and processing functions.
 * 
 * @author Sven Efftinge
 */
@SuppressWarnings("all")
public class AnnotationProcessor {
  @Inject
  private Provider<TransformationContextImpl> modifyContextProvider;
  
  @Inject
  private Provider<RegisterGlobalsContextImpl> registerGlobalsContextProvider;
  
  @Inject
  private Provider<ValidationContextImpl> validationContextProvider;
  
  /**
   * gets called from Xtend compiler, during "model inference", i.e. translation of Xtend AST to Java AST
   */
  public Object indexingPhase(final ActiveAnnotationContext ctx, final IJvmDeclaredTypeAcceptor acceptor, final CancelIndicator monitor) {
    Object _xblockexpression = null;
    {
      final Stopwatches.StoppedTask task = Stopwatches.forTask("[macros] indexingPhase (AnnotationProcessor.indexingPhase)");
      task.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        Object _processorInstance = ctx.getProcessorInstance();
        final Object processor = _processorInstance;
        boolean _matched = false;
        if (!_matched) {
          if (processor instanceof RegisterGlobalsParticipant) {
            _matched=true;
            Object _xblockexpression_1 = null;
            {
              final RegisterGlobalsContextImpl registerGlobalsCtx = this.registerGlobalsContextProvider.get();
              registerGlobalsCtx.setAcceptor(acceptor);
              CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
              registerGlobalsCtx.setCompilationUnit(_compilationUnit);
              final Runnable _function = new Runnable() {
                public void run() {
                  List<XtendAnnotationTarget> _annotatedSourceElements = ctx.getAnnotatedSourceElements();
                  final Function1<XtendAnnotationTarget, MemberDeclaration> _function = new Function1<XtendAnnotationTarget, MemberDeclaration>() {
                    public MemberDeclaration apply(final XtendAnnotationTarget it) {
                      CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                      return _compilationUnit.toXtendMemberDeclaration(((XtendMember) it));
                    }
                  };
                  List<MemberDeclaration> _map = ListExtensions.<XtendAnnotationTarget, MemberDeclaration>map(_annotatedSourceElements, _function);
                  ((RegisterGlobalsParticipant<NamedElement>)processor).doRegisterGlobals(_map, registerGlobalsCtx);
                }
              };
              _xblockexpression_1 = this.runWithCancelIndiciator(ctx, monitor, _function);
            }
            _switchResult = _xblockexpression_1;
          }
        }
        _xtrycatchfinallyexpression = _switchResult;
      } finally {
        task.stop();
      }
      _xblockexpression = _xtrycatchfinallyexpression;
    }
    return _xblockexpression;
  }
  
  public Object inferencePhase(final ActiveAnnotationContext ctx, final CancelIndicator monitor) {
    Object _xblockexpression = null;
    {
      final Stopwatches.StoppedTask task = Stopwatches.forTask("[macros] inferencePhase (AnnotationProcessor.inferencePhase)");
      task.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        Object _processorInstance = ctx.getProcessorInstance();
        final Object processor = _processorInstance;
        boolean _matched = false;
        if (!_matched) {
          if (processor instanceof TransformationParticipant) {
            _matched=true;
            Object _xblockexpression_1 = null;
            {
              final TransformationContextImpl modifyCtx = this.modifyContextProvider.get();
              CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
              modifyCtx.setUnit(_compilationUnit);
              final Runnable _function = new Runnable() {
                public void run() {
                  List<XtendAnnotationTarget> _annotatedSourceElements = ctx.getAnnotatedSourceElements();
                  final Function1<XtendAnnotationTarget, MutableNamedElement> _function = new Function1<XtendAnnotationTarget, MutableNamedElement>() {
                    public MutableNamedElement apply(final XtendAnnotationTarget it) {
                      Declaration _switchResult = null;
                      boolean _matched = false;
                      if (!_matched) {
                        if (it instanceof XtendMember) {
                          _matched=true;
                          CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                          _switchResult = _compilationUnit.toXtendMemberDeclaration(((XtendMember)it));
                        }
                      }
                      if (!_matched) {
                        if (it instanceof XtendParameter) {
                          _matched=true;
                          CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                          _switchResult = _compilationUnit.toXtendParameterDeclaration(((XtendParameter)it));
                        }
                      }
                      final Declaration xtendMember = _switchResult;
                      Element _primaryGeneratedJavaElement = modifyCtx.getPrimaryGeneratedJavaElement(xtendMember);
                      return ((MutableNamedElement) _primaryGeneratedJavaElement);
                    }
                  };
                  final List<MutableNamedElement> map = ListExtensions.<XtendAnnotationTarget, MutableNamedElement>map(_annotatedSourceElements, _function);
                  ((TransformationParticipant<MutableNamedElement>)processor).doTransform(map, modifyCtx);
                }
              };
              _xblockexpression_1 = this.runWithCancelIndiciator(ctx, monitor, _function);
            }
            _switchResult = _xblockexpression_1;
          }
        }
        _xtrycatchfinallyexpression = _switchResult;
      } finally {
        task.stop();
      }
      _xblockexpression = _xtrycatchfinallyexpression;
    }
    return _xblockexpression;
  }
  
  public Object validationPhase(final ActiveAnnotationContext ctx, final CancelIndicator monitor) {
    Object _xblockexpression = null;
    {
      final Stopwatches.StoppedTask task = Stopwatches.forTask("[macros] validationPhase (AnnotationProcessor.validationPhase)");
      task.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        Object _processorInstance = ctx.getProcessorInstance();
        final Object processor = _processorInstance;
        boolean _matched = false;
        if (!_matched) {
          if (processor instanceof ValidationParticipant) {
            _matched=true;
            Object _xblockexpression_1 = null;
            {
              final ValidationContextImpl validationContext = this.validationContextProvider.get();
              CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
              validationContext.setUnit(_compilationUnit);
              final Runnable _function = new Runnable() {
                public void run() {
                  List<XtendAnnotationTarget> _annotatedSourceElements = ctx.getAnnotatedSourceElements();
                  final Function1<XtendAnnotationTarget, NamedElement> _function = new Function1<XtendAnnotationTarget, NamedElement>() {
                    public NamedElement apply(final XtendAnnotationTarget it) {
                      Declaration _switchResult = null;
                      boolean _matched = false;
                      if (!_matched) {
                        if (it instanceof XtendMember) {
                          _matched=true;
                          CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                          _switchResult = _compilationUnit.toXtendMemberDeclaration(((XtendMember)it));
                        }
                      }
                      if (!_matched) {
                        if (it instanceof XtendParameter) {
                          _matched=true;
                          CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                          _switchResult = _compilationUnit.toXtendParameterDeclaration(((XtendParameter)it));
                        }
                      }
                      final Declaration xtendMember = _switchResult;
                      Element _primaryGeneratedJavaElement = validationContext.getPrimaryGeneratedJavaElement(xtendMember);
                      return ((NamedElement) _primaryGeneratedJavaElement);
                    }
                  };
                  final List<NamedElement> map = ListExtensions.<XtendAnnotationTarget, NamedElement>map(_annotatedSourceElements, _function);
                  ((ValidationParticipant<NamedElement>)processor).doValidate(map, validationContext);
                }
              };
              _xblockexpression_1 = this.runWithCancelIndiciator(ctx, monitor, _function);
            }
            _switchResult = _xblockexpression_1;
          }
        }
        _xtrycatchfinallyexpression = _switchResult;
      } finally {
        task.stop();
      }
      _xblockexpression = _xtrycatchfinallyexpression;
    }
    return _xblockexpression;
  }
  
  /**
   * runs the given runnable and another thread in parallel, that sets the timeout property on the compilation unit to true
   * when the given amount of milliseconds have passed by.
   */
  private Object runWithCancelIndiciator(final ActiveAnnotationContext ctx, final CancelIndicator cancelIndicator, final Runnable runnable) {
    Object _xblockexpression = null;
    {
      final AtomicBoolean isFinished = new AtomicBoolean(false);
      final Runnable _function = new Runnable() {
        public void run() {
          try {
            while ((!isFinished.get())) {
              {
                boolean _isCanceled = cancelIndicator.isCanceled();
                if (_isCanceled) {
                  CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                  _compilationUnit.setCanceled(true);
                  return;
                }
                Thread.sleep(100);
              }
            }
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      Thread _thread = new Thread(_function);
      _thread.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        runnable.run();
      } catch (final Throwable _t) {
        if (_t instanceof CancellationException) {
          final CancellationException e = (CancellationException)_t;
          _xtrycatchfinallyexpression = null;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      } finally {
        isFinished.set(true);
      }
      _xblockexpression = _xtrycatchfinallyexpression;
    }
    return _xblockexpression;
  }
}
