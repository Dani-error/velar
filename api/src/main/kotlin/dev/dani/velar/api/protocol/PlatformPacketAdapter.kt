package dev.dani.velar.api.protocol

import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.protocol.enums.EntityAnimation
import dev.dani.velar.api.protocol.enums.ItemSlot
import dev.dani.velar.api.protocol.enums.PlayerInfoAction
import dev.dani.velar.api.protocol.enums.TeamMode
import dev.dani.velar.api.protocol.meta.EntityMetadataFactory


/*
 * Project: velar
 * Created at: 25/05/2025 18:20
 * Created by: Dani-error
 */
interface PlatformPacketAdapter<W, P, I, E> {

    fun createEntitySpawnPacket(): OutboundPacket<W, P, I, E>

    fun createEntityRemovePacket(): OutboundPacket<W, P, I, E>

    fun createPlayerInfoPacket(action: PlayerInfoAction): OutboundPacket<W, P, I, E>

    fun createRotationPacket(yaw: Float, pitch: Float): OutboundPacket<W, P, I, E>

    fun createAnimationPacket(animation: EntityAnimation): OutboundPacket<W, P, I, E>

    fun createEquipmentPacket(slot: ItemSlot, item: I): OutboundPacket<W, P, I, E>

    fun createCustomPayloadPacket(channelId: String, payload: ByteArray): OutboundPacket<W, P, I, E>

    fun <T, O> createEntityMetaPacket(
        metadata: EntityMetadataFactory<T, O>, value: T
    ): OutboundPacket<W, P, I, E>

    fun createTeamsPacket(mode: TeamMode, teamName: String, info: TeamInfo? = null, players: List<String> = emptyList()): OutboundPacket<W, P, I, E>

    fun initialize(platform: Platform<W, P, I, E>)

}