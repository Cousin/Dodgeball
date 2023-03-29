package com.joeyexecutive.dodgeball.util;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import org.bukkit.ChatColor;

/**
 * Utility class for Minecraft chat related things
 */
public class ChatUtils {

    public static String colorCode(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static IChatBaseComponent adventureComponentToIChatBase(net.kyori.adventure.text.Component component) {
        return IChatBaseComponent.ChatSerializer.b(GsonComponentSerializer.gson().serialize(component));
    }

    public static String maxLengthStr(String message, int maxChars) {
        if (maxChars <= 0) {
            return "";
        }
        if (message.length() > maxChars) {
            return message.substring(0, maxChars);
        }
        return message;
    }

}
