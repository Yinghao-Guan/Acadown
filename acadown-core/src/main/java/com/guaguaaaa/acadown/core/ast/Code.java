package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.Collections;

// inline code node in the Pandoc AST.
public class Code extends Inline {
    /**
     * Constructs a new Code node.
     *
     * @param content The text content of the inline code.
     */
    public Code(String content) {
        super("Code", Arrays.asList(
                Arrays.asList("", Collections.emptyList(), Collections.emptyList()),
                content
        ));
    }
}