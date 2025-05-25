package dev.dani.velar.api.event


/*
 * Project: velar
 * Created at: 25/05/2025 17:35
 * Created by: Dani-error
 */
interface NPCEventSubscription<E : NPCEvent> {

    val order: Int
    val eventType: Class<E>
    val eventConsumer: NPCEventConsumer<E>

    fun dispose()

}

internal class DefaultNPCEventSubscription<E : NPCEvent>(
    override val order: Int,
    override val eventType: Class<E>,
    override val eventConsumer: NPCEventConsumer<E>,
    private val eventManager: DefaultNPCEventManager
) : NPCEventSubscription<E> {

    override fun dispose() {
        eventManager.removeSubscription(this)
    }

}