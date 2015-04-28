/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.idea.resource;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Delegate;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class ResourceDescriptionAdapter extends AdapterImpl implements IResourceDescription {
  @Delegate
  private final IResourceDescription delegate;
  
  public static ResourceDescriptionAdapter install(final Resource it) {
    ResourceDescriptionAdapter _xblockexpression = null;
    {
      ResourceDescriptionAdapter.remove(it);
      ResourceDescriptionAdapter _xifexpression = null;
      if ((it instanceof XtextResource)) {
        ResourceDescriptionAdapter _xblockexpression_1 = null;
        {
          IResourceServiceProvider _resourceServiceProvider = ((XtextResource)it).getResourceServiceProvider();
          @Extension
          final IResourceDescription.Manager resourceDescriptionManager = _resourceServiceProvider.getResourceDescriptionManager();
          IResourceDescription _resourceDescription = resourceDescriptionManager.getResourceDescription(it);
          ResourceDescriptionAdapter _resourceDescriptionAdapter = new ResourceDescriptionAdapter(_resourceDescription);
          final Procedure1<ResourceDescriptionAdapter> _function = new Procedure1<ResourceDescriptionAdapter>() {
            @Override
            public void apply(final ResourceDescriptionAdapter adapter) {
              EList<Adapter> _eAdapters = ((XtextResource)it).eAdapters();
              _eAdapters.add(adapter);
            }
          };
          _xblockexpression_1 = ObjectExtensions.<ResourceDescriptionAdapter>operator_doubleArrow(_resourceDescriptionAdapter, _function);
        }
        _xifexpression = _xblockexpression_1;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public static boolean remove(final Resource it) {
    boolean _xblockexpression = false;
    {
      final ResourceDescriptionAdapter adapter = ResourceDescriptionAdapter.get(it);
      boolean _xifexpression = false;
      boolean _notEquals = (!Objects.equal(adapter, null));
      if (_notEquals) {
        EList<Adapter> _eAdapters = it.eAdapters();
        _xifexpression = _eAdapters.remove(adapter);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  public static ResourceDescriptionAdapter get(final Resource it) {
    EList<Adapter> _eAdapters = it.eAdapters();
    Iterable<ResourceDescriptionAdapter> _filter = Iterables.<ResourceDescriptionAdapter>filter(_eAdapters, ResourceDescriptionAdapter.class);
    return IterableExtensions.<ResourceDescriptionAdapter>head(_filter);
  }
  
  @Override
  public String toString() {
    Class<? extends ResourceDescriptionAdapter> _class = this.getClass();
    String _name = _class.getName();
    String _plus = (_name + ":");
    String _string = this.delegate.toString();
    return (_plus + _string);
  }
  
  public ResourceDescriptionAdapter(final IResourceDescription delegate) {
    super();
    this.delegate = delegate;
  }
  
  public Iterable<IEObjectDescription> getExportedObjects() {
    return this.delegate.getExportedObjects();
  }
  
  public Iterable<QualifiedName> getImportedNames() {
    return this.delegate.getImportedNames();
  }
  
  public Iterable<IReferenceDescription> getReferenceDescriptions() {
    return this.delegate.getReferenceDescriptions();
  }
  
  public URI getURI() {
    return this.delegate.getURI();
  }
  
  public Iterable<IEObjectDescription> getExportedObjects(final EClass type, final QualifiedName name, final boolean ignoreCase) {
    return this.delegate.getExportedObjects(type, name, ignoreCase);
  }
  
  public Iterable<IEObjectDescription> getExportedObjectsByObject(final EObject object) {
    return this.delegate.getExportedObjectsByObject(object);
  }
  
  public Iterable<IEObjectDescription> getExportedObjectsByType(final EClass type) {
    return this.delegate.getExportedObjectsByType(type);
  }
  
  public boolean isEmpty() {
    return this.delegate.isEmpty();
  }
}
