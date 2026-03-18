package com.guaguaaaa.acadown.core.api;

public class Diagnostic {
    public final int line;
    public final int column;
    public final int startIndex;
    public final int endIndex;
    public final String message;

    public Diagnostic(int line, int column, int startIndex, int endIndex, String message) {
        this.line = line;
        this.column = column;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Line " + line + ":" + column + " " + message;
    }
}