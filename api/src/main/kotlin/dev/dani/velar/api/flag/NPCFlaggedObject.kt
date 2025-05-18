package dev.dani.velar.api.flag


/*
 * Project: velar
 * Created at: 18/05/2025 20:26
 * Created by: Dani-error
 */
interface NPCFlaggedObject {

    fun <T> flagValue(flag: NPCFlag<T>, newValue: T?)

    fun <T> flagValue(flag: NPCFlag<T>): T?

    fun <T> flagValueOrDefault(flag: NPCFlag<T>): T? =
        flagValue(flag) ?: flag.defaultValue

}