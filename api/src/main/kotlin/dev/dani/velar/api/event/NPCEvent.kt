package dev.dani.velar.api.event

import dev.dani.velar.api.NPC


/*
 * Project: velar
 * Created at: 25/05/2025 17:19
 * Created by: Dani-error
 */
interface NPCEvent {

    val npc: NPC<*, *, *, *>

}

interface CancellableNPCEvent : NPCEvent {

    val cancelled: Boolean

    fun cancelled(cancelled: Boolean)

}