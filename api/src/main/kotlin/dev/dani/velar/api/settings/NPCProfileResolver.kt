package dev.dani.velar.api.settings

import dev.dani.velar.api.NPC
import dev.dani.velar.api.profile.Profile
import java.util.UUID
import java.util.concurrent.CompletableFuture


/*
 * Project: velar
 * Created at: 25/05/2025 17:50
 * Created by: Dani-error
 */
fun interface NPCProfileResolver<P> {

    fun resolveNPCProfile(player: P, npc: NPC<*, P, *, *>): CompletableFuture<Profile.Resolved>

    companion object {

        fun <P> ofSpawnedNPC(): NPCProfileResolver<P> =
            NPCProfileResolver { _, npc -> CompletableFuture.completedFuture(npc.profile) }

        fun <P> ofName(name: String): NPCProfileResolver<P> =
            ofProfile(Profile.unresolved(name))

        fun <P> ofUniqueId(uniqueId: UUID): NPCProfileResolver<P> =
            ofProfile(Profile.unresolved(uniqueId))

        fun <P> ofProfile(profile: Profile): NPCProfileResolver<P> =
            NPCProfileResolver { _, npc ->
                return@NPCProfileResolver npc.platform.profileResolver.resolveProfile(profile)
                    .thenApply { npc.profile.withProperties(it.properties ?: setOf()) }
            }

    }

}