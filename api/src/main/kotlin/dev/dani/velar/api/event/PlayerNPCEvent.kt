package dev.dani.velar.api.event


/*
 * Project: velar
 * Created at: 25/05/2025 17:25
 * Created by: Dani-error
 */

interface PlayerNPCEvent : NPCEvent {

    val player: Any

}

interface AttackNPCEvent : PlayerNPCEvent, CancellableNPCEvent

interface HideNPCEvent : PlayerNPCEvent {
    interface Pre : HideNPCEvent, CancellableNPCEvent

    interface Post : HideNPCEvent
}

interface ShowNPCEvent : PlayerNPCEvent {
    interface Pre : ShowNPCEvent, CancellableNPCEvent

    interface Post : ShowNPCEvent
}

interface InteractNPCEvent : PlayerNPCEvent, CancellableNPCEvent {

    val hand: Hand

    enum class Hand {
        MAIN_HAND,
        OFF_HAND
    }

}