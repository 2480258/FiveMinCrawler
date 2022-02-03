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

suspend fun <T> SessionInitState.ifDetachable(func: suspend (SessionDetachableInitState) -> T): Option<T> {
    return if (this is SessionDetachableInitState) {
        Some(func(this))
    } else {
        none()
    }
}

suspend fun <T> SessionStartedState.ifDetachable(func: suspend (SessionDetachableStartedState) -> T): Option<T> {
    return if (this is SessionDetachableStartedState) {
        Some(func(this))
    } else {
        none()
    }
}

data class SessionData constructor(
    val KeyRepo: UniqueKeyRepository,
    val SessionRepo: SessionRepository,
    val RetryCount: Int
)

enum class ProgressState {
    STARTED, FINISHED
}

enum class DetachableState {
    NOTMODIFIED, WANT, HATE
}

interface SessionState {
    val info: SessionInfo
    val Data: SessionData
}

interface SessionAddableAlias : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    fun addAlias(key: UniqueKey) {
        logger.debug(info.token.tokenNumber.toString() + " < Adding alias [" + key.toString() + "]")
        Data.KeyRepo.addAlias(info.token, key)
    }
}

interface SessionMarkDetachable : SessionState {
    fun setDetachable() {
        info.setDetachable()
    }

    fun setNonDetachable() {
        info.setNonDetachable()
    }

    val isDetachable: DetachableState
        get() {
            return info.isDetachable
        }
}

interface SessionRetryable : SessionState {
    companion object {
        private val logger = LoggerController.getLogger("SessionRetryable")

        val MaxRetryCount: Int = 3
    }

    suspend fun <T> retryAsync(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {
        if (Data.RetryCount >= MaxRetryCount) {
            return coroutineScope {
                async {
                    TaskCanceledException().left()
                }
            }
        }

        logger.info(this.info.token.tokenNumber.toString() + " < retrying")
        var st = this as? SessionDetachable

        var state = st.rightIfNotNull { }
            .fold({ SessionDetachableInitStateImpl(info, Data) }, { SessionInitStateImpl(info, Data) })

        var r = func(state)

        return r
    }
}

interface SessionDetachable : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun detach(func: suspend (SessionInitState) -> Option<Throwable>): Deferred<Option<Throwable>> {
        var detached = Data.SessionRepo.create(info.token.toOption())
        Data.KeyRepo.transferOwnership(info.token, detached.token)

        GlobalScope.launch {
            logger.info(info.token.tokenNumber.toString() + " < detached")
            func(SessionInitStateImpl(detached, Data))
            logger.debug(info.token.tokenNumber.toString() + " < detach job finished")
        }

        return coroutineScope {
            async {
                TaskDetachedException().toOption()
            }
        }
    }
}

interface SessionStartable : SessionState {

    companion object {
        private val logger = LoggerController.getLogger("SessionStartable")
    }

    suspend fun <T> start(
        key: UniqueKey,
        func: suspend (SessionStartedState) -> Deferred<Either<Throwable, T>>
    ): Deferred<Either<Throwable, T>> {

        return coroutineScope {
            async {
                info.doRegisteredTask {
                    Either.catch {
                        Data.KeyRepo.addAlias(info.token, key)
                    }.map {
                        logger.debug(key.toString() + " < creating SessionStartable")

                        var state = if (this@SessionStartable as? SessionDetachable != null) {
                            SessionDetachableStartedStateImpl(info, Data)
                        } else {
                            SessionStartedStateImpl(info, Data)
                        }

                        var f = func(state).await()
                        f
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

    suspend fun <T> getChildSession(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {

        var detached = Data.SessionRepo.create(info.parent)
        logger.debug(info.token.tokenNumber.toString() + " < creating child session")

        var ret = func(SessionDetachableInitStateImpl(detached, Data))
        return ret
    }
}

interface SessionInitState : SessionStartable

interface SessionDetachableInitState : SessionDetachable, SessionInitState

interface SessionStartedState : SessionRetryable, SessionChildGeneratable, SessionAddableAlias, SessionMarkDetachable

interface SessionDetachableStartedState : SessionStartedState, SessionDetachable, SessionDetachRetryable

data class SessionInitStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionInitState

data class SessionStartedStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionStartedState

data class SessionDetachableInitStateImpl constructor(override val info: SessionInfo, override val Data: SessionData) :
    SessionDetachableInitState

data class SessionDetachableStartedStateImpl constructor(
    override val info: SessionInfo,
    override val Data: SessionData
) : SessionDetachableStartedState
