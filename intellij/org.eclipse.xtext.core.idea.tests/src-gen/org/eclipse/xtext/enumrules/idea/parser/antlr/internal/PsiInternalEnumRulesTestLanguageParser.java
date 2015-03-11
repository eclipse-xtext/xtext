package org.eclipse.xtext.enumrules.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.enumrules.idea.lang.EnumRulesTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.enumrules.services.EnumRulesTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalEnumRulesTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'existing'", "'generated'", "'SameName'", "'overridden'", "'DifferentLiteral'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=6;
    public static final int T__15=15;
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


        public PsiInternalEnumRulesTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalEnumRulesTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalEnumRulesTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g"; }



    	protected EnumRulesTestLanguageGrammarAccess grammarAccess;

    	protected EnumRulesTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalEnumRulesTestLanguageParser(PsiBuilder builder, TokenStream input, EnumRulesTestLanguageElementTypeProvider elementTypeProvider, EnumRulesTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:58:1: ruleModel : ( (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? ) | (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) ) ) ;
    public final void ruleModel() throws RecognitionException {
        Token otherlv_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:58:10: ( ( (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? ) | (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:59:2: ( (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? ) | (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:59:2: ( (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? ) | (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) ) )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==11) ) {
                alt2=1;
            }
            else if ( (LA2_0==12) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:60:3: (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:60:3: (otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )? )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:61:4: otherlv_0= 'existing' ( (lv_existing_1_0= ruleExistingEnum ) ) (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )?
                    {

                    				markLeaf(elementTypeProvider.getModel_ExistingKeyword_0_0ElementType());
                    			
                    otherlv_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleModel82); 

                    				doneLeaf(otherlv_0);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:68:4: ( (lv_existing_1_0= ruleExistingEnum ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:69:5: (lv_existing_1_0= ruleExistingEnum )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:69:5: (lv_existing_1_0= ruleExistingEnum )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:70:6: lv_existing_1_0= ruleExistingEnum
                    {

                    						markComposite(elementTypeProvider.getModel_ExistingExistingEnumEnumRuleCall_0_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleExistingEnum_in_ruleModel114);
                    ruleExistingEnum();

                    state._fsp--;


                    						doneComposite();
                    					

                    }


                    }

                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:79:4: (otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) ) )?
                    int alt1=2;
                    int LA1_0 = input.LA(1);

                    if ( (LA1_0==12) ) {
                        alt1=1;
                    }
                    switch (alt1) {
                        case 1 :
                            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:80:5: otherlv_2= 'generated' ( (lv_generated_3_0= ruleGeneratedEnum ) )
                            {

                            					markLeaf(elementTypeProvider.getModel_GeneratedKeyword_0_2_0ElementType());
                            				
                            otherlv_2=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleModel151); 

                            					doneLeaf(otherlv_2);
                            				
                            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:87:5: ( (lv_generated_3_0= ruleGeneratedEnum ) )
                            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:88:6: (lv_generated_3_0= ruleGeneratedEnum )
                            {
                            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:88:6: (lv_generated_3_0= ruleGeneratedEnum )
                            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:89:7: lv_generated_3_0= ruleGeneratedEnum
                            {

                            							markComposite(elementTypeProvider.getModel_GeneratedGeneratedEnumEnumRuleCall_0_2_1_0ElementType());
                            						
                            pushFollow(FollowSets000.FOLLOW_ruleGeneratedEnum_in_ruleModel188);
                            ruleGeneratedEnum();

                            state._fsp--;


                            							doneComposite();
                            						

                            }


                            }


                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:101:3: (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:101:3: (otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:102:4: otherlv_4= 'generated' ( (lv_generated_5_0= ruleGeneratedEnum ) )
                    {

                    				markLeaf(elementTypeProvider.getModel_GeneratedKeyword_1_0ElementType());
                    			
                    otherlv_4=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleModel243); 

                    				doneLeaf(otherlv_4);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:109:4: ( (lv_generated_5_0= ruleGeneratedEnum ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:110:5: (lv_generated_5_0= ruleGeneratedEnum )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:110:5: (lv_generated_5_0= ruleGeneratedEnum )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:111:6: lv_generated_5_0= ruleGeneratedEnum
                    {

                    						markComposite(elementTypeProvider.getModel_GeneratedGeneratedEnumEnumRuleCall_1_1_0ElementType());
                    					
                    pushFollow(FollowSets000.FOLLOW_ruleGeneratedEnum_in_ruleModel275);
                    ruleGeneratedEnum();

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


    // $ANTLR start "ruleExistingEnum"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:125:1: ruleExistingEnum : ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'overridden' ) | (enumLiteral_2= 'DifferentLiteral' ) ) ;
    public final void ruleExistingEnum() throws RecognitionException {
        Token enumLiteral_0=null;
        Token enumLiteral_1=null;
        Token enumLiteral_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:125:17: ( ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'overridden' ) | (enumLiteral_2= 'DifferentLiteral' ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:126:2: ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'overridden' ) | (enumLiteral_2= 'DifferentLiteral' ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:126:2: ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'overridden' ) | (enumLiteral_2= 'DifferentLiteral' ) )
            int alt3=3;
            switch ( input.LA(1) ) {
            case 13:
                {
                alt3=1;
                }
                break;
            case 14:
                {
                alt3=2;
                }
                break;
            case 15:
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
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:127:3: (enumLiteral_0= 'SameName' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:127:3: (enumLiteral_0= 'SameName' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:128:4: enumLiteral_0= 'SameName'
                    {

                    				markLeaf(elementTypeProvider.getExistingEnum_SameNameEnumLiteralDeclaration_0ElementType());
                    			
                    enumLiteral_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleExistingEnum326); 

                    				doneLeaf(enumLiteral_0);
                    			

                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:137:3: (enumLiteral_1= 'overridden' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:137:3: (enumLiteral_1= 'overridden' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:138:4: enumLiteral_1= 'overridden'
                    {

                    				markLeaf(elementTypeProvider.getExistingEnum_OverriddenLiteralEnumLiteralDeclaration_1ElementType());
                    			
                    enumLiteral_1=(Token)match(input,14,FollowSets000.FOLLOW_14_in_ruleExistingEnum359); 

                    				doneLeaf(enumLiteral_1);
                    			

                    }


                    }
                    break;
                case 3 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:147:3: (enumLiteral_2= 'DifferentLiteral' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:147:3: (enumLiteral_2= 'DifferentLiteral' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:148:4: enumLiteral_2= 'DifferentLiteral'
                    {

                    				markLeaf(elementTypeProvider.getExistingEnum_DifferentNameEnumLiteralDeclaration_2ElementType());
                    			
                    enumLiteral_2=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleExistingEnum392); 

                    				doneLeaf(enumLiteral_2);
                    			

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
    // $ANTLR end "ruleExistingEnum"


    // $ANTLR start "ruleGeneratedEnum"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:160:1: ruleGeneratedEnum : ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'DifferentLiteral' ) ) ;
    public final void ruleGeneratedEnum() throws RecognitionException {
        Token enumLiteral_0=null;
        Token enumLiteral_1=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:160:18: ( ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'DifferentLiteral' ) ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:161:2: ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'DifferentLiteral' ) )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:161:2: ( (enumLiteral_0= 'SameName' ) | (enumLiteral_1= 'DifferentLiteral' ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==13) ) {
                alt4=1;
            }
            else if ( (LA4_0==15) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:162:3: (enumLiteral_0= 'SameName' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:162:3: (enumLiteral_0= 'SameName' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:163:4: enumLiteral_0= 'SameName'
                    {

                    				markLeaf(elementTypeProvider.getGeneratedEnum_SameNameEnumLiteralDeclaration_0ElementType());
                    			
                    enumLiteral_0=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleGeneratedEnum430); 

                    				doneLeaf(enumLiteral_0);
                    			

                    }


                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:172:3: (enumLiteral_1= 'DifferentLiteral' )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:172:3: (enumLiteral_1= 'DifferentLiteral' )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/enumrules/idea/parser/antlr/internal/PsiInternalEnumRulesTestLanguage.g:173:4: enumLiteral_1= 'DifferentLiteral'
                    {

                    				markLeaf(elementTypeProvider.getGeneratedEnum_DifferentNameEnumLiteralDeclaration_1ElementType());
                    			
                    enumLiteral_1=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleGeneratedEnum463); 

                    				doneLeaf(enumLiteral_1);
                    			

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
    // $ANTLR end "ruleGeneratedEnum"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleModel82 = new BitSet(new long[]{0x000000000000E000L});
        public static final BitSet FOLLOW_ruleExistingEnum_in_ruleModel114 = new BitSet(new long[]{0x0000000000001002L});
        public static final BitSet FOLLOW_12_in_ruleModel151 = new BitSet(new long[]{0x000000000000A000L});
        public static final BitSet FOLLOW_ruleGeneratedEnum_in_ruleModel188 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_12_in_ruleModel243 = new BitSet(new long[]{0x000000000000A000L});
        public static final BitSet FOLLOW_ruleGeneratedEnum_in_ruleModel275 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleExistingEnum326 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_14_in_ruleExistingEnum359 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_15_in_ruleExistingEnum392 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_ruleGeneratedEnum430 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_15_in_ruleGeneratedEnum463 = new BitSet(new long[]{0x0000000000000002L});
    }


}