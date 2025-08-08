package io.github.kabanfriends.smoothfontgen.font.pixel;

import com.google.gson.*;
import io.github.kabanfriends.smoothfontgen.ImageUtil;
import io.github.kabanfriends.smoothfontgen.config.PixelFontInfo;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PixelWrappedFont implements WrappedFont<PixelFontInfo> {

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    protected final Image fullImage;
    protected final PixelFontInfo info;

    private final Map<Character, Image> glyphs;
    private final Map<Character, Integer> glyphWidths;
    private final int gridWidth;
    private final int gridHeight;

    protected PixelWrappedFont(PixelFontInfo parent) {
        this.info = new PixelFontInfo("#missing", parent.internalPadding(), parent.padding(), parent.scale(), parent.spaceWidth(), parent.additionalArgs());
        this.fullImage = ImageUtil.loadImage(Objects.requireNonNull(getClass().getResource("/missing_char.png")));
        this.gridWidth = 8;
        this.gridHeight = 8;
        this.glyphs = new HashMap<>();
        this.glyphWidths = new HashMap<>();
    }

    public PixelWrappedFont(PixelFontInfo info, File jsonFile, File pngFile) throws IOException {
        this.info = info;
        this.fullImage = ImageIO.read(pngFile);

        JsonObject json = JsonParser.parseReader(GSON.newJsonReader(new FileReader(jsonFile))).getAsJsonObject();
        JsonArray charSetArray = json.getAsJsonArray("chars");
        int length = -1;
        for (int i = 0; i < charSetArray.size(); i++) {
            String chars = charSetArray.get(i).getAsString();
            if (length != -1 && chars.length() != length) {
                throw new IllegalArgumentException("All character sets must have the same length");
            }
            length = chars.length();
        }

        this.glyphs = new HashMap<>();
        this.gridWidth = fullImage.getWidth(null) / length;
        this.gridHeight = fullImage.getHeight(null) / charSetArray.size();
        for (int y = 0; y < charSetArray.size(); y++) {
            String chars = charSetArray.get(y).getAsString();
            for (int x = 0; x < chars.length(); x++) {
                char c = chars.charAt(x);
                int glyphX = x * gridWidth;
                int glyphY = y * gridHeight;
                BufferedImage image = ((BufferedImage) fullImage).getSubimage(glyphX, glyphY, gridWidth, gridHeight);
                glyphs.put(c, image);
            }
        }

        this.glyphWidths = new HashMap<>();
        for (Map.Entry<Character, Image> entry : glyphs.entrySet()) {
            char c = entry.getKey();
            BufferedImage image = (BufferedImage) entry.getValue();
            int maxWidth = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                int width = 0;
                for (int x = 0; x < image.getWidth(); x++) {
                    if ((image.getRGB(x, y) & 0xFF000000) != 0) {
                        width = x;
                    }
                }
                maxWidth = Math.max(maxWidth, width);
            }

            glyphWidths.put(c, maxWidth);
        }
    }

    @Override
    public PixelFontInfo getFontInfo() {
        return info;
    }

    @Override
    public float getWidth(char index) {
        int width = glyphWidths.getOrDefault(index, 0);
        if (index == ' ') {
            width = info.spaceWidth();
        }
        return (width + 1 + info.internalPadding()) / (float) gridWidth * info.scale();
    }

    @Override
    public boolean hasGlyph(char index) {
        return glyphs.containsKey(index);
    }

    protected Image getGlyphImage(char index) {
        return glyphs.get(index);
    }

    @Override
    public List<String> processGlyph(char id, String outFilename) {
        String hexId = String.format("%04X", (int) id);
        double renderScale = 64.0 / gridWidth * info.scale();
        Image image = getGlyphImage(id);
        String shapeDesc = ShapeDescGenerator.imageToShapeDesc(image);

        File descFile = new File("msdfgen/out/shape_" + hexId + ".txt");
        try {
            descFile.createNewFile();
            try (FileWriter writer = new FileWriter(descFile)) {
                writer.write(shapeDesc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create file for shape description file", e);
        }

        return new ArrayList<>(Arrays.asList(
                "msdfgen/msdfgen",
                "mtsdf",
                "-shapedesc",
                descFile.toPath().toString(),
                "-dimensions",
                "64",
                "64",
                "-scale",
                Double.toString(renderScale),
                "-o",
                outFilename
        ));
    }

    @Override
    public void postCleanup(char id, String outFilename) {
        WrappedFont.super.postCleanup(id, outFilename);

        String hexId = String.format("%04X", (int) id);
        File descFile = new File("msdfgen/out/shape_" + hexId + ".txt");
        descFile.delete();
    }
}
