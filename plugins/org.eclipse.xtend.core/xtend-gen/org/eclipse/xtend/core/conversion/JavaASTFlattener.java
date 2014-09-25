/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.conversion;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure2;

/**
 * @author Dennis Huebner - Initial contribution and API
 */
@SuppressWarnings("all")
public class JavaASTFlattener extends ASTVisitor {
  /**
   * The string buffer into which the serialized representation of the AST is
   * written.
   */
  protected StringBuffer fBuffer;
  
  @Inject
  private IValueConverterService converterService;
  
  /**
   * Creates a new AST printer.
   */
  public JavaASTFlattener() {
    StringBuffer _stringBuffer = new StringBuffer();
    this.fBuffer = _stringBuffer;
  }
  
  /**
   * Returns the string accumulated in the visit.
   * 
   * @return the serialized
   */
  public String getResult() {
    return this.fBuffer.toString();
  }
  
  /**
   * Resets this printer so that it can be used again.
   */
  public void reset() {
    this.fBuffer.setLength(0);
  }
  
  private boolean isOverrideMethode(final MethodDeclaration declaration) {
    List _modifiers = declaration.modifiers();
    Iterable<Annotation> _filter = Iterables.<Annotation>filter(_modifiers, Annotation.class);
    final Function1<Annotation, Boolean> _function = new Function1<Annotation, Boolean>() {
      public Boolean apply(final Annotation it) {
        Name _typeName = it.getTypeName();
        String _string = _typeName.toString();
        return Boolean.valueOf(Objects.equal("Override", _string));
      }
    };
    Iterable<Annotation> _filter_1 = IterableExtensions.<Annotation>filter(_filter, _function);
    boolean _isEmpty = IterableExtensions.isEmpty(_filter_1);
    return (!_isEmpty);
  }
  
  public boolean visit(final StringLiteral it) {
    String _literalValue = it.getLiteralValue();
    String _string = this.converterService.toString(_literalValue, "STRING");
    this.appendToBuffer(_string);
    return false;
  }
  
  public void appendModifieres(final ASTNode node, final Iterable<IExtendedModifier> ext) {
    this.appendModifieres(node, ext, null);
  }
  
  public void appendModifieres(final ASTNode node, final Iterable<IExtendedModifier> ext, final Function1<? super ASTNode, ? extends StringBuffer> callBack) {
    final Procedure1<IExtendedModifier> _function = new Procedure1<IExtendedModifier>() {
      public void apply(final IExtendedModifier p) {
        ((ASTNode) p).accept(JavaASTFlattener.this);
      }
    };
    final Procedure1<IExtendedModifier> appender = _function;
    final Function1<IExtendedModifier, Boolean> _function_1 = new Function1<IExtendedModifier, Boolean>() {
      public Boolean apply(final IExtendedModifier it) {
        return Boolean.valueOf(it.isAnnotation());
      }
    };
    Iterable<IExtendedModifier> _filter = IterableExtensions.<IExtendedModifier>filter(ext, _function_1);
    IterableExtensions.<IExtendedModifier>forEach(_filter, appender);
    boolean _notEquals = (!Objects.equal(callBack, null));
    if (_notEquals) {
      callBack.apply(node);
    }
    final Function1<IExtendedModifier, Boolean> _function_2 = new Function1<IExtendedModifier, Boolean>() {
      public Boolean apply(final IExtendedModifier it) {
        return Boolean.valueOf(it.isModifier());
      }
    };
    Iterable<IExtendedModifier> _filter_1 = IterableExtensions.<IExtendedModifier>filter(ext, _function_2);
    IterableExtensions.<IExtendedModifier>forEach(_filter_1, appender);
  }
  
  private StringBuffer appendSpaceToBuffer() {
    return this.appendToBuffer(" ");
  }
  
  private StringBuffer appendLineWrapToBuffer() {
    return this.appendToBuffer("\n");
  }
  
  private StringBuffer appendToBuffer(final String string) {
    return this.fBuffer.append(string);
  }
  
  public boolean visit(final Assignment node) {
    Expression _leftHandSide = node.getLeftHandSide();
    _leftHandSide.accept(this);
    Assignment.Operator _operator = node.getOperator();
    String _string = _operator.toString();
    this.appendToBuffer(_string);
    Expression _rightHandSide = node.getRightHandSide();
    _rightHandSide.accept(this);
    return false;
  }
  
  public boolean visit(final MarkerAnnotation node) {
    this.appendToBuffer("@");
    Name _typeName = node.getTypeName();
    _typeName.accept(this);
    this.appendSpaceToBuffer();
    return false;
  }
  
  public boolean visit(final CompilationUnit it) {
    PackageDeclaration _package = it.getPackage();
    boolean _notEquals = (!Objects.equal(_package, null));
    if (_notEquals) {
      PackageDeclaration _package_1 = it.getPackage();
      _package_1.accept(this);
    }
    List _imports = it.imports();
    this.appendAll(_imports);
    List _types = it.types();
    this.appendAll(_types);
    return false;
  }
  
  public boolean visit(final PackageDeclaration it) {
    Javadoc _javadoc = it.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = it.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _annotations = it.annotations();
    this.appendAll(_annotations, " ");
    this.appendToBuffer("package ");
    Name _name = it.getName();
    _name.accept(this);
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public boolean visit(final ParameterizedType it) {
    Type _type = it.getType();
    _type.accept(this);
    List _typeArguments = it.typeArguments();
    this.appendTypeParameters(_typeArguments);
    return false;
  }
  
  public boolean visit(final PrimitiveType it) {
    PrimitiveType.Code _primitiveTypeCode = it.getPrimitiveTypeCode();
    String _string = _primitiveTypeCode.toString();
    this.appendToBuffer(_string);
    return false;
  }
  
  public boolean visit(final BooleanLiteral it) {
    boolean _booleanValue = it.booleanValue();
    String _valueOf = String.valueOf(_booleanValue);
    this.appendToBuffer(_valueOf);
    return false;
  }
  
  public boolean visit(final ImportDeclaration it) {
    this.appendToBuffer("import ");
    boolean _isStatic = it.isStatic();
    if (_isStatic) {
      this.appendToBuffer("static ");
    }
    Name _name = it.getName();
    _name.accept(this);
    boolean _isOnDemand = it.isOnDemand();
    if (_isOnDemand) {
      this.appendToBuffer(".*");
    }
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public boolean visit(final Initializer it) {
    Javadoc _javadoc = it.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = it.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers);
    Block _body = it.getBody();
    _body.accept(this);
    return false;
  }
  
  public boolean visit(final TypeDeclaration it) {
    Javadoc _javadoc = it.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = it.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers);
    boolean _and = false;
    boolean _and_1 = false;
    boolean _isInterface = it.isInterface();
    boolean _not = (!_isInterface);
    if (!_not) {
      _and_1 = false;
    } else {
      ASTNode _parent = it.getParent();
      _and_1 = (_parent instanceof TypeDeclaration);
    }
    if (!_and_1) {
      _and = false;
    } else {
      List _modifiers_1 = it.modifiers();
      Iterable<Modifier> _filter = Iterables.<Modifier>filter(_modifiers_1, Modifier.class);
      final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
        public Boolean apply(final Modifier it) {
          return Boolean.valueOf(it.isStatic());
        }
      };
      boolean _exists = IterableExtensions.<Modifier>exists(_filter, _function);
      boolean _not_1 = (!_exists);
      _and = _not_1;
    }
    if (_and) {
      this.appendToBuffer("static ");
    }
    List _modifiers_2 = it.modifiers();
    Iterable<Modifier> _filter_1 = Iterables.<Modifier>filter(_modifiers_2, Modifier.class);
    boolean _isPackageVisibility = this.isPackageVisibility(_filter_1);
    if (_isPackageVisibility) {
      this.appendToBuffer("package ");
    }
    boolean _isInterface_1 = it.isInterface();
    if (_isInterface_1) {
      this.appendToBuffer("interface ");
    } else {
      this.appendToBuffer("class ");
    }
    SimpleName _name = it.getName();
    _name.accept(this);
    List _typeParameters = it.typeParameters();
    boolean _isEmpty = _typeParameters.isEmpty();
    boolean _not_2 = (!_isEmpty);
    if (_not_2) {
      List _typeParameters_1 = it.typeParameters();
      this.appendTypeParameters(_typeParameters_1);
    }
    this.appendSpaceToBuffer();
    Type _superclassType = it.getSuperclassType();
    boolean _notEquals_1 = (!Objects.equal(_superclassType, null));
    if (_notEquals_1) {
      this.appendToBuffer("extends ");
      Type _superclassType_1 = it.getSuperclassType();
      _superclassType_1.accept(this);
      this.appendToBuffer(" ");
    }
    List _superInterfaceTypes = it.superInterfaceTypes();
    boolean _isEmpty_1 = _superInterfaceTypes.isEmpty();
    boolean _not_3 = (!_isEmpty_1);
    if (_not_3) {
      boolean _isInterface_2 = it.isInterface();
      if (_isInterface_2) {
        this.appendToBuffer("extends ");
      } else {
        this.appendToBuffer("implements ");
      }
      List _superInterfaceTypes_1 = it.superInterfaceTypes();
      this.appendAllSeparatedByComma(_superInterfaceTypes_1);
    }
    this.appendToBuffer("{");
    this.appendLineWrapToBuffer();
    BodyDeclaration prev = null;
    List _bodyDeclarations = it.bodyDeclarations();
    for (final BodyDeclaration body : ((Iterable<BodyDeclaration>) _bodyDeclarations)) {
      {
        if ((prev instanceof EnumConstantDeclaration)) {
          if ((body instanceof EnumConstantDeclaration)) {
            this.appendToBuffer(", ");
          } else {
            this.appendToBuffer("; ");
          }
        }
        body.accept(this);
        prev = body;
      }
    }
    this.appendLineWrapToBuffer();
    this.appendToBuffer("}");
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public boolean visit(final Javadoc it) {
    this.appendToBuffer("/** ");
    List _tags = it.tags();
    this.appendAll(_tags);
    this.appendToBuffer("\n */");
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public boolean visit(final SimpleName it) {
    String _identifier = it.getIdentifier();
    String convertedName = this.converterService.toString(_identifier, "ID");
    boolean _equals = "it".equals(convertedName);
    if (_equals) {
      this.appendToBuffer("^");
    }
    this.appendToBuffer(convertedName);
    return false;
  }
  
  public boolean visit(final QualifiedName it) {
    Name _qualifier = it.getQualifier();
    _qualifier.accept(this);
    this.appendToBuffer(".");
    SimpleName _name = it.getName();
    _name.accept(this);
    return false;
  }
  
  public boolean visit(final SimpleType node) {
    return true;
  }
  
  public boolean visit(final Modifier it) {
    boolean append = true;
    Modifier.ModifierKeyword _keyword = it.getKeyword();
    int _flagValue = _keyword.toFlagValue();
    switch (_flagValue) {
      case Modifier.PUBLIC:
        boolean _or = false;
        ASTNode _parent = it.getParent();
        if ((_parent instanceof TypeDeclaration)) {
          _or = true;
        } else {
          ASTNode _parent_1 = it.getParent();
          _or = (_parent_1 instanceof MethodDeclaration);
        }
        if (_or) {
          append = false;
        }
        break;
      case Modifier.PRIVATE:
        ASTNode _parent_2 = it.getParent();
        if ((_parent_2 instanceof FieldDeclaration)) {
          append = false;
        }
        break;
      case Modifier.FINAL:
        boolean _or_1 = false;
        ASTNode _parent_3 = it.getParent();
        if ((_parent_3 instanceof VariableDeclarationExpression)) {
          _or_1 = true;
        } else {
          ASTNode _parent_4 = it.getParent();
          _or_1 = (_parent_4 instanceof VariableDeclarationStatement);
        }
        if (_or_1) {
          append = false;
        }
        break;
      default:
        append = true;
        break;
    }
    if (append) {
      Modifier.ModifierKeyword _keyword_1 = it.getKeyword();
      String valueToAppend = _keyword_1.toString();
      Modifier.ModifierKeyword _keyword_2 = it.getKeyword();
      int _flagValue_1 = _keyword_2.toFlagValue();
      boolean _equals = (_flagValue_1 == 0);
      if (_equals) {
        valueToAppend = "package";
      }
      this.appendToBuffer(valueToAppend);
      this.appendSpaceToBuffer();
    }
    return false;
  }
  
  public boolean visit(final FieldDeclaration it) {
    Javadoc _javadoc = it.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = it.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers);
    List _modifiers_1 = it.modifiers();
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(_modifiers_1, Modifier.class);
    boolean _isPackageVisibility = this.isPackageVisibility(_filter);
    if (_isPackageVisibility) {
      this.appendToBuffer("package ");
    }
    Type _type = it.getType();
    _type.accept(this);
    this.appendSpaceToBuffer();
    List _fragments = it.fragments();
    this.appendAllSeparatedByComma(_fragments);
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public boolean visit(final VariableDeclarationExpression it) {
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers);
    List _modifiers_1 = it.modifiers();
    this.handleVariableDeclaration(_modifiers_1);
    Type _type = it.getType();
    _type.accept(this);
    this.appendToBuffer(" ");
    List _fragments = it.fragments();
    this.appendAllSeparatedByComma(_fragments);
    return false;
  }
  
  public StringBuffer handleVariableDeclaration(final Iterable<? extends ASTNode> modifier) {
    StringBuffer _xifexpression = null;
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(modifier, Modifier.class);
    boolean _isFinal = this.isFinal(_filter);
    if (_isFinal) {
      _xifexpression = this.appendToBuffer("val ");
    } else {
      _xifexpression = this.appendToBuffer("var ");
    }
    return _xifexpression;
  }
  
  public boolean isFinal(final Iterable<Modifier> modifier) {
    final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
      public Boolean apply(final Modifier it) {
        return Boolean.valueOf(it.isFinal());
      }
    };
    return IterableExtensions.<Modifier>exists(modifier, _function);
  }
  
  public boolean visit(final VariableDeclarationFragment it) {
    SimpleName _name = it.getName();
    _name.accept(this);
    int _extraDimensions = it.getExtraDimensions();
    this.appendExtraDimensions(_extraDimensions);
    Expression _initializer = it.getInitializer();
    boolean _notEquals = (!Objects.equal(_initializer, null));
    if (_notEquals) {
      this.appendToBuffer("=");
      Expression _initializer_1 = it.getInitializer();
      _initializer_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final ConditionalExpression node) {
    this.appendToBuffer("if (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(") ");
    Expression _thenExpression = node.getThenExpression();
    _thenExpression.accept(this);
    this.appendToBuffer(" else ");
    Expression _elseExpression = node.getElseExpression();
    _elseExpression.accept(this);
    this.appendToBuffer(" ");
    return false;
  }
  
  private void appendExtraDimensions(final int extraDimensions) {
    for (int i = 0; (i < extraDimensions); i++) {
      this.appendToBuffer("[]");
    }
  }
  
  public boolean visit(final VariableDeclarationStatement it) {
    List _modifiers = it.modifiers();
    Iterable<Annotation> _filter = Iterables.<Annotation>filter(_modifiers, Annotation.class);
    boolean _isEmpty = IterableExtensions.isEmpty(_filter);
    final boolean hasAnnotations = (!_isEmpty);
    if (hasAnnotations) {
      this.appendToBuffer("/*FIXME can not add Annotation to Variable declaration. Java code: ");
    }
    List _modifiers_1 = it.modifiers();
    final Function1<ASTNode, StringBuffer> _function = new Function1<ASTNode, StringBuffer>() {
      public StringBuffer apply(final ASTNode it) {
        StringBuffer _xblockexpression = null;
        {
          if (hasAnnotations) {
            JavaASTFlattener.this.appendToBuffer("*/");
          }
          _xblockexpression = JavaASTFlattener.this.appendLineWrapToBuffer();
        }
        return _xblockexpression;
      }
    };
    this.appendModifieres(it, _modifiers_1, _function);
    List _modifiers_2 = it.modifiers();
    this.handleVariableDeclaration(_modifiers_2);
    Type _type = it.getType();
    _type.accept(this);
    this.appendToBuffer(" ");
    List _fragments = it.fragments();
    this.appendAllSeparatedByComma(_fragments);
    this.appendLineWrapToBuffer();
    return false;
  }
  
  public void appendAllSeparatedByComma(final Iterable<? extends ASTNode> iterable) {
    this.appendAll(iterable, ", ");
  }
  
  public void appendAll(final Iterable<? extends ASTNode> iterable) {
    boolean _isEmpty = IterableExtensions.isEmpty(iterable);
    if (_isEmpty) {
      return;
    } else {
      this.appendAll(iterable, "");
    }
  }
  
  public void appendAll(final Iterable<? extends ASTNode> iterable, final String separator) {
    final Procedure2<ASTNode, Integer> _function = new Procedure2<ASTNode, Integer>() {
      public void apply(final ASTNode it, final Integer counter) {
        it.accept(JavaASTFlattener.this);
        int _size = IterableExtensions.size(iterable);
        int _minus = (_size - 1);
        boolean _lessThan = ((counter).intValue() < _minus);
        if (_lessThan) {
          JavaASTFlattener.this.appendToBuffer(separator);
        }
      }
    };
    IterableExtensions.forEach(iterable, _function);
  }
  
  public StringBuffer appendTypeParameters(final Iterable<TypeParameter> iterable) {
    StringBuffer _xblockexpression = null;
    {
      this.appendToBuffer("<");
      this.appendAllSeparatedByComma(iterable);
      _xblockexpression = this.appendToBuffer(">");
    }
    return _xblockexpression;
  }
  
  public boolean visit(final MethodDeclaration it) {
    Javadoc _javadoc = it.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = it.getJavadoc();
      _javadoc_1.accept(this);
    }
    final Function1<ASTNode, StringBuffer> _function = new Function1<ASTNode, StringBuffer>() {
      public StringBuffer apply(final ASTNode node) {
        StringBuffer _xifexpression = null;
        if ((node instanceof MethodDeclaration)) {
          StringBuffer _xifexpression_1 = null;
          boolean _isOverrideMethode = JavaASTFlattener.this.isOverrideMethode(((MethodDeclaration)node));
          if (_isOverrideMethode) {
            _xifexpression_1 = JavaASTFlattener.this.appendToBuffer("override ");
          } else {
            _xifexpression_1 = JavaASTFlattener.this.appendToBuffer("def ");
          }
          _xifexpression = _xifexpression_1;
        }
        return _xifexpression;
      }
    };
    final Function1<ASTNode, StringBuffer> afterAnnotationProcessingCallback = _function;
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers, afterAnnotationProcessingCallback);
    List _modifiers_1 = it.modifiers();
    Iterable<Modifier> _filter = Iterables.<Modifier>filter(_modifiers_1, Modifier.class);
    boolean _isPackageVisibility = this.isPackageVisibility(_filter);
    if (_isPackageVisibility) {
      ASTNode _parent = it.getParent();
      if ((_parent instanceof TypeDeclaration)) {
        ASTNode _parent_1 = it.getParent();
        boolean _isInterface = ((TypeDeclaration) _parent_1).isInterface();
        boolean _not = (!_isInterface);
        if (_not) {
          this.appendToBuffer("package ");
        }
      }
    }
    List _typeParameters = it.typeParameters();
    boolean _isEmpty = _typeParameters.isEmpty();
    boolean _not_1 = (!_isEmpty);
    if (_not_1) {
      List _typeParameters_1 = it.typeParameters();
      this.appendTypeParameters(_typeParameters_1);
    }
    boolean _isConstructor = it.isConstructor();
    boolean _not_2 = (!_isConstructor);
    if (_not_2) {
      Type _returnType2 = it.getReturnType2();
      boolean _notEquals_1 = (!Objects.equal(_returnType2, null));
      if (_notEquals_1) {
        Type _returnType2_1 = it.getReturnType2();
        _returnType2_1.accept(this);
      } else {
        this.appendToBuffer("void");
      }
      this.appendToBuffer(" ");
    }
    SimpleName _name = it.getName();
    _name.accept(this);
    this.appendToBuffer("(");
    List _parameters = it.parameters();
    this.appendAllSeparatedByComma(_parameters);
    this.appendToBuffer(")");
    int _extraDimensions = it.getExtraDimensions();
    this.appendExtraDimensions(_extraDimensions);
    List _thrownExceptions = it.thrownExceptions();
    boolean _isEmpty_1 = _thrownExceptions.isEmpty();
    boolean _not_3 = (!_isEmpty_1);
    if (_not_3) {
      this.appendToBuffer(" throws ");
      List _thrownExceptions_1 = it.thrownExceptions();
      this.appendAllSeparatedByComma(_thrownExceptions_1);
      this.appendToBuffer(" ");
    }
    Block _body = it.getBody();
    boolean _equals = Objects.equal(_body, null);
    if (_equals) {
      this.appendToBuffer(";");
    } else {
      Block _body_1 = it.getBody();
      _body_1.accept(this);
    }
    return false;
  }
  
  public boolean isPackageVisibility(final Iterable<Modifier> modifier) {
    final Function1<Modifier, Boolean> _function = new Function1<Modifier, Boolean>() {
      public Boolean apply(final Modifier it) {
        boolean _or = false;
        boolean _or_1 = false;
        boolean _isPublic = it.isPublic();
        if (_isPublic) {
          _or_1 = true;
        } else {
          boolean _isPrivate = it.isPrivate();
          _or_1 = _isPrivate;
        }
        if (_or_1) {
          _or = true;
        } else {
          boolean _isProtected = it.isProtected();
          _or = _isProtected;
        }
        return Boolean.valueOf(_or);
      }
    };
    Iterable<Modifier> _filter = IterableExtensions.<Modifier>filter(modifier, _function);
    return IterableExtensions.isEmpty(_filter);
  }
  
  public boolean visit(final SingleVariableDeclaration it) {
    List _modifiers = it.modifiers();
    this.appendModifieres(it, _modifiers);
    Type _type = it.getType();
    _type.accept(this);
    boolean _isVarargs = it.isVarargs();
    if (_isVarargs) {
      this.appendToBuffer("...");
    }
    this.appendToBuffer(" ");
    SimpleName _name = it.getName();
    _name.accept(this);
    int _extraDimensions = it.getExtraDimensions();
    this.appendExtraDimensions(_extraDimensions);
    Expression _initializer = it.getInitializer();
    boolean _notEquals = (!Objects.equal(_initializer, null));
    if (_notEquals) {
      this.appendToBuffer("=");
      Expression _initializer_1 = it.getInitializer();
      _initializer_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final ClassInstanceCreation node) {
    Expression _expression = node.getExpression();
    boolean _notEquals = (!Objects.equal(_expression, null));
    if (_notEquals) {
      Expression _expression_1 = node.getExpression();
      _expression_1.accept(this);
      this.appendToBuffer(".");
    }
    this.appendToBuffer("new ");
    List _typeArguments = node.typeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.appendToBuffer("<");
      for (Iterator<Type> it = node.typeArguments().iterator(); it.hasNext();) {
        {
          Type t = it.next();
          t.accept(this);
          boolean _hasNext = it.hasNext();
          if (_hasNext) {
            this.appendToBuffer(",");
          }
        }
      }
      this.appendToBuffer(">");
    }
    Type _type = node.getType();
    _type.accept(this);
    this.appendToBuffer("(");
    for (Iterator<Expression> it = node.arguments().iterator(); it.hasNext();) {
      {
        Expression e = it.next();
        e.accept(this);
        boolean _hasNext = it.hasNext();
        if (_hasNext) {
          this.appendToBuffer(",");
        }
      }
    }
    this.appendToBuffer(")");
    AnonymousClassDeclaration _anonymousClassDeclaration = node.getAnonymousClassDeclaration();
    boolean _notEquals_1 = (!Objects.equal(_anonymousClassDeclaration, null));
    if (_notEquals_1) {
      AnonymousClassDeclaration _anonymousClassDeclaration_1 = node.getAnonymousClassDeclaration();
      _anonymousClassDeclaration_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final Block it) {
    this.appendToBuffer("{");
    this.appendLineWrapToBuffer();
    List _statements = it.statements();
    this.appendAll(_statements);
    this.appendLineWrapToBuffer();
    this.appendToBuffer("}");
    return false;
  }
  
  public boolean visit(final CastExpression node) {
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(" as ");
    Type _type = node.getType();
    _type.accept(this);
    return false;
  }
  
  public boolean visit(final ExpressionStatement it) {
    Expression _expression = it.getExpression();
    _expression.accept(this);
    this.appendSpaceToBuffer();
    return false;
  }
  
  public boolean visit(final MethodInvocation it) {
    Expression _expression = it.getExpression();
    boolean _notEquals = (!Objects.equal(_expression, null));
    if (_notEquals) {
      Expression _expression_1 = it.getExpression();
      _expression_1.accept(this);
      this.appendToBuffer(".");
    }
    List _typeArguments = it.typeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      List _typeArguments_1 = it.typeArguments();
      this.appendTypeParameters(_typeArguments_1);
    }
    SimpleName _name = it.getName();
    _name.accept(this);
    this.appendToBuffer("(");
    List _arguments = it.arguments();
    this.appendAllSeparatedByComma(_arguments);
    this.appendToBuffer(")");
    return false;
  }
  
  public boolean visit(final ForStatement it) {
    this.appendToBuffer("for (");
    List _initializers = it.initializers();
    this.appendAll(_initializers, "");
    this.appendToBuffer("; ");
    Expression _expression = it.getExpression();
    boolean _notEquals = (!Objects.equal(_expression, null));
    if (_notEquals) {
      Expression _expression_1 = it.getExpression();
      _expression_1.accept(this);
    }
    this.appendToBuffer("; ");
    List _updaters = it.updaters();
    this.appendAll(_updaters);
    this.appendToBuffer(") ");
    Statement _body = it.getBody();
    _body.accept(this);
    return false;
  }
  
  public boolean visit(final NullLiteral it) {
    this.appendToBuffer("null");
    return false;
  }
  
  public boolean visit(final ThisExpression it) {
    Name _qualifier = it.getQualifier();
    boolean _notEquals = (!Objects.equal(_qualifier, null));
    if (_notEquals) {
      Name _qualifier_1 = it.getQualifier();
      _qualifier_1.accept(this);
      this.appendToBuffer(".");
    }
    this.appendToBuffer("this");
    return false;
  }
  
  public boolean visit(final IfStatement node) {
    this.appendToBuffer("if (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(") ");
    Statement _thenStatement = node.getThenStatement();
    _thenStatement.accept(this);
    node.getElseStatement();
    Statement _elseStatement = node.getElseStatement();
    boolean _notEquals = (!Objects.equal(_elseStatement, null));
    if (_notEquals) {
      this.appendToBuffer(" else ");
      Statement _elseStatement_1 = node.getElseStatement();
      _elseStatement_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final FieldAccess it) {
    Expression _expression = it.getExpression();
    _expression.accept(this);
    this.appendToBuffer(".");
    SimpleName _name = it.getName();
    _name.accept(this);
    return false;
  }
  
  public boolean visit(final InfixExpression node) {
    Expression _leftOperand = node.getLeftOperand();
    _leftOperand.accept(this);
    this.appendToBuffer(" ");
    InfixExpression.Operator _operator = node.getOperator();
    String _string = _operator.toString();
    this.appendToBuffer(_string);
    this.appendToBuffer(" ");
    Expression _rightOperand = node.getRightOperand();
    _rightOperand.accept(this);
    final List extendedOperands = node.extendedOperands();
    int _size = extendedOperands.size();
    boolean _notEquals = (_size != 0);
    if (_notEquals) {
      this.appendToBuffer(" ");
      for (Iterator<Expression> it = extendedOperands.iterator(); it.hasNext();) {
        {
          InfixExpression.Operator _operator_1 = node.getOperator();
          String _string_1 = _operator_1.toString();
          StringBuffer _appendToBuffer = this.appendToBuffer(_string_1);
          _appendToBuffer.append(" ");
          Expression e = it.next();
          e.accept(this);
        }
      }
    }
    return false;
  }
  
  public boolean visit(final InstanceofExpression node) {
    Expression _leftOperand = node.getLeftOperand();
    _leftOperand.accept(this);
    this.appendToBuffer(" instanceof ");
    Type _rightOperand = node.getRightOperand();
    _rightOperand.accept(this);
    return false;
  }
  
  public boolean visit(final ReturnStatement node) {
    this.appendToBuffer("return");
    Expression _expression = node.getExpression();
    boolean _notEquals = (!Objects.equal(_expression, null));
    if (_notEquals) {
      this.appendToBuffer(" ");
      Expression _expression_1 = node.getExpression();
      _expression_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final BlockComment node) {
    this.appendToBuffer("/* */");
    return false;
  }
  
  public boolean visit(final LineComment node) {
    this.appendToBuffer("//\n");
    return false;
  }
  
  public boolean visit(final ParenthesizedExpression node) {
    this.appendToBuffer("(");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(")");
    return false;
  }
  
  public boolean visit(final PostfixExpression node) {
    Expression _operand = node.getOperand();
    _operand.accept(this);
    PostfixExpression.Operator _operator = node.getOperator();
    String _string = _operator.toString();
    this.appendToBuffer(_string);
    return false;
  }
  
  public boolean visit(final PrefixExpression node) {
    PrefixExpression.Operator _operator = node.getOperator();
    String _string = _operator.toString();
    this.appendToBuffer(_string);
    Expression _operand = node.getOperand();
    _operand.accept(this);
    return false;
  }
  
  public boolean visit(final SingleMemberAnnotation node) {
    this.appendToBuffer("@");
    Name _typeName = node.getTypeName();
    _typeName.accept(this);
    this.appendToBuffer("(");
    Expression _value = node.getValue();
    _value.accept(this);
    this.appendToBuffer(")");
    return false;
  }
  
  @Override
  public boolean visit(final SuperConstructorInvocation node) {
    Expression _expression = node.getExpression();
    boolean _notEquals = (!Objects.equal(_expression, null));
    if (_notEquals) {
      Expression _expression_1 = node.getExpression();
      _expression_1.accept(this);
      this.fBuffer.append(".");
    }
    List _typeArguments = node.typeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.fBuffer.append("<");
      for (Iterator<Type> it = node.typeArguments().iterator(); it.hasNext();) {
        {
          Type t = it.next();
          t.accept(this);
          boolean _hasNext = it.hasNext();
          if (_hasNext) {
            this.fBuffer.append(",");
          }
        }
      }
      this.fBuffer.append(">");
    }
    this.fBuffer.append("super(");
    for (Iterator<Expression> it = node.arguments().iterator(); it.hasNext();) {
      {
        Expression e = it.next();
        e.accept(this);
        boolean _hasNext = it.hasNext();
        if (_hasNext) {
          this.fBuffer.append(",");
        }
      }
    }
    this.fBuffer.append(");");
    return false;
  }
  
  @Override
  public boolean visit(final SuperFieldAccess node) {
    Name _qualifier = node.getQualifier();
    boolean _notEquals = (!Objects.equal(_qualifier, null));
    if (_notEquals) {
      Name _qualifier_1 = node.getQualifier();
      _qualifier_1.accept(this);
      this.fBuffer.append(".");
    }
    this.fBuffer.append("super.");
    SimpleName _name = node.getName();
    _name.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final SuperMethodInvocation node) {
    Name _qualifier = node.getQualifier();
    boolean _notEquals = (!Objects.equal(_qualifier, null));
    if (_notEquals) {
      Name _qualifier_1 = node.getQualifier();
      _qualifier_1.accept(this);
      this.fBuffer.append(".");
    }
    this.fBuffer.append("super.");
    List _typeArguments = node.typeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.fBuffer.append("<");
      for (Iterator<Type> it = node.typeArguments().iterator(); it.hasNext();) {
        {
          Type t = it.next();
          t.accept(this);
          boolean _hasNext = it.hasNext();
          if (_hasNext) {
            this.fBuffer.append(",");
          }
        }
      }
      this.fBuffer.append(">");
    }
    SimpleName _name = node.getName();
    _name.accept(this);
    this.fBuffer.append("(");
    for (Iterator<Expression> it = node.arguments().iterator(); it.hasNext();) {
      {
        Expression e = it.next();
        e.accept(this);
        boolean _hasNext = it.hasNext();
        if (_hasNext) {
          this.fBuffer.append(",");
        }
      }
    }
    this.fBuffer.append(")");
    return false;
  }
  
  protected boolean canHandleAnnotation(final ASTNode node) {
    if ((node instanceof VariableDeclarationStatement)) {
      return false;
    }
    return true;
  }
  
  public boolean visit(final TagElement node) {
    boolean _isNested = node.isNested();
    if (_isNested) {
      this.appendToBuffer("{");
    } else {
      this.appendToBuffer("\n * ");
    }
    boolean previousRequiresWhiteSpace = false;
    String _tagName = node.getTagName();
    boolean _notEquals = (!Objects.equal(_tagName, null));
    if (_notEquals) {
      String _tagName_1 = node.getTagName();
      this.appendToBuffer(_tagName_1);
      previousRequiresWhiteSpace = true;
    }
    boolean previousRequiresNewLine = false;
    for (Iterator<? extends ASTNode> it = node.fragments().iterator(); it.hasNext();) {
      {
        ASTNode e = it.next();
        boolean currentIncludesWhiteSpace = (e instanceof TextElement);
        if ((previousRequiresNewLine && currentIncludesWhiteSpace)) {
          this.appendToBuffer("\n * ");
        }
        previousRequiresNewLine = currentIncludesWhiteSpace;
        if ((previousRequiresWhiteSpace && currentIncludesWhiteSpace)) {
          this.appendToBuffer(" ");
        }
        e.accept(this);
        previousRequiresWhiteSpace = (currentIncludesWhiteSpace && (e instanceof TagElement));
      }
    }
    boolean _isNested_1 = node.isNested();
    if (_isNested_1) {
      this.appendToBuffer("}");
    }
    return false;
  }
  
  public boolean visit(final TextElement node) {
    String _text = node.getText();
    this.appendToBuffer(_text);
    return false;
  }
  
  public boolean visit(final TypeLiteral node) {
    Type _type = node.getType();
    _type.accept(this);
    this.appendToBuffer(".class");
    return false;
  }
  
  public boolean visit(final ThrowStatement node) {
    this.appendToBuffer("throw ");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(";");
    return false;
  }
  
  public boolean visit(final TryStatement node) {
    this.appendToBuffer("try ");
    Block _body = node.getBody();
    _body.accept(this);
    this.appendToBuffer(" ");
    for (Iterator<CatchClause> _it = node.catchClauses().iterator(); _it.hasNext();) {
      {
        CatchClause cc = _it.next();
        cc.accept(this);
      }
    }
    Block _finally = node.getFinally();
    boolean _notEquals = (!Objects.equal(_finally, null));
    if (_notEquals) {
      this.appendToBuffer("finally ");
      Block _finally_1 = node.getFinally();
      _finally_1.accept(this);
    }
    return false;
  }
  
  public boolean visit(final TypeParameter node) {
    SimpleName _name = node.getName();
    _name.accept(this);
    List _typeBounds = node.typeBounds();
    boolean _isEmpty = _typeBounds.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.appendToBuffer(" extends ");
      for (Iterator<Type> _it = node.typeBounds().iterator(); _it.hasNext();) {
        {
          Type t = _it.next();
          t.accept(this);
          boolean _hasNext = _it.hasNext();
          if (_hasNext) {
            this.appendToBuffer(" & ");
          }
        }
      }
    }
    return false;
  }
  
  public boolean visit(final NumberLiteral node) {
    String _token = node.getToken();
    this.appendToBuffer(_token);
    return false;
  }
  
  public boolean visit(final NormalAnnotation node) {
    this.appendToBuffer("@");
    Name _typeName = node.getTypeName();
    _typeName.accept(this);
    this.appendToBuffer("(");
    for (Iterator<MemberValuePair> it = node.values().iterator(); it.hasNext();) {
      {
        MemberValuePair p = it.next();
        p.accept(this);
        boolean _hasNext = it.hasNext();
        if (_hasNext) {
          this.appendToBuffer(",");
        }
      }
    }
    this.appendToBuffer(")");
    return false;
  }
  
  public boolean visit(final CharacterLiteral node) {
    String _escapedValue = node.getEscapedValue();
    this.appendToBuffer(_escapedValue);
    return false;
  }
  
  public boolean visit(final WildcardType node) {
    this.appendToBuffer("?");
    Type bound = node.getBound();
    boolean _notEquals = (!Objects.equal(bound, null));
    if (_notEquals) {
      boolean _isUpperBound = node.isUpperBound();
      if (_isUpperBound) {
        this.appendToBuffer(" extends ");
      } else {
        this.appendToBuffer(" super ");
      }
      bound.accept(this);
    }
    return false;
  }
  
  public boolean visit(final MemberValuePair node) {
    SimpleName _name = node.getName();
    _name.accept(this);
    this.appendToBuffer("=");
    Expression _value = node.getValue();
    _value.accept(this);
    return false;
  }
  
  public boolean visit(final WhileStatement node) {
    this.appendToBuffer("while (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.appendToBuffer(") ");
    Statement _body = node.getBody();
    _body.accept(this);
    return false;
  }
  
  /**
   * Start self Converted part
   */
  @Override
  public boolean visit(final AnnotationTypeDeclaration node) {
    Javadoc _javadoc = node.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = node.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = node.modifiers();
    this.appendModifieres(node, _modifiers);
    this.fBuffer.append("@interface ");
    SimpleName _name = node.getName();
    _name.accept(this);
    this.fBuffer.append(" {");
    List _bodyDeclarations = node.bodyDeclarations();
    this.appendAll(_bodyDeclarations);
    this.fBuffer.append("}");
    return false;
  }
  
  @Override
  public boolean visit(final AnnotationTypeMemberDeclaration node) {
    Javadoc _javadoc = node.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = node.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = node.modifiers();
    this.appendModifieres(node, _modifiers);
    Type _type = node.getType();
    _type.accept(this);
    this.fBuffer.append(" ");
    SimpleName _name = node.getName();
    _name.accept(this);
    this.fBuffer.append("()");
    Expression _default = node.getDefault();
    boolean _notEquals_1 = (!Objects.equal(_default, null));
    if (_notEquals_1) {
      this.fBuffer.append(" default ");
      Expression _default_1 = node.getDefault();
      _default_1.accept(this);
    }
    this.fBuffer.append(";");
    return false;
  }
  
  @Override
  public boolean visit(final AnonymousClassDeclaration node) {
    this.fBuffer.append("{");
    List _bodyDeclarations = node.bodyDeclarations();
    this.appendAll(_bodyDeclarations);
    this.fBuffer.append("}");
    return false;
  }
  
  @Override
  public boolean visit(final ArrayAccess node) {
    Expression _array = node.getArray();
    _array.accept(this);
    this.fBuffer.append("[");
    Expression _index = node.getIndex();
    _index.accept(this);
    this.fBuffer.append("]");
    return false;
  }
  
  @Override
  public boolean visit(final ArrayCreation node) {
    this.fBuffer.append("new ");
    ArrayType at = node.getType();
    int dims = at.getDimensions();
    Type elementType = at.getElementType();
    elementType.accept(this);
    for (Iterator<Expression> it = node.dimensions().iterator(); it.hasNext();) {
      {
        this.fBuffer.append("[");
        Expression e = it.next();
        e.accept(this);
        this.fBuffer.append("]");
        dims--;
      }
    }
    for (int i = 0; (i < dims); i++) {
      this.fBuffer.append("[]");
    }
    ArrayInitializer _initializer = node.getInitializer();
    boolean _notEquals = (!Objects.equal(_initializer, null));
    if (_notEquals) {
      ArrayInitializer _initializer_1 = node.getInitializer();
      _initializer_1.accept(this);
    }
    return false;
  }
  
  @Override
  public boolean visit(final ArrayInitializer node) {
    this.fBuffer.append("{");
    List _expressions = node.expressions();
    this.appendAllSeparatedByComma(_expressions);
    this.fBuffer.append("}");
    return false;
  }
  
  @Override
  public boolean visit(final ArrayType node) {
    Type _componentType = node.getComponentType();
    _componentType.accept(this);
    this.fBuffer.append("[]");
    return false;
  }
  
  @Override
  public boolean visit(final AssertStatement node) {
    this.fBuffer.append("assert ");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    Expression _message = node.getMessage();
    boolean _notEquals = (!Objects.equal(_message, null));
    if (_notEquals) {
      this.fBuffer.append(" : ");
      Expression _message_1 = node.getMessage();
      _message_1.accept(this);
    }
    this.appendLineWrapToBuffer();
    return false;
  }
  
  @Override
  public boolean visit(final BreakStatement node) {
    this.fBuffer.append("/* FIXME unsupported BreakStatement");
    this.appendLineWrapToBuffer();
    SimpleName _label = node.getLabel();
    boolean _notEquals = (!Objects.equal(_label, null));
    if (_notEquals) {
      this.fBuffer.append(" ");
      SimpleName _label_1 = node.getLabel();
      _label_1.accept(this);
    }
    this.appendToBuffer("*/");
    return false;
  }
  
  @Override
  public boolean visit(final CatchClause node) {
    this.fBuffer.append("catch (");
    SingleVariableDeclaration _exception = node.getException();
    _exception.accept(this);
    this.fBuffer.append(") ");
    Block _body = node.getBody();
    _body.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final ConstructorInvocation node) {
    List _typeArguments = node.typeArguments();
    boolean _isEmpty = _typeArguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.fBuffer.append("<");
      List _typeArguments_1 = node.typeArguments();
      this.appendTypeParameters(_typeArguments_1);
      this.fBuffer.append(">");
    }
    this.fBuffer.append("this(");
    List _arguments = node.arguments();
    this.appendAllSeparatedByComma(_arguments);
    this.fBuffer.append(")");
    return false;
  }
  
  @Override
  public boolean visit(final ContinueStatement node) {
    this.fBuffer.append("/* FIXME Unsupported continue statement");
    this.appendLineWrapToBuffer();
    SimpleName _label = node.getLabel();
    boolean _notEquals = (!Objects.equal(_label, null));
    if (_notEquals) {
      this.fBuffer.append(" ");
      SimpleName _label_1 = node.getLabel();
      _label_1.accept(this);
    }
    this.fBuffer.append(";");
    this.appendToBuffer("*/");
    return false;
  }
  
  @Override
  public boolean visit(final DoStatement node) {
    this.fBuffer.append("do ");
    Statement _body = node.getBody();
    _body.accept(this);
    this.fBuffer.append(" while (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.fBuffer.append(")");
    return false;
  }
  
  @Override
  public boolean visit(final EmptyStatement node) {
    this.appendSpaceToBuffer();
    return false;
  }
  
  @Override
  public boolean visit(final EnhancedForStatement node) {
    this.fBuffer.append("for (");
    SingleVariableDeclaration _parameter = node.getParameter();
    _parameter.accept(this);
    this.fBuffer.append(" : ");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.fBuffer.append(") ");
    Statement _body = node.getBody();
    _body.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final EnumConstantDeclaration node) {
    Javadoc _javadoc = node.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = node.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = node.modifiers();
    this.appendModifieres(node, _modifiers);
    SimpleName _name = node.getName();
    _name.accept(this);
    List _arguments = node.arguments();
    boolean _isEmpty = _arguments.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.fBuffer.append("(");
      List _arguments_1 = node.arguments();
      this.appendAllSeparatedByComma(_arguments_1);
      this.fBuffer.append(")");
    }
    AnonymousClassDeclaration _anonymousClassDeclaration = node.getAnonymousClassDeclaration();
    boolean _notEquals_1 = (!Objects.equal(_anonymousClassDeclaration, null));
    if (_notEquals_1) {
      AnonymousClassDeclaration _anonymousClassDeclaration_1 = node.getAnonymousClassDeclaration();
      _anonymousClassDeclaration_1.accept(this);
    }
    return false;
  }
  
  @Override
  public boolean visit(final EnumDeclaration node) {
    Javadoc _javadoc = node.getJavadoc();
    boolean _notEquals = (!Objects.equal(_javadoc, null));
    if (_notEquals) {
      Javadoc _javadoc_1 = node.getJavadoc();
      _javadoc_1.accept(this);
    }
    List _modifiers = node.modifiers();
    this.appendModifieres(node, _modifiers);
    this.fBuffer.append("enum ");
    SimpleName _name = node.getName();
    _name.accept(this);
    this.fBuffer.append(" ");
    List _superInterfaceTypes = node.superInterfaceTypes();
    boolean _isEmpty = _superInterfaceTypes.isEmpty();
    boolean _not = (!_isEmpty);
    if (_not) {
      this.fBuffer.append("implements ");
      List _superInterfaceTypes_1 = node.superInterfaceTypes();
      this.appendAllSeparatedByComma(_superInterfaceTypes_1);
      this.fBuffer.append(" ");
    }
    this.fBuffer.append("{");
    List _enumConstants = node.enumConstants();
    this.appendAllSeparatedByComma(_enumConstants);
    List _bodyDeclarations = node.bodyDeclarations();
    boolean _isEmpty_1 = _bodyDeclarations.isEmpty();
    boolean _not_1 = (!_isEmpty_1);
    if (_not_1) {
      this.fBuffer.append("; ");
      List _bodyDeclarations_1 = node.bodyDeclarations();
      this.appendAll(_bodyDeclarations_1);
    }
    this.fBuffer.append("}");
    return false;
  }
  
  @Override
  public boolean visit(final LabeledStatement node) {
    SimpleName _label = node.getLabel();
    _label.accept(this);
    this.fBuffer.append(": ");
    Statement _body = node.getBody();
    _body.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final MemberRef node) {
    Name _qualifier = node.getQualifier();
    boolean _notEquals = (!Objects.equal(_qualifier, null));
    if (_notEquals) {
      Name _qualifier_1 = node.getQualifier();
      _qualifier_1.accept(this);
    }
    this.fBuffer.append("#");
    SimpleName _name = node.getName();
    _name.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final MethodRef node) {
    Name _qualifier = node.getQualifier();
    boolean _notEquals = (!Objects.equal(_qualifier, null));
    if (_notEquals) {
      Name _qualifier_1 = node.getQualifier();
      _qualifier_1.accept(this);
    }
    this.fBuffer.append("#");
    SimpleName _name = node.getName();
    _name.accept(this);
    this.fBuffer.append("(");
    List _parameters = node.parameters();
    this.appendAllSeparatedByComma(_parameters);
    this.fBuffer.append(")");
    return false;
  }
  
  @Override
  public boolean visit(final MethodRefParameter node) {
    Type _type = node.getType();
    _type.accept(this);
    boolean _isVarargs = node.isVarargs();
    if (_isVarargs) {
      this.fBuffer.append("...");
    }
    SimpleName _name = node.getName();
    boolean _notEquals = (!Objects.equal(_name, null));
    if (_notEquals) {
      this.fBuffer.append(" ");
      SimpleName _name_1 = node.getName();
      _name_1.accept(this);
    }
    return false;
  }
  
  @Override
  public boolean visit(final QualifiedType node) {
    Type _qualifier = node.getQualifier();
    _qualifier.accept(this);
    this.fBuffer.append(".");
    SimpleName _name = node.getName();
    _name.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final SwitchCase node) {
    boolean _isDefault = node.isDefault();
    if (_isDefault) {
      this.fBuffer.append("default :");
    } else {
      this.fBuffer.append("case ");
      Expression _expression = node.getExpression();
      _expression.accept(this);
      this.fBuffer.append(":");
    }
    return false;
  }
  
  @Override
  public boolean visit(final SwitchStatement node) {
    this.fBuffer.append("switch (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.fBuffer.append(") ");
    this.fBuffer.append("{");
    List _statements = node.statements();
    this.appendAll(_statements);
    this.fBuffer.append("}");
    return false;
  }
  
  @Override
  public boolean visit(final SynchronizedStatement node) {
    this.fBuffer.append("synchronized (");
    Expression _expression = node.getExpression();
    _expression.accept(this);
    this.fBuffer.append(") ");
    Block _body = node.getBody();
    _body.accept(this);
    return false;
  }
  
  @Override
  public boolean visit(final TypeDeclarationStatement node) {
    AbstractTypeDeclaration _declaration = node.getDeclaration();
    _declaration.accept(this);
    return false;
  }
}
