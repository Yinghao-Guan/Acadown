package com.guaguaaaa.acadown.core.ast;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Citation {
    public final String citationId;
    public final List<Inline> citationPrefix = Collections.emptyList();
    public final List<Inline> citationSuffix = Collections.emptyList();
    public final Map<String, String> citationMode = Map.of("t", "NormalCitation");
    public final int citationNoteNum = 1;
    public final int citationHash = 0;

    public Citation(String citationId) {
        this.citationId = citationId;
    }
}