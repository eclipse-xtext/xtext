/**
 * Copyright (c) 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.core.tests.macro;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Test;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
@SuppressWarnings("all")
public class Bug467063Test extends AbstractActiveAnnotationTest {
  @Test
  public void testAddInterfaceWithDefaultMethod() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("@org.eclipse.xtend.core.tests.macro.AddInterfaceWithDefault class Foo {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("MULTIPLE FILES WERE GENERATED");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("File 1 : /myProject/xtend-gen/Foo.java");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtend.core.tests.macro.AddInterfaceWithDefault;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@AddInterfaceWithDefault");
    _builder_1.newLine();
    _builder_1.append("@SuppressWarnings(\"all\")");
    _builder_1.newLine();
    _builder_1.append("public class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("File 2 : /myProject/xtend-gen/de/test/Test.java");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("package de.test;");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@SuppressWarnings(\"all\")");
    _builder_1.newLine();
    _builder_1.append("public interface Test {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("default void sayHello() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("System.out.println(\"Hello World\");");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    this._xtendCompilerTester.assertCompilesTo(_builder, _builder_1);
  }
}
