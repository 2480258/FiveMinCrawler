/*
 *
 *     FiveMinCrawler
 *     Copyright (C) 2022  2480258
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
    
    suspend fun runAsync(act: suspend () -> Unit): Deferred<T> {
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
