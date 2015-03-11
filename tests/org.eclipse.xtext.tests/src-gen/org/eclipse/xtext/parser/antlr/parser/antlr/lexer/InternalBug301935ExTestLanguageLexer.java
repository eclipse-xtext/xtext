package org.eclipse.xtext.parser.antlr.parser.antlr.lexer;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.parser.antlr.Lexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalBug301935ExTestLanguageLexer extends Lexer {
    public static final int RULE_ID=6;
    public static final int CarriageReturnCr=5;
    public static final int RULE_WS=7;
    public static final int EOF=-1;
    public static final int RULE_ANY_OTHER=8;
    public static final int LineFeedLf=4;

    // delegates
    // delegators

    public InternalBug301935ExTestLanguageLexer() {;} 
    public InternalBug301935ExTestLanguageLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public InternalBug301935ExTestLanguageLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g"; }

    // $ANTLR start "LineFeedLf"
    public final void mLineFeedLf() throws RecognitionException {
        try {
            int _type = LineFeedLf;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:19:12: ( '\\n' )
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:19:14: '\\n'
            {
            match('\n'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LineFeedLf"

    // $ANTLR start "CarriageReturnCr"
    public final void mCarriageReturnCr() throws RecognitionException {
        try {
            int _type = CarriageReturnCr;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:21:18: ( '\\r' )
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:21:20: '\\r'
            {
            match('\r'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CarriageReturnCr"

    // $ANTLR start "RULE_ID"
    public final void mRULE_ID() throws RecognitionException {
        try {
            int _type = RULE_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:25:9: ( ( '^' )? ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:25:11: ( '^' )? ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:25:11: ( '^' )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='^') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:25:11: '^'
                    {
                    match('^'); 

                    }
                    break;

            }

            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:25:40: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')||(LA2_0>='A' && LA2_0<='Z')||LA2_0=='_'||(LA2_0>='a' && LA2_0<='z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ID"

    // $ANTLR start "RULE_WS"
    public final void mRULE_WS() throws RecognitionException {
        try {
            int _type = RULE_WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:27:9: ( ( ' ' | '\\t' )+ )
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:27:11: ( ' ' | '\\t' )+
            {
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:27:11: ( ' ' | '\\t' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\t'||LA3_0==' ') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_WS"

    // $ANTLR start "RULE_ANY_OTHER"
    public final void mRULE_ANY_OTHER() throws RecognitionException {
        try {
            int _type = RULE_ANY_OTHER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:29:16: ( . )
            // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:29:18: .
            {
            matchAny(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ANY_OTHER"

    public void mTokens() throws RecognitionException {
        // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:8: ( LineFeedLf | CarriageReturnCr | RULE_ID | RULE_WS | RULE_ANY_OTHER )
        int alt4=5;
        alt4 = dfa4.predict(input);
        switch (alt4) {
            case 1 :
                // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:10: LineFeedLf
                {
                mLineFeedLf(); 

                }
                break;
            case 2 :
                // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:21: CarriageReturnCr
                {
                mCarriageReturnCr(); 

                }
                break;
            case 3 :
                // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:38: RULE_ID
                {
                mRULE_ID(); 

                }
                break;
            case 4 :
                // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:46: RULE_WS
                {
                mRULE_WS(); 

                }
                break;
            case 5 :
                // ../org.eclipse.xtext.tests/src-gen/org/eclipse/xtext/parser/antlr/parser/antlr/lexer/InternalBug301935ExTestLanguageLexer.g:1:54: RULE_ANY_OTHER
                {
                mRULE_ANY_OTHER(); 

                }
                break;

        }

    }


    protected DFA4 dfa4 = new DFA4(this);
    static final String DFA4_eotS =
        "\3\uffff\1\6\7\uffff";
    static final String DFA4_eofS =
        "\13\uffff";
    static final String DFA4_minS =
        "\1\0\2\uffff\1\101\7\uffff";
    static final String DFA4_maxS =
        "\1\uffff\2\uffff\1\172\7\uffff";
    static final String DFA4_acceptS =
        "\1\uffff\1\1\1\2\1\uffff\1\3\1\4\1\5\1\1\1\2\1\3\1\4";
    static final String DFA4_specialS =
        "\1\0\12\uffff}>";
    static final String[] DFA4_transitionS = {
            "\11\6\1\5\1\1\2\6\1\2\22\6\1\5\40\6\32\4\3\6\1\3\1\4\1\6\32\4\uff85\6",
            "",
            "",
            "\32\11\4\uffff\1\11\1\uffff\32\11",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    static class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( LineFeedLf | CarriageReturnCr | RULE_ID | RULE_WS | RULE_ANY_OTHER );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_0 = input.LA(1);

                        s = -1;
                        if ( (LA4_0=='\n') ) {s = 1;}

                        else if ( (LA4_0=='\r') ) {s = 2;}

                        else if ( (LA4_0=='^') ) {s = 3;}

                        else if ( ((LA4_0>='A' && LA4_0<='Z')||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')) ) {s = 4;}

                        else if ( (LA4_0=='\t'||LA4_0==' ') ) {s = 5;}

                        else if ( ((LA4_0>='\u0000' && LA4_0<='\b')||(LA4_0>='\u000B' && LA4_0<='\f')||(LA4_0>='\u000E' && LA4_0<='\u001F')||(LA4_0>='!' && LA4_0<='@')||(LA4_0>='[' && LA4_0<=']')||LA4_0=='`'||(LA4_0>='{' && LA4_0<='\uFFFF')) ) {s = 6;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}