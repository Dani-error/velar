package dev.dani.velar.api.util


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
