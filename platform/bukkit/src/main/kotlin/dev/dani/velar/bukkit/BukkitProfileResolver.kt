package dev.dani.velar.bukkit

import com.destroystokyo.paper.profile.PlayerProfile
import dev.dani.velar.api.profile.Profile
import dev.dani.velar.api.profile.ProfileProperty
import dev.dani.velar.api.profile.resolver.ProfileResolver
import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors


/*
 * Project: velar
 * Created at: 25/05/2025 20:43
 * Created by: Dani-error
 */
object BukkitProfileResolver {

    fun profileResolver(): ProfileResolver {
        // check if we're on paper and newer than 1.12 (when the profile API was introduced)
        if (PaperLib.isPaper() && PaperLib.isVersion(12)) {
            return PaperProfileResolver
        }

        // check if we're on spigot and newer than 1.18.2 (when the profile API was introduced)
        if (PaperLib.isSpigot() && PaperLib.isVersion(18, 2)) {
            return SpigotProfileResolver
        }

        // use fallback resolver
        return LegacyResolver.INSTANCE
    }

    internal object PaperProfileResolver : ProfileResolver {
        override fun resolveProfile(profile: Profile): CompletableFuture<Profile.Resolved> {
            return CompletableFuture.supplyAsync {
                // create a profile from the given one and try to complete it
                val playerProfile: PlayerProfile = Bukkit.createProfile(profile.uniqueId, profile.name)
                playerProfile.complete(true, true)

                // convert the profile properties to the wrapper one
                val properties: Set<ProfileProperty> = playerProfile.properties
                    .stream()
                    .map { prop -> ProfileProperty.property(prop.name, prop.value, prop.signature) }
                    .collect(Collectors.toSet())

                // validate that the profile id is present - when resolving by name only the id will be missing
                // see below for further details
                val profileId: UUID? = playerProfile.id
                checkNotNull(profileId) { "Could not resolve profile uuid using paper resolver" }

                // in case the player is not online, the complete method will not actually fill in the profile details.
                // the documentation states it will be, but it is not - even the update() method (added from the bukkit
                // api in 1.18) will not do this on a paper profile.
                // to work around this we just insert a random generated name in this case.
                // see https://github.com/PaperMC/Paper/issues/8927
                var profileName: String? = playerProfile.name
                if (profileName == null) {
                    val randomId = UUID.randomUUID().toString()
                    profileName = randomId.replace("-", "").substring(0, 16)
                }

                Profile.resolved(profileName, profileId, properties)
            }
        }
    }

    internal object SpigotProfileResolver : ProfileResolver {

        private const val NIL_NAME: String = ""
        private val NIL_UUID: UUID = UUID(0, 0)

        private val convertNullToNilValues = nullToNilConversationNecessary()


        @Suppress("UNUSED_VARIABLE")
        private fun nullToNilConversationNecessary(): Boolean {
            try {
                val dummy = Bukkit.createPlayerProfile(UUID.randomUUID(), "dummy")
                val ignored: Method = dummy.javaClass.getDeclaredMethod("buildResolvableProfile")
                return true
            } catch (exception: NoSuchMethodException) {
                return false
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun resolveProfile(profile: Profile): CompletableFuture<Profile.Resolved> {
            val playerProfile: org.bukkit.profile.PlayerProfile
            if (this.convertNullToNilValues) {
                // need to replace null values with nil values, spigot half-changed
                // their handling of these values, so null doesn't properly work anymore
                val profileName = if (profile.name != null) profile.name else NIL_NAME
                val profileId = if (profile.uniqueId != null) profile.uniqueId else NIL_UUID
                playerProfile = Bukkit.createPlayerProfile(profileId, profileName)
            } else {
                // create a raw profile, everything will be wrapped for us
                playerProfile = Bukkit.createPlayerProfile(profile.uniqueId, profile.name)
            }

            return playerProfile.update().thenApplyAsync { resolvedProfile: org.bukkit.profile.PlayerProfile ->
                // validate that the profile was actually completed
                val profileId = resolvedProfile.uniqueId
                val profileName = resolvedProfile.name
                check(!(profileId == null || profileName == null)) { "Could not resolve profile using spigot resolver" }

                // hack to get the data from the profile as it's not exposed directly
                val props = resolvedProfile.serialize()["properties"] as List<Map<String, Any>>?
                    ?: // only present if the profile has any properties, in this case there are no properties
                    return@thenApplyAsync Profile.resolved(resolvedProfile.name!!, resolvedProfile.uniqueId!!)

                // extract all properties of the profile
                val properties: MutableSet<ProfileProperty> = HashSet()
                for (entry in props) {
                    val prop = ProfileProperty.property(
                        (entry["name"] as String?)!!,
                        (entry["value"] as String?)!!,
                        entry["signature"] as String?
                    )
                    properties.add(prop)
                }
                Profile.resolved(profileName, profileId, properties)
            }
        }
    }

    internal class LegacyResolver {

        companion object {
            internal val INSTANCE: ProfileResolver = ProfileResolver.caching(ProfileResolver.mojang())
        }

    }
}