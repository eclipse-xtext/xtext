package org.eclipse.xtend.core.formatting

import org.eclipse.xtext.xbase.formatting.XbaseFormatterPreferenceKeys
import org.eclipse.xtext.xbase.formatting.BlankLineKey
import com.google.inject.Singleton
import org.eclipse.xtext.xbase.formatting.BooleanKey

@Singleton
class XtendFormatterPreferenceKeys extends XbaseFormatterPreferenceKeys {
	public static val blankLinesAfterPackageDecl = new BlankLineKey("blank.lines.after.packagedecl", 1)
	public static val blankLinesBetweenImports = new BlankLineKey("blank.lines.between.imports", 0)
	public static val blankLinesAfterImports = new BlankLineKey("blank.lines.after.imports", 1)
	public static val blankLinesBetweenClasses = new BlankLineKey("blank.lines.between.classes", 1)
	public static val blankLinesBeforeFirstMember = new BlankLineKey("blank.lines.before.first.member", 0)
	public static val blankLinesAfterLastMember = new BlankLineKey("blank.lines.after.last.member", 0)
	public static val blankLinesBetweenFields = new BlankLineKey("blank.lines.between.fields", 0)
	public static val blankLinesBetweenFieldsAndMethods = new BlankLineKey("blank.lines.between.fields.and.methods", 1)
	public static val blankLinesBetweenMethods = new BlankLineKey("blank.lines.between.methods", 1)
	public static val blankLinesBetweenEnumLiterals = new BlankLineKey("blank.lines.between.enum.literals", 0)
	
	public static val preserveOneLineMethods = new BooleanKey("preserve.one.line.methods", false)
}

