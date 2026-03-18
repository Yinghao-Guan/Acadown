package com.guaguaaaa.acadown.core.parser;

import com.guaguaaaa.acadown.core.api.Diagnostic;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

public class SyntaxErrorCollector extends BaseErrorListener {
    private final List<Diagnostic> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {

        int start = -1;
        int end = -1;

        if (offendingSymbol instanceof Token) {
            Token token = (Token) offendingSymbol;
            start = token.getStartIndex();
            end = token.getStopIndex() + 1;
        }

        if (start < 0) {

            if (recognizer instanceof Parser) {
                TokenStream stream = ((Parser) recognizer).getTokenStream();
                for (int i = stream.size() - 1; i >= 0; i--) {
                    Token t = stream.get(i);
                    if (t.getType() != Token.EOF && t.getStartIndex() >= 0) {
                        start = t.getStopIndex() + 1;
                        end = start + 1;
                        break;
                    }
                }
            }

            else if (recognizer instanceof Lexer) {
                if (e instanceof LexerNoViableAltException) {
                    start = ((LexerNoViableAltException) e).getStartIndex();
                    end = start + 1;
                } else {
                    start = ((Lexer) recognizer).getCharIndex();
                    end = start + 1;
                }
            }
        }

        if (start < 0) {
            start = 0;
            end = 1;
        }

        errors.add(new Diagnostic(line, charPositionInLine, start, end, msg));
    }

    public List<Diagnostic> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}