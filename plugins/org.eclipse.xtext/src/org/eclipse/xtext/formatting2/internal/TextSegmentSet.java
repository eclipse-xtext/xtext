/*******************************************************************************
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.formatting2.internal;

import java.util.IdentityHashMap;
import java.util.List;

import org.eclipse.xtext.formatting2.IMerger;
import org.eclipse.xtext.formatting2.ITextSegment;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public abstract class TextSegmentSet<T> implements Iterable<T> {

	private final Function<? super T, ? extends ITextSegment> regionGetter;
	private final Function<? super T, String> titleGetter;
	private final IdentityHashMap<T, RegionTrace> traces = new IdentityHashMap<T, RegionTrace>();

	public TextSegmentSet(Function<? super T, ? extends ITextSegment> region, Function<? super T, String> title) {
		super();
		this.regionGetter = region;
		this.titleGetter = title;
	}

	protected String getTitle(T t) {
		return titleGetter.apply(t);
	}

	public void add(T segment) {
		add(segment, null);
	}

	public abstract void add(T segment, IMerger<T> merger);

	protected ITextSegment getRegion(T t) {
		return regionGetter.apply(t);
	}

	public Function<? super T, ? extends ITextSegment> getRegionAccess() {
		return regionGetter;
	}

	public IdentityHashMap<T, RegionTrace> getTraces() {
		return traces;
	}

	protected void handleConflict(List<T> conflicts, Exception cause) {
		List<RegionTrace> causes = Lists.newArrayList();
		for (T t : conflicts) {
			RegionTrace exception = traces.get(t);
			if (exception != null)
				causes.add(exception);
		}
		throw new ConflictingRegionsException("Conflicting Regions", cause, causes);
	}

	protected boolean isConflict(ITextSegment region1, ITextSegment region2) {
		int offset1 = region1.getOffset();
		int length1 = region1.getLength();
		int offset2 = region2.getOffset();
		int length2 = region2.getLength();
		if (offset1 < offset2) {
			return offset1 + length1 > offset2;
		} else if (offset1 > offset2) {
			return offset2 + length2 > offset1;
		}
		return !(length1 == 0 ^ length2 == 0);
	}

	protected boolean isConflict(T t1, T t2) {
		return isConflict(getRegion(t1), getRegion(t2));
	}

	@Override
	public String toString() {
		TextRegionsToString toString = new TextRegionsToString();
		for (T t : this)
			toString.add(getRegion(t), getTitle(t));
		return toString.toString();
	}
}
