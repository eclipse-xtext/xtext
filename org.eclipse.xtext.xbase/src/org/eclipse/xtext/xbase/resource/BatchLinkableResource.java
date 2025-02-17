/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.resource;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.common.types.impl.JvmDeclaredTypeImplCustom;
import org.eclipse.xtext.common.types.xtext.JvmMemberInitializableResource;
import org.eclipse.xtext.diagnostics.DiagnosticMessage;
import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.lazy.LazyURIEncoder;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.CompilerPhases;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.resource.IBatchLinkableResource;
import org.eclipse.xtext.resource.ISynchronizable;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Triple;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.inject.Inject;

/**
 * A specialized EMF resource that is capable of resolving proxies in batch mode.
 * That is, on {@link #getEObject(String)}, the {@link BatchLinkingService} is used
 * to resolve a chunk of proxies. 
 * 
 * @author Sebastian Zarnekow - Linking assumptions
 */
public class BatchLinkableResource extends DerivedStateAwareResource implements ISynchronizable<BatchLinkableResource>, IBatchLinkableResource, JvmMemberInitializableResource {
	
	private static final Logger log = Logger.getLogger(BatchLinkableResource.class);
	
	@Inject
	private BatchLinkingService batchLinkingService;
	
	@Inject
	private CompilerPhases compilerPhases;
	
	private Set<Runnable> jvmMemberInitializers = null;
	
	private boolean hasJvmMemberInitializers = false;
	
	private boolean isInitializingJvmMembers = false;
	
	/**
	 * Returns the lock of the owning {@link ResourceSet}, if it exposes such a lock.
	 * Otherwise this resource itself is used as the lock context.
	 */
	/* @NonNull */
	@Override
	public Object getLock() {
		ResourceSet resourceSet = getResourceSet();
		if (resourceSet instanceof ISynchronizable<?>) {
			return ((ISynchronizable<?>) resourceSet).getLock();
		}
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @since 2.4
	 */
	/* @Nullable */
	@Override
	public <Result> Result execute(/* @NonNull */ IUnitOfWork<Result, ? super BatchLinkableResource> unit) throws Exception {
		synchronized (getLock()) {
			return unit.exec(this);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Delegates to the {@link BatchLinkingService} if the requested reference is 
	 * {@link BatchLinkingService#isBatchLinkable(EReference) linkeable in batch mode}.
	 * 
	 * Implementation detail: This specialization of {@link #getEObject(String) getEObject}
	 * synchronizes on the {@link #getLock() lock} which is exposed by the synchronizable
	 * resource rather than on the resource directly. This guards against reentrant resolution
	 * from different threads that could block each other.
	 * 
	 * Usually one would want to lock only in the {@link BatchLinkingService} but we could
	 * have intermixed {@link LazyURIEncoder#isCrossLinkFragment(org.eclipse.emf.ecore.resource.Resource, String)
	 * lazy cross reference} and vanilla EMF cross references which again could lead to a
	 * dead lock.
	 */
	@Override
	public EObject getEObject(String uriFragment) {
		synchronized (getLock()) {
			try {
				if (getEncoder().isCrossLinkFragment(this, uriFragment) && !isLoadedFromStorage()) {
					if (!getUnresolvableURIFragments().contains(uriFragment)) {
						Triple<EObject, EReference, INode> triple = getEncoder().decode(this, uriFragment);
						if (batchLinkingService.isBatchLinkable(triple.getSecond())) {
							if (compilerPhases.isIndexing(this))
								log.error("Don't resolve expressions during indexing!", new IllegalStateException("Resource URI : "+getURI()+", fragment : "+uriFragment));
							return batchLinkingService.resolveBatched(triple.getFirst(), triple.getSecond(), uriFragment);
						}
						return getEObject(uriFragment, triple);
					} else {
						return null;
					}
				}
				return super.getEObject(uriFragment);
			} catch (RuntimeException e) {
				operationCanceledManager.propagateAsErrorIfCancelException(e);
				getErrors().add(new ExceptionDiagnostic(e));
				log.error("resolution of uriFragment '" + uriFragment + "' failed.", e);
				// wrapped because the javaDoc of this method states that WrappedExceptions are thrown
				// logged because EcoreUtil.resolve will ignore any exceptions.
				throw new WrappedException(e);
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>Implementation detail: Overridden to use the shared {@link #getLock() lock}.</p> 
	 */
	@SuppressWarnings("sync-override")
	@Override
	public EList<EObject> getContents() {
		synchronized (getLock()) {
			if (isLoaded && !isLoading && !isInitializing && !isUpdating && !fullyInitialized && !isLoadedFromStorage()) {
				try {
					eSetDeliver(false);
					installDerivedState(false);
				} finally {
					eSetDeliver(true);
				}
			}
			return doGetContents();
		}
	}
	
	/**
	 * Delegates to the BatchLinkingService to resolve all references. The linking service
	 * is responsible to lock the resource or resource set. 
	 */
	@Override
	public void resolveLazyCrossReferences(CancelIndicator monitor) {
		IParseResult parseResult = getParseResult();
		if (parseResult != null) {
			batchLinkingService.resolveBatched(parseResult.getRootASTElement(), monitor);
		}
		operationCanceledManager.checkCanceled(monitor);
		super.resolveLazyCrossReferences(monitor);
	}
	
	@Override
	protected EObject handleCyclicResolution(Triple<EObject, EReference, INode> triple) throws AssertionError {
		if (!isValidationDisabled()) {
			EObject context = triple.getFirst();
			if (context.eClass() == TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE) {
				// here we may end up with cyclic resolution requests in rare situations, e.g. for input types
				// like :
				/*
				 * package p;
				 * class C extends p.C.Bogus {}
				 */
				return null;
			}
			DiagnosticMessage message = new DiagnosticMessage("Cyclic linking detected : " + getReferences(triple, resolving), Severity.ERROR, "cyclic-resolution");
			List<Diagnostic> list = getDiagnosticList(message);
			Diagnostic diagnostic = createDiagnostic(triple, message);
			if (!list.contains(diagnostic))
				list.add(diagnostic);
		}
		return null;
	}

	@Override
	public void linkBatched(CancelIndicator monitor) {
		resolveLazyCrossReferences(monitor);
	}
	
	// Lazy initialization
	
	/**
	 * Executes any {@link Runnable}s added through {@link #addJvmMemberInitializer(Runnable)}
	 * 
	 * @since 2.8
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public void ensureJvmMembersInitialized() {
		if (jvmMemberInitializers == null)
			return;
		Set<Runnable> localRunnables = null;
		synchronized(this) {
			localRunnables = jvmMemberInitializers;
			jvmMemberInitializers = null;
			hasJvmMemberInitializers = false;
		}
		if (localRunnables == null)
			return;
		boolean wasDeliver = eDeliver();
		LinkedHashSet<Triple<EObject, EReference, INode>> before = resolving;
		try {
			eSetDeliver(false);
			if (before != null && !before.isEmpty()) {
				resolving = new LinkedHashSet<Triple<EObject, EReference, INode>>();
			}
			if (isInitializingJvmMembers) {
				throw new IllegalStateException("Reentrant attempt to initialize JvmMembers");
			}
			try {
				isInitializingJvmMembers = true;
				for (Runnable runnable : localRunnables) {
					runnable.run();
				}
			} finally {
				isInitializingJvmMembers = false;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (before != null && !before.isEmpty()) {
				resolving = before;
			}
			eSetDeliver(wasDeliver);
		}
	}
	
	@Override
	public boolean isInitializingJvmMembers() {
		return isInitializingJvmMembers;
	}
	
	@Override
	protected void doDiscardDerivedState() {
		this.jvmMemberInitializers = null;
		this.hasJvmMemberInitializers = false;
		super.doDiscardDerivedState();
	}
	
	/**
	 * Register runnables to be executed when the {@link JvmMember members} of {@link JvmType types}
	 * in this resource are initialized.
	 * 
	 * @since 2.8
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Override
	public void addJvmMemberInitializer(Runnable runnable) {
		if (isInitializingJvmMembers) {
			throw new IllegalStateException("Cannot enqueue runnables during JvmMemberInitialization");
		}
		if (this.jvmMemberInitializers == null) {
			this.jvmMemberInitializers = new LinkedHashSet<Runnable>();
			this.hasJvmMemberInitializers = true;
			for (EObject obj : getContents()) {
				if (obj instanceof JvmDeclaredTypeImplCustom) {
					JvmDeclaredTypeImplCustom type = (JvmDeclaredTypeImplCustom) obj;
					markPendingInitialization(type);
				}
			}
		}
		this.jvmMemberInitializers.add(runnable);
	}
	
	/**
	 * Recursively traverse the types in this resource to mark them as lazy initialized types. 
	 */
	private void markPendingInitialization(JvmDeclaredTypeImplCustom type) {
		type.setPendingInitialization(true);
		for (JvmMember member : type.basicGetMembers()) {
			if (member instanceof JvmDeclaredTypeImplCustom) {
				markPendingInitialization((JvmDeclaredTypeImplCustom) member);
			}
		}
	}
	
	@Override
	public boolean hasJvmMemberInitializers() {
		return hasJvmMemberInitializers;
	}
	
}
