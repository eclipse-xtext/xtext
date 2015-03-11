package org.eclipse.xtext.dummy.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.dummy.idea.lang.DummyTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.dummy.services.DummyTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalDummyTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_STRING", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'optional'", "'element'", "';'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=5;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=6;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalDummyTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalDummyTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalDummyTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g"; }



    	protected DummyTestLanguageGrammarAccess grammarAccess;

    	protected DummyTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalDummyTestLanguageParser(PsiBuilder builder, TokenStream input, DummyTestLanguageElementTypeProvider elementTypeProvider, DummyTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:58:1: ruleModel : ( (lv_elements_0_0= ruleElement ) )* ;
    public final void ruleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:58:10: ( ( (lv_elements_0_0= ruleElement ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:59:2: ( (lv_elements_0_0= ruleElement ) )*
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:59:2: ( (lv_elements_0_0= ruleElement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=11 && LA1_0<=12)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:60:3: (lv_elements_0_0= ruleElement )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:60:3: (lv_elements_0_0= ruleElement )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:61:4: lv_elements_0_0= ruleElement
            	    {

            	    				markComposite(elementTypeProvider.getModel_ElementsElementParserRuleCall_0ElementType());
            	    			
            	    pushFollow(FollowSets000.FOLLOW_ruleElement_in_ruleModel82);
            	    ruleElement();

            	    state._fsp--;


            	    				doneComposite();
            	    			

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


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


    // $ANTLR start "entryRuleElement"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:73:1: entryRuleElement : ruleElement EOF ;
    public final void entryRuleElement() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:73:17: ( ruleElement EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:74:2: ruleElement EOF
            {
             markComposite(elementTypeProvider.getElementElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleElement_in_entryRuleElement108);
            ruleElement();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleElement111); 

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
    // $ANTLR end "entryRuleElement"


    // $ANTLR start "ruleElement"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:79:1: ruleElement : ( ( (lv_optional_0_0= 'optional' ) )? otherlv_1= 'element' ( (lv_name_2_0= RULE_ID ) ) ( (lv_descriptions_3_0= RULE_STRING ) )* otherlv_4= ';' ) ;
    public final void ruleElement() throws RecognitionException {
        Token lv_optional_0_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token lv_descriptions_3_0=null;
        Token otherlv_4=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:79:12: ( ( ( (lv_optional_0_0= 'optional' ) )? otherlv_1= 'element' ( (lv_name_2_0= RULE_ID ) ) ( (lv_descriptions_3_0= RULE_STRING ) )* otherlv_4= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:80:2: ( ( (lv_optional_0_0= 'optional' ) )? otherlv_1= 'element' ( (lv_name_2_0= RULE_ID ) ) ( (lv_descriptions_3_0= RULE_STRING ) )* otherlv_4= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:80:2: ( ( (lv_optional_0_0= 'optional' ) )? otherlv_1= 'element' ( (lv_name_2_0= RULE_ID ) ) ( (lv_descriptions_3_0= RULE_STRING ) )* otherlv_4= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:81:3: ( (lv_optional_0_0= 'optional' ) )? otherlv_1= 'element' ( (lv_name_2_0= RULE_ID ) ) ( (lv_descriptions_3_0= RULE_STRING ) )* otherlv_4= ';'
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:81:3: ( (lv_optional_0_0= 'optional' ) )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==11) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:82:4: (lv_optional_0_0= 'optional' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:82:4: (lv_optional_0_0= 'optional' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:83:5: lv_optional_0_0= 'optional'
                    {

                    					markLeaf(elementTypeProvider.getElement_OptionalOptionalKeyword_0_0ElementType());
                    				
                    lv_optional_0_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleElement143); 

                    					doneLeaf(lv_optional_0_0);
                    				

                    }


                    }
                    break;

            }


            			markLeaf(elementTypeProvider.getElement_ElementKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleElement169); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:99:3: ( (lv_name_2_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:100:4: (lv_name_2_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:100:4: (lv_name_2_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:101:5: lv_name_2_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getElement_NameIDTerminalRuleCall_2_0ElementType());
            				
            lv_name_2_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleElement196); 

            					doneLeaf(lv_name_2_0);
            				

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:110:3: ( (lv_descriptions_3_0= RULE_STRING ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==RULE_STRING) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:111:4: (lv_descriptions_3_0= RULE_STRING )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:111:4: (lv_descriptions_3_0= RULE_STRING )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/dummy/idea/parser/antlr/internal/PsiInternalDummyTestLanguage.g:112:5: lv_descriptions_3_0= RULE_STRING
            	    {

            	    					markLeaf(elementTypeProvider.getElement_DescriptionsSTRINGTerminalRuleCall_3_0ElementType());
            	    				
            	    lv_descriptions_3_0=(Token)match(input,RULE_STRING,FollowSets000.FOLLOW_RULE_STRING_in_ruleElement234); 

            	    					doneLeaf(lv_descriptions_3_0);
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            			markLeaf(elementTypeProvider.getElement_SemicolonKeyword_4ElementType());
            		
            otherlv_4=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleElement260); 

            			doneLeaf(otherlv_4);
            		

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
    // $ANTLR end "ruleElement"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleElement_in_ruleModel82 = new BitSet(new long[]{0x0000000000001802L});
        public static final BitSet FOLLOW_ruleElement_in_entryRuleElement108 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleElement111 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleElement143 = new BitSet(new long[]{0x0000000000001000L});
        public static final BitSet FOLLOW_12_in_ruleElement169 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleElement196 = new BitSet(new long[]{0x0000000000002020L});
        public static final BitSet FOLLOW_RULE_STRING_in_ruleElement234 = new BitSet(new long[]{0x0000000000002020L});
        public static final BitSet FOLLOW_13_in_ruleElement260 = new BitSet(new long[]{0x0000000000000002L});
    }


}