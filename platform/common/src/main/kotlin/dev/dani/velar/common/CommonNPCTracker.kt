package dev.dani.velar.common

import dev.dani.velar.api.NPC
import dev.dani.velar.api.NPCTracker
import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 18:56
 * Created by: Dani-error
 */
class CommonNPCTracker<W, P, I, E> : NPCTracker<W, P, I, E> {

    override val trackedNPCs: MutableSet<NPC<W, P, I, E>> = Collections.synchronizedSet(mutableSetOf<NPC<W, P, I, E>>())

    override fun npcById(entityId: Int): NPC<W, P, I, E>? {
        for (trackedNPC in this.trackedNPCs) {
            if (trackedNPC.entityId == entityId) {
                return trackedNPC
            }
        }

        return null
    }

    override fun npcByUniqueId(uniqueId: UUID): NPC<W, P, I, E>? {
        for (trackedNPC in this.trackedNPCs) {
            if (trackedNPC.profile.uniqueId == uniqueId) {
                return trackedNPC
            }
        }

        return null
    }

    override fun stopTrackingNPC(npc: NPC<W, P, I, E>) {
        this.trackedNPCs.remove(npc)
    }

    override fun trackNPC(npc: NPC<W, P, I, E>) {
        this.trackedNPCs.add(npc)
    }

    companion object {

        fun <W, P, I, E> newNPCTracker(): CommonNPCTracker<W, P, I, E> {
            return CommonNPCTracker()
        }

    }

}