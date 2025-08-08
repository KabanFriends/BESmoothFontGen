package io.github.kabanfriends.smoothfontgen.font.file;

import io.github.kabanfriends.smoothfontgen.config.FileFontInfo;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.OpenTypeFont;

import java.io.IOException;

public class OpenTypeWrappedFont implements FileWrappedFont {

    private final FileFontInfo fileFontInfo;
    private final OpenTypeFont font;

    public OpenTypeWrappedFont(FileFontInfo fileFontInfo, OpenTypeFont font) {
        this.fileFontInfo = fileFontInfo;
        this.font = font;
    }

    @Override
    public FileFontInfo getFontInfo() {
        return fileFontInfo;
    }

    @Override
    public OpenTypeFont getFont() {
        return font;
    }

    @Override
    public float getWidth(char index) {
        try {
            CmapSubtable subTable = font.getCmap().getSubtable(3, 1);
            return font.getAdvanceWidth(subTable.getGlyphId(index)) * fileFontInfo.fontSize() * 96 / 72 / font.getUnitsPerEm() / 64;
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
            int glyphId = font.getCmap().getSubtable(3, 1).getGlyphId(index);
            String glyphName = font.getPostScript().getName(glyphId);
            return glyphName != null && font.hasGlyph(glyphName);
        } catch (IOException ignored) {
            return false;
        }
    }
}
