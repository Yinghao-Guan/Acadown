package com.guaguaaaa.acadown.core.ast;

// Represents a plain text string node in the Pandoc AST.
public class Str extends Inline {
    /**
     * Constructs a new Str node.
     *
     * @param text The plain text content.
     */
    public Str(String text) {
        super("Str", text);
    }
}