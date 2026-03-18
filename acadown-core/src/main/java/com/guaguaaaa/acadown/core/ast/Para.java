package com.guaguaaaa.acadown.core.ast;

import java.util.List;

// Represents a paragraph node in the Pandoc AST.
public class Para extends Block {
    /**
     * Constructs a new Para node.
     *
     * @param content A list of inline elements that form the paragraph's content.
     */
    public Para(List<Inline> content) {
        super("Para", content);
    }
}