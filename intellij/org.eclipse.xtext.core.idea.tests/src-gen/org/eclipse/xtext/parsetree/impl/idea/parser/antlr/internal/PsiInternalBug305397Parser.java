package org.eclipse.xtext.parsetree.impl.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parsetree.impl.idea.lang.Bug305397ElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parsetree.impl.services.Bug305397GrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalBug305397Parser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'a'", "'element'", "'end'"
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


        public PsiInternalBug305397Parser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBug305397Parser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalBug305397Parser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g"; }



    	protected Bug305397GrammarAccess grammarAccess;

    	protected Bug305397ElementTypeProvider elementTypeProvider;

    	public PsiInternalBug305397Parser(PsiBuilder builder, TokenStream input, Bug305397ElementTypeProvider elementTypeProvider, Bug305397GrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:58:1: ruleModel : ( (lv_elements_0_0= ruleElement ) )* ;
    public final void ruleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:58:10: ( ( (lv_elements_0_0= ruleElement ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:59:2: ( (lv_elements_0_0= ruleElement ) )*
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:59:2: ( (lv_elements_0_0= ruleElement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=11 && LA1_0<=12)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:60:3: (lv_elements_0_0= ruleElement )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:60:3: (lv_elements_0_0= ruleElement )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:61:4: lv_elements_0_0= ruleElement
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:73:1: entryRuleElement : ruleElement EOF ;
    public final void entryRuleElement() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:73:17: ( ruleElement EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:74:2: ruleElement EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:79:1: ruleElement : ( () ( (lv_a_1_0= 'a' ) )? otherlv_2= 'element' ( (lv_name_3_0= RULE_ID ) )? ( (lv_elements_4_0= ruleElement ) )* otherlv_5= 'end' ) ;
    public final void ruleElement() throws RecognitionException {
        Token lv_a_1_0=null;
        Token otherlv_2=null;
        Token lv_name_3_0=null;
        Token otherlv_5=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:79:12: ( ( () ( (lv_a_1_0= 'a' ) )? otherlv_2= 'element' ( (lv_name_3_0= RULE_ID ) )? ( (lv_elements_4_0= ruleElement ) )* otherlv_5= 'end' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:80:2: ( () ( (lv_a_1_0= 'a' ) )? otherlv_2= 'element' ( (lv_name_3_0= RULE_ID ) )? ( (lv_elements_4_0= ruleElement ) )* otherlv_5= 'end' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:80:2: ( () ( (lv_a_1_0= 'a' ) )? otherlv_2= 'element' ( (lv_name_3_0= RULE_ID ) )? ( (lv_elements_4_0= ruleElement ) )* otherlv_5= 'end' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:81:3: () ( (lv_a_1_0= 'a' ) )? otherlv_2= 'element' ( (lv_name_3_0= RULE_ID ) )? ( (lv_elements_4_0= ruleElement ) )* otherlv_5= 'end'
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:81:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:82:4: 
            {

            				precedeComposite(elementTypeProvider.getElement_ElementAction_0ElementType());
            				doneComposite();
            			

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:87:3: ( (lv_a_1_0= 'a' ) )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==11) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:88:4: (lv_a_1_0= 'a' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:88:4: (lv_a_1_0= 'a' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:89:5: lv_a_1_0= 'a'
                    {

                    					markLeaf(elementTypeProvider.getElement_AAKeyword_1_0ElementType());
                    				
                    lv_a_1_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleElement156); 

                    					doneLeaf(lv_a_1_0);
                    				

                    }


                    }
                    break;

            }


            			markLeaf(elementTypeProvider.getElement_ElementKeyword_2ElementType());
            		
            otherlv_2=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleElement182); 

            			doneLeaf(otherlv_2);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:105:3: ( (lv_name_3_0= RULE_ID ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==RULE_ID) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:106:4: (lv_name_3_0= RULE_ID )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:106:4: (lv_name_3_0= RULE_ID )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:107:5: lv_name_3_0= RULE_ID
                    {

                    					markLeaf(elementTypeProvider.getElement_NameIDTerminalRuleCall_3_0ElementType());
                    				
                    lv_name_3_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleElement209); 

                    					doneLeaf(lv_name_3_0);
                    				

                    }


                    }
                    break;

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:116:3: ( (lv_elements_4_0= ruleElement ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>=11 && LA4_0<=12)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:117:4: (lv_elements_4_0= ruleElement )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:117:4: (lv_elements_4_0= ruleElement )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/impl/idea/parser/antlr/internal/PsiInternalBug305397.g:118:5: lv_elements_4_0= ruleElement
            	    {

            	    					markComposite(elementTypeProvider.getElement_ElementsElementParserRuleCall_4_0ElementType());
            	    				
            	    pushFollow(FollowSets000.FOLLOW_ruleElement_in_ruleElement248);
            	    ruleElement();

            	    state._fsp--;


            	    					doneComposite();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            			markLeaf(elementTypeProvider.getElement_EndKeyword_5ElementType());
            		
            otherlv_5=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleElement274); 

            			doneLeaf(otherlv_5);
            		

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
        public static final BitSet FOLLOW_11_in_ruleElement156 = new BitSet(new long[]{0x0000000000001000L});
        public static final BitSet FOLLOW_12_in_ruleElement182 = new BitSet(new long[]{0x0000000000003810L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleElement209 = new BitSet(new long[]{0x0000000000003800L});
        public static final BitSet FOLLOW_ruleElement_in_ruleElement248 = new BitSet(new long[]{0x0000000000003800L});
        public static final BitSet FOLLOW_13_in_ruleElement274 = new BitSet(new long[]{0x0000000000000002L});
    }


}