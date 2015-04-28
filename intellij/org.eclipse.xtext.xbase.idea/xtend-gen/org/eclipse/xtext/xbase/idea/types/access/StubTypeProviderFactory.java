/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.idea.types.access;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.intellij.openapi.project.Project;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.access.AbstractTypeProviderFactory;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.common.types.access.impl.TypeResourceServices;
import org.eclipse.xtext.idea.resource.ProjectAdapter;
import org.eclipse.xtext.psi.IPsiModelAssociator;
import org.eclipse.xtext.xbase.idea.types.access.StubJvmTypeProvider;

@SuppressWarnings("all")
public class StubTypeProviderFactory extends AbstractTypeProviderFactory {
  @Inject
  private IPsiModelAssociator psiModelAssociator;
  
  @Override
  public IJvmTypeProvider createTypeProvider(final ResourceSet resourceSet) {
    StubJvmTypeProvider _xblockexpression = null;
    {
      boolean _equals = Objects.equal(resourceSet, null);
      if (_equals) {
        throw new IllegalArgumentException("resourceSet may not be null.");
      }
      final Project project = ProjectAdapter.getProject(resourceSet);
      boolean _equals_1 = Objects.equal(project, null);
      if (_equals_1) {
        throw new IllegalArgumentException("project may not be null.");
      }
      IndexedJvmTypeAccess _indexedJvmTypeAccess = this.getIndexedJvmTypeAccess();
      TypeResourceServices _services = this.getServices();
      _xblockexpression = new StubJvmTypeProvider(project, resourceSet, _indexedJvmTypeAccess, _services, this.psiModelAssociator);
    }
    return _xblockexpression;
  }
}
