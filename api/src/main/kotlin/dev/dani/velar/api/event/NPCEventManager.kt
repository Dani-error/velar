package dev.dani.velar.api.event

import dev.dani.velar.api.platform.log.PlatformLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


/*
 * Project: velar
 * Created at: 25/05/2025 17:40
 * Created by: Dani-error
 */
interface NPCEventManager {

    fun <E : NPCEvent> post(event: E): E

    fun <E : NPCEvent> registerEventHandler(
        eventType: Class<E>,
        consumer: NPCEventConsumer<E>
    ): NPCEventSubscription<in E>

    fun <E : NPCEvent> registerEventHandler(
        eventType: Class<E>,
        consumer: NPCEventConsumer<E>,
        eventHandlerPriority: Int
    ): NPCEventSubscription<in E>

    fun unregisterEventHandlerIf(
        subscriptionFilter: (NPCEventSubscription<in NPCEvent>) -> Boolean
    )

    companion object {
        fun createDefault(debugEnabled: Boolean, logger: PlatformLogger): NPCEventManager {
            return DefaultNPCEventManager(debugEnabled, logger)
        }
    }

}

internal class DefaultNPCEventManager(
    private val debugEnabled: Boolean,
    private val platformLogger: PlatformLogger
) : NPCEventManager {

    private val registeredSubscribers: MutableMap<Class<*>, MutableList<NPCEventSubscription<in NPCEvent>>> =
        ConcurrentHashMap(16, 0.9f, 1)

    override fun <E : NPCEvent> post(event: E): E {
        event.npc.eventHandler.handle(event)

        for ((subscribedEventType, subscriptions) in registeredSubscribers) {
            if (subscribedEventType.isInstance(event) && subscriptions.isNotEmpty()) {
                for (subscription in subscriptions) {
                    if (isEventCancelled(event)) break

                    try {
                        subscription.eventConsumer.handle(event)
                    } catch (t: Throwable) {
                        EventExceptionHandler.rethrowFatalException(t)
                        if (debugEnabled) {
                            platformLogger.error(
                                "Subscriber ${subscription.eventConsumer::class.java.name} was unable to handle ${event::class.java.simpleName}",
                                t
                            )
                        }
                    }
                }
            }
        }

        return event
    }

    override fun <E : NPCEvent> registerEventHandler(
        eventType: Class<E>,
        consumer: NPCEventConsumer<E>
    ): NPCEventSubscription<in E> =
        registerEventHandler(eventType, consumer, 0)

    @Suppress("UNCHECKED_CAST")
    override fun <E : NPCEvent> registerEventHandler(
        eventType: Class<E>,
        consumer: NPCEventConsumer<E>,
        eventHandlerPriority: Int
    ): NPCEventSubscription<in E> {
        val subscription = DefaultNPCEventSubscription(
            order = eventHandlerPriority,
            eventType = eventType,
            eventConsumer = consumer,
            eventManager = this
        )

        val eventSubscriptions = registeredSubscribers
            .computeIfAbsent(eventType) { CopyOnWriteArrayList() }

        eventSubscriptions.add(subscription as NPCEventSubscription<in NPCEvent>)
        eventSubscriptions.sortBy { it.order }

        return subscription
    }

    override fun unregisterEventHandlerIf(
        subscriptionFilter: (NPCEventSubscription<in NPCEvent>) -> Boolean
    ) {
        for (subscriptions in registeredSubscribers.values) {
            subscriptions.removeIf(subscriptionFilter)
        }
    }

    fun removeSubscription(subscription: NPCEventSubscription<*>) {
        registeredSubscribers[subscription.eventType]?.remove(subscription)
    }

    private fun isEventCancelled(event: NPCEvent): Boolean =
        event is CancellableNPCEvent && event.cancelled
    
}