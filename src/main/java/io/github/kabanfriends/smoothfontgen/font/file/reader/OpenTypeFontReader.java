package io.github.kabanfriends.smoothfontgen.font.file.reader;

import io.github.kabanfriends.smoothfontgen.config.FileFontInfo;
import io.github.kabanfriends.smoothfontgen.font.file.OpenTypeWrappedFont;
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
    public OpenTypeWrappedFont parse(FileFontInfo fileFontInfo, File file) throws IOException {
        OTFParser parser = new OTFParser();
        return new OpenTypeWrappedFont(fileFontInfo, parser.parse(new RandomAccessReadBufferedFile(file)));
    }
}
