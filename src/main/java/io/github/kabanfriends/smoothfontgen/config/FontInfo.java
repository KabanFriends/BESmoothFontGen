package io.github.kabanfriends.smoothfontgen.config;

public final class FontInfo {

    private final String filename;
    private final float fontSize;
    private final float padding;
    private final String additionalArgs;

    public FontInfo(String filename, float fontSize, float padding, String additionalArgs) {
        this.filename = filename;
        this.fontSize = fontSize;
        this.padding = padding;
        this.additionalArgs = additionalArgs;
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

    public String getAdditionalArgs() {
        return additionalArgs;
    }
}
