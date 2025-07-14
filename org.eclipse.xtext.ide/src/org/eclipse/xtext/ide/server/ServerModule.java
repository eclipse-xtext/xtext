/*******************************************************************************
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.server;

import com.google.inject.AbstractModule;
import java.util.concurrent.ExecutorService;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.xtext.ide.ExecutorServiceProvider;
import org.eclipse.xtext.ide.server.concurrent.IRequestManager;
import org.eclipse.xtext.ide.server.concurrent.RequestManager;
import org.eclipse.xtext.resource.IContainer;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.ResourceServiceProviderServiceLoader;
import org.eclipse.xtext.resource.containers.ProjectDescriptionBasedContainerManager;

/**
 * @author Sven Efftinge - Initial contribution and API
 * @since 2.11
 */
public class ServerModule extends AbstractModule {

	@Override
	protected void configure() {
		binder().bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);

		bind(LanguageServer.class).to(LanguageServerImpl.class);
		bind(IResourceServiceProvider.Registry.class).toProvider(ResourceServiceProviderServiceLoader.class);
		bind(IMultiRootWorkspaceConfigFactory.class).to(MultiRootWorkspaceConfigFactory.class);
		bind(IProjectDescriptionFactory.class).to(DefaultProjectDescriptionFactory.class);
		bind(IContainer.Manager.class).to(ProjectDescriptionBasedContainerManager.class);
		bind(IRequestManager.class).to(RequestManager.class);
	}
}