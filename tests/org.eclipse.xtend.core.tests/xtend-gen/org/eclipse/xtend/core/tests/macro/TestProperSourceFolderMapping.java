/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.macro;

import org.eclipse.xtend.core.compiler.batch.XtendCompilerTester;
import org.eclipse.xtend.core.tests.macro.AbstractActiveAnnotationTest;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtend.lib.macro.declaration.CompilationUnit;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.lib.Extension;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class TestProperSourceFolderMapping extends AbstractActiveAnnotationTest {
  /**
   * checks that the configured target folder matches what is used by the generator.
   * I.e. in case of xtend it is xtend-gen
   */
  @Test
  public void testChangeGeneratedJava() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("@org.eclipse.xtend.core.tests.macro.__GeneratedSourceModifingAnnotation");
    _builder.newLine();
    _builder.append("class MyClass {}");
    _builder.newLine();
    final IAcceptor<XtendCompilerTester.CompilationResult> _function = new IAcceptor<XtendCompilerTester.CompilationResult>() {
      @Override
      public void accept(final XtendCompilerTester.CompilationResult it) {
        @Extension
        final TransformationContext ctx = it.getTransformationContext();
        CompilationUnit _compilationUnit = it.getCompilationUnit();
        Path _filePath = _compilationUnit.getFilePath();
        Path _targetFolder = ctx.getTargetFolder(_filePath);
        final Path file = _targetFolder.append("MyClass.java");
        boolean _exists = ctx.exists(file);
        Assert.assertTrue(_exists);
      }
    };
    this._xtendCompilerTester.compile(_builder, _function);
  }
}
