package org.eclipse.xtend.core.formatting2;

import com.google.inject.Singleton;
import org.eclipse.xtext.preferences.BooleanKey;
import org.eclipse.xtext.xbase.formatting2.BlankLineKey;
import org.eclipse.xtext.xbase.formatting2.XbaseFormatterPreferenceKeys;

@Singleton
@SuppressWarnings("all")
public class XtendFormatterPreferenceKeys extends XbaseFormatterPreferenceKeys {
  public final static BlankLineKey blankLinesAfterPackageDecl = new BlankLineKey("blank.lines.after.packagedecl", Integer.valueOf(1));
  
  public final static BlankLineKey blankLinesBetweenClasses = new BlankLineKey("blank.lines.between.classes", Integer.valueOf(1));
  
  public final static BlankLineKey blankLinesBeforeFirstMember = new BlankLineKey("blank.lines.before.first.member", Integer.valueOf(0));
  
  public final static BlankLineKey blankLinesAfterLastMember = new BlankLineKey("blank.lines.after.last.member", Integer.valueOf(0));
  
  public final static BlankLineKey blankLinesBetweenFields = new BlankLineKey("blank.lines.between.fields", Integer.valueOf(0));
  
  public final static BlankLineKey blankLinesBetweenFieldsAndMethods = new BlankLineKey("blank.lines.between.fields.and.methods", Integer.valueOf(1));
  
  public final static BlankLineKey blankLinesBetweenMethods = new BlankLineKey("blank.lines.between.methods", Integer.valueOf(1));
  
  public final static BlankLineKey blankLinesBetweenEnumLiterals = new BlankLineKey("blank.lines.between.enum.literals", Integer.valueOf(0));
  
  public final static BooleanKey keepOneLineMethods = new BooleanKey("keep.one.line.methods", Boolean.valueOf(true));
}
