package org.eclipse.xtext.parsetree.formatter;

import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
import org.eclipse.xtext.formatting.impl.FormattingConfig;
import org.eclipse.xtext.parsetree.formatter.services.FormatterTestLanguageGrammarAccess;

public class FormatterTestConfig extends AbstractDeclarativeFormatter {

	@Override
	protected void configureFormatting(FormattingConfig c) {
		FormatterTestLanguageGrammarAccess f = (FormatterTestLanguageGrammarAccess) getGrammarAccess();

		c.setAutoLinewrap(30);
		
		// indent wrapped lines (lines exceeding autoLineWrap setting above) by one tab stop
		c.setWrappedLineIndentation(1);
		
		// TestLinewrap
		c.setLinewrap().after(f.getTestLinewrapAccess().getLinewrapKeyword_1());

		// Line
		c.setLinewrap().after(f.getLineAccess().getSemicolonKeyword_1());
		c.setNoSpace().before(f.getLineAccess().getSemicolonKeyword_1());

		// TestIndentation
		c.setIndentationIncrement().after(f.getTestIndentationAccess().getLeftCurlyBracketKeyword_2());
		c.setIndentationDecrement().before(f.getTestIndentationAccess().getRightCurlyBracketKeyword_4());
		c.setLinewrap().after(f.getTestIndentationAccess().getLeftCurlyBracketKeyword_2());
		c.setLinewrap().after(f.getTestIndentationAccess().getRightCurlyBracketKeyword_4());
		c.setLinewrap().after(f.getTestIndentationAccess().getSemiAssignment_5());
		c.setNoLinewrap().between(f.getTestIndentationAccess().getRightCurlyBracketKeyword_4(),
				f.getTestIndentationAccess().getSemiAssignment_5());
		c.setNoSpace().before(f.getTestIndentationAccess().getSemiAssignment_5());

		// Assign
		c.setNoSpace().around(f.getAssignAccess().getOpAssignment_1());
		c.setNoSpace().before(f.getAssignAccess().getCommaKeyword_3_1_0());

		// Meth
		c.setNoSpace().around(f.getMethAccess().getLeftParenthesisKeyword_2());
		c.setNoSpace().before(f.getMethAccess().getRightParenthesisKeyword_4());
		c.setNoSpace().before(f.getMethAccess().getCommaKeyword_3_1_0());
		c.setNoLinewrap().before(f.getMethAccess().getCommaKeyword_3_1_0());
		c.setIndentation(f.getMethAccess().getLeftParenthesisKeyword_2(), f.getMethAccess()
				.getRightParenthesisKeyword_4());

		// Param
		c.setNoLinewrap().around(f.getParamAccess().getColonKeyword_1());
		c.setNoSpace().around(f.getParamAccess().getColonKeyword_1());

		// Space
		c.setSpace("     ").after(f.getSpaceAccess().getSpaceKeyword_0());

		// TestLinewrapMinMax
		c.setLinewrap(2, 3, 5).after(f.getTestLinewrapMinMaxAccess().getWrapminmaxKeyword_1());

		// FqnObj
		c.setLinewrap().before(f.getFqnObjAccess().getNameFQNParserRuleCall_1_0());

		// FqnRef
		c.setLinewrap().before(f.getFqnRefAccess().getRefAssignment_1());

		// Enumeration
		c.setNoSpace().between(f.getEnumerationAccess().getValAssignment_2_1(),
				f.getEnumerationAccess().getCommaKeyword_2_0());
		c.setLinewrap().before(f.getEnumerationAccess().getValEnum1EnumRuleCall_2_1_0());

		// Datatypes
		c.setLinewrap().after(f.getDatatype1Rule());
		c.setLinewrap().before(f.getDatatype2Rule());
		c.setLinewrap().between(f.getDatatype2Rule(), f.getDatatype3Rule());
		c.setLinewrap().between(f.getDatatype3Rule(), f.getDatatypesAccess().getKw3Keyword_5());

		// comments
		c.setNoLinewrap().before(f.getSL_COMMENTRule());

		//		saveDebugGraphvizDiagram("Test.dot");
	}
}
