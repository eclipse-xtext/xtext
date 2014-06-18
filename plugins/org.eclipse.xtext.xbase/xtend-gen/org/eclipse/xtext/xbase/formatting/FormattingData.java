package org.eclipse.xtext.xbase.formatting;

import org.eclipse.xtend.lib.Data;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringHelper;

@Data
@SuppressWarnings("all")
public abstract class FormattingData {
  private final int _offset;
  
  private final int _length;
  
  private final int _increaseIndentationChange;
  
  private final int _decreaseIndentationChange;
  
  private final Throwable _trace;
  
  public abstract boolean isEmpty();
  
  public int getIndentationChange() {
    int _increaseIndentationChange = this.getIncreaseIndentationChange();
    int _decreaseIndentationChange = this.getDecreaseIndentationChange();
    return (_increaseIndentationChange + _decreaseIndentationChange);
  }
  
  public FormattingData(final int offset, final int length, final int increaseIndentationChange, final int decreaseIndentationChange, final Throwable trace) {
    super();
    this._offset = offset;
    this._length = length;
    this._increaseIndentationChange = increaseIndentationChange;
    this._decreaseIndentationChange = decreaseIndentationChange;
    this._trace = trace;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this._offset;
    result = prime * result + this._length;
    result = prime * result + this._increaseIndentationChange;
    result = prime * result + this._decreaseIndentationChange;
    result = prime * result + ((this._trace== null) ? 0 : this._trace.hashCode());
    return result;
  }
  
  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FormattingData other = (FormattingData) obj;
    if (other._offset != this._offset)
      return false;
    if (other._length != this._length)
      return false;
    if (other._increaseIndentationChange != this._increaseIndentationChange)
      return false;
    if (other._decreaseIndentationChange != this._decreaseIndentationChange)
      return false;
    if (this._trace == null) {
      if (other._trace != null)
        return false;
    } else if (!this._trace.equals(other._trace))
      return false;
    return true;
  }
  
  @Override
  @Pure
  public String toString() {
    String result = new ToStringHelper().toString(this);
    return result;
  }
  
  @Pure
  public int getOffset() {
    return this._offset;
  }
  
  @Pure
  public int getLength() {
    return this._length;
  }
  
  @Pure
  public int getIncreaseIndentationChange() {
    return this._increaseIndentationChange;
  }
  
  @Pure
  public int getDecreaseIndentationChange() {
    return this._decreaseIndentationChange;
  }
  
  @Pure
  public Throwable getTrace() {
    return this._trace;
  }
}
