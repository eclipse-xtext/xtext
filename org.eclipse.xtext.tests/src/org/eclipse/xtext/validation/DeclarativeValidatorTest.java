/*******************************************************************************
 * Copyright (c) 2008, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.xtext.util.Exceptions;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator.MethodWrapper;
import org.eclipse.xtext.validation.ValidationTestHelper.TestChain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @author Michael Clay
 */
public class DeclarativeValidatorTest extends Assert {

	class MethodsForTypeTestValidator extends AbstractDeclarativeValidator {

		@Check
		public void foo(Object x) { }
		
		// Exposed this method
		@Override
		public List<MethodWrapper> getMethodsForType(EObject object) {
			return super.getMethodsForType(object);
		}
		
		// Exposed this method
		@Override
		public void initializeCheckMethods() {
			super.initializeCheckMethods();
		}
	}
	
	private ValidationTestHelper helper;

	@Before
	public void setUp() throws Exception {
		this.helper = new ValidationTestHelper();
	}

	@After
	public void tearDown() throws Exception {
		this.helper = null;
	}

	@Test public void testSimpleDispatch() throws Exception {
		AbstractDeclarativeValidator test = helper.validator();
		TestChain chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, null);
		helper.assertMatch(chain, 
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, 
				EcorePackage.Literals.ECLASS__ABSTRACT);
	}

	@Test public void testDeeperHierarchyWithOverwrittenJavaMethods() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Override
			@Check
			public void foo(Object x) {
				error("fooObject", EcorePackage.Literals.ECLASS__EALL_ATTRIBUTES);
			}
		};
		TestChain chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, null);
		helper.assertMatch(chain, 
				EcorePackage.Literals.ECLASS__ABSTRACT, 
				EcorePackage.Literals.ECLASS__EALL_ATTRIBUTES);
	}

	@SuppressWarnings("serial")
	@Test public void testSkipExpensive() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Check(CheckType.EXPENSIVE)
			public void bar(Object x) {
				error("fooObject", EcorePackage.Literals.ECLASS__EALL_ATTRIBUTES);
			}
		};
		TestChain chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, new HashMap<Object, Object>() {
			{
				put(CheckMode.KEY, CheckMode.NORMAL_AND_FAST);
			}
		});
		helper.assertMatch(chain, 
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, 
				EcorePackage.Literals.ECLASS__ABSTRACT);

		chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, new HashMap<Object, Object>() {
			{
				put(CheckMode.KEY, CheckMode.ALL);
			}
		});
		helper.assertMatch(chain, 
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, 
				EcorePackage.Literals.ECLASS__ABSTRACT, 
				EcorePackage.Literals.ECLASS__EALL_ATTRIBUTES);

		chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, null);
		helper.assertMatch(chain, 
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, 
				EcorePackage.Literals.ECLASS__ABSTRACT, 
				EcorePackage.Literals.ECLASS__EALL_ATTRIBUTES);
	}

	@Test public void testGuard() throws Exception {
		AbstractDeclarativeValidator validator = new AbstractDeclarativeValidator() {
			@Check
			public void guarded(EClass x) {
				guard("".equals(x.getName()));
			}
		};

		TestChain diagnostics = helper.chain();
		validator.validate(EcorePackage.eINSTANCE.getEClass(), diagnostics, null);
		assertTrue(diagnostics.toString(), diagnostics.isEmpty());
	}

	@SuppressWarnings("serial")
	@Test public void testCheckModeSettedProperly() throws Exception {
		AbstractDeclarativeValidator test = helper.validator();
		TestChain chain = helper.chain();
		try {
			test.validate(EcorePackage.eINSTANCE.getEClass(), chain, new HashMap<Object, Object>() {
				{
					put(CheckMode.KEY, CheckType.NORMAL);
				}
			});
		}
		catch (IllegalArgumentException iae) {
			return;
		}
		fail("CheckMode with wrong type, should throw an IllegalArgumentException");
	}
	
	@Test public void testAssertThreadLocalState() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Override
			@Check
			public void foo(Object x) {
				Assert.assertNotNull("currentObject must not be null", getCurrentObject());
				Assert.assertNotNull("currentMethod must not be null", getCurrentMethod());
				Assert.assertNotNull("chain must not be null", getChain());
				Assert.assertNotNull("checkMode must not be null", getCheckMode());
				Assert.assertNotNull("context must not be null", getContext());
				
			}
		};
		TestChain chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
	}
	
	@Test public void testCurrentObjectAndMethod() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Override
			@Check
			public void foo(Object x) {
				Assert.assertEquals(EcorePackage.eINSTANCE.getEClass(), getCurrentObject());
				try {
					Assert.assertEquals(getClass().getMethod("foo", Object.class), getCurrentMethod());
				} catch (Exception e) {
					fail("unexpected");
				} 
			}
		};
		TestChain chain = helper.chain();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
	}
	
	@Test public void testContext() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Override
			@Check
			public void foo(Object x) {
				Assert.assertEquals(1, getContext().size());
				Assert.assertEquals(CheckMode.EXPENSIVE_ONLY, getContext().get(CheckMode.KEY));
			}
		};
		TestChain chain = helper.chain();
		Map<Object, Object> singletonMap = Collections.<Object, Object>singletonMap(CheckMode.KEY, CheckMode.EXPENSIVE_ONLY);
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, singletonMap);
	}
	
	@Test public void testCheckMode() throws Exception {
		AbstractDeclarativeValidator test = new ValidationTestHelper.TestValidator() {
			@Override
			@Check
			public void foo(Object x) {
				Assert.assertEquals(CheckMode.EXPENSIVE_ONLY, getCheckMode());
			}
		};
		TestChain chain = helper.chain();
		Map<Object, Object> singletonMap = Collections.<Object, Object>singletonMap(CheckMode.KEY, CheckMode.EXPENSIVE_ONLY);
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, singletonMap);
	}
	
	@Test public void testError() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				error("Error Message", EcorePackage.Literals.ENAMED_ELEMENT__NAME);
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(EcorePackage.eINSTANCE.getEClass().toString(), diag.getSource());
		assertEquals(Diagnostic.ERROR, diag.getSeverity());
	}

	@Test public void testErrorWithSource() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				error(
						"Error Message", 
						EcorePackage.eINSTANCE.getEAnnotation(), 
						EcorePackage.Literals.ENAMED_ELEMENT__NAME,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(EcorePackage.eINSTANCE.getEAnnotation().toString(), diag.getSource());
		assertEquals(Diagnostic.ERROR, diag.getSeverity());
	}

	@Test public void testErrorWithCode() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				error(
						"Error Message", 
						EcorePackage.Literals.ENAMED_ELEMENT__NAME,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
						"42");
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(0, diag.getCode());
		assertTrue(diag instanceof FeatureBasedDiagnostic);
		assertEquals("42", ((FeatureBasedDiagnostic)diag).getIssueCode());
		assertEquals(Diagnostic.ERROR, diag.getSeverity());
	}

	@Test public void testWarning() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				warning("Error Message", EcorePackage.Literals.ENAMED_ELEMENT__NAME);
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(EcorePackage.eINSTANCE.getEClass().toString(), diag.getSource());
		assertEquals(Diagnostic.WARNING, diag.getSeverity());
	}

	@Test public void testWarningWithSource() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				warning(
						"Error Message", 
						EcorePackage.eINSTANCE.getEAnnotation(), 
						EcorePackage.Literals.ENAMED_ELEMENT__NAME,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX);
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(EcorePackage.eINSTANCE.getEAnnotation().toString(), diag.getSource());
		assertEquals(Diagnostic.WARNING, diag.getSeverity());
	}

	@Test public void testWarningWithCode() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				warning(
						"Error Message", 
						EcorePackage.Literals.ENAMED_ELEMENT__NAME,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
						"42");
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		test.validate(EcorePackage.eINSTANCE.getEClass(), chain, Collections.emptyMap());
		assertEquals(1, chain.getChildren().size());

		Diagnostic diag = chain.getChildren().get(0);
		assertEquals("Error Message", diag.getMessage());
		assertEquals(0, diag.getCode());
		assertTrue(diag instanceof FeatureBasedDiagnostic);
		assertEquals("42", ((FeatureBasedDiagnostic)diag).getIssueCode());
		assertEquals(Diagnostic.WARNING, diag.getSeverity());
	}
	
	@Test public void testExceptionWhenGivenWrongEObject() {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				warning(
						"Error Message", 
						EcorePackage.Literals.EANNOTATION,
						EcorePackage.Literals.ENAMED_ELEMENT__NAME,
						ValidationMessageAcceptor.INSIGNIFICANT_INDEX,
						"42");
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		Resource r = new ResourceImpl(URI.createURI("http://foo"));
		EObject x = EcoreFactory.eINSTANCE.createEAttribute();
		r.getContents().add(x);
		try {
			test.validate(x, chain, Collections.emptyMap());
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			// expected
		}
		
	}

	// By default, NPEs occurring in validation code are swallowed 
	@Test public void testSwallowNPEInValidation () {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Check
			public void foo(Object x) {
				throw new NullPointerException();
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		Resource r = new ResourceImpl(URI.createURI("http://foo"));
		EObject x = EcoreFactory.eINSTANCE.createEAttribute();
		r.getContents().add(x);
		test.validate(x, chain, Collections.emptyMap());
		assertTrue(chain.getChildren().isEmpty());
	}

	// configure validator to forward NPEs occurring in validation code
	@Test public void testDontSwallowNPEInValidation () {
		AbstractDeclarativeValidator test = new AbstractDeclarativeValidator() {
			@Override
			protected void handleExceptionDuringValidation(Throwable targetException) throws RuntimeException {
				Exceptions.throwUncheckedException(targetException);
			}
			@Check
			public void foo(Object x) {
				throw new NullPointerException();
			}
		};
		BasicDiagnostic chain = new BasicDiagnostic();
		Resource r = new ResourceImpl(URI.createURI("http://foo"));
		EObject x = EcoreFactory.eINSTANCE.createEAttribute();
		r.getContents().add(x);
		try {
			test.validate(x, chain, Collections.emptyMap());
			fail("NPE expected");
		} catch (NullPointerException expected) {
			; // this is expected
		}
	}
	
	// Tests the ability of cache initialization #3438
	@Test public void testValidationCache() {
		MethodsForTypeTestValidator test = new MethodsForTypeTestValidator();
		test.initializeCheckMethods();
		List<MethodWrapper> methodsForType = test.getMethodsForType(EcoreFactory.eINSTANCE.createEAttribute());
		assertEquals(1, methodsForType.size());
	}
}
