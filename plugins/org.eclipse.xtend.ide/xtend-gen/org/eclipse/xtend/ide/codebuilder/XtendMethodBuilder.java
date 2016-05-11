/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.codebuilder;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.ide.codebuilder.AbstractMethodBuilder;
import org.eclipse.xtend.ide.codebuilder.ICodeBuilder;
import org.eclipse.xtend.ide.codebuilder.InsertionOffsets;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
@SuppressWarnings("all")
public class XtendMethodBuilder extends AbstractMethodBuilder implements ICodeBuilder.Xtend {
  @Inject
  @Extension
  private InsertionOffsets _insertionOffsets;
  
  @Override
  public boolean isValid() {
    return (super.isValid() && (!Objects.equal(this.getMethodName(), null)));
  }
  
  @Override
  public ISourceAppender build(final ISourceAppender appendable) {
    ISourceAppender _xblockexpression = null;
    {
      String _xifexpression = null;
      boolean _isOverrideFlag = this.isOverrideFlag();
      if (_isOverrideFlag) {
        _xifexpression = "override ";
      } else {
        _xifexpression = "def ";
      }
      ISourceAppender _append = appendable.append(_xifexpression);
      JvmVisibility _visibility = this.getVisibility();
      this.appendVisibility(_append, _visibility, JvmVisibility.PUBLIC);
      boolean _isStaticFlag = this.isStaticFlag();
      if (_isStaticFlag) {
        appendable.append("static ");
      }
      List<JvmTypeParameter> _typeParameters = this.getTypeParameters();
      this.appendTypeParameters(appendable, _typeParameters);
      boolean _isAbstractFlag = this.isAbstractFlag();
      if (_isAbstractFlag) {
        LightweightTypeReference _returnType = this.getReturnType();
        ISourceAppender _appendType = this.appendType(appendable, _returnType, "void");
        _appendType.append(" ");
      }
      String _methodName = this.getMethodName();
      ISourceAppender _append_1 = appendable.append(_methodName);
      ISourceAppender _appendParameters = this.appendParameters(_append_1);
      this.appendThrowsClause(_appendParameters);
      boolean _isAbstractFlag_1 = this.isAbstractFlag();
      boolean _not = (!_isAbstractFlag_1);
      if (_not) {
        this.appendBody(appendable, "");
      }
      _xblockexpression = appendable;
    }
    return _xblockexpression;
  }
  
  @Override
  public int getInsertOffset(final XtextResource resource) {
    EObject _context = this.getContext();
    XtendTypeDeclaration _xtendType = this.getXtendType();
    XtendTypeDeclaration _findByFragment = this.<XtendTypeDeclaration>findByFragment(resource, _xtendType);
    return this._insertionOffsets.getNewMethodInsertOffset(_context, _findByFragment);
  }
  
  @Override
  public int getIndentationLevel() {
    return 1;
  }
  
  @Override
  public XtendTypeDeclaration getXtendType() {
    Object _ownerSource = this.getOwnerSource();
    return ((XtendTypeDeclaration) _ownerSource);
  }
}
