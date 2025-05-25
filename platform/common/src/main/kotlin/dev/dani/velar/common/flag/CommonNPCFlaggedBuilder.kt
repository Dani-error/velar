@file:Suppress("UNCHECKED_CAST")

package dev.dani.velar.common.flag

import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.flag.flagged.NPCFlaggedBuilder
import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 19:04
 * Created by: Dani-error
 */
abstract class CommonNPCFlaggedBuilder<B> : NPCFlaggedBuilder<B> {

    protected open val flags: MutableMap<NPCFlag<*>, Any?> = mutableMapOf()

    @Throws(IllegalArgumentException::class)
    override fun <T> flag(flag: NPCFlag<T>, value: T?): B {

        // check if the flag value is acceptable
        if (flag.accepts(value)) {
            this.flags[flag] = Optional.ofNullable<T>(value)
            return this as B
        }

        throw IllegalArgumentException((("Flag " + flag.key) + " does not accept " + value) + " as it's value!")
    }
}