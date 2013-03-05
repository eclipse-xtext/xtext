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
class CompilerBugTest extends AbstractXtendCompilerTest {
	
	@Test
	def testBugReturnInLoop_01() {
		assertCompilesTo('''
			class C {
				def String m(String s) {
					while(true) {
						try {
							return "string"
						} catch(Exception e) {
							return "string"
						}
					}
			    }
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final String s) {
			    boolean _while = true;
			    while (_while) {
			      try {
			        return "string";
			      } catch (final Throwable _t) {
			        if (_t instanceof Exception) {
			          final Exception e = (Exception)_t;
			          return "string";
			        } else {
			          throw Exceptions.sneakyThrow(_t);
			        }
			      }
			      
			    }
			    return null;
			  }
			}
		''')
	}
	
	@Test
	def testBugReturnInLoop_02() {
		assertCompilesTo('''
			class C {
				def String m(String s) {
					while(true) {
						if(true)
							return "string"
					}
			    }
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public String m(final String s) {
			    boolean _while = true;
			    while (_while) {
			      if (true) {
			        return "string";
			      }
			      _while = true;
			    }
			    return null;
			  }
			}
		''')
	}
	
	@Test
	def testBug386110() {
		assertCompilesTo('''
			import com.google.common.util.concurrent.ListenableFuture
			import com.google.common.util.concurrent.MoreExecutors
			
			class Foo<O> {
				val () => O operation = null
			
				def ListenableFuture<O> run() {
					val result = MoreExecutors::sameThreadExecutor.submit(operation)
					operation.apply
					return result
				}
			}
		''', '''
			import com.google.common.util.concurrent.ListenableFuture;
			import com.google.common.util.concurrent.ListeningExecutorService;
			import com.google.common.util.concurrent.MoreExecutors;
			import java.util.concurrent.Callable;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			
			@SuppressWarnings("all")
			public class Foo<O extends Object> {
			  private final Function0<? extends O> operation = null;
			  
			  public ListenableFuture<O> run() {
			    ListeningExecutorService _sameThreadExecutor = MoreExecutors.sameThreadExecutor();
			    final ListenableFuture<O> result = _sameThreadExecutor.<O>submit(new Callable<O>() {
			        public O call() {
			          return Foo.this.operation.apply();
			        }
			    });
			    this.operation.apply();
			    return result;
			  }
			}
		''')
	}
	
	@Test
	def testBug383551_01() {
		assertCompilesTo('''
			abstract class Option<T> {
			  def <X> Option<X> map((T)=>X f) { switch this {
			    Some<T> : new Some<X>
			    None<T> : new None<X>
			  }}
			}
			class Some<T> extends Option<T> {}
			class None<T> extends Option<T> {}
		''', '''
			import org.eclipse.xtext.xbase.lib.Functions.Function1;

			@SuppressWarnings("all")
			public abstract class Option<T extends Object> {
			  public <X extends Object> Option<X> map(final Function1<? super T,? extends X> f) {
			    Option<X> _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (this instanceof Some) {
			        final Some<T> _some = (Some<T>)this;
			        _matched=true;
			        Some<X> _some_1 = new Some<X>();
			        _switchResult = _some_1;
			      }
			    }
			    if (!_matched) {
			      if (this instanceof None) {
			        final None<T> _none = (None<T>)this;
			        _matched=true;
			        None<X> _none_1 = new None<X>();
			        _switchResult = _none_1;
			      }
			    }
			    return _switchResult;
			  }
			}
		''')
	}
	
	@Test
	def testBug383551_02() {
		assertCompilesTo('''
			abstract class C<C_T> {
			  def <X> C<X> m() { switch this {
			    E<C_T> : new E<X>
			  }}
			}
			class D<D_T> extends C<D_T> {}
			class E<E_T> extends D<E_T> {}
		''', '''
			@SuppressWarnings("all")
			public abstract class C<C_T extends Object> {
			  public <X extends Object> C<X> m() {
			    E<X> _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (this instanceof E) {
			        final E<C_T> _e = (E<C_T>)this;
			        _matched=true;
			        E<X> _e_1 = new E<X>();
			        _switchResult = _e_1;
			      }
			    }
			    return _switchResult;
			  }
			}
		''')
	}
	
	@Test def void testBug362285_01() {
		'''
			public class C<T> {
				def m() {
					[T x|x].apply(null)
				}	
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function1;

			@SuppressWarnings("all")
			public class C<T extends Object> {
			  public T m() {
			    final Function1<T,T> _function = new Function1<T,T>() {
			        public T apply(final T x) {
			          return x;
			        }
			      };
			    T _apply = _function.apply(null);
			    return _apply;
			  }
			}
		''')
	}
	
	@Test def void testBug362285_02() {
		'''
			public class C {
				def <T> m() {
					[T x|x].apply(null)
				}	
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class C {
			  public <T extends Object> T m() {
			    final Function1<T,T> _function = new Function1<T,T>() {
			        public T apply(final T x) {
			          return x;
			        }
			      };
			    T _apply = _function.apply(null);
			    return _apply;
			  }
			}
		''')
	}
	
	@Test def void testBug380525_01() {
		'''
			abstract class L<E> extends java.util.AbstractList<E> {
				protected new() {
					new Object() => [add(it as E) && remove]
				}
			}
		'''.assertCompilesTo('''
			import java.util.AbstractList;
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public abstract class L<E extends Object> extends AbstractList<E> {
			  protected L() {
			    Object _object = new Object();
			    final Procedure1<Object> _function = new Procedure1<Object>() {
			        public void apply(final Object it) {
			          boolean _and = false;
			          boolean _add = L.this.add(((E) it));
			          if (!_add) {
			            _and = false;
			          } else {
			            boolean _remove = L.this.remove(it);
			            _and = (_add && _remove);
			          }
			        }
			      };
			    ObjectExtensions.<Object>operator_doubleArrow(_object, _function);
			  }
			}
		''')
	}
	
	@Test def void testBug380525_02() {
		'''
			abstract class L<E> extends java.util.AbstractList<E> {
				protected new(E e) {
					e => [add && remove]
				}
			}
		'''.assertCompilesTo('''
			import java.util.AbstractList;
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public abstract class L<E extends Object> extends AbstractList<E> {
			  protected L(final E e) {
			    final Procedure1<E> _function = new Procedure1<E>() {
			        public void apply(final E it) {
			          boolean _and = false;
			          boolean _add = L.this.add(it);
			          if (!_add) {
			            _and = false;
			          } else {
			            boolean _remove = L.this.remove(it);
			            _and = (_add && _remove);
			          }
			        }
			      };
			    ObjectExtensions.<E>operator_doubleArrow(e, _function);
			  }
			}
		''')
	}
	
	@Test def void testBug400433_01() {
		'''
			class Test<T> {
				new(()=>T arg) {}
				static def <P> newInstance(()=>P arg) { new Test(arg) }
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			
			@SuppressWarnings("all")
			public class Test<T extends Object> {
			  public Test(final Function0<? extends T> arg) {
			  }
			  
			  public static <P extends Object> Test<P> newInstance(final Function0<? extends P> arg) {
			    Test<P> _test = new Test<P>(arg);
			    return _test;
			  }
			}
		''')
	}
	
	@Test
	def testBug380060() { 
		assertCompilesTo('''
			class Foo<T> {
			    def static void main(String[] args) {
			        new Foo()
			    }
			}
		''', '''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public static void main(final String[] args) {
			    new Foo<Object>();
			  }
			}
		''')
	}
	
	@Test
	def testBug372864() { 
		assertCompilesTo('''
			class Foo {
				def protected String testSwitch(Object e) {
				    switch e {
				       String : return ""
				       default: null
				    }
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class Foo {
			  protected String testSwitch(final Object e) {
			    String _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (e instanceof String) {
			        final String _string = (String)e;
			        _matched=true;
			        return "";
			      }
			    }
			    if (!_matched) {
			      _switchResult = null;
			    }
			    return _switchResult;
			  }
			}
		''')
	}
	
	@Test
	def testBug373482() {
		assertCompilesTo(
			'''
				class X {
					def method() �"'''�"�logSomething�"�'''"�
					def void logSomething() {println("zonk")}
				}
			''', '''
				import org.eclipse.xtend2.lib.StringConcatenation;
				import org.eclipse.xtext.xbase.lib.InputOutput;
				
				@SuppressWarnings("all")
				public class X {
				  public CharSequence method() {
				    StringConcatenation _builder = new StringConcatenation();
				    this.logSomething();
				    return _builder;
				  }
				  
				  public void logSomething() {
				    InputOutput.<String>println("zonk");
				  }
				}
			''')
	}
	
	@Test def void testBug380058_01() {
		'''
			import bug380058.Amount
			
			import static bug380058.SI.*
			
			class JScienceTest {
			    def test() { 
			        val w = Amount::valueOf(100, MILLIMETER)
			        val h = Amount::valueOf(50, MILLIMETER)
			        val perim = w.plus(h).times(2)
			        println(perim)
			    }
			}
		'''.assertCompilesTo('''
			import bug380058.Amount;
			import bug380058.Length;
			import bug380058.SI;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class JScienceTest {
			  public Amount<Length> test() {
			    Amount<Length> _xblockexpression = null;
			    {
			      final Amount<Length> w = Amount.<Length>valueOf(100, SI.MILLIMETER);
			      final Amount<Length> h = Amount.<Length>valueOf(50, SI.MILLIMETER);
			      Amount<Length> _plus = w.plus(h);
			      final Amount<Length> perim = _plus.times(2);
			      Amount<Length> _println = InputOutput.<Amount<Length>>println(perim);
			      _xblockexpression = (_println);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testBug380059_01() {
		'''
			class C<T> {
				def m() {
					val C<?> c = this
					c
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C<T extends Object> {
			  public C<? extends Object> m() {
			    C<? extends Object> _xblockexpression = null;
			    {
			      final C<?> c = this;
			      _xblockexpression = (c);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=380062
	 */
	@Test def testBug380062_01() {
		assertCompilesTo('''
			class Foo<T> {
			    def foo(Foo ^new) {
			        foo(^new)
			    }
			}
		''','''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public Object foo(final Foo new_) {
			    Object _foo = this.foo(new_);
			    return _foo;
			  }
			}
		''')
	}
	
	@Test def testBug380062_01_b() {
		assertCompilesTo('''
			class Foo<T> {
			    def Object foo(Foo ^new) {
			        foo(^new)
			    }
			}
		''','''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public Object foo(final Foo new_) {
			    Object _foo = this.foo(new_);
			    return _foo;
			  }
			}
		''')
	}
	
	@Test def testBug380062_02() {
		assertCompilesTo('''
			class Foo<T> {
				val String s
			    new(String ^new) {
			        s = ^new
			    }
			}
		''','''
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  private final String s;
			  
			  public Foo(final String new_) {
			    this.s = new_;
			  }
			}
		''')
	}
	
	@Test def testBug381201_01() {
		'''
			@Data class Foo {
				static String staticField
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtend.lib.Data;
			import org.eclipse.xtext.xbase.lib.util.ToStringHelper;
			
			@Data
			@SuppressWarnings("all")
			public class Foo {
			  private final static String staticField;
			  
			  public Foo() {
			    super();
			  }
			  
			  @Override
			  public int hashCode() {
			    final int prime = 31;
			    int result = 1;
			    return result;
			  }
			  
			  @Override
			  public boolean equals(final Object obj) {
			    if (this == obj)
			      return true;
			    if (obj == null)
			      return false;
			    if (getClass() != obj.getClass())
			      return false;
			    Foo other = (Foo) obj;
			    return true;
			  }
			  
			  @Override
			  public String toString() {
			    String result = new ToStringHelper().toString(this);
			    return result;
			  }
			}
		''')
	}
	
	@Test def testBug381201_02() {
		'''
			@Data class Foo {
				static String staticField
				String nonStaticField
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtend.lib.Data;
			import org.eclipse.xtext.xbase.lib.util.ToStringHelper;
			
			@Data
			@SuppressWarnings("all")
			public class Foo {
			  private final static String staticField;
			  
			  private final String _nonStaticField;
			  
			  public String getNonStaticField() {
			    return this._nonStaticField;
			  }
			  
			  public Foo(final String nonStaticField) {
			    super();
			    this._nonStaticField = nonStaticField;
			  }
			  
			  @Override
			  public int hashCode() {
			    final int prime = 31;
			    int result = 1;
			    result = prime * result + ((_nonStaticField== null) ? 0 : _nonStaticField.hashCode());
			    return result;
			  }
			  
			  @Override
			  public boolean equals(final Object obj) {
			    if (this == obj)
			      return true;
			    if (obj == null)
			      return false;
			    if (getClass() != obj.getClass())
			      return false;
			    Foo other = (Foo) obj;
			    if (_nonStaticField == null) {
			      if (other._nonStaticField != null)
			        return false;
			    } else if (!_nonStaticField.equals(other._nonStaticField))
			      return false;
			    return true;
			  }
			  
			  @Override
			  public String toString() {
			    String result = new ToStringHelper().toString(this);
			    return result;
			  }
			}
		''')
	}
	
	@Test def testBug383568() {
		'''
			@Data class UsesExtension {
			  extension String
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtend.lib.Data;
			import org.eclipse.xtext.xbase.lib.Extension;
			import org.eclipse.xtext.xbase.lib.util.ToStringHelper;
			
			@Data
			@SuppressWarnings("all")
			public class UsesExtension {
			  @Extension
			  private final String __string;
			  
			  public String get_string() {
			    return this.__string;
			  }
			  
			  public UsesExtension(final String _string) {
			    super();
			    this.__string = _string;
			  }
			  
			  @Override
			  public int hashCode() {
			    final int prime = 31;
			    int result = 1;
			    result = prime * result + ((__string== null) ? 0 : __string.hashCode());
			    return result;
			  }
			  
			  @Override
			  public boolean equals(final Object obj) {
			    if (this == obj)
			      return true;
			    if (obj == null)
			      return false;
			    if (getClass() != obj.getClass())
			      return false;
			    UsesExtension other = (UsesExtension) obj;
			    if (__string == null) {
			      if (other.__string != null)
			        return false;
			    } else if (!__string.equals(other.__string))
			      return false;
			    return true;
			  }
			  
			  @Override
			  public String toString() {
			    String result = new ToStringHelper().toString(this);
			    return result;
			  }
			}
		''')
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=391077
	 */
	@Test def void testBug391077() {
		assertCompilesTo('''
			class TestError {
			    def Void voidObjectReturned() {
			    }
			    
			    def void sampleMethod() {
			        try {
			            voidObjectReturned
			        } catch (Exception e) {
			            Integer::parseInt('1')
			        }
			    }
			}
		''','''
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class TestError {
			  public Void voidObjectReturned() {
			    return null;
			  }
			  
			  public void sampleMethod() {
			    try {
			      this.voidObjectReturned();
			    } catch (final Throwable _t) {
			      if (_t instanceof Exception) {
			        final Exception e = (Exception)_t;
			        Integer.parseInt("1");
			      } else {
			        throw Exceptions.sneakyThrow(_t);
			      }
			    }
			  }
			}
		''')
	}
	
	@Test def testBug399527() throws Exception {
		'''
			class Y {
			 static def <T> IExpectationSetters<T> expect(T value) {
			 }
			 HeaderAccess<?> unboundedMockHeaderAccess
			 def test() {
			   val Object header = unboundedMockHeaderAccess.header
			   val IExpectationSetters<Object> exp1 = expect(header)
			   val IExpectationSetters<Object> exp2 = expect(unboundedMockHeaderAccess.getHeader())
			 }
			}
			interface HeaderAccess<T> {
			   def T getHeader();
			}
			interface IExpectationSetters<T> {}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class Y {
			  public static <T extends Object> IExpectationSetters<T> expect(final T value) {
			    return null;
			  }
			  
			  private HeaderAccess<? extends Object> unboundedMockHeaderAccess;
			  
			  public void test() {
			    final Object header = this.unboundedMockHeaderAccess.getHeader();
			    final IExpectationSetters<Object> exp1 = Y.<Object>expect(header);
			    Object _header = this.unboundedMockHeaderAccess.getHeader();
			    final IExpectationSetters<Object> exp2 = Y.<Object>expect(_header);
			  }
			}
		''')
	}
	
	@Test def testBug399975_01() throws Exception {
		'''
			class A {
			 def void m(Class<?>[] c) {}
			}
			class B extends A{
			 override m(Class<? extends Object>[] c) {}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class A {
			  public void m(final Class<? extends Object>[] c) {
			  }
			}
		''')
	}
	
	@Test def testBug399975_02() throws Exception {
		'''
			class B extends A{
			 override m(Class<? extends Object>[] c) {}
			}
			class A {
			 def void m(Class<?>[] c) {}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class B extends A {
			  public void m(final Class<? extends Object>[] c) {
			  }
			}
		''')
	}
	
	@Test def testBug399975_03() throws Exception {
		'''
			class A {
			 def void m(Class<?>... c) {}
			}
			class B extends A{
			 override m(Class<? extends Object>... c) {}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class A {
			  public void m(final Class<? extends Object>... c) {
			  }
			}
		''')
	}
	
	@Test def testBug399975_04() throws Exception {
		'''
			class B extends A{
			 override m(Class<? extends Object>... c) {}
			}
			class A {
			 def void m(Class<?>... c) {}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class B extends A {
			  public void m(final Class<? extends Object>... c) {
			  }
			}
		''')
	}
	
	@Test def void testBug400347_01() {
		'''
			import java.util.LinkedList
			
			interface XType {}
			
			class XItem<T extends XType> {
				XList<T> gen
				T item
				
				new(XList<T> gen, T item) {
					this.gen = gen
					this.item = item
				}
			}
			
			class XList<T extends XType> {
				val items = new LinkedList<XItem<T>>
				
				def add(T item) {
					val result = new XItem<T>(this, item)
					items.add(result)
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public interface XType {
			}
		''')
	}
	
	@Test def void testBug400347_02() {
		'''
			import java.util.LinkedList
			
			class XItem<T extends XType> {
				XList<T> gen
				T item
				
				new(XList<T> gen, T item) {
					this.gen = gen
					this.item = item
				}
			}
			
			interface XType {}
			
			class XList<T extends XType> {
				val items = new LinkedList<XItem<T>>
				
				def add(T item) {
					val result = new XItem<T>(this, item)
					items.add(result)
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class XItem<T extends XType> {
			  private XList<T> gen;
			  
			  private T item;
			  
			  public XItem(final XList<T> gen, final T item) {
			    this.gen = gen;
			    this.item = item;
			  }
			}
		''')
	}
	
	@Test def void testBug400347_03() {
		'''
			import java.util.LinkedList
			
			class XList<T extends XType> {
				val items = new LinkedList<XItem<T>>
				
				def add(T item) {
					val result = new XItem<T>(this, item)
					items.add(result)
				}
			}
			
			interface XType {}
			
			class XItem<T extends XType> {
				XList<T> gen
				T item
				
				new(XList<T> gen, T item) {
					this.gen = gen
					this.item = item
				}
			}
		'''.assertCompilesTo('''
			import java.util.LinkedList;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			
			@SuppressWarnings("all")
			public class XList<T extends XType> {
			  private final LinkedList<XItem<T>> items = new Function0<LinkedList<XItem<T>>>() {
			    public LinkedList<XItem<T>> apply() {
			      LinkedList<XItem<T>> _linkedList = new LinkedList<XItem<T>>();
			      return _linkedList;
			    }
			  }.apply();
			  
			  public boolean add(final T item) {
			    boolean _xblockexpression = false;
			    {
			      XItem<T> _xItem = new XItem<T>(this, item);
			      final XItem<T> result = _xItem;
			      boolean _add = this.items.add(result);
			      _xblockexpression = (_add);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}

	@Test def void testBug400823_01() {
		'''
			package test.plugin
			import static extension test.plugin.Foo.*
			class Foo {
				def foo() {
					this - this
				}
				def static operator_minus(Foo x, Foo y) {
					1
				}
			}
		'''.assertCompilesTo('''
			package test.plugin;
			
			@SuppressWarnings("all")
			public class Foo {
			  public int foo() {
			    int _minus = Foo.operator_minus(this, this);
			    return _minus;
			  }
			  
			  public static int operator_minus(final Foo x, final Foo y) {
			    return 1;
			  }
			}
		''')
	}

	@Test def void testBug401269_01() {
		'''
			import java.util.Map
			import java.util.List
			import java.util.ArrayList
			class C {
				def <A> Map<A, Iterable<A>> then(Iterable<A> iter) {
					null
				}	
				def <A extends Number & Comparable<?>> Map<A, List<A>> then(List<A> expr) {
					null
				}
				def client(ArrayList<Long> arrayList) {
					val plainInvocation = then(arrayList)
					plainInvocation.toString
					val extensionInvocation = arrayList.then
					extensionInvocation.toString
				}
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import java.util.List;
			import java.util.Map;
			
			@SuppressWarnings("all")
			public class C {
			  public <A extends Object> Map<A,Iterable<A>> then(final Iterable<A> iter) {
			    return null;
			  }
			  
			  public <A extends Number & Comparable<?>> Map<A,List<A>> then(final List<A> expr) {
			    return null;
			  }
			  
			  public String client(final ArrayList<Long> arrayList) {
			    String _xblockexpression = null;
			    {
			      final Map<Long,List<Long>> plainInvocation = this.<Long>then(arrayList);
			      plainInvocation.toString();
			      final Map<Long,List<Long>> extensionInvocation = this.<Long>then(arrayList);
			      String _string = extensionInvocation.toString();
			      _xblockexpression = (_string);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testBug401269_02() {
		'''
			import java.util.Map
			import java.util.List
			class C {
				def <A> Map<A, Iterable<A>> then(Iterable<A> iter) {
					null
				}	
				def <A extends Number & Comparable<?>> Map<A, List<A>> then(List<A> expr) {
					null
				}
				def client(Iterable<Long> iterable) {
					val plainInvocation = then(iterable)
					plainInvocation.toString
					val extensionInvocation = iterable.then
					extensionInvocation.toString
				}
			}
		'''.assertCompilesTo('''
			import java.util.List;
			import java.util.Map;
			
			@SuppressWarnings("all")
			public class C {
			  public <A extends Object> Map<A,Iterable<A>> then(final Iterable<A> iter) {
			    return null;
			  }
			  
			  public <A extends Number & Comparable<?>> Map<A,List<A>> then(final List<A> expr) {
			    return null;
			  }
			  
			  public String client(final Iterable<Long> iterable) {
			    String _xblockexpression = null;
			    {
			      final Map<Long,Iterable<Long>> plainInvocation = this.<Long>then(iterable);
			      plainInvocation.toString();
			      final Map<Long,Iterable<Long>> extensionInvocation = this.<Long>then(iterable);
			      String _string = extensionInvocation.toString();
			      _xblockexpression = (_string);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testBug401269_03() {
		'''
			import java.util.Map
			import java.util.List
			import java.util.LinkedList
			class C {
				def <A> Map<A, Iterable<A>> then(Iterable<A> iter) {
					null
				}	
				def <A extends Number & Comparable<?>> Map<A, List<A>> then(List<A> expr) {
					null
				}
				def client(LinkedList<String> linkedList) {
					val plainInvocation = then(linkedList)
					plainInvocation.toString
					val extensionInvocation = linkedList.then
					extensionInvocation.toString
				}
			}
		'''.assertCompilesTo('''
			import java.util.LinkedList;
			import java.util.List;
			import java.util.Map;
			
			@SuppressWarnings("all")
			public class C {
			  public <A extends Object> Map<A,Iterable<A>> then(final Iterable<A> iter) {
			    return null;
			  }
			  
			  public <A extends Number & Comparable<?>> Map<A,List<A>> then(final List<A> expr) {
			    return null;
			  }
			  
			  public String client(final LinkedList<String> linkedList) {
			    String _xblockexpression = null;
			    {
			      final Map<String,Iterable<String>> plainInvocation = this.<String>then(linkedList);
			      plainInvocation.toString();
			      final Map<String,Iterable<String>> extensionInvocation = this.<String>then(linkedList);
			      String _string = extensionInvocation.toString();
			      _xblockexpression = (_string);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testBug401269_04() {
		'''
			import java.util.Map
			import java.util.List
			import java.util.Set
			class C {
				def <A> Map<A, Iterable<A>> then(Iterable<A> iter) {
					null
				}	
				def <A extends Number & Comparable<?>> Map<A, List<A>> then(List<A> expr) {
					null
				}
				def client(Set<Integer> set) {
					val plainInvocation = then(set)
					plainInvocation.toString
					val extensionInvocation = set.then
					extensionInvocation.toString
				}
			}
		'''.assertCompilesTo('''
			import java.util.List;
			import java.util.Map;
			import java.util.Set;
			
			@SuppressWarnings("all")
			public class C {
			  public <A extends Object> Map<A,Iterable<A>> then(final Iterable<A> iter) {
			    return null;
			  }
			  
			  public <A extends Number & Comparable<?>> Map<A,List<A>> then(final List<A> expr) {
			    return null;
			  }
			  
			  public String client(final Set<Integer> set) {
			    String _xblockexpression = null;
			    {
			      final Map<Integer,Iterable<Integer>> plainInvocation = this.<Integer>then(set);
			      plainInvocation.toString();
			      final Map<Integer,Iterable<Integer>> extensionInvocation = this.<Integer>then(set);
			      String _string = extensionInvocation.toString();
			      _xblockexpression = (_string);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testBug401269_05() {
		'''
			import java.util.Map
			class C {
				def <A> Map<A, Expression<A>> then(Expression<A> expr) {}
				def <A extends Number & Comparable<?>> Map<A, NumberExpression<A>> then(NumberExpression<A> expr) {}
				
				def void m() {
					val NumberPath<Long> count = null
					val y = then(count)
					println(y)
				}
			}
			
			class NumberPath<T extends Number & Comparable<?>> extends NumberExpression<T> {}
			class NumberExpression<T extends Number & Comparable<?>> extends ComparableExpressionBase<T> {}
			class ComparableExpressionBase<T extends Comparable<?>> extends SimpleExpression<T> {}
			class SimpleExpression<T> extends ExpressionBase<T> {}
			class ExpressionBase<T> implements Expression<T> {}
			interface Expression<T> {}
		'''.assertCompilesTo('''
			import java.util.Map;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class C {
			  public <A extends Object> Map<A,Expression<A>> then(final Expression<A> expr) {
			    return null;
			  }
			  
			  public <A extends Number & Comparable<?>> Map<A,NumberExpression<A>> then(final NumberExpression<A> expr) {
			    return null;
			  }
			  
			  public void m() {
			    final NumberPath<Long> count = null;
			    final Map<Long,NumberExpression<Long>> y = this.<Long>then(count);
			    InputOutput.<Map<Long,NumberExpression<Long>>>println(y);
			  }
			}
		''')
	}		 
}