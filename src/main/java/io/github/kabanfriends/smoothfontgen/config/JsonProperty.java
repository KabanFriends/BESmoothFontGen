package io.github.kabanfriends.smoothfontgen.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record JsonProperty(JsonObject json, String key) {

    public JsonElement get() {
        return json.get(key);
    }

    public void set(JsonElement value) {
        json.add(key, value);
    }
}
