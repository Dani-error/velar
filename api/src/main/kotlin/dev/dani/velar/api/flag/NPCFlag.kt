package dev.dani.velar.api.flag

import dev.dani.velar.api.util.safeEquals


/*
 * Project: velar
 * Created at: 18/05/2025 20:17
 * Created by: Dani-error
 */
interface NPCFlag<T> {

    val key: String
    val defaultValue: T?
    fun accepts(value: T?): Boolean

    companion object {

        fun <T> flag(key: String, defaultValue: T?): NPCFlag<T> =
            flag(key, defaultValue) { true }

        fun <T> flag(key: String, defaultValue: T?, valueTester: (T?) -> Boolean): NPCFlag<T> =
            DefaultNPCFlag(key, defaultValue, valueTester)

    }

}

internal class DefaultNPCFlag<T>(
    override val key: String,
    override val defaultValue: T?,
    private val valueTester: (T?) -> Boolean
) : NPCFlag<T> {

    override fun accepts(value: T?): Boolean = valueTester(value)

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean =
        safeEquals<NPCFlag<*>>(this, other) { orig, comp ->
            orig.key == comp.key
        }

}