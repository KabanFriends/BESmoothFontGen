package io.github.kabanfriends.smoothfontgen.config;

public final class FontInfo {

    private final String filename;
    private final float fontSize;
    private final float padding;
    private final float widthScale;

    public FontInfo(String filename, float fontSize, float padding, float widthScale) {
        this.filename = filename;
        this.fontSize = fontSize;
        this.padding = padding;
        this.widthScale = widthScale;
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

    public float widthScale() {
        return widthScale;
    }
}
