package dev.dani.velar.api.flag.flagged

import dev.dani.velar.api.flag.NPCFlag


/*
 * Project: velar
 * Created at: 18/05/2025 20:26
 * Created by: Dani-error
 */
interface NPCFlaggedBuilder<B> {

    fun <T> flag(flag: NPCFlag<T>, value: T?): B

    @Suppress("UNCHECKED_CAST")
    fun flags(vararg pairs: Pair<NPCFlag<*>, Any?>): B {
        for ((flag, value) in pairs) {
            flag(flag as NPCFlag<Any?>, value)
        }
        return this as B
    }

}