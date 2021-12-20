package fivemin.core.engine

import kotlinx.coroutines.*
import arrow.core.Either
import arrow.core.Validated
import arrow.core.computations.either
import arrow.core.valid
import fivemin.core.LoggerController
import kotlinx.coroutines.selects.select

class TaskResult<out T> constructor(val Result: Either<TaskError, T>) {
}

class TaskError constructor(val Error: Either<TaskCanceledException, Exception>) {

}

class TaskCanceledException : Exception() {

}

class CrawlerTask1<S1 : Transaction<D1>, S2 : StrictTransaction<S1, D2>, D1 : Request, D2 : Request>
constructor(private val policy: TransactionPolicy<S1, S2, D1, D2>) {

    companion object {
        private val logger = LoggerController.getLogger("CrawlerTask1")
    }

    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Validated<Throwable, S2>> {
        try {
            logger.info(trans.request.getDebugInfo() + " < Starting task")

            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { it ->
                coroutineScope { policy.progressAsync(trans, info, it) }
            }
        } catch (e: Exception) {
            logger.info(trans.request.getDebugInfo() + " < Caught unhandled exception")

            throw e
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

    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Validated<Throwable, S3>> {
        try {
            logger.info(trans.request.getDebugInfo() + " < Starting task")

            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        either<Throwable, S3> {
                            var p1 = policy1.progressAsync(trans, info, state).await().bind()
                            var p2 = policy2.progressAsync(p1, info, state).await().bind()

                            p2
                        }.toValidated()
                    }
                }
            }
        } catch (e: Exception) {
            logger.info(trans.request.getDebugInfo() + " < Caught unhandled exception")

            throw e
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


    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Validated<Throwable, S4>> {
        try {
            logger.info(trans.request.getDebugInfo() + " < Starting task")

            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        either<Throwable, S4> {
                            var p1 = policy1.progressAsync(trans, info, state).await().bind()
                            var p2 = policy2.progressAsync(p1, info, state).await().bind()
                            var p3 = policy3.progressAsync(p2, info, state).await().bind()

                            p3
                        }.toValidated()
                    }
                }
            }
        } catch (e: Exception) {
            logger.info(trans.request.getDebugInfo() + " < Caught unhandled exception")

            throw e
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

    suspend fun start(trans: S1, info: TaskInfo, session: SessionInitState): Deferred<Validated<Throwable, S5>> {
        try {
            logger.info(trans.request.getDebugInfo() + " < Starting task")

            return session.start(info.uniqueKeyProvider.documentKey.create(trans.request)) { state ->
                coroutineScope {
                    async {
                        either<Throwable, S5> {
                            var p1 = policy1.progressAsync(trans, info, state).await().bind()
                            var p2 = policy2.progressAsync(p1, info, state).await().bind()
                            var p3 = policy3.progressAsync(p2, info, state).await().bind()
                            var p4 = policy4.progressAsync(p3, info, state).await().bind()

                            p4
                        }.toValidated()
                    }
                }
            }
        } catch (e: Exception) {
            logger.info(trans.request.getDebugInfo() + " < Caught unhandled exception")

            throw e
        }
    }
}