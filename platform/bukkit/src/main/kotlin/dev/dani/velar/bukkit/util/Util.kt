package dev.dani.velar.bukkit.util

import dev.dani.velar.api.NPC
import dev.dani.velar.api.event.NPCEvent
import dev.dani.velar.api.event.PlayerNPCEvent
import dev.dani.velar.api.settings.NPCProfileResolver
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 21:15
 * Created by: Dani-error
 */
inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> {
    return EnumMap<K, V>(K::class.java).apply { putAll(pairs) }
}

fun NPCEvent.bukkitNPC(): NPC<World, Player, ItemStack, Plugin> {
    @Suppress("UNCHECKED_CAST")
    return npc as NPC<World, Player, ItemStack, Plugin>
}

fun PlayerNPCEvent.bukkitPlayer(): Player = player as Player

fun NPCProfileResolver.Companion.ofViewer(uuid: Boolean = true): NPCProfileResolver<Player> {
    return NPCProfileResolver { player, npc ->
        val resolver = if (uuid) ofUniqueId<Player>(player.uniqueId) else ofName(player.name)

        return@NPCProfileResolver resolver.resolveNPCProfile(player, npc)
    }
}