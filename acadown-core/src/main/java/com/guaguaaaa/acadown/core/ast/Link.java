package com.guaguaaaa.acadown.core.ast;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Link extends Inline {
    /**
     * @param content  链接显示的文字 (List<Inline>)
     * @param url      跳转目标 URL
     */
    public Link(List<Inline> content, String url) {
        super("Link", Arrays.asList(
                Arrays.asList("", Collections.emptyList(), Collections.emptyList()),
                content,
                Arrays.asList(url, "")
        ));
    }
}