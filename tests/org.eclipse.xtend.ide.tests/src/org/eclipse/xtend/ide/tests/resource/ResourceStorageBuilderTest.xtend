/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.resource

import com.google.inject.Inject
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IStorage
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper
import org.eclipse.xtext.builder.trace.StorageAwareTrace
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil
import org.eclipse.xtext.resource.persistence.SourceLevelURIsAdapter
import org.eclipse.xtext.resource.persistence.StorageAwareResource
import org.eclipse.xtext.ui.resource.IResourceSetProvider
import org.eclipse.xtext.ui.resource.IStorage2UriMapper
import org.junit.Test
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.xtext.ui.editor.SchedulingRuleFactory
import com.google.common.base.Throwables

/**
 * @author Sven Efftinge - Initial contribution and API
 */
class ResourceStorageTest extends AbstractXtendUITestCase {
	
	@Inject extension WorkbenchTestHelper helper
	
	@Inject IResourceSetProvider resourceSetProvider
	@Inject IStorage2UriMapper uriMapper
	
	@Test def void testLoadFromStorage() {
		val file = 'src/mypack/MyClass.xtend'.createFile('''
			package mypack
			
			class MyClass {
				public def void foo() {
				}
			}
		''')
		val file2 = 'src/mypack/OtherClass.xtend'.createFile('''
			package mypack
			
			class OtherClass extends MyClass {
				override foo() {
				}
			}
		''')
		
		IResourcesSetupUtil.waitForBuild
		
		val uri = uriMapper.getUri(file)
		val uri2 = uriMapper.getUri(file2)
		
		val resourceSet = resourceSetProvider.get(file.project)
		SourceLevelURIsAdapter.setSourceLevelUris(resourceSet, #[])
		
		val resource2 = resourceSet.getResource(uri2, true) as StorageAwareResource
		assertTrue(resource2.isLoadedFromStorage)
		EcoreUtil.resolveAll(resource2)
		assertEquals("mypack.OtherClass", resource2.resourceDescription.exportedObjects.map[name.toString].join(','))
		
		val resource = resourceSet.getResource(uri, false) as StorageAwareResource
		assertTrue(resource.isLoadedFromStorage)
		assertEquals("mypack.MyClass", resource.resourceDescription.exportedObjects.map[name.toString].join(','))
	}
	
	@Test def void testShouldLoadFromStorage() {
		val file = 'mypack/MyClass.xtend'.createFile('''
			package mypack
			
			class MyClass {
				public def void foo() {
				}
			}
		''')
		IResourcesSetupUtil.waitForBuild

		val uri = uriMapper.getUri(file)
		
		val resourceSet = resourceSetProvider.get(file.project)
		SourceLevelURIsAdapter.setSourceLevelUris(resourceSet, #[])
	
		val resource = resourceSet.createResource(uri) as StorageAwareResource
		
		doWorkInJob[
			assertTrue(resource.resourceStorageFacade.shouldLoadFromStorage(resource))
		]
		
		file.delete(true, new NullProgressMonitor)
		IResourcesSetupUtil.waitForBuild
		
		doWorkInJob[
			assertFalse(resource.resourceStorageFacade.shouldLoadFromStorage(resource))
		]
	}
	
	protected def void doWorkInJob(()=>void work) {
		val throwables = newArrayList
		val testShouldLoadFromStorageJob = new Job('''�class.name�.TestJob''') {

			override protected run(IProgressMonitor monitor) {
				try {
					work.apply
				} catch (Throwable t) {
					throwables += t
				}
				Status.OK_STATUS
			}

		} => [
			rule = SchedulingRuleFactory.INSTANCE.newSequence
		]
		testShouldLoadFromStorageJob.schedule
		testShouldLoadFromStorageJob.join
		val t = throwables.head
		if (t != null) {
			Throwables.propagate(t)
		}
	}

	@Test
	def void testDecodeURI(){
		val file = 'mypack/MyClass Foo.xtend'.createFile('''
			package mypack

			class Foo {
				public def void foo() {
				}
			}
		''')
		IResourcesSetupUtil.waitForBuild
		val storageAwareTrace = new TestableStorageAwareTrace()
		injector.injectMembers(storageAwareTrace)
		storageAwareTrace.localStorage = file
		var result = storageAwareTrace.resolvePath(URI.createURI('mypack/MyClass%20Foo.xtend'))
		assertEquals("platform:/resource/test.project/src/mypack/MyClass%20Foo.xtend",result.toString)
		result = storageAwareTrace.resolvePath(project, URI.createURI('src/mypack/MyClass%20Foo.xtend'))
		assertEquals("platform:/resource/test.project/src/mypack/MyClass%20Foo.xtend",result.toString)
	}


	static class TestableStorageAwareTrace extends StorageAwareTrace {

		override public setLocalStorage(IStorage derivedResource) {
			super.setLocalStorage(derivedResource)
		}

		override public resolvePath(IProject project, URI path) {
			super.resolvePath(project, path)
		}

		override public resolvePath(URI path) {
			super.resolvePath(path)
		}

	}

}