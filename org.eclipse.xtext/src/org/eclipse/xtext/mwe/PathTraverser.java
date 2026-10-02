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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;

import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class PathTraverser {
	private final static Logger LOG = Logger.getLogger(PathTraverser.class);
	
	public Multimap<String, URI> resolvePathes(List<String> pathes, Predicate<URI> isValidPredicate) {
		Multimap<String, URI> uris = HashMultimap.create();
		for (String path : pathes) {
			Set<URI> resourceUris = findAllResourceUris(path, isValidPredicate);
			uris.putAll(path, resourceUris);
		}
		return uris;
	}
	
	public Set<URI> findAllResourceUris(String path, Predicate<URI> isValidPredicate) {
		File file = new File(path);
		if(!file.exists()) {
			LOG.debug("File under : " + path + " doesn't exist.");
			return new HashSet<>();
		} else if (file.isDirectory()) {
			return traverseDir(file, isValidPredicate);
		} else if (file.isFile()) {
			return traverseArchive(file, isValidPredicate);
		}
		throw new IllegalArgumentException("Unsupported path : " + path + " (only folders and archives are supported).");
	}

	protected Set<URI> traverseArchive(File file, Predicate<URI> isValidPredicate) {
		try {
			try (ZipFile zipFile = new ZipFile(file);) {
				return zipFile.stream().map(entry -> getUri(file, entry)) //
						.filter(Objects::nonNull).filter(isValidPredicate) //
						.collect(Collectors.toCollection(HashSet::new));
			}
		} catch (Exception e) {
			throw new WrappedException("Error traversing archive " + file, e);
		}
	}

	protected URI getUri(File file, ZipEntry entry) {
		URI fileToArchive = URI.createFileURI(file.getAbsolutePath());
		return URI.createURI("archive:" + fileToArchive + "!/" + entry.getName());
	}

	protected Set<URI> traverseDir(File file, final Predicate<URI> isValidPredicate) {
		Set<URI> result = new HashSet<>();
		File[] files = file.listFiles();
		if (files == null)
			return result;
		for (File f : files) {
			if (f.isDirectory()) {
				result.addAll(traverseDir(f, isValidPredicate));
			} else {
				URI uri = URI.createFileURI(f.getAbsolutePath());
				if (isValidPredicate.apply(uri)) {
					result.add(uri);
				}
			}
		}
		return result;
	}
}
