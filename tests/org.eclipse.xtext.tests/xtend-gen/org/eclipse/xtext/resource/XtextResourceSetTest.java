package org.eclipse.xtext.resource;

import org.eclipse.xtext.resource.AbstractXtextResourceSetTest;
import org.eclipse.xtext.resource.XtextResourceSet;

@SuppressWarnings("all")
public class XtextResourceSetTest extends AbstractXtextResourceSetTest {
  @Override
  protected XtextResourceSet createEmptyResourceSet() {
    return new XtextResourceSet();
  }
}
