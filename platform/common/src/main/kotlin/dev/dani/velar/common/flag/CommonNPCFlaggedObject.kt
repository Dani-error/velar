@file:Suppress("UNCHECKED_CAST")

package dev.dani.velar.common.flag

import dev.dani.velar.api.flag.NPCFlag
import dev.dani.velar.api.flag.flagged.NPCFlaggedObject


/*
 * Project: velar
 * Created at: 25/05/2025 19:01
 * Created by: Dani-error
 */
abstract class CommonNPCFlaggedObject(flags: Map<NPCFlag<*>, Any?>) : NPCFlaggedObject {

    private val flags: MutableMap<NPCFlag<*>, Any?> = flags.toMutableMap()

    override fun <T> flagValue(flag: NPCFlag<T>, newValue: T?) {
        this.flags[flag] = newValue
    }

    override fun <T> flagValue(flag: NPCFlag<T>): T? {
        return flags.getOrDefault(flag, null) as T?
    }

}