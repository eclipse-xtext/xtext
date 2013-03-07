/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.trace;

import static org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil.*;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.builder.trace.TraceMarkers;
import org.eclipse.xtext.generator.trace.ILocationInResource;
import org.eclipse.xtext.generator.trace.ITrace;
import org.eclipse.xtext.generator.trace.ITraceForStorageProvider;
import org.eclipse.xtext.util.TextRegion;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Holger Schill - Initial contribution and API
 */
public class XtendTraceTests extends AbstractXtendUITestCase {

	@Inject
	private WorkbenchTestHelper testHelper;

	@Inject
	private TraceMarkers traceMarkers;

	@Inject
	private ITraceForStorageProvider traceInformation;


	@Test
	public void testMarkerForTraceIsInstalled() throws Exception {
		IFile file = testHelper.createFile("test/Test", "package test\nclass Test {}");
		waitForAutoBuild();
		List<IPath> traces = traceMarkers.findTraceFiles(file);
		assertEquals(1, traces.size());
	}

	@Test
	public void testTraceFound() throws Exception {
		IFile file = testHelper.createFile("test/Test", "package test\nclass Test {}");
		waitForAutoBuild();
		ITrace traceToTarget = traceInformation.getTraceToTarget(file);
		assertNotNull(traceToTarget);
		Iterable<ILocationInResource> locations = traceToTarget.getAllAssociatedLocations(new TextRegion(20, 0));
		assertTrue( locations.iterator().hasNext());
		IFile generatedFile = testHelper.getProject().getFile("/xtend-gen/test/Test.java");
		Iterable<ILocationInResource> locationsByURI = traceToTarget.getAllAssociatedLocations(new TextRegion(20, 0), generatedFile);
		assertTrue(locationsByURI.iterator().hasNext());

	}
}
