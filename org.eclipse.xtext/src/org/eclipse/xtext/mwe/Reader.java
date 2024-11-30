/*******************************************************************************
 * Copyright (c) 2010, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.mwe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.mwe.core.WorkflowContext;
import org.eclipse.emf.mwe.core.WorkflowInterruptedException;
import org.eclipse.emf.mwe.core.issues.Issues;
import org.eclipse.emf.mwe.core.monitor.ProgressMonitor;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.containers.DelegatingIAllContainerAdapter;
import org.eclipse.xtext.resource.containers.IAllContainersState;

import com.google.common.collect.Multimap;

/**
 * <p>
 * A Reader used to read EMF resources from a set of paths.
 * A path can point to a folder or an archive (zips and jars are supported).
 * Those paths are recursively scanned and all resources for which an {@link IResourceServiceProvider} is
 * registered in the {@link org.eclipse.xtext.resource.IResourceServiceProvider.Registry} will be available.
 * </p>
 * 
 * <p>
 * The {@link ISetup} of an Xtext language registers such an {@link IResourceServiceProvider} automatically. If you want to integrate with non Xtext-based models,
 * make sure to add {@link IResourceServiceProvider} manually. The easiest way might be to implement an {@link ISetup}. 
 * </p>
 * <p>
 * A {@link SlotEntry} is responsible for selecting certain EObjects from the loaded resources.
 * It supports selecting EObjects by their name (see {@link org.eclipse.xtext.resource.IEObjectDescription}) or by an EClass.
 * In many cases such section returns multiple EObjects, if you're only interested in one element set the <code>firstOnly</code> flag to <code>true</code>.
 * </p>
 * <p>
 * You might want to populate multiple workflow slots with model elements.
 * </p>
 * <p>
 * Below are examples of a usage in the MWE2 DSL and MWE XML syntax:
 * </p>
 * <pre>
 * component = org.eclipse.xtext.mwe.Reader {
 *   useJavaClassPath = true
 *   register = foo.bar.MyLanguageStandaloneSetup {}
 *   load = {
 *     slot = "types"
 *     type = "Type"
 *   }
 *   validate = org.eclipse.xtext.mwe.Validator { stopOnError = false }
 * }
 * </pre>
 * <pre>
 * &lt;component class="org.eclipse.xtext.mwe.Reader" useJavaClassPath="true"&gt;
 *   &lt;register class="foo.bar.MyLanguageStandaloneSetup"/&gt;
 *   &lt;load slot="types" type="Type"/&gt;
 *   &lt;validate/&gt; 
 * &lt;/component&gt;
 * </pre>
 * It supports fetching elements by type
 * {@link IResourceServiceProvider#getResourceDescriptionManager()} is used to identify model elements within these resources. 
 * 
 * @author Sven Efftinge - Initial contribution and API
 */
public class Reader extends AbstractReader {

	protected final static Logger log = Logger.getLogger(Reader.class.getName());
	protected List<String> pathes = new ArrayList<>();

	/**
	 * <p>
	 * A comma-separated list of paths pointing to a folder, jar or zip which contains EMF resources.
	 * </p>
	 * <p>
	 * Example use (MWE2):
	 * </p> 
	 * <code>
	 * path = "./foo/bar.jar,./src/main/model"
	 * </code>
	 * <p>
	 * Example use (MWE1):
	 * </p> 
	 * <code>
	 * &lt;path value="./foo/bar.jar,./src/main/model"/&gt;
	 * </code>
	 */
	public void addPath(String path) {
		for (String p : path.split(",")) {
			this.pathes.add(p.trim());
		}
	}

	public List<String> getPathes() {
		return pathes;
	}

	/**
	 * <p>
	 * Automatically adds all class path entries of the current process (more specifically uses 'java.class.path' system property).
	 * </p>
	 * <p>
	 * Example use (MWE2):
	 * </p> 
	 * <code>
	 * useJavaClassPath = true
	 * </code>
	 * <p>
	 * Example use (MWE1):
	 * </p> 
	 * <code>
	 * &lt;useJavaClassPath value="true"/&gt;
	 * </code>
	 */
	public void setUseJavaClassPath(boolean isUse) {
		if (isUse) {
			String classPath = System.getProperty("java.class.path");
			String separator = System.getProperty("path.separator");
			String[] strings = classPath.split(separator);
			for (String path : strings) {
				addPath(path);
			}
		}
	}
	
	private UriFilter filter;
	
	/**
	 * Optionally set a filter that specifies which URIs are valid to be read.
	 * A common use-case for filters is a file-name based selection of valid
	 * URIs.
	 */
	public void setUriFilter(UriFilter filter) {
		this.filter = filter;
	}
	
	public UriFilter getUriFilter() {
		return filter;
	}

	private ContainersStateFactory containersStateFactory = new ContainersStateFactory();

	public void setContainersStateFactory(ContainersStateFactory containersStateFactory) {
		this.containersStateFactory = containersStateFactory;
	}
	
	public ContainersStateFactory getContainersStateFactory() {
		return containersStateFactory;
	}
	
	@Override
	protected void checkConfigurationInternal(Issues issues) {
		super.checkConfigurationInternal(issues);
		if (pathes.isEmpty()) {
			issues.addWarning("No path set, using java class path entries (useJavaClassPath='true').");
			setUseJavaClassPath(true);
		}
		if (log.isDebugEnabled()) {
			log.debug("Resource Pathes : " + pathes);
		}
		for (String path : pathes) {
			if (!new File(path).exists())
				issues.addWarning("Skipping the path '" + path + "', because it does not exist.");
		}
	}

	@Override
	protected void invokeInternal(WorkflowContext ctx, ProgressMonitor monitor, Issues issues) {
		ResourceSet resourceSet = getResourceSet();
		Multimap<String, URI> uris = getPathTraverser().resolvePathes(pathes, input -> {
			boolean result = true;
			if (getUriFilter() != null) {
				result = getUriFilter().matches(input);
			}
			if (result) {
				result = getRegistry().getResourceServiceProvider(input) != null;
			}
			return result;
		});
		IAllContainersState containersState = containersStateFactory.getContainersState(pathes, uris);
		installAsAdapter(resourceSet, containersState);
		populateResourceSet(resourceSet, uris);
		getValidator().validate(resourceSet, getRegistry(), issues);
		addModelElementsToContext(ctx, resourceSet);
	}

	protected PathTraverser getPathTraverser() {
		return new PathTraverser();
	}

	protected void populateResourceSet(ResourceSet set, Multimap<String, URI> uris) {
		uris.values().stream().distinct().forEach(set::createResource);
	}

	protected void installAsAdapter(ResourceSet set, IAllContainersState containersState)
			throws WorkflowInterruptedException {
		set.eAdapters().add(new DelegatingIAllContainerAdapter(containersState));
	}

}
