package org.eclipse.xtend.core.idea.lang;

import javax.swing.Icon;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;

public final class XtendFileType extends LanguageFileType {

	public static final XtendFileType INSTANCE = new XtendFileType();
	
	@NonNls 
	public static final String DEFAULT_EXTENSION = "xtend";

	private XtendFileType() {
		super(XtendLanguage.INSTANCE);
	}

	@Override
	public String getDefaultExtension() {
		return DEFAULT_EXTENSION;
	}

	@Override
	public String getDescription() {
		return "Xtend files";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Xtend";
	}

}
