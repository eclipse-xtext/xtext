/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.compiler;

import bug380058.Amount;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import javax.inject.Provider;
import org.antlr.runtime.Lexer;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider;
import org.eclipse.xtend.core.parser.antlr.internal.InternalXtendLexer;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.lib.annotations.Data;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmTypeParameter;
import org.eclipse.xtext.generator.IFilePostProcessor;
import org.eclipse.xtext.generator.trace.ITraceRegionProvider;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.concurrent.IReadAccess;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.compiler.DisableCodeGenerationAdapter;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;
import org.eclipse.xtext.xbase.compiler.IGeneratorConfigProvider;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.hamcrest.Matcher;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import test.Consumer;
import testdata.Annotation1;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("all")
public abstract class AbstractXtendCompilerTest extends AbstractXtendTestCase {
  @Inject
  protected JvmModelGenerator generator;
  
  @Inject
  protected IGeneratorConfigProvider generatorConfigProvider;
  
  @Inject
  protected IFilePostProcessor postProcessor;
  
  @Inject
  @Extension
  protected CompilationTestHelper compilationTestHelper;
  
  @Inject
  private ProcessorInstanceForJvmTypeProvider processorProvider;
  
  protected boolean useJavaCompiler = false;
  
  @Before
  public void setupCompiler() {
    Class<? extends AbstractXtendCompilerTest> _class = this.getClass();
    final Class<?>[] classes = new Class<?>[] { _class, Amount.class, Consumer.class, Data.class, Inject.class, Provider.class, Lexer.class, InternalXtendLexer.class, IReadAccess.class, CollectionLiterals.class, JvmTypeParameter.class, ITraceRegionProvider.class, XExpression.class, Object.class, Annotation1.class, Lists.class, EObject.class, Matcher.class, Is.class, Assert.class };
    this.compilationTestHelper.setJavaCompilerClassPath(classes);
    Class<? extends AbstractXtendCompilerTest> _class_1 = this.getClass();
    ClassLoader _classLoader = _class_1.getClassLoader();
    this.processorProvider.setClassLoader(_classLoader);
  }
  
  public void assertCompilesTo(final CharSequence input, final CharSequence expected) {
    GeneratorConfig _get = this.generatorConfigProvider.get(null);
    this.doAssertCompilesTo(input, expected, _get, false);
  }
  
  protected XtendFile doAssertCompilesTo(final CharSequence input, final CharSequence expected, final GeneratorConfig config, final boolean serializeAllTypes) {
    try {
      String _string = input.toString();
      final XtendFile file = this.file(_string, true);
      final ArrayList<CharSequence> results = CollectionLiterals.<CharSequence>newArrayList();
      Resource _eResource = file.eResource();
      EList<EObject> _contents = _eResource.getContents();
      Iterable<JvmDeclaredType> _filter = Iterables.<JvmDeclaredType>filter(_contents, JvmDeclaredType.class);
      for (final JvmDeclaredType inferredType : _filter) {
        {
          boolean _isDisabled = DisableCodeGenerationAdapter.isDisabled(inferredType);
          Assert.assertFalse(_isDisabled);
          CharSequence javaCode = this.generator.generateType(inferredType, config);
          CharSequence _postProcess = this.postProcessor.postProcess(null, javaCode);
          javaCode = _postProcess;
          results.add(javaCode);
          if (this.useJavaCompiler) {
            final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
              @Override
              public void accept(final CompilationTestHelper.Result it) {
                it.getCompiledClass();
              }
            };
            this.compilationTestHelper.compile(input, _function);
          }
        }
      }
      if (serializeAllTypes) {
        String _string_1 = expected.toString();
        String _join = IterableExtensions.join(results, "\n");
        Assert.assertEquals(_string_1, _join);
      } else {
        String _string_2 = expected.toString();
        CharSequence _head = IterableExtensions.<CharSequence>head(results);
        String _string_3 = _head.toString();
        Assert.assertEquals(_string_2, _string_3);
      }
      return file;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
