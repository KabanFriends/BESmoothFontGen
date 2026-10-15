package io.github.kabanfriends.smoothfontgen.font;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;

import java.io.File;
import java.util.List;

public interface WrappedFont<T extends FontInfo<?>> {

    T getFontInfo();

    float getWidth(char index);

    boolean hasGlyph(char index);

    List<String> processGlyph(char id, String outFilename);

    /**
     * Identify the texture this font draws for a character. Two characters with equal keys
     * produce identical texture cells, so one cell can serve both.
     */
    String renderKey(char id);

    default void postCleanup(char id, String outFilename) {
        File imageFile = new File(outFilename);
        imageFile.delete();
    }
}
