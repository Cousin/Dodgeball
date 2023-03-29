package com.joeyexecutive.dodgeball.adapter;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;

/**
 * Gson adapter for {@link Location}
 */
public class BukkitLocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject object = jsonElement.getAsJsonObject();
        return new Location(
                Bukkit.getWorld(object.get("world").getAsString()),
                object.get("x").getAsDouble(),
                object.get("y").getAsDouble(),
                object.get("z").getAsDouble(),
                object.has("yaw") ? object.get("yaw").getAsFloat() : 0.0F,
                object.has("pitch") ? object.get("pitch").getAsFloat() : 0.0F
        );
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        final JsonObject object = new JsonObject();
        object.add("world", new JsonPrimitive(location.getWorld().getName()));
        object.add("x", new JsonPrimitive(location.getX()));
        object.add("y", new JsonPrimitive(location.getY()));
        object.add("z", new JsonPrimitive(location.getZ()));
        if (location.getYaw() != 0) {
            object.add("yaw", new JsonPrimitive(location.getYaw()));
        }
        if (location.getPitch() != 0) {
            object.add("pitch", new JsonPrimitive(location.getPitch()));
        }
        return object;
    }

}
