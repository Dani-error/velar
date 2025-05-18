package dev.dani.velar.api.profile.resolver

import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.resolver.impl.DefaultProfileResolverCached
import dev.dani.velar.api.profile.resolver.impl.ProfileResolverMojang
import java.util.UUID
import java.util.concurrent.CompletableFuture


/*
 * Project: velar
 * Created at: 18/05/2025 20:42
 * Created by: Dani-error
 */
fun interface ProfileResolver {

    fun resolveProfile(profile: Profile): CompletableFuture<Profile.Resolved>

    companion object {

        fun mojang(): ProfileResolver = ProfileResolverMojang

        fun caching(delegate: ProfileResolver): Cached = DefaultProfileResolverCached(delegate)

    }

    interface Cached : ProfileResolver {

        fun fromCache(name: String): Profile.Resolved?
        fun fromCache(uniqueId: UUID): Profile.Resolved?
        fun fromCache(profile: Profile): Profile.Resolved?

    }

}