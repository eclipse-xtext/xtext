package org.eclipse.xtext.parser.antlr.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parser.antlr.idea.lang.Bug296889ExTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.services.Bug296889ExTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
@SuppressWarnings("all")
public class PsiInternalBug296889ExTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'Model'", "'DataType'", "'--'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalBug296889ExTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBug296889ExTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalBug296889ExTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g"; }



    /*
      This grammar contains a lot of empty actions to work around a bug in ANTLR.
      Otherwise the ANTLR tool will create synpreds that cannot be compiled in some rare cases.
    */

    	protected Bug296889ExTestLanguageGrammarAccess grammarAccess;

    	protected Bug296889ExTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalBug296889ExTestLanguageParser(PsiBuilder builder, TokenStream input, Bug296889ExTestLanguageElementTypeProvider elementTypeProvider, Bug296889ExTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:58:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:58:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:59:2: ruleModel EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getModelElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleModel_in_entryRuleModel60);
            ruleModel();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleModel63); if (state.failed) return ;

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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:64:1: ruleModel : ( (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* ) | (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* ) ) ;
    public final void ruleModel() throws RecognitionException {
        Token otherlv_0=null;
        Token otherlv_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:64:10: ( ( (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* ) | (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:65:2: ( (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* ) | (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:65:2: ( (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* ) | (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==11) ) {
                alt3=1;
            }
            else if ( (LA3_0==12) ) {
                alt3=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:66:3: (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:66:3: (otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )* )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:67:4: otherlv_0= 'Model' ( (lv_expressions_1_0= ruleExpression ) )*
                    {
                    if ( state.backtracking==0 ) {

                      				markLeaf(elementTypeProvider.getModel_ModelKeyword_0_0ElementType());
                      			
                    }
                    otherlv_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleModel88); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      				doneLeaf(otherlv_0);
                      			
                    }
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:74:4: ( (lv_expressions_1_0= ruleExpression ) )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==RULE_ID||LA1_0==13) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:75:5: (lv_expressions_1_0= ruleExpression )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:75:5: (lv_expressions_1_0= ruleExpression )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:76:6: lv_expressions_1_0= ruleExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						markComposite(elementTypeProvider.getModel_ExpressionsExpressionParserRuleCall_0_1_0ElementType());
                    	      					
                    	    }
                    	    pushFollow(FollowSets000.FOLLOW_ruleExpression_in_ruleModel120);
                    	    ruleExpression();

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


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:87:3: (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:87:3: (otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )* )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:88:4: otherlv_2= 'DataType' ( (lv_values_3_0= ruleDataTypeExpression ) )*
                    {
                    if ( state.backtracking==0 ) {

                      				markLeaf(elementTypeProvider.getModel_DataTypeKeyword_1_0ElementType());
                      			
                    }
                    otherlv_2=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleModel167); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      				doneLeaf(otherlv_2);
                      			
                    }
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:95:4: ( (lv_values_3_0= ruleDataTypeExpression ) )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==RULE_ID||LA2_0==13) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:96:5: (lv_values_3_0= ruleDataTypeExpression )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:96:5: (lv_values_3_0= ruleDataTypeExpression )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:97:6: lv_values_3_0= ruleDataTypeExpression
                    	    {
                    	    if ( state.backtracking==0 ) {

                    	      						markComposite(elementTypeProvider.getModel_ValuesDataTypeExpressionParserRuleCall_1_1_0ElementType());
                    	      					
                    	    }
                    	    pushFollow(FollowSets000.FOLLOW_ruleDataTypeExpression_in_ruleModel199);
                    	    ruleDataTypeExpression();

                    	    state._fsp--;
                    	    if (state.failed) return ;
                    	    if ( state.backtracking==0 ) {

                    	      						doneComposite();
                    	      					
                    	    }

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);


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


    // $ANTLR start "entryRuleExpression"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:111:1: entryRuleExpression : ruleExpression EOF ;
    public final void entryRuleExpression() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:111:20: ( ruleExpression EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:112:2: ruleExpression EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getExpressionElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleExpression_in_entryRuleExpression238);
            ruleExpression();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleExpression241); if (state.failed) return ;

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
    // $ANTLR end "entryRuleExpression"


    // $ANTLR start "ruleExpression"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:117:1: ruleExpression : ( rulePostop | rulePreop ) ;
    public final void ruleExpression() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:117:15: ( ( rulePostop | rulePreop ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:118:2: ( rulePostop | rulePreop )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:118:2: ( rulePostop | rulePreop )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==RULE_ID) ) {
                alt4=1;
            }
            else if ( (LA4_0==13) ) {
                alt4=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:119:3: rulePostop
                    {
                    if ( state.backtracking==0 ) {

                      			/* */
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			markComposite(elementTypeProvider.getExpression_PostopParserRuleCall_0ElementType());
                      		
                    }
                    pushFollow(FollowSets000.FOLLOW_rulePostop_in_ruleExpression262);
                    rulePostop();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      			doneComposite();
                      		
                    }

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:130:3: rulePreop
                    {
                    if ( state.backtracking==0 ) {

                      			/* */
                      		
                    }
                    if ( state.backtracking==0 ) {

                      			markComposite(elementTypeProvider.getExpression_PreopParserRuleCall_1ElementType());
                      		
                    }
                    pushFollow(FollowSets000.FOLLOW_rulePreop_in_ruleExpression286);
                    rulePreop();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      			doneComposite();
                      		
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
    // $ANTLR end "ruleExpression"


    // $ANTLR start "entryRulePreop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:144:1: entryRulePreop : rulePreop EOF ;
    public final void entryRulePreop() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:144:15: ( rulePreop EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:145:2: rulePreop EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getPreopElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_rulePreop_in_entryRulePreop306);
            rulePreop();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRulePreop309); if (state.failed) return ;

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
    // $ANTLR end "entryRulePreop"


    // $ANTLR start "rulePreop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:150:1: rulePreop : ( () ( (lv_functionName_1_0= '--' ) ) ( (lv_expr_2_0= ruleVariable ) ) ) ;
    public final void rulePreop() throws RecognitionException {
        Token lv_functionName_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:150:10: ( ( () ( (lv_functionName_1_0= '--' ) ) ( (lv_expr_2_0= ruleVariable ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:151:2: ( () ( (lv_functionName_1_0= '--' ) ) ( (lv_expr_2_0= ruleVariable ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:151:2: ( () ( (lv_functionName_1_0= '--' ) ) ( (lv_expr_2_0= ruleVariable ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:152:3: () ( (lv_functionName_1_0= '--' ) ) ( (lv_expr_2_0= ruleVariable ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:152:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:153:4: 
            {
            if ( state.backtracking==0 ) {

              				/* */
              			
            }
            if ( state.backtracking==0 ) {

              				precedeComposite(elementTypeProvider.getPreop_PreopAction_0ElementType());
              				doneComposite();
              			
            }

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:161:3: ( (lv_functionName_1_0= '--' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:162:4: (lv_functionName_1_0= '--' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:162:4: (lv_functionName_1_0= '--' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:163:5: lv_functionName_1_0= '--'
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getPreop_FunctionNameHyphenMinusHyphenMinusKeyword_1_0ElementType());
              				
            }
            lv_functionName_1_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_rulePreop359); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_functionName_1_0);
              				
            }

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:172:3: ( (lv_expr_2_0= ruleVariable ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:173:4: (lv_expr_2_0= ruleVariable )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:173:4: (lv_expr_2_0= ruleVariable )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:174:5: lv_expr_2_0= ruleVariable
            {
            if ( state.backtracking==0 ) {

              					markComposite(elementTypeProvider.getPreop_ExprVariableParserRuleCall_2_0ElementType());
              				
            }
            pushFollow(FollowSets000.FOLLOW_ruleVariable_in_rulePreop397);
            ruleVariable();

            state._fsp--;
            if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneComposite();
              				
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
        }
        return ;
    }
    // $ANTLR end "rulePreop"


    // $ANTLR start "entryRulePostop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:187:1: entryRulePostop : rulePostop EOF ;
    public final void entryRulePostop() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:187:16: ( rulePostop EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:188:2: rulePostop EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getPostopElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_rulePostop_in_entryRulePostop428);
            rulePostop();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRulePostop431); if (state.failed) return ;

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
    // $ANTLR end "entryRulePostop"


    // $ANTLR start "rulePostop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:193:1: rulePostop : ( ruleVariable ( () ( (lv_functionName_2_0= '--' ) ) )? ) ;
    public final void rulePostop() throws RecognitionException {
        Token lv_functionName_2_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:193:11: ( ( ruleVariable ( () ( (lv_functionName_2_0= '--' ) ) )? ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:194:2: ( ruleVariable ( () ( (lv_functionName_2_0= '--' ) ) )? )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:194:2: ( ruleVariable ( () ( (lv_functionName_2_0= '--' ) ) )? )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:195:3: ruleVariable ( () ( (lv_functionName_2_0= '--' ) ) )?
            {
            if ( state.backtracking==0 ) {

              			/* */
              		
            }
            if ( state.backtracking==0 ) {

              			markComposite(elementTypeProvider.getPostop_VariableParserRuleCall_0ElementType());
              		
            }
            pushFollow(FollowSets000.FOLLOW_ruleVariable_in_rulePostop452);
            ruleVariable();

            state._fsp--;
            if (state.failed) return ;
            if ( state.backtracking==0 ) {

              			doneComposite();
              		
            }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:205:3: ( () ( (lv_functionName_2_0= '--' ) ) )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==13) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==RULE_ID) ) {
                    int LA5_3 = input.LA(3);

                    if ( (synpred5_PsiInternalBug296889ExTestLanguage()) ) {
                        alt5=1;
                    }
                }
                else if ( (LA5_1==EOF||LA5_1==13) ) {
                    alt5=1;
                }
            }
            switch (alt5) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:206:4: () ( (lv_functionName_2_0= '--' ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:206:4: ()
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:207:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					/* */
                      				
                    }
                    if ( state.backtracking==0 ) {

                      					precedeComposite(elementTypeProvider.getPostop_PostopExprAction_1_0ElementType());
                      					doneComposite();
                      				
                    }

                    }

                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:215:4: ( (lv_functionName_2_0= '--' ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:216:5: (lv_functionName_2_0= '--' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:216:5: (lv_functionName_2_0= '--' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:217:6: lv_functionName_2_0= '--'
                    {
                    if ( state.backtracking==0 ) {

                      						markLeaf(elementTypeProvider.getPostop_FunctionNameHyphenMinusHyphenMinusKeyword_1_1_0ElementType());
                      					
                    }
                    lv_functionName_2_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_rulePostop509); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      						doneLeaf(lv_functionName_2_0);
                      					
                    }

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
    // $ANTLR end "rulePostop"


    // $ANTLR start "entryRuleVariable"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:231:1: entryRuleVariable : ruleVariable EOF ;
    public final void entryRuleVariable() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:231:18: ( ruleVariable EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:232:2: ruleVariable EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getVariableElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleVariable_in_entryRuleVariable548);
            ruleVariable();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleVariable551); if (state.failed) return ;

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
    // $ANTLR end "entryRuleVariable"


    // $ANTLR start "ruleVariable"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:237:1: ruleVariable : ( () ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final void ruleVariable() throws RecognitionException {
        Token lv_name_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:237:13: ( ( () ( (lv_name_1_0= RULE_ID ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:238:2: ( () ( (lv_name_1_0= RULE_ID ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:238:2: ( () ( (lv_name_1_0= RULE_ID ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:239:3: () ( (lv_name_1_0= RULE_ID ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:239:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:240:4: 
            {
            if ( state.backtracking==0 ) {

              				/* */
              			
            }
            if ( state.backtracking==0 ) {

              				precedeComposite(elementTypeProvider.getVariable_VariableAction_0ElementType());
              				doneComposite();
              			
            }

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:248:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:249:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:249:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:250:5: lv_name_1_0= RULE_ID
            {
            if ( state.backtracking==0 ) {

              					markLeaf(elementTypeProvider.getVariable_NameIDTerminalRuleCall_1_0ElementType());
              				
            }
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleVariable601); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              					doneLeaf(lv_name_1_0);
              				
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
        }
        return ;
    }
    // $ANTLR end "ruleVariable"


    // $ANTLR start "entryRuleDataTypeExpression"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:263:1: entryRuleDataTypeExpression : ruleDataTypeExpression EOF ;
    public final void entryRuleDataTypeExpression() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:263:28: ( ruleDataTypeExpression EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:264:2: ruleDataTypeExpression EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getDataTypeExpressionElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypeExpression_in_entryRuleDataTypeExpression632);
            ruleDataTypeExpression();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleDataTypeExpression635); if (state.failed) return ;

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
    // $ANTLR end "entryRuleDataTypeExpression"


    // $ANTLR start "ruleDataTypeExpression"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:269:1: ruleDataTypeExpression : ( ruleDataTypePostop | ruleDataTypePreop ) ;
    public final void ruleDataTypeExpression() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:269:23: ( ( ruleDataTypePostop | ruleDataTypePreop ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:270:2: ( ruleDataTypePostop | ruleDataTypePreop )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:270:2: ( ruleDataTypePostop | ruleDataTypePreop )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==RULE_ID) ) {
                alt6=1;
            }
            else if ( (LA6_0==13) ) {
                alt6=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:271:3: ruleDataTypePostop
                    {
                    if ( state.backtracking==0 ) {

                      			markComposite(elementTypeProvider.getDataTypeExpression_DataTypePostopParserRuleCall_0ElementType());
                      		
                    }
                    pushFollow(FollowSets000.FOLLOW_ruleDataTypePostop_in_ruleDataTypeExpression652);
                    ruleDataTypePostop();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      			doneComposite();
                      		
                    }

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:279:3: ruleDataTypePreop
                    {
                    if ( state.backtracking==0 ) {

                      			markComposite(elementTypeProvider.getDataTypeExpression_DataTypePreopParserRuleCall_1ElementType());
                      		
                    }
                    pushFollow(FollowSets000.FOLLOW_ruleDataTypePreop_in_ruleDataTypeExpression672);
                    ruleDataTypePreop();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      			doneComposite();
                      		
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
    // $ANTLR end "ruleDataTypeExpression"


    // $ANTLR start "entryRuleDataTypePreop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:290:1: entryRuleDataTypePreop : ruleDataTypePreop EOF ;
    public final void entryRuleDataTypePreop() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:290:23: ( ruleDataTypePreop EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:291:2: ruleDataTypePreop EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getDataTypePreopElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypePreop_in_entryRuleDataTypePreop692);
            ruleDataTypePreop();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleDataTypePreop695); if (state.failed) return ;

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
    // $ANTLR end "entryRuleDataTypePreop"


    // $ANTLR start "ruleDataTypePreop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:296:1: ruleDataTypePreop : (kw= '--' ruleDataTypeVariable ) ;
    public final void ruleDataTypePreop() throws RecognitionException {
        Token kw=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:296:18: ( (kw= '--' ruleDataTypeVariable ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:297:2: (kw= '--' ruleDataTypeVariable )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:297:2: (kw= '--' ruleDataTypeVariable )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:298:3: kw= '--' ruleDataTypeVariable
            {
            if ( state.backtracking==0 ) {

              			markLeaf(elementTypeProvider.getDataTypePreop_HyphenMinusHyphenMinusKeyword_0ElementType());
              		
            }
            kw=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleDataTypePreop714); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              			doneLeaf(kw);
              		
            }
            if ( state.backtracking==0 ) {

              			markComposite(elementTypeProvider.getDataTypePreop_DataTypeVariableParserRuleCall_1ElementType());
              		
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypeVariable_in_ruleDataTypePreop726);
            ruleDataTypeVariable();

            state._fsp--;
            if (state.failed) return ;
            if ( state.backtracking==0 ) {

              			doneComposite();
              		
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
    // $ANTLR end "ruleDataTypePreop"


    // $ANTLR start "entryRuleDataTypePostop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:316:1: entryRuleDataTypePostop : ruleDataTypePostop EOF ;
    public final void entryRuleDataTypePostop() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:316:24: ( ruleDataTypePostop EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:317:2: ruleDataTypePostop EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getDataTypePostopElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypePostop_in_entryRuleDataTypePostop746);
            ruleDataTypePostop();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleDataTypePostop749); if (state.failed) return ;

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
    // $ANTLR end "entryRuleDataTypePostop"


    // $ANTLR start "ruleDataTypePostop"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:322:1: ruleDataTypePostop : ( ruleDataTypeVariable (kw= '--' )? ) ;
    public final void ruleDataTypePostop() throws RecognitionException {
        Token kw=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:322:19: ( ( ruleDataTypeVariable (kw= '--' )? ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:323:2: ( ruleDataTypeVariable (kw= '--' )? )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:323:2: ( ruleDataTypeVariable (kw= '--' )? )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:324:3: ruleDataTypeVariable (kw= '--' )?
            {
            if ( state.backtracking==0 ) {

              			markComposite(elementTypeProvider.getDataTypePostop_DataTypeVariableParserRuleCall_0ElementType());
              		
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypeVariable_in_ruleDataTypePostop766);
            ruleDataTypeVariable();

            state._fsp--;
            if (state.failed) return ;
            if ( state.backtracking==0 ) {

              			doneComposite();
              		
            }
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:331:3: (kw= '--' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==13) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==RULE_ID) ) {
                    int LA7_3 = input.LA(3);

                    if ( (synpred7_PsiInternalBug296889ExTestLanguage()) ) {
                        alt7=1;
                    }
                }
                else if ( (LA7_1==EOF||LA7_1==13) ) {
                    alt7=1;
                }
            }
            switch (alt7) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:332:4: kw= '--'
                    {
                    if ( state.backtracking==0 ) {

                      				markLeaf(elementTypeProvider.getDataTypePostop_HyphenMinusHyphenMinusKeyword_1ElementType());
                      			
                    }
                    kw=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleDataTypePostop786); if (state.failed) return ;
                    if ( state.backtracking==0 ) {

                      				doneLeaf(kw);
                      			
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
    // $ANTLR end "ruleDataTypePostop"


    // $ANTLR start "entryRuleDataTypeVariable"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:344:1: entryRuleDataTypeVariable : ruleDataTypeVariable EOF ;
    public final void entryRuleDataTypeVariable() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:344:26: ( ruleDataTypeVariable EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:345:2: ruleDataTypeVariable EOF
            {
            if ( state.backtracking==0 ) {
               markComposite(elementTypeProvider.getDataTypeVariableElementType()); 
            }
            pushFollow(FollowSets000.FOLLOW_ruleDataTypeVariable_in_entryRuleDataTypeVariable812);
            ruleDataTypeVariable();

            state._fsp--;
            if (state.failed) return ;
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleDataTypeVariable815); if (state.failed) return ;

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
    // $ANTLR end "entryRuleDataTypeVariable"


    // $ANTLR start "ruleDataTypeVariable"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:350:1: ruleDataTypeVariable : this_ID_0= RULE_ID ;
    public final void ruleDataTypeVariable() throws RecognitionException {
        Token this_ID_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:350:21: (this_ID_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:351:2: this_ID_0= RULE_ID
            {
            if ( state.backtracking==0 ) {

              		markLeaf(elementTypeProvider.getDataTypeVariable_IDTerminalRuleCallElementType());
              	
            }
            this_ID_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleDataTypeVariable829); if (state.failed) return ;
            if ( state.backtracking==0 ) {

              		doneLeaf(this_ID_0);
              	
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
    // $ANTLR end "ruleDataTypeVariable"

    // $ANTLR start synpred5_PsiInternalBug296889ExTestLanguage
    public final void synpred5_PsiInternalBug296889ExTestLanguage_fragment() throws RecognitionException {   
        Token lv_functionName_2_0=null;

        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:206:4: ( () ( (lv_functionName_2_0= '--' ) ) )
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:206:4: () ( (lv_functionName_2_0= '--' ) )
        {
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:206:4: ()
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:207:5: 
        {
        if ( state.backtracking==0 ) {

          					/* */
          				
        }

        }

        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:215:4: ( (lv_functionName_2_0= '--' ) )
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:216:5: (lv_functionName_2_0= '--' )
        {
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:216:5: (lv_functionName_2_0= '--' )
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:217:6: lv_functionName_2_0= '--'
        {
        if ( state.backtracking==0 ) {

          						markLeaf(elementTypeProvider.getPostop_FunctionNameHyphenMinusHyphenMinusKeyword_1_1_0ElementType());
          					
        }
        lv_functionName_2_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_synpred5_PsiInternalBug296889ExTestLanguage509); if (state.failed) return ;

        }


        }


        }
    }
    // $ANTLR end synpred5_PsiInternalBug296889ExTestLanguage

    // $ANTLR start synpred7_PsiInternalBug296889ExTestLanguage
    public final void synpred7_PsiInternalBug296889ExTestLanguage_fragment() throws RecognitionException {   
        Token kw=null;

        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:332:4: (kw= '--' )
        // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug296889ExTestLanguage.g:332:4: kw= '--'
        {
        if ( state.backtracking==0 ) {

          				markLeaf(elementTypeProvider.getDataTypePostop_HyphenMinusHyphenMinusKeyword_1ElementType());
          			
        }
        kw=(Token)match(input,13,FollowSets000.FOLLOW_13_in_synpred7_PsiInternalBug296889ExTestLanguage786); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_PsiInternalBug296889ExTestLanguage

    // Delegated rules

    public final boolean synpred5_PsiInternalBug296889ExTestLanguage() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_PsiInternalBug296889ExTestLanguage_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_PsiInternalBug296889ExTestLanguage() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_PsiInternalBug296889ExTestLanguage_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel60 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel63 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleModel88 = new BitSet(new long[]{0x0000000000002012L});
        public static final BitSet FOLLOW_ruleExpression_in_ruleModel120 = new BitSet(new long[]{0x0000000000002012L});
        public static final BitSet FOLLOW_12_in_ruleModel167 = new BitSet(new long[]{0x0000000000002012L});
        public static final BitSet FOLLOW_ruleDataTypeExpression_in_ruleModel199 = new BitSet(new long[]{0x0000000000002012L});
        public static final BitSet FOLLOW_ruleExpression_in_entryRuleExpression238 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleExpression241 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rulePostop_in_ruleExpression262 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rulePreop_in_ruleExpression286 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rulePreop_in_entryRulePreop306 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRulePreop309 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_rulePreop359 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_ruleVariable_in_rulePreop397 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rulePostop_in_entryRulePostop428 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRulePostop431 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleVariable_in_rulePostop452 = new BitSet(new long[]{0x0000000000002002L});
        public static final BitSet FOLLOW_13_in_rulePostop509 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleVariable_in_entryRuleVariable548 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleVariable551 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleVariable601 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypeExpression_in_entryRuleDataTypeExpression632 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleDataTypeExpression635 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypePostop_in_ruleDataTypeExpression652 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypePreop_in_ruleDataTypeExpression672 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypePreop_in_entryRuleDataTypePreop692 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleDataTypePreop695 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleDataTypePreop714 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_ruleDataTypeVariable_in_ruleDataTypePreop726 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypePostop_in_entryRuleDataTypePostop746 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleDataTypePostop749 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypeVariable_in_ruleDataTypePostop766 = new BitSet(new long[]{0x0000000000002002L});
        public static final BitSet FOLLOW_13_in_ruleDataTypePostop786 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleDataTypeVariable_in_entryRuleDataTypeVariable812 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleDataTypeVariable815 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleDataTypeVariable829 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_synpred5_PsiInternalBug296889ExTestLanguage509 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_synpred7_PsiInternalBug296889ExTestLanguage786 = new BitSet(new long[]{0x0000000000000002L});
    }


}