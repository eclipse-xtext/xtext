/*******************************************************************************
 * Copyright (c) 2015, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.generator.trace.internal;

import static com.google.common.collect.Lists.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.generator.trace.AbsoluteURI;
import org.eclipse.xtext.generator.trace.AbstractTraceRegion;
import org.eclipse.xtext.generator.trace.ITraceForURIProvider;
import org.eclipse.xtext.generator.trace.ITraceRegionProvider;
import org.eclipse.xtext.generator.trace.ITraceURIConverter;
import org.eclipse.xtext.generator.trace.SourceRelativeURI;
import org.eclipse.xtext.generator.trace.TraceFileNameProvider;
import org.eclipse.xtext.generator.trace.TraceNotFoundException;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.ISourceFolder;

import com.google.common.io.Closeables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public abstract class AbstractTraceForURIProvider<SomeFile, Trace extends AbstractTrace> implements ITraceForURIProvider {

	private static final Logger log = Logger.getLogger(AbstractTraceForURIProvider.class);
	
	@Inject
	private IResourceServiceProvider.Registry serviceRegistry;
	
	@Inject
	private TraceFileNameProvider traceFileNameProvider;
	
	@Inject
	private CachedTraces cachedTraces;
	
	protected interface PersistedTrace {
		AbsoluteURI getPath();
		long getModificationStamp();
		InputStream openStream() throws IOException;
		boolean exists();
	}
	
	@Singleton
	protected static class CachedTraces {
		
		private static class CacheKey {

			private final AbsoluteURI path;
			private final long timestamp;

			public CacheKey(PersistedTrace source) {
				this.path = source.getPath();
				this.timestamp = source.getModificationStamp();
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((path == null) ? 0 : path.hashCode());
				result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				CacheKey other = (CacheKey) obj;
				if (path == null) {
					if (other.path != null)
						return false;
				} else if (!path.equals(other.path))
					return false;
				if (timestamp != other.timestamp)
					return false;
				return true;
			}
			
		}
		
		@Inject
		private TraceRegionSerializer traceRegionSerializer;
		
		private Map<CacheKey, AbstractTraceRegion> cache = new LinkedHashMap<CacheKey, AbstractTraceRegion>(32, 0.75f, true) {
			private static final long serialVersionUID = 1L;
			final int MAX_SIZE = 20;
			@Override
			protected boolean removeEldestEntry(Map.Entry<CacheKey, AbstractTraceRegion> eldest) {
		        return size() > MAX_SIZE;
		    }
		};
		
		protected synchronized AbstractTraceRegion getTraceRegion(PersistedTrace traceFile) throws TraceNotFoundException {
			if (traceFile.exists()) {
				CacheKey key = new CacheKey(traceFile);
				AbstractTraceRegion result = cache.get(key);
				if (result != null)
					return result;
				InputStream contents = null;
				try {
					contents = traceFile.openStream();
					result = traceRegionSerializer.readTraceRegionFrom(contents);
					cache.put(key, result);
					return result;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				} finally {
					try {
						Closeables.close(contents, true);
					} catch (IOException e) {
						//never thrown, swallowed by Closeables.close
					}
				}
			}
			throw new TraceNotFoundException();
		}
	}
	
	/**
	 * Instantiate a new instance of the trace and associate the locate information with the given file.
	 */
	protected abstract Trace newAbstractTrace(SomeFile file);
	
	/**
	 * Obtain the file representation from the given URI.
	 */
	protected abstract SomeFile asFile(AbsoluteURI absoluteURI, IProjectConfig project);
	
	/**
	 * Returns the absolute location from the given file representation.
	 */
	protected abstract AbsoluteURI getAbsoluteLocation(SomeFile file);
	
	protected SomeFile asFile(SourceRelativeURI srcRelativeDerivedResource, IProjectConfig project) {
		String[] pathSegments = srcRelativeDerivedResource.getURI().segments();
		Set<? extends ISourceFolder> sourceFolders = project.getSourceFolders();
		for(ISourceFolder folder: sourceFolders) {
			URI srcFolderPath = folder.getPath();
			URI absoluteURI = srcFolderPath.appendSegments(pathSegments);
			SomeFile result = asFile(new AbsoluteURI(absoluteURI), project);
			if (result != null) {
				return result;
			}
		}
		URI fromRoot = project.getPath().appendSegments(pathSegments);
		return asFile(new AbsoluteURI(fromRoot), project);
	}
	
	/**
	 * Find all the trace files that have been written for the given source file.
	 */
	protected abstract List<PersistedTrace> findInverseTraceFiles(SomeFile sourceFile);
	/**
	 * Find the persisted trace information for the given generated file.
	 */
	protected abstract PersistedTrace findPersistedTrace(SomeFile generatedFile);
	/**
	 * Find the project configuration for the given generated file.
	 */
	protected abstract IProjectConfig getProjectConfig(SomeFile sourceFile);

	@Override
	public Trace getTraceToSource(AbsoluteURI absoluteDerivedResource, IProjectConfig project) {
		final SomeFile generatedFile = asFile(absoluteDerivedResource, project);
		return doGetTraceToSource(generatedFile);
	}
	
	@Override
	public Trace getTraceToSource(SourceRelativeURI srcRelativeDerivedResource, IProjectConfig project) {
		final SomeFile generatedFile = asFile(srcRelativeDerivedResource, project);
		return doGetTraceToSource(generatedFile);
	}
	
	protected Trace doGetTraceToSource(final SomeFile generatedFile) {
		final PersistedTrace persistedTrace = findPersistedTrace(generatedFile);
		if (persistedTrace == null || !persistedTrace.exists()) {
			return null;
		}
		Trace result = newAbstractTrace(generatedFile);
		result.setTraceToSource(true);
		result.setTraceRegionProvider(new ITraceRegionProvider() {
			
			@Override
			public AbstractTraceRegion getTraceRegion() {
				AbstractTraceRegion traceRegion = cachedTraces.getTraceRegion(persistedTrace);
				if (traceRegion == null) {
					throw new TraceNotFoundException();
				}
				return traceRegion;
			}
		});
		return result;
	}

	@Override
	public Trace getTraceToTarget(final AbsoluteURI absoluteSourceResource, IProjectConfig project) {
		final SomeFile sourceFile = asFile(absoluteSourceResource, project);
		return getTraceToTarget(sourceFile, absoluteSourceResource, getProjectConfig(sourceFile));
	}

	@Override
	public Trace getTraceToTarget(SourceRelativeURI srcRelativeSourceResource, IProjectConfig project) {
		final SomeFile sourceFile = asFile(srcRelativeSourceResource, project);
		return getTraceToTarget(sourceFile, getAbsoluteLocation(sourceFile), project);
	}
	
	public Trace getTraceToTarget(final SomeFile sourceFile, final AbsoluteURI absoluteSourceResource, final IProjectConfig projectConfig) {
		final List<PersistedTrace> traceFiles = findInverseTraceFiles(sourceFile);
		if (traceFiles.isEmpty() || projectConfig == null) {
			return null;
		}

		Trace result = newAbstractTrace(sourceFile);
		result.setTraceToSource(false);
		final IResourceServiceProvider serviceProvider = getServiceProvider(absoluteSourceResource);
		final ITraceURIConverter traceURIConverter = serviceProvider.get(ITraceURIConverter.class);
		result.setTraceRegionProvider(new ITraceRegionProvider() {
			@Override
			public AbstractTraceRegion getTraceRegion() {
				List<AbstractTraceRegion> result = newArrayList();
				SourceRelativeURI sourceUriForTrace = traceURIConverter.getURIForTrace(projectConfig, absoluteSourceResource);
				for (PersistedTrace traceFile : traceFiles) {
					AbsoluteURI generatedFileURI = getGeneratedLocation(traceFile);
					if (generatedFileURI == null) {
						throw new TraceNotFoundException();
					}
					AbstractTraceRegion traceRegion = cachedTraces.getTraceRegion(traceFile);

					SourceRelativeURI generatedUriForTrace = getGeneratedUriForTrace(projectConfig, absoluteSourceResource, generatedFileURI, traceURIConverter);
					if(sourceUriForTrace != null && generatedUriForTrace != null) {
						result.addAll(traceRegion.invertFor(sourceUriForTrace, generatedUriForTrace));
					}
				}
				if (result.isEmpty()) 
					throw new TraceNotFoundException();
				return AbstractTraceRegion.mergedFrom(result);
			}
		});
		return result;
	}
	
	protected SourceRelativeURI getGeneratedUriForTrace(IProjectConfig projectConfig, AbsoluteURI absoluteSourceResource, AbsoluteURI generatedFileURI, ITraceURIConverter traceURIConverter) {
		return traceURIConverter.getURIForTrace(projectConfig, generatedFileURI);
	}
	
	protected IResourceServiceProvider getServiceProvider(final AbsoluteURI absoluteSourceResource) {
		return serviceRegistry.getResourceServiceProvider(absoluteSourceResource.getURI());
	}

	/**
	 * Compute the location of the generated file from the given trace file.
	 */
	protected AbsoluteURI getGeneratedLocation(PersistedTrace trace) {
		AbsoluteURI path = trace.getPath();
		String fileName = traceFileNameProvider.getJavaFromTrace(path.getURI().toString());
		return new AbsoluteURI(fileName);
	}
	
	protected TraceFileNameProvider getTraceFileNameProvider() {
		return traceFileNameProvider;
	}
}
