package dev.dani.velar.api.profile.impl

import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.ProfileProperty
import dev.dani.velar.api.util.safeEquals
import java.util.*


/*
 * Project: velar
 * Created at: 18/05/2025 20:58
 * Created by: Dani-error
 */
data class DefaultResolvedProfile(
    override val name: String,
    override val uniqueId: UUID,
    private val rawProperties: Set<ProfileProperty>
) : Profile.Resolved {

    override val properties: Set<ProfileProperty> =
        if (rawProperties.isEmpty()) emptySet() else rawProperties.toSet() // defensive copy

    override fun withName(name: String): Profile.Resolved =
        copy(name = name)

    override fun withUniqueId(uniqueId: UUID): Profile.Resolved =
        copy(uniqueId = uniqueId)

    override fun withoutProperties(): Profile.Resolved =
        copy(rawProperties = emptySet())

    override fun withProperty(property: ProfileProperty): Profile.Resolved =
        copy(rawProperties = properties + property)

    override fun withProperties(properties: Set<ProfileProperty>): Profile.Resolved =
        copy(rawProperties = properties)

    override fun hashCode(): Int =
        Objects.hash(name, uniqueId, properties)

    override fun equals(other: Any?): Boolean = safeEquals<Profile.Resolved>(this, other) { orig, comp ->
        orig.name == comp.name
                && orig.uniqueId == comp.uniqueId
                && orig.properties == comp.properties
    }

}
