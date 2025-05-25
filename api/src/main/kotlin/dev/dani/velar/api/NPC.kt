package dev.dani.velar.api

import dev.dani.velar.api.flag.flagged.NPCFlaggedObject
import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.settings.NPCSettings


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
    // Platform
    val npcTracker: NPCTracker<W, P, I, E>

    val includedPlayers: Collection<P>

    fun shouldIncludePlayer(player: P): Boolean
    //...

}