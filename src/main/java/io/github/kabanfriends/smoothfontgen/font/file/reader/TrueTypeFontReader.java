package io.github.kabanfriends.smoothfontgen.font.file.reader;

import io.github.kabanfriends.smoothfontgen.config.FileFontInfo;
import io.github.kabanfriends.smoothfontgen.font.file.TrueTypeWrappedFont;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import java.io.File;
import java.io.IOException;

public class TrueTypeFontReader implements FontReader<TrueTypeWrappedFont> {

    @Override
    public String extension() {
        return "ttf";
    }

    @Override
    public TrueTypeWrappedFont parse(FileFontInfo fileFontInfo, File file) throws IOException {
        TTFParser parser = new TTFParser();
        return new TrueTypeWrappedFont(fileFontInfo, parser.parse(new RandomAccessReadBufferedFile(file)));
    }
}
