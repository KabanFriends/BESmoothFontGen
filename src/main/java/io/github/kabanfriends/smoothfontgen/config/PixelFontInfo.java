package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.Logger;
import io.github.kabanfriends.smoothfontgen.font.pixel.PixelWrappedFont;

import java.io.File;

public final class PixelFontInfo implements FontInfo<PixelWrappedFont> {

    private final String name;
    private final float padding;
    private final float externalPadding;
    private final float scale;
    private final int spaceWidth;
    private final String additionalArgs;

    public PixelFontInfo(String name, float padding, float externalPadding, float scale, int spaceWidth, String additionalArgs) {
        this.name = name;
        this.padding = padding;
        this.externalPadding = externalPadding;
        this.scale = scale;
        this.spaceWidth = spaceWidth;
        this.additionalArgs = additionalArgs;
    }

    @Override
    public String name() {
        return name;
    }

    public float internalPadding() {
        return padding;
    }

    @Override
    public float padding() {
        return externalPadding;
    }

    public float scale() {
        return scale;
    }

    public int spaceWidth() {
        return spaceWidth;
    }

    @Override
    public String additionalArgs() {
        return additionalArgs;
    }

    @Override
    public PixelWrappedFont loadFont() {
        File jsonFile = new File("pixel/" + name + ".json");
        File pngFile = new File("pixel/" + name + ".png");

        if (!jsonFile.exists() || !pngFile.exists()) {
            Logger.getInstance().warn("Pixel font files {}.(json|png) were not found", name);
            return null;
        }

        try {
            return new PixelWrappedFont(this, jsonFile, pngFile);
        } catch (Throwable e) {
            Logger.getInstance().error("Failed to load pixel font {}", name, e);
            return null;
        }
    }
}
