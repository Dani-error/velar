package dev.dani.velar.api.profile

import dev.dani.velar.api.profile.impl.DefaultProfileProperty


/*
 * Project: velar
 * Created at: 18/05/2025 20:38
 * Created by: Dani-error
 */
interface ProfileProperty {

    val name: String
    val value: String
    val signature: String?

    companion object {

        fun property(name: String, value: String): ProfileProperty = property(name, value, null)

        fun property(name: String, value: String, signature: String?): ProfileProperty = DefaultProfileProperty(name, value, signature)

    }

}