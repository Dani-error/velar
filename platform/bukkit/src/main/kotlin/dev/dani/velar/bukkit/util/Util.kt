package dev.dani.velar.bukkit.util

import java.util.*


/*
 * Project: velar
 * Created at: 25/05/2025 21:15
 * Created by: Dani-error
 */
inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> {
    return EnumMap<K, V>(K::class.java).apply { putAll(pairs) }
}
