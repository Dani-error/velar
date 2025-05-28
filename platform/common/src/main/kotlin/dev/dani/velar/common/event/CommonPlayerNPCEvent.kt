@file:Suppress("UNCHECKED_CAST")

package dev.dani.velar.common.event

import dev.dani.velar.api.NPC
import dev.dani.velar.api.event.*
import dev.dani.velar.api.event.InteractNPCEvent.Hand


/*
 * Project: velar
 * Created at: 25/05/2025 19:19
 * Created by: Dani-error
 */
abstract class CommonPlayerNPCEvent(override val npc: NPC<*, *, *, *>, override val player: Any) : CommonNPCEvent(npc), PlayerNPCEvent

class DefaultAttackNPCEvent(npc: NPC<*, *, *, *>, player: Any) : CommonPlayerNPCEvent(npc, player), AttackNPCEvent {

    override var cancelled: Boolean = false

    override fun cancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {

        fun attackNPC(npc: NPC<*, *, *, *>, player: Any): AttackNPCEvent =
            DefaultAttackNPCEvent(npc, player)

    }

}

class DefaultInteractNPCEvent(npc: NPC<*, *, *, *>, player: Any, override val hand: Hand) : CommonPlayerNPCEvent(npc, player), InteractNPCEvent {

    override var cancelled: Boolean = false

    override fun cancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    companion object {

        fun interactNPC(
            npc: NPC<*, *, *, *>,
            player: Any,
            hand: Hand
        ): InteractNPCEvent = DefaultInteractNPCEvent(npc, player, hand)

    }

}

open class DefaultHideNPCEvent(npc: NPC<*, *, *, *>, player: Any) : CommonPlayerNPCEvent(npc, player), HideNPCEvent {


    companion object {

        fun pre(npc: NPC<*, *, *, *>, player: Any): HideNPCEvent.Pre =
            DefaultPre(npc, player)

        fun post(npc: NPC<*, *, *, *>, player: Any): HideNPCEvent.Post =
            DefaultPost(npc, player)

    }

    internal class DefaultPre(npc: NPC<*, *, *, *>, player: Any) : DefaultHideNPCEvent(npc, player), HideNPCEvent.Pre {

        override var cancelled: Boolean = false

        override fun cancelled(cancelled: Boolean) {
            this.cancelled = cancelled
        }

    }

    internal class DefaultPost(npc: NPC<*, *, *, *>, player: Any) : DefaultHideNPCEvent(npc, player), HideNPCEvent.Post

}

open class DefaultShowNPCEvent(npc: NPC<*, *, *, *>, player: Any) : CommonPlayerNPCEvent(npc, player), ShowNPCEvent {


    companion object {

        fun pre(npc: NPC<*, *, *, *>, player: Any): ShowNPCEvent.Pre =
            DefaultPre(npc, player)

        fun post(npc: NPC<*, *, *, *>, player: Any): ShowNPCEvent.Post =
            DefaultPost(npc, player)

    }

    internal class DefaultPre(npc: NPC<*, *, *, *>, player: Any) : DefaultShowNPCEvent(npc, player), ShowNPCEvent.Pre {

        override var cancelled: Boolean = false

        override fun cancelled(cancelled: Boolean) {
            this.cancelled = cancelled
        }

    }

    internal class DefaultPost(npc: NPC<*, *, *, *>, player: Any) : DefaultShowNPCEvent(npc, player), ShowNPCEvent.Post

}