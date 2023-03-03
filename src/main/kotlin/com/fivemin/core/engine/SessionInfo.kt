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

package com.fivemin.core.engine

import arrow.core.Option
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.session.RetryCountMaxedException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

const val MaxRetry = 3

data class UniqueKeyToken constructor(val tokenNumber: ULong) {
    /**
     * https://kotlinlang.org/docs/data-classes.html#properties-declared-in-the-class-body
     *
     * If there are explicit implementations of equals(), hashCode(), or toString()
     * in the data class body or final implementations in a superclass,
     * then these functions are not generated, and the existing implementations are used.
     */
    
    var retryCount = 0
    fun addDuplicationCountThrows(key: UniqueKey) {
        if(++retryCount >= MaxRetry) {
            throw RetryCountMaxedException("retry count maxed out with key: $key")
        }
    }
    
    override fun equals(other: Any?): Boolean {
        return if (other is UniqueKeyToken) {
            other.tokenNumber == tokenNumber
        } else {
            false
        }
    }
    
    override fun hashCode(): Int {
        //cast? TODO: Log this.
        return tokenNumber.toInt()
    }
    
    override fun toString(): String {
        return tokenNumber.toString()
    }
}

interface DetachObserver {
    fun notifyMarkedDetachable(tokens: Iterable<UniqueKeyToken>)
    
    fun notifyMarkedNotDetachable(tokens: Iterable<UniqueKeyToken>)
}

class SessionInfo
constructor(
    private val finish: FinishObserver,
    private val detach: DetachObserver
) {
    
    companion object {
        private val logger = LoggerController.getLogger("SessionInfo")
    }
    
    val token: SessionToken = SessionToken.create()
    val isDetachable: DetachableState
        get() {
            return detachable
        }
    private var detachable = DetachableState.NOTMODIFIED
    private var progress = ProgressState.STARTED
    
    private var reenterent = 0
    
    init {
        finish.onStart()
    }
    
    /**
     * Counts number of task.
     * Program won't finish until every registered task is done.
     */
    suspend fun <T> doRegisteredTask(func: suspend () -> T): Deferred<T> {
        return GlobalScope.async {
            try {
                reenterent++
                func()
            } finally {
                reenterent--
        
                if (reenterent == 0) {
                    setFinished()
                }
            }
        }
        
    }
    
    private fun setFinished() {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        progress = ProgressState.FINISHED
        finish.onFinish(token)
        
        if(isDetachable == DetachableState.NOTMODIFIED) {
            logger.warn("${token} < is not marked as nether detachable nor not detachable. this session information will not be saved in resume file")
        }
    }
    
    fun setDetachable(tokens: Iterable<UniqueKeyToken>) {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        detachable = DetachableState.WANT
        detach.notifyMarkedDetachable(tokens)
    }
    
    fun setNonDetachable(tokens: Iterable<UniqueKeyToken>) {
        if (progress != ProgressState.STARTED) {
            throw IllegalStateException()
        }
        
        detachable = DetachableState.HATE
        detach.notifyMarkedNotDetachable(tokens)
    }
}
