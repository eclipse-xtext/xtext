package org.eclipse.xtext.xbase.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class XtypeFileTypeFactory extends FileTypeFactory {

	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.xbase.idea.lang.XtypeFileType.INSTANCE, org.eclipse.xtext.xbase.idea.lang.XtypeFileType.DEFAULT_EXTENSION);
	}

}
