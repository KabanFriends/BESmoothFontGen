package io.github.kabanfriends.smoothfontgen.config.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kabanfriends.smoothfontgen.JsonUtil;
import io.github.kabanfriends.smoothfontgen.config.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class JsonCodecs {

    public static final JsonCodec<String> STRING = new JsonCodec<>((property) -> property.get().getAsString());
    public static final JsonCodec<String[]> STRING_ARRAY = new ArrayCodec<>(String.class, JsonElement::getAsString);
    public static final JsonCodec<Integer> INTEGER = new JsonCodec<>((property) -> property.get().getAsInt());
    public static final JsonCodec<Float> FLOAT = new JsonCodec<>((property) -> property.get().getAsFloat());
    public static final JsonCodec<Boolean> BOOLEAN = new JsonCodec<>((property) -> property.get().getAsBoolean());
    public static final JsonCodec<PageRemap[]> PAGE_REMAP_ARRAY = new ArrayCodec<>(PageRemap.class,
            (element) -> new PageRemap(
                    Integer.parseInt(element.getAsJsonObject().get("from").getAsString(), 16),
                    Integer.parseInt(element.getAsJsonObject().get("to").getAsString(), 16)
            )
    );
    public static final JsonCodec<FontInfo[]> FONT_PROPERTY_ARRAY = new ArrayCodec<>(FontInfo.class, FontInfoBuilder::build);
    public static final JsonCodec<WidthOverride[]> WIDTH_OVERRIDE_ARRAY = new ArrayCodec<>(WidthOverride.class,
            (element) -> new WidthOverride(
                    element.getAsJsonObject().get("from").getAsString().charAt(0),
                    element.getAsJsonObject().get("to").getAsString().charAt(0),
                    element.getAsJsonObject().get("width").getAsFloat()
            )
    );
    private static final JsonCodec<CellMapping[]> CELL_MAPPING_INLINE_ARRAY = new ArrayCodec<>(CellMapping.class,
            (element) -> new CellMapping(
                    parseCodepoint(element.getAsJsonObject().get("char").getAsString()),
                    parseCodepoint(element.getAsJsonObject().get("cell").getAsString())
            )
    );
    /**
     * Either an inline array of {"char": "25A0", "cell": "0410"} objects, or the path of a JSON file
     * whose "mappings" property holds such an array.
     */
    public static final JsonCodec<CellMapping[]> CELL_MAPPING_ARRAY = new JsonCodec<>(
            (property) -> {
                JsonElement value = property.get();
                if (value.isJsonArray()) {
                    return CELL_MAPPING_INLINE_ARRAY.deserialize(property);
                }
                File file = new File(value.getAsString());
                try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    return CELL_MAPPING_INLINE_ARRAY.deserialize(new JsonProperty(json, "mappings"));
                } catch (IOException e) {
                    throw new IllegalArgumentException("Failed to read cell mapping file " + file, e);
                }
            }
    );
    public static final JsonCodec<FontRange> FONT_RANGE = new JsonCodec<>(
            (property) -> new FontRange(
                    Integer.parseInt(property.get().getAsJsonObject().get("from").getAsString(), 16) * 0x100,
                    Integer.parseInt(property.get().getAsJsonObject().get("to").getAsString(), 16) * 0x100 + 0xFF
            )
    );

    /** Parse "25A0", "U+25A0" or "0x25A0" as a BMP code point. */
    static char parseCodepoint(String text) {
        String hex = text.trim();
        if (hex.startsWith("U+") || hex.startsWith("u+") || hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        int value = Integer.parseInt(hex, 16);
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException("Code point out of BMP range: " + text);
        }
        return (char) value;
    }

    public static class ArrayCodec<T> extends JsonCodec<T[]> {

        public ArrayCodec(Class<T> clazz, Function<JsonElement, T> deserializer) {
            super(
                    (property) -> {
                        JsonArray jsonArray = property.get().getAsJsonArray();
                        T[] array = (T[]) Array.newInstance(clazz, jsonArray.size());
                        for (int i = 0; i < jsonArray.size(); i++) {
                            array[i] = deserializer.apply(jsonArray.get(i));
                        }
                        return array;
                    }
            );
        }
    }
}
