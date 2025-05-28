package dev.dani.velar.api.platform.log

import dev.dani.velar.api.platform.log.impl.PlatformLoggerJul
import dev.dani.velar.api.platform.log.impl.PlatformLoggerNOP
import java.util.logging.Logger


/*
 * Project: velar
 * Created at: 18/05/2025 20:28
 * Created by: Dani-error
 */
interface PlatformLogger {

    fun info(message: String)
    fun warning(message: String)
    fun error(message: String)
    fun error(message: String, exception: Throwable?)

    companion object {

        fun nop(): PlatformLogger = PlatformLoggerNOP

        fun fromJul(delegate: Logger) = PlatformLoggerJul(delegate)

    }

}