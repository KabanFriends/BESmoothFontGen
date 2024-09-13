package io.github.kabanfriends.smoothfontgen.font;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.OpenTypeFont;

import java.io.IOException;

public class OpenTypeWrappedFont implements WrappedFont {

    private final FontInfo fontInfo;
    private final OpenTypeFont font;

    public OpenTypeWrappedFont(FontInfo fontInfo, OpenTypeFont font) {
        this.fontInfo = fontInfo;
        this.font = font;
    }

    @Override
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    @Override
    public OpenTypeFont getFont() {
        return font;
    }

    @Override
    public float getWidth(char index) {
        try {
            CmapSubtable subTable = font.getCmap().getSubtable(3, 1);
            return font.getAdvanceWidth(subTable.getGlyphId(index)) * fontInfo.fontSize() / font.getUnitsPerEm();
        } catch (IOException ignored) {
            return 0F;
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
