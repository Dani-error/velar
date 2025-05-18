package dev.dani.velar.api.log.impl

import dev.dani.velar.api.log.PlatformLogger


/*
 * Project: velar
 * Created at: 18/05/2025 20:30
 * Created by: Dani-error
 */
object PlatformLoggerNOP : PlatformLogger {

    override fun info(message: String) { }

    override fun warning(message: String) { }

    override fun error(message: String) { }

    override fun error(message: String, exception: Throwable?) { }

}