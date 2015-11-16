package org.eclipse.xtext.parsetree.reconstr.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parsetree.reconstr.idea.lang.Bug299395TestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parsetree.reconstr.services.Bug299395TestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalBug299395TestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'{'", "'}'", "'['", "']'", "'subModel'", "'1'", "'2'"
    };
    public static final int RULE_ID=5;
    public static final int RULE_STRING=4;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=6;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalBug299395TestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBug299395TestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalBug299395TestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "PsiInternalBug299395TestLanguage.g"; }



    	protected Bug299395TestLanguageGrammarAccess grammarAccess;

    	protected Bug299395TestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalBug299395TestLanguageParser(PsiBuilder builder, TokenStream input, Bug299395TestLanguageElementTypeProvider elementTypeProvider, Bug299395TestLanguageGrammarAccess grammarAccess) {
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
    // PsiInternalBug299395TestLanguage.g:52:1: entryRuleModel returns [Boolean current=false] : iv_ruleModel= ruleModel EOF ;
    public final Boolean entryRuleModel() throws RecognitionException {
        Boolean current = false;

        Boolean iv_ruleModel = null;


        try {
            // PsiInternalBug299395TestLanguage.g:52:47: (iv_ruleModel= ruleModel EOF )
            // PsiInternalBug299395TestLanguage.g:53:2: iv_ruleModel= ruleModel EOF
            {
             markComposite(elementTypeProvider.getModelElementType()); 
            pushFollow(FollowSets000.FOLLOW_1);
            iv_ruleModel=ruleModel();

            state._fsp--;

             current =iv_ruleModel; 
            match(input,EOF,FollowSets000.FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // PsiInternalBug299395TestLanguage.g:59:1: ruleModel returns [Boolean current=false] : ( () (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )* (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )* (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )? ) ;
    public final Boolean ruleModel() throws RecognitionException {
        Boolean current = false;

        Token otherlv_1=null;
        Token lv_strings_2_0=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token lv_keys_5_0=null;
        Token lv_values_6_0=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Boolean lv_subModel_9_0 = null;


        try {
            // PsiInternalBug299395TestLanguage.g:60:1: ( ( () (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )* (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )* (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )? ) )
            // PsiInternalBug299395TestLanguage.g:61:2: ( () (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )* (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )* (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )? )
            {
            // PsiInternalBug299395TestLanguage.g:61:2: ( () (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )* (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )* (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )? )
            // PsiInternalBug299395TestLanguage.g:62:3: () (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )* (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )* (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )?
            {
            // PsiInternalBug299395TestLanguage.g:62:3: ()
            // PsiInternalBug299395TestLanguage.g:63:4: 
            {

            				precedeComposite(elementTypeProvider.getModel_ModelAction_0ElementType());
            				doneComposite();
            				associateWithSemanticElement();
            			

            }

            // PsiInternalBug299395TestLanguage.g:69:3: (otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==11) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // PsiInternalBug299395TestLanguage.g:70:4: otherlv_1= '{' ( (lv_strings_2_0= RULE_STRING ) )? otherlv_3= '}'
            	    {

            	    				markLeaf(elementTypeProvider.getModel_LeftCurlyBracketKeyword_1_0ElementType());
            	    			
            	    otherlv_1=(Token)match(input,11,FollowSets000.FOLLOW_3); 

            	    				doneLeaf(otherlv_1);
            	    			
            	    // PsiInternalBug299395TestLanguage.g:77:4: ( (lv_strings_2_0= RULE_STRING ) )?
            	    int alt1=2;
            	    int LA1_0 = input.LA(1);

            	    if ( (LA1_0==RULE_STRING) ) {
            	        alt1=1;
            	    }
            	    switch (alt1) {
            	        case 1 :
            	            // PsiInternalBug299395TestLanguage.g:78:5: (lv_strings_2_0= RULE_STRING )
            	            {
            	            // PsiInternalBug299395TestLanguage.g:78:5: (lv_strings_2_0= RULE_STRING )
            	            // PsiInternalBug299395TestLanguage.g:79:6: lv_strings_2_0= RULE_STRING
            	            {

            	            						markLeaf(elementTypeProvider.getModel_StringsSTRINGTerminalRuleCall_1_1_0ElementType());
            	            					
            	            lv_strings_2_0=(Token)match(input,RULE_STRING,FollowSets000.FOLLOW_4); 

            	            						if(!current) {
            	            							associateWithSemanticElement();
            	            							current = true;
            	            						}
            	            					

            	            						doneLeaf(lv_strings_2_0);
            	            					

            	            }


            	            }
            	            break;

            	    }


            	    				markLeaf(elementTypeProvider.getModel_RightCurlyBracketKeyword_1_2ElementType());
            	    			
            	    otherlv_3=(Token)match(input,12,FollowSets000.FOLLOW_5); 

            	    				doneLeaf(otherlv_3);
            	    			

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            // PsiInternalBug299395TestLanguage.g:102:3: (otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==13) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // PsiInternalBug299395TestLanguage.g:103:4: otherlv_4= '[' ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )* otherlv_7= ']'
            	    {

            	    				markLeaf(elementTypeProvider.getModel_LeftSquareBracketKeyword_2_0ElementType());
            	    			
            	    otherlv_4=(Token)match(input,13,FollowSets000.FOLLOW_6); 

            	    				doneLeaf(otherlv_4);
            	    			
            	    // PsiInternalBug299395TestLanguage.g:110:4: ( ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) ) )*
            	    loop3:
            	    do {
            	        int alt3=2;
            	        int LA3_0 = input.LA(1);

            	        if ( (LA3_0==RULE_STRING) ) {
            	            alt3=1;
            	        }


            	        switch (alt3) {
            	    	case 1 :
            	    	    // PsiInternalBug299395TestLanguage.g:111:5: ( (lv_keys_5_0= RULE_STRING ) ) ( (lv_values_6_0= RULE_STRING ) )
            	    	    {
            	    	    // PsiInternalBug299395TestLanguage.g:111:5: ( (lv_keys_5_0= RULE_STRING ) )
            	    	    // PsiInternalBug299395TestLanguage.g:112:6: (lv_keys_5_0= RULE_STRING )
            	    	    {
            	    	    // PsiInternalBug299395TestLanguage.g:112:6: (lv_keys_5_0= RULE_STRING )
            	    	    // PsiInternalBug299395TestLanguage.g:113:7: lv_keys_5_0= RULE_STRING
            	    	    {

            	    	    							markLeaf(elementTypeProvider.getModel_KeysSTRINGTerminalRuleCall_2_1_0_0ElementType());
            	    	    						
            	    	    lv_keys_5_0=(Token)match(input,RULE_STRING,FollowSets000.FOLLOW_7); 

            	    	    							if(!current) {
            	    	    								associateWithSemanticElement();
            	    	    								current = true;
            	    	    							}
            	    	    						

            	    	    							doneLeaf(lv_keys_5_0);
            	    	    						

            	    	    }


            	    	    }

            	    	    // PsiInternalBug299395TestLanguage.g:128:5: ( (lv_values_6_0= RULE_STRING ) )
            	    	    // PsiInternalBug299395TestLanguage.g:129:6: (lv_values_6_0= RULE_STRING )
            	    	    {
            	    	    // PsiInternalBug299395TestLanguage.g:129:6: (lv_values_6_0= RULE_STRING )
            	    	    // PsiInternalBug299395TestLanguage.g:130:7: lv_values_6_0= RULE_STRING
            	    	    {

            	    	    							markLeaf(elementTypeProvider.getModel_ValuesSTRINGTerminalRuleCall_2_1_1_0ElementType());
            	    	    						
            	    	    lv_values_6_0=(Token)match(input,RULE_STRING,FollowSets000.FOLLOW_6); 

            	    	    							if(!current) {
            	    	    								associateWithSemanticElement();
            	    	    								current = true;
            	    	    							}
            	    	    						

            	    	    							doneLeaf(lv_values_6_0);
            	    	    						

            	    	    }


            	    	    }


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop3;
            	        }
            	    } while (true);


            	    				markLeaf(elementTypeProvider.getModel_RightSquareBracketKeyword_2_2ElementType());
            	    			
            	    otherlv_7=(Token)match(input,14,FollowSets000.FOLLOW_8); 

            	    				doneLeaf(otherlv_7);
            	    			

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // PsiInternalBug299395TestLanguage.g:154:3: (otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) ) )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==15) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // PsiInternalBug299395TestLanguage.g:155:4: otherlv_8= 'subModel' ( (lv_subModel_9_0= ruleSubModel ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_SubModelKeyword_3_0ElementType());
                    			
                    otherlv_8=(Token)match(input,15,FollowSets000.FOLLOW_9); 

                    				doneLeaf(otherlv_8);
                    			
                    // PsiInternalBug299395TestLanguage.g:162:4: ( (lv_subModel_9_0= ruleSubModel ) )
                    // PsiInternalBug299395TestLanguage.g:163:5: (lv_subModel_9_0= ruleSubModel )
                    {
                    // PsiInternalBug299395TestLanguage.g:163:5: (lv_subModel_9_0= ruleSubModel )
                    // PsiInternalBug299395TestLanguage.g:164:6: lv_subModel_9_0= ruleSubModel
                    {

                    						markComposite(elementTypeProvider.getModel_SubModelSubModelParserRuleCall_3_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_2);
                    lv_subModel_9_0=ruleSubModel();

                    state._fsp--;


                    						doneComposite();
                    						if(!current) {
                    							associateWithSemanticElement();
                    							current = true;
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
        return current;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleSubModel"
    // PsiInternalBug299395TestLanguage.g:182:1: entryRuleSubModel returns [Boolean current=false] : iv_ruleSubModel= ruleSubModel EOF ;
    public final Boolean entryRuleSubModel() throws RecognitionException {
        Boolean current = false;

        Boolean iv_ruleSubModel = null;


        try {
            // PsiInternalBug299395TestLanguage.g:182:50: (iv_ruleSubModel= ruleSubModel EOF )
            // PsiInternalBug299395TestLanguage.g:183:2: iv_ruleSubModel= ruleSubModel EOF
            {
             markComposite(elementTypeProvider.getSubModelElementType()); 
            pushFollow(FollowSets000.FOLLOW_1);
            iv_ruleSubModel=ruleSubModel();

            state._fsp--;

             current =iv_ruleSubModel; 
            match(input,EOF,FollowSets000.FOLLOW_2); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSubModel"


    // $ANTLR start "ruleSubModel"
    // PsiInternalBug299395TestLanguage.g:189:1: ruleSubModel returns [Boolean current=false] : ( () (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )* ) ;
    public final Boolean ruleSubModel() throws RecognitionException {
        Boolean current = false;

        Token otherlv_1=null;
        Token otherlv_2=null;
        Token lv_strings_3_0=null;

        try {
            // PsiInternalBug299395TestLanguage.g:190:1: ( ( () (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )* ) )
            // PsiInternalBug299395TestLanguage.g:191:2: ( () (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )* )
            {
            // PsiInternalBug299395TestLanguage.g:191:2: ( () (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )* )
            // PsiInternalBug299395TestLanguage.g:192:3: () (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )*
            {
            // PsiInternalBug299395TestLanguage.g:192:3: ()
            // PsiInternalBug299395TestLanguage.g:193:4: 
            {

            				precedeComposite(elementTypeProvider.getSubModel_SubModelAction_0ElementType());
            				doneComposite();
            				associateWithSemanticElement();
            			

            }

            // PsiInternalBug299395TestLanguage.g:199:3: (otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )? )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==16) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // PsiInternalBug299395TestLanguage.g:200:4: otherlv_1= '1' otherlv_2= '2' ( (lv_strings_3_0= RULE_STRING ) )?
            	    {

            	    				markLeaf(elementTypeProvider.getSubModel_DigitOneKeyword_1_0ElementType());
            	    			
            	    otherlv_1=(Token)match(input,16,FollowSets000.FOLLOW_10); 

            	    				doneLeaf(otherlv_1);
            	    			

            	    				markLeaf(elementTypeProvider.getSubModel_DigitTwoKeyword_1_1ElementType());
            	    			
            	    otherlv_2=(Token)match(input,17,FollowSets000.FOLLOW_11); 

            	    				doneLeaf(otherlv_2);
            	    			
            	    // PsiInternalBug299395TestLanguage.g:214:4: ( (lv_strings_3_0= RULE_STRING ) )?
            	    int alt6=2;
            	    int LA6_0 = input.LA(1);

            	    if ( (LA6_0==RULE_STRING) ) {
            	        alt6=1;
            	    }
            	    switch (alt6) {
            	        case 1 :
            	            // PsiInternalBug299395TestLanguage.g:215:5: (lv_strings_3_0= RULE_STRING )
            	            {
            	            // PsiInternalBug299395TestLanguage.g:215:5: (lv_strings_3_0= RULE_STRING )
            	            // PsiInternalBug299395TestLanguage.g:216:6: lv_strings_3_0= RULE_STRING
            	            {

            	            						markLeaf(elementTypeProvider.getSubModel_StringsSTRINGTerminalRuleCall_1_2_0ElementType());
            	            					
            	            lv_strings_3_0=(Token)match(input,RULE_STRING,FollowSets000.FOLLOW_12); 

            	            						if(!current) {
            	            							associateWithSemanticElement();
            	            							current = true;
            	            						}
            	            					

            	            						doneLeaf(lv_strings_3_0);
            	            					

            	            }


            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop7;
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
        }
        return current;
    }
    // $ANTLR end "ruleSubModel"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000000000001010L});
        public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000001000L});
        public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x000000000000A802L});
        public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000004010L});
        public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x000000000000A002L});
        public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000020000L});
        public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000010012L});
        public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000010002L});
    }


}