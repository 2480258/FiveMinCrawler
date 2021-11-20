package core.engine.transaction.finalizeRequest

import arrow.core.*
import core.engine.*
import core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import java.io.IOException

class RetrySubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Result<FinalizeRequestTransaction<Document>>> {
        return coroutineScope {
            async {
                var ret = dest.result.fold({ x ->
                    x.responseBody.ifRecoverableErr({ y ->
                        Some(request(source, info, state))
                    }, { y ->
                        none()
                    })
                }, { x ->
                    if (x is IOException) { //TODO
                        Some(request(source, info, state))
                    } else {
                        none()
                    }
                })

                Result.success(ret.fold({ dest }, { x ->
                    select {
                        x.onAwait
                    }
                }))
            }
        }

    }

    private suspend fun request(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Result<FinalizeRequestTransaction<Document>>> {
        return state.retryAsync {
            info.createTask<Document>()
                .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                .start(source, info, it)
        }
    }
}