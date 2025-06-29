/*******************************************************************************
 * Copyright (c) 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.ui.tests;

import org.eclipse.jface.text.Region;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractHoverTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.createJavaProject;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(RuleEngineUiInjectorProvider.class)
public class RuleEngineHoverTest extends AbstractHoverTest {

	@Before
	public void setup() throws Exception {
		/*
		 * Xbase-based languages require java project
		 */
		createJavaProject(getProjectName());
	}

	@Test
	public void hover_over_device_name() {
		String model = """
		Device Window can be open, closed
		""";
		hasHoverOver(model, "Window", "Device Window");
	}

	@Test
	public void hover_over_device_state() {
		String model = """
		Device Window can be open, closed
		""";
		hasHoverOver(model, "open", "State open");
	}

	@Test
	public void hover_over_fire_expression() {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		Rule 'rule1' when Window.open then
			fire(Heater.off)
		""";
		hasHoverOver(model, "fire", "void Hover.fire(Object event)");
	}

	@Test
	public void hover_over_link_in_javadoc1() {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		/*
		 * If the {@link Window} is open, the {@link Heater} should be turned off.
		 */
		Rule 'rule1' when Window.open then
			fire(Heater.off)
		""";
		hasHoverInJavadoc(model, "Window", "Device Window");
	}

	@Test
	public void hover_over_link_in_javadoc2() {
		String model = """
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		/*
		 * If the {@link Window} is open, the {@link Heater} should be turned off.
		 */
		Rule 'rule1' when Window.open then
			fire(Heater.off)
		""";
		hasHoverInJavadoc(model, "Heater", "Device Heater");
	}

	@Test
	public void hover_over_link_in_javadoc3() {
		String model = """
		/*
		 * {@link java.util.List}
		 */
		Device Window can be open, closed
		""";
		hasHoverInJavadoc(model, "java.util.List", "Unlike sets, lists typically allow duplicate elements");
	}

	private void hasHoverInJavadoc(CharSequence it, String hoverText, String hoverContent){
		int startOfJavadoc = it.toString().indexOf("/**");
		Region hoverRegion = new Region(it.toString().indexOf(hoverText, startOfJavadoc), hoverText.length());
		hasHoverOver(it, hoverRegion, hoverContent);
	}
}
