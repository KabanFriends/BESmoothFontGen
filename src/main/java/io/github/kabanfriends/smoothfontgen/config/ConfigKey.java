package io.github.kabanfriends.smoothfontgen.config;

import io.github.kabanfriends.smoothfontgen.config.codec.JsonCodec;

public record ConfigKey<T>(String id, JsonCodec<T> codec, T defaultValue) {
}
