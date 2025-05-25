package dev.dani.velar.bukkit.util

import dev.dani.velar.api.NPC
import dev.dani.velar.api.Position
import dev.dani.velar.common.util.ClassHelper.classExists
import org.bukkit.Location
import org.bukkit.util.NumberConversions.square


/*
 * Project: velar
 * Created at: 25/05/2025 20:42
 * Created by: Dani-error
 */
object BukkitPlatformUtil {

    private val FOLIA: Boolean = classExists("io.papermc.paper.threadedregions.RegionizedServerInitEvent")

    fun runsOnFolia(): Boolean {
        return FOLIA
    }

    fun distance(npc: NPC<*, *, *, *>, location: Location): Double {
        val pos: Position = npc.position
        return square(location.x - pos.x) + square(location.y - pos.y) + square(location.z - pos.z)
    }

    fun positionFromBukkitLegacy(loc: Location): Position {
        return Position.position(
            loc.x,
            loc.y,
            loc.z,
            loc.yaw,
            loc.pitch,
            loc.world.name
        )
    }

    fun positionFromBukkitModern(loc: Location): Position {
        return Position.position(
            loc.x,
            loc.y,
            loc.z,
            loc.yaw,
            loc.pitch,
            loc.world.key.toString()
        )
    }

}