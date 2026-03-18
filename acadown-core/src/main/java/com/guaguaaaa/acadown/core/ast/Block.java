package com.guaguaaaa.acadown.core.ast;

/**
 * Represents an abstract block-level element in the Pandoc AST.
 * All block-level elements, such as paragraphs, headers, and lists, extend this class.
 */
public abstract class Block implements PandocNode {
    /** The type of the block element (e.g., "Para", "Header"). */
    public final String t;
    /** The content of the block element. The type of this object depends on 't'. */
    public final Object c;

    /**
     * Constructs a new Block node.
     * @param t The type of the block.
     * @param c The content of the block.
     */
    protected Block(String t, Object c) {
        this.t = t;
        this.c = c;
    }
}