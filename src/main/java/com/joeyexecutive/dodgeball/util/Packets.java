package com.joeyexecutive.dodgeball.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility class for sending packets
 */
public final class Packets {

    /**
     * The singleton {@link ProtocolManager} instance from ProtocolLib
     */
    public static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

    /**
     * Shortcut for {@link ProtocolManager#createPacket(PacketType)}
     */
    public static PacketContainer create(PacketType type) {
        return PROTOCOL_MANAGER.createPacket(type);
    }

    /**
     * Shortcut for {@link #send(PacketContainer, Collection)} for a single player
     */
    public static void send(PacketContainer packet, Player player) {
        send(packet, Collections.singleton(player));
    }

    /**
     * Send the packet to every online player
     * @param packet The packet to send
     */
    public static void sendToEveryone(PacketContainer packet) {
        send(packet, Bukkit.getOnlinePlayers());
    }

    /**
     * Sends the provided packet to every player in the provided collection
     * @param packet The packet to send
     * @param players The players that will have the packet sent to them
     */
    public static void send(PacketContainer packet, Collection<? extends Player> players) {
        for (Player player : players) {
            ((CraftPlayer) player).getHandle().b.a((Packet<?>) packet.getHandle());
        }
    }

    public static WrappedChatComponent convertComponent(TextComponent component) {
        return WrappedChatComponent.fromJson(ComponentSerializer.toString(component));
    }

    public static WrappedChatComponent convertComponent(Component component) {
        return WrappedChatComponent.fromJson(GsonComponentSerializer.gson().serialize(component));
    }
}
