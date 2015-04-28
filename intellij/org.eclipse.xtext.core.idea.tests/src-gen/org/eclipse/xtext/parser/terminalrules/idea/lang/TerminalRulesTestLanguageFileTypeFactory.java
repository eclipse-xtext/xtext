package org.eclipse.xtext.parser.terminalrules.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

public class TerminalRulesTestLanguageFileTypeFactory extends FileTypeFactory {

	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer) {
		consumer.consume(org.eclipse.xtext.parser.terminalrules.idea.lang.TerminalRulesTestLanguageFileType.INSTANCE, org.eclipse.xtext.parser.terminalrules.idea.lang.TerminalRulesTestLanguageFileType.DEFAULT_EXTENSION);
	}

}
