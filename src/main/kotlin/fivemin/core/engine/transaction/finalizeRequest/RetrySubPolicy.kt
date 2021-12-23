package fivemin.core.engine.transaction.finalizeRequest

import arrow.core.*
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

class RetrySubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {

    private val logger = KotlinLogging.logger {}

    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            async {
                dest.result.fold({

                    request(source, info, state).await()
                }, {

                    var recoverable = it.responseBody.ifRecoverableErrAsync({
                        logger.info {
                            source.request.getDebugInfo() + " < trying to retry because " + it.code
                        }
                        request(source, info, state).await()
                    }, {
                        dest.right()
                    })

                    recoverable
                })
            }
        }

    }

    private suspend fun request(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        return state.retryAsync {
            info.createTask<Document>()
                .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                .start(source, info, it)
        }
    }
}