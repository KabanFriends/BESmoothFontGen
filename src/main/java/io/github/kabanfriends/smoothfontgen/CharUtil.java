package io.github.kabanfriends.smoothfontgen;

public class CharUtil {

    private CharUtil() {
    }

    public static boolean isControlChar(char c) {
        return c < 0x20 || (c >= 0x80 && c <= 0x9F);
    }
}
