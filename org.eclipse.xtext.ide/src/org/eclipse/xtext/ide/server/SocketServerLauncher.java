/**
 * Copyright (c) 2019, 2021 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.xtext.xbase.lib.ArrayExtensions;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Allows to launch a language server via a server socket.
 * 
 * Useful for debugging live language servers.
 * 
 * @author Jan K&ouml;hnlein - Initial contribution and API
 * @since 2.18
 */
public class SocketServerLauncher {

	private static final Logger LOG = Logger.getLogger(SocketServerLauncher.class);
	public static final String HOST = "-host";

	public static final String PORT = "-port";

	public static final String TRACE = "-trace";

	public static final String NO_VALIDATE = "-noValidate";

	public static final int DEFAULT_PORT = 5008;

	public static final String DEFAULT_HOST = InetAddress.getLoopbackAddress().getHostAddress();

	public static void main(String[] args) {
		new SocketServerLauncher().launch(args);
	}

	public void launch(String[] args) {
		Injector injector = Guice.createInjector(getServerModule());
		try (AsynchronousServerSocketChannel serverSocket = AsynchronousServerSocketChannel.open()
				.bind(getSocketAddress(args))) {
			LOG.info("Started server socket at " + getSocketAddress(args));
			while (true) {
				AsynchronousSocketChannel socketChannel = serverSocket.accept().get();
				InputStream in = Channels.newInputStream(socketChannel);
				OutputStream out = Channels.newOutputStream(socketChannel);
				PrintWriter trace = getTrace(args);
				boolean validate = shouldValidate(args);
				LanguageServerImpl languageServer = injector.getInstance(LanguageServerImpl.class);
				LOG
						.info("Starting Xtext Language Server for client " + socketChannel.getRemoteAddress());
				Launcher<LanguageClient> launcher = Launcher.createLauncher(languageServer, LanguageClient.class, in,
						out, validate, trace);
				languageServer.connect(launcher.getRemoteProxy());
				launcher.startListening();
				LOG.info("Xtext Language Server has been started.");
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected com.google.inject.Module getServerModule() {
		return new ServerModule();
	}

	protected PrintWriter getTrace(String... args) {
		if (ArrayExtensions.contains(args, TRACE)) {
			return new PrintWriter(System.out);
		}
		return null;
	}

	protected boolean shouldValidate(String... args) {
		return !ArrayExtensions.contains(args, NO_VALIDATE);
	}

	protected InetSocketAddress getSocketAddress(String... args) {
		String host = getValue(args, HOST);
		int port = getPort(args);
		if (host != null) {
			return new InetSocketAddress(host, port);
		}
		return new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
	}

	protected String getHost(String... args) {
		String host = getValue(args, HOST);
		if (host != null) {
			return host;
		} else {
			return DEFAULT_HOST;
		}
	}

	protected int getPort(String... args) {
		String value = getValue(args, PORT);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return DEFAULT_PORT;
			}
		}
		return DEFAULT_PORT;
	}

	protected String getValue(String[] args, String argName) {
		for (int i = 0; (i < (args.length - 1)); i++) {
			if (Objects.equals(args[i], argName)) {
				return args[i + 1];
			}
		}
		return null;
	}
}
