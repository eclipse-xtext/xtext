/*******************************************************************************
 * Copyright (c) 2019, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.buildpath

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtend.ide.buildpath.Junit4LibClasspathAdder
import org.eclipse.xtend.ide.buildpath.Junit6LibClasspathAdder
import org.eclipse.xtend.ide.tests.XtendIDEInjectorProvider
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.junit.Test
import org.junit.runner.RunWith
import com.google.inject.Inject

/**
 * @author vivien.jovet - Initial contribution and API
 */
@InjectWith(XtendIDEInjectorProvider)
@RunWith(XtextRunner)
class JunitLibClasspathAdderTest extends AbstractJunitLibClasspathAdderTestCase {

    @Inject Junit4LibClasspathAdder junit4LibAdder

    @Inject Junit6LibClasspathAdder junit6LibAdder

    @Test
    def void addJUnit4LibToPluginProjectClasspath() {
        junit4LibAdder.addLibsToClasspath(JavaCore.create(project), new NullProgressMonitor)
        JavaCore.create(project).rawClasspath.forEach[println(path)]
        assertRequireBundles(#[Junit4LibClasspathAdder.BUNDLE_ID])
    }

    @Test
    def void addJUnit4LibToProjectClasspath() {
        removePluginNature
        junit4LibAdder.addLibsToClasspath(JavaCore.create(project), new NullProgressMonitor)
        assertClasspath(
            'classpath should contain a JUnit 4 container entry',
            Junit4LibClasspathAdder.JUNIT4_LIBRARY_PATH
        )
    }

    @Test
    def void addJUnit6LibToPluginProjectClasspath() {
        junit6LibAdder.addLibsToClasspath(JavaCore.create(project), new NullProgressMonitor)
        assertRequireBundles(Junit6LibClasspathAdder.BUNDLE_IDS)
    }

    @Test
    def void addJUnit6LibToProjectClasspath() {
        removePluginNature
        junit6LibAdder.addLibsToClasspath(JavaCore.create(project), new NullProgressMonitor)
        assertClasspath(
            'classpath should contain a JUnit 6 container entry',
            Junit6LibClasspathAdder.JUNIT6_LIBRARY_PATH
        )
    }

}
