package org.eclipse.xtext.common.types.shared.jdt38;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IEditorAssociationOverride;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.xtext.builder.trace.ITraceForTypeRootProvider;
import org.eclipse.xtext.generator.trace.ILocationInResource;
import org.eclipse.xtext.generator.trace.ITrace;
import org.eclipse.xtext.generator.trace.ITraceForStorageProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.XtextEditorInfo;
import org.eclipse.xtext.xbase.ui.editor.StacktraceBasedEditorDecider;
import org.eclipse.xtext.xbase.ui.editor.StacktraceBasedEditorDecider.Decision;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Moritz Eysholdt
 */
@SuppressWarnings("restriction")
public class OriginalEditorSelector implements IEditorAssociationOverride {

	private static final Logger logger = Logger. getLogger(OriginalEditorSelector.class);
	
	@Inject
	private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	@Inject
	private ITraceForStorageProvider traceInformation;
	
	@Inject
	private IWorkbench workbench;
	
	@Inject
	private StacktraceBasedEditorDecider decisions;
	
	@Inject
	private DebugPluginListener debugPluginListener;
	
	@Inject
	private ITraceForTypeRootProvider traceForTypeRootProvider;
	
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		IEditorDescriptor xbaseEditor = findXbaseEditor(editorInput);
		if (xbaseEditor != null) {
			List<IEditorDescriptor> result = Lists.asList(xbaseEditor, editorDescriptors);
			return (IEditorDescriptor[]) result.toArray(new IEditorDescriptor[result.size()]);
		}
		return editorDescriptors;
	}

	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		IEditorDescriptor xbaseEditor = findXbaseEditor(fileName);
		if (xbaseEditor != null) {
			List<IEditorDescriptor> result = Lists.asList(xbaseEditor, editorDescriptors);
			return (IEditorDescriptor[]) result.toArray(new IEditorDescriptor[result.size()]);
		}
		return editorDescriptors;
	}

	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IEditorDescriptor result = findXbaseEditor(editorInput);
		if (result != null)
			return result;
		return editorDescriptor;
	}

	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IEditorDescriptor result = findXbaseEditor(fileName);
		if (result != null)
			return result;
		return editorDescriptor;
	}
	
	private IEditorDescriptor findXbaseEditor(String fileName) {
		String file = debugPluginListener.findXtextSourceFileNameForClassFile(fileName);
		if (file != null)
			return getXtextEditor(URI.createURI(file));
		IType type = findJavaTypeForSimpleClassFileName(fileName);
		if (type != null) {
			ITrace trace = traceForTypeRootProvider.getTraceToSource(type.getTypeRoot());
			return getXtextEditor(trace);
		}
		return null;
	}

	public IEditorDescriptor findXbaseEditor(IEditorInput editorInput) {
		IFile file = ResourceUtil.getFile(editorInput);
		if (file == null)
			return null;
		try {
			String favoriteEditor = file.getPersistentProperty(IDE.EDITOR_KEY);
			if (favoriteEditor != null)
				return null;
		} catch (CoreException e) {
			logger.debug(e.getMessage(), e);
		}
		// TODO stay in same editor if local navigation
		Decision decision = decisions.decideAccordingToCaller();
		if (decision == Decision.FORCE_JAVA) {
			return null;
		}
		ITrace traceToSource = traceInformation.getTraceToSource(file);
		return getXtextEditor(traceToSource);
	}

	/**
	 * @param traceToSource
	 */
	protected IEditorDescriptor getXtextEditor(ITrace traceToSource) {
		if (traceToSource != null) {
			Iterator<ILocationInResource> sourceInformationIterator = traceToSource.getAllAssociatedLocations().iterator();
			if (sourceInformationIterator.hasNext()) {
				URI uri = sourceInformationIterator.next().getResourceURI();
				return getXtextEditor(uri);
			}
		}
		return null;
	}

	protected IType findJavaTypeForSimpleClassFileName(String name) {
		if (!name.endsWith(".class"))
			return null;
		final IType[] foundType = new IType[1];
		try {
			char[] typeName = name.substring(0, name.length() - ".class".length()).toCharArray();
			new SearchEngine().searchAllTypeNames(null, 0, // match all package names
					typeName, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE, // and all type names,
					IJavaSearchConstants.TYPE, // search for types
					SearchEngine.createWorkspaceScope(), // in the scope of the current project
					new TypeNameMatchRequestor() {
						@Override
						public void acceptTypeNameMatch(TypeNameMatch match) {
							foundType[0] = match.getType();
						}
					}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, // wait for the jdt index to be ready
					new NullProgressMonitor());
		} catch (JavaModelException e) {
			logger.error(e);
		}
		return foundType[0];
	}

	protected IEditorDescriptor getXtextEditor(URI uri) {
		IResourceServiceProvider serviceProvider = resourceServiceProviderRegistry.getResourceServiceProvider(uri);
		if (serviceProvider != null) {
			XtextEditorInfo editorInfo = serviceProvider.get(XtextEditorInfo.class);
			if (editorInfo != null) {
				IEditorRegistry editorRegistry = workbench.getEditorRegistry();
				IEditorDescriptor result = editorRegistry.findEditor(editorInfo.getEditorId());
				return result; // null is ok
			}
		}
		return null;
	}

}
