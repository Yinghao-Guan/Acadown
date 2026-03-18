package com.guaguaaaa.acadown.core.parser;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PandocAstVisitorTest {

    @Test
    void visitDocument_shouldCreatePandocAstWithMetadataAndBlocks() {
        String input = "---\n" +
                "title: Test Doc\n" +
                "---\n" +
                "# Header 1\n" +
                "Hello World";

        AcadownLexer lexer = new AcadownLexer(CharStreams.fromString(input));
        AcadownParser parser = new AcadownParser(new CommonTokenStream(lexer));

        // 1. 修改点：调用 doc() 而不是 document()
        ParseTree tree = parser.doc();

        PandocAstVisitor visitor = new PandocAstVisitor();
        visitor.visit(tree);

        // 2. 修改点：不再检查返回值，而是检查生成的 JSON
        String json = visitor.getPandocJson();
        assertNotNull(json);

        // 简单的验证 JSON 结构
        Gson gson = new Gson();
        JsonObject root = gson.fromJson(json, JsonObject.class);

        assertTrue(root.has("pandoc-api-version"));
        assertTrue(root.has("meta"));
        assertTrue(root.has("blocks"));

        // 验证 Metadata 解析正确
        JsonObject meta = root.getAsJsonObject("meta");
        assertTrue(meta.has("title"));
        assertEquals("Test Doc", meta.getAsJsonObject("title").get("c").getAsString());
    }
}