package com.fivemin.core.engine.transaction.finalizeRequest

import arrow.core.Either
import com.fivemin.core.LoggerController
import com.fivemin.core.engine.*
import com.fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.*

class ResponseDisposeSubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {

    companion object {
        private val logger = LoggerController.getLogger("SessionDetachable")
    }

    override suspend fun process(
        source: PrepareTransaction<Document>,
        dest: FinalizeRequestTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {

        return coroutineScope {
            async {
                Either.catch {
                    dest.result.map {
                        logger.debug(source.request.getDebugInfo() + " < releasing requester")
                        it.releaseRequester()
                    }

                    dest
                }
            }
        }
    }
}
