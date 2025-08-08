package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.config.PixelFontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;
import io.github.kabanfriends.smoothfontgen.font.file.reader.FileFontTypes;
import io.github.kabanfriends.smoothfontgen.font.pixel.MissingPixelWrappedFont;
import io.github.kabanfriends.smoothfontgen.font.pixel.PixelWrappedFont;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FontHolder {

    private final WrappedFont<?> fallbackFont;

    private final List<WrappedFont<?>> fonts;

    public FontHolder(FontInfo<?>[] fontInfoArray) {
        this.fonts = new ArrayList<>(fontInfoArray.length);

        for (int i = 0; i < fontInfoArray.length; i++) {
            WrappedFont<?> font = fontInfoArray[i].loadFont();
            if (font == null) {
                Logger.getInstance().warn("Font {} could not be loaded, skipping", fontInfoArray[i].name());
                continue;
            }
            fonts.add(font);
        }

        WrappedFont<?> first = fonts.get(0);
        if (first instanceof PixelWrappedFont) {
            this.fallbackFont = new MissingPixelWrappedFont(((PixelWrappedFont) first).getFontInfo());
        } else {
            this.fallbackFont = first;
        }
    }

    public WrappedFont<?> getFirstFont(char index) {
        for (WrappedFont<?> font : fonts) {
            if (font.hasGlyph(index)) {
                return font;
            }
        }
        return fallbackFont;
    }

    public int getFontCount() {
        return fonts.size();
    }
}
