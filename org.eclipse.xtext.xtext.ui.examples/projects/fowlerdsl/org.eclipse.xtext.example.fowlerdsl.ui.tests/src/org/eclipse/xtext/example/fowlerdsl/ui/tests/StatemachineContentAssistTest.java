/*******************************************************************************
 * Copyright (c) 2018, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.example.fowlerdsl.ui.tests;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.testing.AbstractContentAssistTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil.waitForBuild;

/**
 * @author miklossy - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(StatemachineUiInjectorProvider.class)
public class StatemachineContentAssistTest extends AbstractContentAssistTest {

	@Test
	public void empty() throws Exception {
		assertContentAssistant("""
			<|>
		""", new String[]{
			"commands",
			"events",
			"resetEvents",
			"state"
		}, "resetEvents", """
			resetEvents
		""");
	}

	@Test
	public void statemachine_resetEvents() throws Exception {
		assertContentAssistant("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			resetEvents
				<|>
			end
		""", new String[]{
			"doorClosed",
			"drawerOpened",
			"lightOn",
			"doorOpened",
			"panelClosed"
		}, "doorOpened", """
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			resetEvents
				doorOpened
			end
		""");
	}

	@Test
	public void state_actions() throws Exception {
		assertContentAssistant("""
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {<|>}
			end
		""", new String[]{
			"unlockPanel",
			"lockPanel",
			"lockDoor",
			"unlockDoor",
			"{"
		}, "unlockDoor", """
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {unlockDoor}
			end
		""");
	}

	@Test
	public void transition_event() throws Exception {
		assertContentAssistant("""
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
			
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {unlockDoor lockPanel}
				<|>
			end
		""", new String[]{
			"Transition - Template for a Transition",
			"doorClosed",
			"drawerOpened",
			"lightOn",
			"doorOpened",
			"panelClosed",
			"end"
		}, "doorClosed", """
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
			
			commands
				unlockPanel PNUL
				lockPanel   NLK
				lockDoor    D1LK
				unlockDoor  D1UL
			end
			
			state idle
				actions {unlockDoor lockPanel}
				doorClosed
			end
		""");
	}

	@Test
	public void transition_state() throws Exception {
		assertContentAssistant("""
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
				panelClosed => <|>
			end
		""", new String[]{
			"idle",
			"active",
			"waitingForLight",
			"waitingForDrawer",
			"unlockedPanel"
		}, "idle", """
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
		""");
	}

	@Test
	public void transition_template() throws Exception {
		assertContentAssistant("""
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			state idle
				<|>
			end
			
			state active
			end
		""", new String[]{
			"doorClosed",
			"drawerOpened",
			"lightOn",
			"doorOpened",
			"panelClosed",
			"actions",
			"end",
			"Transition - Template for a Transition"
		}, "Transition - Template for a Transition", """
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
			
			state idle
				doorClosed => idle
			end
			
			state active
			end
		""");
	}

	@Test
	public void events_from_another_file() {
		createDslFile("events", """
			events
				doorClosed   D1CL
				drawerOpened D2OP
				lightOn      L1ON
				doorOpened   D1OP
				panelClosed  PNCL
			end
		""");

		waitForBuild();

		assertContentAssistant("""
			resetEvents
				<|>
			end
		""", """
		doorClosed
		drawerOpened
		lightOn
		doorOpened
		panelClosed
		""", "doorOpened", """
			resetEvents
				doorOpened
			end
		""");
	}
}
