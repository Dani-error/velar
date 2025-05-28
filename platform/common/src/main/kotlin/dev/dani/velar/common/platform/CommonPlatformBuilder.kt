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
import dev.dani.velar.common.CommonNPCTracker


/*
 * Project: velar
 * Created at: 25/05/2025 19:51
 * Created by: Dani-error
 */
abstract class CommonPlatformBuilder<W, P, I, E> : Platform.Builder<W, P, I, E> {

    protected var extension: E? = null
    protected var logger: PlatformLogger? = null
    protected var debug: Boolean = DEFAULT_DEBUG
    protected var eventManager: NPCEventManager? = null
    protected var npcTracker: NPCTracker<W, P, I, E>? = null
    protected var profileResolver: ProfileResolver? = null
    protected var taskManager: PlatformTaskManager? = null
    protected var versionAccessor: PlatformVersionAccessor? = null
    protected var worldAccessor: PlatformWorldAccessor<W>? = null
    protected var packetAdapter: PlatformPacketAdapter<W, P, I, E>? = null
    protected var actionControllerDecorator: ((NPCActionController.Builder?) -> Unit)? = null

    override fun debug(debug: Boolean): Platform.Builder<W, P, I, E> {
        this.debug = debug
        return this
    }

    override fun extension(extension: E): Platform.Builder<W, P, I, E> {
        this.extension = extension
        return this
    }

    override fun logger(logger: PlatformLogger): CommonPlatformBuilder<W, P, I, E> {
        this.logger = logger
        return this
    }

    override fun eventManager(eventManager: NPCEventManager): Platform.Builder<W, P, I, E> {
        this.eventManager = eventManager
        return this
    }

    override fun npcTracker(npcTracker: NPCTracker<W, P, I, E>): Platform.Builder<W, P, I, E> {
        this.npcTracker = npcTracker
        return this
    }

    override fun taskManager(taskManager: PlatformTaskManager): Platform.Builder<W, P, I, E> {
        this.taskManager = taskManager
        return this
    }

    override fun profileResolver(profileResolver: ProfileResolver): Platform.Builder<W, P, I, E> {
        this.profileResolver = profileResolver
        return this
    }

    override fun worldAccessor(worldAccessor: PlatformWorldAccessor<W>): Platform.Builder<W, P, I, E> {
        this.worldAccessor = worldAccessor
        return this
    }

    override fun versionAccessor(versionAccessor: PlatformVersionAccessor): Platform.Builder<W, P, I, E> {
        this.versionAccessor = versionAccessor
        return this
    }

    override fun packetFactory(packetFactory: PlatformPacketAdapter<W, P, I, E>): Platform.Builder<W, P, I, E> {
        this.packetAdapter = packetFactory
        return this
    }

    override fun actionController(
        decorator: (NPCActionController.Builder?) -> Unit
    ): CommonPlatformBuilder<W, P, I, E> {
        this.actionControllerDecorator = decorator
        return this
    }

    override fun build(): Platform<W, P, I, E> {
        // validate that the required values are present
        requireNotNull(this.extension) { "extension" }

        // let the downstream builder set all default values if required
        this.prepareBuild()

        // validate that the required values are present
        requireNotNull(this.logger) { "logger" }

        // use the default profile resolver if no specific one was specified
        if (this.profileResolver == null) {
            this.profileResolver = DEFAULT_PROFILE_RESOLVER
        }

        // use a new event bus if no specific one was specified
        if (this.eventManager == null) {
            this.eventManager = NPCEventManager.createDefault(this.debug, this.logger!!)
        }

        // use a new npc tracker if none is given
        if (this.npcTracker == null) {
            this.npcTracker = CommonNPCTracker.newNPCTracker()
        }

        return this.doBuild()
    }

    protected abstract fun prepareBuild()

    protected abstract fun doBuild(): Platform<W, P, I, E>

    companion object {

        protected val DEFAULT_DEBUG: Boolean = java.lang.Boolean.getBoolean("velar.debug")
        protected val DEFAULT_PROFILE_RESOLVER: ProfileResolver = ProfileResolver.caching(ProfileResolver.mojang())

    }
}