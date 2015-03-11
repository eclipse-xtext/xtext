/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.psi.stubs;

import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.tree.IStubFileElementType;
import java.util.List;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.psi.impl.BaseXtextFile;
import org.eclipse.xtext.psi.stubs.ExportedObject;
import org.eclipse.xtext.psi.stubs.XtextFileElementType;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class XtextFileStub<T extends BaseXtextFile> extends PsiFileStubImpl<T> implements PsiFileStub<T> {
  @Accessors
  private URI uri;
  
  @Accessors
  private List<ExportedObject> exportedObjects;
  
  private final XtextFileElementType<?> type;
  
  public XtextFileStub(final T file, final XtextFileElementType<?> type) {
    super(file);
    this.type = type;
  }
  
  @Override
  public IStubFileElementType getType() {
    return this.type;
  }
  
  @Pure
  public URI getUri() {
    return this.uri;
  }
  
  public void setUri(final URI uri) {
    this.uri = uri;
  }
  
  @Pure
  public List<ExportedObject> getExportedObjects() {
    return this.exportedObjects;
  }
  
  public void setExportedObjects(final List<ExportedObject> exportedObjects) {
    this.exportedObjects = exportedObjects;
  }
}
