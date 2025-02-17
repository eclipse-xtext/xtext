/**
 * Copyright (c) 2023, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.compiler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.builder.impl.XtextBuilder;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import com.google.inject.Inject;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
public class QuickDebugSourceInstallingCompilationParticipantTest extends AbstractXtendUITestCase {
	@Inject
	private WorkbenchTestHelper workbenchTestHelper;

	@Test
	public void testIfThereIsAnyStatum() throws Exception {
		final IFile source = workbenchTestHelper.createFile("somePackage/Outer.xtend", """
			package somePackage

			class Outer {
			  def dosomething() {
			  	println(1)
			  	println(2)
			  	println(3)
			  }
			}
		""");
		var project = source.getProject();
		incrementalBuild(project, XtextBuilder.BUILDER_ID,
				new IResourcesSetupUtil.ConsoleLoggingProgressMonitor("XTEXT BUILD"));
		incrementalBuild(project, JavaCore.BUILDER_ID,
				new IResourcesSetupUtil.ConsoleLoggingProgressMonitor("JAVA BUILD"));

		project.refreshLocal(IResource.DEPTH_INFINITE,
				new IResourcesSetupUtil.ConsoleLoggingProgressMonitor("REFRESH"));

		final IFile clazz = project.getFile("bin/somePackage/Outer.class");
		assertTrue("bytecode not found", clazz.exists());
		final AtomicBoolean debugInfoFound = new AtomicBoolean(false);
		Path classFile = clazz.getLocation().toPath();
		try (var in = Files.newInputStream(classFile)) {
			final ClassReader r = new ClassReader(in);
			r.accept(new ClassVisitor(Opcodes.ASM9) {
				@Override
				public void visitSource(final String source, final String debug) {
					if ("Outer.java".equals(source) && debug != null) {
						assertEquals("""
							SMAP
							Outer.java
							Xtend
							*S Xtend
							*F
							+ 0 Outer.xtend
							somePackage/Outer.xtend
							*L
							4:8,2
							5:10
							6:11
							7:12
							4:13,2
							*E
							""", debug.replace("\r", ""));
						debugInfoFound.set(true);
					}
					super.visitSource(source, debug);
				}
			}, 0);
			if (!debugInfoFound.get()) {
				fail("No source attribute found in bytecode");
			}
		}
	}

	private void incrementalBuild(IProject project, String builderName, IProgressMonitor monitor) throws CoreException {
		IResourcesSetupUtil.waitForJdtIndex(monitor);
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, builderName, new HashMap<>(), monitor);
	}
}
