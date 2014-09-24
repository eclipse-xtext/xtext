/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ui.generator.trace;

import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.xtext.generator.trace.ILocationInResource;
import org.eclipse.xtext.generator.trace.ITrace;
import org.eclipse.xtext.generator.trace.ITraceForStorageProvider;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.ITextRegion;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class TraceBasedOpenerContributor extends OppositeFileOpenerContributor {

	@Inject
	private ITraceForStorageProvider traceForStorageProvider;

	protected StorageBasedTextEditorOpener createStorageBasedTextEditorOpener(IStorage storage, ITextRegion textRegion) {
		return new StorageBasedTextEditorOpener(storage, textRegion);
	}

	@Override
	public boolean collectGeneratedFileOpeners(IEditorPart editor, IAcceptor<FileOpener> acceptor) {
		IStorage editorStorage = getStorage(editor);
		if (editorStorage != null) {
			ITrace trace = traceForStorageProvider.getTraceToTarget(editorStorage);
			if (((ITrace.Internal) trace).getRootTraceRegion() != null) {
				collectOpeners(trace, getSelectedRegion(editor), acceptor);
				return true;
			}
		}
		return false;
	}

	private void collectOpeners(ITrace trace, ITextRegion region, IAcceptor<FileOpener> acceptor) {
		Iterable<ILocationInResource> locations = null;
		if (region != null)
			locations = trace.getAllAssociatedLocations(region);
		if (locations == null || Iterables.isEmpty(locations))
			locations = trace.getAllAssociatedLocations();
		Map<IStorage, ITextRegion> result = Maps.newHashMap();
		for (ILocationInResource location : locations) {
			IStorage storage = location.getStorage();
			ITextRegion old = result.put(storage, location.getTextRegion());
			if (old != null) {
				ITextRegion merged = old.merge(location.getTextRegion());
				result.put(storage, merged);
			}
		}
		for (Map.Entry<IStorage, ITextRegion> e : result.entrySet()) {
			IStorage storage = e.getKey();
			ITextRegion textRegion = e.getValue();
			acceptor.accept(createStorageBasedTextEditorOpener(storage, textRegion));
		}
	}

	@Override
	public boolean collectSourceFileOpeners(IEditorPart editor, IAcceptor<FileOpener> acceptor) {
		IStorage editorStorage = getStorage(editor);
		if (editorStorage != null) {
			ITrace trace = traceForStorageProvider.getTraceToSource(editorStorage);
			if (((ITrace.Internal) trace).getRootTraceRegion() != null) {
				collectOpeners(trace, getSelectedRegion(editor), acceptor);
				return true;
			}
		}
		return false;
	}

}
