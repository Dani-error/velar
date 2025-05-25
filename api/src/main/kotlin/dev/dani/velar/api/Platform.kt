package dev.dani.velar.api

import dev.dani.velar.api.event.NPCEventManager
import dev.dani.velar.api.log.PlatformLogger
import dev.dani.velar.api.profile.resolver.ProfileResolver
import dev.dani.velar.api.protocol.PlatformPacketAdapter


/*
 * Project: velar
 * Created at: 25/05/2025 18:22
 * Created by: Dani-error
 */
interface Platform<W, P, I, E> {

    val debug: Boolean
    val extension: E
    val logger: PlatformLogger
    val eventManager: NPCEventManager
    val npcTracker: NPCTracker<W, P, I, E>
    val profileResolver: ProfileResolver
    val taskManager: PlatformTaskManager
    val versionAccessor: PlatformVersionAccessor
    val worldAccessor: PlatformWorldAccessor<W>
    val packetFactory: PlatformPacketAdapter<W, P, I, E>
    val actionController: NPCActionController?

    fun newNpcBuilder(): NPC.Builder<W, P, I, E>

    interface Builder<W, P, I, E> {
        fun debug(debug: Boolean): Builder<W, P, I, E>

        fun extension(extension: E): Builder<W, P, I, E>

        fun logger(logger: PlatformLogger): Builder<W, P, I, E>

        fun eventManager(eventManager: NPCEventManager): Builder<W, P, I, E>

        fun npcTracker(npcTracker: NPCTracker<W, P, I, E>): Builder<W, P, I, E>

        fun taskManager(taskManager: PlatformTaskManager): Builder<W, P, I, E>

        fun profileResolver(profileResolver: ProfileResolver): Builder<W, P, I, E>

        fun worldAccessor(worldAccessor: PlatformWorldAccessor<W>): Builder<W, P, I, E>

        fun versionAccessor(versionAccessor: PlatformVersionAccessor): Builder<W, P, I, E>

        fun packetFactory(packetFactory: PlatformPacketAdapter<W, P, I, E>): Builder<W, P, I, E>

        fun actionController(decorator: (NPCActionController.Builder?) -> Unit): Builder<W, P, I, E>

        fun build(): Platform<W, P, I, E>
    }

}