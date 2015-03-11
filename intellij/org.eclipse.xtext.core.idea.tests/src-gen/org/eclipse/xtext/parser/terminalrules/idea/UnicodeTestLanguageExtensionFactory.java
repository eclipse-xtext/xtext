package org.eclipse.xtext.parser.terminalrules.idea;

import org.eclipse.xtext.parser.terminalrules.idea.lang.UnicodeTestLanguageLanguage;

import com.intellij.openapi.extensions.ExtensionFactory;

public class UnicodeTestLanguageExtensionFactory implements ExtensionFactory {

	public Object createInstance(final String factoryArgument, final String implementationClass) {
		Class<?> clazz;
		try {
			clazz = Class.forName(implementationClass);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Couldn't load "+implementationClass, e);
		}
		return UnicodeTestLanguageLanguage.INSTANCE.<Object> getInstance(clazz);
	}

}
