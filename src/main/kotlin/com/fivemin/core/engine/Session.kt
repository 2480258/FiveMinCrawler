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

import arrow.core.*
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.transaction.prepareRequest.TaskDetachedException
import kotlinx.coroutines.*

suspend fun <T> SessionStartedState.ifDetachable(func: suspend (SessionDetachableStartedState) -> T): Option<T> {
    return if (this is SessionDetachableStartedState) {
        Some(func(this))
    } else {
        none()
    }
}

/**
 * Saves session related data.
 */
data class SessionData constructor(
    val KeyRepo: UniqueKeyRepository,
    val SessionRepo: SessionRepository
) {
    val tokens = mutableMapOf<UniqueKey, UniqueKeyToken>()
}

enum class ProgressState {
    STARTED, FINISHED
}

enum class DetachableState {
    NOTMODIFIED, WANT, HATE
}

interface SessionState {
    val info: SessionInfo
    val data: SessionData
}

interface SessionAddableAlias : SessionMarkDetachable {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }
    
    /**
     * Add alias of request.
     * Can throw if key is duplicated.
     */
    fun addAlias(key: UniqueKey) {
        logger.debug(info.token.tokenNumber.toString() + " < Adding alias [" + key.toString() + "]")
        
        data.tokens[key].toOption().fold({
            addAliasInternal(key)
        }, {
            it.addDuplicationCountThrows()
        })
    }
    
    private fun addAliasInternal(key: UniqueKey) {
        val token = when (isDetachable) {
            DetachableState.WANT -> data.KeyRepo.addUniqueKeyWithDetachableThrows(key)
            DetachableState.HATE -> data.KeyRepo.addUniqueKeyWithNotDetachableThrows(key)
            DetachableState.NOTMODIFIED -> data.KeyRepo.addUniqueKey(key)
        }
    
        data.tokens[key] = token
    }
}

interface SessionMarkDetachable : SessionState {
    fun setDetachable() {
        info.setDetachable(data.tokens.values.asIterable())
    }

    fun setNonDetachable() {
        info.setNonDetachable(data.tokens.values.asIterable())
    }

    val isDetachable: DetachableState
        get() {
            return info.isDetachable
        }
}

interface SessionRetryable : SessionState {
    companion object {
        private val logger = LoggerController.getLogger("SessionRetryable")
    }
    
    suspend fun <T> retryAsync(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {
        logger.info(this.info.token.tokenNumber.toString() + " < retrying")
        val session = this as? SessionDetachable
        
        val state = session.rightIfNotNull { }
            .fold({ SessionDetachableInitStateImpl(info, data) }, { SessionInitStateImpl(info, data) })
    
        return func(state)
    }
}

interface SessionDetachable : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }
    
    /**
     * Detaches and allows task to run from another thread(Global scope).
     * Detach actions are not restrained by structured concurrency mechanism.
     *
     * If detaches, returns Either.Left<TaskDetachedException>
     */
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun detach(func: suspend (SessionInitState) -> Option<Throwable>): Deferred<Option<Throwable>> {
        val detached = data.SessionRepo.create(info.token.toOption())
        
        GlobalScope.launch {
            logger.info(info.token.tokenNumber.toString() + " < detached")
            func(SessionInitStateImpl(detached, data))
            logger.debug(info.token.tokenNumber.toString() + " < detach job finished")
        }

        return coroutineScope {
            async {
                TaskDetachedException().toOption()
            }
        }
    }
}

interface SessionStartable : SessionAddableAlias {

    companion object {
        private val logger = LoggerController.getLogger("SessionStartable")
    }
    
    /**
     * Start session.
     * Note that session allowed starting only once except retry.
     */
    suspend fun <T> start(
        key: UniqueKey,
        func: suspend (SessionStartedState) -> Deferred<Either<Throwable, T>>
    ): Deferred<Either<Throwable, T>> {

        return coroutineScope {
            async {
                info.doRegisteredTask {
                    Either.catch {
                        addAlias(key)
                    }.map {
                        logger.debug(key.toString() + " < creating SessionStartable")

                        val state = if (this@SessionStartable as? SessionDetachable != null) {
                            SessionDetachableStartedStateImpl(info, data)
                        } else {
                            SessionStartedStateImpl(info, data)
                        }

                        val result = func(state).await()
                        result
                    }.flatten()
                }
            }
        }
    }
}

interface SessionDetachRetryable : SessionState

interface SessionChildGeneratable : SessionState {
    companion object {
        private val logger = LoggerController.getLogger("SessionChildGeneratable")
    }
    
    /**
     * Creates child session
     * child session is not detachable if parent is not.
     */
    suspend fun <T> getChildSession(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {
        
        val detached = data.SessionRepo.create(info.parent)
        logger.debug(info.token.tokenNumber.toString() + " < creating child session")
    
        return func(SessionDetachableInitStateImpl(detached, data))
    }
}

interface SessionInitState : SessionStartable

interface SessionDetachableInitState : SessionDetachable, SessionInitState

interface SessionStartedState : SessionRetryable, SessionChildGeneratable, SessionAddableAlias, SessionMarkDetachable

interface SessionDetachableStartedState : SessionStartedState, SessionDetachable, SessionDetachRetryable

data class SessionInitStateImpl constructor(override val info: SessionInfo, override val data: SessionData) :
    SessionInitState

data class SessionStartedStateImpl constructor(override val info: SessionInfo, override val data: SessionData) :
    SessionStartedState

data class SessionDetachableInitStateImpl constructor(override val info: SessionInfo, override val data: SessionData) :
    SessionDetachableInitState

data class SessionDetachableStartedStateImpl constructor(
    override val info: SessionInfo,
    override val data: SessionData
) : SessionDetachableStartedState
