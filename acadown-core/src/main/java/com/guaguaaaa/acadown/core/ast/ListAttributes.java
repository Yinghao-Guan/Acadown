package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class ListAttributes {

    public enum Style {
        DefaultStyle, Example, Decimal, LowerRoman, UpperRoman, LowerAlpha, UpperAlpha
    }

    public enum Delim {
        DefaultDelim, Period, OneParen, TwoParens
    }

    public final int startNumber;
    public final Style style;
    public final Delim delim;

    public ListAttributes(int startNumber, Style style, Delim delim) {
        this.startNumber = startNumber;
        this.style = style;
        this.delim = delim;
    }

    /**
     * 将属性转换为符合 Pandoc JSON 规范的结构：
     * [Integer, {"t": "Style"}, {"t": "Delim"}]
     */
    public Object toPandocStruct() {
        return Arrays.asList(
                startNumber,
                toTag(style.name()),
                toTag(delim.name())
        );
    }

    private Map<String, String> toTag(String name) {
        return Collections.singletonMap("t", name);
    }
}