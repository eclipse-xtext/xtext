/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler

import org.junit.Test

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
class ExtensionsCompilerTest extends AbstractXtendCompilerTest {
	
	@Test
	def testExtensionAnnotations() {
		assertCompilesTo('''
			class C {
				def void m(extension String s) {
					extension val Double d = 1.0
					for(extension j: 1..1) {
						[ extension Object o | 
							try {
							} catch(extension Throwable t) {
							} 
						].apply(null)
					}
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Exceptions;
			import org.eclipse.xtext.xbase.lib.Extension;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			import org.eclipse.xtext.xbase.lib.IntegerRange;
			
			@SuppressWarnings("all")
			public class C {
			  public void m(@Extension final String s) {
			    @Extension
			    final Double d = Double.valueOf(1.0);
			    IntegerRange _upTo = new IntegerRange(1, 1);
			    for (@Extension final Integer j : _upTo) {
			      final Function1<Object,Object> _function = new Function1<Object,Object>() {
			          public Object apply(@Extension final Object o) {
			            Object _xtrycatchfinallyexpression = null;
			            try {
			              _xtrycatchfinallyexpression = null;
			            } catch (final Throwable _t) {
			              if (_t instanceof Throwable) {
			                @Extension final Throwable t = (Throwable)_t;
			                _xtrycatchfinallyexpression = null;
			              } else {
			                throw Exceptions.sneakyThrow(_t);
			              }
			            }
			            return _xtrycatchfinallyexpression;
			          }
			        };
			      _function.apply(null);
			    }
			  }
			}
		''')
	}
	
	@Test
	def testExtensionField() { 
		assertCompilesTo(
			'''
				class NoNPE {
					extension String
					def useExtension() {
						_string.toString
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class NoNPE {
				  @Extension
				  private String _string;
				  
				  public String useExtension() {
				    String _string = this._string.toString();
				    return _string;
				  }
				}
			''')
	}
	
	@Test
	def testInheritedExtensionField() { 
		assertCompilesTo(
			'''
				class C extends D {
					def m(int it) {
						substring
						it.substring
					}
				}
				class D {
					protected extension String
				}
			''', '''
				@SuppressWarnings("all")
				public class C extends D {
				  public String m(final int it) {
				    String _xblockexpression = null;
				    {
				      this._string.substring(it);
				      String _substring = this._string.substring(it);
				      _xblockexpression = (_substring);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testInheritedExtensionFieldWithAnnotation() { 
		assertCompilesTo(
			'''
				import org.eclipse.xtext.xbase.lib.Extension
				class C extends D {
					def m(int it) {
						substring
						it.substring
					}
				}
				class D {
					@Extension protected String string
				}
			''', '''
				@SuppressWarnings("all")
				public class C extends D {
				  public String m(final int it) {
				    String _xblockexpression = null;
				    {
				      this.string.substring(it);
				      String _substring = this.string.substring(it);
				      _xblockexpression = (_substring);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testShadowedExtensionField() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension String local
					def m(int it) {
						substring
					}
				}
				class D {
					protected extension String inherited
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected String local;
				  
				  public String m(final int it) {
				    String _substring = this.local.substring(it);
				    return _substring;
				  }
				}
			''')
	}
	
	@Test
	def testOverloadedExtensionField_01() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension CharSequence local
					def m(int it) {
						charAt
					}
				}
				class D {
					protected extension String inherited
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected CharSequence local;
				  
				  public char m(final int it) {
				    char _charAt = this.local.charAt(it);
				    return _charAt;
				  }
				}
			''')
	}
	
	@Test
	def testOverloadedExtensionField_02() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension A local
					def m(CharSequence it) {
						ext
					}
				}
				class D {
					protected extension B inherited
				}
				class B {
					def void ext(CharSequence s) {}
				}
				class A {
					def void ext(String s) {}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected A local;
				  
				  public void m(final CharSequence it) {
				    this.inherited.ext(it);
				  }
				}
			''')
	}
	
	@Test
	def testOverloadedExtensionField_03() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension A local
					def m(String it) {
						ext
					}
				}
				class D {
					protected extension B inherited
				}
				class B {
					def void ext(CharSequence s) {}
				}
				class A {
					def void ext(String s) {}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected A local;
				  
				  public void m(final String it) {
				    this.local.ext(it);
				  }
				}
			''')
	}
	
	@Test
	def testOverloadedExtensionField_04() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension B local
					def m(CharSequence it) {
						ext
					}
				}
				class D {
					protected extension A inherited
				}
				class B {
					def void ext(CharSequence s) {}
				}
				class A {
					def void ext(String s) {}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected B local;
				  
				  public void m(final CharSequence it) {
				    this.local.ext(it);
				  }
				}
			''')
	}
	
	@Test
	def testPrivateInheritedExtensionField() { 
		assertCompilesTo(
			'''
				class C extends D {
					protected extension A local
					def m(String it) {
						ext
					}
				}
				class D {
					extension B inherited
				}
				class B {
					def void ext(CharSequence s) {}
				}
				class A {
					def void ext(String s) {}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C extends D {
				  @Extension
				  protected A local;
				  
				  public void m(final String it) {
				    this.local.ext(it);
				  }
				}
			''')
	}
	
	@Test
	def testExtensionParameterShadowsField() { 
		assertCompilesTo(
			'''
				class C {
					extension String
					def m(int it, extension String s) {
						substring
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  @Extension
				  private String _string;
				  
				  public String m(final int it, @Extension final String s) {
				    String _substring = s.substring(it);
				    return _substring;
				  }
				}
			''')
	}
	
	@Test
	def testExtensionParameterWithAnnotation() { 
		assertCompilesTo(
			'''
				import org.eclipse.xtext.xbase.lib.Extension
				class C {
					extension String
					def m(int it, @Extension String s) {
						substring
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  @Extension
				  private String _string;
				  
				  public String m(final int it, @Extension final String s) {
				    String _substring = s.substring(it);
				    return _substring;
				  }
				}
			''')
	}
	
	@Test
	def testExtensionLocalShadowsParam() { 
		assertCompilesTo(
			'''
				class C {
					def m(int it, extension String param) {
						val extension String s = ''
						substring
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  public String m(final int it, @Extension final String param) {
				    String _xblockexpression = null;
				    {
				      @Extension
				      final String s = "";
				      String _substring = s.substring(it);
				      _xblockexpression = (_substring);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testExtensionVarIsLocalInBlock() { 
		assertCompilesTo(
			'''
				class C {
					def m(int it, extension String param) {
						{
							val extension String s = ''
							s.toString
						}
						substring
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  public String m(final int it, @Extension final String param) {
				    String _xblockexpression = null;
				    {
				      {
				        @Extension
				        final String s = "";
				        s.toString();
				      }
				      String _substring = param.substring(it);
				      _xblockexpression = (_substring);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testExtensionParamOnLamdba() { 
		assertCompilesTo(
			'''
				class C {
					def m(int it) {
						<String>newArrayList.forEach[ extension s |
							substring
						]
					}
				}
			''', '''
				import java.util.ArrayList;
				import org.eclipse.xtext.xbase.lib.CollectionLiterals;
				import org.eclipse.xtext.xbase.lib.Extension;
				import org.eclipse.xtext.xbase.lib.IterableExtensions;
				import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
				
				@SuppressWarnings("all")
				public class C {
				  public void m(final int it) {
				    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
				    final Procedure1<String> _function = new Procedure1<String>() {
				        public void apply(@Extension final String s) {
				          s.substring(it);
				        }
				      };
				    IterableExtensions.<String>forEach(_newArrayList, _function);
				  }
				}
			''')
	}
	
	@Test
	def testExtensionParamOnForEach() { 
		assertCompilesTo(
			'''
				class C {
					def m(int it) {
						for(extension s: <String>newArrayList) {
							substring
						}
					}
				}
			''', '''
				import java.util.ArrayList;
				import org.eclipse.xtext.xbase.lib.CollectionLiterals;
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  public void m(final int it) {
				    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
				    for (@Extension final String s : _newArrayList) {
				      s.substring(it);
				    }
				  }
				}
			''')
	}
	
	@Test
	def testExtensionParamOnTryCatch() { 
		assertCompilesTo(
			'''
				class C {
					def m(Exception it) {
						try {} catch(extension RuntimeException e) {
							initCause
						}
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Exceptions;
				import org.eclipse.xtext.xbase.lib.Extension;
				
				@SuppressWarnings("all")
				public class C {
				  public Throwable m(final Exception it) {
				    Throwable _xtrycatchfinallyexpression = null;
				    try {
				      _xtrycatchfinallyexpression = null;
				    } catch (final Throwable _t) {
				      if (_t instanceof RuntimeException) {
				        @Extension final RuntimeException e = (RuntimeException)_t;
				        Throwable _initCause = e.initCause(it);
				        _xtrycatchfinallyexpression = _initCause;
				      } else {
				        throw Exceptions.sneakyThrow(_t);
				      }
				    }
				    return _xtrycatchfinallyexpression;
				  }
				}
			''')
	}
	
	@Test def void testExtensionForArrayOfT_01() {
		'''
			class C {
				def m1() {
					val String[] chars = newArrayList('foo','bar')
					chars.at(2)
				}
				def m2() {
					newArrayList('foo','bar').at(2)
				}
				def m3() {
					newArrayList('foo','bar') => [ at(2) ]
				}
				def <T> T at(T[] obj, int index) {
					return obj.get(index)
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.lib.Conversions;
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public class C {
			  public String m1() {
			    String _xblockexpression = null;
			    {
			      final String[] chars = ((String[])Conversions.unwrapArray(CollectionLiterals.<String>newArrayList("foo", "bar"), String.class));
			      String _at = this.<String>at(chars, 2);
			      _xblockexpression = (_at);
			    }
			    return _xblockexpression;
			  }
			  
			  public String m2() {
			    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("foo", "bar");
			    String _at = this.<String>at(((String[])Conversions.unwrapArray(_newArrayList, String.class)), 2);
			    return _at;
			  }
			  
			  public ArrayList<String> m3() {
			    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("foo", "bar");
			    final Procedure1<ArrayList<String>> _function = new Procedure1<ArrayList<String>>() {
			        public void apply(final ArrayList<String> it) {
			          C.this.<String>at(((String[])Conversions.unwrapArray(it, String.class)), 2);
			        }
			      };
			    ArrayList<String> _doubleArrow = ObjectExtensions.<ArrayList<String>>operator_doubleArrow(_newArrayList, _function);
			    return _doubleArrow;
			  }
			  
			  public <T extends Object> T at(final T[] obj, final int index) {
			    return obj[index];
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_01() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def String m(Pair<String, String> in) {
			    	(''->'').m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Pair<String,String> in) {
			    Pair<String,String> _mappedTo = Pair.<String, String>of("", "");
			    String _m = this.m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_02() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def String m(Pair<String, String> in) {
			    	(null->null).m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Pair<String,String> in) {
			    Pair<String,String> _mappedTo = Pair.<String, String>of(null, null);
			    String _m = this.m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_03() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def String m(Pair<String, String> in) {
			    	in.m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Pair<String,String> in) {
			    String _m = this.m(in);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_04() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T> T m(Pair<T, T> in) {
			    	in.m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> T m(final Pair<T,T> in) {
			    T _m = this.<T>m(in);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_05() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T> T m(Pair<T, T> in) {
			    	(null->null).m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> T m(final Pair<T,T> in) {
			    Pair<T,T> _mappedTo = Pair.<T, T>of(null, null);
			    T _m = this.<T>m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_06() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T> String m(Pair<T, T> in) {
			    	(''->'').m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> String m(final Pair<T,T> in) {
			    Pair<String,String> _mappedTo = Pair.<String, String>of("", "");
			    String _m = this.<String>m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_07() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T extends CharSequence> T m(Pair<T, T> in) {
			    	in.m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends CharSequence> T m(final Pair<T,T> in) {
			    T _m = this.<T>m(in);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_08() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T extends CharSequence> T m(Pair<T, T> in) {
			    	(null->null).m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends CharSequence> T m(final Pair<T,T> in) {
			    Pair<T,T> _mappedTo = Pair.<T, T>of(null, null);
			    T _m = this.<T>m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_09() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def String m(Pair<String, String> in) {
			    	(""->�"''''''"�).m
				}
			}
		''', '''
			import org.eclipse.xtend2.lib.StringConcatenation;
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Pair<String,String> in) {
			    StringConcatenation _builder = new StringConcatenation();
			    Pair<String,String> _mappedTo = Pair.<String, String>of("", _builder.toString());
			    String _m = this.m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_10() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T> String m(Pair<T, T> it) {
			    	m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> String m(final Pair<T,T> it) {
			    String _m = this.<T>m(it);
			    return _m;
			  }
			}
		''')
	}
	
	@Test def void testLocalExtensionForPairStringString_11() {
		assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Pair
			public class C  {
			    def <T> String m(Pair<T, T> it) {
			    	(key->value).m
				}
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Pair;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> String m(final Pair<T,T> it) {
			    T _key = it.getKey();
			    T _value = it.getValue();
			    Pair<T,T> _mappedTo = Pair.<T, T>of(_key, _value);
			    String _m = this.<T>m(_mappedTo);
			    return _m;
			  }
			}
		''')
	}
	
	@Test
	def testStringExtensionInlined_01() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				def String returnString(String x, String y) {
					x + '' + y
				}
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Foo {
			  public String returnString(final String x, final String y) {
			    String _plus = (x + "");
			    String _plus_1 = (_plus + y);
			    return _plus_1;
			  }
			}
		''')
	}
}