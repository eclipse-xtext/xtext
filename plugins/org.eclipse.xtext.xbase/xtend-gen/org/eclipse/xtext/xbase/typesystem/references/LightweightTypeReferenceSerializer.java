package org.eclipse.xtext.xbase.typesystem.references;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.xbase.compiler.ISourceAppender;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.typesystem.references.AnyTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ArrayTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.CompoundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.FunctionTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.ParameterizedTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.TypeReferenceVisitor;
import org.eclipse.xtext.xbase.typesystem.references.UnboundTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.UnknownTypeReference;
import org.eclipse.xtext.xbase.typesystem.references.WildcardTypeReference;

@SuppressWarnings("all")
public class LightweightTypeReferenceSerializer extends TypeReferenceVisitor {
  private ISourceAppender appender;
  
  public LightweightTypeReferenceSerializer(final ISourceAppender appender) {
    this.appender = appender;
  }
  
  protected void doVisitAnyTypeReference(final AnyTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  protected void doVisitArrayTypeReference(final ArrayTypeReference reference) {
    LightweightTypeReference _componentType = reference.getComponentType();
    _componentType.accept(this);
    this.appender.append("[]");
  }
  
  protected void doVisitCompoundTypeReference(final CompoundTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  protected void doVisitFunctionTypeReference(final FunctionTypeReference reference) {
    boolean _isJava = this.appender.isJava();
    if (_isJava) {
      this.doVisitParameterizedTypeReference(reference);
    } else {
      this.appender.append("(");
      List<LightweightTypeReference> _parameterTypes = reference.getParameterTypes();
      this.appendCommaSeparated(_parameterTypes);
      this.appender.append(")=>");
      LightweightTypeReference _returnType = reference.getReturnType();
      _returnType.accept(this);
    }
  }
  
  protected void doVisitParameterizedTypeReference(final ParameterizedTypeReference reference) {
    JvmType _type = reference.getType();
    this.appender.append(_type);
    List<LightweightTypeReference> _typeArguments = reference.getTypeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.appender.append("<");
      List<LightweightTypeReference> _typeArguments_1 = reference.getTypeArguments();
      this.appendCommaSeparated(_typeArguments_1);
      this.appender.append(">");
    }
  }
  
  protected void doVisitUnboundTypeReference(final UnboundTypeReference reference) {
    LightweightTypeReference _javaType = reference.toJavaType();
    _javaType.accept(this);
  }
  
  protected void doVisitUnknownTypeReference(final UnknownTypeReference reference) {
    String _simpleName = reference.getSimpleName();
    this.appender.append(_simpleName);
  }
  
  protected void doVisitWildcardTypeReference(final WildcardTypeReference reference) {
    this.appender.append("?");
    LightweightTypeReference _lowerBound = reference.getLowerBound();
    boolean _notEquals = (!Objects.equal(_lowerBound, null));
    if (_notEquals) {
      this.appender.append(" super ");
      LightweightTypeReference _lowerBound_1 = reference.getLowerBound();
      _lowerBound_1.accept(this);
    } else {
      List<LightweightTypeReference> _upperBounds = reference.getUpperBounds();
      final Function1<LightweightTypeReference,Boolean> _function = new Function1<LightweightTypeReference,Boolean>() {
          public Boolean apply(final LightweightTypeReference it) {
            String _identifier = it.getIdentifier();
            boolean _notEquals = (!Objects.equal("java.lang.Object", _identifier));
            return Boolean.valueOf(_notEquals);
          }
        };
      final Iterable<LightweightTypeReference> relevantUpperBounds = IterableExtensions.<LightweightTypeReference>filter(_upperBounds, _function);
      boolean _isEmpty = IterableExtensions.isEmpty(relevantUpperBounds);
      boolean _not = (!_isEmpty);
      if (_not) {
        this.appender.append(" extends ");
        boolean isFirst = true;
        for (final LightweightTypeReference upperBound : relevantUpperBounds) {
          {
            boolean _not_1 = (!isFirst);
            if (_not_1) {
              this.appender.append(" & ");
            }
            isFirst = false;
            upperBound.accept(this);
          }
        }
      }
    }
  }
  
  protected void appendCommaSeparated(final List<LightweightTypeReference> references) {
    boolean isFirst = true;
    for (final LightweightTypeReference reference : references) {
      {
        boolean _not = (!isFirst);
        if (_not) {
          this.appender.append(", ");
        }
        isFirst = false;
        reference.accept(this);
      }
    }
  }
}
