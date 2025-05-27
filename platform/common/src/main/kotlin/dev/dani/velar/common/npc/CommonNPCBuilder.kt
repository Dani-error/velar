package dev.dani.velar.common.npc

import dev.dani.velar.api.NPC
import dev.dani.velar.api.Platform
import dev.dani.velar.api.Position
import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.resolver.ProfileResolver
import dev.dani.velar.api.settings.NPCSettings
import dev.dani.velar.common.flag.CommonNPCFlaggedBuilder
import dev.dani.velar.common.settings.CommonNPCSettingsBuilder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ThreadLocalRandom


/*
 * Project: velar
 * Created at: 25/05/2025 19:41
 * Created by: Dani-error
 */
class CommonNPCBuilder<W, P, I, E>(private val platform: Platform<W, P, I, E>) : CommonNPCFlaggedBuilder<NPC.Builder<W, P, I, E>>(), NPC.Builder<W, P, I, E> {

    private var entityId: Int = ThreadLocalRandom.current().nextInt(0, Int.MAX_VALUE)

    private var world: W? = null
    private var pos: Position? = null

    private var profile: Profile.Resolved? = null
    private var npcSettings: NPCSettings<P>? = null

    override fun entityId(id: Int): NPC.Builder<W, P, I, E> {
        // validate the npc entity id
        require(id >= 0) { "NPC entity id must be positive" }

        this.entityId = id
        return this
    }

    override fun position(position: Position): NPC.Builder<W, P, I, E> {
        // try to resolve the world from the given position
        val world: W = platform.worldAccessor.resolveWorldFromIdentifier(position.worldId)
            ?: throw IllegalArgumentException("Could not resolve world from identifier: " + position.worldId)

        // store both, world and position
        this.world = world
        this.pos = position

        return this
    }

    override fun profile(profile: Profile.Resolved): NPC.Builder<W, P, I, E> {
        this.profile = profile
        return this
    }

    override fun profile(
        resolver: ProfileResolver?,
        profile: Profile
    ): CompletableFuture<NPC.Builder<W, P, I, E>> {
        // use the default platform resolver if no resolver is given
        val usedResolver = resolver ?: platform.profileResolver

        // resolve the profile using the given resolver or the platform resolver
        return usedResolver.resolveProfile(profile)
            .thenApply { resolvedProfile: Profile.Resolved ->
                this.profile(resolvedProfile)
            }
    }

    override fun npcSettings(decorator: (NPCSettings.Builder<P>) -> Unit): NPC.Builder<W, P, I, E> {
        // build the npc settings
        val builder: NPCSettings.Builder<P> = CommonNPCSettingsBuilder()
        decorator(builder)
        this.npcSettings = builder.build()

        return this
    }

    override fun build(): NPC<W, P, I, E> {
        // fill in empty npc settings if not given
        if (this.npcSettings == null) {
            this.npcSettings { }
        }

        return CommonNPC(
            this.flags,
            this.entityId,
            requireNotNull(profile) { "profile must be given" },
            requireNotNull(world) { "world and position must be given" },
            requireNotNull(pos) { "world and position must be given" },
            platform,
            requireNotNull(npcSettings) { "npc settings must be given" }
        )
    }

    override fun buildAndTrack(): NPC<W, P, I, E> {
        val npc: NPC<W, P, I, E> = this.build()
        platform.npcTracker.trackNPC(npc)

        return npc
    }

}