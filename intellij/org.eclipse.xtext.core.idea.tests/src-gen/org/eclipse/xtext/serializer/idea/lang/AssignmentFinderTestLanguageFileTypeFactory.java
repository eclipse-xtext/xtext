package org.eclipse.xtext.serializer.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class AssignmentFinderTestLanguageFileTypeFactory extends FileTypeFactory {

	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.serializer.idea.lang.AssignmentFinderTestLanguageFileType.INSTANCE, org.eclipse.xtext.serializer.idea.lang.AssignmentFinderTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
