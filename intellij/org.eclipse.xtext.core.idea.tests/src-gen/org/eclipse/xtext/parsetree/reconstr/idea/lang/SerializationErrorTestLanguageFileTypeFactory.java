package org.eclipse.xtext.parsetree.reconstr.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class SerializationErrorTestLanguageFileTypeFactory extends FileTypeFactory {

	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.parsetree.reconstr.idea.lang.SerializationErrorTestLanguageFileType.INSTANCE, org.eclipse.xtext.parsetree.reconstr.idea.lang.SerializationErrorTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
