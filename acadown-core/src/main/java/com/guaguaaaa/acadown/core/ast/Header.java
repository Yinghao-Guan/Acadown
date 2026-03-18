package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a header node in the Pandoc AST.
 * <p>
 * The full structure in Pandoc is:
 * {@code [level, [identifier, [classes], [[key, val], ...]], [Inline, ...]]}
 * This implementation simplifies it to only handle the level and content.
 */
public class Header extends Block {
    /**
     * Constructs a new Header node.
     *
     * @param level   The heading level (1-6).
     * @param content A list of inline elements that make up the header's text.
     */
    public Header(int level, List<Inline> content) {
        super("Header", Arrays.asList(
                level,
                Arrays.asList("", Collections.emptyList(), Collections.emptyList()),
                content
        ));
    }
}