package dev.dani.velar.api.profile.impl

import dev.dani.velar.api.profile.ProfileProperty
import dev.dani.velar.api.util.safeEquals
import java.util.Objects


/*
 * Project: velar
 * Created at: 18/05/2025 20:40
 * Created by: Dani-error
 */
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