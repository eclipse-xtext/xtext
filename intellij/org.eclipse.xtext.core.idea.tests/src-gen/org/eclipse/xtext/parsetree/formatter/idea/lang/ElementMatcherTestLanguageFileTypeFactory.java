package org.eclipse.xtext.parsetree.formatter.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class ElementMatcherTestLanguageFileTypeFactory extends FileTypeFactory {

	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.parsetree.formatter.idea.lang.ElementMatcherTestLanguageFileType.INSTANCE, org.eclipse.xtext.parsetree.formatter.idea.lang.ElementMatcherTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
