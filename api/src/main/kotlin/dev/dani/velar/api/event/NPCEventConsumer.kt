package dev.dani.velar.api.event

import kotlin.jvm.Throws


/*
 * Project: velar
 * Created at: 25/05/2025 17:35
 * Created by: Dani-error
 */
fun interface NPCEventConsumer<E : NPCEvent> {

    @Throws(Exception::class)
    fun handle(event: E)

}