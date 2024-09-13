package io.github.kabanfriends.smoothfontgen;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import io.github.kabanfriends.smoothfontgen.config.Config;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class GlyphPage {

    private static final float POINTS_TO_SCALE = 1 / 11.75F;
    private static final float POINTS_TO_PIXELS = 96 / 72F;
    private static final float ONE_PIXEL = 1 / 64F;
    private static final BufferedImage EMPTY_IMAGE;

    static {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.dispose();

        EMPTY_IMAGE = image;
    }

    private final SmoothGenerator main;
    private final ExecutorService executor;
    private final int pageId;

    public GlyphPage(SmoothGenerator generator, int pageId, int threads) {
        this.main = generator;
        this.executor = Executors.newFixedThreadPool(threads);
        this.pageId = pageId;
    }

    public void generate() {
        char start = (char) (pageId * 0x100);

        //noinspection unchecked
        Future<Glyph>[] results = (Future<Glyph>[]) Array.newInstance(Future.class, 0x100);

        for (int i = 0; i < 0x100; i++) {
            char id = (char) (start + i);

            String argStr = main.getConfig().get(Config.ADDITIONAL_ARGS);
            List<String> additionalArgs = argStr.isEmpty() ? Collections.emptyList() : Arrays.asList(argStr.split(" "));

            Callable<Glyph> task = () -> {
                WrappedFont font = main.getFontHolder().getFirstFont(id);
                float width = (font.getWidth(id) * POINTS_TO_PIXELS + font.getFontInfo().padding()) * ONE_PIXEL * font.getFontInfo().widthScale();

                if (id == 0x00) {
                    return new Glyph(id, width, EMPTY_IMAGE);
                }

                Path fontPath = Paths.get("fonts/" + font.getFontInfo().filename());
                String outFilename = "msdfgen/out/out_" + String.format("%04X", (int) id) + ".png";

                List<String> args = new ArrayList<>(Arrays.asList(
                        "msdfgen/msdfgen",
                        "mtsdf",
                        "-font",
                        fontPath.toString(),
                        String.format("0x%04X", (int) id),
                        "-dimensions",
                        "64",
                        "64",
                        "-scale",
                        Float.toString(font.getFontInfo().fontSize() * POINTS_TO_SCALE),
                        "-o",
                        outFilename
                ));
                args.addAll(additionalArgs);

                NuProcessBuilder processBuilder = new NuProcessBuilder(args);
                processBuilder.setProcessListener(new ProcessHandler());

                try {
                    NuProcess process = processBuilder.start();
                    int exitCode = process.waitFor(1, TimeUnit.MINUTES);
                    if (exitCode != 0) {
                        Exception e = new RuntimeException("Process exited with exit code " + exitCode);
                        Logger.getInstance().error("msdfgen for {} failed", String.format("%04X", (int) id), e);
                        throw e;
                    }

                    File imageFile = new File(outFilename);
                    BufferedImage image = ImageIO.read(imageFile);
                    imageFile.delete();

                    return new Glyph(id, width, image);
                } catch (InterruptedException | IOException e) {
                    Logger.getInstance().error("msdfgen for {} failed", String.format("%04X", (int) id), e);
                    throw e;
                }
            };
            results[i] = executor.submit(task);
        }

        executor.shutdown();

        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        ByteBuffer buffer = ByteBuffer.allocate(0x404);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Header
        for (int i = 0; i < 4; i++) {
            buffer.put((byte) 0x00);
        }

        for (int i = 0; i < results.length; i++) {
            int x = i % 0x10;
            int y = i / 0x10;
            try {
                Glyph glyph = results[i].get();
                graphics.drawImage(glyph.image(), x * 64, y * 64, null);
                buffer.putFloat(glyph.width());
            } catch (CancellationException | ExecutionException | InterruptedException e) {
                Logger.getInstance().error("Glyph generation task failed", e);
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream("smooth/smooth_" + String.format("%02X", pageId) + ".fontdata")) {
            ImageIO.write(image, "PNG", new File("smooth/smooth_" + String.format("%02X", pageId) + ".png"));
            outputStream.write(buffer.array());
        } catch (IOException e) {
            Logger.getInstance().error("Failed to write font page {}", String.format("%02X", pageId), e);
        }
    }

    static class Glyph {

        private final char charId;
        private final float width;
        private final BufferedImage image;

        Glyph(char charId, float width, BufferedImage image) {
            this.charId = charId;
            this.width = width;
            this.image = image;
        }

        public char charId() {
            return charId;
        }

        public float width() {
            return width;
        }

        public BufferedImage image() {
            return image;
        }
    }
}
