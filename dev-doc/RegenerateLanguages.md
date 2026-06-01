# Regenerate Languages

This document describes the necessary steps to regenerate the test- and production Xtext languages.

1. Execute the following `MWE2` workflows and Java Main-classes from Eclipse
	```
	org.eclipse.xtext.tests/src/org/eclipse/xtext/GenerateAllTestLanguages.mwe2
	org.eclipse.xtext.testlanguages/src/org/eclipse/xtext/testlanguages/GenerateTestLanguages.mwe2
	org.eclipse.xtext.ide.tests/testlang-src/org/eclipse/xtext/ide/tests/testlanguage/GenerateTestLanguage.mwe2
	org.eclipse.xtext.xtext.bootstrap/src/org/eclipse/xtext/xtext/bootstrap/GenerateXtext.mwe2
	org.eclipse.xtext.xbase/generator/org/eclipse/xtext/xbase/GenerateXbase.java
	org.eclipse.xtext.purexbase/src/org/eclipse/xtext/purexbase/GeneratePureXbase.mwe2
	org.eclipse.xtext.builder.standalone.tests/src/org/eclipse/xtext/builder/tests/GenerateBuilderTestLanguages.mwe2
	org.eclipse.xtext.common.types.tests/src/org/eclipse/xtext/common/types/xtext/ui/GenerateTypesTestLanguages.mwe2
	org.eclipse.xtext.extras.tests/src/org/eclipse/xtext/GenerateAllTestLanguages.mwe2
	org.eclipse.xtext.xbase.testlanguages/src/org/eclipse/xtext/xbase/testlanguages/GenerateXbaseTestLanguages.mwe2
	org.eclipse.xtext.builder.tests/src/org/eclipse/xtext/builder/tests/GenerateBuilderTestLanguages.mwe2
	org.eclipse.xtext.common.types.eclipse.tests/src/org/eclipse/xtext/common/types/xtext/ui/GenerateTypesTestLanguages.mwe2
	org.eclipse.xtext.ui.codetemplates/src/org/eclipse/xtext/ui/codetemplates/GenerateCodetemplateLanguages.mwe2
	org.eclipse.xtext.ui.tests/src/org/eclipse/xtext/ui/tests/GenerateTestLanguages.mwe2
	org.eclipse.xtext.xtext.ui.examples/projects/arithmetics/org.eclipse.xtext.example.arithmetics/src/org/eclipse/xtext/example/arithmetics/GenerateArithmetics.mwe2
	org.eclipse.xtext.xtext.ui.examples/projects/domainmodel/org.eclipse.xtext.example.domainmodel/src/org/eclipse/xtext/example/domainmodel/GenerateDomainmodel.mwe2
	org.eclipse.xtext.xtext.ui.examples/projects/fowlerdsl/org.eclipse.xtext.example.fowlerdsl/src/org/eclipse/xtext/example/fowlerdsl/GenerateStatemachine.mwe2
	org.eclipse.xtext.xtext.ui.examples/projects/homeautomation/org.eclipse.xtext.example.homeautomation/src/org/eclipse/xtext/example/homeautomation/GenerateRuleEngine.mwe2
	org.eclipse.xtext.xtext.ui.tests/generator/org/eclipse/xtext/xtext/ui/ecore2xtext/GenerateEcore2XtextTestGrammar.mwe2
	org.eclipse.xtext.xtext.ui.tests/src/org/eclipse/xtext/xtext/ui/GenerateTestLanguages.mwe2
	org.eclipse.xtend.core/src/org/eclipse/xtend/core/GenerateXtend.mwe2
	```
