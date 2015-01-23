/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.idea.resource;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.idea.resource.IResourceSetProvider;
import org.eclipse.xtext.idea.resource.ProjectAdapter;
import org.eclipse.xtext.resource.XtextResourceSet;

@Singleton
@SuppressWarnings("all")
public class ProjectBasedResourceSetProvider implements IResourceSetProvider {
  @Inject
  private Provider<XtextResourceSet> resourceSetProvider;
  
  @Override
  public ResourceSet get(final Object context) {
    XtextResourceSet _xblockexpression = null;
    {
      final XtextResourceSet resourceSet = this.resourceSetProvider.get();
      if ((context instanceof Project)) {
        EList<Adapter> _eAdapters = resourceSet.eAdapters();
        ProjectAdapter _projectAdapter = new ProjectAdapter(((Project)context));
        _eAdapters.add(_projectAdapter);
        GlobalSearchScope _allScope = GlobalSearchScope.allScope(((Project)context));
        resourceSet.setClasspathURIContext(_allScope);
      }
      if ((context instanceof PsiFile)) {
        EList<Adapter> _eAdapters_1 = resourceSet.eAdapters();
        Project _project = ((PsiFile)context).getProject();
        ProjectAdapter _projectAdapter_1 = new ProjectAdapter(_project);
        _eAdapters_1.add(_projectAdapter_1);
        GlobalSearchScope _resolveScope = ((PsiFile)context).getResolveScope();
        resourceSet.setClasspathURIContext(_resolveScope);
      }
      _xblockexpression = resourceSet;
    }
    return _xblockexpression;
  }
}
