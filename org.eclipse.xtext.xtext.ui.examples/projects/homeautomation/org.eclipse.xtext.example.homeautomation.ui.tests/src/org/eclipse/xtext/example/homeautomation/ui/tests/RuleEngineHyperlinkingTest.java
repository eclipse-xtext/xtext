/*******************************************************************************
 * Copyright (c) 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractHyperlinkingTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.createJavaProject;
/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(RuleEngineUiInjectorProvider.class)
@SuppressWarnings("all")
public class RuleEngineHyperlinkingTest extends AbstractHyperlinkingTest {

	@Before
	public void setup() throws Exception {
		/*
		 * Xbase-based languages require java project
		 */
		createJavaProject(getProjectName());
	}

	@Test
	public void hyperlink_on_rule_when_part() {
		hasHyperlinkTo("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when <|>Window.open<|> then
				fire(Heater.off)
		""", "Window.open");
	}

	@Test
	public void hyperlink_on_device_name_in_rule_then_part() {
		hasHyperlinkTo("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(<|>Heater<|>.off)
		""", "Heater");
	}

	@Test
	public void hyperlink_on_device_state_in_rule_then_part() {
		hasHyperlinkTo("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.<|>off<|>)
		""", "Heater.off");
	}

	@Test
	public void hyperlink_on_link_in_javadoc1() {
		hasHyperlinkTo("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			/**
			 * If the {@link <|>Window<|>} is open, the {@link Heater} should be turned off.
			 */
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "Window");
	}

	@Test
	public void hyperlink_on_link_in_javadoc2() {
		hasHyperlinkTo("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			/**
			 * If the {@link Window} is open, the {@link <|>Heater<|>} should be turned off.
			 */
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "Heater");
	}

	@Test
	public void hyperlink_on_link_in_javadoc3() {
		hasHyperlinkTo("""
			/**
			 * {@link <|>java.util.List<|>}
			 */
			Device Window can be open, closed
		""", "java.util.List");
	}
}
