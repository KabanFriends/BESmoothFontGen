package io.github.kabanfriends.smoothfontgen;

import io.github.kabanfriends.smoothfontgen.config.CellMapping;
import io.github.kabanfriends.smoothfontgen.config.PageRemap;
import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Decides which glyph is drawn in each texture cell, and which characters are
 * redirected to another cell through remapping.dat.
 *
 * <p>The game reads both the texture and the width of a remapped character
 * from the target cell, and a remap is applied only once (no chaining).
 * A cell therefore normally holds its own character's glyph, except when:
 * <ul>
 *     <li>its own character is remapped away, in which case the cell holds
 *     the glyph of a character remapped into it (or nothing), or</li>
 *     <li>a legacy {@code pageRemapping} entry assigns it explicitly.</li>
 * </ul>
 * Every remapped character that shares a cell it does not own must render
 * identically to the cell's occupant (same texture and same width); this is
 * checked by {@link #verify(FontHolder)} before anything is generated.
 */
public final class CellLayout {

    public static final char LEGACY_HIDDEN_TARGET = (char) 0xFFFE;

    /** Character -> cell, written to remapping.dat. */
    private final TreeMap<Character, Character> remaps = new TreeMap<>();
    /** Cell -> character whose glyph is drawn in it, when not the cell itself. */
    private final TreeMap<Character, Character> occupants = new TreeMap<>();
    /** Cells whose own character was remapped away and that nothing was moved into. */
    private final TreeSet<Character> vacant = new TreeSet<>();
    /** (character, cell) pairs whose rendering must match the cell's occupant. */
    private final List<char[]> sharedCells = new ArrayList<>();
    /** Cells assigned by legacy pageRemapping (excluded from inference). */
    private final TreeSet<Character> legacyCells = new TreeSet<>();

    private final List<String> errors = new ArrayList<>();

    public CellLayout(PageRemap[] pageRemaps, boolean remapHangulChars, CellMapping[] cellMappings) {
        for (PageRemap remap : pageRemaps) {
            addLegacyPageRemap(remap, remapHangulChars);
        }
        if (cellMappings.length > 0) {
            addCellMappings(cellMappings);
        }
        // Cells whose own character is drawn elsewhere and that received nothing.
        for (Character c : remaps.keySet()) {
            if (!occupants.containsKey(c) && PageUtil.isPageSupported(c >> 8)) {
                vacant.add(c);
            }
        }
    }

    private void addLegacyPageRemap(PageRemap remap, boolean remapHangulChars) {
        for (int j = 0; j < 0x100; j++) {
            char from = (char) ((remap.from() << 8) | j);
            char to = (char) ((remap.to() << 8) | j);
            putRemap(from, to);
            occupants.put(to, from);
            legacyCells.add(to);
            // The target page's own characters must not show the relocated glyphs.
            // This used to test (remapped >> 8), which was always page 0, so the
            // Hangul exception never applied.
            if (remapHangulChars || !PageUtil.isPageHangul(remap.to())) {
                putRemap(to, LEGACY_HIDDEN_TARGET);
            }
        }
    }

    private void addCellMappings(CellMapping[] cellMappings) {
        Map<Character, List<Character>> incoming = new TreeMap<>();
        for (CellMapping mapping : cellMappings) {
            char c = mapping.character();
            char cell = mapping.cell();
            if (c == cell) {
                continue;
            }
            if (c == 0 || cell == 0) {
                errors.add(String.format("cellMapping %04X -> %04X: U+0000 cannot be mapped or used as a cell", (int) c, (int) cell));
                continue;
            }
            if (!PageUtil.isPageSupported(cell >> 8)) {
                errors.add(String.format("cellMapping %04X -> %04X: the client does not load page %02X", (int) c, (int) cell, cell >> 8));
                continue;
            }
            if (legacyCells.contains(cell)) {
                errors.add(String.format("cellMapping %04X -> %04X: cell is already assigned by pageRemapping", (int) c, (int) cell));
                continue;
            }
            if (!putRemap(c, cell)) {
                continue;
            }
            incoming.computeIfAbsent(cell, k -> new ArrayList<>()).add(c);
        }

        for (Map.Entry<Character, List<Character>> entry : incoming.entrySet()) {
            char cell = entry.getKey();
            List<Character> chars = entry.getValue();
            Collections.sort(chars);
            char occupant;
            if (remaps.containsKey(cell)) {
                // The cell's own character moved away: the lowest incoming character takes the cell.
                occupant = chars.get(0);
                occupants.put(cell, occupant);
            } else {
                occupant = cell;
            }
            for (char c : chars) {
                if (c != occupant) {
                    sharedCells.add(new char[] { c, cell });
                }
            }
        }
    }

    private boolean putRemap(char from, char to) {
        Character previous = remaps.get(from);
        if (previous != null && previous != to) {
            errors.add(String.format("%04X is mapped to both %04X and %04X", (int) from, (int) previous.charValue(), (int) to));
            return false;
        }
        remaps.put(from, to);
        return true;
    }

    /**
     * @return the character whose glyph is drawn in this cell, or {@code null} if the cell is empty
     */
    public Character occupantOf(char cell) {
        if (vacant.contains(cell)) {
            return null;
        }
        return occupants.getOrDefault(cell, cell);
    }

    public boolean isRelocated(char cell) {
        return occupants.containsKey(cell);
    }

    public Map<Character, Character> remaps() {
        return Collections.unmodifiableMap(remaps);
    }

    public boolean isEmpty() {
        return remaps.isEmpty();
    }

    /** @return number of cells whose own character was moved away and that hold nothing */
    public int vacantCellCount() {
        return vacant.size();
    }

    public int relocatedCellCount() {
        return occupants.size() - legacyCells.size();
    }

    public int sharedCellCount() {
        return sharedCells.size();
    }

    public List<String> errors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Check that every character that shares a cell it does not own renders exactly like the
     * cell's occupant, so that redirecting it changes neither its look nor its width.
     *
     * @return descriptions of all mismatches (empty when everything matches)
     */
    public List<String> verify(FontHolder fontHolder) {
        List<String> mismatches = new ArrayList<>();
        for (char[] pair : sharedCells) {
            char c = pair[0];
            char cell = pair[1];
            Character occupant = occupantOf(cell);
            if (occupant == null) {
                continue;
            }
            WrappedFont<?> charFont = fontHolder.getFirstFont(c);
            WrappedFont<?> occupantFont = fontHolder.getFirstFont(occupant);
            String charKey = charFont.renderKey(c);
            String occupantKey = occupantFont.renderKey(occupant);
            float charWidth = fontHolder.getWidth(c);
            float occupantWidth = fontHolder.getWidth(occupant);
            if (!charKey.equals(occupantKey)) {
                mismatches.add(String.format("%04X -> cell %04X: glyph differs from %04X (%s vs %s)",
                        (int) c, (int) cell, (int) occupant.charValue(), charFont.getFontInfo().name(), occupantFont.getFontInfo().name()));
            } else if (Float.floatToIntBits(charWidth) != Float.floatToIntBits(occupantWidth)) {
                mismatches.add(String.format("%04X -> cell %04X: width %s differs from %04X width %s",
                        (int) c, (int) cell, charWidth, (int) occupant.charValue(), occupantWidth));
            }
        }
        return mismatches;
    }
}
