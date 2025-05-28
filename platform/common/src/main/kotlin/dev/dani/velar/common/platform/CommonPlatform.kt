package dev.dani.velar.common.platform

import dev.dani.velar.api.*
import dev.dani.velar.api.event.NPCEventManager
import dev.dani.velar.api.platform.log.PlatformLogger
import dev.dani.velar.api.platform.Platform
import dev.dani.velar.api.platform.PlatformTaskManager
import dev.dani.velar.api.platform.PlatformVersionAccessor
import dev.dani.velar.api.platform.PlatformWorldAccessor
import dev.dani.velar.api.profile.resolver.ProfileResolver
import dev.dani.velar.api.protocol.PlatformPacketAdapter
import dev.dani.velar.common.npc.CommonNPCBuilder


/*
 * Project: velar
 * Created at: 25/05/2025 19:48
 * Created by: Dani-error
 */
class CommonPlatform<W, P, I, E>(
    override val debug: Boolean,
    override val extension: E,
    override val logger: PlatformLogger,
    override val npcTracker: NPCTracker<W, P, I, E>,
    override val profileResolver: ProfileResolver,
    override val taskManager: PlatformTaskManager,
    override val actionController: NPCActionController?,
    override val versionAccessor: PlatformVersionAccessor,
    override val eventManager: NPCEventManager,
    override val worldAccessor: PlatformWorldAccessor<W>,
    override val packetFactory: PlatformPacketAdapter<W, P, I, E>
) : Platform<W, P, I, E> {

    init {
        // register the packet listeners
        this.packetFactory.initialize(this)
    }

    override fun newNPCBuilder(): NPC.Builder<W, P, I, E> =
        CommonNPCBuilder(this)

}