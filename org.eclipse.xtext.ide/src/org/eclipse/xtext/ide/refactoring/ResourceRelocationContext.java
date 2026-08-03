/**
 * Copyright (c) 2017, 2020 TypeFox GmbH (http://www.typefox.io) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.refactoring;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ide.serializer.IChangeSerializer;
import org.eclipse.xtext.xbase.lib.Exceptions;

/**
 * @author koehnlein - Initial contribution and API
 * @since 2.13
 */
public class ResourceRelocationContext {
	public enum ChangeType {
		COPY, MOVE, RENAME;
	}

	private final ResourceRelocationContext.ChangeType changeType;

	private final List<ResourceRelocationChange> changes;

	private final RefactoringIssueAcceptor issueAcceptor;

	private final IChangeSerializer changeSerializer;

	private final ResourceSet resourceSet;

	/**
	 * Loads and watches the respective resource, applies the relocation change and calls the given
	 * <code>modification</code> with the renamed/moved/copied resource.
	 * 
	 * @param change
	 *            the change to execute
	 * @param modification
	 *            the side-effect the rename/move/copy operation should have.
	 */
	public void addModification(ResourceRelocationChange change,
			IChangeSerializer.IModification<Resource> modification) {
		changeSerializer.addModification(loadAndWatchResource(change), modification);
	}

	/**
	 * Loads and watches the respective resource and applies the relocation change. Clients may usually rather call
	 * {@link #addModification} to register their side-effects.
	 * 
	 * @param change
	 *            the change to execute
	 */
	protected Resource loadAndWatchResource(ResourceRelocationChange change) {
			if (changeType != null) {
				switch (changeType) {
				case MOVE:
				case RENAME:
					Resource original = resourceSet.getResource(change.getFromURI(), true);
					changeSerializer.addModification(original, (Resource it) -> original.setURI(change.getToURI()));
					return original;
				case COPY:
					Resource copy = resourceSet.createResource(change.getToURI());
					try {
						copy.load(resourceSet.getURIConverter().createInputStream(change.getFromURI()), null);
					} catch (IOException e) {
						Exceptions.sneakyThrow(e);
					}
					return copy;
				default:
					return null;
				}
			}
			return null;
	}

	public ResourceRelocationContext(ResourceRelocationContext.ChangeType changeType,
			List<ResourceRelocationChange> changes, RefactoringIssueAcceptor issueAcceptor,
			IChangeSerializer changeSerializer, ResourceSet resourceSet) {
		this.changeType = changeType;
		this.changes = changes;
		this.issueAcceptor = issueAcceptor;
		this.changeSerializer = changeSerializer;
		this.resourceSet = resourceSet;
	}

	public ResourceRelocationContext.ChangeType getChangeType() {
		return changeType;
	}

	public List<ResourceRelocationChange> getChanges() {
		return changes;
	}

	public RefactoringIssueAcceptor getIssueAcceptor() {
		return issueAcceptor;
	}

	public IChangeSerializer getChangeSerializer() {
		return changeSerializer;
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}
}
