package io.github.kabanfriends.smoothfontgen.config;

import java.util.Objects;

public final class PageRemap {

    private final int from;
    private final int to;

    public PageRemap(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PageRemap that = (PageRemap) o;
        return from == that.from && to == that.to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
