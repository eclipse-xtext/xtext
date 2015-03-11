package org.eclipse.xtext.lexer.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.lexer.idea.lang.BacktrackingLexerTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.lexer.services.BacktrackingLexerTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
@SuppressWarnings("all")
public class PsiInternalBacktrackingLexerTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_YC", "RULE_CHARY", "RULE_CHARA", "RULE_CHARB", "RULE_CHARX", "RULE_CHARC", "RULE_WS", "RULE_SL_COMMENT", "'Abc'", "'Efg'"
    };
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int RULE_CHARX=8;
    public static final int RULE_YC=4;
    public static final int RULE_CHARY=5;
    public static final int RULE_WS=10;
    public static final int RULE_CHARA=6;
    public static final int RULE_SL_COMMENT=11;
    public static final int RULE_CHARB=7;
    public static final int EOF=-1;
    public static final int RULE_CHARC=9;

    // delegates
    // delegators


        public PsiInternalBacktrackingLexerTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBacktrackingLexerTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[14+1];
             
             
        }
        

    public String[] getTokenNames() { return PsiInternalBacktrackingLexerTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g"; }



    /*
      This grammar contains a lot of empty actions to work around a bug in ANTLR.
      Otherwise the ANTLR tool will create synpreds that cannot be compiled in some rare cases.
    */

    	protected BacktrackingLexerTestLanguageGrammarAccess grammarAccess;

    	protected BacktrackingLexerTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalBacktrackingLexerTestLanguageParser(PsiBuilder builder, TokenStream input, BacktrackingLexerTestLanguageElementTypeProvider elementTypeProvider, BacktrackingLexerTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:59:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        int entryRuleModel_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:59:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:60:2: ruleModel EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getModelElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleModel_in_entryRuleModel66);
            ruleModel();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleModel69); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, entryRuleModel_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:65:1: ruleModel : ( ( (lv_enums_0_0= ruleEnumName ) )* ( (lv_ycs_1_0= RULE_YC ) )* ( (lv_abs_2_0= ruleAb ) )* ( (lv_xbs_3_0= ruleXb ) )* ( (lv_ys_4_0= RULE_CHARY ) )* ( (lv_as_5_0= RULE_CHARA ) )* ) ;
    public final void ruleModel() throws RecognitionException {
        int ruleModel_StartIndex = input.index();
        Token lv_ycs_1_0=null;
        Token lv_ys_4_0=null;
        Token lv_as_5_0=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:65:10: ( ( ( (lv_enums_0_0= ruleEnumName ) )* ( (lv_ycs_1_0= RULE_YC ) )* ( (lv_abs_2_0= ruleAb ) )* ( (lv_xbs_3_0= ruleXb ) )* ( (lv_ys_4_0= RULE_CHARY ) )* ( (lv_as_5_0= RULE_CHARA ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:66:2: ( ( (lv_enums_0_0= ruleEnumName ) )* ( (lv_ycs_1_0= RULE_YC ) )* ( (lv_abs_2_0= ruleAb ) )* ( (lv_xbs_3_0= ruleXb ) )* ( (lv_ys_4_0= RULE_CHARY ) )* ( (lv_as_5_0= RULE_CHARA ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:66:2: ( ( (lv_enums_0_0= ruleEnumName ) )* ( (lv_ycs_1_0= RULE_YC ) )* ( (lv_abs_2_0= ruleAb ) )* ( (lv_xbs_3_0= ruleXb ) )* ( (lv_ys_4_0= RULE_CHARY ) )* ( (lv_as_5_0= RULE_CHARA ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:67:3: ( (lv_enums_0_0= ruleEnumName ) )* ( (lv_ycs_1_0= RULE_YC ) )* ( (lv_abs_2_0= ruleAb ) )* ( (lv_xbs_3_0= ruleXb ) )* ( (lv_ys_4_0= RULE_CHARY ) )* ( (lv_as_5_0= RULE_CHARA ) )*
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:67:3: ( (lv_enums_0_0= ruleEnumName ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=12 && LA1_0<=13)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:68:4: (lv_enums_0_0= ruleEnumName )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:68:4: (lv_enums_0_0= ruleEnumName )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:69:5: lv_enums_0_0= ruleEnumName
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markComposite(elementTypeProvider.getModel_EnumsEnumNameEnumRuleCall_0_0ElementType());
            	      				
            	    }
            	    pushFollow(FollowSets000.FOLLOW_ruleEnumName_in_ruleModel101);
            	    ruleEnumName();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneComposite();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:78:3: ( (lv_ycs_1_0= RULE_YC ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==RULE_YC) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:79:4: (lv_ycs_1_0= RULE_YC )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:79:4: (lv_ycs_1_0= RULE_YC )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:80:5: lv_ycs_1_0= RULE_YC
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markLeaf(elementTypeProvider.getModel_YcsYcTerminalRuleCall_1_0ElementType());
            	      				
            	    }
            	    lv_ycs_1_0=(Token)match(input,RULE_YC,FollowSets000.FOLLOW_RULE_YC_in_ruleModel140); if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneLeaf(lv_ycs_1_0);
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:89:3: ( (lv_abs_2_0= ruleAb ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_CHARA) ) {
                    int LA3_2 = input.LA(2);

                    if ( (LA3_2==RULE_CHARB) ) {
                        alt3=1;
                    }


                }


                switch (alt3) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:90:4: (lv_abs_2_0= ruleAb )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:90:4: (lv_abs_2_0= ruleAb )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:91:5: lv_abs_2_0= ruleAb
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markComposite(elementTypeProvider.getModel_AbsAbParserRuleCall_2_0ElementType());
            	      				
            	    }
            	    pushFollow(FollowSets000.FOLLOW_ruleAb_in_ruleModel179);
            	    ruleAb();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneComposite();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:100:3: ( (lv_xbs_3_0= ruleXb ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==RULE_CHARX) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:101:4: (lv_xbs_3_0= ruleXb )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:101:4: (lv_xbs_3_0= ruleXb )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:102:5: lv_xbs_3_0= ruleXb
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markComposite(elementTypeProvider.getModel_XbsXbParserRuleCall_3_0ElementType());
            	      				
            	    }
            	    pushFollow(FollowSets000.FOLLOW_ruleXb_in_ruleModel218);
            	    ruleXb();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneComposite();
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:111:3: ( (lv_ys_4_0= RULE_CHARY ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==RULE_CHARY) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:112:4: (lv_ys_4_0= RULE_CHARY )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:112:4: (lv_ys_4_0= RULE_CHARY )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:113:5: lv_ys_4_0= RULE_CHARY
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markLeaf(elementTypeProvider.getModel_YsCharYTerminalRuleCall_4_0ElementType());
            	      				
            	    }
            	    lv_ys_4_0=(Token)match(input,RULE_CHARY,FollowSets000.FOLLOW_RULE_CHARY_in_ruleModel257); if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneLeaf(lv_ys_4_0);
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:122:3: ( (lv_as_5_0= RULE_CHARA ) )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==RULE_CHARA) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:123:4: (lv_as_5_0= RULE_CHARA )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:123:4: (lv_as_5_0= RULE_CHARA )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:124:5: lv_as_5_0= RULE_CHARA
            	    {
            	    if ( state.backtracking==0 ) {

            	      					markLeaf(elementTypeProvider.getModel_AsCharATerminalRuleCall_5_0ElementType());
            	      				
            	    }
            	    lv_as_5_0=(Token)match(input,RULE_CHARA,FollowSets000.FOLLOW_RULE_CHARA_in_ruleModel296); if (state.failed) return ;
            	    if ( state.backtracking==0 ) {

            	      					doneLeaf(lv_as_5_0);
            	      				
            	    }

            	    }


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, ruleModel_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleAb"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:137:1: entryRuleAb : ruleAb EOF ;
    public final void entryRuleAb() throws RecognitionException {
        int entryRuleAb_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:137:12: ( ruleAb EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:138:2: ruleAb EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getAbElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleAb_in_entryRuleAb328);
            ruleAb();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleAb331); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, entryRuleAb_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "entryRuleAb"


    // $ANTLR start "ruleAb"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:143:1: ruleAb : ( ( (lv_x_0_0= RULE_CHARA ) ) ( (lv_y_1_0= RULE_CHARB ) ) ) ;
    public final void ruleAb() throws RecognitionException {
        int ruleAb_StartIndex = input.index();
        Token lv_x_0_0=null;
        Token lv_y_1_0=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:143:7: ( ( ( (lv_x_0_0= RULE_CHARA ) ) ( (lv_y_1_0= RULE_CHARB ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:144:2: ( ( (lv_x_0_0= RULE_CHARA ) ) ( (lv_y_1_0= RULE_CHARB ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:144:2: ( ( (lv_x_0_0= RULE_CHARA ) ) ( (lv_y_1_0= RULE_CHARB ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:145:3: ( (lv_x_0_0= RULE_CHARA ) ) ( (lv_y_1_0= RULE_CHARB ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:145:3: ( (lv_x_0_0= RULE_CHARA ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:146:4: (lv_x_0_0= RULE_CHARA )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:146:4: (lv_x_0_0= RULE_CHARA )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:147:5: lv_x_0_0= RULE_CHARA
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getAb_XCharATerminalRuleCall_0_0ElementType());
              				
            }
            lv_x_0_0=(Token)match(input,RULE_CHARA,FollowSets000.FOLLOW_RULE_CHARA_in_ruleAb363); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_x_0_0);
              				
            }

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:156:3: ( (lv_y_1_0= RULE_CHARB ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:157:4: (lv_y_1_0= RULE_CHARB )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:157:4: (lv_y_1_0= RULE_CHARB )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:158:5: lv_y_1_0= RULE_CHARB
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getAb_YCharbTerminalRuleCall_1_0ElementType());
              				
            }
            lv_y_1_0=(Token)match(input,RULE_CHARB,FollowSets000.FOLLOW_RULE_CHARB_in_ruleAb401); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_y_1_0);
              				
            }

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
            if ( state.backtracking>0 ) { memoize(input, 4, ruleAb_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "ruleAb"


    // $ANTLR start "entryRuleXb"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:171:1: entryRuleXb : ruleXb EOF ;
    public final void entryRuleXb() throws RecognitionException {
        int entryRuleXb_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:171:12: ( ruleXb EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:172:2: ruleXb EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getXbElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleXb_in_entryRuleXb432);
            ruleXb();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleXb435); if (state.failed) return ;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, entryRuleXb_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "entryRuleXb"


    // $ANTLR start "ruleXb"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:177:1: ruleXb : ( ( (lv_x_0_0= RULE_CHARX ) ) ( (lv_y_1_0= RULE_CHARB ) ) ) ;
    public final void ruleXb() throws RecognitionException {
        int ruleXb_StartIndex = input.index();
        Token lv_x_0_0=null;
        Token lv_y_1_0=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:177:7: ( ( ( (lv_x_0_0= RULE_CHARX ) ) ( (lv_y_1_0= RULE_CHARB ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:178:2: ( ( (lv_x_0_0= RULE_CHARX ) ) ( (lv_y_1_0= RULE_CHARB ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:178:2: ( ( (lv_x_0_0= RULE_CHARX ) ) ( (lv_y_1_0= RULE_CHARB ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:179:3: ( (lv_x_0_0= RULE_CHARX ) ) ( (lv_y_1_0= RULE_CHARB ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:179:3: ( (lv_x_0_0= RULE_CHARX ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:180:4: (lv_x_0_0= RULE_CHARX )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:180:4: (lv_x_0_0= RULE_CHARX )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:181:5: lv_x_0_0= RULE_CHARX
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getXb_XCharXTerminalRuleCall_0_0ElementType());
              				
            }
            lv_x_0_0=(Token)match(input,RULE_CHARX,FollowSets000.FOLLOW_RULE_CHARX_in_ruleXb467); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_x_0_0);
              				
            }

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:190:3: ( (lv_y_1_0= RULE_CHARB ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:191:4: (lv_y_1_0= RULE_CHARB )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:191:4: (lv_y_1_0= RULE_CHARB )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:192:5: lv_y_1_0= RULE_CHARB
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getXb_YCharbTerminalRuleCall_1_0ElementType());
              				
            }
            lv_y_1_0=(Token)match(input,RULE_CHARB,FollowSets000.FOLLOW_RULE_CHARB_in_ruleXb505); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_y_1_0);
              				
            }

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
            if ( state.backtracking>0 ) { memoize(input, 6, ruleXb_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "ruleXb"


    // $ANTLR start "ruleEnumName"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:205:1: ruleEnumName : ( (enumLiteral_0= 'Abc' ) | (enumLiteral_1= 'Efg' ) ) ;
    public final void ruleEnumName() throws RecognitionException {
        int ruleEnumName_StartIndex = input.index();
        Token enumLiteral_0=null;
        Token enumLiteral_1=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return ; }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:205:13: ( ( (enumLiteral_0= 'Abc' ) | (enumLiteral_1= 'Efg' ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:206:2: ( (enumLiteral_0= 'Abc' ) | (enumLiteral_1= 'Efg' ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:206:2: ( (enumLiteral_0= 'Abc' ) | (enumLiteral_1= 'Efg' ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==12) ) {
                alt7=1;
            }
            else if ( (LA7_0==13) ) {
                alt7=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:207:3: (enumLiteral_0= 'Abc' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:207:3: (enumLiteral_0= 'Abc' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:208:4: enumLiteral_0= 'Abc'
                    {
                    if ( state.backtracking==0 ) {

                      				markLeaf(elementTypeProvider.getEnumName_AbcEnumLiteralDeclaration_0ElementType());
                      			
                    }
                    enumLiteral_0=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleEnumName549); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      				doneLeaf(enumLiteral_0);
                      			
                    }

                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:217:3: (enumLiteral_1= 'Efg' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:217:3: (enumLiteral_1= 'Efg' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/lexer/idea/parser/antlr/internal/PsiInternalBacktrackingLexerTestLanguage.g:218:4: enumLiteral_1= 'Efg'
                    {
                    if ( state.backtracking==0 ) {

                      				markLeaf(elementTypeProvider.getEnumName_EfgEnumLiteralDeclaration_1ElementType());
                      			
                    }
                    enumLiteral_1=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleEnumName582); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      				doneLeaf(enumLiteral_1);
                      			
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
            if ( state.backtracking>0 ) { memoize(input, 7, ruleEnumName_StartIndex); }
        }
        return ;
    }
    // $ANTLR end "ruleEnumName"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel66 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel69 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleEnumName_in_ruleModel101 = new BitSet(new long[]{0x0000000000003172L});
        public static final BitSet FOLLOW_RULE_YC_in_ruleModel140 = new BitSet(new long[]{0x0000000000000172L});
        public static final BitSet FOLLOW_ruleAb_in_ruleModel179 = new BitSet(new long[]{0x0000000000000162L});
        public static final BitSet FOLLOW_ruleXb_in_ruleModel218 = new BitSet(new long[]{0x0000000000000162L});
        public static final BitSet FOLLOW_RULE_CHARY_in_ruleModel257 = new BitSet(new long[]{0x0000000000000062L});
        public static final BitSet FOLLOW_RULE_CHARA_in_ruleModel296 = new BitSet(new long[]{0x0000000000000042L});
        public static final BitSet FOLLOW_ruleAb_in_entryRuleAb328 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleAb331 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_CHARA_in_ruleAb363 = new BitSet(new long[]{0x0000000000000080L});
        public static final BitSet FOLLOW_RULE_CHARB_in_ruleAb401 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleXb_in_entryRuleXb432 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleXb435 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_CHARX_in_ruleXb467 = new BitSet(new long[]{0x0000000000000080L});
        public static final BitSet FOLLOW_RULE_CHARB_in_ruleXb505 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_12_in_ruleEnumName549 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleEnumName582 = new BitSet(new long[]{0x0000000000000002L});
    }


}