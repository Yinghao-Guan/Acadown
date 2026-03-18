parser grammar AcadownParser;

options { tokenVocab=AcadownLexer; }

// ======================= Parser Rules =======================

doc
    : yaml_block?
    (block | PARAGRAPH_END | SOFT_BREAK | SPACE)* EOF
    ;

yaml_block
    : YAML_BLOCK
    ;

block
    : horizontalRule          # HorizontalRuleBlock
    | blockquote              # BlockQuoteBlock
    | { _input.LA(1) == DASH && _input.LA(2) == SPACE }? bulletListBlock   # BulletListRule
    | { _input.LA(1) == ORDERED_LIST_ITEM || _input.LA(1) == PLUS_ITEM }? orderedListBlock # OrderedListRule
    | codeBlock               # CodeBlockRule
    | header                  # HeaderRule
    | blockMath               # BlockMathRule
    | latexEnv                # LatexEnvBlockRule
    | paragraph               # ParagraphBlock
    ;

horizontalRule
    : HR (SOFT_BREAK | PARAGRAPH_END | EOF)?
    ;

blockquote
    : GT SPACE?
      inline+ (SOFT_BREAK | PARAGRAPH_END | EOF)
    ;

header
    : (H1 | H2 | H3 | H4 | H5 | H6) inline+ (SOFT_BREAK | PARAGRAPH_END | EOF)
    ;


paragraph
    : inlineNoBreak+
      (
        {
           _input.LA(1) == SOFT_BREAK &&
           !(_input.LA(2) == DASH && _input.LA(3) == SPACE) &&
           !(_input.LA(2) == ORDERED_LIST_ITEM || _input.LA(2) == PLUS_ITEM) &&
           !(_input.LA(2) == H1 || _input.LA(2) == H2 || _input.LA(2) == H3 ||
             _input.LA(2) == H4 || _input.LA(2) == H5 || _input.LA(2) == H6) &&
           !(_input.LA(2) == HR) &&
           !(_input.LA(2) == GT) &&
           !(_input.LA(2) == CODE_BLOCK) &&
           !(_input.LA(2) == BLOCK_MATH) &&
           !(_input.LA(2) == LATEX_ENV_BLOCK) &&
           !(_input.LA(2) == INDENT) &&
           !(_input.LA(2) == DEDENT)
        }?
        SOFT_BREAK
        inlineNoBreak+
      )*
      (
          PARAGRAPH_END
        | EOF
        | {
            _input.LA(1) == BLOCK_MATH ||
            _input.LA(1) == LATEX_ENV_BLOCK ||
            _input.LA(1) == CODE_BLOCK ||
            _input.LA(1) == H1 || _input.LA(1) == H2 || _input.LA(1) == H3 ||
            _input.LA(1) == H4 || _input.LA(1) == H5 || _input.LA(1) == H6 ||
            (_input.LA(1) == DASH && _input.LA(2) == SPACE) ||
            _input.LA(1) == ORDERED_LIST_ITEM || _input.LA(1) == PLUS_ITEM ||
            (_input.LA(1) == SOFT_BREAK && (
                _input.LA(2) == BLOCK_MATH ||
                _input.LA(2) == LATEX_ENV_BLOCK ||
                _input.LA(2) == CODE_BLOCK ||
                _input.LA(2) == H1 || _input.LA(2) == H2 || _input.LA(2) == H3 ||
                _input.LA(2) == H4 || _input.LA(2) == H5 || _input.LA(2) == H6 ||
                (_input.LA(2) == DASH && _input.LA(3) == SPACE) ||
                _input.LA(2) == ORDERED_LIST_ITEM || _input.LA(2) == PLUS_ITEM ||
                _input.LA(2) == HR ||
                _input.LA(2) == GT ||
                _input.LA(2) == INDENT ||
                _input.LA(2) == DEDENT
            ))
          }?
      )
    ;

blockMath
    : BLOCK_MATH SPACE? REF_ID?
      (PARAGRAPH_END | EOF)?
    ;

latexEnv
    : LATEX_ENV_BLOCK
      (PARAGRAPH_END | EOF)?
    ;

codeBlock
    : CODE_BLOCK (PARAGRAPH_END | EOF)?
    ;

// ======================= List Rules =======================

bulletListBlock
    : bulletList (PARAGRAPH_END | EOF)?
    ;

bulletList
    : listItem+
    ;

listItem
    : DASH SPACE
      inlineCommon+
      (
        (SOFT_BREAK | HARD_BREAK | PARAGRAPH_END)
        INDENT
        nestedBody
        DEDENT
      )?
      (SOFT_BREAK | EOF)?
    ;

orderedListBlock
    : orderedList (PARAGRAPH_END | EOF)?
    ;

orderedList
    : orderedListItem+
    ;

orderedListItem
    : (ORDERED_LIST_ITEM | PLUS_ITEM)
      inlineCommon+
      (
        (SOFT_BREAK | HARD_BREAK | PARAGRAPH_END)
        INDENT
        nestedBody
        DEDENT
      )?
      (SOFT_BREAK | EOF)?
    ;

nestedBody
    : (block | PARAGRAPH_END | SOFT_BREAK | SPACE)+
    ;

// ======================= Inline Rules =======================

inlineNoBreak
    : inline
    ;

inline
    : inlineCommon    # CommonInlineWrapper
    | HARD_BREAK      # HardBreakInline
    ;

inlineCommon
    : image                   # ImageInline
    | link                    # LinkInline
    | ref                     # RefInline
    | bold                    # BoldInline
    | italic                  # ItalicInline
    | INLINE_MATH             # InlineMathInline
    | INLINE_CODE             # InlineCodeInline
    | citation                # CitationInline
    | lbracket                # LBracketInline
    | rbracket                # RBracketInline
    | bang                    # BangInline
    | gt                      # GtInline
    | lparen                  # LParenInline
    | rparen                  # RParenInline
    | urlText                 # UrlTextInline
    | dash                    # DashInline
    | star                    # StarInline
    | ESCAPED                 # EscapedInline
    | TEXT                    # TextInline
    | SPACE                   # SpaceInline
    | ORDERED_LIST_ITEM       # TextInline
    | PLUS_ITEM               # TextInline
    | escapeException         # EscapedExceptionInline
    | rawLatex                # RawLatexInline
    ;

escapeException
    : ESCAPED_BACKSLASH
    | ESCAPED_NEWLINE
    | ESCAPED_STAR
    | ESCAPED_LBRACKET
    | ESCAPED_RBRACKET
    | ESCAPED_HYPHEN
    | ESCAPED_GRAVE
    ;

rawLatex
    : RAW_LATEX_WITH_ARGS
    | RAW_LATEX_CMD
    | RAW_LATEX_SYMBOL
    ;

citation : CITATION ;
lbracket : LBRACKET ;
rbracket : RBRACKET ;
bold     : BOLD_MARK inline+ BOLD_MARK ;
italic   : STAR inline+ STAR ;
bang     : BANG ;
gt       : GT ;
lparen   : LPAREN ;
rparen   : RPAREN ;
urlText  : URL_TEXT ;
dash     : DASH ;
star     : STAR ;
link     : (LBRACKET inline+ RBRACKET | REF_ID) LPAREN url RPAREN ;
image    : BANG (LBRACKET inline* RBRACKET | REF_ID) LPAREN url RPAREN ;
url      : (URL_TEXT | DASH | STAR | TEXT)+ ;
ref      : REF_ID ;