package com.guaguaaaa.acadown.core.ast;

/**
 * Represents an abstract inline-level element in the Pandoc AST.
 * All inline-level elements, such as text, bold, and italic, extend this class.
 */
public abstract class Inline implements PandocNode {
    /** The type of the inline element (e.g., "Str", "Emph"). */
    public final String t;
    /** The content of the inline element. The type of this object depends on 't'. */
    public final Object c;

    /**
     * Constructs a new Inline node.
     * @param t The type of the inline element.
     * @param c The content of the inline element.
     */
    protected Inline(String t, Object c) {
        this.t = t;
        this.c = c;
    }
}