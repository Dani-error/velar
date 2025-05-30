@file:Suppress("unused")

package dev.dani.velar.api.event

import kotlin.reflect.KClass


/*
 * Project: velar
 * Created at: 30/05/2025 22:15
 * Created by: Dani-error
 */
class NPCEventHandler private constructor(
    private val handlers: List<Pair<KClass<out NPCEvent>, NPCEvent.() -> Unit>>
) {
    fun <T : NPCEvent> handle(event: T) {
        handlers.forEach { (clazz, handler) ->
            if (clazz.isInstance(event)) {
                handler(event)
            }
        }
    }

    class Builder {
        val bindings = mutableListOf<Pair<KClass<out NPCEvent>, NPCEvent.() -> Unit>>()

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : NPCEvent> bind(noinline block: T.() -> Unit) {
            bindings += T::class to (block as NPCEvent.() -> Unit)
        }

        fun build(): NPCEventHandler = NPCEventHandler(bindings)
    }
}
