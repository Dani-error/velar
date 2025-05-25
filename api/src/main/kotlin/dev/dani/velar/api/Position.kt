package dev.dani.velar.api

import dev.dani.velar.api.util.floor


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

internal data class DefaultPosition(
    override val x: Double,
    override val y: Double,
    override val z: Double,
    override val yaw: Float,
    override val pitch: Float,
    override val worldId: String
) : Position {

    override val blockX: Int
        get() = this.x.floor()
    override val blockY: Int
        get() = this.y.floor()
    override val blockZ: Int
        get() = this.z.floor()
    override val chunkX: Int
        get() = this.blockX shr 4
    override val chunkY: Int
        get() = this.blockY shr 8
    override val chunkZ: Int
        get() = this.blockZ shr 4

}
