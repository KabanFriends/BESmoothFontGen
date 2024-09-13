package io.github.kabanfriends.smoothfontgen.font.reader;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

import java.io.File;
import java.io.IOException;

public interface FontReader<T extends WrappedFont> {

    String extension();

    T parse(FontInfo fontInfo, File file) throws IOException;
}
