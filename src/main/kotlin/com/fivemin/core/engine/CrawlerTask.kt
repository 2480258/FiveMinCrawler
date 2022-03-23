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

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.toOption
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.transaction.prepareRequest.TaskDetachedException
import kotlinx.coroutines.*

class TaskError constructor(val Error: Either<TaskCanceledException, Exception>)

class TaskCanceledException : Exception()

class CrawlerTask1<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, D1 : Request, D2 : Request>
constructor(private val policy: TransactionPolicy<S1, S2, D1, D2>) {
    
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask1")
    }
    
    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Either<Throwable, S2>> {
        try {
            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { it ->
                logger.debug(trans.request.getDebugInfo() + " < starting task")
                coroutineScope {
                    async {
                        val result = either<Throwable, S2> {
                            val p1 = policy.progressAsync(trans, info, it).await().bind()
                            
                            p1
                        }
                        
                        result.swap().map {
                            if (it is TaskDetachedException) {
                            } else {
                                logger.warn(trans.request, "got task error", it.toOption())
                            }
                        }
                        
                        result
                    }
                }
            }
        } catch (e: Exception) {
            return coroutineScope {
                async {
                    logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                    
                    e.left()
                }
            }
        }
    }
}

class CrawlerTask2<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, S3 : StrictTransaction<S2, D3>, D1 : Request, D2 : Request, D3 : Request>
constructor(
    private val policy1: TransactionPolicy<S1, S2, D1, D2>,
    private val policy2: TransactionPolicy<S2, S3, D2, D3>
) {
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask2")
    }
    
    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Either<Throwable, S3>> {
        try {
            
            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        logger.debug(trans.request.getDebugInfo() + " < starting task")
                        val result = either<Throwable, S3> {
                            val p1 = policy1.progressAsync(trans, info, state).await().bind()
                            val p2 = policy2.progressAsync(p1, info, state).await().bind()
                            
                            p2
                        }
                        
                        result.swap().map {
                            if (it is TaskDetachedException) {
                            } else {
                                logger.warn(trans.request, "got task error", it.toOption())
                            }
                        }
                        
                        result
                    }
                }
            }
        } catch (e: Exception) {
            return coroutineScope {
                async {
                    logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                    
                    e.left()
                }
            }
        }
    }
}

class CrawlerTask3<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, S3 : StrictTransaction<S2, D3>, S4 : StrictTransaction<S3, D4>, D1 : Request, D2 : Request, D3 : Request, D4 : Request>
constructor(
    private val policy1: TransactionPolicy<S1, S2, D1, D2>,
    private val policy2: TransactionPolicy<S2, S3, D2, D3>,
    private val policy3: TransactionPolicy<S3, S4, D3, D4>
) {
    
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask3")
    }
    
    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Either<Throwable, S4>> {
        try {
            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        logger.debug(trans.request.getDebugInfo() + " < starting task")
                        
                        val result = either<Throwable, S4> {
                            val p1 = policy1.progressAsync(trans, info, state).await().bind()
                            val p2 = policy2.progressAsync(p1, info, state).await().bind()
                            val p3 = policy3.progressAsync(p2, info, state).await().bind()
                            
                            p3
                        }
                        
                        result.swap().map {
                            if (it is TaskDetachedException) {
                            } else {
                                logger.warn(trans.request, "got task error", it.toOption())
                            }
                        }
                        
                        result
                    }
                }
            }
        } catch (e: Exception) {
            return coroutineScope {
                async {
                    logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                    
                    e.left()
                }
            }
        }
    }
}

class CrawlerTask4<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, S3 : StrictTransaction<S2, D3>, S4 : StrictTransaction<S3, D4>, S5 : StrictTransaction<S4, D5>, D1 : Request, D2 : Request, D3 : Request, D4 : Request, D5 : Request>
constructor(
    private val policy1: TransactionPolicy<S1, S2, D1, D2>,
    private val policy2: TransactionPolicy<S2, S3, D2, D3>,
    private val policy3: TransactionPolicy<S3, S4, D3, D4>,
    private val policy4: TransactionPolicy<S4, S5, D4, D5>
) {
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask4")
    }
    
    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Either<Throwable, S5>> {
        try {
            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        logger.debug(trans.request.getDebugInfo() + " < starting task")
                        
                        val result = either<Throwable, S5> {
                            val p1 = policy1.progressAsync(trans, info, state).await().bind()
                            val p2 = policy2.progressAsync(p1, info, state).await().bind()
                            val p3 = policy3.progressAsync(p2, info, state).await().bind()
                            val p4 = policy4.progressAsync(p3, info, state).await().bind()
                            
                            p4
                        }
                        
                        result.swap().map {
                            if (it is TaskDetachedException) {
                            } else {
                                logger.warn(trans.request, "got task error", it.toOption())
                            }
                        }
                        
                        result
                    }
                }
            }
        } catch (e: Exception) {
            return coroutineScope {
                async {
                    logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                    
                    e.left()
                }
            }
        }
    }
}
