package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.List;

public class OrderedList extends Block {

    /**
     * Pandoc OrderedList JSON structure:
     * {
     * "t": "OrderedList",
     * "c": [ [start, style, delim], [items...] ]
     * }
     */
    public OrderedList(ListAttributes attrs, List<List<Block>> items) {
        super("OrderedList", Arrays.asList(attrs.toPandocStruct(), items));
    }
}