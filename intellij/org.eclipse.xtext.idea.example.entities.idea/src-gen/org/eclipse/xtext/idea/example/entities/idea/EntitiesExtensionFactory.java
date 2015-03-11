package org.eclipse.xtext.idea.example.entities.idea;

import org.eclipse.xtext.idea.example.entities.idea.lang.EntitiesLanguage;

import com.intellij.openapi.extensions.ExtensionFactory;

public class EntitiesExtensionFactory implements ExtensionFactory {

	public Object createInstance(final String factoryArgument, final String implementationClass) {
		Class<?> clazz;
		try {
			clazz = Class.forName(implementationClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Couldn't load "+implementationClass, e);
		}
		return EntitiesLanguage.INSTANCE.<Object> getInstance(clazz);
	}

}
