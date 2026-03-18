package com.guaguaaaa.acadown.core.ast;
import java.util.List;

public class BlockQuote extends Block {
    public BlockQuote(List<Block> content) {
        super("BlockQuote", content);
    }
}