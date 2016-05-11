/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.macro;

import com.google.common.base.Objects;
import org.eclipse.xtend.lib.macro.AbstractClassProcessor;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.AnnotationReference;
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration;
import org.eclipse.xtend.lib.macro.declaration.MutableMethodDeclaration;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class Bug446364Processor extends AbstractClassProcessor {
  @Override
  public void doTransform(final MutableClassDeclaration annotatedClass, @Extension final TransformationContext context) {
    Iterable<? extends AnnotationReference> _annotations = annotatedClass.getAnnotations();
    AnnotationReference _head = IterableExtensions.head(_annotations);
    String _stringValue = _head.getStringValue("value");
    boolean _matched = false;
    if (Objects.equal(_stringValue, "rename")) {
      _matched=true;
      Iterable<? extends MutableMethodDeclaration> _declaredMethods = annotatedClass.getDeclaredMethods();
      final Procedure1<MutableMethodDeclaration> _function = new Procedure1<MutableMethodDeclaration>() {
        @Override
        public void apply(final MutableMethodDeclaration it) {
          String _simpleName = it.getSimpleName();
          String _plus = ("prefix_" + _simpleName);
          it.setSimpleName(_plus);
        }
      };
      IterableExtensions.forEach(_declaredMethods, _function);
    }
    if (!_matched) {
      if (Objects.equal(_stringValue, "changeBody")) {
        _matched=true;
        Iterable<? extends MutableMethodDeclaration> _declaredMethods_1 = annotatedClass.getDeclaredMethods();
        final Procedure1<MutableMethodDeclaration> _function_1 = new Procedure1<MutableMethodDeclaration>() {
          @Override
          public void apply(final MutableMethodDeclaration it) {
            StringConcatenationClient _client = new StringConcatenationClient() {
              @Override
              protected void appendTo(StringConcatenationClient.TargetStringConcatenation _builder) {
                _builder.append("return null;");
              }
            };
            it.setBody(_client);
          }
        };
        IterableExtensions.forEach(_declaredMethods_1, _function_1);
      }
    }
  }
}
