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
import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import arrow.core.continuations.either
import com.fivemin.core.LoggerController
import com.fivemin.core.TaskDetachedException
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class TaskError constructor(val Error: Either<TaskCanceledException, Exception>)

class TaskCanceledException : Exception()

class CrawlerTask1<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, D1 : Request, D2 : Request>
constructor(private val policy: TransactionPolicy<S1, S2, D1, D2>) {
    
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask1")
    }
    
    suspend fun start(trans: S1, session: SessionInitState): Deferred<Either<Throwable, S2>> {
        
        return session.start(session.taskInfo.uniqueKeyProvider.documentKey.create(trans.request)) { it ->
            try {
                logger.debug(trans.request.getDebugInfo() + " < starting task")
                val result = policy.progressAsync(trans, it, ::identity)
                
                result.swap().map {
                    if (it is TaskDetachedException) {
                    } else {
                        logger.warn(trans.request, "got task error", it.toOption())
                    }
                }
                
                result
                
            } catch (e: Exception) {
                logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                e.left()
            }
        }
    }
}

class CrawlerTask2<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, S3 : StrictTransaction<S2, D3>, D1 : Request, D2 : Request, D3 : Request>
constructor(
    private val policy1: TransactionPolicy<S1, S2, D1, D2>, private val policy2: TransactionPolicy<S2, S3, D2, D3>
) {
    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask2")
    }
    
    suspend fun start(trans: S1, session: SessionInitState): Deferred<Either<Throwable, S3>> {
        
        return session.start(session.taskInfo.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
            try {
                logger.debug(trans.request.getDebugInfo() + " < starting task")
                // @formatter:off
                val p2 = { it: S2 -> suspend { policy2.progressAsync(it, state, ::identity) } }
                val p1 = { it: S1, next: suspend (Either<Throwable, S2>) -> Either<Throwable, S3> -> suspend { policy1.progressAsync( it, state, next ) } }.curried()
                // @formatter:on
                
                val result = either<Throwable, Either<Throwable, S3>> {
                    val a1 = p1(trans)
                    a1 { it ->
                        p2(it.bind())()
                    }()
                }.flatten()
                
                
                result.swap().map {
                    if (it is TaskDetachedException) {
                    } else {
                        logger.warn(trans.request, "got task error", it.toOption())
                    }
                }
                
                result
            } catch (e: Exception) {
                
                logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                
                e.left()
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
    
    suspend fun start(trans: S1, session: SessionInitState): Deferred<Either<Throwable, S4>> {
        
        return session.start(session.taskInfo.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
            try {
                logger.debug(trans.request.getDebugInfo() + " < starting task")
                // @formatter:off
                val p3 = { it: S3 -> suspend { policy3.progressAsync(it, state, ::identity) } }
                val p2 = { it: S2, next: suspend (Either<Throwable, S3>) -> Either<Throwable, S4> -> suspend { policy2.progressAsync( it, state, next ) } }.curried()
                val p1 = { it: S1, next: suspend (Either<Throwable, S2>) -> Either<Throwable, S4> -> suspend { policy1.progressAsync( it, state, next ) } }.curried()
                // @formatter:on
                
                val result = either<Throwable, Either<Throwable, S4>> {
                    val a1 = p1(trans)
                    a1 { it ->
                        val a2 = p2(it.bind())
                        a2 {
                            p3(it.bind())()
                        }()
                    }()
                }.flatten()
                
                result.swap().map {
                    if (it is TaskDetachedException) {
                    } else {
                        logger.warn(trans.request, "got task error", it.toOption())
                    }
                }
                
                result
                
            } catch (e: Exception) {
                logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                e.left()
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
    
    suspend fun start(trans: S1, session: SessionInitState): Deferred<Either<Throwable, S5>> {
        
        return session.start(session.taskInfo.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
            try {
                logger.debug(trans.request.getDebugInfo() + " < starting task")
                // @formatter:off
                val p4 = { it: S4 -> suspend { policy4.progressAsync(it, state, ::identity) } }
                val p3 = { it: S3, next: suspend (Either<Throwable, S4>) -> Either<Throwable, S5> -> suspend { policy3.progressAsync( it, state, next ) } }.curried()
                val p2 = { it: S2, next: suspend (Either<Throwable, S3>) -> Either<Throwable, S5> -> suspend { policy2.progressAsync( it, state, next ) } }.curried()
                val p1 = { it: S1, next: suspend (Either<Throwable, S2>) -> Either<Throwable, S5> -> suspend { policy1.progressAsync( it, state, next ) } }.curried()
                // @formatter:on
                
                val result = either<Throwable, Either<Throwable, S5>> {
                    val a1 = p1(trans)
                    a1 { it ->
                        val a2 = p2(it.bind())
                        a2 {
                            val a3 = p3(it.bind())
                            a3 {
                                p4(it.bind())()
                            }()
                        }()
                    }()
                }.flatten()
                
                
                result.swap().map {
                    if (it is TaskDetachedException) {
                    } else {
                        logger.warn(trans.request, "got task error", it.toOption())
                    }
                }
                
                result
                
            } catch (e: Exception) {
                logger.warn(trans.request, " < caught unhandled exception: ", e.toOption())
                
                e.left()
                
            }
        }
    }
}


typealias Cont<R, T> = (((T) -> R) -> suspend () -> R)

fun <R, A, B> callCC(f: ((A) -> Cont<R, B>) -> Cont<R, A>): Cont<R, A> {
    return { k: (A) -> R ->
        f { a: A ->
            val g: Cont<R, B> = {
                suspend { k(a) }
            }
            
            g
        }(k)
    }
}