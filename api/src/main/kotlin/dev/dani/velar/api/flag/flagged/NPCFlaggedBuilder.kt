package dev.dani.velar.api.flag.flagged

import dev.dani.velar.api.flag.NPCFlag


/*
 * Project: velar
 * Created at: 18/05/2025 20:26
 * Created by: Dani-error
 */
interface NPCFlaggedBuilder<B> {

    fun <T> flag(flag: NPCFlag<T>, value: T?): B

}