package io.github.kabanfriends.smoothfontgen.font;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;

import java.io.File;
import java.util.List;

public interface WrappedFont<T extends FontInfo<?>> {

    T getFontInfo();

    float getWidth(char index);

    boolean hasGlyph(char index);

    List<String> processGlyph(char id, String outFilename);

    default void postCleanup(char id, String outFilename) {
        File imageFile = new File(outFilename);
        imageFile.delete();
    }
}
