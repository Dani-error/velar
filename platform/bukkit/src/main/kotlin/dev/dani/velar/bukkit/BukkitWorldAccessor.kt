package dev.dani.velar.bukkit

import dev.dani.velar.api.platform.PlatformWorldAccessor
import io.papermc.lib.PaperLib
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.World


/*
 * Project: velar
 * Created at: 25/05/2025 20:36
 * Created by: Dani-error
 */
object BukkitWorldAccessor {

    // check if we are on paper and newer (or equal) to 1.16.5
    fun worldAccessor(): PlatformWorldAccessor<World> = if (PaperLib.isPaper() && PaperLib.isVersion(16, 5)) {
        ModernAccessor
    } else {
        LegacyAccessor
    }

    fun nameBasedAccessor(): PlatformWorldAccessor<World> = LegacyAccessor

    fun keyBasedAccessor(): PlatformWorldAccessor<World> = ModernAccessor

    internal object ModernAccessor : PlatformWorldAccessor<World> {

        override fun extractWorldIdentifier(world: World): String =
            world.key.toString()

        override fun resolveWorldFromIdentifier(identifier: String): World? {
            val key = NamespacedKey.fromString(identifier)
            return if (key == null) null else Bukkit.getWorld(key)
        }

    }

    internal object LegacyAccessor : PlatformWorldAccessor<World> {

        override fun extractWorldIdentifier(world: World): String =
            world.name

        override fun resolveWorldFromIdentifier(identifier: String): World? =
            Bukkit.getWorld(identifier)

    }

}