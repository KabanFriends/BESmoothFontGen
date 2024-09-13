package io.github.kabanfriends.smoothfontgen.font;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.TrueTypeFont;

import java.io.IOException;

public class TrueTypeWrappedFont implements WrappedFont {

    private final FontInfo fontInfo;
    private final TrueTypeFont font;

    public TrueTypeWrappedFont(FontInfo fontInfo, TrueTypeFont font) {
        this.fontInfo = fontInfo;
        this.font = font;
    }

    @Override
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    @Override
    public TrueTypeFont getFont() {
        return font;
    }

    @Override
    public float getWidth(char index) {
        try {
            CmapSubtable subTable = font.getCmap().getSubtable(3, 1);
            return font.getAdvanceWidth(subTable.getGlyphId(index)) * fontInfo.fontSize() * 96 / 72 / font.getUnitsPerEm() / 64;
        } catch (IOException ignored) {
            return 0F;
        }
    }

    @Override
    public float getCorrectionFactor() {
        try {
            return 1000F / font.getUnitsPerEm();
        } catch (IOException ignored) {
            return 1F;
        }
    }

    @Override
    public boolean hasGlyph(char index) {
        try {
            return font.hasGlyph(String.valueOf(index));
        } catch (IOException ignored) {
            return false;
        }
    }
}
