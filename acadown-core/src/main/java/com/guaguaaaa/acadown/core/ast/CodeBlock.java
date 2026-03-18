package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodeBlock extends Block {
    /**
     * Constructs a new CodeBlock node.
     * @param content The text content of the code.
     * @param language The language identifier (e.g., "java", "python"). Can be null or empty.
     */
    public CodeBlock(String content, String language) {
        super("CodeBlock", Arrays.asList(
                Arrays.asList(
                        "", // ID
                        (language != null && !language.isBlank())
                                ? Collections.singletonList(language)
                                : Collections.emptyList(),
                        Collections.emptyList() // Key-Value pairs
                ),
                content
        ));
    }
}