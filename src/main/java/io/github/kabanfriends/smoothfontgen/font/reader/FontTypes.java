package io.github.kabanfriends.smoothfontgen.font.reader;

import io.github.kabanfriends.smoothfontgen.Logger;
import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class FontTypes {

    public static final Set<FontReader> FONT_READERS = Set.of(
            new TrueTypeFontReader(),
            new OpenTypeFontReader()
    );

    public static WrappedFont parse(FontInfo fontInfo, File file) {
        for (FontReader reader : FONT_READERS) {
            String extension = FilenameUtils.getExtension(file.getName()).toLowerCase(Locale.ROOT);
            if (extension.equalsIgnoreCase(reader.extension())) {
                try {
                    return reader.parse(fontInfo, file);
                } catch (IOException e) {
                    Logger.getInstance().error("Failed to read {} using {}", file.getName(), reader.getClass().getSimpleName(), e);
                }
            }
        }
        return null;
    }
}
