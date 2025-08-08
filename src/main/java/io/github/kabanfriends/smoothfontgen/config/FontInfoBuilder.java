package io.github.kabanfriends.smoothfontgen.config;

import com.google.gson.JsonElement;
import io.github.kabanfriends.smoothfontgen.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FontInfoBuilder {

    private FontInfoBuilder() {
    }

    private static final Map<String, Function<JsonElement, ? extends FontInfo>> BUILDERS;

    static {
        BUILDERS = new HashMap<>();
        BUILDERS.put("file", element -> new FileFontInfo(
                element.getAsJsonObject().get("file").getAsString(),
                element.getAsJsonObject().get("size").getAsFloat(),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("padding"), JsonElement::getAsFloat, 0f),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("additionalArgs"), JsonElement::getAsString, "")
        ));
        BUILDERS.put("pixel", element -> new PixelFontInfo(
                element.getAsJsonObject().get("name").getAsString(),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("padding"), JsonElement::getAsFloat, 1f),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("externalPadding"), JsonElement::getAsFloat, 0f),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("scale"), JsonElement::getAsFloat, 1f),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("spaceWidth"), JsonElement::getAsInt, 4),
                JsonUtil.getOrDefault(element.getAsJsonObject().get("additionalArgs"), JsonElement::getAsString, "")
        ));
    }

    public static FontInfo build(JsonElement element) {
        String type = element.getAsJsonObject().get("type").getAsString();
        Function<JsonElement, ? extends FontInfo> builder = BUILDERS.get(type);
        if (builder == null) {
            throw new IllegalArgumentException("Unknown font info type: " + type);
        }
        return builder.apply(element);
    }
}
