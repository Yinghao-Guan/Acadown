package com.guaguaaaa.acadown.core.ast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Pandoc 的 Cite 节点结构：
// { "t": "Cite", "c": [ [Citation Objects], [Inline Objects (fallback)] ] }
public class Cite extends Inline {
    public Cite(String citationId) {
        super("Cite", Arrays.asList(
                Collections.singletonList(new Citation(citationId)),
                Collections.singletonList(new Str("[@" + citationId + "]"))
        ));
    }
}