package dev.dani.velar.bukkit.protocol.impl

import dev.dani.velar.api.protocol.PlatformPacketAdapter
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin


/*
 * Project: velar
 * Created at: 25/05/2025 21:47
 * Created by: Dani-error
 */
object BukkitProtocolAdapter {

    fun packetAdapter(): PlatformPacketAdapter<World, Player, ItemStack, Plugin> {
        // check if protocol lib is available
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            return ProtocolLibPacketAdapter
        }

        // fallback
        return PacketEventsPacketAdapter
    }

    fun protocolLib(): PlatformPacketAdapter<World, Player, ItemStack, Plugin> {
        return ProtocolLibPacketAdapter
    }

    fun packetEvents(): PlatformPacketAdapter<World, Player, ItemStack, Plugin> {
        return PacketEventsPacketAdapter
    }

}