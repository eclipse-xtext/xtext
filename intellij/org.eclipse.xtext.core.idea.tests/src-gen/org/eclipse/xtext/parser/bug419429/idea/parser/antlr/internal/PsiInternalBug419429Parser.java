package org.eclipse.xtext.parser.bug419429.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.parser.bug419429.idea.lang.Bug419429ElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.bug419429.services.Bug419429GrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalBug419429Parser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int RULE_ANY_OTHER=10;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;

    // delegates
    // delegators


        public PsiInternalBug419429Parser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalBug419429Parser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalBug419429Parser.tokenNames; }
    public String getGrammarFileName() { return "PsiInternalBug419429.g"; }



    	protected Bug419429GrammarAccess grammarAccess;

    	protected Bug419429ElementTypeProvider elementTypeProvider;

    	public PsiInternalBug419429Parser(PsiBuilder builder, TokenStream input, Bug419429ElementTypeProvider elementTypeProvider, Bug419429GrammarAccess grammarAccess) {
    		this(input);
    		setPsiBuilder(builder);
        	this.grammarAccess = grammarAccess;
    		this.elementTypeProvider = elementTypeProvider;
    	}

    	@Override
    	protected String getFirstRuleName() {
    		return "EReference";
    	}




    // $ANTLR start "entryRuleEReference"
    // PsiInternalBug419429.g:52:1: entryRuleEReference returns [Boolean current=false] : iv_ruleEReference= ruleEReference EOF ;
    public final Boolean entryRuleEReference() throws RecognitionException {
        Boolean current = false;

        Boolean iv_ruleEReference = null;


        try {
            // PsiInternalBug419429.g:52:52: (iv_ruleEReference= ruleEReference EOF )
            // PsiInternalBug419429.g:53:2: iv_ruleEReference= ruleEReference EOF
            {
             markComposite(elementTypeProvider.getEReferenceElementType()); 
            pushFollow(FollowSets000.FOLLOW_1);
            iv_ruleEReference=ruleEReference();

            state._fsp--;

             current =iv_ruleEReference; 
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
    // $ANTLR end "entryRuleEReference"


    // $ANTLR start "ruleEReference"
    // PsiInternalBug419429.g:59:1: ruleEReference returns [Boolean current=false] : ( (otherlv_0= RULE_ID ) ) ;
    public final Boolean ruleEReference() throws RecognitionException {
        Boolean current = false;

        Token otherlv_0=null;

        try {
            // PsiInternalBug419429.g:60:1: ( ( (otherlv_0= RULE_ID ) ) )
            // PsiInternalBug419429.g:61:2: ( (otherlv_0= RULE_ID ) )
            {
            // PsiInternalBug419429.g:61:2: ( (otherlv_0= RULE_ID ) )
            // PsiInternalBug419429.g:62:3: (otherlv_0= RULE_ID )
            {
            // PsiInternalBug419429.g:62:3: (otherlv_0= RULE_ID )
            // PsiInternalBug419429.g:63:4: otherlv_0= RULE_ID
            {

            				if (!current) {
            					associateWithSemanticElement();
            					current = true;
            				}
            			

            				markLeaf(elementTypeProvider.getEReference_ETypeEClassifierCrossReference_0ElementType());
            			
            otherlv_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_2); 

            				doneLeaf(otherlv_0);
            			

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
    // $ANTLR end "ruleEReference"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    }


}