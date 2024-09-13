package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.config.codec.JsonCodec;

public class ConfigKey<T> {

    private final String id;
    private final JsonCodec<T> codec;
    private final T defaultValue;

    public ConfigKey(String id, JsonCodec<T> codec, T defaultValue) {
        this.id = id;
        this.codec = codec;
        this.defaultValue = defaultValue;
    }

    public String id() {
        return id;
    }

    public JsonCodec<T> codec() {
        return codec;
    }

    public T defaultValue() {
        return defaultValue;
    }
}
