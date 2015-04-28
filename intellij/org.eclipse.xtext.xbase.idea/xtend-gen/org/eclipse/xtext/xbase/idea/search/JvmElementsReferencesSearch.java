/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.idea.search;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.intellij.lang.Language;
import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchRequestCollector;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import java.util.Set;
import org.eclipse.xtext.idea.lang.IXtextLanguage;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.xbase.idea.jvmmodel.IPsiJvmModelAssociations;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;

@SuppressWarnings("all")
public class JvmElementsReferencesSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {
  @Inject
  @Extension
  private IPsiJvmModelAssociations _iPsiJvmModelAssociations;
  
  private final IXtextLanguage language;
  
  public JvmElementsReferencesSearch(final IXtextLanguage language) {
    super(true);
    this.language = language;
    this.language.injectMembers(this);
  }
  
  @Override
  public void processQuery(final ReferencesSearch.SearchParameters queryParameters, final Processor<PsiReference> consumer) {
    final PsiElement element = queryParameters.getElementToSearch();
    Language _language = element.getLanguage();
    boolean _notEquals = (!Objects.equal(_language, this.language));
    if (_notEquals) {
      return;
    }
    try {
      Set<PsiElement> _jvmElements = this._iPsiJvmModelAssociations.getJvmElements(element);
      Iterable<PsiNamedElement> _filter = Iterables.<PsiNamedElement>filter(_jvmElements, PsiNamedElement.class);
      for (final PsiNamedElement psiJvmElement : _filter) {
        SearchRequestCollector _optimizer = queryParameters.getOptimizer();
        String _name = psiJvmElement.getName();
        SearchScope _effectiveSearchScope = queryParameters.getEffectiveSearchScope();
        _optimizer.searchWord(_name, _effectiveSearchScope, true, psiJvmElement);
      }
    } catch (final Throwable _t) {
      if (_t instanceof OperationCanceledError) {
        final OperationCanceledError e = (OperationCanceledError)_t;
        throw e.getWrapped();
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
