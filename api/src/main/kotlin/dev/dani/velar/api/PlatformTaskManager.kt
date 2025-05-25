package dev.dani.velar.api


/*
 * Project: velar
 * Created at: 25/05/2025 17:16
 * Created by: Dani-error
 */
interface PlatformTaskManager {

    fun scheduleSync(task: Runnable)

    fun scheduleDelayedSync(delayTicks: Int, task: Runnable)

    fun scheduleAsync(task: Runnable)

    fun scheduleDelayedAsync(delayTicks: Int, task: Runnable)

}