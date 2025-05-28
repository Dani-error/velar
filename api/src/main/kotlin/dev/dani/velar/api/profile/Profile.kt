@file:Suppress("unused")

package dev.dani.velar.api.profile

import dev.dani.velar.api.util.safeEquals
import java.util.*


/*
 * Project: velar
 * Created at: 18/05/2025 20:38
 * Created by: Dani-error
 */
interface Profile {

    val resolved: Boolean
    val uniqueId: UUID?
    val name: String?
    val properties: Set<ProfileProperty>?

    companion object {
        fun unresolved(name: String): Unresolved = DefaultUnresolvedProfile(name, null)


        fun unresolved(uniqueId: UUID): Unresolved = DefaultUnresolvedProfile(null, uniqueId)


        fun resolved(name: String, uniqueId: UUID): Resolved =
            resolved(name, uniqueId, emptySet())

        fun resolved(
            name: String,
            uniqueId: UUID,
            properties: Set<ProfileProperty>
        ): Resolved = DefaultResolvedProfile(name, uniqueId, properties)

    }

    interface Unresolved : Profile {

        override val resolved: Boolean
            get() = false

        override val properties: Set<ProfileProperty>
            get() = Collections.emptySet()

    }

    interface Resolved : Profile {

        override val uniqueId: UUID
        override val name: String

        override val resolved: Boolean
            get() = true

        fun withName(name: String): Resolved
        fun withUniqueId(uniqueId: UUID): Resolved
        fun withoutProperties(): Resolved
        fun withProperty(property: ProfileProperty): Resolved
        fun withProperties(properties: Set<ProfileProperty>): Resolved

    }

}

internal data class DefaultResolvedProfile(
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

internal data class DefaultUnresolvedProfile(
    override val name: String?,
    override val uniqueId: UUID?
) : Profile.Unresolved {

    override fun hashCode(): Int = Objects.hash(name, uniqueId)

    override fun equals(other: Any?): Boolean = safeEquals<Profile.Unresolved>(this, other) { orig, comp ->
        Objects.equals(orig.name, comp.name) && Objects.equals(orig.uniqueId, comp.uniqueId)
    }
}
