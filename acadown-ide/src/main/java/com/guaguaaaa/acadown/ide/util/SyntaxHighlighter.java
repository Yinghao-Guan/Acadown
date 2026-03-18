package com.guaguaaaa.acadown.ide.util;

import com.guaguaaaa.acadown.core.api.Diagnostic;
import com.guaguaaaa.acadown.core.parser.AcadownLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SyntaxHighlighter {

    /**
     * 计算语法高亮 (Base Layer)
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        AcadownLexer lexer = new AcadownLexer(CharStreams.fromString(text));
        lexer.removeErrorListeners();

        // 获取所有 Token 列表，以便支持 Lookahead (向前看)
        List<? extends Token> tokens = lexer.getAllTokens();
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastTokenEnd = 0;

        // 状态标记
        boolean isBold = false;
        boolean isItalic = false;
        boolean isLineStart = true;

        // 使用索引循环，以便访问 i+1
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int tokenStart = token.getStartIndex();
            int tokenEnd = token.getStopIndex() + 1;

            // 填充 Token 之间的空白
            if (tokenStart > lastTokenEnd) {
                spansBuilder.add(Collections.emptyList(), tokenStart - lastTokenEnd);
            }

            int tokenType = token.getType();
            List<String> styles = new ArrayList<>();

            // 动态逻辑处理 (粗体/斜体状态, 列表行首判断, 图片前瞻)
            // 处理粗体 **
            if (tokenType == AcadownLexer.BOLD_MARK) {
                isBold = !isBold;
                styles.add("bold-marker");
                isLineStart = false;
            }
            // 处理斜体 *
            else if (tokenType == AcadownLexer.STAR) {
                isItalic = !isItalic;
                styles.add("italic-marker");
                isLineStart = false;
            }
            // 处理列表
            else if (tokenType == AcadownLexer.DASH) {
                if (isLineStart) {
                    styles.add("list-marker");
                }
                isLineStart = false;
            }
            // 处理图片
            else if (tokenType == AcadownLexer.BANG) {
                boolean isImageStart = false;
                if (i + 1 < tokens.size()) {
                    Token nextToken = tokens.get(i + 1);
                    if (nextToken.getType() == AcadownLexer.LBRACKET) {
                        isImageStart = true;
                    }
                }

                if (isImageStart) {
                    styles.add("image-marker");
                }
                isLineStart = false;
            }

            else if (tokenType == AcadownLexer.HARD_BREAK || tokenType == AcadownLexer.PARAGRAPH_END || tokenType == AcadownLexer.SOFT_BREAK) {
                isBold = false;
                isItalic = false;
                isLineStart = true;
            }
            else if (tokenType == AcadownLexer.SPACE) {
                // 空格不改变行首状态
            }
            else {
                isLineStart = false;
            }

            // 基础样式映射
            String baseStyle = getStyleClass(tokenType);
            if (baseStyle != null) {
                styles.add(baseStyle);
            }

            // 样式叠加 (在粗体/斜体内部的普通文字)
            if (baseStyle == null && !styles.contains("list-marker") && !styles.contains("image-marker")) {
                if (isBold) styles.add("bold");
                if (isItalic) styles.add("italic");
            }

            spansBuilder.add(styles.isEmpty() ? Collections.emptyList() : styles, tokenEnd - tokenStart);
            lastTokenEnd = tokenEnd;
        }

        // 填充剩余文本
        if (lastTokenEnd < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastTokenEnd);
        }

        return spansBuilder.create();
    }

    /**
     * 计算错误高亮 (Error Layer)
     */
    public static StyleSpans<Collection<String>> computeErrorHighlighting(String text, List<Diagnostic> diagnostics) {
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        int lastEnd = 0;

        diagnostics.sort((a, b) -> Integer.compare(a.startIndex, b.startIndex));

        for (Diagnostic d : diagnostics) {
            int start = Math.max(0, d.startIndex);

            int end = Math.max(start + 1, d.endIndex);

            if (end > text.length()) {
                end = text.length();
                if (start >= end && end > 0) {
                    start = end - 1;
                }
            }

            if (start >= end) continue;

            // 填充错误前的空白
            if (start > lastEnd) {
                builder.add(Collections.emptyList(), start - lastEnd);
            }

            // 添加错误样式
            builder.add(Collections.singleton("error-marker"), end - start);
            lastEnd = end;
        }

        // 填充剩余空白
        if (lastEnd < text.length()) {
            builder.add(Collections.emptyList(), text.length() - lastEnd);
        }

        return builder.create();
    }

    /**
     * Token 类型到 CSS 类名的静态映射
     */
    private static String getStyleClass(int tokenType) {
        switch (tokenType) {
            case AcadownLexer.YAML_BLOCK:
                return "yaml-marker";

            case AcadownLexer.H1:
            case AcadownLexer.H2:
            case AcadownLexer.H3:
            case AcadownLexer.H4:
            case AcadownLexer.H5:
            case AcadownLexer.H6:
                return "header";

            case AcadownLexer.CODE_BLOCK: return "code-block";
            case AcadownLexer.INLINE_CODE: return "inline-code";

            case AcadownLexer.BLOCK_MATH:
            case AcadownLexer.INLINE_MATH: return "math";

            case AcadownLexer.GT: return "blockquote";

            case AcadownLexer.LBRACKET:
            case AcadownLexer.RBRACKET:
            case AcadownLexer.LPAREN:
            case AcadownLexer.RPAREN:
                return "link-marker";

            default: return null;
        }
    }
}