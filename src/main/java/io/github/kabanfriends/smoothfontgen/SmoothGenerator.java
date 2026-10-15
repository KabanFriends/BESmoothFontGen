package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.Config;
import io.github.kabanfriends.smoothfontgen.config.FontInfo;
import io.github.kabanfriends.smoothfontgen.config.FontRange;

import java.io.File;
import java.util.List;

public class SmoothGenerator {

    private final Config config;
    private final FontHolder fontHolder;
    private final CellLayout cellLayout;
    private final RemapHandler remapHandler;

    public SmoothGenerator() {
        this.config = new Config();
        config.load(new File("config.json"));

        FontInfo<?>[] fileFontInfoArray = config.get(Config.FONTS);
        this.fontHolder = new FontHolder(fileFontInfoArray);
        this.cellLayout = new CellLayout(config.get(Config.PAGE_REMAPPING), config.get(Config.REMAP_HANGUL_CHARS), config.get(Config.CELL_MAPPING));
        this.remapHandler = new RemapHandler(cellLayout);
    }

    public void start() {
        if (fontHolder.getFontCount() == 0) {
            Logger.getInstance().error("No valid font files were found, aborting");
            return;
        }

        /*
        if (config.get(Config.TEST_MODE)) {
            Tests.testShapeDescGenerator();
            return;
        }
        */

        if (!checkCellLayout()) {
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
            if (!PageUtil.isPageSupported(pageId)) {
                Logger.getInstance().warn("[{}/{} {}%] Skipping {}-{} (Client does not load this page)", i + 1, loops + 1, String.format("%.2f", percentage), String.format("%04X", pageId * 0x100), String.format("%04X", pageId * 0x100 + 0xFF));
            } else {
                Logger.getInstance().info("[{}/{} {}%] Generating {}-{}", i + 1, loops + 1, String.format("%.2f", percentage), String.format("%04X", pageId * 0x100), String.format("%04X", pageId * 0x100 + 0xFF));
                GlyphPage page = new GlyphPage(this, pageId, config.get(Config.THREADS));
                page.generate();
            }
        }

        Logger.getInstance().info("Generation finished");
    }

    private boolean checkCellLayout() {
        if (config.hasFailed(Config.CELL_MAPPING)) {
            Logger.getInstance().error("cellMapping could not be read, aborting");
            return false;
        }
        if (!cellLayout.errors().isEmpty()) {
            for (String error : cellLayout.errors()) {
                Logger.getInstance().error("Invalid cell mapping: {}", error);
            }
            Logger.getInstance().error("{} invalid cell mapping(s), aborting", cellLayout.errors().size());
            return false;
        }
        if (cellLayout.isEmpty()) {
            return true;
        }

        Logger.getInstance().info("Cell mapping: {} remapped characters, {} sharing a cell, {} relocated into other cells, {} vacant cells",
                cellLayout.remaps().size(), cellLayout.sharedCellCount(), cellLayout.relocatedCellCount(), cellLayout.vacantCellCount());

        List<String> mismatches = cellLayout.verify(fontHolder);
        if (!mismatches.isEmpty()) {
            for (String mismatch : mismatches) {
                Logger.getInstance().error("Cell mapping mismatch: {}", mismatch);
            }
            Logger.getInstance().error("{} character(s) would not render like the cell they are mapped to, aborting", mismatches.size());
            return false;
        }
        return true;
    }

    public Config getConfig() {
        return config;
    }

    public FontHolder getFontHolder() {
        return fontHolder;
    }

    public CellLayout getCellLayout() {
        return cellLayout;
    }
}
