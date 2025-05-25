@file:Suppress("UNCHECKED_CAST")

package dev.dani.velar.api.event

import kotlin.jvm.Throws


/*
 * Project: velar
 * Created at: 25/05/2025 17:36
 * Created by: Dani-error
 */
internal class EventExceptionHandler {

    private constructor() {
        throw UnsupportedOperationException()
    }

    companion object {


        fun rethrowFatalException(throwable: Throwable) {
            if (isFatal(throwable)) {
                throwUnchecked<RuntimeException>(throwable)
            }
        }

        private fun isFatal(throwable: Throwable): Boolean {
            // this includes the most fatal errors that can occur on a thread which we should not silently ignore and rethrow
            return throwable is InterruptedException
                    || throwable is LinkageError
                    || throwable is ThreadDeath
                    || throwable is VirtualMachineError
        }

        @Throws(Throwable::class)
        private fun <T : Throwable> throwUnchecked(throwable: Throwable) {
            throw throwable as T
        }

    }

}