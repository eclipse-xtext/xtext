package org.eclipse.xtext.parsetree.transientvalues.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parsetree.transientvalues.idea.lang.TransientValuesTestElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parsetree.transientvalues.services.TransientValuesTestGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalTransientValuesTestParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_INT", "RULE_ID", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'test'", "'required'", "'optional'", "':'", "'list'"
    };
    public static final int RULE_ID=5;
    public static final int RULE_STRING=6;
    public static final int T__15=15;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=4;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalTransientValuesTestParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalTransientValuesTestParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalTransientValuesTestParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g"; }



    	protected TransientValuesTestGrammarAccess grammarAccess;

    	protected TransientValuesTestElementTypeProvider elementTypeProvider;

    	public PsiInternalTransientValuesTestParser(PsiBuilder builder, TokenStream input, TransientValuesTestElementTypeProvider elementTypeProvider, TransientValuesTestGrammarAccess grammarAccess) {
    		this(input);
    		setPsiBuilder(builder);
        	this.grammarAccess = grammarAccess;
    		this.elementTypeProvider = elementTypeProvider;
    	}

    	@Override
    	protected String getFirstRuleName() {
    		return "Root";
    	}




    // $ANTLR start "entryRuleRoot"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:52:1: entryRuleRoot : ruleRoot EOF ;
    public final void entryRuleRoot() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:52:14: ( ruleRoot EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:53:2: ruleRoot EOF
            {
             markComposite(elementTypeProvider.getRootElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleRoot_in_entryRuleRoot54);
            ruleRoot();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleRoot57); 

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
    // $ANTLR end "entryRuleRoot"


    // $ANTLR start "ruleRoot"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:58:1: ruleRoot : (otherlv_0= 'test' ( ruleTestRequired | ruleTestOptional | ruleTestList ) ) ;
    public final void ruleRoot() throws RecognitionException {
        Token otherlv_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:58:9: ( (otherlv_0= 'test' ( ruleTestRequired | ruleTestOptional | ruleTestList ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:59:2: (otherlv_0= 'test' ( ruleTestRequired | ruleTestOptional | ruleTestList ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:59:2: (otherlv_0= 'test' ( ruleTestRequired | ruleTestOptional | ruleTestList ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:60:3: otherlv_0= 'test' ( ruleTestRequired | ruleTestOptional | ruleTestList )
            {

            			markLeaf(elementTypeProvider.getRoot_TestKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleRoot76); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:67:3: ( ruleTestRequired | ruleTestOptional | ruleTestList )
            int alt1=3;
            switch ( input.LA(1) ) {
            case 12:
                {
                alt1=1;
                }
                break;
            case 13:
                {
                alt1=2;
                }
                break;
            case 15:
                {
                alt1=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:68:4: ruleTestRequired
                    {

                    				markComposite(elementTypeProvider.getRoot_TestRequiredParserRuleCall_1_0ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleTestRequired_in_ruleRoot94);
                    ruleTestRequired();

                    state._fsp--;


                    				doneComposite();
                    			

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:76:4: ruleTestOptional
                    {

                    				markComposite(elementTypeProvider.getRoot_TestOptionalParserRuleCall_1_1ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleTestOptional_in_ruleRoot118);
                    ruleTestOptional();

                    state._fsp--;


                    				doneComposite();
                    			

                    }
                    break;
                case 3 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:84:4: ruleTestList
                    {

                    				markComposite(elementTypeProvider.getRoot_TestListParserRuleCall_1_2ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleTestList_in_ruleRoot142);
                    ruleTestList();

                    state._fsp--;


                    				doneComposite();
                    			

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
    // $ANTLR end "ruleRoot"


    // $ANTLR start "entryRuleTestRequired"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:96:1: entryRuleTestRequired : ruleTestRequired EOF ;
    public final void entryRuleTestRequired() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:96:22: ( ruleTestRequired EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:97:2: ruleTestRequired EOF
            {
             markComposite(elementTypeProvider.getTestRequiredElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTestRequired_in_entryRuleTestRequired167);
            ruleTestRequired();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTestRequired170); 

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
    // $ANTLR end "entryRuleTestRequired"


    // $ANTLR start "ruleTestRequired"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:102:1: ruleTestRequired : ( () otherlv_1= 'required' ( (lv_required1_2_0= RULE_INT ) ) ( (lv_required2_3_0= RULE_INT ) ) ) ;
    public final void ruleTestRequired() throws RecognitionException {
        Token otherlv_1=null;
        Token lv_required1_2_0=null;
        Token lv_required2_3_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:102:17: ( ( () otherlv_1= 'required' ( (lv_required1_2_0= RULE_INT ) ) ( (lv_required2_3_0= RULE_INT ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:103:2: ( () otherlv_1= 'required' ( (lv_required1_2_0= RULE_INT ) ) ( (lv_required2_3_0= RULE_INT ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:103:2: ( () otherlv_1= 'required' ( (lv_required1_2_0= RULE_INT ) ) ( (lv_required2_3_0= RULE_INT ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:104:3: () otherlv_1= 'required' ( (lv_required1_2_0= RULE_INT ) ) ( (lv_required2_3_0= RULE_INT ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:104:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:105:4: 
            {

            				precedeComposite(elementTypeProvider.getTestRequired_TestRequiredAction_0ElementType());
            				doneComposite();
            			

            }


            			markLeaf(elementTypeProvider.getTestRequired_RequiredKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleTestRequired202); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:117:3: ( (lv_required1_2_0= RULE_INT ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:118:4: (lv_required1_2_0= RULE_INT )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:118:4: (lv_required1_2_0= RULE_INT )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:119:5: lv_required1_2_0= RULE_INT
            {

            					markLeaf(elementTypeProvider.getTestRequired_Required1INTTerminalRuleCall_2_0ElementType());
            				
            lv_required1_2_0=(Token)match(input,RULE_INT,FollowSets000.FOLLOW_RULE_INT_in_ruleTestRequired229); 

            					doneLeaf(lv_required1_2_0);
            				

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:128:3: ( (lv_required2_3_0= RULE_INT ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:129:4: (lv_required2_3_0= RULE_INT )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:129:4: (lv_required2_3_0= RULE_INT )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:130:5: lv_required2_3_0= RULE_INT
            {

            					markLeaf(elementTypeProvider.getTestRequired_Required2INTTerminalRuleCall_3_0ElementType());
            				
            lv_required2_3_0=(Token)match(input,RULE_INT,FollowSets000.FOLLOW_RULE_INT_in_ruleTestRequired267); 

            					doneLeaf(lv_required2_3_0);
            				

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
    // $ANTLR end "ruleTestRequired"


    // $ANTLR start "entryRuleTestOptional"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:143:1: entryRuleTestOptional : ruleTestOptional EOF ;
    public final void entryRuleTestOptional() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:143:22: ( ruleTestOptional EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:144:2: ruleTestOptional EOF
            {
             markComposite(elementTypeProvider.getTestOptionalElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTestOptional_in_entryRuleTestOptional298);
            ruleTestOptional();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTestOptional301); 

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
    // $ANTLR end "entryRuleTestOptional"


    // $ANTLR start "ruleTestOptional"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:149:1: ruleTestOptional : ( () otherlv_1= 'optional' ( (lv_opt1_2_0= RULE_INT ) )? (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )? ) ;
    public final void ruleTestOptional() throws RecognitionException {
        Token otherlv_1=null;
        Token lv_opt1_2_0=null;
        Token otherlv_3=null;
        Token lv_opt2_4_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:149:17: ( ( () otherlv_1= 'optional' ( (lv_opt1_2_0= RULE_INT ) )? (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )? ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:150:2: ( () otherlv_1= 'optional' ( (lv_opt1_2_0= RULE_INT ) )? (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )? )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:150:2: ( () otherlv_1= 'optional' ( (lv_opt1_2_0= RULE_INT ) )? (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )? )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:151:3: () otherlv_1= 'optional' ( (lv_opt1_2_0= RULE_INT ) )? (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )?
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:151:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:152:4: 
            {

            				precedeComposite(elementTypeProvider.getTestOptional_TestOptionalAction_0ElementType());
            				doneComposite();
            			

            }


            			markLeaf(elementTypeProvider.getTestOptional_OptionalKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleTestOptional333); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:164:3: ( (lv_opt1_2_0= RULE_INT ) )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==RULE_INT) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:165:4: (lv_opt1_2_0= RULE_INT )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:165:4: (lv_opt1_2_0= RULE_INT )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:166:5: lv_opt1_2_0= RULE_INT
                    {

                    					markLeaf(elementTypeProvider.getTestOptional_Opt1INTTerminalRuleCall_2_0ElementType());
                    				
                    lv_opt1_2_0=(Token)match(input,RULE_INT,FollowSets000.FOLLOW_RULE_INT_in_ruleTestOptional360); 

                    					doneLeaf(lv_opt1_2_0);
                    				

                    }


                    }
                    break;

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:175:3: (otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==14) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:176:4: otherlv_3= ':' ( (lv_opt2_4_0= RULE_INT ) )
                    {

                    				markLeaf(elementTypeProvider.getTestOptional_ColonKeyword_3_0ElementType());
                    			
                    otherlv_3=(Token)match(input,14,FollowSets000.FOLLOW_14_in_ruleTestOptional392); 

                    				doneLeaf(otherlv_3);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:183:4: ( (lv_opt2_4_0= RULE_INT ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:184:5: (lv_opt2_4_0= RULE_INT )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:184:5: (lv_opt2_4_0= RULE_INT )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:185:6: lv_opt2_4_0= RULE_INT
                    {

                    						markLeaf(elementTypeProvider.getTestOptional_Opt2INTTerminalRuleCall_3_1_0ElementType());
                    					
                    lv_opt2_4_0=(Token)match(input,RULE_INT,FollowSets000.FOLLOW_RULE_INT_in_ruleTestOptional424); 

                    						doneLeaf(lv_opt2_4_0);
                    					

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
    // $ANTLR end "ruleTestOptional"


    // $ANTLR start "entryRuleTestList"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:199:1: entryRuleTestList : ruleTestList EOF ;
    public final void entryRuleTestList() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:199:18: ( ruleTestList EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:200:2: ruleTestList EOF
            {
             markComposite(elementTypeProvider.getTestListElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTestList_in_entryRuleTestList463);
            ruleTestList();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTestList466); 

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
    // $ANTLR end "entryRuleTestList"


    // $ANTLR start "ruleTestList"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:205:1: ruleTestList : ( () otherlv_1= 'list' ( (lv_item_2_0= RULE_INT ) )* ) ;
    public final void ruleTestList() throws RecognitionException {
        Token otherlv_1=null;
        Token lv_item_2_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:205:13: ( ( () otherlv_1= 'list' ( (lv_item_2_0= RULE_INT ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:206:2: ( () otherlv_1= 'list' ( (lv_item_2_0= RULE_INT ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:206:2: ( () otherlv_1= 'list' ( (lv_item_2_0= RULE_INT ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:207:3: () otherlv_1= 'list' ( (lv_item_2_0= RULE_INT ) )*
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:207:3: ()
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:208:4: 
            {

            				precedeComposite(elementTypeProvider.getTestList_TestListAction_0ElementType());
            				doneComposite();
            			

            }


            			markLeaf(elementTypeProvider.getTestList_ListKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleTestList498); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:220:3: ( (lv_item_2_0= RULE_INT ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==RULE_INT) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:221:4: (lv_item_2_0= RULE_INT )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:221:4: (lv_item_2_0= RULE_INT )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/parsetree/transientvalues/idea/parser/antlr/internal/PsiInternalTransientValuesTest.g:222:5: lv_item_2_0= RULE_INT
            	    {

            	    					markLeaf(elementTypeProvider.getTestList_ItemINTTerminalRuleCall_2_0ElementType());
            	    				
            	    lv_item_2_0=(Token)match(input,RULE_INT,FollowSets000.FOLLOW_RULE_INT_in_ruleTestList525); 

            	    					doneLeaf(lv_item_2_0);
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop4;
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
        return ;
    }
    // $ANTLR end "ruleTestList"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleRoot_in_entryRuleRoot54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleRoot57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleRoot76 = new BitSet(new long[]{0x000000000000B000L});
        public static final BitSet FOLLOW_ruleTestRequired_in_ruleRoot94 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTestOptional_in_ruleRoot118 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTestList_in_ruleRoot142 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTestRequired_in_entryRuleTestRequired167 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTestRequired170 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_12_in_ruleTestRequired202 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_INT_in_ruleTestRequired229 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_INT_in_ruleTestRequired267 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTestOptional_in_entryRuleTestOptional298 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTestOptional301 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleTestOptional333 = new BitSet(new long[]{0x0000000000004012L});
        public static final BitSet FOLLOW_RULE_INT_in_ruleTestOptional360 = new BitSet(new long[]{0x0000000000004002L});
        public static final BitSet FOLLOW_14_in_ruleTestOptional392 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_INT_in_ruleTestOptional424 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTestList_in_entryRuleTestList463 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTestList466 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_15_in_ruleTestList498 = new BitSet(new long[]{0x0000000000000012L});
        public static final BitSet FOLLOW_RULE_INT_in_ruleTestList525 = new BitSet(new long[]{0x0000000000000012L});
    }


}