package com.guaguaaaa.acadown.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CslGenerator {

    public static String generateCslXml(String template) {
        StringBuilder csl = new StringBuilder();
        csl.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        csl.append("<style xmlns=\"http://purl.org/net/xbiblio/csl\" class=\"in-text\" version=\"1.0\" demote-non-dropping-particle=\"sort-only\">\n");
        csl.append("  <info>\n");
        csl.append("    <title>Acadown Custom Style</title>\n");
        csl.append("    <id>http://www.zotero.org/styles/acadown-custom</id>\n");
        csl.append("    <updated>2023-01-01T00:00:00+00:00</updated>\n");
        csl.append("  </info>\n");

        csl.append("  <citation>\n");
        csl.append("    <layout prefix=\"(\" suffix=\")\" delimiter=\"; \">\n");
        csl.append("      <names variable=\"author\">\n");
        csl.append("        <name form=\"short\" and=\"text\" delimiter=\", \"/>\n");
        csl.append("      </names>\n");
        csl.append("      <date variable=\"issued\" prefix=\", \">\n");
        csl.append("        <date-part name=\"year\"/>\n");
        csl.append("      </date>\n");
        csl.append("    </layout>\n");
        csl.append("  </citation>\n");

        csl.append("  <bibliography>\n");
        csl.append("    <layout suffix=\".\">\n");
        csl.append(parseTemplate(template));
        csl.append("    </layout>\n");
        csl.append("  </bibliography>\n");
        csl.append("</style>\n");

        return csl.toString();
    }

    private static String parseTemplate(String template) {
        // 先处理粗体 **text** -> <group font-weight="bold">text</group>
        // 必须先处理双星号
        Pattern boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (boldMatcher.find()) {
            // 使用 quoteReplacement 防止内容中的 $ 等特殊字符导致报错
            boldMatcher.appendReplacement(sb, "<group font-weight=\"bold\">" + Matcher.quoteReplacement(boldMatcher.group(1)) + "</group>");
        }
        boldMatcher.appendTail(sb);
        String processed = sb.toString();

        // 处理斜体 *text* -> <group font-style="italic">text</group>
        Pattern italicPattern = Pattern.compile("\\*(.*?)\\*");
        Matcher italicMatcher = italicPattern.matcher(processed);
        sb = new StringBuffer();
        while (italicMatcher.find()) {
            italicMatcher.appendReplacement(sb, "<group font-style=\"italic\">" + Matcher.quoteReplacement(italicMatcher.group(1)) + "</group>");
        }
        italicMatcher.appendTail(sb);
        processed = sb.toString();

        // 变量映射定义
        String[][] replacements = {
                {"{author}", "<names variable=\"author\"><name name-as-sort-order=\"all\" sort-separator=\", \" initialize-with=\". \" delimiter=\", \"/></names>"},
                {"{year}", "<date variable=\"issued\"><date-part name=\"year\"/></date>"},
                {"{title}", "<text variable=\"title\"/>"},
                {"{journal}", "<text variable=\"container-title\"/>"},
                {"{volume}", "<text variable=\"volume\"/>"},
                {"{issue}", "<text variable=\"issue\"/>"},
                {"{page}", "<text variable=\"page\"/>"},
                {"{doi}", "<text variable=\"DOI\"/>"}
        };

        // 执行变量替换
        for (String[] pair : replacements) {
            processed = processed.replace(pair[0], pair[1]);
        }

        // 包裹普通文本
        return wrapPlainText(processed);
    }

    private static String wrapPlainText(String input) {
        StringBuilder result = new StringBuilder();
        int len = input.length();
        int i = 0;
        StringBuilder currentText = new StringBuilder();

        while (i < len) {
            char c = input.charAt(i);
            if (c == '<') {
                if (currentText.length() > 0) {
                    result.append("<text value=\"").append(escapeXml(currentText.toString())).append("\"/>");
                    currentText.setLength(0);
                }
                int endTag = input.indexOf('>', i);
                if (endTag != -1) {
                    result.append(input, i, endTag + 1);
                    i = endTag + 1;
                    continue;
                }
            }
            currentText.append(c);
            i++;
        }
        if (currentText.length() > 0) {
            result.append("<text value=\"").append(escapeXml(currentText.toString())).append("\"/>");
        }
        return result.toString();
    }

    private static String escapeXml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}