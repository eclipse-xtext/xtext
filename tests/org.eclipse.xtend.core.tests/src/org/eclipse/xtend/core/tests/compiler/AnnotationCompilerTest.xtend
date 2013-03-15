/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler

import org.junit.Test
import org.junit.Ignore

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
class AnnotationCompilerTest extends AbstractXtendCompilerTest {
	@Test
	def testAnnotation() {
		assertCompilesTo('''
			package foo
			class Bar {
				@com.google.inject.Inject String string
			}
		''', '''
			package foo;

			import com.google.inject.Inject;

			@SuppressWarnings("all")
			public class Bar {
			  @Inject
			  private String string;
			}
		''')
	}
	
	@Test
	def testAnnotationWithDefaultEmptyStringArray() {
		assertCompilesTo('''
			annotation DependsOn {
				String[] value = #[]
			}
		''', '''
			public @interface DependsOn {
			  public String[] value() default {};
			}
		''')
	}
	
	@Test
	def testAnnotationWithDefaultStringArray() {
		assertCompilesTo('''
			annotation DependsOn {
				String[] value = #[ 'abc', 'efg' ]
			}
		''', '''
			public @interface DependsOn {
			  public String[] value() default { "abc", "efg" };
			}
		''')
	}
	
	@Test
	@Ignore
	def testAnnotationWithDefaultIntArray() {
		assertCompilesTo('''
			annotation DependsOn {
				val value = #[ 1, 2 ]
			}
		''', '''
			public @interface DependsOn {
			  public int[] value() default { 1, 2 };
			}
		''')
	}
	
	@Test
	@Ignore
	def testAnnotationWithDefaultBooleanArray() {
		assertCompilesTo('''
			annotation DependsOn {
				val value = #[ true, true ]
			}
		''', '''
			public @interface DependsOn {
			  public boolean[] value() default { true, true };
			}
		''')
	}
	
	@Test
	@Ignore
	def testAnnotationWithDefaultLongArray() {
		assertCompilesTo('''
			annotation DependsOn {
				val value = #[ 1l, 2l ]
			}
		''', '''
			public @interface DependsOn {
			  public long[] value() default { 1l, 2l };
			}
		''')
	}
	
	@Test
	def testAnnotationWithClassArray() {
		assertCompilesTo('''
			annotation DependsOn {
				Class<?>[] value = #[ typeof(String), typeof(CharSequence) ]
			}
		''', '''
			public @interface DependsOn {
			  public Class<? extends Object>[] value() default { String.class, CharSequence.class };
			}
		''')
	}
	
	@Test
	def testAnnotationOnAnnotation() {
		assertCompilesTo('''
			package foo
			@java.lang.annotation.Documented
			annotation Bar {
			    @com.google.inject.Inject String string
			}
		''', '''
			package foo;
			
			import com.google.inject.Inject;
			import java.lang.annotation.Documented;
			
			@Documented
			public @interface Bar {
			  @Inject
			  public String string();
			}
		''')
	}
	
	@Test
	def compileAnnotationWithFileHeader(){
		assertCompilesTo(
		''' 
			/**
			 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
			 * All rights reserved. This program and the accompanying materials
			 * are made available under the terms of the Eclipse Public License v1.0
			 * which accompanies this distribution, and is available at
			 * http://www.eclipse.org/legal/epl-v10.html
			 */
			package foo
			
			annotation bar { 
			    String name = 'foobar'
			}
		''',
		'''
			/**
			 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
			 * All rights reserved. This program and the accompanying materials
			 * are made available under the terms of the Eclipse Public License v1.0
			 * which accompanies this distribution, and is available at
			 * http://www.eclipse.org/legal/epl-v10.html
			 */
			package foo;
			
			public @interface bar {
			  public String name() default "foobar";
			}
		''')
	}
	
	@Test
	def testAnnotationType_1(){
		assertCompilesTo(
		'''
			annotation MyAnnotation { 
				String x;
				int y;
				Class<?>[] value;
			}
		''','''
			public @interface MyAnnotation {
			  public String x();
			  public int y();
			  public Class<? extends Object>[] value();
			}
		''')
	}
	
	@Test
	def testAnnotationType_2(){
		assertCompilesTo(
		'''
			annotation MyAnnotation { 
				String x = 'foo'
				int y = 42
				Class<?> value = typeof(String)
				boolean flag = true
			}
		''','''
			public @interface MyAnnotation {
			  public String x() default "foo";
			  public int y() default 42;
			  public Class<? extends Object> value() default String.class;
			  public boolean flag() default true;
			}
		''')
	}
	
	@Test
	def testAnnotationType_3(){
		assertCompilesTo(
		'''
			annotation annotation { 
				String annotation = 'foo'
				val inferred = 'bar'
				val inferredClass = typeof(StringBuilder)
			}
		''','''
			public @interface annotation {
			  public String annotation() default "foo";
			  public String inferred() default "bar";
			  public Class<StringBuilder> inferredClass() default StringBuilder.class;
			}
		''')
	}
	
	@Test def testAnnotationWithIntArray() throws Exception {
		'''
			class TestXtend {
				val static int a = 4
				
				@Click(#[ a, a ])
				def meth() {}
			}
			
			annotation Click {
				int[] value
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class TestXtend {
			  private final static int a = 4;
			  
			  @Click({ TestXtend.a, TestXtend.a })
			  public Object meth() {
			    return null;
			  }
			}
		''')
	}	 
}