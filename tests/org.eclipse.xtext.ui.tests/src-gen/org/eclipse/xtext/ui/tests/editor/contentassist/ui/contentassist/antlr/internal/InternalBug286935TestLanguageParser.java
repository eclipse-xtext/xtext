package org.eclipse.xtext.ui.tests.editor.contentassist.ui.contentassist.antlr.internal; 

import java.io.InputStream;
import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.AbstractInternalContentAssistParser;
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.DFA;
import org.eclipse.xtext.ui.tests.editor.contentassist.services.Bug286935TestLanguageGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalBug286935TestLanguageParser extends AbstractInternalContentAssistParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_STRING", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'NORMAL'", "'cond'", "'reference'", "'textual'", "'state'", "'init'", "'final'"
    };
    public static final int RULE_ID=4;
    public static final int RULE_STRING=5;
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


        public InternalBug286935TestLanguageParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalBug286935TestLanguageParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalBug286935TestLanguageParser.tokenNames; }
    public String getGrammarFileName() { return "../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g"; }


     
     	private Bug286935TestLanguageGrammarAccess grammarAccess;
     	
        public void setGrammarAccess(Bug286935TestLanguageGrammarAccess grammarAccess) {
        	this.grammarAccess = grammarAccess;
        }
        
        @Override
        protected Grammar getGrammar() {
        	return grammarAccess.getGrammar();
        }
        
        @Override
        protected String getValueForTokenName(String tokenName) {
        	return tokenName;
        }




    // $ANTLR start "entryRuleState"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:60:1: entryRuleState : ruleState EOF ;
    public final void entryRuleState() throws RecognitionException {
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:61:1: ( ruleState EOF )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:62:1: ruleState EOF
            {
             before(grammarAccess.getStateRule()); 
            pushFollow(FollowSets000.FOLLOW_ruleState_in_entryRuleState61);
            ruleState();

            state._fsp--;

             after(grammarAccess.getStateRule()); 
            match(input,EOF,FollowSets000.FOLLOW_EOF_in_entryRuleState68); 

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
    // $ANTLR end "entryRuleState"


    // $ANTLR start "ruleState"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:69:1: ruleState : ( ( rule__State__Group__0 ) ) ;
    public final void ruleState() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:73:2: ( ( ( rule__State__Group__0 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:74:1: ( ( rule__State__Group__0 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:74:1: ( ( rule__State__Group__0 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:75:1: ( rule__State__Group__0 )
            {
             before(grammarAccess.getStateAccess().getGroup()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:76:1: ( rule__State__Group__0 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:76:2: rule__State__Group__0
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__0_in_ruleState94);
            rule__State__Group__0();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getGroup()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleState"


    // $ANTLR start "ruleStateType"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:89:1: ruleStateType : ( ( rule__StateType__Alternatives ) ) ;
    public final void ruleStateType() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:93:1: ( ( ( rule__StateType__Alternatives ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:94:1: ( ( rule__StateType__Alternatives ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:94:1: ( ( rule__StateType__Alternatives ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:95:1: ( rule__StateType__Alternatives )
            {
             before(grammarAccess.getStateTypeAccess().getAlternatives()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:96:1: ( rule__StateType__Alternatives )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:96:2: rule__StateType__Alternatives
            {
            pushFollow(FollowSets000.FOLLOW_rule__StateType__Alternatives_in_ruleStateType131);
            rule__StateType__Alternatives();

            state._fsp--;


            }

             after(grammarAccess.getStateTypeAccess().getAlternatives()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "ruleStateType"


    // $ANTLR start "rule__State__Alternatives_1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:107:1: rule__State__Alternatives_1 : ( ( ( rule__State__IsInitialAssignment_1_0 ) ) | ( ( rule__State__IsFinalAssignment_1_1 ) ) | ( ( rule__State__StateKindAssignment_1_2 ) ) | ( ( rule__State__Group_1_3__0 ) ) | ( ( rule__State__Group_1_4__0 ) ) );
    public final void rule__State__Alternatives_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:111:1: ( ( ( rule__State__IsInitialAssignment_1_0 ) ) | ( ( rule__State__IsFinalAssignment_1_1 ) ) | ( ( rule__State__StateKindAssignment_1_2 ) ) | ( ( rule__State__Group_1_3__0 ) ) | ( ( rule__State__Group_1_4__0 ) ) )
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:112:1: ( ( rule__State__IsInitialAssignment_1_0 ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:112:1: ( ( rule__State__IsInitialAssignment_1_0 ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:113:1: ( rule__State__IsInitialAssignment_1_0 )
                    {
                     before(grammarAccess.getStateAccess().getIsInitialAssignment_1_0()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:114:1: ( rule__State__IsInitialAssignment_1_0 )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:114:2: rule__State__IsInitialAssignment_1_0
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__IsInitialAssignment_1_0_in_rule__State__Alternatives_1166);
                    rule__State__IsInitialAssignment_1_0();

                    state._fsp--;


                    }

                     after(grammarAccess.getStateAccess().getIsInitialAssignment_1_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:118:6: ( ( rule__State__IsFinalAssignment_1_1 ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:118:6: ( ( rule__State__IsFinalAssignment_1_1 ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:119:1: ( rule__State__IsFinalAssignment_1_1 )
                    {
                     before(grammarAccess.getStateAccess().getIsFinalAssignment_1_1()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:120:1: ( rule__State__IsFinalAssignment_1_1 )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:120:2: rule__State__IsFinalAssignment_1_1
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__IsFinalAssignment_1_1_in_rule__State__Alternatives_1184);
                    rule__State__IsFinalAssignment_1_1();

                    state._fsp--;


                    }

                     after(grammarAccess.getStateAccess().getIsFinalAssignment_1_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:124:6: ( ( rule__State__StateKindAssignment_1_2 ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:124:6: ( ( rule__State__StateKindAssignment_1_2 ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:125:1: ( rule__State__StateKindAssignment_1_2 )
                    {
                     before(grammarAccess.getStateAccess().getStateKindAssignment_1_2()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:126:1: ( rule__State__StateKindAssignment_1_2 )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:126:2: rule__State__StateKindAssignment_1_2
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__StateKindAssignment_1_2_in_rule__State__Alternatives_1202);
                    rule__State__StateKindAssignment_1_2();

                    state._fsp--;


                    }

                     after(grammarAccess.getStateAccess().getStateKindAssignment_1_2()); 

                    }


                    }
                    break;
                case 4 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:130:6: ( ( rule__State__Group_1_3__0 ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:130:6: ( ( rule__State__Group_1_3__0 ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:131:1: ( rule__State__Group_1_3__0 )
                    {
                     before(grammarAccess.getStateAccess().getGroup_1_3()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:132:1: ( rule__State__Group_1_3__0 )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:132:2: rule__State__Group_1_3__0
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_3__0_in_rule__State__Alternatives_1220);
                    rule__State__Group_1_3__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getStateAccess().getGroup_1_3()); 

                    }


                    }
                    break;
                case 5 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:136:6: ( ( rule__State__Group_1_4__0 ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:136:6: ( ( rule__State__Group_1_4__0 ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:137:1: ( rule__State__Group_1_4__0 )
                    {
                     before(grammarAccess.getStateAccess().getGroup_1_4()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:138:1: ( rule__State__Group_1_4__0 )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:138:2: rule__State__Group_1_4__0
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__0_in_rule__State__Alternatives_1238);
                    rule__State__Group_1_4__0();

                    state._fsp--;


                    }

                     after(grammarAccess.getStateAccess().getGroup_1_4()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Alternatives_1"


    // $ANTLR start "rule__StateType__Alternatives"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:147:1: rule__StateType__Alternatives : ( ( ( 'NORMAL' ) ) | ( ( 'cond' ) ) | ( ( 'reference' ) ) | ( ( 'textual' ) ) );
    public final void rule__StateType__Alternatives() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:151:1: ( ( ( 'NORMAL' ) ) | ( ( 'cond' ) ) | ( ( 'reference' ) ) | ( ( 'textual' ) ) )
            int alt2=4;
            switch ( input.LA(1) ) {
            case 11:
                {
                alt2=1;
                }
                break;
            case 12:
                {
                alt2=2;
                }
                break;
            case 13:
                {
                alt2=3;
                }
                break;
            case 14:
                {
                alt2=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:152:1: ( ( 'NORMAL' ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:152:1: ( ( 'NORMAL' ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:153:1: ( 'NORMAL' )
                    {
                     before(grammarAccess.getStateTypeAccess().getNORMALEnumLiteralDeclaration_0()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:154:1: ( 'NORMAL' )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:154:3: 'NORMAL'
                    {
                    match(input,11,FollowSets000.FOLLOW_11_in_rule__StateType__Alternatives272); 

                    }

                     after(grammarAccess.getStateTypeAccess().getNORMALEnumLiteralDeclaration_0()); 

                    }


                    }
                    break;
                case 2 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:159:6: ( ( 'cond' ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:159:6: ( ( 'cond' ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:160:1: ( 'cond' )
                    {
                     before(grammarAccess.getStateTypeAccess().getPSEUDOEnumLiteralDeclaration_1()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:161:1: ( 'cond' )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:161:3: 'cond'
                    {
                    match(input,12,FollowSets000.FOLLOW_12_in_rule__StateType__Alternatives293); 

                    }

                     after(grammarAccess.getStateTypeAccess().getPSEUDOEnumLiteralDeclaration_1()); 

                    }


                    }
                    break;
                case 3 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:166:6: ( ( 'reference' ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:166:6: ( ( 'reference' ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:167:1: ( 'reference' )
                    {
                     before(grammarAccess.getStateTypeAccess().getREFERENCEEnumLiteralDeclaration_2()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:168:1: ( 'reference' )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:168:3: 'reference'
                    {
                    match(input,13,FollowSets000.FOLLOW_13_in_rule__StateType__Alternatives314); 

                    }

                     after(grammarAccess.getStateTypeAccess().getREFERENCEEnumLiteralDeclaration_2()); 

                    }


                    }
                    break;
                case 4 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:173:6: ( ( 'textual' ) )
                    {
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:173:6: ( ( 'textual' ) )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:174:1: ( 'textual' )
                    {
                     before(grammarAccess.getStateTypeAccess().getTEXTUALEnumLiteralDeclaration_3()); 
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:175:1: ( 'textual' )
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:175:3: 'textual'
                    {
                    match(input,14,FollowSets000.FOLLOW_14_in_rule__StateType__Alternatives335); 

                    }

                     after(grammarAccess.getStateTypeAccess().getTEXTUALEnumLiteralDeclaration_3()); 

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__StateType__Alternatives"


    // $ANTLR start "rule__State__Group__0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:187:1: rule__State__Group__0 : rule__State__Group__0__Impl rule__State__Group__1 ;
    public final void rule__State__Group__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:191:1: ( rule__State__Group__0__Impl rule__State__Group__1 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:192:2: rule__State__Group__0__Impl rule__State__Group__1
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__0__Impl_in_rule__State__Group__0368);
            rule__State__Group__0__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group__1_in_rule__State__Group__0371);
            rule__State__Group__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__0"


    // $ANTLR start "rule__State__Group__0__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:199:1: rule__State__Group__0__Impl : ( () ) ;
    public final void rule__State__Group__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:203:1: ( ( () ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:204:1: ( () )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:204:1: ( () )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:205:1: ()
            {
             before(grammarAccess.getStateAccess().getStateAction_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:206:1: ()
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:208:1: 
            {
            }

             after(grammarAccess.getStateAccess().getStateAction_0()); 

            }


            }

        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__0__Impl"


    // $ANTLR start "rule__State__Group__1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:218:1: rule__State__Group__1 : rule__State__Group__1__Impl rule__State__Group__2 ;
    public final void rule__State__Group__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:222:1: ( rule__State__Group__1__Impl rule__State__Group__2 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:223:2: rule__State__Group__1__Impl rule__State__Group__2
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__1__Impl_in_rule__State__Group__1429);
            rule__State__Group__1__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group__2_in_rule__State__Group__1432);
            rule__State__Group__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__1"


    // $ANTLR start "rule__State__Group__1__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:230:1: rule__State__Group__1__Impl : ( ( rule__State__Alternatives_1 )? ) ;
    public final void rule__State__Group__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:234:1: ( ( ( rule__State__Alternatives_1 )? ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:235:1: ( ( rule__State__Alternatives_1 )? )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:235:1: ( ( rule__State__Alternatives_1 )? )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:236:1: ( rule__State__Alternatives_1 )?
            {
             before(grammarAccess.getStateAccess().getAlternatives_1()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:237:1: ( rule__State__Alternatives_1 )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=11 && LA3_0<=14)||(LA3_0>=16 && LA3_0<=17)) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:237:2: rule__State__Alternatives_1
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__Alternatives_1_in_rule__State__Group__1__Impl459);
                    rule__State__Alternatives_1();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getStateAccess().getAlternatives_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__1__Impl"


    // $ANTLR start "rule__State__Group__2"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:247:1: rule__State__Group__2 : rule__State__Group__2__Impl rule__State__Group__3 ;
    public final void rule__State__Group__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:251:1: ( rule__State__Group__2__Impl rule__State__Group__3 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:252:2: rule__State__Group__2__Impl rule__State__Group__3
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__2__Impl_in_rule__State__Group__2490);
            rule__State__Group__2__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group__3_in_rule__State__Group__2493);
            rule__State__Group__3();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__2"


    // $ANTLR start "rule__State__Group__2__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:259:1: rule__State__Group__2__Impl : ( ( 'state' )? ) ;
    public final void rule__State__Group__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:263:1: ( ( ( 'state' )? ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:264:1: ( ( 'state' )? )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:264:1: ( ( 'state' )? )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:265:1: ( 'state' )?
            {
             before(grammarAccess.getStateAccess().getStateKeyword_2()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:266:1: ( 'state' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==15) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:267:2: 'state'
                    {
                    match(input,15,FollowSets000.FOLLOW_15_in_rule__State__Group__2__Impl522); 

                    }
                    break;

            }

             after(grammarAccess.getStateAccess().getStateKeyword_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__2__Impl"


    // $ANTLR start "rule__State__Group__3"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:278:1: rule__State__Group__3 : rule__State__Group__3__Impl rule__State__Group__4 ;
    public final void rule__State__Group__3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:282:1: ( rule__State__Group__3__Impl rule__State__Group__4 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:283:2: rule__State__Group__3__Impl rule__State__Group__4
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__3__Impl_in_rule__State__Group__3555);
            rule__State__Group__3__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group__4_in_rule__State__Group__3558);
            rule__State__Group__4();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__3"


    // $ANTLR start "rule__State__Group__3__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:290:1: rule__State__Group__3__Impl : ( ( rule__State__StateNameAssignment_3 )? ) ;
    public final void rule__State__Group__3__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:294:1: ( ( ( rule__State__StateNameAssignment_3 )? ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:295:1: ( ( rule__State__StateNameAssignment_3 )? )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:295:1: ( ( rule__State__StateNameAssignment_3 )? )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:296:1: ( rule__State__StateNameAssignment_3 )?
            {
             before(grammarAccess.getStateAccess().getStateNameAssignment_3()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:297:1: ( rule__State__StateNameAssignment_3 )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==RULE_ID) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:297:2: rule__State__StateNameAssignment_3
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__StateNameAssignment_3_in_rule__State__Group__3__Impl585);
                    rule__State__StateNameAssignment_3();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getStateAccess().getStateNameAssignment_3()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__3__Impl"


    // $ANTLR start "rule__State__Group__4"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:307:1: rule__State__Group__4 : rule__State__Group__4__Impl ;
    public final void rule__State__Group__4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:311:1: ( rule__State__Group__4__Impl )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:312:2: rule__State__Group__4__Impl
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group__4__Impl_in_rule__State__Group__4616);
            rule__State__Group__4__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__4"


    // $ANTLR start "rule__State__Group__4__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:318:1: rule__State__Group__4__Impl : ( ( rule__State__LabelAssignment_4 )? ) ;
    public final void rule__State__Group__4__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:322:1: ( ( ( rule__State__LabelAssignment_4 )? ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:323:1: ( ( rule__State__LabelAssignment_4 )? )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:323:1: ( ( rule__State__LabelAssignment_4 )? )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:324:1: ( rule__State__LabelAssignment_4 )?
            {
             before(grammarAccess.getStateAccess().getLabelAssignment_4()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:325:1: ( rule__State__LabelAssignment_4 )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==RULE_STRING) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:325:2: rule__State__LabelAssignment_4
                    {
                    pushFollow(FollowSets000.FOLLOW_rule__State__LabelAssignment_4_in_rule__State__Group__4__Impl643);
                    rule__State__LabelAssignment_4();

                    state._fsp--;


                    }
                    break;

            }

             after(grammarAccess.getStateAccess().getLabelAssignment_4()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group__4__Impl"


    // $ANTLR start "rule__State__Group_1_3__0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:345:1: rule__State__Group_1_3__0 : rule__State__Group_1_3__0__Impl rule__State__Group_1_3__1 ;
    public final void rule__State__Group_1_3__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:349:1: ( rule__State__Group_1_3__0__Impl rule__State__Group_1_3__1 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:350:2: rule__State__Group_1_3__0__Impl rule__State__Group_1_3__1
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_3__0__Impl_in_rule__State__Group_1_3__0684);
            rule__State__Group_1_3__0__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_3__1_in_rule__State__Group_1_3__0687);
            rule__State__Group_1_3__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_3__0"


    // $ANTLR start "rule__State__Group_1_3__0__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:357:1: rule__State__Group_1_3__0__Impl : ( ( rule__State__IsInitialAssignment_1_3_0 ) ) ;
    public final void rule__State__Group_1_3__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:361:1: ( ( ( rule__State__IsInitialAssignment_1_3_0 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:362:1: ( ( rule__State__IsInitialAssignment_1_3_0 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:362:1: ( ( rule__State__IsInitialAssignment_1_3_0 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:363:1: ( rule__State__IsInitialAssignment_1_3_0 )
            {
             before(grammarAccess.getStateAccess().getIsInitialAssignment_1_3_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:364:1: ( rule__State__IsInitialAssignment_1_3_0 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:364:2: rule__State__IsInitialAssignment_1_3_0
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__IsInitialAssignment_1_3_0_in_rule__State__Group_1_3__0__Impl714);
            rule__State__IsInitialAssignment_1_3_0();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getIsInitialAssignment_1_3_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_3__0__Impl"


    // $ANTLR start "rule__State__Group_1_3__1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:374:1: rule__State__Group_1_3__1 : rule__State__Group_1_3__1__Impl ;
    public final void rule__State__Group_1_3__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:378:1: ( rule__State__Group_1_3__1__Impl )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:379:2: rule__State__Group_1_3__1__Impl
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_3__1__Impl_in_rule__State__Group_1_3__1744);
            rule__State__Group_1_3__1__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_3__1"


    // $ANTLR start "rule__State__Group_1_3__1__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:385:1: rule__State__Group_1_3__1__Impl : ( ( rule__State__StateKindAssignment_1_3_1 ) ) ;
    public final void rule__State__Group_1_3__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:389:1: ( ( ( rule__State__StateKindAssignment_1_3_1 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:390:1: ( ( rule__State__StateKindAssignment_1_3_1 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:390:1: ( ( rule__State__StateKindAssignment_1_3_1 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:391:1: ( rule__State__StateKindAssignment_1_3_1 )
            {
             before(grammarAccess.getStateAccess().getStateKindAssignment_1_3_1()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:392:1: ( rule__State__StateKindAssignment_1_3_1 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:392:2: rule__State__StateKindAssignment_1_3_1
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__StateKindAssignment_1_3_1_in_rule__State__Group_1_3__1__Impl771);
            rule__State__StateKindAssignment_1_3_1();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getStateKindAssignment_1_3_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_3__1__Impl"


    // $ANTLR start "rule__State__Group_1_4__0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:406:1: rule__State__Group_1_4__0 : rule__State__Group_1_4__0__Impl rule__State__Group_1_4__1 ;
    public final void rule__State__Group_1_4__0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:410:1: ( rule__State__Group_1_4__0__Impl rule__State__Group_1_4__1 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:411:2: rule__State__Group_1_4__0__Impl rule__State__Group_1_4__1
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__0__Impl_in_rule__State__Group_1_4__0805);
            rule__State__Group_1_4__0__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__1_in_rule__State__Group_1_4__0808);
            rule__State__Group_1_4__1();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__0"


    // $ANTLR start "rule__State__Group_1_4__0__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:418:1: rule__State__Group_1_4__0__Impl : ( ( rule__State__IsInitialAssignment_1_4_0 ) ) ;
    public final void rule__State__Group_1_4__0__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:422:1: ( ( ( rule__State__IsInitialAssignment_1_4_0 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:423:1: ( ( rule__State__IsInitialAssignment_1_4_0 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:423:1: ( ( rule__State__IsInitialAssignment_1_4_0 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:424:1: ( rule__State__IsInitialAssignment_1_4_0 )
            {
             before(grammarAccess.getStateAccess().getIsInitialAssignment_1_4_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:425:1: ( rule__State__IsInitialAssignment_1_4_0 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:425:2: rule__State__IsInitialAssignment_1_4_0
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__IsInitialAssignment_1_4_0_in_rule__State__Group_1_4__0__Impl835);
            rule__State__IsInitialAssignment_1_4_0();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getIsInitialAssignment_1_4_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__0__Impl"


    // $ANTLR start "rule__State__Group_1_4__1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:435:1: rule__State__Group_1_4__1 : rule__State__Group_1_4__1__Impl rule__State__Group_1_4__2 ;
    public final void rule__State__Group_1_4__1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:439:1: ( rule__State__Group_1_4__1__Impl rule__State__Group_1_4__2 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:440:2: rule__State__Group_1_4__1__Impl rule__State__Group_1_4__2
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__1__Impl_in_rule__State__Group_1_4__1865);
            rule__State__Group_1_4__1__Impl();

            state._fsp--;

            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__2_in_rule__State__Group_1_4__1868);
            rule__State__Group_1_4__2();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__1"


    // $ANTLR start "rule__State__Group_1_4__1__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:447:1: rule__State__Group_1_4__1__Impl : ( ( rule__State__StateKindAssignment_1_4_1 ) ) ;
    public final void rule__State__Group_1_4__1__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:451:1: ( ( ( rule__State__StateKindAssignment_1_4_1 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:452:1: ( ( rule__State__StateKindAssignment_1_4_1 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:452:1: ( ( rule__State__StateKindAssignment_1_4_1 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:453:1: ( rule__State__StateKindAssignment_1_4_1 )
            {
             before(grammarAccess.getStateAccess().getStateKindAssignment_1_4_1()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:454:1: ( rule__State__StateKindAssignment_1_4_1 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:454:2: rule__State__StateKindAssignment_1_4_1
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__StateKindAssignment_1_4_1_in_rule__State__Group_1_4__1__Impl895);
            rule__State__StateKindAssignment_1_4_1();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getStateKindAssignment_1_4_1()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__1__Impl"


    // $ANTLR start "rule__State__Group_1_4__2"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:464:1: rule__State__Group_1_4__2 : rule__State__Group_1_4__2__Impl ;
    public final void rule__State__Group_1_4__2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:468:1: ( rule__State__Group_1_4__2__Impl )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:469:2: rule__State__Group_1_4__2__Impl
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__Group_1_4__2__Impl_in_rule__State__Group_1_4__2925);
            rule__State__Group_1_4__2__Impl();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__2"


    // $ANTLR start "rule__State__Group_1_4__2__Impl"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:475:1: rule__State__Group_1_4__2__Impl : ( ( rule__State__IsFinalAssignment_1_4_2 ) ) ;
    public final void rule__State__Group_1_4__2__Impl() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:479:1: ( ( ( rule__State__IsFinalAssignment_1_4_2 ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:480:1: ( ( rule__State__IsFinalAssignment_1_4_2 ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:480:1: ( ( rule__State__IsFinalAssignment_1_4_2 ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:481:1: ( rule__State__IsFinalAssignment_1_4_2 )
            {
             before(grammarAccess.getStateAccess().getIsFinalAssignment_1_4_2()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:482:1: ( rule__State__IsFinalAssignment_1_4_2 )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:482:2: rule__State__IsFinalAssignment_1_4_2
            {
            pushFollow(FollowSets000.FOLLOW_rule__State__IsFinalAssignment_1_4_2_in_rule__State__Group_1_4__2__Impl952);
            rule__State__IsFinalAssignment_1_4_2();

            state._fsp--;


            }

             after(grammarAccess.getStateAccess().getIsFinalAssignment_1_4_2()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__Group_1_4__2__Impl"


    // $ANTLR start "rule__State__IsInitialAssignment_1_0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:499:1: rule__State__IsInitialAssignment_1_0 : ( ( 'init' ) ) ;
    public final void rule__State__IsInitialAssignment_1_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:503:1: ( ( ( 'init' ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:504:1: ( ( 'init' ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:504:1: ( ( 'init' ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:505:1: ( 'init' )
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_0_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:506:1: ( 'init' )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:507:1: 'init'
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_0_0()); 
            match(input,16,FollowSets000.FOLLOW_16_in_rule__State__IsInitialAssignment_1_0998); 
             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_0_0()); 

            }

             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__IsInitialAssignment_1_0"


    // $ANTLR start "rule__State__IsFinalAssignment_1_1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:522:1: rule__State__IsFinalAssignment_1_1 : ( ( 'final' ) ) ;
    public final void rule__State__IsFinalAssignment_1_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:526:1: ( ( ( 'final' ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:527:1: ( ( 'final' ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:527:1: ( ( 'final' ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:528:1: ( 'final' )
            {
             before(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_1_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:529:1: ( 'final' )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:530:1: 'final'
            {
             before(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_1_0()); 
            match(input,17,FollowSets000.FOLLOW_17_in_rule__State__IsFinalAssignment_1_11042); 
             after(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_1_0()); 

            }

             after(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__IsFinalAssignment_1_1"


    // $ANTLR start "rule__State__StateKindAssignment_1_2"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:545:1: rule__State__StateKindAssignment_1_2 : ( ruleStateType ) ;
    public final void rule__State__StateKindAssignment_1_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:549:1: ( ( ruleStateType ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:550:1: ( ruleStateType )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:550:1: ( ruleStateType )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:551:1: ruleStateType
            {
             before(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_2_0()); 
            pushFollow(FollowSets000.FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_21081);
            ruleStateType();

            state._fsp--;

             after(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__StateKindAssignment_1_2"


    // $ANTLR start "rule__State__IsInitialAssignment_1_3_0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:560:1: rule__State__IsInitialAssignment_1_3_0 : ( ( 'init' ) ) ;
    public final void rule__State__IsInitialAssignment_1_3_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:564:1: ( ( ( 'init' ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:565:1: ( ( 'init' ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:565:1: ( ( 'init' ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:566:1: ( 'init' )
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_3_0_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:567:1: ( 'init' )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:568:1: 'init'
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_3_0_0()); 
            match(input,16,FollowSets000.FOLLOW_16_in_rule__State__IsInitialAssignment_1_3_01117); 
             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_3_0_0()); 

            }

             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_3_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__IsInitialAssignment_1_3_0"


    // $ANTLR start "rule__State__StateKindAssignment_1_3_1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:583:1: rule__State__StateKindAssignment_1_3_1 : ( ruleStateType ) ;
    public final void rule__State__StateKindAssignment_1_3_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:587:1: ( ( ruleStateType ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:588:1: ( ruleStateType )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:588:1: ( ruleStateType )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:589:1: ruleStateType
            {
             before(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_3_1_0()); 
            pushFollow(FollowSets000.FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_3_11156);
            ruleStateType();

            state._fsp--;

             after(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_3_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__StateKindAssignment_1_3_1"


    // $ANTLR start "rule__State__IsInitialAssignment_1_4_0"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:598:1: rule__State__IsInitialAssignment_1_4_0 : ( ( 'init' ) ) ;
    public final void rule__State__IsInitialAssignment_1_4_0() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:602:1: ( ( ( 'init' ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:603:1: ( ( 'init' ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:603:1: ( ( 'init' ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:604:1: ( 'init' )
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_4_0_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:605:1: ( 'init' )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:606:1: 'init'
            {
             before(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_4_0_0()); 
            match(input,16,FollowSets000.FOLLOW_16_in_rule__State__IsInitialAssignment_1_4_01192); 
             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_4_0_0()); 

            }

             after(grammarAccess.getStateAccess().getIsInitialInitKeyword_1_4_0_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__IsInitialAssignment_1_4_0"


    // $ANTLR start "rule__State__StateKindAssignment_1_4_1"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:621:1: rule__State__StateKindAssignment_1_4_1 : ( ruleStateType ) ;
    public final void rule__State__StateKindAssignment_1_4_1() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:625:1: ( ( ruleStateType ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:626:1: ( ruleStateType )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:626:1: ( ruleStateType )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:627:1: ruleStateType
            {
             before(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_4_1_0()); 
            pushFollow(FollowSets000.FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_4_11231);
            ruleStateType();

            state._fsp--;

             after(grammarAccess.getStateAccess().getStateKindStateTypeEnumRuleCall_1_4_1_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__StateKindAssignment_1_4_1"


    // $ANTLR start "rule__State__IsFinalAssignment_1_4_2"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:636:1: rule__State__IsFinalAssignment_1_4_2 : ( ( 'final' ) ) ;
    public final void rule__State__IsFinalAssignment_1_4_2() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:640:1: ( ( ( 'final' ) ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:641:1: ( ( 'final' ) )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:641:1: ( ( 'final' ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:642:1: ( 'final' )
            {
             before(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_4_2_0()); 
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:643:1: ( 'final' )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:644:1: 'final'
            {
             before(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_4_2_0()); 
            match(input,17,FollowSets000.FOLLOW_17_in_rule__State__IsFinalAssignment_1_4_21267); 
             after(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_4_2_0()); 

            }

             after(grammarAccess.getStateAccess().getIsFinalFinalKeyword_1_4_2_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__IsFinalAssignment_1_4_2"


    // $ANTLR start "rule__State__StateNameAssignment_3"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:659:1: rule__State__StateNameAssignment_3 : ( RULE_ID ) ;
    public final void rule__State__StateNameAssignment_3() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:663:1: ( ( RULE_ID ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:664:1: ( RULE_ID )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:664:1: ( RULE_ID )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:665:1: RULE_ID
            {
             before(grammarAccess.getStateAccess().getStateNameIDTerminalRuleCall_3_0()); 
            match(input,RULE_ID,FollowSets000.FOLLOW_RULE_ID_in_rule__State__StateNameAssignment_31306); 
             after(grammarAccess.getStateAccess().getStateNameIDTerminalRuleCall_3_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__StateNameAssignment_3"


    // $ANTLR start "rule__State__LabelAssignment_4"
    // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:674:1: rule__State__LabelAssignment_4 : ( RULE_STRING ) ;
    public final void rule__State__LabelAssignment_4() throws RecognitionException {

        		int stackSize = keepStackSize();
            
        try {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:678:1: ( ( RULE_STRING ) )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:679:1: ( RULE_STRING )
            {
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:679:1: ( RULE_STRING )
            // ../org.eclipse.xtext.ui.tests/src-gen/org/eclipse/xtext/ui/tests/editor/contentassist/ui/contentassist/antlr/internal/InternalBug286935TestLanguage.g:680:1: RULE_STRING
            {
             before(grammarAccess.getStateAccess().getLabelSTRINGTerminalRuleCall_4_0()); 
            match(input,RULE_STRING,FollowSets000.FOLLOW_RULE_STRING_in_rule__State__LabelAssignment_41337); 
             after(grammarAccess.getStateAccess().getLabelSTRINGTerminalRuleCall_4_0()); 

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {

            	restoreStackSize(stackSize);

        }
        return ;
    }
    // $ANTLR end "rule__State__LabelAssignment_4"

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    static final String DFA1_eotS =
        "\13\uffff";
    static final String DFA1_eofS =
        "\1\uffff\1\4\3\uffff\4\12\2\uffff";
    static final String DFA1_minS =
        "\1\13\1\4\3\uffff\4\4\2\uffff";
    static final String DFA1_maxS =
        "\1\21\1\17\3\uffff\4\21\2\uffff";
    static final String DFA1_acceptS =
        "\2\uffff\1\2\1\3\1\1\4\uffff\1\5\1\4";
    static final String DFA1_specialS =
        "\13\uffff}>";
    static final String[] DFA1_transitionS = {
            "\4\3\1\uffff\1\1\1\2",
            "\2\4\5\uffff\1\5\1\6\1\7\1\10\1\4",
            "",
            "",
            "",
            "\2\12\11\uffff\1\12\1\uffff\1\11",
            "\2\12\11\uffff\1\12\1\uffff\1\11",
            "\2\12\11\uffff\1\12\1\uffff\1\11",
            "\2\12\11\uffff\1\12\1\uffff\1\11",
            "",
            ""
    };

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "107:1: rule__State__Alternatives_1 : ( ( ( rule__State__IsInitialAssignment_1_0 ) ) | ( ( rule__State__IsFinalAssignment_1_1 ) ) | ( ( rule__State__StateKindAssignment_1_2 ) ) | ( ( rule__State__Group_1_3__0 ) ) | ( ( rule__State__Group_1_4__0 ) ) );";
        }
    }
 

    
    private static class FollowSets000 {
        public static final BitSet FOLLOW_ruleState_in_entryRuleState61 = new BitSet(new long[]{0x0000000000000000L});
        public static final BitSet FOLLOW_EOF_in_entryRuleState68 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__0_in_ruleState94 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__StateType__Alternatives_in_ruleStateType131 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__IsInitialAssignment_1_0_in_rule__State__Alternatives_1166 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__IsFinalAssignment_1_1_in_rule__State__Alternatives_1184 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__StateKindAssignment_1_2_in_rule__State__Alternatives_1202 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_3__0_in_rule__State__Alternatives_1220 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__0_in_rule__State__Alternatives_1238 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_11_in_rule__StateType__Alternatives272 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_12_in_rule__StateType__Alternatives293 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_13_in_rule__StateType__Alternatives314 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_14_in_rule__StateType__Alternatives335 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__0__Impl_in_rule__State__Group__0368 = new BitSet(new long[]{0x000000000003F830L});
        public static final BitSet FOLLOW_rule__State__Group__1_in_rule__State__Group__0371 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__1__Impl_in_rule__State__Group__1429 = new BitSet(new long[]{0x000000000003F830L});
        public static final BitSet FOLLOW_rule__State__Group__2_in_rule__State__Group__1432 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Alternatives_1_in_rule__State__Group__1__Impl459 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__2__Impl_in_rule__State__Group__2490 = new BitSet(new long[]{0x000000000003F830L});
        public static final BitSet FOLLOW_rule__State__Group__3_in_rule__State__Group__2493 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_15_in_rule__State__Group__2__Impl522 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__3__Impl_in_rule__State__Group__3555 = new BitSet(new long[]{0x000000000003F830L});
        public static final BitSet FOLLOW_rule__State__Group__4_in_rule__State__Group__3558 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__StateNameAssignment_3_in_rule__State__Group__3__Impl585 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group__4__Impl_in_rule__State__Group__4616 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__LabelAssignment_4_in_rule__State__Group__4__Impl643 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_3__0__Impl_in_rule__State__Group_1_3__0684 = new BitSet(new long[]{0x0000000000007800L});
        public static final BitSet FOLLOW_rule__State__Group_1_3__1_in_rule__State__Group_1_3__0687 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__IsInitialAssignment_1_3_0_in_rule__State__Group_1_3__0__Impl714 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_3__1__Impl_in_rule__State__Group_1_3__1744 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__StateKindAssignment_1_3_1_in_rule__State__Group_1_3__1__Impl771 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__0__Impl_in_rule__State__Group_1_4__0805 = new BitSet(new long[]{0x0000000000007800L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__1_in_rule__State__Group_1_4__0808 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__IsInitialAssignment_1_4_0_in_rule__State__Group_1_4__0__Impl835 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__1__Impl_in_rule__State__Group_1_4__1865 = new BitSet(new long[]{0x0000000000020000L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__2_in_rule__State__Group_1_4__1868 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__StateKindAssignment_1_4_1_in_rule__State__Group_1_4__1__Impl895 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__Group_1_4__2__Impl_in_rule__State__Group_1_4__2925 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_rule__State__IsFinalAssignment_1_4_2_in_rule__State__Group_1_4__2__Impl952 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_16_in_rule__State__IsInitialAssignment_1_0998 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_17_in_rule__State__IsFinalAssignment_1_11042 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_21081 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_16_in_rule__State__IsInitialAssignment_1_3_01117 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_3_11156 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_16_in_rule__State__IsInitialAssignment_1_4_01192 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_ruleStateType_in_rule__State__StateKindAssignment_1_4_11231 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_17_in_rule__State__IsFinalAssignment_1_4_21267 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_ID_in_rule__State__StateNameAssignment_31306 = new BitSet(new long[]{0x0000000000000002L});
        public static final BitSet FOLLOW_RULE_STRING_in_rule__State__LabelAssignment_41337 = new BitSet(new long[]{0x0000000000000002L});
    }


}