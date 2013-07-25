package org.eclipse.xtext.xbase.tests.typesystem;

import java.util.List;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.xbase.compiler.AbstractStringBuilderBasedAppendable;

@SuppressWarnings("all")
public class TestAppender extends AbstractStringBuilderBasedAppendable {
  public TestAppender(final boolean isJava) {
    super("\t", "\n", isJava);
  }
  
  protected void appendType(final JvmType type, final StringBuilder builder) {
    String _identifier = type.getIdentifier();
    builder.append(_identifier);
  }
  
  public List<String> getImports() {
    UnsupportedOperationException _unsupportedOperationException = new UnsupportedOperationException("TODO: auto-generated method stub");
    throw _unsupportedOperationException;
  }
}
