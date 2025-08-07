package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.Config;
import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.config.FontRange;

import java.io.File;

public class SmoothGenerator {

    private final Config config;
    private final FontHolder fontHolder;
    private final RemapHandler remapHandler;

    public SmoothGenerator() {
        this.config = new Config();
        config.load(new File("config.json"));

        FontInfo[] fontInfoArray = config.get(Config.FONTS);
        this.fontHolder = new FontHolder(fontInfoArray);
        this.remapHandler = new RemapHandler(config.get(Config.PAGE_REMAPPING));
    }

    public void start() {
        if (fontHolder.getFontCount() == 0) {
            Logger.getInstance().error("No valid font files were found, aborting");
            return;
        }

        Logger.getInstance().info("Writing remapping.dat");
        if (!config.get(Config.TEST_MODE)) {
            remapHandler.writeRemappingFile();
        }

        FontRange range = config.get(Config.RANGE);
        Logger.getInstance().info("Range to generate: {}-{}", String.format("%04X", range.start()), String.format("%04X", range.end()));
        Logger.getInstance().info("Available threads: {}", config.get(Config.THREADS));

        int offset = range.start() / 0x100;
        int loops = (range.end() - range.start()) / 0x100;
        for (int i = 0; i <= loops; i++) {
            float percentage = i / (loops + 1F) * 100F;
            int pageId = i + offset;
            int remappedPageId = remapHandler.remap(pageId);
            if (!remapHandler.canWritePage(pageId)) {
                Logger.getInstance().warn("[{}/{} {}%] Skipping {}-{} (Another page remaps to this page)", i + 1, loops + 1, String.format("%.2f", percentage), String.format("%04X", pageId * 0x100), String.format("%04X", pageId * 0x100 + 0xFF));
            } else if (!PageUtil.isPageSupported(remappedPageId)) {
                Logger.getInstance().warn("[{}/{} {}%] Skipping {}-{} (Client does not load this page)", i + 1, loops + 1, String.format("%.2f", percentage), String.format("%04X", pageId * 0x100), String.format("%04X", pageId * 0x100 + 0xFF));
            } else {
                Logger.getInstance().info("[{}/{} {}%] Generating {}-{}", i + 1, loops + 1, String.format("%.2f", percentage), String.format("%04X", pageId * 0x100), String.format("%04X", pageId * 0x100 + 0xFF));
                GlyphPage page = new GlyphPage(this, pageId, config.get(Config.THREADS));
                page.generate();
            }
        }

        Logger.getInstance().info("Generation finished");
    }

    public Config getConfig() {
        return config;
    }

    public FontHolder getFontHolder() {
        return fontHolder;
    }

    public RemapHandler getRemapHandler() {
        return this.remapHandler;
    }
}
