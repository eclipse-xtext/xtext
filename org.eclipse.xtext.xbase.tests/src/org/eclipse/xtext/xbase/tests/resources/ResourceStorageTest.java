/**
 * Copyright (c) 2016, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ExtensibleURIConverterImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.TypesPackage;
import org.eclipse.xtext.nodemodel.BidiTreeIterator;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.persistence.IResourceStorageFacade;
import org.eclipse.xtext.resource.persistence.ResourceStorageLoadable;
import org.eclipse.xtext.resource.persistence.StorageAwareResource;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.resource.BatchLinkableResourceStorageFacade;
import org.eclipse.xtext.xbase.tests.jvmmodel.AbstractJvmModelTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Test for {@link BatchLinkableResourceStorageFacade} Node Model Serialization
 * 
 * @author Christian Dietrich - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(ResourceStorageTest.XbaseWithResourceStorageFacadeInjectorProvider.class)
public class ResourceStorageTest extends AbstractJvmModelTest {
	public static class XbaseWithResourceStorageFacadeInjectorProvider extends AbstractJvmModelTest.SimpleJvmModelTestInjectorProvider {
		@Override
		protected Injector internalCreateInjector() {
			return new XbaseTestWithResourceStorageFacadeStandaloneSetup().createInjectorAndDoEMFRegistration();
		}
	}

	public static class XbaseTestWithResourceStorageFacadeStandaloneSetup
			extends AbstractJvmModelTest.SimpleJvmModelTestInjectorProvider.SimpleJvmModelTestStandaloneSetup {
		@Override
		public Injector createInjector() {
			return Guice.createInjector(new XbaseTestWithResourceStorageFacadeRuntimeModule());
		}
	}

	public static class XbaseTestWithResourceStorageFacadeRuntimeModule extends AbstractJvmModelTest.SimpleJvmModelInferrerRuntimeModule {
		public Class<? extends IResourceStorageFacade> bindIResourceStorageFacade() {
			return BatchLinkableResourceStorageFacade.class;
		}
	}

	public static class InMemoryURIConverter extends ExtensibleURIConverterImpl {
		private final Map<URI, InputStream> models = new HashMap<URI, InputStream>();

		public void addModel(String uri, String content) {
			models.put(URI.createURI(uri), new StringInputStream(content));
		}

		@Override
		public boolean exists(URI uri, Map<?, ?> options) {
			boolean result = models.containsKey(uri);
			if (!result) {
				for (URI knownUri : models.keySet()) {
					if (uri.toString().endsWith(knownUri.toString())) {
						return true;
					}
				}
			}
			return result;
		}

		@Override
		public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
			return models.get(uri);
		}
	}

	@Inject
	private BatchLinkableResourceStorageFacade resourceStorageFacade;

	@Inject
	private JvmModelAssociator associator;
	
	@Test
	public void testWriteAndLoad() throws Exception {
		StringBuilder model = new StringBuilder();
		// @formatter:off
		model.append("{\n");
		model.append("	var x = \"Hello\"\n");
		model.append("	var y = \"\"\n");
		model.append("	val it = x\n");
		model.append("	y = length.toString\n");
		model.append("	println(x)\n");
		model.append("	y = length.toString\n");
		model.append("	println(x)\n");
		model.append("	y = length.toString\n");
		model.append("	println(x)\n");
		model.append("}");
		// @formatter:on
		String contents = model.toString();
		XExpression file = this.expression(contents);
		StorageAwareResource originalResource = ((StorageAwareResource) file.eResource());
		JvmModelAssociator.Adapter originalAdapter = ((JvmModelAssociator.Adapter) EcoreUtil.getExistingAdapter(originalResource, JvmModelAssociator.Adapter.class));
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		resourceStorageFacade.setStoreNodeModel(true);
		resourceStorageFacade.createResourceStorageWritable(bout).writeResource(originalResource);
		ResourceStorageLoadable in = resourceStorageFacade.createResourceStorageLoadable(new ByteArrayInputStream(bout.toByteArray()));
		StorageAwareResource resource = (StorageAwareResource) file.eResource().getResourceSet().createResource(URI.createURI("synthetic:/Test.___xbase"));
		InMemoryURIConverter converter = new InMemoryURIConverter();
		converter.addModel(resource.getURI().toString(), contents);
		resource.getResourceSet().setURIConverter(converter);
		file.eResource().getResourceSet().getResources().add(resource);
		resource.loadFromStorage(in);
		XExpression root = (XExpression) resource.getContents().get(0);
		Assert.assertTrue(root instanceof XBlockExpression);
		BidiTreeIterator<INode> restoredNodes = NodeModelUtils.findActualNodeFor(IterableExtensions.<EObject> head(resource.getContents()))
				.getAsTreeIterable().iterator();
		BidiTreeIterator<INode> originalNodes = NodeModelUtils.findActualNodeFor(file).getAsTreeIterable().iterator();
		while (restoredNodes.hasNext()) {
			INode rest = restoredNodes.next();
			INode orig = originalNodes.next();
			Assert.assertEquals(orig.getStartLine(), rest.getStartLine());
			Assert.assertEquals(orig.getEndLine(), rest.getEndLine());
			Assert.assertEquals(orig.getOffset(), rest.getOffset());
			Assert.assertEquals(orig.getEndOffset(), rest.getEndOffset());
			Assert.assertEquals(orig.getLength(), rest.getLength());
			Assert.assertEquals(orig.getTotalStartLine(), rest.getTotalStartLine());
			Assert.assertEquals(orig.getTotalEndLine(), rest.getTotalEndLine());
			Assert.assertEquals(orig.getTotalOffset(), rest.getTotalOffset());
			Assert.assertEquals(orig.getTotalEndOffset(), rest.getTotalEndOffset());
			Assert.assertEquals(orig.getTotalLength(), rest.getTotalLength());
			Assert.assertSame(orig.getGrammarElement(), rest.getGrammarElement());
			Assert.assertTrue(orig.getSemanticElement() != null && rest.getSemanticElement() != null
					|| orig.getSemanticElement() == null && rest.getSemanticElement() == null);
			if (orig.getSemanticElement() != null) {
				Assert.assertEquals(file.eResource().getURIFragment(orig.getSemanticElement()),
						resource.getURIFragment(rest.getSemanticElement()));
			}
			Assert.assertEquals(orig.getText(), rest.getText());
		}
		Assert.assertFalse(originalNodes.hasNext());
		JvmModelAssociator.Adapter restoredAdapter = (JvmModelAssociator.Adapter) EcoreUtil.getExistingAdapter(resource, JvmModelAssociator.Adapter.class);
		Assert.assertEquals(originalAdapter.logicalContainerMap.size(), restoredAdapter.logicalContainerMap.size());
		Assert.assertEquals(originalAdapter.sourceToTargetMap.size(), restoredAdapter.sourceToTargetMap.size());
		Assert.assertEquals(originalAdapter.targetToSourceMap.size(), restoredAdapter.targetToSourceMap.size());
	}
	
	@Test
	public void testWriteAndLoadWithMissing() throws Exception {
		String code = "{}";
		XExpression file = this.expression(code);

		assertEquals(2, file.eResource().getContents().size()); // Jvm model loaded

		JvmGenericType type = ((JvmGenericType) file.eResource().getContents().get(1));
		// add a synthetic constructor 
		JvmConstructor jvmConstructor = TypesPackage.eINSTANCE.getTypesFactory().createJvmConstructor();
		jvmConstructor.setSimpleName("Test");

		type.getMembers().add(jvmConstructor);
		associator.associate(file.eResource().getContents().get(0), jvmConstructor);

		StorageAwareResource originalResource = ((StorageAwareResource) file.eResource());
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		resourceStorageFacade.setStoreNodeModel(false);
		resourceStorageFacade.createResourceStorageWritable(bout).writeResource(originalResource);
		
		// Remove synthetic constructor to cause an addition of a "none" fragment
		type.getMembers().remove(jvmConstructor);
		bout = new ByteArrayOutputStream();
		resourceStorageFacade.createResourceStorageWritable(bout).writeResource(originalResource);

		ResourceSet resSet = file.eResource().getResourceSet();
		originalResource.unload();

		// Load broken associations
		ResourceStorageLoadable in = resourceStorageFacade.createResourceStorageLoadable(new ByteArrayInputStream(bout.toByteArray()));
		StorageAwareResource resource = (StorageAwareResource) resSet.createResource(URI.createURI("synthetic:/Test.___xbase"));
		InMemoryURIConverter converter = new InMemoryURIConverter();
		converter.addModel(resource.getURI().toString(), code);
		resource.getResourceSet().setURIConverter(converter);
		resSet.getResources().add(resource);
		resource.loadFromStorage(in);

		resource.unload();
		resource.load(null);
		// Write broken associations
		resourceStorageFacade.createResourceStorageWritable(bout).writeResource(resource);
	}

	@Test
	public void testFilterRejectionInAssociationsFallsBackToSource() throws Exception {
		String code = "{ 1 + 2 }";
		XExpression file = this.expression(code);
		StorageAwareResource originalResource = (StorageAwareResource) file.eResource();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		resourceStorageFacade.createResourceStorageWritable(bout).writeResource(originalResource);
		byte[] corrupted = replaceAssociations(bout.toByteArray());

		StorageAwareResource resource = (StorageAwareResource) file.eResource().getResourceSet()
				.createResource(URI.createURI("synthetic:/Test.___xbase"));
		InMemoryURIConverter converter = new InMemoryURIConverter();
		converter.addModel(resource.getURI().toString(), code);
		resource.getResourceSet().setURIConverter(converter);
		setResourceStorageFacade(resource, new FailingStorageFacade(corrupted));

		resource.load(null);

		Assert.assertFalse(resource.isLoadedFromStorage());
		Assert.assertNull(resource.getResourceDescription());
		Assert.assertFalse(resource.getContents().isEmpty());
	}

	private void setResourceStorageFacade(StorageAwareResource resource, IResourceStorageFacade facade)
			throws Exception {
		Field field = StorageAwareResource.class.getDeclaredField("resourceStorageFacade");
		field.setAccessible(true);
		field.set(resource, facade);
	}

	private byte[] replaceAssociations(byte[] storage) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(storage));
				ZipOutputStream output = new ZipOutputStream(result)) {
			ZipEntry entry;
			while ((entry = input.getNextEntry()) != null) {
				output.putNextEntry(new ZipEntry(entry.getName()));
				if ("associations".equals(entry.getName())) {
					ObjectOutputStream objects = new ObjectOutputStream(output);
					objects.writeObject(new Date());
					objects.flush();
				} else {
					input.transferTo(output);
				}
				output.closeEntry();
			}
		}
		return result.toByteArray();
	}

	private static class FailingStorageFacade extends BatchLinkableResourceStorageFacade {
		private final byte[] storage;

		FailingStorageFacade(byte[] storage) {
			this.storage = storage;
		}

		@Override
		public boolean shouldLoadFromStorage(StorageAwareResource resource) {
			return true;
		}

		@Override
		public ResourceStorageLoadable getOrCreateResourceStorageLoadable(StorageAwareResource resource) {
			return createResourceStorageLoadable(new ByteArrayInputStream(storage));
		}
	}
}
