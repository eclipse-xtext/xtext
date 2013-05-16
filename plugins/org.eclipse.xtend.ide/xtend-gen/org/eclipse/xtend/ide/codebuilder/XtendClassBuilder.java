package org.eclipse.xtend.ide.codebuilder;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration;
import org.eclipse.xtend.ide.codebuilder.AbstractClassBuilder;
import org.eclipse.xtend.ide.codebuilder.ICodeBuilder.Xtend;
import org.eclipse.xtend.ide.codebuilder.InsertionOffsets;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.xbase.compiler.IAppendable;
import org.eclipse.xtext.xbase.compiler.TypeReferenceSerializer;
import org.eclipse.xtext.xbase.compiler.output.XtypeTypeReferenceSerializer;
import org.eclipse.xtext.xbase.lib.Extension;

@SuppressWarnings("all")
public class XtendClassBuilder extends AbstractClassBuilder implements Xtend {
  @Inject
  private XtypeTypeReferenceSerializer typeRefSerializer;
  
  @Inject
  @Extension
  private InsertionOffsets _insertionOffsets;
  
  public boolean isValid() {
    boolean _and = false;
    boolean _and_1 = false;
    boolean _isValid = super.isValid();
    if (!_isValid) {
      _and_1 = false;
    } else {
      String _className = this.getClassName();
      boolean _notEquals = (!Objects.equal(_className, null));
      _and_1 = (_isValid && _notEquals);
    }
    if (!_and_1) {
      _and = false;
    } else {
      JvmVisibility _visibility = this.getVisibility();
      boolean _equals = Objects.equal(_visibility, JvmVisibility.PUBLIC);
      _and = (_and_1 && _equals);
    }
    return _and;
  }
  
  protected TypeReferenceSerializer getTypeReferenceSerializer() {
    return this.typeRefSerializer;
  }
  
  public IAppendable build(final IAppendable appendable) {
    IAppendable _append = appendable.append("class ");
    String _className = this.getClassName();
    IAppendable _append_1 = _append.append(_className);
    IAppendable _append_2 = _append_1.append(" {");
    IAppendable _newLine = _append_2.newLine();
    IAppendable _append_3 = _newLine.append("}");
    return _append_3;
  }
  
  public int getInsertOffset() {
    EObject _context = this.getContext();
    XtendTypeDeclaration _xtendType = this.getXtendType();
    int _newTypeInsertOffset = this._insertionOffsets.getNewTypeInsertOffset(_context, _xtendType);
    return _newTypeInsertOffset;
  }
  
  public int getIndentationLevel() {
    return 0;
  }
  
  public XtendTypeDeclaration getXtendType() {
    Object _ownerSource = this.getOwnerSource();
    return ((XtendTypeDeclaration) _ownerSource);
  }
}
