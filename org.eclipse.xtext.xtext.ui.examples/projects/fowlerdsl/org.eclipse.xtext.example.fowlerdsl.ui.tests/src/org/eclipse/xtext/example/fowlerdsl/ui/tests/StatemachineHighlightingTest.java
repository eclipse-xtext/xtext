/*******************************************************************************
 * Copyright (c) 2018, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.fowlerdsl.ui.tests;

import org.eclipse.swt.SWT;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractHighlightingTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(StatemachineUiInjectorProvider.class)
public class StatemachineHighlightingTest extends AbstractHighlightingTest {

	@Test
	public void events_keyword() {
		testHighlighting("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
		""", "events", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void end_keyword() {
		testHighlighting("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
		""", "end", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void resetEvents_keyword() {
		testHighlighting("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			resetEvents
				doorOpened doorClosed
			end
		""", "resetEvents", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void commands_keyword() {
		testHighlighting("""
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
		""", "commands", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void state_keyword() {
		testHighlighting("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			resetEvents
				doorOpened
				doorClosed
			end
			
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {unlockDoor lockPanel}
				doorClosed => active
			end
			
			state active
				drawerOpened => waitingForLight
				lightOn      => waitingForDrawer
			end
			
			state waitingForLight
				lightOn => unlockedPanel
			end
			
			state waitingForDrawer
				drawerOpened => unlockedPanel
			end
			
			state unlockedPanel
				actions {unlockPanel lockDoor}
				panelClosed => idle
			end
		""", "state", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void actions_keyword() {
		testHighlighting("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			resetEvents
				doorOpened
				doorClosed
			end
			
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {unlockDoor lockPanel}
				doorClosed => active
			end
			
			state active
				drawerOpened => waitingForLight
				lightOn      => waitingForDrawer
			end
			
			state waitingForLight
				lightOn => unlockedPanel
			end
			
			state waitingForDrawer
				drawerOpened => unlockedPanel
			end
			
			state unlockedPanel
				actions {unlockPanel lockDoor}
				panelClosed => idle
			end
		""", "actions", SWT.BOLD, 127, 0, 85);
	}

	@Test
	public void single_line_comment() {
		testHighlighting("""
			// An implementation of Martin Fowler's secret compartment state machine
		""", "An implementation of Martin Fowler's secret compartment state machine",
				SWT.NORMAL, 63, 127, 95
		);
	}

	@Test
	public void multi_line_comment() {
		testHighlighting("""
			/*
			 * An implementation of Martin Fowler's secret compartment state machine
			 * 
			 * http://martinfowler.com/dslwip/Intro.html
			 */
		""", """
			/*
			 * An implementation of Martin Fowler's secret compartment state machine
			 * 
			 * http://martinfowler.com/dslwip/Intro.html
			 */
		""", SWT.NORMAL, 63, 127, 95);
	}

	@Test
	public void fixme_task_in_comment() {
		testHighlighting("""
			// FIXME
		""", "FIXME", SWT.BOLD, 127, 159, 191);
	}

	@Test
	public void todo_task_in_comment() {
		testHighlighting("""
			// TODO
		""", "TODO", SWT.BOLD, 127, 159, 191);
	}

	@Test
	public void xxx_task_in_comment() {
		testHighlighting("""
			// XXX
		""", "XXX", SWT.BOLD, 127, 159, 191);
	}
}
