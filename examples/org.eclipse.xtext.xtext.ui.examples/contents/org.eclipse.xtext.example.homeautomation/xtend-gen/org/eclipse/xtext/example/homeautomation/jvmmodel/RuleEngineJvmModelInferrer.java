/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.example.homeautomation.jvmmodel;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
import org.eclipse.xtext.common.types.JvmEnumerationType;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.example.homeautomation.ruleEngine.Declaration;
import org.eclipse.xtext.example.homeautomation.ruleEngine.Device;
import org.eclipse.xtext.example.homeautomation.ruleEngine.Model;
import org.eclipse.xtext.example.homeautomation.ruleEngine.Rule;
import org.eclipse.xtext.example.homeautomation.ruleEngine.State;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * <p>Infers a JVM model from the source model.</p>
 * 
 * <p>The JVM model should contain all elements that would appear in the Java code
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>
 */
@SuppressWarnings("all")
public class RuleEngineJvmModelInferrer extends AbstractModelInferrer {
  /**
   * convenience API to build and initialize JVM types and their members.
   */
  @Inject
  @Extension
  private JvmTypesBuilder _jvmTypesBuilder;
  
  protected void _infer(final Model element, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPreIndexingPhase) {
    Resource _eResource = element.eResource();
    URI _uRI = _eResource.getURI();
    URI _trimFileExtension = _uRI.trimFileExtension();
    final String className = _trimFileExtension.lastSegment();
    JvmGenericType _class = this._jvmTypesBuilder.toClass(element, className);
    final Procedure1<JvmGenericType> _function = new Procedure1<JvmGenericType>() {
      @Override
      public void apply(final JvmGenericType it) {
        EList<Declaration> _declarations = element.getDeclarations();
        Iterable<Rule> _filter = Iterables.<Rule>filter(_declarations, Rule.class);
        for (final Rule device : _filter) {
          EList<JvmMember> _members = it.getMembers();
          String _ruleMethodName = RuleEngineJvmModelInferrer.this.ruleMethodName(device);
          JvmTypeReference _typeRef = RuleEngineJvmModelInferrer.this._typeReferenceBuilder.typeRef(void.class);
          final Procedure1<JvmOperation> _function = new Procedure1<JvmOperation>() {
            @Override
            public void apply(final JvmOperation it) {
              it.setStatic(true);
              XExpression _thenPart = device.getThenPart();
              RuleEngineJvmModelInferrer.this._jvmTypesBuilder.setBody(it, _thenPart);
            }
          };
          JvmOperation _method = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toMethod(device, _ruleMethodName, _typeRef, _function);
          RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmOperation>operator_add(_members, _method);
        }
        EList<JvmMember> _members_1 = it.getMembers();
        JvmTypeReference _typeRef_1 = RuleEngineJvmModelInferrer.this._typeReferenceBuilder.typeRef(void.class);
        final Procedure1<JvmOperation> _function_1 = new Procedure1<JvmOperation>() {
          @Override
          public void apply(final JvmOperation it) {
            it.setStatic(true);
            EList<JvmFormalParameter> _parameters = it.getParameters();
            JvmTypeReference _typeRef = RuleEngineJvmModelInferrer.this._typeReferenceBuilder.typeRef(Object.class);
            JvmFormalParameter _parameter = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toParameter(element, "event", _typeRef);
            RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmFormalParameter>operator_add(_parameters, _parameter);
            StringConcatenationClient _client = new StringConcatenationClient() {
              @Override
              protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
                {
                  EList<Declaration> _declarations = element.getDeclarations();
                  Iterable<Device> _filter = Iterables.<Device>filter(_declarations, Device.class);
                  for(final Device device : _filter) {
                    {
                      EList<State> _states = device.getStates();
                      for(final State state : _states) {
                        _builder.append("if (event == ");
                        String _qualifiedJavaName = RuleEngineJvmModelInferrer.this.getQualifiedJavaName(state);
                        _builder.append(_qualifiedJavaName, "");
                        _builder.append(") {");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t");
                        _builder.append("System.out.println(\"");
                        String _name = device.getName();
                        _builder.append(_name, "\t");
                        _builder.append(" is now ");
                        String _name_1 = state.getName();
                        _builder.append(_name_1, "\t");
                        _builder.append("!\");");
                        _builder.newLineIfNotEmpty();
                        _builder.append("}");
                        _builder.newLine();
                      }
                    }
                  }
                }
                {
                  EList<Declaration> _declarations_1 = element.getDeclarations();
                  Iterable<Rule> _filter_1 = Iterables.<Rule>filter(_declarations_1, Rule.class);
                  for(final Rule rule : _filter_1) {
                    _builder.append("if (event == ");
                    State _deviceState = rule.getDeviceState();
                    String _qualifiedJavaName_1 = RuleEngineJvmModelInferrer.this.getQualifiedJavaName(_deviceState);
                    _builder.append(_qualifiedJavaName_1, "");
                    _builder.append(") {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    String _ruleMethodName = RuleEngineJvmModelInferrer.this.ruleMethodName(rule);
                    _builder.append(_ruleMethodName, "\t");
                    _builder.append("();");
                    _builder.newLineIfNotEmpty();
                    _builder.append("}");
                    _builder.newLine();
                  }
                }
              }
            };
            RuleEngineJvmModelInferrer.this._jvmTypesBuilder.setBody(it, _client);
          }
        };
        JvmOperation _method_1 = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toMethod(element, "fire", _typeRef_1, _function_1);
        RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmOperation>operator_add(_members_1, _method_1);
        EList<JvmMember> _members_2 = it.getMembers();
        JvmTypeReference _typeRef_2 = RuleEngineJvmModelInferrer.this._typeReferenceBuilder.typeRef(void.class);
        final Procedure1<JvmOperation> _function_2 = new Procedure1<JvmOperation>() {
          @Override
          public void apply(final JvmOperation it) {
            it.setStatic(true);
            EList<JvmFormalParameter> _parameters = it.getParameters();
            JvmTypeReference _typeRef = RuleEngineJvmModelInferrer.this._typeReferenceBuilder.typeRef(String.class);
            JvmTypeReference _addArrayTypeDimension = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.addArrayTypeDimension(_typeRef);
            JvmFormalParameter _parameter = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toParameter(element, "args", _addArrayTypeDimension);
            RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmFormalParameter>operator_add(_parameters, _parameter);
            StringConcatenationClient _client = new StringConcatenationClient() {
              @Override
              protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
                _builder.append(Scanner.class, "");
                _builder.append(" scanner = new Scanner(System.in);");
                _builder.newLineIfNotEmpty();
                _builder.append("System.out.println(\"Welcome home!\");");
                _builder.newLine();
                _builder.append("System.out.println(\"Available commands : \");");
                _builder.newLine();
                {
                  EList<Declaration> _declarations = element.getDeclarations();
                  Iterable<Device> _filter = Iterables.<Device>filter(_declarations, Device.class);
                  for(final Device device : _filter) {
                    {
                      EList<State> _states = device.getStates();
                      for(final State state : _states) {
                        _builder.append("System.out.println(\"  ");
                        String _name = device.getName();
                        _builder.append(_name, "");
                        _builder.append(" ");
                        String _name_1 = state.getName();
                        _builder.append(_name_1, "");
                        _builder.append("\" );");
                        _builder.newLineIfNotEmpty();
                      }
                    }
                  }
                }
                _builder.append("System.out.println(\"Have fun!\");");
                _builder.newLine();
                _builder.append("while(true) {");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("String command = scanner.next();");
                _builder.newLine();
                {
                  EList<Declaration> _declarations_1 = element.getDeclarations();
                  Iterable<Device> _filter_1 = Iterables.<Device>filter(_declarations_1, Device.class);
                  for(final Device device_1 : _filter_1) {
                    _builder.append("\t");
                    _builder.append("if (command.equalsIgnoreCase(\"");
                    String _name_2 = device_1.getName();
                    _builder.append(_name_2, "\t");
                    _builder.append("\")) {");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    _builder.append("\t");
                    _builder.append("String secondaryCommand = scanner.next();");
                    _builder.newLine();
                    {
                      EList<State> _states_1 = device_1.getStates();
                      for(final State state_1 : _states_1) {
                        _builder.append("\t");
                        _builder.append("\t");
                        _builder.append("if (secondaryCommand.equalsIgnoreCase(\"");
                        String _name_3 = state_1.getName();
                        _builder.append(_name_3, "\t\t");
                        _builder.append("\")) {");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t");
                        _builder.append("\t");
                        _builder.append("\t");
                        _builder.append("fire(");
                        String _qualifiedJavaName = RuleEngineJvmModelInferrer.this.getQualifiedJavaName(state_1);
                        _builder.append(_qualifiedJavaName, "\t\t\t");
                        _builder.append(");");
                        _builder.newLineIfNotEmpty();
                        _builder.append("\t");
                        _builder.append("\t");
                        _builder.append("} else ");
                        _builder.newLine();
                      }
                    }
                    _builder.append("\t");
                    _builder.append("\t");
                    _builder.append("{");
                    _builder.newLine();
                    _builder.append("\t");
                    _builder.append("\t\t");
                    _builder.append("System.out.println(\"");
                    String _name_4 = device_1.getName();
                    _builder.append(_name_4, "\t\t\t");
                    _builder.append(" can only have the following states: ");
                    EList<State> _states_2 = device_1.getStates();
                    final Function1<State, String> _function = new Function1<State, String>() {
                      @Override
                      public String apply(final State it) {
                        return it.getName();
                      }
                    };
                    List<String> _map = ListExtensions.<State, String>map(_states_2, _function);
                    String _join = IterableExtensions.join(_map, ",");
                    _builder.append(_join, "\t\t\t");
                    _builder.append(".\");");
                    _builder.newLineIfNotEmpty();
                    _builder.append("\t");
                    _builder.append("\t");
                    _builder.append("}");
                    _builder.newLine();
                    _builder.append("\t");
                    _builder.append("}");
                    _builder.newLine();
                  }
                }
                _builder.append("\t");
                _builder.append("if (command.equalsIgnoreCase(\"bye\")) {");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("System.out.println(\"Ciao!\");");
                _builder.newLine();
                _builder.append("\t\t");
                _builder.append("break;");
                _builder.newLine();
                _builder.append("\t");
                _builder.append("}");
                _builder.newLine();
                _builder.append("}");
                _builder.newLine();
              }
            };
            RuleEngineJvmModelInferrer.this._jvmTypesBuilder.setBody(it, _client);
          }
        };
        JvmOperation _method_2 = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toMethod(element, "main", _typeRef_2, _function_2);
        RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmOperation>operator_add(_members_2, _method_2);
      }
    };
    acceptor.<JvmGenericType>accept(_class, _function);
    EList<Declaration> _declarations = element.getDeclarations();
    Iterable<Device> _filter = Iterables.<Device>filter(_declarations, Device.class);
    for (final Device device : _filter) {
      String _name = device.getName();
      final Procedure1<JvmEnumerationType> _function_1 = new Procedure1<JvmEnumerationType>() {
        @Override
        public void apply(final JvmEnumerationType it) {
        }
      };
      JvmEnumerationType _enumerationType = this._jvmTypesBuilder.toEnumerationType(device, _name, _function_1);
      final Procedure1<JvmEnumerationType> _function_2 = new Procedure1<JvmEnumerationType>() {
        @Override
        public void apply(final JvmEnumerationType it) {
          EList<State> _states = device.getStates();
          for (final State state : _states) {
            EList<JvmMember> _members = it.getMembers();
            String _name = state.getName();
            final Procedure1<JvmEnumerationLiteral> _function = new Procedure1<JvmEnumerationLiteral>() {
              @Override
              public void apply(final JvmEnumerationLiteral it) {
                it.setVisibility(JvmVisibility.PUBLIC);
              }
            };
            JvmEnumerationLiteral _enumerationLiteral = RuleEngineJvmModelInferrer.this._jvmTypesBuilder.toEnumerationLiteral(state, _name, _function);
            RuleEngineJvmModelInferrer.this._jvmTypesBuilder.<JvmEnumerationLiteral>operator_add(_members, _enumerationLiteral);
          }
        }
      };
      acceptor.<JvmEnumerationType>accept(_enumerationType, _function_2);
    }
  }
  
  private String ruleMethodName(final Rule device) {
    String _description = device.getDescription();
    String _replaceAll = _description.replaceAll("\\W", "_");
    return ("execute" + _replaceAll);
  }
  
  private String getQualifiedJavaName(final State state) {
    EObject _eContainer = state.eContainer();
    String _name = ((Device) _eContainer).getName();
    String _plus = (_name + ".");
    String _name_1 = state.getName();
    return (_plus + _name_1);
  }
  
  public void infer(final EObject element, final IJvmDeclaredTypeAcceptor acceptor, final boolean isPreIndexingPhase) {
    if (element instanceof Model) {
      _infer((Model)element, acceptor, isPreIndexingPhase);
      return;
    } else if (element != null) {
      _infer(element, acceptor, isPreIndexingPhase);
      return;
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(element, acceptor, isPreIndexingPhase).toString());
    }
  }
}
