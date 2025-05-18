package dev.dani.velar.api

import dev.dani.velar.api.util.floor


/*
 * Project: velar
 * Created at: 18/05/2025 21:45
 * Created by: Dani-error
 */
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
