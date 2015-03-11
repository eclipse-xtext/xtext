package org.eclipse.xtext.serializer.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.serializer.idea.lang.AssignmentFinderTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.serializer.services.AssignmentFinderTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalAssignmentFinderTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_TERMINAL1", "RULE_TERMINAL2", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'#00'", "'#01'", "'#02'", "'#03'", "'#04'", "'#05'", "'#06'", "'#07'", "'#08'", "'#09'", "'kw1'", "'kw2'", "'%foo'", "'true'", "'false'", "'lit3'", "'lit1'", "'lit2'"
    };
    public static final int RULE_ID=6;
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=12;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int RULE_TERMINAL1=4;
    public static final int RULE_TERMINAL2=5;
    public static final int RULE_SL_COMMENT=10;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=9;
    public static final int T__30=30;
    public static final int T__19=19;
    public static final int RULE_STRING=8;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_INT=7;
    public static final int RULE_WS=11;

    // delegates
    // delegators


        public PsiInternalAssignmentFinderTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalAssignmentFinderTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalAssignmentFinderTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g"; }



    	protected AssignmentFinderTestLanguageGrammarAccess grammarAccess;

    	protected AssignmentFinderTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalAssignmentFinderTestLanguageParser(PsiBuilder builder, TokenStream input, AssignmentFinderTestLanguageElementTypeProvider elementTypeProvider, AssignmentFinderTestLanguageGrammarAccess grammarAccess) {
    		this(input);
    		setPsiBuilder(builder);
        	this.grammarAccess = grammarAccess;
    		this.elementTypeProvider = elementTypeProvider;
    	}

    	@Override
    	protected String getFirstRuleName() {
    		return "Model";
    	}




    // $ANTLR start "entryRuleModel"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:53:2: ruleModel EOF
            {
             markComposite(elementTypeProvider.getModelElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleModel_in_entryRuleModel54);
            ruleModel();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleModel57); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:58:1: ruleModel : ( (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) ) | (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) ) | (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) ) | (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) ) | (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) ) | (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) ) | (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) ) | (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) ) | (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) ) | (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) ) ) ;
    public final void ruleModel() throws RecognitionException {
        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_18=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:58:10: ( ( (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) ) | (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) ) | (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) ) | (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) ) | (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) ) | (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) ) | (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) ) | (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) ) | (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) ) | (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:59:2: ( (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) ) | (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) ) | (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) ) | (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) ) | (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) ) | (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) ) | (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) ) | (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) ) | (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) ) | (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:59:2: ( (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) ) | (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) ) | (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) ) | (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) ) | (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) ) | (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) ) | (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) ) | (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) ) | (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) ) | (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) ) )
            int alt1=10;
            switch ( input.LA(1) ) {
            case 13:
                {
                alt1=1;
                }
                break;
            case 14:
                {
                alt1=2;
                }
                break;
            case 15:
                {
                alt1=3;
                }
                break;
            case 16:
                {
                alt1=4;
                }
                break;
            case 17:
                {
                alt1=5;
                }
                break;
            case 18:
                {
                alt1=6;
                }
                break;
            case 19:
                {
                alt1=7;
                }
                break;
            case 20:
                {
                alt1=8;
                }
                break;
            case 21:
                {
                alt1=9;
                }
                break;
            case 22:
                {
                alt1=10;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:60:3: (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:60:3: (otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:61:4: otherlv_0= '#00' ( (lv_keywordVal_1_0= ruleKeywordVal ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitZeroKeyword_0_0ElementType());
                    			
                    otherlv_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleModel82); 

                    				doneLeaf(otherlv_0);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:68:4: ( (lv_keywordVal_1_0= ruleKeywordVal ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:69:5: (lv_keywordVal_1_0= ruleKeywordVal )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:69:5: (lv_keywordVal_1_0= ruleKeywordVal )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:70:6: lv_keywordVal_1_0= ruleKeywordVal
                    {

                    						markComposite(elementTypeProvider.getModel_KeywordValKeywordValParserRuleCall_0_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleKeywordVal_in_ruleModel114);
                    ruleKeywordVal();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:81:3: (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:81:3: (otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:82:4: otherlv_2= '#01' ( (lv_terminalVal_3_0= ruleTerminalVal ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitOneKeyword_1_0ElementType());
                    			
                    otherlv_2=(Token)match(input,14,FollowSets000.FOLLOW_14_in_ruleModel160); 

                    				doneLeaf(otherlv_2);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:89:4: ( (lv_terminalVal_3_0= ruleTerminalVal ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:90:5: (lv_terminalVal_3_0= ruleTerminalVal )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:90:5: (lv_terminalVal_3_0= ruleTerminalVal )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:91:6: lv_terminalVal_3_0= ruleTerminalVal
                    {

                    						markComposite(elementTypeProvider.getModel_TerminalValTerminalValParserRuleCall_1_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleTerminalVal_in_ruleModel192);
                    ruleTerminalVal();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 3 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:102:3: (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:102:3: (otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:103:4: otherlv_4= '#02' ( (lv_enumVal_5_0= ruleEnumVal ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitTwoKeyword_2_0ElementType());
                    			
                    otherlv_4=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleModel238); 

                    				doneLeaf(otherlv_4);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:110:4: ( (lv_enumVal_5_0= ruleEnumVal ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:111:5: (lv_enumVal_5_0= ruleEnumVal )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:111:5: (lv_enumVal_5_0= ruleEnumVal )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:112:6: lv_enumVal_5_0= ruleEnumVal
                    {

                    						markComposite(elementTypeProvider.getModel_EnumValEnumValParserRuleCall_2_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleEnumVal_in_ruleModel270);
                    ruleEnumVal();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 4 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:123:3: (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:123:3: (otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:124:4: otherlv_6= '#03' ( (lv_keywordBool_7_0= ruleKeywordBool ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitThreeKeyword_3_0ElementType());
                    			
                    otherlv_6=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleModel316); 

                    				doneLeaf(otherlv_6);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:131:4: ( (lv_keywordBool_7_0= ruleKeywordBool ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:132:5: (lv_keywordBool_7_0= ruleKeywordBool )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:132:5: (lv_keywordBool_7_0= ruleKeywordBool )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:133:6: lv_keywordBool_7_0= ruleKeywordBool
                    {

                    						markComposite(elementTypeProvider.getModel_KeywordBoolKeywordBoolParserRuleCall_3_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleKeywordBool_in_ruleModel348);
                    ruleKeywordBool();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 5 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:144:3: (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:144:3: (otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:145:4: otherlv_8= '#04' ( (lv_terminalBool_9_0= ruleTerminalBool ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitFourKeyword_4_0ElementType());
                    			
                    otherlv_8=(Token)match(input,17,FollowSets000.FOLLOW_17_in_ruleModel394); 

                    				doneLeaf(otherlv_8);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:152:4: ( (lv_terminalBool_9_0= ruleTerminalBool ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:153:5: (lv_terminalBool_9_0= ruleTerminalBool )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:153:5: (lv_terminalBool_9_0= ruleTerminalBool )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:154:6: lv_terminalBool_9_0= ruleTerminalBool
                    {

                    						markComposite(elementTypeProvider.getModel_TerminalBoolTerminalBoolParserRuleCall_4_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleTerminalBool_in_ruleModel426);
                    ruleTerminalBool();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 6 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:165:3: (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:165:3: (otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:166:4: otherlv_10= '#05' ( (lv_enumBool_11_0= ruleEnumBool ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitFiveKeyword_5_0ElementType());
                    			
                    otherlv_10=(Token)match(input,18,FollowSets000.FOLLOW_18_in_ruleModel472); 

                    				doneLeaf(otherlv_10);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:173:4: ( (lv_enumBool_11_0= ruleEnumBool ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:174:5: (lv_enumBool_11_0= ruleEnumBool )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:174:5: (lv_enumBool_11_0= ruleEnumBool )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:175:6: lv_enumBool_11_0= ruleEnumBool
                    {

                    						markComposite(elementTypeProvider.getModel_EnumBoolEnumBoolParserRuleCall_5_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleEnumBool_in_ruleModel504);
                    ruleEnumBool();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 7 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:186:3: (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:186:3: (otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:187:4: otherlv_12= '#06' ( (lv_mixedBool_13_0= ruleMixedBool ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitSixKeyword_6_0ElementType());
                    			
                    otherlv_12=(Token)match(input,19,FollowSets000.FOLLOW_19_in_ruleModel550); 

                    				doneLeaf(otherlv_12);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:194:4: ( (lv_mixedBool_13_0= ruleMixedBool ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:195:5: (lv_mixedBool_13_0= ruleMixedBool )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:195:5: (lv_mixedBool_13_0= ruleMixedBool )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:196:6: lv_mixedBool_13_0= ruleMixedBool
                    {

                    						markComposite(elementTypeProvider.getModel_MixedBoolMixedBoolParserRuleCall_6_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleMixedBool_in_ruleModel582);
                    ruleMixedBool();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 8 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:207:3: (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:207:3: (otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:208:4: otherlv_14= '#07' ( (lv_mixedValue_15_0= ruleMixedValue ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitSevenKeyword_7_0ElementType());
                    			
                    otherlv_14=(Token)match(input,20,FollowSets000.FOLLOW_20_in_ruleModel628); 

                    				doneLeaf(otherlv_14);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:215:4: ( (lv_mixedValue_15_0= ruleMixedValue ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:216:5: (lv_mixedValue_15_0= ruleMixedValue )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:216:5: (lv_mixedValue_15_0= ruleMixedValue )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:217:6: lv_mixedValue_15_0= ruleMixedValue
                    {

                    						markComposite(elementTypeProvider.getModel_MixedValueMixedValueParserRuleCall_7_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleMixedValue_in_ruleModel660);
                    ruleMixedValue();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 9 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:228:3: (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:228:3: (otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:229:4: otherlv_16= '#08' ( (lv_containmentRef_17_0= ruleContainmentRef ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitEightKeyword_8_0ElementType());
                    			
                    otherlv_16=(Token)match(input,21,FollowSets000.FOLLOW_21_in_ruleModel706); 

                    				doneLeaf(otherlv_16);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:236:4: ( (lv_containmentRef_17_0= ruleContainmentRef ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:237:5: (lv_containmentRef_17_0= ruleContainmentRef )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:237:5: (lv_containmentRef_17_0= ruleContainmentRef )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:238:6: lv_containmentRef_17_0= ruleContainmentRef
                    {

                    						markComposite(elementTypeProvider.getModel_ContainmentRefContainmentRefParserRuleCall_8_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleContainmentRef_in_ruleModel738);
                    ruleContainmentRef();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 10 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:249:3: (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:249:3: (otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:250:4: otherlv_18= '#09' ( (lv_crossRef_19_0= ruleCrossRef ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_NumberSignDigitZeroDigitNineKeyword_9_0ElementType());
                    			
                    otherlv_18=(Token)match(input,22,FollowSets000.FOLLOW_22_in_ruleModel784); 

                    				doneLeaf(otherlv_18);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:257:4: ( (lv_crossRef_19_0= ruleCrossRef ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:258:5: (lv_crossRef_19_0= ruleCrossRef )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:258:5: (lv_crossRef_19_0= ruleCrossRef )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:259:6: lv_crossRef_19_0= ruleCrossRef
                    {

                    						markComposite(elementTypeProvider.getModel_CrossRefCrossRefParserRuleCall_9_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleCrossRef_in_ruleModel816);
                    ruleCrossRef();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleKeywordVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:273:1: entryRuleKeywordVal : ruleKeywordVal EOF ;
    public final void entryRuleKeywordVal() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:273:20: ( ruleKeywordVal EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:274:2: ruleKeywordVal EOF
            {
             markComposite(elementTypeProvider.getKeywordValElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleKeywordVal_in_entryRuleKeywordVal854);
            ruleKeywordVal();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleKeywordVal857); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleKeywordVal"


    // $ANTLR start "ruleKeywordVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:279:1: ruleKeywordVal : ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) ) ;
    public final void ruleKeywordVal() throws RecognitionException {
        Token lv_kw_0_0=null;
        Token lv_kw_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:279:15: ( ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:280:2: ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:280:2: ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==23) ) {
                alt2=1;
            }
            else if ( (LA2_0==24) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:281:3: ( (lv_kw_0_0= 'kw1' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:281:3: ( (lv_kw_0_0= 'kw1' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:282:4: (lv_kw_0_0= 'kw1' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:282:4: (lv_kw_0_0= 'kw1' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:283:5: lv_kw_0_0= 'kw1'
                    {

                    					markLeaf(elementTypeProvider.getKeywordVal_KwKw1Keyword_0_0ElementType());
                    				
                    lv_kw_0_0=(Token)match(input,23,FollowSets000.FOLLOW_23_in_ruleKeywordVal889); 

                    					doneLeaf(lv_kw_0_0);
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:293:3: ( (lv_kw_1_0= 'kw2' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:293:3: ( (lv_kw_1_0= 'kw2' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:294:4: (lv_kw_1_0= 'kw2' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:294:4: (lv_kw_1_0= 'kw2' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:295:5: lv_kw_1_0= 'kw2'
                    {

                    					markLeaf(elementTypeProvider.getKeywordVal_KwKw2Keyword_1_0ElementType());
                    				
                    lv_kw_1_0=(Token)match(input,24,FollowSets000.FOLLOW_24_in_ruleKeywordVal935); 

                    					doneLeaf(lv_kw_1_0);
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleKeywordVal"


    // $ANTLR start "entryRuleTerminalVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:308:1: entryRuleTerminalVal : ruleTerminalVal EOF ;
    public final void entryRuleTerminalVal() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:308:21: ( ruleTerminalVal EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:309:2: ruleTerminalVal EOF
            {
             markComposite(elementTypeProvider.getTerminalValElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTerminalVal_in_entryRuleTerminalVal966);
            ruleTerminalVal();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTerminalVal969); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleTerminalVal"


    // $ANTLR start "ruleTerminalVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:314:1: ruleTerminalVal : ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) ) ;
    public final void ruleTerminalVal() throws RecognitionException {
        Token lv_term_0_0=null;
        Token lv_term_1_0=null;
        Token lv_term_2_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:314:16: ( ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:315:2: ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:315:2: ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) )
            int alt3=3;
            switch ( input.LA(1) ) {
            case RULE_TERMINAL1:
                {
                alt3=1;
                }
                break;
            case RULE_TERMINAL2:
                {
                alt3=2;
                }
                break;
            case 25:
                {
                alt3=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:316:3: ( (lv_term_0_0= RULE_TERMINAL1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:316:3: ( (lv_term_0_0= RULE_TERMINAL1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:317:4: (lv_term_0_0= RULE_TERMINAL1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:317:4: (lv_term_0_0= RULE_TERMINAL1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:318:5: lv_term_0_0= RULE_TERMINAL1
                    {

                    					markLeaf(elementTypeProvider.getTerminalVal_TermTerminal1TerminalRuleCall_0_0ElementType());
                    				
                    lv_term_0_0=(Token)match(input,RULE_TERMINAL1,FollowSets000.FOLLOW_RULE_TERMINAL1_in_ruleTerminalVal1001); 

                    					doneLeaf(lv_term_0_0);
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:328:3: ( (lv_term_1_0= RULE_TERMINAL2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:328:3: ( (lv_term_1_0= RULE_TERMINAL2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:329:4: (lv_term_1_0= RULE_TERMINAL2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:329:4: (lv_term_1_0= RULE_TERMINAL2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:330:5: lv_term_1_0= RULE_TERMINAL2
                    {

                    					markLeaf(elementTypeProvider.getTerminalVal_TermTerminal2TerminalRuleCall_1_0ElementType());
                    				
                    lv_term_1_0=(Token)match(input,RULE_TERMINAL2,FollowSets000.FOLLOW_RULE_TERMINAL2_in_ruleTerminalVal1047); 

                    					doneLeaf(lv_term_1_0);
                    				

                    }


                    }


                    }
                    break;
                case 3 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:340:3: ( (lv_term_2_0= '%foo' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:340:3: ( (lv_term_2_0= '%foo' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:341:4: (lv_term_2_0= '%foo' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:341:4: (lv_term_2_0= '%foo' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:342:5: lv_term_2_0= '%foo'
                    {

                    					markLeaf(elementTypeProvider.getTerminalVal_TermFooKeyword_2_0ElementType());
                    				
                    lv_term_2_0=(Token)match(input,25,FollowSets000.FOLLOW_25_in_ruleTerminalVal1093); 

                    					doneLeaf(lv_term_2_0);
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleTerminalVal"


    // $ANTLR start "entryRuleEnumVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:355:1: entryRuleEnumVal : ruleEnumVal EOF ;
    public final void entryRuleEnumVal() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:355:17: ( ruleEnumVal EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:356:2: ruleEnumVal EOF
            {
             markComposite(elementTypeProvider.getEnumValElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleEnumVal_in_entryRuleEnumVal1124);
            ruleEnumVal();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleEnumVal1127); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleEnumVal"


    // $ANTLR start "ruleEnumVal"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:361:1: ruleEnumVal : ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) ) ;
    public final void ruleEnumVal() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:361:12: ( ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:362:2: ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:362:2: ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==29) ) {
                alt4=1;
            }
            else if ( (LA4_0==30) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:363:3: ( (lv_en_0_0= ruleEnum1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:363:3: ( (lv_en_0_0= ruleEnum1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:364:4: (lv_en_0_0= ruleEnum1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:364:4: (lv_en_0_0= ruleEnum1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:365:5: lv_en_0_0= ruleEnum1
                    {

                    					markComposite(elementTypeProvider.getEnumVal_EnEnum1EnumRuleCall_0_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleEnum1_in_ruleEnumVal1159);
                    ruleEnum1();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:375:3: ( (lv_en_1_0= ruleEnum2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:375:3: ( (lv_en_1_0= ruleEnum2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:376:4: (lv_en_1_0= ruleEnum2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:376:4: (lv_en_1_0= ruleEnum2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:377:5: lv_en_1_0= ruleEnum2
                    {

                    					markComposite(elementTypeProvider.getEnumVal_EnEnum2EnumRuleCall_1_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleEnum2_in_ruleEnumVal1205);
                    ruleEnum2();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleEnumVal"


    // $ANTLR start "entryRuleKeywordBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:390:1: entryRuleKeywordBool : ruleKeywordBool EOF ;
    public final void entryRuleKeywordBool() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:390:21: ( ruleKeywordBool EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:391:2: ruleKeywordBool EOF
            {
             markComposite(elementTypeProvider.getKeywordBoolElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleKeywordBool_in_entryRuleKeywordBool1236);
            ruleKeywordBool();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleKeywordBool1239); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleKeywordBool"


    // $ANTLR start "ruleKeywordBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:396:1: ruleKeywordBool : ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) ) ;
    public final void ruleKeywordBool() throws RecognitionException {
        Token lv_kw_0_0=null;
        Token lv_kw_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:396:16: ( ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:397:2: ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:397:2: ( ( (lv_kw_0_0= 'kw1' ) ) | ( (lv_kw_1_0= 'kw2' ) ) )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==23) ) {
                alt5=1;
            }
            else if ( (LA5_0==24) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:398:3: ( (lv_kw_0_0= 'kw1' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:398:3: ( (lv_kw_0_0= 'kw1' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:399:4: (lv_kw_0_0= 'kw1' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:399:4: (lv_kw_0_0= 'kw1' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:400:5: lv_kw_0_0= 'kw1'
                    {

                    					markLeaf(elementTypeProvider.getKeywordBool_KwKw1Keyword_0_0ElementType());
                    				
                    lv_kw_0_0=(Token)match(input,23,FollowSets000.FOLLOW_23_in_ruleKeywordBool1271); 

                    					doneLeaf(lv_kw_0_0);
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:410:3: ( (lv_kw_1_0= 'kw2' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:410:3: ( (lv_kw_1_0= 'kw2' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:411:4: (lv_kw_1_0= 'kw2' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:411:4: (lv_kw_1_0= 'kw2' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:412:5: lv_kw_1_0= 'kw2'
                    {

                    					markLeaf(elementTypeProvider.getKeywordBool_KwKw2Keyword_1_0ElementType());
                    				
                    lv_kw_1_0=(Token)match(input,24,FollowSets000.FOLLOW_24_in_ruleKeywordBool1317); 

                    					doneLeaf(lv_kw_1_0);
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleKeywordBool"


    // $ANTLR start "entryRuleTerminalBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:425:1: entryRuleTerminalBool : ruleTerminalBool EOF ;
    public final void entryRuleTerminalBool() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:425:22: ( ruleTerminalBool EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:426:2: ruleTerminalBool EOF
            {
             markComposite(elementTypeProvider.getTerminalBoolElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTerminalBool_in_entryRuleTerminalBool1348);
            ruleTerminalBool();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTerminalBool1351); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleTerminalBool"


    // $ANTLR start "ruleTerminalBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:431:1: ruleTerminalBool : ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) ) ;
    public final void ruleTerminalBool() throws RecognitionException {
        Token lv_term_0_0=null;
        Token lv_term_1_0=null;
        Token lv_term_2_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:431:17: ( ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:432:2: ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:432:2: ( ( (lv_term_0_0= RULE_TERMINAL1 ) ) | ( (lv_term_1_0= RULE_TERMINAL2 ) ) | ( (lv_term_2_0= '%foo' ) ) )
            int alt6=3;
            switch ( input.LA(1) ) {
            case RULE_TERMINAL1:
                {
                alt6=1;
                }
                break;
            case RULE_TERMINAL2:
                {
                alt6=2;
                }
                break;
            case 25:
                {
                alt6=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:433:3: ( (lv_term_0_0= RULE_TERMINAL1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:433:3: ( (lv_term_0_0= RULE_TERMINAL1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:434:4: (lv_term_0_0= RULE_TERMINAL1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:434:4: (lv_term_0_0= RULE_TERMINAL1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:435:5: lv_term_0_0= RULE_TERMINAL1
                    {

                    					markLeaf(elementTypeProvider.getTerminalBool_TermTerminal1TerminalRuleCall_0_0ElementType());
                    				
                    lv_term_0_0=(Token)match(input,RULE_TERMINAL1,FollowSets000.FOLLOW_RULE_TERMINAL1_in_ruleTerminalBool1383); 

                    					doneLeaf(lv_term_0_0);
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:445:3: ( (lv_term_1_0= RULE_TERMINAL2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:445:3: ( (lv_term_1_0= RULE_TERMINAL2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:446:4: (lv_term_1_0= RULE_TERMINAL2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:446:4: (lv_term_1_0= RULE_TERMINAL2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:447:5: lv_term_1_0= RULE_TERMINAL2
                    {

                    					markLeaf(elementTypeProvider.getTerminalBool_TermTerminal2TerminalRuleCall_1_0ElementType());
                    				
                    lv_term_1_0=(Token)match(input,RULE_TERMINAL2,FollowSets000.FOLLOW_RULE_TERMINAL2_in_ruleTerminalBool1429); 

                    					doneLeaf(lv_term_1_0);
                    				

                    }


                    }


                    }
                    break;
                case 3 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:457:3: ( (lv_term_2_0= '%foo' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:457:3: ( (lv_term_2_0= '%foo' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:458:4: (lv_term_2_0= '%foo' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:458:4: (lv_term_2_0= '%foo' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:459:5: lv_term_2_0= '%foo'
                    {

                    					markLeaf(elementTypeProvider.getTerminalBool_TermFooKeyword_2_0ElementType());
                    				
                    lv_term_2_0=(Token)match(input,25,FollowSets000.FOLLOW_25_in_ruleTerminalBool1475); 

                    					doneLeaf(lv_term_2_0);
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleTerminalBool"


    // $ANTLR start "entryRuleEnumBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:472:1: entryRuleEnumBool : ruleEnumBool EOF ;
    public final void entryRuleEnumBool() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:472:18: ( ruleEnumBool EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:473:2: ruleEnumBool EOF
            {
             markComposite(elementTypeProvider.getEnumBoolElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleEnumBool_in_entryRuleEnumBool1506);
            ruleEnumBool();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleEnumBool1509); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleEnumBool"


    // $ANTLR start "ruleEnumBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:478:1: ruleEnumBool : ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) ) ;
    public final void ruleEnumBool() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:478:13: ( ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:479:2: ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:479:2: ( ( (lv_en_0_0= ruleEnum1 ) ) | ( (lv_en_1_0= ruleEnum2 ) ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==29) ) {
                alt7=1;
            }
            else if ( (LA7_0==30) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:480:3: ( (lv_en_0_0= ruleEnum1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:480:3: ( (lv_en_0_0= ruleEnum1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:481:4: (lv_en_0_0= ruleEnum1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:481:4: (lv_en_0_0= ruleEnum1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:482:5: lv_en_0_0= ruleEnum1
                    {

                    					markComposite(elementTypeProvider.getEnumBool_EnEnum1EnumRuleCall_0_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleEnum1_in_ruleEnumBool1541);
                    ruleEnum1();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:492:3: ( (lv_en_1_0= ruleEnum2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:492:3: ( (lv_en_1_0= ruleEnum2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:493:4: (lv_en_1_0= ruleEnum2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:493:4: (lv_en_1_0= ruleEnum2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:494:5: lv_en_1_0= ruleEnum2
                    {

                    					markComposite(elementTypeProvider.getEnumBool_EnEnum2EnumRuleCall_1_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleEnum2_in_ruleEnumBool1587);
                    ruleEnum2();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleEnumBool"


    // $ANTLR start "entryRuleMixedBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:507:1: entryRuleMixedBool : ruleMixedBool EOF ;
    public final void entryRuleMixedBool() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:507:19: ( ruleMixedBool EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:508:2: ruleMixedBool EOF
            {
             markComposite(elementTypeProvider.getMixedBoolElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleMixedBool_in_entryRuleMixedBool1618);
            ruleMixedBool();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleMixedBool1621); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleMixedBool"


    // $ANTLR start "ruleMixedBool"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:513:1: ruleMixedBool : ( ( (lv_val_0_0= 'kw1' ) ) | ( (lv_val_1_0= ruleBoolean ) ) ) ;
    public final void ruleMixedBool() throws RecognitionException {
        Token lv_val_0_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:513:14: ( ( ( (lv_val_0_0= 'kw1' ) ) | ( (lv_val_1_0= ruleBoolean ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:514:2: ( ( (lv_val_0_0= 'kw1' ) ) | ( (lv_val_1_0= ruleBoolean ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:514:2: ( ( (lv_val_0_0= 'kw1' ) ) | ( (lv_val_1_0= ruleBoolean ) ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==23) ) {
                alt8=1;
            }
            else if ( ((LA8_0>=26 && LA8_0<=27)) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:515:3: ( (lv_val_0_0= 'kw1' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:515:3: ( (lv_val_0_0= 'kw1' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:516:4: (lv_val_0_0= 'kw1' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:516:4: (lv_val_0_0= 'kw1' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:517:5: lv_val_0_0= 'kw1'
                    {

                    					markLeaf(elementTypeProvider.getMixedBool_ValKw1Keyword_0_0ElementType());
                    				
                    lv_val_0_0=(Token)match(input,23,FollowSets000.FOLLOW_23_in_ruleMixedBool1653); 

                    					doneLeaf(lv_val_0_0);
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:527:3: ( (lv_val_1_0= ruleBoolean ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:527:3: ( (lv_val_1_0= ruleBoolean ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:528:4: (lv_val_1_0= ruleBoolean )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:528:4: (lv_val_1_0= ruleBoolean )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:529:5: lv_val_1_0= ruleBoolean
                    {

                    					markComposite(elementTypeProvider.getMixedBool_ValBooleanParserRuleCall_1_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleBoolean_in_ruleMixedBool1699);
                    ruleBoolean();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleMixedBool"


    // $ANTLR start "entryRuleBoolean"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:542:1: entryRuleBoolean : ruleBoolean EOF ;
    public final void entryRuleBoolean() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:542:17: ( ruleBoolean EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:543:2: ruleBoolean EOF
            {
             markComposite(elementTypeProvider.getBooleanElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleBoolean_in_entryRuleBoolean1730);
            ruleBoolean();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleBoolean1733); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleBoolean"


    // $ANTLR start "ruleBoolean"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:548:1: ruleBoolean : (kw= 'true' | kw= 'false' ) ;
    public final void ruleBoolean() throws RecognitionException {
        Token kw=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:548:12: ( (kw= 'true' | kw= 'false' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:549:2: (kw= 'true' | kw= 'false' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:549:2: (kw= 'true' | kw= 'false' )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==26) ) {
                alt9=1;
            }
            else if ( (LA9_0==27) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:550:3: kw= 'true'
                    {

                    			markLeaf(elementTypeProvider.getBoolean_TrueKeyword_0ElementType());
                    		
                    kw=(Token)match(input,26,FollowSets000.FOLLOW_26_in_ruleBoolean1752); 

                    			doneLeaf(kw);
                    		

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:558:3: kw= 'false'
                    {

                    			markLeaf(elementTypeProvider.getBoolean_FalseKeyword_1ElementType());
                    		
                    kw=(Token)match(input,27,FollowSets000.FOLLOW_27_in_ruleBoolean1774); 

                    			doneLeaf(kw);
                    		

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleBoolean"


    // $ANTLR start "entryRuleMixedValue"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:569:1: entryRuleMixedValue : ruleMixedValue EOF ;
    public final void entryRuleMixedValue() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:569:20: ( ruleMixedValue EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:570:2: ruleMixedValue EOF
            {
             markComposite(elementTypeProvider.getMixedValueElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleMixedValue_in_entryRuleMixedValue1794);
            ruleMixedValue();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleMixedValue1797); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleMixedValue"


    // $ANTLR start "ruleMixedValue"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:575:1: ruleMixedValue : ( ( (lv_val_0_0= ruleEnum1 ) ) | ( (lv_val_1_0= ruleDatEnum ) ) ) ;
    public final void ruleMixedValue() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:575:15: ( ( ( (lv_val_0_0= ruleEnum1 ) ) | ( (lv_val_1_0= ruleDatEnum ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:576:2: ( ( (lv_val_0_0= ruleEnum1 ) ) | ( (lv_val_1_0= ruleDatEnum ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:576:2: ( ( (lv_val_0_0= ruleEnum1 ) ) | ( (lv_val_1_0= ruleDatEnum ) ) )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==29) ) {
                alt10=1;
            }
            else if ( (LA10_0==28) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:577:3: ( (lv_val_0_0= ruleEnum1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:577:3: ( (lv_val_0_0= ruleEnum1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:578:4: (lv_val_0_0= ruleEnum1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:578:4: (lv_val_0_0= ruleEnum1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:579:5: lv_val_0_0= ruleEnum1
                    {

                    					markComposite(elementTypeProvider.getMixedValue_ValEnum1EnumRuleCall_0_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleEnum1_in_ruleMixedValue1829);
                    ruleEnum1();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:589:3: ( (lv_val_1_0= ruleDatEnum ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:589:3: ( (lv_val_1_0= ruleDatEnum ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:590:4: (lv_val_1_0= ruleDatEnum )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:590:4: (lv_val_1_0= ruleDatEnum )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:591:5: lv_val_1_0= ruleDatEnum
                    {

                    					markComposite(elementTypeProvider.getMixedValue_ValDatEnumParserRuleCall_1_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleDatEnum_in_ruleMixedValue1875);
                    ruleDatEnum();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleMixedValue"


    // $ANTLR start "entryRuleDatEnum"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:604:1: entryRuleDatEnum : ruleDatEnum EOF ;
    public final void entryRuleDatEnum() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:604:17: ( ruleDatEnum EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:605:2: ruleDatEnum EOF
            {
             markComposite(elementTypeProvider.getDatEnumElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleDatEnum_in_entryRuleDatEnum1906);
            ruleDatEnum();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleDatEnum1909); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleDatEnum"


    // $ANTLR start "ruleDatEnum"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:610:1: ruleDatEnum : kw= 'lit3' ;
    public final void ruleDatEnum() throws RecognitionException {
        Token kw=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:610:12: (kw= 'lit3' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:611:2: kw= 'lit3'
            {

            		markLeaf(elementTypeProvider.getDatEnum_Lit3KeywordElementType());
            	
            kw=(Token)match(input,28,FollowSets000.FOLLOW_28_in_ruleDatEnum1923); 

            		doneLeaf(kw);
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleDatEnum"


    // $ANTLR start "entryRuleContainmentRef"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:621:1: entryRuleContainmentRef : ruleContainmentRef EOF ;
    public final void entryRuleContainmentRef() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:621:24: ( ruleContainmentRef EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:622:2: ruleContainmentRef EOF
            {
             markComposite(elementTypeProvider.getContainmentRefElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleContainmentRef_in_entryRuleContainmentRef1939);
            ruleContainmentRef();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleContainmentRef1942); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleContainmentRef"


    // $ANTLR start "ruleContainmentRef"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:627:1: ruleContainmentRef : ( ( (lv_ctx_0_0= ruleContainmentRef1 ) ) | ( (lv_ctx_1_0= ruleContainmentRef2 ) ) ) ;
    public final void ruleContainmentRef() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:627:19: ( ( ( (lv_ctx_0_0= ruleContainmentRef1 ) ) | ( (lv_ctx_1_0= ruleContainmentRef2 ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:628:2: ( ( (lv_ctx_0_0= ruleContainmentRef1 ) ) | ( (lv_ctx_1_0= ruleContainmentRef2 ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:628:2: ( ( (lv_ctx_0_0= ruleContainmentRef1 ) ) | ( (lv_ctx_1_0= ruleContainmentRef2 ) ) )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==23) ) {
                alt11=1;
            }
            else if ( (LA11_0==24) ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:629:3: ( (lv_ctx_0_0= ruleContainmentRef1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:629:3: ( (lv_ctx_0_0= ruleContainmentRef1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:630:4: (lv_ctx_0_0= ruleContainmentRef1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:630:4: (lv_ctx_0_0= ruleContainmentRef1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:631:5: lv_ctx_0_0= ruleContainmentRef1
                    {

                    					markComposite(elementTypeProvider.getContainmentRef_CtxContainmentRef1ParserRuleCall_0_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleContainmentRef1_in_ruleContainmentRef1974);
                    ruleContainmentRef1();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:641:3: ( (lv_ctx_1_0= ruleContainmentRef2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:641:3: ( (lv_ctx_1_0= ruleContainmentRef2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:642:4: (lv_ctx_1_0= ruleContainmentRef2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:642:4: (lv_ctx_1_0= ruleContainmentRef2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:643:5: lv_ctx_1_0= ruleContainmentRef2
                    {

                    					markComposite(elementTypeProvider.getContainmentRef_CtxContainmentRef2ParserRuleCall_1_0ElementType());
                    				
                    pushFollow(FollowSets000.FOLLOW_ruleContainmentRef2_in_ruleContainmentRef2020);
                    ruleContainmentRef2();

                    state._fsp--;


                    					doneComposite();
                    				

                    }


                    }


                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleContainmentRef"


    // $ANTLR start "entryRuleContainmentRef1"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:656:1: entryRuleContainmentRef1 : ruleContainmentRef1 EOF ;
    public final void entryRuleContainmentRef1() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:656:25: ( ruleContainmentRef1 EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:657:2: ruleContainmentRef1 EOF
            {
             markComposite(elementTypeProvider.getContainmentRef1ElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleContainmentRef1_in_entryRuleContainmentRef12051);
            ruleContainmentRef1();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleContainmentRef12054); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleContainmentRef1"


    // $ANTLR start "ruleContainmentRef1"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:662:1: ruleContainmentRef1 : (otherlv_0= 'kw1' ( (lv_val1_1_0= RULE_ID ) ) ) ;
    public final void ruleContainmentRef1() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_val1_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:662:20: ( (otherlv_0= 'kw1' ( (lv_val1_1_0= RULE_ID ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:663:2: (otherlv_0= 'kw1' ( (lv_val1_1_0= RULE_ID ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:663:2: (otherlv_0= 'kw1' ( (lv_val1_1_0= RULE_ID ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:664:3: otherlv_0= 'kw1' ( (lv_val1_1_0= RULE_ID ) )
            {

            			markLeaf(elementTypeProvider.getContainmentRef1_Kw1Keyword_0ElementType());
            		
            otherlv_0=(Token)match(input,23,FollowSets000.FOLLOW_23_in_ruleContainmentRef12073); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:671:3: ( (lv_val1_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:672:4: (lv_val1_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:672:4: (lv_val1_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:673:5: lv_val1_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getContainmentRef1_Val1IDTerminalRuleCall_1_0ElementType());
            				
            lv_val1_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleContainmentRef12100); 

            					doneLeaf(lv_val1_1_0);
            				

            }


            }


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleContainmentRef1"


    // $ANTLR start "entryRuleContainmentRef2"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:686:1: entryRuleContainmentRef2 : ruleContainmentRef2 EOF ;
    public final void entryRuleContainmentRef2() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:686:25: ( ruleContainmentRef2 EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:687:2: ruleContainmentRef2 EOF
            {
             markComposite(elementTypeProvider.getContainmentRef2ElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleContainmentRef2_in_entryRuleContainmentRef22131);
            ruleContainmentRef2();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleContainmentRef22134); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleContainmentRef2"


    // $ANTLR start "ruleContainmentRef2"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:692:1: ruleContainmentRef2 : (otherlv_0= 'kw2' ( (lv_val2_1_0= RULE_ID ) ) ) ;
    public final void ruleContainmentRef2() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_val2_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:692:20: ( (otherlv_0= 'kw2' ( (lv_val2_1_0= RULE_ID ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:693:2: (otherlv_0= 'kw2' ( (lv_val2_1_0= RULE_ID ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:693:2: (otherlv_0= 'kw2' ( (lv_val2_1_0= RULE_ID ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:694:3: otherlv_0= 'kw2' ( (lv_val2_1_0= RULE_ID ) )
            {

            			markLeaf(elementTypeProvider.getContainmentRef2_Kw2Keyword_0ElementType());
            		
            otherlv_0=(Token)match(input,24,FollowSets000.FOLLOW_24_in_ruleContainmentRef22153); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:701:3: ( (lv_val2_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:702:4: (lv_val2_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:702:4: (lv_val2_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:703:5: lv_val2_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getContainmentRef2_Val2IDTerminalRuleCall_1_0ElementType());
            				
            lv_val2_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleContainmentRef22180); 

            					doneLeaf(lv_val2_1_0);
            				

            }


            }


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleContainmentRef2"


    // $ANTLR start "entryRuleCrossRef"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:716:1: entryRuleCrossRef : ruleCrossRef EOF ;
    public final void entryRuleCrossRef() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:716:18: ( ruleCrossRef EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:717:2: ruleCrossRef EOF
            {
             markComposite(elementTypeProvider.getCrossRefElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleCrossRef_in_entryRuleCrossRef2211);
            ruleCrossRef();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleCrossRef2214); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "entryRuleCrossRef"


    // $ANTLR start "ruleCrossRef"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:722:1: ruleCrossRef : ( ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) ) ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) ) ) ;
    public final void ruleCrossRef() throws RecognitionException {
        Token lv_name_0_1=null;
        Token lv_name_0_2=null;
        Token otherlv_1=null;
        Token otherlv_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:722:13: ( ( ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) ) ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:723:2: ( ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) ) ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:723:2: ( ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) ) ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:724:3: ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) ) ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:724:3: ( ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:725:4: ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:725:4: ( (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:726:5: (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:726:5: (lv_name_0_1= RULE_TERMINAL1 | lv_name_0_2= RULE_TERMINAL2 )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==RULE_TERMINAL1) ) {
                alt12=1;
            }
            else if ( (LA12_0==RULE_TERMINAL2) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:727:6: lv_name_0_1= RULE_TERMINAL1
                    {

                    						markLeaf(elementTypeProvider.getCrossRef_NameTerminal1TerminalRuleCall_0_0_0ElementType());
                    					
                    lv_name_0_1=(Token)match(input,RULE_TERMINAL1,FollowSets000.FOLLOW_RULE_TERMINAL1_in_ruleCrossRef2254); 

                    						doneLeaf(lv_name_0_1);
                    					

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:735:6: lv_name_0_2= RULE_TERMINAL2
                    {

                    						markLeaf(elementTypeProvider.getCrossRef_NameTerminal2TerminalRuleCall_0_0_1ElementType());
                    					
                    lv_name_0_2=(Token)match(input,RULE_TERMINAL2,FollowSets000.FOLLOW_RULE_TERMINAL2_in_ruleCrossRef2288); 

                    						doneLeaf(lv_name_0_2);
                    					

                    }
                    break;

            }


            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:745:3: ( ( (otherlv_1= RULE_TERMINAL1 ) ) | ( (otherlv_2= RULE_TERMINAL2 ) ) )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==RULE_TERMINAL1) ) {
                alt13=1;
            }
            else if ( (LA13_0==RULE_TERMINAL2) ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:746:4: ( (otherlv_1= RULE_TERMINAL1 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:746:4: ( (otherlv_1= RULE_TERMINAL1 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:747:5: (otherlv_1= RULE_TERMINAL1 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:747:5: (otherlv_1= RULE_TERMINAL1 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:748:6: otherlv_1= RULE_TERMINAL1
                    {

                    						markLeaf(elementTypeProvider.getCrossRef_CrossRefCrossRefCrossReference_1_0_0ElementType());
                    					
                    otherlv_1=(Token)match(input,RULE_TERMINAL1,FollowSets000.FOLLOW_RULE_TERMINAL1_in_ruleCrossRef2341); 

                    						doneLeaf(otherlv_1);
                    					

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:758:4: ( (otherlv_2= RULE_TERMINAL2 ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:758:4: ( (otherlv_2= RULE_TERMINAL2 ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:759:5: (otherlv_2= RULE_TERMINAL2 )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:759:5: (otherlv_2= RULE_TERMINAL2 )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:760:6: otherlv_2= RULE_TERMINAL2
                    {

                    						markLeaf(elementTypeProvider.getCrossRef_CrossRefCrossRefCrossReference_1_1_0ElementType());
                    					
                    otherlv_2=(Token)match(input,RULE_TERMINAL2,FollowSets000.FOLLOW_RULE_TERMINAL2_in_ruleCrossRef2395); 

                    						doneLeaf(otherlv_2);
                    					

                    }


                    }


                    }
                    break;

            }


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleCrossRef"


    // $ANTLR start "ruleEnum1"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:774:1: ruleEnum1 : (enumLiteral_0= 'lit1' ) ;
    public final void ruleEnum1() throws RecognitionException {
        Token enumLiteral_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:774:10: ( (enumLiteral_0= 'lit1' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:775:2: (enumLiteral_0= 'lit1' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:775:2: (enumLiteral_0= 'lit1' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:776:3: enumLiteral_0= 'lit1'
            {

            			markLeaf(elementTypeProvider.getEnum1_Lit1EnumLiteralDeclarationElementType());
            		
            enumLiteral_0=(Token)match(input,29,FollowSets000.FOLLOW_29_in_ruleEnum12440); 

            			doneLeaf(enumLiteral_0);
            		

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleEnum1"


    // $ANTLR start "ruleEnum2"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:787:1: ruleEnum2 : (enumLiteral_0= 'lit2' ) ;
    public final void ruleEnum2() throws RecognitionException {
        Token enumLiteral_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:787:10: ( (enumLiteral_0= 'lit2' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:788:2: (enumLiteral_0= 'lit2' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:788:2: (enumLiteral_0= 'lit2' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/serializer/idea/parser/antlr/internal/PsiInternalAssignmentFinderTestLanguage.g:789:3: enumLiteral_0= 'lit2'
            {

            			markLeaf(elementTypeProvider.getEnum2_Lit2EnumLiteralDeclarationElementType());
            		
            enumLiteral_0=(Token)match(input,30,FollowSets000.FOLLOW_30_in_ruleEnum22467); 

            			doneLeaf(enumLiteral_0);
            		

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ruleEnum2"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleModel82 = new BitSet(new long[]{0x0000000001800000L});
        public static final BitSet FOLLOW_ruleKeywordVal_in_ruleModel114 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_14_in_ruleModel160 = new BitSet(new long[]{0x0000000002000030L});
        public static final BitSet FOLLOW_ruleTerminalVal_in_ruleModel192 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_15_in_ruleModel238 = new BitSet(new long[]{0x0000000060000000L});
        public static final BitSet FOLLOW_ruleEnumVal_in_ruleModel270 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_16_in_ruleModel316 = new BitSet(new long[]{0x0000000001800000L});
        public static final BitSet FOLLOW_ruleKeywordBool_in_ruleModel348 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_17_in_ruleModel394 = new BitSet(new long[]{0x0000000002000030L});
        public static final BitSet FOLLOW_ruleTerminalBool_in_ruleModel426 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_18_in_ruleModel472 = new BitSet(new long[]{0x0000000060000000L});
        public static final BitSet FOLLOW_ruleEnumBool_in_ruleModel504 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_19_in_ruleModel550 = new BitSet(new long[]{0x000000000C800000L});
        public static final BitSet FOLLOW_ruleMixedBool_in_ruleModel582 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_20_in_ruleModel628 = new BitSet(new long[]{0x0000000030000000L});
        public static final BitSet FOLLOW_ruleMixedValue_in_ruleModel660 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_21_in_ruleModel706 = new BitSet(new long[]{0x0000000001800000L});
        public static final BitSet FOLLOW_ruleContainmentRef_in_ruleModel738 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_22_in_ruleModel784 = new BitSet(new long[]{0x0000000000000030L});
        public static final BitSet FOLLOW_ruleCrossRef_in_ruleModel816 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleKeywordVal_in_entryRuleKeywordVal854 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleKeywordVal857 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_23_in_ruleKeywordVal889 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_24_in_ruleKeywordVal935 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTerminalVal_in_entryRuleTerminalVal966 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTerminalVal969 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL1_in_ruleTerminalVal1001 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL2_in_ruleTerminalVal1047 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_25_in_ruleTerminalVal1093 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnumVal_in_entryRuleEnumVal1124 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleEnumVal1127 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnum1_in_ruleEnumVal1159 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnum2_in_ruleEnumVal1205 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleKeywordBool_in_entryRuleKeywordBool1236 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleKeywordBool1239 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_23_in_ruleKeywordBool1271 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_24_in_ruleKeywordBool1317 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTerminalBool_in_entryRuleTerminalBool1348 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTerminalBool1351 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL1_in_ruleTerminalBool1383 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL2_in_ruleTerminalBool1429 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_25_in_ruleTerminalBool1475 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnumBool_in_entryRuleEnumBool1506 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleEnumBool1509 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnum1_in_ruleEnumBool1541 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnum2_in_ruleEnumBool1587 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleMixedBool_in_entryRuleMixedBool1618 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleMixedBool1621 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_23_in_ruleMixedBool1653 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleBoolean_in_ruleMixedBool1699 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleBoolean_in_entryRuleBoolean1730 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleBoolean1733 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_26_in_ruleBoolean1752 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_27_in_ruleBoolean1774 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleMixedValue_in_entryRuleMixedValue1794 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleMixedValue1797 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnum1_in_ruleMixedValue1829 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDatEnum_in_ruleMixedValue1875 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDatEnum_in_entryRuleDatEnum1906 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleDatEnum1909 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_28_in_ruleDatEnum1923 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleContainmentRef_in_entryRuleContainmentRef1939 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleContainmentRef1942 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleContainmentRef1_in_ruleContainmentRef1974 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleContainmentRef2_in_ruleContainmentRef2020 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleContainmentRef1_in_entryRuleContainmentRef12051 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleContainmentRef12054 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_23_in_ruleContainmentRef12073 = new BitSet(new long[]{0x0000000000000040L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleContainmentRef12100 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleContainmentRef2_in_entryRuleContainmentRef22131 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleContainmentRef22134 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_24_in_ruleContainmentRef22153 = new BitSet(new long[]{0x0000000000000040L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleContainmentRef22180 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleCrossRef_in_entryRuleCrossRef2211 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleCrossRef2214 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL1_in_ruleCrossRef2254 = new BitSet(new long[]{0x0000000000000030L});
        public static final BitSet FOLLOW_RULE_TERMINAL2_in_ruleCrossRef2288 = new BitSet(new long[]{0x0000000000000030L});
        public static final BitSet FOLLOW_RULE_TERMINAL1_in_ruleCrossRef2341 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_TERMINAL2_in_ruleCrossRef2395 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_29_in_ruleEnum12440 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_30_in_ruleEnum22467 = new BitSet(new long[]{0x0000000000000002L});
    }


}