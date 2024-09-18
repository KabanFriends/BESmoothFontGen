package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;
import io.github.kabanfriends.smoothfontgen.font.reader.FontTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FontHolder {

    private final List<WrappedFont> fonts;

    public FontHolder(FontInfo[] fontInfoArray) {
        this.fonts = new ArrayList<>(fontInfoArray.length);

        for (int i = 0; i < fontInfoArray.length; i++) {
            File file = new File("fonts/" + fontInfoArray[i].filename());
            if (!file.exists()) {
                Logger.getInstance().warn("Font file {} was not found, skipping", fontInfoArray[i].filename());
                continue;
            }

            WrappedFont font = FontTypes.parse(fontInfoArray[i], file);
            if (font != null) {
                Logger.getInstance().info("Font loaded: {}", file.getName());
                fonts.add(font);
            }
        }
    }

    public WrappedFont getFirstFont(char index) {
        for (WrappedFont font : fonts) {
            // TODO: fix detection
            if (font.hasGlyph(index)) {
                return font;
            }
        }
        return fonts.get(fonts.size() - 1);
    }

    public int getFontCount() {
        return fonts.size();
    }
}
