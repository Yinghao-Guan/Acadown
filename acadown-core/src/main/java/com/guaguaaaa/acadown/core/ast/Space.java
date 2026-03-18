package com.guaguaaaa.acadown.core.ast;

// Represents a space node in the Pandoc AST.
// A Space node does not contain any content.
public class Space extends Inline {
    // Constructs a new Space node.
    public Space() { super("Space", null); } // Pandoc的Space节点没有内容
}