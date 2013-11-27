/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler

import com.google.inject.Inject
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.generator.IFilePostProcessor
import org.junit.Ignore
import org.junit.Test

class XtendCompilerTest extends AbstractXtendCompilerTest {

	@Inject protected IFilePostProcessor postProcessor

	@Test
	def testClassAndLocalVarConflict() {
		assertCompilesTo('''
			
			class A {
				
				def java.util.Set<String> doStuff(String Collections) {
					java::util::Collections::emptySet
				}
			
			}
		''', '''
			import java.util.Set;
			
			@SuppressWarnings("all")
			public class A {
			  public Set<String> doStuff(final String Collections) {
			    Set<String> _emptySet = java.util.Collections.<String>emptySet();
			    return _emptySet;
			  }
			}
		''')
	}
	@Test
	def testExpectationFromTypeParameter() {
		assertCompilesTo('''
			import java.util.Set
			import org.eclipse.xtext.common.types.JvmTypeParameter
			import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference
			import org.eclipse.xtext.xbase.typesystem.util.TypeParameterSubstitutor
			import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner
			
			class C extends TypeParameterSubstitutor<Set<JvmTypeParameter>> {
				
				new(ITypeReferenceOwner owner) {
					super(null, owner)
				}
				
				override substitute(LightweightTypeReference original) {
					original.accept(this, newHashSet)
				}
			
				override protected createVisiting() {
					return newHashSet
				}
			}
		''', '''
			import java.util.HashSet;
			import java.util.Set;
			import org.eclipse.xtext.common.types.JvmTypeParameter;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.typesystem.references.ITypeReferenceOwner;
			import org.eclipse.xtext.xbase.typesystem.references.LightweightTypeReference;
			import org.eclipse.xtext.xbase.typesystem.util.TypeParameterSubstitutor;
			
			@SuppressWarnings("all")
			public class C extends TypeParameterSubstitutor<Set<JvmTypeParameter>> {
			  public C(final ITypeReferenceOwner owner) {
			    super(null, owner);
			  }
			  
			  public LightweightTypeReference substitute(final LightweightTypeReference original) {
			    HashSet<JvmTypeParameter> _newHashSet = CollectionLiterals.<JvmTypeParameter>newHashSet();
			    LightweightTypeReference _accept = original.<Set<JvmTypeParameter>, LightweightTypeReference>accept(this, _newHashSet);
			    return _accept;
			  }
			  
			  protected Set<JvmTypeParameter> createVisiting() {
			    return CollectionLiterals.<JvmTypeParameter>newHashSet();
			  }
			}
		''')
	}
	
	/**
	 * Do not throw an exception for inherited dispatch methods.
	 */
	@Test
	def testInheritedDispatchMethods_01() {
		assertCompilesTo('''
			class C {
				def dispatch testFunction1(String s) {
					s.length
				}
				def dispatch testFunction1(Integer i) {
					i.intValue
				}
			}
			class D extends C {
				def dispatch testFunction1(Double d) {
					d.intValue
				}
			}
		''', '''
			import java.util.Arrays;
			
			@SuppressWarnings("all")
			public class C {
			  protected int _testFunction1(final String s) {
			    int _length = s.length();
			    return _length;
			  }
			  
			  protected int _testFunction1(final Integer i) {
			    int _intValue = i.intValue();
			    return _intValue;
			  }
			  
			  public int testFunction1(final Object i) {
			    if (i instanceof Integer) {
			      return _testFunction1((Integer)i);
			    } else if (i instanceof String) {
			      return _testFunction1((String)i);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(i).toString());
			    }
			  }
			}
		''')
	}
	
	@Ignore("TODO")
	@Test
	def testInheritedDispatchMethods_02() {
		assertCompilesTo('''
			class D extends C {
				def dispatch testFunction1(Double d) {
					d.intValue
				}
			}
			class C {
				def dispatch testFunction1(String s) {
					s.length
				}
				def dispatch testFunction1(Integer i) {
					i.intValue
				}
			}
		''', '''
			TODO
		''')
	}
	
	@Ignore("TODO")
	@Test
	def testInheritedDispatchMethods_03() {
		assertCompilesTo('''
			class D extends C {
				def dispatch testFunction1(Double d) {
					d.intValue
				}
				def dispatch testFunction1(Number n) {
					n.intValue
				}
			}
			class C {
				def dispatch testFunction1(String s) {
					s.length
				}
				def dispatch testFunction1(Integer i) {
					i.intValue
				}
			}
		''', '''
			TODO
		''')
	}
	
	@Test def void testForLoopWithTypeParameter_01() {
		'''
			class C<T extends CharSequence> {
				def void m(Iterable<T> iter) {
					for(x: iter) {
						x.charAt(1)
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C<T extends CharSequence> {
			  public void m(final Iterable<T> iter) {
			    for (final T x : iter) {
			      x.charAt(1);
			    }
			  }
			}
		''')
	}
	
	@Test def void testForLoopWithTypeParameter_02() {
		'''
			class C {
				def <T extends CharSequence> void m(Iterable<T> iter) {
					for(x: iter) {
						x.charAt(1)
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C {
			  public <T extends CharSequence> void m(final Iterable<T> iter) {
			    for (final T x : iter) {
			      x.charAt(1);
			    }
			  }
			}
		''')
	}
	
	@Test def void testForLoopWithTypeParameter_03() {
		'''
			class C<T extends CharSequence> {
				def <L extends Iterable<T>> void m(L iter) {
					for(x: iter) {
						x.charAt(1)
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class C<T extends CharSequence> {
			  public <L extends Iterable<T>> void m(final L iter) {
			    for (final T x : iter) {
			      x.charAt(1);
			    }
			  }
			}
		''')
	}
	
	@Test def void testForLoopWithTypeParameter_04() {
		'''
			abstract class C<T extends CharSequence> implements Iterable<T> {
				def void m() {
					for(x: this) {
						x.charAt(1)
					}
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public abstract class C<T extends CharSequence> implements Iterable<T> {
			  public void m() {
			    for (final T x : this) {
			      x.charAt(1);
			    }
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoid() {
		assertCompilesTo('''
			public class C  {
			    def m() {
			    	if (false) while(true) ''.toString
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public void m() {
			    if (false) {
			      boolean _while = true;
			      while (_while) {
			        "".toString();
			        _while = true;
			      }
			    }
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_01() {
		assertCompilesTo('''
			public class C  {
				def m() {
					val x = if (false) return '';
					x
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public String m() {
			    String _xblockexpression = null;
			    {
			      String _xifexpression = null;
			      if (false) {
			        return "";
			      }
			      final String x = _xifexpression;
			      _xblockexpression = (x);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_02() {
		assertCompilesTo('''
			public class C  {
				def void m() {
					val Object x = if (false) return;
					x.toString
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public void m() {
			    Object _xifexpression = null;
			    if (false) {
			      return;
			    }
			    final Object x = _xifexpression;
			    x.toString();
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_03() {
		assertCompilesTo('''
			public class C  {
				def m() {
					val x = if (false) return;
					voidFunction(x)
				}
				def void voidFunction(Object o) {}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public void m() {
			    Object _xifexpression = null;
			    if (false) {
			      return;
			    }
			    final Object x = _xifexpression;
			    this.voidFunction(x);
			  }
			  
			  public void voidFunction(final Object o) {
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_04() {
		assertCompilesTo('''
			public class C  {
				def m(Object x) {
					val r = switch x { String: return '' };
					r
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public String m(final Object x) {
			    String _xblockexpression = null;
			    {
			      String _switchResult = null;
			      boolean _matched = false;
			      if (!_matched) {
			        if (x instanceof String) {
			          _matched=true;
			          return "";
			        }
			      }
			      final String r = _switchResult;
			      _xblockexpression = (r);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_05() {
		assertCompilesTo('''
			public class C  {
				def void m(Object o) {
					val Object x = switch o { String: return };
					x.toString
				}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public void m(final Object o) {
			    Object _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (o instanceof String) {
			        _matched=true;
			        return;
			      }
			    }
			    final Object x = _switchResult;
			    x.toString();
			  }
			}
		''')
	}
	
	@Test def void testIfWithVoidButNonVoidExpectation_06() {
		assertCompilesTo('''
			public class C  {
				def m(Object o) {
					val x = switch o { String: return };
					voidFunction(x)
				}
				def void voidFunction(Object o) {}
			}
		''', '''
			@SuppressWarnings("all")
			public class C {
			  public void m(final Object o) {
			    Object _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (o instanceof String) {
			        _matched=true;
			        return;
			      }
			    }
			    final Object x = _switchResult;
			    this.voidFunction(x);
			  }
			  
			  public void voidFunction(final Object o) {
			  }
			}
		''')
	}
	
	@Test def void testWorkWithArrays_01() {
		assertCompilesTo('''
			public class Foo  {
			    def void doStuff() {
			    	val strings = newArrayOfSize(2)
			    	strings.set(0, 'world')
			    	strings.set(1, 'hello')
			    	println(strings.get(1) + ' ' + strings.get(0))
			    }
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class Foo {
			  public void doStuff() {
			    final String[] strings = new String[2];
			    strings[0] = "world";
			    strings[1] = "hello";
			    String _get = strings[1];
			    String _plus = (_get + " ");
			    String _get_1 = strings[0];
			    String _plus_1 = (_plus + _get_1);
			    InputOutput.<String>println(_plus_1);
			  }
			}
		''')
	}
	
	@Test def void testWorkWithArrays_02() {
		assertCompilesTo('''
			public class Foo  {
			    def void doStuff() {
			    	val numbers = newIntArrayOfSize(2)
			    	numbers.set(0, 42)
			    	numbers.set(1, 44)
			    	println(numbers.get(1) + ' ' + numbers.get(0) + ' length : ' + numbers.length)
			    }
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class Foo {
			  public void doStuff() {
			    final int[] numbers = new int[2];
			    numbers[0] = 42;
			    numbers[1] = 44;
			    int _get = numbers[1];
			    String _plus = (Integer.valueOf(_get) + " ");
			    int _get_1 = numbers[0];
			    String _plus_1 = (_plus + Integer.valueOf(_get_1));
			    String _plus_2 = (_plus_1 + " length : ");
			    int _length = numbers.length;
			    String _plus_3 = (_plus_2 + Integer.valueOf(_length));
			    InputOutput.<String>println(_plus_3);
			  }
			}
		''')
	}
	
	@Test def void testRecursiveLambda_01() {
		assertCompilesTo('''
			public class Foo  {
			    def void foo((String)=>String func) {
			    	foo[self.apply(it)]
			    }
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class Foo {
			  public void foo(final Function1<? super String,? extends String> func) {
			    final Function1<String,String> _function = new Function1<String,String>() {
			      public String apply(final String it) {
			        String _apply = this.apply(it);
			        return _apply;
			      }
			    };
			    this.foo(_function);
			  }
			}
		''')
	}
	
	@Test def void testRecursiveLambda_02() {
		assertCompilesTo('''
			public class Foo  {
			    def void foo(()=>void func) {
			    	foo[|self.apply()]
			    }
			}
		''', '''
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;

			@SuppressWarnings("all")
			public class Foo {
			  public void foo(final Procedure0 func) {
			    final Procedure0 _function = new Procedure0() {
			      public void apply() {
			        this.apply();
			      }
			    };
			    this.foo(_function);
			  }
			}
		''')
	}
	
	@Test def void testAbstractIterator_01() {
		assertCompilesTo('''
			import java.util.Iterator
			import com.google.common.collect.AbstractIterator
			public class Foo  {
			    def <T> Iterator<T> skipNulls(Iterator<T> iter) {
			    	val AbstractIterator<T> result = [|
			    		while(iter.hasNext) {
			    			val elem = iter.next
			    			if (elem != null)
			    				return elem
			    		}
			    		return self.endOfData
			    	]
			    	return result
			    }
			}
		''', '''
			import com.google.common.base.Objects;
			import com.google.common.collect.AbstractIterator;
			import java.util.Iterator;
			
			@SuppressWarnings("all")
			public class Foo {
			  public <T extends Object> Iterator<T> skipNulls(final Iterator<T> iter) {
			    final AbstractIterator<T> _function = new AbstractIterator<T>() {
			      @Override
			      protected T computeNext() {
			        boolean _hasNext = iter.hasNext();
			        boolean _while = _hasNext;
			        while (_while) {
			          {
			            final T elem = iter.next();
			            boolean _notEquals = (!Objects.equal(elem, null));
			            if (_notEquals) {
			              return elem;
			            }
			          }
			          boolean _hasNext_1 = iter.hasNext();
			          _while = _hasNext_1;
			        }
			        return this.endOfData();
			      }
			    };
			    final AbstractIterator<T> result = _function;
			    return result;
			  }
			}
		''')
	}
	
	@Test def void testAbstractIterator_02() {
		assertCompilesTo('''
			import com.google.common.collect.AbstractIterator
			public class Foo  {
			    def skipNulls() {
			    	val AbstractIterator<String> result = [|
			    		toString
			    		super.toString
			    		self.toString
			    	]
			    	return result
			    }
			}
		''', '''
			import com.google.common.collect.AbstractIterator;
			
			@SuppressWarnings("all")
			public class Foo {
			  public AbstractIterator<String> skipNulls() {
			    final AbstractIterator<String> _function = new AbstractIterator<String>() {
			      @Override
			      protected String computeNext() {
			        String _xblockexpression = null;
			        {
			          Foo.this.toString();
			          Foo.super.toString();
			          String _string = this.toString();
			          _xblockexpression = (_string);
			        }
			        return _xblockexpression;
			      }
			    };
			    final AbstractIterator<String> result = _function;
			    return result;
			  }
			}
		''')
	}
	
	@Test def void testAbstractIterator_03() {
		assertCompilesTo('''
			import java.util.Iterator
			import com.google.common.collect.AbstractIterator
			public class Foo  {
				def <T> Iterator<T> skipNulls(Iterator<T> iter) {
					val AbstractIterator<T> result = [|
						iter.findFirst [ it !== null ] ?: self.endOfData
					]
					return result
				}
			}
		''', '''
			import com.google.common.collect.AbstractIterator;
			import java.util.Iterator;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			import org.eclipse.xtext.xbase.lib.IteratorExtensions;
			import org.eclipse.xtext.xbase.lib.ObjectExtensions;
			
			@SuppressWarnings("all")
			public class Foo {
			  public <T extends Object> Iterator<T> skipNulls(final Iterator<T> iter) {
			    final AbstractIterator<T> _function = new AbstractIterator<T>() {
			      @Override
			      protected T computeNext() {
			        T _elvis = null;
			        final Function1<T,Boolean> _function = new Function1<T,Boolean>() {
			          public Boolean apply(final T it) {
			            boolean _tripleNotEquals = (it != null);
			            return Boolean.valueOf(_tripleNotEquals);
			          }
			        };
			        T _findFirst = IteratorExtensions.<T>findFirst(iter, _function);
			        if (_findFirst != null) {
			          _elvis = _findFirst;
			        } else {
			          T _endOfData = this.endOfData();
			          _elvis = ObjectExtensions.<T>operator_elvis(_findFirst, _endOfData);
			        }
			        return _elvis;
			      }
			    };
			    final AbstractIterator<T> result = _function;
			    return result;
			  }
			}
		''')
	}
	
	@Test
	def testAbstractIterator_04() { 
		assertCompilesTo(
			'''
				import java.util.Iterator
				import com.google.common.collect.AbstractIterator
				class FindFirstOnIt {

					def <T> Iterator<T> skipNulls(Iterator<T> it) {
						val AbstractIterator<T> result = [|
							findFirst [ it != null ] ?: self.endOfData
						]
						return result
					}
				}
			''', '''
				import com.google.common.base.Objects;
				import com.google.common.collect.AbstractIterator;
				import java.util.Iterator;
				import org.eclipse.xtext.xbase.lib.Functions.Function1;
				import org.eclipse.xtext.xbase.lib.IteratorExtensions;
				import org.eclipse.xtext.xbase.lib.ObjectExtensions;
				
				@SuppressWarnings("all")
				public class FindFirstOnIt {
				  public <T extends Object> Iterator<T> skipNulls(final Iterator<T> it) {
				    final AbstractIterator<T> _function = new AbstractIterator<T>() {
				      @Override
				      protected T computeNext() {
				        T _elvis = null;
				        final Function1<T,Boolean> _function = new Function1<T,Boolean>() {
				          public Boolean apply(final T it) {
				            boolean _notEquals = (!Objects.equal(it, null));
				            return Boolean.valueOf(_notEquals);
				          }
				        };
				        T _findFirst = IteratorExtensions.<T>findFirst(it, _function);
				        if (_findFirst != null) {
				          _elvis = _findFirst;
				        } else {
				          T _endOfData = this.endOfData();
				          _elvis = ObjectExtensions.<T>operator_elvis(_findFirst, _endOfData);
				        }
				        return _elvis;
				      }
				    };
				    final AbstractIterator<T> result = _function;
				    return result;
				  }
				}
			''')
	}
	
	@Test def testVoidMethod_01() throws Exception {
		'''
			class A {
				def m() {
					System::out.println('')
				}
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class A {
			  public void m() {
			    System.out.println("");
			  }
			}
		''')
	}
	
	@Test def testVoidMethod_02() throws Exception {
		'''
			class A {
				def m() {
					[ | System::out.println('') ]
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;

			@SuppressWarnings("all")
			public class A {
			  public Procedure0 m() {
			    final Procedure0 _function = new Procedure0() {
			      public void apply() {
			        System.out.println("");
			      }
			    };
			    return _function;
			  }
			}
		''')
	}
	
	@Test def testVoidMethod_03() throws Exception {
		'''
			class A {
				def m() {
					[ | println('') ]
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class A {
			  public Function0<String> m() {
			    final Function0<String> _function = new Function0<String>() {
			      public String apply() {
			        String _println = InputOutput.<String>println("");
			        return _println;
			      }
			    };
			    return _function;
			  }
			}
		''')
	}
	
	@Test def testThrowablesPropagate_01() throws Exception {
		'''
			import static extension Throwables.*
			import java.net.URI
			
			class Throwables {
				def static <T> T propagate(()=>T proc, (Exception)=>Exception handler) {
					try {
						proc.apply
					} catch(Exception e) {
						throw handler.apply(e)
					}
				}
			}
			class Client {
				val uri = [| new URI('')].propagate [ new IllegalArgumentException(it) ]
				
				def foo (){
					uri.toString
				}
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Exceptions;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class Throwables {
			  public static <T extends Object> T propagate(final Function0<? extends T> proc, final Function1<? super Exception,? extends Exception> handler) {
			    try {
			      T _xtrycatchfinallyexpression = null;
			      try {
			        T _apply = proc.apply();
			        _xtrycatchfinallyexpression = _apply;
			      } catch (final Throwable _t) {
			        if (_t instanceof Exception) {
			          final Exception e = (Exception)_t;
			          throw handler.apply(e);
			        } else {
			          throw Exceptions.sneakyThrow(_t);
			        }
			      }
			      return _xtrycatchfinallyexpression;
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}
	
	@Test def testThrowablesPropagate_02() throws Exception {
		'''
			import static extension Throwables.*
			import java.net.URI
			
			class Client {
				val uri = [| new URI('')].propagate [ new IllegalArgumentException(it) ]
			}
			class Throwables {
				def static <T> T propagate(()=>T proc, (Exception)=>Exception handler) {
					try {
						proc.apply
					} catch(Exception e) {
						throw handler.apply(e)
					}
				}
			}
		'''.assertCompilesTo('''
			import java.net.URI;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class Client {
			  private final URI uri = new Function0<URI>() {
			    public URI apply() {
			      final Function0<URI> _function = new Function0<URI>() {
			        public URI apply() {
			          try {
			            URI _uRI = new URI("");
			            return _uRI;
			          } catch (Throwable _e) {
			            throw Exceptions.sneakyThrow(_e);
			          }
			        }
			      };
			      final Function1<Exception,IllegalArgumentException> _function_1 = new Function1<Exception,IllegalArgumentException>() {
			        public IllegalArgumentException apply(final Exception it) {
			          IllegalArgumentException _illegalArgumentException = new IllegalArgumentException(it);
			          return _illegalArgumentException;
			        }
			      };
			      URI _propagate = Throwables.<URI>propagate(_function, _function_1);
			      return _propagate;
			    }
			  }.apply();
			}
		''')
	}
	
	@Test def testInterface() {
		'''
			interface Foo {
				def int foo()
				int bar=42
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public interface Foo {
			  public abstract int foo();
			  
			  public final static int bar = 42;
			}
		''')
	}
	
	@Test def testEnum() {
		'''
			enum Foo {
				FOO, BAR, BAZ
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public enum Foo {
			  FOO,
			  
			  BAR,
			  
			  BAZ;
			}
		''')
	}
	
	@Test def testSneakyThrowable() {
		'''
			class MyClass {
				def void throwsSomething() {
					throw new Throwable
				}
			}
			
		'''.assertCompilesTo('''
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class MyClass {
			  public void throwsSomething() {
			    try {
			      Throwable _throwable = new Throwable();
			      throw _throwable;
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}
	
	
	
	/**
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=395043
	 */
	@Test def testOverriddenVarArgs() {
		'''
			class B extends A {
				override foo(String x, String...args) {
					return x
				}
			}
			abstract class A {
				def String foo(String x, String...args)
			}
			
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class B extends A {
			  public String foo(final String x, final String... args) {
			    return x;
			  }
			}
		''')
	}
	
	@Test def testItShadowing_01() {
		assertCompilesTo('''
			class Foo<T> {
				def grammar(String it) {
					for (it : it.toCharArray) {
						println(it)
					}
				}
			}
		''','''
			import org.eclipse.xtext.xbase.lib.InputOutput;
			
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public void grammar(final String it) {
			    char[] _charArray = it.toCharArray();
			    for (final char it_1 : _charArray) {
			      InputOutput.<Character>println(Character.valueOf(it_1));
			    }
			  }
			}
		''')
	}
	
	@Test def testItShadowing_02() {
		val tquotes = "'''"
		val lt = '�'
		val rt = '�'
		assertCompilesTo('''
			class Foo<T> {
				def grammar(String it) �tquotes�
					�lt�FOR it: it.toCharArray�rt�
						�lt�it�rt�
					�lt�ENDFOR�rt�
				�tquotes�
			}
		''','''
			import org.eclipse.xtend2.lib.StringConcatenation;
			
			@SuppressWarnings("all")
			public class Foo<T extends Object> {
			  public CharSequence grammar(final String it) {
			    StringConcatenation _builder = new StringConcatenation();
			    {
			      char[] _charArray = it.toCharArray();
			      for(final char it_1 : _charArray) {
			        _builder.append(it_1, "");
			        _builder.newLineIfNotEmpty();
			      }
			    }
			    return _builder;
			  }
			}
		''')
	}
	
	@Test
	def testJavaLangReflectImport() { 
		assertCompilesTo('''
			package foo
			class Bar {
				def void doStuff(java.lang.reflect.Method m) {}
			}
		''', '''
			package foo;

			import java.lang.reflect.Method;
			
			@SuppressWarnings("all")
			public class Bar {
			  public void doStuff(final Method m) {
			  }
			}
		''')
	}
	
	@Test def void testSwitchAsVoid() {
		assertCompilesTo('''
			public class C  {
			    def m(Object a) {
			    	switch a {
			    		case 'b': while(true) ''.toString
			    		case 'c': if (true) return
			    		case 'd': for(i: 1..1) i.toString
			    	}
				}
			}
		''', '''
			import com.google.common.base.Objects;
			import org.eclipse.xtext.xbase.lib.IntegerRange;
			
			@SuppressWarnings("all")
			public class C {
			  public void m(final Object a) {
			    boolean _matched = false;
			    if (!_matched) {
			      if (Objects.equal(a,"b")) {
			        _matched=true;
			        boolean _while = true;
			        while (_while) {
			          "".toString();
			          _while = true;
			        }
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(a,"c")) {
			        _matched=true;
			        if (true) {
			          return;
			        }
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(a,"d")) {
			        _matched=true;
			        IntegerRange _upTo = new IntegerRange(1, 1);
			        for (final Integer i : _upTo) {
			          i.toString();
			        }
			      }
			    }
			  }
			}
		''')
	}
	
	@Test def void testSwitchWithNonVoidReturn_01() {
		assertCompilesTo('''
			public class C  {
			    def m(Object a) {
			    	switch a {
			    		case 'b': while(true) ''.toString
			    		case 'c': if (true) return 'a'
			    		case 'd': for(i: 1..1) i.toString
			    	}
				}
			}
		''', '''
			import com.google.common.base.Objects;
			import org.eclipse.xtext.xbase.lib.IntegerRange;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Object a) {
			    boolean _matched = false;
			    if (!_matched) {
			      if (Objects.equal(a,"b")) {
			        _matched=true;
			        boolean _while = true;
			        while (_while) {
			          "".toString();
			          _while = true;
			        }
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(a,"c")) {
			        _matched=true;
			        if (true) {
			          return "a";
			        }
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(a,"d")) {
			        _matched=true;
			        IntegerRange _upTo = new IntegerRange(1, 1);
			        for (final Integer i : _upTo) {
			          i.toString();
			        }
			      }
			    }
			    return null;
			  }
			}
		''')
	}
	
	@Test def void testSwitchWithNonVoidReturn_02() {
		assertCompilesTo('''
			public class C  {
			    def m(Object a) {
			    	val String s = switch a {
			    		case 'a': { while(true) ''.toString '' }
			    		case 'b': if (true) return 'a'
			    	}
				}
			}
		''', '''
			import com.google.common.base.Objects;
			
			@SuppressWarnings("all")
			public class C {
			  public String m(final Object a) {
			    String _switchResult = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (Objects.equal(a,"a")) {
			        _matched=true;
			        String _xblockexpression = null;
			        {
			          boolean _while = true;
			          while (_while) {
			            "".toString();
			            _while = true;
			          }
			          _xblockexpression = ("");
			        }
			        _switchResult = _xblockexpression;
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(a,"b")) {
			        _matched=true;
			        if (true) {
			          return "a";
			        }
			      }
			    }
			    final String s = _switchResult;
			    return null;
			  }
			}
		''')
	}
	
	@Test
	def testSwitchOverNull() {
		assertCompilesTo('''
			public class Foo  {
			    def foo() {
			        switch null {
			            case null : [Object it|it]
			            case null : [Integer it|it]
			        }
			    }    
			}
		''', '''
			import com.google.common.base.Objects;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class Foo {
			  public Function1<? super Integer,? extends Object> foo() {
			    Function1<? super Integer,? extends Object> _switchResult = null;
			    final Object _switchValue = null;
			    boolean _matched = false;
			    if (!_matched) {
			      if (Objects.equal(_switchValue,null)) {
			        _matched=true;
			        final Function1<Object,Object> _function = new Function1<Object,Object>() {
			          public Object apply(final Object it) {
			            return it;
			          }
			        };
			        _switchResult = _function;
			      }
			    }
			    if (!_matched) {
			      if (Objects.equal(_switchValue,null)) {
			        _matched=true;
			        final Function1<Integer,Integer> _function_1 = new Function1<Integer,Integer>() {
			          public Integer apply(final Integer it) {
			            return it;
			          }
			        };
			        _switchResult = _function_1;
			      }
			    }
			    return _switchResult;
			  }
			}
		''')
	}
	
	@Test
	def testTryCatch() { 
		assertCompilesTo('''
			package foo
			class Bar {
				def void doStuff(java.lang.reflect.Method m) {
					try {
						// do nothing
					} catch (java.io.IOException e) {
						throw e
					} catch (Exception e) {
						throw e
					} finally {
						// do nothing
					}
				}
			}
		''', '''
			package foo;
			
			import java.io.IOException;
			import java.lang.reflect.Method;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class Bar {
			  public void doStuff(final Method m) {
			    try {
			      try {
			      } catch (final Throwable _t) {
			        if (_t instanceof IOException) {
			          final IOException e = (IOException)_t;
			          throw e;
			        } else if (_t instanceof Exception) {
			          final Exception e_1 = (Exception)_t;
			          throw e_1;
			        } else {
			          throw Exceptions.sneakyThrow(_t);
			        }
			      } finally {
			      }
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}

	@Test	
	def testClosureSneakyThrow() {
		assertCompilesTo('''
			import java.io.File
			import java.io.IOException
			import java.util.Collections
			
			class Foo {
			   def bar() {
			       try {
			           newArrayList("file1.ext").map(f| new File(f).canonicalFile)
			       } catch(IOException o) {
			           Collections::<File>emptyList
			       }
			   }
			}
		''','''
			import java.io.File;
			import java.io.IOException;
			import java.util.ArrayList;
			import java.util.Collections;
			import java.util.List;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			import org.eclipse.xtext.xbase.lib.ListExtensions;
			
			@SuppressWarnings("all")
			public class Foo {
			  public List<File> bar() {
			    List<File> _xtrycatchfinallyexpression = null;
			    try {
			      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("file1.ext");
			      final Function1<String,File> _function = new Function1<String,File>() {
			        public File apply(final String f) {
			          try {
			            File _file = new File(f);
			            File _canonicalFile = _file.getCanonicalFile();
			            return _canonicalFile;
			          } catch (Throwable _e) {
			            throw Exceptions.sneakyThrow(_e);
			          }
			        }
			      };
			      List<File> _map = ListExtensions.<String, File>map(_newArrayList, _function);
			      _xtrycatchfinallyexpression = _map;
			    } catch (final Throwable _t) {
			      if (_t instanceof IOException) {
			        final IOException o = (IOException)_t;
			        List<File> _emptyList = Collections.<File>emptyList();
			        _xtrycatchfinallyexpression = _emptyList;
			      } else {
			        throw Exceptions.sneakyThrow(_t);
			      }
			    }
			    return _xtrycatchfinallyexpression;
			  }
			}
		''')
	}
	
	@Test
	def testClosureSneakyThrow_02() throws Exception {
		'''
			abstract class Foo {
			 def Object getFoo(String x) throws Exception
			 def bar() {
			   <String>newArrayList.toMap[foo]
			 }
			}
		'''.assertCompilesTo('''
			import java.util.ArrayList;
			import java.util.Map;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			import org.eclipse.xtext.xbase.lib.IterableExtensions;
			
			@SuppressWarnings("all")
			public abstract class Foo {
			  public abstract Object getFoo(final String x) throws Exception;
			  
			  public Map<Object,String> bar() {
			    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
			    final Function1<String,Object> _function = new Function1<String,Object>() {
			      public Object apply(final String it) {
			        try {
			          Object _foo = Foo.this.getFoo(it);
			          return _foo;
			        } catch (Throwable _e) {
			          throw Exceptions.sneakyThrow(_e);
			        }
			      }
			    };
			    Map<Object,String> _map = IterableExtensions.<Object, String>toMap(_newArrayList, _function);
			    return _map;
			  }
			}
		''')
	}
	
	@Test
	def testFieldInitialization_01() { 
		assertCompilesTo('''
			package foo
			class Bar {
				String s1 = null
				protected String s2 = ''
				public String s3 = s2
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  private String s1 = null;
			  
			  protected String s2 = "";
			  
			  public String s3 = this.s2;
			}
		''')
	}
	
	@Test
	def testFieldInitialization_02() { 
		assertCompilesTo('''
			package foo
			class Bar {
				String s0 = s1
				static String s1 = null
				protected static String s2 = ''
				public static String s3 = s2
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  private String s0 = Bar.s1;
			  
			  private static String s1 = null;
			  
			  protected static String s2 = "";
			  
			  public static String s3 = Bar.s2;
			}
		''')
	}
	
	@Test
	def testFieldInitialization_03() { 
		assertCompilesTo('''
			package foo
			class Bar {
				String s = newArrayList.toString
			}
		''', '''
			package foo;
			
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			
			@SuppressWarnings("all")
			public class Bar {
			  private String s = new Function0<String>() {
			    public String apply() {
			      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
			      String _string = _newArrayList.toString();
			      return _string;
			    }
			  }.apply();
			}
		''')
	}
	
	@Test
	def testFieldInitialization_04() { 
		assertCompilesTo('''
			package foo
			class Bar {
				String s = toString + super.toString
			}
		''', '''
			package foo;

			import org.eclipse.xtext.xbase.lib.Functions.Function0;

			@SuppressWarnings("all")
			public class Bar {
			  private String s = new Function0<String>() {
			    public String apply() {
			      String _string = Bar.this.toString();
			      String _string_1 = Bar.super.toString();
			      String _plus = (_string + _string_1);
			      return _plus;
			    }
			  }.apply();
			}
		''')
	}
	
	@Test
	def testConstructorDeclaration_01() { 
		assertCompilesTo('''
			package foo
			class Bar {
				new() {
					super()
				}
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  public Bar() {
			    super();
			  }
			}
		''')
	}
	
	@Test
	def testConstructorDeclaration_02() { 
		assertCompilesTo('''
			package foo
			class Bar {
				new() {
					this(123)
				}
				new(int a) {
					super()
				}
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  public Bar() {
			    this(123);
			  }
			  
			  public Bar(final int a) {
			    super();
			  }
			}
		''')
	}
	
	@Test
	def testConstructorDeclaration_03() { 
		assertCompilesTo('''
			package foo
			class Bar {
				new() {
					this(123.toString)
				}
				new(String s) {}
			}
		''', '''
			package foo;
			
			import org.eclipse.xtext.xbase.lib.Functions.Function0;

			@SuppressWarnings("all")
			public class Bar {
			  public Bar() {
			    this(new Function0<String>() {
			      public String apply() {
			        String _string = Integer.valueOf(123).toString();
			        return _string;
			      }
			    }.apply());
			  }
			  
			  public Bar(final String s) {
			  }
			}
		''')
	}
	
	@Test
	def testSneakyThrow() { 
		assertCompilesTo('''
			package foo
			class Bar {
				def void doStuff() {
					throw new java.io.IOException()
				}
			}
		''', '''
			package foo;

			import java.io.IOException;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class Bar {
			  public void doStuff() {
			    try {
			      IOException _iOException = new IOException();
			      throw _iOException;
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}
	
	@Test
	def testSneakyThrow_01() { 
		assertCompilesTo('''
			package foo
			
			import java.io.IOException
			
			class Bar {
				def void doStuff() throws IOException {
					throw new IOException()
				}
			}
		''', '''
			package foo;

			import java.io.IOException;
			
			@SuppressWarnings("all")
			public class Bar {
			  public void doStuff() throws IOException {
			    IOException _iOException = new IOException();
			    throw _iOException;
			  }
			}
		''')
	}

	@Test
	def testSimple() { 
		assertCompilesTo('''
			package foo
			class Bar {
				def Integer doStuff(String x) {x.length}
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Bar {
			  public Integer doStuff(final String x) {
			    int _length = x.length();
			    return Integer.valueOf(_length);
			  }
			}
		''')
	}

	@Test
	def testConstructor() { 
		assertCompilesTo('''
			package foo
			class Bar {
				def foo() {new String()}
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  public String foo() {
			    String _string = new String();
			    return _string;
			  }
			}
		''')
	}

	@Test
	def testExtends() {
		assertCompilesTo('''
			package foo
			class Bar extends Thread {
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar extends Thread {
			}
		''')
	}

	@Test
	def testExtendsException() {
		assertCompilesTo('''
			package foo
			
			class NoSuchElementException extends java.util.NoSuchElementException {
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class NoSuchElementException extends java.util.NoSuchElementException {
			}
		''')
	}

	@Test
	def testVisibilityOfDispatchMethods() {
		assertCompilesTo('''
			package foo
			class NoSuchElementException {
				def dispatch void foo(String s) {}
				def dispatch void foo(Object s) {}
			}
		''', '''
			package foo;

			import java.util.Arrays;

			@SuppressWarnings("all")
			public class NoSuchElementException {
			  protected void _foo(final String s) {
			  }
			  
			  protected void _foo(final Object s) {
			  }
			  
			  public void foo(final Object s) {
			    if (s instanceof String) {
			      _foo((String)s);
			      return;
			    } else if (s != null) {
			      _foo(s);
			      return;
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(s).toString());
			    }
			  }
			}
		''')
	}
	
	@Test
	def testParenthesisInDispatchMethodsGuards() {
		assertCompilesTo('''
			package foo
			class MyType {
				def dispatch void foo(String s, CharSequence other) {}
				def dispatch void foo(String s, Void other) {}
				def dispatch void foo(Object s, Object other) {}
			}
		''', '''
			package foo;

			import java.util.Arrays;

			@SuppressWarnings("all")
			public class MyType {
			  protected void _foo(final String s, final CharSequence other) {
			  }
			  
			  protected void _foo(final String s, final Void other) {
			  }
			  
			  protected void _foo(final Object s, final Object other) {
			  }
			  
			  public void foo(final Object s, final Object other) {
			    if (s instanceof String
			         && other instanceof CharSequence) {
			      _foo((String)s, (CharSequence)other);
			      return;
			    } else if (s instanceof String
			         && other == null) {
			      _foo((String)s, (Void)null);
			      return;
			    } else if (s != null
			         && other != null) {
			      _foo(s, other);
			      return;
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(s, other).toString());
			    }
			  }
			}
		''')
	}
	
	@Test
	def testParenthesisInDispatchMethodsGuards_reordered() {
		assertCompilesTo('''
			package foo
			class MyType {
				def dispatch void foo(Object s, Object other) {}
				def dispatch void foo(String s, Void other) {}
				def dispatch void foo(String s, CharSequence other) {}
			}
		''', '''
			package foo;

			import java.util.Arrays;

			@SuppressWarnings("all")
			public class MyType {
			  protected void _foo(final Object s, final Object other) {
			  }
			  
			  protected void _foo(final String s, final Void other) {
			  }
			  
			  protected void _foo(final String s, final CharSequence other) {
			  }
			  
			  public void foo(final Object s, final Object other) {
			    if (s instanceof String
			         && other instanceof CharSequence) {
			      _foo((String)s, (CharSequence)other);
			      return;
			    } else if (s instanceof String
			         && other == null) {
			      _foo((String)s, (Void)null);
			      return;
			    } else if (s != null
			         && other != null) {
			      _foo(s, other);
			      return;
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(s, other).toString());
			    }
			  }
			}
		''')
	}

	@Test
	def testNoUnnecessaryCastInDispatchMethods() {
		assertCompilesTo('''
			package foo
			class MyType {
				def dispatch void foo(StringBuilder s, boolean b, String other) {}
				def dispatch void foo(StringBuffer s, boolean b, String other) {}
			}
		''', '''
			package foo;
			
			import java.util.Arrays;
			
			@SuppressWarnings("all")
			public class MyType {
			  protected void _foo(final StringBuilder s, final boolean b, final String other) {
			  }
			  
			  protected void _foo(final StringBuffer s, final boolean b, final String other) {
			  }
			  
			  public void foo(final Object s, final boolean b, final String other) {
			    if (s instanceof StringBuffer) {
			      _foo((StringBuffer)s, b, other);
			      return;
			    } else if (s instanceof StringBuilder) {
			      _foo((StringBuilder)s, b, other);
			      return;
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(s, b, other).toString());
			    }
			  }
			}
		''')
	}

	@Test
	def testExtendsArrayList_01()  {
		assertCompilesTo('''
			package foo
			class MyList<T> extends java.util.ArrayList<T> {}
		''', '''
			package foo;
			
			import java.util.ArrayList;
			
			@SuppressWarnings("all")
			public class MyList<T extends Object> extends ArrayList<T> {
			}
		''')
	}

	@Test
	def testExtendsArrayList_02() {
		assertCompilesTo('''
			package foo
			class StringList extends java.util.ArrayList<String> {
			}
		''', '''
			package foo;
			
			import java.util.ArrayList;
			
			@SuppressWarnings("all")
			public class StringList extends ArrayList<String> {
			}
		''')
	}

	@Test
	def testImplements() {
		assertCompilesTo('''
			package foo
			class Bar implements Cloneable, java.io.Serializable {
			}
		''', '''
			package foo;

			import java.io.Serializable;
			
			@SuppressWarnings("all")
			public class Bar implements Cloneable, Serializable {
			}
		''')
	}

	@Test
	def testConstructor_2() {
		assertCompilesTo('''
			package foo
			class Bar {
				def foo() {new java.util.HashMap<String,java.io.Serializable>()}
			}
		''', '''
			package foo;

			import java.io.Serializable;
			import java.util.HashMap;

			@SuppressWarnings("all")
			public class Bar {
			  public HashMap<String,Serializable> foo() {
			    HashMap<String,Serializable> _hashMap = new HashMap<String, Serializable>();
			    return _hashMap;
			  }
			}
		''')
	}

	@Test
	def testConstructor_3() {
		assertCompilesTo('''
			class C<X extends java.io.IOException> {
				new () throws X {
				}
				def m() {
					new C
				}
			}
		''', '''
			import java.io.IOException;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class C<X extends IOException> {
			  public C() throws X {
			  }
			  
			  public C<IOException> m() {
			    try {
			      C<IOException> _c = new C<IOException>();
			      return _c;
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}

	@Test
	def testConstructor_4() {
		assertCompilesTo('''
			class C<X extends java.io.IOException> {
				new () throws X {
				}
				def void m() {
					new C
				}
			}
		''', '''
			import java.io.IOException;
			import org.eclipse.xtext.xbase.lib.Exceptions;
			
			@SuppressWarnings("all")
			public class C<X extends IOException> {
			  public C() throws X {
			  }
			  
			  public void m() {
			    try {
			      new C<IOException>();
			    } catch (Throwable _e) {
			      throw Exceptions.sneakyThrow(_e);
			    }
			  }
			}
		''')
	}

	@Test
	def testSuperCall_01() {
		assertCompilesTo('''
			package x class Y extends Object {
				override boolean equals(Object p){
					if ('foo' == p)
						return true
					else
						super.equals(p) 
				} 
			}
		''', '''
			package x;

			import com.google.common.base.Objects;

			@SuppressWarnings("all")
			public class Y {
			  public boolean equals(final Object p) {
			    boolean _xifexpression = false;
			    boolean _equals = Objects.equal("foo", p);
			    if (_equals) {
			      return true;
			    } else {
			      boolean _equals_1 = super.equals(p);
			      _xifexpression = _equals_1;
			    }
			    return _xifexpression;
			  }
			}
		''')
	}
	
	@Test
	def testSuperCall_02() {
		assertCompilesTo('''
			package x
			class A {
				def String getThing() {
					this.getThing('')
					''.getThing
					this.thing
					''.thing
					thing
					getThing('')
				}
				def protected String getThing(String s) {
					return s
				}
			}
			class B extends A {
				override getThing() {
					if (true)
						return super.getThing
					return getThing('')
				}
			}
		''', '''
			package x;
			
			@SuppressWarnings("all")
			public class A {
			  public String getThing() {
			    String _xblockexpression = null;
			    {
			      this.getThing("");
			      this.getThing("");
			      this.getThing();
			      this.getThing("");
			      this.getThing();
			      String _thing = this.getThing("");
			      _xblockexpression = (_thing);
			    }
			    return _xblockexpression;
			  }
			  
			  protected String getThing(final String s) {
			    return s;
			  }
			}
		''')
	}
	
	@Test
	def testSuperCall_03() {
		assertCompilesTo('''
			package x
			class B extends A {
				override getThing() {
					this.getThing('')
					''.getThing
					this.thing
					''.thing
					thing
					getThing('')
					super.getThing('')
					super.thing
					super.getThing
					super.getThing()
				}
			}
			class A {
				def String getThing() {
					getThing('')
				}
				def protected String getThing(String s) {
					return s
				}
			}
		''', '''
			package x;
			
			import x.A;
			
			@SuppressWarnings("all")
			public class B extends A {
			  public String getThing() {
			    String _xblockexpression = null;
			    {
			      this.getThing("");
			      this.getThing("");
			      this.getThing();
			      this.getThing("");
			      this.getThing();
			      this.getThing("");
			      super.getThing("");
			      super.getThing();
			      super.getThing();
			      String _thing = super.getThing();
			      _xblockexpression = (_thing);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test
	def testCreateExtension() {
		assertCompilesTo('''
			package foo
			class Bar {
				def create result: new StringBuilder() aBuilder(String x) {
					result.append(x)
				}
			}
		''', '''
			package foo;

			import java.util.ArrayList;
			import java.util.HashMap;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			
			@SuppressWarnings("all")
			public class Bar {
			  public StringBuilder aBuilder(final String x) {
			    final ArrayList<?> _cacheKey = CollectionLiterals.newArrayList(x);
			    final StringBuilder _result;
			    synchronized (_createCache_aBuilder) {
			      if (_createCache_aBuilder.containsKey(_cacheKey)) {
			        return _createCache_aBuilder.get(_cacheKey);
			      }
			      StringBuilder _stringBuilder = new StringBuilder();
			      _result = _stringBuilder;
			      _createCache_aBuilder.put(_cacheKey, _result);
			    }
			    _init_aBuilder(_result, x);
			    return _result;
			  }
			  
			  private final HashMap<ArrayList<? extends Object>,StringBuilder> _createCache_aBuilder = CollectionLiterals.newHashMap();
			  
			  private void _init_aBuilder(final StringBuilder result, final String x) {
			    result.append(x);
			  }
			}
		''');
	}

	@Test
	def testJavaDocs() {
		assertCompilesTo('''
			package foo
			/**
			 * I am Bar
			 */
			class Bar {
				/**
				 */
				def foo() {}
				
				/**
				 * I am bar,
				 * really!
				 */
				int bar
			}
		''', '''
			package foo;

			/**
			 * I am Bar
			 */
			@SuppressWarnings("all")
			public class Bar {
			  public Object foo() {
			    return null;
			  }
			  
			  /**
			   * I am bar,
			   * really!
			   */
			  private int bar;
			}
			''');
	}
	
	@Test
	def testJavaDocs_2() {
		assertCompilesTo('''
			package foo

			class Bar {
				/**
				 * @param string
				 */
				def foo(String string) {}

			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  /**
			   * @param string
			   */
			  public Object foo(final String string) {
			    return null;
			  }
			}
			''');
	}

	@Test
	def testJavaDocs_3() {
		assertCompilesTo('''
			package foo

			class Bar {
				/**
				 * {@link java.lang.String @see Integer}
				 */
				def foo(String string) {}

			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  /**
			   * {@link java.lang.String @see Integer}
			   */
			  public Object foo(final String string) {
			    return null;
			  }
			}
			''');
	}

	@Test
	def testJavaDocs_4() {
		assertCompilesTo('''
			package foo

			class Bar {
				/**
				 * {@link java.lang.String @see Integer}
				 */
				def foo(String string) {}

			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  /**
			   * {@link java.lang.String @see Integer}
			   */
			  public Object foo(final String string) {
			    return null;
			  }
			}
			''');
	}

	@Test
	def testJavaDocs_5() {
		assertCompilesTo('''
			package foo

			class Bar {
				/**
				 * {@link java.lang.String
				 * @param string - stuff
				 * @param string2
				 */
				def foo(String string, String string2) {}

			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  /**
			   * {@link java.lang.String
			   * @param string - stuff
			   * @param string2
			   */
			  public Object foo(final String string, final String string2) {
			    return null;
			  }
			}
			''');
	}

	@Test
	def testStaticMethod() {
		assertCompilesTo('''
			package foo
			class Bar {
				def static foo() { 42 }
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  public static int foo() {
			    return 42;
			  }
			}
			''');
	}
	
	@Test
	def testStaticMethod_02() {
		assertCompilesTo('''
			package foo
			class Bar<T extends CharSequence> {
				static def factory() { new Bar }
				static def concreteFactory() { new Bar<StringBuilder> }
				static def <X extends String> parameterizedFactory() { new Bar<X> }
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar<T extends CharSequence> {
			  public static Bar<CharSequence> factory() {
			    Bar<CharSequence> _bar = new Bar<CharSequence>();
			    return _bar;
			  }
			  
			  public static Bar<StringBuilder> concreteFactory() {
			    Bar<StringBuilder> _bar = new Bar<StringBuilder>();
			    return _bar;
			  }
			  
			  public static <X extends String> Bar<X> parameterizedFactory() {
			    Bar<X> _bar = new Bar<X>();
			    return _bar;
			  }
			}
		''');
	}
	
	@Test
	def testInstanceMethod() {
		assertCompilesTo('''
			package foo
			class Bar<T extends CharSequence> {
				def factory() { new Bar }
				def concreteFactory() { new Bar<String> }
				def parameterizedFactory() { new Bar<T> }
				def <X extends T> concreteParameterizedFactory() { new Bar<X> }
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar<T extends CharSequence> {
			  public Bar<CharSequence> factory() {
			    Bar<CharSequence> _bar = new Bar<CharSequence>();
			    return _bar;
			  }
			  
			  public Bar<String> concreteFactory() {
			    Bar<String> _bar = new Bar<String>();
			    return _bar;
			  }
			  
			  public Bar<T> parameterizedFactory() {
			    Bar<T> _bar = new Bar<T>();
			    return _bar;
			  }
			  
			  public <X extends T> Bar<X> concreteParameterizedFactory() {
			    Bar<X> _bar = new Bar<X>();
			    return _bar;
			  }
			}
		''');
	}
	
	@Test
	def testStaticField() {
		assertCompilesTo('''
			package foo
			class Bar {
				static int foo
			}
		''', '''
			package foo;

			@SuppressWarnings("all")
			public class Bar {
			  private static int foo;
			}
			''');
	}
	
	@Test
	def testNestedClosureWithIt() {
		assertCompilesTo('''
			class X {
				def foo() {
					val (String)=>String function = [ [String it | it].apply(it) ]
					function.apply('foo')
				}
			}
		''','''
			import org.eclipse.xtext.xbase.lib.Functions.Function1;
			
			@SuppressWarnings("all")
			public class X {
			  public String foo() {
			    String _xblockexpression = null;
			    {
			      final Function1<String,String> _function = new Function1<String,String>() {
			        public String apply(final String it) {
			          final Function1<String,String> _function = new Function1<String,String>() {
			            public String apply(final String it) {
			              return it;
			            }
			          };
			          String _apply = _function.apply(it);
			          return _apply;
			        }
			      };
			      final Function1<? super String,? extends String> function = _function;
			      String _apply = function.apply("foo");
			      _xblockexpression = (_apply);
			    }
			    return _xblockexpression;
			  }
			}
		''')
	}
	
	@Test
	def testNestedClosureSuperCall() {
		assertCompilesTo('''
			class X {
				def foo() {
					[| [| super.toString ].apply ].apply
				}
			}
		''','''
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			
			@SuppressWarnings("all")
			public class X {
			  public String foo() {
			    final Function0<String> _function = new Function0<String>() {
			      public String apply() {
			        final Function0<String> _function = new Function0<String>() {
			          public String apply() {
			            String _string = X.super.toString();
			            return _string;
			          }
			        };
			        String _apply = _function.apply();
			        return _apply;
			      }
			    };
			    String _apply = _function.apply();
			    return _apply;
			  }
			}
		''')
	}

	@Test
	def testExplicitBoxingUnboxing_01() {
		assertCompilesTo('''
			class X {
				def foo(int p0, Integer p1) {
					foo(p1,p0)
				}
			}
		''','''
			@SuppressWarnings("all")
			public class X {
			  public Object foo(final int p0, final Integer p1) {
			    Object _foo = this.foo((p1).intValue(), Integer.valueOf(p0));
			    return _foo;
			  }
			}
		''')
	}
	
	@Test def testExplicitBoxingUnboxing_02() {
		assertCompilesTo('''
			class X {
				def Object foo(int p0, Integer p1) {
					foo(p1,p0)
				}
			}
		''','''
			@SuppressWarnings("all")
			public class X {
			  public Object foo(final int p0, final Integer p1) {
			    Object _foo = this.foo((p1).intValue(), Integer.valueOf(p0));
			    return _foo;
			  }
			}
		''')
	}
	
	@Test
	def testOptionalSemicola() {
		assertCompilesTo('''
			package foo;
			
			import java.io.File;
			
			public class Foo {
				private int bar;
				
				private File baz;
			}
		''', '''
			package foo;
			
			import java.io.File;
			
			@SuppressWarnings("all")
			public class Foo {
			  private int bar;
			  
			  private File baz;
			}
		''')
	}
	
	@Test
	def testNoDeclarationForFeatureCalls() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				private static String FOO = "Holla";
				
				private String baz = FOO;
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Foo {
			  private static String FOO = "Holla";
			  
			  private String baz = Foo.FOO;
			}
		''')
	}
	
	@Test
	def testNoDeclarationForFeatureCalls_02() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				private static String FOO = "Holla";
				private String foo = "Holla";
				
				def void test(String x, String y, int integer) {
					new Foo().test(foo, FOO, Integer::MAX_VALUE)
				}
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Foo {
			  private static String FOO = "Holla";
			  
			  private String foo = "Holla";
			  
			  public void test(final String x, final String y, final int integer) {
			    Foo _foo = new Foo();
			    _foo.test(this.foo, Foo.FOO, Integer.MAX_VALUE);
			  }
			}
		''')
	}
	
	@Test
	def testNoVarForReturnExpression() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				def String returnString(String x, String y) {
					return x + '' + y
				}
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Foo {
			  public String returnString(final String x, final String y) {
			    return ((x + "") + y);
			  }
			}
		''')
	}
	
	@Test
	def testNoVarForRightHandSideOfVarDecl() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				def String returnString(String x, String y) {
					val z = x + '' + y
					return z
				}
			}
		''', '''
			package foo;
			
			@SuppressWarnings("all")
			public class Foo {
			  public String returnString(final String x, final String y) {
			    final String z = ((x + "") + y);
			    return z;
			  }
			}
		''')
	}
	@Test
	def testJavaKeywordsUsed() {
		assertCompilesTo('''
			package foo;
			
			public class Foo {
				def void someMethod(String assert) {
					val synchronized = newArrayList('foo')
					for (volatile : synchronized) {
						switch continue : volatile {
							case continue.length > 2 : synchronized.forEach(break | (break + continue).toString)
						}
					}
				}
			}
		''', '''
			package foo;
			
			import java.util.ArrayList;
			import org.eclipse.xtext.xbase.lib.CollectionLiterals;
			import org.eclipse.xtext.xbase.lib.IterableExtensions;
			import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
			
			@SuppressWarnings("all")
			public class Foo {
			  public void someMethod(final String assert_) {
			    final ArrayList<String> synchronized_ = CollectionLiterals.<String>newArrayList("foo");
			    for (final String volatile_ : synchronized_) {
			      final String continue_ = volatile_;
			      boolean _matched = false;
			      if (!_matched) {
			        int _length = continue_.length();
			        boolean _greaterThan = (_length > 2);
			        if (_greaterThan) {
			          _matched=true;
			          final Procedure1<String> _function = new Procedure1<String>() {
			            public void apply(final String break_) {
			              (break_ + continue_).toString();
			            }
			          };
			          IterableExtensions.<String>forEach(synchronized_, _function);
			        }
			      }
			    }
			  }
			}
		''')
	}
	
	@Test
	def testFindFirstOnIt_01() { 
		assertCompilesTo(
			'''
				class FindFirstOnIt {
					def <T> useExtension(Iterable<T> it) {
						findFirst [ it !== null ]
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Functions.Function1;
				import org.eclipse.xtext.xbase.lib.IterableExtensions;
				
				@SuppressWarnings("all")
				public class FindFirstOnIt {
				  public <T extends Object> T useExtension(final Iterable<T> it) {
				    final Function1<T,Boolean> _function = new Function1<T,Boolean>() {
				      public Boolean apply(final T it) {
				        boolean _tripleNotEquals = (it != null);
				        return Boolean.valueOf(_tripleNotEquals);
				      }
				    };
				    T _findFirst = IterableExtensions.<T>findFirst(it, _function);
				    return _findFirst;
				  }
				}
			''')
	}
	
	@Test
	def testReturnType() {
		assertCompilesTo(
			'''
				import test.ReturnTypeUsesTypeParameter
				class MyClass implements ReturnTypeUsesTypeParameter {
				
					override <LocalName extends CharSequence> accept(LocalName param) {
						null
					}
				}
			''', '''
				import test.ReturnTypeUsesTypeParameter;
				
				@SuppressWarnings("all")
				public class MyClass implements ReturnTypeUsesTypeParameter {
				  public <LocalName extends CharSequence> Inner<LocalName> accept(final LocalName param) {
				    return null;
				  }
				}
			''')
		
	}
	
	@Test
	def testReturnType_02() {
		assertCompilesTo(
			'''
				import test.ReturnTypeUsesTypeParameter
				class MyClass implements ReturnTypeUsesTypeParameter {
				
					override <LocalName extends CharSequence> accept(LocalName param) {
						[ if (true) it?.apply(param) ] 
					}
				}
			''', '''
				import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
				import test.ReturnTypeUsesTypeParameter;
				
				@SuppressWarnings("all")
				public class MyClass implements ReturnTypeUsesTypeParameter {
				  public <LocalName extends CharSequence> Inner<LocalName> accept(final LocalName param) {
				    final Inner<LocalName> _function = new Inner<LocalName>() {
				      public void useProcedure(final Procedure1<? super LocalName> it) {
				        if (true) {
				          if (it!=null) {
				            it.apply(param);
				          }
				        }
				      }
				    };
				    return _function;
				  }
				}
			''')
	}
	
	@Test 
	def void testReturnType_03() {
		'''
			import java.util.LinkedList
			
			class B extends A {
				def String client(CharSequence c) {
					client(m)
				}
				override m() {
					''
				}
			}
			
			abstract class A {
				def CharSequence m() 
			}
		'''.assertCompilesTo('''
			@SuppressWarnings("all")
			public class B extends A {
			  public String client(final CharSequence c) {
			    CharSequence _m = this.m();
			    String _client = this.client(_m);
			    return _client;
			  }
			  
			  public CharSequence m() {
			    return "";
			  }
			}
		''')
	}
	
	@Test
	def testRichStringAutoConversionToString_01(){
		assertCompilesTo(
			"class Foo { def String test() '''SomeString''' }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    return _builder.toString();
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionToString_02(){
		assertCompilesTo(
			"class Foo { def String test() { println('''SomeString''') } }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				import org.eclipse.xtext.xbase.lib.InputOutput;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    String _println = InputOutput.<String>println(_builder.toString());
				    return _println;
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionToString_03(){
		assertCompilesTo(
			"class Foo { def String test() { val x = println('''SomeString''') x } }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				import org.eclipse.xtext.xbase.lib.InputOutput;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    String _xblockexpression = null;
				    {
				      StringConcatenation _builder = new StringConcatenation();
				      _builder.append("SomeString");
				      final String x = InputOutput.<String>println(_builder.toString());
				      _xblockexpression = (x);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionToString_04(){
		assertCompilesTo(
			"class Foo { def String test() { val x = '''SomeString''' x } }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    String _xblockexpression = null;
				    {
				      StringConcatenation _builder = new StringConcatenation();
				      _builder.append("SomeString");
				      final String x = _builder.toString();
				      _xblockexpression = (x);
				    }
				    return _xblockexpression;
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionToString_05(){
		assertCompilesTo(
			"class Foo { def String test() { '''SomeString'''.toString } }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    String _string = _builder.toString();
				    return _string;
				  }
				}
			''')
	}

	@Test
	def testRichStringNoAutoConversionToString_01(){
		assertCompilesTo(
			"class Foo { def test() '''SomeString''' }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				
				@SuppressWarnings("all")
				public class Foo {
				  public CharSequence test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    return _builder;
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionDueToUnboundTypeParam_01(){
		assertCompilesTo(
			"class Foo { def test() { println('''SomeString''') } }",
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				import org.eclipse.xtext.xbase.lib.InputOutput;
				
				@SuppressWarnings("all")
				public class Foo {
				  public String test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    String _println = InputOutput.<String>println(_builder.toString());
				    return _println;
				  }
				}
			''')
	}
	
	@Test
	def testRichStringNoAutoConversionToString_02(){
		assertCompilesTo(
			"class Foo { def test(){ System::out.println('''SomeString''') } }", 
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				
				@SuppressWarnings("all")
				public class Foo {
				  public void test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    System.out.println(_builder);
				  }
				}
			''')
	}
	
	@Test
	def testRichStringAutoConversionDueToUnboundTypeParam_02(){
		assertCompilesTo(
			"class Foo { def test(){ System::out.println(println('''SomeString''')) } }", 
			'''
				import org.eclipse.xtend2.lib.StringConcatenation;
				import org.eclipse.xtext.xbase.lib.InputOutput;
				
				@SuppressWarnings("all")
				public class Foo {
				  public void test() {
				    StringConcatenation _builder = new StringConcatenation();
				    _builder.append("SomeString");
				    String _println = InputOutput.<String>println(_builder.toString());
				    System.out.println(_println);
				  }
				}
			''')
	}

	@Test
	def compileClassWithFileHeader(){
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
			
			class bar { 
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
			
			@SuppressWarnings("all")
			public class bar {
			  private String name = "foobar";
			}
		''')
	}
	
	@Test
	def compileClassInDefaultPackageWithFileHeader(){
		assertCompilesTo(''' 
			/**
			 * Xtend can't decide whether this is a header or a javadoc
			 */
			
			class bar { 
			}
		''',
		'''
			/**
			 * Xtend can't decide whether this is a header or a javadoc
			 */
			/**
			 * Xtend can't decide whether this is a header or a javadoc
			 */
			@SuppressWarnings("all")
			public class bar {
			}
		''')
	}
	
	@Test
	def compileClassInDefaultPackageWithFileHeaderAndJavaDoc(){
		assertCompilesTo(''' 
			/**
			 * header
			 */

			/**
			 * javadoc
			 */
			class bar { 
			}
		''',
		'''
			/**
			 * header
			 */
			/**
			 * javadoc
			 */
			@SuppressWarnings("all")
			public class bar {
			}
		''')
	}
	
	@Test
	def compileClassInDefaultPackageWithFileHeaderAndImport(){
		assertCompilesTo(
		''' 
			/**
			 * header
			 */
			 
			import java.util.Date
			
			class bar {
				Date d
			}
		''',
		'''
			/**
			 * header
			 */
			import java.util.Date;
			
			@SuppressWarnings("all")
			public class bar {
			  private Date d;
			}
		''')
	}
	
	@Test
	def compileClassInDefaultPackageWithJavadDocAndImport(){
		assertCompilesTo(''' 
			import java.util.Date
			
			/**
			 * javadoc
			 */
			class bar {
				Date d
			}
		''',
		'''
			import java.util.Date;
			
			/**
			 * javadoc
			 */
			@SuppressWarnings("all")
			public class bar {
			  private Date d;
			}
        ''')
    }

    @Test
    def compileAllClassesWithTheSameFileHeader(){
        val input = '''
            /**
             * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
             * All rights reserved. This program and the accompanying materials
             * are made available under the terms of the Eclipse Public License v1.0
             * which accompanies this distribution, and is available at
             * http://www.eclipse.org/legal/epl-v10.html
             */
            package foo

            class bar {
                String name = 'foobar'
            }

            class baz {
                String name = 'foobaz'
            }
        '''
        val expectedBarClass = '''
            /**
             * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
             * All rights reserved. This program and the accompanying materials
             * are made available under the terms of the Eclipse Public License v1.0
             * which accompanies this distribution, and is available at
             * http://www.eclipse.org/legal/epl-v10.html
             */
            package foo;

            @SuppressWarnings("all")
            public class bar {
              private String name = "foobar";
            }
        '''
        val expectedBazClass = '''
            /**
             * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
             * All rights reserved. This program and the accompanying materials
             * are made available under the terms of the Eclipse Public License v1.0
             * which accompanies this distribution, and is available at
             * http://www.eclipse.org/legal/epl-v10.html
             */
            package foo;

            @SuppressWarnings("all")
            public class baz {
              private String name = "foobaz";
            }
		'''
		val file = file(input.toString(), true)
		val barType = file.eResource.contents.filter(typeof(JvmDeclaredType)).head
		val bazType = file.eResource.contents.filter(typeof(JvmDeclaredType)).last
		val generatorConfig =  generatorConfigProvider.get(barType)
		var barJavaCode = generator.generateType(barType, generatorConfig);
		barJavaCode = postProcessor.postProcess(null, barJavaCode);
		var bazJavaCode = generator.generateType(bazType, generatorConfig);
		bazJavaCode = postProcessor.postProcess(null, bazJavaCode);
		XtendCompilerTest::assertEquals(expectedBarClass.toString, barJavaCode.toString);
		XtendCompilerTest::assertEquals(expectedBazClass.toString, bazJavaCode.toString);
	}

	

	@Test
	def compileImportForTypeRefInJavaDoc(){
		assertCompilesTo(
			'''
				package foo
				import java.util.List
				import java.util.ArrayList

				/**
				 *
				 * {@link List}
				 */
				class Foo {
					/**
					 *
					 * @see ArrayList
					 */
					def doStuff(){}

				}
			''',
			'''
				package foo;

				import java.util.ArrayList;
				import java.util.List;

				/**
				 * {@link List}
				 */
				@SuppressWarnings("all")
				public class Foo {
				  /**
				   * @see ArrayList
				   */
				  public Object doStuff() {
				    return null;
				  }
				}
			''')
	}

	@Test
	def compileImportForTypeRefInJavaDoc_2(){
		assertCompilesTo(
			'''
				package foo
				import java.util.List
				import java.util.ArrayList

				/**
				 *
				 * {@link List}
				 */
				class Foo {
					/**
					 * @see ArrayList
					 */
					def doStuff(ArrayList l){}

				}
			''',
			'''
				package foo;

				import java.util.ArrayList;
				import java.util.List;

				/**
				 * {@link List}
				 */
				@SuppressWarnings("all")
				public class Foo {
				  /**
				   * @see ArrayList
				   */
				  public Object doStuff(final ArrayList l) {
				    return null;
				  }
				}
			''')
	}

	@Test
	def compileImportForTypeRefInJavaDoc_3(){
		assertCompilesTo(
			'''
				package foo
				import java.util.List
				import java.util.ArrayList

				class Foo {
					/**
					 * @see ArrayList
					 */
					List<String> list = null

				}
			''',
			'''
				package foo;

				import java.util.ArrayList;
				import java.util.List;

				@SuppressWarnings("all")
				public class Foo {
				  /**
				   * @see ArrayList
				   */
				  private List<String> list = null;
				}
			''')
	}

	@Test
	def compileImportForTypeRefInJavaDoc_Same_Package(){
		assertCompilesTo(
			'''
				package foo

				/**
				 *
				 * {@link Bar}
				 */
				class Foo {
				}
				class Bar{}
			''',
			'''
				package foo;

				/**
				 * {@link Bar}
				 */
				@SuppressWarnings("all")
				public class Foo {
				}
			''')
	}
	
	@Test
	def compilePackagePrivateClass() {
		assertCompilesTo('''
			package class Foo {
			}
		''', '''
			@SuppressWarnings("all")
			class Foo {
			}
		''')
	}

	@Test
	def compilePackagePrivateInterface() {
		assertCompilesTo('''
			package interface Foo {
			}
		''', '''
			@SuppressWarnings("all")
			interface Foo {
			}
		''')
	}

	@Test
	def compilePackagePrivateEnum() {
		assertCompilesTo('''
			package enum Foo {
			}
		''', '''
			@SuppressWarnings("all")
			enum Foo {
			}
		''')
	}

	@Test
	def compilePackagePrivateAnnotationType() {
		assertCompilesTo('''
			package annotation Foo {
			}
		''', '''
			@interface Foo {
			}
		''')
	}
	
	@Test def testNullSafeFeatureCall_07() {
		assertCompilesTo(
				'''
					class Foo { 
						extension org.eclipse.xtext.xbase.lib.util.ReflectExtensions
						def bar() throws Throwable {
							new String().get('toString')?.get('substring')
						}
					}
				''', '''
					import org.eclipse.xtext.xbase.lib.Extension;
					import org.eclipse.xtext.xbase.lib.util.ReflectExtensions;
					
					@SuppressWarnings("all")
					public class Foo {
					  @Extension
					  private ReflectExtensions _reflectExtensions;
					  
					  public Object bar() throws Throwable {
					    Object _get = null;
					    String _string = new String();
					    Object _get_1 = this._reflectExtensions.<Object>get(_string, "toString");
					    if (_get_1!=null) {
					      _get=this._reflectExtensions.<Object>get(_get_1, "substring");
					    }
					    return _get;
					  }
					}
				''');
	}
	
	@Test def testNullSafeFeatureCall_08() {
		assertCompilesTo(
				'''
					class Foo { 
						String field
						def bar(String str) {
							new Foo().field?.toString?.substring(1) ?: ''
						}
					}
				''', '''
					import org.eclipse.xtext.xbase.lib.ObjectExtensions;
					
					@SuppressWarnings("all")
					public class Foo {
					  private String field;
					  
					  public String bar(final String str) {
					    String _elvis = null;
					    Foo _foo = new Foo();
					    String _field = _foo.field;
					    String _string = null;
					    if (_field!=null) {
					      _string=_field.toString();
					    }
					    String _substring = null;
					    if (_string!=null) {
					      _substring=_string.substring(1);
					    }
					    if (_substring != null) {
					      _elvis = _substring;
					    } else {
					      _elvis = ObjectExtensions.<String>operator_elvis(_substring, "");
					    }
					    return _elvis;
					  }
					}
				''');
	}
	
	@Test def testNullSafeFeatureCall_09() {
		assertCompilesTo(
				'''
					class BlockExpression {
					   def foo(String source) {
					        val x = { "" }?.toString
					        val y = {{{ "" }}}?.toString
					        val z = {{{ var i = 1; "" + i }}}?.toString
					   }
					}
				''', '''
					@SuppressWarnings("all")
					public class BlockExpression {
					  public void foo(final String source) {
					    String _string = null;
					    if (""!=null) {
					      _string="".toString();
					    }
					    final String x = _string;
					    String _string_1 = null;
					    if (""!=null) {
					      _string_1="".toString();
					    }
					    final String y = _string_1;
					    String _xblockexpression = null;
					    {
					      int i = 1;
					      _xblockexpression = (("" + Integer.valueOf(i)));
					    }
					    String _string_2 = null;
					    if (_xblockexpression!=null) {
					      _string_2=_xblockexpression.toString();
					    }
					    final String z = _string_2;
					  }
					}
				''');
	}
	
	@Test def testNullSafeFeatureCall_10() {
		assertCompilesTo(
				'''
					class StrangeBug {
					    def getAnzuwendendeModi(String source) {
						    {
						        if (true)
						            <String>newArrayList()
						        else {
						            <String>newHashSet()
						        }
						    }?.toList
					    }
					}
				''', '''
					import java.util.AbstractCollection;
					import java.util.ArrayList;
					import java.util.HashSet;
					import java.util.List;
					import org.eclipse.xtext.xbase.lib.CollectionLiterals;
					import org.eclipse.xtext.xbase.lib.IterableExtensions;
					
					@SuppressWarnings("all")
					public class StrangeBug {
					  public List<String> getAnzuwendendeModi(final String source) {
					    List<String> _list = null;
					    AbstractCollection<String> _xifexpression = null;
					    if (true) {
					      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
					      _xifexpression = _newArrayList;
					    } else {
					      HashSet<String> _newHashSet = CollectionLiterals.<String>newHashSet();
					      _xifexpression = _newHashSet;
					    }
					    if (_xifexpression!=null) {
					      _list=IterableExtensions.<String>toList(_xifexpression);
					    }
					    return _list;
					  }
					}
				''');
	}
	
	@Test def void testBug412642() throws Exception {
		'''
			package org.xtext.example.mydsl

			class Bug412642 {
			
				val pairField = "Orange" -> 3
			
				def bar() {
					val pair = "Apple" -> 1
					val _pair = pair;
					val map2 = #{
						pair,
						_pair,
						"Banana" -> 2,
						Bug412642_2.staticPair,
						pairField,
						Bug412642_2.methodStaticPair,
						1 + 2
					}
					
					val map3 = #{
						1 * 2,
						"Banana" -> 2
					}
				}
				
				def Pair<Object, Object> operator_plus(int operant, int operand2) {
					
				}
			
			}
			
			class Bug412642_2 {
			
				val public static staticPair = "Mango" -> 4
				
				def static methodStaticPair () {
					staticPair
				}
			
			}
		'''.assertCompilesTo('''
			package org.xtext.example.mydsl;

			import com.google.common.collect.Maps;
			import com.google.common.collect.Sets;
			import java.util.Collections;
			import java.util.Map;
			import java.util.Set;
			import org.eclipse.xtext.xbase.lib.Functions.Function0;
			import org.eclipse.xtext.xbase.lib.Pair;
			import org.xtext.example.mydsl.Bug412642_2;
			
			@SuppressWarnings("all")
			public class Bug412642 {
			  private final Pair<String,Integer> pairField = new Function0<Pair<String,Integer>>() {
			    public Pair<String,Integer> apply() {
			      Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Orange", Integer.valueOf(3));
			      return _mappedTo;
			    }
			  }.apply();
			  
			  public void bar() {
			    final Pair<String,Integer> pair = Pair.<String, Integer>of("Apple", Integer.valueOf(1));
			    final Pair<String,Integer> _pair = pair;
			    Map<Object,Object> _xsetliteral = null;
			    Pair<String,Integer> _methodStaticPair = Bug412642_2.methodStaticPair();
			    Pair<Object,Object> _plus = this.operator_plus(
			      1, 2);
			    Map<Object,Object> _tempMap = Maps.<Object, Object>newHashMap();
			    _tempMap.put(pair == null ? null : pair.getKey(), pair == null ? null : pair.getValue());
			    _tempMap.put(_pair == null ? null : _pair.getKey(), _pair == null ? null : _pair.getValue());
			    _tempMap.put("Banana", Integer.valueOf(2));
			    _tempMap.put(Bug412642_2.staticPair == null ? null : Bug412642_2.staticPair.getKey(), Bug412642_2.staticPair == null ? null : Bug412642_2.staticPair.getValue());
			    _tempMap.put(this.pairField == null ? null : this.pairField.getKey(), this.pairField == null ? null : this.pairField.getValue());
			    _tempMap.put(_methodStaticPair == null ? null : _methodStaticPair.getKey(), _methodStaticPair == null ? null : _methodStaticPair.getValue());
			    _tempMap.put(_plus == null ? null : _plus.getKey(), _plus == null ? null : _plus.getValue());
			    _xsetliteral = Collections.<Object, Object>unmodifiableMap(_tempMap);
			    final Map<Object,Object> map2 = _xsetliteral;
			    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Banana", Integer.valueOf(2));
			    final Set<? extends Object> map3 = Collections.<Object>unmodifiableSet(Sets.<Object>newHashSet((1 * 2), _mappedTo));
			  }
			  
			  public Pair<Object,Object> operator_plus(final int operant, final int operand2) {
			    return null;
			  }
			}
		''')
	}
	
	@Test(timeout = 20000)
	def void testBug412853 () {
		'''
			class Bug {
				def dispatch bug(A a) { 1 }
				def dispatch bug(B b) { 1 }
			}
			
			interface Element<T> {}
			class AbstractElement<T> implements Element<T> {}
			class A extends AbstractElement<A> implements Element<A> {}
			class B implements Element<B> {}
		'''.assertCompilesTo('''
			import java.util.Arrays;
			
			@SuppressWarnings("all")
			public class Bug {
			  protected int _bug(final A a) {
			    return 1;
			  }
			  
			  protected int _bug(final B b) {
			    return 1;
			  }
			  
			  public int bug(final Element<? extends Object> a) {
			    if (a instanceof A) {
			      return _bug((A)a);
			    } else if (a instanceof B) {
			      return _bug((B)a);
			    } else {
			      throw new IllegalArgumentException("Unhandled parameter types: " +
			        Arrays.<Object>asList(a).toString());
			    }
			  }
			}
		''')
	}
	
	/**
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=412363
	 */
	@Test
	def void testEmptyCollectionLiterals() {
		'''
			import java.util.List
			import java.util.Set
			
			class EmptyCollections {
			  
			  def emptyList() {
			    val List<String> result = #[];  // Erroneous type error here
			    result;
			  }
			  
			  def List<String> emptyList2 () {
			    #[]  // Erroneous type error here
			  }
			  
			  def emptySet() {
			    val Set<String> result = #{};  // Erroneous type error here
			    result;
			  }
			  
			  def Set<String> emptySet2 () {
			    #{} // Erroneous type error here
			  }
			  
			}
		'''.assertCompilesTo('''
			import com.google.common.collect.Lists;
			import com.google.common.collect.Sets;
			import java.util.Collections;
			import java.util.List;
			import java.util.Set;
			
			@SuppressWarnings("all")
			public class EmptyCollections {
			  public List<String> emptyList() {
			    List<String> _xblockexpression = null;
			    {
			      final List<String> result = Collections.<String>unmodifiableList(Lists.<String>newArrayList());
			      _xblockexpression = (result);
			    }
			    return _xblockexpression;
			  }
			  
			  public List<String> emptyList2() {
			    return Collections.<String>unmodifiableList(Lists.<String>newArrayList());
			  }
			  
			  public Set<String> emptySet() {
			    Set<String> _xblockexpression = null;
			    {
			      final Set<String> result = Collections.<String>unmodifiableSet(Sets.<String>newHashSet());
			      _xblockexpression = (result);
			    }
			    return _xblockexpression;
			  }
			  
			  public Set<String> emptySet2() {
			    return Collections.<String>unmodifiableSet(Sets.<String>newHashSet());
			  }
			}
		''')
	}

}

//class XtendCompilerTest extends AbstractXtendCompilerTest {
//	
//	/*
//	 * Refined questionable expectation.
//	 */
//	@Test
//	override testRichStringNoAutoConversionToString_03(){
//		assertCompilesTo(
//			"class Foo { def test(){ System::out.println('''SomeString''') } }", 
//			'''
//				import org.eclipse.xtend2.lib.StringConcatenation;
//				
//				@SuppressWarnings("all")
//				public class Foo {
//				  public void test() {
//				    StringConcatenation _builder = new StringConcatenation();
//				    _builder.append("SomeString");
//				    System.out.println(_builder.toString());
//				  }
//				}
//			''')
//	}
//	
//	/*
//	 * Refined questionable expectation.
//	 */
//	@Test
//	override testRichStringNoAutoConversionToString_04(){
//		assertCompilesTo(
//			"class Foo { def test(){ System::out.println(println('''SomeString''')) } }", 
//			'''
//				import org.eclipse.xtend2.lib.StringConcatenation;
//				import org.eclipse.xtext.xbase.lib.InputOutput;
//				
//				@SuppressWarnings("all")
//				public class Foo {
//				  public void test() {
//				    StringConcatenation _builder = new StringConcatenation();
//				    _builder.append("SomeString");
//				    String _println = InputOutput.<String>println(_builder.toString());
//				    System.out.println(_println);
//				  }
//				}
//			''')
//	}
//	
//}
