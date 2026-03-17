/*******************************************************************************
 * Copyright (c) 2009, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.access.xtext.ui;

import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xtext.common.types.access.jdt.JdtTypeProviderFactory;
import org.eclipse.xtext.common.types.access.jdt.MockJavaProjectProvider;
import org.eclipse.xtext.common.types.access.xtext.AbstractTypeScopeTest;
import org.eclipse.xtext.common.types.xtext.ui.JdtBasedSimpleTypeScope;
import org.eclipse.xtext.naming.IQualifiedNameConverter;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.eclipse.xtext.testing.Flaky;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class JdtBasedSimpleTypeScopeTest extends AbstractTypeScopeTest {

	private MockJavaProjectProvider projectProvider;
	private JdtTypeProviderFactory factory;
	private ResourceSet resourceSet;
	private JdtBasedSimpleTypeScope typeScope;

	@Before
	public void setUp() throws Exception {
		projectProvider = new MockJavaProjectProvider();
		factory = new JdtTypeProviderFactory(projectProvider);
		resourceSet = new ResourceSetImpl();
		typeScope = new JdtBasedSimpleTypeScope(factory.createTypeProvider(resourceSet), new IQualifiedNameConverter.DefaultImpl(),Predicates.<IEObjectDescription>alwaysTrue());
	}
	
	@Flaky
	@Test public void testGetContents_01() {
		Iterable<IEObjectDescription> contents = typeScope.getAllElements();
		assertTrue(Iterables.any(contents, new Predicate<IEObjectDescription>() {
			@Override
			public boolean apply(IEObjectDescription input) {
				return List.class.getName().equals(input.getName().toString());
			}
		}));
	}
	
	@Test public void testGetContents_02() {
		Iterable<IEObjectDescription> contents = typeScope.getAllElements();
		assertTrue(Iterables.any(contents, new Predicate<IEObjectDescription>() {
			@Override
			public boolean apply(IEObjectDescription input) {
				return "int".equals(input.getName().toString());
			}
		}));
	}
	
	@Flaky
	@Test public void testGetContents_03() {
		Iterable<IEObjectDescription> contents = typeScope.getAllElements();
		assertTrue(Iterables.any(contents, new Predicate<IEObjectDescription>() {
			@Override
			public boolean apply(IEObjectDescription input) {
				return "java.util.Map.Entry".equals(input.getName().toString());
			}
		}));
	}

	@Override
	protected IScope getTypeScope() {
		return typeScope;
	}
}