/*******************************************************************************
 * Copyright (c) 2026 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.model;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IPath;

/**
 * Adapter factory for {@link XtextDocument}.
 *
 * <p>This factory provides conversions from an Xtext document to commonly used
 * Eclipse types so callers can obtain resource information in a standard way:
 * <ul>
 *   <li>{@link URI} - the document's URI when available</li>
 *   <li>{@link IFile} - the workspace file backing the document</li>
 *   <li>{@link IPath} - the workspace path derived from the file or resource</li>
 * </ul>
 *
 * Use via {@code Adapters.adapt(document, <Type>.class, true)} or
 * {@code document.getAdapter(<Type>.class)}.
 *
 * @author Raghunandana Murthappa - Initial contribution and API
 */
public class XtextDocumentAdapterFactory implements IAdapterFactory {
    private static final Class<?>[] ADAPTER_LIST = new Class<?>[] { URI.class, IFile.class, IPath.class };

    @Override
    public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        XtextDocument doc = (XtextDocument) adaptableObject;
        
        URI uri = doc.getAdapter(URI.class);
        if (uri != null && adapterType.isInstance(uri)) {
            return adapterType.cast(uri);
        }
        
        IFile file = doc.getAdapter(IFile.class);
        if (file != null && adapterType.isInstance(file)) {
            return adapterType.cast(file);
        }

        if (IPath.class.equals(adapterType)) {
            if (file != null) {
                IPath fullPath = file.getFullPath();
                if (fullPath != null && adapterType.isInstance(fullPath)) {
                    return adapterType.cast(fullPath);
                }
            }
        }
        
        return null;
    }

    @Override
    public Class<?>[] getAdapterList() {
        return ADAPTER_LIST;
    }
}