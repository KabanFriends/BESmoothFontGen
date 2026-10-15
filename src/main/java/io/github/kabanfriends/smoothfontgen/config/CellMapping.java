package io.github.kabanfriends.smoothfontgen.config;

import java.util.Objects;

/**
 * Redirect one character to another character's texture cell.
 * The character is drawn using the texture and width stored in {@code cell}.
 */
public final class CellMapping {

    private final char character;
    private final char cell;

    public CellMapping(char character, char cell) {
        this.character = character;
        this.cell = cell;
    }

    public char character() {
        return character;
    }

    public char cell() {
        return cell;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CellMapping that = (CellMapping) o;
        return character == that.character && cell == that.cell;
    }

    @Override
    public int hashCode() {
        return Objects.hash(character, cell);
    }
}
