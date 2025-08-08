package io.github.kabanfriends.smoothfontgen.font.file;

import io.github.kabanfriends.smoothfontgen.config.FileFontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;
import org.apache.fontbox.FontBoxFont;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface FileWrappedFont extends WrappedFont<FileFontInfo> {

    float POINTS_TO_SCALE = 1 / 11.75F;

    FontBoxFont getFont();

    float getCorrectionFactor();

    @Override
    default List<String> processGlyph(char id, String outFilename) {
        Path fontPath = Paths.get("fonts/" + getFontInfo().filename());

        return new ArrayList<>(Arrays.asList(
                "msdfgen/msdfgen",
                "mtsdf",
                "-font",
                fontPath.toString(),
                String.format("0x%04X", (int) id),
                "-dimensions",
                "64",
                "64",
                "-scale",
                Float.toString(getFontInfo().fontSize() * getCorrectionFactor() * POINTS_TO_SCALE),
                "-o",
                outFilename
        ));
    }
}
