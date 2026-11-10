/*******************************************************************************
 * Copyright (c) 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.builder.impl;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IStorage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.util.Pair;
import org.eclipse.xtext.util.Tuples;
import org.junit.Test;

/**
 * A canceled build must not lose per-project queued URIs: the checkpoint has to
 * capture the queue contents, not just the queue references.
 *
 * See https://github.com/eclipse-xtext/xtext/issues/XXXX - flaky
 * {@code SimpleProjectsIntegrationTest.testOpenAndCloseReferencedProjects}.
 */
public class QueuedBuildDataRollbackTest {

	@Test
	public void testRollbackRestoresDrainedPerProjectQueue() {
		URI uri = URI.createPlatformResourceURI("/bar/src/Bar.buildertestlanguage", true);
		QueuedBuildData data = newBuildData("bar", uri);
		assertEquals(1, data.getQueue("bar").size());

		data.createCheckpoint();
		Queue<URI> queue = data.getQueue("bar");
		assertEquals(uri, queue.poll());
		assertTrue(queue.isEmpty());

		data.rollback();

		assertEquals("rollback must restore the drained per-project queue", 1, data.getQueue("bar").size());
		assertEquals(uri, data.getQueue("bar").peek());
	}

	@Test
	public void testRollbackRestoresDrainedGlobalQueue() {
		URI uri = URI.createPlatformResourceURI("/bar/src/Bar.buildertestlanguage", true);
		QueuedBuildData data = new QueuedBuildData(new EmptyMapper(),
				new QueuedBuildData.NullContribution());
		data.queueURI(uri);
		assertEquals(1, data.getQueue("unknownProject").size());

		data.createCheckpoint();
		assertEquals(uri, data.getQueue("unknownProject").poll());

		data.rollback();

		assertEquals(1, data.getQueue("unknownProject").size());
	}

	@Test
	public void testRollbackKeepsQueuedDataWithoutDrain() {
		URI uri = URI.createPlatformResourceURI("/bar/src/Bar.buildertestlanguage", true);
		QueuedBuildData data = newBuildData("bar", uri);

		data.createCheckpoint();
		data.rollback();

		assertEquals(1, data.getQueue("bar").size());
		assertEquals(uri, data.getQueue("bar").peek());
	}

	private static QueuedBuildData newBuildData(String projectName, URI... uris) {
		IProject project = newProject(projectName);
		IStorage2UriMapper mapper = new IStorage2UriMapper() {
			@Override
			public Iterable<Pair<IStorage, IProject>> getStorages(URI uri) {
				return Collections.singletonList(Tuples.pair(newStorage(), project));
			}

			@Override
			public URI getUri(IStorage storage) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Map<URI, IStorage> getAllEntries(IContainer container) {
				throw new UnsupportedOperationException();
			}
		};
		QueuedBuildData data = new QueuedBuildData(mapper, new QueuedBuildData.NullContribution());
		data.queueURIs(java.util.Arrays.asList(uris));
		return data;
	}

	private static IProject newProject(String name) {
		IProjectDescription description = proxy(IProjectDescription.class, (proxy, method, args) -> {
			if ("getBuildSpec".equals(method.getName())) {
				ICommand command = proxy(ICommand.class,
						(p, m, a) -> XtextProjectHelper.BUILDER_ID);
				return new ICommand[] { command };
			}
			return defaultValue(method);
		});
		return proxy(IProject.class, (proxy, method, args) -> {
			switch (method.getName()) {
			case "getName":
				return name;
			case "isAccessible":
			case "hasNature":
				return true;
			case "getDescription":
				return description;
			default:
				return defaultValue(method);
			}
		});
	}

	private static IStorage newStorage() {
		return proxy(IStorage.class, (proxy, method, args) -> defaultValue(method));
	}

	private static Object defaultValue(Method method) {
		Class<?> type = method.getReturnType();
		if (type == boolean.class)
			return false;
		if (type == int.class)
			return 0;
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, InvocationHandler handler) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
	}

	private static final class EmptyMapper implements IStorage2UriMapper {
		@Override
		public Iterable<Pair<IStorage, IProject>> getStorages(URI uri) {
			return Collections.emptyList();
		}

		@Override
		public URI getUri(IStorage storage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<URI, IStorage> getAllEntries(IContainer container) {
			throw new UnsupportedOperationException();
		}
	}
}
