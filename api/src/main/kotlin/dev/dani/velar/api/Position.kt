package dev.dani.velar.api


/*
 * Project: velar
 * Created at: 18/05/2025 21:43
 * Created by: Dani-error
 */
interface Position {

    val x: Double
    val y: Double
    val z: Double

    val yaw: Float
    val pitch: Float

    val worldId: String

    val blockX: Int
    val blockY: Int
    val blockZ: Int

    val chunkX: Int
    val chunkY: Int
    val chunkZ: Int

    companion object {

        fun position(x: Double, y: Double, z: Double, worldId: String): Position =
            position(x, y, z, 0f, 0f, worldId)

        fun position(x: Double, y: Double, z: Double, yaw: Float, pitch: Float, worldId: String): Position =
            DefaultPosition(x, y, z, yaw, pitch, worldId)

    }
}