package dev.dani.velar.bukkit

import dev.dani.velar.api.PlatformTaskManager
import org.bukkit.plugin.Plugin


/*
 * Project: velar
 * Created at: 25/05/2025 20:40
 * Created by: Dani-error
 */
class BukkitPlatformTaskManager(private val plugin: Plugin): PlatformTaskManager {

    override fun scheduleSync(task: Runnable) {
        this.plugin.server.scheduler.runTask(this.plugin, task)
    }

    override fun scheduleDelayedSync(delayTicks: Int, task: Runnable) {
        this.plugin.server.scheduler.runTaskLater(this.plugin, task, delayTicks.toLong())
    }

    override fun scheduleAsync(task: Runnable) {
        this.plugin.server.scheduler.runTaskAsynchronously(this.plugin, task)
    }

    override fun scheduleDelayedAsync(delayTicks: Int, task: Runnable) {
        this.plugin.server.scheduler.runTaskLaterAsynchronously(this.plugin, task, delayTicks.toLong())
    }

    companion object {

        fun taskManager(plugin: Plugin): PlatformTaskManager =
            BukkitPlatformTaskManager(plugin)

    }

}