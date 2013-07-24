/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.codebuilder;

import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.ide.codebuilder.AbstractConstructorBuilder;
import org.eclipse.xtend.ide.codebuilder.ICodeBuilder.Xtend;
import org.eclipse.xtend.ide.codebuilder.InsertionOffsets;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Extension;

@SuppressWarnings("all")
public class XtendConstructorBuilder extends AbstractConstructorBuilder implements Xtend {
  @Inject
  @Extension
  private InsertionOffsets _insertionOffsets;
  
  public ISourceAppender build(final ISourceAppender appendable) {
    JvmVisibility _visibility = this.getVisibility();
    ISourceAppender _appendVisibility = this.appendVisibility(appendable, _visibility, JvmVisibility.PUBLIC);
    ISourceAppender _append = _appendVisibility.append("new");
    ISourceAppender _appendParameters = this.appendParameters(_append);
    ISourceAppender _appendThrowsClause = this.appendThrowsClause(_appendParameters);
    ISourceAppender _appendBody = this.appendBody(_appendThrowsClause, "");
    return _appendBody;
  }
  
  public int getInsertOffset() {
    EObject _context = this.getContext();
    XtendTypeDeclaration _xtendType = this.getXtendType();
    int _newConstructorInsertOffset = this._insertionOffsets.getNewConstructorInsertOffset(_context, _xtendType);
    return _newConstructorInsertOffset;
  }
  
  public int getIndentationLevel() {
    return 1;
  }
  
  public XtendTypeDeclaration getXtendType() {
    Object _ownerSource = this.getOwnerSource();
    return ((XtendTypeDeclaration) _ownerSource);
  }
}
