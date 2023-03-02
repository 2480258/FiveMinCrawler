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
import com.fivemin.core.TaskDetachedException
import com.fivemin.core.logger.Log
import com.fivemin.core.logger.LogLevel
import kotlinx.coroutines.*
import java.net.URI

suspend fun <T> SessionStartedState.ifDetachable(func: suspend (SessionDetachableStartedState) -> T): Option<T> {
    return if (this is SessionDetachableStartedState) {
        Some(func(this))
    } else {
        none()
    }
}

class LocalUniqueKeyTokenRepo {
    private val tokens = mutableMapOf<UniqueKey, UniqueKeyToken>()
    val tokenValues: Iterable<UniqueKeyToken>
        get() {
            return tokens.values.asIterable()
        }
    
    fun add(key: UniqueKey, token: UniqueKeyToken) {
        if (!tokens.contains(key)) {
            tokens[key] = token
        } else {
            throw IllegalAccessException("tried to add key already exists. key: ${key}, token: ${token.tokenNumber}")
        }
    }
    
    fun update(key: UniqueKey) {
        if (tokens.contains(key)) {
            tokens[key]!!.addDuplicationCountThrows(key)
        } else {
            throw IllegalAccessException("tried to update key not exists. key: $key")
        }
    }
    
    fun contains(key: UniqueKey): Boolean {
        return tokens.contains(key)
    }
}

data class SessionContext constructor(
    val localTokenRepo: LocalUniqueKeyTokenRepo, val parent: Option<SessionToken>
)

/**
 * Saves session related data.
 */
data class SessionData constructor(
    val KeyRepo: UniqueKeyRepository, val SessionRepo: SessionRepository
)

enum class ProgressState {
    STARTED, FINISHED
}

enum class DetachableState {
    NOTMODIFIED, WANT, HATE
}

interface SessionState {
    val info: SessionInfo
    val data: SessionData
    val context: SessionContext
    val taskInfo: TaskInfo
}

interface SessionAddableAlias : SessionMarkDetachable {
    
    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }
    
    /**
     * Add alias of request.
     * Can throw if key is duplicated more than max retry.
     */
    
    suspend fun <T> addAlias(
        key: UniqueKey, func: suspend () -> Either<Throwable, T>
    ): Either<Throwable, T> {
        if (!context.localTokenRepo.contains(key)) {
            val token = addAliasInternal(key)
            context.localTokenRepo.add(key, token)
        } else {
            context.localTokenRepo.update(key)
        }
        
        val ret = func()
        
        ret.map {
            data.KeyRepo.finalizeUniqueKey(key)
        }
        
        return ret
    }
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "Adding alias",
        afterReturningMessage = "alias job finished",
        afterThrowingMessage = "failed to add alias"
    )
    private fun addAliasInternal(key: UniqueKey): UniqueKeyToken {
        return when (isDetachable) {
            DetachableState.WANT -> data.KeyRepo.addUniqueKeyWithDetachableThrows(key)
            DetachableState.HATE -> data.KeyRepo.addUniqueKeyWithNotDetachableThrows(key)
            DetachableState.NOTMODIFIED -> data.KeyRepo.addUniqueKey(key)
        }
    }
}

interface SessionMarkDetachable : SessionState {
    fun setDetachable() {
        info.setDetachable(context.localTokenRepo.tokenValues)
    }
    
    fun setNonDetachable() {
        info.setNonDetachable(context.localTokenRepo.tokenValues)
    }
    
    val isDetachable: DetachableState
        get() {
            return info.isDetachable
        }
}

interface SessionRetryable : SessionState {
    
    
    suspend fun <T> retryAsync(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {
        val session = this as? SessionDetachable
        val state = getState(session)
        
        return func(state)
    }
    @Log(
        beforeLogLevel = LogLevel.WARN,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "RETRYING",
        afterReturningMessage = "retry job finished",
        afterThrowingMessage = "failed to retry"
    )
    private fun getState(session: SessionDetachable?) =
        session.rightIfNotNull { }.fold({ SessionDetachableInitStateImpl(info, data, context, taskInfo) },
            { SessionInitStateImpl(info, data, context, taskInfo) })
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
            logger.info(info.token.tokenNumber.toString() + " < detached: " + detached.token.tokenNumber)
            func(SessionInitStateImpl(detached, data, context, taskInfo))
            logger.debug(info.token.tokenNumber.toString() + " < detach job finished" + detached.token.tokenNumber)
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
        key: UniqueKey, func: suspend (SessionStartedState) -> Either<Throwable, T>
    ): Deferred<Either<Throwable, T>> {
        
        return info.doRegisteredTask {
            Either.catch {
                addAlias(key) {
                    val state = getState()
    
                    func(state)
                }
            }.flatten()
        }
    }
    
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "STARTING session",
        afterReturningMessage = "session finished",
        afterThrowingMessage = "failed to finish session"
    )
    private fun getState(): SessionStartedState {
        val state = if (this@SessionStartable as? SessionDetachable != null) {
            SessionDetachableStartedStateImpl(info, data, context, taskInfo)
        } else {
            SessionStartedStateImpl(info, data, context, taskInfo)
        }
        return state
    }
    
    suspend fun <T> createUniqueKeyAndStart(
        request: Request, func: suspend (SessionStartedState) -> Either<Throwable, T>
    ): Deferred<Either<Throwable, T>> {
        val key = taskInfo.uniqueKeyProvider.documentKey.create(request)
        return start(key, func)
    }
}

interface SessionDetachRetryable : SessionState

interface SessionChildGeneratable : SessionState {
    companion object {
        private val logger = LoggerController.getLogger("SessionChildGeneratable")
    }
    
    /**
     * Creates child session
     */
    
    suspend fun <T> getChildSession(func: suspend (SessionInitState) -> Deferred<Either<Throwable, T>>): Deferred<Either<Throwable, T>> {
        
        val detached = data.SessionRepo.create(context.parent)
        
        return func(
            getState(detached)
        )
    }
    
    private fun getState(detached: SessionInfo) = SessionDetachableInitStateImpl(
        detached, data, SessionContext(LocalUniqueKeyTokenRepo(), info.token.toOption()), taskInfo
    )
}

interface SessionInitState : SessionStartable

interface SessionDetachableInitState : SessionDetachable, SessionInitState

interface SessionStartedState : SessionRetryable, SessionChildGeneratable, SessionAddableAlias, SessionMarkDetachable {
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "REQUEST downloading links",
        afterReturningMessage = "download finished",
        afterThrowingMessage = "failed to finish download"
    )
    fun downloadLinksWithCrawlerRequestVerbose(
        targetUri: URI,
        token: RequestToken,
        parentUri: URI,
        customTags: List<Tag> = listOf(),
        customRequestHeaderProfile: PerRequestHeaderProfile? = null,
        customOption: InitialOption = InitialOption()
    ): Deferred<Either<Throwable, ExportTransaction<HttpRequest>>> {
        return downloadLinksWithCrawlerRequest(
            customOption,
            HttpRequestImpl(
                Some(token),
                targetUri,
                RequestType.LINK,
                PerRequestHeaderProfile(none(), none(), Some(parentUri), targetUri),
                TagRepositoryImpl(Some(customTags))
            )
        )
    }
    

    fun downloadLinksWithCrawlerRequest(
        option: InitialOption,
        request: HttpRequest
    ): Deferred<Either<Throwable, ExportTransaction<HttpRequest>>> {
        
        
        val result = GlobalScope.async { // it might be bad.... for now, this may be best
    
            if (request.requestType != RequestType.LINK) {
                throw IllegalArgumentException("request type didn't match with called method")
            }
            
            val task = taskInfo.createTask<HttpRequest>()
                .get4<InitialTransaction<HttpRequest>, PrepareTransaction<HttpRequest>, FinalizeRequestTransaction<HttpRequest>, SerializeTransaction<HttpRequest>, ExportTransaction<HttpRequest>>(
                    DocumentType.NATIVE_HTTP
                )
            
            val ret = getChildSession {
                task.start(InitialTransactionImpl(option, TagRepositoryImpl(), request), it)
            }
            
            try {
                ret.await()
            } catch (e: TaskDetachedException) {
                e.left()
            }
            
        }
        
        return result
    }
    @Log(
        beforeLogLevel = LogLevel.DEBUG,
        afterReturningLogLevel = LogLevel.DEBUG,
        afterThrowingLogLevel = LogLevel.ERROR,
        beforeMessage = "REQUEST downloading attributes",
        afterReturningMessage = "download finished",
        afterThrowingMessage = "failed to finish download"
    )
    fun downloadAttributesWithCrawlerRequestVerbose(
        targetUri: URI,
        token: RequestToken,
        parentUri: URI,
        customTags: List<Tag> = listOf(),
        customRequestHeaderProfile: PerRequestHeaderProfile? = null,
        customOption: InitialOption = InitialOption()
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<HttpRequest>>> {
        return downloadAttributeWithCrawlerRequest(
            customOption,
            HttpRequestImpl(
                Some(token),
                targetUri,
                RequestType.ATTRIBUTE,
                PerRequestHeaderProfile(none(), none(), Some(parentUri), targetUri),
                TagRepositoryImpl(Some(customTags))
            )
        )
    }
    

    fun downloadAttributeWithCrawlerRequest(
        option: InitialOption,
        request: HttpRequest
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<HttpRequest>>> {
        val result = GlobalScope.async { // it might be bad.... for now, this may be best
    
            if (request.requestType != RequestType.ATTRIBUTE) {
                throw IllegalArgumentException("request type didn't match with called method")
            }
    
            val task = taskInfo.createTask<HttpRequest>()
                .get2<InitialTransaction<HttpRequest>, PrepareTransaction<HttpRequest>, FinalizeRequestTransaction<HttpRequest>>(
                    DocumentType.NATIVE_HTTP
                )
            
            val ret = getChildSession {
                task.start(
                    InitialTransactionImpl(option, TagRepositoryImpl(), request), it
                )
            }
            
            ret.await() // no detached exception handling. attributes never detach
        }
        
        return result
    }
}

interface SessionDetachableStartedState : SessionStartedState, SessionDetachable, SessionDetachRetryable

data class SessionInitStateImpl constructor(
    override val info: SessionInfo, override val data: SessionData, override val context: SessionContext,
    override val taskInfo: TaskInfo
) : SessionInitState

data class SessionStartedStateImpl constructor(
    override val info: SessionInfo, override val data: SessionData, override val context: SessionContext,
    override val taskInfo: TaskInfo
) : SessionStartedState

data class SessionDetachableInitStateImpl constructor(
    override val info: SessionInfo, override val data: SessionData, override val context: SessionContext,
    override val taskInfo: TaskInfo
) : SessionDetachableInitState

data class SessionDetachableStartedStateImpl constructor(
    override val info: SessionInfo, override val data: SessionData, override val context: SessionContext,
    override val taskInfo: TaskInfo
) : SessionDetachableStartedState
