package com.fivemin.core.request

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore

class TaskWaitHandle<T> {
    private var result: T? = null
    private val semaphore = Semaphore(1)

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
        try {
            result = _result
        } finally {
            semaphore.release()
        }
    }
}
