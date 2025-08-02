/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource.ignorecase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.DescriptionUtils;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.AbstractContainer;
import org.eclipse.xtext.resource.impl.AbstractResourceDescription;
import org.eclipse.xtext.resource.impl.ChangedResourceDescriptionDelta;
import org.eclipse.xtext.resource.impl.DefaultResourceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionDelta;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionManager;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class DefaultResourceDescriptionManagerTest extends Assert {

	private Resource resource;
	private DefaultResourceDescription resourceDescription;
	private DefaultResourceDescriptionManager manager;
	private Collection<QualifiedName> importedNames;

	@Before
	public void setUp() throws Exception {
		EObject copy = EcoreUtil.copy(EcorePackage.eINSTANCE);
		resource = new ResourceImpl();
		resource.getContents().add(copy);
		IQualifiedNameProvider nameProvider = new IQualifiedNameProvider.AbstractImpl() {
			@Override
			public QualifiedName getFullyQualifiedName(EObject obj) {
				if (obj instanceof ENamedElement)
					return QualifiedName.create(((ENamedElement) obj).getName());
				return null;
			}
		};
		DefaultResourceDescriptionStrategy descriptionStrategy = new DefaultResourceDescriptionStrategy();
		descriptionStrategy.setQualifiedNameProvider(nameProvider);
		resourceDescription = new DefaultResourceDescription(resource, descriptionStrategy) {
			@Override
			public Iterable<QualifiedName> getImportedNames() {
				return importedNames;
			}
		};
		manager = new DefaultResourceDescriptionManager();
		importedNames = Collections.emptySet();
	}
	
	@Test public void testIsAffected() {
		DefaultResourceDescriptionDelta delta = new DefaultResourceDescriptionDelta(null, resourceDescription);
		assertFalse(manager.isAffected(delta, resourceDescription));
		importedNames = Collections.singleton(QualifiedName.create("eclass"));
		assertTrue(manager.isAffected(delta, resourceDescription));
		importedNames = Collections.singleton(QualifiedName.create("ECLASS"));
		assertFalse(manager.isAffected(delta, resourceDescription));
	}
	
	@Test public void testIsAffectedByDeltas() {
		manager.setDescriptionUtils(new DescriptionUtils());

		class DescriptionForTesting extends AbstractResourceDescription {
			private final URI uri;
			private final List<QualifiedName> importedNames;
			private final List<QualifiedName> exportedNames;

			DescriptionForTesting(String path) {
				this.uri = URI.createPlatformResourceURI(path, true);
				this.importedNames = new ArrayList<>();
				this.exportedNames = new ArrayList<>();
			}

			@Override
			public List<QualifiedName> getImportedNames() {
				return importedNames;
			}

			@Override
			public List<IReferenceDescription> getReferenceDescriptions() {
				return Collections.emptyList();
			}

			@Override
			public URI getURI() {
				return uri;
			}

			@Override
			protected List<IEObjectDescription> computeExportedObjects() {
				return Lists.transform(exportedNames, name -> EObjectDescription.create(name, null));
			}
		}
		
		DescriptionForTesting notPresentInContainer = new DescriptionForTesting("a");
		DescriptionForTesting presentInContainer = new DescriptionForTesting("b");
		DescriptionForTesting candidate = new DescriptionForTesting("c");
		QualifiedName name = QualifiedName.create("some", "name");
		candidate.importedNames.add(name);
		notPresentInContainer.exportedNames.add(name);
		presentInContainer.exportedNames.add(name);

		// Mock all the things
		IContainer container = new AbstractContainer() {
			@Override
			public Iterable<IResourceDescription> getResourceDescriptions() {
				return Arrays.asList(presentInContainer, candidate);
			}

		};
		manager.setContainerManager(new IContainer.Manager() {
			@Override
			public List<IContainer> getVisibleContainers(IResourceDescription desc,
					IResourceDescriptions resourceDescriptions) {
				return Collections.singletonList(container);
			}

			@Override
			public IContainer getContainer(IResourceDescription desc, IResourceDescriptions resourceDescriptions) {
				return container;
			}
		});

		IResourceDescription.Delta notPresentDelta = new ChangedResourceDescriptionDelta(null, notPresentInContainer);
		IResourceDescription.Delta presentDelta = new ChangedResourceDescriptionDelta(null, presentInContainer);

		assertTrue(manager.isAffected(
				Arrays.asList(notPresentDelta, presentDelta),
				candidate,
				null /* unused by the mocked container manager */));

		assertTrue(manager.isAffected(presentDelta, candidate));
		// Assert the ill-defined contract for isAffected(Delta, Candidate)
		assertTrue(manager.isAffected(notPresentDelta, candidate));

		presentInContainer.exportedNames.clear();

		assertFalse(manager.isAffected(
				Arrays.asList(notPresentDelta, presentDelta),
				candidate,
				null /* unused by the mocked container manager */));

		assertFalse(manager.isAffected(presentDelta, candidate));
	}
	
}
