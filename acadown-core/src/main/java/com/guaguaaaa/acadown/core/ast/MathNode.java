package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;

// Represents a math node (inline or display) in the Pandoc AST.
public class MathNode extends Inline {

    // Helper inner class for generating the `{"t": "TypeName"}` JSON object format.
    private static class MathTypeObject {
        private final String t;

        public MathTypeObject(String t) {
            this.t = t;
        }
    }

    // Defines the types of math nodes supported.
    public enum MathType {
        INLINE_MATH("InlineMath"),
        DISPLAY_MATH("DisplayMath");

        private final String pandocName;

        MathType(String pandocName) {
            this.pandocName = pandocName;
        }

        public String getPandocName() {
            return pandocName;
        }
    }

    /**
     * Constructs a new MathNode.
     * <p>
     * The structure in Pandoc AST is: `[{"t": "MathType"}, "equation text"]`.
     *
     * @param type The type of the math node (inline or display).
     * @param text The mathematical equation as a string.
     */
    public MathNode(MathType type, String text) {
        super("Math", Arrays.asList(new MathTypeObject(type.getPandocName()), text));
    }
}