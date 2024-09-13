package io.github.kabanfriends.smoothfontgen.config;

public final class FontRange {

    private final int start;
    private final int end;

    public FontRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FontRange that = (FontRange) obj;
        return this.start == that.start &&
                this.end == that.end;
    }
}
