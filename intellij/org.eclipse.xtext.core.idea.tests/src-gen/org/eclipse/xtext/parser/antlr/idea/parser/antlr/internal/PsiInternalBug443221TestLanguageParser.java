package org.eclipse.xtext.parser.antlr.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parser.antlr.idea.lang.Bug443221TestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.services.Bug443221TestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalBug443221TestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'dummy'", "'_'", "'_dummy_'", "'__dummy__'", "'$__dummy__'", "'$'", "'DollarSign'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalBug443221TestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBug443221TestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalBug443221TestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g"; }



    	protected Bug443221TestLanguageGrammarAccess grammarAccess;

    	protected Bug443221TestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalBug443221TestLanguageParser(PsiBuilder builder, TokenStream input, Bug443221TestLanguageElementTypeProvider elementTypeProvider, Bug443221TestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:58:1: ruleModel : ( () otherlv_1= 'dummy' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '_' otherlv_4= '_dummy_' otherlv_5= '__dummy__' otherlv_6= '$__dummy__' otherlv_7= '$' otherlv_8= 'DollarSign' ) ;
    public final void ruleModel() throws RecognitionException {
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_7=null;
        Token otherlv_8=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:58:10: ( ( () otherlv_1= 'dummy' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '_' otherlv_4= '_dummy_' otherlv_5= '__dummy__' otherlv_6= '$__dummy__' otherlv_7= '$' otherlv_8= 'DollarSign' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:59:2: ( () otherlv_1= 'dummy' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '_' otherlv_4= '_dummy_' otherlv_5= '__dummy__' otherlv_6= '$__dummy__' otherlv_7= '$' otherlv_8= 'DollarSign' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:59:2: ( () otherlv_1= 'dummy' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '_' otherlv_4= '_dummy_' otherlv_5= '__dummy__' otherlv_6= '$__dummy__' otherlv_7= '$' otherlv_8= 'DollarSign' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:60:3: () otherlv_1= 'dummy' ( (lv_name_2_0= RULE_ID ) ) otherlv_3= '_' otherlv_4= '_dummy_' otherlv_5= '__dummy__' otherlv_6= '$__dummy__' otherlv_7= '$' otherlv_8= 'DollarSign'
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:60:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:61:4: 
            {

            				precedeComposite(elementTypeProvider.getModel_ModelAction_0ElementType());
            				doneComposite();
            			

            }


            			markLeaf(elementTypeProvider.getModel_DummyKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleModel89); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:73:3: ( (lv_name_2_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:74:4: (lv_name_2_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:74:4: (lv_name_2_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parser/antlr/idea/parser/antlr/internal/PsiInternalBug443221TestLanguage.g:75:5: lv_name_2_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getModel_NameIDTerminalRuleCall_2_0ElementType());
            				
            lv_name_2_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleModel116); 

            					doneLeaf(lv_name_2_0);
            				

            }


            }


            			markLeaf(elementTypeProvider.getModel__Keyword_3ElementType());
            		
            otherlv_3=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleModel141); 

            			doneLeaf(otherlv_3);
            		

            			markLeaf(elementTypeProvider.getModel__dummy_Keyword_4ElementType());
            		
            otherlv_4=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleModel155); 

            			doneLeaf(otherlv_4);
            		

            			markLeaf(elementTypeProvider.getModel___dummy__Keyword_5ElementType());
            		
            otherlv_5=(Token)match(input,14,FollowSets000.FOLLOW_14_in_ruleModel169); 

            			doneLeaf(otherlv_5);
            		

            			markLeaf(elementTypeProvider.getModel___dummy__Keyword_6ElementType());
            		
            otherlv_6=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleModel183); 

            			doneLeaf(otherlv_6);
            		

            			markLeaf(elementTypeProvider.getModel_DollarSignKeyword_7ElementType());
            		
            otherlv_7=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleModel197); 

            			doneLeaf(otherlv_7);
            		

            			markLeaf(elementTypeProvider.getModel_DollarSignKeyword_8ElementType());
            		
            otherlv_8=(Token)match(input,17,FollowSets000.FOLLOW_17_in_ruleModel211); 

            			doneLeaf(otherlv_8);
            		

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

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleModel89 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleModel116 = new BitSet(new long[]{0x0000000000001000L});
        public static final BitSet FOLLOW_12_in_ruleModel141 = new BitSet(new long[]{0x0000000000002000L});
        public static final BitSet FOLLOW_13_in_ruleModel155 = new BitSet(new long[]{0x0000000000004000L});
        public static final BitSet FOLLOW_14_in_ruleModel169 = new BitSet(new long[]{0x0000000000008000L});
        public static final BitSet FOLLOW_15_in_ruleModel183 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_ruleModel197 = new BitSet(new long[]{0x0000000000020000L});
        public static final BitSet FOLLOW_17_in_ruleModel211 = new BitSet(new long[]{0x0000000000000002L});
    }


}