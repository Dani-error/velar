package dev.dani.velar.api


/*
 * Project: velar
 * Created at: 25/05/2025 17:15
 * Created by: Dani-error
 */
interface PlatformVersionAccessor {

    val major: Int
    val minor: Int
    val patch: Int

    fun atLeast(major: Int, minor: Int, patch: Int): Boolean

}