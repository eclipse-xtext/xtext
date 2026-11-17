/**
 * Copyright (c) 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.core.tests.macro;

import java.io.ByteArrayOutputStream;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.resource.BatchLinkableResourceStorageWritable;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Didier Vojtisek - Initial contribution and API
 */
@SuppressWarnings("all")
public class AnnotationPropertyRemoveTest extends AbstractActiveAnnotationTest {
  @Test
  public void testAnnotationPropertyRemove_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("@org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation()");
    _builder.newLine();
    _builder.append("class TestClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public String foo");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("public String _ba ");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@RemoveUnderscoreAnnotation");
    _builder_1.newLine();
    _builder_1.append("@SuppressWarnings(\"all\")");
    _builder_1.newLine();
    _builder_1.append("public class TestClass {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("public String foo;");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this._xtendCompilerTester.assertCompilesTo(_builder, _builder_1);
  }

  @Test
  public void testresourceStorageWrite() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("@org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation()");
      _builder.newLine();
      _builder.append("class AnnotationPropertyRemoveTestClass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public String foo");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public String _ba ");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String contents = _builder.toString();
      final XtendFile file = this.file(contents);
      ByteArrayOutputStream _byteArrayOutputStream = new ByteArrayOutputStream();
      Resource _eResource = file.eResource();
      new BatchLinkableResourceStorageWritable(_byteArrayOutputStream, false) {
        @Override
        public String getFragment(final EObject obj) {
          String _string = obj.toString();
          String _plus = (_string + " is not contained in any resource ");
          Resource _eResource = obj.eResource();
          boolean _tripleNotEquals = (_eResource != null);
          Assert.assertTrue(_plus, _tripleNotEquals);
          return super.getFragment(obj);
        }
      }.writeResource(((StorageAwareResource) _eResource));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
