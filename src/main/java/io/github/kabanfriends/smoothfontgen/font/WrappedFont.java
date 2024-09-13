package io.github.kabanfriends.smoothfontgen.font;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import org.apache.fontbox.FontBoxFont;

public interface WrappedFont {

    FontInfo getFontInfo();

    FontBoxFont getFont();

    float getWidth(char index);

    float getCorrectionFactor();

    boolean hasGlyph(char index);
}
