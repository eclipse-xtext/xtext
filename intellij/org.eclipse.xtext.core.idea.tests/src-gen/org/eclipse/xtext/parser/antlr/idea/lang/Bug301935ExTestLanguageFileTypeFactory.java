package org.eclipse.xtext.parser.antlr.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class Bug301935ExTestLanguageFileTypeFactory extends FileTypeFactory {

	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.parser.antlr.idea.lang.Bug301935ExTestLanguageFileType.INSTANCE, org.eclipse.xtext.parser.antlr.idea.lang.Bug301935ExTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
