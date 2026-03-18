package com.guaguaaaa.acadown.core.ast;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Image extends Inline {
    /**
     * @param altText  图片的替代文本 (List<Inline>)
     * @param url      图片路径
     */
    public Image(List<Inline> altText, String url) {
        super("Image", Arrays.asList(
                Arrays.asList("", Collections.emptyList(), Collections.emptyList()),
                altText,
                Arrays.asList(url, "fig:")
        ));
    }
}