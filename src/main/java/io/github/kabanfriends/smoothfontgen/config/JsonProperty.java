package io.github.kabanfriends.smoothfontgen.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonProperty {

    private final JsonObject json;
    private final String key;

    public JsonProperty(JsonObject json, String key) {
        this.json = json;
        this.key = key;
    }

    public JsonObject json() {
        return json;
    }

    public String key() {
        return key;
    }

    public JsonElement get() {
        return json.get(key);
    }

    public void set(JsonElement value) {
        json.add(key, value);
    }
}
