/*******************************************************************************
 * Copyright (c) 2008, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.linking.lazy;

import static com.google.common.collect.Lists.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.diagnostics.DiagnosticMessage;
import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
import org.eclipse.xtext.linking.ILinkingDiagnosticMessageProvider;
import org.eclipse.xtext.linking.ILinkingDiagnosticMessageProvider.ILinkingDiagnosticContext;
import org.eclipse.xtext.linking.ILinkingService;
import org.eclipse.xtext.linking.impl.IllegalNodeException;
import org.eclipse.xtext.linking.impl.LinkingHelper;
import org.eclipse.xtext.linking.impl.XtextLinkingDiagnostic;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Triple;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Holger Schill
 */
public class LazyLinkingResource extends XtextResource {
	
	/**
	 * @since 2.7
	 */
	@SuppressWarnings("serial")
	public static final class CyclicLinkingException extends AssertionError {

		private Triple<EObject, EReference, INode> triple;

		private CyclicLinkingException(Object detailMessage, Triple<EObject,EReference,INode> triple) {
			super(detailMessage);
			this.triple = triple;
		}
		
	}

	private static Logger log = Logger.getLogger(LazyLinkingResource.class);
	
	/**
	 * The cache key for a Set of uri fragments that cannot be resolved. 
	 * @since 2.4
	 */
	public static final String UNRESOLVEABLE_PROXIES_KEY = "UNRESOLVEABLE_PROXIES";

	/**
	 * Determines the limit that is used to switch from a counter to a set to detect cyclic linking.
	 * @since 2.16
	 */
	public static final String CYCLIC_LINKING_DECTECTION_COUNTER_LIMIT = "CYCLIC_LINKING_DECTECTION_COUNTER_LIMIT";

	@Inject
	private ILinkingService linkingService;

	@Inject
	private LazyURIEncoder encoder;

	@Inject
	private ILinkingDiagnosticMessageProvider diagnosticMessageProvider;
	
	@Inject
	private ILinkingDiagnosticMessageProvider.Extended linkingDiagnosticMessageProvider;

	@Inject
	private LinkingHelper linkingHelper;

	private boolean eagerLinking = false;

	@Named(CYCLIC_LINKING_DECTECTION_COUNTER_LIMIT)
	@Inject(optional=true)
	protected int cyclicLinkingDectectionCounterLimit = 100;

	private int cyclicLinkingDetectionCounter = 0;

	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		super.doLoad(inputStream, options);
		if (options != null && Boolean.TRUE.equals(options.get(OPTION_RESOLVE_ALL)))
			EcoreUtil.resolveAll(this);
	}

	@Override
	protected void doLinking() {
		super.doLinking();
		if (isEagerLinking())
			EcoreUtil.resolveAll(this);
	}
	
	/**
	 * The method can be overridden to avoid creation of a Set object for resources loaded from storage 
	 * or when no cyclic resolution takes place.
	 * 
	 * @since 2.39
	 * 
	 */
	protected LinkedHashSet<Triple<EObject, EReference, INode>> getResolvingSet(boolean initial) {
		if (initial) {
			return Sets.newLinkedHashSet();
		} else {
			return resolving;
		}
	}
	
	/**
	 * @since 2.4
	 */
	protected LinkedHashSet<Triple<EObject, EReference, INode>> resolving = getResolvingSet(true);

	/**
	 * resolves any lazy cross references in this resource, adding Issues for unresolvable elements to this resource.
	 * This resource might still contain resolvable proxies after this method has been called.
	 * 
	 * @param mon a {@link CancelIndicator} can be used to stop the resolution.
	 */
	public void resolveLazyCrossReferences(final CancelIndicator mon) {
		final CancelIndicator monitor = mon == null ? CancelIndicator.NullImpl : mon;
		TreeIterator<Object> iterator = EcoreUtil.getAllContents(this, true);
		while (iterator.hasNext()) {
			operationCanceledManager.checkCanceled(monitor);
			InternalEObject source = (InternalEObject) iterator.next();
			EStructuralFeature[] eStructuralFeatures = ((EClassImpl.FeatureSubsetSupplier) source.eClass()
					.getEAllStructuralFeatures()).crossReferences();
			if (eStructuralFeatures != null) {
				for (EStructuralFeature crossRef : eStructuralFeatures) {
					operationCanceledManager.checkCanceled(monitor);
					resolveLazyCrossReference(source, crossRef);
				}
			}
		}
	}

	/**
	 * If the given {@code crossRef} may hold lazy linking proxies, they are attempted to be resolved. 
	 * @since 2.4
	 * @see #isPotentialLazyCrossReference(EStructuralFeature)
	 * @see #doResolveLazyCrossReference(InternalEObject, EStructuralFeature)
	 */
	protected void resolveLazyCrossReference(InternalEObject source, EStructuralFeature crossRef) {
		if (isPotentialLazyCrossReference(crossRef)) {
			doResolveLazyCrossReference(source, crossRef);
		}
	}

	/**
	 * Ensures that all the lazy proxy values that are referenced by {@code crossRef} are replaced
	 * by non-lazy proxies or resolved instances.
	 * 
	 * @since 2.4
	 */
	protected void doResolveLazyCrossReference(InternalEObject source, EStructuralFeature crossRef) {
		if (crossRef.isMany()) {
			@SuppressWarnings("unchecked")
			InternalEList<EObject> list = (InternalEList<EObject>) source.eGet(crossRef);
			for (int i = 0; i < list.size(); i++) {
				EObject proxy = list.basicGet(i);
				if (proxy.eIsProxy()) {
					URI proxyURI = ((InternalEObject) proxy).eProxyURI();
					if (getURI().equals(proxyURI.trimFragment())) {
						final String fragment = proxyURI.fragment();
						if (getEncoder().isCrossLinkFragment(this, fragment)) {
							EObject target = getEObject(fragment);
							if (target != null) {
								try {
									source.eSetDeliver(false);
									list.setUnique(i, target);
								} finally {
									source.eSetDeliver(true);
								}
							}
						}
					}
				}
			}
		} else {
			EObject proxy = (EObject) source.eGet(crossRef, false);
			if (proxy != null && proxy.eIsProxy()) {
				URI proxyURI = ((InternalEObject) proxy).eProxyURI();
				if (getURI().equals(proxyURI.trimFragment())) {
					final String fragment = proxyURI.fragment();
					if (getEncoder().isCrossLinkFragment(this, fragment)) {
						EObject target = getEObject(fragment);
						if (target != null) {
							try {
								source.eSetDeliver(false);
								source.eSet(crossRef, target);
							} finally {
								source.eSetDeliver(true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Return <code>true</code> if the given feature may hold a proxy that has to be resolved.
	 * 
	 * This is supposed to be an internal hook which allows to resolve proxies even in cases
	 * where EMF prohibits proxies, e.g. in case of opposite references.
	 * 
	 * @since 2.4
	 */
	protected boolean isPotentialLazyCrossReference(EStructuralFeature feature) {
		return !feature.isDerived() && !feature.isTransient() 
				&& feature instanceof EReference && ((EReference)feature).isResolveProxies();
	}

	@Override
	public synchronized EObject getEObject(String uriFragment) {
		try {
			if (getEncoder().isCrossLinkFragment(this, uriFragment)) {
				Triple<EObject, EReference, INode> triple = getEncoder().decode(this, uriFragment);
				return getEObject(uriFragment, triple);
			}
		} catch (RuntimeException e) {
			operationCanceledManager.propagateAsErrorIfCancelException(e);
			getErrors().add(new ExceptionDiagnostic(e));
			log.error("resolution of uriFragment '" + uriFragment + "' failed.", e);
			// wrapped because the javaDoc of this method states that WrappedExceptions are thrown
			// logged because EcoreUtil.resolve will ignore any exceptions.
			throw new WrappedException(e);
		}
		return super.getEObject(uriFragment);
	}

	/**
	 * @since 2.4
	 */
	protected EObject getEObject(String uriFragment, Triple<EObject, EReference, INode> triple) throws AssertionError {
		cyclicLinkingDetectionCounter++;
		if (cyclicLinkingDetectionCounter > cyclicLinkingDectectionCounterLimit) {
			if (!getResolvingSet(false).add(triple)) {
				return handleCyclicResolution(triple);
			}
		}
		try {
			Set<String> unresolveableProxies = getUnresolvableURIFragments();
			if (unresolveableProxies.contains(uriFragment))
				return null;
			EReference reference = triple.getSecond();
			try {
				List<EObject> linkedObjects = getLinkingService().getLinkedObjects(
						triple.getFirst(), 
						reference,
						triple.getThird());
	
				if (linkedObjects.isEmpty()) {
					if (isUnresolveableProxyCacheable(triple))
						unresolveableProxies.add(uriFragment);
					createAndAddDiagnostic(triple);
					return null;
				}
				if (linkedObjects.size() > 1)
					throw new IllegalStateException("linkingService returned more than one object for fragment "
							+ uriFragment);
				EObject result = linkedObjects.get(0);
				if (!EcoreUtil2.isAssignableFrom(reference.getEReferenceType(), result.eClass())) {
					log.error("An element of type " + result.getClass().getName()
							+ " is not assignable to the reference " + reference.getEContainingClass().getName()
							+ "." + reference.getName());
					if (isUnresolveableProxyCacheable(triple))
						unresolveableProxies.add(uriFragment);
					createAndAddDiagnostic(triple);
					return null;
				}
				// remove previously added error markers, since everything should be fine now
				unresolveableProxies.remove(uriFragment);
				removeDiagnostic(triple);
				return result;
			} catch (CyclicLinkingException e) {
				if (e.triple.equals(triple)) {
					log.error(e.getMessage(), e);
					if (isUnresolveableProxyCacheable(triple))
						unresolveableProxies.add(uriFragment);
					createAndAddDiagnostic(triple);
					return null;
				} else {
					throw e;
				}
			}
		} catch (IllegalNodeException ex) {
			createAndAddDiagnostic(triple, ex);
			return null;
		} finally {
			if (cyclicLinkingDetectionCounter > cyclicLinkingDectectionCounterLimit) {
				getResolvingSet(false).remove(triple);
			}
			cyclicLinkingDetectionCounter--;
		}
	}

	/**
	 * @since 2.1
	 */
	protected boolean isUnresolveableProxyCacheable(Triple<EObject, EReference, INode> triple) {
		return true;
	}

	protected EObject handleCyclicResolution(Triple<EObject, EReference, INode> triple) throws AssertionError {
		throw new CyclicLinkingException("Cyclic resolution of lazy links : " + getReferences(triple, getResolvingSet(false)) + " in resource '"+getURI()+"'.", triple);
	}

	protected String getReferences(Triple<EObject, EReference, INode> triple,
			LinkedHashSet<Triple<EObject, EReference, INode>> resolving) {
		StringBuffer buffer = new StringBuffer();
		boolean found = false;
		for (Triple<EObject, EReference, INode> triple2 : resolving) {
			found = found || triple2.equals(triple);
			if (found)
				buffer.append(getQualifiedName(triple2.getSecond())).append("->");
		}
		buffer.append(getQualifiedName(triple.getSecond()));
		return buffer.toString();
	}

	private String getQualifiedName(EReference eReference) {
		return eReference.getEContainingClass().getName() + "." + eReference.getName();
	}

	protected static class DiagnosticMessageContext implements
			ILinkingDiagnosticMessageProvider.ILinkingDiagnosticContext {

		private final Triple<EObject, EReference, INode> triple;
		private final LinkingHelper linkingHelper;

		protected DiagnosticMessageContext(Triple<EObject, EReference, INode> triple, LinkingHelper helper) {
			this.triple = triple;
			this.linkingHelper = helper;
		}

		@Override
		public EObject getContext() {
			return triple.getFirst();
		}

		@Override
		public EReference getReference() {
			return triple.getSecond();
		}

		@Override
		public String getLinkText() {
			return linkingHelper.getCrossRefNodeAsString(triple.getThird(), true);
		}

	}

	protected void createAndAddDiagnostic(Triple<EObject, EReference, INode> triple) {
		if (isValidationDisabled())
			return;
		DiagnosticMessage message = createDiagnosticMessage(triple);
		if (message != null) {
			List<Diagnostic> list = getDiagnosticList(message);
			Diagnostic diagnostic = createDiagnostic(triple, message);
			if (!list.contains(diagnostic))
				list.add(diagnostic);
		}
	}
	
	/**
	 * @since 2.3
	 */
	protected void createAndAddDiagnostic(Triple<EObject, EReference, INode> triple, IllegalNodeException ex) {
		if (isValidationDisabled())
			return;
		ILinkingDiagnosticMessageProvider.ILinkingDiagnosticContext context = createDiagnosticMessageContext(triple);
		DiagnosticMessage message = linkingDiagnosticMessageProvider.getIllegalNodeMessage(context, ex);
		if (message != null) {
			List<Diagnostic> list = getDiagnosticList(message);
			Diagnostic diagnostic = createDiagnostic(triple, message);
			if (!list.contains(diagnostic))
				list.add(diagnostic);
		}
	}

	protected void removeDiagnostic(Triple<EObject, EReference, INode> triple) {
		// return early if there's nothing to remove
		if (getErrors().isEmpty() && getWarnings().isEmpty())
			return;
		DiagnosticMessage message = createDiagnosticMessage(triple);
		List<Diagnostic> list = getDiagnosticList(message);
		if (!list.isEmpty()) {
			Diagnostic diagnostic = createDiagnostic(triple, message);
			list.remove(diagnostic);
		}
	}

	protected Diagnostic createDiagnostic(Triple<EObject, EReference, INode> triple, DiagnosticMessage message) {
		Diagnostic diagnostic = new XtextLinkingDiagnostic(triple.getThird(), message.getMessage(),
				message.getIssueCode(), message.getIssueData());
		return diagnostic;
	}

	protected List<Diagnostic> getDiagnosticList(DiagnosticMessage message) throws AssertionError {
		if (message != null) {
			switch (message.getSeverity()) {
				case ERROR:
					return getErrors();
				case WARNING:
					return getWarnings();
				default:
					throw new AssertionError("Unexpected severity: " + message.getSeverity());
			}
		}
		return Collections.emptyList();
	}

	protected DiagnosticMessage createDiagnosticMessage(Triple<EObject, EReference, INode> triple) {
		ILinkingDiagnosticMessageProvider.ILinkingDiagnosticContext context = createDiagnosticMessageContext(triple);
		DiagnosticMessage message = diagnosticMessageProvider.getUnresolvedProxyMessage(context);
		return message;
	}

	protected ILinkingDiagnosticContext createDiagnosticMessageContext(Triple<EObject, EReference, INode> triple) {
		return new DiagnosticMessageContext(triple, linkingHelper);
	}

	public void setLinkingService(ILinkingService linkingService) {
		this.linkingService = linkingService;
	}

	public ILinkingService getLinkingService() {
		return linkingService;
	}

	public void setEncoder(LazyURIEncoder encoder) {
		this.encoder = encoder;
	}

	public LazyURIEncoder getEncoder() {
		return encoder;
	}

	public void setEagerLinking(boolean eagerLinking) {
		this.eagerLinking = eagerLinking;
	}

	public boolean isEagerLinking() {
		return eagerLinking;
	}

	public ILinkingDiagnosticMessageProvider getDiagnosticMessageProvider() {
		return diagnosticMessageProvider;
	}

	public void setDiagnosticMessageProvider(ILinkingDiagnosticMessageProvider diagnosticMessageProvider) {
		this.diagnosticMessageProvider = diagnosticMessageProvider;
	}

	public LinkingHelper getLinkingHelper() {
		return linkingHelper;
	}

	public void setLinkingHelper(LinkingHelper linkingHelper) {
		this.linkingHelper = linkingHelper;
	}

	/**
	 * Marks the given proxy as unresolvable. Further attempts to resolve it by means of {@link #getEObject(String)}
	 * will yield <code>null</code>.
	 * @since 2.4
	 */
	public void markUnresolvable(EObject referenced) {
		if (!referenced.eIsProxy()) {
			throw new IllegalArgumentException("Cannot mark an instance as unresolvable if it is already resolved");
		}
		URI proxyURI = ((InternalEObject)referenced).eProxyURI();
		getUnresolvableURIFragments().add(proxyURI.fragment());
	}
	
	/**
	 * @since 2.4
	 */
	protected Set<String> getUnresolvableURIFragments() {
		Set<String> unresolveableProxies = getCache().get(UNRESOLVEABLE_PROXIES_KEY, this,
				new Provider<Set<String>>() {
					@Override
					public Set<String> get() {
						return Sets.newHashSet();
					}
				});
		return unresolveableProxies;
	}
	
	private ArrayList<Triple<EObject, EReference, INode>> proxyInformation;
	
	/**
	 * Returns the list of installed lazy linking proxies encoded as a {@link Triple} of the owning object,
	 * the reference and the node that holds the raw text for the link.
	 * 
	 * @since 2.23
	 */
	protected List<Triple<EObject, EReference, INode>> getLazyProxyInformation() {
		if (proxyInformation == null) {
			proxyInformation = newArrayList();
		}
		// Make the list available to sub-types in a modifiable way such that they can work with it more efficiently
		return proxyInformation;
	}
	
	/**
	 * @since 2.7
	 */
	public int addLazyProxyInformation(EObject obj, EReference ref, INode node) {
		List<Triple<EObject, EReference, INode>> information = getLazyProxyInformation();
		int index = information.size();
		information.add(getParseResultWrapper().toProxyInformation(internalGetParseResult(), obj, ref, node));
		return index;
	}
	
	/**
	 * @since 2.7
	 */
	public boolean hasLazyProxyInformation(int idx) {
		return getLazyProxyInformation().get(idx) != null;
	}
	
	/**
	 * @since 2.7
	 */
	public Triple<EObject,EReference,INode> getLazyProxyInformation(int idx) {
		Triple<EObject, EReference, INode> information = getLazyProxyInformation().get(idx);
		if (information == null) {
			throw new IllegalArgumentException("No proxy information for index '"+idx+"' available.");
		}
		return information;
	}
	
	/**
	 * @since 2.7
	 */
	public Triple<EObject,EReference,INode> removeLazyProxyInformation(int idx) {
		return proxyInformation.set(idx, null);
	}
	
	/**
	 * @since 2.7
	 */
	public void clearLazyProxyInformation() {
		if (proxyInformation != null) {
			proxyInformation.clear();
		}
	}
	
}
