package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.Logger;
import io.github.kabanfriends.smoothfontgen.font.pixel.PixelWrappedFont;

import java.io.File;

public final class PixelFontInfo implements FontInfo<PixelWrappedFont> {

    private final String name;
    private final float padding;
    private final float externalPadding;
    private final float scale;
    private final float spaceWidth;
    private final String widthOverride;
    private final String additionalArgs;

    public PixelFontInfo(String name, float padding, float externalPadding, float scale, float spaceWidth, String widthOverride, String additionalArgs) {
        this.name = name;
        this.padding = padding;
        this.externalPadding = externalPadding;
        this.scale = scale;
        this.spaceWidth = spaceWidth;
        this.widthOverride = widthOverride;
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

    public float spaceWidth() {
        return spaceWidth;
    }

    public String widthOverride() {
        return widthOverride;
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
            PixelWrappedFont font = new PixelWrappedFont(this, jsonFile, pngFile);
            Logger.getInstance().info("Loaded pixel font: {} (W:{} H:{} RS:{})", name, font.getGridWidth(), font.getGridHeight(), font.getRenderScale());
            return font;
        } catch (Throwable e) {
            Logger.getInstance().error("Failed to load pixel font {}", name, e);
            return null;
        }
    }
}
