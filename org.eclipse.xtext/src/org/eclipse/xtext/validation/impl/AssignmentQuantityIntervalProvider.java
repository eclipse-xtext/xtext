/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.validation.impl;

import java.util.Set;

import org.eclipse.xtext.validation.IConcreteSyntaxConstraintProvider.ISyntaxConstraint;
import org.eclipse.xtext.validation.IAssignmentQuantityIntervalProvider;
import org.eclipse.xtext.validation.IAssignmentQuantityAllocator.IQuantities;

import com.google.inject.Singleton;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@Singleton
public class AssignmentQuantityIntervalProvider implements IAssignmentQuantityIntervalProvider {
	protected boolean containsUnavailableFeature(IQuantities ctx, ISyntaxConstraint child, String exclude,
			Set<ISyntaxConstraint> involved) {
		if (child.isOptional())
			return false;
		switch (child.getType()) {
			case ASSIGNMENT:
				if (exclude.equals(child.getAssignmentName()))
					return false;
				involved.add(child);
				return ctx.getAssignmentQuantity(child) == 0;
			case GROUP:
				for (ISyntaxConstraint a : child.getContents())
					if (containsUnavailableFeature(ctx, a, exclude, involved))
						return true;
				return false;
			case ALTERNATIVE:
				for (ISyntaxConstraint a : child.getContents())
					if (!containsUnavailableFeature(ctx, a, exclude, involved))
						return false;
				return true;
			case ACTION:
				if (child.getSemanticTypesToCheck() != null)
					return !child.getSemanticTypesToCheck().contains(ctx.getEObject().eClass());
				return false;
			default:
				return false;
		}
	}

	@Override
	public int getMax(IQuantities ctx, ISyntaxConstraint ass, Set<ISyntaxConstraint> involved, String excludeFeature) {
		int c = ass.isRoot() ? 1 : getMaxByParent(ctx, ass.getContainer(), ass, excludeFeature, involved);
		return ass.isMultiple() && c > 0 ? MAX : c;
	}

	protected int getMaxByParent(IQuantities ctx, ISyntaxConstraint ele, ISyntaxConstraint exclude,
			String excludeFeature, Set<ISyntaxConstraint> inv) {
		int max = ele.isRoot() ? (ele.isMultiple() ? MAX : 1) : getMaxByParent(ctx, ele.getContainer(), ele,
				excludeFeature, inv);
		if (max == 0)
			return 0;
		switch (ele.getType()) {
			case GROUP:
				if (ele.isMultiple())
					max = MAX;
				if (ele.isOptional() || max == MAX) {
					for (ISyntaxConstraint a : ele.getContents())
						if (a != exclude) {
							int count = getMaxForChild(ctx, a, inv);
							if (count != UNDEF && (count < max))
								max = count;
						}
				}
				return max == 0 && !ele.isOptional() ? 1 : max;
			case ALTERNATIVE:
				if (ele.isMultiple())
					return MAX;
				for (ISyntaxConstraint a : ele.getContents())
					if (a != exclude) {
						int count = getMinForChild(ctx, a, inv);
						if (count > 0)
							return 0;
					} else if (excludeFeature != null && containsUnavailableFeature(ctx, a, excludeFeature, inv))
						return 0;
				return max;
			default:
				return 1;
		}
	}

	protected int getMaxForChild(IQuantities ctx, ISyntaxConstraint child, Set<ISyntaxConstraint> involved) {
		if (child.getSemanticTypesToCheck() != null
				&& !child.getSemanticTypesToCheck().contains(ctx.getEObject().eClass()))
			return 0;
		if (child.isOptional())
			return MAX;
		switch (child.getType()) {
			case ASSIGNMENT:
				involved.add(child);
				return ctx.getAssignmentQuantity(child);
			case GROUP:
				int count1 = MAX;
				for (ISyntaxConstraint a : child.getContents()) {
					int c = getMaxForChild(ctx, a, involved);
					if (c != UNDEF && c < count1)
						count1 = c;
				}
				return count1;
			case ALTERNATIVE:
				int count2 = UNDEF;
				for (ISyntaxConstraint a : child.getContents()) {
					int c = getMaxForChild(ctx, a, involved);
					if (c == MAX)
						return MAX;
					if (c != UNDEF)
						count2 = count2 == UNDEF ? c : count2 + c;
				}
				return count2;
			case ACTION:
				return MAX;
			default:
				return UNDEF;
		}
	}

	@Override
	public int getMin(IQuantities ctx, ISyntaxConstraint assignment, Set<ISyntaxConstraint> involved) {
		if (assignment.isOptional())
			return 0;
		if (assignment.isRoot())
			return 1;
		return getMinByParent(ctx, assignment.getContainer(), assignment, involved);
	}

	protected int getMinByParent(IQuantities ctx, ISyntaxConstraint parent, ISyntaxConstraint exclude,
			Set<ISyntaxConstraint> involved) {
		switch (parent.getType()) {
			case GROUP:
				if (parent.isRoot() && !parent.isOptional() && !parent.isMultiple())
					return 1;
				int count1 = UNDEF;
				for (ISyntaxConstraint a : parent.getContents())
					if (a != exclude) {
						int c = getMinForChild(ctx, a, involved);
						if (c > count1) {
							count1 = c;
							break;
						}
					}
				if (parent.isOptional())
					return count1 == UNDEF ? 0 : count1;
				if (!parent.isRoot())
					return Math.max(getMinByParent(ctx, parent.getContainer(), parent, involved), count1);
				return UNDEF;
			case ALTERNATIVE:
				if (parent.isOptional())
					return 0;
				for (ISyntaxConstraint a : parent.getContents())
					if (a != exclude) {
						int count2 = getMinForChild(ctx, a, involved);
						if (count2 > 0)
							return 0;
					}
				if (!parent.isRoot())
					return getMinByParent(ctx, parent.getContainer(), parent, involved);
				return 1;
			default:
				return UNDEF;
		}
	}

	protected int getMinForChild(IQuantities ctx, ISyntaxConstraint child, Set<ISyntaxConstraint> involved) {
		if (child.getSemanticTypesToCheck() != null
				&& !child.getSemanticTypesToCheck().contains(ctx.getEObject().eClass()))
			return 0;
		int count = UNDEF;
		switch (child.getType()) {
			case ASSIGNMENT:
				involved.add(child);
				count = ctx.getAssignmentQuantity(child);
				break;
			case GROUP:
				for (ISyntaxConstraint a : child.getContents()) {
					int c = getMinForChild(ctx, a, involved);
					if (c > count)
						count = c;
				}
				break;
			case ALTERNATIVE:
				for (ISyntaxConstraint a : child.getContents()) {
					int c = getMinForChild(ctx, a, involved);
					count = count == UNDEF ? c : c + count;
				}
				break;
			case ACTION:
				return 1;
		}
		if (child.isMultiple() && count > 1)
			count = 1;
		return count;
	}

}
