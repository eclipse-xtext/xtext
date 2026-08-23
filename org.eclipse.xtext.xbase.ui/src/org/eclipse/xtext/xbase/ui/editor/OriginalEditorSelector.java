/*******************************************************************************
 * Copyright (c) 2013, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
import org.eclipse.xtext.common.types.ui.trace.ITraceForTypeRootProvider;
import org.eclipse.xtext.generator.trace.AbsoluteURI;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.XtextEditorInfo;
import org.eclipse.xtext.ui.generator.trace.IEclipseTrace;
import org.eclipse.xtext.ui.generator.trace.ILocationInEclipseResource;
import org.eclipse.xtext.ui.generator.trace.ITraceForStorageProvider;
import org.eclipse.xtext.xbase.ui.editor.StacktraceBasedEditorDecider.Decision;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Moritz Eysholdt
 */
public class OriginalEditorSelector implements IEditorAssociationOverride {

	private static final Logger logger = Logger.getLogger(OriginalEditorSelector.class);

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

	@Override
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		IEditorDescriptor xbaseEditor = findXbaseEditor(editorInput, true);
		if (xbaseEditor != null) {
			List<IEditorDescriptor> result = Lists.asList(xbaseEditor, editorDescriptors);
			return result.toArray(IEditorDescriptor[]::new);
		}
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		IEditorDescriptor xbaseEditor = findXbaseEditor(fileName, true);
		if (xbaseEditor != null) {
			List<IEditorDescriptor> result = Lists.asList(xbaseEditor, editorDescriptors);
			return result.toArray(IEditorDescriptor[]::new);
		}
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IEditorDescriptor result = findXbaseEditor(editorInput, false);
		if (result != null)
			return result;
		return editorDescriptor;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IEditorDescriptor result = findXbaseEditor(fileName, false);
		if (result != null)
			return result;
		return editorDescriptor;
	}

	protected IEditorDescriptor findXbaseEditor(String fileName, boolean ignorePreference) {
		if (decisions.isJDI()) {
			String file = debugPluginListener.findXtextSourceFileNameForClassFile(fileName);
			if (file != null)
				return getXtextEditor(URI.createURI(file));
		}
		if (decisions.decideAccordingToCallerForSimpleFileName() == Decision.FORCE_JAVA) {
			return null;
		}
		IType type = findJavaTypeForSimpleFileName(fileName);
		if (type != null) {
			if (!ignorePreference) {
				IResource resource = type.getResource();
				if (resource != null) {
					try {
						// the user has chosen to always use a particular editor, e.g. by means of
						// Open With in the package explorer
						String favoriteEditor = resource.getPersistentProperty(IDE.EDITOR_KEY);
						if (favoriteEditor != null)
							return null;
					} catch (CoreException e) {
						logger.debug(e.getMessage(), e);
					}
				}
			}
			IEclipseTrace trace = traceForTypeRootProvider.getTraceToSource(type.getTypeRoot());
			return getXtextEditor(trace);
		}
		return null;
	}

	public IEditorDescriptor findXbaseEditor(IEditorInput editorInput, boolean ignorePreference) {
		IFile file = ResourceUtil.getFile(editorInput);
		if (file == null)
			return null;
		if (!ignorePreference) {
			if (file.exists()) {
				try {
					String favoriteEditor = file.getPersistentProperty(IDE.EDITOR_KEY);
					if (favoriteEditor != null)
						return null;
				} catch (CoreException e) {
					logger.debug(e.getMessage(), e);
				}
			}
		}
		// TODO stay in same editor if local navigation
		Decision decision = decisions.decideAccordingToCaller();
		if (decision == Decision.FORCE_JAVA) {
			return null;
		}
		IEclipseTrace traceToSource = traceInformation.getTraceToSource(file);
		return getXtextEditor(traceToSource);
	}

	/**
	 * @param traceToSource
	 */
	protected IEditorDescriptor getXtextEditor(IEclipseTrace traceToSource) {
		if (traceToSource != null) {
			Iterator<? extends ILocationInEclipseResource> sourceInformationIterator = traceToSource.getAllAssociatedLocations().iterator();
			if (sourceInformationIterator.hasNext()) {
				ILocationInEclipseResource sourceInformation = sourceInformationIterator.next();
				AbsoluteURI absoluteURI = sourceInformation.getAbsoluteResourceURI();
				if (absoluteURI != null) {
					URI uri = absoluteURI.getURI();
					return getXtextEditor(uri);
				}
			}
		}
		return null;
	}

	protected IType findJavaTypeForSimpleFileName(String name) {
		int index = name.lastIndexOf('.');
		if (index < 0)
			return null;
		String typeName = name.substring(0, index);
		String ext = name.substring(index + 1).toLowerCase();
		if (!ext.equals("class") && !ext.equals("java"))
			return null;
		final boolean searchForSources = ext.equals("java");
		final SearchResult result = findTypesBySimpleName(typeName, searchForSources);
		switch (result.foundTypes.size()) {
			case 1 : {
				return result.getExactMatch();
			}
			case 0 : {
				// check for nested types
				final String[] splitedName = typeName.split("\\$");
				if (splitedName.length > 1) {
					final SearchResult containerResult = findTypesBySimpleName(splitedName[0], searchForSources);
					IType candidate = null;
					for (IType currentType : containerResult.foundTypes) {
						for (int i = 1; i < splitedName.length; i++) {
							currentType = currentType.getType(splitedName[i]);
							if (currentType == null || !currentType.exists()) {
								break;
							} else if (i == splitedName.length-1) {
								if (candidate != null) {
									// multiple matches
									return null;
								}
								candidate = currentType;
							}
						}
					}
					return candidate;
				}
			}
			default : {
				return null;
			}
		}
	}

	static class SearchResult {
		public List<IType> foundTypes = new ArrayList<>();

		public IType getExactMatch() {
			if (hasExactMatch()) {
				return foundTypes.get(0);
			}
			return null;
		}

		public boolean hasExactMatch() {
			return foundTypes.size()==1;
		}
	}

	private SearchResult findTypesBySimpleName(String simpleTypeName, final boolean searchForSources) {
		final SearchResult result = new SearchResult();
		try {
			new SearchEngine().searchAllTypeNames(null, 0, // match all package names
					simpleTypeName.toCharArray(), SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
						IJavaSearchConstants.TYPE,
						SearchEngine.createWorkspaceScope(),
						new TypeNameMatchRequestor() {
							@Override
							public void acceptTypeNameMatch(TypeNameMatch match) {
								IPackageFragmentRoot fragmentRoot = match.getPackageFragmentRoot();
								boolean externalLib = fragmentRoot.isArchive() || fragmentRoot.isExternal();
								if (externalLib ^ searchForSources) {
									result.foundTypes.add(match.getType());
								}
							}
						}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, // wait for the jdt index to be ready
						new NullProgressMonitor());
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), e);
		}
		return result;
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
