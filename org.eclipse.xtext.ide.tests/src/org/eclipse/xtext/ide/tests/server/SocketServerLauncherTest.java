/*******************************************************************************
 * Copyright (c) 2026 Eclipse Xtext and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.tests.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;

import org.eclipse.xtext.ide.server.SocketServerLauncher;
import org.junit.Test;

public class SocketServerLauncherTest {

	private static class TestLauncher extends SocketServerLauncher {
		InetSocketAddress address(String... args) {
			return getSocketAddress(args);
		}
	}

	@Test
	public void defaultAddressIsLoopback() {
		InetSocketAddress address = new TestLauncher().address();
		assertNotNull(address.getAddress());
		assertTrue(address.getAddress().isLoopbackAddress());
		assertEquals(SocketServerLauncher.DEFAULT_PORT, address.getPort());
	}

	@Test
	public void explicitHostIsUsed() {
		InetSocketAddress address = new TestLauncher().address("-host", "192.0.2.1", "-port", "1234");
		assertNotNull(address.getAddress());
		assertEquals("192.0.2.1", address.getAddress().getHostAddress());
		assertEquals(1234, address.getPort());
	}
}
