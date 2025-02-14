/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.linking.lazy;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.AbstractMetamodelDeclaration;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.IGrammarAccess;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.diagnostics.IDiagnosticConsumer;
import org.eclipse.xtext.diagnostics.IDiagnosticProducer;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.EcoreGenericsUtil;
import org.eclipse.xtext.util.OnChangeEvictingCache;
import org.eclipse.xtext.util.SimpleCache;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Knut Wannheden
 */
public class LazyLinker extends AbstractCleaningLinker {

	private static final Logger log = Logger.getLogger(LazyLinker.class);
	
	private SimpleCache<EClass, EClass> instantiableSubTypes;

	@Inject
	private LazyURIEncoder encoder;

	@Inject
	private Registry registry;

	@Inject
	private EcoreGenericsUtil ecoreGenericsUtil;
	
	@Inject
	private IGrammarAccess grammarAccess;
	
	@Inject
	private OnChangeEvictingCache cache;

	@Override
	protected void doLinkModel(final EObject model, IDiagnosticConsumer consumer) {
		final Multimap<EStructuralFeature.Setting, INode> settingsToLink = ArrayListMultimap.create();
		final IDiagnosticProducer producer = createDiagnosticProducer(consumer, model.eResource());
		cache.execWithoutCacheClear(model.eResource(), new IUnitOfWork.Void<Resource>() {
			@Override
			public void process(Resource state) throws Exception {
				TreeIterator<EObject> iterator = getAllLinkableContents(model);
				boolean clearAllReferencesRequired = isClearAllReferencesRequired(state);
				while (iterator.hasNext()) {
					EObject eObject = iterator.next();
					if (clearAllReferencesRequired) {
						clearReferences(eObject);
					}
					installProxies(eObject, producer, settingsToLink);
				}
			}
		});
		installQueuedLinks(settingsToLink);
	}

	protected void installProxies(EObject obj, IDiagnosticProducer producer,
			Multimap<EStructuralFeature.Setting, INode> settingsToLink) {
		ICompositeNode node = NodeModelUtils.getNode(obj);
		if (node == null)
			return;
		installProxies(obj, producer, settingsToLink, node, false);
	}

	private void installProxies(EObject obj, IDiagnosticProducer producer,
			Multimap<EStructuralFeature.Setting, INode> settingsToLink, ICompositeNode parentNode, boolean dontCheckParent) {
		final EClass eClass = obj.eClass();
		if (eClass.getEAllReferences().size() - eClass.getEAllContainments().size() == 0)
			return;

		for (INode node = parentNode.getFirstChild(); node != null; node = node.getNextSibling()) {
			EObject grammarElement = node.getGrammarElement();
			if (grammarElement instanceof CrossReference && hasLeafNodes(node)) {
				producer.setNode(node);
				CrossReference crossReference = (CrossReference) grammarElement;
				final EReference eRef = GrammarUtil.getReference(crossReference, eClass);
				if (eRef == null) {
					ParserRule parserRule = GrammarUtil.containingParserRule(crossReference);
					final String feature = GrammarUtil.containingAssignment(crossReference).getFeature();
					throw new IllegalStateException("Couldn't find EReference for crossreference '"+eClass.getName()+"::"+feature+"' in parser rule '"+parserRule.getName()+"'.");
				}
				if (!eRef.isResolveProxies() /*|| eRef.getEOpposite() != null see https://bugs.eclipse.org/bugs/show_bug.cgi?id=282486*/) {
					final EStructuralFeature.Setting setting = ((InternalEObject) obj).eSetting(eRef);
					settingsToLink.put(new SettingDelegate(setting), node);
				} else {
					createAndSetProxy(obj, node, eRef);
					afterCreateAndSetProxy(obj, node, eRef, crossReference, producer);
				}
			} else if (grammarElement instanceof RuleCall && node instanceof ICompositeNode) {
				RuleCall ruleCall = (RuleCall) grammarElement;
				AbstractRule calledRule = ruleCall.getRule();
				if (calledRule instanceof ParserRule && ((ParserRule) calledRule).isFragment()) {
					installProxies(obj, producer, settingsToLink, (ICompositeNode) node, true);
				}
			}
		}
		if (!dontCheckParent && shouldCheckParentNode(parentNode)) {
			installProxies(obj, producer, settingsToLink, parentNode.getParent(), dontCheckParent);
		}
	}

	/**
	 * @since 2.7
	 */
	protected void afterCreateAndSetProxy(EObject obj, INode node, EReference eRef, CrossReference crossReference,
			IDiagnosticProducer producer) {
	}

	/**
	 * @since 2.4
	 */
	protected boolean hasLeafNodes(INode node) {
		return !Iterables.isEmpty(node.getLeafNodes());
	}

	@SuppressWarnings("unchecked")
	protected void installQueuedLinks(Multimap<EStructuralFeature.Setting, INode> settingsToLink) {
		for (EStructuralFeature.Setting setting : settingsToLink.keySet()) {
			final EObject eObject = setting.getEObject();
			final EReference eRef = (EReference) setting.getEStructuralFeature();
			final Collection<INode> nodes = settingsToLink.get(setting);
			if (setting.getEStructuralFeature().isMany()) {
				EList<EObject> list = (EList<EObject>) setting.get(false);
				for (INode node : nodes) {
					final EObject proxy = createProxy(eObject, node, eRef);
					list.add(EcoreUtil.resolve(proxy, eObject));
				}
			} else {
				final INode node = nodes.iterator().next();
				final EObject proxy = createProxy(eObject, node, eRef);
				setting.set(EcoreUtil.resolve(proxy, eObject));
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void createAndSetProxy(EObject obj, INode node, EReference eRef) {
		final EObject proxy = createProxy(obj, node, eRef);
		if (eRef.isMany()) {
			((InternalEList<EObject>) obj.eGet(eRef, false)).addUnique(proxy);
		} else {
			obj.eSet(eRef, proxy);
		}
	}

	protected EObject createProxy(EObject obj, INode node, EReference eRef) {
		final Resource resource = obj.eResource();
		if (resource == null)
			throw new IllegalStateException("object must be contained in a resource");
		final URI uri = resource.getURI();
		final URI encodedLink = uri.appendFragment(encoder.encode(obj, eRef, node));
		EClass referenceType = getProxyType(obj, eRef);
		final EObject proxy = EcoreUtil.create(referenceType);
		((InternalEObject) proxy).eSetProxyURI(encodedLink);
		return proxy;
	}

	private void ensureCacheInitialized() {
		if (instantiableSubTypes == null) {
			 instantiableSubTypes = new SimpleCache<EClass, EClass>(this::findInstantiableCompatible);
		} 
	}
	/**
	 * @since 2.7
	 */
	protected EClass getProxyType(EObject obj, EReference eRef) {
		EClass referenceType = ecoreGenericsUtil.getReferenceType(eRef, obj.eClass());
		ensureCacheInitialized();
		EClass instantiableType = instantiableSubTypes.get(referenceType);
		return instantiableType;
	}

	protected EClass findInstantiableCompatible(EClass eType) {
		if (eType.isAbstract() || eType.isInterface()) {
			// check local Package
			EClass eClass = findSubTypeInEPackage(eType.getEPackage(), eType);
			if (eClass != null)
				return eClass;
			return globalFindInstantiableCompatible(eType);
		}
		return eType;
	}

	protected EClass globalFindInstantiableCompatible(EClass eType) {
		Set<String> visitedPackages = Sets.newHashSet(eType.getEPackage().getNsURI());
		for(AbstractMetamodelDeclaration metamodel: GrammarUtil.allMetamodelDeclarations(grammarAccess.getGrammar())) {
			if (visitedPackages.add(metamodel.getEPackage().getNsURI())) {
				EClass result = findSubTypeInEPackage(metamodel.getEPackage(), eType);
				if (result != null)
					return result;
			}
		}
		log.warn("Traversing EPackage registry to find instantiable subtype for '" + eType.getName() + "'");
		log.warn("You may override LazyLinker#globalFindInstantiableCompatible(..) to prevent this.");
		for (String nsURI : getRegisteredNsUris()) {
			if (visitedPackages.add(nsURI)) {
				try {
					EClass result = findSubTypeInEPackage(getRegistry().getEPackage(nsURI), eType);
					if (result != null)
						return result;
				} catch(WrappedException ex) {
					log.error("Error when loading EPackage '" + nsURI + "'");
					log.error("You may override LazyLinker#globalFindInstantiableCompatible(..) to prevent this.");
					log.error("Error when loading EPackage '" + nsURI + "'", ex);
				}
			}
		}
		throw new IllegalStateException(
				"Could not find an instantiable subtype for type: '" + eType.getName() + "' (" + eType.getEPackage().getNsURI() + ").");
	}

	private List<String> getRegisteredNsUris() {
		Set<String> keySet = getRegistry().keySet();
		// copy to avoid ConcurrentModificationException while iterating over the EPackageMap
		List<String> copy = Lists.newArrayList(keySet);
		return copy;
	}

	protected EClass findSubTypeInEPackage(EPackage ePackage, EClass superType) {
		EList<EClassifier> classifiers = ePackage.getEClassifiers();
		for (EClassifier eClassifier : classifiers) {
			if (eClassifier instanceof EClass) {
				EClass c = (EClass) eClassifier;
				if (isInstantiatableSubType(c, superType))
					return c;
			}
		}
		return null;
	}

	private boolean isInstantiatableSubType(EClass c, EClass superType) {
		return !c.isAbstract() && !c.isInterface() && EcoreUtil2.isAssignableFrom(superType, c);
	}

	public LazyURIEncoder getEncoder() {
		return encoder;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public void setEncoder(LazyURIEncoder encoder) {
		this.encoder = encoder;
	}

	public void setGrammarAccess(IGrammarAccess grammarAccess) {
		this.grammarAccess = grammarAccess;
	}

	public IGrammarAccess getGrammarAccess() {
		return grammarAccess;
	}

	/**
	 * @since 2.4
	 */
	protected OnChangeEvictingCache getCache() {
		return cache;
	}
}
