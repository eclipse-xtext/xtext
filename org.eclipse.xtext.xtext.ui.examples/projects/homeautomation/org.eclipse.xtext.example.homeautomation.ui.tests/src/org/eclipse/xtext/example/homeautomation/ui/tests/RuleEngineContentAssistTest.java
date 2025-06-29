/*******************************************************************************
 * Copyright (c) 2018, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(RuleEngineUiInjectorProvider.class)
public class RuleEngineContentAssistTest extends AbstractContentAssistTest {

	@Test
	public void empty() throws Exception {
		String model = """
		<|>
		""";
		assertContentAssistant(model, new String[]{
			"Device",
			"Rule"
		}, "Device", """
		Device
		""");
	}

	@Test
	public void rule_device_state() throws Exception {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when <|>
		""";
		assertContentAssistant(model, new String[]{
			"Window.open",
			"Window.closed",
			"Heater.on",
			"Heater.off",
			"Heater.error"
		}, "Window.open", """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open
		""");
	}

	@Test
	public void rule_device_state_with_prefix() throws Exception {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Win<|>
		""";
		assertContentAssistant(model, new String[]{
			"Window.open",
			"Window.closed"
		}, "Window.open", """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open
		""");
	}

	@Test
	public void rule_then_part() throws Exception {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open then
			<|>
		""";
		assertContentAssistant(model, new String[]{
			"do",
			"false",
			"for",
			"if",
			"new",
			"null",
			"return",
			"switch",
			"synchronized",
			"throw",
			"true",
			"try",
			"typeof",
			"val",
			"var",
			"while"
		}, "switch", """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open then
			switch
		""");
	}

	@Test
	public void rule_then_part_with_prefix() throws Exception {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open then
			fire(Heater.o<|>)
		""";
		assertContentAssistant(model, new String[]{
			"on",
			"off"
		}, "off", """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open then
			fire(Heater.off)
		""");
	}
}
