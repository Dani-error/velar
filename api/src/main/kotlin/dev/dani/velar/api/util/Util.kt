package dev.dani.velar.api.util

import java.util.concurrent.Callable
import java.util.function.Supplier


/*
 * Project: velar
 * Created at: 18/05/2025 20:25
 * Created by: Dani-error
 */
inline fun <reified T> safeEquals(
    original: Any?,
    compare: Any?,
    checker: (T, T) -> Boolean
): Boolean {
    // fast null check
    if (original == null || compare == null) return original == null && compare == null

    // fast identity check
    if (original === compare) return true

    // type check
    if (original !is T || compare !is T) return false

    // apply custom checker
    return checker(original, compare)
}

fun <T> wrap(block: () -> T): () -> T = {
    try {
        block()
    } catch (e: Exception) {
        throw IllegalStateException(e)
    }
}

fun Double.floor(): Int {
    val casted = this.toInt()

    return if (this < casted) casted - 1 else casted
}
