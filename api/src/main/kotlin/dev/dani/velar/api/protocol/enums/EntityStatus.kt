package dev.dani.velar.api.protocol.enums


/*
 * Project: velar
 * Created at: 25/05/2025 18:04
 * Created by: Dani-error
 */
enum class EntityStatus(val bitmask: Byte) {
    ON_FIRE(0x01),
    CROUCHED(0x02),
    SPRINTING(0x08),
    EATING_DRINKING_BLOCKING(0x10),
    SWIMMING(0x10),
    INVISIBLE(0x20),
    GLOWING(0x40),
    FLYING_WITH_ELYTRA(0x80.toByte())
}