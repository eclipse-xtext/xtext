/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.file;

import com.google.common.base.Objects;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.File;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@Singleton
@SuppressWarnings("all")
public class RuntimeWorkspaceConfigProvider implements Provider<WorkspaceConfig> {
  private File _workspaceRoot = new Function0<File>() {
    public File apply() {
      File _file = new File(".");
      File _absoluteFile = _file.getAbsoluteFile();
      File _parentFile = _absoluteFile.getParentFile();
      return _parentFile;
    }
  }.apply();
  
  public File getWorkspaceRoot() {
    return this._workspaceRoot;
  }
  
  public void setWorkspaceRoot(final File workspaceRoot) {
    this._workspaceRoot = workspaceRoot;
  }
  
  private WorkspaceConfig _workspaceConfig;
  
  public WorkspaceConfig getWorkspaceConfig() {
    return this._workspaceConfig;
  }
  
  public void setWorkspaceConfig(final WorkspaceConfig workspaceConfig) {
    this._workspaceConfig = workspaceConfig;
  }
  
  public WorkspaceConfig get() {
    WorkspaceConfig _workspaceConfig = this.getWorkspaceConfig();
    boolean _equals = Objects.equal(_workspaceConfig, null);
    if (_equals) {
      File _workspaceRoot = this.getWorkspaceRoot();
      String _absolutePath = _workspaceRoot.getAbsolutePath();
      WorkspaceConfig _workspaceConfig_1 = new WorkspaceConfig(_absolutePath);
      final Procedure1<WorkspaceConfig> _function = new Procedure1<WorkspaceConfig>() {
        public void apply(final WorkspaceConfig it) {
          File _workspaceRoot = RuntimeWorkspaceConfigProvider.this.getWorkspaceRoot();
          File[] _listFiles = _workspaceRoot.listFiles();
          final Function1<File,Boolean> _function = new Function1<File,Boolean>() {
            public Boolean apply(final File it) {
              boolean _isDirectory = it.isDirectory();
              return Boolean.valueOf(_isDirectory);
            }
          };
          Iterable<File> _filter = IterableExtensions.<File>filter(((Iterable<File>)Conversions.doWrapArray(_listFiles)), _function);
          for (final File dir : _filter) {
            String _name = dir.getName();
            ProjectConfig _projectConfig = new ProjectConfig(_name);
            final Procedure1<ProjectConfig> _function_1 = new Procedure1<ProjectConfig>() {
              public void apply(final ProjectConfig it) {
              }
            };
            ProjectConfig _doubleArrow = ObjectExtensions.<ProjectConfig>operator_doubleArrow(_projectConfig, _function_1);
            it.addProjectConfig(_doubleArrow);
          }
        }
      };
      WorkspaceConfig _doubleArrow = ObjectExtensions.<WorkspaceConfig>operator_doubleArrow(_workspaceConfig_1, _function);
      this.setWorkspaceConfig(_doubleArrow);
    }
    return this.getWorkspaceConfig();
  }
}
