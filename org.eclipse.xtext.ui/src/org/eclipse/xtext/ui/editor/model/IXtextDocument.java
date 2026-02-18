/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IReadAccess;
import org.eclipse.xtext.util.concurrent.IWriteAccess;

/**
 * @author Sebastian Zarnekow
 * @author Sven Efftinge
 * @author Jan Koehnlein
 */
public interface IXtextDocument extends IDocument, IDocumentExtension3, IReadAccess<XtextResource>, IReadAccess.Priority<XtextResource>, IWriteAccess<XtextResource>, IAdaptable {

	/**
	 * Supported adapter types are {@link IFile}, {@link IResource}, {@link URI} and real supertypes of the implementation class, e.g. all
	 * document extensions.
	 */
	@Override
	default public <T> T getAdapter(Class<T> adapterType) {
		if (adapterType.isInstance(this)) {
			return adapterType.cast(this);
		}
		if (URI.class.equals(adapterType)) {
			return adapterType.cast(getResourceURI());
		}
		if (IFile.class.equals(adapterType) || IResource.class.equals(adapterType)) {
			URI resourceURI = getResourceURI();
			if (resourceURI.isPlatformResource()) {
				return adapterType.cast(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(resourceURI.toPlatformString(true))));
			}
		}
		return Platform.getAdapterManager().getAdapter(this, adapterType);
	}

	/**
	 * @since 2.19
	 */
	default public URI getResourceURI() {
		return tryReadOnly(r -> r.getURI());
	}
	
	public void addModelListener(IXtextModelListener listener);

	public void removeModelListener(IXtextModelListener listener);

	void addXtextDocumentContentObserver(IXtextDocumentContentObserver listener);

	void removeXtextDocumentContentObserver(IXtextDocumentContentObserver listener);

}
