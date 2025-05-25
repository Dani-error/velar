package dev.dani.velar.api

import java.util.UUID


/*
 * Project: velar
 * Created at: 25/05/2025 17:47
 * Created by: Dani-error
 */
interface NPCTracker<W, P, I, E> {

    val trackedNPCs: Collection<NPC<W, P, I, E>>

    fun npcById(entityId: Int): NPC<W, P, I, E>?

    fun npcByUniqueId(uniqueId: UUID): NPC<W, P, I, E>?

    fun trackNPC(npc: NPC<W, P, I, E>)

    fun stopTrackingNPC(npc: NPC<W, P, I, E>)

}