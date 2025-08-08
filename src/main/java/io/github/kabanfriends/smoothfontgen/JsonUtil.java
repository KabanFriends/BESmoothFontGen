package io.github.kabanfriends.smoothfontgen;

import com.google.gson.JsonElement;

import java.util.function.Function;

public class JsonUtil {

    public static <T> T getOrDefault(JsonElement element, Function<JsonElement, T> getter, T defaultValue) {
        try {
            return getter.apply(element);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
