package io.github.kabanfriends.smoothfontgen.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.kabanfriends.smoothfontgen.Logger;
import io.github.kabanfriends.smoothfontgen.config.codec.JsonCodecs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    public static final ConfigKey<Boolean> SHOW_GLYPH_INFO = new ConfigKey<>("showGlyphInfo", JsonCodecs.BOOLEAN, false);
    public static final ConfigKey<Integer> THREADS = new ConfigKey<>("threads", JsonCodecs.INTEGER, 4);
    public static final ConfigKey<FontRange> RANGE = new ConfigKey<>("range", JsonCodecs.FONT_RANGE, new FontRange(0x0000, 0xFFFF));
    public static final ConfigKey<FontInfo[]> FONTS = new ConfigKey<>("fonts", JsonCodecs.FONT_PROPERTY_ARRAY, new FontInfo[0]);
    public static final ConfigKey<String> ADDITIONAL_ARGS = new ConfigKey<>("additionalArgs", JsonCodecs.STRING, "");

    private static final List<ConfigKey<?>> CONFIG_KEYS = Arrays.asList(
            SHOW_GLYPH_INFO,
            THREADS,
            RANGE,
            FONTS,
            ADDITIONAL_ARGS
    );

    private final Map<ConfigKey<?>, Object> values = new HashMap<>();

    public void load(File configFile) {
        try {
            JsonObject json = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();
            for (ConfigKey<?> key : CONFIG_KEYS) {
                if (!json.has(key.id())) {
                    continue;
                }
                try {
                    values.put(key, key.codec().deserialize(new JsonProperty(json, key.id())));
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to parse config key {}", key.id(), e);
                }
            }
        } catch (FileNotFoundException e) {
            Logger.getInstance().error("Failed to read config file", e);
        } catch (JsonParseException | IllegalStateException e) {
            Logger.getInstance().error("Malformed config json", e);
        }
    }

    public <T> T get(ConfigKey<T> key) {
        if (!values.containsKey(key)) {
            return key.defaultValue();
        }
        //noinspection unchecked
        return (T) values.get(key);
    }
}
