package org.eclipse.xtend.core.tests.resource;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescription.Manager;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class ImportedNamesTest extends AbstractXtendTestCase {
  @Inject
  private Manager resourceDescriptionManager;
  
  private final List<String> primitives = Collections.<String>unmodifiableList(Lists.<String>newArrayList("boolean", "int", "char", "byte", "short", "long"));
  
  @Test
  public void testPrimitvesNotIncluded() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package testPackage");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import hubbabubba.*");
      _builder.newLine();
      _builder.append("import java.util.*");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class TestCase {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("String x;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("int i;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("boolean b;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("char c;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("short s;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("long l;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("byte t;");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("List<Object> l;");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final XtendFile file = this.file(_builder.toString());
      Resource _eResource = file.eResource();
      final IResourceDescription description = this.resourceDescriptionManager.getResourceDescription(_eResource);
      Iterable<QualifiedName> _importedNames = description.getImportedNames();
      final Function1<QualifiedName,Boolean> _function = new Function1<QualifiedName,Boolean>() {
          public Boolean apply(final QualifiedName it) {
            String _lastSegment = it.getLastSegment();
            boolean _contains = ImportedNamesTest.this.primitives.contains(_lastSegment);
            return Boolean.valueOf(_contains);
          }
        };
      boolean _exists = IterableExtensions.<QualifiedName>exists(_importedNames, _function);
      Assert.assertFalse(_exists);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
