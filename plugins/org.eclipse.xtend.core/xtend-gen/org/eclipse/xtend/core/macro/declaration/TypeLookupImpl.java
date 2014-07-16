package org.eclipse.xtend.core.macro.declaration;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.macro.declaration.CompilationUnitImpl;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.lib.macro.declaration.MutableAnnotationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableEnumerationTypeDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration;
import org.eclipse.xtend.lib.macro.declaration.Type;
import org.eclipse.xtend.lib.macro.services.TypeLookup;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xtype.XtypePackage;

@SuppressWarnings("all")
public class TypeLookupImpl implements TypeLookup {
  @Extension
  private CompilationUnitImpl compilationUnit;
  
  public TypeLookupImpl(final CompilationUnitImpl compilationUnit) {
    this.compilationUnit = compilationUnit;
  }
  
  public MutableAnnotationTypeDeclaration findAnnotationType(final String qualifiedName) {
    final Type type = this.findType(qualifiedName);
    MutableAnnotationTypeDeclaration _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (type instanceof MutableAnnotationTypeDeclaration) {
        _matched=true;
        _switchResult = ((MutableAnnotationTypeDeclaration)type);
      }
    }
    return _switchResult;
  }
  
  public MutableClassDeclaration findClass(final String qualifiedName) {
    final Type type = this.findType(qualifiedName);
    MutableClassDeclaration _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (type instanceof MutableClassDeclaration) {
        _matched=true;
        _switchResult = ((MutableClassDeclaration)type);
      }
    }
    return _switchResult;
  }
  
  public MutableEnumerationTypeDeclaration findEnumerationType(final String qualifiedName) {
    final Type type = this.findType(qualifiedName);
    MutableEnumerationTypeDeclaration _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (type instanceof MutableEnumerationTypeDeclaration) {
        _matched=true;
        _switchResult = ((MutableEnumerationTypeDeclaration)type);
      }
    }
    return _switchResult;
  }
  
  public MutableInterfaceDeclaration findInterface(final String qualifiedName) {
    final Type type = this.findType(qualifiedName);
    MutableInterfaceDeclaration _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if (type instanceof MutableInterfaceDeclaration) {
        _matched=true;
        _switchResult = ((MutableInterfaceDeclaration)type);
      }
    }
    return _switchResult;
  }
  
  private Type findType(final String qualifiedName) {
    XtendFile _xtendFile = this.compilationUnit.getXtendFile();
    Resource _eResource = _xtendFile.eResource();
    EList<EObject> _contents = _eResource.getContents();
    Iterable<JvmDeclaredType> _filter = Iterables.<JvmDeclaredType>filter(_contents, JvmDeclaredType.class);
    final JvmDeclaredType result = this.recursiveFindType(qualifiedName, _filter);
    Type _xifexpression = null;
    boolean _notEquals = (!Objects.equal(result, null));
    if (_notEquals) {
      _xifexpression = this.compilationUnit.toType(result);
    }
    return _xifexpression;
  }
  
  private JvmDeclaredType recursiveFindType(final String qualifiedName, final Iterable<? extends JvmDeclaredType> typeDeclarations) {
    final char dot = '.';
    for (final JvmDeclaredType type : typeDeclarations) {
      {
        final String name = type.getQualifiedName('.');
        boolean _equals = Objects.equal(qualifiedName, name);
        if (_equals) {
          return type;
        }
        boolean _and = false;
        boolean _startsWith = qualifiedName.startsWith(name);
        if (!_startsWith) {
          _and = false;
        } else {
          int _length = name.length();
          char _charAt = qualifiedName.charAt(_length);
          boolean _equals_1 = (_charAt == dot);
          _and = _equals_1;
        }
        if (_and) {
          EList<JvmMember> _members = type.getMembers();
          Iterable<JvmDeclaredType> _filter = Iterables.<JvmDeclaredType>filter(_members, JvmDeclaredType.class);
          return this.recursiveFindType(qualifiedName, _filter);
        }
      }
    }
    return null;
  }
  
  public Type findTypeGlobally(final Class<?> clazz) {
    String _canonicalName = clazz.getCanonicalName();
    return this.findTypeGlobally(_canonicalName);
  }
  
  public Type findTypeGlobally(final String typeName) {
    Type _elvis = null;
    Type _findType = this.findType(typeName);
    if (_findType != null) {
      _elvis = _findType;
    } else {
      Type _xblockexpression = null;
      {
        IQualifiedNameConverter _qualifiedNameConverter = this.compilationUnit.getQualifiedNameConverter();
        final QualifiedName qualifiedName = _qualifiedNameConverter.toQualifiedName(typeName);
        IScopeProvider _scopeProvider = this.compilationUnit.getScopeProvider();
        XtendFile _xtendFile = this.compilationUnit.getXtendFile();
        IScope _scope = _scopeProvider.getScope(_xtendFile, XtypePackage.Literals.XIMPORT_DECLARATION__IMPORTED_TYPE);
        final IEObjectDescription result = _scope.getSingleElement(qualifiedName);
        Type _xifexpression = null;
        boolean _and = false;
        boolean _tripleNotEquals = (result != null);
        if (!_tripleNotEquals) {
          _and = false;
        } else {
          EClass _eClass = result.getEClass();
          boolean _isSuperTypeOf = TypesPackage.Literals.JVM_TYPE.isSuperTypeOf(_eClass);
          _and = _isSuperTypeOf;
        }
        if (_and) {
          EObject _eObjectOrProxy = result.getEObjectOrProxy();
          _xifexpression = this.compilationUnit.toType(((JvmType) _eObjectOrProxy));
        } else {
          _xifexpression = null;
        }
        _xblockexpression = _xifexpression;
      }
      _elvis = _xblockexpression;
    }
    return _elvis;
  }
}
