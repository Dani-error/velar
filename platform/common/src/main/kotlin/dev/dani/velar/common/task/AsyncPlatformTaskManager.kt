package dev.dani.velar.common.task

import dev.dani.velar.api.PlatformTaskManager
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


/*
 * Project: velar
 * Created at: 25/05/2025 19:10
 * Created by: Dani-error
 */
open class AsyncPlatformTaskManager protected constructor(extensionId: String) : PlatformTaskManager {

    private val runOnceExecutorService: ExecutorService
    private val scheduledExecutorService: ScheduledExecutorService

    init {
        val runOnceThreadFactory = AsyncTaskThreadFactory.create("$extensionId Velar Task #%d")
        this.runOnceExecutorService = Executors.newCachedThreadPool(runOnceThreadFactory)
        val scheduledThreadFactory = AsyncTaskThreadFactory.create("$extensionId Velar Scheduled Task #%d")
        this.scheduledExecutorService = Executors.newScheduledThreadPool(0, scheduledThreadFactory)
    }

    override fun scheduleSync(task: Runnable) {
        runOnceExecutorService.execute(task)
    }

    override fun scheduleDelayedSync(delayTicks: Int, task: Runnable) {
        scheduledExecutorService.schedule(task, delayTicks * ONE_TICK_MS, TimeUnit.MILLISECONDS)
    }

    override fun scheduleAsync(task: Runnable) {
        runOnceExecutorService.execute(task)
    }

    override fun scheduleDelayedAsync(delayTicks: Int, task: Runnable) {
        scheduledExecutorService.schedule(task, delayTicks * ONE_TICK_MS, TimeUnit.MILLISECONDS)
    }

    companion object {

        private const val ONE_TICK_MS: Long = (1000 / 20).toLong()

        fun taskManager(extensionIdentifier: String): PlatformTaskManager =
            AsyncPlatformTaskManager(extensionIdentifier)

    }
}