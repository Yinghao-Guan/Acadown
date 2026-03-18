package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.List;

public class RawBlock extends Block {

    public RawBlock(String format, String content) {
        // Pandoc JSON Structure for RawBlock: {"t": "RawBlock", "c": ["format", "content"]}
        super("RawBlock", Arrays.asList(format, content));
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