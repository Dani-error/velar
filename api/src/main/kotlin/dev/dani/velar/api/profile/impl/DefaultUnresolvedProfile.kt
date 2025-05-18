package dev.dani.velar.api.profile.impl

import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.util.safeEquals
import java.util.*


/*
 * Project: velar
 * Created at: 18/05/2025 21:38
 * Created by: Dani-error
 */
data class DefaultUnresolvedProfile(
    override val name: String?,
    override val uniqueId: UUID?
) : Profile.Unresolved {

    override fun hashCode(): Int = Objects.hash(name, uniqueId)

    override fun equals(other: Any?): Boolean = safeEquals<Profile.Unresolved>(this, other) { orig, comp ->
        Objects.equals(orig.name, comp.name) && Objects.equals(orig.uniqueId, comp.uniqueId)
    }
}
