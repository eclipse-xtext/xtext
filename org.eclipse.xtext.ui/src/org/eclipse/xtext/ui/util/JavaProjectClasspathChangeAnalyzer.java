/*******************************************************************************
 * Copyright (c) 2016, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.xtext.xbase.lib.util.ReflectExtensions;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

/**
 * Analyzes a given {@link IJavaElementDelta} for relevant classpath changes
 * 
 * @author Christian Dietrich - Initial contribution and API
 * @since 2.10
 */
public class JavaProjectClasspathChangeAnalyzer {
	
	private static final Logger logger = Logger.getLogger(JavaProjectClasspathChangeAnalyzer.class);
	
	private ReflectExtensions reflectExtensions = new ReflectExtensions();

	/**
	 * retrieves all Java Projects whose classpath was effected by the given delta
	 * 
	 * @param delta the delta to analyze
	 * @return a Set of the affected Java Projects
	 */
	public Set<IJavaProject> getJavaProjectsWithClasspathChange(IJavaElementDelta delta) {
		IJavaElement element = delta.getElement();
		if (element instanceof IPackageFragmentRoot) {
			if (isRelevantPackageFragmentRootChange(delta)) {
				IPackageFragmentRoot root = (IPackageFragmentRoot) element;
				return Collections.singleton(root.getJavaProject());
			}
		} else if (element instanceof IJavaModel) {
			return getJavaProjectsWithClasspathChange(delta.getAffectedChildren());
		} else if (element instanceof IJavaProject) {
			if (isClasspathChangeOnProject(delta)) {
				// filter out IJavaElementDelta.F_SOURCEATTACHED | IJavaElementDelta.F_SOURCEDETACHED only
				if (isAttachmentChangeOnly(delta)) {
					return  Collections.emptySet();
				}
				return Collections.singleton((IJavaProject) element);
			}
			return getJavaProjectsWithClasspathChange(delta.getAffectedChildren());
		}
		return Collections.emptySet();
	}

	/**
	 * Determines if the change i a classpath change on a project
	 * 
	 * @param delta the IJavaElementDelta to analyze. the deltas element must be an instance of IProject
	 */
	public boolean isClasspathChangeOnProject(IJavaElementDelta delta) {
		assert (delta.getElement() instanceof IJavaProject);
		return (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0;
	}

	/**
	 * Determines if a given change is a attachment change only
	 */
	public boolean isAttachmentChangeOnly(IJavaElementDelta delta) {
		// there must be at least one child delta
		if (delta.getAffectedChildren().length == 0) {
			return false;
		}
		// all child deltas have to be attachment changes only
		for (IJavaElementDelta child : delta.getAffectedChildren()) {
			if (!isAttachmentChangeFlagOnly(child)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * this method checks if the change is a source or javadoc (de)attachment change only.
	 * @since 2.30
	 */
	@Beta
	protected boolean isAttachmentChangeFlagOnly(IJavaElementDelta child) {
		int flags = child.getFlags();
		if (flags == 0) {
			return false;
		}
		int F_CLASSPATH_ATTRIBUTES = 0x800000; // TODO use IJavaElementDelta.F_CLASSPATH_ATTRIBUTES
		int allOtherFlags = flags & ~(IJavaElementDelta.F_SOURCEATTACHED | IJavaElementDelta.F_SOURCEDETACHED | F_CLASSPATH_ATTRIBUTES); 
		if (allOtherFlags != 0) {
			// there are other flags present
			return false;
		}
		if ((flags & F_CLASSPATH_ATTRIBUTES) != 0) {
			// this is also an attribute change
			// analyze child.getAttributeDeltas()
			try {
				// TODO dont use reflection when minimal target is 2023-03
				Object[] attributeDeltas = (Object[]) reflectExtensions.invoke(child, "getClasspathAttributeDeltas");
				for (Object attributeDelta : attributeDeltas) {
					String name = (String) reflectExtensions.invoke(attributeDelta, "getAttributeName");
					if (!IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME.equals(name)) {
						return false;
					}
				}
			} catch (ReflectiveOperationException e) {
				logger.error("Something went wrong with the reflection code to read classpath attribute deltas", e);
				return false;
			}
			// the only attribute change was a javadoc change
			return true;
		} else {
			// source attachment change only
			return true;
		}
	}
	
	/**
	 * determines if the delta is a relevant change on a IPackageFragmentRoot
	 * 
	 * @param delta
	 *            the IJavaElementDelta to analyze. the deltas element must be an instance of IPackageFragmentRoot
	 */
	public boolean isRelevantPackageFragmentRootChange(IJavaElementDelta delta) {
		IJavaElement element = delta.getElement();
		assert (element instanceof IPackageFragmentRoot);
		return delta.getKind() == IJavaElementDelta.REMOVED 
				|| delta.getKind() == IJavaElementDelta.ADDED
				|| (delta.getFlags() & IJavaElementDelta.F_REORDER) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0
				|| (
						(
							((IPackageFragmentRoot) element).isExternal() 
							|| ((IPackageFragmentRoot) element).isArchive()
						) && 
						(
							delta.getFlags() & // external folders change
							(IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED)
						) == delta.getFlags()
				);
	}

	private Set<IJavaProject> getJavaProjectsWithClasspathChange(IJavaElementDelta[] affectedChildren) {
		LinkedHashSet<IJavaProject> set = Sets.newLinkedHashSet();
		for (IJavaElementDelta delta : affectedChildren) {
			set.addAll(getJavaProjectsWithClasspathChange(delta));
		}
		return set;
	}

}
