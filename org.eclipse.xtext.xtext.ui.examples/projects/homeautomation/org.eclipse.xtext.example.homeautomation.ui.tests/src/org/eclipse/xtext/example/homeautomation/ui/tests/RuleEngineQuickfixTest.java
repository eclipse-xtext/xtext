/*******************************************************************************
 * Copyright (c) 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractQuickfixTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.eclipse.xtext.diagnostics.Diagnostic.LINKING_DIAGNOSTIC;

import static org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.createJavaProject;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(RuleEngineUiInjectorProvider.class)
public class RuleEngineQuickfixTest extends AbstractQuickfixTest {

	@Before
	public void setup() throws Exception {
		/*
		 * Xbase-based languages require java project
		 */
		createJavaProject(getProjectName());
	}

	@Test
	public void fix_invalid_rule_device_state() {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when foo then
			fire(Heater.off)
		""";
		testQuickfixesOn(model, LINKING_DIAGNOSTIC,
			new Quickfix("Change to 'Window.open'", "Change to 'Window.open'", """
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
			"""),
			new Quickfix("Change to 'Window.closed'", "Change to 'Window.closed'", """
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.closed then
				fire(Heater.off)
			"""),
			new Quickfix("Change to 'Heater.on'", "Change to 'Heater.on'", """
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Heater.on then
				fire(Heater.off)
			"""),
			new Quickfix("Change to 'Heater.off'", "Change to 'Heater.off'", """
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Heater.off then
				fire(Heater.off)
			"""),
			new Quickfix("Change to 'Heater.error'", "Change to 'Heater.error'", """
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Heater.error then
				fire(Heater.off)
			""")
		);
	}
}
