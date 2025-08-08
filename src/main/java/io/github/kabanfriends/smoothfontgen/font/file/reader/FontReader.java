package io.github.kabanfriends.smoothfontgen.font.file.reader;

import io.github.kabanfriends.smoothfontgen.config.FileFontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

import java.io.File;
import java.io.IOException;

public interface FontReader<T extends WrappedFont> {

    String extension();

    T parse(FileFontInfo fileFontInfo, File file) throws IOException;
}
