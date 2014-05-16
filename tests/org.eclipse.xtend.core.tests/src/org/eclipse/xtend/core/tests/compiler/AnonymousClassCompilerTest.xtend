/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler

import org.junit.Test

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
class AnonymousClassCompilerTest extends AbstractXtendCompilerTest {
	
	@Test
	def void testPlain() {
		'''
			class C {
				def m() {
					new Runnable() {
						override run() {}
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public Runnable m() {
			    return new Runnable() {
			      public void run() {
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testConstructor() {
		'''
			class C {
				def m() {
					new D(true) {
					}
				}
				static class D {
					new(boolean b) {}
					new(int i) {}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  @SuppressWarnings("all")
			  public static class D {
			    public D(final boolean b) {
			    }
			    
			    public D(final int i) {
			    }
			  }
			  
			  public C.D m() {
			    return new C.D(true) {
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testConstructorAdditionalMember() {
		'''
			class C {
				def m() {
					new D(true) {
						def void n() {}
					}
				}
				static class D {
					new(boolean b) {}
					new(int i) {}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  @SuppressWarnings("all")
			  public static class D {
			    public D(final boolean b) {
			    }
			    
			    public D(final int i) {
			    }
			  }
			  
			  public C.D m() {
			    @SuppressWarnings("all")
			    final class __C_1 extends C.D {
			      public void n() {
			      }
			      
			      __C_1(final boolean b) {
			        super(b);
			      }
			    }
			    
			    return new __C_1(true);
			  }
			}
		''')
	}
	
	@Test
	def void testNestedLocalClass_01() {
		'''
			class C {
				def m() {
					return newArrayList(new Runnable() { override run() {} })
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public ArrayList<? extends Runnable> m() {
			    return CollectionLiterals.<Runnable>newArrayList(new Runnable() {
			      public void run() {
			      }
			    });
			  }
			}
		''')
	}
	
	@Test
	def void testNestedLocalClass_02() {
		'''
			class C {
				def Iterable<Runnable> m() {
					return newArrayList(new Runnable() { override run() {} })
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public Iterable<Runnable> m() {
			    return CollectionLiterals.<Runnable>newArrayList(new Runnable() {
			      public void run() {
			      }
			    });
			  }
			}
		''')
	}
	
	@Test
	def void testNestedLocalClass_03() {
		'''
			class C {
				def m() {
					return newArrayList(
						new Runnable() { override run() {} def void m() {} }
					)
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public ArrayList<? extends Runnable> m() {
			    @SuppressWarnings("all")
			    final class __C_1 implements Runnable {
			      public void run() {
			      }
			      
			      public void m() {
			      }
			    }
			    
			    __C_1 ___C_1 = new __C_1();
			    return CollectionLiterals.<__C_1>newArrayList(___C_1);
			  }
			}
		''')
	}
	
	@Test
	def void testNestedLocalClass_04() {
		'''
			class C {
				def m() {
					return newArrayList(
						new Runnable() { override run() {} def void m() {} },
						new Runnable() { override run() {} def void m() {} }
					)
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public ArrayList<Runnable> m() {
			    @SuppressWarnings("all")
			    final class __C_1 implements Runnable {
			      public void run() {
			      }
			      
			      public void m() {
			      }
			    }
			    
			    __C_1 ___C_1 = new __C_1();
			    @SuppressWarnings("all")
			    final class __C_2 implements Runnable {
			      public void run() {
			      }
			      
			      public void m() {
			      }
			    }
			    
			    __C_2 ___C_2 = new __C_2();
			    return CollectionLiterals.<Runnable>newArrayList(___C_1, ___C_2);
			  }
			}
		''')
	}
	
	@Test
	def void testNestedLocalClass_05() {
		'''
			class C {
				def m() {
					return newArrayList(newArrayList(new Runnable() { override run() {} def void m() {} }))
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public ArrayList<? extends ArrayList<? extends Runnable>> m() {
			    @SuppressWarnings("all")
			    final class __C_1 implements Runnable {
			      public void run() {
			      }
			      
			      public void m() {
			      }
			    }
			    
			    __C_1 ___C_1 = new __C_1();
			    ArrayList<__C_1> _newArrayList = CollectionLiterals.<__C_1>newArrayList(___C_1);
			    return CollectionLiterals.<ArrayList<__C_1>>newArrayList(_newArrayList);
			  }
			}
		''')
	}
	
	@Test
	def void testCapturedLocalVar() {
		'''
			class Foo {
				def foo() {
					val x = ''
					val bar = new Runnable() {
						override run() { x.toString }
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    final String x = "";
			    final Runnable bar = new Runnable() {
			      public void run() {
			        x.toString();
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testLocalVar() {
		'''
			class Foo {
				def foo() {
					val bar = new Runnable() {
						override run() {}
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    final Runnable bar = new Runnable() {
			      public void run() {
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testNestedTypeScoping_01() {
		'''
			class C {
				def newMap() {
					return new java.util.AbstractMap<String, String>() {
						override java.util.Set<Entry<String, String>> entrySet() {
							newHashSet
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.AbstractMap;
			import java.util.Map;
			import java.util.Set;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public AbstractMap<String, String> newMap() {
			    return new AbstractMap<String, String>() {
			      public Set<Map.Entry<String, String>> entrySet() {
			        return CollectionLiterals.<Map.Entry<String, String>>newHashSet();
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testNestedTypeScoping_02() {
		'''
			class C {
				def newMap() {
					return new java.util.AbstractMap<String, String>() {
						override entrySet() {
							<Entry<String, String>>newHashSet
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.AbstractMap;
			import java.util.Map;
			import java.util.Set;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class C {
			  public AbstractMap<String, String> newMap() {
			    return new AbstractMap<String, String>() {
			      public Set<Map.Entry<String, String>> entrySet() {
			        return CollectionLiterals.<Map.Entry<String, String>>newHashSet();
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testNestedTypeScoping_03() {
		'''
			class C {
				def newMap() {
					return new java.util.AbstractMap<String, String>() {
						override entrySet() {
							Entry.declaredMethods // doesn't make much sense
							#{}
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import com.google.common.collect.Sets;
			import java.util.AbstractMap;
			import java.util.Collections;
			import java.util.Map;
			import java.util.Set;
			
			@SuppressWarnings("all")
			public class C {
			  public AbstractMap<String, String> newMap() {
			    return new AbstractMap<String, String>() {
			      public Set<Map.Entry<String, String>> entrySet() {
			        Set<Map.Entry<String, String>> _xblockexpression = null;
			        {
			          Map.Entry.class.getDeclaredMethods();
			          _xblockexpression = Collections.<Map.Entry<String, String>>unmodifiableSet(Sets.<Map.Entry<String, String>>newHashSet());
			        }
			        return _xblockexpression;
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testThisScoping_01() {
		'''
			class C {
				def newD() {
					return new D {
						def m() {
							return this
						}
					}
				}
			}
			class D {}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public D newD() {
			    @SuppressWarnings("all")
			    final class __C_1 extends D {
			      public __C_1 m() {
			        return this;
			      }
			    }
			    
			    return new __C_1();
			  }
			}
		''')
	}
	
	@Test
	def void testThisScoping_02() {
		'''
			class C {
				def newD() {
					return new D {
						def m() {
							return toString
						}
					}
				}
			}
			class D {}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public D newD() {
			    @SuppressWarnings("all")
			    final class __C_1 extends D {
			      public String m() {
			        return this.toString();
			      }
			    }
			    
			    return new __C_1();
			  }
			}
		''')
	}
	
	@Test
	def void testThisScoping_03() {
		'''
			class C {
				def newD() {
					return new D {
						def m() {
							m2
						}
					}
				}
				def void m2() {}
			}
			class D {}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public D newD() {
			    @SuppressWarnings("all")
			    final class __C_1 extends D {
			      public void m() {
			        C.this.m2();
			      }
			    }
			    
			    return new __C_1();
			  }
			  
			  public void m2() {
			  }
			}
		''')
	}
	
	@Test
	def void testThisScoping_04() {
		'''
			class C {
				def newD() {
					return new D {
						def m() {
							C.this.m
						}
					}
				}
				def void m() {}
			}
			class D {}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public D newD() {
			    @SuppressWarnings("all")
			    final class __C_1 extends D {
			      public void m() {
			        C.this.m();
			      }
			    }
			    
			    return new __C_1();
			  }
			  
			  public void m() {
			  }
			}
		''')
	}
	
	@Test
	def void testSuperScoping_01() {
		'''
			class C extends B {
				def myMethod() {
					return new D {
						override m() {
							super.m
						}
					}
				}
				override m() {}
			}
			class B { def void m() {} }
			class D extends E {}
			class E { def void m() {} }
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C extends B {
			  public D myMethod() {
			    return new D() {
			      public void m() {
			        super.m();
			      }
			    };
			  }
			  
			  public void m() {
			  }
			}
		''')
	}
	
	@Test
	def void testSuperScoping_02() {
		'''
			class C extends B {
				def myMethod() {
					return new D {
						override m() {
							C.super.m
						}
					}
				}
				override m() {}
			}
			class B { def void m() {} }
			class D extends E {}
			class E { def void m() {} }
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C extends B {
			  public D myMethod() {
			    return new D() {
			      public void m() {
			        C.super.m();
			      }
			    };
			  }
			  
			  public void m() {
			  }
			}
		''')
	}
	
	@Test
	def void testLocalVar_AdditionalMember() {
		'''
			class Foo {
				def foo() {
					val bar = new Runnable() {
						override run() {}
						int baz
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    @SuppressWarnings("all")
			    final class __Foo_1 implements Runnable {
			      public void run() {
			      }
			      
			      private int baz;
			    }
			    
			    final __Foo_1 bar = new __Foo_1();
			  }
			}
		''')
	}
	
	@Test
	def void testLocalVar_AdditionalMember_Return() {
		'''
			class Foo {
				def foo() {
					new Runnable() {
						override run() {}
						int baz
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  public Runnable foo() {
			    @SuppressWarnings("all")
			    final class __Foo_1 implements Runnable {
			      public void run() {
			      }
			      
			      private int baz;
			    }
			    
			    return new __Foo_1();
			  }
			}
		''')
	}
	
	@Test
	def void testLocalVarInArray_01() {
		'''
			class Foo {
				def foo() {
					val Object[] bar = #[ 
						new Runnable() {
							override run() {}
						}
					]
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    final Object[] bar = new Object[] { new Runnable() {
			      public void run() {
			      }
			    } };
			  }
			}
		''')
	}
	
	@Test
	def void testLocalVarInArray_02() {
		'''
			class Foo {
				def foo() {
					val bar = #[ 
						new Runnable() {
							override run() {}
						}
					]
				}
			}
		'''.assertCompilesTo('''
			import com.google.common.collect.Lists;
			import java.util.Collections;
			import java.util.List;
			
			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    final List<Runnable> bar = Collections.<Runnable>unmodifiableList(Lists.<Runnable>newArrayList(new Runnable() {
			      public void run() {
			      }
			    }));
			  }
			}
		''')
	}
	
	@Test
	def void testField() {
		'''
			class Foo {
				val bar = new Runnable() {
					override run() {}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo {
			  private final Runnable bar = new Runnable() {
			    public void run() {
			    }
			  };
			}
		''')
	}
	
	@Test
	def void testField_AdditionalMember_01() {
		'''
			class Foo {
				val bar = new Runnable() {
					int baz
					override run() {}
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function0;

			@SuppressWarnings("all")
			public class Foo {
			  private final Runnable bar = new Function0<Runnable>() {
			    public Runnable apply() {
			      @SuppressWarnings("all")
			      final class __Foo_1 implements Runnable {
			        private int baz;
			        
			        public void run() {
			        }
			      }
			      
			      __Foo_1 ___Foo_1 = new __Foo_1();
			      return ___Foo_1;
			    }
			  }.apply();
			}
		''')
	}
	
	@Test
	def void testField_AdditionalMember_02() {
		'''
			class C {
				val secondOuterField = 1
				val outerField = new Object() {
					int localField
				} => [ localField = secondOuterField ]
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public class C {
			  private final int secondOuterField = 1;
			  
			  private final Object outerField = new Function0<Object>() {
			    public Object apply() {
			      @SuppressWarnings("all")
			      final class __C_1 {
			        private int localField;
			      }
			      
			      __C_1 ___C_1 = new __C_1();
			      final Procedure1<__C_1> _function = new Procedure1<__C_1>() {
			        public void apply(final __C_1 it) {
			          it.localField = C.this.secondOuterField;
			        }
			      };
			      __C_1 _doubleArrow = ObjectExtensions.<__C_1>operator_doubleArrow(___C_1, _function);
			      return _doubleArrow;
			    }
			  }.apply();
			}
		''')
	}
	
	@Test
	def void testAdditionalMemberAccess_01() {
		'''
			class C {
				def m() {
					new Object() {
						public int f
					} => [ f = 1 ]
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public class C {
			  public Object m() {
			    @SuppressWarnings("all")
			    final class __C_1 {
			      public int f;
			    }
			    
			    __C_1 ___C_1 = new __C_1();
			    final Procedure1<__C_1> _function = new Procedure1<__C_1>() {
			      public void apply(final __C_1 it) {
			        it.f = 1;
			      }
			    };
			    return ObjectExtensions.<__C_1>operator_doubleArrow(___C_1, _function);
			  }
			}
		''')
	}

	@Test
	def void testGeneric() {
		'''
			class Foo {
				def foo() {
					val bar = new Iterable<String>() {
						override iterator() {
							null
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.Iterator;

			@SuppressWarnings("all")
			public class Foo {
			  public void foo() {
			    final Iterable<String> bar = new Iterable<String>() {
			      public Iterator<String> iterator() {
			        return null;
			      }
			    };
			  }
			}
		''')
	}

	@Test
	def void testGeneric1() {
		'''
			class Foo  {
				def <T> foo() {
					val bar = new Iterable<T>() {
						override iterator() {
							null
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.Iterator;

			@SuppressWarnings("all")
			public class Foo {
			  public <T extends Object> void foo() {
			    final Iterable<T> bar = new Iterable<T>() {
			      public Iterator<T> iterator() {
			        return null;
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testGeneric2() {
		'''
			import java.util.Iterator
			
			class Foo  {
				def <T> foo() {
					val bar = new Iterable<T>() {
						override Iterator<T> iterator() {
							null
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.Iterator;

			@SuppressWarnings("all")
			public class Foo {
			  public <T extends Object> void foo() {
			    final Iterable<T> bar = new Iterable<T>() {
			      public Iterator<T> iterator() {
			        return null;
			      }
			    };
			  }
			}
		''')
	}

	@Test
	def void testGeneric3() {
		'''
			class Foo<T>  {
				def <U> foo() {
					val bar = new Bar<T,U>() {
						override T bar(U it) {
							null
						}
					}
				}
			}

			interface Bar<V,W> {
				def V bar(W it)
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public <U extends Object> void foo() {
			    final Bar<T, U> bar = new Bar<T, U>() {
			      public T bar(final U it) {
			        return null;
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testGeneric4() {
		'''
			class Foo<T>  {
				def foo() {
					val bar = new Bar<T>() {
						override <U> U bar(T it) {
							null
						}
					}
				}
			}

			interface Bar<V> {
				def <W> W bar(V it)
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public void foo() {
			    final Bar<T> bar = new Bar<T>() {
			      public <U extends Object> U bar(final T it) {
			        return null;
			      }
			    };
			  }
			}
		''')
	}
	
	@Test
	def void testGeneric5() {
		'''
			class C {
				def m() {
					new Object {
						def <T> T m2() {}
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public Object m() {
			    @SuppressWarnings("all")
			    final class __C_1 {
			      public <T extends Object> T m2() {
			        return null;
			      }
			    }
			    
			    return new __C_1();
			  }
			}
		''')
	}
	
	@Test
	def void testGeneric6() {
		'''
			class C {
				def <K> m() {
					new Object {
						def <V> m2() {
							new java.util.AbstractMap<K, V> {
								def Entry<K, V> m() {}
								override entrySet() {}
							}
						}
					}
				}
			}
		'''.assertCompilesTo('''
			import java.util.AbstractMap;
			import java.util.Map;
			import java.util.Set;
			
			@SuppressWarnings("all")
			public class C {
			  public <K extends Object> Object m() {
			    @SuppressWarnings("all")
			    final class __C_1 {
			      public <V extends Object> AbstractMap<K, V> m2() {
			        @SuppressWarnings("all")
			        final class ____C_1 extends AbstractMap<K, V> {
			          public Map.Entry<K, V> m() {
			            return null;
			          }
			          
			          public Set<Map.Entry<K, V>> entrySet() {
			            return null;
			          }
			        }
			        
			        return new ____C_1();
			      }
			    }
			    
			    return new __C_1();
			  }
			}
		''')
	}
	
	@Test
	def void testGeneric7() {
		'''
			class Foo<T>  {
				def <W extends Foo<W>> foo() {
					val bar = new Bar<W>() {
						override bar() {
							null
						}
					}
				}
				interface Bar<V extends Foo<V>> {
					def V bar()
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  @SuppressWarnings("all")
			  public interface Bar<V extends Foo<V>> {
			    public abstract V bar();
			  }
			  
			  public <W extends Foo<W>> void foo() {
			    final Foo.Bar<W> bar = new Foo.Bar<W>() {
			      public W bar() {
			        return null;
			      }
			    };
			  }
			}
		''')
	}
}
