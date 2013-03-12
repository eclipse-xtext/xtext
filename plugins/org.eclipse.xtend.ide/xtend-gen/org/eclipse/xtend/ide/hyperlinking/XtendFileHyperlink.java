package org.eclipse.xtend.ide.hyperlinking;

import com.google.common.base.Objects;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class XtendFileHyperlink implements IHyperlink {
  private String fileName;
  
  private int lineNumber;
  
  private IWorkbench workbench;
  
  private TextConsole console;
  
  public XtendFileHyperlink(final String fileName, final IWorkbench workbench, final TextConsole console) {
    final int indexOfColon = fileName.indexOf(":");
    int _minus = (-1);
    boolean _notEquals = (indexOfColon != _minus);
    if (_notEquals) {
      String _substring = fileName.substring(0, indexOfColon);
      this.fileName = _substring;
      int _plus = (indexOfColon + 1);
      String _substring_1 = fileName.substring(_plus);
      Integer _valueOf = Integer.valueOf(_substring_1);
      this.lineNumber = (_valueOf).intValue();
    } else {
      this.fileName = fileName;
    }
    this.workbench = workbench;
    this.console = console;
  }
  
  public void linkActivated() {
    try {
      try {
        ILaunch _launch = this.getLaunch();
        ISourceLocator _sourceLocator = _launch.getSourceLocator();
        final ISourceLocator l = _sourceLocator;
        boolean _matched = false;
        if (!_matched) {
          if (l instanceof AbstractSourceLookupDirector) {
            final AbstractSourceLookupDirector _abstractSourceLookupDirector = (AbstractSourceLookupDirector)l;
            _matched=true;
            final Object result = _abstractSourceLookupDirector.getSourceElement(this.fileName);
            boolean _matched_1 = false;
            if (!_matched_1) {
              if (result instanceof IFile) {
                final IFile _iFile = (IFile)result;
                _matched_1=true;
                IWorkbenchWindow _activeWorkbenchWindow = this.workbench.getActiveWorkbenchWindow();
                IWorkbenchPage _activePage = _activeWorkbenchWindow.getActivePage();
                final IEditorPart editor = IDE.openEditor(_activePage, _iFile);
                boolean _matched_2 = false;
                if (!_matched_2) {
                  if (editor instanceof XtextEditor) {
                    final XtextEditor _xtextEditor = (XtextEditor)editor;
                    _matched_2=true;
                    IXtextDocument _document = _xtextEditor.getDocument();
                    int _minus = (this.lineNumber - 1);
                    final IRegion region = _document.getLineInformation(_minus);
                    int _offset = region.getOffset();
                    int _length = region.getLength();
                    _xtextEditor.selectAndReveal(_offset, _length);
                  }
                }
              }
            }
          }
        }
      } catch (final Throwable _t) {
        if (_t instanceof NumberFormatException) {
          final NumberFormatException e = (NumberFormatException)_t;
        } else {
          throw Exceptions.sneakyThrow(_t);
        }
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void linkEntered() {
  }
  
  public void linkExited() {
  }
  
  private ILaunch getLaunch() {
    Object _attribute = this.console.getAttribute(IDebugUIConstants.ATTR_CONSOLE_PROCESS);
    final IProcess process = ((IProcess) _attribute);
    boolean _notEquals = (!Objects.equal(process, null));
    if (_notEquals) {
      return process.getLaunch();
    }
    return null;
  }
}
