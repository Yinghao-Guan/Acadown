package com.guaguaaaa.acadown.core.ast;

import java.util.List;

// Represents an italic (emphasized) text node in the Pandoc AST.
public class Emph extends Inline {
    /**
     * Constructs a new Emph node.
     *
     * @param content A list of inline elements contained within the emphasis.
     */
    public Emph(List<Inline> content) {
        super("Emph", content);
    }
}