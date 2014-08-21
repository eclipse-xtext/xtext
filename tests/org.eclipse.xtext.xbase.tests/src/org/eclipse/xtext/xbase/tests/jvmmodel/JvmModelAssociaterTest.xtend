package org.eclipse.xtext.xbase.tests.jvmmodel

import com.google.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.resource.DerivedStateAwareResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.junit.Test
import com.google.inject.MembersInjector
import org.junit.Before
import org.junit.After

class JvmModelAssociaterTest extends AbstractJvmModelTest {
	
	@Inject extension JvmTypesBuilder
	@Inject MembersInjector<JvmModelAssociator> associatorInjector
	JvmModelAssociator assoc
	@Inject XtextResourceSet resourceSet
	@Inject DerivedStateAwareResource resource
	
	@Before
	public def void createAssociator() {
		assoc = new JvmModelAssociator
		associatorInjector.injectMembers(assoc)
	}
	
	@After
	public def void discardAssociator() {
		assoc = null
	}
	
	@Test
	def void testInference() {
		assoc.inferrer = [obj, acceptor, preIndexing|
			val firstType = obj.toClass('foo.Bar')
			val secondType = obj.toClass('foo.Baz') 
			assertNull(secondType.eResource)
			acceptor.accept(firstType) [
				^abstract = true
				assertNotNull(firstType.eResource)
				assertNotNull(secondType.eResource)
			]
			acceptor.accept(secondType) [
				^abstract = true
				assertNotNull(firstType.eResource)
			]
		]
		resource.setDerivedStateComputer(null)
		resource.URI = URI::createURI('foo.txt')
		resourceSet.classpathURIContext = getClass()
		resourceSet.resources += resource
		resource.contents += EcoreFactory::eINSTANCE.createEClass
		assoc.installDerivedState(resource,true)
		assertFalse((resource.contents.get(1) as JvmDeclaredType).^abstract)
		
		resource.contents.clear
		resource.contents += EcoreFactory::eINSTANCE.createEClass
		assoc.installDerivedState(resource,false)
		val type = (resource.contents.get(1) as JvmGenericType)
		assertTrue(type.^abstract)
		
		// test extends object and default constructor
		assertEquals(1, type.members.filter(typeof(JvmConstructor)).size)
		assertEquals("java.lang.Object", type.superTypes.head?.qualifiedName)
	}	
}

