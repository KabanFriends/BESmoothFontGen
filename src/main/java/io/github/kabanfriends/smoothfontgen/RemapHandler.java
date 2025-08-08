package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.Config;
import io.github.kabanfriends.smoothfontgen.config.PageRemap;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class RemapHandler {

    private final SmoothGenerator main;
    private final Map<Integer, Integer> remaps;

    public RemapHandler(SmoothGenerator generator, PageRemap[] remps) {
        this.main = generator;
        this.remaps = new HashMap<>();
        for (PageRemap remap : remps) {
            this.remaps.put(remap.from(), remap.to());
        }
    }

    public boolean canWritePage(int originalPage) {
        return remaps.containsKey(originalPage) || !remaps.containsValue(originalPage);
    }

    public int remap(int originalPage) {
        return remaps.getOrDefault(originalPage, originalPage);
    }

    public void writeRemappingFile() {
        ByteBuffer buffer = ByteBuffer.allocate(0x100 * 8 * remaps.size());
        for (int i = 0; i < 0x100; i++) {
            if (!remaps.containsKey(i)) {
                continue;
            }
            int remapped = remaps.get(i);
            for (int j = 0; j < 0x100; j++) {
                // Remap the original characters
                buffer.put((byte) j);
                buffer.put((byte) i);
                buffer.put((byte) j);
                buffer.put((byte) remapped);
                // Remap the target characters to 0xFFFE
                int page = remapped >> 8;
                if (main.getConfig().get(Config.REMAP_HANGUL_CHARS) || !PageUtil.isPageHangul(page)) {
                    buffer.put((byte) j);
                    buffer.put((byte) remapped);
                    buffer.put((byte) 0xFE);
                    buffer.put((byte) 0xFF);
                }
            }
        }
        try (FileOutputStream outputStream = new FileOutputStream("smooth/remapping.dat")) {
            outputStream.write(buffer.array());
        } catch (Exception e) {
            Logger.getInstance().error("Failed to write remapping.dat", e);
        }
    }
}
