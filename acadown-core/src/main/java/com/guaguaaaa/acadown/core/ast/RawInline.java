package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.List;

public class RawInline extends Inline {

    public RawInline(String format, String content) {
        // 必须调用父类构造函数来初始化 t 和 c
        super("RawInline", Arrays.asList(format, content));
    }

    @SuppressWarnings("unchecked")
    public String getFormat() {
        if (c instanceof List) {
            return ((List<String>) c).get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getContent() {
        if (c instanceof List) {
            return ((List<String>) c).get(1);
        }
        return null;
    }
}