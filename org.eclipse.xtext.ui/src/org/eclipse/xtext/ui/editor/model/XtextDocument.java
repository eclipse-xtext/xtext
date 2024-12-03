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

import static com.google.common.collect.Lists.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.DocumentRewriteSessionEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.resource.ISynchronizable;
import org.eclipse.xtext.resource.OutdatedStateManager;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.service.OperationCanceledError;
import org.eclipse.xtext.service.OperationCanceledManager;
import org.eclipse.xtext.ui.editor.DirtyStateEditorSupport;
import org.eclipse.xtext.ui.editor.model.IXtextDocumentContentObserver.Processor;
import org.eclipse.xtext.ui.editor.model.edit.ITextEditComposer;
import org.eclipse.xtext.ui.editor.model.edit.ReconcilingUnitOfWork;
import org.eclipse.xtext.ui.editor.model.edit.ReconcilingUnitOfWork.ReconcilingUnitOfWorkProvider;
import org.eclipse.xtext.ui.editor.validation.ValidationJob;
import org.eclipse.xtext.ui.util.DisplayRunnable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.CancelableUnitOfWork;
import org.eclipse.xtext.util.concurrent.IReadAccess;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.util.concurrent.IWriteAccess;

import com.google.inject.Inject;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Michael Clay
 * @author Jan Koehnlein
 */
public class XtextDocument extends Document implements IXtextDocument {

	private DocumentTokenSource tokenSource;

	private ITextEditComposer composer;

	@Inject
	private OutdatedStateManager outdatedStateManager;
	
	@Inject
	private OperationCanceledManager operationCanceledManager;
	
	@Inject(optional=true)
	private ReconcilingUnitOfWorkProvider reconcilingUnitOfWorkProvider = new ReconcilingUnitOfWorkProvider();
	
	/**
	 * @since 2.8
	 * @noreference This constructor is not intended to be referenced by clients. Only for testing
	 */
	public XtextDocument(DocumentTokenSource tokenSource, ITextEditComposer composer, OutdatedStateManager outdatedStateManager, OperationCanceledManager operationCanceledManager) {
		this(tokenSource, composer);
		this.outdatedStateManager = outdatedStateManager;
		this.operationCanceledManager = operationCanceledManager;
	}
	
	@Inject
	public XtextDocument(DocumentTokenSource tokenSource, ITextEditComposer composer) {
		this.tokenSource = tokenSource;
		tokenSource.computeDamageRegion(new DocumentEvent(this, 0, getLength(), this.get()));
		this.composer = composer;
	}

	private XtextResource resource = null;
	
	private final List<IXtextModelListener> modelListeners = new ArrayList<IXtextModelListener>();
	private final ListenerList<IXtextDocumentContentObserver> xtextDocumentObservers = new ListenerList<IXtextDocumentContentObserver>(ListenerList.IDENTITY);


	public void setInput(XtextResource resource) {
		Assert.isNotNull(resource);
		this.resource = resource;
	}

	public void disposeInput() {
		if (validationJob != null) {
			validationJob.cancel();
		}
		internalModify(new IUnitOfWork.Void<XtextResource>() {
			@Override
			public void process(XtextResource state) throws Exception {
				// the resource may already be null if the document was opened for a bogus
				// storage / stream - NPE guard here
				if (state != null) {
					// clean up resource set and resource to release resources
					state.getResourceSet().eSetDeliver(false);
					state.getResourceSet().eAdapters().clear();
					state.eSetDeliver(false);
					state.eAdapters().clear();
				}
				resource = null;
			}
		});
	}

	private final XtextDocumentLocker stateAccess = createDocumentLocker();

	protected XtextDocumentLocker createDocumentLocker() {
		return new XtextDocumentLocker();
	}

	@Override
	public <T> T readOnly(IUnitOfWork<T, XtextResource> work) {
		T readOnly = stateAccess.readOnly(work);
		return readOnly;
	}
	
	/**
	 * @since 2.7
	 */
	@Override
	public <T> T priorityReadOnly(IUnitOfWork<T, XtextResource> work) {
		return stateAccess.priorityReadOnly(work);
	}
	
	private final static IUnitOfWork.Void<XtextResource> noWork = new IUnitOfWork.Void<XtextResource>() {
		@Override
		public void process(XtextResource state) throws Exception {}
	};

	@Override
	public <T> T modify(IUnitOfWork<T, XtextResource> work) {
		// do a dummy read only, to make sure any scheduled changes get applied.
		readOnly(noWork);
		IUnitOfWork<T, XtextResource> reconcilingUnitOfWork = reconcilingUnitOfWorkProvider.<T>get(work, this, composer);
		return internalModify(reconcilingUnitOfWork);
	}

	/**
	 * Modifies the document's semantic model without reconciling the text nor the node model. For internal use only.
	 */
	public <T> T internalModify(IUnitOfWork<T, XtextResource> work) {
		return stateAccess.modify(work);
	}

	protected void ensureThatStateIsNotReturned(Object exec, IUnitOfWork<?, XtextResource> uow) {
		// TODO activate
		// if (exec instanceof EObject) {
		// if (((EObject) exec).eResource() == resource
		// || ((exec instanceof AbstractNode) &&
		// NodeUtil.getNearestSemanticObject((AbstractNode) exec)
		// .eResource() == resource))
		// throw new IllegalStateException(
		// "The unit of work returned state from the resource. This is causing race condition problems and therefore not allowed! "+uow.getClass().getName());
		// }
	}

	@Override
	public void addModelListener(IXtextModelListener listener) {
		Assert.isNotNull(listener);
		synchronized (modelListeners) {
			if (modelListeners.contains(listener))
				return;
			if (listener instanceof DirtyStateEditorSupport) {
				modelListeners.add(0,listener);
			} else {
				modelListeners.add(listener);
			}
		}
	}

	@Override
	public void removeModelListener(IXtextModelListener listener) {
		Assert.isNotNull(listener);
		synchronized (modelListeners) {
			modelListeners.remove(listener);
		}
	}

	protected void notifyModelListeners(final XtextResource res) {
		if (res == null || res != this.resource)
			return;
		List<IXtextModelListener> modelListenersCopy;
		synchronized (modelListeners) {
			modelListenersCopy = newArrayList(modelListeners);
		}
		CancelIndicator cancelIndicator = getCancelIndicator();
		for (IXtextModelListener listener : modelListenersCopy){
			try {
				if (res != this.resource) {
					return;
				}
				
				operationCanceledManager.checkCanceled(cancelIndicator);
				if (listener instanceof IXtextModelListenerExtension) {
					((IXtextModelListenerExtension)listener).modelChanged(res, cancelIndicator);
				} else {
					listener.modelChanged(res);
				}
			
			} catch(Exception exc) {
				operationCanceledManager.propagateIfCancelException(exc);
				log.error("Error in IXtextModelListener", exc);
			} catch (OperationCanceledError e) {
				throw e.getWrapped();
			}
		}
	}

	@Override
	public void addXtextDocumentContentObserver(IXtextDocumentContentObserver observer) {
		addDocumentListener(observer);
		xtextDocumentObservers.add(observer);
	}

	@Override
	public void removeXtextDocumentContentObserver(IXtextDocumentContentObserver observer) {
		xtextDocumentObservers.remove(observer);
		removeDocumentListener(observer);
	}

	/**
	 * @since 2.7
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected boolean updateContentBeforeRead() {
		Object[] listeners = xtextDocumentObservers.getListeners();
		boolean hadUpdates = false;
		for (int i = 0; i < listeners.length; i++) {
			hadUpdates |= ((IXtextDocumentContentObserver) listeners[i]).performNecessaryUpdates(stateAccess);
		}
		return hadUpdates;
	}
	
	/**
	 * @since 2.7
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected boolean hasPendingUpdates() {
		Object[] listeners = xtextDocumentObservers.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			if(((IXtextDocumentContentObserver) listeners[i]).hasPendingUpdates())
				return true;
		}
		return false;
	}
	
	/**
	 * @since 2.8
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public CancelIndicator getCancelIndicator() {
		if (resource == null) {
			return CancelIndicator.NullImpl;
		}
		return outdatedStateManager.newCancelIndicator(resource.getResourceSet());
	}
	
	/**
	 * @since 2.7
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void setOutdated(boolean outdated) {
		if (resource == null)
			return;
		if (outdated) {
			((XtextResourceSet)resource.getResourceSet()).markOutdated();
		} else {
			((XtextResourceSet)resource.getResourceSet()).markSynced();
		}
	}
	
	/**
	 * @author Sven Efftinge - Initial contribution and API
	 * 
	 */
	protected class XtextDocumentLocker implements Processor, IReadAccess<XtextResource>, Priority<XtextResource>, IWriteAccess<XtextResource> {
		
		private AtomicInteger potentialUpdaterCount = new AtomicInteger(0);
		
		private volatile boolean hadUpdates;
		
		private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

		private final Lock writeLock = rwLock.writeLock();

		private final Lock readLock = rwLock.readLock();

		private ThreadLocal<Integer> readLockCount = new ThreadLocal<Integer>() {
			@Override
			protected Integer initialValue() {
				return 0;
			}
		};
		
		/**
		 * Upgrades a read transaction to a write transaction, executes the work then downgrades to a read transaction
		 * again.
		 * 
		 * @noreference
		 * @since 2.7
		 */
		@Override
		public <T> T process(IUnitOfWork<T,XtextResource> transaction) {
			if (getReadHoldCount() != 1 || getWriteHoldCount() != 0) {
				throw new IllegalStateException(
						"Exactly one read lock and no write locks expected! But was read: "+getReadHoldCount()+", write:"+getWriteHoldCount());
			}
			releaseReadLock();
			// lock upgrade followed by downgrade as described in
			// java.util.concurrent.locks.ReentrantReadWriteLock
			// 
			// caveat: other readers/writers could potentially kick in here
			acquireWriteLock();
			try {
				if (log.isTraceEnabled())
					log.trace("process - " + Thread.currentThread().getName());
				return this.modify(transaction);
			} finally {
				if (log.isTraceEnabled())
					log.trace("Downgrading from write lock to read lock...");
				acquireReadLock();
				releaseWriteLock();
			}
		}

		/**
		 * Queries the number of reentrant write holds on this lock by the current thread. Delegates to
		 * {@link ReentrantReadWriteLock#getWriteHoldCount()}.
		 * 
		 * @return the number of holds on the write lock by the current thread, or zero if the write lock is not held by the
		 *         current thread
		 * @since 2.7
		 * @noreference
		 */
		protected int getWriteHoldCount() {
			return rwLock.getWriteHoldCount();
		}

		/**
		 * Queries the number of reentrant read holds on this lock by the current thread. A reader thread has a hold on a
		 * lock for each lock action that is not matched by an unlock action.
		 * 
		 * That functionality is implemented in {@link ReentrantReadWriteLock} as well, but not before version 1.6. This is
		 * why we have to find our own way to work around it.
		 * 
		 * @return the number of holds on the read lock by the current thread, or zero if the read lock is not held by the
		 *         current thread
		 * @since 2.7
		 * @noreference
		 */
		protected int getReadHoldCount() {
			return readLockCount.get();
		}

		private void acquireReadLock() {
			if (log.isTraceEnabled())
				log.trace("Trying to acquire read lock...");
			readLock.lock();
			readLockCount.set(readLockCount.get() + 1);
			if (log.isTraceEnabled())
				log.trace("...read lock acquired.");
		}

		private void releaseReadLock() {
			readLock.unlock();
			readLockCount.set(readLockCount.get() - 1);
			if (log.isTraceEnabled())
				log.trace("Read lock released.");
		}

		private void acquireWriteLock() {
			if (validationJob!=null) {
				validationJob.cancel();
			}
			setOutdated(true);
			if (log.isTraceEnabled())
				log.trace("Trying to acquire write lock...");
			writeLock.lock();
			if (log.isTraceEnabled())
				log.trace("...write lock acquired.");
			// next reader will get a fresh monitor instance
			setOutdated(false);
		}

		private void releaseWriteLock() {
			writeLock.unlock();
			if (log.isTraceEnabled())
				log.trace("Write lock released.");
		}
		
		protected XtextResource getState() {
			return resource;
		}

		@Override
		public <T> T modify(IUnitOfWork<T, XtextResource> work) {
			boolean isCancelable = work instanceof CancelableUnitOfWork;
			try {
				XtextResource state = getState();
				try {
					synchronized (getResourceLock()) {
						acquireWriteLock();
						T exec = null;
						try {
							potentialUpdaterCount.incrementAndGet();
							state = getState();
							if (log.isDebugEnabled())
								log.debug("write - " + Thread.currentThread().getName());
							exec = outdatedStateManager.exec(work, state);
							return exec;
						} catch (RuntimeException e) {
							throw e;
						} catch (Exception e) {
							throw new WrappedException(e);
						} catch (OperationCanceledError e) {
							throw e.getWrapped();
						} finally {
							try {
								// downgrade lock to read lock 
								acquireReadLock();
								releaseWriteLock();
								ensureThatStateIsNotReturned(exec, work);
								if (potentialUpdaterCount.decrementAndGet() == 0 && !(work instanceof ReconcilingUnitOfWork)) {
									notifyModelListenersOnUiThread();
								}
							} catch (RuntimeException e) {
								if (operationCanceledManager.isOperationCanceledException(e)) {
									if (isCancelable)
										throw e;
								} else {
									throw e;
								}
							} finally {
								releaseReadLock();
							}
						}
					}
				} catch (RuntimeException e) {
					try {
						if (state != null) {
							synchronized (getResourceLock(state)) {
								acquireWriteLock();
								try {
									state.reparse(get());
								} finally {
									releaseWriteLock();
								}
							}
						}
					} catch (IOException ioe) {
					}
					throw e;
				}
			} finally {
				if(!(work instanceof ReconcilingUnitOfWork))
					checkAndUpdateAnnotations();
			}
		}
		
		/**
		 * 
		 * @since 2.7
		 */
		@Override
		public <T> T priorityReadOnly(IUnitOfWork<T, XtextResource> work) {
			return internalReadOnly(work, true);
		}
		
		/**
		 * @since 2.4
		 */
		@Override
		public <T> T readOnly(IUnitOfWork<T, XtextResource> work) {
			return internalReadOnly(work, false);
		}

		/**
		 * @since 2.7
		 */
		protected <T> T internalReadOnly(IUnitOfWork<T, XtextResource> work, boolean isCancelReaders) {
			boolean isCancelable = work instanceof CancelableUnitOfWork;
			if(isCancelReaders) {
				if (Display.getCurrent() == null) {
					log.error("Priority read only called from non UI-thread.", new IllegalStateException());
				}
				cancelReaders(resource);
			}
			XtextResource state = getState();
			synchronized (getResourceLock()) {
				acquireReadLock();
				if(isCancelReaders) 
					setOutdated(false); 
				try {
					potentialUpdaterCount.incrementAndGet();
					if (log.isDebugEnabled())
						log.debug("read - " + Thread.currentThread().getName());
					// Don't updateContent on write lock request. Reentrant read doesn't matter as 
					// updateContentBeforeRead() is cheap when the pending event queue is swept
					if (getReadHoldCount() == 1 && getWriteHoldCount() == 0) {
						hadUpdates |= updateContentBeforeRead();
					}
					T exec = outdatedStateManager.exec(work,state);
					ensureThatStateIsNotReturned(exec, work);
					return  exec;
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new WrappedException(e);
				} catch (OperationCanceledError e) {
					throw e.getWrapped();
				} finally {
					try {
						if(potentialUpdaterCount.decrementAndGet() == 0 && (hadUpdates || isCancelReaders)) {
							boolean wasHadUpdates = hadUpdates;
							hadUpdates = false;
							if (getCancelIndicator().isCanceled() && isCancelable)
								throw new OperationCanceledException();
							if (wasHadUpdates) {
								notifyModelListenersOnUiThread();
							}
						}
					} catch (RuntimeException e) {
						if (operationCanceledManager.isOperationCanceledException(e)) {
							if (isCancelable)
								throw e;
						} else {
							throw e;
						}
					} finally {
						releaseReadLock();
					}
				}
			}
		}

		/**
		 * Run the model listeners on the UI thread.
		 * 
		 * @note Some of the {@link IXtextModelListener}s assume that the {@link XtextDocument} that is associated with the model is not
		 *       changing while they run. To achieve this, we run the {@link IXtextModelListener}s on the UI thread.
		 */
		private void notifyModelListenersOnUiThread() {
			synchronized (modelListeners) {
				if (modelListeners.isEmpty()) {
					return;
				}
			}
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (Thread.currentThread() == display.getThread()) {
				// We are already running on the display thread.  Run the listeners immediately.
				notifyModelListeners(getState());
			} else {
				display.asyncExec(() -> {
					XtextDocument.this.tryReadOnly(((XtextResource resource) -> {
						notifyModelListeners(resource);
						return null;
					}));
				});
			}
		}
	}

	private void cancelReaders(XtextResource resource) {
		if (validationJob != null) {
			validationJob.cancel();
		}
		setOutdated(true);
	}
	
	/**
	 * Introduced in 2.7 to allow read-only transactions to be cancelable. The default implementation disables
	 * concurrent reads on the model. To re-enable concurrent reads, return a new {@link Object} on each call.
	 * 
	 * Caveat: Concurrent read is problematic in EMF because proxy resolution and resource un-/loading are considered
	 * read-only but actually change the model. This yields serious race conditions. Consider using 
	 * {@link CancelableUnitOfWork}s instead.
	 * 
	 * @since 2.7
	 */
	protected Object getResourceLock() {
		return getResourceLock(resource);
	}

	private Object getResourceLock(XtextResource r) {
		if (r != null) {
			return (r instanceof ISynchronizable<?>) ? ((ISynchronizable<?>) r).getLock() : r;
		} else {
			return this;
		}
	}
	
	private static final Logger log = Logger.getLogger(XtextDocument.class);

	private transient Job validationJob;

	public void setValidationJob(Job validationJob) {
		this.validationJob = validationJob;
	}

	public Job getValidationJob() {
		return validationJob;
	}

	public void checkAndUpdateAnnotations() {
		if (validationJob!=null) {
			validationJob.cancel();
			if (resource != null && !getCancelIndicator().isCanceled()) {
				validationJob.schedule();
			}
		}
	}
	
	/**
	 * Returns the {@link URI uri} of the associated {@link org.eclipse.emf.ecore.resource.Resource emf resource}.
	 * May be null if no resource is available or its uri is <code>null</code>.
	 * @return the resource uri if available.
	 * @since 2.1
	 */
	@Override
	public URI getResourceURI() {
		XtextResource resource = this.resource;
		if (resource != null)
			return resource.getURI();
		return null;
	}

	@Override
	public <T> T getAdapter(Class<T> adapterType) {
		if (ValidationJob.class.equals(adapterType)) {
			return adapterType.cast(validationJob);
		}
		return IXtextDocument.super.getAdapter(adapterType);
	}

	/*
	 * fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=297946
	 */
	private ReadWriteLock positionsLock = new ReentrantReadWriteLock();
	private Lock positionsReadLock = positionsLock.readLock();
	private Lock positionsWriteLock = positionsLock.writeLock();

	@Override
	public Position[] getPositions(String category, int offset, int length, boolean canStartBefore, boolean canEndAfter)
			throws BadPositionCategoryException {
		positionsReadLock.lock();
		try {
			return super.getPositions(category, offset, length, canStartBefore, canEndAfter);
		} finally {
			positionsReadLock.unlock();
		}
	}

	@Override
	public Position[] getPositions(String category) throws BadPositionCategoryException {
		positionsReadLock.lock();
		try {
			return super.getPositions(category);
		} finally {
			positionsReadLock.unlock();
		}
	}

	@Override
	public void addPosition(Position position) throws BadLocationException {
		positionsWriteLock.lock();
		try {
			super.addPosition(position);
		} finally {
			positionsWriteLock.unlock();
		}
	}

	@Override
	public void addPosition(String category, Position position) throws BadLocationException,
			BadPositionCategoryException {
		positionsWriteLock.lock();
		try {
			super.addPosition(category, position);
		} finally {
			positionsWriteLock.unlock();
		}
	}

	@Override
	public void removePosition(Position position) {
		positionsWriteLock.lock();
		try {
			super.removePosition(position);
		} finally {
			positionsWriteLock.unlock();
		}
	}

	@Override
	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		positionsWriteLock.lock();
		try {
			super.removePosition(category, position);
		} finally {
			positionsWriteLock.unlock();
		}
	}

	@Override
	protected void fireDocumentChanged(DocumentEvent event) {
		new DisplayRunnable() {
			@Override
			protected void run() throws Exception {
				cancelReaders(resource);
				tokenSource.updateStructure(event);
				XtextDocument.super.fireDocumentChanged(event);
			}
		}.syncExec();
	}

	@Override
	protected void fireRewriteSessionChanged(DocumentRewriteSessionEvent event) {
		new DisplayRunnable() {
			@Override
			protected void run() throws Exception {
				XtextDocument.super.fireRewriteSessionChanged(event);
			}
		}.syncExec();
	}

	public IRegion getLastDamage() {
		return tokenSource.getLastDamagedRegion();
	}

	public Iterable<ILexerTokenRegion> getTokens() {
		return tokenSource.getTokenInfos();
	}
}
