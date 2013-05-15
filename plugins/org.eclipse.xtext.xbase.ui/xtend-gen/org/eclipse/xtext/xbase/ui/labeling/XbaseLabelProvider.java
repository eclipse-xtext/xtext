package org.eclipse.xtext.xbase.ui.labeling;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Arrays;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.xtext.common.types.JvmAnnotationType;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmIdentifiableElement;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.ui.label.AbstractLabelProvider;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.eclipse.xtext.xbase.typesystem.IResolvedTypes;
import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
import org.eclipse.xtext.xbase.ui.labeling.XbaseImageAdornments;
import org.eclipse.xtext.xbase.ui.labeling.XbaseImages2;
import org.eclipse.xtext.xbase.validation.UIStrings;
import org.eclipse.xtext.xtype.XImportDeclaration;
import org.eclipse.xtext.xtype.XImportSection;

@SuppressWarnings("all")
public class XbaseLabelProvider extends AbstractLabelProvider {
  @Inject
  private XbaseImages2 images;
  
  @Inject
  private UIStrings uiStrings;
  
  @Inject
  private IBatchTypeResolver typeResolver;
  
  @Inject
  private XbaseImageAdornments adornments;
  
  @Inject
  public XbaseLabelProvider(final AdapterFactoryLabelProvider delegate) {
    super(delegate);
  }
  
  protected ImageDescriptor _imageDescriptor(final XImportSection importSection) {
    ImageDescriptor _forImportContainer = this.images.forImportContainer();
    return _forImportContainer;
  }
  
  protected ImageDescriptor _imageDescriptor(final XImportDeclaration importDeclaration) {
    ImageDescriptor _forImport = this.images.forImport();
    return _forImport;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmGenericType genericType) {
    ImageDescriptor _xifexpression = null;
    boolean _isInterface = genericType.isInterface();
    if (_isInterface) {
      JvmVisibility _visibility = genericType.getVisibility();
      int _get = this.adornments.get(genericType);
      ImageDescriptor _forInterface = this.images.forInterface(_visibility, _get);
      _xifexpression = _forInterface;
    } else {
      JvmVisibility _visibility_1 = genericType.getVisibility();
      int _get_1 = this.adornments.get(genericType);
      ImageDescriptor _forClass = this.images.forClass(_visibility_1, _get_1);
      _xifexpression = _forClass;
    }
    return _xifexpression;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmEnumerationType enumerationType) {
    JvmVisibility _visibility = enumerationType.getVisibility();
    int _get = this.adornments.get(enumerationType);
    ImageDescriptor _forEnum = this.images.forEnum(_visibility, _get);
    return _forEnum;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmAnnotationType annotationType) {
    JvmVisibility _visibility = annotationType.getVisibility();
    int _get = this.adornments.get(annotationType);
    ImageDescriptor _forAnnotation = this.images.forAnnotation(_visibility, _get);
    return _forAnnotation;
  }
  
  protected String _text(final JvmGenericType genericType) {
    String _simpleName = genericType.getSimpleName();
    return _simpleName;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmOperation operation) {
    JvmVisibility _visibility = operation.getVisibility();
    int _get = this.adornments.get(operation);
    ImageDescriptor _forOperation = this.images.forOperation(_visibility, _get);
    return _forOperation;
  }
  
  protected Object _text(final JvmOperation element) {
    String _simpleName = element.getSimpleName();
    StyledString _signature = this.signature(_simpleName, element);
    return _signature;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmConstructor constructor) {
    JvmVisibility _visibility = constructor.getVisibility();
    int _get = this.adornments.get(constructor);
    ImageDescriptor _forConstructor = this.images.forConstructor(_visibility, _get);
    return _forConstructor;
  }
  
  protected String _text(final JvmConstructor constructor) {
    String _parameters = this.uiStrings.parameters(constructor);
    String _plus = ("new" + _parameters);
    return _plus;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmField field) {
    JvmVisibility _visibility = field.getVisibility();
    int _get = this.adornments.get(field);
    ImageDescriptor _forField = this.images.forField(_visibility, _get);
    return _forField;
  }
  
  protected String _text(final JvmField field) {
    String _simpleName = field.getSimpleName();
    String _plus = (_simpleName + " : ");
    JvmTypeReference _type = field.getType();
    String _simpleName_1 = _type.getSimpleName();
    String _plus_1 = (_plus + _simpleName_1);
    return _plus_1;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmFormalParameter parameter) {
    int _get = this.adornments.get(parameter);
    ImageDescriptor _forLocalVariable = this.images.forLocalVariable(_get);
    return _forLocalVariable;
  }
  
  protected String _text(final JvmFormalParameter parameter) {
    String _xblockexpression = null;
    {
      final JvmTypeReference parameterType = parameter.getParameterType();
      String _xifexpression = null;
      boolean _equals = Objects.equal(parameterType, null);
      if (_equals) {
        String _name = parameter.getName();
        _xifexpression = _name;
      } else {
        String _simpleName = parameterType.getSimpleName();
        String _plus = (_simpleName + " ");
        String _name_1 = parameter.getName();
        String _plus_1 = (_plus + _name_1);
        _xifexpression = _plus_1;
      }
      _xblockexpression = (_xifexpression);
    }
    return _xblockexpression;
  }
  
  protected ImageDescriptor _imageDescriptor(final XVariableDeclaration variableDeclaration) {
    int _get = this.adornments.get(variableDeclaration);
    ImageDescriptor _forLocalVariable = this.images.forLocalVariable(_get);
    return _forLocalVariable;
  }
  
  protected String _text(final XImportDeclaration importDeclaration) {
    String _xifexpression = null;
    String _importedNamespace = importDeclaration.getImportedNamespace();
    boolean _notEquals = (!Objects.equal(_importedNamespace, null));
    if (_notEquals) {
      String _importedNamespace_1 = importDeclaration.getImportedNamespace();
      _xifexpression = _importedNamespace_1;
    } else {
      String _importedTypeName = importDeclaration.getImportedTypeName();
      _xifexpression = _importedTypeName;
    }
    return _xifexpression;
  }
  
  protected String _text(final XVariableDeclaration variableDeclaration) {
    String _xblockexpression = null;
    {
      final IResolvedTypes resolvedTypes = this.typeResolver.resolveTypes(variableDeclaration);
      final LightweightTypeReference type = resolvedTypes.getActualType(((JvmIdentifiableElement) variableDeclaration));
      String _xifexpression = null;
      boolean _notEquals = (!Objects.equal(type, null));
      if (_notEquals) {
        String _simpleName = type.getSimpleName();
        String _plus = (_simpleName + " ");
        String _name = variableDeclaration.getName();
        String _plus_1 = (_plus + _name);
        _xifexpression = _plus_1;
      } else {
        String _name_1 = variableDeclaration.getName();
        _xifexpression = _name_1;
      }
      _xblockexpression = (_xifexpression);
    }
    return _xblockexpression;
  }
  
  protected ImageDescriptor _imageDescriptor(final JvmTypeParameter parameter) {
    int _get = this.adornments.get(parameter);
    ImageDescriptor _forTypeParameter = this.images.forTypeParameter(_get);
    return _forTypeParameter;
  }
  
  protected String _text(final XCasePart casePart) {
    String _xblockexpression = null;
    {
      EObject _eContainer = casePart.eContainer();
      if ((_eContainer instanceof XSwitchExpression)) {
        EObject _eContainer_1 = casePart.eContainer();
        final XSwitchExpression switchExpression = ((XSwitchExpression) _eContainer_1);
        boolean _notEquals = (!Objects.equal(switchExpression, null));
        if (_notEquals) {
          String _localVarName = switchExpression.getLocalVarName();
          boolean _notEquals_1 = (!Objects.equal(_localVarName, null));
          if (_notEquals_1) {
            return switchExpression.getLocalVarName();
          }
          XExpression _switch = switchExpression.getSwitch();
          if ((_switch instanceof XAbstractFeatureCall)) {
            XExpression _switch_1 = switchExpression.getSwitch();
            final XAbstractFeatureCall call = ((XAbstractFeatureCall) _switch_1);
            JvmIdentifiableElement _feature = call.getFeature();
            boolean _notEquals_2 = (!Objects.equal(_feature, null));
            if (_notEquals_2) {
              JvmIdentifiableElement _feature_1 = call.getFeature();
              return _feature_1.getSimpleName();
            }
          }
        }
      }
      _xblockexpression = (null);
    }
    return _xblockexpression;
  }
  
  protected StyledString signature(final String simpleName, final JvmIdentifiableElement element) {
    StyledString _xblockexpression = null;
    {
      IResolvedTypes _resolveTypes = this.typeResolver.resolveTypes(element);
      final LightweightTypeReference returnType = _resolveTypes.getActualType(element);
      String _xifexpression = null;
      boolean _equals = Objects.equal(returnType, null);
      if (_equals) {
        _xifexpression = "void";
      } else {
        String _simpleName = returnType.getSimpleName();
        _xifexpression = _simpleName;
      }
      final String returnTypeString = _xifexpression;
      String _parameters = this.uiStrings.parameters(element);
      String _plus = (simpleName + _parameters);
      StyledString _styledString = new StyledString(_plus);
      String _plus_1 = (" : " + returnTypeString);
      StyledString _styledString_1 = new StyledString(_plus_1, StyledString.DECORATIONS_STYLER);
      StyledString _append = _styledString.append(_styledString_1);
      _xblockexpression = (_append);
    }
    return _xblockexpression;
  }
  
  protected ImageDescriptor _imageDescriptor(final Object element) {
    return null;
  }
  
  protected String _text(final Object element) {
    return null;
  }
  
  protected Object doGetImage(final Object element) {
    Object _elvis = null;
    ImageDescriptor _imageDescriptor = element==null?(ImageDescriptor)null:this.imageDescriptor(element);
    if (_imageDescriptor != null) {
      _elvis = _imageDescriptor;
    } else {
      Object _doGetImage = super.doGetImage(element);
      _elvis = ObjectExtensions.<Object>operator_elvis(_imageDescriptor, _doGetImage);
    }
    return _elvis;
  }
  
  protected Object doGetText(final Object element) {
    Object _elvis = null;
    Object _text = element==null?(Object)null:this.text(element);
    if (_text != null) {
      _elvis = _text;
    } else {
      Object _doGetText = super.doGetText(element);
      _elvis = ObjectExtensions.<Object>operator_elvis(_text, _doGetText);
    }
    return _elvis;
  }
  
  protected ImageDescriptor imageDescriptor(final Object constructor) {
    if (constructor instanceof JvmConstructor) {
      return _imageDescriptor((JvmConstructor)constructor);
    } else if (constructor instanceof JvmOperation) {
      return _imageDescriptor((JvmOperation)constructor);
    } else if (constructor instanceof JvmAnnotationType) {
      return _imageDescriptor((JvmAnnotationType)constructor);
    } else if (constructor instanceof JvmEnumerationType) {
      return _imageDescriptor((JvmEnumerationType)constructor);
    } else if (constructor instanceof JvmField) {
      return _imageDescriptor((JvmField)constructor);
    } else if (constructor instanceof JvmGenericType) {
      return _imageDescriptor((JvmGenericType)constructor);
    } else if (constructor instanceof JvmTypeParameter) {
      return _imageDescriptor((JvmTypeParameter)constructor);
    } else if (constructor instanceof JvmFormalParameter) {
      return _imageDescriptor((JvmFormalParameter)constructor);
    } else if (constructor instanceof XVariableDeclaration) {
      return _imageDescriptor((XVariableDeclaration)constructor);
    } else if (constructor instanceof XImportDeclaration) {
      return _imageDescriptor((XImportDeclaration)constructor);
    } else if (constructor instanceof XImportSection) {
      return _imageDescriptor((XImportSection)constructor);
    } else if (constructor != null) {
      return _imageDescriptor(constructor);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(constructor).toString());
    }
  }
  
  protected Object text(final Object constructor) {
    if (constructor instanceof JvmConstructor) {
      return _text((JvmConstructor)constructor);
    } else if (constructor instanceof JvmOperation) {
      return _text((JvmOperation)constructor);
    } else if (constructor instanceof JvmField) {
      return _text((JvmField)constructor);
    } else if (constructor instanceof JvmGenericType) {
      return _text((JvmGenericType)constructor);
    } else if (constructor instanceof JvmFormalParameter) {
      return _text((JvmFormalParameter)constructor);
    } else if (constructor instanceof XVariableDeclaration) {
      return _text((XVariableDeclaration)constructor);
    } else if (constructor instanceof XCasePart) {
      return _text((XCasePart)constructor);
    } else if (constructor instanceof XImportDeclaration) {
      return _text((XImportDeclaration)constructor);
    } else if (constructor != null) {
      return _text(constructor);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(constructor).toString());
    }
  }
}
