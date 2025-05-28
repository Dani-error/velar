package dev.dani.velar.api.protocol

import dev.dani.velar.api.NPC


/*
 * Project: velar
 * Created at: 25/05/2025 18:15
 * Created by: Dani-error
 */
interface NPCSpecificOutboundPacket<W, P, I, E> {

    val npc: NPC<W, P, I, E>

    fun scheduleForTracked()

    fun schedule(player: P)

    fun schedule(players: Collection<P>)

    fun schedule(extractor: (NPC<W, P, I, E>) -> Collection<P>)

    companion object {

        fun <W, P, I, E> fromOutboundPacket(npc: NPC<W, P, I, E>, packet: OutboundPacket<W, P, I, E>): NPCSpecificOutboundPacket<W, P, I, E> =
            DefaultNPCSpecificOutboundPacket(npc, packet)

    }

}

internal class DefaultNPCSpecificOutboundPacket<W, P, I, E>(
    private val target: NPC<W, P, I, E>,
    private val outboundPacket: OutboundPacket<W, P, I, E>
) : NPCSpecificOutboundPacket<W, P, I, E> {

    override val npc: NPC<W, P, I, E>
        get() = target

    override fun scheduleForTracked() =
        this.outboundPacket.scheduleForTracked(this.target)

    override fun schedule(extractor: (NPC<W, P, I, E>) -> Collection<P>) =
        this.outboundPacket.schedule(this.target, extractor)

    override fun schedule(players: Collection<P>) =
        this.outboundPacket.schedule(players, this.target)

    override fun schedule(player: P) =
        this.outboundPacket.schedule(player, this.target)


}