package dev.dani.velar.api.settings

import dev.dani.velar.api.NPC


/*
 * Project: velar
 * Created at: 25/05/2025 17:54
 * Created by: Dani-error
 */
fun interface NPCTrackingRule<P> {

    fun shouldTrack(npc: NPC<*, P, *, *>, player: P): Boolean

    companion object {

        fun <P> allPlayers(): NPCTrackingRule<P> =
            NPCTrackingRule { _, _ -> true }

        fun <P> onlyUnspecifiedPlayers(): NPCTrackingRule<P> =
            NPCTrackingRule { npc, player -> !npc.includedPlayers.contains(player) }

        fun <P> onlyExplicitlyIncludedPlayers(): NPCTrackingRule<P> =
            NPCTrackingRule { npc, player -> npc.includedPlayers.contains(player) }

    }

}