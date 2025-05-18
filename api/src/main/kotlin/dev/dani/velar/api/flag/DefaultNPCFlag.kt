package dev.dani.velar.api.flag

import dev.dani.velar.api.util.safeEquals

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