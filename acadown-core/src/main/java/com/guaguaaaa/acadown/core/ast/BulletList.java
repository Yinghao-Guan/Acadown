package com.guaguaaaa.acadown.core.ast;

import java.util.List;

/*
 * Represents a bullet list node in the Pandoc AST.
 * In Pandoc, a BulletList's content is a list of list items, where each list item
 * is a list of Block elements.
 */
public class BulletList extends Block {
    /**
     * Constructs a new BulletList node.
     *
     * @param items A list of list items, with each item being a list of blocks.
     */
    public BulletList(List<List<Block>> items) {
        super("BulletList", items);
    }
}