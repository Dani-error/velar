package dev.dani.velar.api.event

import dev.dani.velar.api.NPC


/*
 * Project: velar
 * Created at: 25/05/2025 17:19
 * Created by: Dani-error
 */
interface NPCEvent {

    fun <W, P, I, E> npc(): NPC<W, P, I, E>

}

interface CancellableNPCEvent : NPCEvent {

    val cancelled: Boolean

    fun cancelled(cancelled: Boolean)

}