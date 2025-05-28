package dev.dani.velar.api.profile.resolver.impl

import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.resolver.ProfileResolver
import java.util.*
import java.util.concurrent.CompletableFuture


/*
 * Project: velar
 * Created at: 18/05/2025 20:51
 * Created by: Dani-error
 */
private const val ENTRY_KEEP_ALIVE_TIME = 3 * 60 * 60 * 1000L // 3h in ms

class ProfileResolverCached(private val delegate: ProfileResolver) : ProfileResolver.Cached {

    private val nameToUniqueIdCache = mutableMapOf<String, CacheEntry<UUID>>()
    private val uuidToProfileCache = mutableMapOf<UUID, CacheEntry<Profile.Resolved>>()

    override fun resolveProfile(profile: Profile): CompletableFuture<Profile.Resolved> {
        fromCache(profile)?.let { return CompletableFuture.completedFuture(it) }

        return delegate.resolveProfile(profile).whenComplete { resolved, exception ->
            if (exception == null && resolved != null) {
                nameToUniqueIdCache[resolved.name] =
                    CacheEntry(resolved.uniqueId, ENTRY_KEEP_ALIVE_TIME)
                uuidToProfileCache[resolved.uniqueId] =
                    CacheEntry(resolved, ENTRY_KEEP_ALIVE_TIME)
            }
        }
    }

    override fun fromCache(name: String): Profile.Resolved? {
        val uuid = nameToUniqueIdCache[name]?.takeIf { !it.expired }?.value
        return uuid?.let { fromCache(it) }
    }

    override fun fromCache(uniqueId: UUID): Profile.Resolved? {
        return uuidToProfileCache[uniqueId]?.takeIf { !it.expired }?.value
    }

    override fun fromCache(profile: Profile): Profile.Resolved? {
        return profile.uniqueId?.let { fromCache(it) }
            ?: profile.name?.let { fromCache(it) }
    }

    private data class CacheEntry<T>(
        val value: T,
        private val timeoutTime: Long = System.currentTimeMillis() + ENTRY_KEEP_ALIVE_TIME
    ) {
        val expired: Boolean get() = System.currentTimeMillis() > timeoutTime
    }
}