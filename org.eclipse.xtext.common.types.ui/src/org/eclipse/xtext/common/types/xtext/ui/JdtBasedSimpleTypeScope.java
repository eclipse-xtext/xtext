/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.xtext.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.access.impl.Primitives;
import org.eclipse.xtext.common.types.access.jdt.IJdtTypeProvider;
import org.eclipse.xtext.common.types.xtext.AbstractTypeScope;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Jan Koehnlein - introduced QualifiedName
 */
public class JdtBasedSimpleTypeScope extends AbstractTypeScope {

	public JdtBasedSimpleTypeScope(IJdtTypeProvider typeProvider, IQualifiedNameConverter qualifiedNameConverter, Predicate<IEObjectDescription> filter) {
		super(typeProvider, qualifiedNameConverter, filter);
	}

	@Override
	protected Iterable<IEObjectDescription> internalGetAllElements() {
		IJavaProject javaProject = getTypeProvider().getJavaProject();
		if (javaProject == null)
			return Collections.emptyList();
		final List<IEObjectDescription> allScopedElements = Lists.newArrayListWithExpectedSize(25000);
		try {
			IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject });
			for(Class<?> clazz: Primitives.ALL_PRIMITIVE_TYPES) {
				IEObjectDescription primitive = createScopedElement(clazz.getName());
				if (primitive != null)
					allScopedElements.add(primitive);
			}
			TypeNameRequestor nameMatchRequestor = new TypeNameRequestor() {
				@Override
				public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName,
						char[][] enclosingTypeNames, String path) {
					StringBuilder fqName = new StringBuilder(packageName.length + simpleTypeName.length + 1);
					if (packageName.length != 0) {
						fqName.append(packageName);
						fqName.append('.');
					}
					for(char[] enclosingType: enclosingTypeNames) {
						fqName.append(enclosingType);
						fqName.append('.');
					}
					fqName.append(simpleTypeName);
					String fullyQualifiedName = fqName.toString();
					InternalEObject proxy = createProxy(fullyQualifiedName);
					Map<String, String> userData = null;
					if (enclosingTypeNames.length == 0) {
						userData = ImmutableMap.of("flags", String.valueOf(modifiers));
					} else {
						userData = ImmutableMap.of("flags", String.valueOf(modifiers), "inner", "true");
					}
					IEObjectDescription eObjectDescription = EObjectDescription.create(getQualifiedNameConverter().toQualifiedName(fullyQualifiedName), proxy, userData);
					if (eObjectDescription != null)
						allScopedElements.add(eObjectDescription);
				}
			};
			collectContents(searchScope, nameMatchRequestor);
		}
		catch (JavaModelException e) {
			// ignore
		}
		return allScopedElements;
	}
	
	public IEObjectDescription createScopedElement(String fullyQualifiedName) {
		InternalEObject proxy = createProxy(fullyQualifiedName);
		IEObjectDescription eObjectDescription = EObjectDescription.create(getQualifiedNameConverter().toQualifiedName(fullyQualifiedName), proxy);
		return eObjectDescription;
	}

	protected InternalEObject createProxy(String fullyQualifiedName) {
		URI uri = getTypeProvider().getTypeUriHelper().getFullURIForClass(fullyQualifiedName);
		InternalEObject proxy = (InternalEObject) TypesFactory.eINSTANCE.createJvmVoid();
		proxy.eSetProxyURI(uri);
		return proxy;
	}
	
	public void collectContents(IJavaSearchScope searchScope, TypeNameRequestor nameMatchRequestor) throws JavaModelException {
		new SearchEngine().searchAllTypeNames(
				null, 0, // match all package names
				null, 0, // and all type names,
				IJavaSearchConstants.TYPE, // search for types
				searchScope, // in the scope of the current project
				nameMatchRequestor, 
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, // wait for the jdt index to be ready
				new NullProgressMonitor());
	}
	
	@Override
	public IJdtTypeProvider getTypeProvider() {
		return (IJdtTypeProvider) super.getTypeProvider();
	}

}
