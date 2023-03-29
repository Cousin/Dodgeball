package com.joeyexecutive.dodgeball.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.joeyexecutive.dodgeball.adapter.BukkitLocationAdapter;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Util for Gson serialization/deserialization
 */
public final class GsonHelper {

    public static final Map<Type, Object> ADAPTERS = Maps.newHashMap(Map.of(
            Location.class, new BukkitLocationAdapter()
    ));

    public static Gson GSON = createGson();

    public static Gson PRETTY_GSON = createPrettyGson();

    /**
     * Used for if you need to register an adapter later on in runtime
     * Usually accessed by another plugin
     */
    public static void registerLateAdapter(Type type, Object object) {
        ADAPTERS.put(type, object);
        GSON = createGson();
        PRETTY_GSON = createPrettyGson();
    }

    private static GsonBuilder baseBuilder() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.disableHtmlEscaping();
        for (Map.Entry<Type, Object> entry : ADAPTERS.entrySet()) {
            gsonBuilder.registerTypeAdapter(entry.getKey(), entry.getValue());
        }
        return gsonBuilder;
    }

    private static Gson createGson() {
        return baseBuilder().create();
    }

    private static Gson createPrettyGson() {
        return baseBuilder().setPrettyPrinting().create();
    }

}