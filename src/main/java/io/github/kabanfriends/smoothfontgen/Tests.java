package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.font.pixel.ShapeDescGenerator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Tests {

    private Tests() {
    }

    public static void testShapeDescGenerator() {
        try {
            File file = new File("char_8x8_4b.png");
            Image image = ImageIO.read(file);

            String desc = ShapeDescGenerator.imageToShapeDesc(image);
            Logger.getInstance().info(desc);

            File out = new File("shape.txt");
            try (FileWriter writer = new FileWriter(out)) {
                writer.write(desc);
            }
        } catch (IOException e) {
            Logger.getInstance().error("Failed to process image", e);
        }
    }
}
