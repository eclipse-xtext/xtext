/*******************************************************************************
 * Copyright (c) 2010, 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.naming;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.emf.common.util.CommonUtil;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl.EObjectInputStream;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl.EObjectOutputStream;
import org.eclipse.xtext.util.Strings;

/**
 * A datatype for dealing with qualified names.
 * Instances are usually provided by a {@link IQualifiedNameProvider}.
 *
 * @author Jan Koehnlein - Initial contribution and API
 * @author Sebastian Zarnekow
 */
public class QualifiedName implements Comparable<QualifiedName> {

	private final int hash;

	private final String[] segments;

	private QualifiedName lowerCase;

	static boolean USE_INTERNING = Boolean.getBoolean("xtext.qn.interning");

	/**
	 * The single existing empty QualifiedName.
	 */
	public static final QualifiedName EMPTY = new QualifiedName() {
		@Override
		public QualifiedName append(QualifiedName relativeQualifiedName) {
			return relativeQualifiedName;
		}
		@Override
		public QualifiedName append(String segment) {
			return QualifiedName.create(segment);
		}
		@Override
		boolean hasLowerCase() {
			return true;
		}
		@Override
		public QualifiedName toLowerCase() {
			return this;
		}
		@Override
		public QualifiedName toUpperCase() {
			return this;
		}
		@Override
		public String toString(String delimiter) {
			return "";
		}
	};
	
	/**
	 * The Builder allows to create instances of QualifiedName in a slightly
	 * more efficient way by pre-allocating the underlying data for a known
	 * length.
	 * 
	 * Clients are supposed to create an instance of the builder with the predefined
	 * length and call {@link #add(String)} for each segment sequentially. The final
	 * {@link QualifiedName name} is obtained via {@link #build()}. 
	 * 
	 * @since 2.15
	 */
	public static final class Builder {
		private final String[] segments;
		private int next;

		public Builder(int size) {
			this.segments = new String[size];
			this.next = 0;
		}
		
		public void add(String segment) {
			segments[next++] = intern(segment);
		}
		
		public QualifiedName build() {
			if (next != segments.length) {
				throw new IllegalStateException("Unexpected number of segments");
			}
			return new QualifiedName(segments);
		}
	}

	/**
	 * Low-level factory method. Consider using a {@link IQualifiedNameConverter} instead.
	 *
	 * @param segments the segments of the to-be-created qualified name.
	 * 	May be <code>null</code>, but may not contain <code>null</code> entries.
	 * @return a {@link QualifiedName}. Never <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if any of the segments is null
	 */
	public static QualifiedName create(String... segments) {
		if (segments == null || segments.length == 0) {
			return EMPTY;
		}
		if (segments.length == 1) {
			return create(segments[0]);
		}
		String [] newArray = new String [segments.length];
		for (int i = 0; i < segments.length; i++) {
			String string = segments[i];
			if (string == null) {
				throw new IllegalArgumentException("Segment cannot be null");
			}
			newArray[i] = intern(string);
		}
		return new QualifiedName(newArray);
	}

	/**
	 * Returns string internal instance from string pool, if {@link #USE_INTERNING} is true, or the same object
	 * otherwise.
	 * <p>
	 * {@link #USE_INTERNING} can be set by using the system property {@code xtext.qn.interning} or by calling
	 * {@link QualifiedNameInterning#enable()} / {@link QualifiedNameInterning#disable()}.<br/>
	 * The default value is <code>false</code>.
	 */
	private static String intern(String string) {
		return USE_INTERNING ? CommonUtil.intern(string) : string;
	}

	/**
	 * Internal low level factory method.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 2.4
	 */
	public static QualifiedName createFromStream(EObjectInputStream eObjectInputStream) throws IOException{
		int segmentCount = eObjectInputStream.readCompressedInt();
		if (segmentCount == 0) {
			return QualifiedName.EMPTY;
		}
		// lowercase QN serialize a 'null' value at index 0 and
		String firstSegment = eObjectInputStream.readSegmentedString();
		boolean lowerCase = false;
		if (firstSegment == null) {
			lowerCase = true;
			// first was null, read another string which is the actual first segment
			firstSegment = eObjectInputStream.readSegmentedString();
			if(firstSegment == null){
				throw new IllegalStateException("Read unexpected first segment from object stream");
			}
		}

		String[] segments = readSegmentArray(eObjectInputStream, segmentCount, firstSegment);
		if (lowerCase) {
			return new QualifiedNameLowerCase(segments);
		} else {
			return new QualifiedName(segments);
		}
	}

	private static String[] readSegmentArray(EObjectInputStream from, int count, String first) throws IOException {
		String[] segments = new String[count];
		segments[0] = intern(first);
		for (int i = 1; i < count; i++) {
			String segment = from.readSegmentedString();
			if(segment == null){
				throw new IllegalStateException("Read unexpected segment (#" + i + ") from object stream");
			}
			segments[i] = intern(segment);
		}
		return segments;
	}

	/**
	 * Internal low level serialization of QualifiedNames.
	 * @since 2.4
	 */
	public void writeToStream(EObjectOutputStream eObjectOutputStream) throws IOException {
		int segmentCount = getSegmentCount();
		eObjectOutputStream.writeCompressedInt(segmentCount);
		for (int i = 0; i < segmentCount; ++i) {
			eObjectOutputStream.writeSegmentedString(getSegment(i));
		}
	}

	/**
	 * Low-level factory method. Consider using a {@link IQualifiedNameConverter} instead.
	 *
	 * @param segments
	 *            the segments of the to-be-created qualified name. May be <code>null</code>, but may not contain
	 *            <code>null</code> entries.
	 * @return a {@link QualifiedName}. Never <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if any of the segments is null
	 * @since 2.3
	 */
	public static QualifiedName create(List<String> segments) {
		if (segments == null || segments.isEmpty())
			return QualifiedName.EMPTY;
		if (segments.size() == 1) {
			String singleSegment = segments.get(0);
			return QualifiedName.create(singleSegment);
		}
		String[] segmentArray = new String[segments.size()];
		for (int i = 0; i < segments.size(); i++) {
			String string = segments.get(i);
			if (string == null) {
				throw new IllegalArgumentException("Segment cannot be null");
			}
			segmentArray[i] = intern(string);
		}
		return new QualifiedName(segmentArray);
	}

	/**
	 * Low-level factory method. Consider using a {@link IQualifiedNameConverter} instead.
	 *
	 * @param singleSegment
	 *            the single segment of the newly created qualified name
	 * @exception IllegalArgumentException
	 *                if the singleSegment is null
	 * @since 2.3
	 */
	public static QualifiedName create(String singleSegment) {
		if (singleSegment == null) {
			throw new IllegalArgumentException("Segment cannot be null");
		}
		return new QualifiedName(intern(singleSegment));
	}

	/**
	 * Wraps a name function to return a qualified name. Returns null if the name function returns null.
	 * @deprecated Instead use {@link #wrapper(Function)}
	 */
	@Deprecated(since = "2.35.0", forRemoval = true)
	public static <F> com.google.common.base.Function<F, QualifiedName> wrapper(
			com.google.common.base.Function<F, String> nameFunction) {
		return wrapper((Function<F, String>) nameFunction)::apply;
	}

	/**
	 * Wraps a name function to return a qualified name. Returns null if the name function returns null.
	 */
	public static <F> Function<F, QualifiedName> wrapper(final Function<F, String> nameFunction) {
		return from -> {
			String name = nameFunction.apply(from);
			if (name == null) {
				return null;
			}
			return QualifiedName.create(name);
		};
	}

	protected QualifiedName(String... segments) {
		if (segments == null || segments.length == 0)
			this.segments = Strings.EMPTY_ARRAY;
		else
			this.segments = segments;
		hash = Arrays.hashCode(this.segments);
	}

	public boolean isEmpty() {
		return segments.length == 0;
	}

	public List<String> getSegments() {
		return Collections.unmodifiableList(Arrays.asList(segments));
	}

	public int getSegmentCount() {
		return segments.length;
	}

	public String getSegment(int index) {
		return segments[index];
	}

	public String getLastSegment() {
		return segments[segments.length - 1];
	}

	public String getFirstSegment() {
		return segments[0];
	}

	public QualifiedName append(String segment) {
		if (segment == null) {
			throw new IllegalArgumentException("Segment cannot be null");
		}
		String[] newSegments = new String[getSegmentCount() + 1];
		System.arraycopy(segments, 0, newSegments, 0, segments.length);
		newSegments[segments.length] = intern(segment);
		return new QualifiedName(newSegments);
	}

	public QualifiedName append(QualifiedName relativeQualifiedName) {
		String[] newSegments = new String[getSegmentCount() + relativeQualifiedName.getSegmentCount()];
		for (int i = 0; i < getSegmentCount(); ++i)
			newSegments[i] = getSegment(i);
		for (int i = 0; i < relativeQualifiedName.getSegmentCount(); ++i)
			newSegments[i + getSegmentCount()] = relativeQualifiedName.getSegment(i);
		return new QualifiedName(newSegments);
	}

	public QualifiedName skipFirst(int skipCount) {
		if (skipCount == getSegmentCount()) {
			return EMPTY;
		}
		if (skipCount == 0) {
			return this;
		}
		if (skipCount > getSegmentCount() || skipCount < 0) {
			throw new IllegalArgumentException("Cannot skip " + skipCount + " fragments from QualifiedName with "
					+ getSegmentCount() + " segments");
		}
		String[] newSegments = new String[segments.length - skipCount];
		System.arraycopy(segments, skipCount, newSegments, 0, newSegments.length);
		return new QualifiedName(newSegments);
	}

	public QualifiedName skipLast(int skipCount) {
		if (skipCount == getSegmentCount()) {
			return EMPTY;
		}
		if (skipCount == 0) {
			return this;
		}
		if (skipCount > getSegmentCount() || skipCount < 0) {
			throw new IllegalArgumentException("Cannot skip " + skipCount + " fragments from QualifiedName with "
					+ getSegmentCount() + " segments");
		}
		String[] newSegments = new String[segments.length - skipCount];
		System.arraycopy(segments, 0, newSegments, 0, newSegments.length);
		return new QualifiedName(newSegments);
	}

	public QualifiedName toLowerCase() {
		if (lowerCase != null)
			return lowerCase;
		String[] newSegments = new String[segments.length];
		boolean isLowerCase = true;
		for (int i = 0; i < getSegmentCount(); ++i) {
			String segment = segments[i];
			String lowerCaseSegment = segment.toLowerCase();
			isLowerCase = isLowerCase && segment == lowerCaseSegment;
			newSegments[i] = intern(lowerCaseSegment);
		}
		if (isLowerCase) {
			lowerCase = this;
		} else {
			lowerCase = new QualifiedNameLowerCase(newSegments);
		}
		return lowerCase;
	}

	private static class QualifiedNameLowerCase extends QualifiedName {
		public QualifiedNameLowerCase(String[] segments) {
			super(segments);
		}
		@Override
		public QualifiedName toLowerCase() {
			return this;
		}

		@Override
		boolean hasLowerCase() {
			return true;
		}

		/**
		 * We serialize a segmentCount + 1 and a dummy null value as the first entry.
		 * This is used to retrieve the information about lowercase QN in {@link QualifiedName#createFromStream(EObjectInputStream)}
		 */
		@Override
		public void writeToStream(EObjectOutputStream eObjectOutputStream) throws IOException {
			int segmentCount = getSegmentCount();
			eObjectOutputStream.writeCompressedInt(segmentCount);
			// indicator for lowercase instance
			eObjectOutputStream.writeSegmentedString(null);
			for (int i = 0; i < segmentCount; ++i) {
				eObjectOutputStream.writeSegmentedString(getSegment(i));
			}
		}
	}

	public QualifiedName toUpperCase() {
		String[] newSegments = new String[getSegmentCount()];
		for (int i = 0; i < getSegmentCount(); ++i)
			newSegments[i] = intern(segments[i].toUpperCase());
		QualifiedName result = new QualifiedName(newSegments);
		result.lowerCase = this.lowerCase;
		return result;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj==this)
			return true;
		if (obj instanceof QualifiedName) {
			QualifiedName other = (QualifiedName) obj;
			if (hash != other.hash)
				return false;
			return Arrays.equals(segments, other.segments);
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if this instance can provide a ready to use
	 * lowercase representation.
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	boolean hasLowerCase() {
		return lowerCase != null;
	}

	public boolean equalsIgnoreCase(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof QualifiedName) {
			// if both instances can provide lowerCase representations
			// use their equals method
			QualifiedName other = (QualifiedName) obj;
			if (hasLowerCase() && other.hasLowerCase()) {
				return toLowerCase().equals(other.toLowerCase());
			}
			// check the length
			int segmentCount = getSegmentCount();
			if (segmentCount != other.getSegmentCount()) {
				return false;
			}
			// compare by segment
			for (int i = 0; i < segmentCount; i++) {
				if (!getSegment(i).equalsIgnoreCase(other.getSegment(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public int compareTo(QualifiedName qualifiedName) {
		return compareTo(qualifiedName, false);
	}

	public int compareToIgnoreCase(QualifiedName qualifiedName) {
		return compareTo(qualifiedName, true);
	}

	protected int compareTo(QualifiedName qualifiedName, boolean ignoreCase) {
		if (ignoreCase) {
			for (int i = 0, upTo = Math.min(getSegmentCount(), qualifiedName.getSegmentCount()); i < upTo; ++i) {
				int result = getSegment(i).compareToIgnoreCase(qualifiedName.getSegment(i));
				if (result != 0)
					return result;
			}
		} else {
			for (int i = 0, upTo = Math.min(getSegmentCount(), qualifiedName.getSegmentCount()); i < upTo; ++i) {
				int result = getSegment(i).compareTo(qualifiedName.getSegment(i));
				if (result != 0)
					return result;
			}
		}
		// with Java7 this should probably read
		// return Integer.compare(getSegmentCount(), qualifiedName.getSegmentCount())
		return getSegmentCount() - qualifiedName.getSegmentCount();
	}

	public boolean startsWith(QualifiedName prefix) {
		return startsWith(prefix, false);
	}

	public boolean startsWithIgnoreCase(QualifiedName prefix) {
		return startsWith(prefix, true);
	}

	protected boolean startsWith(QualifiedName prefix, boolean ignoreCase) {
		Objects.requireNonNull(prefix, "prefix must not be null");
		
		if (prefix.getSegmentCount() > getSegmentCount()) {
			return false;
		}
		if (ignoreCase) {
			for (int i = 0; i < prefix.getSegmentCount(); ++i) {
				if (!this.getSegment(i).equalsIgnoreCase(prefix.getSegment(i)))
					return false;
			}
			return true;
		} else {
			for (int i = 0; i < prefix.getSegmentCount(); ++i) {
				if(!getSegment(i).equals(prefix.getSegment(i)))
					return false;
			}
			return true;
		}
	}

	/**
	 * Returns a canonical String representation of this using '.' as namespace delimiter. For language specific
	 * conversion taking the concrete syntax into account see {@link IQualifiedNameConverter#toString(QualifiedName)}.
	 */
	@Override
	public String toString() {
		return toString(".");
	}

	/**
	 * Returns a String representation of this using {@code delimiter} as namespace delimiter.
	 * @param delimiter the delimiter to use. <code>null</code> will be represented as the String "<code>null</code>".
	 * @return the concatenated segments joined with the given {@code delimiter}
	 * @since 2.3
	 */
	public String toString(String delimiter) {
		int segmentCount = getSegmentCount();
		switch (segmentCount) {
			case 0: return "";
			case 1: return getFirstSegment();
			default:
				StringBuilder builder = new StringBuilder();
				builder.append(getFirstSegment());
				for (int i = 1; i < segmentCount; i++) {
					builder.append(delimiter);
					builder.append(segments[i]);
				}
				return builder.toString();
		}
	}
}
