package io.github.kabanfriends.smoothfontgen.config;

public final class WidthOverride {

    private final char from;
    private final char to;
    private final float width;

    public WidthOverride(char from, char to, float width) {
        this.from = from;
        this.to = to;
        this.width = width;
    }

    public char from() {
        return from;
    }

    public char to() {
        return to;
    }

    public float width() {
        return width;
    }
}
