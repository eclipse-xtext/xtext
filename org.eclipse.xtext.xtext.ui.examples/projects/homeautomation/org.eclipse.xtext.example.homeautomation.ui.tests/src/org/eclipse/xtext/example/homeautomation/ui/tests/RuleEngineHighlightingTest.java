/*******************************************************************************
 * Copyright (c) 2018, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.homeautomation.ui.tests;

import com.google.inject.Inject;

import static org.eclipse.xtext.ui.testing.util.JavaProjectSetupUtil.createJavaProject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractHighlightingTest;
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingConfiguration;
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
public class RuleEngineHighlightingTest extends AbstractHighlightingTest {

	@Inject
	private XbaseHighlightingConfiguration xbaseHighlightingConfiguration;

	@Before
	public void setup() throws Exception {
		/*
		 * Xbase-based languages require java project
		 */
		createJavaProject(getProjectName());
	}

	@Test
	public void device_keyword() {
		testHighlighting("""
			Device Window can be open, closed
		""", "Device", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void can_keyword() {
		testHighlighting("""
			Device Window can be open, closed
		""", "can", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void be_keyword() {
		testHighlighting("""
			Device Window can be open, closed
		""", "be", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void rule_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "Rule", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void when_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "when", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void then_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "then", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void switch_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off
			
			Rule "Save energy" when Window.open then
				switch new java.util.Random().nextInt
					case 1,
					case 2:
						fire(Heater.off)
					default:
						fire(Heater.on)
		""", "switch", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void case_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off
			
			Rule "Save energy" when Window.open then
				switch new java.util.Random().nextInt
					case 1,
					case 2:
						fire(Heater.off)
					default:
						fire(Heater.on)
		""", "case", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void default_keyword() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off
			
			Rule "Save energy" when Window.open then
				switch new java.util.Random().nextInt
					case 1,
					case 2:
						fire(Heater.off)
					default:
						fire(Heater.on)
		""", "default", xbaseHighlightingConfiguration.keywordTextStyle());
	}

	@Test
	public void number() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off
			
			Rule "Save energy" when Window.open then
				switch new java.util.Random().nextInt
					case 1,
					case 2:
						fire(Heater.off)
					default:
						fire(Heater.on)
		""", "1", xbaseHighlightingConfiguration.numberTextStyle());
	}

	@Test
	public void punctuation() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", ".", xbaseHighlightingConfiguration.punctuationTextStyle());
	}

	@Test
	public void single_quoted_rule_description() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule 'rule1' when Window.open then
				fire(Heater.off)
		""", "rule1", xbaseHighlightingConfiguration.stringTextStyle());
	}

	@Test
	public void double_quoted_rule_description() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule "rule1" when Window.open then
				fire(Heater.off)
		""", "rule1", xbaseHighlightingConfiguration.stringTextStyle());
	}

	@Test
	public void fire_method_invocation() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule "rule1" when Window.open then
				fire(Heater.off)
		""", "fire", xbaseHighlightingConfiguration.staticMethodInvocation());
	}

	@Test
	public void device_state_access() {
		testHighlighting("""
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule "rule1" when Window.open then
				fire(Heater.off)
		""", "off", xbaseHighlightingConfiguration.staticField());
	}

	@Test
	public void single_line_comment() {
		testHighlighting("""
			// A language for home automation systems.
			
			Device Window can be open, closed
			Device Heater can be on, off, error
			
			Rule "rule1" when Window.open then
				fire(Heater.off)
		""", "A language for home automation systems", xbaseHighlightingConfiguration.commentTextStyle());
	}

	@Test
	public void multi_line_comment() {
		testHighlighting("""
			/*
			 * A language for home automation systems
			 * supporting indentation-based code blocks
			 * similar to Python.
			 */
		""", """
			/*
			 * A language for home automation systems
			 * supporting indentation-based code blocks
			 * similar to Python.
			 */
		""", xbaseHighlightingConfiguration.commentTextStyle());
	}

	@Test
	public void fixme_task_in_comment() {
		testHighlighting("""
			/**
			 * FIXME
			 */
			Device Window can be open, closed
		""", "FIXME", xbaseHighlightingConfiguration.taskTextStyle());
	}

	@Test
	public void todo_task_in_comment() {
		testHighlighting("""
			/**
			 * TODO
			 */
			Device Window can be open, closed
		""", "TODO", xbaseHighlightingConfiguration.taskTextStyle());
	}

	@Test
	public void xxx_task_in_comment() {
		testHighlighting("""
			/**
			 * XXX
			 */
			Device Window can be open, closed
		""", "XXX", xbaseHighlightingConfiguration.taskTextStyle());
	}

	@Override
	protected int getStartPosition(String content, String text) {
		return content.lastIndexOf(text);
	}
}
