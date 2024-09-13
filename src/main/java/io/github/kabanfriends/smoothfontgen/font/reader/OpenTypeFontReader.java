package io.github.kabanfriends.smoothfontgen.font.reader;

import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.font.OpenTypeWrappedFont;
import org.apache.fontbox.ttf.OTFParser;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;

import java.io.File;
import java.io.IOException;

public class OpenTypeFontReader implements FontReader<OpenTypeWrappedFont> {

    @Override
    public String extension() {
        return "otf";
    }

    @Override
    public OpenTypeWrappedFont parse(FontInfo fontInfo, File file) throws IOException {
        OTFParser parser = new OTFParser();
        return new OpenTypeWrappedFont(fontInfo, parser.parse(new RandomAccessReadBufferedFile(file)));
    }
}
