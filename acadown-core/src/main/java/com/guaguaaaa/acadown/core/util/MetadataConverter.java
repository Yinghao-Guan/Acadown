package com.guaguaaaa.acadown.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.yaml.snakeyaml.Yaml;
import java.util.List;
import java.util.Map;

public class MetadataConverter {

    public static JsonObject parseYamlToPandocMeta(String yamlContent) {
        Yaml yaml = new Yaml();
        String cleanYaml = yamlContent;
        if (cleanYaml.startsWith("---")) {
            cleanYaml = cleanYaml.substring(3);
        }
        if (cleanYaml.endsWith("---")) {
            cleanYaml = cleanYaml.substring(0, cleanYaml.length() - 3);
        }

        try {
            Map<String, Object> yamlMap = yaml.load(cleanYaml);
            JsonObject metaRoot = new JsonObject();

            if (yamlMap != null) {
                for (Map.Entry<String, Object> entry : yamlMap.entrySet()) {
                    metaRoot.add(entry.getKey(), convertToPandocNode(entry.getKey(), entry.getValue()));
                }
            }
            return metaRoot;
        } catch (Exception e) {
            System.err.println("YAML Parsing Error: " + e.getMessage());
            return new JsonObject();
        }
    }

    /**
     * 递归转换 YAML 对象为 Pandoc JSON AST 节点
     * @param key 当前字段的 Key（上下文），用于判断是否需要特殊处理（如 Raw LaTeX）
     * @param obj 值
     */
    private static JsonObject convertToPandocNode(String key, Object obj) {
        JsonObject node = new JsonObject();

        if (obj instanceof Boolean) {
            node.addProperty("t", "MetaBool");
            node.addProperty("c", (Boolean) obj);
        }
        else if (obj instanceof List) {
            node.addProperty("t", "MetaList");
            JsonArray listArray = new JsonArray();
            for (Object item : (List<?>) obj) {
                listArray.add(convertToPandocNode(key, item));
            }
            node.add("c", listArray);
        }
        else if (obj instanceof Map) {
            node.addProperty("t", "MetaMap");
            JsonObject mapObj = new JsonObject();
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                mapObj.add(entry.getKey().toString(), convertToPandocNode(entry.getKey().toString(), entry.getValue()));
            }
            node.add("c", mapObj);
        }
        else {
            String text = String.valueOf(obj);

            if ("header-includes".equals(key)) {

                JsonObject rawInline = new JsonObject();
                rawInline.addProperty("t", "RawInline");
                JsonArray cContent = new JsonArray();
                cContent.add("latex"); // 指定格式为 latex
                cContent.add(text);
                rawInline.add("c", cContent);

                JsonArray inlines = new JsonArray();
                inlines.add(rawInline);

                node.addProperty("t", "MetaInlines");
                node.add("c", inlines);
            }
            else {
                node.addProperty("t", "MetaString");
                node.addProperty("c", text);
            }
        }
        return node;
    }
}