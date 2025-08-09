package io.github.kabanfriends.smoothfontgen.config.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.kabanfriends.smoothfontgen.JsonUtil;
import io.github.kabanfriends.smoothfontgen.config.*;

import java.lang.reflect.Array;
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
    public static final JsonCodec<FontRange> FONT_RANGE = new JsonCodec<>(
            (property) -> new FontRange(
                    Integer.parseInt(property.get().getAsJsonObject().get("from").getAsString(), 16) * 0x100,
                    Integer.parseInt(property.get().getAsJsonObject().get("to").getAsString(), 16) * 0x100 + 0xFF
            )
    );

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
