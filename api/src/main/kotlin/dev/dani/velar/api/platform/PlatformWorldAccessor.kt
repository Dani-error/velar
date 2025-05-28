package dev.dani.velar.api.platform


/*
 * Project: velar
 * Created at: 25/05/2025 17:17
 * Created by: Dani-error
 */
interface PlatformWorldAccessor<W> {

    fun extractWorldIdentifier(world: W): String

    fun resolveWorldFromIdentifier(identifier: String): W?

}