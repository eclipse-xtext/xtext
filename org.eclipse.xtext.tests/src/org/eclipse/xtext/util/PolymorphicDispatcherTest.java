/*******************************************************************************
 * Copyright (c) 2008, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.util;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("unused")
public class PolymorphicDispatcherTest extends Assert {

	class SuperClass {
		String label(Number i) {
			return "Number_from_superclass_" + i;
		}
	}

	@Test public void testPerformance() throws Exception {
		Object first = new SuperClass() {
			String label(Integer i) {
				return "Integer_first_" + i;
			}

			String label(BigInteger i) {
				return "BigInteger_" + i;
			}
		};
		Object second = new Object() {
			String label(Integer i) {
				return "Integer_second_" + i;
			}

			String label(Number i) {
				return "Number_" + i;
			}

			String label(Object i) {
				return "Object_" + i;
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(first,
				second));
		long before = System.currentTimeMillis();
		for (int i = 0; i < 30000; i++) {
			String string = dispatcher.invoke(34);
			assertNotNull(string);
		}
		long after = System.currentTimeMillis();
		assertTrue(String.format("Expected %d < 2000", after - before), (after - before) < 2000);
	}

	@Test public void testSimple() throws Exception {
		Object first = new SuperClass() {
			String label(Integer i) {
				return "Integer_first_" + i;
			}

			String label(BigInteger i) {
				return "BigInteger_" + i;
			}
		};
		Object second = new Object() {
			String label(Integer i) {
				return "Integer_second_" + i;
			}

			String label(Number i) {
				return "Number_" + i;
			}

			String label(Object i) {
				return "Object_" + i;
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(first,
				second));

		assertEquals("Integer_first_3", dispatcher.invoke(Integer.valueOf(3)));
		assertEquals("Number_from_superclass_3", dispatcher.invoke(Long.valueOf(3)));
		assertEquals("BigInteger_3", dispatcher.invoke(BigInteger.valueOf(3)));
		assertEquals("Object_foo", dispatcher.invoke("foo"));
	}
	
	@Test public void testSimple_01() throws Exception {
		Object o1 = new Object() {
			String label(CharSequence x) {return "c";}
			String label(Comparable<?> x) {return "comp";}
			String label(Serializable x) {return "seri";}
			String label(Integer x) {return "i";}
			String label(String x) {return "s";}
			String label(Number x) {return "n";}
			String label(Object x) {return "o";}
		};
		PolymorphicDispatcher<String> target = PolymorphicDispatcher.createForSingleTarget("label", o1);
		assertEquals("s",target.invoke("Foo"));
		assertEquals("i",target.invoke(42));
	}

	@Test public void testMixedTypes() throws Exception {
		Object o1 = new Object() {

			String label(Number i) {
				return "Number_" + i;
			}

			String label(String i) {
				return "String_" + i;
			}

			String label(Integer i) {
				return "Integer_" + i;
			}

		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1));

		assertEquals("Integer_3", dispatcher.invoke(Integer.valueOf(3)));
	}

	@Test public void testDifferentParamLength() throws Exception {
		Object o1 = new Object() {
			String label(Integer i, Object stuff) {
				return "Integer_" + i + "_" + stuff;
			}

			String label(Number i, String foo) {
				return "Number_" + i + "_" + foo;
			}

			String label(Object i) {
				return "Object_" + i;
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", 1, 2,
				Lists.newArrayList(o1), new PolymorphicDispatcher.ErrorHandler<String>() {
					@Override
					public String handle(Object[] params, Throwable throwable) {
						return null;
					}
				});
		assertEquals("Object_3", dispatcher.invoke(Integer.valueOf(3)));
		assertNull("Integer_3_4", dispatcher.invoke(Long.valueOf(3), 4));
		assertEquals("Integer_3_4", dispatcher.invoke(Integer.valueOf(3), 4));
		assertEquals("Object_3", dispatcher.invoke(BigInteger.valueOf(3)));
		assertEquals("Number_3_foo", dispatcher.invoke(BigInteger.valueOf(3), "foo"));
	}

	@Test public void testAmbiguous() throws Exception {
		Object o1 = new Object() {

			String label(CharSequence i) {
				return CharSequence.class.getSimpleName();
			}

			String label(Comparable<?> c) {
				return Comparable.class.getSimpleName();
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1));
		try {
			dispatcher.invoke("string");
			fail("exception expected, due to ambiguous methods");
		} catch (IllegalStateException e) {
			// ignore
		}
	}
	
	interface A {}
	interface B {}
	interface CB extends B {}
	class DCBA implements A, CB, E {}
	interface E {}
	
	@Test public void testAmbiguous2() throws Exception {
		Object o1 = new Object() {
			String label(A i) {
				return "A";
			}
			String label(B b) {
				return "B";
			}
			String label(CB cb) {
				return "CB";
			}
			String label(E e) {
				return "E";
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1)) {
			@Override
			protected String handleAmbigousMethods(List<MethodDesc> result, Object... params) {
				List<String> p = new ArrayList<>();
				for(MethodDesc md:result) {
					p.add(md.getParameterTypes()[0].getSimpleName());
				}
				Collections.sort(p);
				return Joiner.on(", ").join(p);
			}
		};
		String string = dispatcher.invoke(new DCBA());
		assertEquals("A, CB, E", string);
	}

	@Test public void testNullParams() throws Exception {
		Object o1 = new Object() {

			String label(String i, CharSequence y) {
				return CharSequence.class.getSimpleName();
			}

			String label(String c, String s) {
				return String.class.getSimpleName();
			}
			
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", 2, 2,
				Lists.newArrayList(o1));
		assertEquals(String.class.getSimpleName(), dispatcher.invoke(null, "Foo"));
		assertEquals(String.class.getSimpleName(), dispatcher.invoke("Foo", null));
		assertEquals(CharSequence.class.getSimpleName(), dispatcher.invoke(null, new StringBuilder()));
	}
	
	@Test public void testNullParams_1() throws Exception {
		Object o1 = new Object() {
			
			String label(Void x, CharSequence y) {
				return CharSequence.class.getSimpleName();
			}
			
			String label(String c, Void s) {
				return String.class.getSimpleName();
			}
			
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", 2, 2,
				Lists.newArrayList(o1));
		assertEquals(CharSequence.class.getSimpleName(), dispatcher.invoke(null, "Foo"));
		assertEquals(String.class.getSimpleName(), dispatcher.invoke("Foo", null));
		assertEquals(CharSequence.class.getSimpleName(), dispatcher.invoke(null, null));
	}

	@Test public void testNullDefaultClass() throws Exception {
		Object o1 = new Object() {
			String label(String s) {
				return String.class.getSimpleName();
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1)) {
			@Override
			protected Class<?> getDefaultClass(int paramIndex) {
				return null;
			}
		};
		assertEquals(String.class.getSimpleName(), dispatcher.invoke((Object) null));
		assertEquals(String.class.getSimpleName(), dispatcher.invoke((Object) null));
	}

	class SuperArg {}
	class SubArg extends SuperArg {}

	@Test public void testSubclassAfterSuperclass() throws Exception {
		Object o1 = new Object() {
			String label(Object o) {
				return "Object";
			}
			String label(SuperArg a) {
				return "SuperArg";
			}
			String label(SubArg a) {
				return "SubArg";
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1));
		assertEquals("SuperArg", dispatcher.invoke(new SuperArg()));
		assertEquals("SubArg", dispatcher.invoke(new SubArg()));
		assertEquals("SuperArg", dispatcher.invoke(new SuperArg() {}));
		assertEquals("SubArg", dispatcher.invoke(new SubArg() {}));
		assertEquals("Object", dispatcher.invoke("Foo"));
	}

	@Test public void testCachedAcrossGc() throws Exception {
		Object o1 = new Object() {
			String label(Object o) {
				return Object.class.getSimpleName();
			}
			String label(String s) {
				return String.class.getSimpleName();
			}
		};
		int[] compared = new int[1];
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1)) {
			@Override
			protected int compare(MethodDesc o1, MethodDesc o2) {
				compared[0]++;
				return super.compare(o1, o2);
			}
		};
		assertEquals(String.class.getSimpleName(), dispatcher.invoke("Foo"));
		int comparedOnFirstCall = compared[0];
		assertTrue(comparedOnFirstCall > 0);
		System.gc();
		assertEquals(String.class.getSimpleName(), dispatcher.invoke("Bar"));
		assertEquals(comparedOnFirstCall, compared[0]);
	}

	@Test public void testPrivateMethodAccess() {
		Object o1 = new Object() {
			private String label(Integer i) {
				return "Integer_" + i;
			}

			private String label(Number n) {
				return "Number_" + n;
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>("label", Lists.newArrayList(o1));
		assertEquals("Integer_17", dispatcher.invoke(Integer.valueOf(17)));
		assertEquals("Number_42", dispatcher.invoke(BigInteger.valueOf(42)));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	private @interface TestLabelAnnotation {
	}

	@Test public void testCustomFilter() {
		Object o1 = new Object() {
			@TestLabelAnnotation
			private String sillyMethodName(Integer i) {
				return "Integer_" + i;
			}

			private String label(Number n) {
				return "Number_" + n;
			}
		};
		PolymorphicDispatcher<String> dispatcher = new PolymorphicDispatcher<String>(Lists.newArrayList(o1),
				new Predicate<Method>() {
					@Override
					public boolean apply(Method param) {
						return ((param.getName().equals("label")) || (param.getAnnotation(TestLabelAnnotation.class) != null));
					}
				});

		assertEquals("Integer_17", dispatcher.invoke(Integer.valueOf(17)));
		assertEquals("Number_42", dispatcher.invoke(BigInteger.valueOf(42)));
	}

}
