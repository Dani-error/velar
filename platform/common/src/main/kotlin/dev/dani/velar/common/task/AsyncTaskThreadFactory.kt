package dev.dani.velar.common.task

import java.util.*
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong


/*
 * Project: velar
 * Created at: 25/05/2025 19:08
 * Created by: Dani-error
 */
internal class AsyncTaskThreadFactory(private val threadNameFormat: String) : ThreadFactory {

    private val parentThreadGroup: ThreadGroup = Thread.currentThread().threadGroup

    private val createdThreadCount = AtomicLong(0)

    override fun newThread(runnable: Runnable): Thread {

        // construct the name thread
        val threadName = String.format(this.threadNameFormat, createdThreadCount.incrementAndGet())
        val thread = Thread(this.parentThreadGroup, runnable, threadName, 0)


        // set up the thread configuration
        thread.isDaemon = true
        thread.priority = Thread.NORM_PRIORITY
        return thread
    }

    companion object {

        fun create(threadNameFormat: String): ThreadFactory =
            AsyncTaskThreadFactory(threadNameFormat)

    }

}