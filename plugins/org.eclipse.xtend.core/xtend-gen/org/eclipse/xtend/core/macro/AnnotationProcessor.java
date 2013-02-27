/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Provider;
import org.eclipse.xtend.core.macro.ActiveAnnotationContext;
import org.eclipse.xtend.core.macro.TransformationContextImpl;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.macro.declaration.XtendMemberDeclarationImpl;
import org.eclipse.xtend.core.xtend.XtendAnnotationTarget;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.lib.macro.RegisterGlobalsParticipant;
import org.eclipse.xtend.lib.macro.TransformationParticipant;
import org.eclipse.xtend.lib.macro.declaration.MutableNamedElement;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.internal.Stopwatches;
import org.eclipse.xtext.util.internal.Stopwatches.StoppedTask;
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
  
  /**
   * gets called from Xtend compiler, during "model inference", i.e. translation of Xtend AST to Java AST
   */
  public Object indexingPhase(final ActiveAnnotationContext ctx, final IAcceptor<JvmDeclaredType> acceptor, final CancelIndicator monitor) {
    Object _xblockexpression = null;
    {
      final StoppedTask task = Stopwatches.forTask("[macros] indexingPhase (AnnotationProcessor.indexingPhase)");
      task.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        Object _processorInstance = ctx.getProcessorInstance();
        final Object processor = _processorInstance;
        boolean _matched = false;
        if (!_matched) {
          if (processor instanceof RegisterGlobalsParticipant) {
            final RegisterGlobalsParticipant<?> _registerGlobalsParticipant = (RegisterGlobalsParticipant<?>)processor;
            _matched=true;
            _switchResult = null;
          }
        }
        _xtrycatchfinallyexpression = _switchResult;
      } finally {
        task.stop();
      }
      _xblockexpression = (_xtrycatchfinallyexpression);
    }
    return _xblockexpression;
  }
  
  public Object inferencePhase(final ActiveAnnotationContext ctx, final CancelIndicator monitor) {
    Object _xblockexpression = null;
    {
      final StoppedTask task = Stopwatches.forTask("[macros] inferencePhase (AnnotationProcessor.inferencePhase)");
      task.start();
      Object _xtrycatchfinallyexpression = null;
      try {
        Object _switchResult = null;
        Object _processorInstance = ctx.getProcessorInstance();
        final Object processor = _processorInstance;
        boolean _matched = false;
        if (!_matched) {
          if (processor instanceof TransformationParticipant) {
            final TransformationParticipant<MutableNamedElement> _transformationParticipant = (TransformationParticipant<MutableNamedElement>)processor;
            _matched=true;
            Object _xblockexpression_1 = null;
            {
              final TransformationContextImpl modifyCtx = this.modifyContextProvider.get();
              CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
              modifyCtx.setUnit(_compilationUnit);
              final Runnable _function = new Runnable() {
                  public void run() {
                    List<XtendAnnotationTarget> _annotatedSourceElements = ctx.getAnnotatedSourceElements();
                    final Function1<XtendAnnotationTarget,MutableNamedElement> _function = new Function1<XtendAnnotationTarget,MutableNamedElement>() {
                        public MutableNamedElement apply(final XtendAnnotationTarget it) {
                          CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                          final XtendMemberDeclarationImpl<? extends XtendMember> xtendMember = _compilationUnit.toXtendMemberDeclaration(((XtendMember) it));
                          return modifyCtx.getPrimaryGeneratedJavaElement(xtendMember);
                        }
                      };
                    final List<MutableNamedElement> map = ListExtensions.<XtendAnnotationTarget, MutableNamedElement>map(_annotatedSourceElements, _function);
                    _transformationParticipant.doTransform(map, modifyCtx);
                  }
                };
              Object _runWithCancelIndiciator = this.runWithCancelIndiciator(ctx, monitor, _function);
              _xblockexpression_1 = (_runWithCancelIndiciator);
            }
            _switchResult = _xblockexpression_1;
          }
        }
        _xtrycatchfinallyexpression = _switchResult;
      } finally {
        task.stop();
      }
      _xblockexpression = (_xtrycatchfinallyexpression);
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
      AtomicBoolean _atomicBoolean = new AtomicBoolean(false);
      final AtomicBoolean isFinished = _atomicBoolean;
      final Runnable _function = new Runnable() {
          public void run() {
            try {
              boolean _get = isFinished.get();
              boolean _not = (!_get);
              boolean _while = _not;
              while (_while) {
                {
                  boolean _isCanceled = cancelIndicator.isCanceled();
                  if (_isCanceled) {
                    CompilationUnitImpl _compilationUnit = ctx.getCompilationUnit();
                    _compilationUnit.setCanceled(true);
                    return;
                  }
                  Thread.sleep(100);
                }
                boolean _get_1 = isFinished.get();
                boolean _not_1 = (!_get_1);
                _while = _not_1;
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
      _xblockexpression = (_xtrycatchfinallyexpression);
    }
    return _xblockexpression;
  }
}
