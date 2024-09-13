package io.github.kabanfriends.smoothfontgen.config;

public final class FontInfo {

    private final String filename;
    private final float fontSize;
    private final float padding;

    public FontInfo(String filename, float fontSize, float padding) {
        this.filename = filename;
        this.fontSize = fontSize;
        this.padding = padding;
    }

    public String filename() {
        return filename;
    }

    public float fontSize() {
        return fontSize;
    }

    public float padding() {
        return padding;
    }
}
