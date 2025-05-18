package dev.dani.velar.api.flag


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