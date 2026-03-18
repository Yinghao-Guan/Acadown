package com.guaguaaaa.acadown.core.util;

import com.guaguaaaa.acadown.core.ast.ListAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListMarker {
    // 匹配: (content) 或 content. 或 content)
    // Group 1: Leading Paren (optional)
    // Group 2: Content
    // Group 3: Trailing Delimiter (. or ))
    private static final Pattern PATTERN = Pattern.compile("^(\\()?([a-zA-Z0-9]+)(\\.|\\))?$");
    // Roman Validation Regex (Simple version for identification)
    private static final Pattern ROMAN_PATTERN = Pattern.compile("^[IVXLCDMivxlcdm]+$");

    public final ListAttributes.Style style;
    public final ListAttributes.Delim delim;
    public final int startNumber;

    // 是否是从 1 (或 i, a) 开始？用于判断是否是 "默认" 列表
    public final boolean isDefaultStart;

    private ListMarker(ListAttributes.Style style, ListAttributes.Delim delim, int startNumber) {
        this.style = style;
        this.delim = delim;
        this.startNumber = startNumber;
        this.isDefaultStart = (startNumber == 1);
    }

    public static ListMarker parse(String text) {
        text = text.trim();
        Matcher m = PATTERN.matcher(text);
        if (!m.find()) return null; // Should not happen if Lexer is correct

        String prefix = m.group(1); // "(" or null
        String content = m.group(2);
        String suffix = m.group(3); // "." or ")" or null (if prefix is present)

        // 1. Determine Delimiter
        ListAttributes.Delim delim;
        if (prefix != null && ")".equals(suffix)) {
            delim = ListAttributes.Delim.TwoParens; // (1)
        } else if (")".equals(suffix)) {
            delim = ListAttributes.Delim.OneParen; // 1)
        } else {
            delim = ListAttributes.Delim.Period;    // 1.
        }

        // 2. Determine Style & Value
        ListAttributes.Style style;
        int value;

        if (Character.isDigit(content.charAt(0))) {
            style = ListAttributes.Style.Decimal;
            value = Integer.parseInt(content);
        } else {

            boolean isRomanChar = ROMAN_PATTERN.matcher(content).matches();

            if (isRomanChar) {
                // Heuristic: "i", "v", "x" are Roman. "l", "c", "d", "m" are Alpha (unless part of multi-char)
                if (content.length() == 1) {
                    char c = content.toLowerCase().charAt(0);
                    if (c == 'i' || c == 'v' || c == 'x') {
                        style = isUpperCase(content) ? ListAttributes.Style.UpperRoman : ListAttributes.Style.LowerRoman;
                        value = parseRoman(content);
                    } else {
                        // l, c, d, m, a, b...
                        style = isUpperCase(content) ? ListAttributes.Style.UpperAlpha : ListAttributes.Style.LowerAlpha;
                        value = parseAlpha(content);
                    }
                } else {
                    // Multi-char Roman (e.g. "ii", "CI")
                    style = isUpperCase(content) ? ListAttributes.Style.UpperRoman : ListAttributes.Style.LowerRoman;
                    value = parseRoman(content);
                }
            } else {
                // Pure Alpha
                style = isUpperCase(content) ? ListAttributes.Style.UpperAlpha : ListAttributes.Style.LowerAlpha;
                value = parseAlpha(content);
            }
        }

        return new ListMarker(style, delim, value);
    }

    // Helper: is compatible?
    public boolean isCompatibleWith(ListMarker other) {
        if (other == null) return true; // + sign
        return this.style == other.style && this.delim == other.delim;
    }

    private static boolean isUpperCase(String s) {
        return Character.isUpperCase(s.charAt(0));
    }

    // 罗马数字字符值映射
    private static int getRomanValue(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> 0;
        };
    }

    /**
     * 解析罗马数字字符串为整数
     * 算法：遍历字符，如果当前值小于下一个字符的值，则是减法（如 IV: 1 < 5 -> -1），否则是加法。
     */
    private static int parseRoman(String roman) {
        if (roman == null || roman.isEmpty()) return 1;

        int sum = 0;
        int n = roman.length();

        for (int i = 0; i < n; i++) {
            int currentVal = getRomanValue(roman.charAt(i));

            // 检查下一个字符是否存在且值更大
            if (i + 1 < n) {
                int nextVal = getRomanValue(roman.charAt(i + 1));
                if (currentVal < nextVal) {
                    sum -= currentVal;
                } else {
                    sum += currentVal;
                }
            } else {
                sum += currentVal;
            }
        }

        // 兜底：如果解析结果 <= 0 (非标准输入)，默认返回 1
        return sum > 0 ? sum : 1;
    }

    private static int parseAlpha(String alpha) {
        // a=1, b=2
        char c = alpha.toLowerCase().charAt(0);
        return c - 'a' + 1;
    }
}