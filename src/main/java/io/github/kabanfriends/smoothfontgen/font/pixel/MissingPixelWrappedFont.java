package io.github.kabanfriends.smoothfontgen.font.pixel;

import io.github.kabanfriends.smoothfontgen.config.PixelFontInfo;

import java.awt.*;

public class MissingPixelWrappedFont extends PixelWrappedFont {

    public MissingPixelWrappedFont(PixelFontInfo info) {
        super(info);
    }

    @Override
    public boolean hasGlyph(char index) {
        return true;
    }

    @Override
    public float getWidth(char index) {
        return (5 + info.internalPadding()) / (float) 8 * info.scale();
    }

    @Override
    protected Image getGlyphImage(char index) {
        return fullImage;
    }
}
