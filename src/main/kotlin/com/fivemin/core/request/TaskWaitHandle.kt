package com.fivemin.core.request

import com.fivemin.core.LoggerController
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

class TaskWaitHandle<T> {
    private var result: T? = null
    private val semaphore = Semaphore(1)

    companion object {
        private val logger = LoggerController.getLogger("TaskWaitHandle")
    }

    suspend fun run(act: () -> Unit): Deferred<T> {
        return coroutineScope {
            async {
                semaphore.acquire()

                try {
                    act()
                } finally {
                    semaphore.acquire()
                }

                result!!
            }
        }
    }

    fun registerResult(_result: T) {
        logger.debug("registerResult: " + semaphore.hashCode())

        try {
            result = _result
        } finally {
            semaphore.release()
        }
    }
}
