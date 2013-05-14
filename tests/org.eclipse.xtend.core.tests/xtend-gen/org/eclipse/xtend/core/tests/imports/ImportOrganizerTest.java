/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.imports;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ReplaceRegion;
import org.eclipse.xtext.xbase.imports.ImportOrganizer;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Jan Koehnlein - Initial conribution and API
 */
@SuppressWarnings("all")
public class ImportOrganizerTest extends AbstractXtendTestCase {
  @Inject
  private ImportOrganizer importOrganizer;
  
  protected void assertIsOrganizedTo(final CharSequence model, final CharSequence expected) {
    this.assertIsOrganizedTo(model, expected, true);
  }
  
  protected void assertIsOrganizedTo(final CharSequence model, final CharSequence expected, final boolean validate) {
    try {
      String _string = model.toString();
      final XtendFile xtendFile = this.file(_string, validate);
      Resource _eResource = xtendFile.eResource();
      final List<ReplaceRegion> changes = this.importOrganizer.getOrganizedImportChanges(((XtextResource) _eResource));
      StringBuilder _stringBuilder = new StringBuilder(model);
      final StringBuilder builder = _stringBuilder;
      final Function1<ReplaceRegion,Integer> _function = new Function1<ReplaceRegion,Integer>() {
          public Integer apply(final ReplaceRegion it) {
            int _offset = it.getOffset();
            return Integer.valueOf(_offset);
          }
        };
      final List<ReplaceRegion> sortedChanges = IterableExtensions.<ReplaceRegion, Integer>sortBy(changes, _function);
      ReplaceRegion lastChange = null;
      for (final ReplaceRegion it : sortedChanges) {
        {
          boolean _and = false;
          boolean _notEquals = (!Objects.equal(lastChange, null));
          if (!_notEquals) {
            _and = false;
          } else {
            int _endOffset = lastChange.getEndOffset();
            int _offset = it.getOffset();
            boolean _greaterThan = (_endOffset > _offset);
            _and = (_notEquals && _greaterThan);
          }
          if (_and) {
            String _plus = ("Overlapping text edits: " + lastChange);
            String _plus_1 = (_plus + " and ");
            String _plus_2 = (_plus_1 + it);
            Assert.fail(_plus_2);
          }
          lastChange = it;
        }
      }
      List<ReplaceRegion> _reverse = ListExtensions.<ReplaceRegion>reverse(sortedChanges);
      for (final ReplaceRegion it_1 : _reverse) {
        int _offset = it_1.getOffset();
        int _offset_1 = it_1.getOffset();
        int _length = it_1.getLength();
        int _plus = (_offset_1 + _length);
        String _text = it_1.getText();
        builder.replace(_offset, _plus, _text);
      }
      String _string_1 = expected.toString();
      String _string_2 = builder.toString();
      Assert.assertEquals(_string_1, _string_2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testSimple() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testDefaultPackage() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testDefaultPackageLeadingWhitespace() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.newLine();
    _builder.append("   \t");
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testDefaultPackageWithJavaDoc() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* some doc");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("/**");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("* some doc");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testWithHeader() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* some header");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("package foo");
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("/**");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("* some header");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("package foo");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testWithHeaderAndJavaDoc() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* some header");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("package foo");
    _builder.newLine();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* some doc");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("/**");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("* some header");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("package foo");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("/**");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("* some doc");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("class Foo implements Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testGetOrganizedImportSection_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.lang.String");
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(List<String> s) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testGetOrganizedImportSection_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.lang.String");
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import java.util.List");
    _builder.newLine();
    _builder.append("import java.lang.Integer");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(List<String> s) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testGetOrganizedImportSection_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(List<String> s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = newArrayList(\'foo\',\'bar\')");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("Collections::sort(x)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.Collections");
    _builder_1.newLine();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = newArrayList(\'foo\',\'bar\')");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("Collections::sort(x)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testGetOrganizedImportSection_04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*");
    _builder.newLine();
    _builder.append("import java.io.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(List<String> s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.append("import java.util.ArrayList");
    _builder_1.newLine();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testGetOrganizedImportSection_05() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*");
    _builder.newLine();
    _builder.append("import java.io.*");
    _builder.newLine();
    _builder.append("import com.google.inject.Inject");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("@Inject String s");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(List<String> s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import com.google.inject.Inject");
    _builder_1.newLine();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.append("import java.util.ArrayList");
    _builder_1.newLine();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("@Inject String s");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = new ArrayList<Map<StringBuilder,? extends Serializable>>()");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testInnerClasses_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(org.eclipse.emf.ecore.resource.Resource$Factory a, org.eclipse.emf.ecore.resource.Resource$Factory$Registry b) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(Resource$Factory a, Resource$Factory$Registry b) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = Resource$Factory$Registry::INSTANCE");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val y = EPackage$Registry::INSTANCE");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.EPackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = Resource$Factory$Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val y = EPackage$Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testInnerClasses_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("typeof(org.eclipse.emf.ecore.resource.Resource$Factory) == typeof(org.eclipse.emf.ecore.resource.Resource$Factory$Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("\t ");
    _builder_1.append("typeof(Resource$Factory) == typeof(Resource$Factory$Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("typeof(Resource$Factory$Registry) == typeof(EPackage$Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.EPackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("typeof(Resource$Factory$Registry) == typeof(EPackage$Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_05() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource$Factory$Registry");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("typeof(Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Registry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("typeof(Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_06() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Resource {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = org::eclipse::emf::ecore::resource::Resource$Factory$Registry::INSTANCE");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Resource {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = org::eclipse::emf::ecore::resource::Resource$Factory$Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testInnerClasses_07() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(org.eclipse.emf.ecore.resource.Resource.Factory a, org.eclipse.emf.ecore.resource.Resource.Factory.Registry b) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(Resource.Factory a, Resource.Factory.Registry b) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_08() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = Resource::Factory::Registry::INSTANCE");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val y = EPackage::Registry::INSTANCE");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.EPackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = Resource::Factory::Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val y = EPackage::Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testInnerClasses_09() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("\t ");
    _builder.append("typeof(org.eclipse.emf.ecore.resource.Resource.Factory) == typeof(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("\t ");
    _builder_1.append("typeof(Resource.Factory) == typeof(Resource.Factory.Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_10() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.EPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("typeof(Resource.Factory.Registry) == typeof(EPackage.Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.EPackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("typeof(Resource.Factory.Registry) == typeof(EPackage.Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_11() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Registry");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("typeof(Registry)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Registry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("typeof(Registry)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClasses_12() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Resource {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("val x = org::eclipse::emf::ecore::resource::Resource::Factory::Registry::INSTANCE");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Resource {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("val x = org::eclipse::emf::ecore::resource::Resource::Factory::Registry::INSTANCE");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1, false);
  }
  
  @Test
  public void testNameClashSameFileWins_1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("java.lang.String x");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class String {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("java.lang.String x");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class String {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashSameFileWins() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo implements java.io.Serializable {}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Serializable {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo implements java.io.Serializable {}");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Serializable {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashOrder_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.awt.List test(java.util.List<String> s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.awt.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def List test(java.util.List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashOrder_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.List<String> test(java.awt.List s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def List<String> test(java.awt.List s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashMoreCommon() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.List<String> test(java.awt.List s) {");
    _builder.newLine();
    _builder.append("  \t");
    _builder.append("new java.awt.List()");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.awt.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def java.util.List<String> test(List s) {");
    _builder_1.newLine();
    _builder_1.append("  \t");
    _builder_1.append("new List()");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashInnerClasses_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.xtext.xbase.XbasePackage");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(XbasePackage$Literals x, XAnnotationsPackage$Literals y) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.xbase.XbasePackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(XbasePackage$Literals x, XAnnotationsPackage$Literals y) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashInnerClasses_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.xtext.xbase.XbasePackage");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(XbasePackage.Literals x, XAnnotationsPackage.Literals y) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.xbase.XbasePackage");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(XbasePackage.Literals x, XAnnotationsPackage.Literals y) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashInnerClassesWithPreference_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.xtext.xbase.XbasePackage$Literals");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(Literals x, XAnnotationsPackage$Literals y) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.xbase.XbasePackage.Literals");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(Literals x, XAnnotationsPackage$Literals y) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNameClashInnerClassesWithPreference_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import org.eclipse.xtext.xbase.XbasePackage.Literals");
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(Literals x, XAnnotationsPackage.Literals y) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.xbase.XbasePackage.Literals");
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(Literals x, XAnnotationsPackage.Literals y) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticImport_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(java.util.List<String> s) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("sort(s)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import static java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("sort(s)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticImport_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(java.util.List<String> s) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("s.sort");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import static extension java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("s.sort");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImportedOperators_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension test.StaticOperators.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(String s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("-s");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension test.StaticOperators.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(String s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("-s");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImportedOperators_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension test.StaticOperators.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(String s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("s-s");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension test.StaticOperators.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(String s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("s-s");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImportedOperators_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension test.StaticOperators.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(String s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("s.assign = s");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension test.StaticOperators.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(String s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("s.assign = s");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImportedOperators_04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension test.StaticOperators.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(String it) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("assign = it");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension test.StaticOperators.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(String it) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("assign = it");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testLocalOperator() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def foo() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("\'\' * \'\'");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def operator_multiply(String x, String y) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def foo() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("\'\' * \'\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def operator_multiply(String x, String y) {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticLocalOperator() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def foo() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("\'\' * \'\'");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def static operator_multiply(String x, String y) {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def foo() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("\'\' * \'\'");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def static operator_multiply(String x, String y) {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImplicitExtensions() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test(java.util.List<String> s) {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("s.head");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test(List<String> s) {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("s.head");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticExtensions() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def void test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("typeof(java.util.Collections).singleton");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.Collections");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import static extension java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def void test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("typeof(Collections).singleton");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticExtensionSameClass() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("static def void test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("\"\".foo");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("  ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("static def void getFoo(String x) {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("} ");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("static def void test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("\"\".foo");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("static def void getFoo(String x) {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("} ");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testObsoleteStatic() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static java.util.Collections.*");
    _builder.newLine();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("\'\'.singleton");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("\'\'.singleton");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testObsoleteStatic_2() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static java.util.Collections.*");
    _builder.newLine();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("\'\'.singleton");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("singleton(\'\')");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("\'\'.singleton");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("singleton(\'\')");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testObsoleteStaticExtension() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static java.util.Collections.*");
    _builder.newLine();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("singleton(\'\')");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static java.util.Collections.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("singleton(\'\')");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map$Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map.Entry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Map$Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map$Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map$Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.Map$Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map$Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Resource$Factory$Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Resource$Factory$Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_05() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Resource$Factory$Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_06() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource$Factory$Descriptor");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_07() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map.Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map.Entry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_08() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Map.Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map.Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_09() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map.Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.Map.Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map.Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_10() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Resource.Factory.Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Resource.Factory.Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_11() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Resource.Factory.Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_12() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Descriptor test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.emf.ecore.resource.Resource.Factory.Descriptor");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Descriptor test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_13() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map.Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.Map$Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map$Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInnerClassImport_14() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map$Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def java.util.Map.Entry test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Map.Entry test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testFunctionTypes_afterResolve_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map$Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def (Entry)=>void test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map.Entry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def (Entry)=>void test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testFunctionTypes_afterResolve_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.util.Map.Entry");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def (Entry)=>void test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.util.Map.Entry");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def (Entry)=>void test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testImport_PairOf() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import static org.eclipse.xtext.xbase.lib.Pair.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Object test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return of(\'\', \'\')");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import static org.eclipse.xtext.xbase.lib.Pair.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Object test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return of(\'\', \'\')");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testArrayType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import java.io.Serializable");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Serializable[][] test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return null");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import java.io.Serializable");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Serializable[][] test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return null");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testAnnotationWithSameName() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("annotation Serializable {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("Class<? extends java.io.Serializable> clazz");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("annotation Serializable {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("Class<? extends java.io.Serializable> clazz");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testClassWithSameName() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("class Serializable {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("Class<? extends java.io.Serializable> clazz");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Serializable {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("Class<? extends java.io.Serializable> clazz");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testStaticImport_IterableExtensions() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("import static org.eclipse.xtext.xbase.lib.IterableExtensions.*");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("def Object test() {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("return filter(null, null)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import static org.eclipse.xtext.xbase.lib.IterableExtensions.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("def Object test() {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("return filter(null, null)");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testPropertyAnnotation() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("@Property int prop");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("  ");
    _builder_1.append("@Property int prop");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider$Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider$Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@org.eclipse.xtext.service.DispatchingProvider$Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@DispatchingProvider$Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@DispatchingProvider.Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_05() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@org.eclipse.xtext.service.DispatchingProvider$Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@DispatchingProvider$Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testNestedAnnotation_06() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package foo.bar");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import org.eclipse.xtext.service.DispatchingProvider$Runtime");
    _builder.newLine();
    _builder.newLine();
    _builder.append("@org.eclipse.xtext.service.DispatchingProvider.Runtime");
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package foo.bar");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import org.eclipse.xtext.service.DispatchingProvider");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("@DispatchingProvider.Runtime");
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testBug388933_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension MyExtensionLib.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class MyExtensionLib {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def static func1 (String a, int b) {  }");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("class MyClient {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def sample (String it) { func1(47)  }");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static extension MyExtensionLib.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class MyExtensionLib {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("def static func1 (String a, int b) {  }");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("class MyClient {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("def sample (String it) { func1(47)  }");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testBug388933_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static java.util.Arrays.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class C {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("var x = asList");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import static java.util.Arrays.*");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class C {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("var x = asList");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testBug380637_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class MyExtensionLib {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("def static func1 (String a, int b) {");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("func1(a,b)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class MyExtensionLib {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("def static func1 (String a, int b) {");
    _builder_1.newLine();
    _builder_1.append("       ");
    _builder_1.append("func1(a,b)");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testBug380637_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("abstract class MyClass implements org.eclipse.xtext.scoping.IScope {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("org.eclipse.xtext.scoping.IScope scope = NULLSCOPE");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.scoping.IScope");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("abstract class MyClass implements IScope {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("IScope scope = NULLSCOPE");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testBug380637_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("abstract class MyClass extends org.eclipse.xtext.scoping.impl.AbstractScope {");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("new() {");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("super(null, false)");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("Object field = NULLSCOPE");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import org.eclipse.xtext.scoping.impl.AbstractScope");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("abstract class MyClass extends AbstractScope {");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("new() {");
    _builder_1.newLine();
    _builder_1.append("        ");
    _builder_1.append("super(null, false)");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("    ");
    _builder_1.append("Object field = NULLSCOPE");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testDontOverrideTypeParameter() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo<String> {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("java.lang.String s");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo<String> {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("java.lang.String s");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInheritedInnerClass_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo extends types.OuterClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("types.OuterClass$MiddleClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import types.OuterClass");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo extends OuterClass {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("OuterClass$MiddleClass foo");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testInheritedInnerClass_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo extends types.OuterClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("types.OuterClass.MiddleClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import types.OuterClass");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo extends OuterClass {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("OuterClass.MiddleClass foo");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testDontOverrideInheritedInnerClass() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo extends types.OuterClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("types.MiddleClass foo");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import types.OuterClass");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo extends OuterClass {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("types.MiddleClass foo");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testEscapedPackageName() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo extends types.^override.EscapePackageName {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import types.^override.EscapePackageName");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo extends EscapePackageName {");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testJavaDoc() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* {@link java.util.List}");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("/**");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("* {@link List}");
    _builder_1.newLine();
    _builder_1.append(" ");
    _builder_1.append("*/");
    _builder_1.newLine();
    _builder_1.append("class Foo {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testAssignmentToStaticLocalField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("static int foo");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def bar() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("foo = 1");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("static int foo");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def bar() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("foo = 1");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testFeatureCallToStaticLocalField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("static int foo");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def bar() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("foo");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("static int foo");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def bar() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("foo");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testExtensionFeatureCallToStaticLocal() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import static extension Foo.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def foo() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("\'\'.bar");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def static bar(String s) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def foo() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("\'\'.bar");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def static bar(String s) {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testExtensionFeatureCallToStaticLocalFieldFromSuper() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.Collections");
    _builder.newLine();
    _builder.newLine();
    _builder.append("import static extension java.util.Collections.*");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Foo extends Collections {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def foo() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("\'\'.singleton");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.Collections");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo extends Collections {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def foo() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("\'\'.singleton");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testFunctionType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def bar() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val (String) => boolean foo = null\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def bar() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("val (String) => boolean foo = null\t");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testFunctionType_1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("def bar() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("val (java.io.File) => java.io.File foo = null\t");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.File");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("def bar() {");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("val (File) => File foo = null\t");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("}");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testWildcard() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("var java.util.List<?> x");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("var List<?> x");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testWildcard_1() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("var java.util.List<? extends java.io.File> x");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("import java.io.File");
    _builder_1.newLine();
    _builder_1.append("import java.util.List");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Foo {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("var List<? extends File> x");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testEscapedKeyword_01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package pack");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Bug406086 extends bug406086.^def.Bug406086 {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package pack");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Bug406086 extends bug406086.^def.Bug406086 {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testEscapedKeyword_02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package pack");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Bug extends bug406086.^def.Bug406086 {}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package pack");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("import bug406086.^def.Bug406086");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Bug extends Bug406086 {}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
  
  @Test
  public void testEscapedKeyword_03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("package pack");
    _builder.newLine();
    _builder.newLine();
    _builder.append("class Bug406086 {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("val x = bug406086::^def::Bug406086::FIELD");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("package pack");
    _builder_1.newLine();
    _builder_1.newLine();
    _builder_1.append("class Bug406086 {");
    _builder_1.newLine();
    _builder_1.append("\t");
    _builder_1.append("val x = bug406086::^def::Bug406086::FIELD");
    _builder_1.newLine();
    _builder_1.append("}");
    _builder_1.newLine();
    this.assertIsOrganizedTo(_builder, _builder_1);
  }
}
