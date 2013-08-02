/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.macro;

import com.google.common.base.Objects;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

@Singleton
@SuppressWarnings("all")
public class ProcessorInstanceForJvmTypeProvider {
  private final static Logger logger = new Function0<Logger>() {
    public Logger apply() {
      Logger _logger = Logger.getLogger(ProcessorInstanceForJvmTypeProvider.class);
      return _logger;
    }
  }.apply();
  
  private ClassLoader classLoader;
  
  public void setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
  
  /**
   * @return an instance of the given JvmType
   */
  public Object getProcessorInstance(final JvmType type) {
    try {
      ClassLoader _classLoader = this.getClassLoader(type);
      Class<? extends Object> _loadClass = null;
      if (_classLoader!=null) {
        String _identifier = type.getIdentifier();
        _loadClass=_classLoader.loadClass(_identifier);
      }
      final Class<? extends Object> loadClass = _loadClass;
      try {
        Object _newInstance = null;
        if (loadClass!=null) {
          _newInstance=loadClass.newInstance();
        }
        return _newInstance;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        String _string = e.toString();
        ProcessorInstanceForJvmTypeProvider.logger.error(_string);
        return null;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  protected ClassLoader getClassLoader(final EObject ctx) {
    boolean _notEquals = (!Objects.equal(this.classLoader, null));
    if (_notEquals) {
      return this.classLoader;
    }
    Resource _eResource = ctx.eResource();
    final ResourceSet resourceSet = _eResource.getResourceSet();
    boolean _matched = false;
    if (!_matched) {
      if (resourceSet instanceof XtextResourceSet) {
        final XtextResourceSet _xtextResourceSet = (XtextResourceSet)resourceSet;
        _matched=true;
        final Object classLoaderCtx = _xtextResourceSet.getClasspathURIContext();
        boolean _matched_1 = false;
        if (!_matched_1) {
          if (classLoaderCtx instanceof ClassLoader) {
            final ClassLoader _classLoader = (ClassLoader)classLoaderCtx;
            _matched_1=true;
            return _classLoader;
          }
        }
        if (!_matched_1) {
          if (classLoaderCtx instanceof Class) {
            final Class<?> _class = (Class<?>)classLoaderCtx;
            _matched_1=true;
            return _class.getClassLoader();
          }
        }
      }
    }
    ProcessorInstanceForJvmTypeProvider.logger.info("No class loader configured. Trying with this class\' classloader.");
    Class<? extends ProcessorInstanceForJvmTypeProvider> _class = this.getClass();
    return _class.getClassLoader();
  }
}
