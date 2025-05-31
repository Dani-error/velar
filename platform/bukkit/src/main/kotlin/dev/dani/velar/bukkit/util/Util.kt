@file:Suppress("unused")

package dev.dani.velar.bukkit.util

import com.comphenix.protocol.wrappers.WrappedChatComponent
import dev.dani.velar.api.NPC
import dev.dani.velar.api.NPC.Builder
import dev.dani.velar.api.event.NPCEvent
import dev.dani.velar.api.event.PlayerNPCEvent
import dev.dani.velar.api.protocol.NPCSpecificOutboundPacket
import dev.dani.velar.api.settings.NPCProfileResolver
import io.papermc.lib.PaperLib
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Location
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

fun <W, P, I, E> Builder<W, P, I, E>.position(location: Location): Builder<W, P, I, E> {
    this.position(if (PaperLib.isPaper() && PaperLib.isVersion(16, 5)) {
        BukkitPlatformUtil.positionFromBukkitModern(location)
    } else {
        BukkitPlatformUtil.positionFromBukkitLegacy(location)
    })

    return this
}

fun <W, P, I, E> NPC<W, P, I, E>.lookAt(location: Location): NPCSpecificOutboundPacket<W, P, I, E> {
    return this.lookAt(if (PaperLib.isPaper() && PaperLib.isVersion(16, 5)) {
        BukkitPlatformUtil.positionFromBukkitModern(location)
    } else {
        BukkitPlatformUtil.positionFromBukkitLegacy(location)
    })
}



fun WrappedChatComponent.toLegacy(): String {
    val json = this.json
    val components: Array<BaseComponent> = ComponentSerializer.parse(json)
    return components.joinToString("") { it.toLegacyText() }
}