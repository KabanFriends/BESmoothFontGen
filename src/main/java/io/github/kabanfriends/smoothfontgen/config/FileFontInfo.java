package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.Logger;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;
import io.github.kabanfriends.smoothfontgen.font.file.FileWrappedFont;
import io.github.kabanfriends.smoothfontgen.font.file.reader.FileFontTypes;

import java.io.File;

public final class FileFontInfo implements FontInfo<FileWrappedFont> {

    private final String filename;
    private final float fontSize;
    private final float padding;
    private final String additionalArgs;

    public FileFontInfo(String filename, float fontSize, float padding, String additionalArgs) {
        this.filename = filename;
        this.fontSize = fontSize;
        this.padding = padding;
        this.additionalArgs = additionalArgs;
    }

    @Override
    public String name() {
        return filename;
    }

    public String filename() {
        return filename;
    }

    public float fontSize() {
        return fontSize;
    }

    @Override
    public float padding() {
        return padding;
    }

    @Override
    public String additionalArgs() {
        return additionalArgs;
    }

    @Override
    public WrappedFont<?> loadFont() {
        File file = new File("fonts/" + filename);
        if (!file.exists()) {
            Logger.getInstance().warn("Font file {} was not found", filename);
            return null;
        }

        return FileFontTypes.parse(this, file);
    }
}
