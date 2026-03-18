package com.guaguaaaa.acadown.core.ast;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class PandocAst implements PandocNode {
    @SerializedName("pandoc-api-version")
    private final List<Integer> pandocApiVersion = List.of(1, 23, 1);

    private final Map<String, Object> meta;
    private final List<Block> blocks;

    public PandocAst(Map<String, Object> meta, List<Block> blocks) {
        this.meta = meta;
        this.blocks = blocks;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}