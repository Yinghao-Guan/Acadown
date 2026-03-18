lexer grammar AcadownLexer;

// ======================= Virtual Tokens =======================
tokens {
    INDENT,
    DEDENT
}

// ======================= Java Members =======================
@header {
    import java.util.LinkedList;
    import java.util.Stack;
    import org.antlr.v4.runtime.CommonToken;
}

@members {
    // 缩进栈，初始推入 0 (基准层级)
    private Stack<Integer> indentLengths = new Stack<>() {{ push(0); }};
    // 待发射的 Token 队列 (用于一次性吐出多个 DEDENT)
    private LinkedList<Token> pendingTokens = new LinkedList<>();
    // 标记当前是否处于行首 (初始为 true)
    private boolean atStartOfLine = true;
    @Override
    public Token nextToken() {
        // 1. 如果队列里有刚才生成的虚拟 Token (如 DEDENT)，优先返回
        if (!pendingTokens.isEmpty()) {
            return pendingTokens.poll();
        }

        // 2. 获取下一个真实的物理 Token
        Token t = super.nextToken();
        // 3. 处理 EOF (文件结束时，必须关闭所有缩进)
        if (t.getType() == EOF) {
            handleEOF();
            if (!pendingTokens.isEmpty()) {
                return pendingTokens.poll();
            }
            return t;
        }

        // 4. 处理换行符 (重置行首标记)
        if (isNewline(t)) {
            atStartOfLine = true;
            return t;
        }

        // 5. 核心逻辑：处理行首的缩进
        if (atStartOfLine) {
            // 情况 A: 行首是空格 -> 计算缩进深度
            if (t.getType() == SPACE) {
                int indent = getTextLength(t.getText());
                // 严格模式检查
                if (indent % 4 != 0) {
                     throw new RuntimeException("Indentation Error: Indentation must be a multiple of 4 spaces. Found: " + indent);
                }

                processIndentation(indent, t);
                atStartOfLine = false;

                if (!pendingTokens.isEmpty()) {
                    return pendingTokens.poll();
                } else {
                    return nextToken();
                }
            }
            // 情况 B: 行首是普通内容 (缩进为 0)
            else {
                if (indentLengths.peek() > 0) {
                    processIndentation(0, t);
                    pendingTokens.add(t);
                    atStartOfLine = false;
                    return pendingTokens.poll();
                }
                atStartOfLine = false;
            }
        }

        return t;
    }

    private boolean isNewline(Token t) {
        int type = t.getType();
        return type == SOFT_BREAK || type == HARD_BREAK || type == PARAGRAPH_END;
    }

    private int getTextLength(String text) {
        int len = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                 throw new RuntimeException("Indentation Error: Tabs are not allowed. Please use 4 spaces.");
            }
            len++;
        }
        return len;
    }

    private void processIndentation(int targetIndent, Token triggerToken) {
        int current = indentLengths.peek();
        if (targetIndent > current) {
            indentLengths.push(targetIndent);
            createToken(INDENT, triggerToken);
        } else if (targetIndent < current) {
            while (indentLengths.peek() > targetIndent) {
                indentLengths.pop();
                createToken(DEDENT, triggerToken);
            }
            if (indentLengths.peek() != targetIndent) {
                 throw new RuntimeException("Indentation Error: Unmatched indentation level. Expected " + indentLengths.peek() + ", found " + targetIndent);
            }
        }
    }

    private void handleEOF() {
        while (indentLengths.peek() > 0) {
            indentLengths.pop();
            createToken(DEDENT, null);
        }
    }

    private void createToken(int type, Token origin) {
        CommonToken token;
        if (origin != null) {
            token = new CommonToken(type, type == INDENT ? "INDENT" : "DEDENT");
            token.setLine(origin.getLine());
            token.setCharPositionInLine(origin.getCharPositionInLine());
            token.setChannel(origin.getChannel());
            token.setStartIndex(origin.getStartIndex());
            token.setStopIndex(origin.getStopIndex());
        } else {
            token = new CommonToken(type, "DEDENT");
        }
        pendingTokens.add(token);
    }
}

// ======================= Lexer Rules =======================

YAML_BLOCK
    : '---' [ \t]* [\r\n]+ ( . | [\r\n] )*?
    [\r\n]+ '---'
    ;

HR
    : '---' '-'*
    ;

// ======================= LaTeX First Rules (High Priority) =======================

ESCAPED_BACKSLASH : '\\\\' ;
ESCAPED_NEWLINE   : '\\n' ;
ESCAPED_STAR      : '\\*' ;
ESCAPED_LBRACKET  : '\\[' ;
ESCAPED_RBRACKET  : '\\]' ;
ESCAPED_HYPHEN    : '\\-' ;
ESCAPED_GRAVE     : '\\`' ;

LATEX_ENV_BLOCK
    : '\\begin{' [a-zA-Z0-9*]+ '}' ( . | [\r\n] )*? '\\end{' [a-zA-Z0-9*]+ '}'
    ;

fragment BALANCED_BRACES
    : '{' ( ~[{}] | BALANCED_BRACES )* '}'
    ;

fragment OPTIONAL_BRACKETS
    : '[' ~[\r\n\]]* ']'
    ;

RAW_LATEX_WITH_ARGS
    : '\\' [a-zA-Z]+ '*'? [ \t]*
      (OPTIONAL_BRACKETS [ \t]*)*
      (BALANCED_BRACES)+
    ;

RAW_LATEX_CMD
    : '\\' [a-zA-Z]+ '*'?
      (OPTIONAL_BRACKETS)*
    ;


RAW_LATEX_SYMBOL
    : '\\' ~[a-zA-Z0-9\r\n\t ]
    ;

// ======================= Standard Markdown Rules =======================

H1 : '#' [ \t]+ ;
H2 : '##' [ \t]+ ;
H3 : '###' [ \t]+ ;
H4 : '####' [ \t]+ ;
H5 : '#####' [ \t]+ ;
H6 : '######' [ \t]+ ;

DASH : '-' ;
STAR : '*' ;
BANG : '!' ;
GT   : '>' ;
LPAREN   : '(' ;
RPAREN   : ')' ;

BOLD_MARK : '**' ;
INLINE_MATH : '$' ~[$]+ '$' ;
BLOCK_MATH: '$$' ( . | [\r\n] )*? '$$' ;
INLINE_CODE : '`' ~[`\r\n]+ '`' ;
CODE_BLOCK : '```' ( . | [\r\n] )*? '```' ;
CITATION : '[' '@' [a-zA-Z0-9_:-]+ ']' ;
REF_ID : '[' ~[ \t\r\n\]]+ ']' ;

PLUS_ITEM : '+' [ \t]+ ;
ORDERED_LIST_ITEM
    : (
        '(' ( [0-9]+ | [a-zA-Z] | [IVXLCDMivxlcdm]+ ) ')'
        |
        ( [0-9]+ | [a-zA-Z] | [IVXLCDMivxlcdm]+ ) ('.' | ')')
      )
      [ \t]+
    ;

LBRACKET : '[' ;
RBRACKET : ']' ;

HARD_BREAK
    : '  ' ('\r'? '\n')
    ;

PARAGRAPH_END : ('\r'? '\n') ('\r'? '\n')+ ;
SOFT_BREAK : '\r'? '\n' ;

ESCAPED : '\\' ~[\r\n] ;
URL_TEXT : [a-zA-Z0-9:/.?#&=_%+]+ ;
TEXT : ~[*\\$`#[\]!>() \t\r\n-]+ ;
SPACE : [ \t]+ ;