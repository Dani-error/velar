package dev.dani.velar.api.profile

import dev.dani.velar.api.profile.impl.DefaultResolvedProfile
import dev.dani.velar.api.profile.impl.DefaultUnresolvedProfile
import java.util.Collections
import java.util.UUID


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