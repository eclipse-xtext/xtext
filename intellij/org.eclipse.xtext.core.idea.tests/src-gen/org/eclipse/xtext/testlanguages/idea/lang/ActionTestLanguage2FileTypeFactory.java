package org.eclipse.xtext.testlanguages.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class ActionTestLanguage2FileTypeFactory extends FileTypeFactory {

	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.testlanguages.idea.lang.ActionTestLanguage2FileType.INSTANCE, org.eclipse.xtext.testlanguages.idea.lang.ActionTestLanguage2FileType.DEFAULT_EXTENSION);
	}

}
