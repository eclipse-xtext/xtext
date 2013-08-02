package org.eclipse.xtext.xbase.file;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.xtend.lib.Data;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.util.ToStringHelper;

@Data
@SuppressWarnings("all")
public class WorkspaceConfig {
  private final String _absoluteFileSystemPath;
  
  public String getAbsoluteFileSystemPath() {
    return this._absoluteFileSystemPath;
  }
  
  private final Map<String,ProjectConfig> _projects = new Function0<Map<String,ProjectConfig>>() {
    public Map<String,ProjectConfig> apply() {
      LinkedHashMap<String,ProjectConfig> _newLinkedHashMap = CollectionLiterals.<String, ProjectConfig>newLinkedHashMap();
      return _newLinkedHashMap;
    }
  }.apply();
  
  public Map<String,ProjectConfig> getProjects() {
    return this._projects;
  }
  
  public ProjectConfig addProjectConfig(final ProjectConfig config) {
    Map<String,ProjectConfig> _projects = this.getProjects();
    String _name = config.getName();
    ProjectConfig _put = _projects.put(_name, config);
    return _put;
  }
  
  public WorkspaceConfig(final String absoluteFileSystemPath) {
    super();
    this._absoluteFileSystemPath = absoluteFileSystemPath;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_absoluteFileSystemPath== null) ? 0 : _absoluteFileSystemPath.hashCode());
    result = prime * result + ((_projects== null) ? 0 : _projects.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WorkspaceConfig other = (WorkspaceConfig) obj;
    if (_absoluteFileSystemPath == null) {
      if (other._absoluteFileSystemPath != null)
        return false;
    } else if (!_absoluteFileSystemPath.equals(other._absoluteFileSystemPath))
      return false;
    if (_projects == null) {
      if (other._projects != null)
        return false;
    } else if (!_projects.equals(other._projects))
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    String result = new ToStringHelper().toString(this);
    return result;
  }
}
