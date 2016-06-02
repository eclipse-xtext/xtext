/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xpect.xtext.lib.tests;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xpect.XpectImport;
import org.eclipse.xpect.expectation.IStringExpectation;
import org.eclipse.xpect.expectation.StringExpectation;
import org.eclipse.xpect.parameter.ParameterParser;
import org.eclipse.xpect.runner.Xpect;
import org.eclipse.xpect.runner.XpectRunner;
import org.eclipse.xpect.xtext.lib.setup.ThisOffset;
import org.eclipse.xpect.xtext.lib.setup.XtextStandaloneSetup;
import org.eclipse.xpect.xtext.lib.setup.XtextWorkspaceSetup;
import org.eclipse.xpect.xtext.lib.util.XtextOffsetAdapter.ICrossEReferenceAndEObject;
import org.eclipse.xtext.common.types.access.TypeResource;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.XtextResource;
import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@SuppressWarnings("restriction")
@RunWith(XpectRunner.class)
@XpectImport({ XtextStandaloneSetup.class, XtextWorkspaceSetup.class })
public class LinkingTest {

	protected String deresolve(URI base, URI uri) {
		if (base.equals(uri.trimFragment()))
			return uri.fragment();
		return uri.deresolve(base).toString();
	}

	@Xpect
	@ParameterParser(syntax = "('at' arg1=OFFSET)?")
	public void linkedFragment(@StringExpectation IStringExpectation expectation, @ThisOffset ICrossEReferenceAndEObject arg1) {
		EObject targetObject = (EObject) arg1.getEObject().eGet(arg1.getCrossEReference());
		if (targetObject == null)
			Assert.fail("Reference is null");
		if (targetObject.eIsProxy())
			Assert.fail("Reference is a Proxy: " + ((InternalEObject) targetObject).eProxyURI());
		Resource targetResource = targetObject.eResource();
		if (targetResource == null)
			Assert.fail("Referenced EObject is not in a resource.");
		URI target = EcoreUtil.getURI(targetObject);
		URI base = arg1.getEObject().eResource().getURI();
		String actual = deresolve(base, target);
		expectation.assertEquals(actual);
	}

	@Xpect
	@ParameterParser(syntax = "('at' arg1=OFFSET)?")
	public void linkedName(@StringExpectation IStringExpectation expectation, @ThisOffset ICrossEReferenceAndEObject arg1) {
		EObject targetObject = (EObject) arg1.getEObject().eGet(arg1.getCrossEReference());
		if (targetObject == null)
			Assert.fail("Reference is null");
		if (targetObject.eIsProxy())
			Assert.fail("Reference is a Proxy: " + ((InternalEObject) targetObject).eProxyURI());
		Resource targetResource = targetObject.eResource();
		if (targetResource instanceof TypeResource)
			targetResource = arg1.getEObject().eResource();
		if (!(targetResource instanceof XtextResource))
			Assert.fail("Referenced EObject is not in an XtextResource.");
		IQualifiedNameProvider provider = ((XtextResource) targetResource).getResourceServiceProvider().get(IQualifiedNameProvider.class);
		QualifiedName name = provider.getFullyQualifiedName(targetObject);
		expectation.assertEquals(name);
	}
}
