package dev.dani.velar.api.profile

import dev.dani.velar.api.util.safeEquals
import java.util.*


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

data class DefaultProfileProperty(
    override val name: String,
    override val value: String,
    override val signature: String?
): ProfileProperty {

    override fun hashCode(): Int =
        Objects.hash(name, value, signature)

    override fun equals(other: Any?): Boolean = safeEquals<ProfileProperty>(this, other) { orig, comp ->
        orig.name == comp.name
                && orig.value == comp.value
                && Objects.equals(orig.signature, comp.signature)
    }

}