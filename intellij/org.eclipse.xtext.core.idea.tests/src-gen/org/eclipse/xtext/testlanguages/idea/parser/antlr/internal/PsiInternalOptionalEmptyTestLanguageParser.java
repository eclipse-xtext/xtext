package org.eclipse.xtext.testlanguages.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.testlanguages.idea.lang.OptionalEmptyTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.testlanguages.services.OptionalEmptyTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalOptionalEmptyTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'hallo'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int T__11=11;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalOptionalEmptyTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalOptionalEmptyTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalOptionalEmptyTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g"; }



    	protected OptionalEmptyTestLanguageGrammarAccess grammarAccess;

    	protected OptionalEmptyTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalOptionalEmptyTestLanguageParser(PsiBuilder builder, TokenStream input, OptionalEmptyTestLanguageElementTypeProvider elementTypeProvider, OptionalEmptyTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:58:1: ruleModel : ( (lv_child_0_0= ruleGreeting ) )? ;
    public final void ruleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:58:10: ( ( (lv_child_0_0= ruleGreeting ) )? )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:59:2: ( (lv_child_0_0= ruleGreeting ) )?
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:59:2: ( (lv_child_0_0= ruleGreeting ) )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==11) ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:60:3: (lv_child_0_0= ruleGreeting )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:60:3: (lv_child_0_0= ruleGreeting )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:61:4: lv_child_0_0= ruleGreeting
                    {

                    				markComposite(elementTypeProvider.getModel_ChildGreetingParserRuleCall_0ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleGreeting_in_ruleModel82);
                    ruleGreeting();

                    state._fsp--;


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
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleGreeting"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:73:1: entryRuleGreeting : ruleGreeting EOF ;
    public final void entryRuleGreeting() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:73:18: ( ruleGreeting EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:74:2: ruleGreeting EOF
            {
             markComposite(elementTypeProvider.getGreetingElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleGreeting_in_entryRuleGreeting108);
            ruleGreeting();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleGreeting111); 

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
    // $ANTLR end "entryRuleGreeting"


    // $ANTLR start "ruleGreeting"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:79:1: ruleGreeting : (otherlv_0= 'hallo' ( (lv_name_1_0= RULE_ID ) ) ) ;
    public final void ruleGreeting() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:79:13: ( (otherlv_0= 'hallo' ( (lv_name_1_0= RULE_ID ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:80:2: (otherlv_0= 'hallo' ( (lv_name_1_0= RULE_ID ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:80:2: (otherlv_0= 'hallo' ( (lv_name_1_0= RULE_ID ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:81:3: otherlv_0= 'hallo' ( (lv_name_1_0= RULE_ID ) )
            {

            			markLeaf(elementTypeProvider.getGreeting_HalloKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleGreeting130); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:88:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:89:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:89:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/testlanguages/idea/parser/antlr/internal/PsiInternalOptionalEmptyTestLanguage.g:90:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getGreeting_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleGreeting157); 

            					doneLeaf(lv_name_1_0);
            				

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
    // $ANTLR end "ruleGreeting"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleGreeting_in_ruleModel82 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleGreeting_in_entryRuleGreeting108 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleGreeting111 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleGreeting130 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleGreeting157 = new BitSet(new long[]{0x0000000000000002L});
    }


}