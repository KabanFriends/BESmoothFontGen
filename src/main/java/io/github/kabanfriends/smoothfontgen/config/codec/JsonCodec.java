package io.github.kabanfriends.smoothfontgen.config.codec;

import io.github.kabanfriends.smoothfontgen.config.JsonProperty;

import java.util.function.Function;

public class JsonCodec<T> {

    private final Function<JsonProperty, T> deserializer;

    public JsonCodec(Function<JsonProperty, T> deserializer) {
        this.deserializer = deserializer;
    }

    public T deserialize(JsonProperty container) {
        return deserializer.apply(container);
    }
}
