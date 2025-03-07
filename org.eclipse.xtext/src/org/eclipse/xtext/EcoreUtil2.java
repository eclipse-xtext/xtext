/*******************************************************************************
 * __  ___            _
 * \ \/ / |_ _____  __ |_
 *  \  /| __/ _ \ \/ / __|
 *  /  \| |_  __/>  <| |_
 * /_/\_\\__\___/_/\_\\__|
 *
 * Copyright (c) 2008, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.SegmentSequence;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.resource.ClassloaderClasspathUriResolver;
import org.eclipse.xtext.resource.DerivedStateAwareResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Strings;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;

/**
 * @author Heiko Behrens
 * @author Sebastian Zarnekow
 * @author Sven Efftinge
 * @author Michael Clay
 */
public class EcoreUtil2 extends EcoreUtil {

	private static Logger log = Logger.getLogger(EcoreUtil2.class);

	/**
	 * @return the next sibling of the passed eObject or null
	 * @since 2.1
	 */
	public static EObject getNextSibling(EObject eObject) {
		EObject next = null;
		if (eObject.eContainingFeature() != null && eObject.eContainingFeature().isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> siblings = (List<EObject>) eObject.eContainer().eGet(eObject.eContainingFeature());
			int indexOf = siblings.indexOf(eObject);
			if (indexOf < siblings.size() - 1) {
				next = siblings.get(indexOf + 1);
			}
		}
		return next;
	}

	/**
	 * @return the previous sibling of the passed eObject or null
	 * @since 2.1
	 */
	public static EObject getPreviousSibling(EObject eObject) {
		EObject previous = null;
		if (eObject.eContainingFeature() != null && eObject.eContainingFeature().isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> siblings = (List<EObject>) eObject.eContainer().eGet(eObject.eContainingFeature());
			int indexOf = siblings.indexOf(eObject);
			if (indexOf > 0) {
				previous = siblings.get(indexOf - 1);
			}
		}
		return previous;
	}

	/**
	 * Returns the closest {@link EObject#eContainer() container object} of the requested type. If the given object is
	 * an instance of the requested type, then the object itself will be returned. If no container object is of the
	 * requested type, then {@code null} will be returned.
	 */
	/* @Nullable */
	public static <T extends EObject> T getContainerOfType(/* @Nullable */ EObject ele, /* @NonNull */ Class<T> type) {
		for (EObject e = ele; e != null; e = e.eContainer()) {
			if (type.isInstance(e)) {
				return type.cast(e);
			}
		}

		return null;
	}
	
	/**
	 * @since 2.1
	 */
	public static <T extends EObject> List<T> getSiblingsOfType(EObject ele, Class<T> type) {
		if (ele.eContainer() != null) {
			List<T> siblings = typeSelect(ele.eContainer().eContents(), type);
			siblings.remove(ele);
			return siblings;
		}
		return Collections.emptyList();
	}

	/**
	 * A generified facade to {@link EcoreUtil#copy(EObject)}.
	 * Note that since EMF 2.6 {@link EcoreUtil#copy(EObject)} is already generic.
	 * @deprecated use {@link EcoreUtil#copy(EObject)} instead.
	 */
	@Deprecated
	public static <T extends EObject> T clone(T eObject) {
		return EcoreUtil.copy(eObject);
	}
	
	/**
	 * Clones the given EObject without resolving any proxies.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EObject> T cloneWithProxies(T original) {
		if (original == null) {
			return original;
		}
		EcoreUtil.Copier copier = new EcoreUtil.Copier(false);
		T copy = (T) copier.copy(original);
		copier.copyReferences();
		return copy;
	}

	/**
	 * only clones the element if it is contained in another {@link EObject} or another {@link Resource}
	 */
	public static <T extends EObject> T cloneIfContained(T eObject) {
		if (eObject != null && (eObject.eContainer() != null || eObject.eResource() != null)) {
			return clone(eObject);
		}
		return eObject;
	}

	/**
	 * copies contents of a resource set into a new one
	 */
	public static <T extends ResourceSet> T clone(T target, ResourceSet source) {
		EList<Resource> resources = source.getResources();
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		for (Resource resource : resources) {
			Resource targetResource = target.createResource(resource.getURI());
			targetResource.getContents().addAll(copier.copyAll(resource.getContents()));
			// mark all resources as fully initialized
			if (resource instanceof DerivedStateAwareResource derivedStateAwareResource &&
					targetResource instanceof DerivedStateAwareResource targetDerivedStateAwareResource) {
				targetDerivedStateAwareResource.setFullyInitialized(
						derivedStateAwareResource.isFullyInitialized());
			}
		}
		copier.copyReferences();
		return target;
	}

	public static <T extends EObject> List<T> getAllContentsOfType(EObject ele, Class<T> type) {
		return Lists.newArrayList(Iterators.filter(ele.eAllContents(), type));
	}

	/**
	 * @since 2.37
	 */
	public static <T extends EObject> List<T> getAllContentsOfType(Resource resource, Class<T> type) {
		return Lists.newArrayList(Iterators.filter(resource.getAllContents(), type));
	}

	public static <T> List<T> typeSelect(List<?> elements, Class<T> clazz) {
		return Lists.newArrayList(Iterables.filter(elements, clazz));
	}

	public static <T> List<T> collect(Collection<? extends EObject> instances, int featureId, Class<T> type) {
		final List<T> result = new ArrayList<>(instances.size());
		for (EObject obj : instances) {
			if (obj == null) {
				throw new NullPointerException("obj may not be null");
			}
			final EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureId);
			if (feature == null) {
				throw new NullPointerException("feature may not be null");
			}
			final Object object = obj.eGet(feature);
			if (object != null) {
				result.add(type.cast(object));
			}
		}
		return result;
	}
	
	public static <T extends EObject> List<T> eAllOfType(EObject ele, Class<T> type) {
		return Lists.newArrayList(Iterators.filter(eAll(ele), type));
	}

	public static TreeIterator<EObject> eAll(final EObject obj) {
		return new TreeIterator<>() {
			private TreeIterator<EObject> it = null;
			private int index = 0;

			@Override
			public void prune() {
				switch (index) {
					case 0:
						return;
					case 1:
						it = null;
						break;
					default:
						if (it != null) {
							it.prune();
						}
				}
			}

			@Override
			public boolean hasNext() {
				if (index == 0) {
					return true;
				}
				if (it != null) {
					return it.hasNext();
				}
				return false;
			}

			@Override
			public EObject next() {
				if (index++ == 0) {
					it = obj.eAllContents();
					return obj;
				}
				if (it != null) {
					return it.next();
				}
				return null;
			}

			@Override
			public void remove() {
				if (index == 0) {
					EcoreUtil.remove(obj);
				}
				if (it != null) {
					it.remove();
				}
			}
		};
	}
	
	public static Iterable<EObject> eAllContents(final EObject n) {
		return () -> eAll(n);
	}

	public static List<EObject> eAllContentsAsList(EObject ele) {
		return Lists.newArrayList(ele.eAllContents());
	}

	public static List<EObject> eAllContentsAsList(Resource resource) {
		return Lists.newArrayList(resource.getAllContents());
	}

	public static final EPackage loadEPackage(String uriAsString, ClassLoader classLoader) {
		if (EPackage.Registry.INSTANCE.containsKey(uriAsString)) {
			return EPackage.Registry.INSTANCE.getEPackage(uriAsString);
		}
		URI uri = URI.createURI(uriAsString);
		uri = new ClassloaderClasspathUriResolver().resolve(classLoader, uri);
		Resource resource = new ResourceSetImpl().getResource(uri, true);
		for (TreeIterator<EObject> allContents = resource.getAllContents(); allContents.hasNext();) {
			EObject next = allContents.next();
			if (next instanceof EPackage ePackage) {
				// if (ePackage.getNsURI() != null &&
				// ePackage.getNsURI().equals(uriAsString)) {
				return ePackage;
				// }
			}
		}
		log.error("Could not load EPackage with nsURI" + uriAsString);
		return null;
	}

	public static String getURIFragment(EObject eObject) {
		Resource resource = eObject.eResource();
		String fragment = resource.getURIFragment(eObject);
		return fragment;
	}
	
	public static EClassifier getCompatibleType(EClassifier typeA, EClassifier typeB) {
		EClassifier result = getCompatibleType(typeA, typeB, null);
		if (result != null) {
			return result;
		}
		if (typeA instanceof EClass && typeB instanceof EClass) {
			return EcorePackage.Literals.EOBJECT;
		}
		return null;
	}
	
	/**
	 * @since 2.1
	 */
	public static EClassifier getCompatibleType(EClassifier typeA, EClassifier typeB, EObject grammarContext) {
		if (typeA.equals(typeB)) {
			return typeA;
		}
		if (typeA instanceof EDataType && typeB instanceof EDataType) {
			Class<?> instanceClassA = typeA.getInstanceClass();
			Class<?> instanceClassB = typeB.getInstanceClass();
			if (instanceClassA != null && instanceClassB != null) {
				if (instanceClassA.isAssignableFrom(instanceClassB)) {
					return typeA;
				}
				if (instanceClassB.isAssignableFrom(instanceClassA)) {
					return typeB;
				}
			}
		}
		
		// no common type for simple datatypes available
		if (!(typeA instanceof EClass eClassA && typeB instanceof EClass eClassB)) {
			return null;
		}

		List<EClass> sortedCandidates = getSortedCommonCompatibleTypeCandidates(eClassA, eClassB);
		for (EClass candidate : sortedCandidates) {
			if (isCommonCompatibleType(candidate, sortedCandidates)) {
				return candidate;
			}
		}
		EClass result = GrammarUtil.findEObject(GrammarUtil.getGrammar(grammarContext));
		return result;
	}

	private static boolean isLooselyCompatibleWith(EClass classA, EClass classB) {
		return classA.equals(classB) || classA.getEAllSuperTypes().contains(classB)
				|| classB.getEAllSuperTypes().contains(classA);
	}

	private static boolean isCommonCompatibleType(EClass candidate, List<EClass> candidates) {
		return candidates.stream().allMatch(otherCandidate -> isLooselyCompatibleWith(candidate, otherCandidate));
	}

	private static List<EClass> getSortedCommonCompatibleTypeCandidates(EClass classA, EClass classB) {
		List<EClass> result = getCompatibleTypesOf(classA);
		List<EClass> compatibleTypesOfB = getCompatibleTypesOf(classB);
		result.retainAll(compatibleTypesOfB);

		result.sort((class1, class2) -> {
			if (class1.getEAllSuperTypes().contains(class2)) {
				return -1;
			}
			if (class2.getEAllSuperTypes().contains(class1)) {
				return 1;
			}
			int nameComparisonResult = class1.getName().compareTo(class2.getName());
			if (nameComparisonResult != 0) {
				return nameComparisonResult;
			}
			return class1.getEPackage().getNsURI().compareTo(class2.getEPackage().getNsURI());
		});

		return result;
	}

	public static List<EClass> getCompatibleTypesOf(EClass eClass) {
		List<EClass> ca = new ArrayList<>(eClass.getEAllSuperTypes());
		ca.add(eClass);
		return ca;
	}

	private static void collectAllSuperTypes(Set<EClass> collectedTypes, EClass eClass) {
		for (EClass superType : eClass.getESuperTypes()) {
			if (collectedTypes.add(superType)) {
				collectAllSuperTypes(collectedTypes, superType);
			}
		}
	}

	/**
	 * In addition to {@link org.eclipse.xtext.xtext.ecoreInference.EClassifierInfos#getAllEClassInfos()} this
	 * implementation can deal with cycles in type hierarchy
	 */
	public static Collection<EClass> getAllSuperTypes(EClass eClass) {
		Set<EClass> allSuperTypes = new HashSet<>();
		collectAllSuperTypes(allSuperTypes, eClass);
		return Collections.unmodifiableSet(allSuperTypes);
	}

	/**
	 * Returns whether the given super type is the same as, or a super type of, some other class.
	 * @param superType the super type
	 * @param candidate the subtype
	 * @return whether the super type is the same as, or a super type of, some other class. Yields <code>null</code>
	 * when either argument is <code>null</code>.
	 */
	public static boolean isAssignableFrom(EClass superType, EClass candidate) {
		if (superType == null || candidate == null) {
			return false;
		}
		return superType == candidate || superType == EcorePackage.Literals.EOBJECT || superType.isSuperTypeOf(candidate);
	}

	@SuppressWarnings("unchecked")
	public static List<EObject> getAllReferencedObjects(EObject referer, EReference reference) {
		if (reference.getUpperBound() == 1) {
			EObject eObject = (EObject) referer.eGet(reference);
			if (null != eObject) {
				return Collections.singletonList(eObject);
			}
			return Collections.<EObject>emptyList();
		}
		return (List<EObject>) referer.eGet(reference);
	}

	/**
	 * checks whether the given URI can be loaded given the context. I.e. there's a resource set with a corresponding
	 * resource factory and the physical resource exists.
	 */
	public static boolean isValidUri(Resource resource, URI uri) {
		if (uri == null || uri.isEmpty()) {
			return false;
		}
		URI newURI = getResolvedImportUri(resource, uri);
		try {
			ResourceSet resourceSet = resource.getResourceSet();
			if (resourceSet.getResource(uri, false) != null) {
				return true;
			}
			URIConverter uriConverter = resourceSet.getURIConverter();
			URI normalized = uriConverter.normalize(newURI);
			if (normalized != null) {
				// fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=326760
				if ("platform".equals(normalized.scheme()) && !normalized.isPlatform()) {
					return false;
				}
				return uriConverter.exists(normalized, Collections.emptyMap());
			}
		} catch (RuntimeException e) { // thrown by org.eclipse.emf.ecore.resource.ResourceSet#getResource(URI, boolean)
			log.trace("Cannot load resource: " + newURI, e);
		}
		return false;
	}
	
	public static boolean isValidUri(EObject context, URI uri) {
		return isValidUri(context.eResource(), uri);
	}

	private static URI getResolvedImportUri(Resource context, URI uri) {
		URI contextURI = context.getURI();
		if (contextURI.isHierarchical() && !contextURI.isRelative() && (uri.isRelative() && !uri.isEmpty())) {
			uri = uri.resolve(contextURI);
		}
		return uri;
	}

	public static Resource getResource(Resource context, String uri) {
		URI newURI = getResolvedImportUri(context, URI.createURI(uri));
		try {
			return context.getResourceSet().getResource(newURI, true);
		} catch (RuntimeException e) { // thrown by org.eclipse.emf.ecore.resource.ResourceSet#getResource(URI, boolean)
			log.trace("Cannot load resource: " + newURI, e);
		}
		return null;
	}

	public static ResourceSet getResourceSet(Notifier ctx) {
		if (ctx instanceof EObject eObject) {
			return getResourceSet(eObject.eResource());
		} else if (ctx instanceof Resource resource) {
			return resource.getResourceSet();
		} else if (ctx instanceof ResourceSet resourceSet) {
			return resourceSet;
		}
		return null;
	}

	public static void resolveAll(Resource resource, CancelIndicator monitor) {
		for (Iterator<EObject> i = resource.getAllContents(); i.hasNext();) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			EObject eObject = i.next();
			resolveCrossReferences(eObject, monitor);
		}
	}

	/**
	 * If the given resource is a {@link LazyLinkingResource} the implementation delegates
	 * to {@link LazyLinkingResource#resolveLazyCrossReferences(CancelIndicator)} otherwise to
	 * {@link EcoreUtil2#resolveAll(Resource, CancelIndicator)}.
	 */
	public static void resolveLazyCrossReferences(Resource resource, CancelIndicator monitor) {
		if (resource instanceof LazyLinkingResource lazyLinkingResource) {
			lazyLinkingResource.resolveLazyCrossReferences(monitor);
		} else {
			resolveAll(resource, monitor);
		}
	}

	/**
	 * @see org.eclipse.emf.ecore.util.EcoreUtil#resolveAll(EObject)
	 */
	public static void resolveAll(EObject eObject, CancelIndicator monitor) {
		resolveCrossReferences(eObject, monitor);
		for (Iterator<EObject> i = eObject.eAllContents(); i.hasNext();) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			EObject childEObject = i.next();
			resolveCrossReferences(childEObject, monitor);
		}
	}

	private static void resolveCrossReferences(EObject eObject, CancelIndicator monitor) {
		for (Iterator<EObject> i = eObject.eCrossReferences().iterator(); i.hasNext(); i
				.next()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			// The loop resolves the cross references by visiting them.
		}
	}

	private static final char DELIM = '«';

	/**
	 * creates an external form of the given EReference. Use
	 * {@link #getEReferenceFromExternalForm(org.eclipse.emf.ecore.EPackage.Registry, String)} to retrieve the
	 * {@link EReference} back.
	 */
	public static String toExternalForm(EReference ref) {
		if (ref == null) {
			return null;
		}
		String result = externalFormCache.get(ref);
		if (result == null) {
			EClass class1 = ref.getEContainingClass();
			if (class1 == null) { // some references may be contained in an EAnnotation
				result = EcoreUtil.getURI(ref).toString();
			} else {
				StringBuilder buff = new StringBuilder(class1.getEPackage().getNsURI());
				buff.append(DELIM).append(class1.getName());
				buff.append(DELIM).append(class1.getFeatureID(ref));
				result = buff.toString();
			}
			externalFormCache.put(ref, result);
		}
		return result;
	}

	private static Map<EReference, String> externalFormCache = new MapMaker().weakKeys().makeMap();

	/**
	 * looks up the EReference in the passed registry, given the external form. if registry == null this
	 */
	public static EReference getEReferenceFromExternalForm(EPackage.Registry registry, String externalForm) {
		if (externalForm == null) {
			return null;
		}
		List<String> split = Strings.split(externalForm, DELIM);
		if (split.size() != 3) {
			URI uri = URI.createURI(externalForm);
			URI packURI = uri.trimFragment();
			EPackage ePackage = registry.getEPackage(packURI.toString());
			if (ePackage == null) {
				return null;
			}
			EReference result = (EReference) ePackage.eResource().getEObject(uri.fragment());
			return result;
		}
		EPackage ePackage = registry.getEPackage(split.get(0));
		if (ePackage == null) {
			return null;
		}
		EClass clazz = (EClass) ePackage.getEClassifier(split.get(1));
		if (clazz == null) {
			return null;
		}
		return (EReference) clazz.getEStructuralFeature(Integer.valueOf(split.get(2)));
	}

	public static boolean hasSameURI(EObject o0, EObject o1) {
		return EcoreUtil.getURI(o0).equals(EcoreUtil.getURI(o1));
	}
	
	public static URI getNormalizedResourceURI(EObject eObject) {
		if (eObject.eResource() != null) {
			return getNormalizedURI(eObject.eResource());
		}
		return URIConverter.INSTANCE.normalize(EcoreUtil.getURI(eObject).trimFragment());
	}
	
	
	public static URI getNormalizedURI(EObject eObject) {
		URI rawURI = EcoreUtil.getURI(eObject);
		ResourceSet resourceSet = getResourceSet(eObject);
		if (resourceSet != null) {
			return resourceSet.getURIConverter().normalize(rawURI);
		} else {
			return URIConverter.INSTANCE.normalize(rawURI);
		}
	}

	public static URI getNormalizedURI(Resource resource) {
		if (resource.getResourceSet() != null) {
			return resource.getResourceSet().getURIConverter().normalize(resource.getURI());
		} else {
			return URIConverter.INSTANCE.normalize(resource.getURI());
		}
	}
	
	/**
	 * @return the eobject's URI in the normalized form or as is if it is a platform:/resource URI.
	 * @since 2.4
	 */
	public static URI getPlatformResourceOrNormalizedURI(EObject eObject) {
		URI rawURI = EcoreUtil.getURI(eObject);
		if (rawURI.isPlatformResource()) {
			return rawURI;
		}
		ResourceSet resourceSet = getResourceSet(eObject);
		if (resourceSet != null) {
			return resourceSet.getURIConverter().normalize(rawURI);
		} else {
			return URIConverter.INSTANCE.normalize(rawURI);
		}
	}
	/**
	 * @return the resources uri in the normalized form or as is if it is a platform:/resource URI.
	 * @since 2.4
	 */
	public static URI getPlatformResourceOrNormalizedURI(Resource resource) {
		URI rawURI = resource.getURI();
		if (rawURI.isPlatformResource()) {
			return rawURI;
		}
		if (resource.getResourceSet() != null) {
			return resource.getResourceSet().getURIConverter().normalize(rawURI);
		} else {
			return URIConverter.INSTANCE.normalize(rawURI);
		}
	}
	
	/**
	 * A better performing alternative to the {@link org.eclipse.emf.ecore.util.EcoreUtil.CrossReferencer}.
	 * 
	 * @since 2.4
	 */
	@SuppressWarnings("unchecked")
	public static void findCrossReferences(EObject rootElement, Set<? extends EObject> targets, ElementReferenceAcceptor acceptor) {
		for (EReference ref: rootElement.eClass().getEAllReferences()) {
			if (rootElement.eIsSet(ref)) {
				if (ref.isContainment()) {
					Object content = rootElement.eGet(ref, false);
					if (ref.isMany()) {
						InternalEList<EObject> contentList = (InternalEList<EObject>) content;
						for (int i = 0; i < contentList.size(); ++i) {
							EObject childElement = contentList.basicGet(i);
							if (!childElement.eIsProxy()) {
								findCrossReferences(childElement, targets, acceptor);
							}
						}
					} else {
						EObject childElement = (EObject) content;
						if (!childElement.eIsProxy()) {
							findCrossReferences(childElement, targets, acceptor);
						}
					}
				} else if (!ref.isContainer()) {
					Object value = rootElement.eGet(ref, false);
					if (ref.isMany()) {
						InternalEList<EObject> values = (InternalEList<EObject>) value;
						for (int i = 0; i < values.size(); ++i) {
							EObject refElement = values.get(i);
							if (targets.contains(refElement)) {
								acceptor.accept(rootElement, refElement, ref, i);
							}
						}
					} else {
						EObject refElement = (EObject) value;
						if (targets.contains(refElement)) {
							acceptor.accept(rootElement, refElement, ref, -1);
						}
					}
				}
			}
		}
	}
	
	/**
	 * @since 2.4
	 */
	public static interface ElementReferenceAcceptor {
		void accept(EObject referrer, EObject referenced, EReference reference, int index);
	}
	
	/**
	 * @since 2.6
	 */
	public static TreeIterator<EObject> getAllNonDerivedContents(EObject root, boolean includeRoot) {
		/*
		 * We cannot simply use root.eAllContents here since the iterator
		 * will probe for #hasNext on each invocation of #next. This is usually
		 * not a problem but with derived containment, it becomes an issue.
		 * For example, the accessor of XAbstractFeatureCall#getImplicitReceiver uses #getFeature
		 * to initialize itself. This will cause the potential proxy feature
		 * to be resolved which in turn tries to access the mapped proxy URI fragments
		 * in the resource. Now these fragments are currently in the process of being
		 * updated, e.g. there may not even be enough entries. Thus #getFeature
		 * shall not be called here. Long story short, this iterator filters
		 * derived containment features.
		 */
		return new AbstractTreeIterator<>(root, includeRoot) {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<EObject> getChildren(Object object) {
				EObject eObject = (EObject) object;
				return getNonDerivedContents(eObject);
			}

		};
	}
	
	/**
	 * Same as {@link #getAllNonDerivedContents(EObject, boolean) getAllNonDerivedContents(root, false)}. 
	 * @since 2.6
	 */
	public static TreeIterator<EObject> getAllNonDerivedContents(EObject root) {
		return getAllNonDerivedContents(root, false);
	}

	/**
	 * @since 2.6
	 */
	public static Iterator<EObject> getNonDerivedContents(EObject eObject) {
		EClassImpl.FeatureSubsetSupplier featureSupplier = (EClassImpl.FeatureSubsetSupplier) eObject.eClass().getEAllStructuralFeatures();
		EStructuralFeature[] eStructuralFeatures = featureSupplier.containments();

		return (eStructuralFeatures == null 
				? EContentsEList.<EObject>emptyContentsEList()
				: new EContentsEList<EObject>(eObject, eStructuralFeatures) {
					@Override
					protected ListIterator<EObject> newResolvingListIterator() {
						return new ResolvingFeatureIteratorImpl<>(eObject, eStructuralFeatures) {
							@Override
							protected boolean isIncluded(EStructuralFeature eStructuralFeature) {
								return !eStructuralFeature.isDerived();
							}
						};
					}
				}).iterator();
	}
	
	
	/**
	 * Returns an Iterable that iterates over all containers of this EObject, from leaf to root. The <code>obj</code>
	 * itself is not included.
	 * 
	 * @since 2.9
	 */
	public static Iterable<EObject> getAllContainers(final EObject obj) {
		return () -> new AbstractIterator<>() {
			private EObject current = obj;

			@Override
			protected EObject computeNext() {
				current = current.eContainer();
				if (current == null) {
					return endOfData();
				}
				return current;
			}
		};
	}
	
	public static URI getFragmentPathURI(final EObject object) {
		Resource resource = object.eResource();
		if (resource != null) {
			String fragment = getFragmentPath(object);
			URI resourceURI = getPlatformResourceOrNormalizedURI(resource);
			return resourceURI.appendFragment(fragment);
		} else {
			return getPlatformResourceOrNormalizedURI(object);
		}
	}

	public static String getFragmentPath(EObject object) {
		SegmentSequence.Builder builder = SegmentSequence.newBuilder("/");
		InternalEObject internalEObject = (InternalEObject) object;
		boolean isContained = internalEObject.eDirectResource() != null;
		for (InternalEObject container = internalEObject.eInternalContainer(); 
			container != null && !isContained; 
			container = internalEObject.eInternalContainer()) {
			builder.append(getFragmentPathSegment(container, internalEObject.eContainingFeature(), internalEObject));
			internalEObject = container;
			if (container.eDirectResource() != null) {
				isContained = true;
			}
		}
		if (!isContained) {
			return "/-1";
		}
		builder.append(getFragmentPathRootSegment(internalEObject));
		builder.append("");
		builder.reverse();
		return builder.toSegmentSequence().toString();
	}
	
	@SuppressWarnings("unchecked")
	protected static String getFragmentPathSegment(InternalEObject container, EStructuralFeature feature, InternalEObject contained) {
		StringBuilder result = new StringBuilder();
		result.append('@');
		result.append(feature.getName());
		if (feature.isMany()) {
			int index = ((List<EObject>) container.eGet(feature)).indexOf(contained);
			result.append(".");
			result.append(index);
		}
		return result.toString();
	}
	
	protected static String getFragmentPathRootSegment(EObject eObject) {
		List<EObject> contents = eObject.eResource().getContents();
		if (contents.size() > 1) {
			return Integer.toString(contents.indexOf(eObject));
		} else {
			return "";
		}
	}

}
