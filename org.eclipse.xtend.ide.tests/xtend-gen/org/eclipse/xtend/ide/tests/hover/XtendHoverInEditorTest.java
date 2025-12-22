/**
 * Copyright (c) 2015, 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.hover;

import com.google.inject.Inject;
import org.apache.log4j.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.text.java.hover.JavadocBrowserInformationControlInput;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.testing.logging.LoggingTester;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.hover.IEObjectHover;
import org.eclipse.xtext.ui.editor.hover.html.XtextBrowserInformationControlInput;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.typesystem.internal.AbstractBatchTypeResolver;
import org.eclipse.xtext.xbase.ui.hover.XbaseInformationControlInput;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class XtendHoverInEditorTest extends AbstractXtendUITestCase {
  @Inject
  @Extension
  private WorkbenchTestHelper helper;

  @Inject
  @Extension
  private SyncUtil _syncUtil;

  @Inject
  private IEObjectHover hoverer;

  @After
  @Override
  public void tearDown() {
    try {
      this.helper.tearDown();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfReferencedElement() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Hello Foo");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Foo {}");
      _builder.newLine();
      final String contentFoo = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Bar extends Foo {}");
      _builder_1.newLine();
      final String contentBar = _builder_1.toString();
      final IFile fileFoo = this.helper.createFile("Foo.xtend", contentFoo);
      final IFile fileBar = this.helper.createFile("Bar.xtend", contentBar);
      this._syncUtil.waitForBuild(null);
      final XtextEditor editor = this.helper.openEditor(fileBar);
      ISourceViewer _internalSourceViewer = editor.getInternalSourceViewer();
      Region _region = new Region(19, 1);
      Object _hoverInfo2 = ((ITextHoverExtension2) this.hoverer).getHoverInfo2(((ITextViewer) _internalSourceViewer), _region);
      final XtextBrowserInformationControlInput info = ((XtextBrowserInformationControlInput) _hoverInfo2);
      Assert.assertTrue(info.getHtml().contains("Hello Foo"));
      final XtextEditor fooEditor = this.helper.openEditor(fileFoo);
      IXtextDocument _document = fooEditor.getDocument();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("/**");
      _builder_2.newLine();
      _builder_2.append(" ");
      _builder_2.append("* Hello BAZ");
      _builder_2.newLine();
      _builder_2.append(" ");
      _builder_2.append("*/");
      _builder_2.newLine();
      _builder_2.append("class Foo {}");
      _builder_2.newLine();
      _document.set(_builder_2.toString());
      this._syncUtil.waitForReconciler(fooEditor);
      this._syncUtil.waitForReconciler(editor);
      ISourceViewer _internalSourceViewer_1 = editor.getInternalSourceViewer();
      Region _region_1 = new Region(19, 1);
      Object _hoverInfo2_1 = ((ITextHoverExtension2) this.hoverer).getHoverInfo2(((ITextViewer) _internalSourceViewer_1), _region_1);
      final XtextBrowserInformationControlInput info2 = ((XtextBrowserInformationControlInput) _hoverInfo2_1);
      Assert.assertFalse(info2.getHtml().contains("Hello Foo"));
      Assert.assertTrue(info2.getHtml().contains("Hello BAZ"));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfReferencedElementWithAnnotation() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Hello Foo");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("@SuppressWarnings(\"foo\")");
      _builder.newLine();
      _builder.append("class Foo {}");
      _builder.newLine();
      final String contentFoo = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Bar extends Foo {}");
      _builder_1.newLine();
      final String contentBar = _builder_1.toString();
      final IFile fileFoo = this.helper.createFile("Foo.xtend", contentFoo);
      final IFile fileBar = this.helper.createFile("Bar.xtend", contentBar);
      this._syncUtil.waitForBuild(null);
      final XtextEditor editor = this.helper.openEditor(fileBar);
      this._syncUtil.waitForReconciler(editor);
      final Runnable _function = () -> {
        ISourceViewer _internalSourceViewer = editor.getInternalSourceViewer();
        Region _region = new Region(19, 1);
        Object _hoverInfo2 = ((ITextHoverExtension2) this.hoverer).getHoverInfo2(((ITextViewer) _internalSourceViewer), _region);
        final XtextBrowserInformationControlInput info = ((XtextBrowserInformationControlInput) _hoverInfo2);
        Assert.assertTrue(info.getHtml(), info.getHtml().contains("Hello Foo"));
        Assert.assertTrue(info.getHtml(), info.getHtml().contains("SuppressWarnings</a>(\"foo\")"));
      };
      final LoggingTester.LogCapture loggings = LoggingTester.captureLogging(Level.ERROR, AbstractBatchTypeResolver.class, _function);
      loggings.assertNoLogEntries();
      final XtextEditor fooEditor = this.helper.openEditor(fileFoo);
      IXtextDocument _document = fooEditor.getDocument();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("/**");
      _builder_2.newLine();
      _builder_2.append(" ");
      _builder_2.append("* Hello BAZ");
      _builder_2.newLine();
      _builder_2.append(" ");
      _builder_2.append("*/");
      _builder_2.newLine();
      _builder_2.append("@SuppressWarnings(\"bar\")");
      _builder_2.newLine();
      _builder_2.append("class Foo {}");
      _builder_2.newLine();
      _document.set(_builder_2.toString());
      this._syncUtil.waitForReconciler(fooEditor);
      this._syncUtil.waitForReconciler(editor);
      final Runnable _function_1 = () -> {
        ISourceViewer _internalSourceViewer = editor.getInternalSourceViewer();
        Region _region = new Region(19, 1);
        Object _hoverInfo2 = ((ITextHoverExtension2) this.hoverer).getHoverInfo2(((ITextViewer) _internalSourceViewer), _region);
        final XtextBrowserInformationControlInput info2 = ((XtextBrowserInformationControlInput) _hoverInfo2);
        Assert.assertFalse(info2.getHtml().contains("Hello Foo"));
        Assert.assertTrue(info2.getHtml().contains("Hello BAZ"));
        Assert.assertTrue(info2.getHtml(), info2.getHtml().contains("SuppressWarnings</a>(\"bar\")"));
      };
      final LoggingTester.LogCapture moreLoggings = LoggingTester.captureLogging(Level.ERROR, AbstractBatchTypeResolver.class, _function_1);
      moreLoggings.assertNoLogEntries();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfJavaType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Object object");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfJavaTypeInJavadocLink() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* {@link Object}");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo {}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfJavaTypeInJavadocSee() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* @see Object");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo {}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfFQNJavaType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Foo {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("java.lang.Object object");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfFQNJavaTypeInJavadocLink() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* {@link java.lang.Object}");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo {}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfFQNJavaTypeInJavadocSee() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* @see java.lang.Object");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("class Foo {}");
    _builder.newLine();
    this.hasHoverOverObject(_builder);
  }

  @Test
  public void testHoverOfXtendType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("Bar bar");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfXtendTypeInJavadocLink() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("/**");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("* {@link Bar}");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("*/");
      _builder_1.newLine();
      _builder_1.append("class Foo {}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfXtendTypeInJavadocSee() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("/**");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("* @see Bar");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("*/");
      _builder_1.newLine();
      _builder_1.append("class Foo {}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfFQNXtendType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package barpackage");
      _builder.newLine();
      _builder.newLine();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("barpackage/Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("class Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("barpackage.Bar bar");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfFQNXtendTypeInJavadocLink() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package barpackage");
      _builder.newLine();
      _builder.newLine();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("barpackage/Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("/**");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("* {@link barpackage.Bar}");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("*/");
      _builder_1.newLine();
      _builder_1.append("class Foo {}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testHoverOfFQNXtendTypeInJavadocSee() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package barpackage");
      _builder.newLine();
      _builder.newLine();
      _builder.append("/**");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("* Documentation of the Bar class.");
      _builder.newLine();
      _builder.append(" ");
      _builder.append("*/");
      _builder.newLine();
      _builder.append("class Bar {}");
      _builder.newLine();
      this.helper.createFile("barpackage/Bar.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("/**");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("* @see barpackage.Bar");
      _builder_1.newLine();
      _builder_1.append(" ");
      _builder_1.append("*/");
      _builder_1.newLine();
      _builder_1.append("class Foo {}");
      _builder_1.newLine();
      this.hasHoverOverBar(_builder_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private void hasHoverOverBar(final CharSequence it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Documentation of the Bar class.");
    this.hasHoverOverXtendType(it, "Bar", _builder.toString());
  }

  private void hasHoverOverObject(final CharSequence it) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("</code> is the root of the class hierarchy.");
    this.hasHoverOverJavaType(it, "Object", _builder.toString());
  }

  private void hasHoverOverJavaType(final CharSequence it, final String textUnderHover, final String expectedHoverContent) {
    this.hasHoverOver(it, textUnderHover, JavadocBrowserInformationControlInput.class, expectedHoverContent);
  }

  private void hasHoverOverXtendType(final CharSequence it, final String textUnderHover, final String expectedHoverContent) {
    this.hasHoverOver(it, textUnderHover, XbaseInformationControlInput.class, expectedHoverContent);
  }

  private void hasHoverOver(final CharSequence it, final String textUnderHover, final Class<? extends BrowserInformationControlInput> expectedPopupType, final String expectedHoverContent) {
    try {
      final IFile fileFoo = this.helper.createFile("Foo.xtend", it.toString());
      this._syncUtil.waitForBuild(null);
      final XtextEditor editor = this.helper.openEditor(fileFoo);
      int _indexOf = it.toString().indexOf(textUnderHover);
      int _length = textUnderHover.length();
      final Region region = new Region(_indexOf, _length);
      final Object info = ((ITextHoverExtension2) this.hoverer).getHoverInfo2(editor.getInternalSourceViewer(), region);
      Assert.assertTrue(expectedPopupType.isInstance(info));
      final String html = ((BrowserInformationControlInput) info).getHtml();
      Assert.assertTrue(html.contains(expectedHoverContent));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
