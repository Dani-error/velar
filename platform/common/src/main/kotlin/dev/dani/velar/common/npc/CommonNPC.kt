package dev.dani.velar.common.npc

import dev.dani.velar.api.NPC
import dev.dani.velar.api.NPCTracker
import dev.dani.velar.api.event.NPCEventHandler
import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.util.Position
import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.protocol.NPCSpecificOutboundPacket
import dev.dani.velar.api.protocol.enums.EntityAnimation
import dev.dani.velar.api.protocol.enums.ItemSlot
import dev.dani.velar.api.protocol.enums.PlayerInfoAction
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory
import dev.dani.velar.api.settings.NPCSettings
import dev.dani.velar.api.util.safeEquals
import dev.dani.velar.common.event.DefaultHideNPCEvent
import dev.dani.velar.common.event.DefaultShowNPCEvent
import dev.dani.velar.common.flag.CommonNPCFlaggedObject
import java.util.*
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.sqrt


/*
 * Project: velar
 * Created at: 25/05/2025 19:29
 * Created by: Dani-error
 */
class CommonNPC<W, P, I, E>(
    flags: Map<NPCFlag<*>, Any?>,
    override val entityId: Int,
    override val profile: Profile.Resolved,
    override val world: W,
    override val position: Position,
    override val platform: Platform<W, P, I, E>,
    override val settings: NPCSettings<P>,
    override val eventHandler: NPCEventHandler
) : CommonNPCFlaggedObject(flags), NPC<W, P, I, E> {

    override val trackedPlayers: MutableSet<P> = Collections.synchronizedSet(HashSet())
    override val includedPlayers: MutableSet<P> = Collections.synchronizedSet(HashSet())

    override val npcTracker: NPCTracker<W, P, I, E>
        get() = platform.npcTracker

    override fun shouldIncludePlayer(player: P): Boolean =
        this.settings.trackingRule.shouldTrack(this, player)

    override fun includesPlayer(player: P): Boolean =
        this.includedPlayers.contains(player)

    override fun addIncludedPlayer(player: P): NPC<W, P, I, E> {
        this.includedPlayers.add(player)
        return this
    }

    override fun removeIncludedPlayer(player: P): NPC<W, P, I, E> {
        this.includedPlayers.remove(player)
        return this
    }

    override fun unlink(): NPC<W, P, I, E> {
        // remove this npc from the tracked ones, do it first to prevent further player tracking
        this.npcTracker.stopTrackingNPC(this)


        // remove this npc for all tracked players
        for (player in trackedPlayers) {
            this.stopTrackingPlayer(player)
        }

        // for chaining
        return this
    }

    override fun tracksPlayer(player: P): Boolean =
        this.trackedPlayers.contains(player)

    override fun trackPlayer(player: P): NPC<W, P, I, E> {
        // check if we should track the player
        if (this.shouldIncludePlayer(player)) {
            return this.forceTrackPlayer(player)
        }

        // nothing to do
        return this
    }

    override fun forceTrackPlayer(player: P): NPC<W, P, I, E> {
        // check if the player is not already tracked
        if (!trackedPlayers.contains(player)) {
            // break early if the add is not wanted by plugin
            if (platform.eventManager.post(DefaultShowNPCEvent.pre(this, player!!)).cancelled) {
                return this
            }

            // register the player, prevent duplicate spawns in case the entity was spawned
            // by a different thread during processing of the pre-track event
            if (!trackedPlayers.add(player)) {
                return this
            }

            // send the player info packet & schedule the actual add of the
            // player entity into the target world
            platform.packetFactory.createPlayerInfoPacket(PlayerInfoAction.ADD_PLAYER).schedule(player, this)
            platform.taskManager.scheduleDelayedAsync(10) {
                platform.packetFactory.createEntitySpawnPacket().schedule(player, this)
                platform.eventManager.post(DefaultShowNPCEvent.post(this, player))
            }
        }

        return this
    }

    override fun stopTrackingPlayer(player: P): NPC<W, P, I, E> {
        // check if the player was previously tracked
        if (this.trackedPlayers.contains(player)) {
            // break early if the removal is not wanted by plugin
            if (this.platform.eventManager.post(DefaultHideNPCEvent.pre(this, player!!)).cancelled) {
                return this
            }

            // unregister the player, prevent duplicate remove packets in case the entity
            // was removed by a different thread during processing of the pre-hide event
            if (!this.trackedPlayers.remove(player)) {
                return this
            }

            // schedule an entity remove (the player list change is not needed normally, but to make sure that the npc is gone)
            this.platform.packetFactory.createEntityRemovePacket().schedule(player, this)
            this.platform.packetFactory.createPlayerInfoPacket(PlayerInfoAction.REMOVE_PLAYER).schedule(player, this)

            // post the finish of the removal to all plugins
            this.platform.eventManager.post(DefaultHideNPCEvent.post(this, player))
        }

        // for chaining
        return this
    }

    override fun rotate(yaw: Float, pitch: Float): NPCSpecificOutboundPacket<W, P, I, E> =
        this.platform.packetFactory.createRotationPacket(yaw, pitch).toSpecific(this)

    override fun lookAt(position: Position): NPCSpecificOutboundPacket<W, P, I, E> {
        val diffX: Double = position.x - this.position.x
        val diffY: Double = position.y - this.position.y
        val diffZ: Double = position.z - this.position.z

        val distanceXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val distanceY = sqrt(distanceXZ * distanceXZ + diffY * diffY)

        var yaw = Math.toDegrees(acos(diffX / distanceXZ))
        val pitch = Math.toDegrees(acos(diffY / distanceY)) - 90


        // correct yaw according to difference
        if (diffZ < 0) {
            yaw += abs(180 - yaw) * 2
        }
        yaw -= 90.0

        return platform.packetFactory.createRotationPacket(yaw.toFloat(), pitch.toFloat()).toSpecific(this)
    }

    override fun playAnimation(animation: EntityAnimation): NPCSpecificOutboundPacket<W, P, I, E> =
        this.platform.packetFactory.createAnimationPacket(animation).toSpecific(this)

    override fun changeItem(slot: ItemSlot, item: I): NPCSpecificOutboundPacket<W, P, I, E> =
        this.platform.packetFactory.createEquipmentPacket(slot, item).toSpecific(this)

    override fun <T, O> changeMetadata(
        metadata: EntityMetadataFactory<T, O>,
        value: T
    ): NPCSpecificOutboundPacket<W, P, I, E> =
        this.platform.packetFactory.createEntityMetaPacket(metadata, value).toSpecific(this)

    override fun hashCode(): Int =
        Integer.hashCode(this.entityId)

    override fun equals(other: Any?): Boolean =
        safeEquals<NPC<*, *, *, *>>(this, other) { orig, comp -> orig.entityId == comp.entityId }
}