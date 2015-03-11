package org.eclipse.xtext.parser.antlr.idea.lang;

import org.eclipse.xtext.idea.lang.AbstractXtextLanguage;

import com.google.inject.Injector;

public final class Bug296889ExTestLanguageLanguage extends AbstractXtextLanguage {

	public static final Bug296889ExTestLanguageLanguage INSTANCE = new Bug296889ExTestLanguageLanguage();

	private Injector injector;

	private Bug296889ExTestLanguageLanguage() {
		super("org.eclipse.xtext.parser.antlr.Bug296889ExTestLanguage");
		this.injector = new org.eclipse.xtext.parser.antlr.idea.Bug296889ExTestLanguageStandaloneSetupIdea().createInjectorAndDoEMFRegistration();
		
	}

	@Override
	protected Injector getInjector() {
		return injector;
	}
}
