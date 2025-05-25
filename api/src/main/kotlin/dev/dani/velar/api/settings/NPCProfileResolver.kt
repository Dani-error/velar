package dev.dani.velar.api.settings

import dev.dani.velar.api.NPC
import dev.dani.velar.api.profile.Profile
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

    }

}