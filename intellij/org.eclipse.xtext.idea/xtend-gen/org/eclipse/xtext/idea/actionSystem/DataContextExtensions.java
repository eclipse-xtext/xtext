/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.idea.actionSystem;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

@SuppressWarnings("all")
public class DataContextExtensions {
  public static Project getProject(final DataContext dataContext) {
    return CommonDataKeys.PROJECT.getData(dataContext);
  }
  
  public static Editor getEditor(final DataContext dataContext) {
    return CommonDataKeys.EDITOR.getData(dataContext);
  }
  
  public static PsiFile getPsiFile(final DataContext dataContext) {
    return CommonDataKeys.PSI_FILE.getData(dataContext);
  }
  
  public static PsiElement getPsiElement(final DataContext dataContext) {
    return CommonDataKeys.PSI_ELEMENT.getData(dataContext);
  }
}
