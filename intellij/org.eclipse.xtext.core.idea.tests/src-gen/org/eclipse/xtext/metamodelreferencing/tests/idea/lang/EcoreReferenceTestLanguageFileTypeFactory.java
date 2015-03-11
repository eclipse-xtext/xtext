package org.eclipse.xtext.metamodelreferencing.tests.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class EcoreReferenceTestLanguageFileTypeFactory extends FileTypeFactory {

	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.metamodelreferencing.tests.idea.lang.EcoreReferenceTestLanguageFileType.INSTANCE, org.eclipse.xtext.metamodelreferencing.tests.idea.lang.EcoreReferenceTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
