package dev.dani.velar.api

import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.flag.flagged.NPCFlaggedBuilder
import dev.dani.velar.api.flag.flagged.NPCFlaggedObject
import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.resolver.ProfileResolver
import dev.dani.velar.api.protocol.NPCSpecificOutboundPacket
import dev.dani.velar.api.protocol.enums.EntityAnimation
import dev.dani.velar.api.protocol.enums.ItemSlot
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory
import dev.dani.velar.api.settings.NPCSettings
import java.util.concurrent.CompletableFuture


/*
 * Project: velar
 * Created at: 25/05/2025 17:19
 * Created by: Dani-error
 */
interface NPC<W, P, I, E> : NPCFlaggedObject {

    val entityId: Int
    val profile: Profile.Resolved
    val world: W
    val position: Position
    val settings: NPCSettings<P>
    val platform: Platform<W, P, I, E>
    val npcTracker: NPCTracker<W, P, I, E>

    val includedPlayers: Collection<P>
    val trackedPlayers: Collection<P>

    fun shouldIncludePlayer(player: P): Boolean
    fun includesPlayer(player: P): Boolean

    fun addIncludedPlayer(player: P): NPC<W, P, I, E>
    fun removeIncludedPlayer(player: P): NPC<W, P, I, E>
    fun unlink(): NPC<W, P, I, E>

    fun tracksPlayer(player: P): Boolean
    fun trackPlayer(player: P): NPC<W, P, I, E>
    fun forceTrackPlayer(player: P): NPC<W, P, I, E>
    fun stopTrackingPlayer(player: P): NPC<W, P, I, E>

    fun rotate(yaw: Float, pitch: Float): NPCSpecificOutboundPacket<W, P, I, E>

    fun lookAt(position: Position): NPCSpecificOutboundPacket<W, P, I, E>

    fun playAnimation(animation: EntityAnimation): NPCSpecificOutboundPacket<W, P, I, E>

    fun changeItem(slot: ItemSlot, item: I): NPCSpecificOutboundPacket<W, P, I, E>

    fun <T, O> changeMetadata(metadata: EntityMetadataFactory<T, O>, value: T): NPCSpecificOutboundPacket<W, P, I, E>

    interface Builder<W, P, I, E> : NPCFlaggedBuilder<Builder<W, P, I, E>> {
        fun entityId(id: Int): Builder<W, P, I, E>

        fun position(position: Position): Builder<W, P, I, E>

        fun profile(profile: Profile.Resolved): Builder<W, P, I, E>

        fun profile(profile: Profile): CompletableFuture<Builder<W, P, I, E>> {
            return this.profile(null, profile)
        }

        fun profile(
            resolver: ProfileResolver?,
            profile: Profile
        ): CompletableFuture<Builder<W, P, I, E>>

        fun npcSettings(decorator: (NPCSettings.Builder<P>) -> Unit): Builder<W, P, I, E>

        fun build(): NPC<W, P, I, E>

        fun buildAndTrack(): NPC<W, P, I, E>
    }

    companion object {

        val LOOK_AT_PLAYER: NPCFlag<Boolean> = NPCFlag.flag("imitate_player_look", false)
        val HIT_WHEN_PLAYER_HITS: NPCFlag<Boolean> = NPCFlag.flag("imitate_player_hit", false)
        val SNEAK_WHEN_PLAYER_SNEAKS: NPCFlag<Boolean> = NPCFlag.flag("imitate_player_sneak", false)

    }
}