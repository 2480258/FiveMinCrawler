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
import kotlinx.coroutines.*

class TaskWaitHandle<T> {
    private var result = CompletableDeferred<T>()
    
    companion object {
        private val logger = LoggerController.getLogger("TaskWaitHandle")
    }
    
    suspend fun runAsync(act: suspend () -> Unit, onCancel: suspend () -> Unit): Deferred<T> {
        val job = GlobalScope.launch {
            try {
                act()
            } catch (e: Exception) {
                result.completeExceptionally(e)
            }
        }
        
        result.invokeOnCompletion { e ->
            whenCancel(e, onCancel)
            job.cancel()
        }
        
        return result
    }
    
    private fun whenCancel(e: Throwable?, onCancel: suspend () -> Unit) {
        when (e) {
            null -> {
                return
            }
            is CancellationException -> {
                runBlocking {
                    onCancel()
                }
                
                logger.debug("${this.hashCode()} < exiting with cancel")
                result.completeExceptionally(e)
            }
            else -> {
                logger.debug("${this.hashCode()} < exiting with error: ${e}")
                result.completeExceptionally(e)
            }
        }
    }
    
    fun registerResult(_result: T) {
        logger.debug("registerResult: " + this.hashCode())
        result!!.complete(_result)
    }
    
    /**
     * Force finish if not completed yet.
     * */
    fun forceFinishIfNot() {
        if(result.isActive) {
            result!!.completeExceptionally(IllegalStateException("handle was forcefully finished"))
        }
    }
}
