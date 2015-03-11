package org.eclipse.xtext.resource.idea.parser.antlr.internal;

import org.eclipse.xtext.idea.parser.AbstractPsiAntlrParser;
import org.eclipse.xtext.resource.idea.lang.LocationProviderTestLanguageElementTypeProvider;
import org.eclipse.xtext.idea.parser.TokenTypeProvider;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.resource.services.LocationProviderTestLanguageGrammarAccess;

import com.intellij.lang.PsiBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class PsiInternalLocationProviderTestLanguageParser extends AbstractPsiAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'element'", "'singleref'", "'multiref'", "'modes'", "'end'", "';'", "'bus'", "'processor'", "'mode'", "':'", "'->'", "'port'", "'data'"
    };
    public static final int RULE_ID=4;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=10;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int RULE_SL_COMMENT=8;
    public static final int EOF=-1;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__19=19;
    public static final int RULE_STRING=6;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int RULE_INT=5;
    public static final int RULE_WS=9;

    // delegates
    // delegators


        public PsiInternalLocationProviderTestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PsiInternalLocationProviderTestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PsiInternalLocationProviderTestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g"; }



    	protected LocationProviderTestLanguageGrammarAccess grammarAccess;

    	protected LocationProviderTestLanguageElementTypeProvider elementTypeProvider;

    	public PsiInternalLocationProviderTestLanguageParser(PsiBuilder builder, TokenStream input, LocationProviderTestLanguageElementTypeProvider elementTypeProvider, LocationProviderTestLanguageGrammarAccess grammarAccess) {
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:52:1: entryRuleModel : ruleModel EOF ;
    public final void entryRuleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:52:15: ( ruleModel EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:53:2: ruleModel EOF
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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:58:1: ruleModel : ( ( (lv_elements_0_0= ruleElement ) )* ( (lv_components_1_0= ruleComponent ) )* ) ;
    public final void ruleModel() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:58:10: ( ( ( (lv_elements_0_0= ruleElement ) )* ( (lv_components_1_0= ruleComponent ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:59:2: ( ( (lv_elements_0_0= ruleElement ) )* ( (lv_components_1_0= ruleComponent ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:59:2: ( ( (lv_elements_0_0= ruleElement ) )* ( (lv_components_1_0= ruleComponent ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:60:3: ( (lv_elements_0_0= ruleElement ) )* ( (lv_components_1_0= ruleComponent ) )*
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:60:3: ( (lv_elements_0_0= ruleElement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:61:4: (lv_elements_0_0= ruleElement )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:61:4: (lv_elements_0_0= ruleElement )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:62:5: lv_elements_0_0= ruleElement
            	    {

            	    					markComposite(elementTypeProvider.getModel_ElementsElementParserRuleCall_0_0ElementType());
            	    				
            	    pushFollow(FollowSets000.FOLLOW_ruleElement_in_ruleModel89);
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

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:71:3: ( (lv_components_1_0= ruleComponent ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=17 && LA2_0<=18)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:72:4: (lv_components_1_0= ruleComponent )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:72:4: (lv_components_1_0= ruleComponent )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:73:5: lv_components_1_0= ruleComponent
            	    {

            	    					markComposite(elementTypeProvider.getModel_ComponentsComponentParserRuleCall_1_0ElementType());
            	    				
            	    pushFollow(FollowSets000.FOLLOW_ruleComponent_in_ruleModel128);
            	    ruleComponent();

            	    state._fsp--;


            	    					doneComposite();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
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
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleElement"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:86:1: entryRuleElement : ruleElement EOF ;
    public final void entryRuleElement() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:86:17: ( ruleElement EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:87:2: ruleElement EOF
            {
             markComposite(elementTypeProvider.getElementElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleElement_in_entryRuleElement160);
            ruleElement();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleElement163); 

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
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:92:1: ruleElement : (otherlv_0= 'element' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )? (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )* ) ;
    public final void ruleElement() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:92:12: ( (otherlv_0= 'element' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )? (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:93:2: (otherlv_0= 'element' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )? (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:93:2: (otherlv_0= 'element' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )? (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:94:3: otherlv_0= 'element' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )? (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )*
            {

            			markLeaf(elementTypeProvider.getElement_ElementKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,11,FollowSets000.FOLLOW_11_in_ruleElement182); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:101:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:102:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:102:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:103:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getElement_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleElement209); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:112:3: (otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) ) )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==12) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:113:4: otherlv_2= 'singleref' ( (otherlv_3= RULE_ID ) )
                    {

                    				markLeaf(elementTypeProvider.getElement_SinglerefKeyword_2_0ElementType());
                    			
                    otherlv_2=(Token)match(input,12,FollowSets000.FOLLOW_12_in_ruleElement240); 

                    				doneLeaf(otherlv_2);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:120:4: ( (otherlv_3= RULE_ID ) )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:121:5: (otherlv_3= RULE_ID )
                    {
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:121:5: (otherlv_3= RULE_ID )
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:122:6: otherlv_3= RULE_ID
                    {

                    						markLeaf(elementTypeProvider.getElement_SinglerefElementCrossReference_2_1_0ElementType());
                    					
                    otherlv_3=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleElement272); 

                    						doneLeaf(otherlv_3);
                    					

                    }


                    }


                    }
                    break;

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:132:3: (otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) ) )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==13) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:133:4: otherlv_4= 'multiref' ( (otherlv_5= RULE_ID ) )
            	    {

            	    				markLeaf(elementTypeProvider.getElement_MultirefKeyword_3_0ElementType());
            	    			
            	    otherlv_4=(Token)match(input,13,FollowSets000.FOLLOW_13_in_ruleElement311); 

            	    				doneLeaf(otherlv_4);
            	    			
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:140:4: ( (otherlv_5= RULE_ID ) )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:141:5: (otherlv_5= RULE_ID )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:141:5: (otherlv_5= RULE_ID )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:142:6: otherlv_5= RULE_ID
            	    {

            	    						markLeaf(elementTypeProvider.getElement_MultirefsElementCrossReference_3_1_0ElementType());
            	    					
            	    otherlv_5=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleElement343); 

            	    						doneLeaf(otherlv_5);
            	    					

            	    }


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
    // $ANTLR end "ruleElement"


    // $ANTLR start "entryRuleComponent"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:156:1: entryRuleComponent : ruleComponent EOF ;
    public final void entryRuleComponent() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:156:19: ( ruleComponent EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:157:2: ruleComponent EOF
            {
             markComposite(elementTypeProvider.getComponentElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleComponent_in_entryRuleComponent382);
            ruleComponent();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleComponent385); 

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
    // $ANTLR end "entryRuleComponent"


    // $ANTLR start "ruleComponent"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:162:1: ruleComponent : ( ( ruleBus | ruleProcessor ) (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )? otherlv_5= 'end' otherlv_6= ';' ) ;
    public final void ruleComponent() throws RecognitionException {
        Token otherlv_2=null;
        Token otherlv_5=null;
        Token otherlv_6=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:162:14: ( ( ( ruleBus | ruleProcessor ) (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )? otherlv_5= 'end' otherlv_6= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:163:2: ( ( ruleBus | ruleProcessor ) (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )? otherlv_5= 'end' otherlv_6= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:163:2: ( ( ruleBus | ruleProcessor ) (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )? otherlv_5= 'end' otherlv_6= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:164:3: ( ruleBus | ruleProcessor ) (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )? otherlv_5= 'end' otherlv_6= ';'
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:164:3: ( ruleBus | ruleProcessor )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==17) ) {
                alt5=1;
            }
            else if ( (LA5_0==18) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:165:4: ruleBus
                    {

                    				markComposite(elementTypeProvider.getComponent_BusParserRuleCall_0_0ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleBus_in_ruleComponent408);
                    ruleBus();

                    state._fsp--;


                    				doneComposite();
                    			

                    }
                    break;
                case 2 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:173:4: ruleProcessor
                    {

                    				markComposite(elementTypeProvider.getComponent_ProcessorParserRuleCall_0_1ElementType());
                    			
                    pushFollow(FollowSets000.FOLLOW_ruleProcessor_in_ruleComponent432);
                    ruleProcessor();

                    state._fsp--;


                    				doneComposite();
                    			

                    }
                    break;

            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:181:3: (otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+ )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==14) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:182:4: otherlv_2= 'modes' ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+
                    {

                    				markLeaf(elementTypeProvider.getComponent_ModesKeyword_1_0ElementType());
                    			
                    otherlv_2=(Token)match(input,14,FollowSets000.FOLLOW_14_in_ruleComponent457); 

                    				doneLeaf(otherlv_2);
                    			
                    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:189:4: ( ( (lv_mode_3_0= ruleMode ) ) | ( (lv_transition_4_0= ruleTransition ) ) )+
                    int cnt6=0;
                    loop6:
                    do {
                        int alt6=3;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==19) ) {
                            alt6=1;
                        }
                        else if ( (LA6_0==RULE_ID) ) {
                            alt6=2;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:190:5: ( (lv_mode_3_0= ruleMode ) )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:190:5: ( (lv_mode_3_0= ruleMode ) )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:191:6: (lv_mode_3_0= ruleMode )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:191:6: (lv_mode_3_0= ruleMode )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:192:7: lv_mode_3_0= ruleMode
                    	    {

                    	    							markComposite(elementTypeProvider.getComponent_ModeModeParserRuleCall_1_1_0_0ElementType());
                    	    						
                    	    pushFollow(FollowSets000.FOLLOW_ruleMode_in_ruleComponent498);
                    	    ruleMode();

                    	    state._fsp--;


                    	    							doneComposite();
                    	    						

                    	    }


                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:202:5: ( (lv_transition_4_0= ruleTransition ) )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:202:5: ( (lv_transition_4_0= ruleTransition ) )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:203:6: (lv_transition_4_0= ruleTransition )
                    	    {
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:203:6: (lv_transition_4_0= ruleTransition )
                    	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:204:7: lv_transition_4_0= ruleTransition
                    	    {

                    	    							markComposite(elementTypeProvider.getComponent_TransitionTransitionParserRuleCall_1_1_1_0ElementType());
                    	    						
                    	    pushFollow(FollowSets000.FOLLOW_ruleTransition_in_ruleComponent560);
                    	    ruleTransition();

                    	    state._fsp--;


                    	    							doneComposite();
                    	    						

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt6 >= 1 ) break loop6;
                                EarlyExitException eee =
                                    new EarlyExitException(6, input);
                                throw eee;
                        }
                        cnt6++;
                    } while (true);


                    }
                    break;

            }


            			markLeaf(elementTypeProvider.getComponent_EndKeyword_2ElementType());
            		
            otherlv_5=(Token)match(input,15,FollowSets000.FOLLOW_15_in_ruleComponent602); 

            			doneLeaf(otherlv_5);
            		

            			markLeaf(elementTypeProvider.getComponent_SemicolonKeyword_3ElementType());
            		
            otherlv_6=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleComponent616); 

            			doneLeaf(otherlv_6);
            		

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
    // $ANTLR end "ruleComponent"


    // $ANTLR start "entryRuleBus"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:233:1: entryRuleBus : ruleBus EOF ;
    public final void entryRuleBus() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:233:13: ( ruleBus EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:234:2: ruleBus EOF
            {
             markComposite(elementTypeProvider.getBusElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleBus_in_entryRuleBus636);
            ruleBus();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleBus639); 

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
    // $ANTLR end "entryRuleBus"


    // $ANTLR start "ruleBus"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:239:1: ruleBus : (otherlv_0= 'bus' ( (lv_name_1_0= RULE_ID ) ) ( (lv_port_2_0= rulePort ) )* ) ;
    public final void ruleBus() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:239:8: ( (otherlv_0= 'bus' ( (lv_name_1_0= RULE_ID ) ) ( (lv_port_2_0= rulePort ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:240:2: (otherlv_0= 'bus' ( (lv_name_1_0= RULE_ID ) ) ( (lv_port_2_0= rulePort ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:240:2: (otherlv_0= 'bus' ( (lv_name_1_0= RULE_ID ) ) ( (lv_port_2_0= rulePort ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:241:3: otherlv_0= 'bus' ( (lv_name_1_0= RULE_ID ) ) ( (lv_port_2_0= rulePort ) )*
            {

            			markLeaf(elementTypeProvider.getBus_BusKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,17,FollowSets000.FOLLOW_17_in_ruleBus658); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:248:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:249:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:249:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:250:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getBus_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleBus685); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:259:3: ( (lv_port_2_0= rulePort ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==22) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:260:4: (lv_port_2_0= rulePort )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:260:4: (lv_port_2_0= rulePort )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:261:5: lv_port_2_0= rulePort
            	    {

            	    					markComposite(elementTypeProvider.getBus_PortPortParserRuleCall_2_0ElementType());
            	    				
            	    pushFollow(FollowSets000.FOLLOW_rulePort_in_ruleBus723);
            	    rulePort();

            	    state._fsp--;


            	    					doneComposite();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop8;
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
    // $ANTLR end "ruleBus"


    // $ANTLR start "entryRuleProcessor"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:274:1: entryRuleProcessor : ruleProcessor EOF ;
    public final void entryRuleProcessor() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:274:19: ( ruleProcessor EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:275:2: ruleProcessor EOF
            {
             markComposite(elementTypeProvider.getProcessorElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleProcessor_in_entryRuleProcessor755);
            ruleProcessor();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleProcessor758); 

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
    // $ANTLR end "entryRuleProcessor"


    // $ANTLR start "ruleProcessor"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:280:1: ruleProcessor : (otherlv_0= 'processor' ( (lv_name_1_0= RULE_ID ) ) ( (lv_data_2_0= ruleData ) )* ) ;
    public final void ruleProcessor() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:280:14: ( (otherlv_0= 'processor' ( (lv_name_1_0= RULE_ID ) ) ( (lv_data_2_0= ruleData ) )* ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:281:2: (otherlv_0= 'processor' ( (lv_name_1_0= RULE_ID ) ) ( (lv_data_2_0= ruleData ) )* )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:281:2: (otherlv_0= 'processor' ( (lv_name_1_0= RULE_ID ) ) ( (lv_data_2_0= ruleData ) )* )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:282:3: otherlv_0= 'processor' ( (lv_name_1_0= RULE_ID ) ) ( (lv_data_2_0= ruleData ) )*
            {

            			markLeaf(elementTypeProvider.getProcessor_ProcessorKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,18,FollowSets000.FOLLOW_18_in_ruleProcessor777); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:289:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:290:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:290:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:291:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getProcessor_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleProcessor804); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }

            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:300:3: ( (lv_data_2_0= ruleData ) )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==23) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:301:4: (lv_data_2_0= ruleData )
            	    {
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:301:4: (lv_data_2_0= ruleData )
            	    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:302:5: lv_data_2_0= ruleData
            	    {

            	    					markComposite(elementTypeProvider.getProcessor_DataDataParserRuleCall_2_0ElementType());
            	    				
            	    pushFollow(FollowSets000.FOLLOW_ruleData_in_ruleProcessor842);
            	    ruleData();

            	    state._fsp--;


            	    					doneComposite();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop9;
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
    // $ANTLR end "ruleProcessor"


    // $ANTLR start "entryRuleMode"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:315:1: entryRuleMode : ruleMode EOF ;
    public final void entryRuleMode() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:315:14: ( ruleMode EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:316:2: ruleMode EOF
            {
             markComposite(elementTypeProvider.getModeElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleMode_in_entryRuleMode874);
            ruleMode();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleMode877); 

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
    // $ANTLR end "entryRuleMode"


    // $ANTLR start "ruleMode"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:321:1: ruleMode : (otherlv_0= 'mode' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) ;
    public final void ruleMode() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:321:9: ( (otherlv_0= 'mode' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:322:2: (otherlv_0= 'mode' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:322:2: (otherlv_0= 'mode' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:323:3: otherlv_0= 'mode' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';'
            {

            			markLeaf(elementTypeProvider.getMode_ModeKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,19,FollowSets000.FOLLOW_19_in_ruleMode896); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:330:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:331:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:331:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:332:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getMode_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleMode923); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }


            			markLeaf(elementTypeProvider.getMode_SemicolonKeyword_2ElementType());
            		
            otherlv_2=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleMode948); 

            			doneLeaf(otherlv_2);
            		

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
    // $ANTLR end "ruleMode"


    // $ANTLR start "entryRuleTransition"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:352:1: entryRuleTransition : ruleTransition EOF ;
    public final void entryRuleTransition() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:352:20: ( ruleTransition EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:353:2: ruleTransition EOF
            {
             markComposite(elementTypeProvider.getTransitionElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleTransition_in_entryRuleTransition968);
            ruleTransition();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleTransition971); 

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
    // $ANTLR end "entryRuleTransition"


    // $ANTLR start "ruleTransition"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:358:1: ruleTransition : ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (otherlv_2= RULE_ID ) ) otherlv_3= '->' ( (otherlv_4= RULE_ID ) ) otherlv_5= ';' ) ;
    public final void ruleTransition() throws RecognitionException {
        Token lv_name_0_0=null;
        Token otherlv_1=null;
        Token otherlv_2=null;
        Token otherlv_3=null;
        Token otherlv_4=null;
        Token otherlv_5=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:358:15: ( ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (otherlv_2= RULE_ID ) ) otherlv_3= '->' ( (otherlv_4= RULE_ID ) ) otherlv_5= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:359:2: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (otherlv_2= RULE_ID ) ) otherlv_3= '->' ( (otherlv_4= RULE_ID ) ) otherlv_5= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:359:2: ( ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (otherlv_2= RULE_ID ) ) otherlv_3= '->' ( (otherlv_4= RULE_ID ) ) otherlv_5= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:360:3: ( (lv_name_0_0= RULE_ID ) ) otherlv_1= ':' ( (otherlv_2= RULE_ID ) ) otherlv_3= '->' ( (otherlv_4= RULE_ID ) ) otherlv_5= ';'
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:360:3: ( (lv_name_0_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:361:4: (lv_name_0_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:361:4: (lv_name_0_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:362:5: lv_name_0_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getTransition_NameIDTerminalRuleCall_0_0ElementType());
            				
            lv_name_0_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleTransition1003); 

            					doneLeaf(lv_name_0_0);
            				

            }


            }


            			markLeaf(elementTypeProvider.getTransition_ColonKeyword_1ElementType());
            		
            otherlv_1=(Token)match(input,20,FollowSets000.FOLLOW_20_in_ruleTransition1028); 

            			doneLeaf(otherlv_1);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:378:3: ( (otherlv_2= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:379:4: (otherlv_2= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:379:4: (otherlv_2= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:380:5: otherlv_2= RULE_ID
            {

            					markLeaf(elementTypeProvider.getTransition_SourceModeCrossReference_2_0ElementType());
            				
            otherlv_2=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleTransition1055); 

            					doneLeaf(otherlv_2);
            				

            }


            }


            			markLeaf(elementTypeProvider.getTransition_HyphenMinusGreaterThanSignKeyword_3ElementType());
            		
            otherlv_3=(Token)match(input,21,FollowSets000.FOLLOW_21_in_ruleTransition1080); 

            			doneLeaf(otherlv_3);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:396:3: ( (otherlv_4= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:397:4: (otherlv_4= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:397:4: (otherlv_4= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:398:5: otherlv_4= RULE_ID
            {

            					markLeaf(elementTypeProvider.getTransition_DestinationModeCrossReference_4_0ElementType());
            				
            otherlv_4=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleTransition1107); 

            					doneLeaf(otherlv_4);
            				

            }


            }


            			markLeaf(elementTypeProvider.getTransition_SemicolonKeyword_5ElementType());
            		
            otherlv_5=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleTransition1132); 

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
    // $ANTLR end "ruleTransition"


    // $ANTLR start "entryRulePort"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:418:1: entryRulePort : rulePort EOF ;
    public final void entryRulePort() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:418:14: ( rulePort EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:419:2: rulePort EOF
            {
             markComposite(elementTypeProvider.getPortElementType()); 
            pushFollow(FollowSets000.FOLLOW_rulePort_in_entryRulePort1152);
            rulePort();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRulePort1155); 

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
    // $ANTLR end "entryRulePort"


    // $ANTLR start "rulePort"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:424:1: rulePort : (otherlv_0= 'port' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) ;
    public final void rulePort() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:424:9: ( (otherlv_0= 'port' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:425:2: (otherlv_0= 'port' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:425:2: (otherlv_0= 'port' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:426:3: otherlv_0= 'port' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';'
            {

            			markLeaf(elementTypeProvider.getPort_PortKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,22,FollowSets000.FOLLOW_22_in_rulePort1174); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:433:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:434:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:434:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:435:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getPort_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_rulePort1201); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }


            			markLeaf(elementTypeProvider.getPort_SemicolonKeyword_2ElementType());
            		
            otherlv_2=(Token)match(input,16,FollowSets000.FOLLOW_16_in_rulePort1226); 

            			doneLeaf(otherlv_2);
            		

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
    // $ANTLR end "rulePort"


    // $ANTLR start "entryRuleData"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:455:1: entryRuleData : ruleData EOF ;
    public final void entryRuleData() throws RecognitionException {
        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:455:14: ( ruleData EOF )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:456:2: ruleData EOF
            {
             markComposite(elementTypeProvider.getDataElementType()); 
            pushFollow(FollowSets000.FOLLOW_ruleData_in_entryRuleData1246);
            ruleData();

            state._fsp--;

            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleData1249); 

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
    // $ANTLR end "entryRuleData"


    // $ANTLR start "ruleData"
    // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:461:1: ruleData : (otherlv_0= 'data' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) ;
    public final void ruleData() throws RecognitionException {
        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;

        try {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:461:9: ( (otherlv_0= 'data' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:462:2: (otherlv_0= 'data' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:462:2: (otherlv_0= 'data' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';' )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:463:3: otherlv_0= 'data' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= ';'
            {

            			markLeaf(elementTypeProvider.getData_DataKeyword_0ElementType());
            		
            otherlv_0=(Token)match(input,23,FollowSets000.FOLLOW_23_in_ruleData1268); 

            			doneLeaf(otherlv_0);
            		
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:470:3: ( (lv_name_1_0= RULE_ID ) )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:471:4: (lv_name_1_0= RULE_ID )
            {
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:471:4: (lv_name_1_0= RULE_ID )
            // ../../intellij/org.eclipse.xtext.core.idea.tests/src-gen/org/eclipse/xtext/resource/idea/parser/antlr/internal/PsiInternalLocationProviderTestLanguage.g:472:5: lv_name_1_0= RULE_ID
            {

            					markLeaf(elementTypeProvider.getData_NameIDTerminalRuleCall_1_0ElementType());
            				
            lv_name_1_0=(Token)match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_ruleData1295); 

            					doneLeaf(lv_name_1_0);
            				

            }


            }


            			markLeaf(elementTypeProvider.getData_SemicolonKeyword_2ElementType());
            		
            otherlv_2=(Token)match(input,16,FollowSets000.FOLLOW_16_in_ruleData1320); 

            			doneLeaf(otherlv_2);
            		

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
    // $ANTLR end "ruleData"

    // Delegated rules


 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleModel_in_entryRuleModel54 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleModel57 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleElement_in_ruleModel89 = new BitSet(new long[]{0x0000000000060802L});
        public static final BitSet FOLLOW_ruleComponent_in_ruleModel128 = new BitSet(new long[]{0x0000000000060002L});
        public static final BitSet FOLLOW_ruleElement_in_entryRuleElement160 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleElement163 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_ruleElement182 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleElement209 = new BitSet(new long[]{0x0000000000003002L});
        public static final BitSet FOLLOW_12_in_ruleElement240 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleElement272 = new BitSet(new long[]{0x0000000000002002L});
        public static final BitSet FOLLOW_13_in_ruleElement311 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleElement343 = new BitSet(new long[]{0x0000000000002002L});
        public static final BitSet FOLLOW_ruleComponent_in_entryRuleComponent382 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleComponent385 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleBus_in_ruleComponent408 = new BitSet(new long[]{0x000000000000C000L});
        public static final BitSet FOLLOW_ruleProcessor_in_ruleComponent432 = new BitSet(new long[]{0x000000000000C000L});
        public static final BitSet FOLLOW_14_in_ruleComponent457 = new BitSet(new long[]{0x0000000000080010L});
        public static final BitSet FOLLOW_ruleMode_in_ruleComponent498 = new BitSet(new long[]{0x0000000000088010L});
        public static final BitSet FOLLOW_ruleTransition_in_ruleComponent560 = new BitSet(new long[]{0x0000000000088010L});
        public static final BitSet FOLLOW_15_in_ruleComponent602 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_ruleComponent616 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleBus_in_entryRuleBus636 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleBus639 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_17_in_ruleBus658 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleBus685 = new BitSet(new long[]{0x0000000000400002L});
        public static final BitSet FOLLOW_rulePort_in_ruleBus723 = new BitSet(new long[]{0x0000000000400002L});
        public static final BitSet FOLLOW_ruleProcessor_in_entryRuleProcessor755 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleProcessor758 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_18_in_ruleProcessor777 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleProcessor804 = new BitSet(new long[]{0x0000000000800002L});
        public static final BitSet FOLLOW_ruleData_in_ruleProcessor842 = new BitSet(new long[]{0x0000000000800002L});
        public static final BitSet FOLLOW_ruleMode_in_entryRuleMode874 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleMode877 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_19_in_ruleMode896 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleMode923 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_ruleMode948 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleTransition_in_entryRuleTransition968 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleTransition971 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleTransition1003 = new BitSet(new long[]{0x0000000000100000L});
        public static final BitSet FOLLOW_20_in_ruleTransition1028 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleTransition1055 = new BitSet(new long[]{0x0000000000200000L});
        public static final BitSet FOLLOW_21_in_ruleTransition1080 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleTransition1107 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_ruleTransition1132 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rulePort_in_entryRulePort1152 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRulePort1155 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_22_in_rulePort1174 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_rulePort1201 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_rulePort1226 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleData_in_entryRuleData1246 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleData1249 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_23_in_ruleData1268 = new BitSet(new long[]{0x0000000000000010L});
        public static final BitSet FOLLOW_RULE_ID_in_ruleData1295 = new BitSet(new long[]{0x0000000000010000L});
        public static final BitSet FOLLOW_16_in_ruleData1320 = new BitSet(new long[]{0x0000000000000002L});
    }


}