/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.idea.containers;

import com.google.common.base.Objects;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.Collection;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.idea.resource.AbstractScopeBasedSelectable;
import org.eclipse.xtext.idea.resource.impl.PsiFileBasedResourceDescription;
import org.eclipse.xtext.psi.impl.BaseXtextFile;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class ResolveScopeBasedContainer extends AbstractScopeBasedSelectable implements IContainer {
  @Override
  public IResourceDescription getResourceDescription(final URI uri) {
    Object _xblockexpression = null;
    {
      final BaseXtextFile psiFile = this.findFile(uri);
      boolean _notEquals = (!Objects.equal(psiFile, null));
      if (_notEquals) {
        return new PsiFileBasedResourceDescription(psiFile);
      }
      _xblockexpression = null;
    }
    return ((IResourceDescription)_xblockexpression);
  }
  
  @Override
  public int getResourceDescriptionCount() {
    Integer _xblockexpression = null;
    {
      final int[] counter = { 0 };
      final Function1<Collection<VirtualFile>, Boolean> _function = new Function1<Collection<VirtualFile>, Boolean>() {
        @Override
        public Boolean apply(final Collection<VirtualFile> files) {
          boolean _xblockexpression = false;
          {
            int _get = counter[0];
            int _size = files.size();
            int _plus = (_get + _size);
            counter[0] = _plus;
            _xblockexpression = true;
          }
          return Boolean.valueOf(_xblockexpression);
        }
      };
      this.processFiles(_function);
      _xblockexpression = IterableExtensions.<Integer>head(((Iterable<Integer>)Conversions.doWrapArray(counter)));
    }
    return (_xblockexpression).intValue();
  }
  
  @Override
  public boolean hasResourceDescription(final URI uri) {
    BaseXtextFile _findFile = this.findFile(uri);
    return (!Objects.equal(_findFile, null));
  }
  
  @Override
  public void setScope(final GlobalSearchScope resolveScope) {
    super.setScope(resolveScope);
  }
}
