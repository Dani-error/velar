package dev.dani.velar.api.protocol

import dev.dani.velar.api.NPC
import java.util.function.Consumer


/*
 * Project: velar
 * Created at: 25/05/2025 18:11
 * Created by: Dani-error
 */
fun interface OutboundPacket<W, P, I, E> {

    fun schedule(player: P, npc: NPC<W, P, I, E>)

    fun scheduleForTracked(npc: NPC<W, P, I, E>) {
        this.schedule(npc) { it.trackedPlayers }
    }

    fun schedule(npc: NPC<W, P, I, E>, extractor: (NPC<W, P, I, E>) -> Collection<P>) {
        this.schedule(extractor(npc), npc)
    }

    fun schedule(players: Collection<P>, npc: NPC<W, P, I, E>) {
        players.forEach(Consumer { player: P -> this.schedule(player, npc) })
    }

    fun toSpecific(targetNpc: NPC<W, P, I, E>): NPCSpecificOutboundPacket<W, P, I, E> {
        return NPCSpecificOutboundPacket.fromOutboundPacket(targetNpc, this)
    }

}