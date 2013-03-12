package org.eclipse.xtend.core.validation;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend.core.validation.IssueCodes;
import org.eclipse.xtend.core.xtend.XtendAnnotationType;
import org.eclipse.xtend.core.xtend.XtendField;
import org.eclipse.xtend.core.xtend.XtendMember;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend.core.xtend.XtendPackage.Literals;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmGenericArrayTypeReference;
import org.eclipse.xtext.common.types.JvmPrimitiveType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;

@SuppressWarnings("all")
public class AnnotationValidation extends AbstractDeclarativeValidator {
  protected List<EPackage> getEPackages() {
    ArrayList<EPackage> _newArrayList = CollectionLiterals.<EPackage>newArrayList(XtendPackage.eINSTANCE, XbasePackage.eINSTANCE, XAnnotationsPackage.eINSTANCE);
    return _newArrayList;
  }
  
  @Check
  public void checkAnnotation(final XtendAnnotationType it) {
    EList<XtendMember> _members = it.getMembers();
    Iterable<XtendField> _filter = Iterables.<XtendField>filter(_members, XtendField.class);
    for (final XtendField it_1 : _filter) {
      JvmTypeReference _type = it_1.getType();
      boolean _isValidAnnotationValueType = this.isValidAnnotationValueType(_type);
      boolean _not = (!_isValidAnnotationValueType);
      if (_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Invalid type ");
        JvmTypeReference _type_1 = it_1.getType();
        String _simpleName = _type_1.getSimpleName();
        _builder.append(_simpleName, "");
        _builder.append(" for the annotation attribute ");
        String _name = it_1.getName();
        _builder.append(_name, "");
        _builder.append("; only primitive type, String, Class, annotation, enumeration are permitted or 1-dimensional arrays thereof");
        this.error(_builder.toString(), it_1, Literals.XTEND_FIELD__TYPE, IssueCodes.INVALID_ANNOTATION_VALUE_TYPE);
      }
    }
  }
  
  public boolean isValidAnnotationValueType(final JvmTypeReference reference) {
    JvmTypeReference _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (reference instanceof JvmGenericArrayTypeReference) {
        final JvmGenericArrayTypeReference _jvmGenericArrayTypeReference = (JvmGenericArrayTypeReference)reference;
        _matched=true;
        JvmTypeReference _componentType = _jvmGenericArrayTypeReference.getComponentType();
        _switchResult = _componentType;
      }
    }
    if (!_matched) {
      _switchResult = reference;
    }
    final JvmTypeReference toCheck = _switchResult;
    boolean _equals = Objects.equal(toCheck, null);
    if (_equals) {
      return true;
    }
    JvmType _type = toCheck.getType();
    if ((_type instanceof JvmPrimitiveType)) {
      return true;
    }
    JvmType _type_1 = toCheck.getType();
    if ((_type_1 instanceof JvmEnumerationType)) {
      return true;
    }
    JvmType _type_2 = toCheck.getType();
    if ((_type_2 instanceof JvmAnnotationType)) {
      return true;
    }
    boolean _or = false;
    JvmType _type_3 = toCheck.getType();
    String _qualifiedName = _type_3.getQualifiedName();
    boolean _equals_1 = Objects.equal(_qualifiedName, "java.lang.String");
    if (_equals_1) {
      _or = true;
    } else {
      JvmType _type_4 = toCheck.getType();
      String _qualifiedName_1 = _type_4.getQualifiedName();
      boolean _equals_2 = Objects.equal(_qualifiedName_1, "java.lang.Class");
      _or = (_equals_1 || _equals_2);
    }
    if (_or) {
      return true;
    }
    return false;
  }
}
