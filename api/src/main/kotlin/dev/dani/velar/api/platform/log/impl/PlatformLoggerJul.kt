package dev.dani.velar.api.platform.log.impl

import dev.dani.velar.api.platform.log.PlatformLogger
import java.util.logging.Level
import java.util.logging.Logger


/*
 * Project: velar
 * Created at: 18/05/2025 20:30
 * Created by: Dani-error
 */
class PlatformLoggerJul(private val delegate: Logger) : PlatformLogger {

    override fun info(message: String) = delegate.info(message)

    override fun warning(message: String) = delegate.warning(message)

    override fun error(message: String) = delegate.severe(message)

    override fun error(message: String, exception: Throwable?) = delegate.log(Level.SEVERE, message, exception)

}