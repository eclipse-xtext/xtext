/**
 * Copyright (c) 2014, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.resource.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.util.internal.Stopwatches;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * A resource implementation that can load itself from ResourceStorage.
 * 
 * @author Sven Efftinge - Initial contribution and API
 */
public class StorageAwareResource extends LazyLinkingResource {
	public static final String UNRESOLVABLE_FRAGMENT = "UNRESOLVABLE";

	private static final Logger LOG = Logger.getLogger(StorageAwareResource.class);

	@Inject(optional = true)
	private IResourceStorageFacade resourceStorageFacade;

	@Inject
	private PortableURIs portableURIs;

	private boolean isLoadedFromStorage = false;

	private IResourceDescription resourceDescription = null;

	private static final int STACK_TRACE_LIMIT = 10;

	/**
	 * Modify the argument StackOverflowError so that it only retains the first and last {@link #STACK_TRACE_LIMIT}
	 * stack trace elements of {@code ex} if it has more than that.
	 *
	 * @param ex
	 *            the StackOverflowError; may get modified
	 */
	@SuppressWarnings("nls")
	private static void trimStackOverflowErrorStackTrace(final StackOverflowError ex) {
		int stackTraceLength = ex.getStackTrace().length;
		if (stackTraceLength > STACK_TRACE_LIMIT * 2 + 1) {
			StackTraceElement[] stackTraceElements = new StackTraceElement[(STACK_TRACE_LIMIT * 2) + 1];
			System.arraycopy(ex.getStackTrace(), 0, stackTraceElements, 0, STACK_TRACE_LIMIT);
			stackTraceElements[STACK_TRACE_LIMIT] = new StackTraceElement("", "\n\t\t\t <Skipped multiple lines> \n",
					null, -1);
			System.arraycopy(ex.getStackTrace(), stackTraceLength - STACK_TRACE_LIMIT, stackTraceElements,
					STACK_TRACE_LIMIT + 1, STACK_TRACE_LIMIT);
			ex.setStackTrace(stackTraceElements);
		}
	}
	  
	private void clearAndUnload() {
		if (contents != null) {
			contents.clear();
		}
		if (eAdapters != null) {
			eAdapters.clear();
		}
		unload();		
	}
	
	@Override
	public void load(Map<?, ?> options) throws IOException {
		if (!isLoaded && !isLoading && resourceStorageFacade != null
				&& resourceStorageFacade.shouldLoadFromStorage(this)) {
			LOG.debug("Loading " + getURI() + " from storage.");
			try {
				ResourceStorageLoadable in = resourceStorageFacade.getOrCreateResourceStorageLoadable(this);
				loadFromStorage(in);
				return;
			} catch (IOException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Failed to load " + getURI() + " from storage", e);
				}
				clearAndUnload();
			} catch (StackOverflowError e) {
				trimStackOverflowErrorStackTrace(e);
				LOG.warn("Failed to load " + uri + " from storage", e); //$NON-NLS-1$//$NON-NLS-2$
				clearAndUnload();
			}
		}
		super.load(options);
	}

	public void loadFromStorage(ResourceStorageLoadable storageInputStream) throws IOException {
		// check the argument for null before the internal state is modified
		Preconditions.checkNotNull(storageInputStream, "storageInputStream");
		Stopwatches.StoppedTask task = Stopwatches.forTask("Loading from storage");
		task.start();
		isLoading = true;
		isLoadedFromStorage = true;
		try {
			storageInputStream.loadIntoResource(this);
			isLoaded = true;
		} finally {
			isLoading = false;
			task.stop();
		}
	}

	@Override
	protected void doUnload() {
		super.doUnload();
		isLoadedFromStorage = false;
	}

	@Override
	protected void clearInternalState() {
		isLoadedFromStorage = false;
		super.clearInternalState();
	}

	@Override
	public synchronized EObject getEObject(String uriFragment) {
		if (portableURIs.isPortableURIFragment(uriFragment)) {
			return portableURIs.resolve(this, uriFragment);
		}
		return super.getEObject(uriFragment);
	}

	@Override
	protected Set<String> getUnresolvableURIFragments() {
		if (isLoadedFromStorage) {
			return Collections.singleton(StorageAwareResource.UNRESOLVABLE_FRAGMENT);
		}
		return super.getUnresolvableURIFragments();
	}

	public IResourceStorageFacade getResourceStorageFacade() {
		return resourceStorageFacade;
	}

	public PortableURIs getPortableURIs() {
		return portableURIs;
	}

	public boolean isLoadedFromStorage() {
		return isLoadedFromStorage;
	}

	public void setIsLoadedFromStorage(boolean isLoadedFromStorage) {
		this.isLoadedFromStorage = isLoadedFromStorage;
	}

	public IResourceDescription getResourceDescription() {
		return resourceDescription;
	}

	public void setResourceDescription(IResourceDescription resourceDescription) {
		this.resourceDescription = resourceDescription;
	}
	
	protected boolean customWriteNodeModel(OutputStream outputStream) throws IOException {
		return getParseResultWrapper().customWriteNodeModel(this, outputStream);
	}
	
	protected boolean customReadNodeModel(InputStream inputStream) throws IOException {
		return getParseResultWrapper().customReadNodeModel(this, inputStream);
	}
}
