package fivemin.core.engine.transaction.finalizeRequest

import arrow.core.*
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class RetrySubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            async {
                dest.result.fold({

                    request(source, info, state).await()
                }, {

                    var recoverable = it.responseBody.ifRecoverableErrAsync({
                        request(source, info, state).await()
                    }, {
                        dest.valid()
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
    ): Deferred<Validated<Throwable, FinalizeRequestTransaction<Document>>> {
        return state.retryAsync {
            info.createTask<Document>()
                .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                .start(source, info, it)
        }
    }
}