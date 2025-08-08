package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.font.WrappedFont;

public interface FontInfo<T extends WrappedFont<?>> {

    String name();

    float padding();

    String additionalArgs();

    WrappedFont<?> loadFont();
}
