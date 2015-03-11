/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.ui.notification;

import static com.google.common.collect.Maps.*;

import java.util.Collection;
import java.util.Map;

/**
 * Represents FQNs of types and their corresponding topLevelType's FQN
 * As a delimiter for the FQN the . should be used
 * As a delimiter for nested classes the $ should be used
 * @author Holger Schill - Initial contribution and API
 * @since 2.8
 */
public class TypeNames {
	
	Map<String,String> delegate = newHashMap();
	
	/**
	 * Remove a typeName including it's corresponding topLevelTypeName
	 * @param typeName FQN of a type (Delimiter . and $ for nested types)
	 */
	public boolean remove(String typeName){
		return delegate.keySet().remove(typeName);
	}
	
	/**
	 * Returns all names of types in a Collection
	 * @return List of FQNs of types - (Delimiter . and $ for nested types)
	 */
	public Collection<String> getTypeNames(){
		return delegate.keySet();
	}
	
	/**
	 * Returns the corresponding toplevelType for a given typeName 
	 * @param typeName FQN of a type (Delimiter . and $ for nested types)
	 */
	public String getTopLevelTypeName(String typeName){
		return delegate.get(typeName);
	}
	
	/**
	 * Adds an entry of a FQN of a type -> FQN of the corresponding topLevelType
	 * @param typeName FQN of a type (Delimiter . and $ for nested types)
	 * @param topLevelTypenName FQN of a topLevelType (Delimiter . )
	 * 
	 */
	public void addTypeName(String typeName, String topLevelTypenName){
		delegate.put(typeName, topLevelTypenName);
	}
	
	/**
	 * Adds all given entries to the existing
	 */
	public void addAll(TypeNames typeNames){
		delegate.putAll(typeNames.delegate);
	}
}
