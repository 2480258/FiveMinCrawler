package fivemin.core.engine.transaction.finalizeRequest

import arrow.core.*
import fivemin.core.LoggerController
import fivemin.core.engine.*
import fivemin.core.engine.transaction.TransactionSubPolicy
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import mu.KotlinLogging

class RetrySubPolicy<Document : Request> :
    TransactionSubPolicy<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>, Document> {
    
    private val RETRY_DELAY = 3000L
    
    companion object {
        private val logger = LoggerController.getLogger("RetrySubPolicy")
    }
    
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
                    if (it.responseBody is CriticalErrorBody || it.responseBody is RecoverableErrorBody) {
                        request(source, info, state).await()
                    } else {
                        dest.right()
                    }
                })
            }
        }
        
    }
    
    private suspend fun request(
        source: PrepareTransaction<Document>,
        info: TaskInfo,
        state: SessionStartedState
    ): Deferred<Either<Throwable, FinalizeRequestTransaction<Document>>> {
        logger.debug(source.request, "trying to retry")
        
        return state.retryAsync {
            delay(RETRY_DELAY)
            
            info.createTask<Document>()
                .get1<PrepareTransaction<Document>, FinalizeRequestTransaction<Document>>(source.request.documentType)
                .start(source, info, it)
        }
    }
}