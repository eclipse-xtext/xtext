package org.eclipse.xtext.parser.antlr.idea.lang;

import javax.swing.Icon;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;

public final class Bug301935TestLanguageFileType extends LanguageFileType {

	public static final Bug301935TestLanguageFileType INSTANCE = new Bug301935TestLanguageFileType();
	
	@NonNls 
	public static final String DEFAULT_EXTENSION = "bug301935testlanguage";

	private Bug301935TestLanguageFileType() {
		super(Bug301935TestLanguageLanguage.INSTANCE);
	}

	@Override
	public String getDefaultExtension() {
		return DEFAULT_EXTENSION;
	}

	@Override
	public String getDescription() {
		return "Bug301935TestLanguage files";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Bug301935TestLanguage";
	}

}
