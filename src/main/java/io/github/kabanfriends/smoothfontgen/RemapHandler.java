package io.github.kabanfriends.smoothfontgen;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

public class RemapHandler {

    private final CellLayout layout;

    public RemapHandler(CellLayout layout) {
        this.layout = layout;
    }

    /**
     * Write remapping.dat: one entry per remapped character, each made of the
     * source and target code points as little-endian 16-bit integers.
     */
    public void writeRemappingFile() {
        Map<Character, Character> remaps = layout.remaps();
        ByteBuffer buffer = ByteBuffer.allocate(remaps.size() * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (Map.Entry<Character, Character> entry : remaps.entrySet()) {
            buffer.putChar(entry.getKey());
            buffer.putChar(entry.getValue());
        }
        try (FileOutputStream outputStream = new FileOutputStream("smooth/remapping.dat")) {
            outputStream.write(buffer.array());
        } catch (Exception e) {
            Logger.getInstance().error("Failed to write remapping.dat", e);
        }
    }
}
