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
import org.eclipse.xtext.ui.testing.AbstractFoldingTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(RuleEngineUiInjectorProvider.class)
@SuppressWarnings("all")
public class RuleEngineFoldingTest extends AbstractFoldingTest {

	@Test
	public void rule() {
		testFoldingRegions("""
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		[>Rule 'rule1' when Window.open then
		[>	fire(Heater.off)<]<]
		""");
	}

	@Test
	public void rules() {
		testFoldingRegions("""
		Device Window can be open, closed
		Device Heater can be on, off, error
		
		[>Rule 'rule1' when Window.open then
		[>	fire(Heater.off)
			<]<]
		[>Rule 'rule2' when Heater.on then
		[>	fire(Window.closed)
			<]<]
		[>Rule 'rule3' when Window.closed then
		[>	fire(Heater.on)
			<]<]
		[>Rule 'rule4' when Window.closed then
		[>	fire(Window.closed)<]<]
		""");
	}
}
