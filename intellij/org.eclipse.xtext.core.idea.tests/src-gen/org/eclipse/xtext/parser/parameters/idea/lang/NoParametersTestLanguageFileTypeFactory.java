package org.eclipse.xtext.parser.parameters.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class NoParametersTestLanguageFileTypeFactory extends FileTypeFactory {

	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.parser.parameters.idea.lang.NoParametersTestLanguageFileType.INSTANCE, org.eclipse.xtext.parser.parameters.idea.lang.AbstractNoParametersTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
