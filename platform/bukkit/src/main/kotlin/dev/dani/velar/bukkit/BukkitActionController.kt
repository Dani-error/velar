package dev.dani.velar.bukkit

import dev.dani.velar.api.NPC.Companion.HIT_WHEN_PLAYER_HITS
import dev.dani.velar.api.NPC.Companion.LOOK_AT_PLAYER
import dev.dani.velar.api.NPC.Companion.SNEAK_WHEN_PLAYER_SNEAKS
import dev.dani.velar.api.NPCActionController
import dev.dani.velar.api.NPCActionController.Companion.AUTO_SYNC_POSITION_ON_SPAWN
import dev.dani.velar.api.NPCActionController.Companion.IMITATE_DISTANCE
import dev.dani.velar.api.NPCActionController.Companion.SPAWN_DISTANCE
import dev.dani.velar.api.NPCActionController.Companion.TAB_REMOVAL_TICKS
import dev.dani.velar.api.NPCTracker
import dev.dani.velar.api.event.NPCEventManager
import dev.dani.velar.api.event.ShowNPCEvent
import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.platform.PlatformVersionAccessor
import dev.dani.velar.api.protocol.enums.EntityAnimation
import dev.dani.velar.api.protocol.enums.PlayerInfoAction
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory
import dev.dani.velar.api.util.Position
import dev.dani.velar.bukkit.util.BukkitPlatformUtil.distance
import dev.dani.velar.bukkit.util.bukkitNPC
import dev.dani.velar.bukkit.util.bukkitPlayer
import dev.dani.velar.bukkit.util.lookAt
import dev.dani.velar.common.CommonNPCActionController
import dev.dani.velar.common.flag.CommonNPCFlaggedBuilder
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 20:49
 * Created by: Dani-error
 */
class BukkitActionController(
    flags: Map<NPCFlag<*>, Any?>,
    plugin: Plugin,
    eventManager: NPCEventManager,
    versionAccessor: PlatformVersionAccessor,
    private val npcTracker: NPCTracker<World, Player, ItemStack, Plugin>
) : CommonNPCActionController(flags), Listener {

    private val spawnDistance: Int
    private val imitateDistance: Int

    init {


        // add all listeners
        plugin.server.pluginManager.registerEvents(this, plugin)


        // register a listener for the post spawn event if we need to send out an update to remove the spawned player
        if (!versionAccessor.atLeast(1, 19, 3)) {
            eventManager.registerEventHandler(ShowNPCEvent.Post::class.java) { event ->
                // remove the npc from the tab list after the given amount of time (never smaller than 0 because of validation)
                val tabRemovalTicks = flagValueOrDefault(TAB_REMOVAL_TICKS)!!
                plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
                    // schedule the removal of the player from the tab list, can be done async
                    val player = event.bukkitPlayer()
                    val npc = event.bukkitNPC()
                    npc.platform.packetFactory
                        .createPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER)
                        .schedule(player, npc)
                }, tabRemovalTicks.toLong())
            }
        }


        // pre-calculate flag values
        val spawnDistance = flagValueOrDefault(SPAWN_DISTANCE)!!
        this.spawnDistance = spawnDistance * spawnDistance

        val imitateDistance = flagValueOrDefault(IMITATE_DISTANCE)!!
        this.imitateDistance = imitateDistance * imitateDistance


        // register listener to update the npc rotation after it is tracked
        if (flagValueOrDefault(AUTO_SYNC_POSITION_ON_SPAWN)!!) {
            eventManager.registerEventHandler(ShowNPCEvent.Post::class.java) { event ->
                val player: Player = event.bukkitPlayer()
                val to: Location = player.location
                val npc = event.bukkitNPC()

                val distance = distance(npc, to)
                if (distance <= this.imitateDistance && npc.flagValueOrDefault(LOOK_AT_PLAYER) == true) {
                    npc.lookAt(to).schedule(player)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun handleMove(event: PlayerMoveEvent) {
        val to = event.to
        val from = event.from

        val changedWorld: Boolean = !Objects.equals(from.world, to.world)
        val changedOrientation = from.yaw != to.yaw || from.pitch != to.pitch
        val changedPosition = from.x != to.x || from.y != to.y || from.z != to.z

        // check if any movement happened (event is also called when standing still)
        if (changedPosition || changedOrientation || changedWorld) {
            val player = event.player
            for (npc in npcTracker.trackedNPCs) {
                // check if the player is still in the same world as the npc
                val pos: Position = npc.position
                if (npc.world != player.world || !npc.world.isChunkLoaded(pos.chunkX, pos.chunkZ)) {
                    // if the player is tracked by the npc, stop that
                    npc.stopTrackingPlayer(player)
                    continue
                }

                // check if the player moved in / out of any npc tracking distance
                val distance = distance(npc, to)
                if (distance > this.spawnDistance) {
                    // this will only do something if the player is already tracked by the npc
                    npc.stopTrackingPlayer(player)
                    continue
                } else {
                    // this will only do something if the player is not already tracked by the npc
                    npc.trackPlayer(player)
                }

                // check if we should rotate the npc towards the player
                if (changedPosition
                    && npc.tracksPlayer(player)
                    && distance <= this.imitateDistance && npc.flagValueOrDefault(LOOK_AT_PLAYER) == true
                ) {
                    npc.lookAt(to).schedule(player)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun handleSneak(event: PlayerToggleSneakEvent) {
        val player = event.player
        for (npc in npcTracker.trackedNPCs) {
            val distance = distance(npc, player.location)

            // check if we should imitate the action
            if (npc.world == player.world
                && npc.tracksPlayer(player)
                && distance <= this.imitateDistance && npc.flagValueOrDefault(SNEAK_WHEN_PLAYER_SNEAKS) == true
            ) {
                // let the npc sneak as well
                npc.platform.packetFactory
                    .createEntityMetaPacket(EntityMetadataFactory.sneakingMetaFactory(), event.isSneaking)
                    .schedule(player, npc)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handleLeftClick(event: PlayerInteractEvent) {
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            val player = event.player
            for (npc in npcTracker.trackedNPCs) {
                val distance = distance(npc, player.location)

                // check if we should imitate the action
                if (npc.world == player.world
                    && npc.tracksPlayer(player)
                    && distance <= this.imitateDistance && npc.flagValueOrDefault(HIT_WHEN_PLAYER_HITS) == true
                ) {
                    // let the npc left click as well
                    npc.platform.packetFactory.createAnimationPacket(EntityAnimation.SWING_MAIN_ARM)
                        .schedule(player, npc)
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun handleQuit(event: PlayerQuitEvent) {
        for (npc in npcTracker.trackedNPCs) {
            // check if the npc tracks the player which disconnected and stop tracking him if so
            npc.stopTrackingPlayer(event.player)
        }
    }

    companion object {

        fun actionControllerBuilder(
            plugin: Plugin,
            eventManager: NPCEventManager,
            versionAccessor: PlatformVersionAccessor,
            npcTracker: NPCTracker<World, Player, ItemStack, Plugin>
        ): NPCActionController.Builder =
            BukkitActionControllerBuilder(plugin, eventManager, versionAccessor, npcTracker)

    }

    private class BukkitActionControllerBuilder
        (
        private val plugin: Plugin,
        private val eventManager: NPCEventManager,
        private val versionAccessor: PlatformVersionAccessor,
        private val npcTracker: NPCTracker<World, Player, ItemStack, Plugin>
    ) : CommonNPCFlaggedBuilder<NPCActionController.Builder>(), NPCActionController.Builder {

        override fun build(): NPCActionController {
            return BukkitActionController(
                this.flags,
                this.plugin,
                this.eventManager,
                this.versionAccessor,
                this.npcTracker
            )
        }
    }
}