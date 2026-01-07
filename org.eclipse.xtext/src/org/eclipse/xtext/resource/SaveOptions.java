/*******************************************************************************
 * Copyright (c) 2010, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable SaveOptions can be used to read and write options into the
 * map that is passed into {@link org.eclipse.emf.ecore.resource.Resource#save(Map)}.
 * Clients should create options by means of <code>SaveOptions.newBuilder()</code> and
 * subsequent calls to configure the result or <code>SaveOptions.defaultOptions()</code>.
 * 
 * The options map may be populated via {@link #toOptionsMap()} or {@link #addTo(Map)}.
 * 
 * Clients are free to extend this interface and the respective builder implementation.
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class SaveOptions {
	
	protected static final String KEY = SaveOptions.class.getName();
	
	private final boolean formatting;
	
	@Deprecated // see https://github.com/eclipse/xtext-core/issues/48
	private final boolean validating;

	protected SaveOptions(boolean formatting, boolean validating) {
		this.formatting = formatting;
		this.validating = validating;
	}
	
	/**
	 * Transparently handles the deprecated options that could be passed as
	 * map-entries to {@link org.eclipse.emf.ecore.resource.Resource#save(Map)} 
	 * and converts them to semantically equal {@link SaveOptions}. 
	 * 
	 * @param saveOptions the options-map or <code>null</code> if none.
	 * @return the options to use. Will never return <code>null</code>.
	 */
	@SuppressWarnings("deprecation")
	public static SaveOptions getOptions(Map<?, ?> saveOptions) {
		if (saveOptions == null || saveOptions.isEmpty())
			return defaultOptions();
		if (saveOptions.containsKey(KEY))
			return (SaveOptions) saveOptions.get(KEY);
		if (saveOptions.containsKey(XtextResource.OPTION_SERIALIZATION_OPTIONS))
			return ((org.eclipse.xtext.parsetree.reconstr.SerializerOptions) 
					saveOptions.get(XtextResource.OPTION_SERIALIZATION_OPTIONS)).toSaveOptions();
		if (Boolean.TRUE.equals(saveOptions.get(XtextResource.OPTION_FORMAT))) {
			return newBuilder().format().getOptions();
		}
		return defaultOptions();
	}
	
	/**
	 * Configures the save options into the given map. The method modifies the
	 * given parameter so it may not be an ImmutableMap or a read-only Map.
	 * @param saveOptions the options to be modified. May not be <code>null</code>. 
	 */
	public void addTo(Map<Object, Object> saveOptions) {
		saveOptions.put(KEY, this);
	}
	
	public Map<Object, Object> toOptionsMap() {
		Map<Object, Object> result = new HashMap<>(3);
		addTo(result);
		return result;
	}

	public boolean isFormatting() {
		return formatting;
	}

	@Deprecated // see https://github.com/eclipse/xtext-core/issues/48
	public boolean isValidating() {
		return validating;
	}
	
	@Override
	public String toString() {
		return "SaveOptions [formatting=" + formatting + ", validating=" + validating + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (formatting ? 1231 : 1237);
		result = prime * result + (validating ? 1231 : 1237);
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
		SaveOptions other = (SaveOptions) obj;
		if (formatting != other.formatting)
			return false;
		if (validating != other.validating)
			return false;
		return true;
	}

	public static Builder newBuilder() {
		return new Builder();
	}
	
	/**
	 * This is equal to <code>newBuilder().getOptions()</code>.
	 */
	public static SaveOptions defaultOptions() {
		return newBuilder().getOptions();
	}
	
	/**
	 * Allows to create new {@link SaveOptions} in a readable manner.
	 * Instead of <code>new SaveOptions(true, false)</code> the code 
	 * looks like <code>SaveOptions.newBuilder().format().noValidation().getOptions()</code>.
	 * @author Sebastian Zarnekow - Initial contribution and API
	 */
	public static class Builder {
		private boolean formatting;
		private boolean validating;

		protected Builder() {
			this.formatting = false;
			this.validating = true;
		}
		
		public Builder format() {
			this.formatting = true;
			return this;
		}
		
		@Deprecated // see https://github.com/eclipse/xtext-core/issues/48
		public Builder noValidation() {
			this.validating = false;
			return this;
		}
		
		public SaveOptions getOptions() {
			return new SaveOptions(formatting, validating);
		}
	}
	
}
