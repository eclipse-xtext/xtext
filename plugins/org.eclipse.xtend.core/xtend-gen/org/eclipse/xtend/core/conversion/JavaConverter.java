/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.conversion;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.osgi.baseadaptor.loader.ClasspathEntry;
import org.eclipse.osgi.baseadaptor.loader.ClasspathManager;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;
import org.eclipse.xtend.core.conversion.JavaASTFlattener;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;

/**
 * @author Dennis H�bner - Initial contribution and API
 */
@SuppressWarnings("all")
public class JavaConverter {
  public static class ConversionResult {
    private String xtendCode;
    
    private Iterable<String> problems = CollectionLiterals.<String>newArrayList();
    
    public String getXtendCode() {
      return this.xtendCode;
    }
    
    public Iterable<String> getProblems() {
      return this.problems;
    }
    
    public static JavaConverter.ConversionResult create(final JavaASTFlattener flattener) {
      final JavaConverter.ConversionResult result = new JavaConverter.ConversionResult();
      String _result = flattener.getResult();
      result.xtendCode = _result;
      List<String> _problems = flattener.getProblems();
      boolean _notEquals = (!Objects.equal(_problems, null));
      if (_notEquals) {
        List<String> _problems_1 = flattener.getProblems();
        result.problems = _problems_1;
      }
      return result;
    }
  }
  
  @Inject
  private Provider<JavaASTFlattener> flattenerProvider;
  
  private String complianceLevel = "1.5";
  
  public JavaConverter.ConversionResult toXtend(final ICompilationUnit cu) {
    final ASTParser parser = ASTParser.newParser(AST.JLS3);
    final Hashtable options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(this.complianceLevel, options);
    parser.setCompilerOptions(options);
    parser.setStatementsRecovery(true);
    parser.setResolveBindings(true);
    parser.setBindingsRecovery(true);
    parser.setSource(cu);
    final JavaASTFlattener flattener = this.flattenerProvider.get();
    ASTNode _createAST = parser.createAST(null);
    _createAST.accept(flattener);
    final JavaConverter.ConversionResult result = JavaConverter.ConversionResult.create(flattener);
    return result;
  }
  
  public JavaConverter.ConversionResult toXtend(final String unitName, final String javaSrc) {
    return this.toXtend(unitName, javaSrc, ASTParser.K_COMPILATION_UNIT);
  }
  
  public JavaConverter.ConversionResult toXtend(final String unitName, final String javaSrc, final int javaSourceKind) {
    final ASTParser parser = ASTParser.newParser(AST.JLS3);
    final Hashtable options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(this.complianceLevel, options);
    parser.setCompilerOptions(options);
    parser.setStatementsRecovery(true);
    parser.setResolveBindings(true);
    parser.setBindingsRecovery(true);
    Class<? extends JavaConverter> _class = this.getClass();
    final ClassLoader cl = _class.getClassLoader();
    if ((cl instanceof DefaultClassLoader)) {
      ClasspathManager _classpathManager = ((DefaultClassLoader)cl).getClasspathManager();
      ClasspathEntry[] _hostClasspathEntries = _classpathManager.getHostClasspathEntries();
      final Function1<ClasspathEntry, Object> _function = new Function1<ClasspathEntry, Object>() {
        public Object apply(final ClasspathEntry it) {
          return null;
        }
      };
      /* ListExtensions.<ClasspathEntry, Object>map(((List<ClasspathEntry>)Conversions.doWrapArray(_hostClasspathEntries)), _function); */
    }
    final ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
    URL[] _uRLs = ((URLClassLoader) sysClassLoader).getURLs();
    final Function1<URL, String> _function_1 = new Function1<URL, String>() {
      public String apply(final URL it) {
        return it.getFile();
      }
    };
    final List<String> cpEntries = ListExtensions.<URL, String>map(((List<URL>)Conversions.doWrapArray(_uRLs)), _function_1);
    parser.setEnvironment(((String[])Conversions.unwrapArray(cpEntries, String.class)), null, null, true);
    parser.setKind(javaSourceKind);
    parser.setUnitName(unitName);
    char[] _charArray = javaSrc.toCharArray();
    parser.setSource(_charArray);
    final JavaASTFlattener flattener = this.flattenerProvider.get();
    flattener.setJavaSourceKind(javaSourceKind);
    ASTNode _createAST = parser.createAST(null);
    _createAST.accept(flattener);
    return JavaConverter.ConversionResult.create(flattener);
  }
}
