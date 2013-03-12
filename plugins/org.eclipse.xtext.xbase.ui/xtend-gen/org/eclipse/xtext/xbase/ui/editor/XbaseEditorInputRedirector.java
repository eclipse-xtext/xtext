package org.eclipse.xtext.xbase.ui.editor;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.LanguageInfo;
import org.eclipse.xtext.generator.trace.ILocationInResource;
import org.eclipse.xtext.generator.trace.ITrace;
import org.eclipse.xtext.generator.trace.ITraceForStorageProvider;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.ui.editor.XtextReadonlyEditorInput;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class XbaseEditorInputRedirector {
  @Inject
  private ITraceForStorageProvider traceInformation;
  
  @Inject
  private LanguageInfo languageInfo;
  
  @Inject
  private FileExtensionProvider fileExtensionProvider;
  
  public IEditorInput findOriginalSource(final IEditorInput input) {
    try {
      final IFile resource = ResourceUtil.getFile(input);
      boolean _notEquals = (!Objects.equal(resource, null));
      if (_notEquals) {
        IPath _fullPath = resource.getFullPath();
        String _fileExtension = _fullPath.getFileExtension();
        boolean _isValid = this.fileExtensionProvider.isValid(_fileExtension);
        if (_isValid) {
          IProject _project = resource.getProject();
          final IJavaProject project = JavaCore.create(_project);
          boolean _exists = project.exists();
          if (_exists) {
            IPath _outputLocation = project.getOutputLocation();
            IPath _fullPath_1 = resource.getFullPath();
            boolean _isPrefixOf = _outputLocation.isPrefixOf(_fullPath_1);
            if (_isPrefixOf) {
              IPath _fullPath_2 = resource.getFullPath();
              IPath _outputLocation_1 = project.getOutputLocation();
              int _segmentCount = _outputLocation_1.segmentCount();
              final IPath relative = _fullPath_2.removeFirstSegments(_segmentCount);
              IPackageFragmentRoot[] _packageFragmentRoots = project.getPackageFragmentRoots();
              final Function1<IPackageFragmentRoot,Boolean> _function = new Function1<IPackageFragmentRoot,Boolean>() {
                  public Boolean apply(final IPackageFragmentRoot it) {
                    try {
                      int _kind = it.getKind();
                      boolean _equals = (_kind == IPackageFragmentRoot.K_SOURCE);
                      return Boolean.valueOf(_equals);
                    } catch (Throwable _e) {
                      throw Exceptions.sneakyThrow(_e);
                    }
                  }
                };
              Iterable<IPackageFragmentRoot> _filter = IterableExtensions.<IPackageFragmentRoot>filter(((Iterable<IPackageFragmentRoot>)Conversions.doWrapArray(_packageFragmentRoots)), _function);
              for (final IPackageFragmentRoot source : _filter) {
                {
                  IResource _correspondingResource = source.getCorrespondingResource();
                  IPath _projectRelativePath = _correspondingResource.getProjectRelativePath();
                  final IPath fullPath = _projectRelativePath.append(relative);
                  IProject _project_1 = resource.getProject();
                  final IFile newFile = _project_1.getFile(fullPath);
                  boolean _exists_1 = newFile.exists();
                  if (_exists_1) {
                    FileEditorInput _fileEditorInput = new FileEditorInput(newFile);
                    return _fileEditorInput;
                  }
                }
              }
            }
          }
          return input;
        }
        final ITrace trace = this.traceInformation.getTraceToSource(resource);
        boolean _equals = Objects.equal(trace, null);
        if (_equals) {
          return input;
        }
        Iterable<ILocationInResource> _allAssociatedLocations = trace.getAllAssociatedLocations();
        final Iterator<ILocationInResource> allLocations = _allAssociatedLocations.iterator();
        ILocationInResource sourceInformation = null;
        boolean _and = false;
        boolean _hasNext = allLocations.hasNext();
        if (!_hasNext) {
          _and = false;
        } else {
          boolean _equals_1 = Objects.equal(sourceInformation, null);
          _and = (_hasNext && _equals_1);
        }
        boolean _while = _and;
        while (_while) {
          {
            final ILocationInResource candidate = allLocations.next();
            LanguageInfo _language = candidate.getLanguage();
            boolean _equals_2 = this.languageInfo.equals(_language);
            if (_equals_2) {
              sourceInformation = candidate;
            }
          }
          boolean _and_1 = false;
          boolean _hasNext_1 = allLocations.hasNext();
          if (!_hasNext_1) {
            _and_1 = false;
          } else {
            boolean _equals_2 = Objects.equal(sourceInformation, null);
            _and_1 = (_hasNext_1 && _equals_2);
          }
          _while = _and_1;
        }
        boolean _equals_2 = Objects.equal(sourceInformation, null);
        if (_equals_2) {
          return input;
        }
        final IStorage originalStorage = sourceInformation.getStorage();
        boolean _matched = false;
        if (!_matched) {
          if (originalStorage instanceof IFile) {
            final IFile _iFile = (IFile)originalStorage;
            _matched=true;
            FileEditorInput _fileEditorInput = new FileEditorInput(_iFile);
            return _fileEditorInput;
          }
        }
        XtextReadonlyEditorInput _xtextReadonlyEditorInput = new XtextReadonlyEditorInput(originalStorage);
        return _xtextReadonlyEditorInput;
      }
      return input;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected IPackageFragmentRoot _getPackageFragmentRoot(final IJavaElement element) {
    IJavaElement _parent = element.getParent();
    return this.getPackageFragmentRoot(_parent);
  }
  
  protected IPackageFragmentRoot _getPackageFragmentRoot(final IPackageFragmentRoot element) {
    return element;
  }
  
  protected IPackageFragmentRoot _getPackageFragmentRoot(final Void element) {
    IllegalStateException _illegalStateException = new IllegalStateException("couldn\'t find source package fragment root.");
    throw _illegalStateException;
  }
  
  public IPackageFragmentRoot getPackageFragmentRoot(final IJavaElement element) {
    if (element instanceof IPackageFragmentRoot) {
      return _getPackageFragmentRoot((IPackageFragmentRoot)element);
    } else if (element != null) {
      return _getPackageFragmentRoot(element);
    } else if (element == null) {
      return _getPackageFragmentRoot((Void)null);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(element).toString());
    }
  }
}
