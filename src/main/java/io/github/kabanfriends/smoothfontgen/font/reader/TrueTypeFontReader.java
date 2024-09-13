package io.github.kabanfriends.smoothfontgen.font.reader;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.font.TrueTypeWrappedFont;
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
    public TrueTypeWrappedFont parse(FontInfo fontInfo, File file) throws IOException {
        TTFParser parser = new TTFParser();
        return new TrueTypeWrappedFont(fontInfo, parser.parse(new RandomAccessReadBufferedFile(file)));
    }
}
